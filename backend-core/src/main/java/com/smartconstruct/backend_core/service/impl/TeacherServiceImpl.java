package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizTeacher;
import com.smartconstruct.backend_core.mapper.BizTeacherMapper;
import com.smartconstruct.backend_core.service.ITeacherService;
import org.springframework.stereotype.Service;

@Service
public class TeacherServiceImpl extends ServiceImpl<BizTeacherMapper, BizTeacher> implements ITeacherService {
}
