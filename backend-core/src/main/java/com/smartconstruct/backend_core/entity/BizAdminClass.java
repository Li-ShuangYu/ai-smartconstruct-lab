package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_admin_class")
public class BizAdminClass {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long majorId;
    private String className;
}
