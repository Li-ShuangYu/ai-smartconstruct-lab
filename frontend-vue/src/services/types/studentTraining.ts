/**
 * 学生实训运行时类型定义
 *
 * 定义学生端实训流程相关的数据结构，包括节点进度、阶段进度、节点AI状态、
 * 幻灯片内容配置和编码实训配置。
 *
 * @module services/types/studentTraining
 */

// ─── 基础枚举与状态 ────────────────────────────────────────────────────────────

/** 学生节点进度状态枚举：0-未开始 1-进行中 2-已完成 3-已跳过 */
export type NodeProgressStatus = 0 | 1 | 2 | 3

/** 参与状态枚举：0-未开始 1-进行中 2-已提交 */
export type ParticipationStatus = 0 | 1 | 2

/** 幻灯片类型 */
export type SlideType = 'theory' | 'intro' | 'code' | 'quiz' | 'summary'

// ─── 幻灯片与理论实训配置 ──────────────────────────────────────────────────────

/** 测验题目 */
export interface QuizQuestion {
  /** 题目内容 */
  question: string
  /** 选项列表 */
  options: string[]
  /** 正确答案 */
  answer: string
}

/** 幻灯片 */
export interface Slide {
  /** 幻灯片类型 */
  type: SlideType
  /** 标题 */
  title: string
  /** 正文内容 */
  content: string
  /** 要点列表（intro/theory 类型可选） */
  bullet_points?: string[]
  /** 关键知识点（summary 类型可选） */
  key_points?: string[]
  /** 代码示例（code 类型必填） */
  code?: string
  /** 代码运行输出（code 类型必填） */
  output?: string
  /** 测验题目列表（quiz 类型必填） */
  questions?: QuizQuestion[]
}

/** 理论实训节点配置（theory_class 的 config_json 结构） */
export interface TheoryClassConfig {
  slides: Slide[]
}

// ─── 编码实训配置 ──────────────────────────────────────────────────────────────

/** 编码实训测试用例 */
export interface CodingTestCase {
  /** 用例描述 */
  description: string
  /** 输入数据 */
  input: string
  /** 期望输出 */
  expected: string
}

/** 编码实训节点配置（coding_class 的 config_json 结构） */
export interface CodingTaskConfig {
  /** 任务描述 */
  description: string
  /** 代码模板 */
  code_template: string
  /** 提示列表（1-5条） */
  hints: string[]
  /** 测试用例（2-10条） */
  test_cases: CodingTestCase[]
  /** 编程语言 */
  language: string
}

// ─── 节点进度 ─────────────────────────────────────────────────────────────────

/** 学生节点进度 */
export interface StudentNodeProgress {
  id: number
  participation_id: number
  node_instance_id: number
  status: NodeProgressStatus
  entered_at: string | null
  exited_at: string | null
  stay_duration_seconds: number
  is_forced_complete: boolean
  node_specific_data_json: Record<string, unknown> | null
}

/** 节点实例进度（运行时视图，含节点元信息） */
export interface NodeInstanceProgress {
  node_instance_id: number
  node_id: string
  node_type: string
  node_name: string
  phase_id: string
  sort_num: number
  is_required: boolean
  status: NodeProgressStatus
  entered_at: string | null
  exited_at: string | null
  stay_duration_seconds: number
}

/** NodeProgress 别名 — 与后端 NodeProgressDTO 对应 */
export type NodeProgress = NodeInstanceProgress

// ─── 阶段进度 ─────────────────────────────────────────────────────────────────

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

// ─── 实训任务总览 ─────────────────────────────────────────────────────────────

/** 参与信息摘要 */
export interface ParticipationInfo {
  participation_id: number
  status: ParticipationStatus
  total_score: number | null
  started_at: string | null
  updated_at: string | null
}

/** 实训任务总览（学生视角） */
export interface StudentTaskOverview {
  task_id: number
  task_name: string
  template_name: string
  start_time: string
  end_time: string
  is_expired: boolean
  participation: ParticipationInfo
  current_phase_id: string | null
  current_node_instance_id: number | null
  phases: PhaseProgress[]
}

// ─── 当前位置 ─────────────────────────────────────────────────────────────────

/** 学生当前位置（与后端 current-position 接口 DTO 对应） */
export interface CurrentPosition {
  current_node_instance_id: number | null
  phase_id: string | null
  node_type: string | null
  node_name: string | null
}

/**
 * 学生当前位置（Store 内部使用的兼容格式）
 * @deprecated 新代码请使用 CurrentPosition
 */
export interface StudentCurrentPosition {
  phase_id: string | null
  node_instance_id: number | null
  node_type: string | null
  node_name: string | null
}

// ─── 节点AI处理状态 ───────────────────────────────────────────────────────────

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

// ─── API 请求/响应 ────────────────────────────────────────────────────────────

/** 节点进入响应 */
export interface EnterNodeResponse {
  progress: StudentNodeProgress
  node_config: Record<string, unknown>
  node_type: string
  node_name: string
}

/** 节点完成响应 */
export interface CompleteNodeResponse {
  progress: StudentNodeProgress
  phase_complete: boolean
  next_node_instance_id: number | null
  training_complete: boolean
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
