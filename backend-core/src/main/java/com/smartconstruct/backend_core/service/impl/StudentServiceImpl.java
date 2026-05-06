package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizStudent;
import com.smartconstruct.backend_core.mapper.BizStudentMapper;
import com.smartconstruct.backend_core.service.IStudentService;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl extends ServiceImpl<BizStudentMapper, BizStudent> implements IStudentService {
}
