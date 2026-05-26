package com.smartconstruct.backend_core.service;

import com.smartconstruct.backend_core.dto.AiJobStatus;

/**
 * AI引擎客户端接口
 *
 * 负责与Python AI引擎通过HTTP通信，提交模板进行AI处理并查询处理状态。
 * 主要通信模式为异步回调，轮询接口作为回调失败时的备用方案。
 *
 * @author SmartConstruct
 */
public interface IAiEngineClient {

    /**
     * 提交模板给AI引擎处理
     *
     * 通过HTTP POST将模板数据发送到AI引擎的 /api/orchestration/process 端点，
     * AI引擎接收后返回jobId，后续通过回调通知处理结果。
     *
     * @param templateId 模板ID（对应 wf_training_template.id）
     * @param canvasJson 原始画布JSON（包含nodes和edges）
     * @return jobId 异步任务ID（UUID格式）
     */
    String submitForProcessing(Long templateId, Object canvasJson);

    /**
     * 查询AI处理状态（备用轮询方案）
     *
     * 当回调机制失败时，可通过此方法主动查询AI引擎的处理状态。
     *
     * @param jobId 任务ID（由 submitForProcessing 返回）
     * @return 处理状态信息
     */
    AiJobStatus queryStatus(String jobId);
}
