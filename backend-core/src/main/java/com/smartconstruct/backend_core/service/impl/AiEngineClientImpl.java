package com.smartconstruct.backend_core.service.impl;

import com.smartconstruct.backend_core.dto.AiJobStatus;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.IAiEngineClient;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * AI引擎客户端实现类
 *
 * 使用Spring WebClient（非阻塞HTTP）与Python AI引擎通信。
 * 提交模板数据到 /api/orchestration/process 端点进行AI处理，
 * 并支持通过 /api/orchestration/status/{jobId} 查询处理状态。
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
     * 提交模板给AI引擎处理
     *
     * 通过HTTP POST将模板数据发送到AI引擎的 /api/orchestration/process 端点。
     * AI引擎接收后返回jobId，后续通过回调通知处理结果。
     *
     * 错误处理：
     * - ConnectException: AI引擎连接失败 → ai_status=3
     * - TimeoutException: AI引擎响应超时 → ai_status=3
     * - WebClientResponseException: AI引擎返回HTTP错误 → ai_status=3
     *
     * @param templateId 模板ID
     * @param canvasJson 原始画布JSON
     * @return jobId 异步任务ID（UUID格式）
     */
    @Override
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

            // 其他未知运行时异常（包括上面的"无效响应"情况，已经在上面处理过了）
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
     *
     * @return 完整的回调URL
     */
    private String buildCallbackUrl() {
        String baseUrl = serverAddress;
        // If serverAddress doesn't start with http, construct the full URL
        if (!baseUrl.startsWith("http")) {
            baseUrl = "http://" + baseUrl + ":" + serverPort;
        }
        return baseUrl + "/api/internal/ai-callback";
    }
}
