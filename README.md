# AI SmartConstruct Lab

基于大模型与松耦合架构的智能教学实训系统。支持可视化工作流编排，以及具备真实数学逻辑的多维量化评分引擎。

## 📋 项目概览

AI SmartConstruct Lab 是一个面向教育领域的智能实训平台，旨在为教师和学生提供沉浸式的实训体验。系统采用微服务架构，分为前端展示层、后端业务层和AI引擎层。

### 核心特性

- 🎨 **可视化工作流编排** - 教师可通过拖拽方式编排实训流程
- 🧠 **AI驱动的智能评分** - 基于大语言模型的量化评分引擎
- 💬 **苏格拉底式辅导** - 针对学生报错代码的引导式答疑
- 🔒 **多级权限管理** - 支持教师、学生、管理员多角色
- 📊 **实时数据看板** - 班级整体数据与个体能力分析

---

## 🛠️ 技术栈

| 模块 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + TypeScript | Vue 3.5+ |
| 前端构建 | Vite | Vite 8.x |
| 状态管理 | Pinia | Pinia 3.x |
| 路由 | Vue Router | Vue Router 5.x |
| 图表 | ECharts | ECharts 6.x |
| 后端 | Spring Boot | Spring Boot 2.7.x |
| ORM | JPA + MyBatis-Plus | MyBatis-Plus 3.5.5 |
| 数据库 | MySQL | MySQL 8.x |
| AI引擎 | Python + FastAPI | FastAPI 0.110+ |
| 容器 | Docker Compose | Docker Compose 24.0+ |

---

## 📁 目录结构

```
ai-smartconstruct-lab/
├── frontend-vue/              # Vue 3 前端应用
│   ├── src/
│   │   ├── assets/            # 静态资源
│   │   ├── components/        # 公共组件
│   │   ├── hooks/             # 组合式函数
│   │   ├── router/            # 路由配置
│   │   ├── services/          # 服务层
│   │   ├── stores/            # 状态管理
│   │   ├── utils/             # 工具函数
│   │   └── views/             # 页面组件
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
├── infrastructure/            # 基础设施配置（可选）
│   └── docker-compose.yml     # Docker 编排配置
└── README.md
```

### 模块职责

| 模块 | 职责 |
|------|------|
| `frontend-vue` | 用户界面展示，包含教师工作台、学生学习空间、管理员后台、实训编排器 |
| `backend-core` | 核心业务逻辑、用户认证、任务流转控制、数据持久化、工作流引擎 |
| `ai-engine` | 量化评分引擎、苏格拉底式智能辅导、大模型交互 |
| `infrastructure` | Docker 容器编排、数据库配置（可选） |

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

#### 1. 基础设施准备

```bash
# 进入基础设施目录
cd infrastructure

# 启动 Docker 服务（如需要）
docker-compose up -d
```

#### 2. 数据库配置

- 连接本地 MySQL（默认端口 3306）
- 创建数据库：`CREATE DATABASE ai-smartconstruct-lab CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
- 配置用户名：`root`，密码：`123456`

#### 3. 后端启动

```bash
cd backend-core

# 编译项目
./mvnw.cmd clean install -DskipTests

# 启动应用
./mvnw.cmd spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

#### 4. AI 引擎启动

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

#### 5. 前端启动

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

## 🎯 功能模块

### 角色划分

| 角色 | 路径前缀 | 功能描述 |
|------|---------|---------|
| 认证模块 | `/auth` | 登录、注册 |
| 管理员 | `/admin` | 用户管理、课程管理、系统监控 |
| 教师 | `/teacher` | 实训编排、班级管理、直播监控 |
| 学生 | `/student` | 课程学习、作业提交、实训参与 |

### 教师功能

- 📋 **工作台概览** - 班级整体数据看板
- 🔧 **实训编排** - 可视化流程设计（基于 Vue Flow）
- 📚 **班级课程管理** - 班级与课程关联管理
- 📊 **能力分析** - 学生能力画像分析（雷达图展示）
- 🎥 **直播监控** - 实时监控学生实训状态
- 📝 **实训发布** - 选择班级下发实训任务

### 学生功能

- 📖 **课程列表** - 选修课程查看
- 📝 **我的作业** - 作业提交与批改查看
- 💻 **实训中心** - 沉浸式实训环境（含 AI 辅导）
- 🏠 **学习空间** - 个人学习数据汇总
- 🔔 **消息通知** - 接收课程和实训通知
- 📊 **实训详情** - 查看实训任务详情

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
| 教师 | `/api/teacher` | 教师工作台、实训管理、课程管理 |
| 学生 | `/api/student` | 学生学习、实训参与、课程选修 |
| 管理员 | `/api/admin` | 用户管理、机构管理、系统运维 |

### AI 引擎 API

| 接口 | 路径 | 说明 |
|------|------|------|
| 评分 | `/api/evaluate` | 量化评分引擎 |
| 辅导 | `/api/tutor` | 苏格拉底式辅导 |
| 健康检查 | `/health` | 服务状态检查 |

---

## 🤝 贡献指南

### 开发流程

1. Fork 项目仓库
2. 创建特性分支：`git checkout -b feature/xxx`
3. 提交代码：`git commit -m "feat: xxx"`
4. 推送到远程分支：`git push origin feature/xxx`
5. 创建 Pull Request

### 代码规范

- **前端**：遵循 Vue 官方风格指南，使用 TypeScript
- **后端**：遵循 Spring Boot 代码规范
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
- 前端: `frontend-vue/src/views/teacher/TrainingCreate.vue:250-334`
- 后端: `backend-core/src/main/java/com/smartconstruct/backend_core/controller/TeacherTemplateController.java:41-61`

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

### 3. AI 引擎调用链路
**未在代码中发现具体实现**
- AI 引擎 (FastAPI) 仅提供了健康检查接口: `GET /api/health`
- 后端无任何 HTTP 客户端调用 AI 引擎的代码
- 仅在模板创建时有 `processTemplateMockAi()` 模拟方法

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
| spring-boot-starter-websocket | 2.7.18 | WebSocket (未实际使用) |

**非预期发现**：
- 同时引入 JPA 和 MyBatis-Plus，但实际仅使用 MyBatis-Plus
- WebFlux 和 WebSocket 依赖已引入但无相关实现代码
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

**关键代码**：`backend-core/src/main/java/com/smartconstruct/backend_core/controller/StudentTrainingController.java:129-182`

**风险等级**：中

### 3. 长连接状态同步
**问题**：
- WebSocket 依赖已引入但无相关实现
- 教师端实时监控学生进度需轮询实现，无推送机制
- 无心跳保活和断线重连机制

**风险等级**：低 (当前轮询可满足基本需求)

### 4. 其他隐患
- 前端 API 请求超时固定 15 秒，无动态调整机制
- 后端 CORS 配置允许 `*` 通配符，生产环境有安全风险
- 无数据库连接池监控和告警机制
- 模板 JSON 直接存储为 TEXT 字段，无版本管理和迁移机制

---

## 📧 联系我们

如有问题或建议，请提交 Issue 或联系开发团队。

---

**AI SmartConstruct Lab** - 让智能教学更简单 ✨
