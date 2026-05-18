package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班级实体
 *
 * 对应数据库表 biz_admin_class
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("biz_admin_class")
public class BizAdminClass {
    /** 主键ID（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 所属专业ID */
    private Long majorId;
    /** 班级名称（如 24级计科1班） */
    private String className;
    /** 入学年份 */
    private Integer enrollmentYear;
    /** 记录创建时间 */
    private LocalDateTime createdAt;
}
