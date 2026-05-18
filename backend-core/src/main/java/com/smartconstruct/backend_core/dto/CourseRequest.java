package com.smartconstruct.backend_core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CourseRequest {
    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    private String description;

    @NotNull(message = "请选择课程状态")
    private Integer status;

    @NotNull(message = "请设置是否需要选课码")
    private Integer needEnrollCode;

    private String enrollCode;

    private String teacherId;

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getNeedEnrollCode() { return needEnrollCode; }
    public void setNeedEnrollCode(Integer needEnrollCode) { this.needEnrollCode = needEnrollCode; }
    public String getEnrollCode() { return enrollCode; }
    public void setEnrollCode(String enrollCode) { this.enrollCode = enrollCode; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public Long getTeacherIdAsLong() { return teacherId == null ? null : Long.parseLong(teacherId); }
}