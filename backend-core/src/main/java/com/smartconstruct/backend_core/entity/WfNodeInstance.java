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
 * 实训节点实例实体
 *
 * 实训任务运行时每个节点的具体实例，包含节点配置和运行状态。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName(value = "wf_node_instance", autoResultMap = true)
public class WfNodeInstance {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 关联实训任务ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    /** 所属阶段ID */
    private String phaseId;

    /** 关联全局节点定义ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nodeDefId;

    /** 节点类型(冗余，快速查询用) */
    private String nodeType;

    /** 节点自定义名称 */
    private String nodeName;

    /** 流程排序序号 */
    private Integer sortNum;

    /** 节点配置属性(完全对应系统设计的Config定义) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object configJson;

    /** 全局状态：0-未开始 1-进行中 2-已结束 */
    private Integer status;

    /** 节点实例开始时间 */
    private LocalDateTime startedAt;

    /** 节点实例结束时间 */
    private LocalDateTime endedAt;

    /** 记录创建时间 */
    private LocalDateTime createdAt;

    /** 记录最后更新时间 */
    private LocalDateTime updatedAt;
}
