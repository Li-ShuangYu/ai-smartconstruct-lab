# AI SmartConstruct Lab - 前端项目

基于 Vue 3 + TypeScript + Vite 的智能教学实训平台前端应用。

---

## 📋 项目概述

本项目是 AI SmartConstruct Lab 的前端部分，采用 Vue 3 Composition API 和 TypeScript 开发，为教师和学生提供沉浸式的实训体验。

### 技术栈

| 技术 | 说明 | 版本 |
|------|------|------|
| Vue 3 | 渐进式JavaScript框架 | 3.5.32 |
| TypeScript | JavaScript超集 | ~6.0.0 |
| Vite | 新一代前端构建工具 | 8.0.8 |
| Vue Router | Vue官方路由管理 | 5.0.6 |
| Pinia | Vue状态管理 | 3.0.4 |
| Naive UI | Vue 3组件库 | 2.44.1 |
| Vue Flow | 流程图/节点编排组件 | 1.48.2 |
| ECharts | 数据可视化图表库 | 6.0.0 |
| NProgress | 路由进度条 | 0.2.0 |
| Axios | HTTP客户端 | 1.15.2 |
| simple-mind-map | 思维导图组件 | 0.14.0-fix.2 |
| Font Awesome | 图标库 | 7.2.0 |

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
│   │   ├── audio/             # 音频资源
│   │   ├── AIZG-Logo.png      # 品牌Logo
│   │   ├── auth-illustration.png # 登录页插图
│   │   ├── logo.svg           # SVG图标
│   │   └── main.css           # 主样式文件
│   │
│   ├── components/             # 组件目录
│   │   ├── AIFloatingAssistant/  # AI浮动助手组件
│   │   │   └── AIFloatingAssistant.vue
│   │   ├── icons/            # 图标组件
│   │   │   ├── IconCommunity.vue
│   │   │   ├── IconDocumentation.vue
│   │   │   ├── IconEcosystem.vue
│   │   │   ├── IconSupport.vue
│   │   │   └── IconTooling.vue
│   │   └── layout/           # 布局组件
│   │       ├── AuthLayout/   # 认证布局（登录/注册）
│   │       │   └── index.vue
│   │       ├── TrainingLayout/ # 实训布局
│   │       │   ├── index.vue
│   │       │   ├── Sidebar/       # 侧边栏子组件
│   │       │   │   ├── StudentSidebar.vue
│   │       │   │   └── TeacherSidebar.vue
│   │       │   ├── StudentSidebar.vue
│   │       │   └── TeacherSidebar.vue
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
│   │       ├── training.ts    # 实训路由（学生端+教师端）
│   │       └── homework.ts    # 作业路由
│   │
│   ├── services/              # 服务层
│   │   ├── api.ts            # Axios封装
│   │   ├── types/            # 类型定义
│   │   │   ├── admin.types.ts
│   │   │   ├── auth.types.ts
│   │   │   ├── dashboard.types.ts
│   │   │   ├── homework.types.ts
│   │   │   ├── org.types.ts
│   │   │   ├── template.types.ts
│   │   │   ├── training.types.ts
│   │   │   └── user.types.ts
│   │   └── modules/          # API服务
│   │       ├── auth.service.ts        # 认证服务
│   │       ├── user.service.ts        # 用户服务
│   │       ├── training.service.ts    # 实训服务
│   │       ├── homework.service.ts    # 作业服务
│   │       ├── assistant.service.ts   # 智能助手服务
│   │       ├── org.service.ts         # 组织机构服务
│   │       ├── admin.service.ts       # 管理员服务
│   │       ├── student.service.ts     # 学生服务
│   │       ├── student-dashboard.service.ts  # 学生仪表盘服务
│   │       ├── teacher.service.ts     # 教师服务
│   │       ├── teacher-dashboard.service.ts  # 教师仪表盘服务
│   │       ├── teacher-template.service.ts    # 教师模板服务
│   │       └── teacher-question.service.ts    # 教师题目服务
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
│   │   ├── telemetry.ts     # 埋点工具
│   │   ├── training-flow.ts # 实训流程工具
│   │   └── websocket.ts     # WebSocket工具
│   │
│   ├── views/                # 页面组件
│   │   ├── auth/            # 认证页面
│   │   │   ├── Login.vue    # 登录页（含角色预填功能）
│   │   │   └── Register.vue # 注册页
│   │   ├── teacher/         # 教师端页面（工作台）
│   │   │   ├── Workbench.vue
│   │   │   ├── TrainingCreate.vue      # 实训编排（可视化拖拽）
│   │   │   ├── TrainingCreate copy.vue # 实训编排备份
│   │   │   ├── TrainingManage.vue      # 实训管理
│   │   │   ├── TrainingPublish.vue     # 实训发布
│   │   │   ├── EvaluationDashboard.vue # 评控面板
│   │   │   ├── EvaluationManage.vue   # 评控管理
│   │   │   ├── ClassCompetencyProfile.vue   # 班级能力画像
│   │   │   ├── StudentCompetencyProfile.vue # 学生能力画像
│   │   │   ├── ClassCourseManage.vue       # 班级课程管理
│   │   │   ├── TeacherLiveMonitor.vue      # 直播监控
│   │   │   ├── QuestionBankManage.vue      # 题库管理
│   │   │   ├── UserProfile.vue             # 用户信息
│   │   │   ├── components/            # 教师端公共组件
│   │   │   │   ├── PropertyEditor.vue
│   │   │   │   └── StandardNode.vue
│   │   │   └── data/                   # 教师端数据
│   │   │       └── node-templates.ts
│   │   ├── student/         # 学生端页面（工作台）
│   │   │   ├── Workbench.vue           # 学习空间
│   │   │   ├── TrainingDetail.vue      # 实训详情
│   │   │   ├── TrainingExecute.vue     # 实训执行（动态组件加载）
│   │   │   ├── TrainingPreview.vue     # 实训预览
│   │   │   ├── StudentCabin.vue        # 学生舱位
│   │   │   ├── MyTraining.vue          # 我的实训
│   │   │   ├── MyHomework.vue         # 我的作业
│   │   │   ├── MySubmission.vue       # 我的提交
│   │   │   ├── MyClass.vue            # 我的班级
│   │   │   ├── CourseList.vue         # 课程列表
│   │   │   ├── Notifications.vue     # 通知中心
│   │   │   ├── UserProfile.vue       # 用户信息
│   │   │   └── flow/                 # 实训节点流组件
│   │   │       ├── GenericNode.vue   # 通用节点
│   │   │       ├── GroupingNode.vue  # 分组节点
│   │   │       ├── HomeworkNode.vue   # 作业节点
│   │   │       ├── ResourceNode.vue   # 资源节点
│   │   │       └── UploadNode.vue     # 上传节点
│   │   ├── homework/        # 作业相关页面
│   │   │   ├── ExamPage.vue        # 考试页面
│   │   │   ├── HomeworkDetail.vue  # 作业详情
│   │   │   └── MindMapPractice.vue # 思维导图练习
│   │   ├── admin/           # 管理员端页面
│   │   │   ├── AdminLayout.vue        # 管理员布局
│   │   │   ├── TeacherManage.vue       # 教师管理
│   │   │   ├── StudentManage.vue      # 学生管理
│   │   │   ├── OrgManage.vue          # 组织机构管理
│   │   │   ├── CourseManage.vue       # 课程管理
│   │   │   ├── NodeManage.vue        # 节点管理
│   │   │   ├── TemplateDashboard.vue  # 模板看板
│   │   │   ├── QuestionDashboard.vue  # 题库看板
│   │   │   ├── TicketManage.vue      # 工单管理
│   │   │   ├── ServiceMonitor.vue    # 服务监控
│   │   │   ├── AuditLog.vue         # 审计日志
│   │   │   ├── MenuManage.vue        # 菜单管理
│   │   │   └── TrainingTestEntry.vue # 实训页面测试入口
│   │   ├── training/        # 实训页面（学生端+教师端）
│   │   │   ├── studentTraining/   # 学生端实训页面
│   │   │   │   ├── StartPortal.vue
│   │   │   │   ├── ResourceViewer.vue
│   │   │   │   ├── VideoPlayer.vue
│   │   │   │   ├── MindMapPreview.vue
│   │   │   │   ├── TaskBoard.vue
│   │   │   │   ├── AIStudyCard.vue
│   │   │   │   ├── AIPractice.vue
│   │   │   │   ├── MindMapEditor.vue
│   │   │   │   ├── RequirementCloud.vue
│   │   │   │   ├── PlanUpload.vue
│   │   │   │   ├── SimulatedIDE.vue      # 模拟IDE（Python数组学习）
│   │   │   │   ├── HomeworkEngine.vue
│   │   │   │   ├── PeerReview.vue        # 同伴互评
│   │   │   │   ├── TeacherComment.vue    # 教师点评（学生端）
│   │   │   │   ├── SummaryReport.vue
│   │   │   │   └── bak/                  # 备份目录
│   │   │   └── teacherTraining/   # 教师端实训监控页面
│   │   │       ├── TeacherStartPortal.vue
│   │   │       ├── TeacherResourceViewer.vue
│   │   │       ├── TeacherVideoPlayer.vue
│   │   │       ├── TeacherMindMapPreview.vue
│   │   │       ├── TeacherTaskBoard.vue
│   │   │       ├── TeacherAIStudyCard.vue
│   │   │       ├── TeacherAIPractice.vue
│   │   │       ├── TeacherMindMapEditor.vue
│   │   │       ├── TeacherRequirementCloud.vue
│   │   │       ├── TeacherPlanUpload.vue
│   │   │       ├── TeacherSimulatedIDE.vue
│   │   │       ├── TeacherHomeworkEngine.vue
│   │   │       ├── TeacherPeerReview.vue
│   │   │       ├── TeacherTeacherComment.vue  # 教师点评监控
│   │   │       ├── TeacherSummaryReport.vue
│   │   │       └── bak/                  # 备份目录
│   │   └── common/          # 公共页面
│   │       ├── 404.vue
│   │       ├── 500.vue
│   │       ├── AdminStandardPage.vue
│   │       └── Pagination.vue
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
└── FRONTEND_README.md      # 前端项目详细说明
```

---

## 🏗️ 核心类与模块架构

### 1. 认证模块 (Auth)

**核心类/组件**:
- `Login.vue` - 登录页面组件（含角色预填功能）
- `Register.vue` - 注册页面组件
- `AuthLayout/index.vue` - 认证页面布局组件
- `useAuth.ts` - 认证相关组合式函数
- `auth.store.ts` - 认证状态管理Store
- `auth.service.ts` - 认证API服务

**角色预填功能**:
登录页面支持根据角色自动预填账号密码，提升登录效率：
| 角色 | 账号 | 密码 |
|------|------|------|
| 学生 | `student` | `123456` |
| 教师 | `teacher` | `123456` |
| 管理员 | `user1` | `123456` |

切换角色时，账号密码输入框自动填充对应角色的默认凭证。

### 2. 实训编排模块 (Training Orchestration)

**核心类/组件**:
- `TrainingCreate.vue` - 实训编排页面（Vue Flow可视化）
- `training.store.ts` - 实训状态管理
- `useTraining.ts` - 实训相关组合式函数
- `training.service.ts` - 实训API服务
- `training.types.ts` - 实训类型定义

**核心类型定义**:
```typescript
interface TrainingNode {
  id: string;                    // 节点唯一ID
  type: NodeType;                // 节点类型
  position: { x: number; y: number };  // 画布位置
  data: {
    label: string;               // 节点标签
    description?: string;        // 节点描述
    config?: Record<string, any>; // 节点配置
  };
}

type NodeType = 
  | 'llm'           // LLM调用节点
  | 'code'          // 代码执行节点
  | 'condition'     // 条件分支节点
  | 'start'         // 开始节点
  | 'end'           // 结束节点
  | 'knowledge';    // 知识库节点
```

**实训编排流程**:
1. 教师进入实训编排页，从左侧组件库拖拽节点到画布
2. 连接节点形成流程（Vue Flow处理）
3. 双击节点配置属性（LLM提示词、代码等）
4. 保存草稿或发布实训（触发AI引擎处理）
5. AI引擎返回处理结果，更新模板状态

**撤销/重做机制**:
- historyStack: 历史状态栈（最大30步）
- redoStack: 重做栈
- 每次节点/连线变更，当前状态压入historyStack

### 3. 实训执行模块 (Training Execution)

**核心类/组件**:
- `TrainingExecute.vue` - 学生实训执行页面（动态组件加载）
- `StudentCabin.vue` - 学生舱位页面
- `TrainingDetail.vue` - 实训详情页面
- `training.store.ts` - 实训状态管理
- `utils/websocket.ts` - WebSocket连接管理

**动态节点组件**:
根据节点类型动态加载对应组件：
- `GenericNode.vue` - 通用节点
- `GroupingNode.vue` - 分组节点
- `UploadNode.vue` - 上传节点
- `ResourceNode.vue` - 资源节点
- `HomeworkNode.vue` - 作业节点

**三状态按钮交互模式**:
实训节点页面采用统一的三状态按钮交互规范：
| 状态 | 按钮文字 | 按钮状态 | 说明 |
|------|----------|----------|------|
| 初始 | `[完成当前任务]` | 可点击 | 用户完成当前节点任务 |
| 等待 | `等待教师进入下一节点` | 禁用+loading | 教师尚未推进到下一节点 |
| 进入 | `进入下一节点` | 可点击 | 教师已确认，学生可进入下一节点 |

**WebSocket实时通信流程**:
- 学生进入实训页面 → 建立 ws://localhost:8080/ws/training 连接
- 学生发送: { type: 'node_complete', nodeId: 'xxx', result: {...} }
- 后端广播: { type: 'progress_update', studentId: 1, progress: 50 }
- 教师监控端实时接收并展示

### 4. 模拟IDE模块 (Simulated IDE)

**核心组件**:
- `SimulatedIDE.vue` - 模拟IDE实训页面（Python数组学习）

**核心特性**:
- **Python数组学习**：涵盖数组反转、快速排序、二分查找等常用算法
- **自动连续流程**：取消localStorage机制，实现"初次生成→报错修改→测试通过"的连续学习流程
- **代码编辑器**：支持Python语法高亮显示
- **终端日志**：实时显示代码执行结果和错误信息

**技术实现**:
- 代码高亮逻辑（基于行的diffType判断背景色、行号颜色、文本颜色）
- 流式输出（代码、富文本、终端日志）
- 自动填充输入逻辑

### 5. 同伴互评模块 (Peer Review)

**核心组件**:
- `PeerReview.vue` - 同伴互评页面

**核心特性**:
- **滑块评分**：评分方式从按钮改为拖动条，分数范围0-10分
- **多维度评价**：支持从多个维度进行评价
- **实时评分显示**：拖动滑块时实时显示当前分数

**UI特性**:
- 滑块组件：min="0" max="10" step="1"
- 刻度标记：显示0、5、10三个参考点
- 分数显示：实时更新当前选中分数

### 6. 教师点评模块 (Teacher Comment)

**核心组件**:
- `TeacherComment.vue` - 教师点评与实训复盘页面（学生端查看）
- `TeacherTeacherComment.vue` - 教师点评监控页面（教师端监控）

**功能说明**:
- 学生端：查看教师对实训的评价和建议、实训成绩和能力分析
- 教师端：查看所有学生的实训进度、对学生实训成果进行点评打分、监控班级整体实训情况

### 7. AI浮动助手模块 (AI Floating Assistant)

**核心组件**:
- `AIFloatingAssistant.vue` - 页面悬浮AI助手组件
- `assistant.store.ts` - 助手状态管理
- `assistant.service.ts` - 助手API服务
- `useAssistant.ts` - 助手组合式函数

**核心特性**:
- 全局悬浮显示，随时可调用
- 提供即时答疑和辅导
- 支持实训相关问题查询
- 可折叠/展开交互
- 集成后端 AI 引擎进行智能问答
- 支持对话历史记录

**AI助手交互流程**:
```
1. 用户点击悬浮助手图标
   ↓
2. 展开对话面板
   ↓
3. 用户输入问题
   ↓
4. 调用 assistant.service.ts 发送请求到后端
   ↓
5. 后端转发到 AI 引擎处理
   ↓
6. 返回回答并展示在对话面板
```

### 8. 评控模块 (Evaluation)

**核心组件**:
- `EvaluationDashboard.vue` - 评控面板
- `EvaluationManage.vue` - 评控管理
- `ClassCompetencyProfile.vue` - 班级能力画像
- `StudentCompetencyProfile.vue` - 学生能力画像

**核心特性**:
- 班级整体能力分析
- 学生个体能力画像
- 实训数据分析与可视化

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
│   ├── /teacher/class-course       # 班级课程管理
│   ├── /teacher/evaluation         # 评估分析
│   └── /teacher/profile           # 用户中心
│
├── /teacher/* (沉浸式，无侧边栏)   # 教师沉浸式页面
│   ├── /teacher/training-create    # 实训编排
│   ├── /teacher/live-monitor       # 直播监控
│   ├── /teacher/training-publish   # 发布实训
│   ├── /teacher/class-competency   # 班级能力分析
│   └── /teacher/student-competency # 学生能力分析
│
├── /student               # 学生学习空间（带侧边栏）
│   ├── /student/workbench          # 工作台
│   ├── /student/student-cabin/:id  # 学生舱位
│   ├── /student/training-detail/:id # 实训详情
│   ├── /student/my-class          # 我的班级
│   ├── /student/courselist        # 课程列表
│   ├── /student/notifications     # 消息通知
│   ├── /student/my-homework       # 我的作业
│   ├── /student/my-submission     # 我的提交
│   ├── /student/my-training       # 我的实训
│   └── /student/profile           # 个人中心
│
├── /training              # 实训模块
│   ├── /training/studentTraining  # 学生端实训页面
│   └── /training/teacherTraining  # 教师端实训监控页面
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
4. **权限校验**：检查登录状态，未登录用户重定向到登录页

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
│         │         (router-view)             │
│         │                                   │
└─────────┴───────────────────────────────────┘
```

**沉浸式模式**：当路由 meta 设置 `hideSidebar: true` 时，不显示 Header 和 Sidebar，全屏展示内容（适用于实训编排、直播监控等场景）。

---

## 🚀 快速开始

### 环境要求
- Node.js 20.19+ 或 >=22.12.0
- npm 或 yarn

### 安装依赖
```bash
cd frontend-vue
npm install
```

### 启动开发服务器
```bash
npm run dev
```

应用将在 `http://localhost:5173` 启动。

### 构建生产版本
```bash
npm run build
```

---

## 📡 API服务配置

前端通过 `services/api.ts` 配置后端接口：

```typescript
const http = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
});

// 请求拦截器：自动携带 JWT Token
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：处理 Token 过期
http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      authStore.logout();
      router.push('/auth/login');
    }
    return Promise.reject(error);
  }
);
```

---

## 🆕 新增功能（2024更新）

### 1. 理论实训模块
- **理论实训课堂页面**：路由 `/training/student-training/theory-lab-task`，对接理论实训任务数据接口和AI对话接口
- **代码块渲染优化**：修复Markdown代码块显示问题，调整背景色为深色主题
- **AI助教默认打开**：进入理论实训页面时自动展开AI浮动助手

### 2. 实训总结报告
- **总结报告页面**：路由 `/training/student-training/summary-report`，展示实训完成情况
- **雷达图展示**：SVG实现的能力维度雷达图，展示编码能力、逻辑思维、知识掌握等维度
- **数值定制**：支持自定义各维度评分数据

### 3. 开始实训节点优化
- **横向流程图布局**：课前阶段 → 课中阶段 → 课后阶段的横向展示
- **阶段节点可视化**：每个阶段内节点纵向排列，带连接线和箭头指示
- **预估时长显示**：展示各阶段和节点的预估完成时间

### 4. 实训流程控制
- **阶段完成祝贺弹窗**：完成课前/课中阶段后显示祝贺弹窗，提供进入下一阶段或返回首页选项
- **重新开始功能**：支持实训重复开始，重置进度并重新定位到第一个节点
- **节点完成跳转**：修复完成节点后正确导航到下一个节点或总结报告页面

### 5. 思维导图优化
- **高度铺满页面**：导图框上下高度平铺整个页面
- **默认放大显示**：进入页面时默认放大到能看清根节点

### 6. 方案上传页面重构
- **左右结构布局**：左侧为上传控件，右侧为AI评审区域
- **布局优化**：修复实训执行页面元素偏左问题

### 7. 学生工作台增强
- **固定实训卡片**：添加"自主编程实训任务"和"自主理论实训任务"两个固定卡片
- **路由配置**：分别路由到 `/training/student-training/ai-teaching-ide` 和 `/training/student-training/theory-lab-home`

### 8. 教师评价页面
- **接口对接**：展示实训卡片，清除mock数据
- **卡片跳转**：点击卡片跳转到教师端总结报告页面

### 9. 路由守卫增强
- **实训路由守卫**：确保学生只能访问已解锁的阶段和节点
- **权限控制**：根据角色动态路由到对应工作台页面

### 10. 编码实训退出修复
- **路由修正**：从学生端编码实训页面退出时正确跳转到 `/student/workbench`

---

## 📚 详细文档链接

- [项目主文档](../README.md) - 项目整体概述
- [后端文档](../backend-core/BACKEND_README.md) - 后端API和架构
- [AI引擎文档](../ai-engine/AI-ENGINE_README.md) - AI引擎说明