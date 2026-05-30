package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_student")
public class BizStudent {
    @TableId(type = IdType.INPUT)
    private Long userId;
    private String studentNo;
    private String realName;
    private Long deptId;
    private Long majorId;
    private Long classId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}