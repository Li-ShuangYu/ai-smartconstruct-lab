package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生节点进度明细实体
 *
 * 记录每个学生在每个节点实例上的进度状态，包括进入时间、离开时间、停留时长等。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName(value = "wf_student_node_progress", autoResultMap = true)
public class WfStudentNodeProgress {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 关联学生实训参与表ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long participationId;

    /** 关联节点实例ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nodeInstanceId;

    /** 个人状态：0-未开始 1-进行中 2-已完成 3-已跳过 */
    private Integer status;

    /** 进入节点时间 */
    private LocalDateTime enteredAt;

    /** 离开节点时间 */
    private LocalDateTime exitedAt;

    /** 总停留时长(秒) */
    private Integer stayDurationSeconds;

    /** 是否被教师强制完成 */
    private Integer isForcedComplete;

    /** 节点专属采集数据(如阅读完成度、播放进度等) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object nodeSpecificDataJson;

    /** 进度明细生成时间 */
    private LocalDateTime createdAt;

    /** 进度明细最后更新时间 */
    private LocalDateTime updatedAt;
}
