package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizMajor;
import com.smartconstruct.backend_core.mapper.BizMajorMapper;
import com.smartconstruct.backend_core.service.IMajorService;
import org.springframework.stereotype.Service;

@Service
public class MajorServiceImpl extends ServiceImpl<BizMajorMapper, BizMajor> implements IMajorService {
}
