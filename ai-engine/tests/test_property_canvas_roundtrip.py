"""
Property 2: Canvas JSON phase structure round-trip.

**Validates: Requirements 1.3**

For any valid canvas JSON with phases containing nodes and edges,
serializing to JSON string and deserializing back SHALL produce a
structurally equivalent object where all phase IDs, node IDs, node configs,
and edge connections are preserved.
"""

import sys
sys.path.insert(0, ".")

import json
import pytest
from hypothesis import given, settings
from hypothesis import strategies as st


# --- Strategies ---

def node_config_strategy():
    """Generate a valid 6-dimension node config."""
    return st.fixed_dictionaries({
        "display": st.dictionaries(st.text(min_size=1, max_size=10), st.text(max_size=20), max_size=3),
        "db_mapping": st.dictionaries(st.text(min_size=1, max_size=10), st.integers(), max_size=3),
        "ai_processing": st.dictionaries(st.text(min_size=1, max_size=10), st.booleans(), max_size=3),
        "orchestration_settings": st.dictionaries(st.text(min_size=1, max_size=10), st.integers(), max_size=3),
        "data_collection": st.dictionaries(st.text(min_size=1, max_size=10), st.booleans(), max_size=3),
        "evaluation": st.dictionaries(st.text(min_size=1, max_size=10), st.floats(allow_nan=False, allow_infinity=False), max_size=3),
    })


def node_strategy(node_id_prefix: str = "node"):
    """Generate a valid orchestration node."""
    return st.fixed_dictionaries({
        "node_id": st.text(min_size=1, max_size=20, alphabet="abcdefghijklmnopqrstuvwxyz0123456789_"),
        "node_type": st.sampled_from(["start", "resource_read", "homework", "exam", "end", "coding_class"]),
        "config": node_config_strategy(),
        "position": st.fixed_dictionaries({
            "x": st.floats(min_value=-1000, max_value=1000, allow_nan=False, allow_infinity=False),
            "y": st.floats(min_value=-1000, max_value=1000, allow_nan=False, allow_infinity=False),
        }),
    })


@st.composite
def edge_strategy(draw, node_ids: list[str]):
    """Generate edges between existing nodes."""
    if len(node_ids) < 2:
        return []
    edges = []
    num_edges = draw(st.integers(min_value=0, max_value=min(5, len(node_ids) - 1)))
    for _ in range(num_edges):
        source = draw(st.sampled_from(node_ids))
        target = draw(st.sampled_from([nid for nid in node_ids if nid != source]))
        edges.append({"source": source, "target": target})
    return edges


@st.composite
def phase_strategy(draw, sort_num: int):
    """Generate a valid phase with nodes and edges."""
    phase_id = f"phase_{sort_num}_{draw(st.text(min_size=3, max_size=8, alphabet='abcdefghijklmnopqrstuvwxyz'))}"
    phase_name = draw(st.text(min_size=1, max_size=20, alphabet="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"))

    num_nodes = draw(st.integers(min_value=0, max_value=5))
    nodes = []
    for i in range(num_nodes):
        node = draw(node_strategy())
        # Ensure unique node_id within phase
        node["node_id"] = f"{phase_id}_node_{i}"
        nodes.append(node)

    node_ids = [n["node_id"] for n in nodes]
    edges = draw(edge_strategy(node_ids)) if len(node_ids) >= 2 else []

    return {
        "phase_id": phase_id,
        "phase_name": phase_name,
        "sort_num": sort_num,
        "nodes": nodes,
        "edges": edges,
    }


@st.composite
def canvas_json_strategy(draw):
    """Generate a valid canvas JSON with phased structure."""
    orchestration_id = draw(st.text(min_size=1, max_size=30, alphabet="abcdefghijklmnopqrstuvwxyz0123456789_-"))
    num_phases = draw(st.integers(min_value=1, max_value=5))

    phases = []
    for i in range(num_phases):
        phase = draw(phase_strategy(i + 1))
        phases.append(phase)

    return {
        "orchestration_id": orchestration_id,
        "phases": phases,
    }


# --- Property Tests ---

class TestCanvasRoundTrip:
    """Property 2: Canvas JSON round-trip serialization."""

    @given(canvas=canvas_json_strategy())
    @settings(max_examples=100)
    def test_serialize_deserialize_preserves_structure(self, canvas):
        """Serializing to JSON and deserializing back preserves all structure."""
        # Serialize
        json_str = json.dumps(canvas, ensure_ascii=False)

        # Deserialize
        restored = json.loads(json_str)

        # Assert structural equality
        assert restored["orchestration_id"] == canvas["orchestration_id"]
        assert len(restored["phases"]) == len(canvas["phases"])

        for orig_phase, rest_phase in zip(canvas["phases"], restored["phases"]):
            assert rest_phase["phase_id"] == orig_phase["phase_id"]
            assert rest_phase["phase_name"] == orig_phase["phase_name"]
            assert rest_phase["sort_num"] == orig_phase["sort_num"]
            assert len(rest_phase["nodes"]) == len(orig_phase["nodes"])
            assert len(rest_phase["edges"]) == len(orig_phase["edges"])

            for orig_node, rest_node in zip(orig_phase["nodes"], rest_phase["nodes"]):
                assert rest_node["node_id"] == orig_node["node_id"]
                assert rest_node["node_type"] == orig_node["node_type"]
                assert rest_node["config"] == orig_node["config"]

            for orig_edge, rest_edge in zip(orig_phase["edges"], rest_phase["edges"]):
                assert rest_edge["source"] == orig_edge["source"]
                assert rest_edge["target"] == orig_edge["target"]

    @given(canvas=canvas_json_strategy())
    @settings(max_examples=100)
    def test_double_roundtrip_is_idempotent(self, canvas):
        """Double round-trip produces identical result to single round-trip."""
        json_str_1 = json.dumps(canvas, sort_keys=True)
        restored_1 = json.loads(json_str_1)
        json_str_2 = json.dumps(restored_1, sort_keys=True)
        restored_2 = json.loads(json_str_2)

        assert restored_1 == restored_2
        assert json_str_1 == json_str_2
