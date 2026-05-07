package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.service.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentDashboardController {

    private final ITrainingTaskService trainingTaskService;
    private final ITrainingParticipationService participationService;
    private final IStudentService studentService;
    private final IAdminClassService adminClassService;
    private final IDepartmentService departmentService;
    private final IMajorService majorService;
    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;

    public StudentDashboardController(ITrainingTaskService trainingTaskService,
                                       ITrainingParticipationService participationService,
                                       IStudentService studentService,
                                       IAdminClassService adminClassService,
                                       IDepartmentService departmentService,
                                       IMajorService majorService,
                                       SysUserService sysUserService,
                                       PasswordEncoder passwordEncoder) {
        this.trainingTaskService = trainingTaskService;
        this.participationService = participationService;
        this.studentService = studentService;
        this.adminClassService = adminClassService;
        this.departmentService = departmentService;
        this.majorService = majorService;
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
    }

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    @GetMapping("/training-tasks")
    public ApiResult<PageResult<Map<String, Object>>> trainingTasks(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        Long studentId = getCurrentUserId();

        LambdaQueryWrapper<BizTrainingParticipation> pqw = new LambdaQueryWrapper<>();
        pqw.eq(BizTrainingParticipation::getStudentId, studentId);
        if (status != null) {
            pqw.eq(BizTrainingParticipation::getStatus, status);
        }
        List<BizTrainingParticipation> participations = participationService.list(pqw);

        if (participations.isEmpty()) {
            return ApiResult.ok(new PageResult<>(List.of(), 0, page, pageSize));
        }

        List<Long> taskIds = participations.stream().map(BizTrainingParticipation::getTaskId).distinct().collect(java.util.stream.Collectors.toList());
        LambdaQueryWrapper<BizTrainingTask> tqw = new LambdaQueryWrapper<>();
        tqw.in(BizTrainingTask::getId, taskIds).orderByDesc(BizTrainingTask::getCreatedAt);
        Page<BizTrainingTask> p = trainingTaskService.page(new Page<>(page, pageSize), tqw);

        Map<Long, BizTrainingParticipation> partMap = new HashMap<>();
        for (BizTrainingParticipation pt : participations) {
            partMap.put(pt.getTaskId(), pt);
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (BizTrainingTask t : p.getRecords()) {
            BizTrainingParticipation pt = partMap.get(t.getId());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", t.getId());
            map.put("taskName", t.getTaskName());
            map.put("status", pt != null ? pt.getStatus() : 0);
            map.put("startTime", t.getStartTime());
            map.put("endTime", t.getEndTime());
            map.put("createdAt", t.getCreatedAt());
            records.add(map);
        }
        return ApiResult.ok(new PageResult<>(records, p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @GetMapping("/my-class/training-tasks")
    public ApiResult<List<Map<String, Object>>> classTrainingTasks() {
        Long userId = getCurrentUserId();
        BizStudent self = studentService.getOne(new LambdaQueryWrapper<BizStudent>().eq(BizStudent::getUserId, userId));
        if (self == null || self.getClassId() == null) return ApiResult.ok(List.of());

        List<BizTrainingTask> tasks = trainingTaskService.list(
                new LambdaQueryWrapper<BizTrainingTask>().eq(BizTrainingTask::getDispatchScope, 1)
                        .eq(BizTrainingTask::getDispatchTargetId, self.getClassId())
                        .orderByDesc(BizTrainingTask::getCreatedAt));

        List<Map<String, Object>> list = new ArrayList<>();
        for (BizTrainingTask t : tasks) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("taskName", t.getTaskName());
            m.put("status", t.getStatus());
            m.put("startTime", t.getStartTime());
            m.put("endTime", t.getEndTime());
            m.put("createdAt", t.getCreatedAt());
            list.add(m);
        }
        return ApiResult.ok(list);
    }

    @GetMapping("/my-class/classmates")
    public ApiResult<List<Map<String, Object>>> classmates() {
        Long userId = getCurrentUserId();
        BizStudent self = studentService.getOne(
                new LambdaQueryWrapper<BizStudent>().eq(BizStudent::getUserId, userId));
        if (self == null || self.getClassId() == null) {
            return ApiResult.ok(List.of());
        }

        List<BizStudent> classmates = studentService.list(
                new LambdaQueryWrapper<BizStudent>().eq(BizStudent::getClassId, self.getClassId()));

        List<Map<String, Object>> list = new ArrayList<>();
        for (BizStudent s : classmates) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", s.getUserId());
            map.put("studentNo", s.getStudentNo());
            map.put("realName", s.getRealName());
            list.add(map);
        }
        return ApiResult.ok(list);
    }

    @GetMapping("/profile")
    public ApiResult<Map<String, Object>> profile() {
        Long userId = getCurrentUserId();
        SysUser user = sysUserService.getById(userId);
        BizStudent student = studentService.getOne(
                new LambdaQueryWrapper<BizStudent>().eq(BizStudent::getUserId, userId));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("phone", user.getPhone());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("bio", user.getBio());
        if (student != null) {
            data.put("realName", student.getRealName());
            data.put("studentNo", student.getStudentNo());
            data.put("deptId", student.getDeptId());
            data.put("majorId", student.getMajorId());
            data.put("classId", student.getClassId());
            BizAdminClass cls = adminClassService.getById(student.getClassId());
            data.put("className", cls != null ? cls.getClassName() : "");
            BizDepartment dept = departmentService.getById(student.getDeptId());
            data.put("deptName", dept != null ? dept.getDeptName() : "");
            BizMajor major = majorService.getById(student.getMajorId());
            data.put("majorName", major != null ? major.getMajorName() : "");
        }
        return ApiResult.ok(data);
    }

    @OperationLog(action = "学生更新个人资料")
    @PutMapping("/profile")
    public ApiResult<Void> updateProfile(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        SysUser user = sysUserService.getById(userId);
        if (body.containsKey("phone")) user.setPhone(body.get("phone"));
        if (body.containsKey("bio")) user.setBio(body.get("bio"));
        user.setUpdatedAt(LocalDateTime.now());
        sysUserService.updateById(user);
        return ApiResult.ok();
    }

    @OperationLog(action = "学生修改密码")
    @PutMapping("/password")
    public ApiResult<Void> updatePassword(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        SysUser user = sysUserService.getById(userId);
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return ApiResult.error("参数不完整");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            return ApiResult.error("原密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        sysUserService.updateById(user);
        return ApiResult.ok();
    }
}
