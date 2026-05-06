package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.BizCourse;
import com.smartconstruct.backend_core.service.ICourseService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    private final ICourseService courseService;

    public AdminCourseController(ICourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResult<PageResult<BizCourse>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BizCourse> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like(BizCourse::getCourseName, keyword).or().like(BizCourse::getCourseCode, keyword);
        }
        qw.orderByDesc(BizCourse::getCreatedAt);
        Page<BizCourse> p = courseService.page(new Page<>(page, pageSize), qw);
        return ApiResult.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResult<BizCourse> detail(@PathVariable Long id) {
        return ApiResult.ok(courseService.getById(id));
    }

    @OperationLog(action = "新增课程")
    @PostMapping
    public ApiResult<Void> create(@RequestBody BizCourse course) {
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        courseService.save(course);
        return ApiResult.ok();
    }

    @OperationLog(action = "编辑课程")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody BizCourse course) {
        course.setId(id);
        course.setUpdatedAt(LocalDateTime.now());
        courseService.updateById(course);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除课程")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return ApiResult.ok();
    }

    @OperationLog(action = "切换课程状态")
    @PutMapping("/{id}/status")
    public ApiResult<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        BizCourse course = new BizCourse();
        course.setId(id);
        course.setStatus(status);
        course.setUpdatedAt(LocalDateTime.now());
        courseService.updateById(course);
        return ApiResult.ok();
    }
}
