"""
Job Store — in-memory job status tracking for the polling endpoint.

Provides a shared, thread-safe store for tracking orchestration job status.
Used by both the orchestration router (to initialize/query jobs) and the
workflow service (to update status as processing progresses).

Supports per-node status tracking and aggregation to overall job status.

Can be replaced with Redis for production multi-instance deployments.
"""

import logging
from typing import Optional

logger = logging.getLogger(__name__)


class JobStore:
    """In-memory job status store.

    Stores job metadata including status, progress, per-node results,
    and error information. Thread-safe for use with asyncio (single-threaded event loop).
    """

    def __init__(self) -> None:
        self._jobs: dict[str, dict] = {}

    def create_job(
        self,
        job_id: str,
        template_id: int,
        orchestration_id: str,
    ) -> dict:
        """Create a new job entry with 'accepted' status.

        Args:
            job_id: Unique job identifier.
            template_id: The template being processed.
            orchestration_id: The orchestration identifier.

        Returns:
            The created job status dict.
        """
        job = {
            "job_id": job_id,
            "status": "accepted",
            "template_id": template_id,
            "orchestration_id": orchestration_id,
            "total_tasks": 0,
            "completed_tasks": 0,
            "failed_tasks": 0,
            "node_statuses": {},  # node_id -> {status, error_reason, result_json}
            "error": None,
        }
        self._jobs[job_id] = job
        logger.info("Job %s created (template_id=%d)", job_id, template_id)
        return job

    def update_status(
        self,
        job_id: str,
        status: str,
        total_tasks: Optional[int] = None,
        error: Optional[str] = None,
    ) -> None:
        """Update the status of an existing job.

        Args:
            job_id: The job to update.
            status: New status (accepted, processing, completed, failed).
            total_tasks: Optional total task count to set.
            error: Optional error message (for failed status).
        """
        if job_id not in self._jobs:
            logger.warning("Attempted to update unknown job %s", job_id)
            return

        self._jobs[job_id]["status"] = status
        if total_tasks is not None:
            self._jobs[job_id]["total_tasks"] = total_tasks
        if error is not None:
            self._jobs[job_id]["error"] = error

        logger.info("Job %s status updated to '%s'", job_id, status)

    def update_node_status(
        self,
        job_id: str,
        node_id: str,
        status: str,
        error_reason: Optional[str] = None,
        result_json: Optional[dict] = None,
    ) -> None:
        """Update the status of a specific node within a job.

        After updating the node, aggregates completed/failed counts and
        determines if the overall job is complete.

        Args:
            job_id: The job containing the node.
            node_id: The node to update.
            status: Node status ('success' or 'failed').
            error_reason: Error reason if status is 'failed'.
            result_json: Result data if status is 'success'.
        """
        if job_id not in self._jobs:
            logger.warning("Attempted to update node in unknown job %s", job_id)
            return

        job = self._jobs[job_id]
        job["node_statuses"][node_id] = {
            "status": status,
            "error_reason": error_reason,
            "result_json": result_json,
        }

        # Aggregate counts
        node_statuses = job["node_statuses"]
        job["completed_tasks"] = sum(
            1 for ns in node_statuses.values() if ns["status"] == "success"
        )
        job["failed_tasks"] = sum(
            1 for ns in node_statuses.values() if ns["status"] == "failed"
        )

        total = job["total_tasks"]
        processed = job["completed_tasks"] + job["failed_tasks"]

        # Determine overall job status when all nodes are processed
        if total > 0 and processed >= total:
            if job["failed_tasks"] == total:
                job["status"] = "failed"
                job["error"] = "All tasks failed"
                logger.info("Job %s: all %d tasks failed", job_id, total)
            elif job["failed_tasks"] > 0:
                job["status"] = "completed_with_errors"
                logger.info(
                    "Job %s: completed with %d/%d failures",
                    job_id,
                    job["failed_tasks"],
                    total,
                )
            else:
                job["status"] = "completed"
                logger.info("Job %s: all %d tasks completed successfully", job_id, total)

    def get_status(self, job_id: str) -> Optional[dict]:
        """Get the current status of a job.

        Supports lookup by either job_id (UUID) or orchestration_id (e.g. tmpl_123).
        This allows the backend's timeout detection task to query by orchestration_id
        when it doesn't know the actual job_id.

        Args:
            job_id: The job_id or orchestration_id to query.

        Returns:
            Job status dict, or None if not found.
        """
        # Direct lookup by job_id
        if job_id in self._jobs:
            return self._jobs[job_id]

        # Fallback: search by orchestration_id
        for job in self._jobs.values():
            if job.get("orchestration_id") == job_id:
                return job

        return None

    def exists(self, job_id: str) -> bool:
        """Check if a job exists."""
        return job_id in self._jobs

    def count_active(self) -> int:
        """Count jobs that are currently in-progress (accepted or processing)."""
        return sum(
            1
            for job in self._jobs.values()
            if job.get("status") in ("accepted", "processing")
        )

    def get_failed_nodes(self, job_id: str) -> list[str]:
        """Get the list of failed node IDs for a job.

        Args:
            job_id: The job to query.

        Returns:
            List of node_ids that have 'failed' status.
        """
        if job_id not in self._jobs:
            return []

        return [
            node_id
            for node_id, ns in self._jobs[job_id]["node_statuses"].items()
            if ns["status"] == "failed"
        ]


# Singleton instance shared across the application
job_store = JobStore()
