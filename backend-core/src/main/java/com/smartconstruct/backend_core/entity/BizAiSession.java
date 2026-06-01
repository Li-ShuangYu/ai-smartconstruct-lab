package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI 对话会话主表
 *
 * 记录学生与 AI 助教的每一次对话会话，支持全局悬浮、AI对练、理论实训等多种场景。
 *
 * @author SmartConstruct
 */
@Data
@TableName("biz_ai_session")
public class BizAiSession {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 学生ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentId;

    /** 会话标题 */
    private String title;

    /** 关联实训任务ID（可空） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    /** 关联节点实例ID（可空） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nodeInstanceId;

    /**
     * 会话类型：
     * 1-全局悬浮 2-AI对练 3-理论实训 4-编码实训 5-答疑
     */
    private Integer sessionType;

    /** AI生成的会话摘要 */
    private String summary;

    /** 软删除标记：0-正常 1-已删除 */
    private Integer isDeleted;

    /** 会话建立时间 */
    private LocalDateTime createdAt;

    /** 会话状态最后更新时间 */
    private LocalDateTime updatedAt;
}
