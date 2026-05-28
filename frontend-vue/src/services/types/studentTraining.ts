/**
 * 学生实训运行时类型定义
 *
 * 定义学生端实训流程相关的数据结构，包括节点进度、阶段进度和节点AI状态。
 *
 * @module services/types/studentTraining
 */

/** 学生节点进度状态枚举 */
export type NodeProgressStatus = 0 | 1 | 2 | 3

/** 学生节点进度 */
export interface StudentNodeProgress {
  id: number
  participation_id: number
  node_instance_id: number
  status: NodeProgressStatus
  entered_at: string | null
  exited_at: string | null
  stay_duration_seconds: number
  node_specific_data_json: Record<string, unknown> | null
}

/** 阶段进度 */
export interface PhaseProgress {
  phase_id: string
  phase_name: string
  sort_num: number
  is_unlocked: boolean
  is_complete: boolean
  total_nodes: number
  completed_nodes: number
  required_nodes: number
  completed_required_nodes: number
  nodes: NodeInstanceProgress[]
}

/** 节点实例进度（运行时视图） */
export interface NodeInstanceProgress {
  node_instance_id: number
  node_id: string
  node_type: string
  node_name: string
  phase_id: string
  is_required: boolean
  status: NodeProgressStatus
  entered_at: string | null
  exited_at: string | null
  stay_duration_seconds: number
}

/** 节点AI处理状态 */
export interface NodeAiStatus {
  id: number
  template_id: number
  node_id: string
  node_type: string
  phase_id: string
  ai_status: 0 | 1 | 2 | 3
  error_reason: string | null
  retry_count: number
  result_json: Record<string, unknown> | null
  last_processed_at: string | null
}

/** 实训任务总览（学生视角） */
export interface StudentTaskOverview {
  task_id: number
  task_name: string
  template_name: string
  start_time: string
  end_time: string
  is_expired: boolean
  current_phase_id: string | null
  current_node_instance_id: number | null
  phases: PhaseProgress[]
}

/** 学生当前位置 */
export interface StudentCurrentPosition {
  phase_id: string | null
  node_instance_id: number | null
  node_type: string | null
  node_name: string | null
}

/** 节点进入响应 */
export interface EnterNodeResponse {
  progress: StudentNodeProgress
  node_config: Record<string, unknown>
  ai_content: Record<string, unknown> | null
}

/** 节点提交请求 — 按node_type路由，body内容因节点类型而异 */
export interface SubmitNodeRequest {
  /** resource_read: reading_duration_seconds, scroll_percentage, knowledge_point_clicks */
  /** coding_class: code_content, language */
  /** homework/exam: answers (array), student_paper_id */
  /** req_upload/plan_upload: resource_id, file_name */
  /** mindmap_draw: map_topology_json */
  /** student_peer_review/inter_group_review: evaluations (array) */
  [key: string]: unknown
}

/** 节点提交响应 — 通用结构，具体字段因节点类型而异 */
export interface SubmitNodeResponse {
  status?: string
  uploadId?: string
  aiReviewStatus?: string
  aiEvalStatus?: string
  savedCount?: number
  aiGradingTriggered?: boolean
  nodeInstanceId?: string
  [key: string]: unknown
}

/** 教师监控 — 节点学生分布 */
export interface NodeStudentDistribution {
  node_instance_id: number
  node_id: string
  node_name: string
  node_type: string
  not_started: number
  in_progress: number
  completed: number
}

/** 教师监控 — 阶段监控数据 */
export interface PhaseMonitorData {
  phase_id: string
  phase_name: string
  sort_num: number
  completion_rate: number
  node_distributions: NodeStudentDistribution[]
}

/** 教师监控 — 任务监控总览 */
export interface TaskMonitorOverview {
  task_id: number
  task_name: string
  total_students: number
  active_students: number
  phases: PhaseMonitorData[]
}

/** 教师监控 — 学生进度详情 */
export interface StudentProgressDetail {
  student_id: number
  student_name: string
  student_no: string
  current_phase_id: string | null
  current_node_name: string | null
  overall_progress: number
  last_active_at: string | null
}
