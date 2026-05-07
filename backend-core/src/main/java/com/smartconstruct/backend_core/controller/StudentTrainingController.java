package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.ParticipationDetailVO;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentTrainingController {

    private static final Logger log = LoggerFactory.getLogger(StudentTrainingController.class);

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
    public ApiResult<ParticipationDetailVO> getParticipation(@PathVariable Long taskId) {
        Long studentId = getCurrentUserId();

        BizTrainingParticipation pt;
        try {
            pt = participationService.getOne(
                    new LambdaQueryWrapper<BizTrainingParticipation>()
                            .eq(BizTrainingParticipation::getTaskId, taskId)
                            .eq(BizTrainingParticipation::getStudentId, studentId));
        } catch (Exception e) {
            log.error("查询参与记录失败 taskId={} studentId={}", taskId, studentId, e);
            return ApiResult.error("查询参与记录失败");
        }
        if (pt == null) return ApiResult.error("您未参与该实训任务");

        ParticipationDetailVO vo = new ParticipationDetailVO();
        vo.setParticipationId(pt.getId());
        vo.setTaskId(taskId);
        vo.setStatus(pt.getStatus());

        try {
            BizTrainingTask task = trainingTaskService.getById(taskId);
            if (task != null) {
                vo.setTaskName(task.getTaskName());
                if (task.getTemplateId() != null) {
                    try {
                        WfTrainingTemplate template = templateService.getById(task.getTemplateId());
                        if (template != null && template.getStandardPayloadJson() != null) {
                            vo.setTemplateJson(template.getStandardPayloadJson());
                        }
                    } catch (Exception e) {
                        log.warn("读取模板JSON失败 templateId={}", task.getTemplateId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("查询任务详情失败 taskId={}", taskId, e);
        }
        if (vo.getTaskName() == null) vo.setTaskName("未知实训");

        // 查活动状态 — 返回字符串 node_id
        try {
            WfStudentActivityState state = activityStateService.getOne(
                    new LambdaQueryWrapper<WfStudentActivityState>()
                            .eq(WfStudentActivityState::getParticipationId, pt.getId()));
            if (state != null && state.getCurrentNodeId() != null) {
                vo.setCurrentNodeId(state.getCurrentNodeId());
            }
        } catch (Exception e) {
            log.warn("查询活动状态失败 participationId={}", pt.getId(), e);
        }

        return ApiResult.ok(vo);
    }

    @OperationLog(action = "学生开始实训")
    @PostMapping("/training-tasks/{participationId}/start")
    public ApiResult<Map<String, Object>> start(@PathVariable Long participationId,
                                                 @RequestBody Map<String, Object> body) {
        if (participationId == null || participationId <= 0) {
            return ApiResult.error("无效的参与记录ID");
        }
        String startNodeId = body.get("startNodeId") != null ? body.get("startNodeId").toString() : null;
        if (startNodeId == null || startNodeId.isBlank()) {
            return ApiResult.error("缺少起始节点ID");
        }

        BizTrainingParticipation pt = participationService.getById(participationId);
        if (pt == null) return ApiResult.error("实训参与记录不存在");
        if (pt.getStatus() != null && pt.getStatus() != 0) return ApiResult.error("实训已经开启");

        pt.setStatus(1);
        pt.setUpdatedAt(LocalDateTime.now());
        participationService.updateById(pt);

        WfStudentActivityState state = new WfStudentActivityState();
        state.setParticipationId(participationId);
        state.setCurrentNodeId(startNodeId);
        state.setUpdatedAt(LocalDateTime.now());
        activityStateService.save(state);

        return ApiResult.ok(Map.of("currentNodeId", startNodeId));
    }

    @OperationLog(action = "学生实训下一步")
    @PostMapping("/training-tasks/{participationId}/next")
    public ApiResult<Map<String, Object>> next(@PathVariable Long participationId,
                                                @RequestBody Map<String, Object> body) {
        WfStudentActivityState state = activityStateService.getOne(
                new LambdaQueryWrapper<WfStudentActivityState>()
                        .eq(WfStudentActivityState::getParticipationId, participationId));
        if (state == null) return ApiResult.error("实训状态不存在");

        String nextNodeId = body.get("nextNodeId") != null ? body.get("nextNodeId").toString() : null;
        boolean isEnd = body.get("isEnd") != null && Boolean.TRUE.equals(body.get("isEnd"));

        if (nextNodeId == null || nextNodeId.isBlank()) {
            return ApiResult.error("缺少下一节点ID");
        }

        if (isEnd) {
            BizTrainingParticipation pt = participationService.getById(participationId);
            pt.setStatus(2);
            pt.setUpdatedAt(LocalDateTime.now());
            participationService.updateById(pt);
            return ApiResult.ok(Map.of("completed", true, "currentNodeId", nextNodeId));
        }

        state.setCurrentNodeId(nextNodeId);
        state.setUpdatedAt(LocalDateTime.now());
        activityStateService.updateById(state);

        return ApiResult.ok(Map.of("currentNodeId", nextNodeId, "completed", false));
    }
}
