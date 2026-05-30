package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.ParticipationDetailVO;
import com.smartconstruct.backend_core.dto.PhaseUnlockStatus;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.exception.BusinessException;
import com.smartconstruct.backend_core.mapper.WfNodeInstanceMapper;
import com.smartconstruct.backend_core.mapper.WfStudentNodeProgressMapper;
import com.smartconstruct.backend_core.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

import com.smartconstruct.backend_core.util.Java8Compat;

/**
 * 学生实训控制器
 * 
 * 负责处理学生端实训相关的所有API请求，包括：
 * - 获取实训参与详情
 * - 开始实训
 * - 推进实训节点（下一节点）
 * - 进入/完成节点（基于 IPhaseExecutionService）
 * - 获取节点内容
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

    /** 阶段执行服务 - 管理节点进入/完成和阶段解锁 */
    private final IPhaseExecutionService phaseExecutionService;

    /** 节点实例 Mapper */
    private final WfNodeInstanceMapper nodeInstanceMapper;

    /** 学生节点进度 Mapper */
    private final WfStudentNodeProgressMapper studentNodeProgressMapper;

    public StudentTrainingController(ITrainingParticipationService participationService,
                                      ITrainingTaskService trainingTaskService,
                                      ITrainingTemplateService templateService,
                                      IStudentActivityStateService activityStateService,
                                      IPhaseExecutionService phaseExecutionService,
                                      WfNodeInstanceMapper nodeInstanceMapper,
                                      WfStudentNodeProgressMapper studentNodeProgressMapper) {
        this.participationService = participationService;
        this.trainingTaskService = trainingTaskService;
        this.templateService = templateService;
        this.activityStateService = activityStateService;
        this.phaseExecutionService = phaseExecutionService;
        this.nodeInstanceMapper = nodeInstanceMapper;
        this.studentNodeProgressMapper = studentNodeProgressMapper;
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
        if (startNodeId == null || Java8Compat.isBlank(startNodeId)) {
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

        return ApiResult.ok(Java8Compat.mapOf("currentNodeId", startNodeId));
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

        if (nextNodeId == null || Java8Compat.isBlank(nextNodeId)) {
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
                return ApiResult.ok(Java8Compat.mapOf("completed", true, "currentNodeId", nextNodeId));
            }
            return ApiResult.ok(Java8Compat.mapOf("currentNodeId", nextNodeId, "completed", false));
        }

        if (isEnd) {
            pt.setStatus(2);
            pt.setUpdatedAt(LocalDateTime.now());
            participationService.updateById(pt);
            return ApiResult.ok(Java8Compat.mapOf("completed", true, "currentNodeId", nextNodeId));
        }

        state.setCurrentNodeId(nextNodeId);
        state.setUpdatedAt(LocalDateTime.now());
        activityStateService.updateById(state);

        return ApiResult.ok(Java8Compat.mapOf("currentNodeId", nextNodeId, "completed", false));
    }

    // ==================== 新版实训流转接口（基于 IPhaseExecutionService） ====================

    /**
     * 开始实训（新版，基于 taskId）
     *
     * 根据当前学生和 taskId 查找 Participation 记录：
     * - status=0（未开始）：更新为 1（进行中），创建 Student_Activity_State，返回第一个节点位置
     * - status=1（进行中）：直接恢复到上次中断的位置
     * - status=2（已完成）：返回完成总结信息
     *
     * @param taskId 实训任务ID
     * @return 开始结果（含当前位置或完成总结）
     */
    @OperationLog(action = "学生开始实训(新版)")
    @PostMapping("/tasks/{taskId}/start")
    public ApiResult<Map<String, Object>> startTraining(@PathVariable Long taskId) {
        Long studentId = getCurrentUserId();

        // 1. 查找学生的 Participation 记录
        BizTrainingParticipation participation = participationService.getOne(
                new LambdaQueryWrapper<BizTrainingParticipation>()
                        .eq(BizTrainingParticipation::getTaskId, taskId)
                        .eq(BizTrainingParticipation::getStudentId, studentId));
        if (participation == null) {
            return ApiResult.error(404, "PARTICIPATION_NOT_FOUND", "您未参与该实训任务");
        }

        Integer status = participation.getStatus();

        // 2. status=2（已完成）：返回完成总结
        if (status != null && status == 2) {
            return buildCompletionSummary(participation);
        }

        // 3. status=1（进行中）：恢复位置
        if (status != null && status == 1) {
            return buildResumePosition(participation);
        }

        // 4. status=0（未开始）：开始实训
        LocalDateTime now = LocalDateTime.now();
        participation.setStatus(1);
        participation.setUpdatedAt(now);
        participationService.updateById(participation);

        // 创建 Student_Activity_State（current_node_instance_id 初始为 null）
        WfStudentActivityState existingState = activityStateService.getOne(
                new LambdaQueryWrapper<WfStudentActivityState>()
                        .eq(WfStudentActivityState::getParticipationId, participation.getId()));
        if (existingState == null) {
            WfStudentActivityState newState = new WfStudentActivityState();
            newState.setParticipationId(participation.getId());
            newState.setCurrentNodeId(null);
            newState.setCreatedAt(now);
            newState.setUpdatedAt(now);
            activityStateService.save(newState);
        }

        // 返回第一个可进入的阶段和节点
        String currentPhaseId = phaseExecutionService.getCurrentUnlockedPhaseId(taskId, studentId);
        Long firstNodeInstanceId = findFirstIncompleteNodeInPhase(participation.getId(), taskId, currentPhaseId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("participationId", String.valueOf(participation.getId()));
        result.put("status", 1);
        result.put("currentPhaseId", currentPhaseId);
        result.put("currentNodeInstanceId", firstNodeInstanceId != null ? String.valueOf(firstNodeInstanceId) : null);
        result.put("message", "实训已开始");

        return ApiResult.ok(result);
    }

    /**
     * 进入节点
     *
     * 调用 IPhaseExecutionService.enterNode() 处理节点进入逻辑：
     * - status=0 或 1 的节点：设置 status=1，记录 entered_at，更新 Activity_State
     * - status=2 或 3 的节点（已完成/已跳过）：不修改记录，直接返回现有状态
     *
     * @param nodeInstanceId 节点实例ID
     * @return 节点进度信息
     */
    @OperationLog(action = "学生进入节点")
    @PostMapping("/nodes/{nodeInstanceId}/enter")
    public ApiResult<Map<String, Object>> enterNode(@PathVariable Long nodeInstanceId) {
        Long studentId = getCurrentUserId();

        // 查找学生对应的 Participation
        Long participationId = findParticipationIdByNodeInstance(nodeInstanceId, studentId);
        if (participationId == null) {
            return ApiResult.error(404, "PARTICIPATION_NOT_FOUND", "未找到您的实训参与记录");
        }

        try {
            WfStudentNodeProgress progress = phaseExecutionService.enterNode(participationId, nodeInstanceId);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("nodeInstanceId", String.valueOf(nodeInstanceId));
            result.put("status", progress.getStatus());
            result.put("enteredAt", progress.getEnteredAt() != null ? progress.getEnteredAt().toString() : null);
            result.put("participationId", String.valueOf(participationId));

            return ApiResult.ok(result);
        } catch (BusinessException e) {
            return ApiResult.error(e.getHttpStatus(), e.getErrorCode(), e.getMessage());
        }
    }

    /**
     * 完成节点
     *
     * 调用 IPhaseExecutionService.completeNode() 处理节点完成逻辑：
     * - 更新 Node_Progress status=2，计算 stay_duration_seconds
     * - 将 Activity_State 的 current_node_instance_id 设为 null
     * - 调用 checkAndUnlockNextPhase 检查阶段解锁
     * - 检查是否所有必修节点完成（Participation status 0→2）
     *
     * @param nodeInstanceId 节点实例ID
     * @return 完成结果
     */
    @OperationLog(action = "学生完成节点")
    @PostMapping("/nodes/{nodeInstanceId}/complete")
    public ApiResult<Map<String, Object>> completeNode(@PathVariable Long nodeInstanceId) {
        Long studentId = getCurrentUserId();

        // 查找学生对应的 Participation
        Long participationId = findParticipationIdByNodeInstance(nodeInstanceId, studentId);
        if (participationId == null) {
            return ApiResult.error(404, "PARTICIPATION_NOT_FOUND", "未找到您的实训参与记录");
        }

        try {
            phaseExecutionService.completeNode(participationId, nodeInstanceId);

            // 检查是否所有必修节点已完成 → 更新 Participation status=2
            BizTrainingParticipation participation = participationService.getById(participationId);
            boolean allComplete = checkAllRequiredNodesComplete(participation);
            if (allComplete && participation.getStatus() != null && participation.getStatus() == 1) {
                participation.setStatus(2);
                participation.setUpdatedAt(LocalDateTime.now());
                participationService.updateById(participation);
            }

            // 查询更新后的进度记录
            WfStudentNodeProgress progress = studentNodeProgressMapper.selectOne(
                    new LambdaQueryWrapper<WfStudentNodeProgress>()
                            .eq(WfStudentNodeProgress::getParticipationId, participationId)
                            .eq(WfStudentNodeProgress::getNodeInstanceId, nodeInstanceId));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("nodeInstanceId", String.valueOf(nodeInstanceId));
            result.put("status", progress != null ? progress.getStatus() : 2);
            result.put("stayDurationSeconds", progress != null ? progress.getStayDurationSeconds() : null);
            result.put("allComplete", allComplete);
            result.put("participationStatus", participation.getStatus());

            return ApiResult.ok(result);
        } catch (BusinessException e) {
            return ApiResult.error(e.getHttpStatus(), e.getErrorCode(), e.getMessage());
        }
    }

    /**
     * 获取节点内容
     *
     * 返回节点实例的 config_json（包含模拟AI生成的内容数据）。
     *
     * @param nodeInstanceId 节点实例ID
     * @return 节点配置内容
     */
    @GetMapping("/nodes/{nodeInstanceId}/content")
    public ApiResult<Map<String, Object>> getNodeContent(@PathVariable Long nodeInstanceId) {
        WfNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
        if (nodeInstance == null) {
            return ApiResult.error(404, "NODE_NOT_FOUND", "节点实例不存在");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nodeInstanceId", String.valueOf(nodeInstance.getId()));
        result.put("nodeType", nodeInstance.getNodeType());
        result.put("nodeName", nodeInstance.getNodeName());
        result.put("node_config", nodeInstance.getConfigJson());

        return ApiResult.ok(result);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据节点实例ID和学生ID查找对应的 participationId
     */
    private Long findParticipationIdByNodeInstance(Long nodeInstanceId, Long studentId) {
        WfNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
        if (nodeInstance == null || nodeInstance.getTaskId() == null) {
            return null;
        }

        BizTrainingParticipation participation = participationService.getOne(
                new LambdaQueryWrapper<BizTrainingParticipation>()
                        .eq(BizTrainingParticipation::getTaskId, nodeInstance.getTaskId())
                        .eq(BizTrainingParticipation::getStudentId, studentId));
        return participation != null ? participation.getId() : null;
    }

    /**
     * 构建恢复位置响应（status=1 时调用）
     *
     * 如果 Activity_State 的 current_node_instance_id 不为 null，恢复到该节点；
     * 否则定位到当前解锁阶段的第一个未完成节点。
     */
    private ApiResult<Map<String, Object>> buildResumePosition(BizTrainingParticipation participation) {
        Long participationId = participation.getId();
        Long taskId = participation.getTaskId();
        Long studentId = participation.getStudentId();

        // 查询 Activity_State
        WfStudentActivityState state = activityStateService.getOne(
                new LambdaQueryWrapper<WfStudentActivityState>()
                        .eq(WfStudentActivityState::getParticipationId, participationId));

        String currentNodeInstanceId = null;
        String currentPhaseId = null;

        if (state != null && state.getCurrentNodeId() != null && !state.getCurrentNodeId().isEmpty()) {
            // 恢复到 Activity_State 记录的节点
            currentNodeInstanceId = state.getCurrentNodeId();
            // 查找该节点所属阶段
            try {
                WfNodeInstance nodeInstance = nodeInstanceMapper.selectById(Long.valueOf(currentNodeInstanceId));
                if (nodeInstance != null) {
                    currentPhaseId = nodeInstance.getPhaseId();
                }
            } catch (NumberFormatException e) {
                log.warn("无法解析 currentNodeId 为 Long: {}", currentNodeInstanceId);
            }
        }

        if (currentNodeInstanceId == null) {
            // 定位到当前解锁阶段的第一个未完成节点
            currentPhaseId = phaseExecutionService.getCurrentUnlockedPhaseId(taskId, studentId);
            Long firstNode = findFirstIncompleteNodeInPhase(participationId, taskId, currentPhaseId);
            currentNodeInstanceId = firstNode != null ? String.valueOf(firstNode) : null;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("participationId", String.valueOf(participationId));
        result.put("status", 1);
        result.put("currentPhaseId", currentPhaseId);
        result.put("currentNodeInstanceId", currentNodeInstanceId);
        result.put("message", "实训进行中，已恢复到上次位置");

        return ApiResult.ok(result);
    }

    /**
     * 构建完成总结响应（status=2 时调用）
     */
    private ApiResult<Map<String, Object>> buildCompletionSummary(BizTrainingParticipation participation) {
        Long participationId = participation.getId();

        // 获取阶段完成状态
        List<PhaseUnlockStatus> phaseStatuses = phaseExecutionService.getPhaseUnlockStatuses(participationId);

        // 计算进度统计
        long totalNodes = studentNodeProgressMapper.selectCount(
                new LambdaQueryWrapper<WfStudentNodeProgress>()
                        .eq(WfStudentNodeProgress::getParticipationId, participationId));
        long completedNodes = studentNodeProgressMapper.selectCount(
                new LambdaQueryWrapper<WfStudentNodeProgress>()
                        .eq(WfStudentNodeProgress::getParticipationId, participationId)
                        .eq(WfStudentNodeProgress::getStatus, 2));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("participationId", String.valueOf(participationId));
        result.put("status", 2);
        result.put("totalScore", participation.getTotalScore());
        result.put("totalNodes", totalNodes);
        result.put("completedNodes", completedNodes);
        result.put("phases", phaseStatuses);
        result.put("message", "实训已完成");

        return ApiResult.ok(result);
    }

    /**
     * 查找指定阶段内第一个未完成的节点实例ID
     */
    private Long findFirstIncompleteNodeInPhase(Long participationId, Long taskId, String phaseId) {
        if (phaseId == null) {
            return null;
        }

        // 查询该阶段内所有节点实例，按 sort_num 升序
        List<WfNodeInstance> nodes = nodeInstanceMapper.selectList(
                new LambdaQueryWrapper<WfNodeInstance>()
                        .eq(WfNodeInstance::getTaskId, taskId)
                        .eq(WfNodeInstance::getPhaseId, phaseId)
                        .orderByAsc(WfNodeInstance::getSortNum));

        for (WfNodeInstance node : nodes) {
            // 查询该节点的进度
            WfStudentNodeProgress progress = studentNodeProgressMapper.selectOne(
                    new LambdaQueryWrapper<WfStudentNodeProgress>()
                            .eq(WfStudentNodeProgress::getParticipationId, participationId)
                            .eq(WfStudentNodeProgress::getNodeInstanceId, node.getId()));

            // 如果没有进度记录或状态不是已完成(2)，则为第一个未完成节点
            if (progress == null || (progress.getStatus() != null && progress.getStatus() != 2)) {
                return node.getId();
            }
        }

        // 所有节点都已完成
        return nodes.isEmpty() ? null : nodes.get(0).getId();
    }

    /**
     * 检查是否所有必修节点都已完成
     */
    private boolean checkAllRequiredNodesComplete(BizTrainingParticipation participation) {
        Long taskId = participation.getTaskId();
        Long studentId = participation.getStudentId();

        // 获取所有阶段的解锁状态
        List<PhaseUnlockStatus> phaseStatuses = phaseExecutionService.getPhaseUnlockStatuses(participation.getId());

        // 如果所有阶段都已完成，则所有必修节点已完成
        for (PhaseUnlockStatus phaseStatus : phaseStatuses) {
            if (phaseStatus.getIsComplete() == null || !phaseStatus.getIsComplete()) {
                return false;
            }
        }
        return !phaseStatuses.isEmpty();
    }
}
