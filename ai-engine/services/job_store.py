"""
Job Store — in-memory job status tracking for the polling endpoint.

Provides a shared, thread-safe store for tracking orchestration job status.
Used by both the orchestration router (to initialize/query jobs) and the
workflow service (to update status as processing progresses).

Can be replaced with Redis for production multi-instance deployments.
"""

import logging
from typing import Optional

logger = logging.getLogger(__name__)


class JobStore:
    """In-memory job status store.

    Stores job metadata including status, progress, and error information.
    Thread-safe for use with asyncio (single-threaded event loop).
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

    def get_status(self, job_id: str) -> Optional[dict]:
        """Get the current status of a job.

        Args:
            job_id: The job to query.

        Returns:
            Job status dict, or None if not found.
        """
        return self._jobs.get(job_id)

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


# Singleton instance shared across the application
job_store = JobStore()
