package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizQuestionBank;
import com.smartconstruct.backend_core.mapper.BizQuestionBankMapper;
import com.smartconstruct.backend_core.service.IQuestionBankService;
import org.springframework.stereotype.Service;

@Service
public class QuestionBankServiceImpl extends ServiceImpl<BizQuestionBankMapper, BizQuestionBank> implements IQuestionBankService {
}
