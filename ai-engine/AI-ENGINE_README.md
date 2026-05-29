# AI SmartConstruct Lab - AI 引擎服务

基于 Python + FastAPI 的智能评分引擎服务，集成 DeepSeek 大语言模型，提供量化评分、苏格拉底式辅导等 AI 能力。

---

## 📋 项目概述

AI 引擎是 AI SmartConstruct Lab 的核心模块之一，负责处理实训模板的 AI 分析、量化评分计算以及智能辅导功能。该服务通过 RESTful API 与后端核心服务通信，支持异步任务处理和回调机制。

### 核心功能

- 🧠 **量化评分引擎** - 基于大语言模型的多维量化评分
- 💬 **苏格拉底式辅导** - 针对学生代码的引导式答疑
- 🔄 **异步任务处理** - 支持长时间运行的 AI 任务
- 📡 **回调通知** - 任务完成后主动通知后端服务
- ⚡ **实时状态查询** - 支持任务状态轮询

---

## 🛠️ 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 框架 | FastAPI | 0.115+ |
| 语言 | Python | 3.10+ |
| 大模型 | DeepSeek API | deepseek-v4-flash |
| HTTP 客户端 | httpx | 0.27+ |
| 环境配置 | python-dotenv | 1.0+ |
| 数据验证 | pydantic | 2.5+ |

---

## 📁 目录结构

```
ai-engine/
├── agents/                   # AI 代理模块
│   ├── scoring_engine.py     # 量化评分引擎
│   └── socratic_tutor.py     # 苏格拉底式辅导
├── routers/                  # API 路由
│   └── orchestration.py      # 编排处理路由
├── services/                 # 业务服务
│   ├── llm_client.py         # LLM 客户端（DeepSeek API）
│   ├── job_store.py          # 任务状态存储（内存）
│   └── callback.py           # 回调服务
├── models/                   # 数据模型
│   └── schemas.py            # 请求/响应数据模型
├── main.py                   # 启动入口
├── cache_models.py           # 模型缓存（向量模型等）
├── .env                      # 环境变量配置
└── requirements.txt          # 依赖列表
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
│              routers/orchestration.py                      │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │  /process      │  │   /retry       │  │ /status/{id} │  │
│  │  处理模板      │  │   重试节点      │  │ 查询状态     │  │
│  └────────────────┘  └────────────────┘  └──────────────┘  │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     服务层                                  │
│              services/                                     │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │  llm_client   │  │   job_store    │  │   callback   │  │
│  │  LLM调用      │  │   任务存储     │  │   回调通知   │  │
│  └────────────────┘  └────────────────┘  └──────────────┘  │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     AI 代理层                               │
│              agents/                                       │
│  ┌────────────────┐  ┌────────────────┐                    │
│  │ scoring_engine │  │ socratic_tutor │                    │
│  │  量化评分      │  │  苏格拉底辅导   │                    │
│  └────────────────┘  └────────────────┘                    │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     外部服务                                 │
│              DeepSeek API (大语言模型)                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔌 API 接口文档

### 基础路径

所有接口前缀：`http://localhost:8000/api/orchestration`

### 接口列表

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 处理模板 | POST | `/process` | 处理实训模板，触发 AI 分析 |
| 重试节点 | POST | `/retry` | 重试失败的节点 |
| 查询状态 | GET | `/status/{job_id}` | 查询任务状态 |
| 健康检查 | GET | `/health` | 服务健康检查 |

### 1. 处理模板

**请求**:
```http
POST /api/orchestration/process
Content-Type: application/json
X-Service-Token: <service-token>

{
  "template_id": "2060045697011781633",
  "raw_canvas_json": {
    "orchestration_id": "orch_xxx",
    "nodes": [...],
    "edges": [...]
  }
}
```

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| template_id | String | 是 | 实训模板ID |
| raw_canvas_json | Object | 是 | 原始画布JSON数据 |

**响应**:
```json
{
  "success": true,
  "job_id": "550e8400-e29b-41d4-a716-446655440000",
  "message": "任务已创建"
}
```

### 2. 重试节点

**请求**:
```http
POST /api/orchestration/retry
Content-Type: application/json
X-Service-Token: <service-token>

{
  "template_id": "2060045697011781633",
  "failed_node_ids": ["node_1", "node_3"]
}
```

**请求参数**:
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| template_id | String | 是 | 实训模板ID |
| failed_node_ids | Array | 是 | 失败节点ID列表 |

**响应**:
```json
{
  "success": true,
  "job_id": "550e8400-e29b-41d4-a716-446655440001",
  "message": "重试任务已创建"
}
```

### 3. 查询任务状态

**请求**:
```http
GET /api/orchestration/status/{job_id}
X-Service-Token: <service-token>
```

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| job_id | String | 任务ID（UUID格式） |

**响应**:
```json
{
  "success": true,
  "job_id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "completed",
  "progress": 100,
  "result": {
    "standard_payload_json": {...},
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

### 4. 健康检查

**请求**:
```http
GET /health
```

**响应**:
```json
{
  "status": "healthy",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

---

## 📡 回调机制

### 回调接口

AI 引擎完成任务后，会主动回调后端核心服务：

**回调地址**: `POST http://localhost:8080/api/internal/ai-callback`

**请求体**:
```json
{
  "job_id": "550e8400-e29b-41d4-a716-446655440000",
  "template_id": "2060045697011781633",
  "status": "completed",
  "result": {
    "standard_payload_json": {...}
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
# LLM 配置
LLM_BASE_URL=https://api.deepseek.com
LLM_API_KEY=your_deepseek_api_key_here
LLM_MODEL_NAME=deepseek-v4-flash

# 服务配置
SERVICE_TOKEN=service-token-123456
BACKEND_URL=http://localhost:8080

# 服务端口（可选，默认8000）
PORT=8000
```

### 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| LLM_BASE_URL | DeepSeek API 基础地址 | https://api.deepseek.com |
| LLM_API_KEY | DeepSeek API 密钥 | - |
| LLM_MODEL_NAME | 使用的模型名称 | deepseek-v4-flash |
| SERVICE_TOKEN | 服务间认证令牌 | service-token-123456 |
| BACKEND_URL | 后端核心服务地址 | http://localhost:8080 |
| PORT | 服务监听端口 | 8000 |

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

复制 `.env.example` 为 `.env` 并配置：

```bash
# 创建 .env 文件
echo "LLM_BASE_URL=https://api.deepseek.com" > .env
echo "LLM_API_KEY=your_api_key" >> .env
echo "LLM_MODEL_NAME=deepseek-v4-flash" >> .env
echo "SERVICE_TOKEN=service-token-123456" >> .env
echo "BACKEND_URL=http://localhost:8080" >> .env
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

## 🧪 测试 API

### 测试健康检查

```bash
curl http://localhost:8000/health
```

### 测试模板处理

```bash
curl -X POST http://localhost:8000/api/orchestration/process \
  -H "Content-Type: application/json" \
  -H "X-Service-Token: service-token-123456" \
  -d '{
    "template_id": "test-template-123",
    "raw_canvas_json": {
      "orchestration_id": "orch_test",
      "nodes": [],
      "edges": []
    }
  }'
```

### 测试状态查询

```bash
curl -H "X-Service-Token: service-token-123456" \
  http://localhost:8000/api/orchestration/status/{job_id}
```

---

## 📊 核心服务说明

### 1. LLM 客户端 (llm_client.py)

**功能**: 封装 DeepSeek API 调用，提供统一的 LLM 访问接口。

**核心方法**:
- `chat_completion(messages)` - 调用聊天接口
- `generate_response(prompt)` - 生成文本响应

### 2. 任务存储 (job_store.py)

**功能**: 内存中的任务状态存储，支持任务创建、查询、更新。

**核心方法**:
- `create_job(template_id, raw_data)` - 创建任务
- `update_job_status(job_id, status, result)` - 更新任务状态
- `get_job(job_id)` - 获取任务信息
- `delete_job(job_id)` - 删除任务

### 3. 回调服务 (callback.py)

**功能**: 处理任务完成后的回调通知。

**核心方法**:
- `notify_backend(job_id, template_id, status, result)` - 通知后端服务

### 4. 量化评分引擎 (scoring_engine.py)

**功能**: 基于大语言模型进行多维量化评分。

**评分维度**:
- 正确性
- 完整性
- 规范性
- 创新性
- 复杂度

### 5. 苏格拉底式辅导 (socratic_tutor.py)

**功能**: 针对学生代码错误进行引导式答疑，不直接给出答案，而是通过提问引导学生思考。

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

---

## 📈 性能考虑

### 异步处理

- AI 任务处理可能耗时较长，采用异步方式执行
- 任务提交后立即返回 job_id，后台异步处理
- 通过状态查询接口获取处理进度和结果

### 内存管理

- 任务存储使用内存字典，重启后数据会丢失
- 生产环境建议使用 Redis 或数据库持久化
- 定期清理已完成的任务记录

---

## 🐛 常见问题

### 1. 连接 DeepSeek API 失败

**问题描述**:
```
ConnectionError: Failed to connect to DeepSeek API
```

**解决方案**:
- 检查网络连接
- 确认 LLM_BASE_URL 配置正确
- 验证 API Key 是否有效

### 2. 回调失败

**问题描述**:
```
Callback failed: Connection refused
```

**解决方案**:
- 检查后端核心服务是否启动
- 确认 BACKEND_URL 配置正确
- 验证服务间网络连通性

### 3. 任务状态异常

**问题描述**: 任务长时间处于 `processing` 状态

**解决方案**:
- 检查 LLM API 调用是否成功
- 查看服务日志定位问题
- 考虑增加任务超时机制

---

## 📚 相关文档

- [项目主文档](../README.md) - 项目整体概述
- [后端文档](../backend-core/BACKEND_README.md) - 后端核心服务
- [前端文档](../frontend-vue/FRONTEND_README.md) - 前端应用

---

**AI SmartConstruct Lab** - 让智能教学更简单 ✨