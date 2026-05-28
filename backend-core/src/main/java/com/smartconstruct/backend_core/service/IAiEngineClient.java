package com.smartconstruct.backend_core.service;

import com.smartconstruct.backend_core.dto.AiJobStatus;

import java.util.List;

/**
 * AI引擎客户端接口
 *
 * 负责与Python AI引擎（ai-engine）通过HTTP通信，管理模板的AI处理管线。
 * 通信模式为异步触发 + 回调通知：
 * <ol>
 *   <li>后端调用 triggerAiPipeline 或 retryFailedNodes 向AI引擎发送处理请求</li>
 *   <li>AI引擎返回202 Accepted（含job_id），后端不阻塞等待</li>
 *   <li>AI引擎处理完成后通过 POST /api/internal/ai/callback 回调通知后端结果</li>
 *   <li>若回调失败，后端通过定时轮询（queryStatus）作为兜底方案</li>
 * </ol>
 *
 * <p>AI引擎端点：
 * <ul>
 *   <li>处理请求：POST {ai-engine.base-url}/api/orchestration/process</li>
 *   <li>选择性重试：POST {ai-engine.base-url}/api/orchestration/retry</li>
 *   <li>状态查询：GET {ai-engine.base-url}/api/orchestration/status/{jobId}</li>
 * </ul>
 *
 * @author SmartConstruct
 * @see com.smartconstruct.backend_core.dto.AiJobStatus
 */
public interface IAiEngineClient {

    /**
     * 触发AI处理管线
     *
     * <p>模板发布时调用此方法，异步触发AI引擎对模板中所有需要AI处理的节点进行内容生成。
     * 方法内部会从 wf_node_def 表加载每个节点类型的 ai_spec_json，注入到 canvas_json 中，
     * 然后将完整请求发送到AI引擎的 /api/orchestration/process 端点。</p>
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>解析 canvasJson，遍历所有阶段（phases）中的所有节点</li>
     *   <li>根据每个节点的 node_type 从 wf_node_def 表查询 ai_spec_json</li>
     *   <li>将 ai_spec_json 注入到对应节点数据中（enrichWithAiSpecs）</li>
     *   <li>构建请求体（含 templateId、orchestrationId、enriched canvasJson、callbackUrl）</li>
     *   <li>异步发送 HTTP POST 到 AI引擎，不阻塞调用方</li>
     * </ol>
     *
     * <p>注意事项：</p>
     * <ul>
     *   <li>此方法为异步执行（@Async），调用后立即返回，不等待AI处理完成</li>
     *   <li>AI处理结果通过回调模式（Callback Pattern）异步通知后端</li>
     *   <li>调用前应确保模板 ai_status 已设为 1（处理中），避免重复触发</li>
     *   <li>若AI引擎不可达，异常将被记录，后端定时轮询任务会兜底检测超时模板</li>
     * </ul>
     *
     * @param templateId 模板ID（对应 wf_training_template.id）
     * @param canvasJson 原始画布JSON字符串，包含阶段化结构：
     *                   {@code {"orchestration_id":"...", "phases":[{"phase_id":"...", "nodes":[...], "edges":[...]}]}}
     */
    void triggerAiPipeline(Long templateId, String canvasJson);

    /**
     * 重试失败节点的AI处理
     *
     * <p>当模板AI处理部分失败（ai_status=3）时，教师可选择重试。此方法仅重新处理
     * 指定的失败节点，已成功处理的节点结果保留不变。方法内部会从 wf_node_ai_status 表
     * 查询失败节点信息，构建仅包含失败节点的部分 canvas，发送到AI引擎的重试端点。</p>
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>根据 templateId 和 nodeIds 查询 wf_node_ai_status 中的失败节点记录</li>
     *   <li>从原始 canvas_json 中提取失败节点的完整数据（含 config、ai_spec）</li>
     *   <li>构建部分 canvas（仅含失败节点），发送到 AI引擎 /api/orchestration/retry</li>
     *   <li>更新失败节点的 retry_count 和 ai_status 为 1（处理中）</li>
     * </ol>
     *
     * <p>约束条件：</p>
     * <ul>
     *   <li>单个模板最大重试次数为5次，超过后应拒绝重试并提示教师联系管理员</li>
     *   <li>仅 ai_status=3（失败）的节点可被重试，已成功节点不会被重新处理</li>
     *   <li>此方法为异步执行，重试结果同样通过回调模式通知</li>
     * </ul>
     *
     * @param templateId 模板ID（对应 wf_training_template.id）
     * @param nodeIds    需要重试的失败节点ID列表（对应 wf_node_ai_status.node_id）
     */
    void retryFailedNodes(Long templateId, List<String> nodeIds);

    /**
     * 提交模板给AI引擎处理（旧版接口，保留向后兼容）
     *
     * <p>通过HTTP POST将模板数据发送到AI引擎的 /api/orchestration/process 端点，
     * AI引擎接收后返回jobId，后续通过回调通知处理结果。</p>
     *
     * @param templateId 模板ID（对应 wf_training_template.id）
     * @param canvasJson 原始画布JSON（包含nodes和edges）
     * @return jobId 异步任务ID（UUID格式）
     * @deprecated 使用 {@link #triggerAiPipeline(Long, String)} 替代，新方法支持阶段化canvas和ai_spec注入
     */
    @Deprecated
    String submitForProcessing(Long templateId, Object canvasJson);

    /**
     * 查询AI处理状态（备用轮询方案）
     *
     * <p>当回调机制失败时，可通过此方法主动查询AI引擎的处理状态。
     * 定时轮询任务（AiTimeoutDetectionTask）每60秒检查 ai_status=1 且超过10分钟的模板，
     * 调用此方法获取最新状态。</p>
     *
     * @param jobId 任务ID（由 submitForProcessing 或 triggerAiPipeline 触发后AI引擎返回）
     * @return 处理状态信息，包含 jobId、status、estimatedRemainingSeconds、errorReason
     */
    AiJobStatus queryStatus(String jobId);
}
