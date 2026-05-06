package com.smartconstruct.backend_core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartconstruct.backend_core.dto.RegisterRequest;
import com.smartconstruct.backend_core.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    void registerUser(RegisterRequest request);
}
