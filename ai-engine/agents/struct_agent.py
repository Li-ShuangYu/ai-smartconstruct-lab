"""
StructAgent — handles structure-based AI generation tasks.

Responsible for:
- Mindmap generation (MINDMAP_PREVIEW, max_nodes limit, 3-4 depth levels)
- Task breakdown (TASK_DISTRIBUTE, 5-7 milestones)
"""

from models.agent_outputs import StructAgentResult, GeneratedMindmap
from models.schemas import AgentTask
from agents.base_agent import BaseAgent
from prompts.struct_prompts import mindmap_prompt, task_breakdown_prompt


class StructAgent(BaseAgent):
    """Domain agent for structural content: mindmaps and task breakdowns."""

    async def execute(self, task: AgentTask) -> StructAgentResult:
        """
        Execute a structure generation task based on node_type.

        Routes to the appropriate generation method:
        - MINDMAP_PREVIEW → mindmap topology
        - TASK_DISTRIBUTE → task breakdown with milestones
        """
        node_type = task.node_type
        config = task.config

        if node_type == "MINDMAP_PREVIEW":
            return await self._generate_mindmap(task.node_id, config)
        elif node_type == "TASK_DISTRIBUTE":
            return await self._generate_task_breakdown(task.node_id, config)
        else:
            return StructAgentResult(node_id=task.node_id)

    async def _generate_mindmap(self, node_id: str, config: dict) -> StructAgentResult:
        """Generate a mindmap topology (max_nodes limit, 3-4 depth levels)."""
        topic = config.get("topic", "知识体系")
        max_nodes = config.get("max_nodes", 20)
        max_depth = config.get("max_depth", 4)
        knowledge_scope = config.get("knowledge_scope", "")

        prompt = mindmap_prompt(
            node_id=node_id,
            topic=topic,
            knowledge_scope=knowledge_scope,
            max_nodes=max_nodes,
            max_depth=max_depth,
        )
        result = await self.invoke_with_validation(prompt, StructAgentResult)
        result.node_id = node_id
        return result

    async def _generate_task_breakdown(self, node_id: str, config: dict) -> StructAgentResult:
        """Generate a task breakdown with 5-7 milestones."""
        task_name = config.get("task_name", "实训任务")
        task_description = config.get("task_description", "")
        total_hours = config.get("total_hours", 8)

        prompt = task_breakdown_prompt(
            node_id=node_id,
            task_name=task_name,
            task_description=task_description,
            total_hours=total_hours,
        )
        result = await self.invoke_with_validation(prompt, StructAgentResult)
        result.node_id = node_id
        return result
