package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wf_global_activity_state")
public class WfGlobalActivityState {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String currentNodeId;
    private LocalDateTime updatedAt;
}
