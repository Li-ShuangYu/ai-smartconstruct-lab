/**
 * 编排系统类型定义
 *
 * 定义实训编排编辑器相关的核心数据结构，包括阶段、节点、6维度配置、画布JSON和AI规格声明。
 *
 * @module services/types/orchestration
 */

/** 编排边（节点间连线） */
export interface OrchestrationEdge {
  source: string
  target: string
  id?: string
}

/** 阶段定义 */
export interface Phase {
  phase_id: string
  phase_name: string
  sort_num: number
  plan_start_time?: string
  plan_end_time?: string
  nodes: OrchestrationNode[]
  edges: OrchestrationEdge[]
}

/** 编排节点 */
export interface OrchestrationNode {
  node_id: string
  node_type: string
  node_name: string
  config: NodeConfig6D
  position: { x: number; y: number }
  is_required: boolean
  ai_overrides: Record<string, boolean>
}

/** 节点6维度配置结构 */
export interface NodeConfig6D {
  display: Record<string, unknown>
  db_mapping: Record<string, unknown>
  ai_processing: Record<string, unknown>
  orchestration_settings: Record<string, unknown>
  data_collection: Record<string, unknown>
  evaluation: Record<string, unknown>
}

/** 画布JSON顶层结构 */
export interface CanvasJson {
  orchestration_id: string
  phases: Phase[]
}

/** 节点AI规格声明 */
export interface NodeAiSpec {
  input_fields: Array<{ name: string; type: string }>
  output_fields: Array<{ name: string; type: string }>
  target_agent: 'TEXT' | 'STRUCT' | 'EXAM' | 'CODE' | 'VIDEO'
  priority: number
  ai_flags: string[]
}

/** 节点类型定义（来自 wf_node_def） */
export interface NodeTypeDef {
  id: number
  node_type: string
  node_name: string
  category: string
  is_active: boolean
  ai_spec_json: NodeAiSpec | null
  default_config_json: NodeConfig6D | null
}

/** 模板发布请求 */
export interface PublishTemplateRequest {
  canvasData: CanvasJson
}

/** 模板发布响应 */
export interface PublishTemplateResponse {
  ai_status: number
  job_id: string
}

/** 重试请求 */
export interface RetryTemplateRequest {
  node_ids?: string[]
}

/** 重试响应 */
export interface RetryTemplateResponse {
  ai_status: number
  retry_count: number
}

/** 模板预览数据 */
export interface TemplatePreviewData {
  template_id: string
  template_name: string
  ai_status: number
  phases: PhasePreview[]
}

/** 阶段预览 */
export interface PhasePreview {
  phase_id: string
  phase_name: string
  sort_num: number
  nodes: NodePreview[]
}

/** 节点预览 */
export interface NodePreview {
  node_id: string
  node_type: string
  node_name: string
  ai_status: number
  config_summary: Record<string, unknown>
  ai_generated_content: Record<string, unknown> | null
}
