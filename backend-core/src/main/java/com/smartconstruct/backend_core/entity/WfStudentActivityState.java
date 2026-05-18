package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生活动状态实体
 *
 * 记录学生在实训流程中的当前节点位置，用于跟踪学生的实训进度。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("wf_student_activity_state")
public class WfStudentActivityState {

    /** 主键ID（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联的参与记录ID */
    private Long participationId;

    /** 当前节点实例ID（对应wf_node_instance.id） */
    @TableField("current_node_instance_id")
    private String currentNodeId;

    /** 记录创建时间 */
    private LocalDateTime createdAt;

    /** 状态最后跃迁时间 */
    private LocalDateTime updatedAt;
}
