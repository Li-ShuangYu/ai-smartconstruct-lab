# AI SmartConstruct Lab

基于大模型与松耦合架构的智能教学实训系统。支持可视化工作流编排，以及具备真实数学逻辑的多维量化评分引擎。

## 📋 项目概览

AI SmartConstruct Lab 是一个面向教育领域的智能实训平台，旨在为教师和学生提供沉浸式的实训体验。系统采用微服务架构，分为前端展示层、后端业务层和AI引擎层。

### 核心特性

- 🎨 **可视化工作流编排** - 教师可通过拖拽方式编排实训流程（基于 Vue Flow）
- 🧠 **AI驱动的智能评分** - 基于大语言模型的量化评分引擎
- 💬 **苏格拉底式辅导** - 针对学生报错代码的引导式答疑
- 🔒 **多级权限管理** - 支持教师、学生、管理员多角色
- 📊 **实时数据看板** - 班级整体数据与个体能力分析
- 🌐 **WebSocket实时通信** - 课中实训实时状态同步
- 🗂️ **思维导图练习** - 支持知识结构化训练（基于 simple-mind-map）
- 🤖 **AI浮动助手** - 页面悬浮AI辅导组件，提供即时帮助
- 💻 **模拟IDE** - Python数组学习环境，支持代码高亮和终端日志
- 👥 **同伴互评** - 滑块评分方式，支持多维度评价

---

## 🛠️ 技术栈

| 模块 | 技术 | 版本 |
|------|------|------|
| 前端框架 | Vue 3 + TypeScript | Vue 3.5.32 / TypeScript ~6.0.0 |
| 前端构建 | Vite | 8.0.8 |
| 状态管理 | Pinia | 3.0.4 |
| 路由 | Vue Router | 5.0.6 |
| 图表 | ECharts | 6.0.0 |
| UI组件 | Naive UI | 2.44.1 |
| 流程图 | Vue Flow | 1.48.2 |
| 思维导图 | simple-mind-map | 0.14.0-fix.2 |
| 图标 | Font Awesome | 7.2.0 |
| 后端框架 | Spring Boot | 2.7.18 |
| ORM | MyBatis-Plus | 3.5.5 |
| 认证 | Spring Security + JWT | jjwt 0.12.5 |
| 实时通信 | WebSocket | Spring WebSocket |
| 工具库 | Hutool | 5.8.26 |
| 数据库 | MySQL | 8.0+ |
| AI引擎 | Python + FastAPI | 0.115+ |
| 容器 | Docker Compose | 24.0+ |

---

## 📁 目录结构

```
ai-smartconstruct-lab/
├── frontend-vue/              # Vue 3 前端应用
│   ├── src/
│   │   ├── assets/            # 静态资源（样式、音频、图片）
│   │   ├── components/        # 公共组件（布局、图标、AI助手）
│   │   │   ├── AIFloatingAssistant/  # AI浮动助手组件
│   │   │   ├── icons/         # 图标组件
│   │   │   └── layout/        # 布局组件
│   │   ├── hooks/             # 组合式函数（认证、实训、WebSocket）
│   │   ├── router/            # 路由配置（模块划分）
│   │   ├── services/          # 服务层（API调用）
│   │   ├── stores/            # 状态管理（Pinia）
│   │   ├── utils/             # 工具函数
│   │   └── views/             # 页面组件（教师/学生/管理员/实训）
│   ├── public/                # 静态资源（直接拷贝至dist）
│   └── package.json
├── backend-core/              # Spring Boot 后端核心
│   ├── src/main/java/         # Java 源代码
│   ├── src/main/resources/    # 配置文件
│   └── pom.xml
├── ai-engine/                 # Python AI 引擎
│   ├── agents/                # AI代理
│   ├── routers/               # API路由
│   ├── services/              # 业务服务
│   ├── main.py                # 启动入口
│   └── requirements.txt       # 依赖列表
├── infrastructure/            # 基础设施配置
│   └── docker-compose.yml     # Docker Compose配置
└── README.md
```

### 模块职责

| 模块 | 职责 |
|------|------|
| `frontend-vue` | 用户界面展示，包含教师工作台、学生学习空间、管理员后台、实训编排器、动态节点执行系统、AI浮动助手、模拟IDE、同伴互评 |
| `backend-core` | 核心业务逻辑、用户认证、任务流转控制、数据持久化、工作流引擎、WebSocket实时通信 |
| `ai-engine` | 量化评分引擎、苏格拉底式智能辅导、大模型交互 |

---

## 🏗️ 核心类与架构

### 后端核心类 (backend-core)

#### 1. 认证模块 (Auth)

**核心类**:
- `AuthController` - 认证接口控制器，处理登录/注册/登出请求
- `SysUserServiceImpl` - 用户服务实现，处理用户CRUD和认证逻辑
- `JwtUtil` - JWT工具类，负责Token生成与验证
- `JwtAuthenticationFilter` - JWT认证过滤器，拦截请求验证Token
- `SecurityConfig` - 安全配置类，配置Spring Security策略

**核心实体**:
- `SysUser` - 系统用户实体，存储用户基本信息
- `BizStudent` - 学生实体，扩展学生特有属性
- `BizTeacher` - 教师实体，扩展教师特有属性

**认证流程**:
```
1. 客户端发送登录请求 (username, password, roleType)
   ↓
2. AuthController.login() 接收请求
   ↓
3. SysUserServiceImpl.verifyUser() 验证用户
   ↓
4. JwtUtil.generateToken() 生成JWT Token
   ↓
5. 返回Token和用户信息给客户端
   ↓
6. 客户端在后续请求中携带 Authorization: Bearer <token>
   ↓
7. JwtAuthenticationFilter 验证Token有效性
   ↓
8. 将用户信息存入 SecurityContext
```

#### 2. 实训编排模块 (Training Orchestration)

**核心类**:
- `TeacherTemplateController` - 教师模板控制器，管理实训模板CRUD
- `TeacherTrainingTaskController` - 任务下发控制器
- `StudentTrainingController` - 学生实训控制器
- `StudentTrainingFlowController` - 学生实训流程控制器
- `TrainingWebSocketHandler` - WebSocket处理器，处理实训实时通信
- `TrainingHandshakeInterceptor` - WebSocket握手拦截器，验证连接身份

**核心实体**:
- `WfTrainingTemplate` - 实训模板
- `WfNodeDef` - 节点定义
- `BizTrainingTask` - 实训任务
- `BizTrainingParticipation` - 学生实训参与记录
- `WfStudentActivityState` - 学生活动状态
- `WfGlobalActivityState` - 全局活动状态

**实训编排流程**:
```
1. 教师创建实训模板
   TeacherTemplateController.create()
   ↓
2. 解析画布数据，保存节点和连线
   WfTrainingTemplate.rawCanvasJson
   ↓
3. AI引擎处理模板（模拟/真实）
   templateService.processTemplateMockAi()
   ↓
4. 教师发布实训任务
   TeacherTrainingTaskController.create()
   ↓
5. 生成学生参与记录
   BizTrainingParticipation
   ↓
6. 学生开始实训，状态流转
   StudentTrainingController.start() / next()
```

#### 3. 组织管理模块 (AdminOrg)

**核心类**:
- `AdminOrgController` - 组织管理控制器
- `DepartmentServiceImpl` - 院系服务
- `MajorServiceImpl` - 专业服务
- `AdminClassServiceImpl` - 班级服务

**核心实体**:
- `BizDepartment` - 院系信息
- `BizMajor` - 专业信息
- `BizAdminClass` - 班级信息

#### 4. 课程资源模块

**核心类**:
- `TeacherCourseController` - 教师课程控制器
- `StudentCourseController` - 学生课程控制器
- `AdminCourseController` - 管理员课程控制器
- `AdminQuestionController` - 题目管理控制器

**核心实体**:
- `BizCourse` - 课程信息
- `BizStudentCourse` - 学生选课记录
- `BizQuestionBank` - 题库
- `BizQuestion` - 题目

#### 5. 配置类

**核心配置类**:
- `SecurityConfig` - Spring Security安全配置
  - 配置JWT无状态认证
  - 配置CORS跨域支持
  - 配置请求权限控制

- `WebSocketConfig` - WebSocket配置
  - 注册WebSocket端点 `/ws/training`
  - 配置握手拦截器
  - 允许跨域连接

- `MybatisPlusConfig` - MyBatis-Plus配置
  - 配置分页插件
  - 配置乐观锁插件
  - 配置自动填充字段

- `AsyncConfig` - 异步线程池配置
  - 配置异步任务执行器
  - 支持模板AI处理等异步操作

- `GlobalExceptionHandler` - 全局异常处理
  - 统一异常响应封装
  - 自定义业务异常处理

### 前端核心类 (frontend-vue)

#### 1. 认证模块 (Auth)

**核心组件**:
- `Login.vue` - 登录页面组件（含角色预填功能）
- `Register.vue` - 注册页面组件
- `AuthLayout/index.vue` - 认证页面布局

**角色预填功能**:
登录页面支持根据角色自动预填账号密码：
| 角色 | 账号 | 密码 |
|------|------|------|
| 学生 | `2270410234` | `123456` |
| 教师 | `teacher` | `123456` |
| 管理员 | `user1` | `123456` |

**核心Store**:
- `auth.store.ts` - 认证状态管理
  - token: JWT Token
  - userInfo: 用户信息
  - login(): 登录方法
  - logout(): 登出方法

**核心Service**:
- `auth.service.ts` - 认证API服务
  - login(data): 登录请求
  - register(data): 注册请求
  - getCurrentUser(): 获取当前用户

#### 2. 实训编排模块 (Training Orchestration)

**核心组件**:
- `TrainingCreate.vue` - 实训编排页面（Vue Flow可视化）
- `TrainingManage.vue` - 实训管理页面
- `TrainingPublish.vue` - 实训发布页面

**核心Store**:
- `training.store.ts` - 实训状态管理
  - nodes: 节点列表
  - edges: 连线列表
  - historyStack: 撤销栈
  - redoStack: 重做栈
  - addNode(): 添加节点
  - undo(): 撤销
  - redo(): 重做

**核心类型**:
```typescript
interface TrainingNode {
  id: string;
  type: 'start' | 'end' | 'llm' | 'code' | 'condition' | 'knowledge' | 'resource' | 'homework' | 'upload';
  position: { x: number; y: number };
  data: { label: string; config?: object };
}

interface TrainingEdge {
  id: string;
  source: string;
  target: string;
}
```

#### 3. 实训执行模块 (Training Execution)

**核心组件**:
- `TrainingExecute.vue` - 实训执行页面（动态组件加载）
- `StudentCabin.vue` - 学生舱位页面
- `TrainingDetail.vue` - 实训详情页面

**动态节点组件**:
`TrainingExecute.vue` 根据节点类型动态加载对应组件：
- `GenericNode.vue` - 通用节点
- `GroupingNode.vue` - 分组节点
- `HomeworkNode.vue` - 作业节点
- `ResourceNode.vue` - 资源节点
- `UploadNode.vue` - 上传节点

**三状态按钮交互模式**:
实训节点页面采用统一的三状态按钮交互规范：
| 状态 | 按钮文字 | 按钮状态 | 说明 |
|------|----------|----------|------|
| 初始 | `[完成当前任务]` | 可点击 | 用户完成当前节点任务 |
| 等待 | `等待教师进入下一节点` | 禁用+loading | 教师尚未推进到下一节点 |
| 进入 | `进入下一节点` | 可点击 | 教师已确认，学生可进入下一节点 |

**核心Hook**:
- `useWebSocket.ts` - WebSocket连接管理
  - connect(sessionId): 建立连接
  - send(message): 发送消息
  - disconnect(): 断开连接

**执行流程**:
```
1. 加载实训流程图
   trainingService.getTrainingDetail(id)
   ↓
2. 建立WebSocket连接
   useWebSocket.connect(sessionId)
   ↓
3. 获取当前节点
   GET /api/student/training-tasks/{taskId}/participation
   ↓
4. 渲染节点内容（动态组件）
   根据节点类型显示不同UI
   ↓
5. 学生完成任务
   POST /api/student/training-tasks/{participationId}/next
   ↓
6. 更新进度，高亮下一节点
```

#### 4. 模拟IDE模块 (Simulated IDE)

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

#### 5. 同伴互评模块 (Peer Review)

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

#### 6. 教师点评模块 (Teacher Comment)

**核心组件**:
- `TeacherComment.vue` - 教师点评与实训复盘页面（学生端查看）
- `TeacherTeacherComment.vue` - 教师点评监控页面（教师端监控）

**功能说明**:
- 学生端：查看教师对实训的评价和建议、实训成绩和能力分析
- 教师端：查看所有学生的实训进度、对学生实训成果进行点评打分、监控班级整体实训情况

#### 7. 思维导图模块 (Mind Map)

**核心组件**:
- `MindMapEditor.vue` - 思维导图编辑器
- `MindMapPreview.vue` - 思维导图预览
- `MindMapPractice.vue` - 思维导图练习页面

**核心特性**:
- 基于 simple-mind-map 库实现
- 支持节点增删改查
- 支持思维导图数据导出

#### 8. AI浮动助手模块 (AI Floating Assistant)

**核心组件**:
- `AIFloatingAssistant.vue` - 页面悬浮AI助手组件

**核心特性**:
- 全局悬浮显示，随时可调用
- 提供即时答疑和辅导
- 支持实训相关问题查询
- 可折叠/展开交互

#### 9. 评控模块 (Evaluation)

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

## 🚀 快速开始

### 环境要求

确保你的开发环境已安装以下软件：

- Java 8+ (配置 JAVA_HOME)
- Node.js 20.19+ 或 >=22.12.0
- Python 3.10+
- MySQL 8.x
- Docker Desktop（可选）

### 启动步骤

#### 1. 数据库配置

- 连接本地 MySQL（默认端口 3306）
- 创建数据库：`CREATE DATABASE ai-smartconstruct-lab CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
- 配置用户名：`root`，密码：`123456`

#### 2. 后端启动

```bash
cd backend-core

# 编译项目
./mvnw.cmd clean install -DskipTests

# 启动应用
./mvnw.cmd spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

#### 3. AI 引擎启动

```bash
cd ai-engine

# 创建虚拟环境
python -m venv venv
.\venv\Scripts\activate

# 安装依赖
pip install -r requirements.txt

# 启动服务
uvicorn main:app --reload --port 8000
```

AI 引擎服务将在 `http://localhost:8000` 启动。

#### 4. 前端启动

```bash
cd frontend-vue

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端应用将在 `http://localhost:5173` 启动。

---

## 🔧 配置说明

### 后端配置

后端使用 Spring Boot 标准配置文件：

- `backend-core/src/main/resources/application.properties` - 主配置文件
- 数据库连接信息：
  - 数据库名：`ai-smartconstruct-lab`
  - 用户名：`root`
  - 密码：`123456`
  - 端口：`3306`
- JWT 配置：
  - 密钥：`smartconstruct-jwt-secret-key-2026-must-be-at-least-256bits-long!!`
  - 有效期：24小时

### 前端配置

前端配置文件：
- `frontend-vue/src/services/api.ts` - API 基础路径配置
- 后端接口地址：`http://localhost:8080`
- 请求超时时间：15秒

---

## 🎯 功能模块与流程

### 角色划分

| 角色 | 路径前缀 | 功能描述 |
|------|---------|---------|
| 认证模块 | `/auth` | 登录、注册 |
| 管理员 | `/admin` | 用户管理、课程管理、系统监控 |
| 教师 | `/teacher` | 实训编排、班级管理、直播监控、评控分析 |
| 学生 | `/student` | 课程学习、作业提交、实训参与 |

### 教师功能流程

#### 1. 实训编排流程
```
登录系统
  ↓
进入工作台 (/teacher/workbench)
  ↓
点击"创建实训"
  ↓
进入编排页面 (/teacher/training-create)
  ↓
从左侧拖拽节点到画布
  ↓
连接节点形成流程
  ↓
配置节点属性（双击节点）
  ↓
保存草稿 / 发布实训
  ↓
跳转到实训管理页
```

#### 2. 实训发布流程
```
在实训管理页 (/teacher/training-manage)
  ↓
选择待发布的实训
  ↓
点击"发布"按钮
  ↓
选择目标班级
  ↓
选择学生（可选）
  ↓
设置截止时间
  ↓
确认发布
  ↓
学生收到实训任务通知
```

#### 3. 实时监控流程
```
进入直播监控页 (/teacher/live-monitor)
  ↓
选择正在进行的实训
  ↓
WebSocket连接建立
  ↓
实时接收学生进度更新
  ↓
查看学生当前节点状态
  ↓
可介入指导学生
```

#### 4. 评控分析流程
```
进入评控面板 (/teacher/evaluation)
  ↓
查看班级能力画像
  ↓
分析学生个体能力
  ↓
生成评估报告
  ↓
导出分析数据
```

### 学生功能流程

#### 1. 实训参与流程
```
登录系统
  ↓
进入学习空间 (/student/workbench)
  ↓
查看待完成的实训任务
  ↓
点击"开始实训"
  ↓
进入实训执行页 (/training/execute/:id)
  ↓
WebSocket连接建立
  ↓
按节点顺序完成任务
  ↓
提交结果，进入下一节点
  ↓
完成所有节点
  ↓
生成实训报告
```

#### 2. 作业提交流程
```
进入我的作业页 (/student/my-homework)
  ↓
选择待完成的作业
  ↓
查看作业详情和要求
  ↓
完成作业内容
  ↓
提交作业
  ↓
等待教师批改
  ↓
查看成绩和评语
```

### 管理员功能

- 👥 **用户管理** - 教师、学生账号管理
- 🏢 **机构管理** - 学校/院系/专业/班级组织架构
- 📦 **课程管理** - 课程模板管理
- 📝 **题库管理** - 题目和题库管理
- 🔧 **节点管理** - 工作流节点定义管理
- 📋 **模板管理** - 实训模板管理
- 🎫 **工单管理** - 系统工单处理
- 📊 **服务监控** - 服务状态监控
- 📝 **审计日志** - 操作日志记录

---

## 📡 API 接口

### 后端 API

| 模块 | 基础路径 | 说明 |
|------|---------|------|
| 认证 | `/api/auth` | 登录、注册、登出、用户信息 |
| 公共数据 | `/api/public` | 公开数据查询（无需认证） |
| 教师 | `/api/teacher` | 教师工作台、实训管理、课程管理、评控分析 |
| 学生 | `/api/student` | 学生学习、实训参与、课程选修 |
| 管理员 | `/api/admin` | 用户管理、机构管理、系统运维 |

### AI 引擎 API

| 接口 | 路径 | 说明 |
|------|------|------|
| 评分 | `/api/evaluate` | 量化评分引擎 |
| 辅导 | `/api/tutor` | 苏格拉底式辅导 |
| 健康检查 | `/health` | 服务状态检查 |

---

## 🔗 系统流转真实链路

### 1. 实训任务发布链路
```
前端 (TrainingCreate.vue) 
  → buildCanvasData() 构建 { orchestration_id, nodes, edges }
  → createTemplate() 调用 POST /api/teacher/templates
  → 后端 TeacherTemplateController.create() 
     → 保存 WfTrainingTemplate (rawCanvasJson 字段)
     → templateService.processTemplateMockAi() 模拟AI处理
     → 返回模板 ID
  → 前端跳转到 /teacher/training-manage
```

**交互契约**：
- 请求体: `{ templateName: string, canvasData: CanvasData }`
- CanvasData 结构:
  ```typescript
  {
    orchestration_id: string,
    nodes: Array<{ node_id: string, node_type: string, name: string, config: object }>,
    edges: Array<{ source: string, target: string }>
  }
  ```
- 响应: `{ code: 200, message: "success", data: { id: number, aiStatus: 1 } }`

**关键代码位置**：
- 前端: `frontend-vue/src/views/teacher/TrainingCreate.vue`
- 后端: `backend-core/src/main/java/com/smartconstruct/backend_core/controller/TeacherTemplateController.java`

### 2. 实训节点流转链路
```
学生访问 /student/training-execute?taskId={taskId}
  → 前端调用 GET /api/student/training-tasks/{taskId}/participation
  → 后端 StudentTrainingController.getParticipation()
     → 查询 BizTrainingParticipation
     → 查询 BizTrainingTask → 获取 templateId
     → 查询 WfTrainingTemplate → 获取 standardPayloadJson
     → 查询 WfStudentActivityState → 获取 currentNodeId
  → 前端渲染当前节点 (TrainingExecute.vue)
  → 学生完成后调用 POST /api/student/training-tasks/{participationId}/next
  → 后端更新 WfStudentActivityState.currentNodeId
  → 若为 END 节点，更新 BizTrainingParticipation.status=2
```

**交互契约**：
- 开始实训: `POST /api/student/training-tasks/{participationId}/start`
  - 请求体: `{ startNodeId: string }`
- 下一节点: `POST /api/student/training-tasks/{participationId}/next`
  - 请求体: `{ nextNodeId: string, isEnd?: boolean }`

**关键代码位置**：
- 前端: `frontend-vue/src/views/student/TrainingExecute.vue`
- 后端: `backend-core/src/main/java/com/smartconstruct/backend_core/controller/StudentTrainingController.java`

### 3. WebSocket实时通信链路
```
教师进入监控页 → 建立 WebSocket 连接 → 监听学生进度
学生进入实训页 → 建立 WebSocket 连接 → 发送状态更新
教师推进节点 → 后端更新全局状态 → 广播到所有学生
学生接收消息 → 自动刷新当前节点
```

**关键代码位置**：
- 后端: `backend-core/src/main/java/com/smartconstruct/backend_core/websocket/TrainingWebSocketHandler.java`
- 前端: `frontend-vue/src/utils/websocket.ts`

---

## 🚨 第三方依赖与非预期设计排雷

### 前端依赖 (package.json)
| 依赖包 | 版本 | 用途 | 引入位置 |
|--------|------|------|----------|
| @vue-flow/core | ^1.48.2 | 流程图编排核心 | TrainingCreate.vue |
| @vue-flow/background | ^1.3.2 | 流程图背景网格 | TrainingCreate.vue |
| @vue-flow/controls | ^1.1.3 | 流程图缩放/平移控件 | TrainingCreate.vue |
| naive-ui | ^2.44.1 | UI 组件库 | 全局 |
| nprogress | ^0.2.0 | 路由进度条 | router/guard.ts |
| echarts | ^6.0.0 | 数据可视化 | 能力分析页面 |
| simple-mind-map | ^0.14.0-fix.2 | 思维导图组件 | MindMapEditor.vue |
| @fortawesome/fontawesome-free | ^7.2.0 | 图标库 | 全局 |
| axios | ^1.15.2 | HTTP客户端 | services/api.ts |
| pinia | ^3.0.4 | 状态管理 | stores/ |
| vue-router | ^5.0.6 | 路由管理 | router/ |

**非预期发现**：
- 使用 `@vue-flow/*` 系列实现可视化编排，而非自定义 Canvas
- 使用 `naive-ui` 作为 UI 组件库
- 埋点相关 hook `useTelemetry.ts` 存在但为空实现

### 后端依赖 (pom.xml)
| 依赖包 | 版本 | 用途 |
|--------|------|------|
| mybatis-plus-boot-starter | 3.5.5 | ORM 增强 |
| jjwt-api/jjwt-impl/jjwt-jackson | 0.12.5 | JWT 认证 |
| hutool-all | 5.8.26 | 工具库 |
| spring-boot-starter-webflux | 2.7.18 | WebFlux (未实际使用) |
| spring-boot-starter-websocket | 2.7.18 | WebSocket |
| spring-boot-starter-data-jpa | 2.7.18 | JPA (仅用于DDL) |

**非预期发现**：
- 同时引入 JPA 和 MyBatis-Plus，但实际仅使用 MyBatis-Plus
- WebFlux 依赖已引入但无相关实现代码
- `@OperationLog` 注解已定义，但功能未完全实现

### AI 引擎依赖 (requirements.txt)
| 依赖包 | 版本 | 用途 |
|--------|------|------|
| fastapi | 0.115.0 | Web 框架 |
| uvicorn | 0.32.0 | ASGI 服务器 |
| langgraph | 0.2.0 | LangGraph (未实际使用) |
| langchain-openai | 0.2.0 | LangChain OpenAI (未实际使用) |

**非预期发现**：
- LangGraph 和 LangChain-OpenAI 已引入，但 AI 引擎仅实现健康检查接口
- 无实际的 AI 评分或辅导逻辑

---

## ⚠️ 客观架构隐患预警

### 1. 高并发 AI 调用
**问题**：
- 后端无任何调用 AI 引擎的代码，因此无相关并发控制
- 即使实现，当前设计是同步阻塞调用模式，无超时/熔断/降级机制
- 无请求队列或批量处理机制

**风险等级**：高 (一旦接入真实 AI 服务)

### 2. 数据一致性
**问题**：
- `StudentTrainingController.next()` 中更新 `BizTrainingParticipation` 和 `WfStudentActivityState` 无事务保护
- 若中途失败可能导致状态不一致
- 无分布式锁机制，并发更新同一学生状态可能产生竞态条件

**关键代码**：`backend-core/src/main/java/com/smartconstruct/backend_core/controller/StudentTrainingController.java`

**风险等级**：中

### 3. 长连接状态同步
**问题**：
- WebSocket 依赖已引入且有实现
- 教师端实时监控学生进度通过 WebSocket 推送实现

**风险等级**：低

### 4. 其他隐患
- 前端 API 请求超时固定 15 秒，无动态调整机制
- 后端 CORS 配置允许 `*` 通配符，生产环境有安全风险
- 无数据库连接池监控和告警机制
- 模板 JSON 直接存储为 TEXT 字段，无版本管理和迁移机制

---

## 📚 详细文档

- [后端详细文档](./backend-core/BACKEND_README.md) - 后端核心类、API接口、数据库设计
- [前端详细文档](./frontend-vue/FRONTEND_README.md) - 前端组件、状态管理、路由系统

---

## 🤝 贡献指南

### 开发流程

1. Fork 项目仓库
2. 创建特性分支：`git checkout -b feature/xxx`
3. 提交代码：`git commit -m "feat: xxx"`
4. 推送到远程分支：`git push origin feature/xxx`
5. 创建 Pull Request

### 代码规范

- **前端**：遵循 Vue 官方风格指南，使用 TypeScript，严格模式
- **后端**：遵循 Spring Boot 代码规范，使用 Lombok
- **Python**：遵循 PEP 8 规范

### 提交规范

使用 Conventional Commits 格式：

- `feat:` - 新增功能
- `fix:` - 修复 Bug
- `docs:` - 文档更新
- `style:` - 代码格式
- `refactor:` - 重构代码
- `test:` - 测试用例

---

## 📄 许可证

MIT License

---

## 📧 联系我们

如有问题或建议，请提交 Issue 或联系开发团队。

---

**AI SmartConstruct Lab** - 让智能教学更简单 ✨