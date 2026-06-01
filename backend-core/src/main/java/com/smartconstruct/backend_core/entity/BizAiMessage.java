package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 对话消息明细表
 *
 * 记录会话中每一条消息，包含角色（user/assistant/system）、内容和 Token 消耗。
 *
 * @author SmartConstruct
 */
@Data
@TableName("biz_ai_message")
public class BizAiMessage {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 关联会话ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sessionId;

    /** 消息角色：user / assistant / system */
    private String role;

    /** 消息内容 */
    private String content;

    /** 消耗 Token 数 */
    private Integer tokensUsed;

    /** 消息生成时间 */
    private LocalDateTime createdAt;
}
