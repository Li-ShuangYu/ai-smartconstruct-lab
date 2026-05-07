package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizTrainingParticipation;
import com.smartconstruct.backend_core.mapper.BizTrainingParticipationMapper;
import com.smartconstruct.backend_core.service.ITrainingParticipationService;
import org.springframework.stereotype.Service;

@Service
public class TrainingParticipationServiceImpl extends ServiceImpl<BizTrainingParticipationMapper, BizTrainingParticipation> implements ITrainingParticipationService {
}
