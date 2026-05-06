package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_question")
public class BizQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bankId;
    private Integer questionType;
    private String content;
    private String standardAnswer;
    private BigDecimal defaultScore;
    private Integer sortNum;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
