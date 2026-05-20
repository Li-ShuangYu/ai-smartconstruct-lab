package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.dto.TeacherCreateRequest;
import com.smartconstruct.backend_core.dto.TeacherUpdateRequest;
import com.smartconstruct.backend_core.dto.TeacherVO;
import com.smartconstruct.backend_core.entity.BizTeacher;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.service.ITeacherService;
import com.smartconstruct.backend_core.service.SysUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/teachers")
public class AdminTeacherController {

    private final ITeacherService teacherService;
    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;

    public AdminTeacherController(ITeacherService teacherService, SysUserService sysUserService, PasswordEncoder passwordEncoder) {
        this.teacherService = teacherService;
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ApiResult<PageResult<TeacherVO>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BizTeacher> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.like(BizTeacher::getRealName, keyword);
        }
        qw.orderByDesc(BizTeacher::getUserId);
        Page<BizTeacher> p = teacherService.page(new Page<>(page, pageSize), qw);

        if (p.getRecords().isEmpty()) {
            return ApiResult.ok(new PageResult<>(List.of(), p.getTotal(), p.getCurrent(), p.getSize()));
        }

        Set<Long> userIds = p.getRecords().stream().map(BizTeacher::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = sysUserService.list(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds))
                .stream().collect(Collectors.toMap(SysUser::getId, u -> u));

        List<TeacherVO> voList = new ArrayList<>();
        for (BizTeacher bt : p.getRecords()) {
            SysUser u = userMap.get(bt.getUserId());
            voList.add(new TeacherVO(
                    bt.getUserId(),
                    u != null ? u.getUsername() : "",
                    bt.getRealName(),
                    bt.getDeptId(),
                    u != null ? u.getCreatedAt() : null
            ));
        }
        return ApiResult.ok(new PageResult<>(voList, p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @OperationLog(action = "新增教师")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> create(@Valid @RequestBody TeacherCreateRequest req) {
        SysUser exist = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
        if (exist != null) {
            return ApiResult.error("账号已被注册");
        }

        LocalDateTime now = LocalDateTime.now();
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRoleType(2);
        user.setStatus(0);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        sysUserService.save(user);

        BizTeacher teacher = new BizTeacher();
        teacher.setUserId(user.getId());
        teacher.setRealName(req.getRealName());
        teacher.setDeptId(req.getDeptIdAsLong());
        teacher.setCreatedAt(now);
        teacher.setUpdatedAt(now);
        teacherService.save(teacher);

        return ApiResult.ok();
    }

    @OperationLog(action = "编辑教师")
    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> update(@PathVariable Long id, @Valid @RequestBody TeacherUpdateRequest req) {
        BizTeacher bt = teacherService.getById(id);
        if (bt == null) {
            return ApiResult.error("教师不存在");
        }
        boolean changed = false;
        if (req.getRealName() != null && !req.getRealName().isBlank()) {
            bt.setRealName(req.getRealName());
            changed = true;
        }
        if (req.getDeptId() != null) {
            bt.setDeptId(req.getDeptIdAsLong());
            changed = true;
        }
        if (changed) {
            bt.setUpdatedAt(LocalDateTime.now());
            teacherService.updateById(bt);
        }

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            SysUser u = sysUserService.getById(id);
            if (u != null) {
                u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
                u.setUpdatedAt(LocalDateTime.now());
                sysUserService.updateById(u);
            }
        }

        return ApiResult.ok();
    }

    @OperationLog(action = "删除教师")
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> delete(@PathVariable Long id) {
        teacherService.removeById(id);
        sysUserService.removeById(id);
        return ApiResult.ok();
    }
}