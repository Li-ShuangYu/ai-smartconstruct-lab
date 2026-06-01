package com.smartconstruct.backend_core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartconstruct.backend_core.entity.BizAiMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 对话消息 Mapper
 */
@Mapper
public interface BizAiMessageMapper extends BaseMapper<BizAiMessage> {
}
