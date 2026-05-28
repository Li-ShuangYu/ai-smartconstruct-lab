package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教师节点操作记录实体
 *
 * 记录教师在实训监控过程中的所有操作，包括催办、强制完成、手动解锁等。
 * 对应表 wf_teacher_node_operation。
 *
 * 操作类型：1-开始节点 2-结束节点 3-催办学生 4-强制完成 5-强制开启实训
 *
 * @author SmartConstruct
 */
@Data
@TableName("wf_teacher_node_operation")
public class WfTeacherNodeOperation {

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 关联实训任务ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    /** 关联节点实例ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nodeInstanceId;

    /** 操作教师ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long teacherId;

    /** 操作类型：1-开始节点 2-结束节点 3-催办学生 4-强制完成 5-强制开启实训 */
    private Integer operationType;

    /** 目标学生ID(空表示全局操作) */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetStudentId;

    /** 操作内容(如催办消息) */
    private String operationContent;

    /** 操作指令下发时间 */
    private LocalDateTime createdAt;
}
