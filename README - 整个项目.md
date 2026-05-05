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
| 前端 | Vue 3 + TypeScript | Vue 3.4+ |
| 前端构建 | Vite | Vite 8.x |
| 状态管理 | Pinia | Pinia 2.x |
| 路由 | Vue Router | Vue Router 4.x |
| 后端 | Spring Boot | Spring Boot 2.7.x |
| 数据库 | MySQL | MySQL 8.x |
| AI引擎 | Python + FastAPI | FastAPI 0.110+ |
| 容器 | Docker Compose | Docker Compose 24.0+ |

---

## 📁 目录结构

```
ai-smartconstruct-lab/
├── frontend-vue/              # Vue 3 前端应用
│   ├── src/
│   │   ├── components/        # 公共组件
│   │   ├── hooks/             # 组合式函数
│   │   ├── router/            # 路由配置
│   │   ├── services/          # 服务层
│   │   ├── stores/            # 状态管理
│   │   ├── utils/             # 工具函数
│   │   └── views/             # 页面组件
│   └── package.json
├── backend-core/              # Spring Boot 后端核心
│   ├── src/main/java/         # Java 源代码
│   └── pom.xml
├── ai-engine/                 # Python AI 引擎
│   ├── src/                   # Python 源代码
│   └── requirements.txt
├── infrastructure/            # 基础设施配置
│   └── docker-compose.yml
└── README.md
```

### 模块职责

| 模块 | 职责 |
|------|------|
| `frontend-vue` | 用户界面展示，包含教师工作台、学生学习空间、管理员后台 |
| `backend-core` | 核心业务逻辑、用户认证、任务流转控制、数据持久化 |
| `ai-engine` | 量化评分引擎、苏格拉底式智能辅导、大模型交互 |
| `infrastructure` | Docker 容器编排、数据库配置 |

---

## 🚀 快速开始

### 环境要求

确保你的开发环境已安装以下软件：

- Java 21 (配置 JAVA_HOME)
- Node.js 20+
- Python 3.12
- MySQL 8.x
- Docker Desktop

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
- 创建数据库：`CREATE DATABASE ai_smartconstruct CHARACTER SET utf8mb4;`
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
  - 数据库名：`ai_smartconstruct`
  - 用户名：`root`
  - 密码：`123456`
  - 端口：`3306`

### 前端配置

前端配置文件：
- `frontend-vue/.env` - 环境变量
- API 基础路径配置

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
- 🔧 **实训编排** - 可视化流程设计
- 📚 **班级课程管理** - 班级与课程关联管理
- 📊 **能力分析** - 学生能力画像分析
- 🎥 **直播监控** - 实时监控学生实训状态

### 学生功能

- 📖 **课程列表** - 选修课程查看
- 📝 **我的作业** - 作业提交与批改查看
- 💻 **实训中心** - 沉浸式实训环境
- 🏠 **学习空间** - 个人学习数据汇总

### 管理员功能

- 👥 **用户管理** - 教师、学生账号管理
- 🏢 **机构管理** - 学校/院系组织架构
- 📦 **课程管理** - 课程模板管理
- 🔧 **系统监控** - 服务状态监控

---

## 📡 API 接口

### 后端 API

| 模块 | 基础路径 | 说明 |
|------|---------|------|
| 认证 | `/api/auth` | 登录、注册、Token刷新 |
| 用户 | `/api/users` | 用户信息管理 |
| 实训 | `/api/training` | 实训编排与管理 |
| 作业 | `/api/homework` | 作业提交与批改 |

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

## 📧 联系我们

如有问题或建议，请提交 Issue 或联系开发团队。

---

**AI SmartConstruct Lab** - 让智能教学更简单 ✨
