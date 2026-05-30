# Implementation Plan: Student Training Demo

## Overview

本实现计划将学生实训演示模块分解为可增量执行的编码任务。后端使用 Java 8 / Spring Boot 2.7 / MyBatis-Plus，前端使用 TypeScript / Vue 3.5 / Pinia。任务按依赖顺序编排：先建立类型和接口，再实现后端服务，最后实现前端组件和集成。

## Tasks

- [x] 1. 后端：DemoDataSeeder 服务实现
  - [x] 1.1 创建 DemoDataSeederService 类和模拟内容生成逻辑
    - 创建 `backend-core/src/main/java/.../service/impl/DemoDataSeederService.java`
    - 注入 `ITrainingTemplateService`、`SysUserService`、`IStudentService`、`ITrainingTaskService` 等依赖
    - 实现 `seedAll()` 方法：按顺序调用 generateAndSeedContent → createDemoStudents → createTaskAndAssign
    - 实现 `generateAndSeedContent(Long templateId)`：读取模板 phases_json，遍历节点，根据 node_type 生成对应 JSON 内容
    - 为 theory_class 节点生成 slides 数组（5-15 个 slide，含 type/title/content 及类型特定字段）
    - 为 coding_class 节点生成 coding_task 对象（含 description/code_template/hints/test_cases/language）
    - 为其他节点类型（resource_read/learning_survey/homework/exam 等）生成对应格式的模拟数据
    - 将生成内容写入 Training_Template 的 standard_payload_json 字段，设置 ai_status=2
    - 实现模板不存在或 phases_json 为空时的错误处理（终止并输出错误信息）
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7_

  - [x] 1.2 实现演示学生账号批量创建逻辑
    - 在 DemoDataSeederService 中实现 `createDemoStudents(int count)` 方法
    - 创建 20 个 sys_user 记录（role_type=3, status=0），用户名格式 demo_student_01 至 demo_student_20
    - 密码 "123456" 使用 BCrypt 编码存储到 password_hash
    - 创建对应 biz_student 记录（student_no 格式 2026010001-2026010020，real_name 格式"演示学生01"-"演示学生20"）
    - class_id 关联"26软件工程1班"
    - 实现幂等性：基于 username 唯一键判断，跳过已存在记录
    - _Requirements: 2.1, 2.3, 2.4, 2.5, 2.6_

  - [x] 1.3 实现实训任务创建与分配逻辑
    - 在 DemoDataSeederService 中实现 `createTaskAndAssign(Long templateId, Long classId)` 方法
    - 创建 Training_Task 记录（task_name 与模板一致，is_in_class=0，dispatch_scope=1，status=0，start_time=now，end_time=now+7天）
    - 基于模板 phases_json 和 standard_payload_json 创建所有 Node_Instance 记录（config_json 取自 standard_payload_json 中对应节点的 ai_generated_content）
    - 为班级中每个学生创建 Participation 记录（status=0）
    - 为每个学生的每个 Node_Instance 创建 Node_Progress 记录（status=0）
    - 实现班级无学生时跳过 Participation/Node_Progress 创建并输出警告
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

  - [x] 1.4 创建 DemoDataSeeder REST 触发端点
    - 在已有的 `InitController.java` 中添加 `@PostMapping("/api/public/demo/seed")` 端点
    - 调用 DemoDataSeederService.seedAll() 并返回 ApiResult<DemoSeedResult>
    - 创建 DemoSeedResult DTO（含 studentsCreated、taskId、nodeInstanceCount、participationCount 等统计字段）
    - 确保端点在 SecurityConfig 的 public paths 中（/api/public/** 已覆盖）
    - _Requirements: 1.1, 1.4_

  - [x] 1.5 编写 DemoDataSeeder 幂等性属性测试
    - **Property 3: Seeder 幂等性**
    - 使用 jqwik 生成随机用户名集合，模拟重复执行 createDemoStudents
    - 验证重复执行后数据库记录数量不变且不抛出异常
    - **Validates: Requirements 2.5**

  - [x] 1.6 编写 theory_class 内容格式属性测试
    - **Property 1: 演示数据内容格式一致性**
    - 使用 jqwik 生成随机 slides 数组，验证每个 slide 的结构约束
    - 验证 type 为 code 时必须含 code/output，type 为 quiz 时必须含 questions 数组
    - **Validates: Requirements 1.2**

  - [x] 1.7 编写 coding_class 内容格式属性测试
    - **Property 2: 编码实训数据格式一致性**
    - 使用 jqwik 生成随机 coding_task 对象，验证字段约束
    - 验证 description≤500字符、hints 1-5条、test_cases 2-10条、language="python"
    - **Validates: Requirements 1.3**

- [x] 2. Checkpoint - 确保 DemoDataSeeder 编译通过
  - Ensure all tests pass, ask the user if questions arise.

- [x] 3. 后端：学生实训流转 API 实现
  - [x] 3.1 实现 IPhaseExecutionService 接口和实现类
    - 创建 `IPhaseExecutionService.java` 接口（checkAndUnlockNextPhase、isPhaseComplete、getPhaseUnlockStatuses）
    - 创建 `PhaseExecutionServiceImpl.java` 实现类
    - `isPhaseComplete`：检查阶段内所有 is_required=true 节点的 Node_Progress status 是否均为 2
    - `checkAndUnlockNextPhase`：当阶段完成时，将 sort_num 紧邻的下一阶段设为 is_unlocked=true
    - `getPhaseUnlockStatuses`：返回学生所有阶段的解锁状态列表
    - 处理无 is_required 节点的阶段：前置阶段完成后自动标记为 complete 并解锁下一阶段
    - _Requirements: 5.1, 5.2, 5.3, 5.6_

  - [x] 3.2 实现 StudentTrainingController 的实训总览和位置接口
    - 创建或扩展 `StudentTrainingController.java`
    - 实现 `GET /api/student/tasks/{taskId}/overview`：返回 Participation + 阶段列表 + 节点进度 + 解锁状态
    - 实现 `GET /api/student/tasks/{taskId}/current-position`：查询 Student_Activity_State 返回当前位置
    - 创建对应的 DTO 类（StudentTaskOverviewDTO、PhaseProgressDTO、NodeProgressDTO、CurrentPositionDTO）
    - _Requirements: 4.1, 4.2, 4.3, 7.6, 7.7_

  - [x] 3.3 实现 StudentTrainingController 的实训开始和节点操作接口
    - 实现 `POST /api/student/tasks/{taskId}/start`：Participation status 0→1，创建 Student_Activity_State
    - 实现 `POST /api/student/nodes/{nodeInstanceId}/enter`：Node_Progress status→1，设置 entered_at，更新 Activity_State
    - 实现 `POST /api/student/nodes/{nodeInstanceId}/complete`：Node_Progress status→2，计算 stay_duration_seconds，调用 checkAndUnlockNextPhase
    - 实现 `GET /api/student/nodes/{nodeInstanceId}/content`：返回节点 config_json
    - 实现状态保护：已完成/已跳过节点的 enter 不修改记录；已进行中的实训 start 直接恢复位置
    - 实现 Participation status=2 时显示完成总结
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 8.1, 8.2, 8.4, 8.5_

  - [x] 3.4 编写阶段顺序解锁不变量属性测试
    - **Property 4: 阶段顺序解锁不变量**
    - 使用 jqwik 生成随机阶段状态组合
    - 验证：如果阶段 N 的 is_unlocked=true 且 N>1，则所有前序阶段 is_complete=true
    - **Validates: Requirements 5.1, 5.2**

  - [x] 3.5 编写节点完成触发阶段解锁属性测试
    - **Property 5: 节点完成触发阶段解锁**
    - 使用 jqwik 生成随机节点完成序列
    - 验证：阶段内所有 is_required 节点 status=2 时，阶段 is_complete=true 且下一阶段 is_unlocked=true
    - **Validates: Requirements 5.2**

  - [x] 3.6 编写节点进入状态转换属性测试
    - **Property 7: 节点进入状态转换正确性**
    - 使用 jqwik 生成随机初始状态（0/1/2/3）
    - 验证：status=0或1时 enterNode 后 status=1 且 entered_at 非空；status=2或3时记录不变
    - **Validates: Requirements 7.1, 7.2**

  - [x] 3.7 编写停留时长累加属性测试
    - **Property 8: 停留时长累加正确性**
    - 使用 jqwik 生成随机时间序列和累计值
    - 验证：stay_duration_seconds = 之前累计值 + (当前时间 - entered_at)，结果为非负整数
    - **Validates: Requirements 7.3**

  - [x] 3.8 编写实训状态流转属性测试
    - **Property 9: 实训状态流转完整性**
    - 使用 jqwik 生成随机操作序列
    - 验证：Participation 状态只能按 0→1→2 顺序流转，不允许逆向或跳跃
    - **Validates: Requirements 8.1, 8.2**

- [x] 4. Checkpoint - 确保后端 API 编译通过并通过单元测试
  - 修复了 StudentFlowController 与 StudentTrainingController 端点冲突 (enterNode/completeNode 重复注册)
  - 修复了 SeederToStudentFlowIntegrationTest 中 ObjectMapper 注入失败、PhaseUnlockStatus 类型缺失、多余 Mockito stub
  - 修复了后端 getNodeContent 返回 key 与前端不匹配 (configJson → node_config)
  - 修复了 nodePageResolver 中 coding_class 映射错误 → 改为 StudentCodingLab.vue 适配 seeder 数据
  - 新增 StudentCodingLab.vue，支持 coding_task 结构

- [x] 5. 前端：类型定义和服务层
  - [x] 5.1 创建学生实训相关 TypeScript 类型定义
    - 创建或扩展 `frontend-vue/src/services/types/studentTraining.ts`
    - 定义 StudentTaskOverview、PhaseProgress、NodeProgress、CurrentPosition 接口
    - 定义 Slide、QuizQuestion、TheoryClassConfig、CodingTaskConfig 接口
    - 定义 EnterNodeResponse、CompleteNodeResponse 接口
    - 确保类型与后端 DTO 一一对应，使用 TypeScript strict mode（无 any）
    - _Requirements: 4.1, 6.1, 6.2, 6.3_

  - [x] 5.2 创建或扩展学生实训服务层
    - 创建或扩展 `frontend-vue/src/services/modules/studentTraining.service.ts`
    - 实现 `getTaskOverview(taskId: number)` → GET /api/student/tasks/{taskId}/overview
    - 实现 `getCurrentPosition(taskId: number)` → GET /api/student/tasks/{taskId}/current-position
    - 实现 `startTraining(taskId: number)` → POST /api/student/tasks/{taskId}/start
    - 实现 `enterNode(nodeInstanceId: number)` → POST /api/student/nodes/{nodeInstanceId}/enter
    - 实现 `completeNode(nodeInstanceId: number)` → POST /api/student/nodes/{nodeInstanceId}/complete
    - 实现 `getNodeContent(nodeInstanceId: number)` → GET /api/student/nodes/{nodeInstanceId}/content
    - 使用已有的 Axios 实例（src/services/api.ts），不直接调用 Axios
    - _Requirements: 4.1, 6.5, 7.1, 8.1_

- [x] 6. 前端：Store 层和节点解析器
  - [x] 6.1 扩展 useStudentFlowStore 实现实训流转状态管理
    - 扩展 `frontend-vue/src/stores/modules/useStudentFlowStore.ts`
    - 添加 state：taskOverview、currentPhaseId、currentNodeId、loading、error
    - 实现 action：loadTaskOverview、startTraining、enterNode、completeNode、navigateToNextNode
    - 实现 getter：currentPhaseProgress、phaseCompletionPercentage、isPhaseUnlocked、currentNodeConfig
    - 实现进度百分比计算：round(已完成节点数 / 总节点数 × 100)
    - 实现恢复位置逻辑：基于 Student_Activity_State 或 Node_Progress 定位
    - _Requirements: 4.2, 4.3, 7.6, 7.7, 8.3, 8.4_

  - [x] 6.2 扩展 nodePageResolver 添加所有节点类型映射
    - 扩展 `frontend-vue/src/views/.../nodePageResolver.ts`
    - 新增 theory_class → TheoryClassView 的 defineAsyncComponent 映射
    - 确保所有 17 种节点类型均有映射（start、resource_read、theory_class、coding_class、learning_survey、homework、exam、mindmap_preview、mindmap_draw、knowledge_eval、student_peer_review、grouping、task_distribute、req_upload、plan_upload、inter_group_review、teacher_eval、end）
    - 对未注册类型返回 null 并显示包含 node_type 名称的错误提示
    - _Requirements: 6.1, 6.4, 6.7_

  - [x] 6.3 编写进度百分比计算属性测试
    - **Property 6: 进度百分比计算正确性**
    - 使用 fast-check 生成随机节点数（1-100）和完成数（0-总数）
    - 验证：百分比 = round(完成数/总数 × 100)，结果范围 0-100 整数
    - **Validates: Requirements 8.3**

- [x] 7. 前端：TheoryClassView 组件实现
  - [x] 7.1 创建 TheoryClassView.vue 幻灯片组件
    - 创建 `frontend-vue/src/views/.../studentTraining/TheoryClassView.vue`
    - 接收 props：nodeInstanceId (number)、nodeConfig ({ slides: Slide[] })
    - 实现幻灯片导航（上一页/下一页/进度指示器）
    - 根据 slide.type 渲染不同内容：
      - intro/theory：标题 + 内容 + bullet_points
      - code：标题 + 内容 + 代码高亮块 + 运行结果
      - quiz：标题 + 问题列表 + 选项 + 答案验证
      - summary：标题 + 内容 + key_points
    - 最后一个 slide 完成后触发 `complete` 事件
    - 使用 CSS 变量（variables.css），scoped styles，无硬编码颜色
    - _Requirements: 6.2, 1.2_

  - [x] 7.2 编写 TheoryClassView 组件单元测试
    - 使用 Vitest + Vue Test Utils 测试组件渲染
    - 测试不同 slide type 的正确渲染
    - 测试导航逻辑（上一页/下一页/边界）
    - 测试 complete 事件触发
    - _Requirements: 6.2_

- [x] 8. 前端：路由守卫和页面集成
  - [x] 8.1 实现 Vue Router 导航守卫和实训路由
    - 扩展 `frontend-vue/src/router/modules/training.ts`
    - 添加学生实训执行路由：`/student/training/:taskId/execute`
    - 实现 beforeEnter 守卫：验证阶段解锁状态
    - 未解锁阶段访问时重定向到当前解锁且未完成阶段的第一个未完成节点
    - 所有阶段完成时允许自由访问
    - _Requirements: 5.4, 5.5, 5.6_

  - [x] 8.2 实现实训预览页面和执行页面集成
    - 创建或扩展实训预览页面（TrainingPreview.vue）：展示阶段列表、解锁状态、进度
    - 创建或扩展实训执行页面（TrainingExecute.vue）：集成 nodePageResolver 动态加载组件
    - 实现阶段点击逻辑：已解锁可进入，未解锁显示提示
    - 实现节点完成后自动导航到下一节点
    - 实现阶段完成提示和返回阶段预览的导航按钮（3秒内显示）
    - 实现加载状态指示和错误提示 + 重试按钮
    - 使用 CSS 变量，scoped styles
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 6.5, 6.6_

  - [x] 8.3 实现实训状态流转 UI 集成
    - 在学生工作台实训列表中显示"进行中"标签和进度百分比
    - 实现"开始实训"按钮（Participation status 0→1）
    - 实现已完成实训的总结页面（总分 + 各阶段完成状态）
    - 实现恢复位置逻辑：已进行中的实训直接恢复到上次中断位置
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 9. Checkpoint - 确保前端编译通过
  - Ensure all tests pass, ask the user if questions arise.

- [x] 10. 集成与端到端验证
  - [x] 10.1 端到端集成：Seeder 触发到学生实训流转
    - 验证 POST /api/public/demo/seed 端点正确执行完整播种流程
    - 验证学生登录后能看到实训任务并开始实训
    - 验证节点内容从 config_json 正确传递到前端组件
    - 验证阶段解锁逻辑在完成节点后正确触发
    - 验证进度持久化：退出后重新进入能恢复位置
    - 编写自动化集成测试覆盖核心流转路径
    - _Requirements: 1.4, 3.2, 4.1, 5.2, 6.5, 7.6, 8.1_

  - [x] 10.2 编写前端集成测试
    - 测试 nodePageResolver 对所有已注册 node_type 返回正确组件
    - 测试 useStudentFlowStore 的完整流转（loadOverview → start → enter → complete → next）
    - 测试路由守卫拦截未解锁阶段的访问
    - _Requirements: 5.5, 6.1, 6.7_

- [x] 11. Final checkpoint - 确保所有测试通过
  - 后端 86 个测试全部通过（0 failures, 0 errors）
  - 前端 vue-tsc 类型检查通过

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties (jqwik for Java, fast-check for TypeScript)
- Unit tests validate specific examples and edge cases
- 后端 PBT 使用 jqwik 库（Java），前端 PBT 使用 fast-check 库（TypeScript）
- 前端严格遵循三层架构：View → Store → Service，不在 View 中直接调用 Axios
- 所有样式使用 CSS 变量（variables.css），scoped styles，无硬编码颜色
- TypeScript strict mode，禁止使用 `any`

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "5.1"] },
    { "id": 1, "tasks": ["1.2", "1.3", "5.2"] },
    { "id": 2, "tasks": ["1.4", "1.5", "1.6", "1.7", "3.1"] },
    { "id": 3, "tasks": ["3.2", "3.3", "6.1"] },
    { "id": 4, "tasks": ["3.4", "3.5", "3.6", "3.7", "3.8", "6.2"] },
    { "id": 5, "tasks": ["6.3", "7.1"] },
    { "id": 6, "tasks": ["7.2", "8.1"] },
    { "id": 7, "tasks": ["8.2", "8.3"] },
    { "id": 8, "tasks": ["10.1"] },
    { "id": 9, "tasks": ["10.2"] }
  ]
}
```
