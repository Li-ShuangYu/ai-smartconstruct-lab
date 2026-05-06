package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_major")
public class BizMajor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deptId;
    private String majorName;
}
