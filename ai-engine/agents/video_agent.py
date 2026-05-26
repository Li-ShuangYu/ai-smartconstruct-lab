"""
VideoAgent — handles video-related AI generation tasks.

Responsible for:
- Chapter segmentation (VIDEO_LEARN, 4-8 chapters per video)
"""

from models.agent_outputs import VideoAgentResult, GeneratedChapter
from models.schemas import AgentTask
from agents.base_agent import BaseAgent
from prompts.video_prompts import chapter_segmentation_prompt


class VideoAgent(BaseAgent):
    """Domain agent for video content: chapter segmentation."""

    async def execute(self, task: AgentTask) -> VideoAgentResult:
        """
        Execute a video generation task based on node_type.

        Routes to the appropriate generation method:
        - VIDEO_LEARN → chapter segmentation
        """
        node_type = task.node_type
        config = task.config

        if node_type == "VIDEO_LEARN":
            return await self._generate_chapters(task.node_id, config)
        else:
            return VideoAgentResult(node_id=task.node_id)

    async def _generate_chapters(self, node_id: str, config: dict) -> VideoAgentResult:
        """Generate chapter segmentation (4-8 chapters) for a VIDEO_LEARN node."""
        video_title = config.get("video_title", "教学视频")
        video_duration = config.get("duration", "00:30:00")
        video_description = config.get("description", "")
        subtitle_text = config.get("subtitle_preview", "")

        prompt = chapter_segmentation_prompt(
            node_id=node_id,
            video_title=video_title,
            video_duration=video_duration,
            video_description=video_description,
            subtitle_text=subtitle_text,
        )
        result = await self.invoke_with_validation(prompt, VideoAgentResult)
        result.node_id = node_id
        return result
