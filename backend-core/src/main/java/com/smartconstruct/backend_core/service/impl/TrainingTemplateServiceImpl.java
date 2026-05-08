package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.ITrainingTemplateService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 实训模板服务实现类
 * 
 * 继承自MyBatis-Plus的ServiceImpl，提供模板的基础CRUD操作。
 * 额外提供模板AI处理的模拟实现。
 * 
 * @author SmartConstruct
 * @version 1.0.0
 */
@Service
public class TrainingTemplateServiceImpl extends ServiceImpl<WfTrainingTemplateMapper, WfTrainingTemplate> implements ITrainingTemplateService {

    /**
     * 模拟AI处理模板
     * 
     * 异步执行模板的AI处理流程（模拟），将原始画布数据转换为标准执行载荷。
     * 实际应调用Python FastAPI AI引擎进行处理，当前仅为模拟实现。
     * 
     * 使用 @Async("templateAiExecutor") 注解指定使用专用线程池执行。
     * 
     * @param templateId 模板ID
     * @param canvasData 原始画布JSON数据
     */
    @Async("templateAiExecutor")
    @Override
    public void processTemplateMockAi(Long templateId, Object canvasData) {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        WfTrainingTemplate template = getById(templateId);
        if (template != null) {
            template.setStandardPayloadJson(canvasData);
            template.setAiStatus(2);
            updateById(template);
        }
    }
}
