package com.smartconstruct.backend_core.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizTrainingExamRecord;
import com.smartconstruct.backend_core.mapper.BizTrainingExamRecordMapper;
import com.smartconstruct.backend_core.service.ITrainingExamRecordService;
import org.springframework.stereotype.Service;
@Service
public class TrainingExamRecordServiceImpl extends ServiceImpl<BizTrainingExamRecordMapper, BizTrainingExamRecord> implements ITrainingExamRecordService {}
