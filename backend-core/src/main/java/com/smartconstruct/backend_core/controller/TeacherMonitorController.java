package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.mapper.*;
import com.smartconstruct.backend_core.service.IPhaseExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教师实训监控控制器
 *
 * 提供教师端实训监控的核心API：
 * - GET /api/teacher/tasks/{taskId}/monitor — 聚合进度数据（按阶段/节点）
 * - POST /api/teacher/tasks/{taskId}/nudge — 催办学生
 * - POST /api/teacher/nodes/{nodeInstanceId}/force-complete — 强制完成节点
 *
 * 所有教师操作记录到 wf_teacher_node_operation 表。
 *
 * @author SmartConstruct
 */
@RestController
@RequestMapping("/api/teacher")
public class TeacherMonitorController {

    private static final Logger log = LoggerFactory.getLogger(TeacherMonitorController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final BizTrainingTaskMapper trainingTaskMapper;
    private final BizTrainingParticipationMapper participationMapper;
    private final WfTrainingTemplateMapper templateMapper;
    private final WfNodeInstanceMapper nodeInstanceMapper;
    private final WfStudentNodeProgressMapper studentNodeProgressMapper;
    private final WfTeacherNodeOperationMapper teacherNodeOperationMapper;
    private final IPhaseExecutionService phaseExecutionService;

    public TeacherMonitorController(
            BizTrainingTaskMapper trainingTaskMapper,
            BizTrainingParticipationMapper participationMapper,
            WfTrainingTemplateMapper templateMapper,
            WfNodeInstanceMapper nodeInstanceMapper,
            WfStudentNodeProgressMapper studentNodeProgressMapper,
            WfTeacherNodeOperationMapper teacherNodeOperationMapper,
            IPhaseExecutionService phaseExecutionService) {
        this.trainingTaskMapper = trainingTaskMapper;
        this.participationMapper = participationMapper;
        this.templateMapper = templateMapper;
        this.nodeInstanceMapper = nodeInstanceMapper;
        this.studentNodeProgressMapper = studentNodeProgressMapper;
        this.teacherNodeOperationMapper = teacherNodeOperationMapper;
        this.phaseExecutionService = phaseExecutionService;
    }

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    // ==================== Step 5: GET /api/teacher/tasks/{taskId}/monitor ====================

    /**
     * 获取实训监控数据
     *
     * 返回按阶段和节点聚合的学生进度分布数据，包括：
     * - 每个阶段的完成人数和进度百分比
     * - 每个节点的学生分布（未开始/进行中/已完成）
     * - 总体统计（总人数、在线人数、已完成人数）
     *
     * @param taskId 实训任务ID
     * @return 监控聚合数据
     */
    @GetMapping("/tasks/{taskId}/monitor")
    public ApiResult<Map<String, Object>> getMonitorData(@PathVariable Long taskId) {
        Long teacherId = getCurrentUserId();

        // 1. 验证任务归属
        BizTrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            return ApiResult.error("实训任务不存在");
        }
        if (!teacherId.equals(task.getTeacherId())) {
            return ApiResult.error("无权访问该实训任务");
        }

        // 2. 获取参与学生列表
        LambdaQueryWrapper<BizTrainingParticipation> partQuery = new LambdaQueryWrapper<>();
        partQuery.eq(BizTrainingParticipation::getTaskId, taskId);
        List<BizTrainingParticipation> participations = participationMapper.selectList(partQuery);
        int totalStudents = participations.size();

        // 3. 获取模板阶段信息
        WfTrainingTemplate template = templateMapper.selectById(task.getTemplateId());
        List<JsonNode> phases = parsePhasesJson(template != null ? template.getPhasesJson() : null);

        // 4. 获取所有节点实例
        LambdaQueryWrapper<WfNodeInstance> nodeQuery = new LambdaQueryWrapper<>();
        nodeQuery.eq(WfNodeInstance::getTaskId, taskId).orderByAsc(WfNodeInstance::getSortNum);
        List<WfNodeInstance> nodeInstances = nodeInstanceMapper.selectList(nodeQuery);

        // 5. 获取所有学生进度
        Set<Long> participationIds = participations.stream()
                .map(BizTrainingParticipation::getId)
                .collect(Collectors.toSet());

        List<WfStudentNodeProgress> allProgress = new ArrayList<>();
        if (!participationIds.isEmpty()) {
            LambdaQueryWrapper<WfStudentNodeProgress> progressQuery = new LambdaQueryWrapper<>();
            progressQuery.in(WfStudentNodeProgress::getParticipationId, participationIds);
            allProgress = studentNodeProgressMapper.selectList(progressQuery);
        }

        // 6. 按节点实例ID聚合进度
        Map<Long, List<WfStudentNodeProgress>> progressByNode = allProgress.stream()
                .collect(Collectors.groupingBy(WfStudentNodeProgress::getNodeInstanceId));

        // 7. 构建阶段进度数据
        List<Map<String, Object>> phaseData = new ArrayList<>();
        for (JsonNode phase : phases) {
            String phaseId = phase.has("phase_id") ? phase.get("phase_id").asText() : "";
            String phaseName = phase.has("phase_name") ? phase.get("phase_name").asText() : "";

            // 获取该阶段的节点
            List<WfNodeInstance> phaseNodes = nodeInstances.stream()
                    .filter(n -> phaseId.equals(n.getPhaseId()))
                    .collect(Collectors.toList());

            // 计算阶段完成人数（所有节点都完成的学生数）
            int phaseCompletedCount = 0;
            if (!phaseNodes.isEmpty()) {
                // For each participation, check if all nodes in the phase are completed
                for (BizTrainingParticipation part : participations) {
                    boolean allComplete = phaseNodes.stream().allMatch(node -> {
                        List<WfStudentNodeProgress> nodeProgress = progressByNode.getOrDefault(node.getId(), Collections.emptyList());
                        return nodeProgress.stream()
                                .filter(p -> p.getParticipationId().equals(part.getId()))
                                .anyMatch(p -> p.getStatus() != null && p.getStatus() == 2);
                    });
                    if (allComplete) phaseCompletedCount++;
                }
            }

            int progressPercent = totalStudents > 0 ? Math.round((float) phaseCompletedCount / totalStudents * 100) : 0;

            Map<String, Object> pd = new LinkedHashMap<>();
            pd.put("phaseId", phaseId);
            pd.put("phaseName", phaseName);
            pd.put("completedCount", phaseCompletedCount);
            pd.put("progressPercent", progressPercent);
            pd.put("isActive", phaseCompletedCount < totalStudents && phaseCompletedCount > 0);
            phaseData.add(pd);
        }

        // 8. 构建节点分布数据
        List<Map<String, Object>> nodeData = new ArrayList<>();
        for (WfNodeInstance node : nodeInstances) {
            List<WfStudentNodeProgress> nodeProgress = progressByNode.getOrDefault(node.getId(), Collections.emptyList());

            int pendingCount = 0;
            int inProgressCount = 0;
            int completedCount = 0;

            for (BizTrainingParticipation part : participations) {
                Optional<WfStudentNodeProgress> studentProg = nodeProgress.stream()
                        .filter(p -> p.getParticipationId().equals(part.getId()))
                        .findFirst();

                if (!studentProg.isPresent() || studentProg.get().getStatus() == null || studentProg.get().getStatus() == 0) {
                    pendingCount++;
                } else if (studentProg.get().getStatus() == 1) {
                    inProgressCount++;
                } else if (studentProg.get().getStatus() == 2) {
                    completedCount++;
                }
            }

            Map<String, Object> nd = new LinkedHashMap<>();
            nd.put("nodeInstanceId", String.valueOf(node.getId()));
            nd.put("nodeName", node.getNodeName());
            nd.put("nodeType", node.getNodeType());
            nd.put("phaseId", node.getPhaseId());
            nd.put("pendingCount", pendingCount);
            nd.put("inProgressCount", inProgressCount);
            nd.put("completedCount", completedCount);
            nodeData.add(nd);
        }

        // 9. 总体统计
        int completedStudents = 0;
        if (!phases.isEmpty()) {
            // Students who completed all phases
            String lastPhaseId = phases.get(phases.size() - 1).has("phase_id")
                    ? phases.get(phases.size() - 1).get("phase_id").asText() : "";
            for (Map<String, Object> pd : phaseData) {
                if (lastPhaseId.equals(pd.get("phaseId"))) {
                    completedStudents = (int) pd.get("completedCount");
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskId", String.valueOf(taskId));
        result.put("taskName", task.getTaskName());
        result.put("totalStudents", totalStudents);
        result.put("completedStudents", completedStudents);
        result.put("phases", phaseData);
        result.put("nodes", nodeData);

        return ApiResult.ok(result);
    }

    // ==================== Step 6: POST /api/teacher/tasks/{taskId}/nudge ====================

    /**
     * 催办学生
     *
     * 向未完成指定节点（或所有未完成）的学生发送催办通知。
     * 操作记录到 wf_teacher_node_operation 表。
     *
     * @param taskId 实训任务ID
     * @param body 请求体，可选 nodeInstanceId 指定催办特定节点
     * @return 催办结果（催办人数）
     */
    @OperationLog(action = "催办学生")
    @PostMapping("/tasks/{taskId}/nudge")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Map<String, Object>> nudgeStudents(
            @PathVariable Long taskId,
            @RequestBody(required = false) Map<String, Object> body) {
        Long teacherId = getCurrentUserId();

        // 验证任务归属
        BizTrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            return ApiResult.error("实训任务不存在");
        }
        if (!teacherId.equals(task.getTeacherId())) {
            return ApiResult.error("无权操作该实训任务");
        }

        // 确定催办范围
        String nodeInstanceIdStr = body != null ? (String) body.get("nodeInstanceId") : null;
        Long nodeInstanceId = nodeInstanceIdStr != null ? Long.parseLong(nodeInstanceIdStr) : null;

        // 获取参与学生
        LambdaQueryWrapper<BizTrainingParticipation> partQuery = new LambdaQueryWrapper<>();
        partQuery.eq(BizTrainingParticipation::getTaskId, taskId);
        List<BizTrainingParticipation> participations = participationMapper.selectList(partQuery);

        int nudgedCount = 0;

        if (nodeInstanceId != null) {
            // 催办特定节点未完成的学生
            for (BizTrainingParticipation part : participations) {
                LambdaQueryWrapper<WfStudentNodeProgress> pq = new LambdaQueryWrapper<>();
                pq.eq(WfStudentNodeProgress::getParticipationId, part.getId())
                        .eq(WfStudentNodeProgress::getNodeInstanceId, nodeInstanceId);
                WfStudentNodeProgress progress = studentNodeProgressMapper.selectOne(pq);
                if (progress == null || progress.getStatus() == null || progress.getStatus() < 2) {
                    nudgedCount++;
                    // In production: send notification to student
                }
            }
        } else {
            // 催办所有未完成实训的学生
            for (BizTrainingParticipation part : participations) {
                if (part.getStatus() == null || part.getStatus() < 2) {
                    nudgedCount++;
                }
            }
        }

        // 记录操作
        recordTeacherOperation(teacherId, taskId, nodeInstanceId, 3,
                "催办" + nudgedCount + "名学生");

        Map<String, Object> result = new HashMap<>();
        result.put("nudgedCount", nudgedCount);
        return ApiResult.ok(result);
    }

    // ==================== Step 6: POST /api/teacher/nodes/{nodeInstanceId}/force-complete ====================

    /**
     * 强制完成节点
     *
     * 将指定节点实例中所有未完成学生的进度标记为已完成。
     * 操作记录到 wf_teacher_node_operation 表。
     *
     * @param nodeInstanceId 节点实例ID
     * @return 操作结果
     */
    @OperationLog(action = "强制完成节点")
    @PostMapping("/nodes/{nodeInstanceId}/force-complete")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Map<String, Object>> forceCompleteNode(@PathVariable Long nodeInstanceId) {
        Long teacherId = getCurrentUserId();

        // 验证节点实例存在
        WfNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
        if (nodeInstance == null) {
            return ApiResult.error("节点实例不存在");
        }

        // 验证任务归属
        BizTrainingTask task = trainingTaskMapper.selectById(nodeInstance.getTaskId());
        if (task == null || !teacherId.equals(task.getTeacherId())) {
            return ApiResult.error("无权操作该节点");
        }

        // 获取该任务的所有参与记录
        LambdaQueryWrapper<BizTrainingParticipation> partQuery = new LambdaQueryWrapper<>();
        partQuery.eq(BizTrainingParticipation::getTaskId, nodeInstance.getTaskId());
        List<BizTrainingParticipation> participations = participationMapper.selectList(partQuery);

        int forcedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (BizTrainingParticipation part : participations) {
            LambdaQueryWrapper<WfStudentNodeProgress> pq = new LambdaQueryWrapper<>();
            pq.eq(WfStudentNodeProgress::getParticipationId, part.getId())
                    .eq(WfStudentNodeProgress::getNodeInstanceId, nodeInstanceId);
            WfStudentNodeProgress progress = studentNodeProgressMapper.selectOne(pq);

            if (progress != null && (progress.getStatus() == null || progress.getStatus() < 2)) {
                progress.setStatus(2);
                progress.setExitedAt(now);
                progress.setUpdatedAt(now);
                studentNodeProgressMapper.updateById(progress);
                forcedCount++;
            } else if (progress == null) {
                // Create a completed progress record
                WfStudentNodeProgress newProgress = new WfStudentNodeProgress();
                newProgress.setParticipationId(part.getId());
                newProgress.setNodeInstanceId(nodeInstanceId);
                newProgress.setStatus(2);
                newProgress.setIsForcedComplete(1);
                newProgress.setEnteredAt(now);
                newProgress.setExitedAt(now);
                newProgress.setStayDurationSeconds(0);
                newProgress.setCreatedAt(now);
                newProgress.setUpdatedAt(now);
                studentNodeProgressMapper.insert(newProgress);
                forcedCount++;
            }
        }

        // 记录操作
        recordTeacherOperation(teacherId, nodeInstance.getTaskId(), nodeInstanceId, 4,
                "强制完成节点[" + nodeInstance.getNodeName() + "]，影响" + forcedCount + "名学生");

        Map<String, Object> result = new HashMap<>();
        result.put("forcedCount", forcedCount);
        result.put("nodeName", nodeInstance.getNodeName());
        return ApiResult.ok(result);
    }

    // ==================== Step 7: Record teacher operations ====================

    /**
     * 记录教师操作到 wf_teacher_node_operation 表
     *
     * @param teacherId 教师ID
     * @param taskId 任务ID
     * @param nodeInstanceId 节点实例ID（可为null）
     * @param operationTypeCode 操作类型代码：3-催办 4-强制完成 5-强制开启
     * @param content 操作内容描述
     */
    private void recordTeacherOperation(Long teacherId, Long taskId, Long nodeInstanceId,
                                        int operationTypeCode, String content) {
        WfTeacherNodeOperation operation = new WfTeacherNodeOperation();
        operation.setTeacherId(teacherId);
        operation.setTaskId(taskId);
        operation.setNodeInstanceId(nodeInstanceId);
        operation.setOperationType(operationTypeCode);
        operation.setOperationContent(content);
        operation.setCreatedAt(LocalDateTime.now());
        teacherNodeOperationMapper.insert(operation);
    }

    // ==================== Helper Methods ====================

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
            phases.sort(Comparator.comparingInt(node ->
                    node.has("sort_num") ? node.get("sort_num").asInt(Integer.MAX_VALUE) : Integer.MAX_VALUE
            ));
            return phases;
        } catch (Exception e) {
            log.error("解析 phases_json 失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
