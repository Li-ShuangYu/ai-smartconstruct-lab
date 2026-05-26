"""In-memory sliding window rate limiter for FastAPI endpoints.

Provides a simple per-endpoint rate limiting dependency that tracks
request timestamps and rejects requests exceeding the configured limit.
"""

import time
from collections import deque
from fastapi import HTTPException, Request


class RateLimiter:
    """Sliding window rate limiter using an in-memory deque of timestamps.

    Args:
        max_requests: Maximum number of requests allowed within the window.
        window_seconds: Duration of the sliding window in seconds.
    """

    def __init__(self, max_requests: int = 10, window_seconds: int = 60):
        self.max_requests = max_requests
        self.window_seconds = window_seconds
        self._timestamps: deque = deque()

    def _cleanup(self, now: float) -> None:
        """Remove timestamps that have fallen outside the sliding window."""
        cutoff = now - self.window_seconds
        while self._timestamps and self._timestamps[0] <= cutoff:
            self._timestamps.popleft()

    async def __call__(self, request: Request) -> None:
        """FastAPI dependency that enforces the rate limit.

        Raises HTTPException 429 when the limit is exceeded.
        """
        now = time.time()
        self._cleanup(now)

        if len(self._timestamps) >= self.max_requests:
            raise HTTPException(
                status_code=429,
                detail={
                    "error_code": "RATE_LIMIT_EXCEEDED",
                    "error_message": (
                        f"Rate limit exceeded: maximum {self.max_requests} "
                        f"requests per {self.window_seconds} seconds. "
                        "Please retry later."
                    ),
                },
            )

        self._timestamps.append(now)
