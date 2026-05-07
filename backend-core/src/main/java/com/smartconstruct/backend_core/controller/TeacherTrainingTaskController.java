package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.service.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/training-tasks")
public class TeacherTrainingTaskController {

    private final ITrainingTaskService trainingTaskService;
    private final ITrainingParticipationService participationService;
    private final IStudentService studentService;
    private final ITrainingTemplateService templateService;

    public TeacherTrainingTaskController(ITrainingTaskService trainingTaskService,
                                          ITrainingParticipationService participationService,
                                          IStudentService studentService,
                                          ITrainingTemplateService templateService) {
        this.trainingTaskService = trainingTaskService;
        this.participationService = participationService;
        this.studentService = studentService;
        this.templateService = templateService;
    }

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    @OperationLog(action = "下发实训任务")
    @PostMapping
    public ApiResult<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Long teacherId = getCurrentUserId();
        Long templateId = body.get("templateId") != null ? Long.valueOf(body.get("templateId").toString()) : null;
        String taskName = (String) body.get("taskName");
        Long classId = body.get("dispatchTargetId") != null ? Long.valueOf(body.get("dispatchTargetId").toString()) : null;

        if (templateId == null || taskName == null || taskName.isBlank() || classId == null) {
            return ApiResult.error("参数不完整：需要 templateId, taskName, dispatchTargetId");
        }

        WfTrainingTemplate template = templateService.getById(templateId);
        if (template == null || (template.getAiStatus() != null && template.getAiStatus() != 2)) {
            return ApiResult.error("模板不存在或尚未就绪");
        }

        BizTrainingTask task = new BizTrainingTask();
        task.setTemplateId(templateId);
        task.setTeacherId(teacherId);
        task.setTaskName(taskName);
        task.setStatus(0);
        task.setDispatchScope(1);
        task.setDispatchTargetId(classId);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        trainingTaskService.save(task);

        List<BizStudent> students = studentService.list(
                new LambdaQueryWrapper<BizStudent>().eq(BizStudent::getClassId, classId));
        List<BizTrainingParticipation> participations = new ArrayList<>();
        for (BizStudent s : students) {
            BizTrainingParticipation p = new BizTrainingParticipation();
            p.setTaskId(task.getId());
            p.setStudentId(s.getUserId());
            p.setStatus(0);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            participations.add(p);
        }
        if (!participations.isEmpty()) {
            participationService.saveBatch(participations);
        }

        return ApiResult.ok(Map.of("taskId", task.getId(), "studentCount", participations.size()));
    }
}
