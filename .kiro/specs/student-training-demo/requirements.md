# Requirements Document

## Introduction

本功能为学生实训演示模块（Student Training Demo），目标是在当前已有的编排系统和AI内容生成基础上，创建完整的演示数据并实现学生端实训执行功能。核心场景：基于已有的【Python算法入门】实训模板，生成高质量模拟数据入库，创建实训任务并分配给演示班级，然后实现学生端按阶段、按节点顺序动态路由流转的完整实训体验。

## Glossary

- **Training_Template**: 实训模板（wf_training_template），教师编排的实训流程定义，包含阶段和节点信息
- **Training_Task**: 实训任务（biz_training_task），基于模板创建的具体实训场次
- **Node_Instance**: 节点实例（wf_node_instance），实训任务运行时每个节点的具体实例
- **Participation**: 学生实训参与记录（biz_training_participation），关联学生与实训任务
- **Student_Activity_State**: 学生活动状态（wf_student_activity_state），记录学生当前所在节点
- **Node_Progress**: 学生节点进度（wf_student_node_progress），记录学生在每个节点的完成状态
- **Phase**: 实训阶段，模板中定义的逻辑分组（如课前、课中、课后），存储在 phases_json 中
- **Demo_Data_Seeder**: 演示数据播种器，负责生成并入库模拟数据的后端模块
- **Node_Page_Resolver**: 节点页面解析器，根据 node_type 动态加载对应的 Vue 组件
- **TheoryClassView**: 理论实训页面组件，以幻灯片形式展示理论内容、代码示例和练习题
- **CodingLab**: 编码实训页面组件，提供代码编辑器、终端输出和AI助手面板
- **Admin_Class**: 行政班级（biz_admin_class），学生所属的行政班级

## Requirements

### Requirement 1: 演示数据生成与入库

**User Story:** As a 开发者/演示人员, I want to 基于【Python算法入门】模板的编排生成完整的模拟AI内容数据并入库, so that 演示时学生端可以直接使用这些数据进行实训流转，绕过有bug的AI引擎。

#### Acceptance Criteria

1. WHEN Demo_Data_Seeder 执行时, THE Demo_Data_Seeder SHALL 读取【Python算法入门】Training_Template 的 phases_json 和 raw_canvas_json，遍历所有阶段中的每个节点，根据节点的 node_type 和节点名称/主题，手工编写高质量的模拟内容数据（模拟AI生成的效果），数据格式须与前端组件（TheoryClassView、CodingLab等）的 props 接口完全匹配
2. WHEN Demo_Data_Seeder 生成理论实训节点（theory_class）内容时, THE Demo_Data_Seeder SHALL 生成包含 slides 数组的 JSON 数据（至少 5 个、至多 15 个 slide），每个 slide 包含 type（取值为 theory、intro、code、quiz、summary 之一）、title（不超过 50 字符）、content 字段；其中 type 为 code 的 slide 须额外包含 code（可运行的Python代码示例）和 output（代码运行结果）字段，type 为 quiz 的 slide 须包含 questions 数组（每个元素含 question、options 数组和 answer 字段），type 为 theory 或 intro 的 slide 可包含 bullet_points 数组，type 为 summary 的 slide 可包含 key_points 数组；内容须围绕Python算法主题且具有教学意义
3. WHEN Demo_Data_Seeder 生成编码实训节点（coding_class）内容时, THE Demo_Data_Seeder SHALL 生成包含 coding_task 对象的 JSON 数据，该对象包含 description（不超过 500 字符的任务描述）、code_template（含可运行的 Python 代码框架，包含函数签名和注释提示）、hints 数组（1 至 5 条提示）、test_cases 数组（2 至 10 条，每条含 description、input、expected 字段）和 language 字段（值为 "python"）；编码任务须与Python算法入门主题相关（如排序、搜索、递归等）
4. WHEN Demo_Data_Seeder 完成所有节点的数据生成后, THE Demo_Data_Seeder SHALL 将生成的内容按节点 ID 组织写入 Training_Template 的 standard_payload_json 字段（格式为 `{ "node_id_1": { ai_generated_content: {...} }, "node_id_2": {...} }`），并将 ai_status 设置为 2（已完成），使得后续创建实训任务时可以从此字段提取每个节点的内容
5. IF Demo_Data_Seeder 执行时目标 Training_Template 不存在或其 phases_json 为空, THEN THE Demo_Data_Seeder SHALL 终止执行并输出错误信息指明失败原因，不修改数据库中任何记录
6. WHEN Demo_Data_Seeder 生成非 theory_class 和 coding_class 类型的节点内容时, THE Demo_Data_Seeder SHALL 根据节点类型生成对应的模拟数据：resource_read 节点生成含 summary 和 key_points 字段的 JSON；learning_survey 节点生成含 questions 数组（每条含 question、type、options 字段）的 JSON；homework 和 exam 节点生成含 questions 数组（每条含 question、options、answer、score 字段）的 JSON；其余节点类型生成含 title 和 content 字段的占位 JSON
7. THE Demo_Data_Seeder SHALL 确保生成的所有模拟数据内容围绕"Python算法入门"主题，包括但不限于：变量与数据类型、条件判断、循环结构、列表操作、函数定义、排序算法（冒泡/选择/插入）、搜索算法（线性/二分）、递归基础等，使演示时内容连贯且具有教学价值

### Requirement 2: 演示班级与学生数据创建

**User Story:** As a 开发者/演示人员, I want to 批量创建演示班级和学生账号, so that 演示时有足够的学生账号可以登录并体验实训功能。

#### Acceptance Criteria

1. THE Demo_Data_Seeder SHALL 批量创建 20 个学生用户，每个学生包含 sys_user 记录（role_type=3, status=0）和 biz_student 记录（含 real_name、dept_id、major_id、class_id），且 class_id 关联到"26软件工程1班"
3. THE Demo_Data_Seeder SHALL 为每个演示学生生成格式为"demo_student_01"至"demo_student_20"的用户名（零填充两位序号），密码统一设置为"123456"并以 BCrypt 编码存储到 password_hash 字段
4. WHEN Demo_Data_Seeder 执行完成, THE Demo_Data_Seeder SHALL 确保每个学生的 sys_user 记录 status=0 且 password_hash 为有效的 BCrypt 编码值，使得使用用户名和密码"demo123456"调用登录接口时能获取到有效的 JWT token
5. IF Demo_Data_Seeder 重复执行, THEN THE Demo_Data_Seeder SHALL 跳过已存在的记录（基于 username 唯一键判断），不抛出异常且不产生重复数据
6. THE Demo_Data_Seeder SHALL 为每个演示学生的 biz_student 记录生成格式为"2026010001"至"2026010020"的 student_no（年份4位 + 班级序号2位 + 学生序号4位），real_name 设置为"演示学生01"至"演示学生20"

### Requirement 3: 实训任务创建与分配

**User Story:** As a 开发者/演示人员, I want to 基于Python算法入门模板创建实训任务并分配给演示班级, so that 学生登录后可以看到并参与实训。

#### Acceptance Criteria

1. THE Demo_Data_Seeder SHALL 基于【Python算法入门】Training_Template（ai_status=2，已就绪）创建一条 Training_Task 记录，字段设置为：task_name 与模板名称一致，template_id 关联该模板，teacher_id 关联演示教师，is_in_class=0（课后实训），dispatch_scope=1（行政班），dispatch_target_id 关联"26软件工程1班"，status=0（未开始），start_time 为当前时间，end_time 为 start_time 后 7 天
2. WHEN Training_Task 创建完成后, THE Demo_Data_Seeder SHALL 基于模板的 phases_json 和 standard_payload_json 为该任务创建所有 Node_Instance 记录，每条记录的 task_id 关联新建任务，phase_id 取自 phases_json 中对应阶段的 ID，sort_num 按节点顺序从 1 递增，node_def_id 关联 wf_node_def 中匹配的节点类型，**config_json 取自 standard_payload_json 中该节点对应的 ai_generated_content 对象**（即把模板中的模拟AI数据拷贝到节点实例的配置中），status=0（未开始）
3. WHEN Node_Instance 创建完成后, THE Demo_Data_Seeder SHALL 为"26软件工程1班"中的每个学生（通过 biz_student.class_id 匹配）创建一条 Participation 记录，字段设置为：task_id 关联新建任务，student_id 关联该学生，status=0（未开始），total_score 为 NULL
4. WHEN Participation 创建完成后, THE Demo_Data_Seeder SHALL 为每个学生的每个 Node_Instance 创建对应的 Node_Progress 记录，字段设置为：participation_id 关联该学生的 Participation 记录，node_instance_id 关联对应节点实例，status=0（未开始），entered_at 为 NULL，is_forced_complete=0
5. IF "26软件工程1班"中无学生记录（biz_student.class_id 匹配结果为空），THEN THE Demo_Data_Seeder SHALL 跳过 Participation 和 Node_Progress 的创建并输出警告信息指明该班级无学生

### Requirement 4: 学生实训入口与阶段预览

**User Story:** As a 学生, I want to 进入实训后预览到实训的所有阶段信息, so that 我可以了解整体实训流程和进度。

#### Acceptance Criteria

1. WHEN 学生进入实训详情页面时, THE Student_Training_System SHALL 调用实训任务总览接口获取该学生的参与状态、实训任务信息和所有阶段及节点进度数据，并在数据加载期间显示加载状态指示
2. WHEN 实训数据加载完成后, THE Student_Training_System SHALL 按 sort_num 升序展示所有阶段，每个阶段显示阶段名称、包含的节点列表（节点名称和节点类型）以及阶段内节点总数
3. WHILE 学生处于阶段预览界面, THE Student_Training_System SHALL 根据阶段内各节点的进度状态计算并显示每个阶段的完成状态：当阶段内所有必修节点（is_required=true）状态均为"已完成"时标记该阶段为"已完成"；当阶段内存在至少一个节点状态为"进行中"或"已完成"但未满足全部必修节点完成时标记为"进行中"；否则标记为"未开始"
4. WHEN 学生点击某个阶段时, IF 该阶段按 sort_num 顺序排列的所有前序阶段均已标记为"已完成", THEN THE Student_Training_System SHALL 允许学生进入该阶段
5. IF 学生点击的阶段存在未完成的前序阶段, THEN THE Student_Training_System SHALL 阻止进入并显示提示信息指明哪个前序阶段尚未完成
6. IF 实训数据加载失败, THEN THE Student_Training_System SHALL 显示错误提示信息并提供重试操作入口

### Requirement 5: 阶段顺序控制与解锁

**User Story:** As a 学生, I want to 按顺序完成实训阶段, so that 实训流程有序推进且不会跳过关键步骤。

#### Acceptance Criteria

1. THE Student_Training_System SHALL 按 Phase 的 sort_num 字段升序确定阶段执行顺序，sort_num 最小的阶段（第一个阶段）在学生进入实训时默认 is_unlocked=true
2. WHEN 学生完成某个阶段内所有 is_required=true 的节点（对应 wf_student_node_progress.status=2）后, THE Student_Training_System SHALL 将该阶段标记为 is_complete=true，并将 sort_num 顺序中紧邻的下一个阶段设为 is_unlocked=true
3. IF 某个阶段内不存在任何 is_required=true 的节点, THEN THE Student_Training_System SHALL 在该阶段所有前置阶段完成后自动将其标记为 is_complete=true 并解锁下一阶段
4. WHILE 某个阶段处于 is_unlocked=false 状态, THE Student_Training_System SHALL 在阶段列表界面对该阶段显示锁定标识（视觉上不可交互），且禁止学生点击进入该阶段的任何节点
5. IF 学生尝试通过路由直接访问属于未解锁阶段的节点页面, THEN THE Student_Training_System SHALL 通过 vue-router 导航守卫拦截请求，并重定向学生到当前 is_unlocked=true 且 is_complete=false 的阶段对应的节点页面
6. IF 所有阶段均已标记为 is_complete=true, THEN THE Student_Training_System SHALL 允许学生访问任意阶段的节点（不再施加锁定限制）

### Requirement 6: 节点内动态页面加载与流转

**User Story:** As a 学生, I want to 在每个阶段内按编排顺序依次完成各节点的学习任务, so that 我可以按照教师设计的流程进行实训。

#### Acceptance Criteria

1. WHEN 学生进入某个阶段后, THE Node_Page_Resolver SHALL 根据该阶段内节点列表的排列顺序（sort_num 升序），定位第一个 status 不为 2（已完成）的节点，并根据其 node_type 调用 resolveNodePage 动态加载对应的 Vue 异步组件
2. WHEN 节点类型为 theory_class 时, THE Node_Page_Resolver SHALL 加载 TheoryClassView 组件（学生版），并将 Node_Instance 的 config_json 中的 slides 数据作为 props 传递给组件，组件直接从 config_json 读取数据渲染幻灯片（不调用教师端的 getTemplatePreview 接口）
3. WHEN 节点类型为 coding_class 时, THE Node_Page_Resolver SHALL 加载 CodingLab 组件（学生版），并将 Node_Instance 的 config_json 中的 coding_task 数据（含 description、code_template、hints、test_cases、language）作为 props 传递给组件，组件直接从 config_json 读取数据渲染编码界面（不调用教师端的 getTemplatePreview 接口）
4. IF resolveNodePage 对给定的 node_type 返回 null（未注册类型）, THEN THE Node_Page_Resolver SHALL 显示一个包含该 node_type 名称的错误提示信息，且不执行节点进入操作
5. WHEN 学生完成当前节点的学习任务（节点组件触发 complete 事件）后, THE Student_Training_System SHALL 调用 completeNode 将该节点的 Node_Progress status 更新为 2（已完成），并在同阶段节点列表中按 sort_num 升序自动导航到下一个 status 不为 2 的节点
6. WHEN 当前阶段内所有必修节点（is_required=true）的 status 均为 2 时, THE Student_Training_System SHALL 显示阶段完成提示，并在 3 秒内提供返回阶段预览页面（StartPortal）的导航按钮
7. THE Node_Page_Resolver SHALL 为以下所有节点类型维护页面组件映射：start、resource_read、theory_class、coding_class、learning_survey、homework、exam、mindmap_preview、mindmap_draw、knowledge_eval、student_peer_review、grouping、task_distribute、req_upload、plan_upload、inter_group_review、teacher_eval、end
8. THE Student_Training_System SHALL 提供学生端专用的实训数据获取接口，该接口根据学生的 participation_id 返回所有 Node_Instance 及其 config_json（含模拟AI内容），前端从此接口获取数据后按节点类型分发给对应组件

### Requirement 7: 学生实训进度持久化

**User Story:** As a 学生, I want to 我的实训进度被实时保存, so that 我可以随时退出并在下次登录时从上次中断的位置继续。

#### Acceptance Criteria

1. WHEN 学生进入某个状态为未开始（status=0）或进行中（status=1）的节点时, THE Student_Training_System SHALL 创建或更新该学生的 Node_Progress 记录，将 status 设为进行中（status=1）并将 entered_at 设为当前时间
2. IF 学生进入的节点 status 已为已完成（status=2）或已跳过（status=3）, THEN THE Student_Training_System SHALL 返回该节点现有的 Node_Progress 记录且不修改任何字段
3. WHEN 学生完成某个节点时, THE Student_Training_System SHALL 更新 Node_Progress 的 status 为已完成（status=2），记录 exited_at 为当前时间，并将 stay_duration_seconds 设为已有累计停留秒数加上本次进入（entered_at）到当前时间的差值
4. WHEN 学生完成某个节点时, THE Student_Training_System SHALL 将该学生的 Student_Activity_State 的 current_node_instance_id 设为 null
5. WHEN 学生进入某个节点时, THE Student_Training_System SHALL 更新或创建 Student_Activity_State 记录，将 current_node_instance_id 设为当前节点实例ID，并将 updated_at 设为当前时间
6. WHEN 学生重新进入实训时, IF Student_Activity_State 的 current_node_instance_id 不为 null, THEN THE Student_Training_System SHALL 将学生定位到该 current_node_instance_id 对应的节点及其所属阶段
7. WHEN 学生重新进入实训时, IF Student_Activity_State 的 current_node_instance_id 为 null 或 Student_Activity_State 记录不存在, THEN THE Student_Training_System SHALL 根据 Node_Progress 记录将学生定位到当前解锁阶段的第一个未完成节点

### Requirement 8: 实训状态流转

**User Story:** As a 学生, I want to 实训状态随我的操作自动更新, so that 系统能准确反映我的实训进度。

#### Acceptance Criteria

1. WHEN 学生点击"开始实训"且当前 Participation 的 status 为 0（未开始）时, THE Student_Training_System SHALL 将 Participation 的 status 更新为 1（进行中），记录 updated_at 为当前时间，并创建 Student_Activity_State 记录（current_node_instance_id 初始为 null）
2. WHEN 学生完成所有阶段中所有 is_required=true 的节点（即每个必须节点的 Node_Progress status 均为 2）后, THE Student_Training_System SHALL 将 Participation 的 status 更新为 2（已提交）并记录 updated_at 为当前时间
3. WHILE Participation 的 status 为 1（进行中）, THE Student_Training_System SHALL 在学生工作台的实训列表中显示"进行中"标签和当前进度百分比（计算公式：已完成节点数 / 总节点数 × 100，四舍五入取整，范围 0-100）
4. IF 学生尝试开始一个 status 已为 1（进行中）的实训, THEN THE Student_Training_System SHALL 直接进入实训工作台并恢复到上次中断的位置，不重复创建 Student_Activity_State 记录
5. IF 学生尝试开始一个 status 已为 2（已提交）的实训, THEN THE Student_Training_System SHALL 显示提示信息指明实训已完成，并展示包含总分和各阶段完成状态的总结页面
