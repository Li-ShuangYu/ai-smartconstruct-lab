"""Tests for rate limiting on the /api/orchestration/process endpoint (Task 4.7).

Covers:
- Requests within the limit succeed (202)
- The 11th request within 60 seconds returns 429
- Error response contains error_code and error_message
- After the window expires, requests are allowed again
"""

import time
from unittest.mock import patch

import pytest
from fastapi.testclient import TestClient

from main import app
from routers.orchestration import _job_status, process_rate_limiter


@pytest.fixture(autouse=True)
def reset_state():
    """Clear job status and rate limiter state between tests."""
    _job_status.clear()
    process_rate_limiter._timestamps.clear()
    yield
    _job_status.clear()
    process_rate_limiter._timestamps.clear()


@pytest.fixture
def client():
    return TestClient(app)


def _valid_request() -> dict:
    """Return a minimal valid orchestration request."""
    return {
        "template_id": 1,
        "orchestration_id": "orch-001",
        "canvas_json": {
            "nodes": [
                {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": True}}
            ],
            "edges": [{"source": "n1", "target": "n2"}],
        },
        "callback_url": "http://localhost:8080/api/internal/ai-callback",
    }


class TestRateLimiting:
    """Test rate limiting behavior on the process endpoint."""

    def test_requests_within_limit_succeed(self, client):
        """First 10 requests within the window should all return 202."""
        req = _valid_request()
        for i in range(10):
            resp = client.post("/api/orchestration/process", json=req)
            assert resp.status_code == 202, f"Request {i + 1} should succeed"

    def test_11th_request_returns_429(self, client):
        """The 11th request within 60 seconds should be rate limited."""
        req = _valid_request()
        # Send 10 successful requests
        for _ in range(10):
            resp = client.post("/api/orchestration/process", json=req)
            assert resp.status_code == 202

        # 11th request should be rejected
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 429

    def test_429_response_format(self, client):
        """Rate limit response should contain error_code and error_message."""
        req = _valid_request()
        for _ in range(10):
            client.post("/api/orchestration/process", json=req)

        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 429
        body = resp.json()["detail"]
        assert body["error_code"] == "RATE_LIMIT_EXCEEDED"
        assert "error_message" in body
        assert "10" in body["error_message"]

    def test_requests_allowed_after_window_expires(self, client):
        """After the 60-second window passes, requests should be allowed again."""
        req = _valid_request()

        # Fill up the rate limit
        for _ in range(10):
            client.post("/api/orchestration/process", json=req)

        # Simulate time passing beyond the window
        future_time = time.time() + 61
        with patch("middleware.rate_limiter.time.time", return_value=future_time):
            resp = client.post("/api/orchestration/process", json=req)
            assert resp.status_code == 202

    def test_rate_limit_does_not_affect_status_endpoint(self, client):
        """The GET /api/orchestration/status endpoint should not be rate limited."""
        req = _valid_request()
        # Exhaust the rate limit on the process endpoint
        for _ in range(10):
            client.post("/api/orchestration/process", json=req)

        # Status endpoint should still work
        resp = client.get("/api/orchestration/status/nonexistent-job")
        assert resp.status_code == 404  # Not 429

    def test_sliding_window_allows_new_requests_as_old_ones_expire(self, client):
        """As old timestamps fall outside the window, new requests are allowed."""
        req = _valid_request()

        # Simulate 10 requests at time T=0
        base_time = time.time()
        with patch("middleware.rate_limiter.time.time", return_value=base_time):
            for _ in range(10):
                client.post("/api/orchestration/process", json=req)

        # At T=61, all old timestamps should have expired
        with patch("middleware.rate_limiter.time.time", return_value=base_time + 61):
            resp = client.post("/api/orchestration/process", json=req)
            assert resp.status_code == 202
