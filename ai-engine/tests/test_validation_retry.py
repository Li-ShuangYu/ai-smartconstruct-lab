"""Tests for BaseAgent.invoke_with_validation retry mechanism."""

import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

import pytest
from unittest.mock import AsyncMock, MagicMock, patch
from pydantic import BaseModel, ValidationError

from agents.base_agent import BaseAgent
from models.schemas import AgentTask, AgentType


class SampleOutput(BaseModel):
    node_id: str
    value: int


class ConcreteAgentForRetry(BaseAgent):
    """Concrete agent for testing invoke_with_validation."""

    async def execute(self, task: AgentTask) -> BaseModel:
        return await self.invoke_with_validation("test prompt", SampleOutput)


class TestInvokeWithValidation:
    """Tests for the invoke_with_validation retry mechanism."""

    @pytest.mark.asyncio
    async def test_success_on_first_attempt(self):
        """Returns validated result when LLM output is valid on first try."""
        mock_llm_service = MagicMock()
        mock_structured = AsyncMock()
        mock_structured.ainvoke = AsyncMock(
            return_value=SampleOutput(node_id="n1", value=42)
        )
        mock_llm_service.with_structured_output = MagicMock(
            return_value=mock_structured
        )

        agent = ConcreteAgentForRetry(llm_service=mock_llm_service)
        result = await agent.invoke_with_validation("test prompt", SampleOutput)

        assert isinstance(result, SampleOutput)
        assert result.node_id == "n1"
        assert result.value == 42
        # Should only be called once (no retry)
        assert mock_structured.ainvoke.call_count == 1

    @pytest.mark.asyncio
    async def test_retry_on_validation_error_then_success(self):
        """Retries with schema hints when first attempt raises ValidationError."""
        mock_llm_service = MagicMock()
        mock_structured = AsyncMock()

        # First call raises ValidationError, second succeeds
        first_error = ValidationError.from_exception_data(
            title="SampleOutput",
            line_errors=[
                {
                    "type": "missing",
                    "loc": ("value",),
                    "msg": "Field required",
                    "input": {"node_id": "n1"},
                }
            ],
        )
        mock_structured.ainvoke = AsyncMock(
            side_effect=[first_error, SampleOutput(node_id="n1", value=99)]
        )
        mock_llm_service.with_structured_output = MagicMock(
            return_value=mock_structured
        )

        agent = ConcreteAgentForRetry(llm_service=mock_llm_service)
        result = await agent.invoke_with_validation("test prompt", SampleOutput)

        assert isinstance(result, SampleOutput)
        assert result.value == 99
        # Should be called twice (initial + retry)
        assert mock_structured.ainvoke.call_count == 2
        # Second call should contain schema hints
        retry_prompt = mock_structured.ainvoke.call_args_list[1][0][0]
        assert "IMPORTANT" in retry_prompt
        assert "schema" in retry_prompt.lower()

    @pytest.mark.asyncio
    async def test_raises_after_both_attempts_fail(self):
        """Raises ValidationError when both attempts fail validation."""
        mock_llm_service = MagicMock()
        mock_structured = AsyncMock()

        validation_error = ValidationError.from_exception_data(
            title="SampleOutput",
            line_errors=[
                {
                    "type": "missing",
                    "loc": ("value",),
                    "msg": "Field required",
                    "input": {"node_id": "n1"},
                }
            ],
        )
        # Both calls raise ValidationError
        mock_structured.ainvoke = AsyncMock(side_effect=validation_error)
        mock_llm_service.with_structured_output = MagicMock(
            return_value=mock_structured
        )

        agent = ConcreteAgentForRetry(llm_service=mock_llm_service)

        with pytest.raises(ValidationError):
            await agent.invoke_with_validation("test prompt", SampleOutput)

        # Should be called twice (initial + retry)
        assert mock_structured.ainvoke.call_count == 2

    @pytest.mark.asyncio
    async def test_non_validation_errors_propagate_immediately(self):
        """Non-ValidationError exceptions are raised without retry."""
        mock_llm_service = MagicMock()
        mock_structured = AsyncMock()
        mock_structured.ainvoke = AsyncMock(
            side_effect=RuntimeError("LLM connection failed")
        )
        mock_llm_service.with_structured_output = MagicMock(
            return_value=mock_structured
        )

        agent = ConcreteAgentForRetry(llm_service=mock_llm_service)

        with pytest.raises(RuntimeError, match="LLM connection failed"):
            await agent.invoke_with_validation("test prompt", SampleOutput)

        # Should only be called once (no retry for non-validation errors)
        assert mock_structured.ainvoke.call_count == 1

    @pytest.mark.asyncio
    async def test_dict_result_is_validated(self):
        """When LLM returns a dict instead of a model, it's validated via model_validate."""
        mock_llm_service = MagicMock()
        mock_structured = AsyncMock()
        # Return a dict that matches the schema
        mock_structured.ainvoke = AsyncMock(
            return_value={"node_id": "n1", "value": 7}
        )
        mock_llm_service.with_structured_output = MagicMock(
            return_value=mock_structured
        )

        agent = ConcreteAgentForRetry(llm_service=mock_llm_service)
        result = await agent.invoke_with_validation("test prompt", SampleOutput)

        assert isinstance(result, SampleOutput)
        assert result.node_id == "n1"
        assert result.value == 7
