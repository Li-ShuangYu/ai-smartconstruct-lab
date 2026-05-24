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
@RequestMapping("/api/teacher")
public class TeacherDashboardController {

    private final ITrainingTaskService trainingTaskService;
    private final ICourseService courseService;
    private final SysUserService sysUserService;
    private final ITeacherService teacherService;
    private final IDepartmentService departmentService;
    private final IStudentService studentService;
    private final IStudentCourseService studentCourseService;
    private final PasswordEncoder passwordEncoder;

    public TeacherDashboardController(ITrainingTaskService trainingTaskService, ICourseService courseService,
                                       SysUserService sysUserService, ITeacherService teacherService,
                                       IDepartmentService departmentService, IStudentService studentService,
                                       IStudentCourseService studentCourseService,
                                       PasswordEncoder passwordEncoder) {
        this.trainingTaskService = trainingTaskService;
        this.courseService = courseService;
        this.sysUserService = sysUserService;
        this.teacherService = teacherService;
        this.departmentService = departmentService;
        this.studentService = studentService;
        this.studentCourseService = studentCourseService;
        this.passwordEncoder = passwordEncoder;
    }

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    @GetMapping("/dashboard/stats")
    public ApiResult<Map<String, Object>> dashboardStats() {
        Long teacherId = getCurrentUserId();

        long ongoingTasks = trainingTaskService.count(
                new LambdaQueryWrapper<BizTrainingTask>().eq(BizTrainingTask::getTeacherId, teacherId).eq(BizTrainingTask::getStatus, 1));
        long totalCourses = courseService.count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("ongoingTasks", ongoingTasks);
        stats.put("totalCourses", totalCourses);
        stats.put("totalTasks", trainingTaskService.count(
                new LambdaQueryWrapper<BizTrainingTask>().eq(BizTrainingTask::getTeacherId, teacherId)));
        return ApiResult.ok(stats);
    }



    @GetMapping("/profile")
    public ApiResult<Map<String, Object>> profile() {
        Long userId = getCurrentUserId();
        SysUser user = sysUserService.getById(userId);
        BizTeacher teacher = teacherService.getOne(
                new LambdaQueryWrapper<BizTeacher>().eq(BizTeacher::getUserId, userId));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("phone", user.getPhone());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("bio", user.getBio());
        if (teacher != null) {
            data.put("realName", teacher.getRealName());
            data.put("deptId", teacher.getDeptId());
            BizDepartment dept = departmentService.getById(teacher.getDeptId());
            data.put("deptName", dept != null ? dept.getDeptName() : "");
        }
        return ApiResult.ok(data);
    }

    @OperationLog(action = "教师更新个人资料")
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

    @GetMapping("/classes/{id}/students")
    public ApiResult<List<Map<String, Object>>> classStudents(@PathVariable Long id, @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BizStudent> qw = new LambdaQueryWrapper<BizStudent>().eq(BizStudent::getClassId, id);
        List<BizStudent> students = studentService.list(qw);
        List<Map<String, Object>> list = new ArrayList<>();
        for (BizStudent s : students) {
            SysUser u = sysUserService.getById(s.getUserId());
            if (u == null) continue;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.toLowerCase();
                boolean nameMatch = s.getRealName() != null && s.getRealName().toLowerCase().contains(kw);
                boolean userMatch = u.getUsername() != null && u.getUsername().toLowerCase().contains(kw);
                if (!nameMatch && !userMatch) continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", s.getUserId());
            m.put("studentNo", u.getUsername());
            m.put("realName", s.getRealName());
            m.put("username", u.getUsername());
            list.add(m);
        }
        return ApiResult.ok(list);
    }

    @GetMapping("/courses/{id}/students")
    public ApiResult<List<Map<String, Object>>> courseStudents(@PathVariable Long id, @RequestParam(required = false) String keyword) {
        List<BizStudentCourse> scList = studentCourseService.list(
                new LambdaQueryWrapper<BizStudentCourse>().eq(BizStudentCourse::getCourseId, id));
        List<Long> studentIds = scList.stream().map(BizStudentCourse::getStudentId).collect(java.util.stream.Collectors.toList());
        if (studentIds.isEmpty()) return ApiResult.ok(new ArrayList<>());
        List<BizStudent> students = studentService.list(new LambdaQueryWrapper<BizStudent>().in(BizStudent::getUserId, studentIds));
        List<Map<String, Object>> list = new ArrayList<>();
        for (BizStudent s : students) {
            SysUser u = sysUserService.getById(s.getUserId());
            if (u == null) continue;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.toLowerCase();
                boolean nameMatch = s.getRealName() != null && s.getRealName().toLowerCase().contains(kw);
                boolean userMatch = u.getUsername() != null && u.getUsername().toLowerCase().contains(kw);
                if (!nameMatch && !userMatch) continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", s.getUserId());
            m.put("studentNo", u.getUsername());
            m.put("realName", s.getRealName());
            m.put("username", u.getUsername());
            list.add(m);
        }
        return ApiResult.ok(list);
    }

    @OperationLog(action = "教师修改密码")
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
