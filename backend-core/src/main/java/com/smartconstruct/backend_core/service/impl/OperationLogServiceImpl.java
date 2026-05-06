package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.SysOperationLog;
import com.smartconstruct.backend_core.mapper.SysOperationLogMapper;
import com.smartconstruct.backend_core.service.IOperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements IOperationLogService {
}
