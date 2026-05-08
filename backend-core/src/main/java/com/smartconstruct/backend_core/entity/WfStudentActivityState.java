package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    
    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 关联的参与记录ID */
    private Long participationId;
    
    /** 当前节点ID（对应模板中的node_id） */
    private String currentNodeId;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
