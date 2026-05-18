package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
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
@TableName(value = "biz_training_participation", autoResultMap = true)
public class BizTrainingParticipation {
    /** 主键ID（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 关联的实训任务ID */
    private Long taskId;
    /** 参与学生ID */
    private Long studentId;
    /** 参与状态：0=未开始，1=进行中，2=已完成 */
    private Integer status;
    /** 本次实训最终总分 */
    private BigDecimal totalScore;
    /** 满意度明细（含内容、难度、AI辅助评分及建议） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object satisfactionDetailJson;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
