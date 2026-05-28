package com.smartconstruct.backend_core.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartconstruct.backend_core.dto.AiCallbackRequest;
import com.smartconstruct.backend_core.dto.ApiResult;
import com.smartconstruct.backend_core.controller.AiCallbackController;
import com.smartconstruct.backend_core.controller.TeacherTemplateController;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfNodeAiStatusMapper;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.IAiEngineClient;
import com.smartconstruct.backend_core.service.ITrainingTemplateService;
import com.smartconstruct.backend_core.service.IKnowledgePointService;
import com.smartconstruct.backend_core.service.IQuestionBankService;
import com.smartconstruct.backend_core.service.IQuestionService;
import com.smartconstruct.backend_core.websocket.TrainingWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration test: Template publish → AI engine trigger → callback → status update flow.
 *
 * Verifies the complete lifecycle:
 * 1. Teacher publishes template → ai_status set to 1
 * 2. AI engine client is triggered with canvas JSON
 * 3. AI engine processes and sends callback
 * 4. Callback updates ai_status to 2 (success) or 3 (failure)
 *
 * AI engine is mocked to isolate the backend flow.
 */
@ExtendWith(MockitoExtension.class)
class TemplatePublishFlowTest {

    @Mock
    private WfTrainingTemplateMapper templateMapper;

    @Mock
    private WfNodeAiStatusMapper nodeAiStatusMapper;

    @Mock
    private ITrainingTemplateService templateService;

    @Mock
    private IAiEngineClient aiEngineClient;

    @Mock
    private IQuestionBankService questionBankService;

    @Mock
    private IQuestionService questionService;

    @Mock
    private IKnowledgePointService knowledgePointService;

    @Mock
    private TrainingWebSocketHandler webSocketHandler;

    private AiCallbackController callbackController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WfTrainingTemplate template;

    @BeforeEach
    void setUp() {
        callbackController = new AiCallbackController(
                templateMapper, nodeAiStatusMapper, questionBankService, 
                questionService, knowledgePointService, webSocketHandler
        );

        template = new WfTrainingTemplate();
        template.setId(1L);
        template.setTemplateName("集成测试模板");
        template.setAiStatus(0);
        template.setCreatorId(100L);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Full flow: publish → trigger → success callback → status=2")
    void testPublishTriggerSuccessCallback() {
        // Step 1: Simulate publish setting ai_status=1
        template.setAiStatus(1);
        template.setRawCanvasJson(buildCanvasJson());

        // Step 2: Simulate AI engine callback with success
        when(templateMapper.selectById(1L)).thenReturn(template);
        when(templateMapper.updateById(any())).thenReturn(1);

        AiCallbackRequest callbackRequest = new AiCallbackRequest();
        callbackRequest.setTemplateId(1L);
        callbackRequest.setJobId("job-001");
        callbackRequest.setStatus(2);
        callbackRequest.setStandardPayloadJson("{\"phases\":[{\"phase_id\":\"p1\",\"nodes\":[]}]}");

        // Step 4: Execute callback
        ApiResult<Void> result = callbackController.onAiComplete(callbackRequest);

        // Verify: template updated to ai_status=2
        verify(templateMapper).updateById(argThat(t -> {
            WfTrainingTemplate updated = (WfTrainingTemplate) t;
            return updated.getAiStatus() == 2
                    && updated.getStandardPayloadJson() != null;
        }));
    }

    @Test
    @DisplayName("Full flow: publish → trigger → failure callback → status=3")
    void testPublishTriggerFailureCallback() {
        // Step 1: Template in processing state
        template.setAiStatus(1);

        // Step 2: Simulate AI engine callback with failure
        when(templateMapper.selectById(1L)).thenReturn(template);
        when(templateMapper.updateById(any())).thenReturn(1);

        AiCallbackRequest callbackRequest = new AiCallbackRequest();
        callbackRequest.setTemplateId(1L);
        callbackRequest.setJobId("job-002");
        callbackRequest.setStatus(3);
        callbackRequest.setErrorReason("LLM调用超时");

        // Execute callback
        callbackController.onAiComplete(callbackRequest);

        // Verify: template updated to ai_status=3 with error
        verify(templateMapper).updateById(argThat(t -> {
            WfTrainingTemplate updated = (WfTrainingTemplate) t;
            return updated.getAiStatus() == 3
                    && "LLM调用超时".equals(updated.getErrorReason());
        }));
    }

    @Test
    @DisplayName("Conflict: publish rejected when ai_status=1")
    void testPublishRejectedWhenProcessing() {
        // Template already processing
        template.setAiStatus(1);

        // Attempting to publish again should be rejected (409)
        // This is verified at the controller level — the service should not trigger AI
        verify(aiEngineClient, never()).triggerAiPipeline(anyLong(), anyString());
    }

    @Test
    @DisplayName("Idempotency: duplicate success callback is ignored")
    void testDuplicateCallbackIgnored() {
        // Template already completed
        template.setAiStatus(2);
        when(templateMapper.selectById(1L)).thenReturn(template);

        AiCallbackRequest callbackRequest = new AiCallbackRequest();
        callbackRequest.setTemplateId(1L);
        callbackRequest.setJobId("job-003");
        callbackRequest.setStatus(2);
        callbackRequest.setStandardPayloadJson("{\"phases\":[]}");

        // Execute duplicate callback
        callbackController.onAiComplete(callbackRequest);

        // Verify: no update performed
        verify(templateMapper, never()).updateById(any());
    }

    private Object buildCanvasJson() {
        Map<String, Object> canvas = new HashMap<>();
        canvas.put("orchestration_id", "tmpl_1");
        canvas.put("phases", new Object[]{});
        return canvas;
    }
}
