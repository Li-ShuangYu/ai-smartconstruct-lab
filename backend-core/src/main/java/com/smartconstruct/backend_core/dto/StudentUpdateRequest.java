package com.smartconstruct.backend_core.dto;

public class StudentUpdateRequest {
    private String realName;
    private String deptId;
    private String majorId;
    private String classId;
    private String password;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    public Long getDeptIdAsLong() { return deptId == null ? null : Long.parseLong(deptId); }
    public String getMajorId() { return majorId; }
    public void setMajorId(String majorId) { this.majorId = majorId; }
    public Long getMajorIdAsLong() { return majorId == null ? null : Long.parseLong(majorId); }
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    public Long getClassIdAsLong() { return classId == null ? null : Long.parseLong(classId); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}