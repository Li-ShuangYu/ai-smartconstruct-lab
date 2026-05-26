"""
Prompt templates for CodeAgent.

Covers:
- Review checkpoints (CODING_CLASS with enable_code_review)
- Evaluation indicators/rubrics (PLAN_UPLOAD with enable_ai_pre_evaluation)
"""


def review_checkpoints_prompt(
    node_id: str,
    language: str,
    task_description: str,
    difficulty: str,
    focus_areas: list[str] | None = None,
) -> str:
    """Build a prompt for generating code review checkpoints."""
    focus_text = (
        "、".join(focus_areas) if focus_areas else "代码质量、逻辑正确性、规范性"
    )

    return (
        f"为一个编程练习生成代码审查检查点（review checkpoints）。\n"
        f"编程语言：{language}\n"
        f"任务描述：{task_description}\n"
        f"难度：{difficulty}\n"
        f"重点关注：{focus_text}\n\n"
        f"每个检查点格式：\n"
        f"- checkpoint_name: 检查点名称\n"
        f"- description: 检查内容描述\n"
        f"- category: 类别（correctness/style/performance/security）\n"
        f"- weight: 权重（0-1之间，所有权重之和为1）\n"
        f"- pass_criteria: 通过标准\n\n"
        f"要求：生成5-8个检查点，覆盖代码正确性、风格规范、性能和安全性。\n"
        f"请生成结构化JSON，包含 node_id=\"{node_id}\" 和 review_checkpoints 数组。"
    )


def eval_indicators_prompt(
    node_id: str,
    plan_type: str,
    max_score: int = 100,
    evaluation_dimensions: list[str] | None = None,
) -> str:
    """Build a prompt for generating evaluation indicators/rubrics."""
    dims_text = (
        "、".join(evaluation_dimensions)
        if evaluation_dimensions
        else "完整性、可行性、创新性、规范性"
    )

    return (
        f"为「{plan_type}」类型的方案提交生成AI预评价指标体系。\n"
        f"评价维度：{dims_text}\n"
        f"总分：{max_score}分\n\n"
        f"每个评价指标格式：\n"
        f"- indicator_name: 指标名称\n"
        f"- dimension: 所属维度\n"
        f"- description: 评价标准描述\n"
        f"- max_score: 该指标满分\n"
        f"- scoring_rubric: 评分细则（包含各分数段的标准）\n\n"
        f"要求：各指标满分之和等于{max_score}分，覆盖所有评价维度。\n"
        f"请生成结构化JSON，包含 node_id=\"{node_id}\" 和 eval_indicators 数组。"
    )
