package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizTrainingTask;
import com.smartconstruct.backend_core.mapper.BizTrainingTaskMapper;
import com.smartconstruct.backend_core.service.ITrainingTaskService;
import org.springframework.stereotype.Service;

@Service
public class TrainingTaskServiceImpl extends ServiceImpl<BizTrainingTaskMapper, BizTrainingTask> implements ITrainingTaskService {
}
