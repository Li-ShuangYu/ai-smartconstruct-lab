package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 阶段解锁状态 DTO
 *
 * 表示学生在某个实训任务中各阶段的解锁和完成状态。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhaseUnlockStatus {
    /** 阶段ID */
    private String phaseId;
    /** 阶段名称 */
    private String phaseName;
    /** 阶段排序号 */
    private Integer sortNum;
    /** 是否已解锁 */
    private Boolean isUnlocked;
    /** 是否已完成 */
    private Boolean isComplete;
}
