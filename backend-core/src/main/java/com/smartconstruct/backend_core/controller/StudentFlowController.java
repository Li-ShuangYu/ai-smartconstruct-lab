package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.mapper.BizTrainingParticipationMapper;
import com.smartconstruct.backend_core.mapper.BizTrainingTaskMapper;
import com.smartconstruct.backend_core.mapper.WfNodeInstanceMapper;
import com.smartconstruct.backend_core.mapper.WfStudentActivityStateMapper;
import com.smartconstruct.backend_core.mapper.WfStudentNodeProgressMapper;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.IPhaseExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生实训流程控制器
 *
 * 提供学生端实训运行时的核心API：任务总览、节点进入/完成、当前位置查询。
 * 所有接口均基于当前登录学生的SecurityContext获取身份，仅返回该学生自身的数据，
 * 不暴露其他学生的进度信息。
 *
 * 活动状态追踪（wf_student_activity_state）由 PhaseExecutionService.enterNode/completeNode 自动维护。
 * 业务异常（PHASE_LOCKED, TASK_EXPIRED）由 PhaseExecutionService 抛出，
 * 由 GlobalExceptionHandler 统一捕获并返回对应HTTP状态码。
 *
 * @author SmartConstruct
 * @see IPhaseExecutionService
 */
@RestController
@RequestMapping("/api/student")
public class StudentFlowController {

    private static final Logger log = LoggerFactory.getLogger(StudentFlowController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final IPhaseExecutionService phaseExecutionService;
    private final BizTrainingParticipationMapper participationMapper;
    private final BizTrainingTaskMapper trainingTaskMapper;
    private final WfTrainingTemplateMapper trainingTemplateMapper;
    private final WfNodeInstanceMapper nodeInstanceMapper;
    private final WfStudentNodeProgressMapper studentNodeProgressMapper;
    private final WfStudentActivityStateMapper studentActivityStateMapper;

    public StudentFlowController(IPhaseExecutionService phaseExecutionService,
                                 BizTrainingParticipationMapper participationMapper,
                                 BizTrainingTaskMapper trainingTaskMapper,
                                 WfTrainingTemplateMapper trainingTemplateMapper,
                                 WfNodeInstanceMapper nodeInstanceMapper,
                                 WfStudentNodeProgressMapper studentNodeProgressMapper,
                                 WfStudentActivityStateMapper studentActivityStateMapper) {
        this.phaseExecutionService = phaseExecutionService;
        this.participationMapper = participationMapper;
        this.trainingTaskMapper = trainingTaskMapper;
        this.trainingTemplateMapper = trainingTemplateMapper;
        this.nodeInstanceMapper = nodeInstanceMapper;
        this.studentNodeProgressMapper = studentNodeProgressMapper;
        this.studentActivityStateMapper = studentActivityStateMapper;
    }

    /**
     * 获取当前登录学生的用户ID
     */
    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    /**
     * 根据taskId和当前学生ID查找参与记录
     *
     * @param taskId 实训任务ID
     * @return 参与记录，不存在则返回null
     */
    private BizTrainingParticipation getParticipation(Long taskId, Long studentId) {
        LambdaQueryWrapper<BizTrainingParticipation> qw = new LambdaQueryWrapper<>();
        qw.eq(BizTrainingParticipation::getTaskId, taskId)
                .eq(BizTrainingParticipation::getStudentId, studentId);
        return participationMapper.selectOne(qw);
    }

    // ==================== Step 1: GET /api/student/tasks/{taskId}/overview ====================

    /**
     * 获取实训任务总览
     *
     * 返回当前学生在指定实训任务中的完整视图：
     * - 所有阶段列表（按sort_num排序）
     * - 每个阶段内的节点实例列表
     * - 每个节点的学生进度状态
     * - 阶段解锁状态
     *
     * 仅返回当前学生自身的进度数据，不暴露其他学生信息。
     *
     * @param taskId 实训任务ID
     * @return 实训总览数据
     */
    @GetMapping("/tasks/{taskId}/overview")
    public ApiResult<Map<String, Object>> getTaskOverview(@PathVariable Long taskId) {
        Long currentUserId = getCurrentUserId();

        // 1. 获取学生的参与记录
        BizTrainingParticipation participation = getParticipation(taskId, currentUserId);
        if (participation == null) {
            return ApiResult.error("未找到该实训任务的参与记录");
        }

        // 2. 获取实训任务信息
        BizTrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            return ApiResult.error("实训任务不存在");
        }

        // 3. 获取模板信息（含phases_json）
        WfTrainingTemplate template = trainingTemplateMapper.selectById(task.getTemplateId());
        if (template == null) {
            return ApiResult.error("实训模板不存在");
        }

        // 4. 解析phases_json
        List<JsonNode> sortedPhases = parsePhasesJson(template.getPhasesJson());

        // 5. 获取该任务的所有节点实例
        LambdaQueryWrapper<WfNodeInstance> nodeQuery = new LambdaQueryWrapper<>();
        nodeQuery.eq(WfNodeInstance::getTaskId, taskId)
                .orderByAsc(WfNodeInstance::getSortNum);
        List<WfNodeInstance> allNodeInstances = nodeInstanceMapper.selectList(nodeQuery);

        // 6. 获取该学生在此任务中的所有节点进度
        LambdaQueryWrapper<WfStudentNodeProgress> progressQuery = new LambdaQueryWrapper<>();
        progressQuery.eq(WfStudentNodeProgress::getParticipationId, participation.getId());
        List<WfStudentNodeProgress> allProgress = studentNodeProgressMapper.selectList(progressQuery);

        // 构建 nodeInstanceId -> progress 映射
        Map<Long, WfStudentNodeProgress> progressMap = new HashMap<>();
        for (WfStudentNodeProgress p : allProgress) {
            progressMap.put(p.getNodeInstanceId(), p);
        }

        // 7. 获取当前解锁阶段
        String currentUnlockedPhaseId = phaseExecutionService.getCurrentUnlockedPhaseId(taskId, currentUserId);

        // 8. 构建响应数据
        List<Map<String, Object>> phasesData = new ArrayList<>();
        for (JsonNode phase : sortedPhases) {
            String phaseId = phase.has("phase_id") ? phase.get("phase_id").asText() : "";
            String phaseName = phase.has("phase_name") ? phase.get("phase_name").asText() : "";
            int sortNum = phase.has("sort_num") ? phase.get("sort_num").asInt(0) : 0;

            // 判断阶段是否已解锁
            boolean isUnlocked = isPhaseUnlocked(phaseId, currentUnlockedPhaseId, sortedPhases);
            boolean isComplete = phaseExecutionService.isPhaseComplete(taskId, phaseId, currentUserId);

            // 获取该阶段的节点实例
            List<WfNodeInstance> phaseNodes = allNodeInstances.stream()
                    .filter(n -> phaseId.equals(n.getPhaseId()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> nodesData = new ArrayList<>();
            for (WfNodeInstance node : phaseNodes) {
                Map<String, Object> nodeData = new HashMap<>();
                nodeData.put("nodeInstanceId", String.valueOf(node.getId()));
                nodeData.put("nodeType", node.getNodeType());
                nodeData.put("nodeName", node.getNodeName());
                nodeData.put("sortNum", node.getSortNum());

                // 学生进度
                WfStudentNodeProgress progress = progressMap.get(node.getId());
                if (progress != null) {
                    nodeData.put("status", progress.getStatus());
                    nodeData.put("enteredAt", progress.getEnteredAt());
                    nodeData.put("exitedAt", progress.getExitedAt());
                    nodeData.put("stayDurationSeconds", progress.getStayDurationSeconds());
                } else {
                    nodeData.put("status", 0); // 未开始
                    nodeData.put("enteredAt", null);
                    nodeData.put("exitedAt", null);
                    nodeData.put("stayDurationSeconds", null);
                }

                nodesData.add(nodeData);
            }

            Map<String, Object> phaseData = new HashMap<>();
            phaseData.put("phaseId", phaseId);
            phaseData.put("phaseName", phaseName);
            phaseData.put("sortNum", sortNum);
            phaseData.put("isUnlocked", isUnlocked);
            phaseData.put("isComplete", isComplete);
            phaseData.put("nodes", nodesData);

            // 可选时间字段
            if (phase.has("plan_start_time") && !phase.get("plan_start_time").isNull()) {
                phaseData.put("planStartTime", phase.get("plan_start_time").asText());
            }
            if (phase.has("plan_end_time") && !phase.get("plan_end_time").isNull()) {
                phaseData.put("planEndTime", phase.get("plan_end_time").asText());
            }

            phasesData.add(phaseData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", String.valueOf(taskId));
        result.put("taskName", task.getTaskName());
        result.put("startTime", task.getStartTime());
        result.put("endTime", task.getEndTime());
        result.put("participationId", String.valueOf(participation.getId()));
        result.put("participationStatus", participation.getStatus());
        result.put("currentUnlockedPhaseId", currentUnlockedPhaseId);
        result.put("phases", phasesData);

        return ApiResult.ok(result);
    }

    // ==================== Step 2: POST /api/student/nodes/{nodeInstanceId}/enter ====================

    /**
     * 学生进入节点
     *
     * 调用 PhaseExecutionService.enterNode 创建或恢复进度记录。
     * PhaseExecutionService 内部会：
     * - 检查阶段锁定（PHASE_LOCKED）
     * - 检查任务过期（TASK_EXPIRED）
     * - 更新 wf_student_activity_state（活动状态追踪）
     *
     * @param nodeInstanceId 节点实例ID
     * @return 节点进度记录
     */
    @PostMapping("/nodes/{nodeInstanceId}/enter")
    public ApiResult<WfStudentNodeProgress> enterNode(@PathVariable Long nodeInstanceId) {
        Long currentUserId = getCurrentUserId();

        // 查找节点实例获取taskId
        WfNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
        if (nodeInstance == null) {
            return ApiResult.error("节点实例不存在");
        }

        // 获取学生的参与记录
        BizTrainingParticipation participation = getParticipation(nodeInstance.getTaskId(), currentUserId);
        if (participation == null) {
            return ApiResult.error("未找到该实训任务的参与记录");
        }

        // 调用PhaseExecutionService.enterNode（内部处理阶段锁定、过期检查、活动状态更新）
        WfStudentNodeProgress progress = phaseExecutionService.enterNode(participation.getId(), nodeInstanceId);

        return ApiResult.ok(progress);
    }

    // ==================== Step 3: POST /api/student/nodes/{nodeInstanceId}/complete ====================

    /**
     * 学生完成节点
     *
     * 调用 PhaseExecutionService.completeNode 将节点标记为已完成。
     * PhaseExecutionService 内部会：
     * - 设置 status=2, exited_at, stay_duration_seconds
     * - 清除 wf_student_activity_state.current_node_instance_id（活动状态追踪）
     * - 自动检查阶段推进
     *
     * @param nodeInstanceId 节点实例ID
     * @return 操作结果
     */
    @PostMapping("/nodes/{nodeInstanceId}/complete")
    public ApiResult<Void> completeNode(@PathVariable Long nodeInstanceId) {
        Long currentUserId = getCurrentUserId();

        // 查找节点实例获取taskId
        WfNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
        if (nodeInstance == null) {
            return ApiResult.error("节点实例不存在");
        }

        // 获取学生的参与记录
        BizTrainingParticipation participation = getParticipation(nodeInstance.getTaskId(), currentUserId);
        if (participation == null) {
            return ApiResult.error("未找到该实训任务的参与记录");
        }

        // 调用PhaseExecutionService.completeNode（内部处理活动状态更新）
        phaseExecutionService.completeNode(participation.getId(), nodeInstanceId);

        return ApiResult.ok();
    }

    // ==================== Step 4: GET /api/student/tasks/{taskId}/current-position ====================

    /**
     * 获取学生当前位置
     *
     * 返回学生当前所在的阶段和节点信息，用于断线重连或页面初始化时定位。
     *
     * @param taskId 实训任务ID
     * @return 当前阶段ID和节点实例ID
     */
    @GetMapping("/tasks/{taskId}/current-position")
    public ApiResult<Map<String, Object>> getCurrentPosition(@PathVariable Long taskId) {
        Long currentUserId = getCurrentUserId();

        // 获取学生的参与记录
        BizTrainingParticipation participation = getParticipation(taskId, currentUserId);
        if (participation == null) {
            return ApiResult.error("未找到该实训任务的参与记录");
        }

        // 获取当前解锁阶段
        String currentPhaseId = phaseExecutionService.getCurrentUnlockedPhaseId(taskId, currentUserId);

        // 获取活动状态（当前所在节点）
        LambdaQueryWrapper<WfStudentActivityState> stateQuery = new LambdaQueryWrapper<>();
        stateQuery.eq(WfStudentActivityState::getParticipationId, participation.getId());
        WfStudentActivityState activityState = studentActivityStateMapper.selectOne(stateQuery);

        String currentNodeInstanceId = null;
        String currentNodeType = null;
        String currentNodeName = null;

        if (activityState != null && activityState.getCurrentNodeId() != null) {
            currentNodeInstanceId = activityState.getCurrentNodeId();

            // 获取节点实例详情
            try {
                WfNodeInstance nodeInstance = nodeInstanceMapper.selectById(Long.parseLong(currentNodeInstanceId));
                if (nodeInstance != null) {
                    currentNodeType = nodeInstance.getNodeType();
                    currentNodeName = nodeInstance.getNodeName();
                }
            } catch (NumberFormatException e) {
                log.warn("无法解析当前节点实例ID: {}", currentNodeInstanceId);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", String.valueOf(taskId));
        result.put("participationId", String.valueOf(participation.getId()));
        result.put("currentPhaseId", currentPhaseId);
        result.put("currentNodeInstanceId", currentNodeInstanceId);
        result.put("currentNodeType", currentNodeType);
        result.put("currentNodeName", currentNodeName);
        result.put("participationStatus", participation.getStatus());

        return ApiResult.ok(result);
    }

    // ==================== Helper Methods ====================

    /**
     * 解析 phases_json 为排序后的 JsonNode 列表
     */
    private List<JsonNode> parsePhasesJson(Object phasesJson) {
        if (phasesJson == null) {
            return new ArrayList<>();
        }

        try {
            JsonNode arrayNode;
            if (phasesJson instanceof String) {
                arrayNode = objectMapper.readTree((String) phasesJson);
            } else {
                String jsonStr = objectMapper.writeValueAsString(phasesJson);
                arrayNode = objectMapper.readTree(jsonStr);
            }

            if (!arrayNode.isArray()) {
                return new ArrayList<>();
            }

            List<JsonNode> phases = new ArrayList<>();
            for (JsonNode node : arrayNode) {
                phases.add(node);
            }

            // 按 sort_num 升序排序
            phases.sort(Comparator.comparingInt(node ->
                    node.has("sort_num") ? node.get("sort_num").asInt(Integer.MAX_VALUE) : Integer.MAX_VALUE
            ));

            return phases;
        } catch (Exception e) {
            log.error("解析 phases_json 失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 判断指定阶段是否已解锁
     *
     * 逻辑：阶段的sort_num <= 当前解锁阶段的sort_num 则视为已解锁
     *
     * @param phaseId              要判断的阶段ID
     * @param currentUnlockedPhaseId 当前解锁的阶段ID
     * @param sortedPhases         按sort_num排序的阶段列表
     * @return 是否已解锁
     */
    private boolean isPhaseUnlocked(String phaseId, String currentUnlockedPhaseId, List<JsonNode> sortedPhases) {
        if (currentUnlockedPhaseId == null) {
            // 无法确定解锁阶段，默认第一个阶段解锁
            return sortedPhases.size() > 0 && phaseId.equals(
                    sortedPhases.get(0).has("phase_id") ? sortedPhases.get(0).get("phase_id").asText() : "");
        }

        int targetSortNum = Integer.MAX_VALUE;
        int unlockedSortNum = Integer.MAX_VALUE;

        for (JsonNode phase : sortedPhases) {
            String id = phase.has("phase_id") ? phase.get("phase_id").asText() : "";
            int sortNum = phase.has("sort_num") ? phase.get("sort_num").asInt(Integer.MAX_VALUE) : Integer.MAX_VALUE;

            if (phaseId.equals(id)) {
                targetSortNum = sortNum;
            }
            if (currentUnlockedPhaseId.equals(id)) {
                unlockedSortNum = sortNum;
            }
        }

        return targetSortNum <= unlockedSortNum;
    }
}
