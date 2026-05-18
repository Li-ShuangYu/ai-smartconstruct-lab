package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName(value = "biz_mindmap_record", autoResultMap = true)
public class BizMindmapRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long participationId;
    @TableField("node_instance_id")
    private String nodeId;
    @TableField(typeHandler = JacksonTypeHandler.class, value = "map_topology_json")
    private Object mindmapJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}