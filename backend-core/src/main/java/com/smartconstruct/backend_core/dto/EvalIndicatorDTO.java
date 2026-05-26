package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI生成的评价指标DTO
 *
 * 用于AI回调时传递生成的评价指标/评分标准数据。
 * 包含指标名称、描述、权重和评分标准。
 *
 * @author SmartConstruct
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvalIndicatorDTO {
    /** 指标名称 */
    private String name;
    /** 指标描述 */
    private String description;
    /** 权重 */
    private Double weight;
    /** 评分标准 */
    private String scoringCriteria;
}
