package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "wf_training_template", autoResultMap = true)
public class WfTrainingTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateName;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object rawCanvasJson;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object standardPayloadJson;
    private Integer aiStatus;
    private String errorReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
