package com.smartconstruct.backend_core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 节点AI处理状态回调载荷
 *
 * AI引擎在处理每个节点时，通过 POST /api/internal/ai/callback/node-status
 * 独立更新该节点的AI处理状态。
 *
 * @author SmartConstruct
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeAiStatusPayload {

    /** 模板ID */
    @JsonProperty("template_id")
    private Long templateId;

    /** 画布节点ID */
    @JsonProperty("node_id")
    private String nodeId;

    /** 节点类型 */
    @JsonProperty("node_type")
    private String nodeType;

    /** 所属阶段ID */
    @JsonProperty("phase_id")
    private String phaseId;

    /** AI处理状态：0-待处理 1-处理中 2-成功 3-失败 */
    @JsonProperty("ai_status")
    private Integer aiStatus;

    /** 失败原因 (ai_status=3时有值) */
    @JsonProperty("error_reason")
    private String errorReason;

    /** AI处理结果JSON (ai_status=2时有值) */
    @JsonProperty("result_json")
    private Object resultJson;
}
