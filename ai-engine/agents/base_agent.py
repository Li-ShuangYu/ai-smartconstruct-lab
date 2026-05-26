"""
Base Agent — abstract base class for all domain-specific AI agents.

All domain agents (TextAgent, StructAgent, ExamAgent, CodeAgent, VideoAgent)
inherit from BaseAgent and implement the `execute` method to produce
structured Pydantic output via LLM calls.

Includes a retry mechanism for Pydantic validation errors: if the LLM output
doesn't match the expected schema, the agent retries once with stricter
prompting (appending schema hints). If the second attempt also fails, the
ValidationError is raised.
"""

import logging
from abc import ABC, abstractmethod
from typing import Type, TypeVar

from pydantic import BaseModel, ValidationError

from models.schemas import AgentTask

logger = logging.getLogger(__name__)

T = TypeVar("T", bound=BaseModel)


class BaseAgent(ABC):
    """
    Abstract base class for domain-specific AI agents.

    Each agent receives an LLM service in its constructor and implements
    the `execute` method to process a single AgentTask, returning a
    structured Pydantic result model.

    Provides `invoke_with_validation` helper that wraps LLM calls with
    Pydantic validation retry logic.
    """

    MAX_VALIDATION_RETRIES = 1  # Retry once on ValidationError

    def __init__(self, llm_service) -> None:
        """
        Initialize the agent with an LLM service.

        Args:
            llm_service: An instance of LLMService providing access to
                         the language model and structured output capabilities.
        """
        self.llm_service = llm_service

    @abstractmethod
    async def execute(self, task: AgentTask) -> BaseModel:
        """
        Execute a single AI generation task and return a structured result.

        Each domain agent implements this method to:
        1. Extract relevant config from the task
        2. Build appropriate prompts
        3. Call the LLM via self.llm_service
        4. Return a validated Pydantic model (e.g., TextAgentResult, ExamAgentResult)

        Args:
            task: An AgentTask containing node_id, node_type, config, and context.

        Returns:
            A Pydantic BaseModel subclass specific to the agent's domain.

        Raises:
            ValidationError: If the LLM output fails Pydantic schema validation
                             after retry.
        """
        pass

    async def invoke_with_validation(
        self, prompt: str, output_schema: Type[T]
    ) -> T:
        """
        Invoke the LLM with structured output and retry on ValidationError.

        On the first attempt, calls the LLM with the given prompt and schema.
        If a ValidationError occurs, retries once with a stricter prompt that
        appends the schema definition as a hint. If the second attempt also
        fails, the ValidationError is raised.

        Args:
            prompt: The prompt string to send to the LLM.
            output_schema: The Pydantic model class to validate output against.

        Returns:
            A validated instance of output_schema.

        Raises:
            ValidationError: If both attempts fail schema validation.
        """
        structured_llm = self.llm_service.with_structured_output(output_schema)

        try:
            result = await structured_llm.ainvoke(prompt)
            # Validate by reconstructing the model from the result dict
            if isinstance(result, BaseModel):
                return result
            # If the LLM returns a dict, validate it
            return output_schema.model_validate(result)
        except (ValidationError, Exception) as first_error:
            if not isinstance(first_error, ValidationError):
                raise

            logger.warning(
                "Pydantic validation failed on first attempt for %s: %s. Retrying with schema hints.",
                output_schema.__name__,
                str(first_error),
            )

            # Build a stricter prompt with schema hints
            schema_json = output_schema.model_json_schema()
            strict_prompt = (
                f"{prompt}\n\n"
                f"[IMPORTANT] Your previous response did not match the required schema. "
                f"You MUST return valid JSON that strictly conforms to this schema:\n"
                f"{schema_json}\n\n"
                f"Validation error from previous attempt:\n{first_error}\n\n"
                f"Please fix the output to match the schema exactly."
            )

            try:
                result = await structured_llm.ainvoke(strict_prompt)
                if isinstance(result, BaseModel):
                    return result
                return output_schema.model_validate(result)
            except ValidationError as second_error:
                logger.error(
                    "Pydantic validation failed on retry for %s: %s",
                    output_schema.__name__,
                    str(second_error),
                )
                raise
