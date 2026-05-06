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
| Axios | HTTP客户端 | 1.x |

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
│   │   │   ├── 侧信道.mp3
│   │   │   ├── 后量子算法.mp3
│   │   │   ├── 抗重放.mp3
│   │   │   └── 轻量级.mp3
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
│   │       ├── TrainingLayout/ # 实训布局
│   │       │   ├── index.vue
│   │       │   ├── StudentHeader.vue
│   │       │   └── TeacherHeader.vue
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
│   │   │   ├── homework.types.ts
│   │   │   └── org.types.ts
│   │   └── modules/          # API服务
│   │       ├── auth.service.ts
│   │       ├── user.service.ts
│   │       ├── training.service.ts
│   │       ├── homework.service.ts
│   │       ├── assistant.service.ts
│   │       └── org.service.ts
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
│   │   ├── student/         # 学生端页面
│   │   ├── admin/           # 管理员端页面
│   │   ├── homework/        # 作业页面
│   │   ├── training/        # 实训页面
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

## 🚀 快速开始

### 环境要求

- Node.js 18+
- npm 9+

### 安装依赖

```bash
cd frontend-vue
npm install
```

### 开发模式

```bash
npm run dev
```

访问: http://localhost:5173

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
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
│   ├── /student/my-homework       # 我的作业
│   ├── /student/my-submission     # 我的提交
│   ├── /student/my-training       # 我的实训
│   └── /student/profile           # 个人中心
│
├── /training              # 实训模块
│   ├── /training/theory           # 理论实训
│   └── /training/code             # 代码实训
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

---

## 🔧 配置说明

### 后端接口配置

前端默认连接后端地址：`http://localhost:8080`

如需修改后端地址，请编辑 `src/services/api.ts`：

```typescript
const http: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080', // 修改此地址
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})
```

### 跨域配置

后端已配置允许以下前端域名访问：
- http://localhost:5173
- http://localhost:5174
- http://localhost:3000

---

## 📖 开发指南

### 添加新页面

1. 在 `src/views/` 目录创建页面组件
2. 在 `src/router/modules/` 目录添加路由配置
3. 在 `src/services/modules/` 添加对应的 API 服务（如需要）
4. 在 `src/stores/modules/` 添加状态管理（如需要）

### 代码规范

- 使用 Vue 3 Composition API + `<script setup>` 语法
- 使用 TypeScript 进行类型检查
- 使用 Pinia 进行状态管理
- 使用 Axios 进行 HTTP 请求
- 组件命名使用 PascalCase
- 文件命名使用 kebab-case

---

## ❓ 常见问题

### 1. 登录时报"网络错误，请稍后重试"

**问题描述**: 输入正确账号密码后提示网络错误

**原因分析**: 通常是跨域问题或后端服务未启动

**解决方案**:
1. 确认后端服务正在运行（端口 8080）
2. 确认前端运行端口已添加到后端 CORS 配置
3. 使用命令检查端口状态：`netstat -ano | findstr ":8080"`

### 2. 页面显示空白

**问题描述**: 页面加载后显示空白

**排查步骤**:
1. 打开浏览器开发者工具（F12）
2. 检查 Console 面板是否有错误
3. 检查 Network 面板是否有请求失败
4. 确认路由配置正确

### 3. 构建失败

**问题描述**: `npm run build` 失败

**排查步骤**:
1. 检查是否有 TypeScript 类型错误
2. 检查依赖是否安装完整
3. 尝试删除 `node_modules` 后重新安装

---

## 📞 联系方式

如有问题请联系开发团队。