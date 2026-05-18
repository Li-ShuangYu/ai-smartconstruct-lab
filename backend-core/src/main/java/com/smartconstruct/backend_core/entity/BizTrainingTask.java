package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实训任务实体
 *
 * 表示教师发布的实训任务，关联实训模板和参与学生。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName(value = "biz_training_task", autoResultMap = true)
public class BizTrainingTask {

    /** 主键ID（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联的实训模板ID */
    private Long templateId;

    /** 创建任务的教师ID */
    private Long teacherId;

    /** 任务名称 */
    private String taskName;

    /** 任务开始时间 */
    private LocalDateTime startTime;

    /** 任务结束时间 */
    private LocalDateTime endTime;

    /** 是否为课堂实训（0=课后异步，1=课中同步） */
    private Integer isInClass;

    /** 是否分组（1=是，0=否） */
    private Integer hasGroup;

    /** 分组配置JSON（自动/手动分组、每组人数等） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object groupConfigJson;

    /** 任务状态：0=未开始，1=进行中，2=已结束 */
    private Integer status;

    /** 发布范围：1=行政班，2=课程名单 */
    private Integer dispatchScope;

    /** 发布目标ID（如班级ID） */
    private Long dispatchTargetId;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}