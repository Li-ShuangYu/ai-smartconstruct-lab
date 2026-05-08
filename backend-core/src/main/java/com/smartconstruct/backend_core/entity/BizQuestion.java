package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 题目实体
 *
 * 对应数据库表 biz_question
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("biz_question")
public class BizQuestion {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属题库ID */
    private Long bankId;
    /** 题目类型：1=单选题，2=多选题，3=判断题，4=简答题 */
    private Integer questionType;
    /** 题目内容 */
    private String content;
    /** 标准答案 */
    private String standardAnswer;
    /** 默认分值 */
    private BigDecimal defaultScore;
    /** 排序序号 */
    private Integer sortNum;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
