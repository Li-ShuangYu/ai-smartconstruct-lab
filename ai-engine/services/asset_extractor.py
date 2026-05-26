"""
Asset Extractor Service — extracts DB-persistable assets from agent results.

Extracts:
- questions from ExamAgentResult
- knowledge_points from StructAgentResult
- eval_indicators from CodeAgentResult

These assets are sent to the Java backend via callback for persistence
into biz_question, biz_knowledge_point, and related tables.
"""

import logging
from typing import Any, List

from pydantic import BaseModel

from models.agent_outputs import (
    ExamAgentResult,
    StructAgentResult,
    CodeAgentResult,
)

logger = logging.getLogger(__name__)


def extract_db_assets(agent_results: List[Any]) -> dict:
    """
    Extract database-persistable assets from agent results.

    Scans all successful agent results and extracts:
    - questions: from ExamAgentResult.questions
    - knowledge_points: from StructAgentResult.knowledge_points
    - eval_indicators: from CodeAgentResult.eval_indicators

    Args:
        agent_results: List of agent results (BaseModel instances or Exceptions).

    Returns:
        Dict with keys: questions, knowledge_points, eval_indicators.
        Each value is a list of dicts ready for DB persistence.
    """
    questions: list[dict] = []
    knowledge_points: list[dict] = []
    eval_indicators: list[dict] = []

    for result in agent_results:
        if isinstance(result, Exception):
            continue

        if not isinstance(result, BaseModel):
            continue

        # Extract questions from ExamAgentResult
        if isinstance(result, ExamAgentResult):
            if result.questions:
                for q in result.questions:
                    questions.append(q.model_dump())
                logger.info(
                    "Extracted %d questions from node %s",
                    len(result.questions),
                    result.node_id,
                )

        # Extract knowledge_points from StructAgentResult
        elif isinstance(result, StructAgentResult):
            if result.knowledge_points:
                for kp in result.knowledge_points:
                    knowledge_points.append({
                        "name": kp,
                        "source_node_id": result.node_id,
                    })
                logger.info(
                    "Extracted %d knowledge_points from node %s",
                    len(result.knowledge_points),
                    result.node_id,
                )

        # Extract eval_indicators from CodeAgentResult
        elif isinstance(result, CodeAgentResult):
            if result.eval_indicators:
                for indicator in result.eval_indicators:
                    indicator_entry = dict(indicator)
                    indicator_entry["source_node_id"] = result.node_id
                    eval_indicators.append(indicator_entry)
                logger.info(
                    "Extracted %d eval_indicators from node %s",
                    len(result.eval_indicators),
                    result.node_id,
                )

    return {
        "questions": questions,
        "knowledge_points": knowledge_points,
        "eval_indicators": eval_indicators,
    }
