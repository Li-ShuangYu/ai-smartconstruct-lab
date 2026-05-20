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

import com.smartconstruct.backend_core.util.Java8Compat;

@RestController
@RequestMapping("/api/teacher/courses")
public class TeacherCourseController {

    private final ICourseService courseService;
    private final IStudentCourseService studentCourseService;
    private final IStudentService studentService;
    private final SysUserService sysUserService;

    public TeacherCourseController(ICourseService courseService,
                                   IStudentCourseService studentCourseService,
                                   IStudentService studentService,
                                   SysUserService sysUserService) {
        this.courseService = courseService;
        this.studentCourseService = studentCourseService;
        this.studentService = studentService;
        this.sysUserService = sysUserService;
    }

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    @GetMapping
    public ApiResult<PageResult<BizCourse>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        Long teacherId = getCurrentUserId();
        LambdaQueryWrapper<BizCourse> qw = new LambdaQueryWrapper<>();
        qw.eq(BizCourse::getTeacherId, teacherId);
        if (keyword != null && !Java8Compat.isBlank(keyword)) {
            qw.like(BizCourse::getCourseName, keyword);
        }
        qw.orderByDesc(BizCourse::getCreatedAt);
        Page<BizCourse> p = courseService.page(new Page<>(page, pageSize), qw);
        return ApiResult.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @OperationLog(action = "教师新增课程")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> create(@RequestBody BizCourse course) {
        Long teacherId = getCurrentUserId();
        course.setTeacherId(teacherId);
        course.setStatus(0);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        courseService.save(course);
        return ApiResult.ok();
    }

    @OperationLog(action = "教师编辑课程")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody BizCourse course) {
        course.setId(id);
        course.setUpdatedAt(LocalDateTime.now());
        courseService.updateById(course);
        return ApiResult.ok();
    }

    @OperationLog(action = "教师删除课程")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return ApiResult.ok();
    }

    @OperationLog(action = "教师切换课程状态")
    @PutMapping("/{id}/status")
    public ApiResult<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        BizCourse course = new BizCourse();
        course.setId(id);
        course.setStatus(status);
        course.setUpdatedAt(LocalDateTime.now());
        courseService.updateById(course);
        return ApiResult.ok();
    }

    @GetMapping("/{courseId}/students")
    public ApiResult<List<Map<String, Object>>> getCourseStudents(@PathVariable Long courseId,
                                                                    @RequestParam(required = false) String keyword) {
        List<BizStudentCourse> scList = studentCourseService.list(
                new LambdaQueryWrapper<BizStudentCourse>().eq(BizStudentCourse::getCourseId, courseId));
        if (scList.isEmpty()) return ApiResult.ok(Java8Compat.emptyList());

        Set<Long> studentIds = new HashSet<>();
        for (BizStudentCourse sc : scList) studentIds.add(sc.getStudentId());
        List<BizStudent> students = studentService.list(new LambdaQueryWrapper<BizStudent>().in(BizStudent::getUserId, studentIds));

        List<Map<String, Object>> result = new ArrayList<>();
        for (BizStudent s : students) {
            SysUser u = sysUserService.getById(s.getUserId());
            if (u == null) continue;
            if (keyword != null && !Java8Compat.isBlank(keyword)) {
                String kw = keyword.toLowerCase();
                boolean match = (s.getRealName() != null && s.getRealName().toLowerCase().contains(kw))
                        || (u.getUsername() != null && u.getUsername().toLowerCase().contains(kw));
                if (!match) continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", s.getUserId());
            m.put("realName", s.getRealName());
            m.put("username", u.getUsername());
            m.put("createdAt", u.getCreatedAt());
            result.add(m);
        }
        return ApiResult.ok(result);
    }
}