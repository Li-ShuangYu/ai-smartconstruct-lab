"""
Workflow Orchestration Service — main async workflow for AI pipeline.

Implements the phased orchestration pipeline:
1. Supervisor analyzes phased canvas_json → list of AgentTasks
2. Parallel execution of all agent tasks via asyncio.gather
3. Per-node status callback after each node completes (success or failure)
4. Merge results into standard_payload_json
5. Extract DB assets (questions, knowledge_points, eval_indicators)
6. Send overall callback to Java backend

Includes:
- Per-task 60s timeout
- Overall workflow 5-minute timeout with cancellation
- Retry logic: 3 retries with exponential backoff (2s, 4s, 8s) per agent task
- Per-node status tracking and callback
- Edge case: no nodes need AI → immediate success callback
"""

import asyncio
import logging
from typing import Optional
from uuid import uuid4

from pydantic import BaseModel
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type

from agents.supervisor import detect_ai_nodes_phased
from agents.text_agent import TextAgent
from agents.struct_agent import StructAgent
from agents.exam_agent import ExamAgent
from agents.code_agent import CodeAgent
from agents.video_agent import VideoAgent
from models.schemas import AgentTask, AgentType
from models.callback import AiCallbackPayload, NodeAiStatusPayload
from services.llm_service import LLMService
from services.merger import merge_results_into_payload
from services.asset_extractor import extract_db_assets
from services.callback import send_callback, send_node_status_callback
from services.job_store import job_store

logger = logging.getLogger(__name__)

# Per-task timeout in seconds
TASK_TIMEOUT_SECONDS = 60

# Overall workflow timeout in seconds (5 minutes)
WORKFLOW_TIMEOUT_SECONDS = 300


def _get_agent_for_type(agent_type: AgentType, llm_service: LLMService):
    """Instantiate the appropriate agent class based on AgentType."""
    agent_map = {
        AgentType.TEXT: TextAgent,
        AgentType.STRUCT: StructAgent,
        AgentType.EXAM: ExamAgent,
        AgentType.CODE: CodeAgent,
        AgentType.VIDEO: VideoAgent,
    }
    agent_class = agent_map.get(agent_type)
    if agent_class is None:
        raise ValueError(f"Unknown agent type: {agent_type}")
    return agent_class(llm_service)


@retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=2, min=2, max=8),
    retry=retry_if_exception_type(Exception),
    reraise=True,
)
async def _execute_agent_with_retry(
    agent, task: AgentTask
) -> BaseModel:
    """Execute a single agent task with tenacity retry (3 attempts, exponential backoff 2s/4s/8s)."""
    return await agent.execute(task)


async def _execute_single_task(
    task: AgentTask, llm_service: LLMService
) -> BaseModel:
    """Execute a single agent task with per-task 60s timeout and retry logic."""
    agent = _get_agent_for_type(task.agent_type, llm_service)
    try:
        result = await asyncio.wait_for(
            _execute_agent_with_retry(agent, task),
            timeout=TASK_TIMEOUT_SECONDS,
        )
        return result
    except asyncio.TimeoutError:
        logger.error(
            "Task %s timed out after %ds", task.task_id, TASK_TIMEOUT_SECONDS
        )
        raise TimeoutError(
            f"Agent task {task.task_id} timed out after {TASK_TIMEOUT_SECONDS}s"
        )


async def _process_node_with_callback(
    task: AgentTask,
    llm_service: LLMService,
    template_id: int,
    job_id: str,
    callback_url: str,
) -> tuple[AgentTask, Optional[BaseModel], Optional[str]]:
    """
    Process a single node and send per-node status callback.

    Returns a tuple of (task, result_or_None, error_or_None).
    After execution (success or failure), sends a per-node callback to the backend
    and updates the job store.
    """
    node_callback_url = callback_url.rstrip("/") + "/node-status"
    result = None
    error_reason = None

    try:
        result = await _execute_single_task(task, llm_service)
        ai_status = 2  # success

        # Update job store with success
        job_store.update_node_status(
            job_id=job_id,
            node_id=task.node_id,
            status="success",
            result_json=result.model_dump() if result else None,
        )
    except Exception as e:
        ai_status = 3  # failed
        error_reason = str(e)[:500]
        logger.error("Node %s failed: %s", task.node_id, error_reason)

        # Update job store with failure
        job_store.update_node_status(
            job_id=job_id,
            node_id=task.node_id,
            status="failed",
            error_reason=error_reason,
        )

    # Send per-node status callback (best-effort, don't fail the pipeline)
    node_payload = NodeAiStatusPayload(
        template_id=template_id,
        job_id=job_id,
        node_id=task.node_id,
        node_type=task.node_type,
        phase_id=task.phase_id,
        ai_status=ai_status,
        error_reason=error_reason,
        result_json=result.model_dump() if result else None,
    )
    try:
        await send_node_status_callback(node_callback_url, node_payload)
    except Exception as cb_err:
        logger.warning(
            "Failed to send node status callback for node %s: %s",
            task.node_id,
            cb_err,
        )

    return (task, result, error_reason)


async def execute_orchestration_workflow(
    canvas_json: dict,
    template_id: int,
    callback_url: str,
    job_id: Optional[str] = None,
) -> str:
    """
    Main orchestration workflow: parse → dispatch → parallel execute → merge → callback.

    Preconditions:
    - canvas_json is valid phased orchestration JSON with phases containing nodes
    - callback_url is a reachable HTTP endpoint
    - LLM API key is configured

    Postconditions:
    - All AI tasks are executed (or failed with error)
    - Per-node callbacks are sent for each node independently
    - Overall callback is sent to backend with aggregated results
    - Job store tracks per-node completion and overall status

    Args:
        canvas_json: The raw canvas JSON dict (phased structure with ai_spec per node).
        template_id: The template ID from the Java backend.
        callback_url: URL to POST results to after completion.
        job_id: Optional pre-generated job ID. If None, a new UUID is generated.

    Returns:
        The job_id string.
    """
    if job_id is None:
        job_id = str(uuid4())

    # Update job status to processing
    job_store.update_status(job_id, "processing")

    try:
        # Apply overall 5-minute workflow timeout
        result = await asyncio.wait_for(
            _run_workflow_steps(canvas_json, template_id, callback_url, job_id),
            timeout=WORKFLOW_TIMEOUT_SECONDS,
        )
        return result
    except asyncio.TimeoutError:
        logger.error(
            "Workflow for job %s timed out after %ds",
            job_id,
            WORKFLOW_TIMEOUT_SECONDS,
        )
        job_store.update_status(job_id, "failed", error="Workflow timed out after 5 minutes")

        # Send failure callback
        error_payload = AiCallbackPayload(
            template_id=template_id,
            job_id=job_id,
            status=3,
            error_reason="AI处理超时（5分钟），请稍后重试",
        )
        try:
            await send_callback(callback_url, error_payload)
        except Exception as cb_err:
            logger.error("Failed to send timeout callback: %s", cb_err)

        return job_id
    except Exception as e:
        logger.exception("Workflow for job %s failed: %s", job_id, e)
        job_store.update_status(job_id, "failed", error=str(e))

        # Send failure callback
        error_payload = AiCallbackPayload(
            template_id=template_id,
            job_id=job_id,
            status=3,
            error_reason=f"AI处理失败: {str(e)[:200]}",
        )
        try:
            await send_callback(callback_url, error_payload)
        except Exception as cb_err:
            logger.error("Failed to send error callback: %s", cb_err)

        return job_id


async def _run_workflow_steps(
    canvas_json: dict,
    template_id: int,
    callback_url: str,
    job_id: str,
) -> str:
    """Internal workflow steps executed within the overall timeout."""
    # Step 1: Detect AI-enabled nodes from phased canvas_json
    tasks = detect_ai_nodes_phased(canvas_json)
    total_tasks = len(tasks)

    logger.info(
        "Job %s: Detected %d AI tasks from phased canvas",
        job_id,
        total_tasks,
    )
    job_store.update_status(job_id, "processing", total_tasks=total_tasks)

    # Step 7 (Edge case): No nodes need AI processing — immediately callback success
    if total_tasks == 0:
        logger.info("Job %s: No AI tasks detected, sending immediate success callback", job_id)
        payload = AiCallbackPayload(
            template_id=template_id,
            job_id=job_id,
            status=2,
            standard_payload_json=canvas_json,
            generated_assets={"questions": [], "knowledge_points": [], "eval_indicators": []},
        )
        await send_callback(callback_url, payload)
        job_store.update_status(job_id, "completed")
        return job_id

    # Step 2: Parallel execution of all agent tasks with per-node callbacks
    llm_service = LLMService()

    node_coroutines = [
        _process_node_with_callback(
            task=task,
            llm_service=llm_service,
            template_id=template_id,
            job_id=job_id,
            callback_url=callback_url,
        )
        for task in tasks
    ]

    # asyncio.gather with return_exceptions=False since _process_node_with_callback
    # handles exceptions internally and always returns a tuple
    node_results = await asyncio.gather(*node_coroutines)

    # Separate successes and failures
    successes = []
    failures = []
    for task, result, error in node_results:
        if error is None and result is not None:
            successes.append((task, result))
        else:
            failures.append((task, error))

    logger.info(
        "Job %s: %d/%d tasks succeeded, %d failed",
        job_id,
        len(successes),
        total_tasks,
        len(failures),
    )

    # Step 3: Merge successful results into standard_payload_json
    successful_results = [result for _, result in successes]
    standard_payload = merge_results_into_payload(canvas_json, successful_results)

    # Step 4: Extract DB assets from successful results
    generated_assets = extract_db_assets(successful_results)

    # Step 5: Determine overall status and send final callback
    if len(successes) == 0 and len(failures) > 0:
        # All tasks failed
        error_messages = [err for _, err in failures[:3]]
        error_payload = AiCallbackPayload(
            template_id=template_id,
            job_id=job_id,
            status=3,
            error_reason=f"所有AI任务失败: {'; '.join(error_messages)}"[:500],
        )
        await send_callback(callback_url, error_payload)
        job_store.update_status(job_id, "failed", error="All tasks failed")
    elif len(failures) > 0:
        # Partial success — report success with partial results, note failures
        failed_node_ids = [task.node_id for task, _ in failures]
        success_payload = AiCallbackPayload(
            template_id=template_id,
            job_id=job_id,
            status=3,  # Mark as failed since some nodes failed
            standard_payload_json=standard_payload,
            generated_assets=generated_assets,
            error_reason=f"部分节点处理失败: {', '.join(failed_node_ids)}"[:500],
        )
        await send_callback(callback_url, success_payload)
        job_store.update_status(
            job_id, "completed_with_errors",
            error=f"Failed nodes: {', '.join(failed_node_ids)}"
        )
    else:
        # All tasks succeeded
        success_payload = AiCallbackPayload(
            template_id=template_id,
            job_id=job_id,
            status=2,
            standard_payload_json=standard_payload,
            generated_assets=generated_assets,
        )
        await send_callback(callback_url, success_payload)
        job_store.update_status(job_id, "completed")

    return job_id


async def execute_retry_workflow(
    canvas_json: dict,
    template_id: int,
    callback_url: str,
    node_ids: list[str],
    job_id: Optional[str] = None,
) -> str:
    """
    Selective retry workflow: reprocess only the specified failed nodes.

    Filters the phased canvas to only include the specified node_ids,
    then runs the same parallel processing pipeline.

    Args:
        canvas_json: The full phased canvas JSON.
        template_id: The template ID.
        callback_url: URL to POST results to after completion.
        node_ids: List of node_ids to retry (must be subset of nodes in canvas).
        job_id: Optional pre-generated job ID.

    Returns:
        The job_id string.
    """
    if job_id is None:
        job_id = str(uuid4())

    # Update job status to processing
    job_store.update_status(job_id, "processing")

    try:
        result = await asyncio.wait_for(
            _run_retry_steps(canvas_json, template_id, callback_url, node_ids, job_id),
            timeout=WORKFLOW_TIMEOUT_SECONDS,
        )
        return result
    except asyncio.TimeoutError:
        logger.error("Retry workflow for job %s timed out", job_id)
        job_store.update_status(job_id, "failed", error="Retry workflow timed out")

        error_payload = AiCallbackPayload(
            template_id=template_id,
            job_id=job_id,
            status=3,
            error_reason="重试处理超时（5分钟）",
        )
        try:
            await send_callback(callback_url, error_payload)
        except Exception as cb_err:
            logger.error("Failed to send retry timeout callback: %s", cb_err)

        return job_id
    except Exception as e:
        logger.exception("Retry workflow for job %s failed: %s", job_id, e)
        job_store.update_status(job_id, "failed", error=str(e))

        error_payload = AiCallbackPayload(
            template_id=template_id,
            job_id=job_id,
            status=3,
            error_reason=f"重试处理失败: {str(e)[:200]}",
        )
        try:
            await send_callback(callback_url, error_payload)
        except Exception as cb_err:
            logger.error("Failed to send retry error callback: %s", cb_err)

        return job_id


async def _run_retry_steps(
    canvas_json: dict,
    template_id: int,
    callback_url: str,
    node_ids: list[str],
    job_id: str,
) -> str:
    """Internal retry workflow steps — only processes specified node_ids."""
    # Detect all AI nodes, then filter to only the requested retry nodes
    all_tasks = detect_ai_nodes_phased(canvas_json)
    retry_node_set = set(node_ids)
    tasks = [t for t in all_tasks if t.node_id in retry_node_set]

    total_tasks = len(tasks)
    logger.info(
        "Job %s: Retrying %d nodes (requested %d)",
        job_id,
        total_tasks,
        len(node_ids),
    )
    job_store.update_status(job_id, "processing", total_tasks=total_tasks)

    if total_tasks == 0:
        logger.warning("Job %s: No matching nodes found for retry", job_id)
        payload = AiCallbackPayload(
            template_id=template_id,
            job_id=job_id,
            status=2,
            standard_payload_json=canvas_json,
        )
        await send_callback(callback_url, payload)
        job_store.update_status(job_id, "completed")
        return job_id

    # Parallel execution with per-node callbacks
    llm_service = LLMService()

    node_coroutines = [
        _process_node_with_callback(
            task=task,
            llm_service=llm_service,
            template_id=template_id,
            job_id=job_id,
            callback_url=callback_url,
        )
        for task in tasks
    ]

    node_results = await asyncio.gather(*node_coroutines)

    # Aggregate results
    successes = []
    failures = []
    for task, result, error in node_results:
        if error is None and result is not None:
            successes.append((task, result))
        else:
            failures.append((task, error))

    # Send overall callback
    if len(successes) == 0 and len(failures) > 0:
        error_messages = [err for _, err in failures[:3]]
        error_payload = AiCallbackPayload(
            template_id=template_id,
            job_id=job_id,
            status=3,
            error_reason=f"重试节点全部失败: {'; '.join(error_messages)}"[:500],
        )
        await send_callback(callback_url, error_payload)
        job_store.update_status(job_id, "failed", error="All retry tasks failed")
    else:
        successful_results = [result for _, result in successes]
        standard_payload = merge_results_into_payload(canvas_json, successful_results)
        generated_assets = extract_db_assets(successful_results)

        status = 2 if len(failures) == 0 else 3
        payload = AiCallbackPayload(
            template_id=template_id,
            job_id=job_id,
            status=status,
            standard_payload_json=standard_payload,
            generated_assets=generated_assets,
            error_reason=(
                f"部分重试节点失败: {', '.join(t.node_id for t, _ in failures)}"[:500]
                if failures
                else None
            ),
        )
        await send_callback(callback_url, payload)
        final_status = "completed" if len(failures) == 0 else "completed_with_errors"
        job_store.update_status(job_id, final_status)

    return job_id
