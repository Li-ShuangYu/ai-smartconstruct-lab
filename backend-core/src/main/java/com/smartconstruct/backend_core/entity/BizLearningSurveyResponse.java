package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学情调查回答表实体
 *
 * 记录学生对学情调查问卷的回答内容。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName(value = "biz_learning_survey_response", autoResultMap = true)
public class BizLearningSurveyResponse {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 关联参与表ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long participationId;

    /** 关联节点实例ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nodeInstanceId;

    /** 学生ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentId;

    /** 问卷回答JSON数组 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object responsesJson;

    /** 提交时间 */
    private LocalDateTime submittedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
