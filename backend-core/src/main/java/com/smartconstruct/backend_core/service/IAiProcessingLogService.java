package com.smartconstruct.backend_core.service;

import com.smartconstruct.backend_core.entity.AiProcessingLog;

import java.util.List;

/**
 * AI处理日志服务接口
 *
 * @author SmartConstruct
 */
public interface IAiProcessingLogService {

    /**
     * 记录AI处理事件
     *
     * @param templateId  模板ID
     * @param eventType   事件类型
     * @param eventLabel  事件中文标签
     * @param eventStatus 状态: processing/success/failed/warning
     * @param message     描述文本
     */
    void log(Long templateId, String eventType, String eventLabel, String eventStatus, String message);

    /**
     * 记录带详情的AI处理事件
     *
     * @param templateId  模板ID
     * @param nodeId      节点ID（可选）
     * @param nodeType    节点类型（可选）
     * @param jobId       AI引擎job_id（可选）
     * @param eventType   事件类型
     * @param eventLabel  事件中文标签
     * @param eventStatus 状态
     * @param message     描述文本
     * @param detailJson  详情JSON（可选）
     */
    void logDetail(Long templateId, String nodeId, String nodeType, String jobId,
                   String eventType, String eventLabel, String eventStatus,
                   String message, Object detailJson);

    /**
     * 查询模板的AI处理日志列表（按时间升序）
     *
     * @param templateId 模板ID
     * @return 日志列表
     */
    List<AiProcessingLog> getLogsByTemplateId(Long templateId);
}