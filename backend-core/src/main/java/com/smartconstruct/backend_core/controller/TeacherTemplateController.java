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

/**
 * 教师模板控制器
 * 
 * 负责处理教师端实训模板相关的API请求，包括：
 * - 查询模板列表（支持分页和AI状态筛选）
 * - 创建实训模板（保存画布JSON并触发AI处理）
 * - 删除实训模板（需校验是否被任务引用）
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/teacher/templates")
public class TeacherTemplateController {

    /** 实训模板服务 - 管理模板的CRUD操作 */
    private final ITrainingTemplateService templateService;
    
    /** 实训任务服务 - 用于校验模板是否被任务引用 */
    private final ITrainingTaskService trainingTaskService;

    public TeacherTemplateController(ITrainingTemplateService templateService, ITrainingTaskService trainingTaskService) {
        this.templateService = templateService;
        this.trainingTaskService = trainingTaskService;
    }

    /**
     * 查询实训模板列表（分页）
     * 
     * 支持按AI处理状态筛选，默认按创建时间倒序排列。
     * 
     * @param page 页码，默认1
     * @param pageSize 每页大小，默认10
     * @param aiStatus AI处理状态（可选）：1=处理中，2=完成
     * @return 分页结果
     */
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

    /**
     * 创建实训模板
     * 
     * 保存教师编排的实训流程画布数据，包括：
     * - 模板名称
     * - 原始画布JSON（Vue Flow编排数据）
     * 
     * 创建后会异步触发AI处理（模拟），将原始数据转换为标准执行载荷。
     * 
     * @param body 请求体，包含 templateName（模板名称）和 canvasData（画布JSON）
     * @return 模板ID和AI处理状态
     */
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

    /**
     * 删除实训模板
     * 
     * 删除前会校验该模板是否被实训任务引用，若已被引用则拒绝删除。
     * 
     * @param id 模板ID
     * @return 操作结果
     */
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
