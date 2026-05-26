package com.smartconstruct.backend_core.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
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
 * 每分钟检查 ai_status=1（处理中）且 updated_at 超过10分钟的模板，
 * 将其标记为 ai_status=3（失败），并设置 error_reason="AI处理超时"。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Component
public class AiTimeoutDetectionTask {

    private static final Logger log = LoggerFactory.getLogger(AiTimeoutDetectionTask.class);

    /** AI处理中状态 */
    private static final int AI_STATUS_PROCESSING = 1;

    /** AI处理失败状态 */
    private static final int AI_STATUS_FAILED = 3;

    /** 超时阈值（分钟） */
    private static final int TIMEOUT_MINUTES = 10;

    @Autowired
    private WfTrainingTemplateMapper templateMapper;

    /**
     * 每60秒执行一次，检测超时的AI处理任务
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

        log.info("Detected {} stale AI processing template(s) exceeding {} minute timeout", 
                staleTemplates.size(), TIMEOUT_MINUTES);

        // Update each stale template to failed status with timeout error reason
        for (WfTrainingTemplate template : staleTemplates) {
            LambdaUpdateWrapper<WfTrainingTemplate> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(WfTrainingTemplate::getId, template.getId())
                         .eq(WfTrainingTemplate::getAiStatus, AI_STATUS_PROCESSING)
                         .set(WfTrainingTemplate::getAiStatus, AI_STATUS_FAILED)
                         .set(WfTrainingTemplate::getErrorReason, "AI处理超时")
                         .set(WfTrainingTemplate::getUpdatedAt, LocalDateTime.now());

            int updated = templateMapper.update(null, updateWrapper);
            if (updated > 0) {
                log.info("Marked template [id={}] as timed out (ai_status=3)", template.getId());
            }
        }

        log.info("Timeout detection complete: updated {} template(s)", staleTemplates.size());
    }
}
