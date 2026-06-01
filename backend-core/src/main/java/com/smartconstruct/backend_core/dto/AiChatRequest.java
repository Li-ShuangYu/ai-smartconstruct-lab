package com.smartconstruct.backend_core.dto;

import lombok.Data;

/**
 * AI 对话请求 DTO
 *
 * 用于学生发送消息给 AI 助教。
 * sessionId 为空时自动创建新会话。
 */
@Data
public class AiChatRequest {

    /**
     * 会话ID（可空，为空时创建新会话）
     */
    private Long sessionId;

    /**
     * 用户消息内容（必填）
     */
    private String content;

    /**
     * 会话类型（新建会话时使用）：
     * 1-全局悬浮 2-AI对练 3-理论实训 4-编码实训 5-答疑
     * 默认为 1（全局悬浮）
     */
    private Integer sessionType;

    /**
     * 关联实训任务ID（可空）
     */
    private Long taskId;

    /**
     * 关联节点实例ID（可空）
     */
    private Long nodeInstanceId;
}
