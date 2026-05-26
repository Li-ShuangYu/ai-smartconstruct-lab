"""Domain-specific output models for AI agents.

Each agent produces structured results conforming to these Pydantic models,
ensuring type-safe outputs that map directly to MySQL business tables.
"""

from pydantic import BaseModel, Field
from typing import Optional, List


# --- Shared sub-models used by domain agents ---


class GeneratedQuestion(BaseModel):
    """A single AI-generated exam question."""

    question_type: int = Field(
        description="1-单选 2-多选 3-填空 4-判断 5-简答", ge=1, le=5
    )
    content: str = Field(description="题干与选项JSON字符串")
    standard_answer: str
    default_score: float = Field(gt=0)
    knowledge_point: Optional[str] = None


class GeneratedMindmap(BaseModel):
    """A hierarchical mindmap structure."""

    root: str
    children: List[dict]


class GeneratedChapter(BaseModel):
    """A video chapter segment with time boundaries."""

    title: str
    start_time: str
    end_time: str
    summary: str


class GeneratedFlashcard(BaseModel):
    """A flashcard with front/back content for spaced repetition."""

    front_content: str
    back_content: str
    order: int


# --- Agent result models ---


class TextAgentResult(BaseModel):
    """Output from TEXT_AGENT: welcome messages, summaries, key points, navigation."""

    node_id: str
    welcome_message: Optional[str] = None
    summary: Optional[str] = None
    key_points: Optional[List[dict]] = None
    navigation_tree: Optional[List[dict]] = None


class StructAgentResult(BaseModel):
    """Output from STRUCT_AGENT: mindmaps, task breakdowns, knowledge points."""

    node_id: str
    mindmap: Optional[GeneratedMindmap] = None
    task_breakdown: Optional[List[dict]] = None
    knowledge_points: Optional[List[str]] = None


class ExamAgentResult(BaseModel):
    """Output from EXAM_AGENT: questions and flashcards."""

    node_id: str
    questions: Optional[List[GeneratedQuestion]] = None
    flashcards: Optional[List[GeneratedFlashcard]] = None


class CodeAgentResult(BaseModel):
    """Output from CODE_AGENT: review checkpoints and evaluation indicators."""

    node_id: str
    review_checkpoints: Optional[List[dict]] = None
    eval_indicators: Optional[List[dict]] = None


class VideoAgentResult(BaseModel):
    """Output from VIDEO_AGENT: chapter segmentation."""

    node_id: str
    chapters: Optional[List[GeneratedChapter]] = None
