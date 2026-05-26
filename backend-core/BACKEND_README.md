# SmartConstruct 后端核心服务

## 项目简介

SmartConstruct 是一个智慧建造实验室管理系统的后端服务，基于 Spring Boot 构建，提供用户认证、组织管理、师生信息管理、实训流程编排、实时通信等核心功能。

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
│         - 权限校验 (Authorization)                          │
│         - CORS 配置                                         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      应用层 (Application)                   │
│                    REST API Controllers                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │   Auth   │ │Training  │ │   Org    │ │Dashboard │      │
│  │Controller│ │Controller│ │Controller│ │Controller│      │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      服务层 (Service)                       │
│                    Business Logic Layer                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │SysUser   │ │Training  │ │   Org     │ │ Dashboard │      │
│  │Service   │ │Template  │ │ Service   │ │ Service  │      │
│  │          │ │Service   │ │           │ │          │      │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      数据层 (Data Access)                    │
│                   MyBatis-Plus Mapper                       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │SysUser   │ │WfTraining│ │BizDept   │ │BizStudent│      │
│  │Mapper    │ │Template  │ │Mapper    │ │Mapper    │      │
│  │          │ │Mapper    │ │           │ │          │      │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      实时通信层 (WebSocket)                  │
│            TrainingWebSocketHandler + Interceptor          │
│         - 节点状态广播 (NODE_STATUS)                        │
│         - 心跳检测 (PING/PONG)                              │
│         - 自动重连 (Reconnection)                           │
└─────────────────────────────────────────────────────────────┘
```

## 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 2.7.18 |
| 数据库 | MySQL | 8.0+ |
| ORM | MyBatis-Plus | 3.5.5 |
| 安全 | Spring Security + JWT | jjwt 0.12.5 |
| 实时通信 | WebSocket | Spring WebSocket |
| 工具库 | Hutool | 5.8.26 |
| JSON处理 | Jackson | Spring Boot 内置 |
| 代码简化 | Lombok | Spring Boot 内置 |

## 环境要求

- JDK 8 或更高版本（推荐 JDK 11+）
- Maven 3.6+
- MySQL 8.0+

## 快速开始

### 1. 克隆项目

```bash
git clone <项目地址>
cd ai-smartconstruct-lab/backend-core
```

### 2. 数据库配置

创建 MySQL 数据库：

```sql
CREATE DATABASE `ai-smartconstruct-lab` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `src/main/resources/application.properties` 中的数据库连接信息：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ai-smartconstruct-lab?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=123456
```

### 3. 构建项目

```bash
mvn clean package -DskipTests
```

### 4. 运行项目

**开发模式**:
```bash
mvn spring-boot:run
```

**生产模式**:
```bash
java -jar target/backend-core-0.0.1-SNAPSHOT.jar
```

### 5. 访问服务

服务启动后访问: http://localhost:8080

---

## 项目结构

```
backend-core/
├── src/main/java/com/smartconstruct/backend_core/
│   ├── annotation/          # 自定义注解
│   │   └── OperationLog.java           # 操作日志注解
│   ├── aspect/              # AOP切面
│   │   └── OperationLogAspect.java     # 操作日志切面
│   ├── config/              # 配置类
│   │   ├── SecurityConfig.java         # Spring Security配置
│   │   ├── JwtAuthenticationFilter.java # JWT认证过滤器
│   │   ├── PasswordEncoderConfig.java  # 密码编码器配置
│   │   ├── WebSocketConfig.java        # WebSocket配置
│   │   ├── AsyncConfig.java            # 异步线程池配置
│   │   ├── MybatisPlusConfig.java      # MyBatis-Plus配置
│   │   └── GlobalExceptionHandler.java # 全局异常处理
│   ├── controller/          # REST API 控制器
│   │   ├── AuthController.java         # 认证接口
│   │   ├── InitController.java         # 初始化接口
│   │   ├── PublicDataController.java   # 公共数据接口
│   │   ├── FileController.java         # 文件上传接口
│   │   ├── StudentTrainingController.java    # 学生实训接口
│   │   ├── StudentTrainingFlowController.java # 学生实训流程接口
│   │   ├── StudentDashboardController.java   # 学生仪表板接口
│   │   ├── StudentCourseController.java      # 学生课程接口
│   │   ├── TeacherTrainingController.java    # 教师实训接口
│   │   ├── TeacherTemplateController.java    # 教师模板接口
│   │   ├── TeacherTrainingTaskController.java # 任务下发接口
│   │   ├── TeacherDashboardController.java   # 教师仪表板接口
│   │   ├── TeacherCourseController.java      # 教师课程接口
│   │   ├── TeacherQuestionBankController.java # 教师题库管理接口
│   │   ├── TeacherQuestionController.java    # 教师题目管理接口
│   │   ├── AdminOrgController.java     # 组织管理接口
│   │   ├── AdminStudentController.java # 学生管理接口
│   │   ├── AdminTeacherController.java # 教师管理接口
│   │   ├── AdminCourseController.java  # 课程管理接口
│   │   ├── AdminNodeController.java    # 节点管理接口
│   │   ├── AdminTemplateController.java # 模板管理接口
│   │   ├── AdminQuestionController.java # 题目管理接口
│   │   ├── AdminLogController.java     # 日志管理接口
│   │   ├── AdminTicketController.java  # 工单管理接口
│   │   └── AdminFeedbackController.java # 反馈管理接口
│   ├── dto/                 # 数据传输对象
│   │   ├── ApiResult.java              # 统一响应封装
│   │   ├── PageResult.java             # 分页响应封装
│   │   ├── LoginRequest.java           # 登录请求DTO
│   │   ├── RegisterRequest.java        # 注册请求DTO
│   │   ├── AuthResponse.java           # 认证响应DTO
│   │   ├── ParticipationDetailVO.java  # 参与详情VO
│   │   ├── TrainingNodeProgressDTO.java # 节点进度DTO
│   │   ├── StudentVO.java              # 学生VO
│   │   ├── TeacherVO.java              # 教师VO
│   │   ├── CourseVO.java               # 课程VO
│   │   ├── QuestionBankVO.java         # 题库VO
│   │   ├── StudentCreateRequest.java   # 学生创建请求DTO
│   │   ├── StudentUpdateRequest.java   # 学生更新请求DTO
│   │   ├── TeacherCreateRequest.java   # 教师创建请求DTO
│   │   ├── TeacherUpdateRequest.java   # 教师更新请求DTO
│   │   ├── CourseRequest.java          # 课程请求DTO
│   │   ├── DeptRequest.java            # 院系请求DTO
│   │   ├── MajorRequest.java           # 专业请求DTO
│   │   ├── ClassRequest.java           # 班级请求DTO
│   │   └── 请求/响应DTO...
│   ├── entity/              # 数据库实体类
│   │   ├── SysUser.java                # 系统用户
│   │   ├── SysResource.java            # 系统资源
│   │   ├── BizStudent.java             # 学生信息
│   │   ├── BizTeacher.java             # 教师信息
│   │   ├── BizTrainingTask.java        # 实训任务
│   │   ├── BizTrainingParticipation.java # 实训参与记录
│   │   ├── WfTrainingTemplate.java     # 实训模板
│   │   ├── WfStudentActivityState.java # 学生活动状态
│   │   ├── WfGlobalActivityState.java  # 全局活动状态
│   │   ├── WfNodeDef.java              # 节点定义
│   │   ├── BizDepartment.java          # 院系信息
│   │   ├── BizMajor.java               # 专业信息
│   │   ├── BizAdminClass.java          # 班级信息
│   │   ├── BizCourse.java              # 课程信息
│   │   ├── BizStudentCourse.java       # 学生选课
│   │   ├── BizQuestion.java            # 题目
│   │   ├── BizQuestionBank.java        # 题库
│   │   ├── BizMindmapRecord.java       # 思维导图记录
│   │   ├── BizMindmapEvalDetail.java   # 思维导图评测详情
│   │   ├── BizTrainingExamRecord.java  # 考试记录
│   │   ├── BizTrainingChatLog.java     # 聊天日志
│   │   ├── SysOperationLog.java        # 操作日志
│   │   ├── SysFeedback.java            # 反馈
│   │   └── SysTicket.java              # 工单
│   ├── mapper/              # MyBatis 映射接口
│   │   ├── SysUserMapper.java
│   │   ├── SysResourceMapper.java
│   │   ├── BizStudentMapper.java
│   │   ├── BizTeacherMapper.java
│   │   ├── BizTrainingTaskMapper.java
│   │   ├── BizTrainingParticipationMapper.java
│   │   ├── WfTrainingTemplateMapper.java
│   │   ├── WfStudentActivityStateMapper.java
│   │   ├── WfGlobalActivityStateMapper.java
│   │   ├── WfNodeDefMapper.java
│   │   ├── BizDepartmentMapper.java
│   │   ├── BizMajorMapper.java
│   │   ├── BizAdminClassMapper.java
│   │   ├── BizCourseMapper.java
│   │   ├── BizStudentCourseMapper.java
│   │   ├── BizQuestionMapper.java
│   │   ├── BizQuestionBankMapper.java
│   │   ├── BizMindmapRecordMapper.java
│   │   ├── BizMindmapEvalDetailMapper.java
│   │   ├── BizTrainingExamRecordMapper.java
│   │   ├── BizTrainingChatLogMapper.java
│   │   ├── SysOperationLogMapper.java
│   │   ├── SysFeedbackMapper.java
│   │   └── SysTicketMapper.java
│   ├── service/             # 业务服务层
│   │   ├── impl/            # 服务实现类
│   │   │   ├── SysUserServiceImpl.java       # 用户服务实现
│   │   │   ├── TrainingTemplateServiceImpl.java # 模板服务实现
│   │   │   ├── StudentActivityStateServiceImpl.java # 活动状态服务
│   │   │   ├── GlobalActivityStateServiceImpl.java # 全局状态服务
│   │   │   ├── StudentServiceImpl.java       # 学生服务实现
│   │   │   ├── TeacherServiceImpl.java       # 教师服务实现
│   │   │   ├── TrainingTaskServiceImpl.java  # 实训任务服务实现
│   │   │   ├── TrainingParticipationServiceImpl.java # 参与服务实现
│   │   │   ├── TrainingChatLogServiceImpl.java # 聊天日志服务实现
│   │   │   ├── TrainingExamRecordServiceImpl.java # 考试记录服务实现
│   │   │   ├── CourseServiceImpl.java        # 课程服务实现
│   │   │   ├── StudentCourseServiceImpl.java  # 学生选课服务实现
│   │   │   ├── DepartmentServiceImpl.java    # 院系服务实现
│   │   │   ├── MajorServiceImpl.java         # 专业服务实现
│   │   │   ├── AdminClassServiceImpl.java    # 班级服务实现
│   │   │   ├── QuestionServiceImpl.java      # 题目服务实现
│   │   │   ├── QuestionBankServiceImpl.java  # 题库服务实现
│   │   │   ├── MindmapRecordServiceImpl.java # 思维导图服务实现
│   │   │   ├── MindmapEvalDetailServiceImpl.java # 评测详情服务实现
│   │   │   ├── OperationLogServiceImpl.java  # 操作日志服务实现
│   │   │   ├── TicketServiceImpl.java        # 工单服务实现
│   │   │   ├── FeedbackServiceImpl.java      # 反馈服务实现
│   │   │   ├── NodeDefServiceImpl.java       # 节点定义服务实现
│   │   │   └── SysResourceServiceImpl.java   # 资源服务实现
│   │   ├── SysUserService.java               # 用户服务接口
│   │   ├── ITrainingTemplateService.java     # 模板服务接口
│   │   ├── IStudentActivityStateService.java # 活动状态服务接口
│   │   ├── IGlobalActivityStateService.java  # 全局状态服务接口
│   │   ├── IStudentService.java              # 学生服务接口
│   │   ├── ITeacherService.java              # 教师服务接口
│   │   ├── ITrainingTaskService.java         # 实训任务服务接口
│   │   ├── ITrainingParticipationService.java # 参与服务接口
│   │   ├── ITrainingChatLogService.java      # 聊天日志服务接口
│   │   ├── ITrainingExamRecordService.java   # 考试记录服务接口
│   │   ├── ICourseService.java               # 课程服务接口
│   │   ├── IStudentCourseService.java        # 学生选课服务接口
│   │   ├── IDepartmentService.java           # 院系服务接口
│   │   ├── IMajorService.java                # 专业服务接口
│   │   ├── IAdminClassService.java           # 班级服务接口
│   │   ├── IQuestionService.java             # 题目服务接口
│   │   ├── IQuestionBankService.java         # 题库服务接口
│   │   ├── IMindmapRecordService.java        # 思维导图服务接口
│   │   ├── IMindmapEvalDetailService.java    # 评测详情服务接口
│   │   ├── IOperationLogService.java         # 操作日志服务接口
│   │   ├── ITicketService.java               # 工单服务接口
│   │   ├── IFeedbackService.java             # 反馈服务接口
│   │   ├── INodeDefService.java              # 节点定义服务接口
│   │   └── ISysResourceService.java          # 资源服务接口
│   ├── util/                # 工具类
│   │   ├── JwtUtil.java                # JWT工具类
│   │   └── Java8Compat.java            # Java 8兼容工具
│   ├── websocket/           # WebSocket相关
│   │   ├── TrainingWebSocketHandler.java     # WebSocket处理器
│   │   └── TrainingHandshakeInterceptor.java # 握手拦截器
│   └── BackendCoreApplication.java  # 启动类
├── src/main/resources/
│   └── application.properties  # 应用配置
└── pom.xml              # Maven 依赖管理
```

---

## 核心功能模块

### 1. 用户认证模块 (Auth)

**核心类**:
- `AuthController` - 认证接口控制器
- `SysUserServiceImpl` - 用户服务实现
- `JwtUtil` - JWT工具类
- `JwtAuthenticationFilter` - JWT认证过滤器
- `SecurityConfig` - 安全配置

**功能**:
- 用户注册（支持学生、教师角色）
- 用户登录（支持学生/教师/管理员三种角色）
- JWT Token 生成与验证
- 用户信息查询

**认证流程**:
```
1. 客户端发送登录请求 (username, password, roleType)
   ↓
2. AuthController.login() 接收请求
   ↓
3. 验证用户存在性、账号状态、密码、角色
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

---

### 2. 实训流程编排模块 (Training)

**核心类**:
- `TeacherTemplateController` - 模板管理控制器
- `TrainingTemplateServiceImpl` - 模板服务实现
- `TeacherTrainingTaskController` - 任务下发控制器
- `StudentTrainingController` - 学生实训控制器
- `StudentTrainingFlowController` - 学生实训流程控制器
- `TeacherTrainingController` - 教师实训控制器
- `TrainingWebSocketHandler` - WebSocket实时通信

**功能**:
- 实训模板创建与管理（图形化编排）
- AI模拟处理（异步处理模板数据）
- 实训任务下发（班级/课程范围）
- 学生实训执行（节点推进）
- 实时状态同步（WebSocket广播）

**模板创建流程**:
```
1. 教师在 TrainingCreate.vue 中编排节点和连线
   ↓
2. 点击发布，发送 POST /api/teacher/templates
   ↓
3. TeacherTemplateController.create() 接收请求
   ↓
4. 保存模板到 wf_training_template (aiStatus=1)
   ↓
5. 异步调用 processTemplateMockAi() 模拟AI处理
   ↓
6. AI处理完成后更新模板 (aiStatus=2)
   ↓
7. 教师可在模板管理页面查看处理状态
```

**任务下发流程**:
```
1. 教师选择模板和学生范围（班级/课程）
   ↓
2. 发送 POST /api/teacher/training-tasks
   ↓
3. TeacherTrainingTaskController.create() 接收请求
   ↓
4. 验证模板存在且AI处理完成
   ↓
5. 根据范围查询目标学生列表
   ↓
6. 创建实训任务 (biz_training_task)
   ↓
7. 批量创建参与记录 (biz_training_participation)
   ↓
8. 返回任务ID和学生数量
```

**学生实训流程**:
```
1. 学生查看可参与的实训任务列表
   ↓
2. 点击开始，发送 POST /api/student/training-tasks/{id}/start
   ↓
3. StudentTrainingController.start() 接收请求
   ↓
4. 更新参与状态为进行中 (status=1)
   ↓
5. 创建/更新学生活动状态 (wf_student_activity_state)
   ↓
6. 学生完成当前节点，发送 POST /api/student/training-tasks/{id}/next
   ↓
7. 更新当前节点ID，推进到下一节点
   ↓
8. 如到达结束节点，标记为已完成 (status=2)
```

**课中实训实时同步流程**:
```
1. 教师点击"下一节点"
   ↓
2. TeacherTrainingController.nextNode() 接收请求
   ↓
3. 更新全局活动状态 (wf_global_activity_state)
   ↓
4. 通过 WebSocket 广播消息到所有学生
   ↓
5. 学生端收到消息，自动刷新当前节点
```

---

### 3. 组织管理模块 (AdminOrg)

**核心类**:
- `AdminOrgController` - 组织管理控制器
- `DepartmentServiceImpl` - 院系服务
- `MajorServiceImpl` - 专业服务
- `AdminClassServiceImpl` - 班级服务

**功能**:
- 院系管理（增删改查）
- 专业管理（增删改查）
- 班级管理（增删改查）

---

### 4. 师生管理模块

**核心类**:
- `AdminStudentController` - 学生管理控制器
- `AdminTeacherController` - 教师管理控制器
- `StudentDashboardController` - 学生仪表板控制器
- `StudentCourseController` - 学生课程控制器
- `TeacherDashboardController` - 教师仪表板控制器
- `TeacherCourseController` - 教师课程控制器

**功能**:
- 学生信息管理（列表、详情、创建、更新、删除）
- 教师信息管理（列表、详情、创建、更新、删除）
- 个人资料管理
- 密码修改
- 课程选修管理

---

### 5. 课程资源模块

**核心类**:
- `TeacherCourseController` - 教师课程控制器
- `StudentCourseController` - 学生课程控制器
- `AdminCourseController` - 管理员课程控制器
- `AdminQuestionController` - 题目管理控制器

**功能**:
- 课程管理 (BizCourse)
- 课程学生关联 (BizStudentCourse)
- 题库管理 (BizQuestionBank)
- 题目管理 (BizQuestion)

---

### 6. 系统运维模块

**核心类**:
- `OperationLogAspect` - 操作日志切面
- `AdminLogController` - 日志管理控制器
- `AdminTicketController` - 工单管理控制器
- `AdminFeedbackController` - 反馈管理控制器

**功能**:
- 操作日志记录（AOP切面实现）
- 工单管理 (SysTicket)
- 反馈管理 (SysFeedback)

---

## API 接口文档

### 认证接口

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 登录 | POST | `/api/auth/login` | 用户登录获取 Token |
| 注册 | POST | `/api/auth/register` | 用户注册 |
| 登出 | POST | `/api/auth/logout` | 用户登出 |
| 用户信息 | GET | `/api/auth/userinfo` | 获取当前用户信息（需认证） |

### 实训模板接口（教师）

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 模板列表 | GET | `/api/teacher/templates` | 获取模板列表 |
| 创建模板 | POST | `/api/teacher/templates` | 创建模板（触发AI处理） |
| 删除模板 | DELETE | `/api/teacher/templates/{id}` | 删除模板 |

### 实训任务接口（教师）

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 下发任务 | POST | `/api/teacher/training-tasks` | 创建并下发实训任务 |
| 推进节点 | POST | `/api/teacher/training/{taskId}/next` | 课中实训推进到下一节点 |

### 学生实训接口

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 参与详情 | GET | `/api/student/training-tasks/{taskId}/participation` | 获取实训参与详情 |
| 开始实训 | POST | `/api/student/training-tasks/{participationId}/start` | 开始实训 |
| 下一节点 | POST | `/api/student/training-tasks/{participationId}/next` | 进入下一节点 |

### 组织管理接口（管理员）

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 院系列表 | GET | `/api/admin/org/depts` | 获取院系列表 |
| 创建院系 | POST | `/api/admin/org/depts` | 创建院系 |
| 更新院系 | PUT | `/api/admin/org/depts/{id}` | 更新院系信息 |
| 删除院系 | DELETE | `/api/admin/org/depts/{id}` | 删除院系 |
| 专业列表 | GET | `/api/admin/org/majors` | 获取专业列表 |
| 创建专业 | POST | `/api/admin/org/majors` | 创建专业 |
| 更新专业 | PUT | `/api/admin/org/majors/{id}` | 更新专业信息 |
| 删除专业 | DELETE | `/api/admin/org/majors/{id}` | 删除专业 |
| 班级列表 | GET | `/api/admin/org/classes` | 获取班级列表 |
| 创建班级 | POST | `/api/admin/org/classes` | 创建班级 |
| 更新班级 | PUT | `/api/admin/org/classes/{id}` | 更新班级信息 |
| 删除班级 | DELETE | `/api/admin/org/classes/{id}` | 删除班级 |

### 课程管理接口（管理员）

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 课程列表 | GET | `/api/admin/course` | 获取课程列表 |
| 创建课程 | POST | `/api/admin/course` | 创建课程 |
| 更新课程 | PUT | `/api/admin/course/{id}` | 更新课程信息 |
| 删除课程 | DELETE | `/api/admin/course/{id}` | 删除课程 |

### 题目管理接口（管理员）

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 题库列表 | GET | `/api/admin/question/banks` | 获取题库列表 |
| 创建题库 | POST | `/api/admin/question/banks` | 创建题库 |
| 题目列表 | GET | `/api/admin/question` | 获取题目列表 |
| 创建题目 | POST | `/api/admin/question` | 创建题目 |
| 更新题目 | PUT | `/api/admin/question/{id}` | 更新题目信息 |
| 删除题目 | DELETE | `/api/admin/question/{id}` | 删除题目 |

### 系统运维接口（管理员）

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 操作日志 | GET | `/api/admin/log` | 获取操作日志列表 |
| 工单列表 | GET | `/api/admin/ticket` | 获取工单列表 |
| 创建工单 | POST | `/api/admin/ticket` | 创建工单 |
| 更新工单 | PUT | `/api/admin/ticket/{id}` | 更新工单状态 |
| 反馈列表 | GET | `/api/admin/feedback` | 获取反馈列表 |

---

## 核心实体详解

### 1. 用户相关实体

#### SysUser (系统用户)
**表名**: `sys_user`
**文件位置**: `entity/SysUser.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| username | String | 用户名（登录账号） |
| passwordHash | String | 密码哈希（BCrypt加密） |
| phone | String | 手机号 |
| avatarUrl | String | 头像URL |
| bio | String | 个人简介 |
| roleType | Integer | 角色类型：1=管理员，2=教师，3=学生 |
| status | Integer | 账号状态：0=正常，1=封禁 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

**说明**: 实现 Spring Security 的 UserDetails 接口，支持用户认证和授权。

#### BizStudent (学生信息)
**表名**: `biz_student`
**文件位置**: `entity/BizStudent.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID（关联sys_user.id） |
| studentNo | String | 学号 |
| realName | String | 真实姓名 |
| deptId | Long | 院系ID |
| majorId | Long | 专业ID |
| classId | Long | 班级ID |

#### BizTeacher (教师信息)
**表名**: `biz_teacher`
**文件位置**: `entity/BizTeacher.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID（关联sys_user.id） |
| employeeNo | String | 工号 |
| realName | String | 真实姓名 |
| deptId | Long | 院系ID |

---

### 2. 实训相关实体

#### BizTrainingTask (实训任务)
**表名**: `biz_training_task`
**文件位置**: `entity/BizTrainingTask.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| templateId | Long | 模板ID（关联wf_training_template.id） |
| teacherId | Long | 教师ID（关联sys_user.id） |
| taskName | String | 任务名称 |
| startTime | LocalDateTime | 开始时间 |
| endTime | LocalDateTime | 结束时间 |
| isInClass | Integer | 是否课堂实训：0=否，1=是 |
| hasGroup | Integer | 是否分组：0=否，1=是 |
| status | Integer | 任务状态：0=未开始，1=进行中，2=已结束 |
| dispatchScope | Integer | 下发范围：1=班级，2=课程 |
| dispatchTargetId | Long | 下发目标ID |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

#### WfTrainingTemplate (实训模板)
**表名**: `wf_training_template`
**文件位置**: `entity/WfTrainingTemplate.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| templateName | String | 模板名称 |
| rawCanvasJson | Object | 原始画布JSON（前端编排数据） |
| standardPayloadJson | Object | 标准执行载荷JSON（AI处理后） |
| aiStatus | Integer | AI处理状态：1=处理中，2=完成 |
| errorReason | String | 错误原因 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

**JSON结构示例**:
```json
{
  "orchestration_id": "orch_xxx",
  "nodes": [
    {"node_id": "node_1", "node_type": "START", "name": "开始", "config": {}},
    {"node_id": "node_2", "node_type": "TASK_DISTRIBUTE", "name": "任务分发", "config": {...}},
    {"node_id": "node_3", "node_type": "END", "name": "结束", "config": {}}
  ],
  "edges": [
    {"source": "node_1", "target": "node_2"},
    {"source": "node_2", "target": "node_3"}
  ]
}
```

#### BizTrainingParticipation (实训参与记录)
**表名**: `biz_training_participation`
**文件位置**: `entity/BizTrainingParticipation.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| taskId | Long | 实训任务ID |
| studentId | Long | 学生ID |
| status | Integer | 参与状态：0=未开始，1=进行中，2=已完成 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

#### WfStudentActivityState (学生活动状态)
**表名**: `wf_student_activity_state`
**文件位置**: `entity/WfStudentActivityState.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| participationId | Long | 参与记录ID |
| currentNodeId | String | 当前节点ID（模板节点的node_id） |
| updatedAt | LocalDateTime | 更新时间 |

#### WfGlobalActivityState (全局活动状态)
**表名**: `wf_global_activity_state`
**文件位置**: `entity/WfGlobalActivityState.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| taskId | Long | 实训任务ID |
| currentNodeId | String | 当前节点ID |
| updatedAt | LocalDateTime | 更新时间 |

**说明**: 用于课中实训，教师推进节点时更新，学生通过WebSocket接收变更通知。

#### WfNodeDef (节点定义)
**表名**: `wf_node_def`
**文件位置**: `entity/WfNodeDef.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| nodeCode | String | 节点编码 |
| nodeName | String | 节点名称 |
| isActive | Integer | 是否启用：0=禁用，1=启用 |

---

### 3. 组织相关实体

#### BizDepartment (院系)
**表名**: `biz_department`
**文件位置**: `entity/BizDepartment.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| deptName | String | 院系名称 |
| deptCode | String | 院系编码 |

#### BizMajor (专业)
**表名**: `biz_major`
**文件位置**: `entity/BizMajor.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| majorName | String | 专业名称 |
| majorCode | String | 专业编码 |
| deptId | Long | 所属院系ID |

#### BizAdminClass (班级)
**表名**: `biz_admin_class`
**文件位置**: `entity/BizAdminClass.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| className | String | 班级名称 |
| classCode | String | 班级编码 |
| majorId | Long | 所属专业ID |
| grade | Integer | 年级 |

---

### 4. 课程相关实体

#### BizCourse (课程)
**表名**: `biz_course`
**文件位置**: `entity/BizCourse.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| courseName | String | 课程名称 |
| courseCode | String | 课程编码 |
| teacherId | Long | 授课教师ID |
| deptId | Long | 所属院系ID |
| credit | Integer | 学分 |
| description | String | 课程描述 |
| status | Integer | 课程状态 |

#### BizStudentCourse (学生选课)
**表名**: `biz_student_course`
**文件位置**: `entity/BizStudentCourse.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| studentId | Long | 学生ID |
| courseId | Long | 课程ID |
| enrollTime | LocalDateTime | 选课时间 |

---

### 5. 题库相关实体

#### BizQuestionBank (题库)
**表名**: `biz_question_bank`
**文件位置**: `entity/BizQuestionBank.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| bankName | String | 题库名称 |
| description | String | 题库描述 |
| creatorId | Long | 创建者ID |
| isShared | Integer | 是否共享：0=私有，1=共享 |

#### BizQuestion (题目)
**表名**: `biz_question`
**文件位置**: `entity/BizQuestion.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| bankId | Long | 所属题库ID |
| questionType | Integer | 题目类型 |
| content | String | 题目内容 |
| answer | String | 正确答案 |
| score | Integer | 分值 |

---

### 6. 思维导图相关实体

#### BizMindmapRecord (思维导图记录)
**表名**: `biz_mindmap_record`
**文件位置**: `entity/BizMindmapRecord.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| participationId | Long | 参与记录ID |
| nodeId | String | 节点ID |
| mindmapData | String | 思维导图数据（JSON） |
| submitTime | LocalDateTime | 提交时间 |

#### BizMindmapEvalDetail (思维导图评测详情)
**表名**: `biz_mindmap_eval_detail`
**文件位置**: `entity/BizMindmapEvalDetail.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| recordId | Long | 思维导图记录ID |
| evalScore | Integer | 评测分数 |
| evalComment | String | 评测评语 |
| evalTime | LocalDateTime | 评测时间 |

---

### 7. 系统运维实体

#### SysOperationLog (操作日志)
**表名**: `sys_operation_log`
**文件位置**: `entity/SysOperationLog.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| userId | Long | 操作用户ID |
| action | String | 操作描述 |
| method | String | 请求方法 |
| url | String | 请求URL |
| ip | String | 客户端IP |
| params | String | 请求参数 |
| result | String | 操作结果 |
| createTime | LocalDateTime | 操作时间 |

#### SysFeedback (反馈)
**表名**: `sys_feedback`
**文件位置**: `entity/SysFeedback.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| userId | Long | 反馈用户ID |
| content | String | 反馈内容 |
| type | Integer | 反馈类型 |
| status | Integer | 处理状态 |
| createTime | LocalDateTime | 创建时间 |

#### SysTicket (工单)
**表名**: `sys_ticket`
**文件位置**: `entity/SysTicket.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| userId | Long | 提交用户ID |
| title | String | 工单标题 |
| content | String | 工单内容 |
| type | Integer | 工单类型 |
| status | Integer | 工单状态 |
| createTime | LocalDateTime | 创建时间 |

---

## 实体关联关系图

```
┌─────────────────┐     ┌─────────────────────┐     ┌─────────────────┐
│   SysUser       │     │  BizTrainingTask    │     │ WfTrainingTemplate│
│  (系统用户)      │◄────┤   (实训任务)         │────►│   (实训模板)      │
│                 │     │                     │     │                 │
│ - id            │     │ - id                │     │ - id            │
│ - username      │     │ - templateId        │     │ - templateName  │
│ - roleType      │     │ - teacherId         │     │ - rawCanvasJson │
│ - passwordHash  │     │ - taskName          │     │ - aiStatus      │
└─────────────────┘     │ - status            │     └─────────────────┘
         ▲              └─────────────────────┘              ▲
         │                       │                           │
         │                       ▼                           │
         │              ┌─────────────────────┐              │
         │              │BizTrainingParticipation            │
         │              │   (实训参与记录)      │              │
         │              │                     │              │
         │              │ - id                │              │
         └──────────────┤ - taskId            │              │
                        │ - studentId         │              │
                        │ - status            │              │
                        └─────────────────────┘              │
                                   │                         │
                                   ▼                         │
                        ┌─────────────────────┐              │
                        │WfStudentActivityState              │
                        │  (学生活动状态)       │◄─────────────┘
                        │                     │   currentNodeId
                        │ - id                │
                        │ - participationId   │
                        │ - currentNodeId     │
                        └─────────────────────┘

┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ BizDepartment   │◄────┤   BizMajor      │◄────┤ BizAdminClass   │
│   (院系)         │     │   (专业)         │     │   (班级)         │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         ▲                                              ▲
         │                                              │
         └──────────────────┬───────────────────────────┘
                            │
                    ┌───────┴───────┐
                    │   BizStudent  │
                    │   (学生信息)   │
                    └───────────────┘
```

---

## 安全说明

### JWT Token 使用

登录成功后，服务端返回 JWT Token，客户端需在每次请求的 `Authorization` 头中携带：

```
Authorization: Bearer <token>
```

### 权限控制

| 路径模式 | 权限要求 |
|---------|---------|
| `/api/auth/login` | 公开访问 |
| `/api/auth/register` | 公开访问 |
| `/api/auth/captcha` | 公开访问 |
| `/api/public/**` | 公开访问 |
| `/api/**` | 需要认证 |

### 角色鉴权说明

当前实现仅区分"已认证"和"未认证"，未实现基于角色的细粒度接口级权限控制（如 @PreAuthorize）。业务层需手动校验用户身份。

---

## 配置说明

### JWT 配置

```properties
jwt.secret=smartconstruct-jwt-secret-key-2026-must-be-at-least-256bits-long!!
jwt.expiration=86400000  # Token有效期（毫秒），默认24小时
```

### 跨域配置

后端已配置允许所有来源访问（开发环境）。生产环境应限制为特定域名：

```java
// SecurityConfig.java
config.addAllowedOriginPattern("*");  // 开发环境
// config.addAllowedOrigin("https://your-domain.com");  // 生产环境
```

### 数据库配置

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ai-smartconstruct-lab?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### WebSocket 配置

WebSocket 端点：`ws://localhost:8080/ws/training`

---

## 开发指南

### 添加新功能

1. 在 `entity/` 目录创建实体类
2. 在 `mapper/` 目录创建 Mapper 接口
3. 在 `service/` 目录创建服务接口
4. 在 `service/impl/` 目录实现服务
5. 在 `controller/` 目录创建控制器
6. 在 `dto/` 目录添加对应的请求/响应对象

### 代码规范

- 遵循 Spring Boot 命名规范
- 使用 Lombok 简化代码（@Data、@Builder 等）
- Service 层使用事务注解 `@Transactional`
- Controller 层使用统一的 `ApiResult` 封装响应
- 所有类和方法必须添加 JavaDoc 注释
- 异步方法使用 `@Async` 注解

---

## 常见问题

### 1. 启动时报 CircularReference 错误

**问题描述**: 
```
Error creating bean with name 'jwtAuthenticationFilter': Requested bean is currently in creation: Is there an unresolvable circular reference?
```

**解决方案**: 确保 `PasswordEncoder` 已在独立的 `PasswordEncoderConfig.java` 中定义，避免循环依赖。

### 2. 前端请求报 CORS 错误

**问题描述**:
```
Access-Control-Allow-Origin header missing
```

**解决方案**: 确保前端域名已添加到 `SecurityConfig.java` 的 `corsConfigurationSource()` 方法中。

### 3. 登录时报 401 错误

**问题描述**: 输入正确账号密码仍无法登录

**排查步骤**:
1. 检查后端服务是否正常运行
2. 确认数据库中存在该用户
3. 检查密码是否正确（数据库存储的是 BCrypt 加密后的密码）
4. 确认请求格式正确（Content-Type: application/json）

### 4. WebSocket 连接失败

**问题描述**: 前端无法建立 WebSocket 连接

**排查步骤**:
1. 检查后端是否启用了 WebSocket 配置
2. 确认 WebSocket 端点地址正确
3. 检查 CORS 配置是否允许 WebSocket 连接
4. 查看后端日志是否有相关错误信息

---

## 联系方式

如有问题请联系开发团队。