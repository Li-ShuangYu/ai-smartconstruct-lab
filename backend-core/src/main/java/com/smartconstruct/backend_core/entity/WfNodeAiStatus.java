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
 * 节点AI处理状态实体
 *
 * 记录每个节点的AI处理状态，支持节点级粒度追踪和选择性重试。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName(value = "wf_node_ai_status", autoResultMap = true)
public class WfNodeAiStatus {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 关联模板ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /** 画布节点ID */
    private String nodeId;

    /** 节点类型 */
    private String nodeType;

    /** 所属阶段ID */
    private String phaseId;

    /** AI处理状态：0-待处理 1-处理中 2-成功 3-失败 */
    private Integer aiStatus;

    /** 失败原因 */
    private String errorReason;

    /** 已重试次数 */
    private Integer retryCount;

    /** AI处理结果JSON */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object resultJson;

    /** 最后处理时间 */
    private LocalDateTime lastProcessedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
