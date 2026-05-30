package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartconstruct.backend_core.annotation.OperationLog;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.PageResult;
import com.smartconstruct.backend_core.dto.ValidationResult;
import com.smartconstruct.backend_core.entity.AiProcessingLog;
import com.smartconstruct.backend_core.entity.BizTrainingTask;
import com.smartconstruct.backend_core.entity.SysUser;
import com.smartconstruct.backend_core.entity.WfNodeAiStatus;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfNodeAiStatusMapper;
import com.smartconstruct.backend_core.service.IAiEngineClient;
import com.smartconstruct.backend_core.service.IAiProcessingLogService;
import com.smartconstruct.backend_core.service.INodeValidationService;
import com.smartconstruct.backend_core.service.ITrainingTaskService;
import com.smartconstruct.backend_core.service.ITrainingTemplateService;
import com.smartconstruct.backend_core.util.Java8Compat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher/templates")
public class TeacherTemplateController {

    private static final Logger log = LoggerFactory.getLogger(TeacherTemplateController.class);

    private final ITrainingTemplateService templateService;
    private final ITrainingTaskService trainingTaskService;
    private final INodeValidationService nodeValidationService;
    private final IAiEngineClient aiEngineClient;
    private final WfNodeAiStatusMapper nodeAiStatusMapper;
    private final IAiProcessingLogService aiLogService;
    private final ObjectMapper objectMapper;

    public TeacherTemplateController(ITrainingTemplateService templateService,
                                     ITrainingTaskService trainingTaskService,
                                     INodeValidationService nodeValidationService,
                                     IAiEngineClient aiEngineClient,
                                     WfNodeAiStatusMapper nodeAiStatusMapper,
                                     IAiProcessingLogService aiLogService) {
        this.templateService = templateService;
        this.trainingTaskService = trainingTaskService;
        this.nodeValidationService = nodeValidationService;
        this.aiEngineClient = aiEngineClient;
        this.nodeAiStatusMapper = nodeAiStatusMapper;
        this.aiLogService = aiLogService;
        this.objectMapper = new ObjectMapper();
    }

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    @GetMapping
    public ApiResult<PageResult<WfTrainingTemplate>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer aiStatus) {
        Long currentUserId = getCurrentUserId();
        LambdaQueryWrapper<WfTrainingTemplate> qw = new LambdaQueryWrapper<>();
        qw.eq(WfTrainingTemplate::getCreatorId, currentUserId);
        if (aiStatus != null) qw.eq(WfTrainingTemplate::getAiStatus, aiStatus);
        qw.orderByDesc(WfTrainingTemplate::getCreatedAt);
        Page<WfTrainingTemplate> p = templateService.page(new Page<>(page, pageSize), qw);
        return ApiResult.ok(new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @OperationLog(action = "创建实训模板编排")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String templateName = (String) body.get("templateName");
        Object canvasData = body.get("canvasData");
        if (templateName == null || Java8Compat.isBlank(templateName)) {
            return ResponseEntity.badRequest().body(ApiResult.error("模板名称不能为空"));
        }

        // Skip validation on create (draft save) — validation happens on publish
        WfTrainingTemplate template = new WfTrainingTemplate();
        template.setTemplateName(templateName);
        template.setTemplateDescription((String) body.get("templateDescription"));
        template.setRawCanvasJson(canvasData);
        template.setCreatorId(getCurrentUserId());
        template.setAiStatus(0);
        LocalDateTime now = LocalDateTime.now();
        template.setCreatedAt(now);
        template.setUpdatedAt(now);
        
        try {
            templateService.save(template);
        } catch (Exception e) {
            log.error("Failed to save template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.error("保存模板失败: " + e.getMessage()));
        }

        // Return id as String to avoid JS number precision loss (snowflake IDs are 19 digits, exceed 2^53).
        // Without ToStringSerializer applied (raw Map bypasses entity-level @JsonSerialize),
        // the frontend would receive a truncated number and the subsequent publish call would 404.
        return ResponseEntity.ok(ApiResult.ok(Java8Compat.mapOf(
                "id", String.valueOf(template.getId()),
                "aiStatus", 0
        )));
    }

    @OperationLog(action = "删除实训模板")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable String id) {
        Long currentUserId = getCurrentUserId();
        WfTrainingTemplate template = templateService.getById(Long.parseLong(id));
        if (template == null) return ApiResult.error("模板不存在");
        if (!Long.valueOf(currentUserId).equals(template.getCreatorId())) {
            return ApiResult.error("无权操作非本人创建的模板");
        }
        long usageCount = trainingTaskService.count(
                new LambdaQueryWrapper<BizTrainingTask>().eq(BizTrainingTask::getTemplateId, template.getId()));
        if (usageCount > 0) {
            return ApiResult.error("该模板已被用于实训任务，无法直接删除");
        }
        templateService.removeById(template.getId());
        return ApiResult.ok();
    }

    @OperationLog(action = "修改实训模板")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Long currentUserId = getCurrentUserId();
        WfTrainingTemplate template = templateService.getById(Long.parseLong(id));
        if (template == null) return ResponseEntity.badRequest().body(ApiResult.error("模板不存在"));
        if (!Long.valueOf(currentUserId).equals(template.getCreatorId())) {
            return ResponseEntity.badRequest().body(ApiResult.error("无权操作非本人创建的模板"));
        }

        // Validate canvas data if provided in the update
        Object canvasData = body.get("canvasData");
        if (canvasData != null) {
            ResponseEntity<?> validationError = validateCanvasData(canvasData);
            if (validationError != null) {
                return validationError;
            }
            template.setRawCanvasJson(canvasData);
        }

        String name = (String) body.get("templateName");
        String desc = body.get("templateDescription") != null ? (String) body.get("templateDescription") : (String) body.get("templateDesc");
        if (name != null && !Java8Compat.isBlank(name)) template.setTemplateName(name);
        if (desc != null) template.setTemplateDescription(desc);
        template.setUpdatedAt(LocalDateTime.now());
        templateService.updateById(template);
        return ResponseEntity.ok(ApiResult.ok());
    }

    /**
     * 发布模板并触发AI处理管线
     *
     * 保存raw_canvas_json，设置ai_status=1（处理中），异步触发AI管线。
     * 如果模板当前ai_status=1（正在处理中），返回409冲突。
     *
     * @param id 模板ID
     * @param body 请求体，包含canvasData（画布JSON）
     * @return 发布结果
     */
    @OperationLog(action = "发布实训模板")
    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publish(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long currentUserId = getCurrentUserId();
        log.info("Teacher [userId={}] publishing template [id={}]", currentUserId, id);

        WfTrainingTemplate template = templateService.getById(id);
        if (template == null) {
            return ResponseEntity.badRequest().body(ApiResult.error("模板不存在"));
        }
        if (!currentUserId.equals(template.getCreatorId())) {
            return ResponseEntity.badRequest().body(ApiResult.error("无权操作非本人创建的模板"));
        }

        // Step 5: Conflict check — reject if ai_status=1 (processing)
        if (template.getAiStatus() != null && template.getAiStatus() == 1) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResult.error(409, "AI_PROCESSING", "模板AI处理中，请勿重复发布"));
        }

        // Get canvas data from request body
        Object canvasData = body.get("canvasData");
        if (canvasData == null) {
            // If no canvasData in body, use existing raw_canvas_json
            canvasData = template.getRawCanvasJson();
        }
        if (canvasData == null) {
            return ResponseEntity.badRequest().body(ApiResult.error("画布数据不能为空"));
        }

        // Save raw_canvas_json and set ai_status=1
        template.setRawCanvasJson(canvasData);
        template.setAiStatus(1);
        template.setErrorReason(null);
        template.setUpdatedAt(LocalDateTime.now());
        templateService.updateById(template);

        // Log: TRIGGER_START
        aiLogService.log(id, "TRIGGER_START", "触发AI处理", "processing",
                "已提交画布数据到AI引擎");

        // Trigger AI pipeline asynchronously
        try {
            String canvasJsonString = objectMapper.writeValueAsString(canvasData);
            aiEngineClient.triggerAiPipeline(id, canvasJsonString);
            log.info("Template [id={}] published, ai_status=1, AI pipeline triggered", id);
        } catch (JsonProcessingException e) {
            log.error("Template [id={}] - failed to serialize canvas JSON: {}", id, e.getMessage());
            template.setAiStatus(3);
            template.setErrorReason("画布数据序列化失败: " + e.getMessage());
            template.setUpdatedAt(LocalDateTime.now());
            templateService.updateById(template);
            aiLogService.log(id, "TRIGGER_START", "触发AI处理", "failed",
                    "画布数据序列化失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.error(500, "画布数据序列化失败"));
        }

        return ResponseEntity.ok(ApiResult.ok(Java8Compat.mapOf(
                "id", String.valueOf(template.getId()),
                "aiStatus", 1
        )));
    }

    @OperationLog(action = "重试AI处理")
    @PostMapping("/{id}/retry-ai")
    public ResponseEntity<?> retryAi(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        log.info("Teacher [userId={}] requested AI retry for template [id={}]", currentUserId, id);

        WfTrainingTemplate template = templateService.getById(id);
        if (template == null) {
            return ResponseEntity.badRequest().body(ApiResult.error("模板不存在"));
        }
        if (!currentUserId.equals(template.getCreatorId())) {
            return ResponseEntity.badRequest().body(ApiResult.error("无权操作非本人创建的模板"));
        }
        if (template.getAiStatus() == null || template.getAiStatus() != 3) {
            return ResponseEntity.badRequest().body(ApiResult.error("只有AI处理失败的模板才能重试"));
        }

        // Step 4: Check retry_count < 5 by querying max retry_count from wf_node_ai_status
        LambdaQueryWrapper<WfNodeAiStatus> retryQuery = new LambdaQueryWrapper<>();
        retryQuery.eq(WfNodeAiStatus::getTemplateId, id)
                .eq(WfNodeAiStatus::getAiStatus, 3);
        List<WfNodeAiStatus> failedNodes = nodeAiStatusMapper.selectList(retryQuery);

        // Check if any node has exceeded max retry count
        boolean retryLimitExceeded = false;
        for (WfNodeAiStatus node : failedNodes) {
            if (node.getRetryCount() != null && node.getRetryCount() >= 5) {
                retryLimitExceeded = true;
                break;
            }
        }
        if (retryLimitExceeded) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResult.error(429, "RETRY_LIMIT_EXCEEDED", "重试次数已达上限(5次)，请联系管理员"));
        }

        // Get failed node IDs
        List<String> failedNodeIds = failedNodes.stream()
                .map(WfNodeAiStatus::getNodeId)
                .collect(Collectors.toList());

        if (failedNodeIds.isEmpty()) {
            // No per-node failures tracked, fall back to full re-processing via the new pipeline.
            log.info("Template [id={}] - no per-node failures found, re-triggering full pipeline", id);
            template.setAiStatus(1);
            template.setErrorReason(null);
            template.setUpdatedAt(LocalDateTime.now());
            templateService.updateById(template);
            aiLogService.log(id, "RETRY_START", "重试AI处理", "processing",
                    "无节点级失败记录，重新触发完整AI管线");
            try {
                String canvasJsonString = objectMapper.writeValueAsString(template.getRawCanvasJson());
                aiEngineClient.triggerAiPipeline(id, canvasJsonString);
            } catch (JsonProcessingException e) {
                log.error("Template [id={}] - failed to serialize canvas JSON for retry: {}", id, e.getMessage());
                template.setAiStatus(3);
                template.setErrorReason("画布数据序列化失败: " + e.getMessage());
                template.setUpdatedAt(LocalDateTime.now());
                templateService.updateById(template);
                aiLogService.log(id, "RETRY_START", "重试AI处理", "failed",
                        "画布数据序列化失败: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResult.error(500, "画布数据序列化失败"));
            }
        } else {
            // Retry only failed nodes
            log.info("Template [id={}] - retrying {} failed nodes: {}", id, failedNodeIds.size(), failedNodeIds);
            template.setAiStatus(1);
            template.setErrorReason(null);
            template.setUpdatedAt(LocalDateTime.now());
            templateService.updateById(template);
            aiLogService.log(id, "RETRY_START", "重试AI处理", "processing",
                    "重试" + failedNodeIds.size() + "个失败节点: " + String.join(", ", failedNodeIds));
            aiEngineClient.retryFailedNodes(id, failedNodeIds);
        }

        log.info("Template [id={}] retry initiated, ai_status reset to 1", id);
        return ResponseEntity.ok(ApiResult.ok(Java8Compat.mapOf(
                "id", String.valueOf(id),
                "aiStatus", 1,
                "retriedNodes", failedNodeIds.isEmpty() ? "all" : failedNodeIds
        )));
    }

    /**
     * 获取模板预览数据
     *
     * 返回模板的阶段化结构、节点配置摘要和AI生成内容，用于前端预览展示。
     *
     * @param id 模板ID
     * @return 模板预览数据
     */
    @GetMapping("/{id}/preview")
    public ResponseEntity<?> preview(@PathVariable Long id) {
        WfTrainingTemplate template = templateService.getById(id);
        if (template == null) {
            return ResponseEntity.badRequest().body(ApiResult.error("模板不存在"));
        }

        Map<String, Object> previewData = new java.util.LinkedHashMap<>();
        previewData.put("template_id", String.valueOf(template.getId()));
        previewData.put("template_name", template.getTemplateName());
        previewData.put("ai_status", template.getAiStatus() != null ? template.getAiStatus() : 0);

        // Parse raw_canvas_json to extract phases and nodes
        List<Map<String, Object>> phasesPreview = new java.util.ArrayList<>();
        Object rawCanvas = template.getRawCanvasJson();
        if (rawCanvas != null) {
            try {
                String canvasStr = rawCanvas instanceof String ? (String) rawCanvas : objectMapper.writeValueAsString(rawCanvas);
                JsonNode canvasNode = objectMapper.readTree(canvasStr);
                JsonNode phasesNode = canvasNode.get("phases");
                if (phasesNode != null && phasesNode.isArray()) {
                    for (JsonNode phase : phasesNode) {
                        Map<String, Object> phaseMap = new java.util.LinkedHashMap<>();
                        phaseMap.put("phase_id", phase.has("phase_id") ? phase.get("phase_id").asText() : "");
                        phaseMap.put("phase_name", phase.has("phase_name") ? phase.get("phase_name").asText() : "");
                        phaseMap.put("sort_num", phase.has("sort_num") ? phase.get("sort_num").asInt() : 0);

                        List<Map<String, Object>> nodesPreview = new java.util.ArrayList<>();
                        JsonNode nodesNode = phase.get("nodes");
                        if (nodesNode != null && nodesNode.isArray()) {
                            for (JsonNode node : nodesNode) {
                                Map<String, Object> nodeMap = new java.util.LinkedHashMap<>();
                                nodeMap.put("node_id", node.has("node_id") ? node.get("node_id").asText() : "");
                                nodeMap.put("node_type", node.has("node_type") ? node.get("node_type").asText() : "");
                                nodeMap.put("node_name", node.has("node_name") ? node.get("node_name").asText() : "");
                                nodeMap.put("ai_status", template.getAiStatus() != null ? template.getAiStatus() : 0);
                                // Config summary: extract display dimension or flatten top-level config
                                Map<String, Object> configSummary = new java.util.LinkedHashMap<>();
                                JsonNode configNode = node.get("config");
                                if (configNode != null) {
                                    if (configNode.has("display")) {
                                        configNode.get("display").fields().forEachRemaining(f -> configSummary.put(f.getKey(), f.getValue().isTextual() ? f.getValue().asText() : f.getValue().toString()));
                                    } else {
                                        configNode.fields().forEachRemaining(f -> {
                                            if (!f.getKey().startsWith("_")) {
                                                configSummary.put(f.getKey(), f.getValue().isTextual() ? f.getValue().asText() : f.getValue().toString());
                                            }
                                        });
                                    }
                                }
                                nodeMap.put("config_summary", configSummary);
                                nodeMap.put("ai_generated_content", null);
                                nodesPreview.add(nodeMap);
                            }
                        }
                        phaseMap.put("nodes", nodesPreview);
                        phasesPreview.add(phaseMap);
                    }
                } else {
                    // Legacy flat format: nodes + edges (no phases)
                    JsonNode nodesNode = canvasNode.get("nodes");
                    if (nodesNode != null && nodesNode.isArray()) {
                        Map<String, Object> defaultPhase = new java.util.LinkedHashMap<>();
                        defaultPhase.put("phase_id", "default");
                        defaultPhase.put("phase_name", "全部节点");
                        defaultPhase.put("sort_num", 1);
                        List<Map<String, Object>> nodesPreview = new java.util.ArrayList<>();
                        for (JsonNode node : nodesNode) {
                            Map<String, Object> nodeMap = new java.util.LinkedHashMap<>();
                            nodeMap.put("node_id", node.has("node_id") ? node.get("node_id").asText() : "");
                            nodeMap.put("node_type", node.has("node_type") ? node.get("node_type").asText() : (node.has("type") ? node.get("type").asText() : ""));
                            nodeMap.put("node_name", node.has("node_name") ? node.get("node_name").asText() : (node.has("name") ? node.get("name").asText() : ""));
                            nodeMap.put("ai_status", template.getAiStatus() != null ? template.getAiStatus() : 0);
                            Map<String, Object> configSummary = new java.util.LinkedHashMap<>();
                            JsonNode configNode = node.get("config");
                            if (configNode != null) {
                                configNode.fields().forEachRemaining(f -> {
                                    if (!f.getKey().startsWith("_")) {
                                        configSummary.put(f.getKey(), f.getValue().isTextual() ? f.getValue().asText() : f.getValue().toString());
                                    }
                                });
                            }
                            nodeMap.put("config_summary", configSummary);
                            nodeMap.put("ai_generated_content", null);
                            nodesPreview.add(nodeMap);
                        }
                        defaultPhase.put("nodes", nodesPreview);
                        phasesPreview.add(defaultPhase);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse canvas JSON for preview: {}", e.getMessage());
            }
        }
        // Sort phases by sort_num ascending before adding to response
        phasesPreview.sort((a, b) -> {
            Integer sortA = (Integer) a.get("sort_num");
            Integer sortB = (Integer) b.get("sort_num");
            if (sortA == null) sortA = 0;
            if (sortB == null) sortB = 0;
            return sortA.compareTo(sortB);
        });

        previewData.put("phases", phasesPreview);

        // If AI processing is complete, enrich nodes with AI-generated content
        if (template.getAiStatus() != null && template.getAiStatus() == 2 && template.getStandardPayloadJson() != null) {
            try {
                String payloadStr = template.getStandardPayloadJson() instanceof String
                        ? (String) template.getStandardPayloadJson()
                        : objectMapper.writeValueAsString(template.getStandardPayloadJson());
                JsonNode payloadNode = objectMapper.readTree(payloadStr);

                // Build Map<nodeId, aiContent> from standard_payload_json
                Map<String, Object> nodeAiContentMap = new java.util.HashMap<>();
                JsonNode nodesInPayload = payloadNode.get("nodes");
                if (nodesInPayload != null && nodesInPayload.isArray()) {
                    for (JsonNode nodeEntry : nodesInPayload) {
                        String nodeId = nodeEntry.has("node_id") ? nodeEntry.get("node_id").asText() : null;
                        if (nodeId != null) {
                            // Use ai_generated_content field if present, otherwise use the whole node entry
                            if (nodeEntry.has("ai_generated_content")) {
                                nodeAiContentMap.put(nodeId, nodeEntry.get("ai_generated_content").asText());
                            } else {
                                // Use the entire node entry as content (excluding node_id)
                                Map<String, Object> contentMap = new java.util.LinkedHashMap<>();
                                nodeEntry.fields().forEachRemaining(f -> {
                                    if (!"node_id".equals(f.getKey())) {
                                        contentMap.put(f.getKey(), f.getValue().isTextual() ? f.getValue().asText() : f.getValue().toString());
                                    }
                                });
                                nodeAiContentMap.put(nodeId, contentMap);
                            }
                        }
                    }
                }

                // Collect node IDs not found in standard_payload_json for fallback query
                List<String> missingNodeIds = new java.util.ArrayList<>();

                // Iterate through all nodes in phasesPreview and set ai_generated_content
                for (Map<String, Object> phase : phasesPreview) {
                    List<Map<String, Object>> nodes = (List<Map<String, Object>>) phase.get("nodes");
                    if (nodes != null) {
                        for (Map<String, Object> node : nodes) {
                            String nodeId = (String) node.get("node_id");
                            if (nodeId != null && nodeAiContentMap.containsKey(nodeId)) {
                                node.put("ai_generated_content", nodeAiContentMap.get(nodeId));
                            } else if (nodeId != null && !nodeId.isEmpty()) {
                                missingNodeIds.add(nodeId);
                            }
                        }
                    }
                }

                // Fallback: check wf_node_ai_status.result_json for nodes not found in standard_payload_json
                if (!missingNodeIds.isEmpty()) {
                    LambdaQueryWrapper<WfNodeAiStatus> fallbackQuery = new LambdaQueryWrapper<>();
                    fallbackQuery.eq(WfNodeAiStatus::getTemplateId, id)
                            .in(WfNodeAiStatus::getNodeId, missingNodeIds)
                            .eq(WfNodeAiStatus::getAiStatus, 2);
                    List<WfNodeAiStatus> fallbackResults = nodeAiStatusMapper.selectList(fallbackQuery);

                    Map<String, Object> fallbackMap = new java.util.HashMap<>();
                    for (WfNodeAiStatus status : fallbackResults) {
                        if (status.getResultJson() != null) {
                            fallbackMap.put(status.getNodeId(), status.getResultJson());
                        }
                    }

                    // Apply fallback content to nodes
                    for (Map<String, Object> phase : phasesPreview) {
                        List<Map<String, Object>> nodes = (List<Map<String, Object>>) phase.get("nodes");
                        if (nodes != null) {
                            for (Map<String, Object> node : nodes) {
                                String nodeId = (String) node.get("node_id");
                                if (nodeId != null && node.get("ai_generated_content") == null
                                        && fallbackMap.containsKey(nodeId)) {
                                    node.put("ai_generated_content", fallbackMap.get(nodeId));
                                }
                            }
                        }
                    }
                }

                log.debug("Template [id={}] enriched with AI content from standard_payload_json ({} nodes from payload, {} from fallback)",
                        id, nodeAiContentMap.size(), missingNodeIds.size());
            } catch (Exception e) {
                log.warn("Failed to parse standard_payload_json for preview enrichment: {}", e.getMessage());
            }
        }

        return ResponseEntity.ok(ApiResult.ok(previewData));
    }

    /**
     * 获取模板的AI处理日志（按时间升序）
     *
     * @param id 模板ID
     * @return AI处理日志列表，按时间升序排列
     */
    @GetMapping("/{id}/ai-logs")
    public ResponseEntity<?> getAiLogs(@PathVariable Long id) {
        WfTrainingTemplate template = templateService.getById(id);
        if (template == null) {
            return ResponseEntity.badRequest().body(ApiResult.error("模板不存在"));
        }

        List<AiProcessingLog> logs = aiLogService.getLogsByTemplateId(id);
        return ResponseEntity.ok(ApiResult.ok(Java8Compat.mapOf(
                "template_id", String.valueOf(id),
                "template_name", template.getTemplateName(),
                "ai_status", template.getAiStatus(),
                "error_reason", template.getErrorReason(),
                "logs", logs
        )));
    }

    /**
     * 验证画布数据中的阶段配置和节点配置。
     *
     * 验证流程：
     * 1. 解析canvas JSON，提取phases数组
     * 2. 调用 validatePhases 验证阶段配置合法性
     * 3. 遍历每个阶段中的每个节点，调用 validateNodeConfig 验证节点配置
     *
     * @param canvasData 画布数据对象
     * @return 如果验证失败返回 ResponseEntity(400) + ValidationResult，验证通过返回 null
     */
    private ResponseEntity<?> validateCanvasData(Object canvasData) {
        try {
            String canvasJsonStr = objectMapper.writeValueAsString(canvasData);
            JsonNode canvasNode = objectMapper.readTree(canvasJsonStr);

            // Validate phases if present
            JsonNode phasesNode = canvasNode.get("phases");
            if (phasesNode != null && phasesNode.isArray()) {
                String phasesJsonStr = objectMapper.writeValueAsString(phasesNode);
                ValidationResult phasesResult = nodeValidationService.validatePhases(phasesJsonStr);
                if (!phasesResult.isValid()) {
                    return ResponseEntity.badRequest().body(phasesResult);
                }

                // Validate each node's config within each phase
                for (JsonNode phase : phasesNode) {
                    JsonNode nodesNode = phase.get("nodes");
                    if (nodesNode == null || !nodesNode.isArray()) {
                        continue;
                    }
                    for (JsonNode node : nodesNode) {
                        JsonNode nodeTypeNode = node.get("node_type");
                        JsonNode configNode = node.get("config");
                        if (nodeTypeNode == null || configNode == null) {
                            continue;
                        }
                        String nodeType = nodeTypeNode.asText();
                        String configJsonStr = objectMapper.writeValueAsString(configNode);
                        ValidationResult nodeResult = nodeValidationService.validateNodeConfig(nodeType, configJsonStr);
                        if (!nodeResult.isValid()) {
                            return ResponseEntity.badRequest().body(nodeResult);
                        }
                    }
                }
            }
        } catch (JsonProcessingException e) {
            log.warn("Canvas data JSON processing failed: {}", e.getMessage());
            ValidationResult errorResult = ValidationResult.error("画布数据JSON格式无效: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResult);
        }
        return null;
    }
}