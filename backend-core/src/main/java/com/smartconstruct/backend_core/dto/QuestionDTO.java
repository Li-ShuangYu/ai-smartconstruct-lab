package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI生成的题目DTO
 *
 * 对应 biz_question 表结构，用于AI回调时传递生成的题目数据。
 * 题目类型: 1-单选 2-多选 3-填空 4-判断 5-简答
 *
 * @author SmartConstruct
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    /** 题目类型: 1-单选 2-多选 3-填空 4-判断 5-简答 */
    private Integer questionType;
    /** 题干与选项JSON字符串 */
    private String content;
    /** 标准答案 */
    private String standardAnswer;
    /** 默认分值 */
    private Double defaultScore;
    /** 关联知识点（可选） */
    private String knowledgePoint;
}
