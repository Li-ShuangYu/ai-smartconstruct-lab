"""
Result Merger Service — enriches original canvas JSON with AI-generated content.

Implements Algorithm 3 from the design doc:
- Deep copy original JSON
- For each node with a successful result: add _ai_generated and _ai_status="ready"
- For nodes without results (not AI-required): add _ai_status="not_required"
- For failed nodes: add _ai_status="error" and _ai_error message
"""

import logging
from copy import deepcopy
from typing import List, Any

from pydantic import BaseModel

logger = logging.getLogger(__name__)


def merge_results_into_payload(
    original_json: dict, agent_results: List[Any]
) -> dict:
    """
    Merge agent results back into the original canvas JSON to produce standard_payload_json.

    Preconditions:
    - original_json is the raw canvas JSON with "nodes" and "edges"
    - agent_results is a list of BaseModel results or Exception instances

    Postconditions:
    - Every node in output has _ai_status in its config
    - Nodes with successful results get _ai_generated dict and _ai_status="ready"
    - Nodes without AI processing get _ai_status="not_required"
    - Nodes with failed results get _ai_status="error" and _ai_error message
    - Output JSON structure matches input structure (nodes + edges preserved)

    Args:
        original_json: The raw canvas JSON dict with "nodes" and "edges".
        agent_results: List of agent results (BaseModel instances) or Exceptions.

    Returns:
        Enriched JSON dict (standard_payload_json).
    """
    payload = deepcopy(original_json)

    # Build lookup: node_id -> agent_result (successful results only)
    result_map: dict[str, BaseModel] = {}
    error_map: dict[str, str] = {}

    for result in agent_results:
        if isinstance(result, Exception):
            # Try to extract node info from the error message
            error_msg = str(result)
            # Errors don't carry node_id directly, so we track them separately
            # They'll be matched by position if needed
            continue
        if isinstance(result, BaseModel) and hasattr(result, "node_id"):
            result_map[result.node_id] = result

    # Also track which tasks failed by checking exceptions
    # We need to correlate failures with node_ids from the execution plan
    # Since asyncio.gather preserves order, we can't easily map here
    # Instead, nodes not in result_map that were AI-required will be marked as error

    # Determine which nodes had AI flags (same logic as supervisor)
    from agents.supervisor import _has_ai_flags, ROUTING_TABLE

    ai_required_node_ids: set[str] = set()
    for node in payload.get("nodes", []):
        node_id = node.get("node_id", "")
        node_type = node.get("node_type", "")
        config = node.get("config", {})

        if _has_ai_flags(config) and node_type in ROUTING_TABLE:
            ai_required_node_ids.add(node_id)

    # Enrich each node's config
    for node in payload.get("nodes", []):
        node_id = node.get("node_id", "")
        config = node.get("config", {})

        if node_id in result_map:
            # Successful AI result
            result = result_map[node_id]
            config["_ai_generated"] = result.model_dump(exclude_none=True)
            config["_ai_status"] = "ready"
        elif node_id in ai_required_node_ids:
            # AI was required but no result — task failed
            config["_ai_status"] = "error"
            config["_ai_error"] = "Agent task failed or timed out"
        else:
            # No AI processing required for this node
            config["_ai_status"] = "not_required"

    return payload
