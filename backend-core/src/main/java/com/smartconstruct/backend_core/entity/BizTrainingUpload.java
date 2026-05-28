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
 * 实训产出上传物实体
 *
 * 存储学生在实训中提交的文档和代码产出物，支持AI审查和教师评审。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("biz_training_upload")
public class BizTrainingUpload {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 关联参与表ID */
    private Long participationId;

    /** 关联节点实例ID */
    private Long nodeInstanceId;

    /** 物料类型：1-文档 2-代码 */
    private Integer uploadType;

    /** 关联统一资源管理表ID */
    private Long resourceId;

    /** 状态：0-待AI审 1-AI打回 2-待教师审 3-教师打回 4-终审通过 */
    private Integer status;

    /** AI初评分 */
    private BigDecimal aiScore;

    /** AI审查意见与修改指引 */
    private String aiComment;

    /** 教师核定分 */
    private BigDecimal teacherScore;

    /** 教师批注评语 */
    private String teacherComment;

    /** 产出物归档时间 */
    private LocalDateTime createdAt;

    /** 产出物状态最后更新时间 */
    private LocalDateTime updatedAt;
}
