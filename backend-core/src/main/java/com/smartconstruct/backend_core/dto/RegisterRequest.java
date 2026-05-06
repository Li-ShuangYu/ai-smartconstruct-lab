package com.smartconstruct.backend_core.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotNull(message = "角色类型不能为空")
    private Integer roleType;

    private String studentNo;
    private String employeeNo;
    private String realName;
    private Long deptId;
    private Long majorId;
    private Long classId;
}
