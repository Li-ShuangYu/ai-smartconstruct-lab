package com.smartconstruct.backend_core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ClassRequest {
    @NotNull(message = "请选择院系")
    private String deptId;

    @NotNull(message = "请选择专业")
    private String majorId;

    @NotBlank(message = "班级名称不能为空")
    private String className;

    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    public Long getDeptIdAsLong() { return deptId == null ? null : Long.parseLong(deptId); }
    public String getMajorId() { return majorId; }
    public void setMajorId(String majorId) { this.majorId = majorId; }
    public Long getMajorIdAsLong() { return majorId == null ? null : Long.parseLong(majorId); }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}