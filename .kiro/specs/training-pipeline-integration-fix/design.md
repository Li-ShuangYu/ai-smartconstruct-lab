# Training Pipeline Integration Fix - Bugfix Design

## Overview

AI SmartConstruct Lab 教学平台存在三条核心功能链路断裂：(1) 非 AI 对练类节点（resource_read、video_watch 等 12 种类型）在编排发布后入库数据不完整、AI 处理链路异常、AI 结果无法回写；(2) TemplatePreview 页面未按 sort_num 排序阶段且 AI 生成内容未填充；(3) PlanUpload 组件使用硬编码数据、无真实 API 调用、提交不入库。

修复策略：在后端补全节点配置转换逻辑、AI 回调结果回写逻辑、预览接口 enrichment 逻辑；在前端修复阶段排序、接入真实 API 替换硬编码数据、实现完整提交流程。

## Glossary

- **Bug_Condition (C)**: 触发 bug 的条件集合——当节点类型为非 ai_practice/theory_class/coding_class 时的编排发布、当预览页面渲染阶段列表时、当学生使用 PlanUpload 组件时
- **Property (P)**: 期望行为——所有节点类型正确入库并被 AI 处理、阶段按 sort_num 排序且 AI 内容填充、PlanUpload 调用真实 API 并入库
- **Preservation**: 已有 ai_practice/theory_class/coding_class 节点的完整处理链路、模板基础信息保存、WebSocket 通知、req_upload 提交流程
- **enrichWithAiSpecs()**: `AiEngineClientImpl` 中为 canvas 节点注入 `ai_spec` 的方法，从 `wf_node_def` 表查询 `ai_spec_json`
- **standard_payload_json**: AI 引擎处理完成后返回的标准化结果 JSON，存储在 `wf_training_template` 表中
- **NodeValidationService**: 验证节点配置是否符合 6 维度结构（display/db_mapping/ai_processing/orchestration_settings/data_collection/evaluation）的服务
- **TeacherTemplateController.preview()**: 后端预览接口，解析 raw_canvas_json 构建预览数据但未实现 AI 内容 enrichment
- **wf_node_ai_status**: 节点级 AI 处理状态表，存储每个节点的 AI 处理结果（result_json 字段）

## Bug Details

### Bug Condition

Bug 在以下三个独立条件下触发：

**条件 1 - 非 AI 对练类节点处理链路断裂：**
当教师编排包含 resource_read/video_watch/survey/task_distribute/req_upload/plan_upload/homework/mindmap_preview/mindmap_draw/student_peer_review/teacher_eval/grouping 节点并发布模板时，前端提交的扁平 config 格式与 NodeValidationService 要求的 6 维度结构不匹配，导致校验失败或 AI 引擎无法正确路由处理。

**条件 2 - 模版预览排序与内容缺失：**
当教师打开 TemplatePreview 页面时，后端 `preview()` 方法按 raw_canvas_json 中 phases 数组原始顺序返回数据，未按 sort_num 排序；且 AI 内容 enrichment 逻辑仅有 `log.debug` 占位，`ai_generated_content` 始终为 null。

**条件 3 - PlanUpload 组件孤岛：**
当学生进入方案上传节点时，`radarDimensions` 使用硬编码固定分数，`handleSubmit()` 仅触发 `emit('complete')` 无 API 调用，TeacherPlanUpload 使用硬编码 `studentList`。

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type { action: string, nodeType?: string, templateId?: number, component?: string }
  OUTPUT: boolean
  
  // Chain 1: Non-AI node types fail in orchestration pipeline
  CONDITION_1 := input.action == "publish_template"
                 AND input.nodeType IN ['resource_read', 'video_watch', 'survey', 
                     'task_distribute', 'req_upload', 'plan_upload', 'homework',
                     'mindmap_preview', 'mindmap_draw', 'student_peer_review', 
                     'teacher_eval', 'grouping']
                 AND (nodeConfigMissing6Dimensions(input.nodeType)
                      OR aiCallbackNotWritingBackToNodes(input.templateId))
  
  // Chain 2: Template preview phases not sorted, AI content not populated
  CONDITION_2 := input.action == "preview_template"
                 AND (phasesNotSortedBySortNum(input.templateId)
                      OR aiGeneratedContentIsNull(input.templateId))
  
  // Chain 3: PlanUpload uses hardcoded data, no real API calls
  CONDITION_3 := input.action == "student_plan_upload"
                 AND input.component == "PlanUpload"
                 AND (radarDataIsHardcoded()
                      OR submitDoesNotCallApi()
                      OR teacherMonitorUsesHardcodedList())
  
  RETURN CONDITION_1 OR CONDITION_2 OR CONDITION_3
END FUNCTION
```

### Examples

- **Chain 1 示例**: 教师编排包含 `resource_read` 节点，发布后 raw_canvas_json 中该节点 config 为 `{"resource_id": 123, "min_reading_duration": 300}`（扁平格式），NodeValidationService 校验报错"缺少 display/db_mapping/ai_processing 等维度"，AI 引擎跳过该节点
- **Chain 1 示例**: AI 回调返回 standard_payload_json 包含 resource_read 节点的摘要内容，但 `AiCallbackController.onAiPipelineComplete()` 仅更新模板级字段，未将节点级内容写入 wf_node_ai_status.result_json
- **Chain 2 示例**: raw_canvas_json 中 phases 顺序为 `[{phase_name:"课中阶段", sort_num:2}, {phase_name:"课前阶段", sort_num:1}]`，预览页面按原始顺序展示"课中→课前"而非"课前→课中"
- **Chain 2 示例**: 模板 ai_status=2 且 standard_payload_json 包含节点 AI 内容，但预览接口返回的每个节点 `ai_generated_content` 均为 null
- **Chain 3 示例**: 学生打开 PlanUpload 组件，雷达图始终显示"技术可行性 85、时间可行性 72"等固定值，无论是否已有 AI 分析结果
- **Chain 3 示例**: 学生点击"提交方案"，仅触发 `emit('complete')`，文件未上传到服务器，StudentSubmissionController 未被调用

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- ai_practice（AI 对练）节点的完整处理链路：入库 → AI 处理 → 内容回写，必须继续正常工作
- theory_class（理论实训）节点的学习卡片生成流程不受影响
- coding_class（编码实训）节点的 AI 代码审查配置继续生效
- 模板基础信息（templateName、templateDescription、creatorId）保存逻辑不变
- AI 回调成功后 ai_status=2 更新和 WebSocket 通知机制不变
- req_upload 节点的 handleFileUpload 入库和 AI 预评审流程不变
- TrainingManage 页面"预览"按钮路由到 /teacher/template-preview/:templateId 不变

**Scope:**
所有不涉及上述三条断裂链路的功能应完全不受影响。具体包括：
- 已正常工作的 3 种 AI 节点类型的处理流程
- 用户认证、权限控制、JWT 验证
- 其他学生端实训节点（如 ai_practice 对话流程）
- 管理员端功能

## Hypothesized Root Cause

Based on the bug description and code analysis, the most likely issues are:

1. **节点配置格式不匹配（Chain 1）**: 前端编排器提交节点配置时，非 AI 对练类节点使用扁平 key-value 格式（如 `{resource_id, min_reading_duration}`），但 `NodeValidationServiceImpl.validateNodeConfig()` 严格要求 6 维度顶层结构（display/db_mapping/ai_processing/orchestration_settings/data_collection/evaluation）。需要在入库前增加配置格式转换层，或放宽校验策略。

2. **AI 回调未回写节点级内容（Chain 1）**: `AiCallbackController.onAiPipelineComplete()` 在 AI 处理成功时仅更新模板级 `standard_payload_json` 和 `ai_status`，未解析 standard_payload_json 中各节点的生成内容并写入 `wf_node_ai_status.result_json`。虽然 `onNodeAiStatusUpdate()` 端点存在，但 AI 引擎可能未对每个节点独立回调。

3. **预览接口 enrichment 逻辑未实现（Chain 2）**: `TeacherTemplateController.preview()` 中 AI 内容 enrichment 部分仅有 `log.debug("Template [id={}] has standard_payload_json, enriching preview", id)` 占位，未实际解析 standard_payload_json 并将节点级内容注入到 `ai_generated_content` 字段。同时，phases 列表未按 sort_num 排序后返回。

4. **PlanUpload 组件完全硬编码（Chain 3）**: `PlanUpload.vue` 的 `radarDimensions` 是 computed 属性返回固定数组，`handleSubmit()` 仅调用 `emit('complete')` 无任何 API 请求。`TeacherPlanUpload.vue` 的 `studentList` 是硬编码的 ref 数组，未调用后端接口获取真实学生提交数据。

## Correctness Properties

Property 1: Bug Condition - Non-AI Node Types Complete Pipeline

_For any_ template publish action where the template contains nodes of type resource_read, video_watch, survey, task_distribute, req_upload, plan_upload, homework, mindmap_preview, mindmap_draw, student_peer_review, teacher_eval, or grouping, the fixed system SHALL correctly transform node configurations into AI-processable format, ensure AI engine processes all node types, and write back AI-generated content to queryable storage (wf_node_ai_status.result_json or standard_payload_json node entries).

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Bug Condition - Template Preview Phase Ordering and AI Content

_For any_ template preview request where the template has multiple phases with different sort_num values, the fixed preview endpoint SHALL return phases sorted by sort_num ascending (课前→课中→课后), and when ai_status=2, SHALL populate each node's ai_generated_content from standard_payload_json.

**Validates: Requirements 2.4, 2.5**

Property 3: Bug Condition - PlanUpload Real API Integration

_For any_ student interaction with the PlanUpload component, the fixed component SHALL call backend APIs to fetch real AI feasibility analysis data (replacing hardcoded scores), call StudentSubmissionController's submit endpoint on form submission (uploading file and triggering AI pre-review), and the TeacherPlanUpload monitor SHALL fetch real student submission records from the backend.

**Validates: Requirements 2.6, 2.7, 2.8**

Property 4: Preservation - Existing AI Node Types Unchanged

_For any_ template containing ai_practice, theory_class, or coding_class nodes, the fixed system SHALL produce exactly the same processing results as the original system, preserving the complete pipeline of storage, AI processing, and content write-back for these node types.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `backend-core/.../service/impl/NodeValidationServiceImpl.java`

**Change 1: Relax or adapt validation for non-6-dimension configs**
- Add a config transformation layer that wraps flat node configs into the 6-dimension structure before validation
- OR add a `validateNodeConfigFlat()` method that validates flat configs against a simplified schema for non-AI-intensive node types
- Ensure `enrichWithAiSpecs()` can still locate and inject `ai_spec` regardless of config format

---

**File**: `backend-core/.../controller/AiCallbackController.java`

**Function**: `onAiPipelineComplete()`

**Change 2: Parse and persist node-level AI content from standard_payload_json**
- After updating template-level fields, parse `standard_payload_json` to extract per-node AI results
- For each node entry in the payload, upsert `wf_node_ai_status` record with `result_json` = node's generated content and `ai_status` = 2
- This ensures node-level AI content is queryable by the preview endpoint

---

**File**: `backend-core/.../controller/TeacherTemplateController.java`

**Function**: `preview()`

**Change 3: Sort phases by sort_num and implement AI content enrichment**
- After building `phasesPreview` list, sort it by `sort_num` ascending before adding to response
- In the AI enrichment block (currently only `log.debug`), parse `standard_payload_json`, build a `Map<nodeId, aiContent>`, then iterate through all nodes in `phasesPreview` and set `ai_generated_content` from the map
- Also check `wf_node_ai_status.result_json` as fallback source for node AI content

---

**File**: `frontend-vue/src/views/training/studentTraining/PlanUpload.vue`

**Change 4: Replace hardcoded data with real API calls**
- Add `onMounted` lifecycle hook to call backend API for fetching AI feasibility analysis results for the current node instance
- Replace hardcoded `radarDimensions` with reactive data populated from API response
- Implement `handleSubmit()` to: (1) upload file via file upload API, (2) call `/api/student/training/{nodeInstanceId}/submit` with resource_id, (3) show submission status and AI review progress
- Add loading states and empty states for when no AI analysis is available yet

---

**File**: `frontend-vue/src/views/training/teacherTraining/TeacherPlanUpload.vue`

**Change 5: Replace hardcoded student list with real API data**
- Remove hardcoded `studentList` ref
- Add `onMounted` to fetch real student submission records from backend API (e.g., `/api/teacher/training/{nodeInstanceId}/submissions`)
- Implement real `previewPlan()` to fetch file preview URL from backend
- Implement real `rejectPlan()` to call backend rejection API
- Implement real `batchDownload()` to trigger backend batch download endpoint

---

**File**: `backend-core/.../service/impl/AiEngineClientImpl.java`

**Function**: `enrichWithAiSpecs()`

**Change 6: Ensure enrichment works with both flat and 6-dimension config formats**
- The current implementation correctly iterates phases→nodes and injects `ai_spec` based on `node_type`
- Verify that `wf_node_def` table has `ai_spec_json` entries for all 12 non-AI node types
- If missing, add seed data for these node types with appropriate AI processing instructions

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Write tests that simulate template publishing with non-AI node types, template preview requests, and PlanUpload component interactions. Run these tests on the UNFIXED code to observe failures and understand the root cause.

**Test Cases**:
1. **Node Validation Test**: Call `NodeValidationServiceImpl.validateNodeConfig("resource_read", flatConfigJson)` — expect validation failure due to missing 6-dimension structure (will fail on unfixed code)
2. **AI Callback Write-back Test**: Simulate AI callback with standard_payload_json containing node-level content, then query wf_node_ai_status for that node — expect result_json to be null (will fail on unfixed code)
3. **Preview Sort Order Test**: Create template with phases in reverse sort_num order, call preview endpoint — expect phases returned in original (wrong) order (will fail on unfixed code)
4. **Preview AI Content Test**: Create template with ai_status=2 and standard_payload_json, call preview endpoint — expect ai_generated_content to be null for all nodes (will fail on unfixed code)
5. **PlanUpload Hardcoded Data Test**: Render PlanUpload component and inspect radarDimensions — expect hardcoded values regardless of props (will fail on unfixed code)

**Expected Counterexamples**:
- NodeValidationService returns error "结构错误: 缺少=[display, db_mapping, ai_processing, ...]" for flat configs
- Preview endpoint returns phases in insertion order, not sort_num order
- ai_generated_content is always null even when standard_payload_json contains data
- PlanUpload always shows score 85/72/90/88/65 regardless of backend state

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed function produces the expected behavior.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  IF input.action == "publish_template" THEN
    result := publishTemplate_fixed(input)
    ASSERT nodeConfigStoredCorrectly(result)
    ASSERT aiEngineCanProcessAllNodeTypes(result)
    ASSERT aiContentWrittenBackToNodes(result)
  
  IF input.action == "preview_template" THEN
    result := previewTemplate_fixed(input)
    ASSERT phasesSortedBySortNum(result.phases)
    ASSERT aiGeneratedContentPopulated(result.phases) WHEN template.ai_status == 2
  
  IF input.action == "student_plan_upload" THEN
    result := submitPlan_fixed(input)
    ASSERT fileUploadedToServer(result)
    ASSERT submissionPersistedInDatabase(result)
    ASSERT aiPreReviewTriggered(result)
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed function produces the same result as the original function.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT originalFunction(input) = fixedFunction(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain (various node type combinations, template states)
- It catches edge cases that manual unit tests might miss (e.g., templates with mixed AI and non-AI nodes)
- It provides strong guarantees that behavior is unchanged for all non-buggy inputs

**Test Plan**: Observe behavior on UNFIXED code first for ai_practice/theory_class/coding_class nodes, then write property-based tests capturing that behavior.

**Test Cases**:
1. **AI Practice Preservation**: Verify ai_practice node publish → AI process → content write-back continues to work identically after fix
2. **Theory Class Preservation**: Verify theory_class learning card generation is unchanged
3. **Coding Class Preservation**: Verify coding_class AI code review config continues to function
4. **Template Basic Info Preservation**: Verify template name/description/creator save logic unchanged
5. **WebSocket Notification Preservation**: Verify AI completion WebSocket broadcast unchanged
6. **Req Upload Preservation**: Verify req_upload handleFileUpload flow unchanged

### Unit Tests

- Test `NodeValidationServiceImpl` with flat configs for each of the 12 non-AI node types (after adding transformation layer)
- Test `TeacherTemplateController.preview()` phase sorting with various sort_num orderings
- Test AI content enrichment logic with mock standard_payload_json containing node entries
- Test `AiCallbackController.onAiPipelineComplete()` node-level content persistence
- Test PlanUpload component API integration with mocked backend responses

### Property-Based Tests

- Generate random template configurations with mixed node types and verify all nodes are correctly processed through the pipeline
- Generate random phase orderings with various sort_num values and verify preview always returns sorted results
- Generate random standard_payload_json structures and verify enrichment correctly maps content to nodes by node_id
- Generate random combinations of existing AI node types and verify their processing is identical before and after fix

### Integration Tests

- End-to-end test: Teacher creates template with resource_read + video_watch nodes → publishes → AI processes → preview shows AI content
- End-to-end test: Student opens PlanUpload → uploads file → submits → teacher sees submission in TeacherPlanUpload monitor
- End-to-end test: Template with 3 phases (课前 sort_num=1, 课中 sort_num=2, 课后 sort_num=3) → preview shows correct order
- End-to-end test: Mixed template with ai_practice + resource_read → both nodes processed correctly, ai_practice unchanged
