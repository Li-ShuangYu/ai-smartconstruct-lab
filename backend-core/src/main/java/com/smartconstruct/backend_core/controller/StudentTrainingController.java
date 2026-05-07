package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.service.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentTrainingController {

    private final ITrainingParticipationService participationService;
    private final ITrainingTaskService trainingTaskService;
    private final ITrainingTemplateService templateService;
    private final IStudentActivityStateService activityStateService;

    public StudentTrainingController(ITrainingParticipationService participationService,
                                      ITrainingTaskService trainingTaskService,
                                      ITrainingTemplateService templateService,
                                      IStudentActivityStateService activityStateService) {
        this.participationService = participationService;
        this.trainingTaskService = trainingTaskService;
        this.templateService = templateService;
        this.activityStateService = activityStateService;
    }

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    @GetMapping("/training-tasks/{taskId}/participation")
    public ApiResult<Map<String, Object>> getParticipation(@PathVariable Long taskId) {
        Long studentId = getCurrentUserId();
        BizTrainingParticipation pt = participationService.getOne(
                new LambdaQueryWrapper<BizTrainingParticipation>()
                        .eq(BizTrainingParticipation::getTaskId, taskId)
                        .eq(BizTrainingParticipation::getStudentId, studentId));
        if (pt == null) {
            return ApiResult.error("未找到实训参与记录");
        }

        BizTrainingTask task = trainingTaskService.getById(taskId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("participationId", pt.getId());
        data.put("taskId", taskId);
        data.put("status", pt.getStatus());
        data.put("taskName", task != null ? task.getTaskName() : "");
        data.put("templateId", task != null ? task.getTemplateId() : null);

        // get template JSON
        if (task != null && task.getTemplateId() != null) {
            WfTrainingTemplate template = templateService.getById(task.getTemplateId());
            if (template != null) {
                data.put("templateJson", template.getStandardPayloadJson());
            }
        }

        // get current activity state
        WfStudentActivityState state = activityStateService.getOne(
                new LambdaQueryWrapper<WfStudentActivityState>().eq(WfStudentActivityState::getParticipationId, pt.getId()));
        if (state != null) {
            data.put("currentNodeIndex", state.getCurrentNodeIndex());
        } else {
            data.put("currentNodeIndex", -1);
        }

        return ApiResult.ok(data);
    }

    @OperationLog(action = "学生开始实训")
    @PostMapping("/training-tasks/{participationId}/start")
    public ApiResult<Map<String, Object>> start(@PathVariable Long participationId) {
        BizTrainingParticipation pt = participationService.getById(participationId);
        if (pt == null) return ApiResult.error("实训参与记录不存在");
        if (pt.getStatus() != null && pt.getStatus() != 0) return ApiResult.error("实训已经开启");

        pt.setStatus(1);
        pt.setUpdatedAt(LocalDateTime.now());
        participationService.updateById(pt);

        WfStudentActivityState state = new WfStudentActivityState();
        state.setParticipationId(participationId);
        state.setCurrentNodeIndex(0);
        state.setUpdatedAt(LocalDateTime.now());
        activityStateService.save(state);

        return ApiResult.ok(Map.of("currentNodeIndex", 0));
    }

    @OperationLog(action = "学生实训下一步")
    @PostMapping("/training-tasks/{participationId}/next")
    public ApiResult<Map<String, Object>> next(@PathVariable Long participationId) {
        WfStudentActivityState state = activityStateService.getOne(
                new LambdaQueryWrapper<WfStudentActivityState>().eq(WfStudentActivityState::getParticipationId, participationId));
        if (state == null) return ApiResult.error("实训状态不存在，请先开始实训");

        BizTrainingParticipation pt = participationService.getById(participationId);
        BizTrainingTask task = trainingTaskService.getById(pt.getTaskId());
        WfTrainingTemplate template = templateService.getById(task.getTemplateId());
        Object payloadObj = template.getStandardPayloadJson();

        // parse nodes count from template JSON
        int totalNodes = 0;
        if (payloadObj instanceof Map) {
            Map<?, ?> payloadMap = (Map<?, ?>) payloadObj;
            Object nodesObj = payloadMap.get("nodes");
            if (nodesObj instanceof List) totalNodes = ((List<?>) nodesObj).size();
        }

        int nextIndex = (state.getCurrentNodeIndex() != null ? state.getCurrentNodeIndex() : 0) + 1;

        if (nextIndex >= totalNodes) {
            // reached end
            pt.setStatus(2);
            pt.setUpdatedAt(LocalDateTime.now());
            participationService.updateById(pt);
            return ApiResult.ok(Map.of("completed", true, "currentNodeIndex", nextIndex));
        }

        state.setCurrentNodeIndex(nextIndex);
        state.setUpdatedAt(LocalDateTime.now());
        activityStateService.updateById(state);

        return ApiResult.ok(Map.of("currentNodeIndex", nextIndex, "completed", false));
    }
}
