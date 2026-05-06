package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizStudentCourse;
import com.smartconstruct.backend_core.mapper.BizStudentCourseMapper;
import com.smartconstruct.backend_core.service.IStudentCourseService;
import org.springframework.stereotype.Service;

@Service
public class StudentCourseServiceImpl extends ServiceImpl<BizStudentCourseMapper, BizStudentCourse> implements IStudentCourseService {
}
