"""Tests for orchestration router request validation (Task 4.6).

Covers:
- Missing canvas_json fields (400)
- Invalid JSON structure via Pydantic (422)
- Overload protection (503)
- Consistent error response format (error_code + error_message)
"""

import pytest
from fastapi.testclient import TestClient

from main import app
from routers.orchestration import MAX_ACTIVE_JOBS, process_rate_limiter
from services.job_store import job_store


@pytest.fixture(autouse=True)
def clear_job_status():
    """Clear job status and rate limiter state between tests."""
    job_store._jobs.clear()
    process_rate_limiter._timestamps.clear()
    yield
    job_store._jobs.clear()
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


class TestCanvasJsonValidation:
    """Test 400 responses for invalid canvas_json structure."""

    def test_missing_nodes_key(self, client):
        req = _valid_request()
        req["canvas_json"] = {"edges": []}
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 400
        body = resp.json()["detail"]
        assert body["error_code"] == "INVALID_CANVAS_STRUCTURE"
        assert "nodes" in body["error_message"]

    def test_nodes_not_a_list(self, client):
        req = _valid_request()
        req["canvas_json"] = {"nodes": "not-a-list", "edges": []}
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 400
        body = resp.json()["detail"]
        assert body["error_code"] == "INVALID_CANVAS_STRUCTURE"

    def test_missing_edges_key(self, client):
        req = _valid_request()
        req["canvas_json"] = {
            "nodes": [{"node_id": "n1", "node_type": "START", "config": {}}]
        }
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 400
        body = resp.json()["detail"]
        assert body["error_code"] == "INVALID_CANVAS_STRUCTURE"
        assert "edges" in body["error_message"]

    def test_edges_not_a_list(self, client):
        req = _valid_request()
        req["canvas_json"] = {
            "nodes": [{"node_id": "n1", "node_type": "START", "config": {}}],
            "edges": "not-a-list",
        }
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 400
        body = resp.json()["detail"]
        assert body["error_code"] == "INVALID_CANVAS_STRUCTURE"

    def test_node_missing_node_id(self, client):
        req = _valid_request()
        req["canvas_json"] = {
            "nodes": [{"node_type": "START", "config": {}}],
            "edges": [],
        }
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 400
        body = resp.json()["detail"]
        assert body["error_code"] == "MISSING_NODE_FIELDS"
        assert "node_id" in body["error_message"]

    def test_node_missing_node_type(self, client):
        req = _valid_request()
        req["canvas_json"] = {
            "nodes": [{"node_id": "n1", "config": {}}],
            "edges": [],
        }
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 400
        body = resp.json()["detail"]
        assert body["error_code"] == "MISSING_NODE_FIELDS"
        assert "node_type" in body["error_message"]

    def test_node_missing_config(self, client):
        req = _valid_request()
        req["canvas_json"] = {
            "nodes": [{"node_id": "n1", "node_type": "START"}],
            "edges": [],
        }
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 400
        body = resp.json()["detail"]
        assert body["error_code"] == "MISSING_NODE_FIELDS"
        assert "config" in body["error_message"]

    def test_node_missing_multiple_fields(self, client):
        req = _valid_request()
        req["canvas_json"] = {
            "nodes": [{"node_id": "n1"}],
            "edges": [],
        }
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 400
        body = resp.json()["detail"]
        assert body["error_code"] == "MISSING_NODE_FIELDS"
        assert "node_type" in body["error_message"]
        assert "config" in body["error_message"]

    def test_node_not_a_dict(self, client):
        req = _valid_request()
        req["canvas_json"] = {
            "nodes": ["not-a-dict"],
            "edges": [],
        }
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 400
        body = resp.json()["detail"]
        assert body["error_code"] == "INVALID_NODE_STRUCTURE"


class TestPydanticValidation:
    """Test 422 responses for invalid request structure (handled by FastAPI/Pydantic)."""

    def test_missing_template_id(self, client):
        req = _valid_request()
        del req["template_id"]
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 422
        body = resp.json()
        assert body["error_code"] == "VALIDATION_ERROR"
        assert "error_message" in body

    def test_invalid_template_id_type(self, client):
        req = _valid_request()
        req["template_id"] = "not-an-int"
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 422
        body = resp.json()
        assert body["error_code"] == "VALIDATION_ERROR"

    def test_template_id_not_positive(self, client):
        req = _valid_request()
        req["template_id"] = 0
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 422
        body = resp.json()
        assert body["error_code"] == "VALIDATION_ERROR"

    def test_missing_callback_url(self, client):
        req = _valid_request()
        del req["callback_url"]
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 422
        body = resp.json()
        assert body["error_code"] == "VALIDATION_ERROR"

    def test_invalid_json_body(self, client):
        resp = client.post(
            "/api/orchestration/process",
            content="not valid json {{{",
            headers={"Content-Type": "application/json"},
        )
        assert resp.status_code == 422
        body = resp.json()
        assert body["error_code"] == "VALIDATION_ERROR"


class TestOverloadProtection:
    """Test 503 responses when service is at capacity."""

    def test_overloaded_returns_503(self, client):
        # Fill up the job status with active jobs
        for i in range(MAX_ACTIVE_JOBS):
            job_store._jobs[f"job-{i}"] = {
                "job_id": f"job-{i}",
                "status": "accepted",
            }

        req = _valid_request()
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 503
        body = resp.json()["detail"]
        assert body["error_code"] == "SERVICE_OVERLOADED"
        assert "error_message" in body

    def test_completed_jobs_dont_count_toward_limit(self, client):
        # Fill with completed jobs — should not trigger overload
        for i in range(MAX_ACTIVE_JOBS + 10):
            job_store._jobs[f"job-{i}"] = {
                "job_id": f"job-{i}",
                "status": "completed",
            }

        req = _valid_request()
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 202

    def test_just_under_limit_succeeds(self, client):
        # Fill to one below the limit
        for i in range(MAX_ACTIVE_JOBS - 1):
            job_store._jobs[f"job-{i}"] = {
                "job_id": f"job-{i}",
                "status": "processing",
            }

        req = _valid_request()
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 202


class TestValidRequestSucceeds:
    """Verify that a valid request still works correctly."""

    def test_valid_request_returns_202(self, client):
        req = _valid_request()
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 202
        body = resp.json()
        assert body["status"] == "accepted"
        assert "job_id" in body
        assert body["estimated_duration_seconds"] > 0

    def test_empty_nodes_list_is_valid(self, client):
        req = _valid_request()
        req["canvas_json"] = {"nodes": [], "edges": []}
        resp = client.post("/api/orchestration/process", json=req)
        assert resp.status_code == 202
