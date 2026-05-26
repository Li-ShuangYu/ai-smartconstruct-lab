"""
Prompt templates for ExamAgent.

Covers:
- Question generation (HOMEWORK with source_mode=ai)
- Flashcard generation (THEORY_CLASS with enable_ai_error_book)
"""


def questions_prompt(
    node_id: str,
    subject: str,
    knowledge_scope: str,
    difficulty_level: str,
    question_type_counts: dict[str, int],
) -> str:
    """Build a prompt for generating exam questions matching type counts."""
    type_names = {
        "1": "单选题",
        "2": "多选题",
        "3": "填空题",
        "4": "判断题",
        "5": "简答题",
    }

    type_details = []
    for qtype, count in question_type_counts.items():
        name = type_names.get(str(qtype), f"类型{qtype}")
        type_details.append(f"- {name}（type={qtype}）：{count}道")

    type_spec = "\n".join(type_details)

    difficulty_map = {"easy": "简单", "medium": "中等", "hard": "困难"}
    difficulty_cn = difficulty_map.get(difficulty_level, "中等")

    return (
        f"为以下知识范围生成考试题目：\n"
        f"学科/主题：{subject}\n"
        f"知识范围：{knowledge_scope}\n"
        f"难度：{difficulty_cn}\n\n"
        f"题目数量要求：\n{type_spec}\n\n"
        f"每道题的格式要求：\n"
        f"- question_type: 题型编号(1-5)\n"
        f"- content: 题干内容（选择题需包含选项，JSON字符串格式）\n"
        f"- standard_answer: 标准答案\n"
        f"- default_score: 默认分值（单选2分，多选3分，填空2分，判断1分，简答5分）\n"
        f"- knowledge_point: 对应知识点\n\n"
        f"请生成结构化JSON，包含 node_id=\"{node_id}\" 和 questions 数组。"
    )


def flashcards_prompt(
    node_id: str,
    topic: str,
    card_count: int = 10,
    knowledge_points: list[str] | None = None,
) -> str:
    """Build a prompt for generating flashcards with front/back content."""
    kp_text = "、".join(knowledge_points) if knowledge_points else topic

    return (
        f"为以下知识点生成{card_count}张闪卡（flashcard）：\n"
        f"知识点：{kp_text}\n\n"
        f"每张闪卡格式：\n"
        f"- front_content: 正面内容（问题或概念名称）\n"
        f"- back_content: 背面内容（答案或解释）\n"
        f"- order: 序号（从1开始）\n\n"
        f"要求：覆盖核心概念，由浅入深排列。\n"
        f"请生成结构化JSON，包含 node_id=\"{node_id}\" 和 flashcards 数组。"
    )
