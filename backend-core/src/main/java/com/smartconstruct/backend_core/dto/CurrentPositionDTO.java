package com.smartconstruct.backend_core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生当前位置 DTO
 *
 * 表示学生在实训流程中的当前位置信息，用于断线重连或页面初始化时定位。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentPositionDTO {
    /** 当前节点实例ID */
    @JsonProperty("current_node_instance_id")
    private String currentNodeInstanceId;
    /** 当前所在阶段ID */
    @JsonProperty("phase_id")
    private String phaseId;
    /** 当前节点类型 */
    @JsonProperty("node_type")
    private String nodeType;
    /** 当前节点名称 */
    @JsonProperty("node_name")
    private String nodeName;
}
