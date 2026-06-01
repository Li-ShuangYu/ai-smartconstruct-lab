package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartconstruct.backend_core.dto.*;
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

import java.time.LocalDateTime;
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
     * - 任务基本信息（名称、时间、过期状态）
     * - 学生参与状态
     * - 所有阶段列表（按sort_num排序），含解锁状态和节点进度统计
     * - 每个阶段内的节点实例列表及学生进度
     * - 当前位置信息（阶段ID、节点实例ID）
     *
     * 仅返回当前学生自身的进度数据，不暴露其他学生信息。
     *
     * @param taskId 实训任务ID
     * @return 实训总览数据（StudentTaskOverviewDTO）
     */
    @GetMapping("/tasks/{taskId}/overview")
    public ApiResult<StudentTaskOverviewDTO> getTaskOverview(@PathVariable Long taskId) {
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

        // 4. 解析phases_json（优先使用 phases_json，如果为空则从 raw_canvas_json 的 "phases" 数组解析）
        List<JsonNode> sortedPhases = parsePhasesJson(template.getPhasesJson());
        if (sortedPhases.isEmpty() && template.getRawCanvasJson() != null) {
            log.info("phases_json 为空，从 raw_canvas_json 解析 phases");
            try {
                JsonNode canvasNode;
                if (template.getRawCanvasJson() instanceof String) {
                    canvasNode = objectMapper.readTree((String) template.getRawCanvasJson());
                } else {
                    canvasNode = objectMapper.valueToTree(template.getRawCanvasJson());
                }
                JsonNode phasesNode = canvasNode.get("phases");
                if (phasesNode != null && phasesNode.isArray()) {
                    for (JsonNode node : phasesNode) { sortedPhases.add(node); }
                    sortedPhases.sort(Comparator.comparingInt(n ->
                            n.has("sort_num") ? n.get("sort_num").asInt(Integer.MAX_VALUE) : Integer.MAX_VALUE));
                }
            } catch (Exception e) {
                log.error("从 raw_canvas_json 解析 phases 失败: {}", e.getMessage());
            }
        }

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

        // 7. 获取阶段解锁状态
        List<PhaseUnlockStatus> phaseUnlockStatuses = phaseExecutionService.getPhaseUnlockStatuses(participation.getId());
        Map<String, PhaseUnlockStatus> unlockStatusMap = new HashMap<>();
        for (PhaseUnlockStatus pus : phaseUnlockStatuses) {
            unlockStatusMap.put(pus.getPhaseId(), pus);
        }

        // 8. 获取当前活动状态（当前节点）
        LambdaQueryWrapper<WfStudentActivityState> stateQuery = new LambdaQueryWrapper<>();
        stateQuery.eq(WfStudentActivityState::getParticipationId, participation.getId());
        WfStudentActivityState activityState = studentActivityStateMapper.selectOne(stateQuery);

        String currentNodeInstanceId = null;
        String currentPhaseId = null;
        if (activityState != null && activityState.getCurrentNodeId() != null) {
            currentNodeInstanceId = activityState.getCurrentNodeId();
            // 查找当前节点所属阶段
            try {
                WfNodeInstance currentNode = nodeInstanceMapper.selectById(Long.parseLong(currentNodeInstanceId));
                if (currentNode != null) {
                    currentPhaseId = currentNode.getPhaseId();
                }
            } catch (NumberFormatException e) {
                log.warn("无法解析当前节点实例ID: {}", currentNodeInstanceId);
            }
        }
        // 如果没有活动状态，使用当前解锁阶段
        if (currentPhaseId == null) {
            currentPhaseId = phaseExecutionService.getCurrentUnlockedPhaseId(taskId, currentUserId);
        }

        // 9. 构建阶段进度列表
        List<PhaseProgressDTO> phasesData = new ArrayList<>();
        for (JsonNode phase : sortedPhases) {
            String phaseId = phase.has("phase_id") ? phase.get("phase_id").asText() : "";
            String phaseName = phase.has("phase_name") ? phase.get("phase_name").asText() : "";
            int sortNum = phase.has("sort_num") ? phase.get("sort_num").asInt(0) : 0;

            // 从解锁状态映射获取
            PhaseUnlockStatus unlockStatus = unlockStatusMap.get(phaseId);
            boolean isUnlocked = unlockStatus != null ? Boolean.TRUE.equals(unlockStatus.getIsUnlocked()) : false;
            boolean isComplete = unlockStatus != null ? Boolean.TRUE.equals(unlockStatus.getIsComplete()) : false;

            // 获取该阶段的节点实例
            List<WfNodeInstance> phaseNodes = allNodeInstances.stream()
                    .filter(n -> phaseId.equals(n.getPhaseId()))
                    .collect(Collectors.toList());

            int totalNodes = phaseNodes.size();
            int completedNodes = 0;
            int requiredNodes = 0;
            int completedRequiredNodes = 0;

            List<NodeProgressDTO> nodesData = new ArrayList<>();
            for (WfNodeInstance node : phaseNodes) {
                boolean nodeIsRequired = parseIsRequired(node);
                if (nodeIsRequired) {
                    requiredNodes++;
                }

                NodeProgressDTO nodeDTO = new NodeProgressDTO();
                nodeDTO.setNodeInstanceId(String.valueOf(node.getId()));
                nodeDTO.setNodeId(node.getNodeDefId() != null ? String.valueOf(node.getNodeDefId()) : null);
                nodeDTO.setNodeType(node.getNodeType());
                nodeDTO.setNodeName(node.getNodeName());
                nodeDTO.setPhaseId(node.getPhaseId());
                nodeDTO.setSortNum(node.getSortNum());
                nodeDTO.setIsRequired(nodeIsRequired);

                // 学生进度
                WfStudentNodeProgress progress = progressMap.get(node.getId());
                if (progress != null) {
                    nodeDTO.setStatus(progress.getStatus());
                    nodeDTO.setEnteredAt(progress.getEnteredAt());
                    nodeDTO.setExitedAt(progress.getExitedAt());
                    nodeDTO.setStayDurationSeconds(progress.getStayDurationSeconds());

                    if (progress.getStatus() != null && progress.getStatus() == 2) {
                        completedNodes++;
                        if (nodeIsRequired) {
                            completedRequiredNodes++;
                        }
                    }
                } else {
                    nodeDTO.setStatus(0);
                }

                nodesData.add(nodeDTO);
            }

            PhaseProgressDTO phaseDTO = new PhaseProgressDTO();
            phaseDTO.setPhaseId(phaseId);
            phaseDTO.setPhaseName(phaseName);
            phaseDTO.setSortNum(sortNum);
            phaseDTO.setIsUnlocked(isUnlocked);
            phaseDTO.setIsComplete(isComplete);
            phaseDTO.setTotalNodes(totalNodes);
            phaseDTO.setCompletedNodes(completedNodes);
            phaseDTO.setRequiredNodes(requiredNodes);
            phaseDTO.setCompletedRequiredNodes(completedRequiredNodes);
            phaseDTO.setNodes(nodesData);

            phasesData.add(phaseDTO);
        }

        // 10. 构建总览DTO
        boolean isExpired = task.getEndTime() != null && LocalDateTime.now().isAfter(task.getEndTime());

        StudentTaskOverviewDTO overview = new StudentTaskOverviewDTO();
        overview.setTaskId(String.valueOf(taskId));
        overview.setTaskName(task.getTaskName());
        overview.setTemplateName(template.getTemplateName());
        overview.setStartTime(task.getStartTime());
        overview.setEndTime(task.getEndTime());
        overview.setIsExpired(isExpired);
        overview.setParticipationId(String.valueOf(participation.getId()));
        overview.setParticipationStatus(participation.getStatus());
        overview.setCurrentPhaseId(currentPhaseId);
        overview.setCurrentNodeInstanceId(currentNodeInstanceId);
        overview.setPhases(phasesData);

        return ApiResult.ok(overview);
    }

    // ==================== Step 2: POST /api/student/nodes/{nodeInstanceId}/enter ====================
    // 端点移至 StudentTrainingController，此处不再重复注册

    // ==================== Step 3: POST /api/student/nodes/{nodeInstanceId}/complete ====================
    // 端点移至 StudentTrainingController，此处不再重复注册

    // ==================== Step 4: GET /api/student/tasks/{taskId}/current-position ====================

    /**
     * 获取学生当前位置
     *
     * 返回学生当前所在的阶段和节点信息，用于断线重连或页面初始化时定位。
     * - 如果 Student_Activity_State 的 current_node_instance_id 不为 null，返回该节点位置
     * - 如果为 null 或记录不存在，返回当前解锁阶段的第一个未完成节点
     *
     * @param taskId 实训任务ID
     * @return 当前位置信息（CurrentPositionDTO）
     */
    @GetMapping("/tasks/{taskId}/current-position")
    public ApiResult<CurrentPositionDTO> getCurrentPosition(@PathVariable Long taskId) {
        Long currentUserId = getCurrentUserId();

        // 获取学生的参与记录
        BizTrainingParticipation participation = getParticipation(taskId, currentUserId);
        if (participation == null) {
            return ApiResult.error("未找到该实训任务的参与记录");
        }

        // 获取活动状态（当前所在节点）
        LambdaQueryWrapper<WfStudentActivityState> stateQuery = new LambdaQueryWrapper<>();
        stateQuery.eq(WfStudentActivityState::getParticipationId, participation.getId());
        WfStudentActivityState activityState = studentActivityStateMapper.selectOne(stateQuery);

        String currentNodeInstanceId = null;
        String currentPhaseId = null;
        String currentNodeType = null;
        String currentNodeName = null;

        if (activityState != null && activityState.getCurrentNodeId() != null) {
            // Requirement 7.6: 如果 current_node_instance_id 不为 null，定位到该节点
            currentNodeInstanceId = activityState.getCurrentNodeId();

            try {
                WfNodeInstance nodeInstance = nodeInstanceMapper.selectById(Long.parseLong(currentNodeInstanceId));
                if (nodeInstance != null) {
                    currentPhaseId = nodeInstance.getPhaseId();
                    currentNodeType = nodeInstance.getNodeType();
                    currentNodeName = nodeInstance.getNodeName();
                }
            } catch (NumberFormatException e) {
                log.warn("无法解析当前节点实例ID: {}", currentNodeInstanceId);
            }
        } else {
            // Requirement 7.7: 如果 current_node_instance_id 为 null 或记录不存在，
            // 定位到当前解锁阶段的第一个未完成节点
            currentPhaseId = phaseExecutionService.getCurrentUnlockedPhaseId(taskId, currentUserId);

            if (currentPhaseId != null) {
                // 获取该阶段的节点实例（按sort_num排序）
                LambdaQueryWrapper<WfNodeInstance> nodeQuery = new LambdaQueryWrapper<>();
                nodeQuery.eq(WfNodeInstance::getTaskId, taskId)
                        .eq(WfNodeInstance::getPhaseId, currentPhaseId)
                        .orderByAsc(WfNodeInstance::getSortNum);
                List<WfNodeInstance> phaseNodes = nodeInstanceMapper.selectList(nodeQuery);

                // 获取该学生的节点进度
                LambdaQueryWrapper<WfStudentNodeProgress> progressQuery = new LambdaQueryWrapper<>();
                progressQuery.eq(WfStudentNodeProgress::getParticipationId, participation.getId());
                List<WfStudentNodeProgress> allProgress = studentNodeProgressMapper.selectList(progressQuery);
                Map<Long, WfStudentNodeProgress> progressMap = new HashMap<>();
                for (WfStudentNodeProgress p : allProgress) {
                    progressMap.put(p.getNodeInstanceId(), p);
                }

                // 找到第一个未完成的节点
                for (WfNodeInstance node : phaseNodes) {
                    WfStudentNodeProgress progress = progressMap.get(node.getId());
                    int status = (progress != null && progress.getStatus() != null) ? progress.getStatus() : 0;
                    if (status != 2) { // 不是已完成
                        currentNodeInstanceId = String.valueOf(node.getId());
                        currentNodeType = node.getNodeType();
                        currentNodeName = node.getNodeName();
                        break;
                    }
                }
            }
        }

        CurrentPositionDTO position = new CurrentPositionDTO();
        position.setCurrentNodeInstanceId(currentNodeInstanceId);
        position.setPhaseId(currentPhaseId);
        position.setNodeType(currentNodeType);
        position.setNodeName(currentNodeName);

        return ApiResult.ok(position);
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
     * 解析节点的 is_required 字段
     *
     * 从节点的 configJson.orchestration_settings.is_required 中读取是否为必修节点。
     * 如果字段缺失或解析失败，默认视为必修（true）。
     *
     * @param node 节点实例
     * @return 是否为必修节点
     */
    private boolean parseIsRequired(WfNodeInstance node) {
        Object configJson = node.getConfigJson();
        if (configJson == null) {
            return true;
        }

        try {
            JsonNode configNode;
            if (configJson instanceof String) {
                configNode = objectMapper.readTree((String) configJson);
            } else {
                String jsonStr = objectMapper.writeValueAsString(configJson);
                configNode = objectMapper.readTree(jsonStr);
            }

            JsonNode orchestrationSettings = configNode.get("orchestration_settings");
            if (orchestrationSettings == null || !orchestrationSettings.isObject()) {
                return true;
            }

            JsonNode isRequiredNode = orchestrationSettings.get("is_required");
            if (isRequiredNode == null || !isRequiredNode.isBoolean()) {
                return true;
            }

            return isRequiredNode.asBoolean();
        } catch (Exception e) {
            log.warn("解析节点 {} 的 is_required 字段失败，默认视为必须完成: {}", node.getId(), e.getMessage());
            return true;
        }
    }
}
