package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingNodeProgressDTO {
    private Long participationId;
    private String currentNodeId;
    private String nodeType;
    private String nodeName;
    private Object config;
    private Object allNodes;
    private Object allEdges;
}
