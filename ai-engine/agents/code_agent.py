"""
CodeAgent — handles code-related AI generation tasks.

Responsible for:
- Review checkpoints (CODING_CLASS with enable_code_review)
- Evaluation indicators/rubrics (PLAN_UPLOAD with enable_ai_pre_evaluation)
"""

from models.agent_outputs import CodeAgentResult
from models.schemas import AgentTask
from agents.base_agent import BaseAgent
from prompts.code_prompts import review_checkpoints_prompt, eval_indicators_prompt


class CodeAgent(BaseAgent):
    """Domain agent for code-related content: review checkpoints and evaluation rubrics."""

    async def execute(self, task: AgentTask) -> CodeAgentResult:
        """
        Execute a code-related generation task based on node_type.

        Routes to the appropriate generation method:
        - CODING_CLASS → code review checkpoints
        - PLAN_UPLOAD → evaluation indicators/rubrics
        """
        node_type = task.node_type
        config = task.config

        if node_type == "CODING_CLASS":
            return await self._generate_review_checkpoints(task.node_id, config)
        elif node_type == "PLAN_UPLOAD":
            return await self._generate_eval_indicators(task.node_id, config)
        else:
            return CodeAgentResult(node_id=task.node_id)

    async def _generate_review_checkpoints(self, node_id: str, config: dict) -> CodeAgentResult:
        """Generate code review checkpoints for a CODING_CLASS node."""
        language = config.get("programming_language", "Python")
        task_description = config.get("task_description", "编程练习")
        difficulty = config.get("difficulty_level", "medium")
        focus_areas = config.get("focus_areas", [])

        prompt = review_checkpoints_prompt(
            node_id=node_id,
            language=language,
            task_description=task_description,
            difficulty=difficulty,
            focus_areas=focus_areas if focus_areas else None,
        )
        result = await self.invoke_with_validation(prompt, CodeAgentResult)
        result.node_id = node_id
        return result

    async def _generate_eval_indicators(self, node_id: str, config: dict) -> CodeAgentResult:
        """Generate evaluation indicators/rubrics for a PLAN_UPLOAD node."""
        plan_type = config.get("plan_type", "项目方案")
        evaluation_dimensions = config.get("evaluation_dimensions", [])
        max_score = config.get("max_score", 100)

        prompt = eval_indicators_prompt(
            node_id=node_id,
            plan_type=plan_type,
            max_score=max_score,
            evaluation_dimensions=evaluation_dimensions if evaluation_dimensions else None,
        )
        result = await self.invoke_with_validation(prompt, CodeAgentResult)
        result.node_id = node_id
        return result
