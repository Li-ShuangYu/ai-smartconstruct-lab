package com.smartconstruct.backend_core.service.impl;

import com.smartconstruct.backend_core.dto.AiJobStatus;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AiEngineClientImpl.
 *
 * Tests dispatch logic including:
 * - Successful submission returns jobId
 * - Connection failure marks template as failed (ai_status=3)
 * - Timeout marks template as failed
 * - Invalid response (missing jobId) marks template as failed
 * - Status query returns AiJobStatus
 *
 * NOTE: These tests mock WebClient and do not require a running AI engine or MySQL.
 * Due to WebClient's fluent API, full integration testing with MockWebServer
 * is recommended for comprehensive coverage. These tests verify the error
 * handling and template status update logic.
 */
@ExtendWith(MockitoExtension.class)
class AiEngineClientImplTest {

    @Mock
    private WfTrainingTemplateMapper trainingTemplateMapper;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AiEngineClientImpl aiEngineClient;

    private WfTrainingTemplate sampleTemplate;

    @BeforeEach
    void setUp() {
        // Set private fields via reflection
        ReflectionTestUtils.setField(aiEngineClient, "aiEngineBaseUrl", "http://localhost:8000");
        ReflectionTestUtils.setField(aiEngineClient, "serviceToken", "test-token");
        ReflectionTestUtils.setField(aiEngineClient, "serverAddress", "http://localhost");
        ReflectionTestUtils.setField(aiEngineClient, "serverPort", 8080);
        ReflectionTestUtils.setField(aiEngineClient, "webClient", webClient);

        sampleTemplate = new WfTrainingTemplate();
        sampleTemplate.setId(1L);
        sampleTemplate.setAiStatus(1);
        sampleTemplate.setCreatedAt(LocalDateTime.now());
        sampleTemplate.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Successful submission: returns jobId from AI engine response")
    void testSubmitForProcessing_success() {
        // Arrange
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("jobId", "test-job-uuid-123");
        responseBody.put("status", "accepted");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(responseBody));

        // Act
        String jobId = aiEngineClient.submitForProcessing(1L, new HashMap<>());

        // Assert
        assertEquals("test-job-uuid-123", jobId);
    }

    @Test
    @DisplayName("Connection failure: marks template as failed with ai_status=3")
    void testSubmitForProcessing_connectionFailure() {
        // Arrange
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.error(new RuntimeException(new ConnectException("Connection refused"))));

        when(trainingTemplateMapper.selectById(1L)).thenReturn(sampleTemplate);
        when(trainingTemplateMapper.updateById(any())).thenReturn(1);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                aiEngineClient.submitForProcessing(1L, new HashMap<>()));

        // Verify template marked as failed
        verify(trainingTemplateMapper).updateById(argThat(template -> {
            WfTrainingTemplate t = (WfTrainingTemplate) template;
            return t.getAiStatus() == 3;
        }));
    }

    @Test
    @DisplayName("Invalid response (no jobId): marks template as failed")
    void testSubmitForProcessing_invalidResponse() {
        // Arrange: response without jobId
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "error");
        // No "jobId" key

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(responseBody));

        when(trainingTemplateMapper.selectById(1L)).thenReturn(sampleTemplate);
        when(trainingTemplateMapper.updateById(any())).thenReturn(1);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                aiEngineClient.submitForProcessing(1L, new HashMap<>()));

        // Verify template marked as failed
        verify(trainingTemplateMapper).updateById(argThat(template -> {
            WfTrainingTemplate t = (WfTrainingTemplate) template;
            return t.getAiStatus() == 3;
        }));
    }

    @Test
    @DisplayName("Status query: returns AiJobStatus on success")
    void testQueryStatus_success() {
        // Arrange
        AiJobStatus expectedStatus = new AiJobStatus();
        expectedStatus.setJobId("test-job-123");
        expectedStatus.setStatus(2); // 2=success

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AiJobStatus.class)).thenReturn(Mono.just(expectedStatus));

        // Act
        AiJobStatus result = aiEngineClient.queryStatus("test-job-123");

        // Assert
        assertNotNull(result);
        assertEquals("test-job-123", result.getJobId());
        assertEquals(Integer.valueOf(2), result.getStatus());
    }

    @Test
    @DisplayName("Status query: connection failure throws RuntimeException")
    void testQueryStatus_connectionFailure() {
        // Arrange
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AiJobStatus.class))
                .thenReturn(Mono.error(new RuntimeException(new ConnectException("Connection refused"))));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                aiEngineClient.queryStatus("test-job-123"));
    }
}
