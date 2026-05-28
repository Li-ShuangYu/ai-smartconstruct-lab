"""
Unit tests for SupervisorAgent covering the refactored data-driven routing.

Tests verify:
- Correct routing based on ai_spec.target_agent
- Skipping of nodes without ai_spec or with invalid target_agent
- AI flag checking (at least one enabled flag or source_mode="ai")
- Teacher override handling (overridden fields skipped from AI flag check)
- Deduplication (one task per node regardless of multiple AI flags)
- Phase-based traversal and priority sorting
- analyze_orchestration() returns proper ExecutionPlan
- Warning logging for invalid ai_spec without pipeline failure
- Edge cases: empty phases, missing config field
"""

import sys

sys.path.insert(0, ".")

import pytest

from agents.supervisor import (
    SupervisorAgent,
    detect_ai_nodes_phased,
    _should_process_node,
    VALID_AGENT_TYPES,
)
from models.schemas import AgentType, ExecutionPlan


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_phased_canvas(phases, orchestration_id="test-orch"):
    """Helper to build a minimal phased canvas_json dict."""
    return {
        "orchestration_id": orchestration_id,
        "phases": phases,
    }


def _make_phase(phase_id, nodes, sort_num=1):
    """Helper to build a single phase dict."""
    return {
        "phase_id": phase_id,
        "phase_name": f"Phase {phase_id}",
        "sort_num": sort_num,
        "nodes": nodes,
        "edges": [],
    }


def _make_node(node_id, node_type, config=None, ai_spec=None):
    """Helper to build a single node dict with optional ai_spec."""
    node = {"node_id": node_id, "node_type": node_type}
    if config is not None:
        node["config"] = config
    else:
        node["config"] = {}
    if ai_spec is not None:
        node["ai_spec"] = ai_spec
    return node


def _make_ai_spec(target_agent="TEXT", priority=5, ai_flags=None):
    """Helper to build an ai_spec dict."""
    return {
        "target_agent": target_agent,
        "priority": priority,
        "ai_flags": ai_flags or [],
    }


# ---------------------------------------------------------------------------
# Tests: _should_process_node
# ---------------------------------------------------------------------------

class TestShouldProcessNode:
    """Test the _should_process_node function."""

    def test_none_ai_spec_returns_false(self):
        """Nodes with ai_spec=None are skipped."""
        node = _make_node("n1", "START", {"enable_ai_welcome": True})
        assert _should_process_node(node, None) is False

    def test_invalid_target_agent_returns_false(self):
        """Nodes with invalid target_agent are skipped."""
        node = _make_node("n1", "START", {"enable_ai_welcome": True})
        ai_spec = _make_ai_spec(target_agent="INVALID", ai_flags=["enable_ai_welcome"])
        assert _should_process_node(node, ai_spec) is False

    def test_empty_target_agent_returns_false(self):
        """Nodes with empty target_agent are skipped."""
        node = _make_node("n1", "START", {"enable_ai_welcome": True})
        ai_spec = _make_ai_spec(target_agent="", ai_flags=["enable_ai_welcome"])
        assert _should_process_node(node, ai_spec) is False

    def test_valid_spec_with_enabled_flag(self):
        """Node with valid ai_spec and enabled flag returns True."""
        node = _make_node("n1", "START", {"enable_ai_welcome": True})
        ai_spec = _make_ai_spec(target_agent="TEXT", ai_flags=["enable_ai_welcome"])
        assert _should_process_node(node, ai_spec) is True

    def test_valid_spec_with_all_flags_disabled(self):
        """Node with valid ai_spec but all flags disabled returns False."""
        node = _make_node("n1", "START", {"enable_ai_welcome": False})
        ai_spec = _make_ai_spec(target_agent="TEXT", ai_flags=["enable_ai_welcome"])
        assert _should_process_node(node, ai_spec) is False

    def test_valid_spec_with_source_mode_ai(self):
        """Node with source_mode='ai' returns True even without ai_flags."""
        node = _make_node("n1", "HOMEWORK", {"source_mode": "ai"})
        ai_spec = _make_ai_spec(target_agent="EXAM", ai_flags=["enable_ai_grading"])
        assert _should_process_node(node, ai_spec) is True

    def test_source_mode_manual_no_flags_returns_false(self):
        """Node with source_mode='manual' and no enabled flags returns False."""
        node = _make_node("n1", "HOMEWORK", {"source_mode": "manual"})
        ai_spec = _make_ai_spec(target_agent="EXAM", ai_flags=["enable_ai_grading"])
        assert _should_process_node(node, ai_spec) is False

    def test_multiple_flags_one_enabled(self):
        """Node with multiple flags where at least one is enabled returns True."""
        node = _make_node("n1", "RESOURCE_READ", {
            "enable_ai_summary": False,
            "enable_ai_key_points": True,
        })
        ai_spec = _make_ai_spec(
            target_agent="TEXT",
            ai_flags=["enable_ai_summary", "enable_ai_key_points"],
        )
        assert _should_process_node(node, ai_spec) is True

    def test_flag_missing_from_config_treated_as_false(self):
        """Flags not present in config are treated as disabled."""
        node = _make_node("n1", "START", {})
        ai_spec = _make_ai_spec(target_agent="TEXT", ai_flags=["enable_ai_welcome"])
        assert _should_process_node(node, ai_spec) is False

    def test_all_valid_agent_types(self):
        """All valid agent types are accepted."""
        for agent_type in VALID_AGENT_TYPES:
            node = _make_node("n1", "TEST", {"some_flag": True})
            ai_spec = _make_ai_spec(target_agent=agent_type, ai_flags=["some_flag"])
            assert _should_process_node(node, ai_spec) is True


# ---------------------------------------------------------------------------
# Tests: Teacher overrides (Step 5)
# ---------------------------------------------------------------------------

class TestTeacherOverrides:
    """Test that teacher-overridden fields are skipped from AI flag checks."""

    def test_overridden_flag_is_skipped(self):
        """If the only enabled flag is overridden, node is skipped."""
        node = _make_node("n1", "START", {
            "enable_ai_welcome": True,
            "ai_processing": {
                "_overrides": {"enable_ai_welcome": True},
            },
        })
        ai_spec = _make_ai_spec(target_agent="TEXT", ai_flags=["enable_ai_welcome"])
        assert _should_process_node(node, ai_spec) is False

    def test_partial_override_still_processes(self):
        """If one flag is overridden but another is enabled, node is processed."""
        node = _make_node("n1", "RESOURCE_READ", {
            "enable_ai_summary": True,
            "enable_ai_key_points": True,
            "ai_processing": {
                "_overrides": {"enable_ai_summary": True},
            },
        })
        ai_spec = _make_ai_spec(
            target_agent="TEXT",
            ai_flags=["enable_ai_summary", "enable_ai_key_points"],
        )
        assert _should_process_node(node, ai_spec) is True

    def test_override_false_does_not_skip(self):
        """Override marked as False does not skip the flag."""
        node = _make_node("n1", "START", {
            "enable_ai_welcome": True,
            "ai_processing": {
                "_overrides": {"enable_ai_welcome": False},
            },
        })
        ai_spec = _make_ai_spec(target_agent="TEXT", ai_flags=["enable_ai_welcome"])
        assert _should_process_node(node, ai_spec) is True

    def test_no_overrides_key_processes_normally(self):
        """Missing _overrides key processes normally."""
        node = _make_node("n1", "START", {
            "enable_ai_welcome": True,
            "ai_processing": {},
        })
        ai_spec = _make_ai_spec(target_agent="TEXT", ai_flags=["enable_ai_welcome"])
        assert _should_process_node(node, ai_spec) is True

    def test_source_mode_ai_not_affected_by_overrides(self):
        """source_mode='ai' still triggers processing even with overrides."""
        node = _make_node("n1", "HOMEWORK", {
            "source_mode": "ai",
            "enable_ai_grading": True,
            "ai_processing": {
                "_overrides": {"enable_ai_grading": True},
            },
        })
        ai_spec = _make_ai_spec(target_agent="EXAM", ai_flags=["enable_ai_grading"])
        assert _should_process_node(node, ai_spec) is True


# ---------------------------------------------------------------------------
# Tests: detect_ai_nodes_phased (Step 3)
# ---------------------------------------------------------------------------

class TestDetectAiNodesPhased:
    """Test the phased node detection and task generation."""

    def test_basic_phased_detection(self):
        """Nodes with valid ai_spec in phases produce tasks."""
        canvas = _make_phased_canvas([
            _make_phase("p1", [
                _make_node("n1", "START", {"enable_ai_welcome": True},
                           _make_ai_spec("TEXT", 5, ["enable_ai_welcome"])),
            ]),
        ])
        tasks = detect_ai_nodes_phased(canvas)
        assert len(tasks) == 1
        assert tasks[0].node_id == "n1"
        assert tasks[0].agent_type == AgentType.TEXT
        assert tasks[0].phase_id == "p1"

    def test_multi_phase_detection(self):
        """Nodes across multiple phases are all detected."""
        canvas = _make_phased_canvas([
            _make_phase("p1", [
                _make_node("n1", "START", {"enable_ai_welcome": True},
                           _make_ai_spec("TEXT", 5, ["enable_ai_welcome"])),
            ], sort_num=1),
            _make_phase("p2", [
                _make_node("n2", "HOMEWORK", {"source_mode": "ai"},
                           _make_ai_spec("EXAM", 2, ["enable_ai_grading"])),
            ], sort_num=2),
        ])
        tasks = detect_ai_nodes_phased(canvas)
        assert len(tasks) == 2
        # Sorted by priority: n2 (priority=2) before n1 (priority=5)
        assert tasks[0].node_id == "n2"
        assert tasks[0].phase_id == "p2"
        assert tasks[1].node_id == "n1"
        assert tasks[1].phase_id == "p1"

    def test_priority_sorting(self):
        """Tasks are sorted by priority (lower number first)."""
        canvas = _make_phased_canvas([
            _make_phase("p1", [
                _make_node("n1", "START", {"enable_ai_welcome": True},
                           _make_ai_spec("TEXT", 8, ["enable_ai_welcome"])),
                _make_node("n2", "EXAM", {"source_mode": "ai"},
                           _make_ai_spec("EXAM", 1, ["enable_ai_grading"])),
                _make_node("n3", "RESOURCE_READ", {"enable_ai_summary": True},
                           _make_ai_spec("TEXT", 3, ["enable_ai_summary"])),
            ]),
        ])
        tasks = detect_ai_nodes_phased(canvas)
        assert len(tasks) == 3
        assert tasks[0].priority == 1
        assert tasks[1].priority == 3
        assert tasks[2].priority == 8

    def test_deduplication_across_phases(self):
        """Same node_id in different phases produces only one task."""
        canvas = _make_phased_canvas([
            _make_phase("p1", [
                _make_node("n1", "START", {"enable_ai_welcome": True},
                           _make_ai_spec("TEXT", 5, ["enable_ai_welcome"])),
            ]),
            _make_phase("p2", [
                _make_node("n1", "START", {"enable_ai_welcome": True},
                           _make_ai_spec("TEXT", 5, ["enable_ai_welcome"])),
            ]),
        ])
        tasks = detect_ai_nodes_phased(canvas)
        assert len(tasks) == 1

    def test_nodes_without_ai_spec_skipped(self):
        """Nodes without ai_spec are skipped."""
        canvas = _make_phased_canvas([
            _make_phase("p1", [
                _make_node("n1", "END", {}),
                _make_node("n2", "START", {"enable_ai_welcome": True},
                           _make_ai_spec("TEXT", 5, ["enable_ai_welcome"])),
            ]),
        ])
        tasks = detect_ai_nodes_phased(canvas)
        assert len(tasks) == 1
        assert tasks[0].node_id == "n2"

    def test_invalid_ai_spec_skipped_without_failure(self):
        """Nodes with invalid ai_spec are skipped without failing the pipeline."""
        canvas = _make_phased_canvas([
            _make_phase("p1", [
                _make_node("n1", "START", {"enable_ai_welcome": True},
                           _make_ai_spec("INVALID_AGENT", 5, ["enable_ai_welcome"])),
                _make_node("n2", "RESOURCE_READ", {"enable_ai_summary": True},
                           _make_ai_spec("TEXT", 3, ["enable_ai_summary"])),
            ]),
        ])
        tasks = detect_ai_nodes_phased(canvas)
        # n1 skipped due to invalid target_agent, n2 processed
        assert len(tasks) == 1
        assert tasks[0].node_id == "n2"

    def test_empty_phases_returns_empty_list(self):
        """Canvas with empty phases returns empty task list."""
        canvas = _make_phased_canvas([])
        tasks = detect_ai_nodes_phased(canvas)
        assert len(tasks) == 0

    def test_task_id_format(self):
        """Task IDs follow the format: {orchestration_id}_{node_id}."""
        canvas = _make_phased_canvas([
            _make_phase("p1", [
                _make_node("node-abc", "START", {"enable_ai_welcome": True},
                           _make_ai_spec("TEXT", 5, ["enable_ai_welcome"])),
            ]),
        ], orchestration_id="orch-xyz")
        tasks = detect_ai_nodes_phased(canvas)
        assert tasks[0].task_id == "orch-xyz_node-abc"

    def test_config_preserved_in_task(self):
        """The full node config is preserved in the AgentTask."""
        config = {
            "enable_ai_welcome": True,
            "welcome_style": "formal",
            "max_length": 200,
        }
        canvas = _make_phased_canvas([
            _make_phase("p1", [
                _make_node("n1", "START", config,
                           _make_ai_spec("TEXT", 5, ["enable_ai_welcome"])),
            ]),
        ])
        tasks = detect_ai_nodes_phased(canvas)
        assert tasks[0].config == config

    def test_default_priority_when_missing(self):
        """Default priority is 5 when not specified in ai_spec."""
        ai_spec = {"target_agent": "TEXT", "ai_flags": ["enable_ai_welcome"]}
        canvas = _make_phased_canvas([
            _make_phase("p1", [
                _make_node("n1", "START", {"enable_ai_welcome": True}, ai_spec),
            ]),
        ])
        tasks = detect_ai_nodes_phased(canvas)
        assert tasks[0].priority == 5

    def test_all_agent_types_route_correctly(self):
        """All valid agent types route to the correct AgentType enum."""
        nodes = [
            _make_node("n1", "START", {"flag": True},
                       _make_ai_spec("TEXT", 1, ["flag"])),
            _make_node("n2", "MINDMAP", {"flag": True},
                       _make_ai_spec("STRUCT", 2, ["flag"])),
            _make_node("n3", "EXAM", {"flag": True},
                       _make_ai_spec("EXAM", 3, ["flag"])),
            _make_node("n4", "CODE", {"flag": True},
                       _make_ai_spec("CODE", 4, ["flag"])),
            _make_node("n5", "VIDEO", {"flag": True},
                       _make_ai_spec("VIDEO", 5, ["flag"])),
        ]
        canvas = _make_phased_canvas([_make_phase("p1", nodes)])
        tasks = detect_ai_nodes_phased(canvas)
        assert len(tasks) == 5
        assert tasks[0].agent_type == AgentType.TEXT
        assert tasks[1].agent_type == AgentType.STRUCT
        assert tasks[2].agent_type == AgentType.EXAM
        assert tasks[3].agent_type == AgentType.CODE
        assert tasks[4].agent_type == AgentType.VIDEO


# ---------------------------------------------------------------------------
# Tests: analyze_orchestration() returns proper ExecutionPlan
# ---------------------------------------------------------------------------

class TestAnalyzeOrchestration:
    """Test the SupervisorAgent.analyze_orchestration() method."""

    def test_returns_execution_plan(self):
        """analyze_orchestration() returns proper ExecutionPlan with correct fields."""
        supervisor = SupervisorAgent()
        canvas = _make_phased_canvas([
            _make_phase("p1", [
                _make_node("n1", "START", {"enable_ai_welcome": True},
                           _make_ai_spec("TEXT", 5, ["enable_ai_welcome"])),
                _make_node("n2", "HOMEWORK", {"source_mode": "ai"},
                           _make_ai_spec("EXAM", 2, ["enable_ai_grading"])),
                _make_node("n3", "END", {}),
            ]),
        ], orchestration_id="orch-plan-001")

        plan = supervisor.analyze_orchestration(canvas)

        assert isinstance(plan, ExecutionPlan)
        assert plan.orchestration_id == "orch-plan-001"
        assert plan.total_tasks == 2
        assert len(plan.parallel_jobs) == 2
        # Sorted by priority: n2 (priority=2) first, n1 (priority=5) second
        assert plan.parallel_jobs[0].task_id == "orch-plan-001_n2"
        assert plan.parallel_jobs[1].task_id == "orch-plan-001_n1"

    def test_empty_phases_returns_empty_plan(self):
        """Empty phases list returns empty plan."""
        supervisor = SupervisorAgent()
        canvas = _make_phased_canvas([], orchestration_id="orch-empty")

        plan = supervisor.analyze_orchestration(canvas)

        assert isinstance(plan, ExecutionPlan)
        assert plan.orchestration_id == "orch-empty"
        assert plan.total_tasks == 0
        assert len(plan.parallel_jobs) == 0

    def test_all_skipped_nodes_returns_empty_plan(self):
        """Canvas with only non-AI nodes returns empty plan."""
        supervisor = SupervisorAgent()
        canvas = _make_phased_canvas([
            _make_phase("p1", [
                _make_node("n1", "END", {}),
                _make_node("n2", "SURVEY", {"title": "survey"}),
            ]),
        ], orchestration_id="orch-no-ai")

        plan = supervisor.analyze_orchestration(canvas)

        assert plan.total_tasks == 0
        assert len(plan.parallel_jobs) == 0


# ---------------------------------------------------------------------------
# Tests: Warning logging (Step 6)
# ---------------------------------------------------------------------------

class TestWarningLogging:
    """Test that warnings are logged for invalid ai_spec without failing."""

    def test_null_ai_spec_logs_warning(self, caplog):
        """Null ai_spec logs a warning and skips the node."""
        import logging
        with caplog.at_level(logging.WARNING):
            node = _make_node("n1", "START", {"enable_ai_welcome": True})
            result = _should_process_node(node, None)
            assert result is False
            assert "ai_spec is null" in caplog.text

    def test_invalid_target_agent_logs_warning(self, caplog):
        """Invalid target_agent logs a warning and skips the node."""
        import logging
        with caplog.at_level(logging.WARNING):
            node = _make_node("n1", "START", {"enable_ai_welcome": True})
            ai_spec = _make_ai_spec(target_agent="BOGUS", ai_flags=["enable_ai_welcome"])
            result = _should_process_node(node, ai_spec)
            assert result is False
            assert "invalid target_agent" in caplog.text
            assert "BOGUS" in caplog.text

    def test_pipeline_continues_after_invalid_nodes(self):
        """Pipeline processes valid nodes even when some have invalid ai_spec."""
        canvas = _make_phased_canvas([
            _make_phase("p1", [
                # Invalid: null ai_spec
                _make_node("n1", "START", {"enable_ai_welcome": True}),
                # Invalid: bad target_agent
                _make_node("n2", "RESOURCE_READ", {"enable_ai_summary": True},
                           _make_ai_spec("NONEXISTENT", 3, ["enable_ai_summary"])),
                # Valid
                _make_node("n3", "EXAM", {"source_mode": "ai"},
                           _make_ai_spec("EXAM", 1, ["enable_ai_grading"])),
            ]),
        ])
        tasks = detect_ai_nodes_phased(canvas)
        # Only the valid node produces a task
        assert len(tasks) == 1
        assert tasks[0].node_id == "n3"


# ---------------------------------------------------------------------------
# Tests: Edge cases
# ---------------------------------------------------------------------------

class TestEdgeCases:
    """Test graceful handling of edge cases."""

    def test_node_without_config_field(self):
        """Nodes without config field are handled gracefully."""
        canvas = _make_phased_canvas([
            _make_phase("p1", [
                {"node_id": "n1", "node_type": "START",
                 "ai_spec": _make_ai_spec("TEXT", 5, ["enable_ai_welcome"])},
            ]),
        ])
        tasks = detect_ai_nodes_phased(canvas)
        # No config means no flags enabled → skipped
        assert len(tasks) == 0

    def test_canvas_without_phases_key(self):
        """Canvas without 'phases' key returns empty list."""
        canvas = {"orchestration_id": "edge-002"}
        tasks = detect_ai_nodes_phased(canvas)
        assert len(tasks) == 0

    def test_phase_without_nodes_key(self):
        """Phase without 'nodes' key is handled gracefully."""
        canvas = _make_phased_canvas([
            {"phase_id": "p1", "phase_name": "Empty", "sort_num": 1, "edges": []},
        ])
        tasks = detect_ai_nodes_phased(canvas)
        assert len(tasks) == 0
