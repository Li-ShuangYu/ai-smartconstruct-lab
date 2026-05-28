"""Quick verification tests for SupervisorAgent (Task 7 refactor)."""

import sys
sys.path.insert(0, ".")

from agents.supervisor import (
    SupervisorAgent,
    detect_ai_nodes_phased,
    _should_process_node,
    VALID_AGENT_TYPES,
)
from models.schemas import AgentType


def _make_ai_spec(target_agent="TEXT", priority=5, ai_flags=None):
    return {"target_agent": target_agent, "priority": priority, "ai_flags": ai_flags or []}


def test_should_process_node_valid():
    """Valid ai_spec with enabled flag returns True."""
    node = {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": True}}
    ai_spec = _make_ai_spec("TEXT", 5, ["enable_ai_welcome"])
    assert _should_process_node(node, ai_spec) is True
    print("PASSED: _should_process_node valid case")


def test_should_process_node_null_spec():
    """Null ai_spec returns False."""
    node = {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": True}}
    assert _should_process_node(node, None) is False
    print("PASSED: _should_process_node null ai_spec")


def test_should_process_node_invalid_agent():
    """Invalid target_agent returns False."""
    node = {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": True}}
    ai_spec = _make_ai_spec("INVALID", 5, ["enable_ai_welcome"])
    assert _should_process_node(node, ai_spec) is False
    print("PASSED: _should_process_node invalid target_agent")


def test_should_process_node_source_mode_ai():
    """source_mode='ai' triggers processing."""
    node = {"node_id": "n1", "node_type": "HOMEWORK", "config": {"source_mode": "ai"}}
    ai_spec = _make_ai_spec("EXAM", 2, ["enable_ai_grading"])
    assert _should_process_node(node, ai_spec) is True
    print("PASSED: _should_process_node source_mode=ai")


def test_should_process_node_all_flags_disabled():
    """All flags disabled and no source_mode=ai returns False."""
    node = {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": False}}
    ai_spec = _make_ai_spec("TEXT", 5, ["enable_ai_welcome"])
    assert _should_process_node(node, ai_spec) is False
    print("PASSED: _should_process_node all flags disabled")


def test_teacher_override_skips_flag():
    """Overridden flag is skipped from AI processing check."""
    node = {
        "node_id": "n1",
        "node_type": "START",
        "config": {
            "enable_ai_welcome": True,
            "ai_processing": {"_overrides": {"enable_ai_welcome": True}},
        },
    }
    ai_spec = _make_ai_spec("TEXT", 5, ["enable_ai_welcome"])
    assert _should_process_node(node, ai_spec) is False
    print("PASSED: teacher override skips flag")


def test_detect_ai_nodes_phased_basic():
    """Basic phased detection works."""
    canvas = {
        "orchestration_id": "test-001",
        "phases": [
            {
                "phase_id": "p1",
                "phase_name": "Phase 1",
                "sort_num": 1,
                "nodes": [
                    {
                        "node_id": "n1",
                        "node_type": "START",
                        "config": {"enable_ai_welcome": True},
                        "ai_spec": _make_ai_spec("TEXT", 5, ["enable_ai_welcome"]),
                    },
                    {
                        "node_id": "n2",
                        "node_type": "EXAM",
                        "config": {"source_mode": "ai"},
                        "ai_spec": _make_ai_spec("EXAM", 1, ["enable_ai_grading"]),
                    },
                    {
                        "node_id": "n3",
                        "node_type": "END",
                        "config": {},
                    },
                ],
                "edges": [],
            },
        ],
    }
    tasks = detect_ai_nodes_phased(canvas)
    assert len(tasks) == 2
    # Sorted by priority: n2 (1) before n1 (5)
    assert tasks[0].node_id == "n2"
    assert tasks[0].priority == 1
    assert tasks[0].phase_id == "p1"
    assert tasks[1].node_id == "n1"
    assert tasks[1].priority == 5
    print("PASSED: detect_ai_nodes_phased basic")


def test_analyze_orchestration():
    """analyze_orchestration returns ExecutionPlan."""
    supervisor = SupervisorAgent()
    canvas = {
        "orchestration_id": "orch-100",
        "phases": [
            {
                "phase_id": "p1",
                "phase_name": "Phase 1",
                "sort_num": 1,
                "nodes": [
                    {
                        "node_id": "n1",
                        "node_type": "START",
                        "config": {"enable_ai_welcome": True},
                        "ai_spec": _make_ai_spec("TEXT", 5, ["enable_ai_welcome"]),
                    },
                    {
                        "node_id": "n2",
                        "node_type": "END",
                        "config": {},
                    },
                ],
                "edges": [],
            },
        ],
    }
    plan = supervisor.analyze_orchestration(canvas)
    assert plan.orchestration_id == "orch-100"
    assert plan.total_tasks == 1
    assert len(plan.parallel_jobs) == 1
    assert plan.parallel_jobs[0].task_id == "orch-100_n1"
    print("PASSED: analyze_orchestration returns correct ExecutionPlan")


def test_deduplication():
    """Duplicate node_ids produce only one task."""
    canvas = {
        "orchestration_id": "test-dedup",
        "phases": [
            {
                "phase_id": "p1",
                "phase_name": "Phase 1",
                "sort_num": 1,
                "nodes": [
                    {
                        "node_id": "n1",
                        "node_type": "START",
                        "config": {"enable_ai_welcome": True},
                        "ai_spec": _make_ai_spec("TEXT", 5, ["enable_ai_welcome"]),
                    },
                ],
                "edges": [],
            },
            {
                "phase_id": "p2",
                "phase_name": "Phase 2",
                "sort_num": 2,
                "nodes": [
                    {
                        "node_id": "n1",
                        "node_type": "START",
                        "config": {"enable_ai_welcome": True},
                        "ai_spec": _make_ai_spec("TEXT", 5, ["enable_ai_welcome"]),
                    },
                ],
                "edges": [],
            },
        ],
    }
    tasks = detect_ai_nodes_phased(canvas)
    assert len(tasks) == 1
    print("PASSED: deduplication across phases")


if __name__ == "__main__":
    test_should_process_node_valid()
    test_should_process_node_null_spec()
    test_should_process_node_invalid_agent()
    test_should_process_node_source_mode_ai()
    test_should_process_node_all_flags_disabled()
    test_teacher_override_skips_flag()
    test_detect_ai_nodes_phased_basic()
    test_analyze_orchestration()
    test_deduplication()
    print("\nALL TESTS PASSED")
