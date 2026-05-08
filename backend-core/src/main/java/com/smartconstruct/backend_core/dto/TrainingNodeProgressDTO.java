package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实训节点进度数据传输对象
 *
 * 用于前端展示学生的实训节点进度信息
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingNodeProgressDTO {
    /** 参与记录ID */
    private Long participationId;
    /** 当前节点ID */
    private String currentNodeId;
    /** 节点类型 */
    private String nodeType;
    /** 节点名称 */
    private String nodeName;
    /** 节点配置 */
    private Object config;
    /** 所有节点列表 */
    private Object allNodes;
    /** 所有边列表 */
    private Object allEdges;
}
