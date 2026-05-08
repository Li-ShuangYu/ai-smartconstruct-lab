package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.AuthResponse;
import com.smartconstruct.backend_core.dto.LoginRequest;
import com.smartconstruct.backend_core.dto.RegisterRequest;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.service.SysUserService;
import com.smartconstruct.backend_core.util.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * 负责处理用户认证相关的API请求，包括：
 * - 用户注册
 * - 用户登录（JWT Token生成）
 * - 用户登出
 * - 获取当前登录用户信息
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** 用户服务 - 处理用户注册和查询 */
    private final SysUserService sysUserService;
    
    /** 密码编码器 - 用于密码验证 */
    private final PasswordEncoder passwordEncoder;
    
    /** JWT工具类 - 用于生成和验证Token */
    private final JwtUtil jwtUtil;

    /**
     * 构造函数 - 注入依赖服务
     * 
     * @param sysUserService 用户服务
     * @param passwordEncoder 密码编码器
     * @param jwtUtil JWT工具类
     */
    public AuthController(SysUserService sysUserService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户注册
     * 
     * 创建新用户账号，密码会进行BCrypt加密存储。
     * 
     * @param req 注册请求，包含用户名、密码、角色类型等信息
     * @return 操作结果
     */
    @PostMapping("/register")
    public ApiResult<Void> register(@Validated @RequestBody RegisterRequest req) {
        try {
            sysUserService.registerUser(req);
            return ApiResult.ok();
        } catch (RuntimeException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     * 
     * 验证用户名和密码，验证通过后生成JWT Token。
     * 
     * 验证流程：
     * 1. 根据用户名查询用户
     * 2. 检查账号状态（是否被封禁）
     * 3. 验证密码（BCrypt匹配）
     * 4. 验证角色类型（可选）
     * 5. 生成JWT Token并返回
     * 
     * @param req 登录请求，包含用户名、密码、角色类型
     * @return 认证响应，包含Token和用户信息
     */
    @PostMapping("/login")
    public ApiResult<AuthResponse> login(@Validated @RequestBody LoginRequest req) {
        // 1. 根据用户名查询用户
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
        if (user == null) {
            return ApiResult.error("用户名或密码错误");
        }
        
        // 2. 检查账号是否被封禁
        if (user.getStatus() != null && user.getStatus() == 1) {
            return ApiResult.error("账号已被封禁");
        }
        
        // 3. 验证密码
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            return ApiResult.error("用户名或密码错误");
        }
        
        // 4. 验证角色类型（如果指定了角色）
        if (req.getRoleType() != null && !req.getRoleType().equals(user.getRoleType())) {
            return ApiResult.error("角色不匹配，请确认登录身份");
        }
        
        // 5. 生成JWT Token
        String token = jwtUtil.generateToken(user.getUsername());
        AuthResponse resp = new AuthResponse(token, user.getId(), user.getUsername(), user.getRoleType());
        return ApiResult.ok(resp);
    }

    /**
     * 用户登出
     * 
     * 由于使用JWT无状态认证，服务端无需特殊处理，由客户端丢弃Token即可。
     * 
     * @return 操作结果
     */
    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        return ApiResult.ok();
    }

    /**
     * 获取当前登录用户信息
     * 
     * 从Spring Security上下文获取当前认证用户的信息。
     * 
     * @return 用户信息（ID、用户名、角色类型）
     */
    @GetMapping("/userinfo")
    public ApiResult<AuthResponse> userinfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SysUser)) {
            return ApiResult.error(401, "未登录");
        }
        SysUser user = (SysUser) auth.getPrincipal();
        AuthResponse resp = new AuthResponse(null, user.getId(), user.getUsername(), user.getRoleType());
        return ApiResult.ok(resp);
    }
}
