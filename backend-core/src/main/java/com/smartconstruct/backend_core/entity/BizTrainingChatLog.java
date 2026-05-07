package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_training_chat_log")
public class BizTrainingChatLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long participationId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}
