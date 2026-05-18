package com.smartconstruct.backend_core.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

public class TeacherVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String username;
    private String realName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    private LocalDateTime createdAt;

    public TeacherVO() {}

    public TeacherVO(Long userId, String username, String realName, Long deptId, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.deptId = deptId;
        this.createdAt = createdAt;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}