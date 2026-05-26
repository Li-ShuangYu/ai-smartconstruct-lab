package com.smartconstruct.backend_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI生成的资产DTO
 *
 * 包含AI引擎生成的题目、知识点、评价指标等资产数据。
 * 作为 {@link AiCallbackRequest} 的嵌套对象，在回调时传递给后端进行持久化。
 *
 * @author SmartConstruct
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedAssets {
    /** AI生成的题目列表 */
    private List<QuestionDTO> questions;
    /** AI生成的知识点列表 */
    private List<KnowledgePointDTO> knowledgePoints;
    /** AI生成的评价指标列表 */
    private List<EvalIndicatorDTO> evalIndicators;
}
