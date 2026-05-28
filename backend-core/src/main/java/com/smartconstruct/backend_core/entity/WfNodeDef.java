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

@Data
@TableName(value = "wf_node_def", autoResultMap = true)
public class WfNodeDef {
    /** 主键ID（雪花算法，序列化为字符串防止前端精度丢失） */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 节点类型（如 START/RESOURCE_READ） */
    private String nodeType;
    /** 节点名称 */
    private String nodeName;
    /** 节点全局开关 */
    private Integer isActive;
    /** 节点AI规格声明 — 需要先执行 ALTER TABLE wf_node_def ADD COLUMN ai_spec_json JSON 后移除 exist=false */
    @TableField(value = "ai_spec_json", typeHandler = JacksonTypeHandler.class)
    private Object aiSpecJson;
    /** 默认配置模板(6维度) — 需要先执行 ALTER TABLE wf_node_def ADD COLUMN default_config_json JSON 后移除 exist=false */
    @TableField(value = "default_config_json", typeHandler = JacksonTypeHandler.class)
    private Object defaultConfigJson;
    /** 节点定义注册时间 */
    private LocalDateTime createdAt;
    /** 节点定义最后更新时间 */
    private LocalDateTime updatedAt;
}