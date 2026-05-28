"""
Integration tests for the AI orchestration pipeline.

Tests the complete flow:
1. Submit sample orchestration JSON
2. Verify the SupervisorAgent creates the correct execution plan
3. Verify the merged output has correct _ai_status values
4. Verify callback payload matches expected schema

Uses MockLLMService to avoid real LLM API calls.
"""

import sys

sys.path.insert(0, ".")

import asyncio
from unittest.mock import AsyncMock, patch

import pytest

from agents.supervisor import SupervisorAgent, detect_ai_nodes_phased
from models.agent_outputs import (
    TextAgentResult,
    StructAgentResult,
    ExamAgentResult,
    CodeAgentResult,
    VideoAgentResult,
)
from models.callback import AiCallbackPayload
from models.schemas import AgentType, ExecutionPlan
from services.merger import merge_results_into_payload
from services.asset_extractor import extract_db_assets
from tests.mocks.mock_llm import (
    MockLLMService,
    FIXED_TEXT_RESULT,
    FIXED_STRUCT_RESULT,
    FIXED_EXAM_RESULT,
    FIXED_CODE_RESULT,
    FIXED_VIDEO_RESULT,
)


# ---------------------------------------------------------------------------
# Sample orchestration JSON fixture (representative subset)
# ---------------------------------------------------------------------------

SAMPLE_CANVAS_JSON = {
    "orchestration_id": "integration-test-001",
    "phases": [
        {
            "phase_id": "phase-1",
            "phase_name": "课前阶段",
            "sort_num": 1,
            "nodes": [
                {
                    "node_id": "start-1",
                    "node_type": "START",
                    "name": "开始节点",
                    "config": {"enable_ai_welcome": True, "welcome_style": "formal"},
                    "ai_spec": {"target_agent": "TEXT", "priority": 5, "ai_flags": ["enable_ai_welcome"]},
                },
                {
                    "node_id": "resource-1",
                    "node_type": "RESOURCE_READ",
                    "name": "阅读材料",
                    "config": {"enable_ai_summary": True, "enable_ai_key_points": True},
                    "ai_spec": {"target_agent": "TEXT", "priority": 3, "ai_flags": ["enable_ai_summary", "enable_ai_key_points"]},
                },
            ],
            "edges": [
                {"source": "start-1", "target": "resource-1"},
            ],
        },
        {
            "phase_id": "phase-2",
            "phase_name": "课中阶段",
            "sort_num": 2,
            "nodes": [
                {
                    "node_id": "video-1",
                    "node_type": "VIDEO_LEARN",
                    "name": "视频学习",
                    "config": {"enable_ai_chapter": True, "video_url": "https://example.com/video.mp4"},
                    "ai_spec": {"target_agent": "VIDEO", "priority": 4, "ai_flags": ["enable_ai_chapter"]},
                },
                {
                    "node_id": "mindmap-1",
                    "node_type": "MINDMAP_PREVIEW",
                    "name": "思维导图",
                    "config": {"enable_ai_generate_map": True, "max_nodes": 20},
                    "ai_spec": {"target_agent": "STRUCT", "priority": 5, "ai_flags": ["enable_ai_generate_map"]},
                },
                {
                    "node_id": "homework-1",
                    "node_type": "HOMEWORK",
                    "name": "作业",
                    "config": {"source_mode": "ai", "question_type_counts": {"single_choice": 3}},
                    "ai_spec": {"target_agent": "EXAM", "priority": 2, "ai_flags": ["enable_ai_grading"]},
                },
                {
                    "node_id": "coding-1",
                    "node_type": "CODING_CLASS",
                    "name": "编程实践",
                    "config": {"enable_ai_code_review": True, "language": "python"},
                    "ai_spec": {"target_agent": "CODE", "priority": 4, "ai_flags": ["enable_ai_code_review"]},
                },
            ],
            "edges": [
                {"source": "video-1", "target": "mindmap-1"},
                {"source": "mindmap-1", "target": "homework-1"},
                {"source": "homework-1", "target": "coding-1"},
            ],
        },
        {
            "phase_id": "phase-3",
            "phase_name": "课后阶段",
            "sort_num": 3,
            "nodes": [
                {
                    "node_id": "survey-1",
                    "node_type": "SEMESTER_SURVEY",
                    "name": "问卷调查",
                    "config": {"survey_title": "课程反馈"},
                },
                {
                    "node_id": "end-1",
                    "node_type": "END",
                    "name": "结束",
                    "config": {},
                },
            ],
            "edges": [
                {"source": "survey-1", "target": "end-1"},
            ],
        },
    ],
}


# ---------------------------------------------------------------------------
# Task 8.2: Integration test — execution plan and merged output
# ---------------------------------------------------------------------------


class TestExecutionPlanIntegration:
    """Verify SupervisorAgent creates correct execution plan from sample JSON."""

    def test_supervisor_creates_correct_plan(self):
        """Submit sample JSON → verify execution plan has correct tasks."""
        supervisor = SupervisorAgent()
        plan = supervisor.analyze_orchestration(SAMPLE_CANVAS_JSON)

        assert isinstance(plan, ExecutionPlan)
        assert plan.orchestration_id == "integration-test-001"
        # 6 AI-enabled nodes: start-1, resource-1, video-1, mindmap-1, homework-1, coding-1
        assert plan.total_tasks == 6
        assert len(plan.parallel_jobs) == 6

        # Verify routing
        task_map = {t.node_id: t for t in plan.parallel_jobs}
        assert task_map["start-1"].agent_type == AgentType.TEXT
        assert task_map["resource-1"].agent_type == AgentType.TEXT
        assert task_map["video-1"].agent_type == AgentType.VIDEO
        assert task_map["mindmap-1"].agent_type == AgentType.STRUCT
        assert task_map["homework-1"].agent_type == AgentType.EXAM
        assert task_map["coding-1"].agent_type == AgentType.CODE

    def test_non_ai_nodes_excluded_from_plan(self):
        """Verify non-AI nodes (SEMESTER_SURVEY, END) are not in the plan."""
        supervisor = SupervisorAgent()
        plan = supervisor.analyze_orchestration(SAMPLE_CANVAS_JSON)

        node_ids_in_plan = {t.node_id for t in plan.parallel_jobs}
        assert "survey-1" not in node_ids_in_plan
        assert "end-1" not in node_ids_in_plan


class TestMergedOutputIntegration:
    """Verify merged output has correct _ai_status values."""

    def test_merged_output_preserves_structure(self):
        """Merged output preserves phases and nodes structure."""
        # Simulate successful agent results
        results = [
            TextAgentResult(node_id="start-1", welcome_message="Welcome!"),
            TextAgentResult(node_id="resource-1", summary="Summary text"),
            VideoAgentResult(node_id="video-1", chapters=[]),
            StructAgentResult(node_id="mindmap-1", mindmap=None),
            ExamAgentResult(node_id="homework-1", questions=[]),
            CodeAgentResult(node_id="coding-1", review_checkpoints=[]),
        ]

        payload = merge_results_into_payload(SAMPLE_CANVAS_JSON, results)

        # Structure preserved
        assert len(payload["phases"]) == len(SAMPLE_CANVAS_JSON["phases"])
        assert payload["orchestration_id"] == "integration-test-001"

    def test_ai_nodes_get_ready_status(self):
        """AI-processed nodes get _ai_status='ready'."""
        results = [
            TextAgentResult(node_id="start-1", welcome_message="Welcome!"),
            TextAgentResult(node_id="resource-1", summary="Summary text"),
            VideoAgentResult(node_id="video-1", chapters=[]),
            StructAgentResult(node_id="mindmap-1", mindmap=None),
            ExamAgentResult(node_id="homework-1", questions=[]),
            CodeAgentResult(node_id="coding-1", review_checkpoints=[]),
        ]

        payload = merge_results_into_payload(SAMPLE_CANVAS_JSON, results)

        # Collect all nodes from phases
        all_nodes = []
        for phase in payload["phases"]:
            all_nodes.extend(phase.get("nodes", []))
        node_map = {n["node_id"]: n for n in all_nodes}
        assert node_map["start-1"]["config"]["_ai_status"] == "ready"
        assert node_map["resource-1"]["config"]["_ai_status"] == "ready"
        assert node_map["video-1"]["config"]["_ai_status"] == "ready"
        assert node_map["mindmap-1"]["config"]["_ai_status"] == "ready"
        assert node_map["homework-1"]["config"]["_ai_status"] == "ready"
        assert node_map["coding-1"]["config"]["_ai_status"] == "ready"

    def test_non_ai_nodes_get_not_required_status(self):
        """Non-AI nodes get _ai_status='not_required'."""
        results = [
            TextAgentResult(node_id="start-1", welcome_message="Welcome!"),
            TextAgentResult(node_id="resource-1", summary="Summary text"),
            VideoAgentResult(node_id="video-1", chapters=[]),
            StructAgentResult(node_id="mindmap-1", mindmap=None),
            ExamAgentResult(node_id="homework-1", questions=[]),
            CodeAgentResult(node_id="coding-1", review_checkpoints=[]),
        ]

        payload = merge_results_into_payload(SAMPLE_CANVAS_JSON, results)

        # Collect all nodes from phases
        all_nodes = []
        for phase in payload["phases"]:
            all_nodes.extend(phase.get("nodes", []))
        node_map = {n["node_id"]: n for n in all_nodes}
        assert node_map["survey-1"]["config"]["_ai_status"] == "not_required"
        assert node_map["end-1"]["config"]["_ai_status"] == "not_required"

    def test_failed_ai_nodes_get_error_status(self):
        """AI nodes with failed results get _ai_status='error'."""
        # Only some results succeed; coding-1 fails
        results = [
            TextAgentResult(node_id="start-1", welcome_message="Welcome!"),
            TextAgentResult(node_id="resource-1", summary="Summary text"),
            VideoAgentResult(node_id="video-1", chapters=[]),
            StructAgentResult(node_id="mindmap-1", mindmap=None),
            ExamAgentResult(node_id="homework-1", questions=[]),
            RuntimeError("LLM timeout for coding-1"),  # Exception for coding-1
        ]

        payload = merge_results_into_payload(SAMPLE_CANVAS_JSON, results)

        # Collect all nodes from phases
        all_nodes = []
        for phase in payload["phases"]:
            all_nodes.extend(phase.get("nodes", []))
        node_map = {n["node_id"]: n for n in all_nodes}
        # coding-1 was AI-required but has no result → error
        assert node_map["coding-1"]["config"]["_ai_status"] == "error"

    def test_ai_generated_content_in_merged_output(self):
        """Verify _ai_generated contains the agent's output data."""
        results = [
            TextAgentResult(node_id="start-1", welcome_message="Hello!"),
            TextAgentResult(node_id="resource-1", summary="Summary"),
            VideoAgentResult(node_id="video-1", chapters=[]),
            StructAgentResult(node_id="mindmap-1", mindmap=None),
            ExamAgentResult(node_id="homework-1", questions=[]),
            CodeAgentResult(node_id="coding-1", review_checkpoints=[]),
        ]

        payload = merge_results_into_payload(SAMPLE_CANVAS_JSON, results)

        # Collect all nodes from phases
        all_nodes = []
        for phase in payload["phases"]:
            all_nodes.extend(phase.get("nodes", []))
        node_map = {n["node_id"]: n for n in all_nodes}
        ai_gen = node_map["start-1"]["config"]["_ai_generated"]
        assert ai_gen["node_id"] == "start-1"
        assert ai_gen["welcome_message"] == "Hello!"


# ---------------------------------------------------------------------------
# Task 8.3: Integration test — callback payload schema validation
# ---------------------------------------------------------------------------


class TestCallbackPayloadSchema:
    """Verify callback payload matches AiCallbackPayload schema."""

    def test_success_callback_payload_valid(self):
        """Success callback payload conforms to AiCallbackPayload schema."""
        results = [
            TextAgentResult(node_id="start-1", welcome_message="Welcome!"),
            ExamAgentResult(
                node_id="homework-1",
                questions=[
                    {
                        "question_type": 1,
                        "content": '{"stem": "Q1"}',
                        "standard_answer": "A",
                        "default_score": 5.0,
                    }
                ],
            ),
        ]

        standard_payload = merge_results_into_payload(SAMPLE_CANVAS_JSON, results)
        generated_assets = extract_db_assets(results)

        # Build callback payload as the workflow would
        payload = AiCallbackPayload(
            template_id=100,
            job_id="test-job-uuid-001",
            status=2,
            standard_payload_json=standard_payload,
            generated_assets=generated_assets,
        )

        # Validate schema
        assert payload.template_id == 100
        assert payload.job_id == "test-job-uuid-001"
        assert payload.status == 2
        assert payload.standard_payload_json is not None
        assert payload.error_reason is None
        assert isinstance(payload.generated_assets, dict)
        assert "questions" in payload.generated_assets
        assert "knowledge_points" in payload.generated_assets
        assert "eval_indicators" in payload.generated_assets

    def test_failure_callback_payload_valid(self):
        """Failure callback payload conforms to AiCallbackPayload schema."""
        payload = AiCallbackPayload(
            template_id=100,
            job_id="test-job-uuid-002",
            status=3,
            error_reason="AI处理超时（5分钟），请稍后重试",
        )

        assert payload.template_id == 100
        assert payload.status == 3
        assert payload.error_reason is not None
        assert payload.standard_payload_json is None
        assert payload.generated_assets is None

    def test_generated_assets_structure(self):
        """Verify generated_assets contains expected keys and structure."""
        results = [
            ExamAgentResult(
                node_id="hw-1",
                questions=[
                    {
                        "question_type": 1,
                        "content": '{"stem": "Test Q"}',
                        "standard_answer": "B",
                        "default_score": 3.0,
                        "knowledge_point": "Testing",
                    }
                ],
            ),
            StructAgentResult(
                node_id="struct-1",
                knowledge_points=["知识点A", "知识点B"],
            ),
            CodeAgentResult(
                node_id="code-1",
                eval_indicators=[
                    {"name": "代码质量", "max_score": 30, "criteria": "清晰可读"}
                ],
            ),
        ]

        assets = extract_db_assets(results)

        # Questions
        assert len(assets["questions"]) == 1
        assert assets["questions"][0]["question_type"] == 1
        assert assets["questions"][0]["content"] == '{"stem": "Test Q"}'
        assert assets["questions"][0]["standard_answer"] == "B"
        assert assets["questions"][0]["default_score"] == 3.0

        # Knowledge points
        assert len(assets["knowledge_points"]) == 2
        assert assets["knowledge_points"][0]["name"] == "知识点A"
        assert assets["knowledge_points"][0]["source_node_id"] == "struct-1"

        # Eval indicators
        assert len(assets["eval_indicators"]) == 1
        assert assets["eval_indicators"][0]["name"] == "代码质量"
        assert assets["eval_indicators"][0]["source_node_id"] == "code-1"

    def test_callback_payload_serializable(self):
        """Callback payload can be serialized to dict (for HTTP POST)."""
        payload = AiCallbackPayload(
            template_id=42,
            job_id="serialize-test",
            status=2,
            standard_payload_json={"nodes": [], "edges": []},
            generated_assets={"questions": [], "knowledge_points": [], "eval_indicators": []},
        )

        payload_dict = payload.model_dump()
        assert isinstance(payload_dict, dict)
        assert payload_dict["template_id"] == 42
        assert payload_dict["job_id"] == "serialize-test"
        assert payload_dict["status"] == 2
