package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartconstruct.backend_core.dto.AiJobStatus;
import com.smartconstruct.backend_core.entity.WfNodeAiStatus;
import com.smartconstruct.backend_core.entity.WfNodeDef;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfNodeAiStatusMapper;
import com.smartconstruct.backend_core.mapper.WfNodeDefMapper;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.IAiEngineClient;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * AI引擎客户端实现类
 *
 * 使用Spring WebClient（非阻塞HTTP）与Python AI引擎通信。
 * 支持两种模式：
 * <ul>
 *   <li>旧版：submitForProcessing — 直接提交原始canvas</li>
 *   <li>新版：triggerAiPipeline — 从wf_node_def加载ai_spec_json注入canvas后提交</li>
 * </ul>
 *
 * 超时配置：
 * - 连接超时：5秒
 * - 读取/响应超时：10秒
 *
 * 错误处理：
 * - 连接失败、响应超时、HTTP错误时更新模板 ai_status=3
 *
 * @author SmartConstruct
 */
@Service
public class AiEngineClientImpl implements IAiEngineClient {

    private static final Logger log = LoggerFactory.getLogger(AiEngineClientImpl.class);

    /** 连接超时时间（毫秒） */
    private static final int CONNECT_TIMEOUT_MS = 5000;

    /** 响应超时时间（秒） */
    private static final int RESPONSE_TIMEOUT_SECONDS = 10;

    @Value("${ai-engine.base-url:http://localhost:8000}")
    private String aiEngineBaseUrl;

    @Value("${ai-engine.service-token:}")
    private String serviceToken;

    @Value("${server.address:http://localhost}")
    private String serverAddress;

    @Value("${server.port:8080}")
    private int serverPort;

    @Autowired
    private WfTrainingTemplateMapper trainingTemplateMapper;

    @Autowired
    private WfNodeDefMapper nodeDefMapper;

    @Autowired
    private WfNodeAiStatusMapper nodeAiStatusMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        // 配置Netty HttpClient：5秒连接超时，10秒响应超时
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SECONDS));

        this.webClient = WebClient.builder()
                .baseUrl(aiEngineBaseUrl)
                .defaultHeader("X-Service-Token", serviceToken)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * 触发AI处理管线（新版）
     *
     * 从wf_node_def表加载每个节点类型的ai_spec_json，注入到canvas_json中，
     * 然后将完整请求异步发送到AI引擎的 /api/orchestration/process 端点。
     *
     * 处理流程：
     * 1. 解析canvasJson为Jackson ObjectNode
     * 2. 遍历phases中的所有节点，按node_type查询wf_node_def获取ai_spec_json
     * 3. 将ai_spec_json注入到对应节点的"ai_spec"字段
     * 4. 构建请求体（templateId、orchestrationId、enriched canvas、callbackUrl）
     * 5. 异步POST到AI引擎
     *
     * @param templateId 模板ID
     * @param canvasJson 原始画布JSON字符串（阶段化结构）
     */
    @Override
    @Async("templateAiExecutor")
    public void triggerAiPipeline(Long templateId, String canvasJson) {
        log.info("Triggering AI pipeline for template {}", templateId);

        try {
            // 1. 解析canvas JSON
            ObjectNode canvas = (ObjectNode) objectMapper.readTree(canvasJson);

            // 2. 为每个节点注入ai_spec
            enrichWithAiSpecs(canvas);

            // 3. 构建请求体（注意字段名使用下划线格式，与AI引擎期望一致）
            String callbackUrl = buildCallbackUrl();
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("template_id", templateId);
            requestBody.put("orchestration_id", "tmpl_" + templateId);
            requestBody.put("canvas_json", canvas);
            requestBody.put("callback_url", callbackUrl);

            log.info("Sending enriched canvas to AI engine for template {}, callbackUrl={}", templateId, callbackUrl);

            // 4. 发送到AI Engine
            Map response = webClient.post()
                    .uri("/api/orchestration/process")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("job_id")) {
                log.info("AI engine accepted template {} pipeline, jobId={}", templateId, response.get("job_id"));
            } else {
                String errorMsg = "AI引擎返回无效响应，缺少job_id";
                log.error("Template {} - {}", templateId, errorMsg);
                markTemplateAsFailed(templateId, errorMsg);
            }

        } catch (Exception e) {
            Throwable cause = findRootCause(e);
            String errorMsg;

            if (cause instanceof ConnectException) {
                errorMsg = "AI引擎不可用，无法连接（请检查 AI 引擎是否已启动在 " + aiEngineBaseUrl + "）";
            } else if (cause instanceof TimeoutException
                    || cause instanceof io.netty.handler.timeout.ReadTimeoutException) {
                errorMsg = "AI引擎响应超时，请稍后重试";
            } else if (e instanceof WebClientResponseException) {
                WebClientResponseException responseEx = (WebClientResponseException) e;
                errorMsg = "AI引擎返回错误，HTTP状态码: " + responseEx.getStatusCode().value();
            } else {
                errorMsg = "AI引擎调用异常: " + e.getMessage();
            }

            log.error("Template {} - AI pipeline trigger failed: {}", templateId, errorMsg, e);
            markTemplateAsFailed(templateId, errorMsg);
        }
    }

    /**
     * 重试失败节点的AI处理
     *
     * 当模板AI处理部分失败时，仅重新处理指定的失败节点。
     * 已成功处理的节点结果保留不变。
     *
     * 处理流程：
     * 1. 查询wf_node_ai_status中的失败节点记录
     * 2. 从模板的raw_canvas_json中提取失败节点的完整数据
     * 3. 构建仅含失败节点的部分canvas（保留阶段结构）
     * 4. 为部分canvas注入ai_spec
     * 5. 更新失败节点状态为处理中，递增retry_count
     * 6. 发送到AI引擎 /api/orchestration/retry 端点
     *
     * @param templateId 模板ID
     * @param nodeIds    需要重试的失败节点ID列表
     */
    @Override
    @Async("templateAiExecutor")
    public void retryFailedNodes(Long templateId, List<String> nodeIds) {
        log.info("Retry failed nodes for template {}, nodeIds={}", templateId, nodeIds);

        try {
            // 1. 查询wf_node_ai_status中状态为失败(3)的节点记录
            LambdaQueryWrapper<WfNodeAiStatus> statusQuery = new LambdaQueryWrapper<>();
            statusQuery.eq(WfNodeAiStatus::getTemplateId, templateId)
                    .in(WfNodeAiStatus::getNodeId, nodeIds)
                    .eq(WfNodeAiStatus::getAiStatus, 3);
            List<WfNodeAiStatus> failedNodes = nodeAiStatusMapper.selectList(statusQuery);

            if (failedNodes.isEmpty()) {
                log.warn("Template {} - no failed nodes found for nodeIds={}", templateId, nodeIds);
                return;
            }

            Set<String> failedNodeIds = failedNodes.stream()
                    .map(WfNodeAiStatus::getNodeId)
                    .collect(Collectors.toSet());

            log.info("Template {} - found {} failed nodes to retry: {}", templateId, failedNodeIds.size(), failedNodeIds);

            // 2. 查询模板的raw_canvas_json获取完整画布
            WfTrainingTemplate template = trainingTemplateMapper.selectById(templateId);
            if (template == null) {
                log.error("Template {} not found when retrying failed nodes", templateId);
                return;
            }

            if (template.getRawCanvasJson() == null) {
                log.error("Template {} has no raw_canvas_json", templateId);
                markFailedNodesAsError(failedNodes, "模板画布数据为空");
                return;
            }

            // 3. 解析canvas并构建仅含失败节点的部分canvas
            String canvasStr = objectMapper.writeValueAsString(template.getRawCanvasJson());
            ObjectNode fullCanvas = (ObjectNode) objectMapper.readTree(canvasStr);
            ObjectNode partialCanvas = buildPartialCanvas(fullCanvas, failedNodeIds);

            if (partialCanvas == null) {
                log.error("Template {} - failed to build partial canvas for retry", templateId);
                markFailedNodesAsError(failedNodes, "构建重试画布失败");
                return;
            }

            // 4. 为部分canvas注入ai_spec
            enrichWithAiSpecs(partialCanvas);

            // 5. 更新失败节点状态为处理中(1)，递增retry_count，更新last_processed_at
            LocalDateTime now = LocalDateTime.now();
            for (WfNodeAiStatus nodeStatus : failedNodes) {
                LambdaUpdateWrapper<WfNodeAiStatus> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(WfNodeAiStatus::getId, nodeStatus.getId())
                        .set(WfNodeAiStatus::getAiStatus, 1)
                        .set(WfNodeAiStatus::getRetryCount, nodeStatus.getRetryCount() + 1)
                        .set(WfNodeAiStatus::getLastProcessedAt, now)
                        .set(WfNodeAiStatus::getErrorReason, null)
                        .set(WfNodeAiStatus::getUpdatedAt, now);
                nodeAiStatusMapper.update(null, updateWrapper);
            }

            // 6. 构建请求体并发送到AI引擎重试端点
            String callbackUrl = buildCallbackUrl();
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("templateId", templateId);
            requestBody.put("orchestrationId", "tmpl_" + templateId + "_retry");
            requestBody.put("canvasJson", partialCanvas);
            requestBody.put("callbackUrl", callbackUrl);
            requestBody.put("retryNodeIds", new ArrayList<>(failedNodeIds));

            log.info("Sending retry request to AI engine for template {}, nodes={}, callbackUrl={}",
                    templateId, failedNodeIds, callbackUrl);

            Map response = webClient.post()
                    .uri("/api/orchestration/retry")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("jobId")) {
                log.info("AI engine accepted retry for template {}, jobId={}", templateId, response.get("jobId"));
            } else {
                String errorMsg = "AI引擎重试请求返回无效响应，缺少jobId";
                log.error("Template {} - {}", templateId, errorMsg);
                markFailedNodesAsError(failedNodes, errorMsg);
            }

        } catch (Exception e) {
            Throwable cause = findRootCause(e);
            String errorMsg;

            if (cause instanceof ConnectException) {
                errorMsg = "AI引擎连接失败，重试请求未送达";
            } else if (cause instanceof TimeoutException
                    || cause instanceof io.netty.handler.timeout.ReadTimeoutException) {
                errorMsg = "AI引擎重试请求响应超时";
            } else if (e instanceof WebClientResponseException) {
                WebClientResponseException responseEx = (WebClientResponseException) e;
                errorMsg = "AI引擎重试请求返回错误，HTTP状态码: " + responseEx.getStatusCode().value();
            } else {
                errorMsg = "AI引擎重试请求异常: " + e.getMessage();
            }

            log.error("Template {} - retry failed nodes error: {}", templateId, errorMsg, e);

            // 将节点重新标记为失败
            try {
                LambdaQueryWrapper<WfNodeAiStatus> query = new LambdaQueryWrapper<>();
                query.eq(WfNodeAiStatus::getTemplateId, templateId)
                        .in(WfNodeAiStatus::getNodeId, nodeIds)
                        .eq(WfNodeAiStatus::getAiStatus, 1);
                List<WfNodeAiStatus> processingNodes = nodeAiStatusMapper.selectList(query);
                markFailedNodesAsError(processingNodes, errorMsg);
            } catch (Exception ex) {
                log.error("Template {} - failed to revert node status after retry error: {}", templateId, ex.getMessage(), ex);
            }
        }
    }

    /**
     * 构建仅包含失败节点的部分canvas（保留阶段结构）
     *
     * 遍历完整canvas的所有阶段，提取匹配失败节点ID的节点，
     * 仅保留包含失败节点的阶段。
     *
     * @param fullCanvas 完整画布JSON
     * @param failedNodeIds 失败节点ID集合
     * @return 部分canvas，仅含失败节点；若无匹配节点返回null
     */
    private ObjectNode buildPartialCanvas(ObjectNode fullCanvas, Set<String> failedNodeIds) {
        JsonNode phasesNode = fullCanvas.get("phases");
        if (phasesNode == null || !phasesNode.isArray()) {
            log.warn("Full canvas has no 'phases' array, cannot build partial canvas");
            return null;
        }

        ObjectNode partialCanvas = objectMapper.createObjectNode();
        // 保留orchestration_id等顶层字段
        if (fullCanvas.has("orchestration_id")) {
            partialCanvas.set("orchestration_id", fullCanvas.get("orchestration_id"));
        }

        ArrayNode partialPhases = objectMapper.createArrayNode();
        ArrayNode phases = (ArrayNode) phasesNode;

        for (int i = 0; i < phases.size(); i++) {
            JsonNode phase = phases.get(i);
            JsonNode nodesNode = phase.get("nodes");
            if (nodesNode == null || !nodesNode.isArray()) {
                continue;
            }

            // 筛选出属于失败节点的节点
            ArrayNode filteredNodes = objectMapper.createArrayNode();
            ArrayNode nodes = (ArrayNode) nodesNode;
            for (int j = 0; j < nodes.size(); j++) {
                JsonNode node = nodes.get(j);
                JsonNode nodeIdNode = node.get("node_id");
                if (nodeIdNode != null && failedNodeIds.contains(nodeIdNode.asText())) {
                    filteredNodes.add(node);
                }
            }

            // 仅保留包含失败节点的阶段
            if (filteredNodes.size() > 0) {
                ObjectNode partialPhase = objectMapper.createObjectNode();
                // 保留阶段元数据
                if (phase.has("phase_id")) {
                    partialPhase.set("phase_id", phase.get("phase_id"));
                }
                if (phase.has("phase_name")) {
                    partialPhase.set("phase_name", phase.get("phase_name"));
                }
                if (phase.has("sort_num")) {
                    partialPhase.set("sort_num", phase.get("sort_num"));
                }
                partialPhase.set("nodes", filteredNodes);
                // 不包含edges，重试时不需要连线信息
                partialPhase.set("edges", objectMapper.createArrayNode());
                partialPhases.add(partialPhase);
            }
        }

        if (partialPhases.size() == 0) {
            log.warn("No matching failed nodes found in canvas phases");
            return null;
        }

        partialCanvas.set("phases", partialPhases);
        return partialCanvas;
    }

    /**
     * 将节点列表标记为失败状态
     *
     * @param nodes 节点状态记录列表
     * @param errorReason 错误原因
     */
    private void markFailedNodesAsError(List<WfNodeAiStatus> nodes, String errorReason) {
        LocalDateTime now = LocalDateTime.now();
        for (WfNodeAiStatus nodeStatus : nodes) {
            LambdaUpdateWrapper<WfNodeAiStatus> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(WfNodeAiStatus::getId, nodeStatus.getId())
                    .set(WfNodeAiStatus::getAiStatus, 3)
                    .set(WfNodeAiStatus::getErrorReason, errorReason)
                    .set(WfNodeAiStatus::getUpdatedAt, now);
            nodeAiStatusMapper.update(null, updateWrapper);
        }
    }

    /**
     * 为canvas中的每个节点注入ai_spec
     *
     * 遍历canvas中所有phases的所有nodes，根据node_type从wf_node_def表
     * 查询ai_spec_json，如果存在则将其作为"ai_spec"字段附加到节点上。
     *
     * 双格式支持说明：
     * node_type 始终位于节点对象的顶层（phases[].nodes[].node_type），
     * 而非嵌套在 config 内部。因此无论节点配置是扁平格式（如 {resource_id: 123}）
     * 还是6维度格式（{display: {}, db_mapping: {}, ...}），enrichment 逻辑均可正确
     * 定位 node_type 并注入 ai_spec。
     *
     * 前提条件：wf_node_def 表中所有节点类型（包括12种非AI密集型节点）
     * 均需有对应的 ai_spec_json 记录，否则该节点将被跳过（仅记录日志）。
     *
     * @param canvas 画布JSON ObjectNode（会被原地修改）
     */
    private void enrichWithAiSpecs(ObjectNode canvas) {
        JsonNode phasesNode = canvas.get("phases");
        if (phasesNode == null || !phasesNode.isArray()) {
            log.warn("Canvas has no 'phases' array, skipping ai_spec enrichment");
            return;
        }

        ArrayNode phases = (ArrayNode) phasesNode;
        int enrichedCount = 0;
        int skippedCount = 0;

        for (int i = 0; i < phases.size(); i++) {
            JsonNode phase = phases.get(i);
            JsonNode nodesNode = phase.get("nodes");
            if (nodesNode == null || !nodesNode.isArray()) {
                continue;
            }

            ArrayNode nodes = (ArrayNode) nodesNode;
            for (int j = 0; j < nodes.size(); j++) {
                JsonNode node = nodes.get(j);
                if (!node.isObject()) {
                    continue;
                }

                ObjectNode nodeObj = (ObjectNode) node;

                // node_type is always at the node level, regardless of config format (flat or 6-dimension)
                JsonNode nodeTypeNode = nodeObj.get("node_type");
                if (nodeTypeNode == null || nodeTypeNode.isNull()) {
                    continue;
                }

                String nodeType = nodeTypeNode.asText();
                if (nodeType.isEmpty()) {
                    continue;
                }

                // Normalize node_type to lowercase for lookup. Frontend uppercases node types
                // (e.g. mindmap_draw → MINDMAP_DRAW) via NODE_TYPE_MAP, but wf_node_def seed
                // data uses lowercase keys. Without normalization, ai_spec injection silently
                // fails for every node and the AI engine receives nothing to process.
                String lookupKey = nodeType.toLowerCase();

                // Lookup wf_node_def by node_type (case-insensitive)
                LambdaQueryWrapper<WfNodeDef> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(WfNodeDef::getNodeType, lookupKey);
                WfNodeDef nodeDef = nodeDefMapper.selectOne(queryWrapper);

                if (nodeDef != null && nodeDef.getAiSpecJson() != null) {
                    try {
                        // Convert ai_spec_json (Object) to JsonNode and attach to node
                        JsonNode aiSpecJsonNode = objectMapper.valueToTree(nodeDef.getAiSpecJson());
                        nodeObj.set("ai_spec", aiSpecJsonNode);
                        // Normalize node_type to the canonical (lowercase) form so the AI engine
                        // and downstream consumers see a consistent value.
                        if (!lookupKey.equals(nodeType)) {
                            nodeObj.put("node_type", lookupKey);
                        }
                        enrichedCount++;
                    } catch (Exception e) {
                        log.warn("Failed to parse ai_spec_json for node_type={}: {}", nodeType, e.getMessage());
                        skippedCount++;
                    }
                } else {
                    log.warn("No ai_spec_json found in wf_node_def for node_type='{}' (lookup key '{}'), skipping enrichment", nodeType, lookupKey);
                    skippedCount++;
                }
            }
        }

        log.info("AI spec enrichment complete: {} nodes enriched, {} nodes skipped", enrichedCount, skippedCount);
    }

    /**
     * 提交模板给AI引擎处理（旧版接口，保留向后兼容）
     *
     * @param templateId 模板ID
     * @param canvasJson 原始画布JSON
     * @return jobId 异步任务ID
     * @deprecated 使用 {@link #triggerAiPipeline(Long, String)} 替代
     */
    @Override
    @Deprecated
    public String submitForProcessing(Long templateId, Object canvasJson) {
        String callbackUrl = buildCallbackUrl();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("templateId", templateId);
        requestBody.put("orchestrationId", String.valueOf(templateId));
        requestBody.put("canvasJson", canvasJson);
        requestBody.put("callbackUrl", callbackUrl);

        log.info("Submitting template {} to AI engine for processing, callbackUrl={}", templateId, callbackUrl);

        try {
            Map response = webClient.post()
                    .uri("/api/orchestration/process")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("jobId")) {
                String errorMsg = "AI引擎返回无效响应，缺少jobId";
                log.error("Template {} - {}", templateId, errorMsg);
                markTemplateAsFailed(templateId, errorMsg);
                throw new RuntimeException(errorMsg);
            }

            String jobId = (String) response.get("jobId");
            log.info("AI engine accepted template {}, jobId={}", templateId, jobId);
            return jobId;

        } catch (RuntimeException e) {
            Throwable cause = findRootCause(e);

            if (cause instanceof ConnectException) {
                String errorMsg = "AI引擎连接失败，请稍后重试";
                log.error("Template {} - AI引擎连接失败: {}", templateId, cause.getMessage(), e);
                markTemplateAsFailed(templateId, errorMsg);
                throw new RuntimeException(errorMsg, e);
            }

            if (cause instanceof TimeoutException
                    || cause instanceof io.netty.handler.timeout.ReadTimeoutException) {
                String errorMsg = "AI引擎响应超时，请稍后重试";
                log.error("Template {} - AI引擎响应超时: {}", templateId, cause.getMessage(), e);
                markTemplateAsFailed(templateId, errorMsg);
                throw new RuntimeException(errorMsg, e);
            }

            if (e instanceof WebClientResponseException) {
                WebClientResponseException responseEx = (WebClientResponseException) e;
                String errorMsg = "AI引擎返回错误，HTTP状态码: " + responseEx.getStatusCode().value();
                log.error("Template {} - AI引擎HTTP错误: status={}, body={}",
                        templateId, responseEx.getStatusCode(), responseEx.getResponseBodyAsString(), e);
                markTemplateAsFailed(templateId, errorMsg);
                throw new RuntimeException(errorMsg, e);
            }

            // 其他未知运行时异常
            if (!(e.getMessage() != null && e.getMessage().contains("AI引擎返回无效响应"))) {
                String errorMsg = "AI引擎调用异常: " + e.getMessage();
                log.error("Template {} - AI引擎调用异常: {}", templateId, e.getMessage(), e);
                markTemplateAsFailed(templateId, errorMsg);
            }
            throw e;
        }
    }

    /**
     * 查询AI处理状态（备用轮询方案）
     *
     * 当回调机制失败时，可通过此方法主动查询AI引擎的处理状态。
     *
     * @param jobId 任务ID
     * @return 处理状态信息
     */
    @Override
    public AiJobStatus queryStatus(String jobId) {
        log.info("Querying AI engine status for jobId={}", jobId);

        try {
            AiJobStatus status = webClient.get()
                    .uri("/api/orchestration/status/{jobId}", jobId)
                    .retrieve()
                    .bodyToMono(AiJobStatus.class)
                    .block();

            if (status == null) {
                throw new RuntimeException("AI引擎状态查询返回空响应, jobId=" + jobId);
            }

            return status;

        } catch (RuntimeException e) {
            Throwable cause = findRootCause(e);

            if (cause instanceof ConnectException) {
                log.error("AI引擎连接失败，无法查询状态, jobId={}: {}", jobId, cause.getMessage(), e);
                throw new RuntimeException("AI引擎连接失败，无法查询状态", e);
            }

            if (cause instanceof TimeoutException
                    || cause instanceof io.netty.handler.timeout.ReadTimeoutException) {
                log.error("AI引擎状态查询超时, jobId={}: {}", jobId, cause.getMessage(), e);
                throw new RuntimeException("AI引擎状态查询超时", e);
            }

            if (e instanceof WebClientResponseException) {
                WebClientResponseException responseEx = (WebClientResponseException) e;
                log.error("AI引擎状态查询HTTP错误, jobId={}: status={}", jobId, responseEx.getStatusCode(), e);
                throw new RuntimeException("AI引擎状态查询失败，HTTP状态码: " + responseEx.getStatusCode().value(), e);
            }

            log.error("AI引擎状态查询异常, jobId={}: {}", jobId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 标记模板AI处理失败
     *
     * 更新模板的 ai_status 为 3（失败），并记录错误原因。
     *
     * @param templateId 模板ID
     * @param errorReason 错误原因（中文描述）
     */
    private void markTemplateAsFailed(Long templateId, String errorReason) {
        try {
            WfTrainingTemplate template = trainingTemplateMapper.selectById(templateId);
            if (template != null) {
                template.setAiStatus(3);
                template.setErrorReason(errorReason);
                template.setUpdatedAt(LocalDateTime.now());
                trainingTemplateMapper.updateById(template);
                log.info("Template {} marked as failed, ai_status=3, error_reason={}", templateId, errorReason);
            } else {
                log.warn("Template {} not found when trying to mark as failed", templateId);
            }
        } catch (Exception ex) {
            log.error("Failed to update template {} ai_status to 3: {}", templateId, ex.getMessage(), ex);
        }
    }

    /**
     * 查找异常的根本原因
     *
     * @param e 异常
     * @return 根本原因异常
     */
    private Throwable findRootCause(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * 构建回调URL
     *
     * 从服务器配置中构建回调地址，指向内部回调端点。
     * 始终使用 localhost + 配置的端口号，确保 AI 引擎能回调到本机后端。
     *
     * @return 完整的回调URL
     */
    private String buildCallbackUrl() {
        return "http://localhost:" + serverPort + "/api/internal/ai/callback";
    }
}
