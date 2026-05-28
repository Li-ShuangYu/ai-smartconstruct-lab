from pydantic import BaseModel
from typing import Optional


class AiCallbackPayload(BaseModel):
    """Overall AI processing callback payload sent to the backend."""

    template_id: int
    job_id: str
    status: int  # 2=success, 3=error
    standard_payload_json: Optional[dict] = None
    error_reason: Optional[str] = None
    generated_assets: Optional[dict] = None


class NodeAiStatusPayload(BaseModel):
    """Per-node AI status callback payload sent to the backend after each node completes."""

    template_id: int
    job_id: str
    node_id: str
    node_type: str
    phase_id: Optional[str] = None
    ai_status: int  # 0=pending, 1=processing, 2=success, 3=failed
    error_reason: Optional[str] = None
    result_json: Optional[dict] = None
