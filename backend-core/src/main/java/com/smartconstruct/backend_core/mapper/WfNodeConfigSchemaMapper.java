package com.smartconstruct.backend_core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartconstruct.backend_core.entity.WfNodeConfigSchema;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WfNodeConfigSchemaMapper extends BaseMapper<WfNodeConfigSchema> {

    @Select("SELECT id, node_type, schema_json, schema_version, created_at, updated_at FROM wf_node_config_schema WHERE node_type = #{nodeType}")
    WfNodeConfigSchema selectByNodeType(@Param("nodeType") String nodeType);
}
