package com.smartconstruct.backend_core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class MajorRequest {
    @NotNull(message = "请选择院系")
    private String deptId;

    @NotBlank(message = "专业名称不能为空")
    private String majorName;

    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    public Long getDeptIdAsLong() { return deptId == null ? null : Long.parseLong(deptId); }
    public String getMajorName() { return majorName; }
    public void setMajorName(String majorName) { this.majorName = majorName; }
}