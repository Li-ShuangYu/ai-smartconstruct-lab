package com.smartconstruct.backend_core.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizTrainingChatLog;
import com.smartconstruct.backend_core.mapper.BizTrainingChatLogMapper;
import com.smartconstruct.backend_core.service.ITrainingChatLogService;
import org.springframework.stereotype.Service;
@Service
public class TrainingChatLogServiceImpl extends ServiceImpl<BizTrainingChatLogMapper, BizTrainingChatLog> implements ITrainingChatLogService {}
