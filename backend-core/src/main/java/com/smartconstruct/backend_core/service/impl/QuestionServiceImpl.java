package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizQuestion;
import com.smartconstruct.backend_core.mapper.BizQuestionMapper;
import com.smartconstruct.backend_core.service.IQuestionService;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl extends ServiceImpl<BizQuestionMapper, BizQuestion> implements IQuestionService {
}
