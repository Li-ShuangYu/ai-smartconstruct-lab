"""
TextAgent — handles text-based AI generation tasks.

Responsible for:
- Welcome messages (START nodes, 150-250 chars)
- Summaries (RESOURCE_READ, ≤300 chars), key points (5-7 items), navigation trees
- System prompts (AI_PRACTICE nodes)
"""

from models.agent_outputs import TextAgentResult
from models.schemas import AgentTask
from agents.base_agent import BaseAgent
from prompts.text_prompts import (
    welcome_prompt,
    resource_content_prompt,
    system_prompt_prompt,
)


class TextAgent(BaseAgent):
    """Domain agent for text generation: welcome messages, summaries, key points, navigation."""

    async def execute(self, task: AgentTask) -> TextAgentResult:
        """
        Execute a text generation task based on node_type.

        Routes to the appropriate generation method:
        - START → welcome message
        - RESOURCE_READ → summary, key points, navigation tree
        - AI_PRACTICE → system prompt (stored as welcome_message)
        """
        node_type = task.node_type
        config = task.config

        if node_type == "START":
            return await self._generate_welcome(task.node_id, config)
        elif node_type == "RESOURCE_READ":
            return await self._generate_resource_content(task.node_id, config)
        elif node_type == "AI_PRACTICE":
            return await self._generate_system_prompt(task.node_id, config)
        else:
            return TextAgentResult(node_id=task.node_id)

    async def _generate_welcome(self, node_id: str, config: dict) -> TextAgentResult:
        """Generate a welcome message (150-250 chars) for a START node."""
        training_name = config.get("training_name", "实训课程")
        description = config.get("description", "")

        prompt = welcome_prompt(node_id, training_name, description)
        result = await self.invoke_with_validation(prompt, TextAgentResult)
        result.node_id = node_id
        return result

    async def _generate_resource_content(self, node_id: str, config: dict) -> TextAgentResult:
        """Generate summary, key points, and navigation tree for a RESOURCE_READ node."""
        resource_name = config.get("resource_name", "学习资源")
        resource_content = config.get("content_preview", "")
        enable_summary = config.get("enable_ai_summary", False)
        enable_key_points = config.get("enable_ai_key_points", False)
        enable_nav = config.get("enable_ai_quick_nav", False)

        prompt = resource_content_prompt(
            node_id=node_id,
            resource_name=resource_name,
            resource_content=resource_content,
            enable_summary=enable_summary,
            enable_key_points=enable_key_points,
            enable_nav=enable_nav,
        )
        result = await self.invoke_with_validation(prompt, TextAgentResult)
        result.node_id = node_id
        return result

    async def _generate_system_prompt(self, node_id: str, config: dict) -> TextAgentResult:
        """Generate a system prompt for an AI_PRACTICE node."""
        practice_topic = config.get("topic", "AI对话练习")
        practice_goal = config.get("goal", "帮助学生理解知识点")
        persona = config.get("persona", "专业导师")

        prompt = system_prompt_prompt(node_id, practice_topic, practice_goal, persona)
        result = await self.invoke_with_validation(prompt, TextAgentResult)
        result.node_id = node_id
        return result
