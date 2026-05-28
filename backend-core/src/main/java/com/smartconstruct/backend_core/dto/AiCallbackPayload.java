package com.smartconstruct.backend_core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI引擎处理完成回调载荷（新版，节点级AI管线）
 *
 * AI引擎处理完成后，通过 POST /api/internal/ai/callback 将结果回调给后端。
 * 与旧版 AiCallbackRequest 的区别：
 * - 使用 snake_case 字段名（与AI引擎Python端保持一致）
 * - 支持节点级状态追踪
 *
 * @author SmartConstruct
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiCallbackPayload {

    /** 模板ID */
    @JsonProperty("template_id")
    private Long templateId;

    /** 任务ID (UUID格式) */
    @JsonProperty("job_id")
    private String jobId;

    /** 处理状态: 2=成功, 3=失败 */
    private Integer status;

    /** 标准化后的编排JSON (status=2时有值) */
    @JsonProperty("standard_payload_json")
    private Object standardPayloadJson;

    /** 错误原因 (status=3时有值) */
    @JsonProperty("error_reason")
    private String errorReason;

    /** AI生成的资产 (题目、知识点等) */
    @JsonProperty("generated_assets")
    private GeneratedAssets generatedAssets;
}
