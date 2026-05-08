package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程实体
 *
 * 对应数据库表 biz_course
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("biz_course")
public class BizCourse {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 课程名称 */
    private String courseName;
    /** 课程编码 */
    private String courseCode;
    /** 课程描述 */
    private String description;
    /** 课程状态：0=禁用，1=启用 */
    private Integer status;
    /** 是否需要选课码：0=不需要，1=需要 */
    private Integer needEnrollCode;
    /** 选课码 */
    private String enrollCode;
    /** 创建者ID */
    private Long creatorId;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
