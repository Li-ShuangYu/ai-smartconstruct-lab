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
│   │   │   ├── Login.vue    # 登录页
│   │   │   └── Register.vue # 注册页
│   │   ├── teacher/         # 教师端页面
│   │   │   ├── Workbench.vue           # 教师工作台
│   │   │   ├── TrainingCreate.vue      # 实训编排
│   │   │   ├── TrainingManage.vue       # 实训管理
│   │   │   ├── TrainingPublish.vue     # 发布实训
│   │   │   ├── ClassCourseManage.vue   # 班级课程管理
│   │   │   ├── TeacherLiveMonitor.vue  # 直播监控
│   │   │   ├── EvaluationDashboard.vue # 评估分析
│   │   │   ├── EvaluationManage.vue   # 评估管理
│   │   │   ├── ClassCompetencyProfile.vue  # 班级能力分析
│   │   │   ├── StudentCompetencyProfile.vue # 学生能力分析
│   │   │   ├── UserProfile.vue        # 用户中心
│   │   │   └── components/           # 教师端专用组件
│   │   │       ├── StandardNode.vue   # 标准节点组件
│   │   │       └── PropertyEditor.vue # 属性编辑器
│   │   │       └── data/
│   │   │           └── node-templates.ts # 节点模板数据
│   │   ├── student/         # 学生端页面
│   │   │   ├── Workbench.vue        # 学生工作台
│   │   │   ├── StudentCabin.vue     # 学生舱位
│   │   │   ├── TrainingDetail.vue   # 实训详情
│   │   │   ├── MyClass.vue         # 我的班级
│   │   │   ├── CourseList.vue      # 课程列表
│   │   │   ├── MyTraining.vue      # 我的实训
│   │   │   ├── MyHomework.vue      # 我的作业
│   │   │   ├── MySubmission.vue    # 我的提交
│   │   │   ├── Notifications.vue   # 通知消息
│   │   │   └── UserProfile.vue     # 个人中心
│   │   ├── admin/           # 管理员端页面
│   │   │   ├── AdminLayout.vue     # 管理员布局
│   │   │   ├── TeacherManage.vue   # 教师管理
│   │   │   ├── StudentManage.vue   # 学生管理
│   │   │   ├── OrgManage.vue      # 机构管理
│   │   │   ├── CourseManage.vue   # 课程管理
│   │   │   ├── MenuManage.vue     # 菜单管理
│   │   │   ├── NodeManage.vue     # 节点管理
│   │   │   ├── TemplateDashboard.vue # 模板管理
│   │   │   ├── QuestionDashboard.vue # 题目管理
│   │   │   ├── TicketManage.vue   # 工单管理
│   │   │   ├── ServiceMonitor.vue # 服务监控
│   │   │   └── AuditLog.vue      # 审计日志
│   │   ├── homework/        # 作业页面
│   │   │   ├── ExamPage.vue        # 考试页面
│   │   │   ├── HomeworkDetail.vue  # 作业详情
│   │   │   └── MindMapPractice.vue # 思维导图练习
│   │   ├── training/         # 实训页面
│   │   │   ├── CodeTraining.vue    # 编码实训
│   │   │   ├── TheoryTraining.vue   # 理论实训
│   │   │   └── studentTraining/
│   │   │       └── TrainingStart.vue # 学生实训开始
│   │   └── common/          # 公共页面
│   │       ├── 404.vue     # 404错误页
│   │       ├── 500.vue     # 500错误页
│   │       ├── Pagination.vue      # 分页组件
│   │       └── AdminStandardPage.vue # 管理员标准页面
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

## � 服务层 (Services)

### API 封装

`services/api.ts` 封装了 Axios 实例，提供：
- 请求拦截器（添加 Token、请求 ID）
- 响应拦截器（统一错误处理、Token 刷新）
- 统一错误提示

### 类型定义

| 文件 | 说明 |
|------|------|
| auth.types.ts | 认证相关类型（登录请求/响应、注册请求/响应） |
| user.types.ts | 用户相关类型（用户信息、权限） |
| training.types.ts | 实训相关类型（实训创建/查询/更新） |
| homework.types.ts | 作业相关类型（作业提交/批改） |

### API 服务

| 服务 | 说明 |
|------|------|
| auth.service.ts | 认证服务（登录、注册、Token刷新） |
| user.service.ts | 用户服务（用户信息、权限查询） |
| training.service.ts | 实训服务（CRUD操作） |
| homework.service.ts | 作业服务（提交、批改） |
| assistant.service.ts | 智能助手服务 |

---

## 💾 状态管理 (Stores)

使用 Pinia 进行状态管理，采用 Setup 语法。

### Store 模块

| Store | 说明 |
|-------|------|
| auth.store.ts | 用户认证状态（Token、用户信息、登录状态） |
| app.store.ts | 全局应用状态（加载状态、主题） |
| training.store.ts | 实训状态（当前实训、实训列表） |
| homework.store.ts | 作业状态 |
| assistant.store.ts | 智能助手对话状态 |

---

## 🪝 Hooks

组合式函数封装复用逻辑：

| Hook | 说明 |
|------|------|
| useAuth | 权限认证（登录、登出、权限检查） |
| useTraining | 实训操作（创建、提交、评分） |
| useStepProgress | 步进流程（实训节点进度） |
| useTelemetry | 埋点上报（用户行为追踪） |
| useAssistant | 智能助手（对话、问答） |

---

## 🧰 工具函数

| 工具 | 说明 |
|------|------|
| format.ts | 格式化（日期、数字、文件大小） |
| storage.ts | 本地存储（Token、用户信息） |
| validate.ts | 表单校验（邮箱、手机、密码） |
| telemetry.ts | 埋点上报（页面访问、点击事件） |

---

## 📄 页面详解

### 认证模块 (auth)

#### Login.vue
- **路径**：`/auth/login`
- **功能**：用户登录
- **字段**：用户名/邮箱、密码、记住我
- **布局**：AuthLayout 左右分栏

#### Register.vue
- **路径**：`/auth/register`
- **功能**：用户注册
- **字段**：用户名、邮箱、密码、确认密码、验证码

### 教师端 (teacher)

#### Workbench.vue
- **路径**：`/teacher/workbench`
- **功能**：教师工作台概览
- **内容**：班级列表、实训统计、最近活动

#### TrainingCreate.vue
- **路径**：`/teacher/training-create`
- **功能**：可视化实训编排（沉浸式）
- **布局**：全屏 Vue Flow 画布
- **组件**：StandardNode（标准节点）、PropertyEditor（属性编辑）

#### TrainingManage.vue
- **路径**：`/teacher/training-manage`
- **功能**：实训列表管理
- **操作**：创建、编辑、发布、删除

#### TrainingPublish.vue
- **路径**：`/teacher/training-publish`
- **功能**：发布实训到班级（沉浸式）

#### ClassCourseManage.vue
- **路径**：`/teacher/class-course-manage`
- **功能**：班级与课程关联管理
- **标签页**：班级管理、课程管理

#### TeacherLiveMonitor.vue
- **路径**：`/teacher/teacher-live-monitor`
- **功能**：实时监控学生实训状态（沉浸式）

#### EvaluationDashboard.vue
- **路径**：`/teacher/evaluation`
- **功能**：班级/学生能力分析看板

#### EvaluationManage.vue
- **路径**：`/teacher/evaluation-manage`
- **功能**：评估标准管理

#### ClassCompetencyProfile.vue
- **路径**：`/teacher/class-competency/:studentId`
- **功能**：班级实训总结分析

#### StudentCompetencyProfile.vue
- **路径**：`/teacher/student-competency/:studentId`
- **功能**：学生实训能力评价

#### UserProfile.vue
- **路径**：`/teacher/profile`
- **功能**：个人信息管理

### 学生端 (student)

#### Workbench.vue
- **路径**：`/student/workbench`
- **功能**：学生学习工作台
- **内容**：进行中实训、已完成、作业提醒

#### StudentCabin.vue
- **路径**：`/student/student-cabin/:id`
- **功能**：沉浸式实训舱（WebIDE风格）
- **布局**：全屏沉浸式，左右分栏（题目区+答题区）

#### TrainingDetail.vue
- **路径**：`/student/training-detail/:id`
- **功能**：实训详情（只读）

#### MyClass.vue
- **路径**：`/student/my-class`
- **功能**：我的班级、班级成员

#### CourseList.vue
- **路径**：`/student/courselist`
- **功能**：选修课程列表

#### MyTraining.vue
- **路径**：`/student/my-training`
- **功能**：我的实训记录

#### MyHomework.vue
- **路径**：`/student/my-homework`
- **功能**：我的作业列表

#### MySubmission.vue
- **路径**：`/student/my-submission`
- **功能**：我的提交记录

#### Notifications.vue
- **路径**：`/student/notifications`
- **功能**：系统通知、个人消息

#### UserProfile.vue
- **路径**：`/student/profile`
- **功能**：个人中心

### 管理员端 (admin)

#### TeacherManage.vue
- **路径**：`/admin/teacher`
- **功能**：教师账号管理（增删改查）

#### StudentManage.vue
- **路径**：`/admin/student`
- **功能**：学生账号管理

#### OrgManage.vue
- **路径**：`/admin/org`
- **功能**：组织机构管理（学校/院系）

#### CourseManage.vue
- **路径**：`/admin/course`
- **功能**：课程模板管理

#### MenuManage.vue
- **路径**：`/admin/menu`
- **功能**：系统菜单配置

#### NodeManage.vue
- **路径**：`/admin/node`
- **功能**：节点/题库管理

#### TemplateDashboard.vue
- **路径**：`/admin/template`
- **功能**：实训模板管理

#### QuestionDashboard.vue
- **路径**：`/admin/question`
- **功能**：题目管理

#### TicketManage.vue
- **路径**：`/admin/ticket`
- **功能**：工单/问题反馈

#### ServiceMonitor.vue
- **路径**：`/admin/monitor`
- **功能**：系统服务监控

#### AuditLog.vue
- **路径**：`/admin/audit`
- **功能**：操作审计日志

### 作业模块 (homework)

#### ExamPage.vue
- **功能**：在线考试页面
- **特性**：计时器、题目切换、提交

#### HomeworkDetail.vue
- **功能**：作业详情与批改

#### MindMapPractice.vue
- **功能**：思维导图练习（封装 Vue Flow）

### 实训模块 (training)

#### CodeTraining.vue
- **功能**：编码实训舱
- **组件**：代码编辑器（Monaco Editor）

#### TheoryTraining.vue
- **功能**：理论实训（选择题/填空题）

#### TrainingStart.vue
- **路径**：`/training/student-training/training-start`
- **功能**：学生开始实训

---

## � 开发命令

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
