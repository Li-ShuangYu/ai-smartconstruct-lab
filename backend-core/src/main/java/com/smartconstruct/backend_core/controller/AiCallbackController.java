package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartconstruct.backend_core.dto.*;
import com.smartconstruct.backend_core.entity.BizKnowledgePoint;
import com.smartconstruct.backend_core.entity.BizQuestion;
import com.smartconstruct.backend_core.entity.BizQuestionBank;
import com.smartconstruct.backend_core.entity.WfNodeAiStatus;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfNodeAiStatusMapper;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.IKnowledgePointService;
import com.smartconstruct.backend_core.service.IQuestionBankService;
import com.smartconstruct.backend_core.service.IQuestionService;
import com.smartconstruct.backend_core.websocket.TrainingWebSocketHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI引擎处理完成回调控制器
 *
 * 接收AI引擎处理完成后的回调通知，更新模板状态并持久化生成结果。
 * 这是一个内部接口，不对外暴露给前端用户。
 *
 * @author SmartConstruct
 */
@RestController
@RequestMapping("/api/internal")
public class AiCallbackController {

    private static final Logger log = LoggerFactory.getLogger(AiCallbackController.class);

    private final WfTrainingTemplateMapper templateMapper;
    private final WfNodeAiStatusMapper nodeAiStatusMapper;
    private final IQuestionBankService questionBankService;
    private final IQuestionService questionService;
    private final IKnowledgePointService knowledgePointService;
    private final TrainingWebSocketHandler webSocketHandler;

    /**
     * 构造函数注入，支持测试场景
     */
    public AiCallbackController(WfTrainingTemplateMapper templateMapper,
                                WfNodeAiStatusMapper nodeAiStatusMapper,
                                IQuestionBankService questionBankService,
                                IQuestionService questionService,
                                IKnowledgePointService knowledgePointService,
                                TrainingWebSocketHandler webSocketHandler) {
        this.templateMapper = templateMapper;
        this.nodeAiStatusMapper = nodeAiStatusMapper;
        this.questionBankService = questionBankService;
        this.questionService = questionService;
        this.knowledgePointService = knowledgePointService;
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * AI处理状态轮询端点（备用方案）
     *
     * 当AI引擎回调投递失败时，AI引擎可通过此端点主动查询模板的当前AI处理状态。
     *
     * @param templateId 模板ID
     * @return 包含 templateId, aiStatus, errorReason 的状态信息
     */
    @GetMapping("/ai-status/{templateId}")
    public ApiResult<Map<String, Object>> getAiStatus(@PathVariable("templateId") Long templateId) {
        log.info("AI status polling request: templateId={}", templateId);

        WfTrainingTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            log.warn("AI status polling: template not found, templateId={}", templateId);
            return ApiResult.error(404, "模板不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("templateId", template.getId());
        result.put("aiStatus", template.getAiStatus());
        result.put("errorReason", template.getErrorReason());

        return ApiResult.ok(result);
    }

    /**
     * AI处理完成回调
     *
     * @param request 包含templateId, jobId, status, standardPayloadJson, errorReason, generatedAssets
     * @return ApiResult<Void>
     */
    @PostMapping("/ai-callback")
    public ApiResult<Void> onAiComplete(@RequestBody AiCallbackRequest request) {
        log.info("Received AI callback: templateId={}, jobId={}, status={}",
                request.getTemplateId(), request.getJobId(), request.getStatus());

        // Validate required fields
        if (request.getTemplateId() == null) {
            log.warn("AI callback rejected: templateId is null");
            return ApiResult.error(400, "templateId不能为空");
        }
        if (request.getStatus() == null) {
            log.warn("AI callback rejected: status is null");
            return ApiResult.error(400, "status不能为空");
        }

        // Look up the template by templateId
        WfTrainingTemplate template = templateMapper.selectById(request.getTemplateId());
        if (template == null) {
            log.warn("AI callback rejected: template not found, templateId={}", request.getTemplateId());
            return ApiResult.error(404, "模板不存在");
        }

        // Idempotency check: if ai_status is already 2 (success), skip processing
        if (template.getAiStatus() != null && template.getAiStatus() == 2) {
            log.warn("Duplicate AI callback ignored: templateId={} already has ai_status=2, jobId={}",
                    request.getTemplateId(), request.getJobId());
            return ApiResult.ok();
        }

        // Process based on callback status
        if (request.getStatus() == 2) {
            // Success: update standard_payload_json and ai_status
            template.setStandardPayloadJson(request.getStandardPayloadJson());
            template.setAiStatus(2);
            template.setUpdatedAt(LocalDateTime.now());
            log.info("AI processing succeeded for templateId={}, updating standard_payload_json and ai_status=2",
                    request.getTemplateId());
        } else if (request.getStatus() == 3) {
            // Failure: update ai_status and error_reason
            template.setAiStatus(3);
            template.setErrorReason(request.getErrorReason());
            template.setUpdatedAt(LocalDateTime.now());
            log.warn("AI processing failed for templateId={}, errorReason={}",
                    request.getTemplateId(), request.getErrorReason());
        } else {
            log.warn("AI callback rejected: invalid status={}, templateId={}",
                    request.getStatus(), request.getTemplateId());
            return ApiResult.error(400, "无效的状态值，status必须为2(成功)或3(失败)");
        }

        // Save the updated template to the database
        templateMapper.updateById(template);

        // Persist AI-generated questions if present (only on success)
        if (request.getStatus() == 2 && request.getGeneratedAssets() != null
                && request.getGeneratedAssets().getQuestions() != null
                && !request.getGeneratedAssets().getQuestions().isEmpty()) {
            persistAiGeneratedQuestions(request.getGeneratedAssets().getQuestions(), template);
        }

        // Persist AI-generated knowledge points if present (only on success)
        if (request.getStatus() == 2 && request.getGeneratedAssets() != null
                && request.getGeneratedAssets().getKnowledgePoints() != null
                && !request.getGeneratedAssets().getKnowledgePoints().isEmpty()) {
            persistAiGeneratedKnowledgePoints(request.getGeneratedAssets().getKnowledgePoints(), template);
        }

        log.info("AI callback processed successfully for templateId={}", request.getTemplateId());
        return ApiResult.ok();
    }

    /**
     * AI处理完成回调（新版，节点级AI管线）
     *
     * 接收AI引擎处理完成后的回调，更新模板的standard_payload_json和ai_status。
     * 同时通过WebSocket通知教师前端状态变更。
     *
     * @param payload 包含template_id, job_id, status, standard_payload_json, error_reason
     * @return ApiResult<Void>
     */
    @PostMapping("/ai/callback")
    public ApiResult<Void> onAiPipelineComplete(@RequestBody AiCallbackPayload payload) {
        log.info("Received AI pipeline callback: templateId={}, jobId={}, status={}",
                payload.getTemplateId(), payload.getJobId(), payload.getStatus());

        // Validate required fields
        if (payload.getTemplateId() == null) {
            log.warn("AI pipeline callback rejected: template_id is null");
            return ApiResult.error(400, "template_id不能为空");
        }
        if (payload.getStatus() == null) {
            log.warn("AI pipeline callback rejected: status is null");
            return ApiResult.error(400, "status不能为空");
        }

        // Look up the template
        WfTrainingTemplate template = templateMapper.selectById(payload.getTemplateId());
        if (template == null) {
            log.warn("AI pipeline callback rejected: template not found, templateId={}", payload.getTemplateId());
            return ApiResult.error(404, "模板不存在");
        }

        // Idempotency check: if ai_status is already 2 (success), skip processing
        if (template.getAiStatus() != null && template.getAiStatus() == 2) {
            log.warn("Duplicate AI pipeline callback ignored: templateId={} already has ai_status=2", payload.getTemplateId());
            return ApiResult.ok();
        }

        // Process based on callback status
        if (payload.getStatus() == 2) {
            // Success: update standard_payload_json and ai_status
            template.setStandardPayloadJson(payload.getStandardPayloadJson());
            template.setAiStatus(2);
            template.setErrorReason(null);
            template.setUpdatedAt(java.time.LocalDateTime.now());
            log.info("AI pipeline succeeded for templateId={}, updating ai_status=2", payload.getTemplateId());
        } else if (payload.getStatus() == 3) {
            // Failure: update ai_status and error_reason
            template.setAiStatus(3);
            template.setErrorReason(payload.getErrorReason());
            template.setUpdatedAt(java.time.LocalDateTime.now());
            log.warn("AI pipeline failed for templateId={}, errorReason={}", payload.getTemplateId(), payload.getErrorReason());
        } else {
            log.warn("AI pipeline callback rejected: invalid status={}", payload.getStatus());
            return ApiResult.error(400, "无效的状态值，status必须为2(成功)或3(失败)");
        }

        // Save the updated template
        templateMapper.updateById(template);

        // Persist AI-generated assets if present (only on success)
        if (payload.getStatus() == 2 && payload.getGeneratedAssets() != null) {
            if (payload.getGeneratedAssets().getQuestions() != null
                    && !payload.getGeneratedAssets().getQuestions().isEmpty()) {
                persistAiGeneratedQuestions(payload.getGeneratedAssets().getQuestions(), template);
            }
            if (payload.getGeneratedAssets().getKnowledgePoints() != null
                    && !payload.getGeneratedAssets().getKnowledgePoints().isEmpty()) {
                persistAiGeneratedKnowledgePoints(payload.getGeneratedAssets().getKnowledgePoints(), template);
            }
        }

        // Write back node-level AI content from standard_payload_json to wf_node_ai_status
        if (payload.getStatus() == 2 && payload.getStandardPayloadJson() != null) {
            persistNodeLevelAiContent(payload.getTemplateId(), payload.getStandardPayloadJson());
        }

        // Step 6: WebSocket notification on AI completion
        notifyAiStatusChange(payload.getTemplateId(), payload.getStatus());

        log.info("AI pipeline callback processed successfully for templateId={}", payload.getTemplateId());
        return ApiResult.ok();
    }

    /**
     * 节点AI处理状态更新回调
     *
     * AI引擎在处理每个节点时独立回调此端点，更新wf_node_ai_status记录。
     * 使用try/catch实现优雅降级：如果wf_node_ai_status表不存在，仅记录警告不影响主流程。
     *
     * @param payload 节点状态载荷
     * @return ApiResult<Void>
     */
    @PostMapping("/ai/callback/node-status")
    public ApiResult<Void> onNodeAiStatusUpdate(@RequestBody NodeAiStatusPayload payload) {
        log.info("Received node AI status update: templateId={}, nodeId={}, aiStatus={}",
                payload.getTemplateId(), payload.getNodeId(), payload.getAiStatus());

        // Validate required fields
        if (payload.getTemplateId() == null || payload.getNodeId() == null || payload.getAiStatus() == null) {
            return ApiResult.error(400, "template_id, node_id, ai_status不能为空");
        }

        // Step 3: Upsert wf_node_ai_status record with graceful degradation
        try {
            // Try to find existing record
            LambdaQueryWrapper<WfNodeAiStatus> query = new LambdaQueryWrapper<>();
            query.eq(WfNodeAiStatus::getTemplateId, payload.getTemplateId())
                    .eq(WfNodeAiStatus::getNodeId, payload.getNodeId());
            WfNodeAiStatus existing = nodeAiStatusMapper.selectOne(query);

            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            if (existing != null) {
                // Update existing record
                LambdaUpdateWrapper<WfNodeAiStatus> update = new LambdaUpdateWrapper<>();
                update.eq(WfNodeAiStatus::getId, existing.getId())
                        .set(WfNodeAiStatus::getAiStatus, payload.getAiStatus())
                        .set(WfNodeAiStatus::getLastProcessedAt, now)
                        .set(WfNodeAiStatus::getUpdatedAt, now);

                if (payload.getErrorReason() != null) {
                    update.set(WfNodeAiStatus::getErrorReason, payload.getErrorReason());
                }
                if (payload.getResultJson() != null) {
                    update.set(WfNodeAiStatus::getResultJson, payload.getResultJson());
                }
                if (payload.getNodeType() != null) {
                    update.set(WfNodeAiStatus::getNodeType, payload.getNodeType());
                }
                if (payload.getPhaseId() != null) {
                    update.set(WfNodeAiStatus::getPhaseId, payload.getPhaseId());
                }

                nodeAiStatusMapper.update(null, update);
                log.info("Updated node AI status: templateId={}, nodeId={}, aiStatus={}",
                        payload.getTemplateId(), payload.getNodeId(), payload.getAiStatus());
            } else {
                // Insert new record
                WfNodeAiStatus nodeStatus = new WfNodeAiStatus();
                nodeStatus.setTemplateId(payload.getTemplateId());
                nodeStatus.setNodeId(payload.getNodeId());
                nodeStatus.setNodeType(payload.getNodeType() != null ? payload.getNodeType() : "unknown");
                nodeStatus.setPhaseId(payload.getPhaseId() != null ? payload.getPhaseId() : "");
                nodeStatus.setAiStatus(payload.getAiStatus());
                nodeStatus.setErrorReason(payload.getErrorReason());
                nodeStatus.setRetryCount(0);
                nodeStatus.setResultJson(payload.getResultJson());
                nodeStatus.setLastProcessedAt(now);
                nodeStatus.setCreatedAt(now);
                nodeStatus.setUpdatedAt(now);

                nodeAiStatusMapper.insert(nodeStatus);
                log.info("Inserted node AI status: templateId={}, nodeId={}, aiStatus={}",
                        payload.getTemplateId(), payload.getNodeId(), payload.getAiStatus());
            }
        } catch (Exception e) {
            // Graceful degradation: log warning but don't fail the callback
            log.warn("Failed to upsert wf_node_ai_status (table may not exist yet): templateId={}, nodeId={}, error={}",
                    payload.getTemplateId(), payload.getNodeId(), e.getMessage());
        }

        return ApiResult.ok();
    }

    /**
     * 解析 standard_payload_json 中的节点级 AI 内容，并持久化到 wf_node_ai_status 表
     *
     * standard_payload_json 结构示例：
     * {
     *   "nodes": [
     *     {"node_id": "node_xxx", "ai_generated_content": "...", "summary": "..."},
     *     ...
     *   ]
     * }
     *
     * 对每个节点执行 upsert：若已存在则更新 result_json 和 ai_status，否则插入新记录。
     *
     * @param templateId 模板ID
     * @param standardPayloadJson standard_payload_json 内容（可能是 String 或已解析的 Object）
     */
    private void persistNodeLevelAiContent(Long templateId, Object standardPayloadJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root;

            // Parse standardPayloadJson - it may be a String or already a parsed object
            if (standardPayloadJson instanceof String) {
                root = mapper.readTree((String) standardPayloadJson);
            } else {
                root = mapper.valueToTree(standardPayloadJson);
            }

            // Extract nodes array from the payload
            JsonNode nodesArray = root.get("nodes");
            if (nodesArray == null || !nodesArray.isArray()) {
                log.debug("No nodes array found in standard_payload_json for templateId={}, skipping node-level write-back", templateId);
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            for (JsonNode nodeEntry : nodesArray) {
                String nodeId = nodeEntry.has("node_id") ? nodeEntry.get("node_id").asText() : null;
                if (nodeId == null || nodeId.isEmpty()) {
                    log.debug("Skipping node entry without node_id in standard_payload_json");
                    continue;
                }

                // Extract node type if available
                String nodeType = nodeEntry.has("node_type") ? nodeEntry.get("node_type").asText() : "unknown";

                // The entire node entry serves as the result_json content
                String resultJson = mapper.writeValueAsString(nodeEntry);

                // Upsert: check if record already exists
                LambdaQueryWrapper<WfNodeAiStatus> query = new LambdaQueryWrapper<>();
                query.eq(WfNodeAiStatus::getTemplateId, templateId)
                        .eq(WfNodeAiStatus::getNodeId, nodeId);
                WfNodeAiStatus existing = nodeAiStatusMapper.selectOne(query);

                if (existing != null) {
                    // Update existing record
                    LambdaUpdateWrapper<WfNodeAiStatus> update = new LambdaUpdateWrapper<>();
                    update.eq(WfNodeAiStatus::getId, existing.getId())
                            .set(WfNodeAiStatus::getResultJson, resultJson)
                            .set(WfNodeAiStatus::getAiStatus, 2)
                            .set(WfNodeAiStatus::getLastProcessedAt, now)
                            .set(WfNodeAiStatus::getUpdatedAt, now);
                    nodeAiStatusMapper.update(null, update);
                    log.debug("Updated node AI content: templateId={}, nodeId={}", templateId, nodeId);
                } else {
                    // Insert new record
                    WfNodeAiStatus nodeStatus = new WfNodeAiStatus();
                    nodeStatus.setTemplateId(templateId);
                    nodeStatus.setNodeId(nodeId);
                    nodeStatus.setNodeType(nodeType);
                    nodeStatus.setPhaseId("");
                    nodeStatus.setAiStatus(2);
                    nodeStatus.setRetryCount(0);
                    nodeStatus.setResultJson(resultJson);
                    nodeStatus.setLastProcessedAt(now);
                    nodeStatus.setCreatedAt(now);
                    nodeStatus.setUpdatedAt(now);
                    nodeAiStatusMapper.insert(nodeStatus);
                    log.debug("Inserted node AI content: templateId={}, nodeId={}", templateId, nodeId);
                }
            }

            log.info("Node-level AI content write-back completed for templateId={}, nodes processed={}",
                    templateId, nodesArray.size());
        } catch (Exception e) {
            // Graceful degradation: log warning but don't fail the callback
            log.warn("Failed to persist node-level AI content for templateId={}: {}", templateId, e.getMessage());
        }
    }

    /**
     * 通过WebSocket通知教师前端AI处理状态变更
     *
     * 向所有连接的客户端广播AI完成/失败通知。
     * 由于模板级别的通知不绑定特定taskId，使用templateId作为广播标识。
     *
     * @param templateId 模板ID
     * @param status AI处理状态 (2=成功, 3=失败)
     */
    private void notifyAiStatusChange(Long templateId, Integer status) {
        try {
            String statusText = (status == 2) ? "completed" : "failed";
            String message = String.format(
                    "{\"type\":\"ai_status_change\",\"template_id\":%d,\"ai_status\":%d,\"status_text\":\"%s\"}",
                    templateId, status, statusText);

            // Broadcast to all task sessions that might be related to this template
            // Since template-level notifications don't have a taskId, we broadcast broadly
            // The frontend will filter by template_id
            webSocketHandler.broadcastToTask(templateId, message);
            log.info("WebSocket notification sent: templateId={}, status={}", templateId, statusText);
        } catch (Exception e) {
            // WebSocket notification failure should not affect the callback processing
            log.warn("Failed to send WebSocket notification for templateId={}: {}", templateId, e.getMessage());
        }
    }

    /**
     * 持久化AI生成的题目
     *
     * 自动创建一个题库（biz_question_bank），然后将所有题目批量插入到 biz_question 表中，
     * 并设置 create_type=1 表示AI生成。
     *
     * @param questions AI生成的题目列表
     * @param template 关联的模板实体（用于获取创建者ID和模板名称）
     */
    private void persistAiGeneratedQuestions(List<QuestionDTO> questions, WfTrainingTemplate template) {
        log.info("Persisting {} AI-generated questions for templateId={}", questions.size(), template.getId());

        // Step 1: Auto-create a question bank linked to this template
        BizQuestionBank questionBank = new BizQuestionBank();
        questionBank.setTeacherId(template.getCreatorId());
        questionBank.setBankName("AI生成题库-" + template.getTemplateName());
        questionBank.setIsPublic(0); // Private by default
        questionBank.setCreatedAt(LocalDateTime.now());
        questionBank.setUpdatedAt(LocalDateTime.now());
        questionBankService.save(questionBank);

        log.info("Created question bank: id={}, bankName={}", questionBank.getId(), questionBank.getBankName());

        // Step 2: Map QuestionDTOs to BizQuestion entities
        List<BizQuestion> questionEntities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < questions.size(); i++) {
            QuestionDTO dto = questions.get(i);
            BizQuestion entity = new BizQuestion();
            entity.setBankId(questionBank.getId());
            entity.setQuestionType(dto.getQuestionType());
            entity.setContent(dto.getContent());
            entity.setStandardAnswer(dto.getStandardAnswer());
            entity.setDefaultScore(dto.getDefaultScore() != null
                    ? BigDecimal.valueOf(dto.getDefaultScore())
                    : BigDecimal.ZERO);
            entity.setSortNum(i + 1);
            entity.setCreateType(1); // AI-generated
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            questionEntities.add(entity);
        }

        // Step 3: Batch insert using MyBatis-Plus saveBatch()
        questionService.saveBatch(questionEntities);
        log.info("Batch inserted {} questions into biz_question for bankId={}", questionEntities.size(), questionBank.getId());
    }

    /**
     * 持久化AI生成的知识点
     *
     * 处理知识点的父子层级关系：AI生成的知识点使用相对parentId引用（0表示根节点，
     * 其他值引用列表中的索引位置）。插入时需要将相对引用映射为实际的数据库ID。
     *
     * 策略：
     * 1. 按层级顺序插入：先插入根节点（parentId=0或null），获取DB生成的ID
     * 2. 再插入子节点，将相对parentId映射为实际的DB ID
     *
     * @param knowledgePoints AI生成的知识点列表
     * @param template 关联的模板实体（用于获取模板ID作为courseId占位）
     */
    private void persistAiGeneratedKnowledgePoints(List<KnowledgePointDTO> knowledgePoints, WfTrainingTemplate template) {
        log.info("Persisting {} AI-generated knowledge points for templateId={}", knowledgePoints.size(), template.getId());

        LocalDateTime now = LocalDateTime.now();
        // Map from relative parentId (list index + 1) to actual DB-generated ID
        Map<Long, Long> relativeIdToDbId = new HashMap<>();

        // First pass: insert root-level knowledge points (parentId == 0 or null)
        for (int i = 0; i < knowledgePoints.size(); i++) {
            KnowledgePointDTO dto = knowledgePoints.get(i);
            if (dto.getParentId() == null || dto.getParentId() == 0L) {
                BizKnowledgePoint entity = new BizKnowledgePoint();
                entity.setCourseId(template.getId()); // Use templateId as courseId reference
                entity.setKpName(dto.getName());
                entity.setParentId(null); // Root node
                entity.setCreatedAt(now);
                entity.setUpdatedAt(now);
                knowledgePointService.save(entity);

                // Store mapping: relative index (i+1) -> actual DB ID
                relativeIdToDbId.put((long) (i + 1), entity.getId());
                log.debug("Inserted root knowledge point: index={}, dbId={}, name={}", i, entity.getId(), dto.getName());
            }
        }

        // Second pass: insert child knowledge points with resolved parent IDs
        for (int i = 0; i < knowledgePoints.size(); i++) {
            KnowledgePointDTO dto = knowledgePoints.get(i);
            if (dto.getParentId() != null && dto.getParentId() != 0L) {
                Long actualParentId = relativeIdToDbId.get(dto.getParentId());
                BizKnowledgePoint entity = new BizKnowledgePoint();
                entity.setCourseId(template.getId()); // Use templateId as courseId reference
                entity.setKpName(dto.getName());
                entity.setParentId(actualParentId); // Resolved actual DB parent ID
                entity.setCreatedAt(now);
                entity.setUpdatedAt(now);
                knowledgePointService.save(entity);

                // Store this node's mapping too (for deeper nesting)
                relativeIdToDbId.put((long) (i + 1), entity.getId());
                log.debug("Inserted child knowledge point: index={}, dbId={}, parentId={}, name={}",
                        i, entity.getId(), actualParentId, dto.getName());
            }
        }

        log.info("Persisted {} knowledge points into biz_knowledge_point for templateId={}",
                knowledgePoints.size(), template.getId());
    }
}