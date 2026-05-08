package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("biz_training_task")
public class BizTrainingTask {
    
    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
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
    
    /** 是否为课堂实训（1=是，0=否） */
    private Integer isInClass;
    
    /** 是否分组（1=是，0=否） */
    private Integer hasGroup;
    
    /** 任务状态 */
    private Integer status;
    
    /** 发布范围（如：全体、指定班级等） */
    private Integer dispatchScope;
    
    /** 发布目标ID（如班级ID） */
    private Long dispatchTargetId;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
