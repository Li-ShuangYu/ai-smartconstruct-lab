package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wf_node_def")
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
    /** 节点定义注册时间 */
    private LocalDateTime createdAt;
    /** 节点定义最后更新时间 */
    private LocalDateTime updatedAt;
}