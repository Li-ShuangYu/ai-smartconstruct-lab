package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizDepartment;
import com.smartconstruct.backend_core.mapper.BizDepartmentMapper;
import com.smartconstruct.backend_core.service.IDepartmentService;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl extends ServiceImpl<BizDepartmentMapper, BizDepartment> implements IDepartmentService {
}
