package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.SysFeedback;
import com.smartconstruct.backend_core.mapper.SysFeedbackMapper;
import com.smartconstruct.backend_core.service.IFeedbackService;
import org.springframework.stereotype.Service;

@Service
public class FeedbackServiceImpl extends ServiceImpl<SysFeedbackMapper, SysFeedback> implements IFeedbackService {
}
