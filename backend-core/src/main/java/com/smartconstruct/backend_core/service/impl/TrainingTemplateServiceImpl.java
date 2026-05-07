package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.ITrainingTemplateService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TrainingTemplateServiceImpl extends ServiceImpl<WfTrainingTemplateMapper, WfTrainingTemplate> implements ITrainingTemplateService {

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
