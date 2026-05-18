package com.smartconstruct.backend_core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * 系统用户实体类
 * 
 * 实现Spring Security的UserDetails接口，支持用户认证和授权。
 * 
 * 角色类型：
 * - 1: 管理员（ROLE_ADMIN）
 * - 2: 教师（ROLE_TEACHER）
 * - 3: 学生（ROLE_STUDENT）
 * 
 * 状态：
 * - 0: 正常
 * - 1: 封禁
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@Data
@TableName("sys_user")
public class SysUser implements UserDetails {

    /** 用户ID（主键，雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** 用户名（登录账号） */
    private String username;
    
    /** 密码哈希值（BCrypt加密） */
    @ToString.Exclude
    private String passwordHash;
    
    /** 手机号 */
    private String phone;
    
    /** 头像URL */
    private String avatarUrl;
    
    /** 个人简介 */
    private String bio;
    
    /** 角色类型：1=管理员，2=教师，3=学生 */
    private Integer roleType;
    
    /** 账号状态：0=正常，1=封禁 */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    private LocalDateTime updatedAt;

    /**
     * 获取用户权限列表
     * 
     * 根据角色类型返回对应的Spring Security权限。
     * 
     * @return 权限集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role;
        switch (roleType == null ? 3 : roleType) {
            case 1: role = "ROLE_ADMIN"; break;
            case 2: role = "ROLE_TEACHER"; break;
            default: role = "ROLE_STUDENT"; break;
        }
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    /**
     * 获取密码
     * 
     * 返回密码哈希值用于Spring Security认证。
     * 
     * @return 密码哈希值
     */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /**
     * 账号是否未过期
     * 
     * 当前实现返回true，账号永不过期。
     * 
     * @return true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账号是否未锁定
     * 
     * 根据status字段判断：status=0或null表示未锁定。
     * 
     * @return true表示未锁定，false表示已锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return status == null || status == 0;
    }

    /**
     * 凭证是否未过期
     * 
     * 当前实现返回true，凭证永不过期。
     * 
     * @return true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账号是否启用
     * 
     * 根据status字段判断：status=0或null表示启用。
     * 
     * @return true表示启用，false表示禁用
     */
    @Override
    public boolean isEnabled() {
        return status == null || status == 0;
    }
}
