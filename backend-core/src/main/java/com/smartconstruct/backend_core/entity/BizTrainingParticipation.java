package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实训参与记录实体
 *
 * 对应数据库表 biz_training_participation
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("biz_training_participation")
public class BizTrainingParticipation {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联的实训任务ID */
    private Long taskId;
    /** 参与学生ID */
    private Long studentId;
    /** 参与状态：0=未开始，1=进行中，2=已完成 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
