package com.smartconstruct.backend_core.bugfix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartconstruct.backend_core.controller.AiCallbackController;
import com.smartconstruct.backend_core.controller.TeacherTemplateController;
import com.smartconstruct.backend_core.dto.*;
import com.smartconstruct.backend_core.entity.WfNodeAiStatus;
import com.smartconstruct.backend_core.entity.WfNodeConfigSchema;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfNodeAiStatusMapper;
import com.smartconstruct.backend_core.mapper.WfNodeConfigSchemaMapper;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.IAiEngineClient;
import com.smartconstruct.backend_core.service.IKnowledgePointService;
import com.smartconstruct.backend_core.service.INodeValidationService;
import com.smartconstruct.backend_core.service.IQuestionBankService;
import com.smartconstruct.backend_core.service.IQuestionService;
import com.smartconstruct.backend_core.service.ITrainingTaskService;
import com.smartconstruct.backend_core.service.ITrainingTemplateService;
import com.smartconstruct.backend_core.service.impl.NodeValidationServiceImpl;
import com.smartconstruct.backend_core.websocket.TrainingWebSocketHandler;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Preservation Property-Based Tests
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7**
 *
 * These tests verify that EXISTING working behavior is preserved.
 * They MUST PASS on unfixed code — they capture the baseline behavior
 * of ai_practice, theory_class, and coding_class node types.
 *
 * After the fix is implemented, these same tests will be re-run to confirm
 * no regressions were introduced.
 *
 * Properties tested:
 * - P1: ai_practice/theory_class/coding_class nodes with 6-dimension configs pass validation
 * - P2: Template basic info (templateName, templateDescription, creatorId) saves correctly
 * - P3: AI callback with status=2 updates ai_status=2 and triggers WebSocket notification
 * - P4: Preview endpoint returns correct structure for templates with AI node types
 */
class PreservationPropertyTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** The 3 existing AI node types that already work correctly */
    private static final String[] AI_NODE_TYPES = {
            "ai_practice", "theory_class", "coding_class"
    };

    // ========== Property 1: AI Node Types with 6-Dimension Config Pass Validation ==========

    /**
     * **Validates: Requirements 3.1, 3.2, 3.3**
     *
     * Property: For all templates containing ONLY ai_practice/theory_class/coding_class nodes,
     * the NodeValidationService accepts their 6-dimension configs.
     *
     * This confirms the existing validation logic works correctly for AI node types
     * that already submit configs in the required 6-dimension format.
     *
     * EXPECTED: PASSES on unfixed code (these node types already work)
     */
    @Property(tries = 50)
    void aiNodeTypesWithSixDimensionConfigPassValidation(
            @ForAll("aiNodeTypes") String nodeType,
            @ForAll("sixDimensionConfigs") String configJson
    ) {
        // Setup: mock the schema mapper to return a permissive schema
        WfNodeConfigSchemaMapper schemaMapper = Mockito.mock(WfNodeConfigSchemaMapper.class);
        WfNodeConfigSchema schema = new WfNodeConfigSchema();
        schema.setNodeType(nodeType);
        schema.setSchemaJson(buildPermissiveSixDimensionSchema());
        when(schemaMapper.selectByNodeType(nodeType)).thenReturn(schema);

        // Create the service under test
        NodeValidationServiceImpl service = new NodeValidationServiceImpl();
        try {
            java.lang.reflect.Field field = NodeValidationServiceImpl.class.getDeclaredField("schemaMapper");
            field.setAccessible(true);
            field.set(service, schemaMapper);
        } catch (Exception e) {
            fail("Failed to inject mock: " + e.getMessage());
        }

        // Act: validate the 6-dimension config (as AI node types submit)
        ValidationResult result = service.validateNodeConfig(nodeType, configJson);

        // Assert: AI node types with proper 6-dimension structure SHOULD pass validation
        assertTrue(result.isValid(),
                "PRESERVATION FAILED: " + nodeType + " with 6-dimension config should pass validation, " +
                        "but got error: " + result.getMessage());
    }

    // ========== Property 2: Template Basic Info Saves Correctly ==========

    /**
     * **Validates: Requirements 3.4**
     *
     * Property: For all template save operations with valid templateName/templateDescription/creatorId,
     * basic info is persisted identically.
     *
     * This confirms the template create endpoint correctly saves basic info fields.
     *
     * EXPECTED: PASSES on unfixed code (template save already works)
     */
    @Property(tries = 30)
    void templateBasicInfoSavesCorrectly(
            @ForAll("templateNames") String templateName,
            @ForAll("templateDescriptions") String templateDescription,
            @ForAll("creatorIds") Long creatorId
    ) {
        // Setup: mock the template service
        ITrainingTemplateService templateService = Mockito.mock(ITrainingTemplateService.class);
        ITrainingTaskService trainingTaskService = Mockito.mock(ITrainingTaskService.class);
        INodeValidationService nodeValidationService = Mockito.mock(INodeValidationService.class);
        IAiEngineClient aiEngineClient = Mockito.mock(IAiEngineClient.class);
        WfNodeAiStatusMapper nodeAiStatusMapper = Mockito.mock(WfNodeAiStatusMapper.class);

        // Capture the template that gets saved
        ArgumentCaptor<WfTrainingTemplate> templateCaptor = ArgumentCaptor.forClass(WfTrainingTemplate.class);
        when(templateService.save(templateCaptor.capture())).thenReturn(true);

        TeacherTemplateController controller = new TeacherTemplateController(
                templateService, trainingTaskService,
                nodeValidationService, aiEngineClient, nodeAiStatusMapper
        );

        // Mock SecurityContext to return our creatorId
        mockSecurityContext(controller, creatorId);

        // Act: create template with basic info
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("templateName", templateName);
        body.put("templateDescription", templateDescription);
        body.put("canvasData", buildSimpleAiCanvas());

        ResponseEntity<?> response = controller.create(body);

        // Assert: template is saved with correct basic info
        assertEquals(200, response.getStatusCodeValue(),
                "PRESERVATION FAILED: Template create should return 200");

        WfTrainingTemplate saved = templateCaptor.getValue();
        assertEquals(templateName, saved.getTemplateName(),
                "PRESERVATION FAILED: templateName not persisted correctly");
        assertEquals(templateDescription, saved.getTemplateDescription(),
                "PRESERVATION FAILED: templateDescription not persisted correctly");
        assertEquals(creatorId, saved.getCreatorId(),
                "PRESERVATION FAILED: creatorId not persisted correctly");
        assertEquals(Integer.valueOf(0), saved.getAiStatus(),
                "PRESERVATION FAILED: initial aiStatus should be 0");
        assertNotNull(saved.getCreatedAt(),
                "PRESERVATION FAILED: createdAt should be set");
        assertNotNull(saved.getUpdatedAt(),
                "PRESERVATION FAILED: updatedAt should be set");
    }

    // ========== Property 3: AI Callback with status=2 Updates Template and Triggers WebSocket ==========

    /**
     * **Validates: Requirements 3.5**
     *
     * Property: For all AI callbacks with status=2, template ai_status update
     * and WebSocket notification behavior is identical.
     *
     * This confirms the existing AI callback flow:
     * 1. Updates template ai_status to 2
     * 2. Stores standard_payload_json
     * 3. Triggers WebSocket notification
     *
     * EXPECTED: PASSES on unfixed code (callback already works at template level)
     */
    @Property(tries = 30)
    void aiCallbackWithStatus2UpdatesTemplateAndNotifiesWebSocket(
            @ForAll("templateIds") Long templateId,
            @ForAll("jobIds") String jobId,
            @ForAll("standardPayloads") String standardPayloadJson
    ) {
        // Setup mocks
        WfTrainingTemplateMapper templateMapper = Mockito.mock(WfTrainingTemplateMapper.class);
        WfNodeAiStatusMapper nodeAiStatusMapper = Mockito.mock(WfNodeAiStatusMapper.class);
        IQuestionBankService questionBankService = Mockito.mock(IQuestionBankService.class);
        IQuestionService questionService = Mockito.mock(IQuestionService.class);
        IKnowledgePointService knowledgePointService = Mockito.mock(IKnowledgePointService.class);
        TrainingWebSocketHandler webSocketHandler = Mockito.mock(TrainingWebSocketHandler.class);

        // Template exists with ai_status=1 (processing)
        WfTrainingTemplate template = new WfTrainingTemplate();
        template.setId(templateId);
        template.setTemplateName("AI Processing Template");
        template.setAiStatus(1);
        template.setCreatorId(100L);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        when(templateMapper.selectById(templateId)).thenReturn(template);
        when(templateMapper.updateById(any())).thenReturn(1);

        // Create controller
        AiCallbackController controller = new AiCallbackController(
                templateMapper, nodeAiStatusMapper,
                questionBankService, questionService,
                knowledgePointService, webSocketHandler
        );

        // Act: simulate AI callback with status=2 (success)
        AiCallbackPayload payload = new AiCallbackPayload();
        payload.setTemplateId(templateId);
        payload.setJobId(jobId);
        payload.setStatus(2);
        payload.setStandardPayloadJson(standardPayloadJson);

        ApiResult<Void> result = controller.onAiPipelineComplete(payload);

        // Assert 1: callback returns success
        assertEquals(200, result.getCode(),
                "PRESERVATION FAILED: AI callback should return 200 for status=2");

        // Assert 2: template is updated with ai_status=2 and standard_payload_json
        ArgumentCaptor<WfTrainingTemplate> captor = ArgumentCaptor.forClass(WfTrainingTemplate.class);
        verify(templateMapper).updateById(captor.capture());
        WfTrainingTemplate updated = captor.getValue();
        assertEquals(Integer.valueOf(2), updated.getAiStatus(),
                "PRESERVATION FAILED: ai_status should be updated to 2");
        assertEquals(standardPayloadJson, updated.getStandardPayloadJson(),
                "PRESERVATION FAILED: standard_payload_json should be stored");
        assertNull(updated.getErrorReason(),
                "PRESERVATION FAILED: error_reason should be null on success");

        // Assert 3: WebSocket notification is triggered
        verify(webSocketHandler).broadcastToTask(eq(templateId), argThat(msg ->
                msg != null && msg.contains("ai_status_change")));
    }

    // ========== Property 4: Preview Returns Correct Structure for AI Node Templates ==========

    /**
     * **Validates: Requirements 3.7**
     *
     * Property: For templates containing ai_practice/theory_class/coding_class nodes,
     * the preview endpoint returns the correct structure with all expected fields.
     *
     * This confirms the preview endpoint correctly parses raw_canvas_json and
     * returns phase/node structure for templates with AI node types.
     *
     * EXPECTED: PASSES on unfixed code (preview structure works for AI nodes)
     */
    @Property(tries = 30)
    void previewReturnsCorrectStructureForAiNodeTemplates(
            @ForAll("aiNodeTypeCombinations") List<String> nodeTypes,
            @ForAll("templateNames") String templateName
    ) throws Exception {
        // Setup: create template with AI node types
        WfTrainingTemplate template = new WfTrainingTemplate();
        template.setId(99L);
        template.setTemplateName(templateName);
        template.setAiStatus(0);
        template.setCreatorId(100L);

        // Build raw_canvas_json with AI node types in a single phase (sorted)
        String rawCanvasJson = buildCanvasWithAiNodes(nodeTypes);
        template.setRawCanvasJson(rawCanvasJson);

        // Setup mocks
        ITrainingTemplateService templateService = Mockito.mock(ITrainingTemplateService.class);
        ITrainingTaskService trainingTaskService = Mockito.mock(ITrainingTaskService.class);
        INodeValidationService nodeValidationService = Mockito.mock(INodeValidationService.class);
        IAiEngineClient aiEngineClient = Mockito.mock(IAiEngineClient.class);
        WfNodeAiStatusMapper nodeAiStatusMapper = Mockito.mock(WfNodeAiStatusMapper.class);

        when(templateService.getById(99L)).thenReturn(template);

        TeacherTemplateController controller = new TeacherTemplateController(
                templateService, trainingTaskService,
                nodeValidationService, aiEngineClient, nodeAiStatusMapper
        );

        // Act: call preview
        ResponseEntity<?> response = controller.preview(99L);

        // Assert: response structure is correct
        assertEquals(200, response.getStatusCodeValue(),
                "PRESERVATION FAILED: Preview should return 200");

        ApiResult<?> apiResult = (ApiResult<?>) response.getBody();
        assertNotNull(apiResult, "PRESERVATION FAILED: Response body should not be null");
        assertEquals(200, apiResult.getCode(),
                "PRESERVATION FAILED: ApiResult code should be 200");

        Map<String, Object> data = (Map<String, Object>) apiResult.getData();
        assertNotNull(data, "PRESERVATION FAILED: Preview data should not be null");

        // Verify template-level fields
        assertEquals(templateName, data.get("template_name"),
                "PRESERVATION FAILED: template_name should match");
        assertEquals(0, data.get("ai_status"),
                "PRESERVATION FAILED: ai_status should be 0");

        // Verify phases structure
        List<Map<String, Object>> phases = (List<Map<String, Object>>) data.get("phases");
        assertNotNull(phases, "PRESERVATION FAILED: phases should not be null");
        assertFalse(phases.isEmpty(), "PRESERVATION FAILED: phases should not be empty");

        // Verify nodes within phases
        Map<String, Object> firstPhase = phases.get(0);
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) firstPhase.get("nodes");
        assertNotNull(nodes, "PRESERVATION FAILED: nodes should not be null");
        assertEquals(nodeTypes.size(), nodes.size(),
                "PRESERVATION FAILED: node count should match input");

        // Verify each node has expected fields
        for (int i = 0; i < nodes.size(); i++) {
            Map<String, Object> node = nodes.get(i);
            assertNotNull(node.get("node_id"), "PRESERVATION FAILED: node_id should not be null");
            assertEquals(nodeTypes.get(i), node.get("node_type"),
                    "PRESERVATION FAILED: node_type should match");
            assertNotNull(node.get("node_name"), "PRESERVATION FAILED: node_name should not be null");
            assertTrue(node.containsKey("ai_generated_content"),
                    "PRESERVATION FAILED: ai_generated_content field should exist");
            assertTrue(node.containsKey("config_summary"),
                    "PRESERVATION FAILED: config_summary field should exist");
        }
    }

    // ========== Property 5: AI Callback Idempotency for Already-Completed Templates ==========

    /**
     * **Validates: Requirements 3.5**
     *
     * Property: For templates that already have ai_status=2, duplicate AI callbacks
     * are safely ignored (idempotency).
     *
     * EXPECTED: PASSES on unfixed code (idempotency check already exists)
     */
    @Property(tries = 20)
    void aiCallbackIsIdempotentForCompletedTemplates(
            @ForAll("templateIds") Long templateId,
            @ForAll("jobIds") String jobId
    ) {
        // Setup mocks
        WfTrainingTemplateMapper templateMapper = Mockito.mock(WfTrainingTemplateMapper.class);
        WfNodeAiStatusMapper nodeAiStatusMapper = Mockito.mock(WfNodeAiStatusMapper.class);
        IQuestionBankService questionBankService = Mockito.mock(IQuestionBankService.class);
        IQuestionService questionService = Mockito.mock(IQuestionService.class);
        IKnowledgePointService knowledgePointService = Mockito.mock(IKnowledgePointService.class);
        TrainingWebSocketHandler webSocketHandler = Mockito.mock(TrainingWebSocketHandler.class);

        // Template already has ai_status=2 (completed)
        WfTrainingTemplate template = new WfTrainingTemplate();
        template.setId(templateId);
        template.setTemplateName("Already Completed Template");
        template.setAiStatus(2);
        template.setCreatorId(100L);
        when(templateMapper.selectById(templateId)).thenReturn(template);

        AiCallbackController controller = new AiCallbackController(
                templateMapper, nodeAiStatusMapper,
                questionBankService, questionService,
                knowledgePointService, webSocketHandler
        );

        // Act: send duplicate callback
        AiCallbackPayload payload = new AiCallbackPayload();
        payload.setTemplateId(templateId);
        payload.setJobId(jobId);
        payload.setStatus(2);
        payload.setStandardPayloadJson("{\"nodes\":[]}");

        ApiResult<Void> result = controller.onAiPipelineComplete(payload);

        // Assert: callback is accepted but template is NOT updated again
        assertEquals(200, result.getCode(),
                "PRESERVATION FAILED: Duplicate callback should return 200 (idempotent)");
        verify(templateMapper, never()).updateById(any());
        verify(webSocketHandler, never()).broadcastToTask(anyLong(), anyString());
    }

    // ========== Arbitraries (Generators) ==========

    @Provide
    Arbitrary<String> aiNodeTypes() {
        return Arbitraries.of(AI_NODE_TYPES);
    }

    @Provide
    Arbitrary<List<String>> aiNodeTypeCombinations() {
        return Arbitraries.of(AI_NODE_TYPES)
                .list().ofMinSize(1).ofMaxSize(5);
    }

    @Provide
    Arbitrary<String> sixDimensionConfigs() {
        // Generate valid 6-dimension configs that AI node types submit
        return Arbitraries.of(
                buildSixDimensionConfig("ai_practice", "AI对练练习", true),
                buildSixDimensionConfig("theory_class", "理论学习卡片", true),
                buildSixDimensionConfig("coding_class", "代码审查配置", true),
                buildSixDimensionConfig("ai_practice", "高级对练", false),
                buildSixDimensionConfig("theory_class", "基础理论", false),
                buildSixDimensionConfig("coding_class", "编码实训", true)
        );
    }

    @Provide
    Arbitrary<String> templateNames() {
        return Arbitraries.of(
                "Java基础实训", "Python入门课程", "数据结构与算法",
                "Web开发实战", "机器学习入门", "软件工程实践",
                "数据库设计", "网络安全基础", "操作系统原理"
        );
    }

    @Provide
    Arbitrary<String> templateDescriptions() {
        return Arbitraries.of(
                "这是一个基础实训模板", "适合初学者的课程模板",
                "高级进阶实训", "综合实践项目模板",
                "", "包含多个阶段的完整实训流程"
        );
    }

    @Provide
    Arbitrary<Long> creatorIds() {
        return Arbitraries.longs().between(1L, 500L);
    }

    @Provide
    Arbitrary<Long> templateIds() {
        return Arbitraries.longs().between(1L, 1000L);
    }

    @Provide
    Arbitrary<String> jobIds() {
        return Arbitraries.strings().alpha().ofMinLength(8).ofMaxLength(16)
                .map(s -> "job-" + s);
    }

    @Provide
    Arbitrary<String> standardPayloads() {
        // Generate various standard_payload_json structures with AI node content
        return Arbitraries.of(AI_NODE_TYPES)
                .list().ofMinSize(1).ofMaxSize(4)
                .map(this::buildStandardPayloadForAiNodes);
    }

    // ========== Helper Methods ==========

    private static String buildSixDimensionConfig(String nodeType, String title, boolean enableAi) {
        try {
            ObjectNode config = objectMapper.createObjectNode();

            // display dimension
            ObjectNode display = objectMapper.createObjectNode();
            display.put("title", title);
            display.put("icon", "fa-" + nodeType);
            config.set("display", display);

            // db_mapping dimension
            ObjectNode dbMapping = objectMapper.createObjectNode();
            dbMapping.put("table", "wf_node_instance");
            dbMapping.put("node_type", nodeType);
            config.set("db_mapping", dbMapping);

            // ai_processing dimension
            ObjectNode aiProcessing = objectMapper.createObjectNode();
            aiProcessing.put("enabled", enableAi);
            aiProcessing.put("model", "qwen-plus");
            config.set("ai_processing", aiProcessing);

            // orchestration_settings dimension
            ObjectNode orchestration = objectMapper.createObjectNode();
            orchestration.put("timeout_minutes", 30);
            orchestration.put("allow_skip", false);
            config.set("orchestration_settings", orchestration);

            // data_collection dimension
            ObjectNode dataCollection = objectMapper.createObjectNode();
            dataCollection.put("collect_interactions", true);
            config.set("data_collection", dataCollection);

            // evaluation dimension
            ObjectNode evaluation = objectMapper.createObjectNode();
            evaluation.put("auto_score", enableAi);
            evaluation.put("max_score", 100);
            config.set("evaluation", evaluation);

            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> buildPermissiveSixDimensionSchema() {
        // Schema that validates the 6-dimension structure exists but is permissive on content
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> properties = new LinkedHashMap<>();
        for (String dim : new String[]{"display", "db_mapping", "ai_processing",
                "orchestration_settings", "data_collection", "evaluation"}) {
            Map<String, Object> dimSchema = new LinkedHashMap<>();
            dimSchema.put("type", "object");
            properties.put(dim, dimSchema);
        }
        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        List<String> required = Arrays.asList("display", "db_mapping", "ai_processing",
                "orchestration_settings", "data_collection", "evaluation");
        schema.put("required", required);
        return schema;
    }

    private Object buildSimpleAiCanvas() {
        try {
            ObjectNode canvas = objectMapper.createObjectNode();
            ArrayNode phases = objectMapper.createArrayNode();
            ObjectNode phase = objectMapper.createObjectNode();
            phase.put("phase_id", "phase_1");
            phase.put("phase_name", "课前阶段");
            phase.put("sort_num", 1);
            ArrayNode nodes = objectMapper.createArrayNode();
            ObjectNode node = objectMapper.createObjectNode();
            node.put("node_id", "node_1");
            node.put("node_type", "ai_practice");
            node.put("node_name", "AI对练");
            node.set("config", objectMapper.readTree(
                    buildSixDimensionConfig("ai_practice", "AI对练", true)));
            nodes.add(node);
            phase.set("nodes", nodes);
            phases.add(phase);
            canvas.set("phases", phases);
            return objectMapper.readTree(objectMapper.writeValueAsString(canvas));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildCanvasWithAiNodes(List<String> nodeTypes) {
        try {
            ObjectNode canvas = objectMapper.createObjectNode();
            ArrayNode phases = objectMapper.createArrayNode();
            ObjectNode phase = objectMapper.createObjectNode();
            phase.put("phase_id", "phase_1");
            phase.put("phase_name", "课前阶段");
            phase.put("sort_num", 1);
            ArrayNode nodes = objectMapper.createArrayNode();
            for (int i = 0; i < nodeTypes.size(); i++) {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("node_id", "node_" + (i + 1));
                node.put("node_type", nodeTypes.get(i));
                node.put("node_name", "Node " + nodeTypes.get(i) + " " + (i + 1));
                ObjectNode config = objectMapper.createObjectNode();
                config.put("title", "Config for " + nodeTypes.get(i));
                node.set("config", config);
                nodes.add(node);
            }
            phase.set("nodes", nodes);
            phases.add(phase);
            canvas.set("phases", phases);
            return objectMapper.writeValueAsString(canvas);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildStandardPayloadForAiNodes(List<String> nodeTypes) {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            ArrayNode nodesArray = objectMapper.createArrayNode();
            for (int i = 0; i < nodeTypes.size(); i++) {
                ObjectNode nodeContent = objectMapper.createObjectNode();
                nodeContent.put("node_id", "node_" + (i + 1));
                nodeContent.put("node_type", nodeTypes.get(i));
                nodeContent.put("ai_generated_content", "AI生成内容 for " + nodeTypes.get(i));
                nodesArray.add(nodeContent);
            }
            payload.set("nodes", nodesArray);
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper to mock SecurityContext for TeacherTemplateController.
     * Uses reflection to bypass the getCurrentUserId() call.
     */
    private void mockSecurityContext(TeacherTemplateController controller, Long userId) {
        // We need to mock the SecurityContextHolder for the controller
        // Since this is a unit test, we'll use a spy approach
        org.springframework.security.core.context.SecurityContext securityContext =
                Mockito.mock(org.springframework.security.core.context.SecurityContext.class);
        org.springframework.security.core.Authentication authentication =
                Mockito.mock(org.springframework.security.core.Authentication.class);

        com.smartconstruct.backend_core.entity.SysUser mockUser = new com.smartconstruct.backend_core.entity.SysUser();
        mockUser.setId(userId);
        mockUser.setUsername("test_teacher");

        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);
    }
}
