package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationDetailVO {
    private Long participationId;
    private Long taskId;
    private String taskName;
    private Integer status;
    private Object templateJson;
    private String currentNodeId;
}
