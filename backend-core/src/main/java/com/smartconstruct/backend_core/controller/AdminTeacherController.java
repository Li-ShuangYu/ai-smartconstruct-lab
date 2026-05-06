package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.BizTeacher;
import com.smartconstruct.backend_core.service.ITeacherService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/teachers")
public class AdminTeacherController {

    private final ITeacherService teacherService;

    public AdminTeacherController(ITeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public ApiResult<PageResult<BizTeacher>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BizTeacher> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like(BizTeacher::getRealName, keyword).or().like(BizTeacher::getEmployeeNo, keyword);
        }
        qw.orderByDesc(BizTeacher::getUserId);
        Page<BizTeacher> p = teacherService.page(new Page<>(page, pageSize), qw);
        return ApiResult.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResult<BizTeacher> detail(@PathVariable Long id) {
        return ApiResult.ok(teacherService.getById(id));
    }

    @OperationLog(action = "新增教师")
    @PostMapping
    public ApiResult<Void> create(@RequestBody BizTeacher teacher) {
        teacherService.save(teacher);
        return ApiResult.ok();
    }

    @OperationLog(action = "编辑教师")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody BizTeacher teacher) {
        teacher.setUserId(id);
        teacherService.updateById(teacher);
        return ApiResult.ok();
    }

    @OperationLog(action = "删除教师")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        teacherService.removeById(id);
        return ApiResult.ok();
    }
}