package com.smartconstruct.backend_core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 节点进度 DTO
 *
 * 表示学生在某个节点实例上的进度信息，包含节点基本信息和学生进度状态。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeProgressDTO {
    /** 节点实例ID */
    @JsonProperty("node_instance_id")
    private String nodeInstanceId;
    /** 节点定义ID */
    @JsonProperty("node_id")
    private String nodeId;
    /** 节点类型 */
    @JsonProperty("node_type")
    private String nodeType;
    /** 节点名称 */
    @JsonProperty("node_name")
    private String nodeName;
    /** 所属阶段ID */
    @JsonProperty("phase_id")
    private String phaseId;
    /** 排序序号 */
    @JsonProperty("sort_num")
    private Integer sortNum;
    /** 是否为必修节点 */
    @JsonProperty("is_required")
    private Boolean isRequired;
    /** 节点进度状态：0-未开始 1-进行中 2-已完成 3-已跳过 */
    private Integer status;
    /** 进入节点时间 */
    @JsonProperty("entered_at")
    private LocalDateTime enteredAt;
    /** 离开节点时间 */
    @JsonProperty("exited_at")
    private LocalDateTime exitedAt;
    /** 总停留时长(秒) */
    @JsonProperty("stay_duration_seconds")
    private Integer stayDurationSeconds;
}
