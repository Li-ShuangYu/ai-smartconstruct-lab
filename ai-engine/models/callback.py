from pydantic import BaseModel
from typing import Optional


class AiCallbackPayload(BaseModel):
    template_id: int
    job_id: str
    status: int  # 2=success, 3=error
    standard_payload_json: Optional[dict] = None
    error_reason: Optional[str] = None
    generated_assets: Optional[dict] = None
