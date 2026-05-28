package com.smartconstruct.backend_core.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartconstruct.backend_core.dto.AiJobStatus;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.IAiEngineClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI处理超时检测定时任务
 *
 * 每60秒检查 ai_status=1（处理中）且 updated_at 超过10分钟的模板，
 * 尝试通过AI引擎查询实际处理状态：
 * <ul>
 *   <li>若AI引擎报告处理完成(status=2)，更新模板为已就绪</li>
 *   <li>若AI引擎报告处理失败(status=3)，标记模板为失败并记录原因</li>
 *   <li>若AI引擎不可达或查询异常，标记模板为失败（AI处理超时）</li>
 * </ul>
 *
 * 此任务作为回调机制的兜底方案，确保模板不会永久停留在"处理中"状态。
 *
 * @author SmartConstruct
 * @version 2.0.0
 * @see com.smartconstruct.backend_core.service.IAiEngineClient#queryStatus(String)
 */
@Component
public class AiTimeoutDetectionTask {

    private static final Logger log = LoggerFactory.getLogger(AiTimeoutDetectionTask.class);

    /** AI处理中状态 */
    private static final int AI_STATUS_PROCESSING = 1;

    /** AI处理成功状态 */
    private static final int AI_STATUS_SUCCESS = 2;

    /** AI处理失败状态 */
    private static final int AI_STATUS_FAILED = 3;

    /** 超时阈值（分钟） */
    private static final int TIMEOUT_MINUTES = 10;

    @Autowired
    private WfTrainingTemplateMapper templateMapper;

    @Autowired
    private IAiEngineClient aiEngineClient;

    /**
     * 每60秒执行一次，检测超时的AI处理任务。
     *
     * 对于每个超时模板，先尝试通过AI引擎查询状态（使用 tmpl_{templateId} 作为jobId），
     * 根据查询结果更新模板状态。若AI引擎不可达则直接标记为超时失败。
     */
    @Scheduled(fixedRate = 60000)
    public void detectStaleAiProcessing() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);

        // Query templates that are still processing and have exceeded the timeout threshold
        LambdaQueryWrapper<WfTrainingTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WfTrainingTemplate::getAiStatus, AI_STATUS_PROCESSING)
                    .lt(WfTrainingTemplate::getUpdatedAt, timeoutThreshold);

        List<WfTrainingTemplate> staleTemplates = templateMapper.selectList(queryWrapper);

        if (staleTemplates.isEmpty()) {
            return;
        }

        log.warn("Detected {} stale AI processing template(s) exceeding {} minute timeout",
                staleTemplates.size(), TIMEOUT_MINUTES);

        for (WfTrainingTemplate template : staleTemplates) {
            try {
                processStaleTemplate(template);
            } catch (Exception e) {
                // Don't let one failed template block processing of others
                log.error("Error processing stale template [id={}]: {}",
                        template.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * 处理单个超时模板
     *
     * 尝试通过AI引擎查询状态，根据结果更新模板：
     * - status=2: AI引擎已完成但回调失败，标记模板为成功
     * - status=3: AI引擎报告失败，标记模板为失败
     * - status=1: AI引擎仍在处理中（异常情况），标记为超时失败
     * - 查询异常: AI引擎不可达，标记为超时失败
     *
     * @param template 超时的模板记录
     */
    private void processStaleTemplate(WfTrainingTemplate template) {
        Long templateId = template.getId();
        String jobId = "tmpl_" + templateId;

        log.warn("Template [id={}] has been in AI processing state for over {} minutes, querying AI engine status...",
                templateId, TIMEOUT_MINUTES);

        try {
            AiJobStatus status = aiEngineClient.queryStatus(jobId);

            if (status != null && status.getStatus() != null) {
                handleAiEngineResponse(template, status);
            } else {
                // Null response or null status — treat as unreachable
                markTemplateAsFailed(templateId, "AI处理超时：AI引擎状态查询返回空响应");
            }
        } catch (Exception e) {
            // AI engine is unreachable or returned an error — mark as failed
            log.warn("Template [id={}] - AI engine unreachable during status query: {}",
                    templateId, e.getMessage());
            markTemplateAsFailed(templateId, "AI处理超时：AI引擎不可达 (" + e.getMessage() + ")");
        }
    }

    /**
     * 根据AI引擎返回的状态更新模板
     *
     * @param template 模板记录
     * @param status AI引擎返回的状态
     */
    private void handleAiEngineResponse(WfTrainingTemplate template, AiJobStatus status) {
        Long templateId = template.getId();

        switch (status.getStatus()) {
            case AI_STATUS_SUCCESS:
                // AI engine reports completion — callback must have failed
                log.info("Template [id={}] - AI engine reports completion (callback may have failed), marking as success",
                        templateId);
                markTemplateAsSuccess(templateId);
                break;

            case AI_STATUS_FAILED:
                // AI engine reports failure
                String errorReason = status.getErrorReason() != null
                        ? status.getErrorReason()
                        : "AI引擎报告处理失败";
                log.warn("Template [id={}] - AI engine reports failure: {}", templateId, errorReason);
                markTemplateAsFailed(templateId, errorReason);
                break;

            case AI_STATUS_PROCESSING:
                // Still processing after 10+ minutes — mark as timed out
                log.warn("Template [id={}] - AI engine still reports processing after {} minutes, marking as timed out",
                        templateId, TIMEOUT_MINUTES);
                markTemplateAsFailed(templateId, "AI处理超时：处理时间超过" + TIMEOUT_MINUTES + "分钟");
                break;

            default:
                log.warn("Template [id={}] - AI engine returned unknown status: {}", templateId, status.getStatus());
                markTemplateAsFailed(templateId, "AI处理超时：AI引擎返回未知状态 " + status.getStatus());
                break;
        }
    }

    /**
     * 标记模板为AI处理成功
     *
     * @param templateId 模板ID
     */
    private void markTemplateAsSuccess(Long templateId) {
        LambdaUpdateWrapper<WfTrainingTemplate> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WfTrainingTemplate::getId, templateId)
                     .eq(WfTrainingTemplate::getAiStatus, AI_STATUS_PROCESSING)
                     .set(WfTrainingTemplate::getAiStatus, AI_STATUS_SUCCESS)
                     .set(WfTrainingTemplate::getErrorReason, null)
                     .set(WfTrainingTemplate::getUpdatedAt, LocalDateTime.now());

        int updated = templateMapper.update(null, updateWrapper);
        if (updated > 0) {
            log.info("Template [id={}] marked as AI processing success (ai_status=2) via timeout detection",
                    templateId);
        }
    }

    /**
     * 标记模板为AI处理失败
     *
     * @param templateId 模板ID
     * @param errorReason 错误原因
     */
    private void markTemplateAsFailed(Long templateId, String errorReason) {
        LambdaUpdateWrapper<WfTrainingTemplate> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WfTrainingTemplate::getId, templateId)
                     .eq(WfTrainingTemplate::getAiStatus, AI_STATUS_PROCESSING)
                     .set(WfTrainingTemplate::getAiStatus, AI_STATUS_FAILED)
                     .set(WfTrainingTemplate::getErrorReason, errorReason)
                     .set(WfTrainingTemplate::getUpdatedAt, LocalDateTime.now());

        int updated = templateMapper.update(null, updateWrapper);
        if (updated > 0) {
            log.info("Template [id={}] marked as failed (ai_status=3), reason: {}", templateId, errorReason);
        }
    }
}
