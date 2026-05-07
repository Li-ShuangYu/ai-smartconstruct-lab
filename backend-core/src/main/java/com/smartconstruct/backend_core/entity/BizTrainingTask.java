package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_training_task")
public class BizTrainingTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long templateId;
    private Long teacherId;
    private String taskName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer isInClass;
    private Integer hasGroup;
    private Integer status;
    private Integer dispatchScope;
    private Long dispatchTargetId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
