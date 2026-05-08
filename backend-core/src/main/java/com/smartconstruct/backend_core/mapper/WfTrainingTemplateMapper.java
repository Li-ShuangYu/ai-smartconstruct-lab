package com.smartconstruct.backend_core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartconstruct.backend_core.entity.WfTrainingTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实训模板 Mapper 接口
 *
 * 继承 MyBatis-Plus BaseMapper，提供 CRUD 基础操作
 *
 * @author SmartConstruct
 * @version 1.0.0
 */
@Mapper
public interface WfTrainingTemplateMapper extends BaseMapper<WfTrainingTemplate> {
}
