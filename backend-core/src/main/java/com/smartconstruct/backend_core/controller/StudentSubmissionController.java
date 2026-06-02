package com.smartconstruct.backend_core.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.entity.*;
import com.smartconstruct.backend_core.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 学生节点数据提交控制器
 *
 * 提供统一的节点数据提交入口，按node_type路由到不同的处理逻辑。
 * 支持的节点类型：
 * - resource_read: 阅读数据采集
 * - coding_class: 代码提交 + AI代码审查
 * - homework/exam: 答题提交 + AI批改
 * - req_upload/plan_upload: 文件上传 + AI预评审
 * - mindmap_draw: 思维导图提交 + AI结构评价
 * - student_peer_review/inter_group_review: 互评提交
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/student/nodes")
public class StudentSubmissionController {

    private static final Logger log = LoggerFactory.getLogger(StudentSubmissionController.class);

    private final WfNodeInstanceMapper nodeInstanceMapper;
    private final WfStudentNodeProgressMapper studentNodeProgressMapper;
    private final BizTrainingParticipationMapper participationMapper;
    private final BizTrainingUploadMapper trainingUploadMapper;
    private final BizStudentAnswerDetailMapper studentAnswerDetailMapper;
    private final BizMindmapRecordMapper mindmapRecordMapper;
    private final FactEvalResultMapper factEvalResultMapper;

    public StudentSubmissionController(
            WfNodeInstanceMapper nodeInstanceMapper,
            WfStudentNodeProgressMapper studentNodeProgressMapper,
            BizTrainingParticipationMapper participationMapper,
            BizTrainingUploadMapper trainingUploadMapper,
            BizStudentAnswerDetailMapper studentAnswerDetailMapper,
            BizMindmapRecordMapper mindmapRecordMapper,
            FactEvalResultMapper factEvalResultMapper) {
        this.nodeInstanceMapper = nodeInstanceMapper;
        this.studentNodeProgressMapper = studentNodeProgressMapper;
        this.participationMapper = participationMapper;
        this.trainingUploadMapper = trainingUploadMapper;
        this.studentAnswerDetailMapper = studentAnswerDetailMapper;
        this.mindmapRecordMapper = mindmapRecordMapper;
        this.factEvalResultMapper = factEvalResultMapper;
    }

    private Long getCurrentUserId() {
        SysUser user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    // ==================== Step 1: Generic submission endpoint ====================

    /**
     * POST /api/student/nodes/{nodeInstanceId}/submit
     *
     * Generic submission endpoint that routes by node_type.
     * The request body varies by node type.
     */
    @PostMapping("/{nodeInstanceId}/submit")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Map<String, Object>> submit(
            @PathVariable Long nodeInstanceId,
            @RequestBody Map<String, Object> body) {

        Long currentUserId = getCurrentUserId();

        // Validate node instance exists
        WfNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
        if (nodeInstance == null) {
            return ApiResult.error("节点实例不存在");
        }

        // Find participation record
        BizTrainingParticipation participation = getParticipation(nodeInstance.getTaskId(), currentUserId);
        if (participation == null) {
            return ApiResult.error("未找到该实训任务的参与记录");
        }

        // Route by node_type
        String nodeType = nodeInstance.getNodeType();
        if (nodeType == null || nodeType.trim().isEmpty()) {
            return ApiResult.error("节点类型未定义");
        }

        switch (nodeType.toLowerCase()) {
            case "resource_read":
                return handleResourceRead(nodeInstanceId, participation, body);
            case "coding_class":
                return handleCodingClass(nodeInstanceId, participation, body);
            case "homework":
            case "exam":
                return handleHomeworkExam(nodeInstanceId, participation, body);
            case "req_upload":
            case "plan_upload":
                return handleFileUpload(nodeInstanceId, participation, body, nodeType);
            case "mindmap_draw":
                return handleMindmapDraw(nodeInstanceId, participation, body);
            case "student_peer_review":
            case "inter_group_review":
                return handlePeerReview(nodeInstanceId, participation, body, nodeType);
            default:
                return ApiResult.error("不支持的节点类型提交: " + nodeType);
        }
    }

    // ==================== Step 2: resource_read data collection ====================

    /**
     * Collect reading data: reading_duration_seconds, scroll_percentage, knowledge_point_clicks.
     * Stores in wf_student_node_progress.node_specific_data_json.
     */
    private ApiResult<Map<String, Object>> handleResourceRead(
            Long nodeInstanceId, BizTrainingParticipation participation, Map<String, Object> body) {

        // Validate required fields
        Integer readingDuration = parseInteger(body.get("reading_duration_seconds"));
        Integer scrollPercentage = parseInteger(body.get("scroll_percentage"));

        if (readingDuration == null) {
            return ApiResult.error("reading_duration_seconds 不能为空");
        }
        if (scrollPercentage == null || scrollPercentage < 0 || scrollPercentage > 100) {
            return ApiResult.error("scroll_percentage 必须为0-100的整数");
        }

        // Build node_specific_data_json
        Map<String, Object> specificData = new LinkedHashMap<>();
        specificData.put("reading_duration_seconds", readingDuration);
        specificData.put("scroll_percentage", scrollPercentage);
        if (body.containsKey("knowledge_point_clicks")) {
            specificData.put("knowledge_point_clicks", body.get("knowledge_point_clicks"));
        }

        // Update wf_student_node_progress
        WfStudentNodeProgress progress = getOrCreateProgress(participation.getId(), nodeInstanceId);
        progress.setNodeSpecificDataJson(specificData);
        progress.setUpdatedAt(LocalDateTime.now());
        studentNodeProgressMapper.updateById(progress);

        Map<String, Object> result = new HashMap<>();
        result.put("nodeInstanceId", String.valueOf(nodeInstanceId));
        result.put("status", "saved");
        return ApiResult.ok(result);
    }

    // ==================== Step 3: coding_class submission ====================

    /**
     * Code submission: code content → biz_training_upload (upload_type=2), trigger async AI code review.
     */
    private ApiResult<Map<String, Object>> handleCodingClass(
            Long nodeInstanceId, BizTrainingParticipation participation, Map<String, Object> body) {

        String codeContent = (String) body.get("code_content");
        if (codeContent == null || codeContent.trim().isEmpty()) {
            return ApiResult.error("code_content 不能为空");
        }

        LocalDateTime now = LocalDateTime.now();

        // Create biz_training_upload record (upload_type=2 for code)
        BizTrainingUpload upload = new BizTrainingUpload();
        upload.setParticipationId(participation.getId());
        upload.setNodeInstanceId(nodeInstanceId);
        upload.setUploadType(2); // code
        upload.setResourceId(0L); // code content stored inline, no resource file
        upload.setStatus(0); // 待AI审
        upload.setCreatedAt(now);
        upload.setUpdatedAt(now);
        trainingUploadMapper.insert(upload);

        // Store code content in node_specific_data_json for reference
        WfStudentNodeProgress progress = getOrCreateProgress(participation.getId(), nodeInstanceId);
        Map<String, Object> specificData = new LinkedHashMap<>();
        specificData.put("code_content", codeContent);
        specificData.put("upload_id", String.valueOf(upload.getId()));
        specificData.put("submitted_at", now.toString());
        if (body.containsKey("language")) {
            specificData.put("language", body.get("language"));
        }
        progress.setNodeSpecificDataJson(specificData);
        progress.setUpdatedAt(now);
        studentNodeProgressMapper.updateById(progress);

        // Trigger async AI code review (stub — logs intent)
        log.info("[AI-TRIGGER] Code review requested for upload_id={}, node_instance_id={}, participation_id={}",
                upload.getId(), nodeInstanceId, participation.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("uploadId", String.valueOf(upload.getId()));
        result.put("status", "submitted");
        result.put("aiReviewStatus", "pending");
        return ApiResult.ok(result);
    }

    // ==================== Step 4: homework/exam submission ====================

    /**
     * Homework/exam answers → biz_student_answer_detail, trigger AI grading for subjective questions.
     */
    @SuppressWarnings("unchecked")
    private ApiResult<Map<String, Object>> handleHomeworkExam(
            Long nodeInstanceId, BizTrainingParticipation participation, Map<String, Object> body) {

        Object answersObj = body.get("answers");
        if (answersObj == null) {
            return ApiResult.error("answers 不能为空");
        }

        List<Map<String, Object>> answers;
        if (answersObj instanceof List) {
            answers = (List<Map<String, Object>>) answersObj;
        } else {
            return ApiResult.error("answers 必须为数组格式");
        }

        if (answers.isEmpty()) {
            return ApiResult.error("answers 不能为空数组");
        }

        // Validate each answer has required fields
        for (int i = 0; i < answers.size(); i++) {
            Map<String, Object> answer = answers.get(i);
            if (answer.get("question_id") == null) {
                return ApiResult.error("第" + (i + 1) + "题缺少 question_id");
            }
            if (answer.get("student_answer") == null || answer.get("student_answer").toString().trim().isEmpty()) {
                return ApiResult.error("第" + (i + 1) + "题 student_answer 不能为空");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        Long studentPaperId = parseLong(body.get("student_paper_id"));
        List<String> savedIds = new ArrayList<>();
        boolean hasSubjective = false;

        for (Map<String, Object> answer : answers) {
            BizStudentAnswerDetail detail = new BizStudentAnswerDetail();
            detail.setStudentPaperId(studentPaperId != null ? studentPaperId : 0L);
            detail.setPaperQuestionId(parseLong(answer.get("question_id")));
            detail.setStudentAnswer(answer.get("student_answer").toString());
            detail.setCreatedAt(now);
            detail.setUpdatedAt(now);
            studentAnswerDetailMapper.insert(detail);
            savedIds.add(String.valueOf(detail.getId()));

            // Check if question is subjective (needs AI grading)
            String questionType = answer.get("question_type") != null ? answer.get("question_type").toString() : "";
            if ("subjective".equals(questionType) || "short_answer".equals(questionType)) {
                hasSubjective = true;
            }
        }

        // Trigger async AI grading for subjective questions
        if (hasSubjective) {
            log.info("[AI-TRIGGER] AI grading requested for node_instance_id={}, participation_id={}, answer_count={}",
                    nodeInstanceId, participation.getId(), answers.size());
        }

        // Update progress
        WfStudentNodeProgress progress = getOrCreateProgress(participation.getId(), nodeInstanceId);
        Map<String, Object> specificData = new LinkedHashMap<>();
        specificData.put("answer_count", answers.size());
        specificData.put("submitted_at", now.toString());
        specificData.put("has_subjective", hasSubjective);
        progress.setNodeSpecificDataJson(specificData);
        progress.setUpdatedAt(now);
        studentNodeProgressMapper.updateById(progress);

        Map<String, Object> result = new HashMap<>();
        result.put("savedCount", savedIds.size());
        result.put("aiGradingTriggered", hasSubjective);
        result.put("status", "submitted");
        return ApiResult.ok(result);
    }

    // ==================== Step 5: file upload submissions (req_upload, plan_upload) ====================

    /**
     * File upload → biz_training_upload (upload_type=1), trigger AI pre-review.
     */
    private ApiResult<Map<String, Object>> handleFileUpload(
            Long nodeInstanceId, BizTrainingParticipation participation,
            Map<String, Object> body, String nodeType) {

        Long resourceId = parseLong(body.get("resource_id"));
        if (resourceId == null) {
            return ApiResult.error("resource_id 不能为空");
        }

        LocalDateTime now = LocalDateTime.now();

        // Create biz_training_upload record (upload_type=1 for document)
        BizTrainingUpload upload = new BizTrainingUpload();
        upload.setParticipationId(participation.getId());
        upload.setNodeInstanceId(nodeInstanceId);
        upload.setUploadType(1); // document
        upload.setResourceId(resourceId);
        upload.setStatus(0); // 待AI审
        upload.setCreatedAt(now);
        upload.setUpdatedAt(now);
        trainingUploadMapper.insert(upload);

        // Update progress
        WfStudentNodeProgress progress = getOrCreateProgress(participation.getId(), nodeInstanceId);
        Map<String, Object> specificData = new LinkedHashMap<>();
        specificData.put("upload_id", String.valueOf(upload.getId()));
        specificData.put("resource_id", String.valueOf(resourceId));
        specificData.put("submitted_at", now.toString());
        if (body.containsKey("file_name")) {
            specificData.put("file_name", body.get("file_name"));
        }
        progress.setNodeSpecificDataJson(specificData);
        progress.setUpdatedAt(now);
        studentNodeProgressMapper.updateById(progress);

        // Trigger async AI pre-review
        log.info("[AI-TRIGGER] File pre-review requested for upload_id={}, node_type={}, node_instance_id={}",
                upload.getId(), nodeType, nodeInstanceId);

        Map<String, Object> result = new HashMap<>();
        result.put("uploadId", String.valueOf(upload.getId()));
        result.put("status", "submitted");
        result.put("aiReviewStatus", "pending");
        return ApiResult.ok(result);
    }

    // ==================== Step 6: mindmap submissions ====================

    /**
     * Mindmap submission: map_topology_json → biz_mindmap_record, trigger AI structure evaluation.
     */
    private ApiResult<Map<String, Object>> handleMindmapDraw(
            Long nodeInstanceId, BizTrainingParticipation participation, Map<String, Object> body) {

        Object mapTopologyJson = body.get("map_topology_json");
        if (mapTopologyJson == null) {
            return ApiResult.error("map_topology_json 不能为空");
        }

        LocalDateTime now = LocalDateTime.now();

        // Check if record already exists (update) or create new
        LambdaQueryWrapper<BizMindmapRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(BizMindmapRecord::getParticipationId, participation.getId())
                .eq(BizMindmapRecord::getNodeId, String.valueOf(nodeInstanceId));
        BizMindmapRecord existing = mindmapRecordMapper.selectOne(qw);

        if (existing != null) {
            existing.setMindmapJson(mapTopologyJson);
            existing.setUpdatedAt(now);
            mindmapRecordMapper.updateById(existing);
        } else {
            BizMindmapRecord record = new BizMindmapRecord();
            record.setParticipationId(participation.getId());
            record.setNodeId(String.valueOf(nodeInstanceId));
            record.setMindmapJson(mapTopologyJson);
            record.setCreatedAt(now);
            record.setUpdatedAt(now);
            mindmapRecordMapper.insert(record);
        }

        // Update progress
        WfStudentNodeProgress progress = getOrCreateProgress(participation.getId(), nodeInstanceId);
        Map<String, Object> specificData = new LinkedHashMap<>();
        specificData.put("submitted_at", now.toString());
        specificData.put("has_topology", true);
        progress.setNodeSpecificDataJson(specificData);
        progress.setUpdatedAt(now);
        studentNodeProgressMapper.updateById(progress);

        // Trigger async AI structure evaluation
        log.info("[AI-TRIGGER] Mindmap structure evaluation requested for node_instance_id={}, participation_id={}",
                nodeInstanceId, participation.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("status", "submitted");
        result.put("aiEvalStatus", "pending");
        return ApiResult.ok(result);
    }

    // ==================== Step 7: peer review submissions ====================

    /**
     * Peer review: scores + comments → fact_eval_result.
     */
    @SuppressWarnings("unchecked")
    private ApiResult<Map<String, Object>> handlePeerReview(
            Long nodeInstanceId, BizTrainingParticipation participation,
            Map<String, Object> body, String nodeType) {

        Object evaluationsObj = body.get("evaluations");
        if (evaluationsObj == null) {
            return ApiResult.error("evaluations 不能为空");
        }

        List<Map<String, Object>> evaluations;
        if (evaluationsObj instanceof List) {
            evaluations = (List<Map<String, Object>>) evaluationsObj;
        } else {
            return ApiResult.error("evaluations 必须为数组格式");
        }

        if (evaluations.isEmpty()) {
            return ApiResult.error("evaluations 不能为空数组");
        }

        // Validate each evaluation
        for (int i = 0; i < evaluations.size(); i++) {
            Map<String, Object> eval = evaluations.get(i);
            if (eval.get("target_id") == null) {
                return ApiResult.error("第" + (i + 1) + "条评价缺少 target_id");
            }
            if (eval.get("indicator_id") == null) {
                return ApiResult.error("第" + (i + 1) + "条评价缺少 indicator_id");
            }
            if (eval.get("score") == null) {
                return ApiResult.error("第" + (i + 1) + "条评价缺少 score");
            }
        }

        // Get task_id from node instance
        WfNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
        Long taskId = nodeInstance != null ? nodeInstance.getTaskId() : 0L;

        LocalDateTime now = LocalDateTime.now();
        int targetType = "inter_group_review".equals(nodeType) ? 2 : 1; // 2=group, 1=person
        List<String> savedIds = new ArrayList<>();

        for (Map<String, Object> eval : evaluations) {
            FactEvalResult result = new FactEvalResult();
            result.setTaskId(taskId);
            result.setTargetType(targetType);
            result.setTargetId(parseLong(eval.get("target_id")));
            result.setIndicatorId(eval.get("indicator_id").toString());

            BigDecimal score = parseBigDecimal(eval.get("score"));
            result.setRawScore(score != null ? score : BigDecimal.ZERO);
            result.setCalcScore(score != null ? score : BigDecimal.ZERO);

            if (eval.containsKey("comment")) {
                result.setTeacherComment(null); // teacher comment set later
                // Store peer comment in ai_comment field for now (peer-generated)
                result.setAiComment(eval.get("comment") != null ? eval.get("comment").toString() : null);
            }

            if (eval.containsKey("peer_review_id")) {
                result.setPeerReviewId(parseLong(eval.get("peer_review_id")));
            }

            result.setCreatedAt(now);
            factEvalResultMapper.insert(result);
            savedIds.add(String.valueOf(result.getId()));
        }

        // Update progress
        WfStudentNodeProgress progress = getOrCreateProgress(participation.getId(), nodeInstanceId);
        Map<String, Object> specificData = new LinkedHashMap<>();
        specificData.put("evaluation_count", evaluations.size());
        specificData.put("submitted_at", now.toString());
        progress.setNodeSpecificDataJson(specificData);
        progress.setUpdatedAt(now);
        studentNodeProgressMapper.updateById(progress);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("savedCount", savedIds.size());
        resultMap.put("status", "submitted");
        return ApiResult.ok(resultMap);
    }

    // ==================== Helper Methods ====================

    /**
     * Get participation record for current student and task.
     */
    private BizTrainingParticipation getParticipation(Long taskId, Long studentId) {
        LambdaQueryWrapper<BizTrainingParticipation> qw = new LambdaQueryWrapper<>();
        qw.eq(BizTrainingParticipation::getTaskId, taskId)
                .eq(BizTrainingParticipation::getStudentId, studentId)
                .orderByDesc(BizTrainingParticipation::getId)
                .last("LIMIT 1");
        return participationMapper.selectOne(qw);
    }

    /**
     * Get or create a student node progress record.
     */
    private WfStudentNodeProgress getOrCreateProgress(Long participationId, Long nodeInstanceId) {
        LambdaQueryWrapper<WfStudentNodeProgress> qw = new LambdaQueryWrapper<>();
        qw.eq(WfStudentNodeProgress::getParticipationId, participationId)
                .eq(WfStudentNodeProgress::getNodeInstanceId, nodeInstanceId);
        WfStudentNodeProgress progress = studentNodeProgressMapper.selectOne(qw);

        if (progress == null) {
            progress = new WfStudentNodeProgress();
            progress.setParticipationId(participationId);
            progress.setNodeInstanceId(nodeInstanceId);
            progress.setStatus(1); // 进行中
            progress.setIsForcedComplete(0);
            progress.setCreatedAt(LocalDateTime.now());
            progress.setUpdatedAt(LocalDateTime.now());
            studentNodeProgressMapper.insert(progress);
        }

        return progress;
    }

    private Integer parseInteger(Object value) {
        if (value == null) return null;
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return null;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
