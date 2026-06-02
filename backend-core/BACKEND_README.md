# SmartConstruct 后端核心服务

> AI SmartConstruct Lab — Spring Boot 2.7 / Java 8 / MyBatis-Plus 3.5 后端核心服务
>
> 提供用户认证、组织管理、师生信息管理、实训流程编排、AI 引擎编排回调、实时通信、节点数据提交等核心能力。

---

## 📋 项目简介

SmartConstruct 是一个智慧建造实验室管理系统的后端服务，基于 Spring Boot 2.7 构建，采用经典 Controller → Service → Mapper 分层结构，ORM 框架使用 MyBatis-Plus，并辅以 Hibernate `ddl-auto=update` 进行表结构演进。系统通过 RESTful API 与前端 Vue 3 应用通信，通过 HTTP+WebHook 与 Python AI 引擎通信，通过 WebSocket 推送课中实训实时状态。

### 核心架构

```
┌─────────────────────────────────────────────────────────────┐
│                      客户端层 (Client)                       │
│              Vue 3 Frontend / Mobile / Other                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      网关层 (Gateway)                       │
│              Spring Security + JWT Filter                   │
│         - 认证授权 (JwtAuthenticationFilter)                 │
│         - 内部服务Token (InternalServiceTokenFilter)         │
│         - 权限校验 (Authorization)                          │
│         - CORS 配置                                         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      应用层 (Application)                   │
│                 REST API Controllers (/api/**)              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │   Auth   │ │ Student  │ │ Teacher  │ │  Admin   │      │
│  │Controller│ │Controller│ │Controller│ │Controller│      │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
│   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│   │ AI 回调  │ │  文件    │ │  公共    │ │  初始化  │      │
│   │Controller│ │Controller│ │Controller│ │Controller│      │
│   └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      服务层 (Service)                       │
│           IService 接口  +  ServiceImpl 实现                │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │  SysUser │ │ Training │ │   Org    │ │   Phase  │      │
│  │  Service │ │ Template │ │ Service  │ │Execution │      │
│  │          │ │ Service  │ │          │ │ Service  │      │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │   AI     │ │  Node    │ │  Demo    │ │Knowledge │      │
│  │ Engine   │ │Validation│ │  Data    │ │  Point   │      │
│  │  Client  │ │ Service  │ │ Seeder   │ │ Service  │      │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      数据层 (Data Access)                    │
│                   MyBatis-Plus BaseMapper                   │
│  SysUserMapper | WfTrainingTemplateMapper | BizStudentMapper│
│  WfNodeInstanceMapper | WfStudentNodeProgressMapper | ...   │
│  Hibernate ddl-auto=update (仅用于表结构演进)                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      实时通信层 (WebSocket)                  │
│            TrainingWebSocketHandler + Interceptor          │
│  - 节点状态广播 (NODE_STATUS)                                │
│  - 心跳检测 (PING/PONG)                                      │
│  - 自动重连 (Reconnection)                                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 2.7.18 |
| 语言 | Java | 8（严格遵守语法；可运行于 JDK 17/21） |
| 数据库 | MySQL | 8.0+ |
| ORM | MyBatis-Plus | 3.5.5 |
| Schema 演进 | Hibernate `ddl-auto=update` | Spring Boot 内置 |
| 安全 | Spring Security + JWT | jjwt 0.12.5 |
| 实时通信 | WebSocket | Spring WebSocket |
| 工具库 | Hutool | 5.8.26 |
| JSON处理 | Jackson | Spring Boot 内置 |
| 代码简化 | Lombok | Spring Boot 内置 |
| 构建 | Maven Wrapper | 3.9.x |

---

## 🚀 快速开始

### 1. 环境要求

- JDK 8 或更高（推荐 JDK 17/21）
- Maven 3.6+（推荐使用 `mvnw.cmd` 包装器）
- MySQL 8.0+

### 2. 数据库配置

创建 MySQL 数据库：

```sql
CREATE DATABASE `ai-smartconstruct-lab` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

数据库配置在 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ai-smartconstruct-lab?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456
```

> 启动时 JPA 会自动建表/补字段（`ddl-auto=update`），完整 DDL 参考 `infrastructure/db_mysql.txt`（22 张表）。

### 3. 构建 & 启动

```bash
cd backend-core

# 编译
./mvnw.cmd clean install -DskipTests

# 启动（开发模式）
./mvnw.cmd spring-boot:run

# 或以 Jar 包形式启动
java -jar target/backend-core-0.0.1-SNAPSHOT.jar
```

启动后访问：`http://localhost:8080`（应用根路径前缀为 `/api-smart`）。

### 4. 初始化种子数据

服务首次启动后，调用初始化接口写入种子数据（用户/角色/课程/班级/部分模板）：

```http
POST http://localhost:8080/api-smart/api/public/init
```

> 默认测试账号（密码统一为 `123456`）：
>
> | 角色 | 账号 |
> |------|------|
> | 学生 | `student` |
> | 教师 | `teacher` |
> | 管理员 | `user1` |

---

## 📁 项目结构

```
backend-core/
├── src/main/java/com/smartconstruct/backend_core/
│   ├── annotation/          # 自定义注解
│   │   └── OperationLog.java                       # 操作日志注解
│   ├── aspect/              # AOP 切面
│   │   └── OperationLogAspect.java                 # 操作日志切面（AOP 实现）
│   ├── config/              # 配置类
│   │   ├── SecurityConfig.java                     # Spring Security 配置
│   │   ├── JwtAuthenticationFilter.java           # JWT 认证过滤器
│   │   ├── InternalServiceTokenFilter.java         # 内部服务 Token 过滤器（AI 引擎回调）
│   │   ├── PasswordEncoderConfig.java              # 密码编码器配置
│   │   ├── WebSocketConfig.java                    # WebSocket 配置
│   │   ├── AsyncConfig.java                        # 异步线程池配置
│   │   ├── MybatisPlusConfig.java                  # MyBatis-Plus 配置
│   │   ├── GlobalExceptionHandler.java             # 全局异常处理（含 400 业务异常）
│   │   └── NodeDefSeeder.java                      # 节点定义种子（CommandLineRunner）
│   │
│   ├── controller/          # REST API 控制器（共 28 个）
│   │   ├── AuthController.java                     # 认证接口
│   │   ├── InitController.java                     # 初始化接口（种子数据）
│   │   ├── PublicDataController.java               # 公共数据接口
│   │   ├── FileController.java                     # 文件上传接口
│   │   ├── StudentDashboardController.java         # 学生仪表板
│   │   ├── StudentCourseController.java            # 学生课程
│   │   ├── StudentFlowController.java              # 学生实训流程（核心：节点进入/完成）
│   │   ├── StudentSubmissionController.java        # 学生节点数据提交（按 node_type 路由）
│   │   ├── StudentTrainingController.java          # 学生实训（开始/重置/下一节点）
│   │   ├── StudentTrainingFlowController.java      # 学生实训流程控制器
│   │   ├── StudentAiChatController.java            # 学生 AI 对话
│   │   ├── TeacherDashboardController.java         # 教师仪表板
│   │   ├── TeacherCourseController.java            # 教师课程
│   │   ├── TeacherMonitorController.java           # 教师实训监控
│   │   ├── TeacherQuestionBankController.java       # 教师题库
│   │   ├── TeacherQuestionController.java          # 教师题目
│   │   ├── TeacherTemplateController.java          # 教师模板
│   │   ├── TeacherTrainingController.java          # 教师实训
│   │   ├── TeacherTrainingTaskController.java      # 教师任务下发
│   │   ├── AdminCourseController.java              # 管理员课程
│   │   ├── AdminFeedbackController.java            # 管理员反馈
│   │   ├── AdminLogController.java                 # 管理员日志
│   │   ├── AdminNodeController.java                # 管理员节点
│   │   ├── AdminOrgController.java                 # 管理员组织
│   │   ├── AdminQuestionController.java            # 管理员题目
│   │   ├── AdminStudentController.java             # 管理员学生
│   │   ├── AdminTeacherController.java             # 管理员教师
│   │   ├── AdminTemplateController.java            # 管理员模板
│   │   ├── AdminTicketController.java              # 管理员工单
│   │   └── AiCallbackController.java               # AI 引擎回调接口（/api/internal/ai-callback）
│   │
│   ├── dto/                 # 数据传输对象（Request/VO/DTO）
│   │   ├── ApiResult.java                          # 统一响应封装 {code, message, data}
│   │   ├── PageResult.java                         # 分页响应封装
│   │   ├── LoginRequest / RegisterRequest / AuthResponse
│   │   ├── CurrentPositionDTO.java                 # 学生当前节点位置
│   │   ├── PhaseProgressDTO.java                   # 阶段进度
│   │   ├── PhaseUnlockStatus.java                  # 阶段解锁状态
│   │   ├── StudentTaskOverviewDTO.java             # 学生实训任务总览
│   │   ├── TrainingNodeProgressDTO.java            # 节点进度
│   │   ├── NodeProgressDTO.java                    # 通用节点进度
│   │   ├── ParticipationDetailVO.java              # 参与详情 VO
│   │   ├── AiChatRequest / AiChatResponse / AiSessionVO
│   │   ├── AiCallbackRequest / AiCallbackPayload / AiProcessRequest
│   │   ├── AiJobStatus.java                        # AI 引擎任务状态
│   │   ├── NodeAiStatusPayload.java                # 节点 AI 状态
│   │   ├── GeneratedAssets.java                    # AI 生成的资源（题目/视频等）
│   │   ├── ValidationResult.java                   # 节点配置校验
│   │   ├── EvalIndicatorDTO.java                   # 评控指标
│   │   ├── KnowledgePointDTO.java                  # 知识点 DTO
│   │   ├── StudentVO / TeacherVO / CourseVO / QuestionBankVO / QuestionDTO
│   │   ├── StudentCreateRequest / StudentUpdateRequest
│   │   ├── TeacherCreateRequest / TeacherUpdateRequest
│   │   ├── CourseRequest / DeptRequest / MajorRequest / ClassRequest
│   │   └── DemoSeedResult.java                     # 种子数据结果
│   │
│   ├── entity/              # 数据库实体（22 张表 + AI 状态/会话/消息等）
│   │   ├── SysUser / SysResource / SysOperationLog / SysFeedback / SysTicket
│   │   ├── BizStudent / BizTeacher / BizDepartment / BizMajor / BizAdminClass
│   │   ├── BizCourse / BizStudentCourse
│   │   ├── BizQuestion / BizQuestionBank
│   │   ├── BizTrainingTask / BizTrainingParticipation
│   │   ├── BizMindmapRecord / BizMindmapEvalDetail
│   │   ├── BizTrainingExamRecord / BizStudentAnswerDetail
│   │   ├── BizTrainingChatLog / BizTrainingUpload
│   │   ├── BizAiSession / BizAiMessage              # AI 会话/消息
│   │   ├── BizLearningSurveyResponse               # 学情调查
│   │   ├── BizKnowledgePoint                       # 知识点
│   │   ├── WfTrainingTemplate / WfNodeDef / WfNodeInstance
│   │   ├── WfStudentActivityState / WfGlobalActivityState
│   │   ├── WfStudentNodeProgress                   # 学生节点进度
│   │   ├── WfNodeAiStatus / WfNodeConfigSchema
│   │   ├── WfTeacherNodeOperation                  # 教师节点操作
│   │   ├── AiProcessingLog                         # AI 处理日志
│   │   └── FactEvalResult                          # 事实评估结果
│   │
│   ├── exception/           # 业务异常
│   │   └── BusinessException.java                  # 业务异常基类
│   │
│   ├── mapper/              # MyBatis-Plus 映射接口（每张实体表对应一个）
│   │   └── ...（与 entity 一一对应）
│   │
│   ├── service/             # 业务服务层（接口 + 实现）
│   │   ├── impl/            # 服务实现
│   │   │   ├── SysUserServiceImpl.java             # 用户服务实现
│   │   │   ├── TrainingTemplateServiceImpl.java    # 模板服务实现
│   │   │   ├── TrainingTaskServiceImpl.java        # 任务服务实现
│   │   │   ├── TrainingParticipationServiceImpl.java # 参与服务实现（含重复记录清理）
│   │   │   ├── PhaseExecutionServiceImpl.java      # 阶段执行（核心：节点进入/完成/阶段解锁）
│   │   │   ├── NodeValidationServiceImpl.java       # 节点配置校验
│   │   │   ├── NodeDefServiceImpl.java             # 节点定义服务
│   │   │   ├── StudentActivityStateServiceImpl.java # 学生活动状态
│   │   │   ├── GlobalActivityStateServiceImpl.java # 全局活动状态
│   │   │   ├── AiEngineClientImpl.java              # AI 引擎 HTTP 客户端
│   │   │   ├── AiProcessingLogServiceImpl.java      # AI 处理日志
│   │   │   ├── AiSessionServiceImpl.java           # AI 会话服务
│   │   │   ├── AiMessageServiceImpl.java           # AI 消息服务
│   │   │   ├── StudentServiceImpl.java / TeacherServiceImpl.java
│   │   │   ├── CourseServiceImpl.java / StudentCourseServiceImpl.java
│   │   │   ├── DepartmentServiceImpl.java / MajorServiceImpl.java / AdminClassServiceImpl.java
│   │   │   ├── QuestionServiceImpl.java / QuestionBankServiceImpl.java
│   │   │   ├── MindmapRecordServiceImpl.java / MindmapEvalDetailServiceImpl.java
│   │   │   ├── TrainingChatLogServiceImpl.java / TrainingExamRecordServiceImpl.java
│   │   │   ├── KnowledgePointServiceImpl.java
│   │   │   ├── OperationLogServiceImpl.java / TicketServiceImpl.java / FeedbackServiceImpl.java
│   │   │   ├── SysResourceServiceImpl.java
│   │   │   └── DemoDataSeederService.java          # 种子数据 seeder
│   │   └── ...（与实现一一对应的接口：IService / I...Service）
│   │
│   ├── util/                # 工具类
│   │   ├── JwtUtil.java                            # JWT 生成/解析
│   │   └── Java8Compat.java                        # Java 8 语法兼容工具
│   │
│   ├── websocket/           # WebSocket 相关
│   │   ├── TrainingWebSocketHandler.java           # 课中实训 WebSocket 处理器
│   │   └── TrainingHandshakeInterceptor.java       # 握手拦截器
│   │
│   └── BackendCoreApplication.java                 # Spring Boot 启动入口
│
├── src/main/resources/
│   └── application.properties                      # 应用配置
│
├── AI_DEV_PROMPT.md                                # 后端开发约定（强制）
├── BACKEND_README.md
└── pom.xml                                         # Maven 依赖管理
```

---

## 🧱 核心功能模块

### 1. 用户认证模块 (Auth)

**核心类**:
- `AuthController` - 认证接口控制器（`/api/auth/login`、`/api/auth/register`、`/api/auth/userinfo`、`/api/auth/logout`）
- `SysUserServiceImpl` - 用户服务实现（实现 `UserDetails`，支持 BCrypt 校验）
- `JwtUtil` - JWT 工具类（jjwt 0.12.5）
- `JwtAuthenticationFilter` - JWT 认证过滤器（`OncePerRequestFilter`）
- `SecurityConfig` - Spring Security 配置（无状态、CORS、公开路径白名单）
- `PasswordEncoderConfig` - 独立 `PasswordEncoder` Bean（避免循环依赖）

**认证流程**:
```
1. 客户端登录 → AuthController.login(username, password, roleType)
2. 校验账号状态、密码（BCrypt）、角色
3. JwtUtil.generateToken() 生成 24h 有效 JWT
4. 返回 {token, user}
5. 客户端后续请求携带 Authorization: Bearer <token>
6. JwtAuthenticationFilter 校验 Token，写入 SecurityContext
7. 控制器可通过 SecurityContextHolder 获取当前用户
```

**公开路径**:
- `/api/auth/**`
- `/api/public/**`
- `/api/internal/**` （通过 `InternalServiceTokenFilter` 校验 `X-Service-Token`）

---

### 2. 实训编排模块 (Teacher Template)

**核心类**:
- `TeacherTemplateController` - 教师模板 CRUD
- `TrainingTemplateServiceImpl` - 模板持久化
- `WfTrainingTemplate` - 模板实体（`rawCanvasJson`、`standardPayloadJson`、`aiStatus`、`aiJobId`）
- `AiEngineClientImpl` - AI 引擎 HTTP 客户端

**模板创建流程**:
```
1. 教师在 TrainingCreate.vue 编排节点/连线，生成 canvas JSON
2. POST /api/teacher/templates 保存 WfTrainingTemplate (aiStatus=1)
3. AiEngineClientImpl.triggerAiPipeline() 调用 AI 引擎 process 接口
4. AI 引擎返回 job_id，存储到 aiJobId 字段
5. AI 引擎异步处理完成 → 回调 POST /api/internal/ai-callback
6. 后端更新模板 aiStatus=2，并将 standardPayloadJson 写入
```

---

### 3. 阶段执行核心服务 (PhaseExecution)

> **本模块是整个实训流程运行时的心脏。**

**核心类**:
- `IPhaseExecutionService` / `PhaseExecutionServiceImpl` - 阶段执行核心
- `StudentFlowController` - 学生端流程入口
- `StudentTrainingController` - 学生端开始/重置/推进
- `WfStudentNodeProgress` - 学生节点进度
- `WfStudentActivityState` - 学生活动状态

**关键方法**:
- `enterNode(participationId, nodeInstanceId)` - 进入节点，更新活动状态
- `completeNode(participationId, nodeInstanceId, payload)` - 完成节点，判定阶段解锁
- `getCurrentPosition(taskId, studentId)` - 获取学生当前节点位置
- `getTaskOverview(taskId, studentId)` - 获取任务总览（阶段/节点/解锁状态）

**重复参与记录处理**:
> 由于历史数据中可能存在同一个 `(taskId, studentId)` 下的多条 `BizTrainingParticipation` 记录，会导致 `selectOne` 抛 `TooManyResultsException`。所有使用 `selectOne` 查询参与记录的地方都加上了 `orderByDesc(BizTrainingParticipation::getId)`，且在 `resetTraining` 入口处会主动清理重复记录。

---

### 4. 学生节点数据提交 (StudentSubmission)

**核心类**:
- `StudentSubmissionController` - 统一节点提交入口 `/api/student/nodes`
- `BizTrainingUpload` - 方案/代码上传
- `BizStudentAnswerDetail` - 答题详情
- `BizMindmapRecord` - 思维导图记录
- `FactEvalResult` - 事实评估结果
- `BizTrainingChatLog` - 实训聊天日志

**支持的节点类型**:
| 节点类型 | 处理逻辑 |
|---------|---------|
| `resource_read` | 阅读数据采集（阅读时长/进度） |
| `coding_class` | 代码提交 + AI 代码审查（通过 AI 引擎） |
| `homework` / `exam` | 答题提交 + AI 批改 |
| `req_upload` / `plan_upload` | 文件上传 + AI 预评审 |
| `mindmap_draw` | 思维导图提交 + AI 结构评价 |
| `student_peer_review` / `inter_group_review` | 互评提交（滑块评分） |

---

### 5. AI 引擎集成

**核心类**:
- `AiEngineClientImpl` - AI 引擎 HTTP 客户端
  - `triggerAiPipeline(templateId, canvasJson)` - 触发编排处理
  - `retryFailedNodes(templateId, failedNodeIds)` - 重试失败节点
  - `queryJobStatus(jobId)` - 查询任务状态
- `AiCallbackController` - AI 引擎回调接收（`/api/internal/ai-callback`）
- `InternalServiceTokenFilter` - 内部服务 Token 过滤器
- `AiProcessingLog` / `WfNodeAiStatus` - AI 处理日志与节点状态

**AI 引擎通信**:
```
backend-core ──HTTP──> ai-engine
   │                      │
   │  POST /api/orchestration/process   (触发)
   │  POST /api/orchestration/retry      (重试)
   │  GET  /api/orchestration/status/{id} (查询)
   │                      │
   │ <── callback ────── │
   POST /api/internal/ai-callback        (X-Service-Token 头)
```

> 所有 AI 引擎调用走 `ai-engine.base-url=http://localhost:8000`，服务间鉴权 `ai-engine.service-token=smartconstruct-internal-token`。

---

### 6. WebSocket 实时通信

**核心类**:
- `WebSocketConfig` - WebSocket 端点注册（`/ws/training`）
- `TrainingWebSocketHandler` - 消息处理器
- `TrainingHandshakeInterceptor` - 握手拦截器（从 Token 解析用户身份）

**消息类型**:
| 类型 | 方向 | 说明 |
|------|------|------|
| `NODE_STATUS` | Server→Client | 节点状态变更（教师推进 → 学生收到） |
| `PING` / `PONG` | 双向 | 心跳 |
| `progress_update` | Server→Client | 学生进度更新（教师监控） |
| `node_complete` | Client→Server | 学生完成节点通知 |

---

### 7. 系统运维模块

**核心类**:
- `OperationLogAspect` - 操作日志切面（`@OperationLog` 注解 + AOP）
- `AdminLogController` - 日志管理
- `AdminTicketController` - 工单管理
- `AdminFeedbackController` - 反馈管理
- `GlobalExceptionHandler` - 全局异常处理（统一错误响应格式）

**业务异常**:
- `BusinessException` - 业务异常基类
- `PhaseExecutionService` 抛出 `PHASE_LOCKED`（阶段未解锁）和 `TASK_EXPIRED`（任务过期）异常
- `GlobalExceptionHandler` 统一返回 4xx 状态码 + `{error_code, error_message}`

---

## 📡 API 接口文档

### 认证接口 (`/api/auth`)

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 登录 | POST | `/api/auth/login` | 用户登录获取 Token |
| 注册 | POST | `/api/auth/register` | 用户注册 |
| 登出 | POST | `/api/auth/logout` | 用户登出 |
| 用户信息 | GET | `/api/auth/userinfo` | 获取当前用户信息（需认证） |

### 公共接口 (`/api/public`)

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 初始化 | POST | `/api/public/init` | 初始化种子数据（首次部署调用） |
| 公共数据 | GET | `/api/public/**` | 课程/班级等公开数据 |

### 文件接口 (`/api/file`)

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 上传 | POST | `/api/file/upload` | 通用文件上传 |
| 列表 | GET | `/api/file/list` | 文件列表 |

### 教师接口 (`/api/teacher`)

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 模板 | `/api/teacher/templates` | 模板 CRUD，触发 AI 编排 |
| 任务下发 | `/api/teacher/training-tasks` | 任务下发到班级/课程 |
| 课中推进 | `/api/teacher/training/{taskId}/next` | 推进课中节点 |
| 实训管理 | `/api/teacher/training/**` | 实训列表/详情/监控 |
| 仪表板 | `/api/teacher/dashboard/**` | 教师工作台数据 |
| 课程 | `/api/teacher/course/**` | 课程管理 |
| 题库 | `/api/teacher/question-bank/**` | 题库/题目管理 |
| 监控 | `/api/teacher/monitor/**` | 实训实时监控 |

### 学生接口 (`/api/student`)

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 任务总览 | `/api/student/tasks/{taskId}/overview` | 任务总览（阶段/节点） |
| 当前节点 | `/api/student/tasks/{taskId}/current` | 当前节点位置 |
| 进入节点 | POST `/api/student/tasks/{taskId}/nodes/{nodeId}/enter` | 进入节点 |
| 完成节点 | POST `/api/student/tasks/{taskId}/nodes/{nodeId}/complete` | 完成节点 |
| 开始实训 | POST `/api/student/training-tasks/{participationId}/start` | 开始实训 |
| 重置实训 | POST `/api/student/training-tasks/{taskId}/reset` | 重置（可重复开始） |
| 节点提交 | `/api/student/nodes/**` | 统一节点数据提交（按 node_type 路由） |
| AI 对话 | `/api/student/ai-chat/**` | 学生 AI 助手对话 |
| 仪表板 | `/api/student/dashboard/**` | 学生工作台数据 |
| 课程 | `/api/student/course/**` | 学生选课 |

### 管理员接口 (`/api/admin`)

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 用户 | `/api/admin/student`、`/api/admin/teacher` | 师生管理 |
| 组织 | `/api/admin/org/{depts,majors,classes}` | 院系/专业/班级 |
| 课程 | `/api/admin/course` | 课程管理 |
| 节点 | `/api/admin/node` | 节点定义管理 |
| 模板 | `/api/admin/template` | 模板管理 |
| 题目 | `/api/admin/question` | 题目/题库管理 |
| 日志 | `/api/admin/log` | 操作日志 |
| 工单 | `/api/admin/ticket` | 工单管理 |
| 反馈 | `/api/admin/feedback` | 反馈管理 |

### 内部接口 (`/api/internal`)

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| AI 回调 | POST | `/api/internal/ai-callback` | AI 引擎处理完成回调（X-Service-Token 头） |

---

## 🗃️ 数据库设计

完整 DDL 见 `infrastructure/db_mysql.txt`，共 22 张核心表 + AI 状态/会话/消息等扩展表。

### 表前缀规范

| 前缀 | 范围 | 示例 |
|------|------|------|
| `sys_` | 系统级 | sys_user, sys_role, sys_resource, sys_operation_log, sys_feedback, sys_ticket |
| `biz_` | 业务实体 | biz_student, biz_teacher, biz_course, biz_training_task, biz_question |
| `wf_` | 工作流 | wf_training_template, wf_node_def, wf_node_instance, wf_student_node_progress, wf_student_activity_state |
| `fact_` | 事实表 | fact_eval_result |

### 关键实体字段

#### WfTrainingTemplate（实训模板）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 雪花 ID（ToStringSerializer） |
| templateName | String | 模板名称 |
| rawCanvasJson | Object | 原始画布 JSON（前端编排数据，含 phases/nodes/edges） |
| standardPayloadJson | Object | AI 处理后的标准执行载荷 |
| aiStatus | Integer | 1=处理中, 2=完成, 3=失败 |
| aiJobId | String | AI 引擎任务 ID（UUID） |
| errorReason | String | 错误原因 |

#### BizTrainingParticipation（学生参与记录）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 雪花 ID |
| taskId | Long | 任务 ID |
| studentId | Long | 学生 ID |
| status | Integer | 0=未开始, 1=进行中, 2=已完成 |
| createdAt / updatedAt | LocalDateTime | 时间戳 |

> 同一 `(taskId, studentId)` 应只保留一条参与记录；`resetTraining` 入口会清理重复项。

#### WfStudentNodeProgress（学生节点进度）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 雪花 ID |
| participationId | Long | 参与记录 ID |
| nodeInstanceId | Long | 节点实例 ID |
| status | Integer | 0=未开始, 1=进行中, 2=已完成 |
| payload | Object | 节点提交数据（JSON） |
| startedAt / completedAt | LocalDateTime | 开始/完成时间 |

#### WfStudentActivityState（学生活动状态）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 雪花 ID |
| participationId | Long | 参与记录 ID |
| currentNodeId | String | 当前节点 ID |
| updatedAt | LocalDateTime | 更新时间 |

---

## 🏗️ 实体关联关系

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│   SysUser        │     │ BizTrainingTask  │     │ WfTrainingTemplate│
│   (系统用户)      │◄────┤   (实训任务)      │────►│    (实训模板)     │
│                  │     │                  │     │                  │
│ - id             │     │ - id             │     │ - id             │
│ - username       │     │ - templateId     │     │ - templateName   │
│ - roleType       │     │ - teacherId      │     │ - rawCanvasJson  │
│ - passwordHash   │     │ - taskName       │     │ - aiStatus       │
└──────────────────┘     │ - dispatchScope  │     │ - aiJobId        │
         ▲              └──────────────────┘     └──────────────────┘
         │                       │
         │                       ▼
         │              ┌──────────────────────┐
         │              │BizTrainingParticipation│
         │              │   (参与记录)           │
         │              │                      │
         │              │ - id                 │
         └──────────────┤ - taskId             │
                        │ - studentId          │
                        │ - status             │
                        └──────────────────────┘
                                  │
                                  ▼
                        ┌──────────────────────┐
                        │WfStudentNodeProgress │
                        │   (节点进度)          │
                        │                      │
                        │ - participationId    │
                        │ - nodeInstanceId     │
                        │ - status (0/1/2)     │
                        │ - payload            │
                        └──────────────────────┘
                                  │
                                  ▼
                        ┌──────────────────────┐
                        │WfStudentActivityState│
                        │   (活动状态)          │
                        │                      │
                        │ - participationId    │
                        │ - currentNodeId      │
                        └──────────────────────┘

┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│ BizDepartment    │◄────┤  BizMajor        │◄────┤ BizAdminClass    │
│   (院系)          │     │   (专业)          │     │   (班级)          │
└──────────────────┘     └──────────────────┘     └──────────────────┘
         ▲                                              ▲
         │                                              │
         └──────────────────┬───────────────────────────┘
                            │
                    ┌───────┴───────┐
                    │  BizStudent   │
                    │  (学生信息)    │
                    └───────────────┘
```

---

## 🔒 安全说明

### JWT Token 使用

登录成功后返回 JWT Token，客户端需在每次请求的 `Authorization` 头中携带：

```
Authorization: Bearer <token>
```

### 内部服务 Token

AI 引擎回调到 `POST /api/internal/ai-callback` 时，必须携带：

```
X-Service-Token: smartconstruct-internal-token
```

由 `InternalServiceTokenFilter` 校验。

### 公开路径白名单

| 路径 | 权限 |
|------|------|
| `/api/auth/**` | 公开 |
| `/api/public/**` | 公开 |
| `/api/internal/**` | 需 Service Token |
| `/api/**` | 需 JWT Token |
| `/ws/**` | WebSocket（握手时校验） |

### 雪花 ID 序列化

所有实体主键（`id`/`userId`）和外键字段（`deptId`/`majorId`/`classId`/`teacherId`）均加 `@JsonSerialize(using = ToStringSerializer.class)`，避免前端 JS 精度丢失。前端 TS 类型对应字段必须为 `string` 而非 `number`。

---

## ⚙️ 配置说明

### application.properties 关键项

```properties
# 上下文路径
server.servlet.context-path=/api-smart

# JWT
jwt.secret=smartconstruct-jwt-secret-key-2026-must-be-at-least-256bits-long!!
jwt.expiration=86400000  # 24h

# 文件上传
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
file.upload-dir=${user.dir}/infrastructure/uploads

# JPA（仅用于 ddl-auto=update）
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# AI 引擎
ai-engine.base-url=http://localhost:8000
ai-engine.service-token=smartconstruct-internal-token
```

### CORS

`SecurityConfig` 已配置 `addAllowedOriginPattern("*")`（开发环境）。生产环境应替换为白名单域名。

### WebSocket 端点

`ws://localhost:8080/api-smart/ws/training`

---

## 🔗 系统流转真实链路

### 1. 实训任务发布链路
```
教师端 TrainingCreate.vue
  → buildCanvasData() 构建 { orchestration_id, phases, nodes, edges }
  → POST /api/teacher/templates
  → TeacherTemplateController.create()
     → 保存 WfTrainingTemplate (aiStatus=1, rawCanvasJson)
     → AiEngineClientImpl.triggerAiPipeline() 异步调用 ai-engine
     → AI 引擎返回 job_id → 存储到 aiJobId
  → 前端跳转到 /teacher/training-manage
```

### 2. AI 引擎处理链路
```
AiEngineClientImpl.triggerAiPipeline()
  → POST http://localhost:8000/api/orchestration/process
  → AI 引擎生成 UUID job_id → job_store
  → 异步调用 Supervisor + LLM Agents 处理
  → 处理完成 → callback POST http://localhost:8080/api-smart/api/internal/ai-callback
  → AiCallbackController 写入 standardPayloadJson + aiStatus=2
```

### 3. 学生实训执行链路
```
学生进入 /student/training/{taskId}/execute
  → studentTrainingGuard 校验阶段解锁
  → StudentFlowController.getTaskOverview()
  → PhaseExecutionService.getTaskOverview()
  → 前端展示阶段/节点列表
  → 点击节点 → StudentFlowController.enterNode()
     → 写入 WfStudentActivityState
     → 返回节点内容（含 ai_spec、payload）
  → 提交节点数据 → StudentSubmissionController（按 node_type 路由）
  → 完成节点 → StudentFlowController.completeNode()
     → 更新 WfStudentNodeProgress.status=2
     → 判定当前阶段是否完成 → 解锁下一阶段
     → 返回下一节点信息
```

### 4. 超时检测链路
```
AiTimeoutDetectionTask（定时任务，未来扩展）
  → 查询 aiStatus=1 且超时的模板
  → GET http://localhost:8000/api/orchestration/status/{job_id}
  → 根据状态更新 aiStatus
```

---

## 📐 开发约定

详见 `backend-core/AI_DEV_PROMPT.md`（强制遵守）：

1. **雪花 ID** 全部 `IdType.ASSIGN_ID`，外键/主键字段必须 `@JsonSerialize(using = ToStringSerializer.class)`
2. **时间** 统一 `LocalDateTime`，`createdAt`/`updatedAt` 自动填充
3. **命名** 数据库下划线，Java 驼峰，表前缀 `biz_`/`sys_`/`wf_`
4. **Lombok** 实体 `@Data @NoArgsConstructor @AllArgsConstructor @Builder`
5. **接口** 返回统一 `ApiResult`，分页用 `PageResult`
6. **事务** 增删改加 `@Transactional(rollbackFor = Exception.class)`
7. **查询** 优先 `LambdaQueryWrapper`，禁止硬编码列名
8. **异常** 业务错误抛 `BusinessException`，全局 `GlobalExceptionHandler` 统一处理
9. **时区** 三端统一 `Asia/Shanghai`
10. **测试** 用 REST Client `.http` 文件或 curl，不用浏览器

---

## 🐛 常见问题

### 1. 启动报 `CircularReference` 错误
确保 `PasswordEncoder` 已在独立的 `PasswordEncoderConfig` 中定义。

### 2. CORS 错误
检查 `SecurityConfig.corsConfigurationSource()` 中是否允许前端源。

### 3. 登录 401
- 后端服务是否启动
- 数据库中存在该用户
- 密码是否正确（BCrypt 加密后存储）
- 请求格式正确（`Content-Type: application/json`）

### 4. WebSocket 连接失败
- WebSocket 端点：`ws://localhost:8080/api-smart/ws/training`
- CORS 是否允许
- 握手拦截器是否能从 Token 解析用户

### 5. `selectOne` 报 `TooManyResultsException`
数据库中存在重复 `BizTrainingParticipation` 记录。修复方案：
- 代码层：所有 `selectOne` 加 `orderByDesc(BizTrainingParticipation::getId)`
- 数据层：`resetTraining` 入口清理重复项（保留最新一条 + 清理关联子表）

### 6. 前端 ID 精度丢失
后端实体主键/外键缺少 `@JsonSerialize(using = ToStringSerializer.class)`。**新建/修改 DTO/Entity 时必须检查。**

---

## 🆕 新增功能（2024更新）

### 1. 重复参与记录处理
- **问题修复**：修复 `selectOne` 查询多条记录导致的 `TooManyResultsException`
- **解决方案**：
  - 在所有使用 `selectOne` 查询参与记录的地方添加 `orderByDesc(BizTrainingParticipation::getId)`
  - 在 `resetTraining` 方法中添加清理重复参与记录的逻辑，确保每个学生在一个任务下只有一条参与记录
  - 清理时先删除关联的子记录（`WfStudentNodeProgress`、`WfStudentActivityState`）

### 2. 实训重置功能增强
- **支持重复开始**：实训可以重复开始，重置所有进度数据
- **清理逻辑**：重置时清理该学生在该任务下的所有参与记录，只保留最新一条
- **重新初始化**：重置后调用 `startTraining()` 重新初始化实训流程

### 3. 节点完成跳转优化
- **状态更新**：完成节点时正确更新 Pinia store 中的当前节点状态
- **下一节点导航**：调用 `completeNode()` 和 `navigateToNextNode()` 更新状态，跳转时携带新节点 ID
- **阶段解锁判定**：完成节点后自动判定当前阶段是否完成，解锁下一阶段

### 4. 阶段执行服务增强
- **核心方法优化**：
  - `enterNode()` - 进入节点，更新活动状态
  - `completeNode()` - 完成节点，判定阶段解锁
  - `getCurrentPosition()` - 获取学生当前节点位置
  - `getTaskOverview()` - 获取任务总览（阶段/节点/解锁状态）

### 5. 异常处理增强
- **全局异常处理**：`GlobalExceptionHandler` 统一处理业务异常
- **错误响应格式**：统一返回 `{error_code, error_message}` 格式
- **业务异常类型**：`PHASE_LOCKED`（阶段未解锁）、`TASK_EXPIRED`（任务过期）

### 6. 学生节点数据提交扩展
- **支持多种节点类型**：
  - `resource_read` - 阅读数据采集
  - `coding_class` - 代码提交 + AI 代码审查
  - `homework`/`exam` - 答题提交 + AI 批改
  - `req_upload`/`plan_upload` - 文件上传 + AI 预评审
  - `mindmap_draw` - 思维导图提交 + AI 结构评价
  - `student_peer_review`/`inter_group_review` - 互评提交

### 7. 数据库实体扩展
- **新增实体**：
  - `BizAiSession` / `BizAiMessage` - AI 会话/消息
  - `BizLearningSurveyResponse` - 学情调查
  - `BizKnowledgePoint` - 知识点
  - `AiProcessingLog` - AI 处理日志
  - `FactEvalResult` - 事实评估结果

### 8. 定时任务支持
- **AI 超时检测**：`AiTimeoutDetectionTask` 定期检查 AI 引擎任务状态
- **自动重试机制**：根据状态更新模板 `aiStatus`

---

## 📚 相关文档

- [项目主文档](../README.md) - 项目整体概述
- [前端文档](../frontend-vue/FRONTEND_README.md) - 前端应用
- [AI 引擎文档](../ai-engine/AI-ENGINE_README.md) - AI 引擎
- [开发约定](./AI_DEV_PROMPT.md) - 后端 AI 开发约定（强制）

---

**SmartConstruct Backend** - 让智能教学更简单 ✨
