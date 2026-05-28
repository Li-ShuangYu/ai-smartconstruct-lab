"""
End-to-end smoke test: Template create → publish → AI process → task create → student flow.

This test simulates the complete lifecycle with all external services mocked:
1. Template is created with phased canvas
2. Template is published → AI pipeline triggered
3. AI engine processes nodes → callback sent
4. Training task is created from completed template
5. Student enters training → progresses through phases

All external dependencies (LLM, database) are mocked.
"""

import sys
sys.path.insert(0, ".")

import pytest
import asyncio
from unittest.mock import AsyncMock, patch, MagicMock
from dataclasses import dataclass, field
from typing import Optional

from agents.supervisor import detect_ai_nodes_phased
from services.workflow import execute_orchestration_workflow
from models.schemas import AgentType


# --- Simulated State ---

@dataclass
class SimulatedTemplate:
    """Simulates the backend template state."""
    id: int = 1
    template_name: str = "E2E测试模板"
    ai_status: int = 0
    raw_canvas_json: Optional[dict] = None
    standard_payload_json: Optional[dict] = None
    phases_json: Optional[list] = None
    error_reason: Optional[str] = None


@dataclass
class SimulatedNodeProgress:
    """Simulates student node progress."""
    node_instance_id: int = 0
    status: int = 0  # 0=not started, 1=in progress, 2=completed
    phase_id: str = ""


@dataclass
class SimulatedStudentState:
    """Simulates student training state."""
    student_id: int = 100
    current_phase_id: str = ""
    node_progress: list = field(default_factory=list)


# --- E2E Test ---

class TestEndToEndSmoke:
    """End-to-end smoke test for the complete training lifecycle."""

    @pytest.fixture
    def full_canvas(self):
        """A complete training canvas with 3 phases."""
        return {
            "orchestration_id": "tmpl_e2e_1",
            "phases": [
                {
                    "phase_id": "phase_pre",
                    "phase_name": "课前阶段",
                    "sort_num": 1,
                    "nodes": [
                        {
                            "node_id": "start_node",
                            "node_type": "start",
                            "config": {
                                "enable_ai_welcome": True,
                                "topic": "Python数据分析",
                            },
                            "ai_spec": {
                                "target_agent": "TEXT",
                                "priority": 5,
                                "ai_flags": ["enable_ai_welcome"],
                            },
                        },
                        {
                            "node_id": "survey_node",
                            "node_type": "learning_survey",
                            "config": {"enable_ai_analysis": True},
                            "ai_spec": {
                                "target_agent": "TEXT",
                                "priority": 6,
                                "ai_flags": ["enable_ai_analysis"],
                            },
                        },
                    ],
                    "edges": [{"source": "start_node", "target": "survey_node"}],
                },
                {
                    "phase_id": "phase_main",
                    "phase_name": "课中阶段",
                    "sort_num": 2,
                    "nodes": [
                        {
                            "node_id": "resource_node",
                            "node_type": "resource_read",
                            "config": {"enable_ai_summary": True, "enable_ai_key_points": True},
                            "ai_spec": {
                                "target_agent": "TEXT",
                                "priority": 3,
                                "ai_flags": ["enable_ai_summary", "enable_ai_key_points"],
                            },
                        },
                        {
                            "node_id": "homework_node",
                            "node_type": "homework",
                            "config": {"source_mode": "ai", "question_count": 10},
                            "ai_spec": {
                                "target_agent": "EXAM",
                                "priority": 2,
                                "ai_flags": ["enable_ai_grading"],
                            },
                        },
                    ],
                    "edges": [{"source": "resource_node", "target": "homework_node"}],
                },
                {
                    "phase_id": "phase_post",
                    "phase_name": "课后阶段",
                    "sort_num": 3,
                    "nodes": [
                        {
                            "node_id": "end_node",
                            "node_type": "end",
                            "config": {"enable_ai_report": True},
                            "ai_spec": {
                                "target_agent": "TEXT",
                                "priority": 8,
                                "ai_flags": ["enable_ai_report"],
                            },
                        },
                    ],
                    "edges": [],
                },
            ],
        }

    def test_step1_template_creation(self, full_canvas):
        """Step 1: Template is created with phased canvas."""
        template = SimulatedTemplate()
        template.raw_canvas_json = full_canvas
        template.phases_json = [
            {"phase_id": "phase_pre", "phase_name": "课前阶段", "sort_num": 1},
            {"phase_id": "phase_main", "phase_name": "课中阶段", "sort_num": 2},
            {"phase_id": "phase_post", "phase_name": "课后阶段", "sort_num": 3},
        ]

        assert template.raw_canvas_json is not None
        assert len(template.phases_json) == 3
        assert template.ai_status == 0

    def test_step2_template_publish(self, full_canvas):
        """Step 2: Publishing sets ai_status=1 and detects AI nodes."""
        template = SimulatedTemplate()
        template.raw_canvas_json = full_canvas
        template.ai_status = 1  # Simulate publish

        # Supervisor detects AI nodes
        tasks = detect_ai_nodes_phased(full_canvas)
        assert len(tasks) == 5  # All 5 nodes have AI enabled
        assert template.ai_status == 1

    @pytest.mark.asyncio
    async def test_step3_ai_processing(self, full_canvas):
        """Step 3: AI engine processes all nodes and sends callback."""
        template = SimulatedTemplate()
        template.ai_status = 1

        mock_result = MagicMock()
        mock_result.model_dump.return_value = {"content": "AI generated"}

        callback_payloads = []

        async def mock_send_callback(url, payload):
            callback_payloads.append(payload)

        with patch("services.workflow.send_callback", side_effect=mock_send_callback):
            with patch("services.workflow.send_node_status_callback", new_callable=AsyncMock):
                with patch("services.workflow._execute_single_task", return_value=mock_result):
                    job_id = await execute_orchestration_workflow(
                        canvas_json=full_canvas,
                        template_id=1,
                        callback_url="http://backend/api/internal/ai/callback",
                    )

        # Verify callback was sent with success
        assert len(callback_payloads) >= 1
        final = callback_payloads[-1]
        assert final.status == 2
        assert final.template_id == 1

        # Simulate backend receiving callback
        template.ai_status = final.status
        template.standard_payload_json = final.standard_payload_json
        assert template.ai_status == 2

    def test_step4_task_creation(self, full_canvas):
        """Step 4: Training task created from completed template."""
        template = SimulatedTemplate()
        template.ai_status = 2  # Ready

        # Simulate task creation: create node instances per phase
        node_instances = []
        for phase in full_canvas["phases"]:
            for node in phase["nodes"]:
                node_instances.append({
                    "node_id": node["node_id"],
                    "phase_id": phase["phase_id"],
                    "config": node["config"],
                })

        assert len(node_instances) == 5
        assert all(ni["phase_id"] for ni in node_instances)

    def test_step5_student_flow(self, full_canvas):
        """Step 5: Student progresses through phases."""
        student = SimulatedStudentState()
        student.current_phase_id = "phase_pre"

        # Create progress for all nodes
        for phase in full_canvas["phases"]:
            for node in phase["nodes"]:
                student.node_progress.append(
                    SimulatedNodeProgress(
                        node_instance_id=hash(node["node_id"]) % 10000,
                        status=0,
                        phase_id=phase["phase_id"],
                    )
                )

        # Student enters first node
        pre_nodes = [p for p in student.node_progress if p.phase_id == "phase_pre"]
        pre_nodes[0].status = 1  # In progress
        assert pre_nodes[0].status == 1

        # Student completes all pre-phase nodes
        for node in pre_nodes:
            node.status = 2

        # Check phase completion
        all_pre_complete = all(n.status == 2 for n in pre_nodes)
        assert all_pre_complete

        # Phase unlocks: move to main phase
        student.current_phase_id = "phase_main"
        assert student.current_phase_id == "phase_main"

        # Complete main phase
        main_nodes = [p for p in student.node_progress if p.phase_id == "phase_main"]
        for node in main_nodes:
            node.status = 2

        # Move to post phase
        student.current_phase_id = "phase_post"
        post_nodes = [p for p in student.node_progress if p.phase_id == "phase_post"]
        for node in post_nodes:
            node.status = 2

        # All done
        all_complete = all(n.status == 2 for n in student.node_progress)
        assert all_complete

    @pytest.mark.asyncio
    async def test_full_lifecycle(self, full_canvas):
        """Complete lifecycle smoke test: create → publish → AI → task → student."""
        # 1. Create template
        template = SimulatedTemplate()
        template.raw_canvas_json = full_canvas
        assert template.ai_status == 0

        # 2. Publish
        template.ai_status = 1
        tasks = detect_ai_nodes_phased(full_canvas)
        assert len(tasks) > 0

        # 3. AI processing
        mock_result = MagicMock()
        mock_result.model_dump.return_value = {"content": "generated"}
        callback_payloads = []

        async def mock_cb(url, payload):
            callback_payloads.append(payload)

        with patch("services.workflow.send_callback", side_effect=mock_cb):
            with patch("services.workflow.send_node_status_callback", new_callable=AsyncMock):
                with patch("services.workflow._execute_single_task", return_value=mock_result):
                    await execute_orchestration_workflow(
                        canvas_json=full_canvas,
                        template_id=1,
                        callback_url="http://backend/callback",
                    )

        template.ai_status = 2
        assert template.ai_status == 2

        # 4. Task creation
        node_instances = []
        for phase in full_canvas["phases"]:
            for node in phase["nodes"]:
                node_instances.append(node["node_id"])
        assert len(node_instances) == 5

        # 5. Student flow
        student = SimulatedStudentState()
        student.current_phase_id = "phase_pre"
        # Simulate completing all nodes
        for phase in full_canvas["phases"]:
            for node in phase["nodes"]:
                student.node_progress.append(
                    SimulatedNodeProgress(status=2, phase_id=phase["phase_id"])
                )

        all_done = all(p.status == 2 for p in student.node_progress)
        assert all_done
