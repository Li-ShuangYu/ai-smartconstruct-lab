"""
Supervisor Agent — parses orchestration canvas JSON, detects AI-enabled nodes,
and generates a parallel execution plan.

Refactored to use data-driven routing via ai_spec_json instead of a hardcoded
ROUTING_TABLE. Each node declares its AI requirements through ai_spec attached
to the node in the canvas JSON.
"""

import logging

from models.schemas import AgentTask, AgentType, ExecutionPlan

logger = logging.getLogger(__name__)

# Valid agent type identifiers that can appear in ai_spec.target_agent
VALID_AGENT_TYPES = {"TEXT", "STRUCT", "EXAM", "CODE", "VIDEO"}


def _should_process_node(node: dict, ai_spec: dict | None) -> bool:
    """
    Determine whether a node requires AI processing based on its ai_spec.

    A node is processed when ALL of the following are true:
    1. ai_spec is not None
    2. ai_spec.target_agent is in the VALID_AGENT_TYPES set
    3. At least one of:
       - A flag listed in ai_spec.ai_flags is True in the node config
       - The node config contains source_mode="ai"

    Teacher overrides: fields listed in config.ai_processing._overrides are
    considered already handled by the teacher and excluded from AI flag checks.

    Args:
        node: The raw node dict from the canvas JSON.
        ai_spec: The ai_spec dict for this node (may be None).

    Returns:
        True if the node should be dispatched for AI processing.
    """
    if ai_spec is None:
        logger.warning(
            "Node %s: ai_spec is null, skipping AI processing",
            node.get("node_id", "unknown"),
        )
        return False

    target_agent = ai_spec.get("target_agent", "")
    if target_agent not in VALID_AGENT_TYPES:
        logger.warning(
            "Node %s: invalid target_agent '%s', skipping AI processing",
            node.get("node_id", "unknown"),
            target_agent,
        )
        return False

    config = node.get("config", {})

    # Determine teacher-overridden fields to skip
    ai_processing = config.get("ai_processing", {})
    overrides = ai_processing.get("_overrides", {})
    overridden_fields: set[str] = {
        field for field, is_overridden in overrides.items() if is_overridden
    }

    # Check if any declared AI flag is enabled (and not overridden)
    ai_flags = ai_spec.get("ai_flags", [])
    for flag in ai_flags:
        if flag in overridden_fields:
            continue
        if config.get(flag) is True:
            return True

    # Check for source_mode == "ai" (not subject to override check)
    for key, value in config.items():
        if key == "source_mode" and value == "ai":
            return True

    return False


def detect_ai_nodes_phased(canvas_json: dict) -> list[AgentTask]:
    """
    Traverse all phases and nodes in the canvas JSON, building a prioritized
    list of AgentTasks based on each node's ai_spec.

    The canvas_json is expected to have the phased structure:
    {
        "orchestration_id": "...",
        "phases": [
            {
                "phase_id": "...",
                "phase_name": "...",
                "sort_num": 1,
                "nodes": [...],
                "edges": [...]
            },
            ...
        ]
    }

    Nodes are deduplicated by node_id. The returned list is sorted by priority
    (lower number = higher priority).

    Args:
        canvas_json: The raw canvas JSON dict with phased structure.

    Returns:
        A sorted list of AgentTask objects for AI-enabled nodes.
    """
    tasks: list[AgentTask] = []
    seen_node_ids: set[str] = set()
    orchestration_id = canvas_json.get("orchestration_id", "unknown")

    for phase in canvas_json.get("phases", []):
        for node in phase.get("nodes", []):
            node_id = node.get("node_id", "")
            if node_id in seen_node_ids:
                continue

            ai_spec = node.get("ai_spec")
            if not _should_process_node(node, ai_spec):
                continue

            target_agent = ai_spec["target_agent"]
            priority = ai_spec.get("priority", 5)

            task = AgentTask(
                task_id=f"{orchestration_id}_{node_id}",
                agent_type=AgentType(f"{target_agent}_AGENT"),
                node_id=node_id,
                node_type=node.get("node_type", ""),
                config=node.get("config", {}),
                priority=priority,
                phase_id=phase.get("phase_id"),
            )
            tasks.append(task)
            seen_node_ids.add(node_id)

    # Sort by priority (lower number = higher priority)
    tasks.sort(key=lambda t: t.priority)
    return tasks


class SupervisorAgent:
    """
    Supervisor Agent responsible for analyzing orchestration canvas JSON
    and producing a parallel execution plan for domain agents.

    Uses data-driven routing: each node's ai_spec declares which agent
    should handle it, eliminating the need for a hardcoded routing table.
    """

    def analyze_orchestration(self, canvas_json: dict) -> ExecutionPlan:
        """
        Parse the canvas JSON, detect AI-enabled nodes, and return an ExecutionPlan.

        Args:
            canvas_json: The raw canvas JSON dict with phased structure
                         containing "phases" and "orchestration_id".

        Returns:
            An ExecutionPlan containing all parallel AgentTasks sorted by priority.
        """
        orchestration_id = canvas_json.get("orchestration_id", "unknown")
        tasks = detect_ai_nodes_phased(canvas_json)

        return ExecutionPlan(
            orchestration_id=orchestration_id,
            parallel_jobs=tasks,
            total_tasks=len(tasks),
        )
