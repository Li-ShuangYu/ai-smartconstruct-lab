"""
Orchestration Router — FastAPI endpoints for AI orchestration pipeline.

POST /api/orchestration/process — accepts phased orchestration request, starts async workflow
POST /api/orchestration/retry — selectively retry failed nodes
GET /api/orchestration/status/{job_id} — polls job status
"""

from fastapi import APIRouter, BackgroundTasks, Depends, HTTPException
from uuid import uuid4

from models.schemas import OrchestrationRequest, OrchestrationResponse, RetryRequest
from middleware.rate_limiter import RateLimiter
from services.job_store import job_store
from services.workflow import execute_orchestration_workflow, execute_retry_workflow

router = APIRouter()

# Maximum number of concurrent active jobs before returning 503
MAX_ACTIVE_JOBS = 50

# Rate limiter: 10 requests per 60 seconds for the process endpoint
process_rate_limiter = RateLimiter(max_requests=10, window_seconds=60)


def _validate_canvas_json(canvas_json: dict) -> None:
    """Validate phased canvas_json structure.

    Accepts BOTH the new phased structure (with 'phases') and the legacy
    flat structure (with 'nodes'/'edges') for backward compatibility.

    Raises HTTPException(400) if:
    - Neither 'phases' nor 'nodes' key is present
    - For phased: any phase is missing 'nodes' or 'phase_id'
    - For phased: any node is missing required fields: node_id, node_type, config
    - For legacy: 'nodes'/'edges' validation (existing behavior)
    """
    # New phased structure
    if "phases" in canvas_json:
        phases = canvas_json["phases"]
        if not isinstance(phases, list):
            raise HTTPException(
                status_code=400,
                detail={
                    "error_code": "INVALID_CANVAS_STRUCTURE",
                    "error_message": "canvas_json 'phases' must be a list",
                },
            )

        if len(phases) == 0:
            raise HTTPException(
                status_code=400,
                detail={
                    "error_code": "INVALID_CANVAS_STRUCTURE",
                    "error_message": "canvas_json must contain at least one phase",
                },
            )

        required_node_fields = ("node_id", "node_type", "config")
        for phase_idx, phase in enumerate(phases):
            if not isinstance(phase, dict):
                raise HTTPException(
                    status_code=400,
                    detail={
                        "error_code": "INVALID_PHASE_STRUCTURE",
                        "error_message": f"Phase at index {phase_idx} must be a dictionary",
                    },
                )

            if "phase_id" not in phase:
                raise HTTPException(
                    status_code=400,
                    detail={
                        "error_code": "MISSING_PHASE_FIELDS",
                        "error_message": f"Phase at index {phase_idx} is missing 'phase_id'",
                    },
                )

            nodes = phase.get("nodes", [])
            if not isinstance(nodes, list):
                raise HTTPException(
                    status_code=400,
                    detail={
                        "error_code": "INVALID_PHASE_STRUCTURE",
                        "error_message": (
                            f"Phase '{phase.get('phase_id', phase_idx)}' "
                            "'nodes' must be a list"
                        ),
                    },
                )

            for node_idx, node in enumerate(nodes):
                if not isinstance(node, dict):
                    raise HTTPException(
                        status_code=400,
                        detail={
                            "error_code": "INVALID_NODE_STRUCTURE",
                            "error_message": (
                                f"Node at index {node_idx} in phase "
                                f"'{phase.get('phase_id', phase_idx)}' must be a dictionary"
                            ),
                        },
                    )
                missing_fields = [f for f in required_node_fields if f not in node]
                if missing_fields:
                    raise HTTPException(
                        status_code=400,
                        detail={
                            "error_code": "MISSING_NODE_FIELDS",
                            "error_message": (
                                f"Node at index {node_idx} in phase "
                                f"'{phase.get('phase_id', phase_idx)}' is missing "
                                f"required fields: {', '.join(missing_fields)}"
                            ),
                        },
                    )
        return

    # Legacy flat structure (backward compatibility)
    if "nodes" not in canvas_json or not isinstance(canvas_json.get("nodes"), list):
        raise HTTPException(
            status_code=400,
            detail={
                "error_code": "INVALID_CANVAS_STRUCTURE",
                "error_message": (
                    "canvas_json must contain either 'phases' (phased structure) "
                    "or 'nodes' (legacy structure)"
                ),
            },
        )

    if "edges" not in canvas_json or not isinstance(canvas_json.get("edges"), list):
        raise HTTPException(
            status_code=400,
            detail={
                "error_code": "INVALID_CANVAS_STRUCTURE",
                "error_message": "canvas_json must contain an 'edges' key with a list value",
            },
        )

    # Validate each node has required fields (legacy)
    required_node_fields = ("node_id", "node_type", "config")
    for idx, node in enumerate(canvas_json["nodes"]):
        if not isinstance(node, dict):
            raise HTTPException(
                status_code=400,
                detail={
                    "error_code": "INVALID_NODE_STRUCTURE",
                    "error_message": f"Node at index {idx} must be a dictionary",
                },
            )
        missing_fields = [f for f in required_node_fields if f not in node]
        if missing_fields:
            raise HTTPException(
                status_code=400,
                detail={
                    "error_code": "MISSING_NODE_FIELDS",
                    "error_message": (
                        f"Node at index {idx} is missing required fields: "
                        f"{', '.join(missing_fields)}"
                    ),
                },
            )


def estimate_duration(canvas_json: dict) -> int:
    """Estimate processing duration in seconds based on canvas complexity."""
    # Count nodes across all phases (or flat nodes for legacy)
    node_count = 0
    if "phases" in canvas_json:
        for phase in canvas_json.get("phases", []):
            node_count += len(phase.get("nodes", []))
    else:
        node_count = len(canvas_json.get("nodes", []))

    # Base 30s + 5s per node, capped at 300s
    estimated = 30 + node_count * 5
    return min(estimated, 300)


@router.post(
    "/api/orchestration/process",
    response_model=OrchestrationResponse,
    status_code=202,
    summary="Submit orchestration for AI processing",
    dependencies=[Depends(process_rate_limiter)],
)
async def process_orchestration(
    request: OrchestrationRequest,
    background_tasks: BackgroundTasks,
):
    """Accept a phased orchestration request and start async AI processing.

    Supports the new phased canvas_json structure where each node carries
    its own ai_spec for data-driven routing. Also supports legacy flat structure
    for backward compatibility.

    Returns immediately with a job_id for status tracking.
    Validates canvas_json structure and rejects overloaded requests.
    """
    # Overload protection: reject if too many active jobs
    if job_store.count_active() >= MAX_ACTIVE_JOBS:
        raise HTTPException(
            status_code=503,
            detail={
                "error_code": "SERVICE_OVERLOADED",
                "error_message": (
                    f"Service is at capacity with {MAX_ACTIVE_JOBS} active jobs. "
                    "Please retry later."
                ),
            },
        )

    # Validate canvas_json structure (phased or legacy)
    _validate_canvas_json(request.canvas_json)

    job_id = str(uuid4())

    # Initialize job status in the shared store
    job_store.create_job(
        job_id=job_id,
        template_id=request.template_id,
        orchestration_id=request.orchestration_id,
    )

    # Start background processing with the phased workflow
    background_tasks.add_task(
        execute_orchestration_workflow,
        canvas_json=request.canvas_json,
        template_id=request.template_id,
        callback_url=request.callback_url,
        job_id=job_id,
    )

    return OrchestrationResponse(
        job_id=job_id,
        status="accepted",
        estimated_duration_seconds=estimate_duration(request.canvas_json),
    )


@router.post(
    "/api/orchestration/retry",
    response_model=OrchestrationResponse,
    status_code=202,
    summary="Selectively retry failed nodes",
    dependencies=[Depends(process_rate_limiter)],
)
async def retry_failed_nodes(
    request: RetryRequest,
    background_tasks: BackgroundTasks,
):
    """Accept a selective retry request for specific failed nodes.

    Only reprocesses the specified node_ids. Already-succeeded nodes
    retain their results and are not reprocessed.

    Args:
        request: RetryRequest containing template_id, node_ids, canvas_json, callback_url.

    Returns:
        OrchestrationResponse with a new job_id for tracking the retry.
    """
    # Overload protection
    if job_store.count_active() >= MAX_ACTIVE_JOBS:
        raise HTTPException(
            status_code=503,
            detail={
                "error_code": "SERVICE_OVERLOADED",
                "error_message": (
                    f"Service is at capacity with {MAX_ACTIVE_JOBS} active jobs. "
                    "Please retry later."
                ),
            },
        )

    # Validate canvas_json structure
    _validate_canvas_json(request.canvas_json)

    job_id = str(uuid4())

    # Initialize job status
    job_store.create_job(
        job_id=job_id,
        template_id=request.template_id,
        orchestration_id=request.orchestration_id,
    )

    # Start background retry processing
    background_tasks.add_task(
        execute_retry_workflow,
        canvas_json=request.canvas_json,
        template_id=request.template_id,
        callback_url=request.callback_url,
        node_ids=request.node_ids,
        job_id=job_id,
    )

    return OrchestrationResponse(
        job_id=job_id,
        status="accepted",
        estimated_duration_seconds=estimate_duration(request.canvas_json),
    )


@router.get(
    "/api/orchestration/status/{job_id}",
    summary="Poll orchestration job status",
)
async def get_orchestration_status(job_id: str):
    """Return the current processing status for a given job_id.

    Returns 404 if the job_id is not found.
    Includes per-node status information when available.
    """
    status = job_store.get_status(job_id)
    if status is None:
        raise HTTPException(status_code=404, detail=f"Job {job_id} not found")

    return status
