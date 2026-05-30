package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 阶段进度 DTO
 *
 * 表示学生在某个实训阶段的进度信息，包含阶段基本信息、解锁状态和节点进度列表。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhaseProgressDTO {
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
    /** 阶段内节点总数 */
    private Integer totalNodes;
    /** 已完成节点数 */
    private Integer completedNodes;
    /** 必修节点总数 */
    private Integer requiredNodes;
    /** 已完成的必修节点数 */
    private Integer completedRequiredNodes;
    /** 阶段内节点进度列表 */
    private List<NodeProgressDTO> nodes;
}
