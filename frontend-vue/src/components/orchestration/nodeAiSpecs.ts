/**
 * 节点AI规格声明注册表（前端本地副本）
 *
 * 与 wf_node_def.ai_spec_json 保持一致，用于前端编排编辑器
 * 判断哪些字段属于AI自动生成区、哪些属于教师手动设置区。
 */
import type { NodeAiSpec } from '@/services/types/orchestration'

/** 节点类型 → AI规格映射 */
const NODE_AI_SPEC_REGISTRY: Record<string, NodeAiSpec> = {
  start: {
    input_fields: [
      { name: 'topic_name', type: 'string' },
      { name: 'description', type: 'string' }
    ],
    output_fields: [
      { name: 'welcome_text', type: 'string' }
    ],
    target_agent: 'TEXT',
    priority: 5,
    ai_flags: ['enable_ai_welcome']
  },
  resource_read: {
    input_fields: [
      { name: 'resource_id', type: 'bigint' },
      { name: 'topic_name', type: 'string' }
    ],
    output_fields: [
      { name: 'summary_text', type: 'string' },
      { name: 'key_points', type: 'array' }
    ],
    target_agent: 'TEXT',
    priority: 3,
    ai_flags: ['enable_ai_summary', 'enable_ai_key_points']
  },
  coding_class: {
    input_fields: [
      { name: 'task_description', type: 'string' },
      { name: 'env_type', type: 'string' }
    ],
    output_fields: [
      { name: 'code_review', type: 'string' }
    ],
    target_agent: 'CODE',
    priority: 4,
    ai_flags: ['enable_ai_code_review']
  },
  learning_survey: {
    input_fields: [
      { name: 'survey_questions', type: 'array' }
    ],
    output_fields: [
      { name: 'analysis_report', type: 'string' }
    ],
    target_agent: 'TEXT',
    priority: 6,
    ai_flags: ['enable_ai_analysis']
  },
  task_distribute: {
    input_fields: [
      { name: 'task_title', type: 'string' },
      { name: 'task_description', type: 'string' }
    ],
    output_fields: [
      { name: 'sub_tasks', type: 'array' }
    ],
    target_agent: 'STRUCT',
    priority: 4,
    ai_flags: ['enable_ai_task_split']
  },
  req_upload: {
    input_fields: [
      { name: 'scenario', type: 'string' }
    ],
    output_fields: [
      { name: 'review_result', type: 'string' }
    ],
    target_agent: 'TEXT',
    priority: 4,
    ai_flags: ['enable_ai_pre_review']
  },
  plan_upload: {
    input_fields: [
      { name: 'plan_requirements', type: 'string' }
    ],
    output_fields: [
      { name: 'feasibility_report', type: 'string' }
    ],
    target_agent: 'CODE',
    priority: 4,
    ai_flags: ['enable_ai_feasibility']
  },
  homework: {
    input_fields: [
      { name: 'knowledge_points', type: 'array' },
      { name: 'difficulty_distribution', type: 'object' }
    ],
    output_fields: [
      { name: 'generated_questions', type: 'array' }
    ],
    target_agent: 'EXAM',
    priority: 2,
    ai_flags: ['enable_ai_grading']
  },
  exam: {
    input_fields: [
      { name: 'knowledge_points', type: 'array' },
      { name: 'difficulty_distribution', type: 'object' }
    ],
    output_fields: [
      { name: 'generated_paper', type: 'object' }
    ],
    target_agent: 'EXAM',
    priority: 1,
    ai_flags: ['enable_ai_grading']
  },
  mindmap_preview: {
    input_fields: [
      { name: 'topic', type: 'string' },
      { name: 'knowledge_points', type: 'array' }
    ],
    output_fields: [
      { name: 'mindmap_json', type: 'object' }
    ],
    target_agent: 'STRUCT',
    priority: 5,
    ai_flags: ['enable_ai_generate_map']
  },
  mindmap_draw: {
    input_fields: [
      { name: 'topic', type: 'string' },
      { name: 'reference_points', type: 'array' }
    ],
    output_fields: [
      { name: 'structure_eval', type: 'object' }
    ],
    target_agent: 'STRUCT',
    priority: 6,
    ai_flags: ['enable_ai_structure_eval']
  },
  knowledge_eval: {
    input_fields: [
      { name: 'knowledge_points', type: 'array' }
    ],
    output_fields: [
      { name: 'difficulty_report', type: 'string' }
    ],
    target_agent: 'TEXT',
    priority: 7,
    ai_flags: ['enable_ai_analysis']
  },
  student_peer_review: {
    input_fields: [
      { name: 'review_dimensions', type: 'array' }
    ],
    output_fields: [
      { name: 'anomaly_flags', type: 'array' }
    ],
    target_agent: 'TEXT',
    priority: 6,
    ai_flags: ['enable_ai_anomaly_detection']
  },
  inter_group_review: {
    input_fields: [
      { name: 'review_dimensions', type: 'array' }
    ],
    output_fields: [
      { name: 'comprehensive_comment', type: 'string' }
    ],
    target_agent: 'TEXT',
    priority: 6,
    ai_flags: ['enable_ai_comprehensive_comment']
  },
  teacher_eval: {
    input_fields: [
      { name: 'weight_config', type: 'object' }
    ],
    output_fields: [
      { name: 'eval_suggestion', type: 'string' }
    ],
    target_agent: 'TEXT',
    priority: 3,
    ai_flags: ['enable_ai_eval_suggestion']
  },
  end: {
    input_fields: [
      { name: 'student_data', type: 'object' }
    ],
    output_fields: [
      { name: 'learning_report', type: 'string' },
      { name: 'radar_chart_data', type: 'object' }
    ],
    target_agent: 'TEXT',
    priority: 8,
    ai_flags: ['enable_ai_report']
  },
  grouping: {
    input_fields: [
      { name: 'student_scores', type: 'array' }
    ],
    output_fields: [
      { name: 'balanced_groups', type: 'array' }
    ],
    target_agent: 'STRUCT',
    priority: 5,
    ai_flags: ['enable_ai_balanced_grouping']
  }
}

/**
 * 获取指定节点类型的AI规格声明
 */
export function getAiSpecForNodeType(nodeType: string): NodeAiSpec | null {
  return NODE_AI_SPEC_REGISTRY[nodeType] ?? null
}

/**
 * 获取节点AI处理摘要信息（用于hover tooltip）
 * Step 6: 返回需AI处理的字段数量和预估处理时间
 */
export function getAiProcessingSummary(nodeType: string): { fieldCount: number; estimatedSeconds: number } | null {
  const spec = NODE_AI_SPEC_REGISTRY[nodeType]
  if (!spec) return null

  const fieldCount = spec.output_fields.length
  // Estimated time: base 5s per field, adjusted by priority (lower priority = faster)
  const estimatedSeconds = fieldCount * 5

  return { fieldCount, estimatedSeconds }
}
