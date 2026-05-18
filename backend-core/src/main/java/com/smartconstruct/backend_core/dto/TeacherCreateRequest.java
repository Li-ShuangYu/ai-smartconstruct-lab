package com.smartconstruct.backend_core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class TeacherCreateRequest {
    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotNull(message = "院系不能为空")
    private String deptId;

    @NotBlank(message = "登录密码不能为空")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    public Long getDeptIdAsLong() { return deptId == null ? null : Long.parseLong(deptId); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}