package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_teacher")
public class BizTeacher {
    private Long userId;
    private String employeeNo;
    private String realName;
    private Long deptId;
}
