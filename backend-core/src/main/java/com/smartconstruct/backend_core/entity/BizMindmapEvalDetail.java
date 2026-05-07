package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_mindmap_eval_detail")
public class BizMindmapEvalDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private String nodeKey;
    private String difficulty;
    private LocalDateTime createdAt;
}
