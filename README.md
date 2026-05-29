# AI SmartConstruct Lab

基于大模型与松耦合架构的智能教学实训系统。支持可视化工作流编排，以及具备真实数学逻辑的多维量化评分引擎。

## 📋 项目概览

AI SmartConstruct Lab 是一个面向教育领域的智能实训平台，旨在为教师和学生提供沉浸式的实训体验。系统采用微服务架构，分为前端展示层、后端业务层和AI引擎层。

### 核心特性

- 🎨 **可视化工作流编排** - 教师可通过拖拽方式编排实训流程（基于 Vue Flow）
- 🧠 **AI驱动的智能评分** - 基于大语言模型的量化评分引擎（DeepSeek API）
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
| AI模型 | DeepSeek API | deepseek-v4-flash |
| 容器 | Docker Compose | 24.0+ |

---

## 📁 目录结构

```
ai-smartconstruct-lab/
├── frontend-vue/              # Vue 3 前端应用
│   ├── src/
│   │   ├── assets/            # 静态资源（样式、音频、图片）
│   │   ├── components/        # 公共组件（布局、图标、AI助手）
│   │   ├── hooks/             # 组合式函数（认证、实训、WebSocket、智能助手）
│   │   ├── router/            # 路由配置（模块划分：auth、teacher、student、admin、training、homework）
│   │   ├── services/          # 服务层（API调用：auth、training、homework、assistant等）
│   │   ├── stores/            # 状态管理（Pinia：auth、training、homework、assistant、app）
│   │   ├── utils/             # 工具函数（format、storage、validate、websocket、training-flow）
│   │   ├── views/             # 页面组件
│   │   ├── rules/             # 开发规范（.cursorrules、SKILL.md、PROMPT.md）
│   │   ├── App.vue            # 根组件
│   │   ├── main.ts            # 应用入口
│   │   └── env.d.ts           # TypeScript环境声明
│   ├── public/                # 静态资源（直接拷贝至dist）
│   └── package.json
├── backend-core/              # Spring Boot 后端核心
│   ├── src/main/java/com/smartconstruct/backend_core/
│   │   ├── annotation/        # 自定义注解（OperationLog）
│   │   ├── aspect/            # AOP切面（OperationLogAspect）
│   │   ├── config/            # 配置类（SecurityConfig、WebSocketConfig、AsyncConfig等）
│   │   ├── controller/        # REST API控制器（Auth、Teacher、Student、Admin等）
│   │   ├── dto/               # 数据传输对象（ApiResult、PageResult、VO/DTO类）
│   │   ├── entity/            # 数据库实体类（SysUser、BizStudent、WfTrainingTemplate等）
│   │   ├── mapper/            # MyBatis映射接口
│   │   ├── service/           # 业务服务层（接口+实现）
│   │   │   └── impl/          # 服务实现类（含AiEngineClientImpl）
│   │   ├── task/              # 定时任务（AiTimeoutDetectionTask）
│   │   ├── util/              # 工具类（JwtUtil、Java8Compat）
│   │   ├── websocket/         # WebSocket相关（TrainingWebSocketHandler）
│   │   └── BackendCoreApplication.java  # 启动类
│   ├── src/main/resources/    # 配置文件
│   └── pom.xml
├── ai-engine/                 # Python AI 引擎
│   ├── agents/                # AI代理（评分引擎、苏格拉底辅导）
│   ├── routers/               # API路由（process、retry、health）
│   ├── services/              # 业务服务（callback、job_store、llm_client）
│   ├── models/                # 数据模型（schemas）
│   ├── main.py                # 启动入口
│   ├── cache_models.py        # 模型缓存
│   ├── .env                   # 环境变量配置
│   └── requirements.txt       # 依赖列表
├── infrastructure/            # 基础设施配置
│   ├── docker-compose.yml     # Docker Compose配置
│   └── db_mysql.txt           # 数据库DDL脚本
├── .agents/                   # 智能助手配置
│   └── skills/                # 技能定义
├── AGENTS.md                  # 智能助手配置说明
├── CLAUDE.md                  # Claude配置说明
└── README.md
```

### 模块职责

| 模块 | 职责 |
|------|------|
| `frontend-vue` | 用户界面展示，包含教师工作台、学生学习空间、管理员后台、实训编排器、动态节点执行系统、AI浮动助手、模拟IDE、同伴互评、思维导图练习、教师点评 |
| `backend-core` | 核心业务逻辑、用户认证、任务流转控制、数据持久化、工作流引擎、WebSocket实时通信、题库管理、课程管理、系统运维、AI引擎客户端 |
| `ai-engine` | 量化评分引擎、苏格拉底式智能辅导、大模型交互(DeepSeek)、向量存储(RAG)、异步任务处理 |
| `.agents` | 智能助手技能定义，支持代码诊断、架构优化、交互式审查等功能 |

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

#### 2. 实训编排模块 (Training Orchestration)

**核心类**:
- `TeacherTemplateController` - 教师模板控制器，管理实训模板CRUD
- `TeacherTrainingTaskController` - 任务下发控制器
- `TeacherTrainingController` - 教师实训监控控制器
- `StudentTrainingController` - 学生实训控制器
- `StudentTrainingFlowController` - 学生实训流程控制器
- `TrainingWebSocketHandler` - WebSocket处理器，处理实训实时通信
- `TrainingHandshakeInterceptor` - WebSocket握手拦截器，验证连接身份
- `AiEngineClientImpl` - AI引擎客户端，处理与AI引擎的通信

**核心实体**:
- `WfTrainingTemplate` - 实训模板（新增aiJobId字段）
- `WfNodeDef` - 节点定义
- `BizTrainingTask` - 实训任务
- `BizTrainingParticipation` - 学生实训参与记录
- `WfStudentActivityState` - 学生活动状态
- `WfGlobalActivityState` - 全局活动状态
- `WfNodeAiStatus` - 节点AI处理状态

#### 3. AI引擎通信模块

**核心类**:
- `AiEngineClientImpl` - AI引擎客户端实现
  - `triggerAiPipeline()` - 触发AI处理流程
  - `retryFailedNodes()` - 重试失败节点
  - `queryJobStatus()` - 查询任务状态

**定时任务**:
- `AiTimeoutDetectionTask` - AI处理超时检测，定期检查AI引擎任务状态

#### 4. 配置类

**核心配置类**:
- `SecurityConfig` - Spring Security安全配置
- `WebSocketConfig` - WebSocket配置
- `MybatisPlusConfig` - MyBatis-Plus配置
- `AsyncConfig` - 异步线程池配置
- `GlobalExceptionHandler` - 全局异常处理
- `InternalServiceTokenFilter` - 服务间认证过滤器

### AI引擎核心类 (ai-engine)

#### 1. 路由模块
- `orchestration.py` - 编排API路由
  - `/api/orchestration/process` - 处理实训模板
  - `/api/orchestration/retry` - 重试失败节点
  - `/api/orchestration/status/{job_id}` - 查询任务状态

#### 2. 服务模块
- `llm_client.py` - LLM客户端（DeepSeek API）
- `job_store.py` - 任务状态存储（内存）
- `callback.py` - 回调服务（后端通知）

#### 3. 数据模型
- `schemas.py` - 请求/响应数据模型
  - `ProcessRequest` - 处理请求
  - `RetryRequest` - 重试请求
  - `JobStatusResponse` - 任务状态响应

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

# 配置环境变量（修改 .env 文件）
# LLM_BASE_URL=https://api.deepseek.com
# LLM_API_KEY=your_api_key
# LLM_MODEL_NAME=deepseek-v4-flash
# SERVICE_TOKEN=your_service_token

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
- AI引擎配置：
  - 服务地址：`http://localhost:8000`
  - 服务令牌：`service-token-123456`

### AI引擎配置

AI引擎使用 `.env` 文件配置：

```env
# LLM配置
LLM_BASE_URL=https://api.deepseek.com
LLM_API_KEY=your_deepseek_api_key
LLM_MODEL_NAME=deepseek-v4-flash

# 服务配置
SERVICE_TOKEN=service-token-123456
BACKEND_URL=http://localhost:8080
```

### 前端配置

前端配置文件：
- `frontend-vue/src/services/api.ts` - API 基础路径配置
- 后端接口地址：`http://localhost:8080`
- 请求超时时间：15秒

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
| AI回调 | `/api/internal/ai-callback` | AI引擎回调接口（内部） |

### AI 引擎 API

| 接口 | 路径 | 说明 |
|------|------|------|
| 处理模板 | POST `/api/orchestration/process` | 处理实训模板 |
| 重试节点 | POST `/api/orchestration/retry` | 重试失败节点 |
| 查询状态 | GET `/api/orchestration/status/{job_id}` | 查询任务状态 |
| 健康检查 | GET `/health` | 服务状态检查 |

---

## 🔗 系统流转真实链路

### 1. 实训任务发布链路
```
前端 (TrainingCreate.vue) 
  → buildCanvasData() 构建 { orchestration_id, nodes, edges }
  → createTemplate() 调用 POST /api/teacher/templates
  → 后端 TeacherTemplateController.create() 
     → 保存 WfTrainingTemplate (rawCanvasJson 字段)
     → AiEngineClientImpl.triggerAiPipeline() 调用AI引擎
     → AI引擎返回 job_id，存储到 aiJobId 字段
     → 返回模板 ID
  → 前端跳转到 /teacher/training-manage
```

### 2. AI引擎处理链路
```
后端 AiEngineClientImpl.triggerAiPipeline()
  → POST http://localhost:8000/api/orchestration/process
  → AI引擎接收请求，生成 UUID job_id
  → 创建任务，存储到 job_store
  → 异步调用 LLM 处理模板
  → 处理完成后回调 POST /api/internal/ai-callback
```

### 3. 超时检测链路
```
AiTimeoutDetectionTask (定时任务)
  → 查询 aiStatus=1 且超时的模板
  → 使用存储的 aiJobId 查询 AI引擎状态
  → GET http://localhost:8000/api/orchestration/status/{job_id}
  → 根据状态更新模板 aiStatus
```

---

## 📚 详细文档

- [后端详细文档](./backend-core/BACKEND_README.md) - 后端核心类、API接口、数据库设计、AI引擎客户端集成
- [前端详细文档](./frontend-vue/FRONTEND_README.md) - 前端组件、状态管理、路由系统、AI浮动助手模块
- [AI引擎文档](./ai-engine/AI-ENGINE_README.md) - AI引擎架构、API接口、DeepSeek配置、异步任务处理

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