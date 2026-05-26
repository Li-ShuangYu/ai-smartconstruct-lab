"""
ExamAgent — handles exam/assessment AI generation tasks.

Responsible for:
- Question generation (HOMEWORK with source_mode=ai, matching type counts from config)
- Flashcard generation (THEORY_CLASS with enable_ai_error_book, front/back format)
"""

from models.agent_outputs import ExamAgentResult, GeneratedQuestion, GeneratedFlashcard
from models.schemas import AgentTask
from agents.base_agent import BaseAgent
from prompts.exam_prompts import questions_prompt, flashcards_prompt


class ExamAgent(BaseAgent):
    """Domain agent for exam content: questions and flashcards."""

    async def execute(self, task: AgentTask) -> ExamAgentResult:
        """
        Execute an exam generation task based on node_type and config.

        Routes to the appropriate generation method:
        - HOMEWORK (source_mode=ai) → question generation
        - THEORY_CLASS (enable_ai_error_book) → flashcard generation
        """
        node_type = task.node_type
        config = task.config

        if node_type == "HOMEWORK" and config.get("source_mode") == "ai":
            return await self._generate_questions(task.node_id, config)
        elif node_type == "THEORY_CLASS" and config.get("enable_ai_error_book"):
            return await self._generate_flashcards(task.node_id, config)
        else:
            return ExamAgentResult(node_id=task.node_id)

    async def _generate_questions(self, node_id: str, config: dict) -> ExamAgentResult:
        """
        Generate exam questions matching type counts from config.

        Question types: 1-单选 2-多选 3-填空 4-判断 5-简答
        """
        question_type_counts = config.get("question_type_counts", {"1": 5})
        difficulty_level = config.get("difficulty_level", "medium")
        knowledge_scope = config.get("knowledge_scope", "通用知识")
        subject = config.get("subject", "")

        prompt = questions_prompt(
            node_id=node_id,
            subject=subject,
            knowledge_scope=knowledge_scope,
            difficulty_level=difficulty_level,
            question_type_counts=question_type_counts,
        )
        result = await self.invoke_with_validation(prompt, ExamAgentResult)
        result.node_id = node_id
        return result

    async def _generate_flashcards(self, node_id: str, config: dict) -> ExamAgentResult:
        """Generate flashcards with front/back content for spaced repetition."""
        topic = config.get("topic", "课程知识点")
        card_count = config.get("flashcard_count", 10)
        knowledge_points = config.get("knowledge_points", [])

        prompt = flashcards_prompt(
            node_id=node_id,
            topic=topic,
            card_count=card_count,
            knowledge_points=knowledge_points if knowledge_points else None,
        )
        result = await self.invoke_with_validation(prompt, ExamAgentResult)
        result.node_id = node_id
        return result
