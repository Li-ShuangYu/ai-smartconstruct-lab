# AI SmartConstruct Lab - 前端项目

基于 Vue 3 + TypeScript + Vite 的智能教学实训平台前端应用。

---

## 📋 项目概述

本项目是 AI SmartConstruct Lab 的前端部分，采用 Vue 3 Composition API 和 TypeScript 开发，为教师和学生提供沉浸式的实训体验。

### 技术栈

| 技术 | 说明 | 版本 |
|------|------|------|
| Vue 3 | 渐进式JavaScript框架 | 3.4+ |
| TypeScript | JavaScript超集 | 5.x |
| Vite | 新一代前端构建工具 | 8.x |
| Vue Router | Vue官方路由管理 | 4.x |
| Pinia | Vue状态管理 | 2.x |
| Naive UI | Vue 3组件库 | 2.x |
| Vue Flow | 流程图/节点编排组件 | 11.x |
| NProgress | 路由进度条 | - |

---

## 📁 目录结构

```
frontend-vue/
├── public/                     # 静态资源（直接拷贝至dist）
│   ├── favicon.ico
│   └── 1.ico
├── src/                        # 源代码目录
│   ├── assets/                 # 静态资源
│   │   ├── styles/            # 全局样式
│   │   │   ├── reset.css      # 浏览器样式重置
│   │   │   ├── variables.css  # CSS变量（配色/间距/字号）
│   │   │   ├── layout.css    # 全局布局规范
│   │   │   ├── components.css # 通用组件样式
│   │   │   └── base.css       # 基础样式
│   │   ├── AIZG-Logo.png      # 品牌Logo
│   │   ├── auth-illustration.png # 登录页插图
│   │   ├── logo.svg           # SVG图标
│   │   └── main.css           # 主样式文件
│   │
│   ├── components/             # 组件目录
│   │   ├── icons/            # 图标组件
│   │   │   ├── IconCommunity.vue
│   │   │   ├── IconDocumentation.vue
│   │   │   ├── IconEcosystem.vue
│   │   │   ├── IconSupport.vue
│   │   │   └── IconTooling.vue
│   │   └── layout/           # 布局组件
│   │       ├── AuthLayout/   # 认证布局（登录/注册）
│   │       │   └── index.vue
│   │       └── WorkbenchLayout/ # 工作台布局
│   │           ├── index.vue         # 主布局入口
│   │           ├── Sidebar.vue       # 通用侧边栏
│   │           ├── TeacherHeader.vue # 教师顶部导航
│   │           ├── StudentHeader.vue # 学生顶部导航
│   │           ├── AdminHeader.vue   # 管理员顶部导航
│   │           ├── SuperHeader.vue   # 超级管理员顶部导航
│   │           └── AdminSidebar.vue  # 管理员侧边栏
│   │
│   ├── hooks/                  # 组合式函数
│   │   ├── useAuth.ts         # 权限认证相关
│   │   ├── useTraining.ts     # 实训相关
│   │   ├── useStepProgress.ts # 步进流程
│   │   ├── useTelemetry.ts    # 埋点上报
│   │   └── useAssistant.ts    # 智能助手
│   │
│   ├── router/                 # 路由配置
│   │   ├── index.ts           # 路由入口
│   │   ├── guard.ts          # 路由守卫
│   │   └── modules/          # 路由模块
│   │       ├── auth.ts        # 认证路由
│   │       ├── teacher.ts     # 教师路由
│   │       ├── student.ts     # 学生路由
│   │       ├── admin.ts       # 管理员路由
│   │       ├── training.ts    # 实训路由
│   │       └── homework.ts    # 作业路由
│   │
│   ├── services/              # 服务层
│   │   ├── api.ts            # Axios封装
│   │   ├── types/            # 类型定义
│   │   │   ├── auth.types.ts
│   │   │   ├── user.types.ts
│   │   │   ├── training.types.ts
│   │   │   └── homework.types.ts
│   │   └── modules/          # API服务
│   │       ├── auth.service.ts
│   │       ├── user.service.ts
│   │       ├── training.service.ts
│   │       ├── homework.service.ts
│   │       └── assistant.service.ts
│   │
│   ├── stores/                # 状态管理
│   │   ├── index.ts          # Store入口
│   │   └── modules/          # Store模块
│   │       ├── auth.store.ts
│   │       ├── app.store.ts
│   │       ├── training.store.ts
│   │       ├── homework.store.ts
│   │       └── assistant.store.ts
│   │
│   ├── utils/                # 工具函数
│   │   ├── format.ts        # 格式化工具
│   │   ├── storage.ts       # 本地存储
│   │   ├── validate.ts      # 校验函数
│   │   └── telemetry.ts     # 埋点工具
│   │
│   ├── views/                # 页面组件
│   │   ├── auth/            # 认证页面
│   │   ├── teacher/         # 教师端页面
│   │   ├── student/         # 学生端页面
│   │   ├── admin/           # 管理员端页面
│   │   ├── homework/        # 作业页面
│   │   ├── training/         # 实训页面
│   │   └── common/          # 公共页面
│   │
│   ├── rules/               # 开发规范
│   │   ├── .cursorrules    # Cursor AI规则
│   │   ├── SKILL.md        # 代码生成技能规范
│   │   └── PROMPT.md       # 组件生成提示词
│   │
│   ├── App.vue             # 根组件
│   ├── main.ts             # 应用入口
│   └── env.d.ts            # TypeScript环境声明
│
├── package.json
├── tsconfig.json           # TypeScript配置
├── tsconfig.app.json       # 应用TypeScript配置
├── tsconfig.node.json      # 节点TypeScript配置
├── vite.config.ts         # Vite配置
├── index.html              # HTML入口
├── env.d.ts               # 环境声明
└── README.md              # 项目说明
```

---

## 🎯 路由系统

### 路由架构

```
/                           # 根路径，重定向到登录页
├── /auth                   # 认证模块
│   ├── /auth/login        # 登录页
│   └── /auth/register     # 注册页
│
├── /teacher               # 教师工作台（带侧边栏）
│   ├── /teacher/workbench           # 工作台概览
│   ├── /teacher/training-manage    # 实训管理
│   ├── /teacher/class-course-manage # 班级课程管理
│   ├── /teacher/evaluation         # 评估分析
│   └── /teacher/profile           # 用户中心
│
├── /teacher/* (沉浸式，无侧边栏)   # 教师沉浸式页面
│   ├── /teacher/training-create    # 实训编排
│   ├── /teacher/teacher-live-monitor # 直播监控
│   ├── /teacher/training-publish   # 发布实训
│   ├── /teacher/class-competency/:studentId  # 班级能力分析
│   └── /teacher/student-competency/:studentId # 学生能力分析
│
├── /student               # 学生学习空间（带侧边栏）
│   ├── /student/workbench          # 工作台
│   ├── /student/student-cabin/:id # 学生舱位
│   ├── /student/training-detail/:id # 实训详情
│   ├── /student/my-class          # 我的班级
│   ├── /student/courselist        # 课程列表
│   ├── /student/notifications     # 消息通知
│   └── /student/profile           # 个人中心
│
├── /admin                 # 管理员后台
│   ├── /admin/teacher     # 教师管理
│   ├── /admin/student     # 学生管理
│   ├── /admin/org         # 机构管理
│   ├── /admin/course      # 课程管理
│   ├── /admin/menu        # 菜单管理
│   ├── /admin/node        # 节点管理
│   ├── /admin/template    # 模板管理
│   ├── /admin/question    # 题目管理
│   ├── /admin/ticket      # 工单管理
│   ├── /admin/monitor     # 服务监控
│   └── /admin/audit       # 审计日志
│
└── /:pathMatch(.*)*       # 404页面
```

### 路由守卫

路由守卫在 `router/guard.ts` 中实现，主要功能：

1. **进度条**：使用 NProgress 显示页面加载进度
2. **角色注入**：根据路径自动注入 `meta.role`（student/teacher/admin/auth）
3. **标题设置**：自动设置页面标题，格式为 `{页面标题} - AI学苑`

---

## 🎨 布局系统

### AuthLayout - 认证布局

用于登录/注册页面，采用左右分栏设计：

- **左侧**：品牌展示区，包含Logo、标题、副标题和背景插图
- **右侧**：登录/注册表单区域，毛玻璃效果背景
- **底部**：版权信息

### WorkbenchLayout - 工作台布局

用于教师/学生/管理员的工作台页面，结构如下：

```
┌─────────────────────────────────────────────┐
│  Header (根据角色显示不同导航)              │
├─────────┬───────────────────────────────────┤
│         │                                   │
│ Sidebar │         Main Content              │
│         │                                   │
│         │                                   │
└─────────┴───────────────────────────────────┘
```

#### Header 组件

| 组件 | 用途 |
|------|------|
| TeacherHeader | 教师顶部导航 |
| StudentHeader | 学生顶部导航 |
| AdminHeader | 管理员顶部导航 |
| SuperHeader | 超级管理员顶部导航 |

#### Sidebar 组件

| 组件 | 用途 |
|------|------|
| Sidebar | 教师/通用侧边栏 |
| AdminSidebar | 管理员侧边栏 |

#### 沉浸式模式

当路由 meta 设置 `hideSidebar: true` 时：
- 不显示 Header 和 Sidebar
- 全屏展示内容
- 适用于：实训编排、直播监控、发布实训等沉浸式场景

---

## 📄 页面详解

### 认证模块 (auth)

#### Login.vue
- **路径**：`/auth/login`
- **功能**：用户登录，支持学生/教师/管理员三种角色切换登录
- **技术实现**：
  - Vue 3 Composition API + `<script setup>` 语法
  - 响应式数据管理（ref、computed）
  - 路由编程式导航（useRouter + router.push）
  - CSS Scoped 样式隔离
  - 角色切换状态管理
- **UI特性**：
  - **角色分段控制器**：三个可点击角色卡片（学生端/教师端/管理端），带SVG图标
  - **动态账号占位符**：根据角色切换显示不同提示文字（学号/工号/管理员账号）
  - **表单选项**：记住我复选框、忘记密码链接
  - **注册入口**：底部跳转到注册页面链接
- **布局**：AuthLayout 左右分栏结构，左侧品牌展示区，右侧登录表单

#### Register.vue
- **路径**：`/auth/register`
- **功能**：用户注册账号
- **技术实现**：表单校验、路由跳转

### 教师端 (teacher)

#### Workbench.vue
- **路径**：`/teacher/workbench`
- **功能**：教师工作台首页，仪表盘式数据展示
- **技术实现**：
  - Vue 3 Composition API + `<script setup>`
  - 响应式布局（flex、grid）
  - 路由编程式导航
  - 模拟数据驱动展示
- **UI特性**：
  - **Header区域**：问候语 + 今日任务统计卡片（本周活跃学生、待办任务）
  - **快捷入口网格**：三个主操作卡片（创建实训/开启实训/综合评价），带图标和描述
  - **历史实训列表**：状态标签（已就绪/进行中/已结束）、时间信息、参与人数统计
  - **卡片悬浮效果**：hover transform 动画
- **业务场景**：密码学多智能体协同实训管理

#### TrainingCreate.vue
- **路径**：`/teacher/training-create`
- **功能**：可视化实训流程编排器，拖拽式节点工作流设计
- **技术实现**：
  - **Vue Flow**：基于 @vue-flow/core 的可视化工作流编辑器
  - **拖拽API**：HTML5 Drag & Drop API（draggable、ondragstart、ondrop）
  - **坐标转换**：screenToFlowCoordinate 将页面坐标转换为流程图坐标
  - **节点组件**：markRaw 动态组件注册（避免响应式开销）
  - **历史记录系统**：撤回/恢复栈（historyStack、redoStack），最多保存30步
  - **连线校验**：checkValidConnection 防止自环和类型错误连接
  - **状态管理**：Pinia store（useTrainingStore）管理 nodes 和 edges
- **UI特性**：
  - **左侧组件仓库面板**：可折叠侧边栏，分类展示节点（LLM调用/代码执行/条件分支等）
  - **顶部工具栏**：撤回/恢复按钮、草稿暂存、发布实训
  - **Canvas画布**：VueFlow 实例，支持缩放、拖拽、节点连接
  - **Background背景**：点阵图案（pattern-color, gap, size）
  - **Controls控件**：右下角最小化导航控件
  - **成功弹窗**：发布后的模态框 + 倒计时自动跳转
- **布局**：MaterialSidebar + CanvasContainer + ModalOverlay 三栏结构
- **业务场景**：编排 SM4 密钥配置、无人机抗重放攻击等实训流程

#### TrainingManage.vue
- **路径**：`/teacher/training-manage`
- **功能**：实训资源全生命周期管理（模板/待发布/进行中/历史）
- **技术实现**：
  - Vue 3 Composition API + TypeScript 类型断言
  - Tab 状态切换（template/pending/ongoing/history）
  - 分页逻辑（totalPages、handlePageChange）
  - 路由编程跳转
  - Transition 组件实现 Tab 切换动画
- **UI特性**：
  - **Tab导航栏**：四个状态切换标签，当前选中项高亮
  - **数据表格**：Grid 布局表头 + 表体，支持不同操作按钮
  - **状态标签**：根据状态显示不同颜色标签（已就绪/进行中/已结束）
  - **分页控件**：上一页/下一页 + 页码数字按钮 + 共多少项信息
  - **空状态**：无数据时显示友好提示
- **操作流程**：编辑模板 → 创建实训任务 → 开启实训 → 进入实训监控 → 结束实训 → 查看历史

#### TrainingPublish.vue
- **路径**：`/teacher/training-publish`
- **功能**：实训任务发布，选择班级和学生下发
- **技术实现**：表单配置、班级选择组件

#### EvaluationDashboard.vue
- **路径**：`/teacher/evaluation`
- **功能**：实训评价总览，多维数据统计
- **技术实现**：
  - Vue 3 Composition API
  - 计算属性过滤（filteredProjects）
  - 搜索功能（searchQuery）
  - 路由编程跳转（useRouter）
- **UI特性**：
  - **搜索栏**：输入框过滤项目名称或分类
  - **项目卡片网格**：每个项目显示分类标签、日期、平均分、完成率、技能标签
  - **指标展示**：平均分、完成率数字高亮
  - **技能标签**：如 SM4、时间戳验证、Dilithium 等密码学相关标签
  - **操作按钮**：查看详细报告跳转
- **业务场景**：评价学生密码学实训能力，如无人机通信抗重放攻击、国密SM3杂凑分析等

#### EvaluationManage.vue
- **路径**：`/teacher/evaluation-manage`
- **功能**：评价任务管理，创建和管理评价活动
- **技术实现**：表单编辑、状态管理

#### ClassCompetencyProfile.vue
- **路径**：`/teacher/class-competency/:studentId`
- **功能**：班级实训总结分析，能力雷达图
- **技术实现**：
  - 路由参数获取（useRoute.params）
  - 图表可视化（雷达图/柱状图）
  - 数据聚合分析

#### StudentCompetencyProfile.vue
- **路径**：`/teacher/student-competency/:studentId`
- **功能**：学生个人实训能力画像
- **技术实现**：
  - 单学生数据查询
  - 能力维度展示

#### ClassCourseManage.vue
- **路径**：`/teacher/class-course`
- **功能**：班级课程关联管理
- **技术实现**：课程分配、班级数据管理

#### TeacherLiveMonitor.vue
- **路径**：`/teacher/live-monitor`
- **功能**：实训现场实时监控
- **技术实现**：实时数据刷新、WebSocket 连接

#### UserProfile.vue
- **路径**：`/teacher/profile`
- **功能**：教师个人信息管理
- **技术实现**：表单编辑、个人信息展示

### 学生端 (student)

#### Workbench.vue
- **路径**：`/student/workbench`
- **功能**：学生学习工作台，AI助教交互 + 实训任务列表
- **技术实现**：
  - Vue 3 Composition API + `<script setup>` 语法
  - 分页组件集成（Pagination.vue）
  - 计算属性过滤（tabFilteredList、paginatedList）
  - 响应式数据处理
- **UI特性**：
  - **背景效果**：动态光晕背景（bg-glow、radial-gradient）
  - **Hero区域**：品牌Logo + AI对话气泡 + 快捷建议标签（解释抗重放机制/SM4编程第一步/查看我的进度）
  - **AI对话界面**：输入框 + 发送按钮，支持语音图标
  - **玻璃态卡片**：backdrop-filter blur 毛玻璃效果，悬浮动画
  - **Tab导航**：全部实训/进行中/已结束切换
  - **实训卡片网格**：
    - 课程标签（国密算法、密码学等）
    - 状态指示器（ongoing/not_started/ended）
    - 进度条（渐变色填充）
    - 截止日期展示
    - 继续/查看按钮
  - **自定义分页**：融入玻璃态设计风格
  - **底部状态栏**：版本信息 + 更新说明
- **业务场景**：密码学多智能体协同实训教室，沉浸式学习体验

#### StudentCabin.vue
- **路径**：`/student/student-cabin/:id`
- **功能**：沉浸式实训舱，WebIDE风格，左侧AI指导 + 右侧代码编辑
- **技术实现**：
  - 全屏深色主题布局（100vh）
  - 左右分栏结构（flex）
  - 聊天消息流渲染（v-for、动态class）
  - 响应式高度计算
- **UI特性**：
  - **左侧AI助手面板**（白色背景，宽度400px）：
    - Header：标题 "理论指导伙伴 (LangChain 驱动)"
    - 聊天消息流：AI气泡（灰色背景）、用户气泡（紫色背景）、思考状态提示
    - 输入区域：输入框 + 发送按钮
  - **右侧IDE容器**（深色背景，flex: 1）：
    - IDE Header：文件名显示 + 编译验证按钮（绿色）
    - 代码编辑器区：等宽字体，预设代码模板（SM4时间戳校验）
    - 底部终端区：暗黑色背景，绿色命令行风格输出
- **布局**：ai-assistant (400px) + ide-container (flex: 1)
- **业务场景**：无人机抗重放攻击的 SM4 密钥配置实训，AI 实时指导编程

#### TrainingDetail.vue
- **路径**：`/student/training-detail/:id`
- **功能**：实训详情查看（只读模式）
- **技术实现**：路由参数获取实训ID，详情数据展示

#### MyClass.vue
- **路径**：`/student/my-class`
- **功能**：我的班级、班级成员查看
- **UI特性**：班级信息卡片、成员列表展示

#### CourseList.vue
- **路径**：`/student/courselist`
- **功能**：选修课程列表，搜索 + 分页
- **技术实现**：
  - Vue 3 Composition API + `<script setup>`
  - 计算属性过滤（filteredList）
  - 分页状态管理（currentPage、pageSize）
  - Pagination 组件集成
- **UI特性**：
  - **页头**：标题 + 搜索框（实时过滤课程名或教师名）
  - **卡片网格**：每张卡片显示课程代码、类型标签、名称、教师、学分
  - **操作按钮**：进入课程区
  - **空状态**：无匹配数据显示友好提示
- **业务场景**：密码系统设计、UAV通信加密专论等课程选修

#### MyTraining.vue
- **路径**：`/student/my-training`
- **功能**：我的实训记录，学习历程追踪
- **UI特性**：实训历史列表、时间线展示

#### MyHomework.vue
- **路径**：`/student/my-homework`
- **功能**：我的作业列表，提交状态查看
- **UI特性**：作业列表、批改状态标签

#### MySubmission.vue
- **路径**：`/student/my-submission`
- **功能**：我的提交记录，代码和答案追踪
- **UI特性**：提交历史、版本对比

#### Notifications.vue
- **路径**：`/student/notifications`
- **功能**：系统通知、个人消息中心
- **UI特性**：消息列表、时间轴、未读标记

#### UserProfile.vue
- **路径**：`/student/profile`
- **功能**：个人中心，信息编辑
- **UI特性**：个人信息展示、编辑表单

### 管理员端 (admin)

#### TeacherManage.vue
- **路径**：`/admin/teacher`
- **功能**：教师账号管理（增删改查）
- **技术实现**：
  - 复用 AdminStandardPage 通用组件
  - TypeScript 列定义接口
  - 模拟数据列表
- **UI特性**：
  - **表格列**：教职工号、姓名、所属院系、账号状态
  - **状态标签**：账号状态标签（正常/禁用等）
  - **操作按钮**：编辑、删除
- **布局**：使用 AdminStandardPage 统一后台管理页面样式

#### StudentManage.vue
- **路径**：`/admin/student`
- **功能**：学生账号管理
- **技术实现**：AdminStandardPage 组件复用，数据类型定义
- **UI特性**：学生表格、批量操作支持

#### OrgManage.vue
- **路径**：`/admin/org`
- **功能**：组织机构管理（学校/院系）
- **技术实现**：树形数据结构、递归组件渲染
- **UI特性**：树形结构展示、增删改操作

#### CourseManage.vue
- **路径**：`/admin/course`
- **功能**：课程模板管理
- **技术实现**：CRUD 操作、数据持久化模拟
- **UI特性**：课程列表、类型标签

#### MenuManage.vue
- **路径**：`/admin/menu`
- **功能**：系统菜单配置
- **技术实现**：菜单树编辑、层级关系管理
- **UI特性**：菜单树编辑界面、拖拽排序

#### NodeManage.vue
- **路径**：`/admin/node`
- **功能**：节点/题库管理，实训原子组件配置
- **技术实现**：节点类型定义、配置界面
- **UI特性**：节点列表、属性编辑

#### TemplateDashboard.vue
- **路径**：`/admin/template`
- **功能**：实训模板管理
- **技术实现**：模板列表、分类管理
- **UI特性**：模板卡片、操作按钮

#### QuestionDashboard.vue
- **路径**：`/admin/question`
- **功能**：题目管理
- **技术实现**：题目 CRUD、分类标签
- **UI特性**：题目编辑、题目库列表

#### TicketManage.vue
- **路径**：`/admin/ticket`
- **功能**：工单/问题反馈处理
- **技术实现**：工单状态流转、处理流程
- **UI特性**：工单列表、状态标签

#### ServiceMonitor.vue
- **路径**：`/admin/monitor`
- **功能**：系统服务监控
- **技术实现**：状态指标采集、前端可视化
- **UI特性**：状态指示、图表展示

#### AuditLog.vue
- **路径**：`/admin/audit`
- **功能**：操作审计日志
- **技术实现**：日志查询、时间范围筛选
- **UI特性**：日志列表、时间线

#### AdminStandardPage.vue
- **路径**：（通用组件，被各管理页面复用）
- **功能**：统一后台管理页面模板，减少重复代码
- **技术实现**：
  - Vue 3 defineProps 接收配置参数
  - 搜索过滤（filteredData computed）
  - 分页状态管理
  - defineEmits 定义事件
  - CSS Grid 布局
- **Props配置**：
  - title：页面标题
  - columns：列定义数组（key, label, isTag）
  - data：数据源数组
  - gridLayout：表格列宽布局
- **Events**：
  - add：新增数据
  - edit：编辑数据
  - delete：删除数据
- **UI特性**：
  - **工具栏**：搜索框 + 新增按钮
  - **表头行**：Grid 布局，列定义
  - **数据行**：v-for 渲染，支持状态标签
  - **操作列**：编辑/删除按钮
  - **空状态**：无数据显示友好提示
  - **分页组件**：Pagination 复用

### 作业模块 (homework)

#### ExamPage.vue
- **路径**：`/homework/exam`
- **功能**：在线考试页面
- **技术实现**：
  - 计时器功能（setInterval）
  - 题目状态管理
  - 答案暂存机制
- **UI特性**：考试界面、进度显示、计时器

#### HomeworkDetail.vue
- **路径**：`/homework/detail`
- **功能**：作业详情查看与批改
- **UI特性**：作业内容展示、批改状态

#### MindMapPractice.vue
- **路径**：`/homework/mindmap`
- **功能**：思维导图练习
- **技术实现**：
  - Vue Flow 封装实现思维导图
  - 节点编辑、连线
- **UI特性**：节点可视化、拖拽编辑

### 实训模块 (training)

#### CodeTraining.vue
- **路径**：`/training/code`
- **功能**：编码实训舱
- **技术实现**：代码编辑器集成
- **UI特性**：Monaco Editor 或类似编辑器

#### TheoryTraining.vue
- **路径**：`/training/theory`
- **功能**：理论实训（选择题/填空题）
- **UI特性**：题目展示、答题卡

### 学生实训流程 (studentTraining)

#### StudentGroupChoose.vue
- **路径**：`/training/student-training/group-choose`
- **功能**：课题攻坚方向选择，学生组队入口
- **技术实现**：
  - Vue 3 Composition API + Transition 动画组件
  - 响应式状态管理（selectedGroupId、isLoading）
  - 本地存储操作（getColorFromStorage）
  - 路由编程式导航（goToTaskSelect）
- **UI特性**：
  - **页头区域**：标题 + 确认按钮（根据选择状态动态启用/禁用）
  - **小组卡片网格**：4宫格玻璃态卡片，支持 hover 效果和选中状态
  - **卡片内容**：组号标签（带主题色）、算法名称、人物画像、性格标签、做事风格描述
  - **选中状态**：卡片边框高亮 + 对勾图标显示
  - **成功弹窗**：组队成功后展示模态框，显示团队成员列表
  - **动画效果**：fade-in、fade-up、transition-group 列表动画
- **业务场景**：密码学研究方向选择（低功耗优化/侧信道防护/抗重放攻击/后量子算法）

#### StudentTaskSelect.vue
- **路径**：`/training/student-training/task-select`
- **功能**：需求分析 - 任务接收与选择
- **技术实现**：
  - Toast 提示组件（transition + v-if）
  - 等待接收动画状态管理（isReceiving）
  - 任务选择状态管理（selectedGroupId、hoveredGroup）
- **UI特性**：
  - **顶部Toast**：任务确认成功提示，带勾号图标
  - **状态指示器**：根据选择状态显示不同颜色
  - **等待接收遮罩**：加载动画 + 调试跳过按钮
  - **主线任务横幅**：玻璃态卡片展示核心任务描述
  - **支线任务4宫格**：可选择的任务方向卡片，带 AI 助教分析
  - **确认按钮**：根据选择状态动态变化
- **业务场景**：无人机通信加密系统全流程设计任务分配

#### StudentAiGenerate.vue
- **路径**：`/training/student-training/ai-generate`
- **功能**：AI 辅助代码生成控制台
- **技术实现**：
  - VSCode 风格布局（header + sidebar + editor + terminal）
  - 模拟硬件遥测数据（mockCpu、mockRam）
  - 雷达扫描动画效果
  - 代码高亮展示
  - 终端日志实时输出
- **UI特性**：
  - **顶部状态栏**：设备连接状态指示（ONLINE/WAITING）
  - **左侧面板**：
    - AI 任务下发控制台：显示当前提示词，一键生成 ROS 代码
    - 环境与硬件遥测：CPU/内存进度条、雷达扫描动画
  - **右侧面板**：
    - 代码编辑器：仿 VSCode 标签页，显示 Python ROS 代码
    - 终端面板：命令行风格输出日志
  - **模态遮罩**：AI 诊断中显示加载动画
- **业务场景**：冰达 NanoCar 智能调试，一键生成巡线与图传代码

#### StudentDebug.vue
- **路径**：`/training/student-training/debug`
- **功能**：代码调试与闭环验证
- **技术实现**：
  - 代码差异高亮（diffType: added/removed）
  - 多标签页编辑器（仿 VSCode）
  - 终端日志实时滚动
  - 错误提示与修复建议
- **UI特性**：
  - **顶部提示条**：成功/错误状态提示（fade-down 动画）
  - **编辑器主体**：
    - Mac 风格窗口控制按钮
    - 面包屑导航路径
    - 代码差异条（接受/拒绝变更）
    - 行号 + 差异标记（绿色新增/红色删除）
  - **终端面板**：多标签（问题/输出/调试控制台/终端）
- **业务场景**：ROS 代码调试，处理权限错误、订阅失败等问题

#### StudentSchemeUpload.vue
- **路径**：`/training/student-training/scheme-upload`
- **功能**：方案文档上传与提交
- **技术实现**：文件上传、进度追踪、提交确认

#### StudentSchemeDetail.vue
- **路径**：`/training/student-training/scheme-detail`
- **功能**：各组方案互评详情
- **技术实现**：
  - 多组方案切换（prevGroup/nextGroup）
  - 评分状态管理（allReviewsSubmitted）
  - 动态主题色获取（getHexColor）
- **UI特性**：
  - **12列栅格布局**：基础信息(3列) + 架构展示(5列) + 互评面板(4列)
  - **左侧信息卡**：方案代号、副标题、目标硬件、核心算法列表
  - **中间架构区**：系统架构层级展示、安全流程图
  - **右侧互评区**：评分表单、评语输入、提交状态
- **业务场景**：学生小组方案互评打分

#### StudentTaskSplit.vue
- **路径**：`/training/student-training/task-split`
- **功能**：任务分工与协作
- **技术实现**：任务分配、角色分工、进度跟踪

#### StudentRobotDebug.vue
- **路径**：`/training/student-training/robot-debug`
- **功能**：机器人调试界面
- **技术实现**：硬件状态监控、调试工具集成

#### StudentDeploy.vue
- **路径**：`/training/student-training/deploy`
- **功能**：代码部署到硬件
- **技术实现**：部署流程、状态反馈

#### StudentMyScoreResult.vue
- **路径**：`/training/student-training/score-result`
- **功能**：成绩与评价结果查看
- **技术实现**：成绩展示、评价详情

### 教师实训流程 (teacherTraining)

#### TeacherDemandSplit.vue
- **路径**：`/training/teacher-training/demand-split`
- **功能**：需求分析与分组展示（教师端）
- **技术实现**：
  - 4组并行展示（2x2 网格布局）
  - 加载进度模拟（progress + delay）
  - 响应式状态管理（reactive）
- **UI特性**：
  - **2x2 卡片网格**：每组一个玻璃态卡片，顶部彩色边框区分
  - **加载动画**：AI 需求深度推演中进度条
  - **卡片内容**：
    - 主线需求：通信加密设计
    - 支线需求：各组专项方向
    - AI 需求分类与资料推送
    - 预选方案与分工说明
- **业务场景**：教师查看各组需求分析结果，确认任务分配

#### TeacherTaskSplit.vue
- **路径**：`/training/teacher-training/task-split`
- **功能**：教师任务分配与队歌生成
- **技术实现**：
  - 队歌生成状态管理（isGeneratingSongs、allSongsGenerated）
  - 音乐播放控制（playMusic）
  - 音频可视化效果（music-wave-container）
- **UI特性**：
  - **生成按钮**：状态切换（生成中/已完成）
  - **音乐播放**：各组独立播放/暂停按钮
  - **音乐波形动画**：播放时显示跳动柱状图
- **业务场景**：教师为各组生成队歌，增强团队凝聚力

#### TeacherSchemeSplit.vue
- **路径**：`/training/teacher-training/scheme-split`
- **功能**：方案分屏评审（教师端）
- **技术实现**：
  - 上传状态监听（uploadedStates）
  - 自动监听机制
  - 方案导出功能
- **UI特性**：
  - **等待上传状态**：加载动画 + 逐字显示文字
  - **已上传状态**：方案文档展示、文件预览
  - **架构评审 Agent 按钮**：等待所有组上传后启用
  - **自动监听提示**：显示监听中状态
- **业务场景**：教师实时监控学生方案提交状态

#### TeacherAiEvaluate.vue
- **路径**：`/training/teacher-training/ai-evaluate`
- **功能**：方案 AI 评估（教师端）
- **技术实现**：
  - 深色主题布局（darkBg、panelBg）
  - AI 评估进度模拟（progress + scan-line 动画）
  - 悬停交互效果（hoveredGroup）
- **UI特性**：
  - **深色主题**：类似专业 IDE 的暗色风格
  - **四象限布局**：4个小组的评估报告卡片
  - **AI 推演动画**：扫描线效果 + 进度条
  - **评分展示**：AI 综合评分、各项指标
- **业务场景**：AI 专家系统对各组方案进行深度评估

#### TeacherSchemeDetail.vue
- **路径**：`/training/teacher-training/scheme-detail`
- **功能**：方案详情查看（教师端）
- **技术实现**：方案详情展示、评分管理

#### TeacherSimulation.vue
- **路径**：`/training/teacher-training/simulation`
- **功能**：仿真验证（教师端）
- **技术实现**：仿真参数配置、结果展示

#### TeacherStudentGroup.vue
- **路径**：`/training/teacher-training/student-group`
- **功能**：学生分组管理（教师端）
- **技术实现**：分组配置、成员管理

#### TeacherTaskPublish.vue
- **路径**：`/training/teacher-training/task-publish`
- **功能**：任务发布（教师端）
- **技术实现**：任务配置、发布流程

#### TeacherDemandSummary.vue
- **路径**：`/training/teacher-training/demand-summary`
- **功能**：需求汇总（教师端）
- **技术实现**：需求整理、报告生成

#### TeacherGroupScoreOverview.vue
- **路径**：`/training/teacher-training/group-score-overview`
- **功能**：各组成绩总览（教师端）
- **技术实现**：成绩统计、可视化展示

#### StudentTaskReceive.vue
- **路径**：`/training/teacher-training/student-task-receive`
- **功能**：学生任务接收状态监控（教师端）
- **技术实现**：实时状态监控、进度追踪

---

## 🔧 开发命令

```bash
# 安装依赖
npm install

# 开发服务器
npm run dev

# 类型检查
npm run type-check

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

---

## 📐 代码规范

### 组件规范

1. **使用 `<script setup lang="ts">`** 语法
2. **类型定义使用 interface**，不使用 type
3. **组件引用使用 PascalCase**
4. **Props 使用 defineProps 泛型定义**

### 样式规范

1. **使用 CSS 变量**：通过 `var(--xxx)` 引用全局变量
2. **Scoped CSS**：组件样式使用 `scoped` 属性
3. **禁止行内样式**：除动态计算值外

### 文件命名

| 类型 | 规范 | 示例 |
|------|------|------|
| Vue组件 | PascalCase | `TrainingCreate.vue` |
| TypeScript文件 | camelCase | `training.service.ts` |
| 样式文件 | kebab-case | `reset.css` |

### 四层架构

```
View (.vue) → Store (.store.ts) → Service (.service.ts) → Types (.types.ts)
```

- **View**：UI渲染、表单校验、调用Store
- **Store**：状态管理、loading状态、调用Service
- **Service**：网络请求、返回Promise
- **Types**：类型定义、接口契约

---

## 📄 许可证

MIT License
