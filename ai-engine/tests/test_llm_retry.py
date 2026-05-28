"""
Tests for LLMService.invoke_with_retry — verifies retry behavior for
HTTP 429, 5xx, and timeout errors, plus final failure wrapping in LLMInvocationError.
"""

import asyncio
from unittest.mock import AsyncMock, MagicMock, patch

import httpx
import pytest

from services.llm_service import LLMInvocationError, LLMService


def _make_http_status_error(status_code: int, headers: dict | None = None) -> httpx.HTTPStatusError:
    """Helper to create an httpx.HTTPStatusError with a mock response."""
    response = httpx.Response(
        status_code=status_code,
        headers=headers or {},
        request=httpx.Request("POST", "https://api.deepseek.com/v1/chat/completions"),
    )
    return httpx.HTTPStatusError(
        message=f"Server error {status_code}",
        request=response.request,
        response=response,
    )


class TestInvokeWithRetry:
    """Tests for LLMService.invoke_with_retry."""

    @pytest.fixture
    def llm_service(self):
        """Create an LLMService with a mocked internal LLM."""
        svc = LLMService(api_key="test-key")
        svc._llm = MagicMock()
        return svc

    @pytest.mark.asyncio
    async def test_success_on_first_attempt(self, llm_service):
        """Returns result when LLM call succeeds on first try."""
        llm_service._llm.ainvoke = AsyncMock(return_value="Hello world")

        result = await llm_service.invoke_with_retry([{"role": "user", "content": "hi"}])

        assert result == "Hello world"
        assert llm_service._llm.ainvoke.call_count == 1

    @pytest.mark.asyncio
    async def test_success_with_structured_output(self, llm_service):
        """Returns structured output when output_schema is provided."""
        from pydantic import BaseModel

        class TestOutput(BaseModel):
            answer: str

        mock_chain = MagicMock()
        mock_chain.ainvoke = AsyncMock(return_value=TestOutput(answer="42"))
        llm_service._llm.with_structured_output = MagicMock(return_value=mock_chain)

        result = await llm_service.invoke_with_retry(
            [{"role": "user", "content": "what is 6*7?"}],
            output_schema=TestOutput,
        )

        assert result.answer == "42"
        llm_service._llm.with_structured_output.assert_called_once_with(TestOutput)

    @pytest.mark.asyncio
    async def test_retries_on_5xx_then_succeeds(self, llm_service):
        """Retries on 5xx server error and succeeds on subsequent attempt."""
        error_500 = _make_http_status_error(500)
        llm_service._llm.ainvoke = AsyncMock(
            side_effect=[error_500, "success after retry"]
        )

        # Patch tenacity wait to avoid real delays in tests
        with patch("services.llm_service.wait_exponential", return_value=0):
            # Re-create the service to pick up patched wait
            pass

        # Use the service directly — tenacity will retry
        # We need to patch the wait to be instant for testing
        from tenacity import wait_none

        llm_service._invoke_with_tenacity.retry.wait = wait_none()

        result = await llm_service.invoke_with_retry(
            [{"role": "user", "content": "hi"}]
        )

        assert result == "success after retry"
        assert llm_service._llm.ainvoke.call_count == 2

    @pytest.mark.asyncio
    async def test_retries_on_timeout_then_succeeds(self, llm_service):
        """Retries on timeout and succeeds on subsequent attempt."""
        timeout_error = httpx.ReadTimeout("Connection timed out")
        llm_service._llm.ainvoke = AsyncMock(
            side_effect=[timeout_error, "success after timeout retry"]
        )

        from tenacity import wait_none

        llm_service._invoke_with_tenacity.retry.wait = wait_none()

        result = await llm_service.invoke_with_retry(
            [{"role": "user", "content": "hi"}]
        )

        assert result == "success after timeout retry"
        assert llm_service._llm.ainvoke.call_count == 2

    @pytest.mark.asyncio
    async def test_raises_llm_invocation_error_after_3_failures_5xx(self, llm_service):
        """Raises LLMInvocationError after 3 failed attempts with 5xx."""
        error_503 = _make_http_status_error(503)
        llm_service._llm.ainvoke = AsyncMock(side_effect=error_503)

        from tenacity import wait_none

        llm_service._invoke_with_tenacity.retry.wait = wait_none()

        with pytest.raises(LLMInvocationError) as exc_info:
            await llm_service.invoke_with_retry(
                [{"role": "user", "content": "hi"}]
            )

        assert exc_info.value.status_code == 503
        assert "3 attempts" in exc_info.value.message
        assert llm_service._llm.ainvoke.call_count == 3

    @pytest.mark.asyncio
    async def test_raises_llm_invocation_error_after_3_timeouts(self, llm_service):
        """Raises LLMInvocationError after 3 timeout failures."""
        timeout_error = httpx.ReadTimeout("Connection timed out")
        llm_service._llm.ainvoke = AsyncMock(side_effect=timeout_error)

        from tenacity import wait_none

        llm_service._invoke_with_tenacity.retry.wait = wait_none()

        with pytest.raises(LLMInvocationError) as exc_info:
            await llm_service.invoke_with_retry(
                [{"role": "user", "content": "hi"}]
            )

        assert exc_info.value.status_code is None
        assert "timed out" in exc_info.value.message
        assert llm_service._llm.ainvoke.call_count == 3

    @pytest.mark.asyncio
    async def test_handles_429_with_retry_after_header(self, llm_service):
        """On 429, sleeps for retry-after duration then retries."""
        error_429 = _make_http_status_error(429, headers={"retry-after": "3"})
        llm_service._llm.ainvoke = AsyncMock(
            side_effect=[error_429, "success after rate limit"]
        )

        from tenacity import wait_none

        llm_service._invoke_with_tenacity.retry.wait = wait_none()

        with patch("services.llm_service.asyncio.sleep", new_callable=AsyncMock) as mock_sleep:
            result = await llm_service.invoke_with_retry(
                [{"role": "user", "content": "hi"}]
            )

        assert result == "success after rate limit"
        # Should have slept for 3 seconds (from retry-after header)
        mock_sleep.assert_any_call(3)

    @pytest.mark.asyncio
    async def test_429_retry_after_capped_at_60(self, llm_service):
        """Retry-after header value is capped at 60 seconds."""
        error_429 = _make_http_status_error(429, headers={"retry-after": "120"})
        llm_service._llm.ainvoke = AsyncMock(
            side_effect=[error_429, "success"]
        )

        from tenacity import wait_none

        llm_service._invoke_with_tenacity.retry.wait = wait_none()

        with patch("services.llm_service.asyncio.sleep", new_callable=AsyncMock) as mock_sleep:
            result = await llm_service.invoke_with_retry(
                [{"role": "user", "content": "hi"}]
            )

        assert result == "success"
        mock_sleep.assert_any_call(60)

    @pytest.mark.asyncio
    async def test_429_defaults_to_5s_when_no_header(self, llm_service):
        """Defaults to 5s sleep when retry-after header is missing."""
        error_429 = _make_http_status_error(429, headers={})
        llm_service._llm.ainvoke = AsyncMock(
            side_effect=[error_429, "success"]
        )

        from tenacity import wait_none

        llm_service._invoke_with_tenacity.retry.wait = wait_none()

        with patch("services.llm_service.asyncio.sleep", new_callable=AsyncMock) as mock_sleep:
            result = await llm_service.invoke_with_retry(
                [{"role": "user", "content": "hi"}]
            )

        assert result == "success"
        mock_sleep.assert_any_call(5)

    @pytest.mark.asyncio
    async def test_non_retryable_4xx_raises_immediately(self, llm_service):
        """Non-retryable 4xx errors (e.g., 401) raise LLMInvocationError immediately."""
        error_401 = _make_http_status_error(401)
        llm_service._llm.ainvoke = AsyncMock(side_effect=error_401)

        from tenacity import wait_none

        llm_service._invoke_with_tenacity.retry.wait = wait_none()

        with pytest.raises(LLMInvocationError) as exc_info:
            await llm_service.invoke_with_retry(
                [{"role": "user", "content": "hi"}]
            )

        assert exc_info.value.status_code == 401
        # Should only attempt once — no retry for 401
        assert llm_service._llm.ainvoke.call_count == 1


class TestLLMInvocationError:
    """Tests for the LLMInvocationError exception class."""

    def test_has_status_code_and_message(self):
        """LLMInvocationError stores status_code and message."""
        err = LLMInvocationError(status_code=503, message="Service unavailable")
        assert err.status_code == 503
        assert err.message == "Service unavailable"
        assert str(err) == "Service unavailable"

    def test_none_status_code_for_timeout(self):
        """LLMInvocationError can have None status_code for timeouts."""
        err = LLMInvocationError(status_code=None, message="Timed out")
        assert err.status_code is None
        assert err.message == "Timed out"
