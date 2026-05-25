package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.service.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/courses")
public class StudentCourseController {

    private final ICourseService courseService;
    private final IStudentCourseService studentCourseService;
    private final SysUserService sysUserService;

    public StudentCourseController(ICourseService courseService, IStudentCourseService studentCourseService,
                                   SysUserService sysUserService) {
        this.courseService = courseService;
        this.studentCourseService = studentCourseService;
        this.sysUserService = sysUserService;
    }

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    private String getTeacherName(BizCourse course) {
        if (course.getTeacherId() == null) return "-";
        SysUser u = sysUserService.getById(course.getTeacherId());
        return u != null ? u.getUsername() : "-";
    }

    @GetMapping("/available")
    public ApiResult<PageResult<Map<String, Object>>> available(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        Long studentId = getCurrentUserId();

        List<Long> enrolledIds = studentCourseService.list(
                new LambdaQueryWrapper<BizStudentCourse>().eq(BizStudentCourse::getStudentId, studentId))
                .stream().map(BizStudentCourse::getCourseId).collect(Collectors.toList());

        LambdaQueryWrapper<BizCourse> qw = new LambdaQueryWrapper<>();
        qw.eq(BizCourse::getStatus, 1);
        if (!enrolledIds.isEmpty()) {
            qw.notIn(BizCourse::getId, enrolledIds);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(BizCourse::getCourseName, keyword));
        }
        qw.orderByDesc(BizCourse::getCreatedAt);
        Page<BizCourse> p = courseService.page(new Page<>(page, pageSize), qw);

        List<Map<String, Object>> records = p.getRecords().stream().map(course -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", String.valueOf(course.getId()));
            map.put("courseName", course.getCourseName());
            map.put("description", course.getDescription());
            map.put("teacherName", getTeacherName(course));
            map.put("status", course.getStatus());
            // course_code 不返回给前端，只返回布尔标记
            map.put("needEnrollCode", course.getNeedEnrollCode() != null && course.getNeedEnrollCode() == 1 ? 1 : 0);
            map.put("createdAt", course.getCreatedAt());
            map.put("updatedAt", course.getUpdatedAt());
            map.put("isEnrolled", false);
            return map;
        }).collect(Collectors.toList());

        return ApiResult.ok(new PageResult<>(records, p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @GetMapping("/my")
    public ApiResult<PageResult<Map<String, Object>>> my(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        Long studentId = getCurrentUserId();

        List<BizStudentCourse> scList = studentCourseService.list(
                new LambdaQueryWrapper<BizStudentCourse>().eq(BizStudentCourse::getStudentId, studentId));
        List<Long> courseIds = scList.stream().map(BizStudentCourse::getCourseId).collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return ApiResult.ok(new PageResult<>(Collections.emptyList(), 0, page, pageSize));
        }

        LambdaQueryWrapper<BizCourse> qw = new LambdaQueryWrapper<>();
        qw.in(BizCourse::getId, courseIds).orderByDesc(BizCourse::getCreatedAt);
        Page<BizCourse> p = courseService.page(new Page<>(page, pageSize), qw);

        List<Map<String, Object>> records = p.getRecords().stream().map(course -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", String.valueOf(course.getId()));
            map.put("courseName", course.getCourseName());
            map.put("description", course.getDescription());
            map.put("teacherName", getTeacherName(course));
            map.put("status", course.getStatus());
            map.put("createdAt", course.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        return ApiResult.ok(new PageResult<>(records, p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @OperationLog(action = "学生选课")
    @PostMapping("/enroll/{courseId}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> enroll(@PathVariable String courseId, @RequestBody(required = false) Map<String, String> body) {
        Long studentId = getCurrentUserId();
        Long cId = Long.parseLong(courseId);

        BizCourse course = courseService.getById(cId);
        if (course == null) return ApiResult.error("课程不存在");
        if (course.getStatus() == null || course.getStatus() != 1) return ApiResult.error("课程未开放选课");

        if (course.getNeedEnrollCode() != null && course.getNeedEnrollCode() == 1) {
            String code = body != null ? body.get("enrollCode") : null;
            if (code == null || !code.equals(course.getEnrollCode())) {
                return ApiResult.error("选课码错误");
            }
        }

        long dup = studentCourseService.count(new LambdaQueryWrapper<BizStudentCourse>()
                .eq(BizStudentCourse::getStudentId, studentId)
                .eq(BizStudentCourse::getCourseId, cId));
        if (dup > 0) return ApiResult.error("已加入该课程");

        BizStudentCourse sc = new BizStudentCourse();
        sc.setStudentId(studentId);
        sc.setCourseId(cId);
        sc.setCreatedAt(LocalDateTime.now());
        studentCourseService.save(sc);
        return ApiResult.ok();
    }
}