package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库实体
 *
 * 对应数据库表 biz_question_bank
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("biz_question_bank")
public class BizQuestionBank {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 创建教师ID */
    private Long teacherId;
    /** 题库名称 */
    private String bankName;
    /** 是否公开：0=私有，1=公开 */
    private Integer isPublic;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
