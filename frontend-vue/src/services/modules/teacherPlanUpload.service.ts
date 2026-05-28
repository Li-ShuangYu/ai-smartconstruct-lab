/**
 * 教师端方案上传监控（TeacherPlanUpload）API 服务
 *
 * 提供教师端方案上传节点监控的 API 调用，包括获取学生提交列表、文件预览、驳回和批量下载。
 *
 * @module services/modules/teacherPlanUpload
 */
import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type {
  TeacherSubmissionsResponse,
  FilePreviewResponse,
  RejectPlanRequest,
  RejectPlanResponse,
  BatchDownloadResponse,
  TeacherFeedbackRequest,
  TeacherFeedbackResponse
} from '@/services/types/teacherPlanUpload.types'

/**
 * 获取学生方案提交记录列表
 *
 * @param nodeInstanceId 节点实例 ID
 * @returns 学生提交记录列表
 */
export const getStudentSubmissions = (nodeInstanceId: number) =>
  http.get<ApiResult<TeacherSubmissionsResponse>>(
    `/api/teacher/training/${nodeInstanceId}/submissions`
  ).then(r => r.data)

/**
 * 获取文件预览 URL
 *
 * @param nodeInstanceId 节点实例 ID
 * @param studentId 学生 ID
 * @returns 文件预览 URL
 */
export const getFilePreviewUrl = (nodeInstanceId: number, studentId: number) =>
  http.get<ApiResult<FilePreviewResponse>>(
    `/api/teacher/training/${nodeInstanceId}/submissions/${studentId}/preview`
  ).then(r => r.data)

/**
 * 驳回学生方案
 *
 * @param nodeInstanceId 节点实例 ID
 * @param studentId 学生 ID
 * @param data 驳回请求体（包含理由）
 * @returns 驳回结果
 */
export const rejectStudentPlan = (nodeInstanceId: number, studentId: number, data: RejectPlanRequest) =>
  http.post<ApiResult<RejectPlanResponse>>(
    `/api/teacher/training/${nodeInstanceId}/submissions/${studentId}/reject`,
    data
  ).then(r => r.data)

/**
 * 批量下载已提交的方案文件
 *
 * @param nodeInstanceId 节点实例 ID
 * @returns 批量下载 URL
 */
export const batchDownloadPlans = (nodeInstanceId: number) =>
  http.get<ApiResult<BatchDownloadResponse>>(
    `/api/teacher/training/${nodeInstanceId}/submissions/batch-download`
  ).then(r => r.data)

/**
 * 提交教师批阅意见
 *
 * @param nodeInstanceId 节点实例 ID
 * @param studentId 学生 ID
 * @param data 批阅请求体
 * @returns 批阅结果
 */
export const submitTeacherFeedback = (nodeInstanceId: number, studentId: number, data: TeacherFeedbackRequest) =>
  http.post<ApiResult<TeacherFeedbackResponse>>(
    `/api/teacher/training/${nodeInstanceId}/submissions/${studentId}/feedback`,
    data
  ).then(r => r.data)
