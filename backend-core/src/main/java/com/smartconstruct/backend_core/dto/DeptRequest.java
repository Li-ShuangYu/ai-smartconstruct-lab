package com.smartconstruct.backend_core.dto;

import javax.validation.constraints.NotBlank;

public class DeptRequest {
    @NotBlank(message = "院系名称不能为空")
    private String deptName;

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
}