package com.smartconstruct.backend_core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartconstruct.backend_core.entity.BizCourse;
import com.smartconstruct.backend_core.mapper.BizCourseMapper;
import com.smartconstruct.backend_core.service.ICourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends ServiceImpl<BizCourseMapper, BizCourse> implements ICourseService {
}
