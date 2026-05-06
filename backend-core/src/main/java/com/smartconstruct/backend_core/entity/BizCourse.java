package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_course")
public class BizCourse {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseName;
    private String courseCode;
    private String description;
    private Integer status;
    private Integer needEnrollCode;
    private String enrollCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
