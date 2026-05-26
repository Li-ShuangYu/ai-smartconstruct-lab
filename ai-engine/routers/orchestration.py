"""
Orchestration Router — FastAPI endpoints for AI orchestration pipeline.

POST /api/orchestration/process — accepts orchestration request, starts async workflow
GET /api/orchestration/status/{job_id} — polls job status
"""

from fastapi import APIRouter, BackgroundTasks, Depends, HTTPException
from uuid import uuid4

from models.schemas import OrchestrationRequest, OrchestrationResponse
from middleware.rate_limiter import RateLimiter
from services.job_store import job_store
from services.workflow import execute_orchestration_workflow

router = APIRouter()

# Maximum number of concurrent active jobs before returning 503
MAX_ACTIVE_JOBS = 50

# Rate limiter: 10 requests per 60 seconds for the process endpoint
process_rate_limiter = RateLimiter(max_requests=10, window_seconds=60)


def _validate_canvas_json(canvas_json: dict) -> None:
    """Validate canvas_json structure.

    Raises HTTPException(400) if:
    - 'nodes' key is missing or not a list
    - 'edges' key is missing or not a list
    - Any node is missing required fields: node_id, node_type, config
    """
    # Validate top-level keys
    if "nodes" not in canvas_json or not isinstance(canvas_json.get("nodes"), list):
        raise HTTPException(
            status_code=400,
            detail={
                "error_code": "INVALID_CANVAS_STRUCTURE",
                "error_message": "canvas_json must contain a 'nodes' key with a list value",
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

    # Validate each node has required fields
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
    nodes = canvas_json.get("nodes", [])
    # Base 30s + 5s per node, capped at 300s
    estimated = 30 + len(nodes) * 5
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
    """Accept an orchestration request and start async AI processing.

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

    # Validate canvas_json structure (nodes/edges and node fields)
    _validate_canvas_json(request.canvas_json)

    job_id = str(uuid4())

    # Initialize job status in the shared store
    job_store.create_job(
        job_id=job_id,
        template_id=request.template_id,
        orchestration_id=request.orchestration_id,
    )

    # Start background processing with the real workflow
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


@router.get(
    "/api/orchestration/status/{job_id}",
    summary="Poll orchestration job status",
)
async def get_orchestration_status(job_id: str):
    """Return the current processing status for a given job_id.

    Returns 404 if the job_id is not found.
    """
    status = job_store.get_status(job_id)
    if status is None:
        raise HTTPException(status_code=404, detail=f"Job {job_id} not found")

    return status
