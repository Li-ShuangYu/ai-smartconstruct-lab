# SmartConstruct 后端 AI 开发约定（强制遵守）

技术栈：Spring Boot 2.7 + MyBatis-Plus 3.5 + MySQL 8.0 + Java 8

## 0. 雪花 ID 精度（最常见的坑）

虽然升级到了Java 21但严格遵守Java 8语法，所有实体主键（@TableId）必须同时加ToStringSerializer，外键字段也加：

```java
@TableId(type = IdType.ASSIGN_ID)
@JsonSerialize(using = ToStringSerializer.class)
private Long id;
```

哪些字段要加：所有 **实体主键**（id / userId） + **外键字段**（deptId, majorId, classId, teacherId 等）
不加的话前端 JS 接收 19 位数字会精度丢失，PUT/DELETE 发回错误 ID。
**每次新建或修改 DTO/Entity 都要检查这个，漏掉必出 bug。**

```java
@JsonSerialize(using = ToStringSerializer.class)
private Long deptId;
```

## 1. 数据库与实体

- **主键**：全部 `IdType.ASSIGN_ID`（雪花算法），禁止 `IdType.AUTO`
- **时间**：统一 `LocalDateTime`，`createdAt`/`updatedAt` 自动填充
- **命名**：数据库下划线，Java 驼峰，表前缀 `biz_`/`sys_`/`wf_`
- **Lombok**：实体加 `@Data`+`@NoArgsConstructor`+`@AllArgsConstructor`+`@Builder`
- **逻辑删除**：核心表加 `is_deleted`，全局开启
- 实体字段必须和 `db_mysql.txt` DDL 严格对齐

## 2. 接口与传输

- 所有返回用 `ApiResult`，分页用 `PageResult`
- 严禁直接暴露 Entity，入参 `XXXRequest`，出参 `XXXVO`
- Controller 入参加 JSR-303 校验 + `@Validated`
- URL 全小写连字符，HTTP Method 区分 CRUD，禁止 URL 带动词

## 3. 业务与持久层

- 增删改加 `@Transactional(rollbackFor = Exception.class)`
- 查询优先 `LambdaQueryWrapper`，禁止硬编码列名
- 多表 Join 写在 Mapper XML 中，禁止代码拼接

## 4. 异常与日志

- 业务错误抛 `BusinessException`（没有就建一个），全局统一处理
- 用 `@Slf4j`，关键流程/异常带 `userId`/`taskId` 日志
- 禁止 Service 层 catch 吞没异常

## 5. 其他要求

- 三端时区统一 `Asia/Shanghai`
- 代码结构对齐现有目录：controller/service/mapper/entity/dto
- 复用项目已有组件：ApiResult/OperationLog/JwtUtil，不重复造轮子

## 6. 前端接口对齐（防止前后端不匹配）

- 后端发了 `@JsonSerialize(using = ToStringSerializer.class)` 后，前端对应 TypeScript 类型 `id`/`userId`/`deptId` 等必须是 `string` 不是 `number`
- Service 层 URL 路径参数也传 `string`
- 新增/修改 Request Body 中关联 ID（如 `deptId`、`teacherId`）也传 `string`，后端 DTO 用 `String` 接收 + `getXxxAsLong()` 转换
- win 平台：mvnw.cmd 不是 mvnw

## 7. 文件编辑安全

- **不要用 Write 改写已有文件**，优先用 Edit（只传 diff）
- Edit 失败时（old_string 不匹配），用 Read 重新读取后重试
- 多次 Edit 同一个文件导致损坏时，立刻用 Write 写入完整内容
- pom.xml, application.properties 等配置文件不要大规模改动

## 8. 测试接口

- 用 REST Client (`.http` 文件) 或 curl 命令行测试，不用浏览器
- 后端启动后先调 `POST /api/public/init` 初始化种子数据
- 登录测试：`POST /api/auth/login` → 拿 token → 带 `Authorization: Bearer xxx` 调业务接口