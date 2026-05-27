# Tasks: Training Orchestration AI Pipeline (实训编排AI流水线)

## Task 1: Java Backend — AI Engine Client & Dispatch Infrastructure

### Description
Create the `IAiEngineClient` interface and implementation for communicating with the Python AI engine. Replace the existing mock AI processing with real HTTP dispatch.

### Requirements Covered
- Requirement 2 (AI Engine Dispatch)
- Requirement 1.1 (async dispatch after save)

### Subtasks
- [x] 1.1 Create `IAiEngineClient` interface with `submitForProcessing(Long templateId, Object canvasJson)` and `queryStatus(String jobId)` methods
- [x] 1.2 Create `AiEngineClientImpl` using Spring WebClient (non-blocking HTTP) to POST to AI engine `/api/orchestration/process`
- [x] 1.3 Add `ai-engine.base-url` and `ai-engine.service-token` properties to `application.properties`
- [x] 1.4 Create `AiProcessRequest` DTO with fields: templateId, orchestrationId, canvasJson, callbackUrl
- [x] 1.5 Create `AiJobStatus` DTO for status query response
- [x] 1.6 Implement timeout handling (5s connect, 10s read) and error handling in the client — update `ai_status=3` on failure
- [x] 1.7 Add `spring-boot-starter-webflux` dependency to `pom.xml` for WebClient
- [x] 1.8 Refactor `TrainingTemplateServiceImpl.processTemplateMockAi()` to call `aiEngineClient.submitForProcessing()` instead of Thread.sleep mock

---

## Task 2: Java Backend — AI Callback Endpoint & Persistence

### Description
Create the internal callback controller that receives AI processing results and persists them to the database.

### Requirements Covered
- Requirement 7 (AI Callback & Persistence)
- Requirement 10.1 (callback security)

### Subtasks
- [x] 2.1 Create `AiCallbackRequest` DTO with fields: templateId, jobId, status, standardPayloadJson, errorReason, generatedAssets
- [x] 2.2 Create `GeneratedAssets` DTO containing lists of QuestionDTO, KnowledgePointDTO, EvalIndicatorDTO
- [x] 2.3 Create `AiCallbackController` at `/api/internal/ai-callback` with POST handler
- [x] 2.4 Implement callback logic: update `wf_training_template` (standard_payload_json, ai_status) based on status
- [x] 2.5 Implement question persistence: batch insert AI-generated questions into `biz_question` with `create_type=1`
- [x] 2.6 Implement knowledge point persistence: insert into `biz_knowledge_point` with parent-child hierarchy
- [x] 2.7 Implement idempotency check: if `ai_status` already equals 2, return 200 without modification
- [x] 2.8 Add `/api/internal/**` to SecurityConfig as a path that bypasses JWT but requires internal service token header validation

---

## Task 3: Java Backend — Retry Mechanism & Timeout Detection

### Description
Implement the manual retry endpoint and scheduled timeout detection for stale AI processing tasks.

### Requirements Covered
- Requirement 8 (Error Recovery & Retry)

### Subtasks
- [x] 3.1 Add `POST /api/teacher/templates/{id}/retry-ai` endpoint to `TeacherTemplateController`
- [x] 3.2 Implement retry logic: validate ai_status=3, verify creator_id matches current user, reset to ai_status=1, re-dispatch
- [x] 3.3 Create a `@Scheduled` task that runs every minute to detect templates with ai_status=1 older than 10 minutes
- [x] 3.4 The scheduled task updates stale templates to ai_status=3 with error_reason="AI处理超时"
- [x] 3.5 Add `@EnableScheduling` to a config class if not already present
- [x] 3.6 Add `GET /api/internal/ai-status/{templateId}` polling endpoint for AI engine fallback queries

---

## Task 4: Python AI Engine — Orchestration Router & Models

### Description
Create the FastAPI router for receiving orchestration requests and define all Pydantic models for the system.

### Requirements Covered
- Requirement 9 (AI Engine API Contract)
- Requirement 3.5 (ExecutionPlan model)

### Subtasks
- [x] 4.1 Create `models/schemas.py` with all Pydantic models: OrchestrationRequest, OrchestrationResponse, AgentTask, ExecutionPlan, AgentType enum
- [x] 4.2 Create `models/agent_outputs.py` with domain-specific output models: TextAgentResult, StructAgentResult, ExamAgentResult, CodeAgentResult, VideoAgentResult, GeneratedQuestion, GeneratedMindmap, GeneratedChapter, GeneratedFlashcard
- [x] 4.3 Create `models/callback.py` with AiCallbackPayload model
- [x] 4.4 Create `routers/orchestration.py` with POST `/api/orchestration/process` endpoint that accepts OrchestrationRequest and returns OrchestrationResponse
- [x] 4.5 Create `routers/orchestration.py` with GET `/api/orchestration/status/{job_id}` endpoint for status polling
- [x] 4.6 Add request validation: reject missing fields (400), invalid JSON (422), overloaded (503)
- [x] 4.7 Add rate limiting middleware (10 requests/minute) for the process endpoint
- [x] 4.8 Register the orchestration router in `main.py`
- [x] 4.9 Add `httpx`, `tenacity`, `structlog` to `requirements.txt`

---

## Task 5: Python AI Engine — Supervisor Agent & Node Detection

### Description
Implement the Supervisor Agent that parses orchestration JSON and generates the parallel execution plan.

### Requirements Covered
- Requirement 3 (AI Node Detection)

### Subtasks
- [x] 5.1 Create `agents/supervisor.py` with `SupervisorAgent` class
- [x] 5.2 Implement `detect_ai_nodes(canvas_json)` function with the routing table mapping node_type → AgentType
- [x] 5.3 Implement AI flag detection logic: scan config for `enable_ai_*: true`, `source_mode: "ai"`, `enable_code_review: true`, `enable_ai_pre_evaluation: true`
- [x] 5.4 Ensure nodes without AI flags are skipped (SEMESTER_SURVEY, MINDMAP_DRAW with enable_ai_generate_map=false, END, etc.)
- [x] 5.5 Ensure no duplicate tasks per node_id — one AgentTask per node regardless of multiple AI flags
- [x] 5.6 Create `analyze_orchestration()` method that returns an ExecutionPlan
- [x] 5.7 Write unit tests in `tests/test_supervisor.py` covering all 16 node types from the sample JSON

---

## Task 6: Python AI Engine — Domain Agents Implementation

### Description
Implement the five domain-specific agents with LLM integration and structured output.

### Requirements Covered
- Requirement 5 (Domain Agent Structured Output)

### Subtasks
- [x] 6.1 Create `agents/base_agent.py` with abstract `BaseAgent` class defining `execute(task: AgentTask) -> AgentResult` interface
- [x] 6.2 Create `services/llm_service.py` — initialize ChatOpenAI with DashScope endpoint (from .env), provide `with_structured_output()` wrapper
- [x] 6.3 Implement `agents/text_agent.py` — TextAgent handling: welcome messages (START), summaries/key_points/nav_tree (RESOURCE_READ), system prompts (AI_PRACTICE)
- [x] 6.4 Implement `agents/struct_agent.py` — StructAgent handling: mindmap generation (MINDMAP_PREVIEW), task breakdown (TASK_DISTRIBUTE)
- [x] 6.5 Implement `agents/exam_agent.py` — ExamAgent handling: question generation (HOMEWORK with source_mode=ai), flashcard generation (THEORY_CLASS)
- [x] 6.6 Implement `agents/code_agent.py` — CodeAgent handling: review checkpoints (CODING_CLASS), evaluation indicators/rubrics (PLAN_UPLOAD)
- [x] 6.7 Implement `agents/video_agent.py` — VideoAgent handling: chapter segmentation (VIDEO_LEARN)
- [x] 6.8 Add prompt templates for each agent in `prompts/` directory (one .txt or .py per agent)
- [x] 6.9 Implement Pydantic validation on all agent outputs with retry on ValidationError

---

## Task 7: Python AI Engine — Workflow Orchestration & Parallel Execution

### Description
Implement the LangGraph-based workflow that orchestrates parallel agent execution, result merging, and callback delivery.

### Requirements Covered
- Requirement 4 (Parallel Agent Execution)
- Requirement 6 (Result Merging & Standard Payload)
- Requirement 7.1 (Callback delivery)

### Subtasks
- [x] 7.1 Create `services/workflow.py` with `execute_orchestration_workflow()` async function
- [x] 7.2 Implement parallel execution using `asyncio.gather(*tasks, return_exceptions=True)` with per-task 60s timeout
- [x] 7.3 Implement overall workflow 5-minute timeout with cancellation of remaining tasks
- [x] 7.4 Implement retry logic using `tenacity`: 3 retries with exponential backoff (2s, 4s, 8s) per agent task
- [x] 7.5 Create `services/merger.py` with `merge_results_into_payload()` — enriches original JSON with `_ai_generated` and `_ai_status` per node
- [x] 7.6 Create `services/asset_extractor.py` with `extract_db_assets()` — extracts questions, knowledge_points, eval_indicators from agent results
- [x] 7.7 Create `services/callback.py` with `send_callback()` — POST results to Java backend callback URL with 3 retries (5s/15s/30s)
- [x] 7.8 Implement job status tracking (in-memory dict or Redis) for the polling endpoint
- [x] 7.9 Wire everything together: orchestration router → supervisor → parallel agents → merger → callback

---

## Task 8: Integration Testing & End-to-End Validation

### Description
Create integration tests that validate the complete flow from template publish to AI callback completion.

### Requirements Covered
- All requirements (end-to-end validation)

### Subtasks
- [x] 8.1 Create a mock LLM service in `tests/mocks/mock_llm.py` that returns fixed structured responses for each agent type
- [x] 8.2 Write Python integration test: submit sample orchestration JSON → verify execution plan → verify merged output structure
- [x] 8.3 Write Python integration test: verify callback payload matches expected schema
- [x] 8.4 Write Java unit test for `AiCallbackController`: mock callback request → verify DB state changes
- [x] 8.5 Write Java unit test for `AiEngineClientImpl`: mock HTTP responses → verify dispatch logic
- [x] 8.6 Create a test fixture using the `docs/samples/complete-orchestration.json` as input data
- [x] 8.7 Verify that the sample JSON (16 nodes) produces exactly the expected number of AI tasks (nodes with AI flags: ~10 tasks)
- [x] 8.8 Document manual E2E testing steps in a test plan (start both services, publish template, verify DB state)
