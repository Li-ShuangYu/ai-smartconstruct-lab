"""
LLM Service — initializes ChatOpenAI with DashScope-compatible endpoint
and provides structured output capabilities.

Reads configuration from environment variables (loaded via python-dotenv):
- LLM_BASE_URL: DashScope OpenAI-compatible API endpoint
- LLM_API_KEY: API key for authentication
- LLM_MODEL_NAME: Model name (default: "qwen-plus")
"""

import os
from typing import Type, TypeVar

from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from pydantic import BaseModel

# Load .env from the ai-engine directory
load_dotenv()

T = TypeVar("T", bound=BaseModel)


class LLMService:
    """
    Service wrapping ChatOpenAI configured for DashScope (Qwen) endpoint.

    Provides:
    - Access to the raw ChatOpenAI instance via `get_llm()`
    - A `with_structured_output(schema)` helper that returns a runnable
      producing validated Pydantic objects
    """

    def __init__(
        self,
        base_url: str | None = None,
        api_key: str | None = None,
        model_name: str | None = None,
        temperature: float = 0.7,
    ) -> None:
        """
        Initialize the LLM service.

        Args:
            base_url: DashScope API base URL. Falls back to LLM_BASE_URL env var.
            api_key: API key. Falls back to LLM_API_KEY env var.
            model_name: Model identifier. Falls back to LLM_MODEL_NAME env var,
                        then defaults to "qwen-plus".
            temperature: Sampling temperature for generation (default 0.7).
        """
        self._base_url = base_url or os.getenv(
            "LLM_BASE_URL", "https://dashscope.aliyuncs.com/compatible-mode/v1"
        )
        self._api_key = api_key or os.getenv("LLM_API_KEY", "")
        self._model_name = model_name or os.getenv("LLM_MODEL_NAME", "qwen-plus")
        self._temperature = temperature

        self._llm = ChatOpenAI(
            base_url=self._base_url,
            api_key=self._api_key,
            model=self._model_name,
            temperature=self._temperature,
        )

    def get_llm(self) -> ChatOpenAI:
        """
        Return the underlying ChatOpenAI instance.

        Useful when you need direct access to the LLM for custom chains
        or non-structured invocations.
        """
        return self._llm

    def with_structured_output(self, output_schema: Type[T]):
        """
        Return a runnable chain that produces validated Pydantic output.

        Uses LangChain's `with_structured_output` to constrain the LLM
        response to the given Pydantic schema, enabling reliable structured
        data extraction.

        Args:
            output_schema: A Pydantic BaseModel subclass defining the
                           expected output structure.

        Returns:
            A LangChain Runnable that accepts messages/prompts and returns
            an instance of `output_schema`.
        """
        return self._llm.with_structured_output(output_schema)
