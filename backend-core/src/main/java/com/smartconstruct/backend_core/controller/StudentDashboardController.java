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

/**
 * 学生仪表板控制器
 * 
 * 负责处理学生端个人中心相关的API请求，主要功能：
 * - 查询我的实训任务列表（支持分页和状态筛选）
 * - 查询班级实训任务
 * - 查询班级同学列表
 * - 获取/更新个人资料
 * - 修改密码
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/student")
public class StudentDashboardController {

    /** 实训任务服务 - 管理实训任务信息 */
    private final ITrainingTaskService trainingTaskService;
    
    /** 实训参与服务 - 管理学生参与记录 */
    private final ITrainingParticipationService participationService;
    
    /** 学生服务 - 查询学生信息 */
    private final IStudentService studentService;
    
    /** 班级服务 - 查询班级信息 */
    private final IAdminClassService adminClassService;
    
    /** 院系服务 - 查询院系信息 */
    private final IDepartmentService departmentService;
    
    /** 专业服务 - 查询专业信息 */
    private final IMajorService majorService;
    
    /** 用户服务 - 管理用户信息 */
    private final SysUserService sysUserService;
    
    /** 密码编码器 - 用于密码验证和加密 */
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造函数 - 注入依赖服务
     * 
     * @param trainingTaskService 实训任务服务
     * @param participationService 实训参与服务
     * @param studentService 学生服务
     * @param adminClassService 班级服务
     * @param departmentService 院系服务
     * @param majorService 专业服务
     * @param sysUserService 用户服务
     * @param passwordEncoder 密码编码器
     */
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

    /**
     * 获取当前登录学生ID
     * 
     * 从Spring Security上下文获取当前认证学生的ID
     * 
     * @return 当前学生ID
     */
    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    /**
     * 查询我的实训任务列表（分页）
     * 
     * 查询当前学生参与的实训任务，支持按状态筛选。
     * 
     * @param page 页码，默认1
     * @param pageSize 每页大小，默认10
     * @param status 参与状态筛选（可选）：0=未开始，1=进行中，2=已完成
     * @return 分页结果，包含任务列表和参与状态
     */
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

    /**
     * 查询班级实训任务列表
     * 
     * 查询下发到当前学生所在班级的所有实训任务。
     * 
     * @return 班级实训任务列表
     */
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

    /**
     * 查询班级同学列表
     * 
     * 查询当前学生所在班级的所有同学信息。
     * 
     * @return 同学列表，包含用户ID、学号、真实姓名
     */
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

    /**
     * 获取学生个人资料
     * 
     * 返回当前学生的完整个人信息，包括：
     * - 用户基本信息（用户名、手机号、头像、简介）
     * - 学生信息（真实姓名、学号、班级、院系、专业）
     * 
     * @return 个人资料信息
     */
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

    /**
     * 更新学生个人资料
     * 
     * 支持更新手机号和个人简介。
     * 
     * @param body 请求体，包含 phone（手机号）和 bio（简介）
     * @return 操作结果
     */
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

    /**
     * 修改学生密码
     * 
     * 验证原密码后更新为新密码，新密码会进行BCrypt加密存储。
     * 
     * @param body 请求体，包含 oldPassword（原密码）和 newPassword（新密码）
     * @return 操作结果
     */
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
