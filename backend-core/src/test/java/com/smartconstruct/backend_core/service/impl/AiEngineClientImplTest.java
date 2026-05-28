package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartconstruct.backend_core.dto.AiJobStatus;
import com.smartconstruct.backend_core.entity.WfNodeDef;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfNodeAiStatusMapper;
import com.smartconstruct.backend_core.mapper.WfNodeDefMapper;
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

import java.lang.reflect.Method;
import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
 * - enrichWithAiSpecs() dual-format support (flat config and 6-dimension config)
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
    private WfNodeDefMapper nodeDefMapper;

    @Mock
    private WfNodeAiStatusMapper nodeAiStatusMapper;

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

    // ========== enrichWithAiSpecs() Dual-Format Support Tests ==========

    @Test
    @DisplayName("enrichWithAiSpecs: injects ai_spec for node with flat config format")
    void testEnrichWithAiSpecs_flatConfigFormat() throws Exception {
        // Arrange: canvas with a node using flat config (typical frontend submission)
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode canvas = mapper.createObjectNode();
        ArrayNode phases = mapper.createArrayNode();
        ObjectNode phase = mapper.createObjectNode();
        phase.put("phase_id", "phase_1");
        phase.put("phase_name", "课前阶段");
        phase.put("sort_num", 1);

        ArrayNode nodes = mapper.createArrayNode();
        ObjectNode node = mapper.createObjectNode();
        node.put("node_id", "node_1");
        node.put("node_type", "resource_read");
        node.put("node_name", "阅读资料");
        // Flat config format - no 6-dimension structure
        ObjectNode config = mapper.createObjectNode();
        config.put("resource_id", 123);
        config.put("min_reading_duration", 300);
        node.set("config", config);
        nodes.add(node);

        phase.set("nodes", nodes);
        phases.add(phase);
        canvas.set("phases", phases);

        // Mock wf_node_def lookup to return ai_spec_json for resource_read
        WfNodeDef nodeDef = new WfNodeDef();
        nodeDef.setNodeType("resource_read");
        Map<String, Object> aiSpec = new LinkedHashMap<>();
        aiSpec.put("target_agent", "TEXT");
        aiSpec.put("priority", 3);
        nodeDef.setAiSpecJson(aiSpec);

        when(nodeDefMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(nodeDef);

        // Inject ObjectMapper via reflection
        ReflectionTestUtils.setField(aiEngineClient, "objectMapper", mapper);

        // Act: invoke enrichWithAiSpecs via reflection (private method)
        Method enrichMethod = AiEngineClientImpl.class.getDeclaredMethod("enrichWithAiSpecs", ObjectNode.class);
        enrichMethod.setAccessible(true);
        enrichMethod.invoke(aiEngineClient, canvas);

        // Assert: ai_spec should be injected regardless of config format
        JsonNode enrichedNode = canvas.get("phases").get(0).get("nodes").get(0);
        assertNotNull(enrichedNode.get("ai_spec"),
                "ai_spec should be injected for flat-config node (node_type is at node level, not inside config)");
        assertEquals("TEXT", enrichedNode.get("ai_spec").get("target_agent").asText());
        assertEquals(3, enrichedNode.get("ai_spec").get("priority").asInt());
    }

    @Test
    @DisplayName("enrichWithAiSpecs: injects ai_spec for node with 6-dimension config format")
    void testEnrichWithAiSpecs_sixDimensionConfigFormat() throws Exception {
        // Arrange: canvas with a node using 6-dimension config
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode canvas = mapper.createObjectNode();
        ArrayNode phases = mapper.createArrayNode();
        ObjectNode phase = mapper.createObjectNode();
        phase.put("phase_id", "phase_1");
        phase.put("phase_name", "课中阶段");
        phase.put("sort_num", 2);

        ArrayNode nodes = mapper.createArrayNode();
        ObjectNode node = mapper.createObjectNode();
        node.put("node_id", "node_2");
        node.put("node_type", "coding_class");
        node.put("node_name", "编码实训");
        // 6-dimension config format
        ObjectNode config = mapper.createObjectNode();
        config.set("display", mapper.createObjectNode().put("show_ai_review_panel", true));
        config.set("db_mapping", mapper.createObjectNode());
        config.set("ai_processing", mapper.createObjectNode().put("enable_ai_code_review", true));
        config.set("orchestration_settings", mapper.createObjectNode().put("environment_type", "java"));
        config.set("data_collection", mapper.createObjectNode().put("track_submissions", true));
        config.set("evaluation", mapper.createObjectNode().put("max_score", 100));
        node.set("config", config);
        nodes.add(node);

        phase.set("nodes", nodes);
        phases.add(phase);
        canvas.set("phases", phases);

        // Mock wf_node_def lookup
        WfNodeDef nodeDef = new WfNodeDef();
        nodeDef.setNodeType("coding_class");
        Map<String, Object> aiSpec = new LinkedHashMap<>();
        aiSpec.put("target_agent", "CODE");
        aiSpec.put("priority", 4);
        nodeDef.setAiSpecJson(aiSpec);

        when(nodeDefMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(nodeDef);

        // Inject ObjectMapper via reflection
        ReflectionTestUtils.setField(aiEngineClient, "objectMapper", mapper);

        // Act
        Method enrichMethod = AiEngineClientImpl.class.getDeclaredMethod("enrichWithAiSpecs", ObjectNode.class);
        enrichMethod.setAccessible(true);
        enrichMethod.invoke(aiEngineClient, canvas);

        // Assert: ai_spec should be injected for 6-dimension config node too
        JsonNode enrichedNode = canvas.get("phases").get(0).get("nodes").get(0);
        assertNotNull(enrichedNode.get("ai_spec"),
                "ai_spec should be injected for 6-dimension-config node");
        assertEquals("CODE", enrichedNode.get("ai_spec").get("target_agent").asText());
        assertEquals(4, enrichedNode.get("ai_spec").get("priority").asInt());
    }

    @Test
    @DisplayName("enrichWithAiSpecs: handles mixed format nodes in same canvas")
    void testEnrichWithAiSpecs_mixedFormats() throws Exception {
        // Arrange: canvas with both flat and 6-dimension config nodes
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode canvas = mapper.createObjectNode();
        ArrayNode phases = mapper.createArrayNode();
        ObjectNode phase = mapper.createObjectNode();
        phase.put("phase_id", "phase_1");
        phase.put("phase_name", "课前阶段");
        phase.put("sort_num", 1);

        ArrayNode nodes = mapper.createArrayNode();

        // Node 1: flat config (video_watch)
        ObjectNode node1 = mapper.createObjectNode();
        node1.put("node_id", "node_flat");
        node1.put("node_type", "video_watch");
        node1.put("node_name", "观看视频");
        ObjectNode flatConfig = mapper.createObjectNode();
        flatConfig.put("video_url", "https://example.com/video.mp4");
        flatConfig.put("auto_play", true);
        node1.set("config", flatConfig);
        nodes.add(node1);

        // Node 2: 6-dimension config (ai_practice)
        ObjectNode node2 = mapper.createObjectNode();
        node2.put("node_id", "node_6dim");
        node2.put("node_type", "ai_practice");
        node2.put("node_name", "AI对练");
        ObjectNode sixDimConfig = mapper.createObjectNode();
        sixDimConfig.set("display", mapper.createObjectNode());
        sixDimConfig.set("db_mapping", mapper.createObjectNode());
        sixDimConfig.set("ai_processing", mapper.createObjectNode().put("mode", "socratic"));
        sixDimConfig.set("orchestration_settings", mapper.createObjectNode());
        sixDimConfig.set("data_collection", mapper.createObjectNode());
        sixDimConfig.set("evaluation", mapper.createObjectNode());
        node2.set("config", sixDimConfig);
        nodes.add(node2);

        phase.set("nodes", nodes);
        phases.add(phase);
        canvas.set("phases", phases);

        // Mock wf_node_def lookups for both node types
        WfNodeDef videoWatchDef = new WfNodeDef();
        videoWatchDef.setNodeType("video_watch");
        Map<String, Object> videoSpec = new LinkedHashMap<>();
        videoSpec.put("target_agent", "TEXT");
        videoSpec.put("priority", 3);
        videoWatchDef.setAiSpecJson(videoSpec);

        WfNodeDef aiPracticeDef = new WfNodeDef();
        aiPracticeDef.setNodeType("ai_practice");
        Map<String, Object> aiPracticeSpec = new LinkedHashMap<>();
        aiPracticeSpec.put("target_agent", "SOCRATIC");
        aiPracticeSpec.put("priority", 1);
        aiPracticeDef.setAiSpecJson(aiPracticeSpec);

        // Return appropriate def for sequential calls (video_watch first, then ai_practice)
        when(nodeDefMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(videoWatchDef)
                .thenReturn(aiPracticeDef);

        // Inject ObjectMapper via reflection
        ReflectionTestUtils.setField(aiEngineClient, "objectMapper", mapper);

        // Act
        Method enrichMethod = AiEngineClientImpl.class.getDeclaredMethod("enrichWithAiSpecs", ObjectNode.class);
        enrichMethod.setAccessible(true);
        enrichMethod.invoke(aiEngineClient, canvas);

        // Assert: both nodes should have ai_spec injected
        JsonNode enrichedNode1 = canvas.get("phases").get(0).get("nodes").get(0);
        JsonNode enrichedNode2 = canvas.get("phases").get(0).get("nodes").get(1);

        assertNotNull(enrichedNode1.get("ai_spec"),
                "ai_spec should be injected for flat-config video_watch node");
        assertEquals("TEXT", enrichedNode1.get("ai_spec").get("target_agent").asText());

        assertNotNull(enrichedNode2.get("ai_spec"),
                "ai_spec should be injected for 6-dimension ai_practice node");
        assertEquals("SOCRATIC", enrichedNode2.get("ai_spec").get("target_agent").asText());
    }

    @Test
    @DisplayName("enrichWithAiSpecs: skips node when wf_node_def has no ai_spec_json")
    void testEnrichWithAiSpecs_noAiSpecInNodeDef() throws Exception {
        // Arrange: canvas with a node whose type has no ai_spec_json in wf_node_def
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode canvas = mapper.createObjectNode();
        ArrayNode phases = mapper.createArrayNode();
        ObjectNode phase = mapper.createObjectNode();
        phase.put("phase_id", "phase_1");
        phase.put("phase_name", "阶段");
        phase.put("sort_num", 1);

        ArrayNode nodes = mapper.createArrayNode();
        ObjectNode node = mapper.createObjectNode();
        node.put("node_id", "node_no_spec");
        node.put("node_type", "unknown_type");
        node.put("node_name", "未知节点");
        nodes.add(node);

        phase.set("nodes", nodes);
        phases.add(phase);
        canvas.set("phases", phases);

        // Mock: no wf_node_def found for this type
        when(nodeDefMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Inject ObjectMapper via reflection
        ReflectionTestUtils.setField(aiEngineClient, "objectMapper", mapper);

        // Act
        Method enrichMethod = AiEngineClientImpl.class.getDeclaredMethod("enrichWithAiSpecs", ObjectNode.class);
        enrichMethod.setAccessible(true);
        enrichMethod.invoke(aiEngineClient, canvas);

        // Assert: node should NOT have ai_spec (gracefully skipped)
        JsonNode enrichedNode = canvas.get("phases").get(0).get("nodes").get(0);
        assertNull(enrichedNode.get("ai_spec"),
                "ai_spec should not be present when wf_node_def has no entry for the node_type");
    }
}
