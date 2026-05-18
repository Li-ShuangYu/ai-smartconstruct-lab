package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 院系实体
 *
 * 对应数据库表 biz_department
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("biz_department")
public class BizDepartment {
    /** 主键ID（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 院系名称 */
    private String deptName;
    /** 记录创建时间 */
    private LocalDateTime createdAt;
}
