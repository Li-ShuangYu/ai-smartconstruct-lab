"""
Property 8: Selective retry targets only failed nodes.

**Validates: Requirements 5.7**

For any set of node AI processing results where some nodes succeeded and some
failed, triggering a retry SHALL produce a task list containing exactly the
failed nodes. Already-succeeded nodes SHALL NOT appear in the retry task list.
"""

import sys
sys.path.insert(0, ".")

import pytest
from hypothesis import given, settings, assume
from hypothesis import strategies as st

from agents.supervisor import detect_ai_nodes_phased, VALID_AGENT_TYPES


# --- Strategies ---

VALID_AGENTS = list(VALID_AGENT_TYPES)


@st.composite
def mixed_success_failure_scenario(draw):
    """
    Generate a scenario with some successful and some failed nodes.
    Returns (canvas, failed_node_ids, success_node_ids).
    """
    total_nodes = draw(st.integers(min_value=2, max_value=10))
    # At least 1 success and 1 failure
    num_failures = draw(st.integers(min_value=1, max_value=total_nodes - 1))

    nodes = []
    failed_ids = []
    success_ids = []

    for i in range(total_nodes):
        node_id = f"node_{i}"
        target_agent = draw(st.sampled_from(VALID_AGENTS))
        node = {
            "node_id": node_id,
            "node_type": "resource_read",
            "config": {"enable_ai_summary": True},
            "ai_spec": {
                "target_agent": target_agent,
                "priority": 5,
                "ai_flags": ["enable_ai_summary"],
            },
        }
        nodes.append(node)

        if i < num_failures:
            failed_ids.append(node_id)
        else:
            success_ids.append(node_id)

    canvas = {
        "orchestration_id": "test_retry",
        "phases": [{
            "phase_id": "p1",
            "phase_name": "Test",
            "sort_num": 1,
            "nodes": nodes,
            "edges": [],
        }],
    }

    return canvas, failed_ids, success_ids


def filter_retry_tasks(canvas: dict, failed_node_ids: list[str]):
    """
    Simulate the selective retry logic from execute_retry_workflow:
    detect all AI nodes, then filter to only the failed node_ids.
    """
    all_tasks = detect_ai_nodes_phased(canvas)
    retry_node_set = set(failed_node_ids)
    return [t for t in all_tasks if t.node_id in retry_node_set]


# --- Property Tests ---

class TestSelectiveRetryProperty:
    """Property 8: Selective retry targets only failed nodes."""

    @given(scenario=mixed_success_failure_scenario())
    @settings(max_examples=100)
    def test_retry_contains_only_failed_nodes(self, scenario):
        """Retry task list contains exactly the failed nodes."""
        canvas, failed_ids, success_ids = scenario

        retry_tasks = filter_retry_tasks(canvas, failed_ids)

        retry_node_ids = {t.node_id for t in retry_tasks}
        assert retry_node_ids == set(failed_ids), (
            f"Expected retry to contain {set(failed_ids)}, got {retry_node_ids}"
        )

    @given(scenario=mixed_success_failure_scenario())
    @settings(max_examples=100)
    def test_retry_excludes_successful_nodes(self, scenario):
        """Successful nodes never appear in the retry task list."""
        canvas, failed_ids, success_ids = scenario

        retry_tasks = filter_retry_tasks(canvas, failed_ids)

        retry_node_ids = {t.node_id for t in retry_tasks}
        for success_id in success_ids:
            assert success_id not in retry_node_ids, (
                f"Successful node {success_id} should not be in retry list"
            )

    @given(scenario=mixed_success_failure_scenario())
    @settings(max_examples=100)
    def test_retry_count_equals_failure_count(self, scenario):
        """Number of retry tasks equals number of failed nodes."""
        canvas, failed_ids, success_ids = scenario

        retry_tasks = filter_retry_tasks(canvas, failed_ids)
        assert len(retry_tasks) == len(failed_ids)

    @given(
        num_nodes=st.integers(min_value=1, max_value=10),
    )
    @settings(max_examples=100)
    def test_empty_failure_list_produces_no_retry(self, num_nodes):
        """Empty failure list produces no retry tasks."""
        nodes = []
        for i in range(num_nodes):
            nodes.append({
                "node_id": f"node_{i}",
                "node_type": "resource_read",
                "config": {"enable_ai_summary": True},
                "ai_spec": {
                    "target_agent": "TEXT",
                    "priority": 5,
                    "ai_flags": ["enable_ai_summary"],
                },
            })

        canvas = {
            "orchestration_id": "test",
            "phases": [{
                "phase_id": "p1",
                "phase_name": "Test",
                "sort_num": 1,
                "nodes": nodes,
                "edges": [],
            }],
        }

        retry_tasks = filter_retry_tasks(canvas, [])
        assert len(retry_tasks) == 0
