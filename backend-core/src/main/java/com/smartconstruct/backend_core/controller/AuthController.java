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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(SysUserService sysUserService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ApiResult<Void> register(@Validated @RequestBody RegisterRequest req) {
        try {
            sysUserService.registerUser(req);
            return ApiResult.ok();
        } catch (RuntimeException e) {
            return ApiResult.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResult<AuthResponse> login(@Validated @RequestBody LoginRequest req) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
        if (user == null) {
            return ApiResult.error("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            return ApiResult.error("账号已被封禁");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            return ApiResult.error("用户名或密码错误");
        }
        if (req.getRoleType() != null && !req.getRoleType().equals(user.getRoleType())) {
            return ApiResult.error("角色不匹配，请确认登录身份");
        }
        String token = jwtUtil.generateToken(user.getUsername());
        AuthResponse resp = new AuthResponse(token, user.getId(), user.getUsername(), user.getRoleType());
        return ApiResult.ok(resp);
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        return ApiResult.ok();
    }

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
