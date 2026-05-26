"""
Unit tests for SupervisorAgent covering all 16 node types from the sample JSON.

Tests verify:
- Correct routing of each AI-enabled node type to its domain agent
- Skipping of nodes without AI flags or unknown node types
- Deduplication (one task per node regardless of multiple AI flags)
- Complete canvas with mixed AI/non-AI nodes
- analyze_orchestration() returns proper ExecutionPlan
- Edge cases: empty nodes, missing config field
"""

import sys

sys.path.insert(0, ".")

import pytest

from agents.supervisor import SupervisorAgent, detect_ai_nodes, _has_ai_flags
from models.schemas import AgentType, ExecutionPlan


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_canvas(nodes, orchestration_id="test-orch"):
    """Helper to build a minimal canvas_json dict."""
    return {
        "orchestration_id": orchestration_id,
        "nodes": nodes,
        "edges": [],
    }


def _make_node(node_id, node_type, config=None):
    """Helper to build a single node dict."""
    node = {"node_id": node_id, "node_type": node_type}
    if config is not None:
        node["config"] = config
    else:
        node["config"] = {}
    return node


# ---------------------------------------------------------------------------
# Tests: Individual node type routing (scenarios 1-10)
# ---------------------------------------------------------------------------

class TestNodeTypeRouting:
    """Test that each AI-enabled node type routes to the correct agent."""

    def test_start_with_ai_welcome(self):
        """Scenario 1: START with enable_ai_welcome=True → TEXT_AGENT"""
        canvas = _make_canvas([
            _make_node("n1", "START", {"enable_ai_welcome": True}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 1
        assert tasks[0].agent_type == AgentType.TEXT
        assert tasks[0].node_type == "START"
        assert tasks[0].node_id == "n1"

    def test_resource_read_with_ai_summary(self):
        """Scenario 2: RESOURCE_READ with enable_ai_summary=True → TEXT_AGENT"""
        canvas = _make_canvas([
            _make_node("n2", "RESOURCE_READ", {"enable_ai_summary": True}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 1
        assert tasks[0].agent_type == AgentType.TEXT
        assert tasks[0].node_type == "RESOURCE_READ"

    def test_video_learn_with_ai_chapter(self):
        """Scenario 3: VIDEO_LEARN with enable_ai_chapter=True → VIDEO_AGENT"""
        canvas = _make_canvas([
            _make_node("n3", "VIDEO_LEARN", {"enable_ai_chapter": True}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 1
        assert tasks[0].agent_type == AgentType.VIDEO
        assert tasks[0].node_type == "VIDEO_LEARN"

    def test_mindmap_preview_with_ai_generate_map(self):
        """Scenario 4: MINDMAP_PREVIEW with enable_ai_generate_map=True → STRUCT_AGENT"""
        canvas = _make_canvas([
            _make_node("n4", "MINDMAP_PREVIEW", {"enable_ai_generate_map": True}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 1
        assert tasks[0].agent_type == AgentType.STRUCT
        assert tasks[0].node_type == "MINDMAP_PREVIEW"

    def test_theory_class_with_ai_error_book(self):
        """Scenario 5: THEORY_CLASS with enable_ai_error_book=True → EXAM_AGENT"""
        canvas = _make_canvas([
            _make_node("n5", "THEORY_CLASS", {"enable_ai_error_book": True}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 1
        assert tasks[0].agent_type == AgentType.EXAM
        assert tasks[0].node_type == "THEORY_CLASS"

    def test_task_distribute_with_ai_task_split(self):
        """Scenario 6: TASK_DISTRIBUTE with enable_ai_task_split=True → STRUCT_AGENT"""
        canvas = _make_canvas([
            _make_node("n6", "TASK_DISTRIBUTE", {"enable_ai_task_split": True}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 1
        assert tasks[0].agent_type == AgentType.STRUCT
        assert tasks[0].node_type == "TASK_DISTRIBUTE"

    def test_homework_with_source_mode_ai(self):
        """Scenario 7: HOMEWORK with source_mode='ai' → EXAM_AGENT"""
        canvas = _make_canvas([
            _make_node("n7", "HOMEWORK", {"source_mode": "ai"}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 1
        assert tasks[0].agent_type == AgentType.EXAM
        assert tasks[0].node_type == "HOMEWORK"

    def test_coding_class_with_code_review(self):
        """Scenario 8: CODING_CLASS with enable_code_review=True → CODE_AGENT"""
        canvas = _make_canvas([
            _make_node("n8", "CODING_CLASS", {"enable_code_review": True}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 1
        assert tasks[0].agent_type == AgentType.CODE
        assert tasks[0].node_type == "CODING_CLASS"

    def test_plan_upload_with_ai_pre_evaluation(self):
        """Scenario 9: PLAN_UPLOAD with enable_ai_pre_evaluation=True → CODE_AGENT"""
        canvas = _make_canvas([
            _make_node("n9", "PLAN_UPLOAD", {"enable_ai_pre_evaluation": True}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 1
        assert tasks[0].agent_type == AgentType.CODE
        assert tasks[0].node_type == "PLAN_UPLOAD"

    def test_ai_practice_with_ai_system_prompt(self):
        """Scenario 10: AI_PRACTICE with enable_ai_system_prompt=True → TEXT_AGENT"""
        canvas = _make_canvas([
            _make_node("n10", "AI_PRACTICE", {"enable_ai_system_prompt": True}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 1
        assert tasks[0].agent_type == AgentType.TEXT
        assert tasks[0].node_type == "AI_PRACTICE"


# ---------------------------------------------------------------------------
# Tests: Skipped nodes (scenarios 11-14)
# ---------------------------------------------------------------------------

class TestSkippedNodes:
    """Test that nodes without AI flags or unknown types are skipped."""

    def test_semester_survey_no_ai_flags(self):
        """Scenario 11: SEMESTER_SURVEY with no AI flags → SKIPPED"""
        canvas = _make_canvas([
            _make_node("n11", "SEMESTER_SURVEY", {"survey_title": "期末调查"}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 0

    def test_mindmap_draw_with_ai_flag_false(self):
        """Scenario 12: MINDMAP_DRAW with enable_ai_generate_map=False → SKIPPED"""
        canvas = _make_canvas([
            _make_node("n12", "MINDMAP_DRAW", {"enable_ai_generate_map": False}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 0

    def test_end_node_no_ai_flags(self):
        """Scenario 13: END with no AI flags → SKIPPED"""
        canvas = _make_canvas([
            _make_node("n13", "END", {}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 0

    def test_unknown_node_type_with_ai_flags(self):
        """Scenario 14: Unknown node type with AI flags → SKIPPED (not in routing table)"""
        canvas = _make_canvas([
            _make_node("n14", "UNKNOWN_FANCY_NODE", {"enable_ai_magic": True}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 0


# ---------------------------------------------------------------------------
# Tests: Deduplication and complete canvas (scenarios 15-16)
# ---------------------------------------------------------------------------

class TestDeduplicationAndCompleteCanvas:
    """Test deduplication and full canvas processing."""

    def test_multiple_ai_flags_one_task(self):
        """Scenario 15: Node with multiple AI flags → only ONE task created"""
        canvas = _make_canvas([
            _make_node("n15", "RESOURCE_READ", {
                "enable_ai_summary": True,
                "enable_ai_key_points": True,
                "enable_ai_quick_nav": True,
            }),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 1
        assert tasks[0].node_id == "n15"
        assert tasks[0].agent_type == AgentType.TEXT

    def test_complete_canvas_mixed_nodes(self):
        """Scenario 16: Complete canvas with mix of AI and non-AI nodes → correct count"""
        canvas = _make_canvas([
            # AI-enabled nodes (should produce tasks)
            _make_node("n1", "START", {"enable_ai_welcome": True}),
            _make_node("n2", "RESOURCE_READ", {"enable_ai_summary": True}),
            _make_node("n3", "VIDEO_LEARN", {"enable_ai_chapter": True}),
            _make_node("n4", "MINDMAP_PREVIEW", {"enable_ai_generate_map": True}),
            _make_node("n5", "THEORY_CLASS", {"enable_ai_error_book": True}),
            _make_node("n6", "TASK_DISTRIBUTE", {"enable_ai_task_split": True}),
            _make_node("n7", "HOMEWORK", {"source_mode": "ai"}),
            _make_node("n8", "CODING_CLASS", {"enable_code_review": True}),
            _make_node("n9", "PLAN_UPLOAD", {"enable_ai_pre_evaluation": True}),
            _make_node("n10", "AI_PRACTICE", {"enable_ai_system_prompt": True}),
            # Non-AI nodes (should be skipped)
            _make_node("n11", "SEMESTER_SURVEY", {"survey_title": "期末调查"}),
            _make_node("n12", "MINDMAP_DRAW", {"enable_ai_generate_map": False}),
            _make_node("n13", "END", {}),
            _make_node("n14", "UNKNOWN_TYPE", {"enable_ai_something": True}),
            _make_node("n15", "START", {"enable_ai_welcome": False}),
            _make_node("n16", "HOMEWORK", {"source_mode": "manual"}),
        ], orchestration_id="full-canvas-001")

        tasks = detect_ai_nodes(canvas)

        # Exactly 10 AI-enabled nodes should produce tasks
        assert len(tasks) == 10

        # Verify correct agent type assignments
        task_map = {t.node_id: t for t in tasks}
        assert task_map["n1"].agent_type == AgentType.TEXT
        assert task_map["n2"].agent_type == AgentType.TEXT
        assert task_map["n3"].agent_type == AgentType.VIDEO
        assert task_map["n4"].agent_type == AgentType.STRUCT
        assert task_map["n5"].agent_type == AgentType.EXAM
        assert task_map["n6"].agent_type == AgentType.STRUCT
        assert task_map["n7"].agent_type == AgentType.EXAM
        assert task_map["n8"].agent_type == AgentType.CODE
        assert task_map["n9"].agent_type == AgentType.CODE
        assert task_map["n10"].agent_type == AgentType.TEXT

        # Verify skipped nodes are not in the task list
        assert "n11" not in task_map
        assert "n12" not in task_map
        assert "n13" not in task_map
        assert "n14" not in task_map
        assert "n15" not in task_map
        assert "n16" not in task_map


# ---------------------------------------------------------------------------
# Tests: analyze_orchestration() returns proper ExecutionPlan
# ---------------------------------------------------------------------------

class TestAnalyzeOrchestration:
    """Test the SupervisorAgent.analyze_orchestration() method."""

    def test_returns_execution_plan(self):
        """analyze_orchestration() returns proper ExecutionPlan with correct fields."""
        supervisor = SupervisorAgent()
        canvas = _make_canvas([
            _make_node("n1", "START", {"enable_ai_welcome": True}),
            _make_node("n2", "HOMEWORK", {"source_mode": "ai"}),
            _make_node("n3", "END", {}),
        ], orchestration_id="orch-plan-001")

        plan = supervisor.analyze_orchestration(canvas)

        assert isinstance(plan, ExecutionPlan)
        assert plan.orchestration_id == "orch-plan-001"
        assert plan.total_tasks == 2
        assert len(plan.parallel_jobs) == 2
        assert plan.parallel_jobs[0].task_id == "orch-plan-001_n1"
        assert plan.parallel_jobs[1].task_id == "orch-plan-001_n2"

    def test_empty_nodes_returns_empty_plan(self):
        """Empty nodes list returns empty plan."""
        supervisor = SupervisorAgent()
        canvas = _make_canvas([], orchestration_id="orch-empty")

        plan = supervisor.analyze_orchestration(canvas)

        assert isinstance(plan, ExecutionPlan)
        assert plan.orchestration_id == "orch-empty"
        assert plan.total_tasks == 0
        assert len(plan.parallel_jobs) == 0

    def test_all_skipped_nodes_returns_empty_plan(self):
        """Canvas with only non-AI nodes returns empty plan."""
        supervisor = SupervisorAgent()
        canvas = _make_canvas([
            _make_node("n1", "END", {}),
            _make_node("n2", "SEMESTER_SURVEY", {"title": "survey"}),
        ], orchestration_id="orch-no-ai")

        plan = supervisor.analyze_orchestration(canvas)

        assert plan.total_tasks == 0
        assert len(plan.parallel_jobs) == 0


# ---------------------------------------------------------------------------
# Tests: Edge cases — graceful handling
# ---------------------------------------------------------------------------

class TestEdgeCases:
    """Test graceful handling of edge cases."""

    def test_node_without_config_field(self):
        """Nodes without config field are handled gracefully (treated as no AI flags)."""
        canvas = {
            "orchestration_id": "edge-001",
            "nodes": [
                {"node_id": "n1", "node_type": "START"},  # no "config" key at all
            ],
            "edges": [],
        }
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 0

    def test_node_with_empty_config(self):
        """Nodes with empty config dict are handled gracefully."""
        canvas = _make_canvas([
            _make_node("n1", "START", {}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 0

    def test_canvas_without_nodes_key(self):
        """Canvas without 'nodes' key returns empty list."""
        canvas = {"orchestration_id": "edge-002", "edges": []}
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 0

    def test_task_id_format(self):
        """Task IDs follow the format: {orchestration_id}_{node_id}."""
        canvas = _make_canvas([
            _make_node("node-abc", "START", {"enable_ai_welcome": True}),
        ], orchestration_id="orch-xyz")

        tasks = detect_ai_nodes(canvas)
        assert tasks[0].task_id == "orch-xyz_node-abc"

    def test_config_preserved_in_task(self):
        """The full node config is preserved in the AgentTask."""
        config = {
            "enable_ai_welcome": True,
            "welcome_style": "formal",
            "max_length": 200,
        }
        canvas = _make_canvas([
            _make_node("n1", "START", config),
        ])

        tasks = detect_ai_nodes(canvas)
        assert tasks[0].config == config

    def test_duplicate_node_ids_deduplicated(self):
        """Duplicate node_ids in the canvas produce only one task."""
        canvas = _make_canvas([
            _make_node("dup-1", "START", {"enable_ai_welcome": True}),
            _make_node("dup-1", "START", {"enable_ai_welcome": True}),
            _make_node("dup-1", "START", {"enable_ai_welcome": True}),
        ])
        tasks = detect_ai_nodes(canvas)
        assert len(tasks) == 1
        assert tasks[0].node_id == "dup-1"
