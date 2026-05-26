package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.IAiEngineClient;
import com.smartconstruct.backend_core.service.ITrainingTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 实训模板服务实现类
 *
 * 继承自MyBatis-Plus的ServiceImpl，提供模板的基础CRUD操作。
 * 提供模板AI处理的异步调度，将画布数据提交给AI引擎进行处理。
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Service
public class TrainingTemplateServiceImpl extends ServiceImpl<WfTrainingTemplateMapper, WfTrainingTemplate> implements ITrainingTemplateService {

    private static final Logger log = LoggerFactory.getLogger(TrainingTemplateServiceImpl.class);

    @Autowired
    private IAiEngineClient aiEngineClient;

    /**
     * 异步提交模板给AI引擎处理
     *
     * 通过专用线程池异步调用AI引擎客户端，将模板画布数据提交给Python AI引擎进行处理。
     * AI引擎处理完成后通过回调接口通知后端更新状态。
     *
     * 使用 @Async("templateAiExecutor") 注解指定使用专用线程池执行，
     * 确保不阻塞HTTP响应返回给前端。
     *
     * @param templateId 模板ID
     * @param canvasData 原始画布JSON数据
     */
    @Async("templateAiExecutor")
    @Override
    public void processTemplateMockAi(Long templateId, Object canvasData) {
        log.info("Dispatching template {} to AI engine for processing", templateId);
        try {
            String jobId = aiEngineClient.submitForProcessing(templateId, canvasData);
            log.info("Template {} submitted to AI engine successfully, jobId={}", templateId, jobId);
        } catch (Exception e) {
            // AiEngineClientImpl already handles marking the template as failed (ai_status=3)
            log.error("Failed to submit template {} to AI engine: {}", templateId, e.getMessage(), e);
        }
    }
}
