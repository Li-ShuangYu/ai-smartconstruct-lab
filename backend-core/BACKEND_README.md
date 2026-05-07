# SmartConstruct 后端核心服务

## 项目简介

SmartConstruct 是一个智慧建造实验室管理系统的后端服务，基于 Spring Boot 构建，提供用户认证、组织管理、师生信息管理等核心功能。

## 技术栈

- **框架**: Spring Boot 2.7.18
- **数据库**: MySQL 8.0+
- **ORM**: JPA + MyBatis-Plus 3.5.5
- **安全**: Spring Security + JWT (jjwt 0.12.5)
- **工具库**: Hutool 5.8.26, Jackson, Lombok

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
spring.datasource.password=your_password
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

## 项目结构

```
backend-core/
├── src/main/java/com/smartconstruct/backend_core/
│   ├── config/          # 配置类（安全配置、过滤器等）
│   │   ├── SecurityConfig.java      # Spring Security配置
│   │   ├── JwtAuthenticationFilter.java  # JWT认证过滤器
│   │   └── PasswordEncoderConfig.java    # 密码编码器配置
│   ├── controller/      # REST API 控制器
│   │   ├── AuthController.java      # 认证接口
│   │   ├── AdminOrgController.java  # 组织管理接口
│   │   └── PublicDataController.java # 公共数据接口
│   ├── dto/             # 数据传输对象
│   │   ├── ApiResult.java       # 统一响应封装
│   │   ├── LoginRequest.java    # 登录请求DTO
│   │   ├── RegisterRequest.java # 注册请求DTO
│   │   └── AuthResponse.java    # 认证响应DTO
│   ├── entity/          # 数据库实体类
│   │   ├── SysUser.java          # 系统用户
│   │   ├── BizStudent.java       # 学生信息
│   │   ├── BizTeacher.java       # 教师信息
│   │   ├── BizDepartment.java    # 院系信息
│   │   ├── BizMajor.java         # 专业信息
│   │   └── BizAdminClass.java    # 班级信息
│   ├── mapper/          # MyBatis 映射接口
│   ├── service/         # 业务服务层
│   │   └── impl/        # 服务实现类
│   ├── util/            # 工具类（JWT工具等）
│   │   └── JwtUtil.java          # JWT工具类
│   └── BackendCoreApplication.java  # 启动类
├── src/main/resources/
│   └── application.properties  # 应用配置
└── pom.xml              # Maven 依赖管理
```

## 核心功能模块

### 1. 用户认证模块 (Auth)
- 用户注册（支持学生、教师角色）
- 用户登录（支持学生/教师/管理员三种角色）
- JWT Token 验证
- 用户信息查询

### 2. 组织管理模块 (AdminOrg)
- 院系管理（增删改查）
- 专业管理（增删改查）
- 班级管理（增删改查）

### 3. 师生管理模块
- 学生信息管理
- 教师信息管理
- 实训任务管理 (BizTrainingTask)
- 实训参与记录 (BizTrainingParticipation)
- 工作流节点定义 (WfNodeDef)
- 工作流学生状态 (WfStudentActivityState)
- 工作流模板 (WfTrainingTemplate)

### 4. 课程资源模块
- 课程管理 (BizCourse)
- 课程学生关联 (BizStudentCourse)
- 题库管理 (BizQuestionBank)
- 题目管理 (BizQuestion)

### 5. 系统运维模块
- 操作日志 (SysOperationLog)
- 工单管理 (SysTicket)
- 反馈管理 (SysFeedback)

### 6. 公共数据模块 (PublicData)
- 提供公开的基础数据查询（无需认证）

## API 接口

### 认证接口

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 登录 | POST | `/api/auth/login` | 用户登录获取 Token |
| 注册 | POST | `/api/auth/register` | 用户注册 |
| 登出 | POST | `/api/auth/logout` | 用户登出 |
| 用户信息 | GET | `/api/auth/userinfo` | 获取当前用户信息（需认证） |

### 公共接口

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 获取院系列表 | GET | `/api/public/departments` | 获取所有院系（公开） |
| 获取专业列表 | GET | `/api/public/majors` | 获取所有专业（公开） |

### 组织管理接口

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 获取院系列表 | GET | `/api/admin/org/depts` | 获取院系列表 |
| 创建院系 | POST | `/api/admin/org/depts` | 创建院系 |
| 更新院系 | PUT | `/api/admin/org/depts/{id}` | 更新院系信息 |
| 删除院系 | DELETE | `/api/admin/org/depts/{id}` | 删除院系 |
| 获取专业列表 | GET | `/api/admin/org/majors` | 获取专业列表 |
| 创建专业 | POST | `/api/admin/org/majors` | 创建专业 |
| 更新专业 | PUT | `/api/admin/org/majors/{id}` | 更新专业信息 |
| 删除专业 | DELETE | `/api/admin/org/majors/{id}` | 删除专业 |
| 获取班级列表 | GET | `/api/admin/org/classes` | 获取班级列表 |
| 创建班级 | POST | `/api/admin/org/classes` | 创建班级 |
| 更新班级 | PUT | `/api/admin/org/classes/{id}` | 更新班级信息 |
| 删除班级 | DELETE | `/api/admin/org/classes/{id}` | 删除班级 |

### 教师端接口

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 仪表盘统计 | GET | `/api/teacher/dashboard/stats` | 获取工作台统计数据 |
| 实训任务列表 | GET | `/api/teacher/training-tasks` | 获取实训任务列表 |
| 模板列表 | GET | `/api/teacher/templates` | 获取模板列表 |
| 创建模板 | POST | `/api/teacher/templates` | 创建模板 |
| 删除模板 | DELETE | `/api/teacher/templates/{id}` | 删除模板 |
| 课程列表 | GET | `/api/teacher/courses` | 获取课程列表 |
| 创建课程 | POST | `/api/teacher/courses` | 创建课程 |
| 更新课程 | PUT | `/api/teacher/courses/{id}` | 更新课程信息 |
| 删除课程 | DELETE | `/api/teacher/courses/{id}` | 删除课程 |
| 教师信息 | GET | `/api/teacher/profile` | 获取教师个人信息 |
| 更新教师信息 | PUT | `/api/teacher/profile` | 更新教师信息 |
| 修改密码 | PUT | `/api/teacher/password` | 修改密码 |

### 学生端接口

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 实训任务列表 | GET | `/api/student/training-tasks` | 获取可参与的实训任务 |
| 实训参与详情 | GET | `/api/student/training-tasks/{taskId}/participation` | 获取参与详情 |
| 开始实训 | POST | `/api/student/training-tasks/{participationId}/start` | 开始实训 |
| 下一节点 | POST | `/api/student/training-tasks/{participationId}/next` | 进入下一节点 |
| 班级同学 | GET | `/api/student/my-class/classmates` | 获取同班同学 |
| 可选课程 | GET | `/api/student/courses/available` | 获取可选课程 |
| 选修课程 | POST | `/api/student/courses/enroll/{courseId}` | 选修课程 |
| 我的课程 | GET | `/api/student/courses/my` | 获取我的课程 |
| 学生信息 | GET | `/api/student/profile` | 获取学生个人信息 |
| 更新学生信息 | PUT | `/api/student/profile` | 更新学生信息 |
| 修改密码 | PUT | `/api/student/password` | 修改密码 |

### 管理员端接口

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 学生列表 | GET | `/api/admin/students` | 获取学生列表 |
| 学生详情 | GET | `/api/admin/students/{id}` | 获取学生详情 |
| 创建学生 | POST | `/api/admin/students` | 创建学生 |
| 更新学生 | PUT | `/api/admin/students/{id}` | 更新学生信息 |
| 删除学生 | DELETE | `/api/admin/students/{id}` | 删除学生 |
| 教师列表 | GET | `/api/admin/teachers` | 获取教师列表 |
| 教师详情 | GET | `/api/admin/teachers/{id}` | 获取教师详情 |
| 创建教师 | POST | `/api/admin/teachers` | 创建教师 |
| 更新教师 | PUT | `/api/admin/teachers/{id}` | 更新教师信息 |
| 删除教师 | DELETE | `/api/admin/teachers/{id}` | 删除教师 |
| 课程列表 | GET | `/api/admin/courses` | 获取课程列表 |
| 课程详情 | GET | `/api/admin/courses/{id}` | 获取课程详情 |
| 题库列表 | GET | `/api/admin/question-banks` | 获取题库列表 |
| 题库详情 | GET | `/api/admin/question-banks/{id}` | 获取题库详情 |
| 创建题库 | POST | `/api/admin/question-banks` | 创建题库 |
| 更新题库 | PUT | `/api/admin/question-banks/{id}` | 更新题库 |
| 删除题库 | DELETE | `/api/admin/question-banks/{id}` | 删除题库 |
| 共享题库 | PUT | `/api/admin/question-banks/{id}/share` | 共享题库 |
| 题目列表 | GET | `/api/admin/questions` | 获取题目列表 |
| 题目详情 | GET | `/api/admin/questions/{id}` | 获取题目详情 |
| 创建题目 | POST | `/api/admin/questions` | 创建题目 |
| 更新题目 | PUT | `/api/admin/questions/{id}` | 更新题目 |
| 删除题目 | DELETE | `/api/admin/questions/{id}` | 删除题目 |
| 模板列表 | GET | `/api/admin/templates` | 获取模板列表 |
| 模板详情 | GET | `/api/admin/templates/{id}` | 获取模板详情 |
| 删除模板 | DELETE | `/api/admin/templates/{id}` | 删除模板 |
| 模板状态 | PUT | `/api/admin/templates/{id}/status` | 更新模板状态 |
| 节点列表 | GET | `/api/admin/nodes` | 获取节点列表 |
| 节点详情 | GET | `/api/admin/nodes/{id}` | 获取节点详情 |
| 创建节点 | POST | `/api/admin/nodes` | 创建节点 |
| 更新节点 | PUT | `/api/admin/nodes/{id}` | 更新节点 |
| 删除节点 | DELETE | `/api/admin/nodes/{id}` | 删除节点 |
| 切换节点 | PUT | `/api/admin/nodes/{id}/toggle` | 切换节点状态 |
| 工单列表 | GET | `/api/admin/tickets` | 获取工单列表 |
| 工单详情 | GET | `/api/admin/tickets/{id}` | 获取工单详情 |
| 更新工单状态 | PUT | `/api/admin/tickets/{id}/status` | 更新工单状态 |
| 删除工单 | DELETE | `/api/admin/tickets/{id}` | 删除工单 |
| 日志列表 | GET | `/api/admin/logs` | 获取操作日志列表 |
| 反馈列表 | GET | `/api/admin/feedbacks` | 获取反馈列表 |
| 反馈详情 | GET | `/api/admin/feedbacks/{id}` | 获取反馈详情 |
| 删除反馈 | DELETE | `/api/admin/feedbacks/{id}` | 删除反馈 |

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

### 默认测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | user1 | 123456 |

## 配置说明

### JWT 配置

```properties
jwt.secret=smartconstruct-jwt-secret-key-2026-must-be-at-least-256bits-long!!
jwt.expiration=86400000  # Token有效期（毫秒），默认24小时
```

### 跨域配置

后端已配置允许以下前端域名访问：
- http://localhost:5173
- http://localhost:5174
- http://localhost:3000

如需添加其他域名，请修改 `SecurityConfig.java` 中的 `corsConfigurationSource()` 方法。

### 数据库配置

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ai-smartconstruct-lab?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

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

---

## 🗄️ 核心实体与数据结构落地

### 1. 工作流实训相关核心实体

#### BizTrainingTask (实训任务)
**表名**: `biz_training_task`  
**文件位置**: `entity/BizTrainingTask.java`

| 字段 | 类型 | 说明 | 关联 |
|------|------|------|------|
| id | Long | 主键，自增 | - |
| templateId | Long | 实训模板 ID | WfTrainingTemplate.id |
| teacherId | Long | 教师 ID | SysUser.id |
| taskName | String | 任务名称 | - |
| startTime | LocalDateTime | 开始时间 | - |
| endTime | LocalDateTime | 结束时间 | - |
| isInClass | Integer | 是否课堂实训 | - |
| hasGroup | Integer | 是否分组 | - |
| status | Integer | 任务状态 | - |
| dispatchScope | Integer | 发布范围 | - |
| dispatchTargetId | Long | 发布目标 ID | - |
| createdAt | LocalDateTime | 创建时间 | - |
| updatedAt | LocalDateTime | 更新时间 | - |

---

#### WfTrainingTemplate (实训模板)
**表名**: `wf_training_template`  
**文件位置**: `entity/WfTrainingTemplate.java`

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | Long | 主键，自增 | - |
| templateName | String | 模板名称 | - |
| rawCanvasJson | Object | 原始画布 JSON | 使用 JacksonTypeHandler 自动序列化 |
| standardPayloadJson | Object | 标准执行载荷 JSON | AI 处理后生成 |
| aiStatus | Integer | AI 处理状态 | 1=处理中, 2=完成 |
| errorReason | String | 错误原因 | - |
| createdAt | LocalDateTime | 创建时间 | - |
| updatedAt | LocalDateTime | 更新时间 | - |

**JSON 结构** (rawCanvasJson/standardPayloadJson):
```json
{
  "orchestration_id": "orch_xxx",
  "nodes": [
    {"node_id": "node_1", "node_type": "START", "name": "开始", "config": {}},
    {"node_id": "node_2", "node_type": "TASK_DISTRIBUTE", "name": "任务", "config": {...}},
    {"node_id": "node_3", "node_type": "END", "name": "结束", "config": {}}
  ],
  "edges": [
    {"source": "node_1", "target": "node_2"},
    {"source": "node_2", "target": "node_3"}
  ]
}
```

---

#### WfNodeDef (节点定义)
**表名**: `wf_node_def`  
**文件位置**: `entity/WfNodeDef.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| nodeCode | String | 节点编码 |
| nodeName | String | 节点名称 |
| isActive | Integer | 是否启用 |

---

#### WfStudentActivityState (学生活动状态)
**表名**: `wf_student_activity_state`  
**文件位置**: `entity/WfStudentActivityState.java`

| 字段 | 类型 | 说明 | 关联 |
|------|------|------|------|
| id | Long | 主键，自增 | - |
| participationId | Long | 参与记录 ID | BizTrainingParticipation.id |
| currentNodeId | String | 当前节点 ID | 模板节点的 node_id |
| updatedAt | LocalDateTime | 更新时间 | - |

---

#### BizTrainingParticipation (实训参与记录)
**表名**: `biz_training_participation`  
**文件位置**: `entity/BizTrainingParticipation.java`

| 字段 | 类型 | 说明 | 关联 |
|------|------|------|------|
| id | Long | 主键，自增 | - |
| taskId | Long | 实训任务 ID | BizTrainingTask.id |
| studentId | Long | 学生 ID | SysUser.id |
| status | Integer | 参与状态 | 0=未开始, 1=进行中, 2=已完成 |
| createdAt | LocalDateTime | 创建时间 | - |
| updatedAt | LocalDateTime | 更新时间 | - |

---

### 实体关联关系图
```
BizTrainingTask (实训任务)
  ├── templateId ──→ WfTrainingTemplate (实训模板)
  │                     ├── rawCanvasJson (原始编排数据)
  │                     └── standardPayloadJson (标准执行数据)
  └── teacherId ──→ SysUser (教师)

BizTrainingParticipation (参与记录)
  ├── taskId ──→ BizTrainingTask
  ├── studentId ──→ SysUser (学生)
  └── id ──→ WfStudentActivityState.participationId
                               └── currentNodeId (指向模板节点)
```

---

## 🤖 AI 引擎交互切面

### 当前状态
**未在代码中发现真实的 AI 引擎调用**

### 模拟实现 (processTemplateMockAi)
**文件位置**: `service/impl/TrainingTemplateServiceImpl.java:13-27`

```java
@Async("templateAiExecutor")  // 使用异步线程池
@Override
public void processTemplateMockAi(Long templateId, Object canvasData) {
    try {
        Thread.sleep(4000);  // 模拟 AI 处理耗时 4 秒
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    WfTrainingTemplate template = getById(templateId);
    if (template != null) {
        template.setStandardPayloadJson(canvasData);  // 直接复制原始数据
        template.setAiStatus(2);  // 标记为完成
        updateById(template);
    }
}
```

### 关键发现
| 项目 | 状态 | 说明 |
|------|------|------|
| HTTP 客户端 | 未实现 | 无 RestTemplate/WebClient/Feign 调用 AI 引擎 |
| 同步/异步 | 异步 | 使用 `@Async` 注解异步执行 |
| 超时机制 | 无 | 无超时配置，Thread.sleep 仅作模拟 |
| 重试机制 | 无 | 无失败重试逻辑 |
| 熔断降级 | 无 | 无 Resilience4j/Hystrix 集成 |
| 线程池配置 | 有 | 线程池 bean 名称 `templateAiExecutor` |

### 调用链路 (模拟)
```
TeacherTemplateController.create()
  → 保存模板 (aiStatus=1)
  → 调用 trainingTemplateService.processTemplateMockAi() [异步]
     → Thread.sleep(4000)
     → 更新模板 (standardPayloadJson=canvasData, aiStatus=2)
```

**关键代码位置**:
- 控制器: `controller/TeacherTemplateController.java:58`
- 服务实现: `service/impl/TrainingTemplateServiceImpl.java:13-27`

---

## 🔐 事务与权限边界

### 1. 事务边界 (@Transactional)
**关键发现**: 核心业务流程中**未发现 @Transactional 注解**

#### 高风险操作 - 无事务保护
| 操作 | 方法位置 | 涉及表 | 风险 |
|------|----------|--------|------|
| 开始实训 | StudentTrainingController.start() | biz_training_participation, wf_student_activity_state | 两表更新可能不一致 |
| 进入下一节点 | StudentTrainingController.next() | biz_training_participation, wf_student_activity_state | 两表更新可能不一致 |
| 创建模板 | TeacherTemplateController.create() | wf_training_template | 单表操作风险较低 |

**关键代码示例** (StudentTrainingController.next()):
```java
// L129-182，无 @Transactional 注解
public ApiResult<Map<String, Object>> next(...) {
    // 1. 查询 participation
    BizTrainingParticipation pt = participationService.getById(participationId);
    // 2. 查询或创建 state
    WfStudentActivityState state = activityStateService.getOne(...);
    if (state == null) {
        state = new WfStudentActivityState();
        state.setParticipationId(participationId);
        state.setCurrentNodeId(nextNodeId);
        activityStateService.save(state);  // 第一次写操作
        // 3. 可能更新 participation
        if (pt.getStatus() == null || pt.getStatus() == 0) {
            pt.setStatus(1);
            participationService.updateById(pt);  // 第二次写操作
        }
    } else {
        // 更新 state
        state.setCurrentNodeId(nextNodeId);
        activityStateService.updateById(state);  // 写操作
    }
    // 无回滚机制！
}
```

**关键代码位置**: `controller/StudentTrainingController.java:99-182`

---

### 2. Spring Security 权限控制
**文件位置**: `config/SecurityConfig.java`

#### 过滤器链配置
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors().configurationSource(corsConfigurationSource())
        .and()
        .csrf().disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 无状态
        .and()
        .authorizeRequests()
        .antMatchers("/api/auth/login", "/api/auth/register", "/api/auth/captcha").permitAll()
        .antMatchers("/api/public/**").permitAll()
        .antMatchers("/api/**").authenticated()  // 其他 API 需要认证
        .anyRequest().permitAll()
        .and()
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http;
}
```

#### CORS 配置
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOriginPattern("*");  // ⚠️ 允许所有来源，生产环境有风险
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

#### 权限路径映射
| 路径模式 | 权限要求 | 说明 |
|----------|----------|------|
| `/api/auth/login` | permitAll | 登录接口 |
| `/api/auth/register` | permitAll | 注册接口 |
| `/api/public/**` | permitAll | 公开数据接口 |
| `/api/**` | authenticated | 需要 JWT 认证 |
| 其他 | permitAll | 静态资源等 |

#### JWT 认证过滤器
**文件位置**: `config/JwtAuthenticationFilter.java`
- 从请求头 `Authorization: Bearer <token>` 中提取 Token
- 使用 `JwtUtil` 验证和解析 Token
- 将用户信息存入 SecurityContext

#### 角色鉴权
**未在代码中发现基于角色的细粒度鉴权 (如 @PreAuthorize)**
- 仅区分"已认证"和"未认证"
- 无 student/teacher/admin 角色的接口级权限校验
- 依赖业务层手动校验用户身份

**关键代码位置**:
- Security 配置: `config/SecurityConfig.java:31-48`
- JWT 工具: `util/JwtUtil.java`

---

## 📋 其他核心实体补充

### SysUser (系统用户)
**用途**: 统一用户表，包含学生、教师、管理员

### BizStudent / BizTeacher (学生/教师详情)
**用途**: 扩展用户详情信息

### BizCourse / BizStudentCourse (课程/选课)
**用途**: 课程管理

### BizQuestion / BizQuestionBank (题目/题库)
**用途**: 题目库管理

### BizMindmapRecord / BizMindmapEvalDetail (思维导图/评测)
**用途**: 思维导图实训相关

### BizTrainingChatLog / BizTrainingExamRecord (聊天日志/考试记录)
**用途**: 实训过程数据记录

### SysOperationLog / SysFeedback / SysTicket (日志/反馈/工单)
**用途**: 系统运维功能

---

## 联系方式

如有问题请联系开发团队。