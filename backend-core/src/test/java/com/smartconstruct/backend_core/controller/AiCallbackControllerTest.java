package com.smartconstruct.backend_core.controller;

import com.smartconstruct.backend_core.dto.AiCallbackRequest;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.dto.GeneratedAssets;
import com.smartconstruct.backend_core.dto.QuestionDTO;
import com.smartconstruct.backend_core.dto.KnowledgePointDTO;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.IKnowledgePointService;
import com.smartconstruct.backend_core.service.IQuestionBankService;
import com.smartconstruct.backend_core.service.IQuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AiCallbackController.
 *
 * Tests callback handling logic including:
 * - Successful callback updates ai_status to 2
 * - Failed callback updates ai_status to 3
 * - Idempotency: duplicate callbacks are ignored
 * - Validation: missing fields return 400
 * - Template not found returns 404
 *
 * NOTE: These tests use Mockito and do not require a running MySQL database.
 */
@ExtendWith(MockitoExtension.class)
class AiCallbackControllerTest {

    @Mock
    private WfTrainingTemplateMapper templateMapper;

    @Mock
    private IQuestionBankService questionBankService;

    @Mock
    private IQuestionService questionService;

    @Mock
    private IKnowledgePointService knowledgePointService;

    @InjectMocks
    private AiCallbackController controller;

    private WfTrainingTemplate sampleTemplate;

    @BeforeEach
    void setUp() {
        sampleTemplate = new WfTrainingTemplate();
        sampleTemplate.setId(1L);
        sampleTemplate.setTemplateName("测试模板");
        sampleTemplate.setAiStatus(1); // Processing
        sampleTemplate.setCreatorId(100L);
        sampleTemplate.setCreatedAt(LocalDateTime.now());
        sampleTemplate.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Success callback: updates ai_status to 2 and persists standard_payload_json")
    void testSuccessCallback_updatesStatusTo2() {
        // Arrange
        when(templateMapper.selectById(1L)).thenReturn(sampleTemplate);
        when(templateMapper.updateById(any())).thenReturn(1);

        AiCallbackRequest request = new AiCallbackRequest();
        request.setTemplateId(1L);
        request.setJobId("job-uuid-001");
        request.setStatus(2);
        request.setStandardPayloadJson("{\"nodes\": [], \"edges\": []}");

        // Act
        ApiResult<Void> result = controller.onAiComplete(request);

        // Assert
        assertNotNull(result);
        verify(templateMapper).updateById(argThat(template -> {
            WfTrainingTemplate t = (WfTrainingTemplate) template;
            return t.getAiStatus() == 2 && t.getStandardPayloadJson() != null;
        }));
    }

    @Test
    @DisplayName("Failure callback: updates ai_status to 3 and persists error_reason")
    void testFailureCallback_updatesStatusTo3() {
        // Arrange
        when(templateMapper.selectById(1L)).thenReturn(sampleTemplate);
        when(templateMapper.updateById(any())).thenReturn(1);

        AiCallbackRequest request = new AiCallbackRequest();
        request.setTemplateId(1L);
        request.setJobId("job-uuid-002");
        request.setStatus(3);
        request.setErrorReason("AI处理超时");

        // Act
        ApiResult<Void> result = controller.onAiComplete(request);

        // Assert
        assertNotNull(result);
        verify(templateMapper).updateById(argThat(template -> {
            WfTrainingTemplate t = (WfTrainingTemplate) template;
            return t.getAiStatus() == 3 && "AI处理超时".equals(t.getErrorReason());
        }));
    }

    @Test
    @DisplayName("Idempotency: duplicate callback for ai_status=2 is ignored")
    void testDuplicateCallback_ignored() {
        // Arrange: template already has ai_status=2
        sampleTemplate.setAiStatus(2);
        when(templateMapper.selectById(1L)).thenReturn(sampleTemplate);

        AiCallbackRequest request = new AiCallbackRequest();
        request.setTemplateId(1L);
        request.setJobId("job-uuid-003");
        request.setStatus(2);
        request.setStandardPayloadJson("{\"nodes\": []}");

        // Act
        ApiResult<Void> result = controller.onAiComplete(request);

        // Assert: updateById should NOT be called (idempotent)
        verify(templateMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("Validation: null templateId returns error")
    void testNullTemplateId_returnsError() {
        // Arrange
        AiCallbackRequest request = new AiCallbackRequest();
        request.setTemplateId(null);
        request.setStatus(2);

        // Act
        ApiResult<Void> result = controller.onAiComplete(request);

        // Assert: should return error without DB interaction
        verify(templateMapper, never()).selectById(any());
    }

    @Test
    @DisplayName("Validation: null status returns error")
    void testNullStatus_returnsError() {
        // Arrange
        AiCallbackRequest request = new AiCallbackRequest();
        request.setTemplateId(1L);
        request.setStatus(null);

        // Act
        ApiResult<Void> result = controller.onAiComplete(request);

        // Assert: should return error without DB update
        verify(templateMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("Template not found: returns 404 error")
    void testTemplateNotFound_returns404() {
        // Arrange
        when(templateMapper.selectById(999L)).thenReturn(null);

        AiCallbackRequest request = new AiCallbackRequest();
        request.setTemplateId(999L);
        request.setJobId("job-uuid-004");
        request.setStatus(2);

        // Act
        ApiResult<Void> result = controller.onAiComplete(request);

        // Assert: no update attempted
        verify(templateMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("Invalid status value returns error")
    void testInvalidStatus_returnsError() {
        // Arrange
        when(templateMapper.selectById(1L)).thenReturn(sampleTemplate);

        AiCallbackRequest request = new AiCallbackRequest();
        request.setTemplateId(1L);
        request.setJobId("job-uuid-005");
        request.setStatus(99); // Invalid status

        // Act
        ApiResult<Void> result = controller.onAiComplete(request);

        // Assert: no update attempted for invalid status
        verify(templateMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("Success callback with questions: triggers question persistence")
    void testSuccessWithQuestions_persistsQuestions() {
        // Arrange
        when(templateMapper.selectById(1L)).thenReturn(sampleTemplate);
        when(templateMapper.updateById(any())).thenReturn(1);
        when(questionBankService.save(any())).thenReturn(true);
        when(questionService.saveBatch(anyList())).thenReturn(true);

        QuestionDTO q1 = new QuestionDTO();
        q1.setQuestionType(1);
        q1.setContent("{\"stem\": \"Test question\"}");
        q1.setStandardAnswer("A");
        q1.setDefaultScore(5.0);

        GeneratedAssets assets = new GeneratedAssets();
        assets.setQuestions(Arrays.asList(q1));

        AiCallbackRequest request = new AiCallbackRequest();
        request.setTemplateId(1L);
        request.setJobId("job-uuid-006");
        request.setStatus(2);
        request.setStandardPayloadJson("{\"nodes\": []}");
        request.setGeneratedAssets(assets);

        // Act
        controller.onAiComplete(request);

        // Assert: question bank created and questions batch-inserted
        verify(questionBankService).save(any());
        verify(questionService).saveBatch(anyList());
    }

    @Test
    @DisplayName("Success callback with knowledge points: triggers KP persistence")
    void testSuccessWithKnowledgePoints_persistsKPs() {
        // Arrange
        when(templateMapper.selectById(1L)).thenReturn(sampleTemplate);
        when(templateMapper.updateById(any())).thenReturn(1);
        when(knowledgePointService.save(any())).thenReturn(true);

        KnowledgePointDTO kp1 = new KnowledgePointDTO();
        kp1.setName("知识点A");
        kp1.setParentId(0L);

        GeneratedAssets assets = new GeneratedAssets();
        assets.setKnowledgePoints(Arrays.asList(kp1));

        AiCallbackRequest request = new AiCallbackRequest();
        request.setTemplateId(1L);
        request.setJobId("job-uuid-007");
        request.setStatus(2);
        request.setStandardPayloadJson("{\"nodes\": []}");
        request.setGeneratedAssets(assets);

        // Act
        controller.onAiComplete(request);

        // Assert: knowledge point persisted
        verify(knowledgePointService, atLeastOnce()).save(any());
    }
}
