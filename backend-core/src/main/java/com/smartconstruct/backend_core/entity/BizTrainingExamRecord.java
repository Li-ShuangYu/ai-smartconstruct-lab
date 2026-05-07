package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_training_exam_record")
public class BizTrainingExamRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long participationId;
    private String nodeId;
    private String answersJson;
    private Integer score;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
