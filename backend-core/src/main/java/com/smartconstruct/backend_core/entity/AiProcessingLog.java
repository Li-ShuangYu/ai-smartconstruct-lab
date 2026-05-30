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
 * AI处理日志实体
 *
 * 记录AI处理全链路的关键事件，支持端到端追踪：
 * - 触发AI处理 → AI引擎接收/拒绝 → 节点处理 → 回调完成/超时
 *
 * 每个事件包含状态、描述、详情JSON，可通过 templateId 聚合查询。
 *
 * @author SmartConstruct
 */
@Data
@TableName(value = "ai_processing_log", autoResultMap = true)
public class AiProcessingLog {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 模板ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /** 节点ID（节点级事件时非空） */
    private String nodeId;

    /** 节点类型 */
    private String nodeType;

    /** AI引擎job_id */
    private String jobId;

    /** 事件类型: TRIGGER_START / JOB_ACCEPTED / JOB_REJECTED / ENRICHMENT_SUMMARY /
     *  NODE_PROCESSING / NODE_COMPLETED / NODE_FAILED / PIPELINE_COMPLETED /
     *  PIPELINE_FAILED / TIMEOUT_DETECTED / CONNECTION_FAILED */
    private String eventType;

    /** 事件中文标签 */
    private String eventLabel;

    /** 状态: processing / success / failed / warning */
    private String eventStatus;

    /** 事件描述文本 */
    private String message;

    /** 事件详情JSON */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object detailJson;

    /** 事件时间 */
    private LocalDateTime createdAt;
}