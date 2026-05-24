package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实训模板实体
 * 
 * 存储教师编排的实训流程模板数据，包含原始画布数据和AI处理后的标准执行载荷。
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName(value = "wf_training_template", autoResultMap = true)
public class WfTrainingTemplate {
    
    /** 主键ID，自增 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    /** 模板名称 */
    private String templateName;

    /** 模板描述 */
    @JsonProperty("templateDesc")
    private String templateDescription;
    
    /** 原始画布JSON - 前端Vue Flow编排的原始数据 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object rawCanvasJson;
    
    /** 标准执行载荷JSON - AI处理后生成的标准格式数据 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object standardPayloadJson;
    
    /** 创建者ID（关联 sys_user.id） */
    private Long creatorId;

    /** AI处理状态：1=处理中，2=完成 */
    private Integer aiStatus;
    
    /** 错误原因（AI处理失败时记录） */
    private String errorReason;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
