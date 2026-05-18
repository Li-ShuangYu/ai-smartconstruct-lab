package com.smartconstruct.backend_core.dto;

import javax.validation.constraints.NotBlank;

public class StudentCreateRequest {
    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotBlank(message = "请选择院系")
    private String deptId;

    @NotBlank(message = "请选择专业")
    private String majorId;

    @NotBlank(message = "请选择班级")
    private String classId;

    @NotBlank(message = "登录密码不能为空")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
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