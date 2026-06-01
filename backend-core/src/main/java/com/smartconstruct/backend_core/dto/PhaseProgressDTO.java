package com.smartconstruct.backend_core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("phase_id")
    private String phaseId;
    /** 阶段名称 */
    @JsonProperty("phase_name")
    private String phaseName;
    /** 阶段排序号 */
    @JsonProperty("sort_num")
    private Integer sortNum;
    /** 是否已解锁 */
    @JsonProperty("is_unlocked")
    private Boolean isUnlocked;
    /** 是否已完成 */
    @JsonProperty("is_complete")
    private Boolean isComplete;
    /** 阶段内节点总数 */
    @JsonProperty("total_nodes")
    private Integer totalNodes;
    /** 已完成节点数 */
    @JsonProperty("completed_nodes")
    private Integer completedNodes;
    /** 必修节点总数 */
    @JsonProperty("required_nodes")
    private Integer requiredNodes;
    /** 已完成的必修节点数 */
    @JsonProperty("completed_required_nodes")
    private Integer completedRequiredNodes;
    /** 阶段内节点进度列表 */
    private List<NodeProgressDTO> nodes;
}
