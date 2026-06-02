"""
Chat Router — FastAPI endpoint for AI floating assistant conversations.

POST /api/chat — accepts a user message with optional history, returns AI reply.
"""

import logging
import os
from typing import Any

from dotenv import load_dotenv
from fastapi import APIRouter
from pydantic import BaseModel, Field

from services.llm_service import LLMService, LLMInvocationError

load_dotenv()

logger = logging.getLogger(__name__)

router = APIRouter()

# System prompt for the floating AI assistant
_SYSTEM_PROMPT = """你是"喵喵 AI 助教"，一个智能实训平台的 AI 助教。

你的职责：
- 解答学生在实训过程中遇到的技术问题和概念疑惑
- 提供苏格拉底式引导，帮助学生自主思考，而非直接给出答案
- 对编程、理论知识等专业问题给出准确、简洁的解释
- 保持友好、耐心、鼓励的语气

回答规范：
- 使用中文回答
- 回答简洁明了，避免冗长
- 对代码问题可以给出代码示例
- 不确定的内容要如实说明，不要编造
- 不要主动提及或假设学生所在的具体专业领域，根据学生的问题内容来判断
"""

# Session type context hints
_SESSION_TYPE_HINTS = {
    1: "学生正在使用全局悬浮助手，可能有各类问题。",
    2: "学生正在进行 AI 对练练习。",
    3: "学生正在学习理论实训内容。",
    4: "学生正在进行编码实训，可能需要代码相关帮助。",
    5: "学生正在进行答疑环节。",
}


class ChatMessage(BaseModel):
    """Single message in conversation history."""

    role: str = Field(..., description="Message role: user or assistant")
    content: str = Field(..., description="Message content")


class ChatRequest(BaseModel):
    """Request body for the chat endpoint."""

    message: str = Field(..., min_length=1, description="User's current message")
    history: list[ChatMessage] = Field(
        default_factory=list,
        description="Previous conversation messages (up to 10)",
    )
    session_type: int = Field(
        default=1,
        description="Session type: 1=global float, 2=AI drill, 3=theory, 4=coding, 5=QA",
    )
    task_id: int | None = Field(default=None, description="Associated training task ID")


class ChatResponse(BaseModel):
    """Response body for the chat endpoint."""

    content: str = Field(..., description="AI assistant reply")
    tokens_used: int | None = Field(default=None, description="Tokens consumed")


@router.post(
    "/api/chat",
    response_model=ChatResponse,
    summary="AI floating assistant chat",
)
async def chat(request: ChatRequest) -> ChatResponse:
    """Handle a single chat turn for the AI floating assistant.

    Builds a message list from system prompt + history + current message,
    calls the LLM, and returns the reply.

    Falls back to a polite error message if the LLM is unavailable.
    """
    llm_service = LLMService(temperature=0.7, timeout=30)

    # Build system prompt with session context hint
    session_hint = _SESSION_TYPE_HINTS.get(request.session_type, "")
    system_content = _SYSTEM_PROMPT
    if session_hint:
        system_content += f"\n\n当前场景提示：{session_hint}"

    # Assemble messages: system + history + current user message
    messages: list[dict[str, Any]] = [{"role": "system", "content": system_content}]

    # Append history (cap at 10 to avoid token overflow)
    for msg in request.history[-10:]:
        messages.append({"role": msg.role, "content": msg.content})

    # Append current user message
    messages.append({"role": "user", "content": request.message})

    try:
        response = await llm_service.invoke_with_retry(messages)
        content = response.content if hasattr(response, "content") else str(response)

        # Extract token usage if available
        tokens_used: int | None = None
        if hasattr(response, "usage_metadata") and response.usage_metadata:
            tokens_used = response.usage_metadata.get("total_tokens")

        logger.info(
            "Chat response generated, session_type=%d, tokens=%s",
            request.session_type,
            tokens_used,
        )
        return ChatResponse(content=content, tokens_used=tokens_used)

    except LLMInvocationError as e:
        logger.error("LLM invocation failed for chat: %s", e.message)
        return ChatResponse(
            content="抱歉，AI 助教暂时无法响应，请稍后再试。如有紧急问题，请联系老师。",
            tokens_used=None,
        )
    except Exception as e:
        logger.error("Unexpected error in chat endpoint: %s", str(e))
        return ChatResponse(
            content="抱歉，AI 助教遇到了一个意外错误，请稍后再试。",
            tokens_used=None,
        )
