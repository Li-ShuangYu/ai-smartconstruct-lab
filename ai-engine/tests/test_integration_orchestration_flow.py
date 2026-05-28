"""
Integration test: Receive phased canvas → supervisor routing → agent execution → callback.

Tests the full AI engine orchestration flow with mocked LLM:
1. Receives a phased canvas JSON via the process endpoint
2. Supervisor detects AI-enabled nodes and routes to correct agents
3. Agents execute (with mocked LLM responses)
4. Callback is sent to the backend with results
"""

import sys
sys.path.insert(0, ".")

import pytest
import asyncio
from unittest.mock import AsyncMock, patch, MagicMock

from main import app
from models.schemas import AgentType
from agents.supervisor import detect_ai_nodes_phased
from services.job_store import job_store


@pytest.fixture
def phased_canvas():
    """A valid phased canvas with multiple AI-enabled nodes."""
    return {
        "orchestration_id": "tmpl_integration_1",
        "phases": [
            {
                "phase_id": "phase_pre",
                "phase_name": "课前阶段",
                "sort_num": 1,
                "nodes": [
                    {
                        "node_id": "node_start",
                        "node_type": "start",
                        "config": {"enable_ai_welcome": True, "topic": "Python基础"},
                        "ai_spec": {
                            "target_agent": "TEXT",
                            "priority": 5,
                            "ai_flags": ["enable_ai_welcome"],
                        },
                    },
                    {
                        "node_id": "node_survey",
                        "node_type": "learning_survey",
                        "config": {"enable_ai_analysis": False},
                        "ai_spec": {
                            "target_agent": "TEXT",
                            "priority": 6,
                            "ai_flags": ["enable_ai_analysis"],
                        },
                    },
                ],
                "edges": [{"source": "node_start", "target": "node_survey"}],
            },
            {
                "phase_id": "phase_main",
                "phase_name": "课中阶段",
                "sort_num": 2,
                "nodes": [
                    {
                        "node_id": "node_homework",
                        "node_type": "homework",
                        "config": {"source_mode": "ai", "question_count": 5},
                        "ai_spec": {
                            "target_agent": "EXAM",
                            "priority": 2,
                            "ai_flags": ["enable_ai_grading"],
                        },
                    },
                ],
                "edges": [],
            },
        ],
    }


class TestOrchestrationIntegrationFlow:
    """Integration test for the full orchestration pipeline."""

    def test_supervisor_routes_correctly(self, phased_canvas):
        """Supervisor detects AI-enabled nodes and routes to correct agents."""
        tasks = detect_ai_nodes_phased(phased_canvas)

        # node_start has enable_ai_welcome=True → should be detected
        # node_survey has enable_ai_analysis=False → should be skipped
        # node_homework has source_mode="ai" → should be detected
        assert len(tasks) == 2

        # Sorted by priority: homework (2) before start (5)
        assert tasks[0].node_id == "node_homework"
        assert tasks[0].agent_type == AgentType.EXAM
        assert tasks[0].phase_id == "phase_main"

        assert tasks[1].node_id == "node_start"
        assert tasks[1].agent_type == AgentType.TEXT
        assert tasks[1].phase_id == "phase_pre"

    def test_skipped_nodes_not_in_task_list(self, phased_canvas):
        """Nodes with all AI flags disabled are excluded."""
        tasks = detect_ai_nodes_phased(phased_canvas)
        task_node_ids = {t.node_id for t in tasks}
        assert "node_survey" not in task_node_ids

    @pytest.mark.asyncio
    async def test_process_endpoint_validates_canvas(self, phased_canvas):
        """The orchestration router validates phased canvas structure correctly."""
        from routers.orchestration import _validate_canvas_json

        # Valid canvas should not raise
        _validate_canvas_json(phased_canvas)

        # Invalid canvas (missing phases) should raise
        import pytest
        from fastapi import HTTPException
        with pytest.raises(HTTPException) as exc_info:
            _validate_canvas_json({"nodes": "not_a_list"})
        assert exc_info.value.status_code == 400

    @pytest.mark.asyncio
    async def test_workflow_with_mocked_llm_sends_callback(self, phased_canvas):
        """Full workflow with mocked LLM sends callback on completion."""
        from services.workflow import execute_orchestration_workflow

        callback_payloads = []

        async def mock_send_callback(url, payload):
            callback_payloads.append(payload)

        mock_result = MagicMock()
        mock_result.model_dump.return_value = {"generated": "content"}

        with patch("services.workflow.send_callback", side_effect=mock_send_callback):
            with patch("services.workflow.send_node_status_callback", new_callable=AsyncMock):
                with patch("services.workflow._execute_single_task", return_value=mock_result):
                    job_id = await execute_orchestration_workflow(
                        canvas_json=phased_canvas,
                        template_id=1,
                        callback_url="http://localhost:8080/api/internal/ai/callback",
                    )

        assert job_id is not None
        # Callback should have been sent
        assert len(callback_payloads) >= 1
        final_callback = callback_payloads[-1]
        assert final_callback.template_id == 1
        assert final_callback.status == 2  # Success

    @pytest.mark.asyncio
    async def test_no_ai_nodes_immediate_success(self):
        """Canvas with no AI-enabled nodes triggers immediate success callback."""
        from services.workflow import execute_orchestration_workflow

        canvas_no_ai = {
            "orchestration_id": "tmpl_no_ai",
            "phases": [{
                "phase_id": "p1",
                "phase_name": "Test",
                "sort_num": 1,
                "nodes": [{
                    "node_id": "n1",
                    "node_type": "end",
                    "config": {},
                }],
                "edges": [],
            }],
        }

        callback_payloads = []

        async def mock_send_callback(url, payload):
            callback_payloads.append(payload)

        with patch("services.workflow.send_callback", side_effect=mock_send_callback):
            job_id = await execute_orchestration_workflow(
                canvas_json=canvas_no_ai,
                template_id=2,
                callback_url="http://localhost:8080/api/internal/ai/callback",
            )

        assert len(callback_payloads) == 1
        assert callback_payloads[0].status == 2  # Immediate success
        assert callback_payloads[0].template_id == 2
