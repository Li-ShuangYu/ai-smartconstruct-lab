package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.entity.BizTrainingTask;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.service.ITrainingTaskService;
import com.smartconstruct.backend_core.service.ITrainingTemplateService;
import com.smartconstruct.backend_core.util.Java8Compat;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    @GetMapping
    public ApiResult<PageResult<WfTrainingTemplate>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer aiStatus) {
        Long currentUserId = getCurrentUserId();
        LambdaQueryWrapper<WfTrainingTemplate> qw = new LambdaQueryWrapper<>();
        qw.eq(WfTrainingTemplate::getCreatorId, currentUserId);
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
        if (templateName == null || Java8Compat.isBlank(templateName)) {
            return ApiResult.error("模板名称不能为空");
        }

        WfTrainingTemplate template = new WfTrainingTemplate();
        template.setTemplateName(templateName);
        template.setTemplateDescription((String) body.get("templateDescription"));
        template.setRawCanvasJson(canvasData);
        template.setCreatorId(getCurrentUserId());
        template.setAiStatus(0);
        LocalDateTime now = LocalDateTime.now();
        template.setCreatedAt(now);
        template.setUpdatedAt(now);
        templateService.save(template);

        return ApiResult.ok(Java8Compat.mapOf("id", template.getId(), "aiStatus", 0));
    }

    @OperationLog(action = "删除实训模板")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable String id) {
        Long currentUserId = getCurrentUserId();
        WfTrainingTemplate template = templateService.getById(Long.parseLong(id));
        if (template == null) return ApiResult.error("模板不存在");
        if (!Long.valueOf(currentUserId).equals(template.getCreatorId())) {
            return ApiResult.error("无权操作非本人创建的模板");
        }
        long usageCount = trainingTaskService.count(
                new LambdaQueryWrapper<BizTrainingTask>().eq(BizTrainingTask::getTemplateId, template.getId()));
        if (usageCount > 0) {
            return ApiResult.error("该模板已被用于实训任务，无法直接删除");
        }
        templateService.removeById(template.getId());
        return ApiResult.ok();
    }

    @OperationLog(action = "修改实训模板")
    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Long currentUserId = getCurrentUserId();
        WfTrainingTemplate template = templateService.getById(Long.parseLong(id));
        if (template == null) return ApiResult.error("模板不存在");
        if (!Long.valueOf(currentUserId).equals(template.getCreatorId())) {
            return ApiResult.error("无权操作非本人创建的模板");
        }
        String name = (String) body.get("templateName");
        String desc = body.get("templateDescription") != null ? (String) body.get("templateDescription") : (String) body.get("templateDesc");
        if (name != null && !Java8Compat.isBlank(name)) template.setTemplateName(name);
        if (desc != null) template.setTemplateDescription(desc);
        template.setUpdatedAt(LocalDateTime.now());
        templateService.updateById(template);
        return ApiResult.ok();
    }
}