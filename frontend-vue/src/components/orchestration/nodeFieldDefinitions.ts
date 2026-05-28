/**
 * 节点字段定义注册表
 *
 * 定义每种节点类型的可配置字段、所属维度、表单类型、是否必填等信息。
 * 用于 NodeTypeSettings 组件动态渲染表单。
 */

export interface FieldDefinition {
  /** 字段名 */
  field: string
  /** 显示标签 */
  label: string
  /** 所属6维度 */
  dimension: string
  /** 表单控件类型 */
  type: 'input' | 'textarea' | 'number' | 'switch' | 'select' | 'radio'
  /** 是否必填 */
  required: boolean
  /** 是否属于AI区域（由ai_flags控制） */
  isAiField: boolean
  /** select/radio 选项 */
  options?: Array<{ label: string; value: string | number | boolean }>
  /** 占位文本 */
  placeholder?: string
  /** number类型的最小值 */
  min?: number
  /** number类型的最大值 */
  max?: number
}

/** 节点类型 → 字段定义列表 */
const NODE_FIELD_REGISTRY: Record<string, FieldDefinition[]> = {
  start: [
    { field: 'topic_name', label: '实训主题名称', dimension: 'display', type: 'input', required: true, isAiField: false, placeholder: '请输入实训主题（1-100字符）' },
    { field: 'description', label: '实训简介', dimension: 'display', type: 'textarea', required: true, isAiField: false, placeholder: '请输入实训简介（1-500字符）' },
    { field: 'enable_ai_welcome', label: '启用AI欢迎语', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
  ],
  grouping: [
    { field: 'grouping_method', label: '分组方式', dimension: 'orchestration_settings', type: 'radio', required: true, isAiField: false, options: [
      { label: '随机分组', value: 'random' },
      { label: '手动分组', value: 'manual' },
      { label: 'AI均衡分组', value: 'ai_balanced' }
    ]},
    { field: 'group_size', label: '每组人数', dimension: 'orchestration_settings', type: 'number', required: true, isAiField: false, min: 2, max: 20 },
    { field: 'enable_ai_balanced_grouping', label: '启用AI均衡分组', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
  ],
  resource_read: [
    { field: 'resource_id', label: '关联资源', dimension: 'db_mapping', type: 'select', required: true, isAiField: false, placeholder: '请选择关联资源', options: [] },
    { field: 'min_reading_duration', label: '最低阅读时长(分钟)', dimension: 'orchestration_settings', type: 'number', required: true, isAiField: false, min: 1, max: 60 },
    { field: 'enable_ai_summary', label: '启用AI摘要', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
    { field: 'enable_ai_key_points', label: '启用AI重点提取', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
  ],
  homework: [
    { field: 'source_mode', label: '题目来源', dimension: 'orchestration_settings', type: 'radio', required: true, isAiField: false, options: [
      { label: 'AI自动生成', value: 'ai_generate' },
      { label: '手动出题', value: 'manual' },
      { label: '题库选取', value: 'bank_select' }
    ]},
    { field: 'question_count', label: '题目数量', dimension: 'orchestration_settings', type: 'number', required: true, isAiField: false, min: 1, max: 50 },
    { field: 'enable_ai_grading', label: '启用AI批改', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
  ],
  exam: [
    { field: 'exam_duration', label: '考试时长(分钟)', dimension: 'orchestration_settings', type: 'number', required: true, isAiField: false, min: 10, max: 300 },
    { field: 'total_score', label: '总分值', dimension: 'orchestration_settings', type: 'number', required: true, isAiField: false, min: 10, max: 200 },
    { field: 'source_mode', label: '组卷方式', dimension: 'orchestration_settings', type: 'radio', required: true, isAiField: false, options: [
      { label: 'AI自动生成', value: 'ai_generate' },
      { label: '手动出题', value: 'manual' },
      { label: '题库选取', value: 'bank_select' }
    ]},
    { field: 'question_count', label: '题目数量', dimension: 'orchestration_settings', type: 'number', required: true, isAiField: false, min: 1, max: 100 },
    { field: 'enable_ai_grading', label: '启用AI批改', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
  ],
  end: [
    { field: 'enable_ai_report', label: '启用AI学习报告', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
    { field: 'enable_achievement_badges', label: '启用成就徽章', dimension: 'orchestration_settings', type: 'switch', required: false, isAiField: false },
  ],
  coding_class: [
    { field: 'task_description', label: '编码任务描述', dimension: 'display', type: 'textarea', required: true, isAiField: false, placeholder: '请输入编码任务描述' },
    { field: 'env_type', label: '编码环境类型', dimension: 'orchestration_settings', type: 'input', required: true, isAiField: false, placeholder: '如: k8s, docker' },
    { field: 'enable_ai_code_review', label: '启用AI代码审查', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
    { field: 'ai_guide_level', label: 'AI引导级别', dimension: 'ai_processing', type: 'radio', required: false, isAiField: true, options: [
      { label: '提示', value: 'hint' },
      { label: '审查', value: 'review' },
      { label: '完整审查', value: 'full_review' }
    ]},
  ],
  learning_survey: [
    { field: 'survey_questions', label: '调查问题列表', dimension: 'display', type: 'textarea', required: true, isAiField: false, placeholder: '请配置调查问题（JSON格式）' },
    { field: 'enable_ai_analysis', label: '启用AI学情分析', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
  ],
  task_distribute: [
    { field: 'task_title', label: '任务标题', dimension: 'display', type: 'input', required: true, isAiField: false, placeholder: '请输入任务标题' },
    { field: 'task_description', label: '任务描述', dimension: 'display', type: 'textarea', required: true, isAiField: false, placeholder: '请输入任务描述' },
    { field: 'enable_ai_task_split', label: '启用AI任务拆解', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
    { field: 'split_granularity', label: '拆解粒度', dimension: 'orchestration_settings', type: 'radio', required: false, isAiField: false, options: [
      { label: '按组', value: 'by_group' },
      { label: '按人', value: 'by_person' }
    ]},
  ],
  req_upload: [
    { field: 'scenario', label: '场景描述', dimension: 'display', type: 'textarea', required: true, isAiField: false, placeholder: '请输入场景描述' },
    { field: 'upload_format', label: '上传格式要求', dimension: 'orchestration_settings', type: 'input', required: false, isAiField: false, placeholder: '如: pdf, docx' },
    { field: 'enable_ai_pre_review', label: '启用AI预评审', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
    { field: 'show_word_cloud', label: '展示词云', dimension: 'display', type: 'switch', required: false, isAiField: false },
  ],
  plan_upload: [
    { field: 'plan_requirements', label: '方案要求描述', dimension: 'display', type: 'textarea', required: true, isAiField: false, placeholder: '请输入方案要求' },
    { field: 'upload_format', label: '上传格式', dimension: 'orchestration_settings', type: 'input', required: false, isAiField: false, placeholder: '如: pdf, docx' },
    { field: 'enable_ai_feasibility', label: '启用AI可行性评估', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
  ],
  mindmap_preview: [
    { field: 'topic', label: '预习主题', dimension: 'display', type: 'input', required: true, isAiField: false, placeholder: '请输入预习主题' },
    { field: 'knowledge_points', label: '关联知识点', dimension: 'db_mapping', type: 'textarea', required: true, isAiField: false, placeholder: '请选择关联知识点' },
    { field: 'enable_ai_generate_map', label: '启用AI生成导图', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
  ],
  mindmap_draw: [
    { field: 'topic', label: '绘制主题', dimension: 'display', type: 'input', required: true, isAiField: false, placeholder: '请输入绘制主题' },
    { field: 'reference_points', label: '参考知识点范围', dimension: 'db_mapping', type: 'textarea', required: false, isAiField: false, placeholder: '请选择参考知识点' },
    { field: 'enable_ai_structure_eval', label: '启用AI结构评价', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
  ],
  knowledge_eval: [
    { field: 'knowledge_points', label: '关联知识点列表', dimension: 'db_mapping', type: 'textarea', required: true, isAiField: false, placeholder: '请选择关联知识点' },
    { field: 'enable_ai_analysis', label: '启用AI汇总分析', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
  ],
  student_peer_review: [
    { field: 'review_dimensions', label: '互评维度列表', dimension: 'orchestration_settings', type: 'textarea', required: true, isAiField: false, placeholder: '请配置互评维度' },
    { field: 'reviews_per_student', label: '每人评审份数', dimension: 'orchestration_settings', type: 'number', required: true, isAiField: false, min: 1, max: 10 },
    { field: 'target_type', label: '互评目标类型', dimension: 'orchestration_settings', type: 'radio', required: true, isAiField: false, options: [
      { label: '代码', value: 'code' },
      { label: '文档', value: 'document' },
      { label: '方案', value: 'plan' }
    ]},
    { field: 'enable_ai_anomaly_detection', label: '启用AI异常检测', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
  ],
  inter_group_review: [
    { field: 'review_dimensions', label: '互评维度列表', dimension: 'orchestration_settings', type: 'textarea', required: true, isAiField: false, placeholder: '请配置互评维度' },
    { field: 'assignment_method', label: '评审分配方式', dimension: 'orchestration_settings', type: 'radio', required: true, isAiField: false, options: [
      { label: '轮转', value: 'rotation' },
      { label: '随机', value: 'random' }
    ]},
    { field: 'enable_ai_comprehensive_comment', label: '启用AI综合评语', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
  ],
  teacher_eval: [
    { field: 'weight_config', label: '评价权重配置', dimension: 'evaluation', type: 'textarea', required: true, isAiField: false, placeholder: '请配置各节点得分占比（JSON格式）' },
    { field: 'enable_ai_eval_suggestion', label: '启用AI评价建议', dimension: 'ai_processing', type: 'switch', required: false, isAiField: true },
  ],
}

/**
 * 获取指定节点类型的所有字段定义
 */
export function getFieldsForNodeType(nodeType: string): FieldDefinition[] {
  return NODE_FIELD_REGISTRY[nodeType] ?? []
}

/**
 * 获取指定节点类型的必填字段定义（用于校验）
 */
export function getRequiredFieldsForNodeType(nodeType: string): FieldDefinition[] {
  const fields = NODE_FIELD_REGISTRY[nodeType] ?? []
  return fields.filter(f => f.required)
}

/**
 * 获取指定节点类型的手动设置区字段
 */
export function getManualFields(nodeType: string, aiFlags: string[]): FieldDefinition[] {
  const fields = NODE_FIELD_REGISTRY[nodeType] ?? []
  return fields.filter(f => !f.isAiField && !aiFlags.includes(f.field))
}

/**
 * 获取指定节点类型的AI自动生成区字段
 */
export function getAiFields(nodeType: string, aiFlags: string[]): FieldDefinition[] {
  const fields = NODE_FIELD_REGISTRY[nodeType] ?? []
  return fields.filter(f => f.isAiField || aiFlags.includes(f.field))
}
