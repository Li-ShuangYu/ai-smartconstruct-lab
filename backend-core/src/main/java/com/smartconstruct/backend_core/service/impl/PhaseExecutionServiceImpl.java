package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartconstruct.backend_core.dto.PhaseUnlockStatus;
import com.smartconstruct.backend_core.entity.BizTrainingParticipation;
import com.smartconstruct.backend_core.entity.BizTrainingTask;
import com.smartconstruct.backend_core.entity.WfNodeInstance;
import com.smartconstruct.backend_core.entity.WfStudentNodeProgress;
import com.smartconstruct.backend_core.entity.WfStudentActivityState;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.exception.BusinessException;
import com.smartconstruct.backend_core.mapper.BizTrainingParticipationMapper;
import com.smartconstruct.backend_core.mapper.BizTrainingTaskMapper;
import com.smartconstruct.backend_core.mapper.WfNodeInstanceMapper;
import com.smartconstruct.backend_core.mapper.WfStudentNodeProgressMapper;
import com.smartconstruct.backend_core.mapper.WfStudentActivityStateMapper;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.IPhaseExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 阶段执行服务实现
 *
 * 负责实训运行时的阶段推进逻辑，包括判断阶段完成状态、确定当前可进入阶段、
 * 管理学生节点进入和完成操作。
 *
 * @author SmartConstruct
 */
@Service
public class PhaseExecutionServiceImpl implements IPhaseExecutionService {

    private static final Logger log = LoggerFactory.getLogger(PhaseExecutionServiceImpl.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** 节点进度状态：已完成 */
    private static final int STATUS_COMPLETED = 2;

    @Autowired
    private WfNodeInstanceMapper nodeInstanceMapper;

    @Autowired
    private WfStudentNodeProgressMapper studentNodeProgressMapper;

    @Autowired
    private BizTrainingParticipationMapper participationMapper;

    @Autowired
    private BizTrainingTaskMapper trainingTaskMapper;

    @Autowired
    private WfTrainingTemplateMapper trainingTemplateMapper;

    @Autowired
    private WfStudentActivityStateMapper studentActivityStateMapper;

    @Override
    public boolean isPhaseComplete(Long taskId, String phaseId, Long studentId) {
        // 1. 查询该阶段内所有节点实例
        LambdaQueryWrapper<WfNodeInstance> nodeQuery = new LambdaQueryWrapper<>();
        nodeQuery.eq(WfNodeInstance::getTaskId, taskId)
                .eq(WfNodeInstance::getPhaseId, phaseId);
        List<WfNodeInstance> allNodes = nodeInstanceMapper.selectList(nodeQuery);

        if (allNodes.isEmpty()) {
            // 阶段内无节点，视为已完成
            return true;
        }

        // 2. 过滤出必须完成的节点（is_required=true）
        List<WfNodeInstance> requiredNodes = filterRequiredNodes(allNodes);

        if (requiredNodes.isEmpty()) {
            // 无必须完成的节点，阶段视为已完成
            return true;
        }

        // 3. 查找该学生在此任务中的参与记录
        LambdaQueryWrapper<BizTrainingParticipation> participationQuery = new LambdaQueryWrapper<>();
        participationQuery.eq(BizTrainingParticipation::getTaskId, taskId)
                .eq(BizTrainingParticipation::getStudentId, studentId);
        BizTrainingParticipation participation = participationMapper.selectOne(participationQuery);

        if (participation == null) {
            // 学生未参与该实训，阶段未完成
            return false;
        }

        // 4. 对每个必须完成的节点，检查是否有对应的已完成进度记录
        for (WfNodeInstance requiredNode : requiredNodes) {
            LambdaQueryWrapper<WfStudentNodeProgress> progressQuery = new LambdaQueryWrapper<>();
            progressQuery.eq(WfStudentNodeProgress::getParticipationId, participation.getId())
                    .eq(WfStudentNodeProgress::getNodeInstanceId, requiredNode.getId())
                    .eq(WfStudentNodeProgress::getStatus, STATUS_COMPLETED);
            Long count = studentNodeProgressMapper.selectCount(progressQuery);

            if (count == null || count == 0) {
                // 该必须节点未完成
                return false;
            }
        }

        // 所有必须节点均已完成
        return true;
    }

    @Override
    public String getCurrentUnlockedPhaseId(Long taskId, Long studentId) {
        // 1. 查询实训任务获取 template_id
        BizTrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            log.warn("实训任务不存在: taskId={}", taskId);
            return null;
        }

        // 2. 查询实训模板获取 phases_json
        WfTrainingTemplate template = trainingTemplateMapper.selectById(task.getTemplateId());
        if (template == null) {
            log.warn("实训模板不存在: templateId={}", task.getTemplateId());
            return null;
        }

        // 3. 解析 phases_json 为阶段列表并按 sort_num 排序
        List<JsonNode> sortedPhases = parsePhasesJson(template.getPhasesJson());
        if (sortedPhases.isEmpty()) {
            log.warn("模板 {} 的 phases_json 为空或无法解析", template.getId());
            return null;
        }

        // 4. 遍历阶段，返回第一个未完成的阶段ID
        String lastPhaseId = null;
        for (JsonNode phase : sortedPhases) {
            String phaseId = phase.has("phase_id") ? phase.get("phase_id").asText() : null;
            if (phaseId == null || phaseId.isEmpty()) {
                continue;
            }
            lastPhaseId = phaseId;

            // 5. 检查该阶段是否已完成
            boolean complete = isPhaseComplete(taskId, phaseId, studentId);
            if (!complete) {
                // 6. 返回第一个未完成的阶段
                return phaseId;
            }
        }

        // 7. 所有阶段均已完成，返回最后一个阶段ID
        return lastPhaseId;
    }

    /**
     * 解析 phases_json 字段为 JsonNode 列表，并按 sort_num 升序排序。
     *
     * phases_json 可能是 String、List/Map（由JacksonTypeHandler反序列化），
     * 需要统一转换为 JsonNode 数组后排序。
     *
     * @param phasesJson phases_json 字段值
     * @return 按 sort_num 排序的阶段 JsonNode 列表，解析失败返回空列表
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
                // 已被 JacksonTypeHandler 反序列化为 List/Map
                String jsonStr = objectMapper.writeValueAsString(phasesJson);
                arrayNode = objectMapper.readTree(jsonStr);
            }

            if (!arrayNode.isArray()) {
                log.warn("phases_json 不是数组格式");
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

    @Override
    public WfStudentNodeProgress enterNode(Long participationId, Long nodeInstanceId) {
        LocalDateTime now = LocalDateTime.now();

        // Expiry check: verify the training task has not expired
        checkTaskExpiry(participationId);

        // Phase lock check: verify the node's phase is unlocked for this student
        checkPhaseLock(participationId, nodeInstanceId);

        // 1. Query existing progress record for this participation + node instance
        LambdaQueryWrapper<WfStudentNodeProgress> progressQuery = new LambdaQueryWrapper<>();
        progressQuery.eq(WfStudentNodeProgress::getParticipationId, participationId)
                .eq(WfStudentNodeProgress::getNodeInstanceId, nodeInstanceId);
        WfStudentNodeProgress existing = studentNodeProgressMapper.selectOne(progressQuery);

        WfStudentNodeProgress result;

        if (existing == null) {
            // 2. No record exists — create a new one with status=1 (进行中)
            WfStudentNodeProgress progress = new WfStudentNodeProgress();
            progress.setParticipationId(participationId);
            progress.setNodeInstanceId(nodeInstanceId);
            progress.setStatus(1); // 进行中
            progress.setEnteredAt(now);
            progress.setCreatedAt(now);
            progress.setUpdatedAt(now);
            studentNodeProgressMapper.insert(progress);
            result = progress;
        } else if (existing.getStatus() == 0 || existing.getStatus() == 1) {
            // 3. Record exists with status 0 (未开始) or 1 (进行中) — update to in-progress
            existing.setStatus(1); // 进行中
            existing.setEnteredAt(now);
            existing.setUpdatedAt(now);
            studentNodeProgressMapper.updateById(existing);
            result = existing;
        } else {
            // Status is 2 (已完成) or 3 (已跳过) — return as-is without modification
            result = existing;
        }

        // 4. Update wf_student_activity_state: set current_node_instance_id
        LambdaQueryWrapper<WfStudentActivityState> stateQuery = new LambdaQueryWrapper<>();
        stateQuery.eq(WfStudentActivityState::getParticipationId, participationId);
        WfStudentActivityState activityState = studentActivityStateMapper.selectOne(stateQuery);

        if (activityState == null) {
            // Create new activity state record
            WfStudentActivityState newState = new WfStudentActivityState();
            newState.setParticipationId(participationId);
            newState.setCurrentNodeId(String.valueOf(nodeInstanceId));
            newState.setCreatedAt(now);
            newState.setUpdatedAt(now);
            studentActivityStateMapper.insert(newState);
        } else {
            // Update existing activity state
            activityState.setCurrentNodeId(String.valueOf(nodeInstanceId));
            activityState.setUpdatedAt(now);
            studentActivityStateMapper.updateById(activityState);
        }

        return result;
    }

    @Override
    public void completeNode(Long participationId, Long nodeInstanceId) {
        // 1. Query wf_student_node_progress for the record
        LambdaQueryWrapper<WfStudentNodeProgress> progressQuery = new LambdaQueryWrapper<>();
        progressQuery.eq(WfStudentNodeProgress::getParticipationId, participationId)
                .eq(WfStudentNodeProgress::getNodeInstanceId, nodeInstanceId);
        WfStudentNodeProgress progress = studentNodeProgressMapper.selectOne(progressQuery);

        // 2. If no record exists or already completed, log warning and return
        if (progress == null) {
            log.warn("completeNode: 未找到进度记录, participationId={}, nodeInstanceId={}", participationId, nodeInstanceId);
            return;
        }
        if (progress.getStatus() == STATUS_COMPLETED) {
            log.warn("completeNode: 节点已完成, participationId={}, nodeInstanceId={}", participationId, nodeInstanceId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 3. Set status = 2 (已完成)
        progress.setStatus(STATUS_COMPLETED);

        // 4. Set exited_at = now
        progress.setExitedAt(now);

        // 5. Calculate stay_duration_seconds (cumulative)
        if (progress.getEnteredAt() != null) {
            long currentVisitSeconds = Duration.between(progress.getEnteredAt(), now).getSeconds();
            Integer previousDuration = progress.getStayDurationSeconds();
            if (previousDuration != null && previousDuration > 0) {
                // Add new duration to previous cumulative duration
                progress.setStayDurationSeconds(previousDuration + (int) currentVisitSeconds);
            } else {
                progress.setStayDurationSeconds((int) currentVisitSeconds);
            }
        }

        // 6. Set updated_at = now
        progress.setUpdatedAt(now);

        // 7. Update the record in the database
        studentNodeProgressMapper.updateById(progress);

        // 8. Update wf_student_activity_state: set current_node_instance_id to null
        LambdaQueryWrapper<WfStudentActivityState> stateQuery = new LambdaQueryWrapper<>();
        stateQuery.eq(WfStudentActivityState::getParticipationId, participationId);
        WfStudentActivityState activityState = studentActivityStateMapper.selectOne(stateQuery);

        if (activityState != null) {
            activityState.setCurrentNodeId(null);
            activityState.setUpdatedAt(now);
            studentActivityStateMapper.updateById(activityState);
        }

        // 9. Check and unlock next phase after node completion
        WfNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
        if (nodeInstance != null && nodeInstance.getPhaseId() != null) {
            checkAndUnlockNextPhase(participationId, nodeInstance.getPhaseId());
        }
    }

    @Override
    public void checkAndUnlockNextPhase(Long participationId, String currentPhaseId) {
        // 1. Get participation to find taskId and studentId
        BizTrainingParticipation participation = participationMapper.selectById(participationId);
        if (participation == null) {
            log.warn("checkAndUnlockNextPhase: 参与记录不存在, participationId={}", participationId);
            return;
        }

        Long taskId = participation.getTaskId();
        Long studentId = participation.getStudentId();

        // 2. Check if current phase is complete
        boolean currentPhaseComplete = isPhaseComplete(taskId, currentPhaseId, studentId);
        if (!currentPhaseComplete) {
            // Current phase not yet complete, no unlock needed
            return;
        }

        // 3. Get sorted phases from template
        BizTrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            log.warn("checkAndUnlockNextPhase: 实训任务不存在, taskId={}", taskId);
            return;
        }

        WfTrainingTemplate template = trainingTemplateMapper.selectById(task.getTemplateId());
        if (template == null) {
            log.warn("checkAndUnlockNextPhase: 实训模板不存在, templateId={}", task.getTemplateId());
            return;
        }

        List<JsonNode> sortedPhases = parsePhasesJson(template.getPhasesJson());
        if (sortedPhases.isEmpty()) {
            return;
        }

        // 4. Find the current phase index and get the next phase
        int currentIndex = -1;
        for (int i = 0; i < sortedPhases.size(); i++) {
            JsonNode phase = sortedPhases.get(i);
            String phaseId = phase.has("phase_id") ? phase.get("phase_id").asText() : null;
            if (currentPhaseId.equals(phaseId)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex < 0 || currentIndex >= sortedPhases.size() - 1) {
            // Current phase not found or is the last phase — nothing to unlock
            log.info("checkAndUnlockNextPhase: 当前阶段为最后阶段或未找到, currentPhaseId={}", currentPhaseId);
            return;
        }

        // 5. Process subsequent phases: unlock next, and auto-complete phases with no required nodes
        for (int i = currentIndex + 1; i < sortedPhases.size(); i++) {
            JsonNode nextPhase = sortedPhases.get(i);
            String nextPhaseId = nextPhase.has("phase_id") ? nextPhase.get("phase_id").asText() : null;
            if (nextPhaseId == null || nextPhaseId.isEmpty()) {
                continue;
            }

            // The next phase is now unlocked (this is computed dynamically via getPhaseUnlockStatuses,
            // but we log the unlock event for traceability)
            log.info("checkAndUnlockNextPhase: 解锁阶段, participationId={}, phaseId={}", participationId, nextPhaseId);

            // Check if this next phase has no required nodes — if so, it auto-completes
            // and we continue to unlock the phase after it
            boolean nextPhaseHasNoRequiredNodes = hasNoRequiredNodes(taskId, nextPhaseId);
            if (nextPhaseHasNoRequiredNodes) {
                log.info("checkAndUnlockNextPhase: 阶段无必修节点，自动完成, phaseId={}", nextPhaseId);
                // Continue to unlock the phase after this one
                continue;
            } else {
                // Next phase has required nodes — stop here, it needs student action
                break;
            }
        }
    }

    @Override
    public List<PhaseUnlockStatus> getPhaseUnlockStatuses(Long participationId) {
        List<PhaseUnlockStatus> result = new ArrayList<>();

        // 1. Get participation to find taskId and studentId
        BizTrainingParticipation participation = participationMapper.selectById(participationId);
        if (participation == null) {
            log.warn("getPhaseUnlockStatuses: 参与记录不存在, participationId={}", participationId);
            return result;
        }

        Long taskId = participation.getTaskId();
        Long studentId = participation.getStudentId();

        // 2. Get sorted phases from template
        BizTrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            log.warn("getPhaseUnlockStatuses: 实训任务不存在, taskId={}", taskId);
            return result;
        }

        WfTrainingTemplate template = trainingTemplateMapper.selectById(task.getTemplateId());
        if (template == null) {
            log.warn("getPhaseUnlockStatuses: 实训模板不存在, templateId={}", task.getTemplateId());
            return result;
        }

        List<JsonNode> sortedPhases = parsePhasesJson(template.getPhasesJson());
        if (sortedPhases.isEmpty()) {
            return result;
        }

        // 3. Compute unlock and completion status for each phase
        // Rules:
        // - First phase (index 0) is always unlocked
        // - A phase is complete when all is_required=true nodes have status=2
        // - A phase with no required nodes is auto-complete when all preceding phases are complete
        // - A phase is unlocked when all preceding phases are complete
        // - If all phases are complete, all are unlocked (Requirement 5.6)

        boolean allPrecedingComplete = true;

        for (int i = 0; i < sortedPhases.size(); i++) {
            JsonNode phase = sortedPhases.get(i);
            String phaseId = phase.has("phase_id") ? phase.get("phase_id").asText() : null;
            String phaseName = phase.has("phase_name") ? phase.get("phase_name").asText() : "";
            int sortNum = phase.has("sort_num") ? phase.get("sort_num").asInt(i + 1) : i + 1;

            if (phaseId == null || phaseId.isEmpty()) {
                continue;
            }

            PhaseUnlockStatus status = new PhaseUnlockStatus();
            status.setPhaseId(phaseId);
            status.setPhaseName(phaseName);
            status.setSortNum(sortNum);

            // Determine unlock status
            boolean isUnlocked;
            if (i == 0) {
                // First phase is always unlocked
                isUnlocked = true;
            } else {
                // Unlocked if all preceding phases are complete
                isUnlocked = allPrecedingComplete;
            }
            status.setIsUnlocked(isUnlocked);

            // Determine completion status
            boolean isComplete;
            boolean hasNoRequired = hasNoRequiredNodes(taskId, phaseId);
            if (hasNoRequired) {
                // Phase with no required nodes: auto-complete when all preceding phases are complete
                isComplete = allPrecedingComplete;
            } else {
                // Phase with required nodes: check if all required nodes are completed
                isComplete = isPhaseComplete(taskId, phaseId, studentId);
            }
            status.setIsComplete(isComplete);

            result.add(status);

            // Update allPrecedingComplete for next iteration
            allPrecedingComplete = allPrecedingComplete && isComplete;
        }

        return result;
    }

    /**
     * 检查指定阶段是否没有任何 is_required=true 的节点。
     *
     * @param taskId  实训任务ID
     * @param phaseId 阶段ID
     * @return true 表示该阶段没有必修节点
     */
    private boolean hasNoRequiredNodes(Long taskId, String phaseId) {
        LambdaQueryWrapper<WfNodeInstance> nodeQuery = new LambdaQueryWrapper<>();
        nodeQuery.eq(WfNodeInstance::getTaskId, taskId)
                .eq(WfNodeInstance::getPhaseId, phaseId);
        List<WfNodeInstance> allNodes = nodeInstanceMapper.selectList(nodeQuery);

        if (allNodes.isEmpty()) {
            return true;
        }

        List<WfNodeInstance> requiredNodes = filterRequiredNodes(allNodes);
        return requiredNodes.isEmpty();
    }

    /**
     * 从节点实例列表中过滤出标记为 is_required=true 的节点。
     *
     * is_required 字段存储在 config_json.orchestration_settings.is_required 中。
     * 如果该字段缺失或无法解析，默认视为 required（true）。
     *
     * @param nodes 节点实例列表
     * @return 必须完成的节点列表
     */
    private List<WfNodeInstance> filterRequiredNodes(List<WfNodeInstance> nodes) {
        List<WfNodeInstance> requiredNodes = new ArrayList<>();

        for (WfNodeInstance node : nodes) {
            boolean isRequired = parseIsRequired(node);
            if (isRequired) {
                requiredNodes.add(node);
            }
        }

        return requiredNodes;
    }

    /**
     * 从节点实例的 configJson 中解析 is_required 字段。
     *
     * 路径: config_json -> orchestration_settings -> is_required
     * 如果字段缺失、configJson为null或解析失败，默认返回 true（所有节点默认必须完成）。
     *
     * @param node 节点实例
     * @return 是否为必须完成的节点
     */
    private boolean parseIsRequired(WfNodeInstance node) {
        Object configJson = node.getConfigJson();
        if (configJson == null) {
            return true;
        }

        try {
            // configJson 可能是 Map（由JacksonTypeHandler反序列化）或 String
            JsonNode configNode;
            if (configJson instanceof String) {
                configNode = objectMapper.readTree((String) configJson);
            } else {
                // 已经被JacksonTypeHandler反序列化为Map/LinkedHashMap
                String jsonStr = objectMapper.writeValueAsString(configJson);
                configNode = objectMapper.readTree(jsonStr);
            }

            JsonNode orchestrationSettings = configNode.get("orchestration_settings");
            if (orchestrationSettings == null || !orchestrationSettings.isObject()) {
                return true;
            }

            JsonNode isRequiredNode = orchestrationSettings.get("is_required");
            if (isRequiredNode == null || !isRequiredNode.isBoolean()) {
                // 字段缺失或非布尔类型，默认为 true
                return true;
            }

            return isRequiredNode.asBoolean();
        } catch (Exception e) {
            log.warn("解析节点 {} 的 is_required 字段失败，默认视为必须完成: {}", node.getId(), e.getMessage());
            return true;
        }
    }

    /**
     * 检查阶段锁定状态。
     *
     * 如果学生尝试进入的节点所属阶段尚未解锁（即该阶段排序在当前解锁阶段之后），
     * 则抛出 BusinessException（HTTP 403, PHASE_LOCKED）。
     *
     * 逻辑：
     * 1. 查询节点实例获取 phaseId 和 taskId
     * 2. 查询参与记录获取 taskId 和 studentId
     * 3. 调用 getCurrentUnlockedPhaseId 获取当前解锁阶段
     * 4. 比较节点阶段的 sort_num 与解锁阶段的 sort_num
     * 5. 如果节点阶段 sort_num > 解锁阶段 sort_num，抛出异常
     *
     * @param participationId 学生参与记录ID
     * @param nodeInstanceId 节点实例ID
     * @throws BusinessException 如果节点所属阶段被锁定
     */
    private void checkPhaseLock(Long participationId, Long nodeInstanceId) {
        // 1. 查询节点实例获取 phaseId 和 taskId
        WfNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
        if (nodeInstance == null) {
            log.warn("checkPhaseLock: 节点实例不存在, nodeInstanceId={}", nodeInstanceId);
            return;
        }

        String nodePhaseId = nodeInstance.getPhaseId();
        if (nodePhaseId == null || nodePhaseId.isEmpty()) {
            // 节点未关联阶段，不做锁定检查
            return;
        }

        // 2. 查询参与记录获取 taskId 和 studentId
        BizTrainingParticipation participation = participationMapper.selectById(participationId);
        if (participation == null) {
            log.warn("checkPhaseLock: 参与记录不存在, participationId={}", participationId);
            return;
        }

        Long taskId = participation.getTaskId();
        Long studentId = participation.getStudentId();

        // 3. 获取当前解锁阶段ID
        String unlockedPhaseId = getCurrentUnlockedPhaseId(taskId, studentId);
        if (unlockedPhaseId == null) {
            // 无法确定解锁阶段（模板/任务数据异常），不阻塞
            log.warn("checkPhaseLock: 无法确定当前解锁阶段, taskId={}, studentId={}", taskId, studentId);
            return;
        }

        // 如果节点阶段就是当前解锁阶段，直接放行
        if (nodePhaseId.equals(unlockedPhaseId)) {
            return;
        }

        // 4. 解析 phases_json 获取两个阶段的 sort_num 进行比较
        BizTrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            return;
        }

        WfTrainingTemplate template = trainingTemplateMapper.selectById(task.getTemplateId());
        if (template == null) {
            return;
        }

        List<JsonNode> sortedPhases = parsePhasesJson(template.getPhasesJson());
        if (sortedPhases.isEmpty()) {
            return;
        }

        // 查找节点阶段和解锁阶段的 sort_num
        int nodePhaseSortNum = Integer.MAX_VALUE;
        int unlockedPhaseSortNum = Integer.MAX_VALUE;

        for (JsonNode phase : sortedPhases) {
            String phaseId = phase.has("phase_id") ? phase.get("phase_id").asText() : null;
            int sortNum = phase.has("sort_num") ? phase.get("sort_num").asInt(Integer.MAX_VALUE) : Integer.MAX_VALUE;

            if (nodePhaseId.equals(phaseId)) {
                nodePhaseSortNum = sortNum;
            }
            if (unlockedPhaseId.equals(phaseId)) {
                unlockedPhaseSortNum = sortNum;
            }
        }

        // 5. 如果节点阶段排序在解锁阶段之后，说明该阶段被锁定
        if (nodePhaseSortNum > unlockedPhaseSortNum) {
            log.info("checkPhaseLock: 阶段锁定, studentId={}, nodePhaseId={}, unlockedPhaseId={}",
                    studentId, nodePhaseId, unlockedPhaseId);
            throw new BusinessException(403, "PHASE_LOCKED",
                    "该节点所属阶段尚未解锁，请先完成当前阶段的必修节点",
                    java.util.Collections.singletonMap("current_phase_id", unlockedPhaseId));
        }
    }

    /**
     * 检查实训任务是否已过期。
     *
     * 如果当前时间超过实训任务的 end_time，则抛出 BusinessException（HTTP 403, TASK_EXPIRED）。
     *
     * 逻辑：
     * 1. 查询参与记录获取 taskId
     * 2. 查询实训任务获取 endTime
     * 3. 如果当前时间超过 endTime，抛出异常
     *
     * @param participationId 学生参与记录ID
     * @throws BusinessException 如果实训任务已过期
     */
    private void checkTaskExpiry(Long participationId) {
        // 1. 查询参与记录获取 taskId
        BizTrainingParticipation participation = participationMapper.selectById(participationId);
        if (participation == null) {
            log.warn("checkTaskExpiry: 参与记录不存在, participationId={}", participationId);
            return;
        }

        Long taskId = participation.getTaskId();
        if (taskId == null) {
            log.warn("checkTaskExpiry: 参与记录缺少taskId, participationId={}", participationId);
            return;
        }

        // 2. 查询实训任务获取 endTime
        BizTrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            log.warn("checkTaskExpiry: 实训任务不存在, taskId={}", taskId);
            return;
        }

        LocalDateTime endTime = task.getEndTime();
        if (endTime == null) {
            // 未设置结束时间，不做过期检查
            return;
        }

        // 3. 如果当前时间超过 endTime，抛出异常
        if (LocalDateTime.now().isAfter(endTime)) {
            log.info("checkTaskExpiry: 实训任务已过期, participationId={}, taskId={}, endTime={}",
                    participationId, taskId, endTime);
            throw new BusinessException(403, "TASK_EXPIRED",
                    "实训任务已过期，无法继续操作",
                    java.util.Collections.singletonMap("end_time", endTime.toString()));
        }
    }
}
