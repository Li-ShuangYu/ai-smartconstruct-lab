package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.TrainingNodeProgressDTO;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

import com.smartconstruct.backend_core.util.Java8Compat;

@RestController
@RequestMapping("/api/student/training")
public class StudentTrainingFlowController {

    private static final Logger log = LoggerFactory.getLogger(StudentTrainingFlowController.class);
    private static final ObjectMapper om = new ObjectMapper();

    private final ITrainingParticipationService participationService;
    private final ITrainingTaskService trainingTaskService;
    private final ITrainingTemplateService templateService;
    private final IStudentActivityStateService activityStateService;
    private final IMindmapRecordService mindmapRecordService;
    private final ITrainingExamRecordService examRecordService;
    private final ITrainingChatLogService chatLogService;
    private final IGlobalActivityStateService globalStateService;

    public StudentTrainingFlowController(ITrainingParticipationService participationService,
                                          ITrainingTaskService trainingTaskService,
                                          ITrainingTemplateService templateService,
                                          IStudentActivityStateService activityStateService,
                                          IMindmapRecordService mindmapRecordService,
                                          ITrainingExamRecordService examRecordService,
                                          ITrainingChatLogService chatLogService,
                                          IGlobalActivityStateService globalStateService) {
        this.participationService = participationService;
        this.trainingTaskService = trainingTaskService;
        this.templateService = templateService;
        this.activityStateService = activityStateService;
        this.mindmapRecordService = mindmapRecordService;
        this.examRecordService = examRecordService;
        this.chatLogService = chatLogService;
        this.globalStateService = globalStateService;
    }

    /** 获取当前节点与配置 */
    @GetMapping("/{participationId}/current-state")
    public ApiResult<TrainingNodeProgressDTO> getCurrentState(@PathVariable Long participationId) {
        BizTrainingParticipation pt = participationService.getById(participationId);
        if (pt == null) return ApiResult.error("实训参与记录不存在");

        WfStudentActivityState state = activityStateService.getOne(
                new LambdaQueryWrapper<WfStudentActivityState>().eq(WfStudentActivityState::getParticipationId, participationId));
        if (state == null || state.getCurrentNodeId() == null) {
            return ApiResult.error("未开始实训，请先调用 /start");
        }

        BizTrainingTask task = trainingTaskService.getById(pt.getTaskId());
        if (task == null || task.getTemplateId() == null) return ApiResult.error("任务数据异常");
        WfTrainingTemplate template = templateService.getById(task.getTemplateId());
        if (template == null) return ApiResult.error("模板数据异常");

        TrainingNodeProgressDTO dto = new TrainingNodeProgressDTO();
        dto.setParticipationId(participationId);
        dto.setCurrentNodeId(state.getCurrentNodeId());

        try {
            Object payloadObj = template.getStandardPayloadJson();
            JsonNode root = om.valueToTree(payloadObj);
            JsonNode nodes = root.get("nodes");
            JsonNode edges = root.get("edges");

            dto.setAllNodes(nodes);
            dto.setAllEdges(edges);

            if (nodes != null && nodes.isArray()) {
                for (JsonNode n : nodes) {
                    String nid = n.has("node_id") ? n.get("node_id").asText() : "";
                    if (nid.equals(state.getCurrentNodeId())) {
                        dto.setNodeType(n.has("node_type") ? n.get("node_type").asText() : "");
                        dto.setNodeName(n.has("name") ? n.get("name").asText() : "");
                        dto.setConfig(n.has("config") ? om.treeToValue(n.get("config"), Object.class) : null);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析模板JSON失败 templateId={}", task.getTemplateId(), e);
        }

        return ApiResult.ok(dto);
    }

    /** 状态机推进：校验前置条件 → 更新 current_node_id */
    @OperationLog(action = "学生实训推进")
    @PostMapping("/{participationId}/next-node")
    public ApiResult<TrainingNodeProgressDTO> proceedNext(@PathVariable Long participationId,
                                                           @RequestBody Map<String, Object> body) {
        String currentNodeId = body.get("currentNodeId") != null ? body.get("currentNodeId").toString() : null;
        if (currentNodeId == null || Java8Compat.isBlank(currentNodeId)) return ApiResult.error("缺少当前节点ID");

        BizTrainingParticipation pt = participationService.getById(participationId);
        if (pt == null) return ApiResult.error("实训参与记录不存在");

        BizTrainingTask task = trainingTaskService.getById(pt.getTaskId());
        if (task == null || task.getTemplateId() == null) return ApiResult.error("任务数据异常");
        WfTrainingTemplate template = templateService.getById(task.getTemplateId());

        // 校验当前节点前置条件
        try {
            Object payloadObj = template.getStandardPayloadJson();
            JsonNode root = om.valueToTree(payloadObj);
            JsonNode nodes = root.get("nodes");
            String nodeType = "";
            if (nodes != null && nodes.isArray()) {
                for (JsonNode n : nodes) {
                    if (currentNodeId.equals(n.has("node_id") ? n.get("node_id").asText() : "")) {
                        nodeType = n.has("node_type") ? n.get("node_type").asText().toUpperCase() : "";
                        break;
                    }
                }
            }

            // 校验产出物
            if (nodeType.contains("MINDMAP") || nodeType.contains("DRAW")) {
                long count = mindmapRecordService.count(new LambdaQueryWrapper<BizMindmapRecord>()
                        .eq(BizMindmapRecord::getParticipationId, participationId)
                        .eq(BizMindmapRecord::getNodeId, currentNodeId));
                if (count == 0) return ApiResult.error("请先提交思维导图");
            }
            if (nodeType.contains("UPLOAD") || nodeType.contains("PLAN") || nodeType.contains("REQ")) {
                // check biz_training_upload — if needed
            }
            if (nodeType.contains("HOMEWORK") || nodeType.contains("EXAM")) {
                long count = examRecordService.count(new LambdaQueryWrapper<BizTrainingExamRecord>()
                        .eq(BizTrainingExamRecord::getParticipationId, participationId)
                        .eq(BizTrainingExamRecord::getNodeId, currentNodeId));
                if (count == 0) return ApiResult.error("请先提交作业/考试答案");
            }

            // 找下一个节点
            JsonNode edges = root.get("edges");
            String nextNodeId = null;
            if (edges != null && edges.isArray()) {
                for (JsonNode e : edges) {
                    if (currentNodeId.equals(e.has("source") ? e.get("source").asText() : "")) {
                        nextNodeId = e.has("target") ? e.get("target").asText() : null;
                        break;
                    }
                }
            }
            if (nextNodeId == null) return ApiResult.error("未找到下一节点，可能已到达终点");

            // 检查是否 END 节点
            boolean isEnd = false;
            if (nodes != null && nodes.isArray()) {
                for (JsonNode n : nodes) {
                    if (nextNodeId.equals(n.has("node_id") ? n.get("node_id").asText() : "")) {
                        String nt = n.has("node_type") ? n.get("node_type").asText().toUpperCase() : "";
                        if (nt.contains("END")) isEnd = true;
                        break;
                    }
                }
            }

            // 更新 state
            WfStudentActivityState state = activityStateService.getOne(
                    new LambdaQueryWrapper<WfStudentActivityState>().eq(WfStudentActivityState::getParticipationId, participationId));
            if (state == null) {
                state = new WfStudentActivityState();
                state.setParticipationId(participationId);
                state.setCurrentNodeId(nextNodeId);
                state.setUpdatedAt(LocalDateTime.now());
                activityStateService.save(state);
            } else {
                state.setCurrentNodeId(nextNodeId);
                state.setUpdatedAt(LocalDateTime.now());
                activityStateService.updateById(state);
            }

            if (isEnd) {
                pt.setStatus(2);
                pt.setUpdatedAt(LocalDateTime.now());
                participationService.updateById(pt);
            } else if (pt.getStatus() == 0) {
                pt.setStatus(1);
                pt.setUpdatedAt(LocalDateTime.now());
                participationService.updateById(pt);
            }

            return getCurrentState(participationId);
        } catch (Exception e) {
            log.error("推进节点失败 participationId={}", participationId, e);
            return ApiResult.error("推进失败: " + e.getMessage());
        }
    }

    /** 提交思维导图 */
    @OperationLog(action = "提交思维导图")
    @PostMapping("/{participationId}/action/mindmap")
    public ApiResult<Void> submitMindmap(@PathVariable Long participationId, @RequestBody Map<String, Object> body) {
        String nodeId = body.get("nodeId") != null ? body.get("nodeId").toString() : null;
        Object payload = body.get("payload");
        if (nodeId == null || payload == null) return ApiResult.error("缺少参数");

        BizMindmapRecord record = new BizMindmapRecord();
        record.setParticipationId(participationId);
        record.setNodeId(nodeId);
        record.setMindmapJson(payload instanceof String ? (String) payload : om.valueToTree(payload).toString());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        mindmapRecordService.save(record);
        return ApiResult.ok();
    }

    /** 提交作业/考试 */
    @OperationLog(action = "提交考试")
    @PostMapping("/{participationId}/action/exam")
    public ApiResult<Void> submitExam(@PathVariable Long participationId, @RequestBody Map<String, Object> body) {
        String nodeId = body.get("nodeId") != null ? body.get("nodeId").toString() : null;
        Object answers = body.get("answers");
        if (nodeId == null || answers == null) return ApiResult.error("缺少参数");

        // upsert
        BizTrainingExamRecord existing = examRecordService.getOne(
                new LambdaQueryWrapper<BizTrainingExamRecord>()
                        .eq(BizTrainingExamRecord::getParticipationId, participationId)
                        .eq(BizTrainingExamRecord::getNodeId, nodeId));
        BizTrainingExamRecord record = existing != null ? existing : new BizTrainingExamRecord();
        record.setParticipationId(participationId);
        record.setNodeId(nodeId);
        record.setAnswersJson(answers instanceof String ? (String) answers : om.valueToTree(answers).toString());
        record.setScore(0);
        record.setUpdatedAt(LocalDateTime.now());
        if (existing != null) examRecordService.updateById(record);
        else { record.setCreatedAt(LocalDateTime.now()); examRecordService.save(record); }
        return ApiResult.ok();
    }

    /** 学生端获取课中实训的全局当前状态（用于初始化或断线重连对齐） */
    @GetMapping("/in-class/{taskId}/current-state")
    public ApiResult<Map<String, Object>> getInClassState(@PathVariable Long taskId) {
        WfGlobalActivityState state = globalStateService.getOne(
                new LambdaQueryWrapper<WfGlobalActivityState>().eq(WfGlobalActivityState::getTaskId, taskId));
        if (state == null) return ApiResult.error("课中实训尚未初始化，等待教师推进");

        BizTrainingTask task = trainingTaskService.getById(taskId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskId", taskId);
        data.put("taskName", task != null ? task.getTaskName() : "");
        data.put("currentNodeId", state.getCurrentNodeId());

        // 尝试从模板 JSON 中找到对应节点信息
        if (task != null && task.getTemplateId() != null) {
            WfTrainingTemplate template = templateService.getById(task.getTemplateId());
            if (template != null && template.getStandardPayloadJson() != null) {
                try {
                    JsonNode root = om.valueToTree(template.getStandardPayloadJson());
                    data.put("allNodes", root.get("nodes"));
                    data.put("allEdges", root.get("edges"));
                    JsonNode nodes = root.get("nodes");
                    if (nodes != null && nodes.isArray()) {
                        for (JsonNode n : nodes) {
                            if (state.getCurrentNodeId().equals(n.has("node_id") ? n.get("node_id").asText() : "")) {
                                data.put("nodeType", n.has("node_type") ? n.get("node_type").asText() : "");
                                data.put("nodeName", n.has("name") ? n.get("name").asText() : "");
                                data.put("config", n.has("config") ? om.treeToValue(n.get("config"), Object.class) : null);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析全局状态模板JSON失败 taskId={}", taskId, e);
                }
            }
        }

        return ApiResult.ok(data);
    }
}
