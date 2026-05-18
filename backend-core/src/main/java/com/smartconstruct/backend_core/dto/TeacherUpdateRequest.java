package com.smartconstruct.backend_core.dto;

public class TeacherUpdateRequest {
    private String realName;
    private String deptId;
    private String password;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    public Long getDeptIdAsLong() { return deptId == null ? null : Long.parseLong(deptId); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}