package com.smartconstruct.backend_core.service;

import com.smartconstruct.backend_core.dto.ValidationResult;

/**
 * 节点配置验证服务接口
 *
 * 负责验证节点配置JSON是否符合6维度结构规范和对应节点类型的JSON Schema定义，
 * 以及验证阶段（Phase）配置的合法性。
 *
 * @author SmartConstruct
 */
public interface INodeValidationService {

    /**
     * 验证节点配置是否符合6维度结构和对应的JSON Schema
     *
     * 验证流程：
     * 1. 查找该节点类型在wf_node_config_schema表中注册的JSON Schema
     * 2. 验证config_json是否包含且仅包含6个必需的顶层维度键
     *    (display, db_mapping, ai_processing, orchestration_settings, data_collection, evaluation)
     * 3. 按JSON Schema验证各维度内部字段的合法性
     *
     * @param nodeType   节点类型标识（对应 wf_node_def.node_type）
     * @param configJson 节点配置JSON字符串（6维度结构）
     * @return 验证结果，包含是否通过、错误码和字段级错误详情
     */
    ValidationResult validateNodeConfig(String nodeType, String configJson);

    /**
     * 验证阶段配置的合法性
     *
     * 验证规则：
     * - 阶段数组长度必须在1到10之间（含边界）
     * - 每个阶段名称长度必须在1到20个字符之间
     * - 阶段sort_num必须唯一（不允许重复排序号）
     *
     * @param phasesJson 阶段定义JSON字符串（JSON数组格式）
     * @return 验证结果
     */
    ValidationResult validatePhases(String phasesJson);
}
