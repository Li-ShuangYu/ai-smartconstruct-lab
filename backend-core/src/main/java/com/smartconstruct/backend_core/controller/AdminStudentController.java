package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.BizStudent;
import com.smartconstruct.backend_core.service.IStudentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/students")
public class AdminStudentController {

    private final IStudentService studentService;

    public AdminStudentController(IStudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ApiResult<PageResult<BizStudent>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BizStudent> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like(BizStudent::getRealName, keyword).or().like(BizStudent::getStudentNo, keyword);
        }
        qw.orderByDesc(BizStudent::getUserId);
        Page<BizStudent> p = studentService.page(new Page<>(page, pageSize), qw);
        return ApiResult.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResult<BizStudent> detail(@PathVariable Long id) {
        return ApiResult.ok(studentService.getById(id));
    }

    @OperationLog(action = "新增学生")
    @PostMapping
    public ApiResult<Void> create(@RequestBody BizStudent student) {
        studentService.save(student);
        return ApiResult.ok();
    }

    @OperationLog(action = "编辑学生")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody BizStudent student) {
        student.setUserId(id);
        studentService.updateById(student);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除学生")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        studentService.removeById(id);
        return ApiResult.ok();
    }
}