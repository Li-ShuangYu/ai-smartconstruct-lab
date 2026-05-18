package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.dto.RegisterRequest;
import com.smartconstruct.backend_core.entity.BizStudent;
import com.smartconstruct.backend_core.entity.BizTeacher;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.mapper.SysUserMapper;
import com.smartconstruct.backend_core.service.IStudentService;
import com.smartconstruct.backend_core.service.ITeacherService;
import com.smartconstruct.backend_core.service.SysUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 系统用户服务实现类
 * 
 * 实现用户管理核心功能：
 * - 用户注册（学生/教师）
 * - Spring Security用户认证加载
 * - 密码加密存储
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService, UserDetailsService {

    /** 密码编码器 - BCrypt加密 */
    private final PasswordEncoder passwordEncoder;
    
    /** 学生服务 - 创建学生记录 */
    private final IStudentService studentService;
    
    /** 教师服务 - 创建教师记录 */
    private final ITeacherService teacherService;

    /**
     * 构造函数 - 注入依赖服务
     * 
     * @param passwordEncoder 密码编码器
     * @param studentService 学生服务
     * @param teacherService 教师服务
     */
    public SysUserServiceImpl(PasswordEncoder passwordEncoder, IStudentService studentService, ITeacherService teacherService) {
        this.passwordEncoder = passwordEncoder;
        this.studentService = studentService;
        this.teacherService = teacherService;
    }

    /**
     * 加载用户详情（Spring Security认证）
     * 
     * 根据用户名查询用户信息，用于Spring Security认证流程。
     * 
     * @param username 用户名
     * @return 用户详情（继承UserDetails）
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return user;
    }

    /**
     * 用户注册
     * 
     * 注册流程：
     * 1. 校验角色类型（禁止注册管理员）
     * 2. 检查用户名是否已存在
     * 3. 创建用户记录（密码BCrypt加密）
     * 4. 根据角色创建对应的扩展记录（学生/教师）
     * 
     * @param request 注册请求，包含用户名、密码、角色类型等信息
     * @throws RuntimeException 用户名已存在或角色非法时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerUser(RegisterRequest request) {
        if (request.getRoleType() == 1) {
            throw new RuntimeException("非法操作：禁止注册管理员账号");
        }

        SysUser exist = getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (exist != null) {
            throw new RuntimeException("用户名已被注册");
        }

        LocalDateTime now = LocalDateTime.now();
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoleType(request.getRoleType());
        user.setStatus(0);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        save(user);

        if (request.getRoleType() == 3) {
            BizStudent student = new BizStudent();
            student.setUserId(user.getId());
            student.setRealName(request.getRealName());
            student.setDeptId(request.getDeptId());
            student.setMajorId(request.getMajorId());
            student.setClassId(request.getClassId());
            student.setCreatedAt(now);
            student.setUpdatedAt(now);
            studentService.save(student);
        } else if (request.getRoleType() == 2) {
            BizTeacher teacher = new BizTeacher();
            teacher.setUserId(user.getId());
            teacher.setRealName(request.getRealName());
            teacher.setDeptId(request.getDeptId());
            teacher.setCreatedAt(now);
            teacher.setUpdatedAt(now);
            teacherService.save(teacher);
        }
    }
}
