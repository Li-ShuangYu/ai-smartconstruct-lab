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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher/training-tasks")
public class TeacherTrainingTaskController {

    private final ITrainingTaskService trainingTaskService;
    private final ITrainingParticipationService participationService;
    private final IStudentService studentService;
    private final IStudentCourseService studentCourseService;
    private final ITrainingTemplateService templateService;

    public TeacherTrainingTaskController(ITrainingTaskService trainingTaskService,
                                          ITrainingParticipationService participationService,
                                          IStudentService studentService,
                                          IStudentCourseService studentCourseService,
                                          ITrainingTemplateService templateService) {
        this.trainingTaskService = trainingTaskService;
        this.participationService = participationService;
        this.studentService = studentService;
        this.studentCourseService = studentCourseService;
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
        Integer dispatchScope = body.get("dispatchScope") != null ? Integer.valueOf(body.get("dispatchScope").toString()) : null;
        Long targetId = body.get("dispatchTargetId") != null ? Long.valueOf(body.get("dispatchTargetId").toString()) : null;

        if (templateId == null || taskName == null || taskName.isBlank() || dispatchScope == null || targetId == null) {
            return ApiResult.error("参数不完整：需要 templateId, taskName, dispatchScope, dispatchTargetId");
        }

        WfTrainingTemplate template = templateService.getById(templateId);
        if (template == null || (template.getAiStatus() != null && template.getAiStatus() != 2)) {
            return ApiResult.error("模板不存在或尚未就绪");
        }

        // 根据下发范围获取学生列表
        List<Long> studentIds;
        if (dispatchScope == 1) {
            studentIds = studentService.list(new LambdaQueryWrapper<BizStudent>().eq(BizStudent::getClassId, targetId))
                    .stream().map(BizStudent::getUserId).collect(Collectors.toList());
        } else if (dispatchScope == 2) {
            studentIds = studentCourseService.list(new LambdaQueryWrapper<BizStudentCourse>().eq(BizStudentCourse::getCourseId, targetId))
                    .stream().map(BizStudentCourse::getStudentId).collect(Collectors.toList());
        } else {
            return ApiResult.error("无效的下发范围类型");
        }

        if (studentIds.isEmpty()) {
            return ApiResult.error("目标范围内没有学生");
        }

        BizTrainingTask task = new BizTrainingTask();
        task.setTemplateId(templateId);
        task.setTeacherId(teacherId);
        task.setTaskName(taskName);
        task.setStatus(0);
        task.setDispatchScope(dispatchScope);
        task.setDispatchTargetId(targetId);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        trainingTaskService.save(task);

        List<BizTrainingParticipation> participations = new ArrayList<>();
        for (Long sid : studentIds) {
            BizTrainingParticipation p = new BizTrainingParticipation();
            p.setTaskId(task.getId());
            p.setStudentId(sid);
            p.setStatus(0);
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            participations.add(p);
        }
        participationService.saveBatch(participations);

        return ApiResult.ok(Map.of("taskId", task.getId(), "studentCount", participations.size()));
    }
}
