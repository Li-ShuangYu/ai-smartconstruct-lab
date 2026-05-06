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

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService, UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final IStudentService studentService;
    private final ITeacherService teacherService;

    public SysUserServiceImpl(PasswordEncoder passwordEncoder, IStudentService studentService, ITeacherService teacherService) {
        this.passwordEncoder = passwordEncoder;
        this.studentService = studentService;
        this.teacherService = teacherService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return user;
    }

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

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoleType(request.getRoleType());
        user.setStatus(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        save(user);

        if (request.getRoleType() == 3) {
            BizStudent student = new BizStudent();
            student.setUserId(user.getId());
            student.setStudentNo(request.getStudentNo());
            student.setRealName(request.getRealName());
            student.setDeptId(request.getDeptId());
            student.setMajorId(request.getMajorId());
            student.setClassId(request.getClassId());
            studentService.save(student);
        } else if (request.getRoleType() == 2) {
            BizTeacher teacher = new BizTeacher();
            teacher.setUserId(user.getId());
            teacher.setEmployeeNo(request.getEmployeeNo());
            teacher.setRealName(request.getRealName());
            teacher.setDeptId(request.getDeptId());
            teacherService.save(teacher);
        }
    }
}
