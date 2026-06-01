package com.smartconstruct.backend_core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartconstruct.backend_core.entity.BizAiSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 对话会话 Mapper
 */
@Mapper
public interface BizAiSessionMapper extends BaseMapper<BizAiSession> {
}
