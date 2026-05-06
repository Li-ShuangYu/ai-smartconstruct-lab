package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizAdminClass;
import com.smartconstruct.backend_core.mapper.BizAdminClassMapper;
import com.smartconstruct.backend_core.service.IAdminClassService;
import org.springframework.stereotype.Service;

@Service
public class AdminClassServiceImpl extends ServiceImpl<BizAdminClassMapper, BizAdminClass> implements IAdminClassService {
}
