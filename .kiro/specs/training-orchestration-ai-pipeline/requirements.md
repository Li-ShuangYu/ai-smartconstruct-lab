# Requirements: Training Orchestration AI Pipeline (实训编排AI流水线)

## Requirement 1: Template Publish & Save

### Acceptance Criteria

1.1 When a teacher publishes a template, the system saves `raw_canvas_json` to `wf_training_template` with `ai_status = 1` (processing) and returns the template ID immediately without blocking.

1.2 The `raw_canvas_json` field stores the exact JSON payload from the frontend canvas editor, preserving all node coordinates, edges, and config properties without modification.

1.3 The template record includes `template_name`, `template_description`, `creator_id` (the authenticated teacher's user ID), `created_at`, and `updated_at` timestamps.

1.4 If the template name is empty or exceeds 128 characters, the system returns a 400 error with a descriptive message.

1.5 Only authenticated users with `role_type = 2` (teacher) can create templates. Unauthorized users receive a 403 response.

---

## Requirement 2: AI Engine Dispatch

### Acceptance Criteria

2.1 After saving the template, the backend asynchronously dispatches the canvas JSON to the AI engine via HTTP POST to `/api/orchestration/process` with `templateId`, `orchestrationId`, `canvasJson`, and `callbackUrl`.

2.2 The dispatch uses a dedicated thread pool (`templateAiExecutor`) and does not block the HTTP response to the frontend.

2.3 If the AI engine is unreachable (connection timeout or refused), the system updates `ai_status = 3` and sets `error_reason` to a human-readable error message.

2.4 The AI engine responds with HTTP 202 Accepted and a `jobId` (UUID format) within 5 seconds, confirming the task was queued.

2.5 The `callbackUrl` points to the backend's internal callback endpoint (`/api/internal/ai-callback`) and is constructed from server configuration, not hardcoded.

---

## Requirement 3: AI Node Detection (Supervisor Agent)

### Acceptance Criteria

3.1 The Supervisor Agent traverses all nodes in the canvas JSON and identifies nodes requiring AI processing by detecting `enable_ai_*: true` flags or `source_mode: "ai"` in the node config.

3.2 Each detected AI node is routed to the correct domain agent based on a routing table: START/RESOURCE_READ/AI_PRACTICE → TEXT_AGENT, MINDMAP_PREVIEW/TASK_DISTRIBUTE → STRUCT_AGENT, HOMEWORK/THEORY_CLASS → EXAM_AGENT, CODING_CLASS/PLAN_UPLOAD → CODE_AGENT, VIDEO_LEARN → VIDEO_AGENT.

3.3 Nodes without any AI flags (e.g., SEMESTER_SURVEY with no `enable_ai_*` flags, MINDMAP_DRAW with `enable_ai_generate_map: false`) are skipped and not dispatched.

3.4 The detection algorithm produces no duplicate tasks for the same node — each node generates at most one AgentTask regardless of how many AI flags it has.

3.5 The output is an `ExecutionPlan` containing a list of `AgentTask` objects, each with `task_id`, `agent_type`, `node_id`, `node_type`, and the full node `config`.

---

## Requirement 4: Parallel Agent Execution

### Acceptance Criteria

4.1 All agent tasks in the execution plan are executed in parallel using `asyncio.gather`, with no sequential dependencies between tasks.

4.2 Each agent task has a 60-second timeout. If exceeded, the task is cancelled and marked as failed without affecting other running tasks.

4.3 The overall orchestration workflow has a 5-minute timeout. If exceeded, all remaining tasks are cancelled and a failure callback is sent.

4.4 Individual agent failures (LLM timeout, schema violation) do not cause the entire workflow to fail — other agents continue execution.

4.5 Failed tasks are retried up to 3 times with exponential backoff (2s, 4s, 8s) before being marked as permanently failed.

---

## Requirement 5: Domain Agent Structured Output

### Acceptance Criteria

5.1 TEXT_AGENT generates: welcome messages (150-250 chars), resource summaries (≤300 chars), key points (5-7 items), and navigation trees — all conforming to their respective Pydantic models.

5.2 STRUCT_AGENT generates: mindmap topologies (respecting `max_nodes` limit, 3-4 depth levels) and task breakdowns (5-7 milestones) — output as nested JSON structures.

5.3 EXAM_AGENT generates questions matching exact type counts from config (`single_choice`, `multi_choice`, `fill_blank`, `true_false`, `essay`) with `question_type` enum values 1-5, and generates flashcards with `front_content`/`back_content` pairs.

5.4 CODE_AGENT generates code review checkpoints and evaluation indicators (rubrics) with scoring criteria — output as structured lists.

5.5 VIDEO_AGENT generates chapter segmentation with `title`, `start_time`, `end_time`, and `summary` for each chapter (4-8 chapters per video).

5.6 All agent outputs are validated against their Pydantic model before being accepted. Invalid outputs trigger a retry with stricter prompting.

---

## Requirement 6: Result Merging & Standard Payload

### Acceptance Criteria

6.1 After all agents complete, their results are merged back into the original canvas JSON structure to produce `standard_payload_json`.

6.2 The `standard_payload_json` preserves the exact same `nodes` array length and `edges` array as the original `raw_canvas_json` — no nodes or edges are added or removed.

6.3 For AI-processed nodes, the config is enriched with an `_ai_generated` object containing the agent's output and `_ai_status: "ready"`.

6.4 For nodes that did not require AI processing, `_ai_status: "not_required"` is set in their config.

6.5 For nodes where AI processing failed, `_ai_status: "error"` is set with an `_ai_error` message in their config.

---

## Requirement 7: AI Callback & Persistence

### Acceptance Criteria

7.1 The AI engine sends a POST request to `/api/internal/ai-callback` with `templateId`, `jobId`, `status` (2=success, 3=failure), `standardPayloadJson`, `errorReason`, and `generatedAssets`.

7.2 On successful callback (status=2), the backend updates `wf_training_template` setting `standard_payload_json` and `ai_status = 2`.

7.3 On failure callback (status=3), the backend updates `ai_status = 3` and persists the `error_reason`.

7.4 If `generatedAssets.questions` is non-empty, each question is inserted into `biz_question` with `create_type = 1` (AI-generated), linked to an auto-created `biz_question_bank`.

7.5 If `generatedAssets.knowledgePoints` is non-empty, each knowledge point is inserted into `biz_knowledge_point` with proper parent-child hierarchy.

7.6 Duplicate callbacks for the same `templateId` (where `ai_status` is already 2) are idempotent — they return 200 OK without modifying data.

7.7 The callback endpoint is protected and only accessible from the AI engine's IP address or via an internal service token.

---

## Requirement 8: Error Recovery & Retry

### Acceptance Criteria

8.1 Teachers can manually retry failed AI processing via `POST /api/teacher/templates/{id}/retry-ai`, which resets `ai_status` to 1 and re-dispatches to the AI engine.

8.2 Retry is only allowed when `ai_status = 3` (failed). Attempting to retry a template with `ai_status = 1` (processing) or `ai_status = 2` (ready) returns a 400 error.

8.3 If the AI engine fails to deliver the callback, it retries 3 times with intervals of 5s, 15s, 30s before persisting results locally.

8.4 A scheduled task on the backend checks for templates with `ai_status = 1` that have been processing for more than 10 minutes, and marks them as `ai_status = 3` with `error_reason = "AI处理超时"`.

8.5 The frontend displays the current `ai_status` and `error_reason` to the teacher, with a retry button visible when status is 3.

---

## Requirement 9: AI Engine API Contract

### Acceptance Criteria

9.1 The AI engine exposes `POST /api/orchestration/process` accepting `OrchestrationRequest` (template_id, orchestration_id, canvas_json, callback_url) and returning `OrchestrationResponse` (job_id, status, estimated_duration_seconds).

9.2 The AI engine exposes `GET /api/orchestration/status/{job_id}` returning the current processing status and partial results (for polling fallback).

9.3 The AI engine validates incoming requests: rejects requests with missing required fields (400), invalid JSON structure (422), or when the service is overloaded (503).

9.4 The AI engine rate-limits the `/api/orchestration/process` endpoint to 10 requests per minute to prevent LLM API quota exhaustion.

9.5 All AI engine endpoints return structured JSON error responses with `error_code` and `error_message` fields.

---

## Requirement 10: Security & Access Control

### Acceptance Criteria

10.1 The `/api/internal/ai-callback` endpoint is excluded from JWT authentication but protected by IP whitelist or internal service token validation.

10.2 The AI engine's LLM API key is stored only in the AI engine's `.env` file and never transmitted to or through the Java backend.

10.3 The Java backend authenticates to the AI engine using a shared internal service token (configured in `application.properties`), not user JWT tokens.

10.4 All JSON payloads are validated against expected schemas before processing to prevent injection attacks.

10.5 The `/api/teacher/templates/{id}/retry-ai` endpoint verifies that the requesting teacher is the template's `creator_id` before allowing retry.
