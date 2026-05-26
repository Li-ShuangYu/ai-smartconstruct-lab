package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI引擎处理请求DTO
 *
 * 用于Java后端通过HTTP POST将模板数据提交给AI引擎进行异步处理
 *
 * @author SmartConstruct
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiProcessRequest {
    /** 模板ID */
    private Long templateId;
    /** 编排ID */
    private String orchestrationId;
    /** 原始编排JSON (包含nodes和edges的画布数据) */
    private Object canvasJson;
    /** 回调地址 (AI引擎处理完成后通知结果的URL) */
    private String callbackUrl;
}
