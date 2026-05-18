package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wf_global_activity_state")
public class WfGlobalActivityState {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long taskId;
    /** 当前节点实例ID */
    @TableField("current_node_instance_id")
    private String currentNodeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
