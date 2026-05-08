package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈实体
 *
 * 对应数据库表 sys_feedback
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("sys_feedback")
public class SysFeedback {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 反馈用户ID */
    private Long userId;
    /** 反馈内容 */
    private String content;
    /** 反馈时间 */
    private LocalDateTime createdAt;
}
