"""
Property 5: Nodes without enabled AI flags are skipped.

**Validates: Requirements 2.4**

For any node whose config has ALL fields listed in ai_spec_json.ai_flags set
to false (or missing) AND no field with source_mode="ai", the AI pipeline SHALL
exclude that node from the task list regardless of its node type or ai_spec definition.
"""

import sys
sys.path.insert(0, ".")

import pytest
from hypothesis import given, settings, assume
from hypothesis import strategies as st

from agents.supervisor import detect_ai_nodes_phased, _should_process_node, VALID_AGENT_TYPES


# --- Strategies ---

VALID_AGENTS = list(VALID_AGENT_TYPES)
NODE_TYPES = ["start", "resource_read", "homework", "exam", "end", "coding_class",
              "mindmap_preview", "mindmap_draw", "grouping", "learning_survey"]


@st.composite
def node_all_flags_disabled(draw):
    """Generate a node where all ai_flags are disabled and no source_mode='ai'."""
    target_agent = draw(st.sampled_from(VALID_AGENTS))
    num_flags = draw(st.integers(min_value=1, max_value=5))
    flag_names = [f"enable_ai_flag_{i}" for i in range(num_flags)]
    node_type = draw(st.sampled_from(NODE_TYPES))
    node_id = draw(st.text(min_size=1, max_size=20, alphabet="abcdefghijklmnopqrstuvwxyz0123456789_"))

    # All flags set to False or missing
    config = {}
    for flag in flag_names:
        # Randomly either set to False or omit entirely
        include = draw(st.booleans())
        if include:
            config[flag] = False

    # Ensure no source_mode="ai"
    has_source_mode = draw(st.booleans())
    if has_source_mode:
        config["source_mode"] = draw(st.sampled_from(["manual", "bank_select", "teacher"]))

    ai_spec = {
        "target_agent": target_agent,
        "priority": draw(st.integers(min_value=1, max_value=10)),
        "ai_flags": flag_names,
    }

    node = {
        "node_id": node_id,
        "node_type": node_type,
        "config": config,
        "ai_spec": ai_spec,
    }
    return node


@st.composite
def mixed_nodes_all_disabled(draw):
    """Generate multiple nodes where ALL have flags disabled."""
    num_nodes = draw(st.integers(min_value=1, max_value=8))
    nodes = []
    for i in range(num_nodes):
        node = draw(node_all_flags_disabled())
        node["node_id"] = f"node_{i}"  # Ensure unique IDs
        nodes.append(node)
    return nodes


# --- Property Tests ---

class TestAiFlagSkipProperty:
    """Property 5: Nodes with all AI flags disabled are excluded."""

    @given(node=node_all_flags_disabled())
    @settings(max_examples=100)
    def test_single_node_all_flags_disabled_is_skipped(self, node):
        """A single node with all flags disabled is excluded from task list."""
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
        assert len(tasks) == 0, f"Expected 0 tasks but got {len(tasks)} for node config: {node['config']}"

    @given(nodes=mixed_nodes_all_disabled())
    @settings(max_examples=100)
    def test_multiple_nodes_all_disabled_produces_empty_task_list(self, nodes):
        """Multiple nodes with all flags disabled produce an empty task list."""
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
        assert len(tasks) == 0

    @given(
        target_agent=st.sampled_from(VALID_AGENTS),
        node_type=st.sampled_from(NODE_TYPES),
    )
    @settings(max_examples=100)
    def test_regardless_of_node_type_or_agent(self, target_agent, node_type):
        """Skipping applies regardless of node_type or target_agent value."""
        node = {
            "node_id": "test_node",
            "node_type": node_type,
            "config": {
                "enable_ai_flag_1": False,
                "enable_ai_flag_2": False,
                "source_mode": "manual",
            },
            "ai_spec": {
                "target_agent": target_agent,
                "priority": 5,
                "ai_flags": ["enable_ai_flag_1", "enable_ai_flag_2"],
            },
        }

        result = _should_process_node(node, node["ai_spec"])
        assert result is False

    @given(node=node_all_flags_disabled())
    @settings(max_examples=100)
    def test_should_process_node_returns_false(self, node):
        """_should_process_node returns False for nodes with all flags disabled."""
        result = _should_process_node(node, node.get("ai_spec"))
        assert result is False
