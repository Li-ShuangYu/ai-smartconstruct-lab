# End-to-End Test Plan: Training Orchestration AI Pipeline

## Overview

This document describes the manual end-to-end testing steps for the Training Orchestration AI Pipeline. The test validates the complete flow from template publish (Java backend) through AI processing (Python AI engine) to callback completion and database persistence.

## Prerequisites

### Services Required
| Service | URL | Start Command |
|---------|-----|---------------|
| MySQL 8 | localhost:3306 | `docker-compose -f infrastructure/docker-compose.yml up -d` |
| Java Backend | localhost:8080 | `cd backend-core && ./mvnw.cmd spring-boot:run` |
| Python AI Engine | localhost:8000 | `cd ai-engine && uvicorn main:app --reload --port 8000` |

### Configuration
1. **Backend** (`backend-core/src/main/resources/application.properties`):
   - `ai-engine.base-url=http://localhost:8000`
   - `ai-engine.service-token=<your-service-token>`
   - Database connection: `spring.datasource.url=jdbc:mysql://localhost:3306/ai-smartconstruct-lab`

2. **AI Engine** (`ai-engine/.env`):
   - `LLM_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1`
   - `LLM_API_KEY=<your-dashscope-api-key>`
   - `LLM_MODEL_NAME=qwen-plus`

### Test Data
- Use the fixture at `ai-engine/tests/fixtures/complete_orchestration.json` as the canvas JSON payload
- Ensure a teacher user exists in `sys_user` with `role_type=2`

---

## Test Scenarios

### Scenario 1: Happy Path — Full Orchestration Flow

**Objective**: Verify the complete pipeline from template publish to AI callback completion.

#### Steps

1. **Authenticate as Teacher**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "teacher1", "password": "password123"}'
   ```
   Save the returned `token` for subsequent requests.

2. **Publish Template with Canvas JSON**
   ```bash
   curl -X POST http://localhost:8080/api/teacher/templates \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d '{
       "templateName": "E2E测试模板",
       "templateDesc": "端到端测试用模板",
       "rawCanvasJson": <contents of complete_orchestration.json>
     }'
   ```
   **Expected**: HTTP 200, response contains `id` and `aiStatus: 1`

3. **Verify Initial DB State**
   ```sql
   SELECT id, template_name, ai_status, error_reason, created_at
   FROM wf_training_template
   WHERE template_name = 'E2E测试模板';
   ```
   **Expected**: `ai_status = 1` (processing)

4. **Wait for AI Processing** (30-90 seconds)
   Monitor AI engine logs:
   ```bash
   # In ai-engine terminal, watch for:
   # "Supervisor created plan with 10 tasks"
   # "X/10 tasks succeeded"
   # "Sending callback to ..."
   ```

5. **Verify Final DB State**
   ```sql
   SELECT id, ai_status, standard_payload_json IS NOT NULL as has_payload,
          error_reason
   FROM wf_training_template
   WHERE template_name = 'E2E测试模板';
   ```
   **Expected**: `ai_status = 2`, `has_payload = 1`

6. **Verify Generated Questions**
   ```sql
   SELECT COUNT(*) as question_count, qb.bank_name
   FROM biz_question q
   JOIN biz_question_bank qb ON q.bank_id = qb.id
   WHERE qb.bank_name LIKE 'AI生成题库-E2E测试模板%';
   ```
   **Expected**: Questions exist with `create_type = 1`

7. **Verify Generated Knowledge Points**
   ```sql
   SELECT COUNT(*) as kp_count
   FROM biz_knowledge_point
   WHERE course_id = <template_id>;
   ```
   **Expected**: Knowledge points exist if STRUCT_AGENT produced them

8. **Verify Standard Payload JSON Structure**
   ```sql
   SELECT JSON_EXTRACT(standard_payload_json, '$.nodes[0].config._ai_status') as first_node_status
   FROM wf_training_template
   WHERE template_name = 'E2E测试模板';
   ```
   **Expected**: `"ready"` for AI-processed nodes

---

### Scenario 2: AI Engine Unreachable

**Objective**: Verify graceful failure when AI engine is down.

#### Steps

1. Stop the AI engine service
2. Publish a template (same as Scenario 1, Step 2)
3. **Expected**: Template saved with `ai_status = 3`, `error_reason` contains "AI引擎连接失败"
4. Start the AI engine service
5. Retry via:
   ```bash
   curl -X POST http://localhost:8080/api/teacher/templates/<id>/retry-ai \
     -H "Authorization: Bearer <token>"
   ```
6. **Expected**: `ai_status` resets to 1, processing begins

---

### Scenario 3: Timeout Detection

**Objective**: Verify the scheduled timeout detection marks stale templates.

#### Steps

1. Publish a template
2. Immediately stop the AI engine (before it can process)
3. Wait 11 minutes (or temporarily adjust the scheduled task interval for testing)
4. **Verify**:
   ```sql
   SELECT ai_status, error_reason
   FROM wf_training_template
   WHERE id = <template_id>;
   ```
   **Expected**: `ai_status = 3`, `error_reason = "AI处理超时"`

---

### Scenario 4: Idempotent Callback

**Objective**: Verify duplicate callbacks don't corrupt data.

#### Steps

1. Complete Scenario 1 successfully (ai_status = 2)
2. Manually send a duplicate callback:
   ```bash
   curl -X POST http://localhost:8080/api/internal/ai-callback \
     -H "Content-Type: application/json" \
     -H "X-Service-Token: <service-token>" \
     -d '{
       "templateId": <id>,
       "jobId": "duplicate-test",
       "status": 2,
       "standardPayloadJson": {"nodes": [], "edges": []}
     }'
   ```
3. **Expected**: HTTP 200, but `standard_payload_json` remains unchanged (original data preserved)

---

### Scenario 5: Partial Failure

**Objective**: Verify behavior when some AI tasks fail but others succeed.

#### Steps

1. Configure AI engine with a very short per-task timeout (modify `TASK_TIMEOUT_SECONDS` temporarily)
2. Submit a template with many nodes
3. **Expected**: Callback with `status=2` (partial success) or `status=3` (all failed)
4. Check `standard_payload_json` for nodes with `_ai_status: "error"`

---

## Verification Checklist

| # | Check | Expected Result | Pass/Fail |
|---|-------|-----------------|-----------|
| 1 | Template saved to DB | `ai_status=1`, `raw_canvas_json` populated | |
| 2 | AI engine receives request | Logs show "Received orchestration request" | |
| 3 | Supervisor detects correct nodes | Logs show "plan with 10 tasks" | |
| 4 | Agents execute in parallel | Multiple agent logs appear simultaneously | |
| 5 | Callback delivered to backend | Backend logs show "Received AI callback" | |
| 6 | Template status updated | `ai_status=2` in DB | |
| 7 | Standard payload persisted | `standard_payload_json` is valid JSON | |
| 8 | Questions persisted | `biz_question` rows with `create_type=1` | |
| 9 | Knowledge points persisted | `biz_knowledge_point` rows exist | |
| 10 | Retry works after failure | `ai_status` resets to 1, then completes to 2 | |
| 11 | Timeout detection works | Stale templates marked as `ai_status=3` | |
| 12 | Duplicate callback idempotent | No data corruption on second callback | |

---

## Troubleshooting

| Issue | Possible Cause | Resolution |
|-------|---------------|------------|
| `ai_status` stays at 1 | AI engine not running or unreachable | Check AI engine logs, verify `ai-engine.base-url` |
| Callback fails | Backend not reachable from AI engine | Verify `callback_url` in AI engine logs |
| No questions in DB | EXAM_AGENT failed or no HOMEWORK nodes with `source_mode=ai` | Check AI engine logs for agent errors |
| LLM timeout | DashScope API rate limit or network issue | Check `LLM_API_KEY`, verify API quota |
| 403 on template create | User not authenticated or not a teacher | Verify JWT token and `role_type=2` |

---

## Notes

- The AI engine processes templates asynchronously. The publish endpoint returns immediately.
- Typical processing time for a 16-node template: 30-90 seconds depending on LLM response times.
- The scheduled timeout task runs every minute. Templates processing for >10 minutes are marked as failed.
- For development testing, consider using the MockLLMService (see `ai-engine/tests/mocks/mock_llm.py`) to avoid LLM API costs.
