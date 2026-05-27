"""
Test fixture validation — verifies the complete-orchestration.json fixture
produces exactly the expected number of AI tasks.

Task 8.7: The sample JSON (16 nodes) should produce exactly 10 AI tasks
(nodes with AI flags enabled).
"""

import sys

sys.path.insert(0, ".")

import json
import os

import pytest

from agents.supervisor import SupervisorAgent, detect_ai_nodes
from models.schemas import AgentType


# ---------------------------------------------------------------------------
# Load the fixture
# ---------------------------------------------------------------------------

FIXTURE_PATH = os.path.join(
    os.path.dirname(__file__), "fixtures", "complete_orchestration.json"
)


@pytest.fixture
def complete_canvas():
    """Load the complete orchestration JSON fixture."""
    with open(FIXTURE_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


# ---------------------------------------------------------------------------
# Tests
# ---------------------------------------------------------------------------


class TestFixtureNodeCount:
    """Verify the fixture has exactly 16 nodes."""

    def test_fixture_has_16_nodes(self, complete_canvas):
        """The complete orchestration fixture contains exactly 16 nodes."""
        assert len(complete_canvas["nodes"]) == 16

    def test_fixture_has_edges(self, complete_canvas):
        """The fixture has edges connecting the nodes."""
        assert len(complete_canvas["edges"]) == 15  # 16 nodes, linear chain


class TestAiTaskDetection:
    """Verify detect_ai_nodes produces exactly 10 tasks from the 16-node fixture."""

    def test_produces_exactly_10_ai_tasks(self, complete_canvas):
        """
        The 16-node fixture should produce exactly 10 AI tasks.

        AI-enabled nodes (10):
        - node-01: START with enable_ai_welcome=true → TEXT_AGENT
        - node-02: RESOURCE_READ with enable_ai_summary=true → TEXT_AGENT
        - node-03: VIDEO_LEARN with enable_ai_chapter=true → VIDEO_AGENT
        - node-04: MINDMAP_PREVIEW with enable_ai_generate_map=true → STRUCT_AGENT
        - node-05: THEORY_CLASS with enable_ai_error_book=true → EXAM_AGENT
        - node-06: TASK_DISTRIBUTE with enable_ai_task_split=true → STRUCT_AGENT
        - node-07: HOMEWORK with source_mode="ai" → EXAM_AGENT
        - node-08: CODING_CLASS with enable_code_review=true → CODE_AGENT
        - node-09: PLAN_UPLOAD with enable_ai_pre_evaluation=true → CODE_AGENT
        - node-10: AI_PRACTICE with enable_ai_system_prompt=true → TEXT_AGENT

        Non-AI nodes (6):
        - node-11: SEMESTER_SURVEY (no AI flags)
        - node-12: MINDMAP_DRAW with enable_ai_generate_map=false
        - node-13: HOMEWORK with source_mode="manual"
        - node-14: RESOURCE_READ (no AI flags)
        - node-15: VIDEO_LEARN (no AI flags)
        - node-16: END (no AI flags)
        """
        tasks = detect_ai_nodes(complete_canvas)
        assert len(tasks) == 10

    def test_correct_agent_type_routing(self, complete_canvas):
        """Each AI task is routed to the correct agent type."""
        tasks = detect_ai_nodes(complete_canvas)
        task_map = {t.node_id: t for t in tasks}

        # TEXT_AGENT nodes
        assert task_map["node-01"].agent_type == AgentType.TEXT
        assert task_map["node-02"].agent_type == AgentType.TEXT
        assert task_map["node-10"].agent_type == AgentType.TEXT

        # VIDEO_AGENT nodes
        assert task_map["node-03"].agent_type == AgentType.VIDEO

        # STRUCT_AGENT nodes
        assert task_map["node-04"].agent_type == AgentType.STRUCT
        assert task_map["node-06"].agent_type == AgentType.STRUCT

        # EXAM_AGENT nodes
        assert task_map["node-05"].agent_type == AgentType.EXAM
        assert task_map["node-07"].agent_type == AgentType.EXAM

        # CODE_AGENT nodes
        assert task_map["node-08"].agent_type == AgentType.CODE
        assert task_map["node-09"].agent_type == AgentType.CODE

    def test_non_ai_nodes_excluded(self, complete_canvas):
        """Non-AI nodes are not included in the task list."""
        tasks = detect_ai_nodes(complete_canvas)
        task_node_ids = {t.node_id for t in tasks}

        # These should NOT be in the task list
        assert "node-11" not in task_node_ids  # SEMESTER_SURVEY
        assert "node-12" not in task_node_ids  # MINDMAP_DRAW (ai=false)
        assert "node-13" not in task_node_ids  # HOMEWORK (manual)
        assert "node-14" not in task_node_ids  # RESOURCE_READ (no flags)
        assert "node-15" not in task_node_ids  # VIDEO_LEARN (no flags)
        assert "node-16" not in task_node_ids  # END

    def test_execution_plan_from_fixture(self, complete_canvas):
        """SupervisorAgent.analyze_orchestration produces correct plan from fixture."""
        supervisor = SupervisorAgent()
        plan = supervisor.analyze_orchestration(complete_canvas)

        assert plan.orchestration_id == "complete-orch-001"
        assert plan.total_tasks == 10
        assert len(plan.parallel_jobs) == 10

    def test_agent_type_distribution(self, complete_canvas):
        """Verify the distribution of agent types across the 10 tasks."""
        tasks = detect_ai_nodes(complete_canvas)

        type_counts = {}
        for task in tasks:
            agent_type = task.agent_type.value
            type_counts[agent_type] = type_counts.get(agent_type, 0) + 1

        assert type_counts["TEXT_AGENT"] == 3
        assert type_counts["VIDEO_AGENT"] == 1
        assert type_counts["STRUCT_AGENT"] == 2
        assert type_counts["EXAM_AGENT"] == 2
        assert type_counts["CODE_AGENT"] == 2
