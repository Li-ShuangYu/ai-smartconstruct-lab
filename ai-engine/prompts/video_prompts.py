"""
Prompt templates for VideoAgent.

Covers:
- Chapter segmentation (VIDEO_LEARN nodes)
"""


def chapter_segmentation_prompt(
    node_id: str,
    video_title: str,
    video_duration: str,
    video_description: str = "",
    subtitle_text: str = "",
) -> str:
    """Build a prompt for generating video chapter segmentation (4-8 chapters)."""
    prompt = (
        f"为教学视频「{video_title}」生成章节切片。\n"
        f"视频时长：{video_duration}\n"
        f"视频描述：{video_description}\n"
    )

    if subtitle_text:
        prompt += f"字幕预览：{subtitle_text[:500]}\n"

    prompt += (
        f"\n要求：\n"
        f"- 生成4-8个章节\n"
        f"- 每个章节包含：title（章节标题）、start_time（开始时间HH:MM:SS）、"
        f"end_time（结束时间HH:MM:SS）、summary（章节摘要）\n"
        f"- 章节时间应连续覆盖整个视频时长\n"
        f"- 第一个章节从00:00:00开始，最后一个章节结束时间为视频总时长\n\n"
        f"请生成结构化JSON，包含 node_id=\"{node_id}\" 和 chapters 数组。"
    )

    return prompt
