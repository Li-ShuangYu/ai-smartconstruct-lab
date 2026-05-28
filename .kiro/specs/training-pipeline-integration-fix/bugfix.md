# Bugfix Requirements Document

## Introduction

AI SmartConstruct Lab 教学平台存在三条核心功能链路断裂问题，导致教师端"用不了"：

1. **实训编排节点检查功能不通**：教师创建实训编排后，除 AI 对练（ai_practice）、理论实训（theory_class）、编码实训（coding_class）以外的节点类型（如 resource_read、video_watch、survey、task_distribute、req_upload、plan_upload、homework、mindmap_preview、mindmap_draw、student_peer_review、teacher_eval、grouping 等），编排完成后入库数据不完整、AI 处理链路异常、AI 处理完成后内容无法正常回写入库。
2. **模版预览功能不正常**：TemplatePreview 页面未按"课前阶段 → 课中阶段 → 课后阶段"分阶段纵向展示节点列表，且 AI 生成内容（standard_payload_json）未正确回填到各节点的预览中。
3. **上传方案功能是孤岛**：PlanUpload 组件使用硬编码的 placeholder 数据（雷达图分数、反馈文字全部写死），未调用真实 API 获取 AI 可行性分析结果，提交后无实际入库操作，且与教师端监控页面（TeacherPlanUpload）、模版预览等功能未打通。

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN 教师编排包含 resource_read/video_watch/survey/task_distribute/req_upload/plan_upload/homework/mindmap_preview/mindmap_draw/student_peer_review/teacher_eval/grouping 等节点并发布模板 THEN 系统将节点配置以扁平 key-value 格式入库到 raw_canvas_json，但 AI 引擎期望的 6 维度结构（display/db_mapping/ai_processing/orchestration_settings/data_collection/evaluation）缺失，导致 NodeValidationService 校验失败或 AI 引擎无法正确路由处理

1.2 WHEN AI 引擎处理上述节点类型时 THEN 由于 AiEngineClientImpl.enrichWithAiSpecs() 注入的 ai_spec_json 与前端提交的扁平 config 结构不匹配，AI 处理链路报错或跳过这些节点，导致 standard_payload_json 中缺少对应节点的生成内容

1.3 WHEN AI 处理完成回调（/api/internal/ai/callback）返回 standard_payload_json 后 THEN 系统仅更新模板级 ai_status=2 和 standard_payload_json 字段，但未将 AI 生成的节点级内容（如 AI 摘要、AI 欢迎语、AI 题目等）回写到各节点实例的 config_json 或独立存储表中，导致后续预览和学生端无法获取 AI 生成内容

1.4 WHEN 教师打开模版预览页面（TemplatePreview.vue）THEN 系统使用 NCollapse 折叠面板展示阶段，但未按照"课前阶段 → 课中阶段 → 课后阶段"的固定顺序排列，而是按 raw_canvas_json 中 phases 数组的原始顺序展示，且阶段默认折叠状态不一致

1.5 WHEN 模版预览页面渲染节点列表 THEN 节点以卡片网格（nodes-grid）纵向排列，但每个节点的 ai_generated_content 字段始终为 null（后端 preview 接口中 enrichment 逻辑仅有 log.debug 占位，未实际从 standard_payload_json 中提取并注入各节点的 AI 生成内容）

1.6 WHEN 学生在实训流程中进入方案上传（PlanUpload.vue）节点 THEN 系统展示硬编码的 AI 可行性分析雷达图（技术可行性 85、时间可行性 72 等固定值），而非调用后端 API 获取真实的 AI 分析结果

1.7 WHEN 学生在 PlanUpload 组件中点击"提交方案"THEN 系统仅触发 emit('complete') 事件，未调用 StudentSubmissionController 的文件上传接口（/api/student/training/submit），导致方案文件未入库、未触发 AI 预评审

1.8 WHEN 教师在 TeacherPlanUpload 监控页面查看学生方案上传状态 THEN 页面无法获取到学生的实际提交数据，因为 PlanUpload 组件提交未入库，监控页面与学生提交流程完全断开

### Expected Behavior (Correct)

2.1 WHEN 教师编排包含任意节点类型并发布模板 THEN 系统 SHALL 将前端提交的节点配置正确转换为 AI 引擎可处理的格式（包含必要的 node_type、config 字段），确保 raw_canvas_json 入库数据结构完整，AI 引擎能正确识别和路由每种节点类型

2.2 WHEN AI 引擎处理各类节点时 THEN 系统 SHALL 确保 AiEngineClientImpl.enrichWithAiSpecs() 正确注入每种节点类型的 ai_spec_json，AI 引擎能根据 ai_spec 中的处理指令对每个节点执行对应的内容生成（如资源摘要、视频章节、题目生成等），并在 standard_payload_json 中包含所有节点的处理结果

2.3 WHEN AI 处理完成回调返回 standard_payload_json 后 THEN 系统 SHALL 解析 standard_payload_json 中各节点的 AI 生成内容，并将其持久化到可被预览接口和学生端查询的存储位置（如 wf_node_ai_status 表的 generated_content 字段或模板的结构化存储中）

2.4 WHEN 教师打开模版预览页面 THEN 系统 SHALL 按照阶段的 sort_num 升序排列展示（对应"课前阶段 → 课中阶段 → 课后阶段"），每个阶段默认展开，阶段内节点以纵向列表形式清晰排列

2.5 WHEN 模版预览页面渲染节点列表且模板 ai_status=2（AI 处理完成）THEN 系统 SHALL 从 standard_payload_json 中提取每个节点对应的 AI 生成内容，并填充到预览数据的 ai_generated_content 字段中，使教师能在预览中看到 AI 生成的具体内容（如摘要、题目、欢迎语等）

2.6 WHEN 学生在实训流程中进入方案上传节点 THEN 系统 SHALL 调用后端 API 获取该节点的 AI 可行性分析配置和历史提交状态，若已有 AI 分析结果则展示真实数据，若尚未提交则展示空状态引导上传

2.7 WHEN 学生在 PlanUpload 组件中选择文件并点击"提交方案"THEN 系统 SHALL 先调用文件上传接口获取 resource_id，再调用 StudentSubmissionController 的提交接口（/api/student/training/submit）将方案入库，并触发 AI 预评审流程，提交成功后展示提交状态和 AI 评审进度

2.8 WHEN 教师在 TeacherPlanUpload 监控页面查看学生方案上传状态 THEN 系统 SHALL 能实时获取到学生的提交记录（包括文件信息、提交时间、AI 评审状态和评审结果），实现教师监控与学生提交的完整数据链路

### Unchanged Behavior (Regression Prevention)

3.1 WHEN 教师编排包含 ai_practice（AI 对练）节点并发布模板 THEN 系统 SHALL CONTINUE TO 正常入库、正常触发 AI 处理、AI 处理完成后内容正常回写

3.2 WHEN 教师编排包含 theory_class（理论实训）节点并发布模板 THEN 系统 SHALL CONTINUE TO 正常入库、正常触发 AI 处理、AI 处理完成后生成学习卡片内容正常回写

3.3 WHEN 教师编排包含 coding_class（编码实训）节点并发布模板 THEN 系统 SHALL CONTINUE TO 正常入库、正常触发 AI 处理、AI 代码审查配置正常生效

3.4 WHEN 教师创建模板时输入模板名称和描述 THEN 系统 SHALL CONTINUE TO 正确保存模板基础信息（templateName、templateDescription、creatorId）到 wf_training_template 表

3.5 WHEN AI 引擎处理完成后通过回调接口返回 status=2 THEN 系统 SHALL CONTINUE TO 正确更新模板 ai_status=2 和 standard_payload_json 字段，WebSocket 通知教师前端状态变更

3.6 WHEN 学生在实训流程中提交需求上传（req_upload）THEN 系统 SHALL CONTINUE TO 正常调用 handleFileUpload 入库并触发 AI 预评审

3.7 WHEN 教师在 TrainingManage 页面点击"预览"按钮 THEN 系统 SHALL CONTINUE TO 正确路由到 /teacher/template-preview/:templateId 页面并加载预览数据
