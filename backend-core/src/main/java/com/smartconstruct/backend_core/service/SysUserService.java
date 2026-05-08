package com.smartconstruct.backend_core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartconstruct.backend_core.dto.RegisterRequest;
import com.smartconstruct.backend_core.entity.SysUser;

/**
 * 系统用户服务接口
 *
 * 定义用户相关的业务操作
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
public interface SysUserService extends IService<SysUser> {
    /**
     * 用户注册
     *
     * @param request 注册请求参数（包含用户名、密码、角色类型等）
     */
    void registerUser(RegisterRequest request);
}
