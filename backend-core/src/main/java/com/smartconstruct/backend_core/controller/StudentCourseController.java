package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.BizCourse;
import com.smartconstruct.backend_core.entity.BizStudentCourse;
import com.smartconstruct.backend_core.service.ICourseService;
import com.smartconstruct.backend_core.service.IStudentCourseService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/courses")
public class StudentCourseController {

    private final ICourseService courseService;
    private final IStudentCourseService studentCourseService;

    public StudentCourseController(ICourseService courseService, IStudentCourseService studentCourseService) {
        this.courseService = courseService;
        this.studentCourseService = studentCourseService;
    }

    @GetMapping("/available")
    public ApiResult<PageResult<Map<String, Object>>> available(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        Long studentId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        LambdaQueryWrapper<BizCourse> qw = new LambdaQueryWrapper<>();
        qw.eq(BizCourse::getStatus, 1);
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like(BizCourse::getCourseName, keyword).or().like(BizCourse::getCourseCode, keyword));
        }
        qw.orderByDesc(BizCourse::getCreatedAt);
        Page<BizCourse> p = courseService.page(new Page<>(page, pageSize), qw);

        List<Long> courseIds = p.getRecords().stream().map(BizCourse::getId).collect(Collectors.toList());
        Set<Long> enrolledCourseIds = Set.of();
        if (!courseIds.isEmpty()) {
            LambdaQueryWrapper<BizStudentCourse> scQw = new LambdaQueryWrapper<>();
            scQw.eq(BizStudentCourse::getStudentId, studentId).in(BizStudentCourse::getCourseId, courseIds);
            enrolledCourseIds = studentCourseService.list(scQw).stream()
                    .map(BizStudentCourse::getCourseId).collect(Collectors.toSet());
        }

        final Set<Long> enrolledSet = enrolledCourseIds;
        List<Map<String, Object>> records = p.getRecords().stream().map(course -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", course.getId());
            map.put("courseName", course.getCourseName());
            map.put("courseCode", course.getCourseCode());
            map.put("description", course.getDescription());
            map.put("status", course.getStatus());
            map.put("needEnrollCode", course.getNeedEnrollCode());
            map.put("createdAt", course.getCreatedAt());
            map.put("updatedAt", course.getUpdatedAt());
            map.put("isEnrolled", enrolledSet.contains(course.getId()));
            return map;
        }).collect(Collectors.toList());

        return ApiResult.ok(new PageResult<>(records, p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @OperationLog(action = "学生选课")
    @PostMapping("/enroll/{courseId}")
    public ApiResult<Void> enroll(@PathVariable Long courseId, @RequestBody(required = false) Map<String, String> body) {
        Long studentId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        BizCourse course = courseService.getById(courseId);
        if (course == null) {
            return ApiResult.error("课程不存在");
        }
        if (course.getStatus() == null || course.getStatus() != 1) {
            return ApiResult.error("课程未开放选课");
        }
        if (course.getNeedEnrollCode() != null && course.getNeedEnrollCode() == 1) {
            String enrollCode = body != null ? body.get("enrollCode") : null;
            if (enrollCode == null || !enrollCode.equals(course.getEnrollCode())) {
                return ApiResult.error("选课码错误");
            }
        }

        LambdaQueryWrapper<BizStudentCourse> qw = new LambdaQueryWrapper<>();
        qw.eq(BizStudentCourse::getStudentId, studentId).eq(BizStudentCourse::getCourseId, courseId);
        if (studentCourseService.count(qw) > 0) {
            return ApiResult.error("已选择该课程，不可重复选课");
        }

        BizStudentCourse sc = new BizStudentCourse();
        sc.setStudentId(studentId);
        sc.setCourseId(courseId);
        sc.setCreatedAt(LocalDateTime.now());
        studentCourseService.save(sc);
        return ApiResult.ok();
    }

    @GetMapping("/my")
    public ApiResult<PageResult<Map<String, Object>>> myCourses(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        Long studentId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        LambdaQueryWrapper<BizStudentCourse> scQw = new LambdaQueryWrapper<>();
        scQw.eq(BizStudentCourse::getStudentId, studentId);
        List<Long> courseIds = studentCourseService.list(scQw).stream()
                .map(BizStudentCourse::getCourseId).collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return ApiResult.ok(new PageResult<>(List.of(), 0, page, pageSize));
        }

        LambdaQueryWrapper<BizCourse> qw = new LambdaQueryWrapper<>();
        qw.in(BizCourse::getId, courseIds).orderByDesc(BizCourse::getCreatedAt);
        Page<BizCourse> p = courseService.page(new Page<>(page, pageSize), qw);

        List<Map<String, Object>> records = p.getRecords().stream().map(course -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", course.getId());
            map.put("courseName", course.getCourseName());
            map.put("courseCode", course.getCourseCode());
            map.put("description", course.getDescription());
            map.put("status", course.getStatus());
            map.put("createdAt", course.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        return ApiResult.ok(new PageResult<>(records, p.getTotal(), p.getCurrent(), p.getSize()));
    }
}
