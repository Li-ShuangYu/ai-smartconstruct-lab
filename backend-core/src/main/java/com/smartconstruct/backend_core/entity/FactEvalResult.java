package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评价事实结果实体
 *
 * 存储实训中各维度的评价结果，支持个人/小组/实训/班级多种评价对象。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("fact_eval_result")
public class FactEvalResult {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 关联实训任务 */
    private Long taskId;

    /** 评分对象类型：1-人 2-组 3-实训 4-班 */
    private Integer targetType;

    /** 对应的学生/组/实训/班级ID */
    private Long targetId;

    /** 关联字典表指标 */
    private String indicatorId;

    /** 原始分 */
    private BigDecimal rawScore;

    /** 计算图表分 */
    private BigDecimal calcScore;

    /** 大模型给出的评语 */
    private String aiComment;

    /** 教师给出的评语 */
    private String teacherComment;

    /** 关联互评分配表ID */
    private Long peerReviewId;

    /** 评价结果落库时间 */
    private LocalDateTime createdAt;
}
