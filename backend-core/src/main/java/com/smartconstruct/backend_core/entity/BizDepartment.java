package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 院系名称 */
    private String deptName;
}
