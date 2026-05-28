# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** - Training Pipeline Integration Failures (Non-AI Node Types, Preview Sorting, PlanUpload Hardcoded Data)
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the three broken chains exist
  - **Scoped PBT Approach**: Scope properties to concrete failing cases for each chain:
    - Chain 1: `NodeValidationServiceImpl.validateNodeConfig("resource_read", flatConfigJson)` fails due to missing 6-dimension structure
    - Chain 1: After AI callback with standard_payload_json containing node-level content, `wf_node_ai_status.result_json` is null for those nodes
    - Chain 2: Template with phases `[{sort_num:2, "课中"}, {sort_num:1, "课前"}]` — preview returns phases in insertion order, not sort_num order
    - Chain 2: Template with `ai_status=2` and populated `standard_payload_json` — preview returns `ai_generated_content=null` for all nodes
    - Chain 3: PlanUpload component `radarDimensions` returns hardcoded values (85, 72, 90, 88, 65) regardless of backend state
    - Chain 3: PlanUpload `handleSubmit()` only emits 'complete', does not call any API endpoint
  - Test assertions should match Expected Behavior Properties from design:
    - `nodeConfigStoredCorrectly(result)` for non-AI node types
    - `phasesSortedBySortNum(result.phases)` for preview
    - `aiGeneratedContentPopulated(result.phases)` when `ai_status=2`
    - `fileUploadedToServer(result)` and `submissionPersistedInDatabase(result)` for PlanUpload
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests FAIL (this is correct - it proves the bugs exist)
  - Document counterexamples found:
    - NodeValidationService returns error "缺少 display/db_mapping/ai_processing 等维度" for flat configs
    - Preview endpoint returns phases in insertion order, not sort_num order
    - `ai_generated_content` is always null even when `standard_payload_json` contains data
    - PlanUpload always shows score 85/72/90/88/65 regardless of backend state
  - Mark task complete when tests are written, run, and failure is documented
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Existing AI Node Types (ai_practice, theory_class, coding_class) Pipeline Unchanged
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for non-buggy inputs (existing AI node types):
    - Observe: `ai_practice` node publish → AI process → content write-back works correctly on unfixed code
    - Observe: `theory_class` node publish → learning card generation works correctly on unfixed code
    - Observe: `coding_class` node publish → AI code review config functions correctly on unfixed code
    - Observe: Template basic info (templateName, templateDescription, creatorId) saves correctly on unfixed code
    - Observe: AI callback with `status=2` updates `ai_status=2` and triggers WebSocket notification on unfixed code
    - Observe: `req_upload` node `handleFileUpload` triggers AI pre-review on unfixed code
    - Observe: TrainingManage "预览" button routes to `/teacher/template-preview/:templateId` on unfixed code
  - Write property-based tests capturing observed behavior patterns:
    - For all templates containing ONLY ai_practice/theory_class/coding_class nodes, the publish → AI process → write-back pipeline produces identical results before and after fix
    - For all template save operations with valid templateName/templateDescription/creatorId, basic info is persisted identically
    - For all AI callbacks with status=2, template ai_status update and WebSocket notification behavior is identical
  - Property-based testing generates many test cases for stronger preservation guarantees across:
    - Various combinations of existing AI node types
    - Different template metadata values
    - Different AI callback payload structures
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

- [x] 3. Fix for Training Pipeline Integration - Non-AI Node Types Processing Chain

  - [x] 3.1 Implement NodeValidationServiceImpl config transformation/relaxation
    - Add config format detection: check if incoming config has 6-dimension top-level keys (display/db_mapping/ai_processing/orchestration_settings/data_collection/evaluation)
    - If flat config detected for non-AI-intensive node types, wrap into simplified 6-dimension structure OR add `validateNodeConfigFlat()` bypass
    - Ensure `enrichWithAiSpecs()` can locate node_type and inject ai_spec regardless of config format
    - Verify all 12 non-AI node types (resource_read, video_watch, survey, task_distribute, req_upload, plan_upload, homework, mindmap_preview, mindmap_draw, student_peer_review, teacher_eval, grouping) pass validation
    - _Bug_Condition: isBugCondition(input) where input.action == "publish_template" AND input.nodeType IN non-AI types AND nodeConfigMissing6Dimensions_
    - _Expected_Behavior: nodeConfigStoredCorrectly(result) — all node types correctly transformed and stored_
    - _Preservation: ai_practice/theory_class/coding_class validation unchanged — these already have 6-dimension configs_
    - _Requirements: 2.1, 2.2, 3.1, 3.2, 3.3_

  - [x] 3.2 Implement AiCallbackController.onAiPipelineComplete() node-level content write-back
    - After existing template-level update (ai_status=2, standard_payload_json), parse standard_payload_json to extract per-node AI results
    - For each node entry in the payload, upsert `wf_node_ai_status` record: set `result_json` = node's generated content, `ai_status` = 2
    - Ensure node_instance_id mapping is correct (match by node_id or node_type+position in phases)
    - Preserve existing template-level ai_status update and WebSocket notification logic
    - _Bug_Condition: aiCallbackNotWritingBackToNodes(templateId) — node-level content not persisted_
    - _Expected_Behavior: aiContentWrittenBackToNodes(result) — each node's AI content queryable from wf_node_ai_status.result_json_
    - _Preservation: Template-level ai_status=2 update and WebSocket notification unchanged_
    - _Requirements: 2.3, 3.5_

  - [x] 3.3 Implement TeacherTemplateController.preview() phase sorting and AI content enrichment
    - After building `phasesPreview` list, sort by `sort_num` ascending before adding to response
    - In the AI enrichment block (currently `log.debug` placeholder):
      - Parse `standard_payload_json` into structured map
      - Build `Map<nodeId, aiContent>` from parsed payload
      - Iterate through all nodes in `phasesPreview` and set `ai_generated_content` from the map
      - Fallback: check `wf_node_ai_status.result_json` if node not found in standard_payload_json
    - Only perform enrichment when template `ai_status == 2`
    - _Bug_Condition: phasesNotSortedBySortNum(templateId) OR aiGeneratedContentIsNull(templateId)_
    - _Expected_Behavior: phasesSortedBySortNum(result.phases) AND aiGeneratedContentPopulated(result.phases) when ai_status=2_
    - _Preservation: Preview endpoint continues to return correct structure for templates without AI processing_
    - _Requirements: 2.4, 2.5, 3.7_

  - [x] 3.4 Implement AiEngineClientImpl.enrichWithAiSpecs() dual-format support
    - Verify `wf_node_def` table has `ai_spec_json` entries for all 12 non-AI node types
    - If missing, add seed data SQL for these node types with appropriate AI processing instructions
    - Ensure enrichment logic handles both flat config and 6-dimension config formats when locating node_type
    - _Bug_Condition: enrichWithAiSpecs fails to inject ai_spec for flat-config nodes_
    - _Expected_Behavior: aiEngineCanProcessAllNodeTypes(result) — all node types receive correct ai_spec injection_
    - _Preservation: Existing ai_practice/theory_class/coding_class enrichment unchanged_
    - _Requirements: 2.2, 3.1, 3.2, 3.3_

  - [x] 3.5 Implement PlanUpload.vue real API integration
    - Add `onMounted` lifecycle hook to call backend API for AI feasibility analysis results for current node instance
    - Replace hardcoded `radarDimensions` computed property with reactive data from API response
    - Implement `handleSubmit()`:
      1. Upload file via file upload API to get `resource_id`
      2. Call `/api/student/training/{nodeInstanceId}/submit` with resource_id and metadata
      3. Show submission status and AI review progress
    - Add loading states, empty states (no AI analysis yet), and error handling
    - Follow frontend three-layer architecture: View → Store → Service
    - _Bug_Condition: radarDataIsHardcoded() OR submitDoesNotCallApi()_
    - _Expected_Behavior: fileUploadedToServer(result) AND submissionPersistedInDatabase(result) AND aiPreReviewTriggered(result)_
    - _Preservation: Component emit('complete') still fires after successful submission for parent flow control_
    - _Requirements: 2.6, 2.7_

  - [x] 3.6 Implement TeacherPlanUpload.vue real API integration
    - Remove hardcoded `studentList` ref
    - Add `onMounted` to fetch real student submission records from `/api/teacher/training/{nodeInstanceId}/submissions`
    - Implement real `previewPlan()` to fetch file preview URL from backend
    - Implement real `rejectPlan()` to call backend rejection API
    - Implement real `batchDownload()` to trigger backend batch download endpoint
    - Follow frontend three-layer architecture: View → Store → Service
    - _Bug_Condition: teacherMonitorUsesHardcodedList()_
    - _Expected_Behavior: Teacher monitor displays real student submission records with file info, timestamps, AI review status_
    - _Preservation: UI layout and component structure unchanged_
    - _Requirements: 2.8_

  - [x] 3.7 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Training Pipeline Integration Works Correctly
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior for all three chains
    - When this test passes, it confirms:
      - Non-AI node types are correctly validated, processed by AI engine, and content written back
      - Preview phases are sorted by sort_num and AI content is populated
      - PlanUpload calls real APIs and persists submissions
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bugs are fixed)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8_

  - [x] 3.8 Verify preservation tests still pass
    - **Property 2: Preservation** - Existing AI Node Types Pipeline Unchanged
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all preservation tests still pass after fix:
      - ai_practice pipeline unchanged
      - theory_class learning card generation unchanged
      - coding_class AI code review unchanged
      - Template basic info save unchanged
      - WebSocket notification unchanged
      - req_upload flow unchanged

- [x] 4. Checkpoint - Ensure all tests pass
  - Run full backend test suite: `./mvnw.cmd test` in backend-core/
  - Run frontend type-check: `npx vue-tsc --build` in frontend-vue/
  - Verify all property-based tests (bug condition + preservation) pass
  - Verify no regressions in existing test suite
  - Ensure all tests pass, ask the user if questions arise.
