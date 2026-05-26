package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI生成的知识点DTO
 *
 * 对应 biz_knowledge_point 表结构，用于AI回调时传递生成的知识点数据。
 * 支持通过 parentId 构建层级关系。
 *
 * @author SmartConstruct
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePointDTO {
    /** 知识点名称 */
    private String name;
    /** 父知识点ID（用于层级关系） */
    private Long parentId;
    /** 排序序号 */
    private Integer sortOrder;
    /** 描述（可选） */
    private String description;
}
