"""Quick verification tests for SupervisorAgent (tasks 5.1-5.6)."""

import sys
sys.path.insert(0, ".")

from agents.supervisor import SupervisorAgent, detect_ai_nodes, _has_ai_flags
from models.schemas import AgentType


def test_ai_flag_detection():
    """Task 5.3: AI flag detection logic."""
    # Positive cases
    assert _has_ai_flags({"enable_ai_welcome": True}) is True
    assert _has_ai_flags({"enable_ai_summary": True, "enable_ai_key_points": True}) is True
    assert _has_ai_flags({"source_mode": "ai"}) is True
    assert _has_ai_flags({"enable_code_review": True}) is True
    assert _has_ai_flags({"enable_ai_pre_evaluation": True}) is True
    # Negative cases
    assert _has_ai_flags({"enable_ai_welcome": False}) is False
    assert _has_ai_flags({"source_mode": "manual"}) is False
    assert _has_ai_flags({"enable_code_review": False}) is False
    assert _has_ai_flags({}) is False
    print("PASSED: AI flag detection")


def test_nodes_without_ai_flags_skipped():
    """Task 5.4: Nodes without AI flags are skipped."""
    canvas = {
        "orchestration_id": "test-001",
        "nodes": [
            {"node_id": "n1", "node_type": "END", "config": {}},
            {"node_id": "n2", "node_type": "SEMESTER_SURVEY", "config": {"some_flag": True}},
            {"node_id": "n3", "node_type": "MINDMAP_PREVIEW", "config": {"enable_ai_generate_map": False}},
        ],
        "edges": [],
    }
    tasks = detect_ai_nodes(canvas)
    assert len(tasks) == 0
    print("PASSED: Nodes without AI flags skipped")


def test_ai_nodes_detected_and_routed():
    """Task 5.2: Routing table maps node_type to AgentType."""
    canvas = {
        "orchestration_id": "test-002",
        "nodes": [
            {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": True}},
            {"node_id": "n2", "node_type": "HOMEWORK", "config": {"source_mode": "ai"}},
            {"node_id": "n3", "node_type": "CODING_CLASS", "config": {"enable_code_review": True}},
            {"node_id": "n4", "node_type": "VIDEO_LEARN", "config": {"enable_ai_chapter": True}},
        ],
        "edges": [],
    }
    tasks = detect_ai_nodes(canvas)
    assert len(tasks) == 4
    assert tasks[0].agent_type == AgentType.TEXT
    assert tasks[1].agent_type == AgentType.EXAM
    assert tasks[2].agent_type == AgentType.CODE
    assert tasks[3].agent_type == AgentType.VIDEO
    print("PASSED: AI nodes detected and routed correctly")


def test_no_duplicate_tasks_per_node_id():
    """Task 5.5: No duplicate tasks per node_id."""
    canvas = {
        "orchestration_id": "test-003",
        "nodes": [
            {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": True}},
            {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": True}},
        ],
        "edges": [],
    }
    tasks = detect_ai_nodes(canvas)
    assert len(tasks) == 1
    print("PASSED: No duplicate tasks per node_id")


def test_multiple_flags_one_task():
    """Task 5.5: Multiple AI flags on one node produce one task."""
    canvas = {
        "orchestration_id": "test-004",
        "nodes": [
            {
                "node_id": "n1",
                "node_type": "RESOURCE_READ",
                "config": {
                    "enable_ai_summary": True,
                    "enable_ai_key_points": True,
                    "enable_ai_quick_nav": True,
                },
            },
        ],
        "edges": [],
    }
    tasks = detect_ai_nodes(canvas)
    assert len(tasks) == 1
    assert tasks[0].node_id == "n1"
    assert tasks[0].agent_type == AgentType.TEXT
    print("PASSED: Multiple AI flags produce one task per node")


def test_analyze_orchestration():
    """Task 5.6: analyze_orchestration returns ExecutionPlan."""
    supervisor = SupervisorAgent()
    canvas = {
        "orchestration_id": "orch-100",
        "nodes": [
            {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": True}},
            {"node_id": "n2", "node_type": "PLAN_UPLOAD", "config": {"enable_ai_pre_evaluation": True}},
            {"node_id": "n3", "node_type": "END", "config": {}},
        ],
        "edges": [],
    }
    plan = supervisor.analyze_orchestration(canvas)
    assert plan.orchestration_id == "orch-100"
    assert plan.total_tasks == 2
    assert len(plan.parallel_jobs) == 2
    assert plan.parallel_jobs[0].task_id == "orch-100_n1"
    assert plan.parallel_jobs[1].task_id == "orch-100_n2"
    print("PASSED: analyze_orchestration returns correct ExecutionPlan")


def test_unknown_node_type_skipped():
    """Task 5.4: Unknown node_type with AI flags is skipped."""
    canvas = {
        "orchestration_id": "test-005",
        "nodes": [
            {"node_id": "n1", "node_type": "UNKNOWN_TYPE", "config": {"enable_ai_something": True}},
        ],
        "edges": [],
    }
    tasks = detect_ai_nodes(canvas)
    assert len(tasks) == 0
    print("PASSED: Unknown node_type with AI flags is skipped")


if __name__ == "__main__":
    test_ai_flag_detection()
    test_nodes_without_ai_flags_skipped()
    test_ai_nodes_detected_and_routed()
    test_no_duplicate_tasks_per_node_id()
    test_multiple_flags_one_task()
    test_analyze_orchestration()
    test_unknown_node_type_skipped()
    print("\nALL TESTS PASSED")
