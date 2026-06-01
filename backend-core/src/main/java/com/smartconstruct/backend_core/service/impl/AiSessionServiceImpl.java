package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizAiSession;
import com.smartconstruct.backend_core.mapper.BizAiSessionMapper;
import com.smartconstruct.backend_core.service.IAiSessionService;
import org.springframework.stereotype.Service;

/**
 * AI 对话会话服务实现
 */
@Service
public class AiSessionServiceImpl extends ServiceImpl<BizAiSessionMapper, BizAiSession>
        implements IAiSessionService {
}
