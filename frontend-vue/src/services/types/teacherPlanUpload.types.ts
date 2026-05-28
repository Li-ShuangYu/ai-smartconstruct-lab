/**
 * 教师端方案上传监控（TeacherPlanUpload）相关类型定义
 *
 * 定义教师端方案上传节点监控的 API 契约类型，包括学生提交记录、预览、驳回和批量下载。
 *
 * @module services/types/teacherPlanUpload
 */

/** 学生提交状态 */
export type SubmissionStatus = 'submitted' | 'pending' | 'rejected'

/** 学生方案提交记录 */
export interface StudentSubmissionRecord {
  /** 学生 ID */
  id: number
  /** 学生姓名 */
  name: string
  /** 提交状态 */
  status: SubmissionStatus
  /** 文件名（已提交时有值） */
  fileName: string
  /** 上传时间（已提交时有值） */
  uploadTime: string
  /** 资源 ID（已提交时有值） */
  resourceId: string | null
  /** AI 评审状态 */
  aiReviewStatus: string | null
  /** AI 评审结果 */
  aiReviewResult: Record<string, unknown> | null
}

/** 学生提交列表响应 */
export interface TeacherSubmissionsResponse {
  /** 学生提交记录列表 */
  submissions: StudentSubmissionRecord[]
  /** 总学生数 */
  totalCount: number
}

/** 文件预览 URL 响应 */
export interface FilePreviewResponse {
  /** 预览 URL */
  previewUrl: string
  /** 文件名 */
  fileName: string
}

/** 驳回请求体 */
export interface RejectPlanRequest {
  /** 驳回理由 */
  reason: string
}

/** 驳回响应 */
export interface RejectPlanResponse {
  /** 是否成功 */
  success: boolean
  /** 消息 */
  message: string
}

/** 批量下载响应 */
export interface BatchDownloadResponse {
  /** 下载 URL */
  downloadUrl: string
  /** 文件数量 */
  fileCount: number
}

/** 教师批阅请求体 */
export interface TeacherFeedbackRequest {
  /** 批阅内容 */
  feedback: string
}

/** 教师批阅响应 */
export interface TeacherFeedbackResponse {
  /** 是否成功 */
  success: boolean
  /** 消息 */
  message: string
}
