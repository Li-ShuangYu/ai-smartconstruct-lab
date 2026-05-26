"""Tests for BaseAgent abstract class and LLMService."""

import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

import pytest
from pydantic import BaseModel

from agents.base_agent import BaseAgent
from services.llm_service import LLMService
from models.schemas import AgentTask, AgentType


class DummyResult(BaseModel):
    message: str


class ConcreteAgent(BaseAgent):
    """A concrete implementation for testing."""

    async def execute(self, task: AgentTask) -> BaseModel:
        return DummyResult(message=f"Processed {task.node_id}")


class TestBaseAgent:
    """Tests for the BaseAgent abstract class."""

    def test_cannot_instantiate_abstract_class(self):
        """BaseAgent cannot be instantiated directly."""
        with pytest.raises(TypeError):
            BaseAgent(llm_service=None)

    def test_concrete_subclass_can_be_instantiated(self):
        """A concrete subclass implementing execute() can be instantiated."""
        svc = LLMService(api_key="test-key")
        agent = ConcreteAgent(llm_service=svc)
        assert agent.llm_service is svc

    @pytest.mark.asyncio
    async def test_concrete_agent_execute(self):
        """A concrete agent can execute a task and return a result."""
        svc = LLMService(api_key="test-key")
        agent = ConcreteAgent(llm_service=svc)
        task = AgentTask(
            task_id="test_1",
            agent_type=AgentType.TEXT,
            node_id="node_001",
            node_type="START",
            config={"enable_ai_welcome": True},
        )
        result = await agent.execute(task)
        assert isinstance(result, DummyResult)
        assert result.message == "Processed node_001"


class TestLLMService:
    """Tests for the LLMService."""

    def test_initialization_with_explicit_params(self):
        """LLMService can be initialized with explicit parameters."""
        svc = LLMService(
            base_url="https://example.com/v1",
            api_key="sk-test",
            model_name="qwen-turbo",
            temperature=0.5,
        )
        assert svc._base_url == "https://example.com/v1"
        assert svc._api_key == "sk-test"
        assert svc._model_name == "qwen-turbo"
        assert svc._temperature == 0.5

    def test_initialization_defaults(self):
        """LLMService uses env vars / defaults when params not provided."""
        svc = LLMService(api_key="sk-test")
        # Should fall back to env or default
        assert svc._model_name == os.getenv("LLM_MODEL_NAME", "qwen-plus")
        assert "dashscope" in svc._base_url or svc._base_url == os.getenv(
            "LLM_BASE_URL", "https://dashscope.aliyuncs.com/compatible-mode/v1"
        )

    def test_get_llm_returns_chat_openai(self):
        """get_llm() returns a ChatOpenAI instance."""
        from langchain_openai import ChatOpenAI

        svc = LLMService(api_key="sk-test")
        llm = svc.get_llm()
        assert isinstance(llm, ChatOpenAI)

    def test_with_structured_output_returns_runnable(self):
        """with_structured_output() returns a runnable chain."""
        svc = LLMService(api_key="sk-test")
        chain = svc.with_structured_output(DummyResult)
        # The chain should be a runnable (has invoke/ainvoke methods)
        assert hasattr(chain, "invoke") or hasattr(chain, "ainvoke")
