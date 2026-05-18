package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.*;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.service.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentController {

    private final IStudentService studentService;
    private final SysUserService sysUserService;
    private final IDepartmentService departmentService;
    private final IMajorService majorService;
    private final IAdminClassService adminClassService;
    private final PasswordEncoder passwordEncoder;

    public AdminStudentController(IStudentService studentService, SysUserService sysUserService,
                                  IDepartmentService departmentService, IMajorService majorService,
                                  IAdminClassService adminClassService, PasswordEncoder passwordEncoder) {
        this.studentService = studentService;
        this.sysUserService = sysUserService;
        this.departmentService = departmentService;
        this.majorService = majorService;
        this.adminClassService = adminClassService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ApiResult<PageResult<StudentVO>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BizStudent> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.like(BizStudent::getRealName, keyword);
        }
        qw.orderByDesc(BizStudent::getUserId);
        Page<BizStudent> p = studentService.page(new Page<>(page, pageSize), qw);

        if (p.getRecords().isEmpty()) {
            return ApiResult.ok(new PageResult<>(List.of(), p.getTotal(), p.getCurrent(), p.getSize()));
        }

        Set<Long> userIds = p.getRecords().stream().map(BizStudent::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = sysUserService.list(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds))
                .stream().collect(Collectors.toMap(SysUser::getId, u -> u));

        Set<Long> deptIds = p.getRecords().stream().map(BizStudent::getDeptId).collect(Collectors.toSet());
        Map<Long, BizDepartment> deptMap = departmentService.list(new LambdaQueryWrapper<BizDepartment>().in(BizDepartment::getId, deptIds))
                .stream().collect(Collectors.toMap(BizDepartment::getId, d -> d));

        Set<Long> majorIds = p.getRecords().stream().map(BizStudent::getMajorId).collect(Collectors.toSet());
        Map<Long, BizMajor> majorMap = majorService.list(new LambdaQueryWrapper<BizMajor>().in(BizMajor::getId, majorIds))
                .stream().collect(Collectors.toMap(BizMajor::getId, m -> m));

        Set<Long> classIds = p.getRecords().stream().map(BizStudent::getClassId).collect(Collectors.toSet());
        Map<Long, BizAdminClass> classMap = adminClassService.list(new LambdaQueryWrapper<BizAdminClass>().in(BizAdminClass::getId, classIds))
                .stream().collect(Collectors.toMap(BizAdminClass::getId, c -> c));

        List<StudentVO> voList = new ArrayList<>();
        for (BizStudent bs : p.getRecords()) {
            SysUser u = userMap.get(bs.getUserId());
            BizDepartment d = deptMap.get(bs.getDeptId());
            BizMajor m = majorMap.get(bs.getMajorId());
            BizAdminClass c = classMap.get(bs.getClassId());
            StudentVO vo = new StudentVO();
            vo.setUserId(bs.getUserId());
            vo.setUsername(u != null ? u.getUsername() : "");
            vo.setRealName(bs.getRealName());
            vo.setDeptId(bs.getDeptId());
            vo.setDeptName(d != null ? d.getDeptName() : "");
            vo.setMajorId(bs.getMajorId());
            vo.setMajorName(m != null ? m.getMajorName() : "");
            vo.setClassId(bs.getClassId());
            vo.setClassName(c != null ? c.getClassName() : "");
            vo.setCreatedAt(u != null ? u.getCreatedAt() : null);
            voList.add(vo);
        }
        return ApiResult.ok(new PageResult<>(voList, p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @OperationLog(action = "新增学生")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> create(@Valid @RequestBody StudentCreateRequest req) {
        SysUser exist = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
        if (exist != null) {
            return ApiResult.error("账号已被注册");
        }

        LocalDateTime now = LocalDateTime.now();
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRoleType(3);
        user.setStatus(0);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        sysUserService.save(user);

        BizStudent student = new BizStudent();
        student.setUserId(user.getId());
        student.setRealName(req.getRealName());
        student.setDeptId(req.getDeptIdAsLong());
        student.setMajorId(req.getMajorIdAsLong());
        student.setClassId(req.getClassIdAsLong());
        student.setCreatedAt(now);
        student.setUpdatedAt(now);
        studentService.save(student);

        return ApiResult.ok();
    }

    @OperationLog(action = "编辑学生")
    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> update(@PathVariable String id, @Valid @RequestBody StudentUpdateRequest req) {
        Long userId = Long.parseLong(id);
        BizStudent bs = studentService.getById(userId);
        if (bs == null) {
            return ApiResult.error("学生不存在");
        }
        boolean changed = false;
        if (req.getRealName() != null && !req.getRealName().isBlank()) {
            bs.setRealName(req.getRealName());
            changed = true;
        }
        if (req.getDeptId() != null) {
            bs.setDeptId(req.getDeptIdAsLong());
            changed = true;
        }
        if (req.getMajorId() != null) {
            bs.setMajorId(req.getMajorIdAsLong());
            changed = true;
        }
        if (req.getClassId() != null) {
            bs.setClassId(req.getClassIdAsLong());
            changed = true;
        }
        if (changed) {
            bs.setUpdatedAt(LocalDateTime.now());
            studentService.updateById(bs);
        }

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            SysUser u = sysUserService.getById(userId);
            if (u != null) {
                u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
                u.setUpdatedAt(LocalDateTime.now());
                sysUserService.updateById(u);
            }
        }

        return ApiResult.ok();
    }

    @OperationLog(action = "删除学生")
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> delete(@PathVariable String id) {
        Long userId = Long.parseLong(id);
        studentService.removeById(userId);
        sysUserService.removeById(userId);
        return ApiResult.ok();
    }
}