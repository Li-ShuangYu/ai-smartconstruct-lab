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
 * 节点配置Schema实体
 *
 * 存储每种节点类型的JSON Schema定义，用于运行时校验节点配置合法性。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName(value = "wf_node_config_schema", autoResultMap = true)
public class WfNodeConfigSchema {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 节点类型（唯一） */
    private String nodeType;

    /** JSON Schema定义 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object schemaJson;

    /** 版本号 */
    private Integer schemaVersion;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
