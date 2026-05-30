package com.smartconstruct.backend_core.bugfix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartconstruct.backend_core.controller.AiCallbackController;
import com.smartconstruct.backend_core.controller.TeacherTemplateController;
import com.smartconstruct.backend_core.dto.*;
import com.smartconstruct.backend_core.entity.WfNodeAiStatus;
import com.smartconstruct.backend_core.entity.WfNodeConfigSchema;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfNodeAiStatusMapper;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.IAiEngineClient;
import com.smartconstruct.backend_core.service.IAiProcessingLogService;
import com.smartconstruct.backend_core.service.IKnowledgePointService;
import com.smartconstruct.backend_core.service.INodeValidationService;
import com.smartconstruct.backend_core.service.IQuestionBankService;
import com.smartconstruct.backend_core.service.IQuestionService;
import com.smartconstruct.backend_core.service.ITrainingTaskService;
import com.smartconstruct.backend_core.service.ITrainingTemplateService;
import com.smartconstruct.backend_core.service.impl.NodeValidationServiceImpl;
import com.smartconstruct.backend_core.mapper.WfNodeConfigSchemaMapper;
import com.smartconstruct.backend_core.websocket.TrainingWebSocketHandler;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Bug Condition Exploration Property-Based Test
 *
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7**
 *
 * This test is EXPECTED TO FAIL on unfixed code — failure confirms the bugs exist.
 * DO NOT fix the code or the test when it fails.
 *
 * Tests three broken chains:
 * - Chain 1: Non-AI node types fail validation due to missing 6-dimension structure
 * - Chain 1: AI callback does not write back node-level content to wf_node_ai_status
 * - Chain 2: Preview returns phases in insertion order, not sort_num order
 * - Chain 2: Preview returns ai_generated_content=null even when standard_payload_json has data
 * - Chain 3: (Tested in frontend vitest)
 */
class BugConditionExplorationTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** All 12 non-AI node types that should be supported but currently fail */
    private static final String[] NON_AI_NODE_TYPES = {
            "resource_read", "video_watch", "survey", "task_distribute",
            "req_upload", "plan_upload", "homework", "mindmap_preview",
            "mindmap_draw", "student_peer_review", "teacher_eval", "grouping"
    };

    // ========== Chain 1: Node Validation Fails for Flat Configs ==========

    /**
     * Property: nodeConfigStoredCorrectly(result) for non-AI node types
     *
     * For any non-AI node type with a flat config (typical frontend submission format),
     * NodeValidationServiceImpl.validateNodeConfig() SHOULD accept the config.
     * On UNFIXED code, this WILL FAIL because the validator requires 6-dimension structure.
     *
     * Expected counterexample: NodeValidationService returns error
     * "结构错误: 缺少=[display, db_mapping, ai_processing, ...]" for flat configs
     */
    @Property(tries = 50)
    void chain1_flatConfigShouldBeAcceptedForNonAiNodeTypes(
            @ForAll("nonAiNodeTypes") String nodeType,
            @ForAll("flatNodeConfigs") String flatConfigJson
    ) {
        // Setup: mock the schema mapper to return a valid schema for the node type
        WfNodeConfigSchemaMapper schemaMapper = Mockito.mock(WfNodeConfigSchemaMapper.class);
        WfNodeConfigSchema schema = new WfNodeConfigSchema();
        schema.setNodeType(nodeType);
        // Provide a permissive schema that would pass if the structure were correct
        schema.setSchemaJson(buildPermissiveSchema());
        when(schemaMapper.selectByNodeType(nodeType)).thenReturn(schema);

        // Create the service under test with mocked mapper
        NodeValidationServiceImpl service = new NodeValidationServiceImpl();
        // Inject mock via reflection since @Autowired
        try {
            java.lang.reflect.Field field = NodeValidationServiceImpl.class.getDeclaredField("schemaMapper");
            field.setAccessible(true);
            field.set(service, schemaMapper);
        } catch (Exception e) {
            fail("Failed to inject mock: " + e.getMessage());
        }

        // Act: validate the flat config (as frontend would submit)
        ValidationResult result = service.validateNodeConfig(nodeType, flatConfigJson);

        // Assert: the config SHOULD be accepted (this will FAIL on unfixed code)
        assertTrue(result.isValid(),
                "nodeConfigStoredCorrectly FAILED: nodeType=" + nodeType +
                        ", error=" + result.getMessage() +
                        ". Flat config was rejected because 6-dimension structure is missing.");
    }

    // ========== Chain 1: AI Callback Does Not Write Back Node-Level Content ==========

    /**
     * Property: After AI callback with standard_payload_json containing node-level content,
     * wf_node_ai_status.result_json should be populated for those nodes.
     *
     * On UNFIXED code, onAiPipelineComplete() only updates template-level fields
     * and does NOT parse standard_payload_json to write node-level content.
     *
     * Expected counterexample: wf_node_ai_status.result_json is null for nodes
     * that have content in standard_payload_json
     */
    @Property(tries = 20)
    void chain1_aiCallbackShouldWriteBackNodeLevelContent(
            @ForAll("templateIds") Long templateId,
            @ForAll("nodeIdsWithContent") List<String> nodeIds
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
        template.setTemplateName("Test Template");
        template.setAiStatus(1);
        template.setCreatorId(100L);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        when(templateMapper.selectById(templateId)).thenReturn(template);
        when(templateMapper.updateById(any())).thenReturn(1);

        // Build standard_payload_json with node-level content
        String standardPayloadJson = buildStandardPayloadWithNodes(nodeIds);

        // Create controller
        AiCallbackController controller = new AiCallbackController(
                templateMapper, nodeAiStatusMapper,
                questionBankService, questionService,
                knowledgePointService, webSocketHandler,
                Mockito.mock(IAiProcessingLogService.class)
        );

        // Act: simulate AI callback with node-level content
        AiCallbackPayload payload = new AiCallbackPayload();
        payload.setTemplateId(templateId);
        payload.setJobId("job-" + UUID.randomUUID().toString());
        payload.setStatus(2);
        payload.setStandardPayloadJson(standardPayloadJson);

        controller.onAiPipelineComplete(payload);

        // Assert: node-level content SHOULD be written to wf_node_ai_status
        // On UNFIXED code, nodeAiStatusMapper is NEVER called for node-level writes
        for (String nodeId : nodeIds) {
            verify(nodeAiStatusMapper, atLeastOnce().description(
                    "aiContentWrittenBackToNodes FAILED: node_id=" + nodeId +
                            " should have result_json written to wf_node_ai_status, but callback only updates template-level fields"
            )).insert(argThat(status -> nodeId.equals(((WfNodeAiStatus) status).getNodeId())));
        }
    }

    // ========== Chain 2: Preview Phases Not Sorted by sort_num ==========

    /**
     * Property: phasesSortedBySortNum(result.phases) for preview
     *
     * For any template with phases in non-sorted order in raw_canvas_json,
     * the preview endpoint SHOULD return phases sorted by sort_num ascending.
     *
     * On UNFIXED code, phases are returned in insertion order (original array order).
     *
     * Expected counterexample: Preview returns phases in insertion order, not sort_num order
     */
    @Property(tries = 30)
    void chain2_previewShouldReturnPhasesSortedBySortNum(
            @ForAll("unsortedPhaseOrders") List<Integer> sortNums
    ) throws Exception {
        // Setup: create template with phases in reverse/random sort_num order
        WfTrainingTemplate template = new WfTrainingTemplate();
        template.setId(1L);
        template.setTemplateName("Test Template");
        template.setAiStatus(0);
        template.setCreatorId(100L);

        // Build raw_canvas_json with phases in the given (unsorted) order
        String rawCanvasJson = buildCanvasWithPhases(sortNums);
        template.setRawCanvasJson(rawCanvasJson);

        // Setup mocks for TeacherTemplateController
        ITrainingTemplateService templateService = Mockito.mock(ITrainingTemplateService.class);
        ITrainingTaskService trainingTaskService = Mockito.mock(ITrainingTaskService.class);
        INodeValidationService nodeValidationService = Mockito.mock(INodeValidationService.class);
        IAiEngineClient aiEngineClient = Mockito.mock(IAiEngineClient.class);
        WfNodeAiStatusMapper nodeAiStatusMapper = Mockito.mock(WfNodeAiStatusMapper.class);

        when(templateService.getById(1L)).thenReturn(template);

        TeacherTemplateController controller = new TeacherTemplateController(
                templateService, trainingTaskService,
                nodeValidationService, aiEngineClient, nodeAiStatusMapper,
                Mockito.mock(IAiProcessingLogService.class)
        );

        // Act: call preview
        // Need to mock SecurityContext for getCurrentUserId - but preview() doesn't call it
        ResponseEntity<?> response = controller.preview(1L);
        assertEquals(200, response.getStatusCodeValue());

        // Extract phases from response
        ApiResult<?> apiResult = (ApiResult<?>) response.getBody();
        assertNotNull(apiResult);
        Map<String, Object> data = (Map<String, Object>) apiResult.getData();
        List<Map<String, Object>> phases = (List<Map<String, Object>>) data.get("phases");

        // Assert: phases SHOULD be sorted by sort_num ascending
        List<Integer> returnedSortNums = new ArrayList<>();
        for (Map<String, Object> phase : phases) {
            returnedSortNums.add((Integer) phase.get("sort_num"));
        }

        List<Integer> expectedSorted = new ArrayList<>(sortNums);
        Collections.sort(expectedSorted);

        assertEquals(expectedSorted, returnedSortNums,
                "phasesSortedBySortNum FAILED: phases returned in insertion order " +
                        returnedSortNums + " instead of sorted order " + expectedSorted);
    }

    // ========== Chain 2: AI Generated Content Not Populated ==========

    /**
     * Property: aiGeneratedContentPopulated(result.phases) when ai_status=2
     *
     * For any template with ai_status=2 and populated standard_payload_json,
     * the preview endpoint SHOULD populate ai_generated_content for each node.
     *
     * On UNFIXED code, ai_generated_content is always null (enrichment is only log.debug).
     *
     * Expected counterexample: ai_generated_content is always null even when
     * standard_payload_json contains data
     */
    @Property(tries = 20)
    void chain2_previewShouldPopulateAiGeneratedContentWhenAiStatusIs2(
            @ForAll("nodeIdsWithContent") List<String> nodeIds
    ) throws Exception {
        // Setup: template with ai_status=2 and standard_payload_json containing node content
        WfTrainingTemplate template = new WfTrainingTemplate();
        template.setId(2L);
        template.setTemplateName("AI Processed Template");
        template.setAiStatus(2);
        template.setCreatorId(100L);

        // Build raw_canvas_json with nodes
        String rawCanvasJson = buildCanvasWithNodes(nodeIds);
        template.setRawCanvasJson(rawCanvasJson);

        // Build standard_payload_json with AI content for each node
        String standardPayload = buildStandardPayloadWithNodes(nodeIds);
        template.setStandardPayloadJson(standardPayload);

        // Setup mocks
        ITrainingTemplateService templateService = Mockito.mock(ITrainingTemplateService.class);
        ITrainingTaskService trainingTaskService = Mockito.mock(ITrainingTaskService.class);
        INodeValidationService nodeValidationService = Mockito.mock(INodeValidationService.class);
        IAiEngineClient aiEngineClient = Mockito.mock(IAiEngineClient.class);
        WfNodeAiStatusMapper nodeAiStatusMapper = Mockito.mock(WfNodeAiStatusMapper.class);

        when(templateService.getById(2L)).thenReturn(template);

        TeacherTemplateController controller = new TeacherTemplateController(
                templateService, trainingTaskService,
                nodeValidationService, aiEngineClient, nodeAiStatusMapper,
                Mockito.mock(IAiProcessingLogService.class)
        );

        // Act: call preview
        ResponseEntity<?> response = controller.preview(2L);
        assertEquals(200, response.getStatusCodeValue());

        // Extract nodes from response
        ApiResult<?> apiResult = (ApiResult<?>) response.getBody();
        assertNotNull(apiResult);
        Map<String, Object> data = (Map<String, Object>) apiResult.getData();
        List<Map<String, Object>> phases = (List<Map<String, Object>>) data.get("phases");

        // Assert: ai_generated_content SHOULD be populated for nodes in standard_payload_json
        for (Map<String, Object> phase : phases) {
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) phase.get("nodes");
            if (nodes != null) {
                for (Map<String, Object> node : nodes) {
                    String nodeId = (String) node.get("node_id");
                    if (nodeIds.contains(nodeId)) {
                        assertNotNull(node.get("ai_generated_content"),
                                "aiGeneratedContentPopulated FAILED: node_id=" + nodeId +
                                        " has content in standard_payload_json but ai_generated_content is null. " +
                                        "The preview enrichment logic is only a log.debug placeholder.");
                    }
                }
            }
        }
    }

    // ========== Arbitraries (Generators) ==========

    @Provide
    Arbitrary<String> nonAiNodeTypes() {
        return Arbitraries.of(NON_AI_NODE_TYPES);
    }

    @Provide
    Arbitrary<String> flatNodeConfigs() {
        // Generate realistic flat configs that frontend would submit for non-AI nodes
        return Arbitraries.of(
                "{\"resource_id\": 123, \"min_reading_duration\": 300}",
                "{\"video_url\": \"https://example.com/video.mp4\", \"auto_play\": true}",
                "{\"survey_title\": \"课程反馈\", \"questions\": [{\"q\": \"满意度\"}]}",
                "{\"task_title\": \"小组讨论\", \"deadline_minutes\": 30}",
                "{\"upload_types\": [\".doc\", \".pdf\"], \"max_file_size\": 10485760}",
                "{\"plan_type\": \"project\", \"enable_ai_review\": true}",
                "{\"homework_title\": \"课后作业\", \"due_days\": 7}",
                "{\"mindmap_template\": \"default\", \"max_nodes\": 50}",
                "{\"draw_mode\": \"free\", \"collaboration\": false}",
                "{\"review_criteria\": [\"内容\", \"格式\", \"创新\"], \"peer_count\": 3}",
                "{\"eval_dimensions\": [\"参与度\", \"完成度\"], \"weight\": [0.5, 0.5]}",
                "{\"group_size\": 4, \"strategy\": \"random\"}"
        );
    }

    @Provide
    Arbitrary<Long> templateIds() {
        return Arbitraries.longs().between(1L, 1000L);
    }

    @Provide
    Arbitrary<List<String>> nodeIdsWithContent() {
        return Arbitraries.strings()
                .alpha().ofMinLength(3).ofMaxLength(8)
                .map(s -> "node_" + s)
                .list().ofMinSize(1).ofMaxSize(5);
    }

    @Provide
    Arbitrary<List<Integer>> unsortedPhaseOrders() {
        // Generate sort_num lists that are NOT already sorted (to test sorting)
        return Arbitraries.integers().between(1, 10)
                .list().ofMinSize(2).ofMaxSize(5)
                .filter(list -> {
                    // Ensure unique values
                    Set<Integer> unique = new HashSet<>(list);
                    if (unique.size() != list.size()) return false;
                    // Ensure NOT already sorted (so we can detect if sorting is missing)
                    List<Integer> sorted = new ArrayList<>(list);
                    Collections.sort(sorted);
                    return !list.equals(sorted);
                });
    }

    // ========== Helper Methods ==========

    private Map<String, Object> buildPermissiveSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", true);
        return schema;
    }

    private String buildCanvasWithPhases(List<Integer> sortNums) {
        try {
            ObjectNode canvas = objectMapper.createObjectNode();
            com.fasterxml.jackson.databind.node.ArrayNode phasesArray = objectMapper.createArrayNode();
            for (int i = 0; i < sortNums.size(); i++) {
                ObjectNode phase = objectMapper.createObjectNode();
                phase.put("phase_id", "phase_" + i);
                phase.put("phase_name", "阶段" + sortNums.get(i));
                phase.put("sort_num", sortNums.get(i));
                phase.set("nodes", objectMapper.createArrayNode());
                phasesArray.add(phase);
            }
            canvas.set("phases", phasesArray);
            return objectMapper.writeValueAsString(canvas);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildCanvasWithNodes(List<String> nodeIds) {
        try {
            ObjectNode canvas = objectMapper.createObjectNode();
            com.fasterxml.jackson.databind.node.ArrayNode phasesArray = objectMapper.createArrayNode();
            ObjectNode phase = objectMapper.createObjectNode();
            phase.put("phase_id", "phase_1");
            phase.put("phase_name", "课前阶段");
            phase.put("sort_num", 1);
            com.fasterxml.jackson.databind.node.ArrayNode nodesArray = objectMapper.createArrayNode();
            for (String nodeId : nodeIds) {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("node_id", nodeId);
                node.put("node_type", "resource_read");
                node.put("node_name", "Node " + nodeId);
                node.set("config", objectMapper.createObjectNode().put("resource_id", 1));
                nodesArray.add(node);
            }
            phase.set("nodes", nodesArray);
            phasesArray.add(phase);
            canvas.set("phases", phasesArray);
            return objectMapper.writeValueAsString(canvas);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildStandardPayloadWithNodes(List<String> nodeIds) {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            com.fasterxml.jackson.databind.node.ArrayNode nodesArray = objectMapper.createArrayNode();
            for (String nodeId : nodeIds) {
                ObjectNode nodeContent = objectMapper.createObjectNode();
                nodeContent.put("node_id", nodeId);
                nodeContent.put("ai_generated_content", "AI生成的内容 for " + nodeId);
                nodeContent.put("summary", "这是节点 " + nodeId + " 的AI摘要");
                nodesArray.add(nodeContent);
            }
            payload.set("nodes", nodesArray);
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
