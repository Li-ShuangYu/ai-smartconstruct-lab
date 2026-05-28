/**
 * 方案上传（PlanUpload）相关类型定义
 *
 * 定义学生端方案上传节点的 API 契约类型，包括 AI 可行性分析、文件上传和方案提交。
 *
 * @module services/types/planUpload
 */

/** AI 可行性分析维度评分 */
export interface FeasibilityDimension {
  /** 维度名称 */
  name: string
  /** 维度评分 (0-100) */
  score: number
  /** AI 反馈文字 */
  feedback: string
}

/** AI 可行性分析结果 */
export interface AiFeasibilityAnalysis {
  /** 分析维度列表 */
  dimensions: FeasibilityDimension[]
  /** 分析状态: 0=未分析, 1=分析中, 2=已完成 */
  status: 0 | 1 | 2
  /** 分析完成时间 */
  analyzed_at: string | null
}

/** 节点 AI 分析与历史提交状态响应 */
export interface PlanUploadNodeState {
  /** AI 可行性分析结果（null 表示尚无分析） */
  ai_analysis: AiFeasibilityAnalysis | null
  /** 历史提交记录（null 表示尚未提交） */
  last_submission: PlanSubmissionRecord | null
}

/** 方案提交记录 */
export interface PlanSubmissionRecord {
  /** 上传记录 ID */
  upload_id: string
  /** 资源文件 ID */
  resource_id: string
  /** 文件名 */
  file_name: string
  /** 提交时间 */
  submitted_at: string
  /** AI 评审状态: pending | reviewing | completed */
  ai_review_status: string
  /** AI 评审结果（评审完成后有值） */
  ai_review_result: Record<string, unknown> | null
}

/** 文件上传响应 */
export interface FileUploadResponse {
  /** 资源 ID */
  id: string
  /** 文件名 */
  fileName: string
  /** 文件大小（格式化字符串） */
  fileSize: string
  /** 资源类型 */
  resourceType: number
}

/** 方案提交请求体 */
export interface PlanSubmitRequest {
  /** 文件资源 ID */
  resource_id: number
  /** 文件名 */
  file_name: string
}

/** 方案提交响应 */
export interface PlanSubmitResponse {
  /** 上传记录 ID */
  uploadId: string
  /** 提交状态 */
  status: string
  /** AI 评审状态 */
  aiReviewStatus: string
}
