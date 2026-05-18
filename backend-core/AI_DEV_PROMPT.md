# SmartConstruct后端AI开发约定（强制遵守）
技术栈：Spring Boot 2.7 + MyBatis-Plus 3.5 + MySQL 8.0

## 1. 数据库与实体
- 主键：BIGINT+雪花算法 @TableId(type=IdType.ASSIGN_ID)
- 时间：统一LocalDateTime，created_at/updated_at自动填充
- 命名：数据库下划线，Java驼峰，表前缀biz_/sys_/wf_
- Lombok：实体加@Data+@NoArgsConstructor+@AllArgsConstructor+@Builder
- 逻辑删除：核心表加is_deleted，全局开启
- 敏感字段：passwordHash等加@JsonIgnore，禁止出现在VO

## 2. 接口与传输
- 所有返回必须用ApiResult，分页用PageResult
- 严禁直接暴露Entity，入参XXXRequest，出参XXXVO
- Controller入参必须加JSR-303校验+@Validated
- URL全小写连字符，用HTTP Method区分CRUD，禁止URL带动词

## 3. 业务与持久层
- 增删改加@Transactional(rollbackFor=Exception.class)
- 查询优先LambdaQueryWrapper，禁止硬编码列名
- 多表Join必须写在Mapper XML中，禁止代码拼接

## 4. 异常与日志
- 业务错误抛BusinessException，全局统一处理
- 用@Slf4j，关键流程/异常必须带userId/taskId日志
- 禁止Service层catch吞没异常

## 5. 其他要求
- 三端时区统一Asia/Shanghai
- 代码结构对齐现有目录：controller/service/mapper/entity/dto
- 复用项目已有组件：ApiResult/OperationLog/JwtUtil，不重复造轮子