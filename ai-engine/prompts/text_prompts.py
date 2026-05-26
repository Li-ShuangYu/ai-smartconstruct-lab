"""
Prompt templates for TextAgent.

Covers:
- Welcome message generation (START nodes)
- Resource summary, key points, navigation tree (RESOURCE_READ nodes)
- System prompt generation (AI_PRACTICE nodes)
"""


def welcome_prompt(node_id: str, training_name: str, description: str) -> str:
    """Build a prompt for generating a welcome message (150-250 chars)."""
    return (
        f"为一个名为「{training_name}」的实训课程生成一段欢迎语。"
        f"课程简介：{description}\n"
        f"要求：150-250个字符，语气友好专业，激发学习兴趣。"
        f"直接输出欢迎语文本，不要加引号或额外说明。\n\n"
        f"请以JSON格式返回，字段：node_id=\"{node_id}\", welcome_message=生成的欢迎语"
    )


def resource_content_prompt(
    node_id: str,
    resource_name: str,
    resource_content: str,
    enable_summary: bool = False,
    enable_key_points: bool = False,
    enable_nav: bool = False,
) -> str:
    """Build a prompt for generating summary, key points, and/or navigation tree."""
    parts = []
    if enable_summary:
        parts.append("summary: 资源内容摘要（不超过300字符）")
    if enable_key_points:
        parts.append("key_points: 5-7个重点知识点，每个包含 {title, description} 字段")
    if enable_nav:
        parts.append(
            "navigation_tree: 导航树结构，每个节点包含 {title, anchor, children} 字段"
        )

    fields_desc = "\n".join(parts) if parts else "summary: 资源内容摘要（不超过300字符）"

    return (
        f"为学习资源「{resource_name}」生成以下内容：\n"
        f"{fields_desc}\n\n"
        f"资源预览内容：{resource_content[:500]}\n\n"
        f"请生成结构化的JSON结果，node_id=\"{node_id}\"。"
    )


def system_prompt_prompt(
    node_id: str, practice_topic: str, practice_goal: str, persona: str
) -> str:
    """Build a prompt for generating a system prompt for AI_PRACTICE nodes."""
    return (
        f"为一个AI对话练习节点生成系统提示词（system prompt）。\n"
        f"练习主题：{practice_topic}\n"
        f"练习目标：{practice_goal}\n"
        f"AI角色：{persona}\n\n"
        f"要求：生成一段清晰的系统提示词，定义AI的角色、行为规范和对话策略。"
        f"将结果放在welcome_message字段中，node_id=\"{node_id}\"。"
    )
