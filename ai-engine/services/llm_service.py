"""
LLM Service — initializes ChatOpenAI with DeepSeek-compatible endpoint
and provides structured output capabilities with retry logic.

Reads configuration from environment variables (loaded via python-dotenv):
- LLM_BASE_URL: OpenAI-compatible API endpoint (default: https://api.deepseek.com)
- LLM_API_KEY: API key for authentication
- LLM_MODEL_NAME: Model name (default: "deepseek-v4-flash")

Retry strategy:
- Uses tenacity for exponential backoff on transient failures
- Handles HTTP 429 (rate limit) by respecting retry-after header (capped at 60s)
- Handles 5xx and timeout errors with exponential backoff (2s, 4s, 8s)
- Raises LLMInvocationError on final failure with status code and message
"""

import asyncio
import logging
import os
from typing import Type, TypeVar

import httpx
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from pydantic import BaseModel
from tenacity import (
    retry,
    retry_if_exception_type,
    stop_after_attempt,
    wait_exponential,
)

# Load .env from the ai-engine directory
load_dotenv()

logger = logging.getLogger(__name__)

T = TypeVar("T", bound=BaseModel)


class LLMInvocationError(Exception):
    """
    Raised when the LLM invocation fails after all retry attempts.

    Attributes:
        status_code: HTTP status code from the last failed response (None for timeouts).
        message: Human-readable error description including the last error details.
    """

    def __init__(self, status_code: int | None, message: str) -> None:
        self.status_code = status_code
        self.message = message
        super().__init__(message)


def _should_retry_http_error(exc: BaseException) -> bool:
    """
    Determine if an HTTPStatusError should be retried.

    Retries on:
    - 429 (rate limited) — after sleeping for retry-after duration
    - 5xx (server errors) — with exponential backoff via tenacity
    """
    if isinstance(exc, httpx.HTTPStatusError):
        return exc.response.status_code == 429 or exc.response.status_code >= 500
    return False


class LLMService:
    """
    Service wrapping ChatOpenAI configured for DeepSeek v4 Flash endpoint.

    Provides:
    - Access to the raw ChatOpenAI instance via `get_llm()`
    - A `with_structured_output(schema)` helper that returns a runnable
      producing validated Pydantic objects
    - `invoke_with_retry()` for resilient LLM calls with retry on transient errors
    """

    def __init__(
        self,
        base_url: str | None = None,
        api_key: str | None = None,
        model_name: str | None = None,
        temperature: float = 0.7,
        timeout: int = 30,
    ) -> None:
        """
        Initialize the LLM service.

        Args:
            base_url: API base URL. Falls back to LLM_BASE_URL env var.
            api_key: API key. Falls back to LLM_API_KEY env var.
            model_name: Model identifier. Falls back to LLM_MODEL_NAME env var,
                        then defaults to "deepseek-v4-flash".
            temperature: Sampling temperature for generation (default 0.7).
            timeout: Per-request timeout in seconds (default 30).
        """
        self._base_url = base_url or os.getenv(
            "LLM_BASE_URL", "https://api.deepseek.com"
        )
        self._api_key = api_key or os.getenv("LLM_API_KEY", "")
        self._model_name = model_name or os.getenv(
            "LLM_MODEL_NAME", "deepseek-v4-flash"
        )
        self._temperature = temperature
        self._timeout = timeout

        self._llm = ChatOpenAI(
            base_url=self._base_url,
            api_key=self._api_key,
            model=self._model_name,
            temperature=self._temperature,
            request_timeout=self._timeout,
        )

    def get_llm(self) -> ChatOpenAI:
        """
        Return the underlying ChatOpenAI instance.

        Useful when you need direct access to the LLM for custom chains
        or non-structured invocations.
        """
        return self._llm

    def with_structured_output(self, output_schema: Type[T]):
        """
        Return a runnable chain that produces validated Pydantic output.

        Uses LangChain's `with_structured_output` to constrain the LLM
        response to the given Pydantic schema, enabling reliable structured
        data extraction.

        Args:
            output_schema: A Pydantic BaseModel subclass defining the
                           expected output structure.

        Returns:
            A LangChain Runnable that accepts messages/prompts and returns
            an instance of `output_schema`.
        """
        return self._llm.with_structured_output(output_schema)

    async def invoke_with_retry(self, messages, output_schema: Type[T] | None = None):
        """
        Invoke the LLM with automatic retry on transient failures.

        Handles:
        - HTTP 429 (rate limited): reads retry-after header, sleeps (capped at 60s),
          then re-raises to let tenacity retry.
        - HTTP 5xx (server error): re-raises to let tenacity retry with
          exponential backoff.
        - Timeout: re-raises to let tenacity retry with exponential backoff.

        On final failure after all retries exhausted, raises LLMInvocationError
        with the HTTP status code and last error message.

        Args:
            messages: Messages to send to the LLM (list of message dicts or
                      LangChain message objects).
            output_schema: Optional Pydantic schema for structured output.
                           If provided, uses `with_structured_output`.

        Returns:
            LLM response — either a structured Pydantic model (if output_schema
            is provided) or a raw LangChain AIMessage.

        Raises:
            LLMInvocationError: After all retry attempts are exhausted.
        """
        try:
            return await self._invoke_with_tenacity(messages, output_schema)
        except httpx.HTTPStatusError as e:
            raise LLMInvocationError(
                status_code=e.response.status_code,
                message=(
                    f"LLM request failed after 3 attempts with HTTP "
                    f"{e.response.status_code}: {str(e)}"
                ),
            ) from e
        except httpx.TimeoutException as e:
            raise LLMInvocationError(
                status_code=None,
                message=f"LLM request timed out after 3 attempts: {str(e)}",
            ) from e

    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=2, min=2, max=8),
        retry=retry_if_exception_type(
            (httpx.TimeoutException, httpx.HTTPStatusError)
        ),
        reraise=True,
    )
    async def _invoke_with_tenacity(self, messages, output_schema: Type[T] | None = None):
        """
        Internal method decorated with tenacity retry logic.

        This is separated from invoke_with_retry so the outer method can
        catch the final exception and wrap it in LLMInvocationError.
        """
        try:
            if output_schema:
                chain = self._llm.with_structured_output(output_schema)
                return await chain.ainvoke(messages)
            return await self._llm.ainvoke(messages)
        except httpx.HTTPStatusError as e:
            status_code = e.response.status_code
            if status_code == 429:
                # Respect retry-after header, capped at 60 seconds
                retry_after = int(e.response.headers.get("retry-after", "5"))
                retry_after = min(retry_after, 60)
                logger.warning(
                    "LLM rate limited (429). Sleeping %ds before retry.",
                    retry_after,
                )
                await asyncio.sleep(retry_after)
            elif status_code >= 500:
                logger.warning(
                    "LLM server error (%d). Will retry with backoff.",
                    status_code,
                )
            else:
                # Non-retryable HTTP error (4xx other than 429) — don't retry
                raise LLMInvocationError(
                    status_code=status_code,
                    message=f"LLM request failed with HTTP {status_code}: {str(e)}",
                ) from e
            # Re-raise so tenacity can handle the retry
            raise
        except httpx.TimeoutException:
            logger.warning("LLM request timed out. Will retry with backoff.")
            raise

