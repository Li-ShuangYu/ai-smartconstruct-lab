"""
Workflow Orchestration Service — main async workflow for AI pipeline.

Implements Algorithm 2 from the design doc:
1. Supervisor analyzes canvas_json → ExecutionPlan
2. Parallel execution of all agent tasks via asyncio.gather
3. Merge results into standard_payload_json
4. Extract DB assets (questions, knowledge_points, eval_indicators)
5. Send callback to Java backend

Includes:
- Per-task 60s timeout
- Overall workflow 5-minute timeout with cancellation
- Retry logic: 3 retries with exponential backoff (2s, 4s, 8s) per agent task
"""

import asyncio
import logging
from typing import Optional
from uuid import uuid4

from pydantic import BaseModel
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type

from agents.supervisor import SupervisorAgent
from agents.text_agent import TextAgent
from agents.struct_agent import StructAgent
from agents.exam_agent import ExamAgent
from agents.code_agent import CodeAgent
from agents.video_agent import VideoAgent
from models.schemas import AgentTask, AgentType, ExecutionPlan
from models.callback import AiCallbackPayload
from services.llm_service import LLMService
from services.merger import merge_results_into_payload
from services.asset_extractor import extract_db_assets
from services.callback import send_callback
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


async def execute_orchestration_workflow(
    canvas_json: dict,
    template_id: int,
    callback_url: str,
    job_id: Optional[str] = None,
) -> str:
    """
    Main orchestration workflow: parse → dispatch → parallel execute → merge → callback.

    Preconditions:
    - canvas_json is valid orchestration JSON with nodes and edges
    - callback_url is a reachable HTTP endpoint
    - LLM API key is configured

    Postconditions:
    - All AI tasks are executed (or failed with error)
    - Callback is sent to backend with results
    - standard_payload_json contains enriched node configs

    Args:
        canvas_json: The raw canvas JSON dict.
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
    # Step 1: Supervisor analyzes canvas_json → ExecutionPlan
    supervisor = SupervisorAgent()
    plan: ExecutionPlan = supervisor.analyze_orchestration(canvas_json)

    logger.info(
        "Job %s: Supervisor created plan with %d tasks",
        job_id,
        plan.total_tasks,
    )
    job_store.update_status(
        job_id, "processing", total_tasks=plan.total_tasks
    )

    if plan.total_tasks == 0:
        # No AI tasks needed — send success callback with original JSON
        logger.info("Job %s: No AI tasks detected, sending callback", job_id)
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

    # Step 2: Parallel execution of all agent tasks
    llm_service = LLMService()

    tasks = [
        _execute_single_task(task, llm_service)
        for task in plan.parallel_jobs
    ]
    results = await asyncio.gather(*tasks, return_exceptions=True)

    # Log results summary
    successes = [r for r in results if not isinstance(r, Exception)]
    failures = [r for r in results if isinstance(r, Exception)]
    logger.info(
        "Job %s: %d/%d tasks succeeded, %d failed",
        job_id,
        len(successes),
        plan.total_tasks,
        len(failures),
    )

    # Step 3: Merge results into standard_payload_json
    standard_payload = merge_results_into_payload(canvas_json, results)

    # Step 4: Extract DB assets (questions, knowledge_points, eval_indicators)
    generated_assets = extract_db_assets(results)

    # Step 5: Determine overall status and send callback
    # If all tasks failed, report error; otherwise report success with partial results
    if len(successes) == 0 and len(failures) > 0:
        error_messages = [str(e) for e in failures[:3]]
        error_payload = AiCallbackPayload(
            template_id=template_id,
            job_id=job_id,
            status=3,
            error_reason=f"所有AI任务失败: {'; '.join(error_messages)}"[:500],
        )
        await send_callback(callback_url, error_payload)
        job_store.update_status(job_id, "failed", error="All tasks failed")
    else:
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
