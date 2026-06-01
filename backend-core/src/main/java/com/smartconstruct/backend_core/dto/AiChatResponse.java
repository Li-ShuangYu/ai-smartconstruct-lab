package com.smartconstruct.backend_core.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * AI 对话响应 DTO
 *
 * 包含 AI 回复内容、会话ID 和消息ID。
 */
@Data
public class AiChatResponse {

    /** 会话ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sessionId;

    /** AI 回复消息ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long messageId;

    /** AI 回复内容 */
    private String content;

    /** 消耗 Token 数 */
    private Integer tokensUsed;
}
