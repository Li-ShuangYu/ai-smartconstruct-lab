package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI引擎处理完成回调请求DTO
 *
 * AI引擎处理完成后，通过POST /api/internal/ai-callback将结果回调给Java后端。
 * 包含处理状态、标准化编排JSON、生成的资产（题目、知识点、评价指标）等信息。
 *
 * @author SmartConstruct
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiCallbackRequest {
    /** 模板ID */
    private Long templateId;
    /** 任务ID (UUID格式) */
    private String jobId;
    /** 处理状态: 2=成功, 3=失败 */
    private Integer status;
    /** 标准化后的编排JSON (status=2时有值) */
    private Object standardPayloadJson;
    /** 错误原因 (status=3时有值) */
    private String errorReason;
    /** AI生成的资产 (题目、知识点、评价指标) */
    private GeneratedAssets generatedAssets;
}
