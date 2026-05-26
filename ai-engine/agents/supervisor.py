"""
Supervisor Agent — parses orchestration canvas JSON, detects AI-enabled nodes,
and generates a parallel execution plan.
"""

from models.schemas import AgentTask, AgentType, ExecutionPlan


# Node type → Agent type routing table
ROUTING_TABLE: dict[str, AgentType] = {
    "START": AgentType.TEXT,            # enable_ai_welcome
    "RESOURCE_READ": AgentType.TEXT,    # enable_ai_summary, key_points, quick_nav
    "VIDEO_LEARN": AgentType.VIDEO,     # enable_ai_subtitle, enable_ai_chapter
    "MINDMAP_PREVIEW": AgentType.STRUCT,  # enable_ai_generate_map
    "THEORY_CLASS": AgentType.EXAM,     # enable_ai_error_book (flashcards)
    "TASK_DISTRIBUTE": AgentType.STRUCT,  # enable_ai_task_split
    "HOMEWORK": AgentType.EXAM,         # source_mode == "ai"
    "CODING_CLASS": AgentType.CODE,     # enable_code_review
    "PLAN_UPLOAD": AgentType.CODE,      # enable_ai_pre_evaluation
    "AI_PRACTICE": AgentType.TEXT,      # system prompt generation
}


def _has_ai_flags(config: dict) -> bool:
    """
    Check whether a node's config contains any AI-processing flags.

    AI flags are:
    - Any key starting with "enable_ai_" with value True
    - "source_mode" == "ai"
    - "enable_code_review" == True
    - "enable_ai_pre_evaluation" == True (also caught by enable_ai_* prefix)
    """
    for key, value in config.items():
        if key.startswith("enable_ai_") and value is True:
            return True
        if key == "source_mode" and value == "ai":
            return True
        if key == "enable_code_review" and value is True:
            return True
    return False


def detect_ai_nodes(canvas_json: dict) -> list[AgentTask]:
    """
    Traverse all nodes in the canvas JSON and identify nodes requiring AI processing.

    For each node:
    1. Extract its config and check for AI flags.
    2. If AI flags are present AND the node_type is in the routing table,
       create exactly one AgentTask (no duplicates per node_id).
    3. Nodes without AI flags or with node_types not in the routing table are skipped.

    Args:
        canvas_json: The raw canvas JSON dict containing "nodes" and "edges".

    Returns:
        A list of AgentTask objects, one per AI-enabled node.
    """
    tasks: list[AgentTask] = []
    seen_node_ids: set[str] = set()

    orchestration_id = canvas_json.get("orchestration_id", "unknown")

    for node in canvas_json.get("nodes", []):
        node_id = node.get("node_id", "")
        node_type = node.get("node_type", "")
        config = node.get("config", {})

        # Skip if we already created a task for this node_id (deduplication)
        if node_id in seen_node_ids:
            continue

        # Skip nodes without AI flags
        if not _has_ai_flags(config):
            continue

        # Skip node types not in the routing table
        agent_type = ROUTING_TABLE.get(node_type)
        if agent_type is None:
            continue

        task = AgentTask(
            task_id=f"{orchestration_id}_{node_id}",
            agent_type=agent_type,
            node_id=node_id,
            node_type=node_type,
            config=config,
        )
        tasks.append(task)
        seen_node_ids.add(node_id)

    return tasks


class SupervisorAgent:
    """
    Supervisor Agent responsible for analyzing orchestration canvas JSON
    and producing a parallel execution plan for domain agents.
    """

    def analyze_orchestration(self, canvas_json: dict) -> ExecutionPlan:
        """
        Parse the canvas JSON, detect AI-enabled nodes, and return an ExecutionPlan.

        Args:
            canvas_json: The raw canvas JSON dict with "nodes", "edges",
                         and "orchestration_id".

        Returns:
            An ExecutionPlan containing all parallel AgentTasks.
        """
        orchestration_id = canvas_json.get("orchestration_id", "unknown")
        tasks = detect_ai_nodes(canvas_json)

        return ExecutionPlan(
            orchestration_id=orchestration_id,
            parallel_jobs=tasks,
            total_tasks=len(tasks),
        )
