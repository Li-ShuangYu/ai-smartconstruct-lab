package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wf_training_template")
public class WfTrainingTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateName;
    private String rawCanvasJson;
    private String standardPayloadJson;
    private Integer aiStatus;
    private String errorReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
