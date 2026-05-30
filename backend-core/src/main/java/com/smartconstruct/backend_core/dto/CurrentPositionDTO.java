package com.smartconstruct.backend_core.dto;

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
    private String currentNodeInstanceId;
    /** 当前所在阶段ID */
    private String phaseId;
    /** 当前节点类型 */
    private String nodeType;
    /** 当前节点名称 */
    private String nodeName;
}
