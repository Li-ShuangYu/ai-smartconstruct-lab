# AI SmartConstruct Lab - AI 引擎服务

> 基于 Python + FastAPI + LangGraph 的智能评分引擎服务，集成 DeepSeek / 阿里云 DashScope 大语言模型，提供量化评分、苏格拉底式辅导、智能问答等 AI 能力。

---

## 📋 项目概述

AI 引擎是 AI SmartConstruct Lab 的核心模块之一，负责处理实训模板的 AI 分析、量化评分计算、智能辅导功能以及代码审查。该服务通过 RESTful API 与后端核心服务通信，支持异步任务处理、回调机制，并基于 LangGraph 实现多 Agent 协作编排。

### 核心功能

| 功能模块 | 说明 |
|---------|------|
| 🧠 **量化评分引擎** | 基于大语言模型的多维量化评分（正确性、完整性、规范性、创新性、复杂度） |
| 💬 **苏格拉底式辅导** | 针对学生代码错误的引导式答疑，不直接给答案，通过提问引导思考 |
| 🔄 **异步任务处理** | 支持长时间运行的 AI 任务（编排处理、代码分析） |
| 📡 **回调通知** | 任务完成后主动通知后端服务（WebHook） |
| ⚡ **实时状态查询** | 支持任务状态轮询（pending/processing/completed/failed） |
| 🔍 **代码审查** | Python 代码语法分析、错误检测、优化建议 |
| ✍️ **自动出题** | 根据知识点自动生成考试题目 |
| 📊 **结构评价** | 思维导图结构分析与评价 |

---

## 🛠️ 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 框架 | FastAPI | 0.115+ |
| 语言 | Python | 3.10+ |
| Agent 框架 | LangGraph | 0.2.0 |
| LLM 集成 | langchain-openai | 0.2.0 |
| 大模型 | DeepSeek API / 阿里云 DashScope | deepseek-v4-flash / qwen-plus |
| HTTP 客户端 | httpx | 0.27+ |
| 环境配置 | python-dotenv | 1.0+ |
| 数据验证 | pydantic | 2.5+ |
| 日志 | structlog | 24.1+ |
| 重试 | tenacity | 8.2+ |
| 测试 | pytest | 8.3+ |
| 属性测试 | hypothesis | 6.112+ |

---

## 📁 目录结构

```
ai-engine/
├── agents/                   # AI 代理模块（多 Agent 协作）
│   ├── __init__.py
│   ├── base_agent.py         # 基础代理抽象类
│   ├── code_agent.py         # 代码分析代理（Python 代码审查、优化建议）
│   ├── exam_agent.py         # 出题代理（自动生成考试题目）
│   ├── struct_agent.py       # 结构分析代理（思维导图评价）
│   ├── supervisor.py         # 监督代理（解析画布、路由到对应 Agent）
│   ├── text_agent.py         # 文本处理代理（理论知识生成、问答）
│   └── video_agent.py        # 视频分析代理（视频内容理解）
│
├── routers/                  # API 路由
│   ├── __init__.py
│   ├── health.py             # 健康检查
│   ├── orchestration.py      # 编排处理路由（核心：process/retry/status）
│   ├── chat.py               # 对话接口（AI 助手聊天）
│   └── ide.py                # IDE 代码分析接口
│
├── services/                 # 业务服务
│   ├── __init__.py
│   ├── llm_service.py        # LLM 客户端（DeepSeek / DashScope 兼容）
│   ├── job_store.py          # 任务状态存储（内存 + 可扩展 Redis）
│   ├── callback.py           # 回调服务（通知后端核心）
│   ├── workflow.py           # 工作流编排执行
│   ├── asset_extractor.py    # 资源提取服务
│   └── merger.py             # 结果合并服务
│
├── models/                   # 数据模型
│   ├── __init__.py
│   ├── schemas.py            # 请求/响应数据模型（Pydantic）
│   ├── agent_outputs.py      # Agent 输出模型
│   └── callback.py           # 回调数据模型
│
├── prompts/                  # 提示词模板
│   ├── __init__.py
│   ├── code_prompts.py       # 代码分析提示词
│   ├── exam_prompts.py       # 出题提示词
│   ├── struct_prompts.py     # 结构分析提示词
│   ├── text_prompts.py       # 文本处理提示词
│   └── video_prompts.py      # 视频分析提示词
│
├── middleware/               # 中间件
│   ├── __init__.py
│   └── rate_limiter.py       # 限流中间件
│
├── tests/                    # 测试目录
│   ├── __init__.py
│   ├── conftest.py           # pytest 配置
│   ├── fixtures/             # 测试数据
│   ├── mocks/               # Mock 实现
│   ├── test_base_agent_and_llm.py      # 基础代理测试
│   ├── test_e2e_smoke.py              # E2E 冒烟测试
│   ├── test_fixture_validation.py     # Fixture 验证
│   ├── test_integration.py             # 集成测试
│   ├── test_integration_orchestration_flow.py  # 编排流程集成测试
│   ├── test_integration_selective_retry.py     # 选择性重试集成测试
│   ├── test_llm_retry.py              # LLM 重试测试
│   ├── test_orchestration_validation.py      # 编排验证测试
│   ├── test_property_ai_flag_skip.py        # AI 标志跳过属性测试
│   ├── test_property_ai_spec_routing.py     # AI Spec 路由属性测试
│   ├── test_property_canvas_roundtrip.py    # 画布往返属性测试
│   ├── test_property_llm_retry_backoff.py  # LLM 重试退避属性测试
│   ├── test_property_phase_validation.py    # 阶段验证属性测试
│   ├── test_property_selective_retry.py     # 选择性重试属性测试
│   ├── test_rate_limiter.py              # 限流测试
│   ├── test_supervisor.py                # 监督代理测试
│   ├── test_supervisor_quick.py          # 监督代理快速测试
│   ├── test_validation_retry.py          # 验证重试测试
│   └── test_workflow_services.py         # 工作流服务测试
│
├── main.py                   # 启动入口（FastAPI 应用）
├── cache_models.py           # 模型缓存（向量模型等）
├── requirements.txt          # 依赖列表
└── AI-ENGINE_README.md
```

---

## 🏗️ 核心架构

### 架构层次

```
┌─────────────────────────────────────────────────────────────┐
│                     外部调用层                              │
│              backend-core (Spring Boot)                    │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP REST API
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     API 路由层                              │
│              routers/                                      │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │ /process      │  │   /retry       │  │ /status/{id} │  │
│  │  处理模板      │  │   重试节点      │  │ 查询状态     │  │
│  └────────────────┘  └────────────────┘  └──────────────┘  │
│  ┌────────────────┐  ┌────────────────┐                    │
│  │ /chat         │  │ /ide           │                    │
│  │  AI 对话      │  │  代码分析      │                    │
│  └────────────────┘  └────────────────┘                    │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     服务层                                  │
│              services/                                     │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │  llm_service   │  │   job_store    │  │   callback   │  │
│  │  LLM调用      │  │   任务存储     │  │   回调通知   │  │
│  └────────────────┘  └────────────────┘  └──────────────┘  │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │   workflow     │  │ asset_extractor│  │   merger     │  │
│  │  工作流编排    │  │   资源提取     │  │   结果合并   │  │
│  └────────────────┘  └────────────────┘  └──────────────┘  │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     AI 代理层                               │
│              agents/ (LangGraph 驱动)                      │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │ supervisor     │  │   code_agent   │  │  exam_agent  │  │
│  │  监督路由      │  │   代码分析     │  │   自动出题   │  │
│  └────────────────┘  └────────────────┘  └──────────────┘  │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │  struct_agent  │  │   text_agent   │  │  video_agent │  │
│  │   结构分析     │  │   文本处理     │  │   视频分析   │  │
│  └────────────────┘  └────────────────┘  └──────────────┘  │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     外部服务                                 │
│         DeepSeek API / 阿里云 DashScope (大语言模型)        │
│         Qdrant Vector DB (可选，用于 RAG)                   │
└─────────────────────────────────────────────────────────────┘
```

### Agent 路由机制

Supervisor 代理通过 `ai_spec` 进行数据驱动路由：

```
Canvas JSON → Supervisor.detect_ai_nodes_phased() → 根据 ai_spec.target_agent 分发
    │
    ├─ target_agent="TEXT"     → TextAgent
    ├─ target_agent="STRUCT"   → StructAgent
    ├─ target_agent="EXAM"     → ExamAgent
    ├─ target_agent="CODE"     → CodeAgent
    └─ target_agent="VIDEO"    → VideoAgent
```

---

## 🔌 API 接口文档

### 基础路径

服务地址：`http://localhost:8000`

### 接口列表

| 模块 | 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|------|
| 健康检查 | 健康检查 | GET | `/api/health` | 服务状态检查 |
| 编排处理 | 处理模板 | POST | `/api/orchestration/process` | 处理实训模板，触发 AI 分析 |
| 编排处理 | 重试节点 | POST | `/api/orchestration/retry` | 选择性重试失败节点 |
| 编排处理 | 查询状态 | GET | `/api/orchestration/status/{job_id}` | 轮询任务状态 |
| 对话 | 聊天 | POST | `/api/chat/completions` | AI 助手对话接口 |
| IDE | 代码分析 | POST | `/api/ide/analyze` | 代码审查与优化建议 |

### 1. 处理模板 (`POST /api/orchestration/process`)

**请求**:
```http
POST /api/orchestration/process
Content-Type: application/json
X-Service-Token: <service-token>

{
  "template_id": "2060045697011781633",
  "canvas_json": {
    "orchestration_id": "orch_xxx",
    "phases": [
      {
        "phase_id": "phase_1",
        "phase_name": "课前阶段",
        "sort_num": 1,
        "nodes": [...],
        "edges": [...]
      }
    ]
  }
}
```

**支持的画布结构**:
- **新结构**（推荐）：`{ orchestration_id, phases[] }` 带阶段的结构
- **旧结构**（兼容）：`{ orchestration_id, nodes[], edges[] }` 扁平结构

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| template_id | String | 是 | 实训模板 ID |
| canvas_json | Object | 是 | 画布 JSON 数据 |

**响应**:
```json
{
  "success": true,
  "job_id": "550e8400-e29b-41d4-a716-446655440000",
  "message": "任务已创建"
}
```

### 2. 重试节点 (`POST /api/orchestration/retry`)

**请求**:
```http
POST /api/orchestration/retry
Content-Type: application/json
X-Service-Token: <service-token>

{
  "template_id": "2060045697011781633",
  "failed_node_ids": ["node_1", "node_3"],
  "canvas_json": { ... }
}
```

**响应**:
```json
{
  "success": true,
  "job_id": "550e8400-e29b-41d4-a716-446655440001",
  "message": "重试任务已创建"
}
```

### 3. 查询任务状态 (`GET /api/orchestration/status/{job_id}`)

**请求**:
```http
GET /api/orchestration/status/{job_id}
X-Service-Token: <service-token>
```

**响应**:
```json
{
  "success": true,
  "job_id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "completed",
  "progress": 100,
  "result": {
    "standard_payload": {...},
    "node_results": [...]
  },
  "error": null,
  "created_at": "2024-01-01T12:00:00Z",
  "updated_at": "2024-01-01T12:05:00Z"
}
```

**状态说明**:
| 状态值 | 说明 |
|--------|------|
| `pending` | 等待处理 |
| `processing` | 处理中 |
| `completed` | 处理完成 |
| `failed` | 处理失败 |

### 4. 健康检查 (`GET /api/health`)

**请求**:
```http
GET /api/health
```

**响应**:
```json
{
  "status": "healthy",
  "service": "ai-engine",
  "version": "1.0.0",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### 5. AI 对话 (`POST /api/chat/completions`)

**请求**:
```http
POST /api/chat/completions
Content-Type: application/json
X-Service-Token: <service-token>

{
  "session_id": "session_xxx",
  "messages": [
    {"role": "user", "content": "解释一下什么是Python闭包"}
  ],
  "max_tokens": 1000
}
```

**响应**:
```json
{
  "success": true,
  "session_id": "session_xxx",
  "message": {"role": "assistant", "content": "闭包是指..."}
}
```

### 6. 代码分析 (`POST /api/ide/analyze`)

**请求**:
```http
POST /api/ide/analyze
Content-Type: application/json
X-Service-Token: <service-token>

{
  "code": "def fib(n):\n    if n <= 1:\n        return n\n    return fib(n-1) + fib(n-2)",
  "language": "python"
}
```

**响应**:
```json
{
  "success": true,
  "analysis": {
    "issues": [...],
    "suggestions": [...],
    "complexity": "O(2^n)"
  }
}
```

---

## 📡 回调机制

### 回调接口

AI 引擎完成任务后，会主动回调后端核心服务：

**回调地址**: `POST http://localhost:8080/api-smart/api/internal/ai-callback`

**请求体**:
```json
{
  "job_id": "550e8400-e29b-41d4-a716-446655440000",
  "template_id": "2060045697011781633",
  "status": "completed",
  "result": {
    "standard_payload": {...},
    "node_results": [...]
  },
  "error": null
}
```

**认证**: 回调请求携带 `X-Service-Token` 头进行身份验证。

---

## ⚙️ 配置说明

### 环境变量配置

在 `.env` 文件中配置以下环境变量：

```env
# LLM 配置（二选一：DeepSeek 或 DashScope）
LLM_PROVIDER=deepseek  # deepseek 或 dashscope

# DeepSeek 配置
DEEPSEEK_API_KEY=your_deepseek_api_key
DEEPSEEK_BASE_URL=https://api.deepseek.com
DEEPSEEK_MODEL_NAME=deepseek-v4-flash

# 阿里云 DashScope 配置（可选）
DASHSCOPE_API_KEY=your_dashscope_api_key
DASHSCOPE_MODEL_NAME=qwen-plus

# 服务配置
SERVICE_TOKEN=smartconstruct-internal-token
BACKEND_URL=http://localhost:8080/api-smart

# 服务端口
PORT=8000

# 限流配置
MAX_ACTIVE_JOBS=50
RATE_LIMIT_MAX_REQUESTS=10
RATE_LIMIT_WINDOW_SECONDS=60

# 日志级别
LOG_LEVEL=INFO
```

### 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| LLM_PROVIDER | LLM 服务商：`deepseek` 或 `dashscope` | deepseek |
| DEEPSEEK_API_KEY | DeepSeek API 密钥 | - |
| DASHSCOPE_API_KEY | 阿里云 DashScope API 密钥 | - |
| SERVICE_TOKEN | 服务间认证令牌（需与后端一致） | smartconstruct-internal-token |
| BACKEND_URL | 后端核心服务地址 | http://localhost:8080/api-smart |
| PORT | 服务监听端口 | 8000 |
| MAX_ACTIVE_JOBS | 最大并发任务数 | 50 |

---

## 🚀 快速开始

### 环境要求

- Python 3.10+
- pip 包管理工具

### 安装依赖

```bash
cd ai-engine

# 创建虚拟环境
python -m venv venv

# 激活虚拟环境
# Windows
.\venv\Scripts\activate
# Linux/Mac
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

### 配置环境变量

创建 `.env` 文件：

```bash
echo "LLM_PROVIDER=deepseek" > .env
echo "DEEPSEEK_API_KEY=your_api_key_here" >> .env
echo "SERVICE_TOKEN=smartconstruct-internal-token" >> .env
echo "BACKEND_URL=http://localhost:8080/api-smart" >> .env
```

### 启动服务

```bash
# 开发模式（自动重载）
uvicorn main:app --reload --port 8000

# 生产模式
uvicorn main:app --host 0.0.0.0 --port 8000
```

服务启动后访问: http://localhost:8000

---

## 🧪 测试

### 运行测试

```bash
# 运行所有测试
pytest

# 运行特定测试文件
pytest tests/test_supervisor.py

# 运行冒烟测试
pytest tests/test_e2e_smoke.py

# 生成覆盖率报告
pytest --cov=. --cov-report=html
```

### 测试分类

| 测试类型 | 文件 | 说明 |
|---------|------|------|
| 单元测试 | `test_base_agent_and_llm.py`, `test_supervisor.py` | 单个组件测试 |
| 集成测试 | `test_integration.py`, `test_integration_orchestration_flow.py` | 多组件协作测试 |
| 属性测试 | `test_property_*.py` | 基于 Hypothesis 的属性测试 |
| 冒烟测试 | `test_e2e_smoke.py` | E2E 端到端验证 |
| 限流测试 | `test_rate_limiter.py` | 限流中间件测试 |

---

## 📊 核心服务说明

### 1. LLM 服务 (llm_service.py)

**功能**: 封装 LLM API 调用，支持 DeepSeek 和阿里云 DashScope 双后端。

**核心方法**:
- `chat_completion(messages, model_name)` - 调用聊天接口
- `generate_response(prompt)` - 生成文本响应
- `retry_with_backoff(func, max_retries=3)` - 带退避的重试机制

### 2. 任务存储 (job_store.py)

**功能**: 内存中的任务状态存储，支持任务创建、查询、更新。

**核心方法**:
- `create_job(template_id, canvas_json)` - 创建任务
- `update_job_status(job_id, status, result)` - 更新任务状态
- `get_job(job_id)` - 获取任务信息
- `delete_job(job_id)` - 删除任务

> **生产建议**: 当前使用内存存储，重启后数据丢失。生产环境建议使用 Redis 或数据库持久化。

### 3. 工作流服务 (workflow.py)

**功能**: 编排工作流执行引擎。

**核心方法**:
- `execute_orchestration_workflow(template_id, canvas_json)` - 执行编排工作流
- `execute_retry_workflow(template_id, failed_node_ids, canvas_json)` - 执行重试工作流

### 4. 监督代理 (supervisor.py)

**功能**: 解析画布 JSON，检测 AI 节点，生成执行计划。

**核心方法**:
- `detect_ai_nodes_phased(canvas_json)` - 检测所有阶段中的 AI 节点
- `_should_process_node(node, ai_spec)` - 判断节点是否需要 AI 处理
- `generate_execution_plan(canvas_json)` - 生成执行计划

**AI 节点判定条件**:
1. `ai_spec` 不为空
2. `ai_spec.target_agent` 在 `VALID_AGENT_TYPES` 中
3. 满足以下任一条件：
   - `ai_spec.ai_flags` 中有任一标志为 True（且未被教师覆盖）
   - 节点配置中 `source_mode="ai"`

### 5. 代码代理 (code_agent.py)

**功能**: Python 代码分析、错误检测、优化建议。

**分析维度**:
- 语法错误检测
- 代码复杂度分析
- 性能优化建议
- 代码规范检查（PEP 8）

### 6. 出题代理 (exam_agent.py)

**功能**: 根据知识点自动生成考试题目。

**支持题型**:
- 单选题
- 多选题
- 判断题
- 填空题
- 简答题
- 编程题

### 7. 结构代理 (struct_agent.py)

**功能**: 思维导图结构分析与评价。

**评价维度**:
- 结构完整性
- 层次合理性
- 知识点覆盖度
- 逻辑连贯性

---

## 🔒 安全说明

### 服务间认证

- 所有 API 接口（除健康检查外）都需要携带 `X-Service-Token` 头
- Token 值在 `.env` 文件中配置，需与后端核心服务保持一致
- 未携带或无效 Token 的请求将返回 401 Unauthorized

### 请求格式

- 所有请求必须使用 `Content-Type: application/json`
- 请求体字段需符合 Pydantic 模型验证
- 无效请求格式将返回 422 Unprocessable Entity

### 限流保护

- `process` 接口限制：60 秒内最多 10 次请求
- 最大并发任务数：50 个
- 超过限制返回 429 Too Many Requests

---

## 📈 性能考虑

### 异步处理

- AI 任务处理可能耗时较长，采用异步方式执行
- 任务提交后立即返回 job_id，后台异步处理
- 通过状态查询接口获取处理进度和结果

### 重试机制

- LLM 调用失败自动重试（最多 3 次）
- 指数退避策略避免过度重试
- 选择性重试：只重试失败的节点，不重复处理成功节点

### 内存管理

- 任务存储使用内存字典，重启后数据会丢失
- 定期清理已完成的任务记录（默认保留 24 小时）
- 生产环境建议使用 Redis 持久化

---

## 🐛 常见问题

### 1. 连接 LLM API 失败

**问题描述**:
```
ConnectionError: Failed to connect to LLM API
```

**解决方案**:
- 检查网络连接
- 确认 API Key 配置正确
- 验证 LLM_BASE_URL 配置正确
- 检查防火墙是否允许出站连接

### 2. 回调失败

**问题描述**:
```
Callback failed: Connection refused
```

**解决方案**:
- 检查后端核心服务是否启动
- 确认 BACKEND_URL 配置正确
- 验证服务间网络连通性
- 检查 X-Service-Token 是否匹配

### 3. 任务状态异常

**问题描述**: 任务长时间处于 `processing` 状态

**解决方案**:
- 检查 LLM API 调用是否成功
- 查看服务日志定位问题
- 考虑增加任务超时机制
- 使用状态查询接口获取详细信息

### 4. 限流被触发

**问题描述**: 返回 429 Too Many Requests

**解决方案**:
- 减少请求频率
- 合并批量请求
- 联系管理员调整限流参数

---

## 🆕 新增功能（2024更新）

### 1. 多Agent架构升级
- **LangGraph 集成**：基于 LangGraph 实现多 Agent 协作编排
- **Agent 路由机制**：Supervisor 代理根据 `ai_spec.target_agent` 智能分发任务
- **支持的 Agent 类型**：TextAgent、CodeAgent、ExamAgent、StructAgent、VideoAgent

### 2. 代码分析能力增强
- **Python 代码审查**：语法错误检测、代码复杂度分析、性能优化建议
- **PEP 8 规范检查**：自动检测代码风格问题
- **IDE 集成接口**：支持实时代码分析请求

### 3. 自动出题功能
- **多题型支持**：单选、多选、判断、填空、简答、编程题
- **知识点驱动**：根据知识点自动生成考试题目

### 4. 结构评价模块
- **思维导图分析**：结构完整性、层次合理性、知识点覆盖度、逻辑连贯性评价

### 5. 异步任务处理优化
- **选择性重试**：只重试失败节点，不重复处理成功节点
- **指数退避策略**：LLM 调用失败自动重试（最多 3 次）
- **任务状态轮询**：支持 pending/processing/completed/failed 状态查询

---

## 📚 相关文档

- [项目主文档](../README.md) - 项目整体概述
- [后端文档](../backend-core/BACKEND_README.md) - 后端核心服务
- [前端文档](../frontend-vue/FRONTEND_README.md) - 前端应用

---

**AI SmartConstruct Lab - AI Engine** - 让智能教学更简单 ✨
