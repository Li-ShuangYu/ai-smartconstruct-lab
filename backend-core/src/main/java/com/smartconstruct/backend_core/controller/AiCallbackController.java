package com.smartconstruct.backend_core.controller;

import com.smartconstruct.backend_core.dto.AiCallbackRequest;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.KnowledgePointDTO;
import com.smartconstruct.backend_core.dto.QuestionDTO;
import com.smartconstruct.backend_core.entity.BizKnowledgePoint;
import com.smartconstruct.backend_core.entity.BizQuestion;
import com.smartconstruct.backend_core.entity.BizQuestionBank;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.IKnowledgePointService;
import com.smartconstruct.backend_core.service.IQuestionBankService;
import com.smartconstruct.backend_core.service.IQuestionService;
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

    @Autowired
    private WfTrainingTemplateMapper templateMapper;

    @Autowired
    private IQuestionBankService questionBankService;

    @Autowired
    private IQuestionService questionService;

    @Autowired
    private IKnowledgePointService knowledgePointService;

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