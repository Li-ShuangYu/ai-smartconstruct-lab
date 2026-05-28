package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生单题作答明细实体
 *
 * 记录学生在作业/考试中每道题的作答内容和AI/教师评分。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("biz_student_answer_detail")
public class BizStudentAnswerDetail {

    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 关联学生卷ID */
    private Long studentPaperId;

    /** 关联实训题目快照ID */
    private Long paperQuestionId;

    /** 学生提交的具体答案/代码 */
    private String studentAnswer;

    /** AI针对本题给出的评分 */
    private BigDecimal aiScore;

    /** AI给出的单题批改意见与逻辑解析 */
    private String aiComment;

    /** 教师针对本题的核查得分 */
    private BigDecimal teacherScore;

    /** 教师单题批注与人工纠偏说明 */
    private String teacherComment;

    /** 作答明细生成时间 */
    private LocalDateTime createdAt;

    /** 作答明细最后更新时间 */
    private LocalDateTime updatedAt;
}
