"""
Callback Service — sends AI processing results to the Java backend.

POSTs the AiCallbackPayload to the callback_url with retry logic:
- 3 retries with delays of 5s, 15s, 30s
- Uses httpx async client for non-blocking HTTP
"""

import logging

import httpx
from tenacity import (
    retry,
    stop_after_attempt,
    wait_fixed,
    retry_if_exception_type,
    before_sleep_log,
)

from models.callback import AiCallbackPayload

logger = logging.getLogger(__name__)

# Retry delays: 5s, 15s, 30s (custom wait strategy)
RETRY_DELAYS = [5, 15, 30]


class _CallbackRetryWait:
    """Custom wait strategy for callback retries: 5s, 15s, 30s."""

    def __call__(self, retry_state):
        attempt = retry_state.attempt_number
        if attempt <= len(RETRY_DELAYS):
            return RETRY_DELAYS[attempt - 1]
        return RETRY_DELAYS[-1]


@retry(
    stop=stop_after_attempt(3),
    wait=_CallbackRetryWait(),
    retry=retry_if_exception_type((httpx.HTTPStatusError, httpx.RequestError)),
    before_sleep=before_sleep_log(logger, logging.WARNING),
    reraise=True,
)
async def send_callback(callback_url: str, payload: AiCallbackPayload) -> None:
    """
    POST AI processing results to the Java backend callback URL.

    Retries up to 3 times with delays of 5s, 15s, 30s on HTTP errors
    or connection failures.

    Args:
        callback_url: The URL to POST results to.
        payload: The AiCallbackPayload containing results or error info.

    Raises:
        httpx.HTTPStatusError: If all retries fail with HTTP error responses.
        httpx.RequestError: If all retries fail with connection errors.
    """
    logger.info(
        "Sending callback to %s (template_id=%d, status=%d)",
        callback_url,
        payload.template_id,
        payload.status,
    )

    async with httpx.AsyncClient(timeout=30.0) as client:
        response = await client.post(
            callback_url,
            json=payload.model_dump(exclude_none=True),
            headers={"Content-Type": "application/json"},
        )
        response.raise_for_status()

    logger.info(
        "Callback sent successfully to %s (template_id=%d)",
        callback_url,
        payload.template_id,
    )
