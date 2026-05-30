package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartconstruct.backend_core.entity.AiProcessingLog;
import com.smartconstruct.backend_core.mapper.AiProcessingLogMapper;
import com.smartconstruct.backend_core.service.IAiProcessingLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI处理日志服务实现
 *
 * 异步记录AI处理过程中的关键事件到 ai_processing_log 表。
 * 记录操作使用 @Async 避免阻塞主流程。
 *
 * @author SmartConstruct
 */
@Service
public class AiProcessingLogServiceImpl implements IAiProcessingLogService {

    private static final Logger log = LoggerFactory.getLogger(AiProcessingLogServiceImpl.class);

    private final AiProcessingLogMapper mapper;

    public AiProcessingLogServiceImpl(AiProcessingLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Async("templateAiExecutor")
    public void log(Long templateId, String eventType, String eventLabel,
                    String eventStatus, String message) {
        doLog(templateId, null, null, null, eventType, eventLabel, eventStatus, message, null);
    }

    @Override
    @Async("templateAiExecutor")
    public void logDetail(Long templateId, String nodeId, String nodeType, String jobId,
                          String eventType, String eventLabel, String eventStatus,
                          String message, Object detailJson) {
        doLog(templateId, nodeId, nodeType, jobId, eventType, eventLabel, eventStatus, message, detailJson);
    }

    private void doLog(Long templateId, String nodeId, String nodeType, String jobId,
                       String eventType, String eventLabel, String eventStatus,
                       String message, Object detailJson) {
        try {
            AiProcessingLog entity = new AiProcessingLog();
            entity.setTemplateId(templateId);
            entity.setNodeId(nodeId);
            entity.setNodeType(nodeType);
            entity.setJobId(jobId);
            entity.setEventType(eventType);
            entity.setEventLabel(eventLabel);
            entity.setEventStatus(eventStatus);
            entity.setMessage(message);
            entity.setDetailJson(detailJson);
            entity.setCreatedAt(LocalDateTime.now());
            mapper.insert(entity);
        } catch (Exception e) {
            // 日志记录失败不应影响主流程
            log.warn("Failed to save AI processing log for templateId={}, eventType={}: {}",
                    templateId, eventType, e.getMessage());
        }
    }

    @Override
    public List<AiProcessingLog> getLogsByTemplateId(Long templateId) {
        LambdaQueryWrapper<AiProcessingLog> query = new LambdaQueryWrapper<>();
        query.eq(AiProcessingLog::getTemplateId, templateId)
                .orderByAsc(AiProcessingLog::getCreatedAt);
        return mapper.selectList(query);
    }
}