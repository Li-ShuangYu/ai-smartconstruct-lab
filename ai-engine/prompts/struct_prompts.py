"""
Prompt templates for StructAgent.

Covers:
- Mindmap generation (MINDMAP_PREVIEW nodes)
- Task breakdown (TASK_DISTRIBUTE nodes)
"""


def mindmap_prompt(
    node_id: str,
    topic: str,
    knowledge_scope: str,
    max_nodes: int = 20,
    max_depth: int = 4,
) -> str:
    """Build a prompt for generating a mindmap topology."""
    return (
        f"为主题「{topic}」生成一个思维导图结构。\n"
        f"知识范围：{knowledge_scope}\n\n"
        f"要求：\n"
        f"- 总节点数不超过{max_nodes}个\n"
        f"- 层级深度为3-{max_depth}层\n"
        f"- 根节点为主题名称\n"
        f"- children为数组，每个子节点包含 {{\"label\": \"节点名\", \"children\": [...]}} 结构\n\n"
        f"请生成结构化JSON，包含 node_id=\"{node_id}\" 和 mindmap 字段"
        f"（mindmap包含 root 和 children）。"
    )


def task_breakdown_prompt(
    node_id: str, task_name: str, task_description: str, total_hours: int = 8
) -> str:
    """Build a prompt for generating a task breakdown with 5-7 milestones."""
    return (
        f"为实训任务「{task_name}」生成任务拆解方案。\n"
        f"任务描述：{task_description}\n"
        f"总学时：{total_hours}小时\n\n"
        f"要求：\n"
        f"- 拆解为5-7个里程碑阶段\n"
        f"- 每个阶段包含 {{\"milestone\": \"阶段名\", \"description\": \"描述\", "
        f"\"estimated_hours\": 数值, \"deliverables\": [\"交付物\"]}} 结构\n"
        f"- 各阶段学时之和应接近总学时\n\n"
        f"请生成结构化JSON，包含 node_id=\"{node_id}\" 和 task_breakdown 字段。"
    )
