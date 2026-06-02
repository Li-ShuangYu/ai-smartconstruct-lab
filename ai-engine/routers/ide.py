"""
IDE Router — endpoints for the AI Teaching IDE page.

POST /api/chat/stream  — SSE streaming chat for the coding assistant
POST /api/execute      — run code in a sandboxed subprocess
GET  /api/health       — already exists in health.py, re-exported here for IDE use
"""

import asyncio
import json
import logging
import subprocess
import sys
import tempfile
import os
from pathlib import Path
from typing import AsyncGenerator

from dotenv import load_dotenv
from fastapi import APIRouter
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

from services.llm_service import LLMService, LLMInvocationError

load_dotenv()
logger = logging.getLogger(__name__)
router = APIRouter()

# ─── System prompt for the coding assistant ───────────────────────────────────
_CODING_SYSTEM_PROMPT = """你是一位编程导师 AI 助手，专注于帮助学生学习编程。

你的职责：
- 用苏格拉底式引导帮助学生理解代码，而非直接给出答案
- 解释代码原理、指出潜在问题、提供优化建议
- 对代码错误给出清晰的诊断和修复思路
- 保持友好、鼓励的语气

回答规范：
- 使用中文回答
- 代码示例用 markdown 代码块包裹，并标注语言
- 回答简洁，避免冗长
"""

# ─── Schemas ──────────────────────────────────────────────────────────────────

class StreamMessage(BaseModel):
    role: str
    content: str

class StreamChatRequest(BaseModel):
    messages: list[StreamMessage] = Field(..., description="Conversation messages")

class ExecuteRequest(BaseModel):
    code: str = Field(..., description="Source code to execute")
    language: str = Field(default="python", description="Programming language")
    filename: str = Field(default="main.py", description="Filename hint")

class ExecuteResponse(BaseModel):
    stdout: str
    stderr: str
    exit_code: int

# ─── SSE streaming chat ───────────────────────────────────────────────────────

async def _stream_llm(messages: list[dict]) -> AsyncGenerator[str, None]:
    """Stream LLM tokens as SSE data events."""
    llm_service = LLMService(temperature=0.7, timeout=60)
    llm = llm_service.get_llm()

    try:
        async for chunk in llm.astream(messages):
            content = chunk.content if hasattr(chunk, "content") else str(chunk)
            if content:
                payload = json.dumps({"content": content}, ensure_ascii=False)
                yield f"data: {payload}\n\n"
        yield "data: {\"done\": true}\n\n"
    except LLMInvocationError as e:
        err = json.dumps({"error": e.message}, ensure_ascii=False)
        yield f"data: {err}\n\n"
    except Exception as e:
        err = json.dumps({"error": str(e)}, ensure_ascii=False)
        yield f"data: {err}\n\n"


@router.post("/api/chat/stream", summary="Streaming chat for coding assistant")
async def chat_stream(request: StreamChatRequest) -> StreamingResponse:
    """Accept a list of messages and stream the LLM reply as SSE."""
    # Build message list: inject system prompt if not already present
    msgs: list[dict] = []
    if not request.messages or request.messages[0].role != "system":
        msgs.append({"role": "system", "content": _CODING_SYSTEM_PROMPT})
    for m in request.messages:
        msgs.append({"role": m.role, "content": m.content})

    return StreamingResponse(
        _stream_llm(msgs),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no",
        },
    )

# ─── Code execution ───────────────────────────────────────────────────────────

# Language → (interpreter command, file extension)
_LANG_CONFIG: dict[str, tuple[list[str], str]] = {
    "python":     (["python", "-u"], ".py"),
    "javascript": (["node"], ".js"),
    "java":       ([], ".java"),   # handled specially
    "cpp":        ([], ".cpp"),    # handled specially
}

_EXEC_TIMEOUT = 10  # seconds


@router.post("/api/execute", response_model=ExecuteResponse, summary="Execute code")
async def execute_code(request: ExecuteRequest) -> ExecuteResponse:
    """Run submitted code in a temporary file and return stdout/stderr."""
    lang = request.language.lower()
    config = _LANG_CONFIG.get(lang)
    if config is None:
        return ExecuteResponse(stdout="", stderr=f"不支持的语言: {lang}", exit_code=1)

    cmd_prefix, ext = config

    with tempfile.TemporaryDirectory() as tmpdir:
        src_path = Path(tmpdir) / f"main{ext}"
        src_path.write_text(request.code, encoding="utf-8")

        try:
            if lang == "python":
                cmd = [sys.executable, "-u", str(src_path)]
            elif lang == "javascript":
                cmd = ["node", str(src_path)]
            elif lang == "java":
                # Compile then run
                compile_result = subprocess.run(
                    ["javac", str(src_path)],
                    capture_output=True, text=True, timeout=_EXEC_TIMEOUT, cwd=tmpdir
                )
                if compile_result.returncode != 0:
                    return ExecuteResponse(
                        stdout="", stderr=compile_result.stderr, exit_code=compile_result.returncode
                    )
                cmd = ["java", "-cp", tmpdir, "main"]
            elif lang == "cpp":
                out_path = Path(tmpdir) / "main_out"
                compile_result = subprocess.run(
                    ["g++", str(src_path), "-o", str(out_path)],
                    capture_output=True, text=True, timeout=_EXEC_TIMEOUT
                )
                if compile_result.returncode != 0:
                    return ExecuteResponse(
                        stdout="", stderr=compile_result.stderr, exit_code=compile_result.returncode
                    )
                cmd = [str(out_path)]
            else:
                return ExecuteResponse(stdout="", stderr=f"不支持的语言: {lang}", exit_code=1)

            result = subprocess.run(
                cmd,
                capture_output=True, text=True,
                timeout=_EXEC_TIMEOUT,
                cwd=tmpdir,
            )
            return ExecuteResponse(
                stdout=result.stdout,
                stderr=result.stderr,
                exit_code=result.returncode,
            )

        except subprocess.TimeoutExpired:
            return ExecuteResponse(
                stdout="", stderr=f"执行超时（>{_EXEC_TIMEOUT}s），请检查是否有死循环。", exit_code=124
            )
        except FileNotFoundError as e:
            return ExecuteResponse(
                stdout="", stderr=f"运行环境未找到: {e}。请确认服务器已安装对应语言环境。", exit_code=127
            )
        except Exception as e:
            logger.error("Code execution error: %s", e)
            return ExecuteResponse(stdout="", stderr=str(e), exit_code=1)
