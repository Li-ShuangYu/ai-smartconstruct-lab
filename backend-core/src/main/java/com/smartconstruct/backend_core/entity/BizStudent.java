package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_student")
public class BizStudent {
    private Long userId;
    private String studentNo;
    private String realName;
    private Long deptId;
    private Long majorId;
    private Long classId;
}
