"""Tests for workflow orchestration services (Tasks 7.1-7.9).

Tests the merger, asset_extractor, job_store, and callback modules
without requiring LLM connectivity.
"""

import pytest

from services.job_store import JobStore
from services.merger import merge_results_into_payload
from services.asset_extractor import extract_db_assets
from models.agent_outputs import (
    TextAgentResult,
    StructAgentResult,
    ExamAgentResult,
    CodeAgentResult,
    VideoAgentResult,
    GeneratedQuestion,
    GeneratedFlashcard,
    GeneratedChapter,
    GeneratedMindmap,
)


# --- JobStore Tests ---


class TestJobStore:
    """Test in-memory job status tracking."""

    def setup_method(self):
        self.store = JobStore()

    def test_create_job(self):
        job = self.store.create_job("job-1", 100, "orch-1")
        assert job["job_id"] == "job-1"
        assert job["status"] == "accepted"
        assert job["template_id"] == 100
        assert job["orchestration_id"] == "orch-1"

    def test_update_status(self):
        self.store.create_job("job-1", 100, "orch-1")
        self.store.update_status("job-1", "processing", total_tasks=5)
        status = self.store.get_status("job-1")
        assert status["status"] == "processing"
        assert status["total_tasks"] == 5

    def test_update_status_with_error(self):
        self.store.create_job("job-1", 100, "orch-1")
        self.store.update_status("job-1", "failed", error="Something went wrong")
        status = self.store.get_status("job-1")
        assert status["status"] == "failed"
        assert status["error"] == "Something went wrong"

    def test_get_status_nonexistent(self):
        assert self.store.get_status("nonexistent") is None

    def test_exists(self):
        self.store.create_job("job-1", 100, "orch-1")
        assert self.store.exists("job-1") is True
        assert self.store.exists("job-2") is False

    def test_count_active(self):
        self.store.create_job("job-1", 100, "orch-1")
        self.store.create_job("job-2", 101, "orch-2")
        self.store.create_job("job-3", 102, "orch-3")
        self.store.update_status("job-3", "completed")
        assert self.store.count_active() == 2  # job-1 and job-2 are "accepted"

    def test_update_unknown_job_no_error(self):
        # Should not raise, just log a warning
        self.store.update_status("unknown-job", "processing")


# --- Merger Tests ---


class TestMerger:
    """Test merge_results_into_payload."""

    def _make_canvas(self, nodes):
        return {
            "orchestration_id": "orch-1",
            "nodes": nodes,
            "edges": [{"source": "n1", "target": "n2"}],
        }

    def test_successful_result_adds_ai_generated(self):
        canvas = self._make_canvas([
            {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": True}},
        ])
        results = [
            TextAgentResult(node_id="n1", welcome_message="Hello!"),
        ]
        payload = merge_results_into_payload(canvas, results)

        node = payload["nodes"][0]
        assert node["config"]["_ai_status"] == "ready"
        assert node["config"]["_ai_generated"]["welcome_message"] == "Hello!"

    def test_node_without_ai_flags_gets_not_required(self):
        canvas = self._make_canvas([
            {"node_id": "n1", "node_type": "END", "config": {}},
        ])
        results = []
        payload = merge_results_into_payload(canvas, results)

        node = payload["nodes"][0]
        assert node["config"]["_ai_status"] == "not_required"

    def test_failed_task_gets_error_status(self):
        canvas = self._make_canvas([
            {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": True}},
        ])
        # Simulate a failed task (exception in results)
        results = [RuntimeError("LLM timeout")]
        payload = merge_results_into_payload(canvas, results)

        node = payload["nodes"][0]
        assert node["config"]["_ai_status"] == "error"
        assert "_ai_error" in node["config"]

    def test_preserves_original_structure(self):
        canvas = self._make_canvas([
            {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": True}},
            {"node_id": "n2", "node_type": "END", "config": {}},
        ])
        results = [TextAgentResult(node_id="n1", welcome_message="Hi")]
        payload = merge_results_into_payload(canvas, results)

        # Structure preserved
        assert len(payload["nodes"]) == 2
        assert len(payload["edges"]) == 1
        assert payload["orchestration_id"] == "orch-1"

    def test_does_not_mutate_original(self):
        canvas = self._make_canvas([
            {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": True}},
        ])
        results = [TextAgentResult(node_id="n1", welcome_message="Hi")]
        merge_results_into_payload(canvas, results)

        # Original should not be modified
        assert "_ai_status" not in canvas["nodes"][0]["config"]

    def test_mixed_success_and_failure(self):
        canvas = self._make_canvas([
            {"node_id": "n1", "node_type": "START", "config": {"enable_ai_welcome": True}},
            {"node_id": "n2", "node_type": "HOMEWORK", "config": {"source_mode": "ai"}},
            {"node_id": "n3", "node_type": "END", "config": {}},
        ])
        results = [
            TextAgentResult(node_id="n1", welcome_message="Welcome"),
            RuntimeError("Failed to generate questions"),
        ]
        payload = merge_results_into_payload(canvas, results)

        assert payload["nodes"][0]["config"]["_ai_status"] == "ready"
        assert payload["nodes"][1]["config"]["_ai_status"] == "error"
        assert payload["nodes"][2]["config"]["_ai_status"] == "not_required"


# --- Asset Extractor Tests ---


class TestAssetExtractor:
    """Test extract_db_assets."""

    def test_extracts_questions_from_exam_result(self):
        results = [
            ExamAgentResult(
                node_id="n1",
                questions=[
                    GeneratedQuestion(
                        question_type=1,
                        content='{"stem": "What is 1+1?", "options": ["1","2","3","4"]}',
                        standard_answer="B",
                        default_score=5.0,
                        knowledge_point="arithmetic",
                    ),
                    GeneratedQuestion(
                        question_type=4,
                        content='{"stem": "Python is compiled"}',
                        standard_answer="False",
                        default_score=2.0,
                    ),
                ],
            )
        ]
        assets = extract_db_assets(results)
        assert len(assets["questions"]) == 2
        assert assets["questions"][0]["question_type"] == 1
        assert assets["questions"][1]["question_type"] == 4

    def test_extracts_knowledge_points_from_struct_result(self):
        results = [
            StructAgentResult(
                node_id="n2",
                knowledge_points=["OOP", "Inheritance", "Polymorphism"],
            )
        ]
        assets = extract_db_assets(results)
        assert len(assets["knowledge_points"]) == 3
        assert assets["knowledge_points"][0]["name"] == "OOP"
        assert assets["knowledge_points"][0]["source_node_id"] == "n2"

    def test_extracts_eval_indicators_from_code_result(self):
        results = [
            CodeAgentResult(
                node_id="n3",
                eval_indicators=[
                    {"name": "Code Style", "weight": 0.3},
                    {"name": "Correctness", "weight": 0.7},
                ],
            )
        ]
        assets = extract_db_assets(results)
        assert len(assets["eval_indicators"]) == 2
        assert assets["eval_indicators"][0]["name"] == "Code Style"
        assert assets["eval_indicators"][0]["source_node_id"] == "n3"

    def test_skips_exceptions(self):
        results = [
            RuntimeError("Failed"),
            ExamAgentResult(node_id="n1", questions=[
                GeneratedQuestion(
                    question_type=1,
                    content="Q1",
                    standard_answer="A",
                    default_score=5.0,
                ),
            ]),
        ]
        assets = extract_db_assets(results)
        assert len(assets["questions"]) == 1

    def test_empty_results(self):
        assets = extract_db_assets([])
        assert assets == {
            "questions": [],
            "knowledge_points": [],
            "eval_indicators": [],
        }

    def test_results_without_extractable_assets(self):
        results = [
            TextAgentResult(node_id="n1", welcome_message="Hello"),
            VideoAgentResult(
                node_id="n2",
                chapters=[
                    GeneratedChapter(
                        title="Intro", start_time="00:00", end_time="05:00", summary="Introduction"
                    )
                ],
            ),
        ]
        assets = extract_db_assets(results)
        assert assets == {
            "questions": [],
            "knowledge_points": [],
            "eval_indicators": [],
        }

    def test_multiple_exam_results_combined(self):
        results = [
            ExamAgentResult(
                node_id="n1",
                questions=[
                    GeneratedQuestion(question_type=1, content="Q1", standard_answer="A", default_score=5.0),
                ],
            ),
            ExamAgentResult(
                node_id="n2",
                questions=[
                    GeneratedQuestion(question_type=2, content="Q2", standard_answer="B,C", default_score=10.0),
                ],
            ),
        ]
        assets = extract_db_assets(results)
        assert len(assets["questions"]) == 2
