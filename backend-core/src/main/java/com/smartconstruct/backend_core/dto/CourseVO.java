package com.smartconstruct.backend_core.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;

public class CourseVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String courseName;
    private String description;
    private Integer status;
    private Integer needEnrollCode;
    private String enrollCode;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long teacherId;
    private String teacherName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}