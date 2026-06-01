package com.smartconstruct.backend_core.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 会话视图对象
 *
 * 包含会话基本信息和消息列表，用于前端渲染历史对话。
 */
@Data
public class AiSessionVO {

    /** 会话ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 会话标题 */
    private String title;

    /** 会话类型 */
    private Integer sessionType;

    /** AI生成的会话摘要 */
    private String summary;

    /** 会话建立时间 */
    private LocalDateTime createdAt;

    /** 消息列表（按时间升序） */
    private List<AiMessageVO> messages;

    /** 消息视图对象 */
    @Data
    public static class AiMessageVO {

        /** 消息ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;

        /** 角色：user / assistant */
        private String role;

        /** 消息内容 */
        private String content;

        /** 消息生成时间 */
        private LocalDateTime createdAt;
    }
}
