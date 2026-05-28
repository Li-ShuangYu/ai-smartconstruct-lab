"""
Integration test: Selective retry with mixed success/failure nodes.

Tests the retry workflow:
1. Initial processing has some nodes succeed and some fail
2. Retry is triggered for only the failed nodes
3. Successful nodes are not reprocessed
4. Retry results are correctly aggregated
"""

import sys
sys.path.insert(0, ".")

import pytest
import asyncio
from unittest.mock import AsyncMock, patch, MagicMock

from services.workflow import execute_retry_workflow, execute_orchestration_workflow
from agents.supervisor import detect_ai_nodes_phased
from services.job_store import job_store


@pytest.fixture
def canvas_with_mixed_nodes():
    """Canvas with 4 nodes: 2 will succeed, 2 will fail."""
    return {
        "orchestration_id": "tmpl_retry_test",
        "phases": [
            {
                "phase_id": "p1",
                "phase_name": "Phase 1",
                "sort_num": 1,
                "nodes": [
                    {
                        "node_id": "success_1",
                        "node_type": "resource_read",
                        "config": {"enable_ai_summary": True},
                        "ai_spec": {
                            "target_agent": "TEXT",
                            "priority": 3,
                            "ai_flags": ["enable_ai_summary"],
                        },
                    },
                    {
                        "node_id": "success_2",
                        "node_type": "start",
                        "config": {"enable_ai_welcome": True},
                        "ai_spec": {
                            "target_agent": "TEXT",
                            "priority": 5,
                            "ai_flags": ["enable_ai_welcome"],
                        },
                    },
                    {
                        "node_id": "fail_1",
                        "node_type": "homework",
                        "config": {"source_mode": "ai"},
                        "ai_spec": {
                            "target_agent": "EXAM",
                            "priority": 2,
                            "ai_flags": ["enable_ai_grading"],
                        },
                    },
                    {
                        "node_id": "fail_2",
                        "node_type": "exam",
                        "config": {"source_mode": "ai"},
                        "ai_spec": {
                            "target_agent": "EXAM",
                            "priority": 1,
                            "ai_flags": ["enable_ai_grading"],
                        },
                    },
                ],
                "edges": [],
            },
        ],
    }


class TestSelectiveRetryIntegration:
    """Integration tests for selective retry workflow."""

    @pytest.mark.asyncio
    async def test_retry_only_processes_failed_nodes(self, canvas_with_mixed_nodes):
        """Retry workflow only processes the specified failed node IDs."""
        failed_node_ids = ["fail_1", "fail_2"]
        processed_node_ids = []

        mock_result = MagicMock()
        mock_result.model_dump.return_value = {"generated": "retry_content"}

        async def mock_execute_task(task, llm_service):
            processed_node_ids.append(task.node_id)
            return mock_result

        callback_payloads = []

        async def mock_send_callback(url, payload):
            callback_payloads.append(payload)

        with patch("services.workflow.send_callback", side_effect=mock_send_callback):
            with patch("services.workflow.send_node_status_callback", new_callable=AsyncMock):
                with patch("services.workflow._execute_single_task", side_effect=mock_execute_task):
                    job_id = await execute_retry_workflow(
                        canvas_json=canvas_with_mixed_nodes,
                        template_id=1,
                        callback_url="http://localhost:8080/api/internal/ai/callback",
                        node_ids=failed_node_ids,
                    )

        # Only failed nodes should have been processed
        assert set(processed_node_ids) == set(failed_node_ids)
        assert "success_1" not in processed_node_ids
        assert "success_2" not in processed_node_ids

    @pytest.mark.asyncio
    async def test_retry_success_sends_status_2_callback(self, canvas_with_mixed_nodes):
        """When all retry nodes succeed, callback has status=2."""
        failed_node_ids = ["fail_1", "fail_2"]

        mock_result = MagicMock()
        mock_result.model_dump.return_value = {"generated": "content"}

        callback_payloads = []

        async def mock_send_callback(url, payload):
            callback_payloads.append(payload)

        with patch("services.workflow.send_callback", side_effect=mock_send_callback):
            with patch("services.workflow.send_node_status_callback", new_callable=AsyncMock):
                with patch("services.workflow._execute_single_task", return_value=mock_result):
                    await execute_retry_workflow(
                        canvas_json=canvas_with_mixed_nodes,
                        template_id=1,
                        callback_url="http://localhost:8080/api/internal/ai/callback",
                        node_ids=failed_node_ids,
                    )

        assert len(callback_payloads) >= 1
        final_callback = callback_payloads[-1]
        assert final_callback.status == 2

    @pytest.mark.asyncio
    async def test_retry_partial_failure_sends_status_3(self, canvas_with_mixed_nodes):
        """When some retry nodes fail again, callback has status=3."""
        failed_node_ids = ["fail_1", "fail_2"]
        call_count = 0

        mock_result = MagicMock()
        mock_result.model_dump.return_value = {"generated": "content"}

        async def mock_execute_task(task, llm_service):
            nonlocal call_count
            call_count += 1
            if task.node_id == "fail_2":
                raise Exception("LLM timeout again")
            return mock_result

        callback_payloads = []

        async def mock_send_callback(url, payload):
            callback_payloads.append(payload)

        with patch("services.workflow.send_callback", side_effect=mock_send_callback):
            with patch("services.workflow.send_node_status_callback", new_callable=AsyncMock):
                with patch("services.workflow._execute_single_task", side_effect=mock_execute_task):
                    await execute_retry_workflow(
                        canvas_json=canvas_with_mixed_nodes,
                        template_id=1,
                        callback_url="http://localhost:8080/api/internal/ai/callback",
                        node_ids=failed_node_ids,
                    )

        assert len(callback_payloads) >= 1
        final_callback = callback_payloads[-1]
        assert final_callback.status == 3  # Partial failure

    @pytest.mark.asyncio
    async def test_retry_empty_node_list_immediate_success(self, canvas_with_mixed_nodes):
        """Retry with node IDs that don't match any AI nodes returns immediate success."""
        callback_payloads = []

        async def mock_send_callback(url, payload):
            callback_payloads.append(payload)

        with patch("services.workflow.send_callback", side_effect=mock_send_callback):
            await execute_retry_workflow(
                canvas_json=canvas_with_mixed_nodes,
                template_id=1,
                callback_url="http://localhost:8080/api/internal/ai/callback",
                node_ids=["nonexistent_node"],
            )

        assert len(callback_payloads) == 1
        assert callback_payloads[0].status == 2  # No matching nodes → success

    @pytest.mark.asyncio
    async def test_retry_endpoint_validates_request(self, canvas_with_mixed_nodes):
        """The retry endpoint validates canvas structure correctly."""
        from routers.orchestration import _validate_canvas_json

        # Valid canvas should not raise
        _validate_canvas_json(canvas_with_mixed_nodes)

        # Canvas with invalid phase structure should raise
        import pytest
        from fastapi import HTTPException
        with pytest.raises(HTTPException):
            _validate_canvas_json({"phases": "not_a_list"})
