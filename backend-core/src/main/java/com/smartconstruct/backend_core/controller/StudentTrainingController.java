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

/**
 * 学生实训控制器
 * 
 * 负责处理学生端实训相关的所有API请求，包括：
 * - 获取实训参与详情
 * - 开始实训
 * - 推进实训节点（下一节点）
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/student")
public class StudentTrainingController {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(StudentTrainingController.class);

    /** 实训参与服务 - 管理学生参与实训的记录 */
    private final ITrainingParticipationService participationService;
    
    /** 实训任务服务 - 管理实训任务信息 */
    private final ITrainingTaskService trainingTaskService;
    
    /** 实训模板服务 - 管理实训流程模板 */
    private final ITrainingTemplateService templateService;
    
    /** 学生活动状态服务 - 管理学生在实训中的当前节点状态 */
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

    /**
     * 获取当前登录用户ID
     * 
     * 从Spring Security上下文获取当前认证用户的ID
     * 
     * @return 当前用户ID
     */
    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    /**
     * 获取学生实训参与详情
     * 
     * 根据任务ID和当前登录学生ID，获取实训参与记录详情，包括：
     * - 参与记录基本信息（状态、任务名称等）
     * - 实训模板JSON（用于前端渲染流程）
     * - 当前节点ID（学生当前所在的实训节点）
     * 
     * @param taskId 实训任务ID
     * @return 参与详情VO
     */
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

    /**
     * 学生开始实训
     * 
     * 将实训参与状态设置为进行中（status=1），并创建学生活动状态记录，记录当前节点ID。
     * 
     * @param participationId 参与记录ID
     * @param body 请求体，包含 startNodeId（起始节点ID）
     * @return 当前节点ID
     */
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

    /**
     * 学生实训推进到下一节点
     * 
     * 更新学生活动状态，将当前节点ID更新为下一节点。
     * 如果是结束节点（isEnd=true），则将实训参与状态设置为已完成（status=2）。
     * 
     * 注意：此方法包含兜底逻辑，若活动状态记录缺失会自动创建，并同步修正参与状态。
     * 
     * @param participationId 参与记录ID
     * @param body 请求体，包含 nextNodeId（下一节点ID）和 isEnd（是否为结束节点）
     * @return 当前节点ID和是否已完成
     */
    @OperationLog(action = "学生实训下一步")
    @PostMapping("/training-tasks/{participationId}/next")
    public ApiResult<Map<String, Object>> next(@PathVariable Long participationId,
                                                @RequestBody Map<String, Object> body) {
        String nextNodeId = body.get("nextNodeId") != null ? body.get("nextNodeId").toString() : null;
        boolean isEnd = body.get("isEnd") != null && Boolean.TRUE.equals(body.get("isEnd"));

        if (nextNodeId == null || nextNodeId.isBlank()) {
            return ApiResult.error("缺少下一节点ID");
        }

        BizTrainingParticipation pt = participationService.getById(participationId);
        if (pt == null) return ApiResult.error("实训参与记录不存在");

        // 兜底：如果 state 记录不存在（之前因类型不匹配写入失败），自动创建
        WfStudentActivityState state = activityStateService.getOne(
                new LambdaQueryWrapper<WfStudentActivityState>()
                        .eq(WfStudentActivityState::getParticipationId, participationId));
        if (state == null) {
            log.warn("活动状态缺失，自动创建 participationId={}", participationId);
            state = new WfStudentActivityState();
            state.setParticipationId(participationId);
            state.setCurrentNodeId(nextNodeId);
            state.setUpdatedAt(LocalDateTime.now());
            activityStateService.save(state);

            // 同步修正 participation 状态（如果之前 start 只改了一半）
            if (pt.getStatus() == null || pt.getStatus() == 0) {
                pt.setStatus(1);
                pt.setUpdatedAt(LocalDateTime.now());
                participationService.updateById(pt);
            }

            if (isEnd) {
                pt.setStatus(2);
                pt.setUpdatedAt(LocalDateTime.now());
                participationService.updateById(pt);
                return ApiResult.ok(Map.of("completed", true, "currentNodeId", nextNodeId));
            }
            return ApiResult.ok(Map.of("currentNodeId", nextNodeId, "completed", false));
        }

        if (isEnd) {
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
