package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.service.ITrainingTemplateService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/templates")
public class AdminTemplateController {

    private final ITrainingTemplateService templateService;

    public AdminTemplateController(ITrainingTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ApiResult<PageResult<WfTrainingTemplate>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer aiStatus) {
        LambdaQueryWrapper<WfTrainingTemplate> qw = new LambdaQueryWrapper<>();
        if (aiStatus != null) qw.eq(WfTrainingTemplate::getAiStatus, aiStatus);
        qw.orderByDesc(WfTrainingTemplate::getCreatedAt);
        Page<WfTrainingTemplate> p = templateService.page(new Page<>(page, pageSize), qw);
        return ApiResult.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @GetMapping("/{id}")
    public ApiResult<WfTrainingTemplate> detail(@PathVariable Long id) {
        return ApiResult.ok(templateService.getById(id));
    }

    @OperationLog(action = "删除实训模板")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        templateService.removeById(id);
        return ApiResult.ok();
    }

    @OperationLog(action = "修改模板状态")
    @PutMapping("/{id}/status")
    public ApiResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer aiStatus) {
        WfTrainingTemplate t = new WfTrainingTemplate();
        t.setId(id);
        t.setAiStatus(aiStatus);
        templateService.updateById(t);
        return ApiResult.ok();
    }
}
