package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_mindmap_eval_detail")
public class BizMindmapEvalDetail {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long participationId;
    private Long knowledgePointId;
    private Integer difficultyLevel;
    private String studentDoubt;
    private LocalDateTime createdAt;
}
