package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizAiMessage;
import com.smartconstruct.backend_core.mapper.BizAiMessageMapper;
import com.smartconstruct.backend_core.service.IAiMessageService;
import org.springframework.stereotype.Service;

/**
 * AI 对话消息服务实现
 */
@Service
public class AiMessageServiceImpl extends ServiceImpl<BizAiMessageMapper, BizAiMessage>
        implements IAiMessageService {
}
