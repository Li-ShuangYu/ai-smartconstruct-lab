"""
Property 4: AI spec routing is data-driven and correct.

**Validates: Requirements 2.1, 2.2, 5.2**

For any node with a valid ai_spec_json containing target_agent in
{"TEXT", "STRUCT", "EXAM", "CODE", "VIDEO"} and at least one enabled AI flag
in its config, the Supervisor SHALL route it to the agent type specified by
target_agent. Nodes with ai_spec_json set to NULL or with target_agent outside
the valid set SHALL be skipped.
"""

import sys
sys.path.insert(0, ".")

import pytest
from hypothesis import given, settings, assume
from hypothesis import strategies as st

from agents.supervisor import detect_ai_nodes_phased, _should_process_node, VALID_AGENT_TYPES
from models.schemas import AgentType


# --- Strategies ---

VALID_AGENTS = list(VALID_AGENT_TYPES)
INVALID_AGENTS = ["INVALID", "", "text", "AUDIO", "IMAGE", "GPT", "NONE", "NULL"]
NODE_TYPES = ["start", "resource_read", "homework", "exam", "end", "coding_class",
              "mindmap_preview", "mindmap_draw", "grouping", "learning_survey"]


@st.composite
def node_with_valid_routing(draw):
    """Generate a node with valid ai_spec and at least one enabled flag."""
    target_agent = draw(st.sampled_from(VALID_AGENTS))
    priority = draw(st.integers(min_value=1, max_value=10))
    flag_name = draw(st.text(min_size=5, max_size=30, alphabet="abcdefghijklmnopqrstuvwxyz_"))
    node_type = draw(st.sampled_from(NODE_TYPES))
    node_id = draw(st.text(min_size=1, max_size=20, alphabet="abcdefghijklmnopqrstuvwxyz0123456789_"))

    ai_spec = {
        "target_agent": target_agent,
        "priority": priority,
        "ai_flags": [flag_name],
    }
    config = {flag_name: True}
    node = {
        "node_id": node_id,
        "node_type": node_type,
        "config": config,
        "ai_spec": ai_spec,
    }
    return node, target_agent


@st.composite
def node_with_null_ai_spec(draw):
    """Generate a node with no ai_spec (should be skipped)."""
    node_type = draw(st.sampled_from(NODE_TYPES))
    node_id = draw(st.text(min_size=1, max_size=20, alphabet="abcdefghijklmnopqrstuvwxyz0123456789_"))
    config = {"enable_ai_something": True}
    node = {
        "node_id": node_id,
        "node_type": node_type,
        "config": config,
    }
    return node


@st.composite
def node_with_invalid_target_agent(draw):
    """Generate a node with invalid target_agent (should be skipped)."""
    invalid_agent = draw(st.sampled_from(INVALID_AGENTS))
    flag_name = "enable_ai_test"
    node_type = draw(st.sampled_from(NODE_TYPES))
    node_id = draw(st.text(min_size=1, max_size=20, alphabet="abcdefghijklmnopqrstuvwxyz0123456789_"))

    ai_spec = {
        "target_agent": invalid_agent,
        "priority": 5,
        "ai_flags": [flag_name],
    }
    config = {flag_name: True}
    node = {
        "node_id": node_id,
        "node_type": node_type,
        "config": config,
        "ai_spec": ai_spec,
    }
    return node


# --- Property Tests ---

class TestAiSpecRoutingProperty:
    """Property 4: AI spec routing correctness."""

    @given(data=node_with_valid_routing())
    @settings(max_examples=100)
    def test_valid_ai_spec_routes_to_correct_agent(self, data):
        """Nodes with valid ai_spec and enabled flags route to the correct agent."""
        node, expected_agent = data

        canvas = {
            "orchestration_id": "test",
            "phases": [{
                "phase_id": "p1",
                "phase_name": "Test",
                "sort_num": 1,
                "nodes": [node],
                "edges": [],
            }],
        }

        tasks = detect_ai_nodes_phased(canvas)
        assert len(tasks) == 1
        assert tasks[0].agent_type == AgentType(f"{expected_agent}_AGENT")
        assert tasks[0].node_id == node["node_id"]

    @given(node=node_with_null_ai_spec())
    @settings(max_examples=100)
    def test_null_ai_spec_is_skipped(self, node):
        """Nodes without ai_spec are always skipped."""
        canvas = {
            "orchestration_id": "test",
            "phases": [{
                "phase_id": "p1",
                "phase_name": "Test",
                "sort_num": 1,
                "nodes": [node],
                "edges": [],
            }],
        }

        tasks = detect_ai_nodes_phased(canvas)
        assert len(tasks) == 0

    @given(node=node_with_invalid_target_agent())
    @settings(max_examples=100)
    def test_invalid_target_agent_is_skipped(self, node):
        """Nodes with invalid target_agent are always skipped."""
        canvas = {
            "orchestration_id": "test",
            "phases": [{
                "phase_id": "p1",
                "phase_name": "Test",
                "sort_num": 1,
                "nodes": [node],
                "edges": [],
            }],
        }

        tasks = detect_ai_nodes_phased(canvas)
        assert len(tasks) == 0

    @given(
        target_agent=st.sampled_from(VALID_AGENTS),
        num_nodes=st.integers(min_value=1, max_value=8),
    )
    @settings(max_examples=100)
    def test_all_valid_nodes_produce_tasks(self, target_agent, num_nodes):
        """All nodes with valid routing produce exactly one task each."""
        nodes = []
        for i in range(num_nodes):
            nodes.append({
                "node_id": f"node_{i}",
                "node_type": "resource_read",
                "config": {"enable_ai_summary": True},
                "ai_spec": {
                    "target_agent": target_agent,
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

        tasks = detect_ai_nodes_phased(canvas)
        assert len(tasks) == num_nodes
        for task in tasks:
            assert task.agent_type == AgentType(f"{target_agent}_AGENT")
