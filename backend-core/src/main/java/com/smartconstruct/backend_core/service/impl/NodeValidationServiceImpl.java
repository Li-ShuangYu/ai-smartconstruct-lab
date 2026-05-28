package com.smartconstruct.backend_core.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.smartconstruct.backend_core.dto.ValidationResult;
import com.smartconstruct.backend_core.entity.WfNodeConfigSchema;
import com.smartconstruct.backend_core.mapper.WfNodeConfigSchemaMapper;
import com.smartconstruct.backend_core.service.INodeValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 节点配置验证服务实现
 *
 * 负责验证节点配置JSON是否符合6维度结构规范和对应节点类型的JSON Schema定义。
 * 对于非AI密集型节点类型（如resource_read、video_watch等12种），支持扁平配置格式，
 * 自动将其转换为简化的6维度结构后进行验证。
 *
 * @author SmartConstruct
 */
@Service
public class NodeValidationServiceImpl implements INodeValidationService {

    private static final Logger log = LoggerFactory.getLogger(NodeValidationServiceImpl.class);

    /** 6维度必需的顶层键 */
    private static final Set<String> REQUIRED_DIMENSIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "display", "db_mapping", "ai_processing",
                    "orchestration_settings", "data_collection", "evaluation"
            ))
    );

    /** AI密集型节点类型 — 这些节点必须提交完整的6维度配置 */
    private static final Set<String> AI_INTENSIVE_NODE_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "ai_practice", "theory_class", "coding_class"
            ))
    );

    /** 非AI密集型节点类型 — 这些节点允许扁平配置格式 */
    private static final Set<String> NON_AI_NODE_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "resource_read", "video_watch", "survey", "task_distribute",
                    "req_upload", "plan_upload", "homework", "mindmap_preview",
                    "mindmap_draw", "student_peer_review", "teacher_eval", "grouping"
            ))
    );

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final JsonSchemaFactory schemaFactory =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    @Autowired
    private WfNodeConfigSchemaMapper schemaMapper;

    @Override
    public ValidationResult validateNodeConfig(String nodeType, String configJson) {
        // 1. 查找schema定义
        WfNodeConfigSchema schema = schemaMapper.selectByNodeType(nodeType);
        if (schema == null) {
            return ValidationResult.error("SCHEMA_NOT_FOUND", "节点类型 " + nodeType + " 缺少schema定义");
        }

        // 2. 解析JSON
        JsonNode config;
        try {
            config = objectMapper.readTree(configJson);
        } catch (Exception e) {
            log.warn("节点配置JSON解析失败, nodeType={}, error={}", nodeType, e.getMessage());
            return ValidationResult.error("配置JSON格式无效: " + e.getMessage());
        }

        if (!config.isObject()) {
            return ValidationResult.error("配置JSON必须是一个对象");
        }

        // 3. 检测配置格式：判断是否已经是6维度结构
        Set<String> topKeys = new HashSet<>();
        Iterator<String> fieldNames = config.fieldNames();
        while (fieldNames.hasNext()) {
            topKeys.add(fieldNames.next());
        }

        boolean hasSixDimensionStructure = topKeys.equals(REQUIRED_DIMENSIONS);

        if (hasSixDimensionStructure) {
            // 已经是6维度结构，直接按JSON Schema验证（适用于所有节点类型）
            return validateAgainstSchema(config, schema.getSchemaJson(), nodeType);
        }

        // 4. 扁平配置格式处理：仅允许非AI密集型节点类型使用扁平格式
        if (AI_INTENSIVE_NODE_TYPES.contains(nodeType)) {
            // AI密集型节点必须提交完整6维度结构
            Set<String> missing = new HashSet<>(REQUIRED_DIMENSIONS);
            missing.removeAll(topKeys);

            Set<String> extra = new HashSet<>(topKeys);
            extra.removeAll(REQUIRED_DIMENSIONS);

            return ValidationResult.error("结构错误: 缺少=" + missing + ", 多余=" + extra);
        }

        // 5. 非AI密集型节点：将扁平配置转换为简化6维度结构后验证
        log.info("节点类型 {} 使用扁平配置格式，自动转换为6维度结构", nodeType);
        JsonNode transformedConfig = transformFlatToSixDimension(config, nodeType);

        return validateAgainstSchema(transformedConfig, schema.getSchemaJson(), nodeType);
    }

    /**
     * 将扁平配置转换为简化的6维度结构
     *
     * 对于非AI密集型节点类型，前端提交的扁平key-value配置会被包装到6维度结构中：
     * - display: 包含原始配置中的展示相关字段
     * - db_mapping: 包含node_type标识
     * - ai_processing: 设置为默认的轻量级AI处理配置
     * - orchestration_settings: 设置为默认编排配置
     * - data_collection: 设置为默认数据采集配置
     * - evaluation: 设置为默认评估配置
     *
     * 原始扁平配置的所有字段保留在display维度中，确保数据不丢失。
     *
     * @param flatConfig 扁平配置JsonNode
     * @param nodeType   节点类型
     * @return 转换后的6维度结构JsonNode
     */
    private JsonNode transformFlatToSixDimension(JsonNode flatConfig, String nodeType) {
        ObjectNode transformed = objectMapper.createObjectNode();

        // display: 将原始扁平配置的所有字段放入display维度
        ObjectNode display = objectMapper.createObjectNode();
        Iterator<Map.Entry<String, JsonNode>> fields = flatConfig.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            display.set(entry.getKey(), entry.getValue());
        }
        transformed.set("display", display);

        // db_mapping: 包含node_type标识
        ObjectNode dbMapping = objectMapper.createObjectNode();
        dbMapping.put("node_type", nodeType);
        dbMapping.put("table", "wf_node_instance");
        transformed.set("db_mapping", dbMapping);

        // ai_processing: 默认轻量级AI处理配置
        ObjectNode aiProcessing = objectMapper.createObjectNode();
        aiProcessing.put("enabled", true);
        aiProcessing.put("mode", "lightweight");
        transformed.set("ai_processing", aiProcessing);

        // orchestration_settings: 默认编排配置
        ObjectNode orchestration = objectMapper.createObjectNode();
        orchestration.put("timeout_minutes", 60);
        orchestration.put("allow_skip", true);
        transformed.set("orchestration_settings", orchestration);

        // data_collection: 默认数据采集配置
        ObjectNode dataCollection = objectMapper.createObjectNode();
        dataCollection.put("collect_interactions", true);
        transformed.set("data_collection", dataCollection);

        // evaluation: 默认评估配置
        ObjectNode evaluation = objectMapper.createObjectNode();
        evaluation.put("auto_score", false);
        evaluation.put("max_score", 100);
        transformed.set("evaluation", evaluation);

        return transformed;
    }

    /**
     * 使用 networknt/json-schema-validator 对配置JSON进行Schema校验
     *
     * @param config     已解析的配置JsonNode
     * @param schemaObj  从数据库加载的schema对象（JacksonTypeHandler反序列化为Object）
     * @param nodeType   节点类型（用于日志）
     * @return 验证结果
     */
    private ValidationResult validateAgainstSchema(JsonNode config, Object schemaObj, String nodeType) {
        // 将schema对象序列化为JSON字符串，再解析为JsonNode供schema factory使用
        JsonNode schemaNode;
        try {
            String schemaJsonStr = objectMapper.writeValueAsString(schemaObj);
            schemaNode = objectMapper.readTree(schemaJsonStr);
        } catch (JsonProcessingException e) {
            log.error("节点类型 {} 的schema_json序列化失败: {}", nodeType, e.getMessage());
            return ValidationResult.error("Schema定义格式无效: " + e.getMessage());
        }

        // 创建JsonSchema实例并执行验证
        JsonSchema jsonSchema = schemaFactory.getSchema(schemaNode);
        Set<ValidationMessage> errors = jsonSchema.validate(config);

        if (errors == null || errors.isEmpty()) {
            return ValidationResult.success();
        }

        // 将验证错误转换为字段级错误列表
        List<ValidationResult.FieldError> fieldErrors = new ArrayList<>();
        for (ValidationMessage error : errors) {
            String path = error.getPath();
            // networknt返回的path以"$."开头，去掉前缀使其更友好
            if (path != null && path.startsWith("$.")) {
                path = path.substring(2);
            } else if (path != null && path.equals("$")) {
                path = "(root)";
            }
            fieldErrors.add(new ValidationResult.FieldError(path, error.getMessage()));
        }

        return ValidationResult.error("VALIDATION_FAILED", fieldErrors);
    }

    @Override
    public ValidationResult validatePhases(String phasesJson) {
        // 1. 解析JSON
        JsonNode phasesNode;
        try {
            phasesNode = objectMapper.readTree(phasesJson);
        } catch (Exception e) {
            log.warn("阶段JSON解析失败: {}", e.getMessage());
            return ValidationResult.error("阶段JSON格式无效: " + e.getMessage());
        }

        // 2. 必须是数组
        if (!phasesNode.isArray()) {
            return ValidationResult.error("阶段配置必须是一个JSON数组");
        }

        // 3. 数组长度检查: 1-10
        int size = phasesNode.size();
        if (size < 1 || size > 10) {
            return ValidationResult.error("阶段数量必须在1到10之间，当前数量: " + size);
        }

        // 4. 遍历每个阶段，校验phase_name和sort_num
        Set<Integer> sortNums = new HashSet<>();
        for (int i = 0; i < phasesNode.size(); i++) {
            JsonNode phase = phasesNode.get(i);

            // 校验phase_name
            JsonNode nameNode = phase.get("phase_name");
            if (nameNode == null || !nameNode.isTextual()) {
                return ValidationResult.error("阶段[" + i + "]缺少有效的phase_name字段");
            }
            String phaseName = nameNode.asText();
            if (phaseName.isEmpty() || phaseName.length() > 20) {
                return ValidationResult.error("阶段[" + i + "]的phase_name长度必须在1到20个字符之间，当前: \"" + phaseName + "\"");
            }

            // 校验sort_num
            JsonNode sortNumNode = phase.get("sort_num");
            if (sortNumNode == null || !sortNumNode.isNumber()) {
                return ValidationResult.error("阶段[" + i + "]缺少有效的sort_num字段");
            }
            int sortNum = sortNumNode.asInt();
            if (!sortNums.add(sortNum)) {
                return ValidationResult.error("阶段sort_num值必须唯一，重复值: " + sortNum);
            }
        }

        return ValidationResult.success();
    }
}
