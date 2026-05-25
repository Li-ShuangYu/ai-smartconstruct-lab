package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.SysResource;
import com.smartconstruct.backend_core.mapper.SysResourceMapper;
import com.smartconstruct.backend_core.service.ISysResourceService;
import org.springframework.stereotype.Service;

@Service
public class SysResourceServiceImpl extends ServiceImpl<SysResourceMapper, SysResource> implements ISysResourceService {
}