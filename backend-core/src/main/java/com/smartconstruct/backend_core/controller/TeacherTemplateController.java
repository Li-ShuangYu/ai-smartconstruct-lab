package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.BizTrainingTask;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.service.ITrainingTaskService;
import com.smartconstruct.backend_core.service.ITrainingTemplateService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/templates")
public class TeacherTemplateController {

    private final ITrainingTemplateService templateService;
    private final ITrainingTaskService trainingTaskService;

    public TeacherTemplateController(ITrainingTemplateService templateService, ITrainingTaskService trainingTaskService) {
        this.templateService = templateService;
        this.trainingTaskService = trainingTaskService;
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

    @OperationLog(action = "创建实训模板编排")
    @PostMapping
    public ApiResult<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String templateName = (String) body.get("templateName");
        Object canvasData = body.get("canvasData");
        if (templateName == null || templateName.isBlank()) {
            return ApiResult.error("模板名称不能为空");
        }

        WfTrainingTemplate template = new WfTrainingTemplate();
        template.setTemplateName(templateName);
        template.setRawCanvasJson(canvasData);
        template.setAiStatus(1);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        templateService.save(template);

        templateService.processTemplateMockAi(template.getId(), canvasData);

        return ApiResult.ok(Map.of("id", template.getId(), "aiStatus", 1));
    }

    @OperationLog(action = "删除实训模板")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        long usageCount = trainingTaskService.count(
                new LambdaQueryWrapper<BizTrainingTask>().eq(BizTrainingTask::getTemplateId, id));
        if (usageCount > 0) {
            return ApiResult.error("该模板已被用于实训任务，无法直接删除");
        }
        templateService.removeById(id);
        return ApiResult.ok();
    }
}
