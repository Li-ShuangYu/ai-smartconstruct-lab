package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import com.smartconstruct.backend_core.mapper.WfTrainingTemplateMapper;
import com.smartconstruct.backend_core.service.ITrainingTemplateService;
import org.springframework.stereotype.Service;

@Service
public class TrainingTemplateServiceImpl extends ServiceImpl<WfTrainingTemplateMapper, WfTrainingTemplate> implements ITrainingTemplateService {
}
