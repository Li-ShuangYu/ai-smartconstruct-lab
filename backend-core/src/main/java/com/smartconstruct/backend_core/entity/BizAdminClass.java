package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属专业ID */
    private Long majorId;
    /** 班级名称 */
    private String className;
}
