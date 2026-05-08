package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 专业实体
 *
 * 对应数据库表 biz_major
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("biz_major")
public class BizMajor {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属院系ID */
    private Long deptId;
    /** 专业名称 */
    private String majorName;
}
