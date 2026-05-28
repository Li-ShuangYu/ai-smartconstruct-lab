/**
 * 方案上传（PlanUpload）API 服务
 *
 * 提供学生端方案上传节点的 API 调用，包括获取 AI 可行性分析、文件上传和方案提交。
 *
 * @module services/modules/planUpload
 */
import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type {
  PlanUploadNodeState,
  FileUploadResponse,
  PlanSubmitResponse
} from '@/services/types/planUpload.types'

/**
 * 获取方案上传节点的 AI 分析结果和历史提交状态
 *
 * @param nodeInstanceId 节点实例 ID
 * @returns AI 可行性分析结果和历史提交记录
 */
export const getPlanUploadNodeState = (nodeInstanceId: number) =>
  http.get<ApiResult<PlanUploadNodeState>>(
    `/api/student/nodes/${nodeInstanceId}/content`
  ).then(r => r.data)

/**
 * 上传文件到服务器
 *
 * @param file 要上传的文件
 * @returns 文件资源信息（包含 resource_id）
 */
export const uploadFile = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<ApiResult<FileUploadResponse>>(
    '/api/file/upload',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  ).then(r => r.data)
}

/**
 * 提交方案（文件上传后调用）
 *
 * @param nodeInstanceId 节点实例 ID
 * @param resourceId 文件资源 ID
 * @param fileName 文件名
 * @returns 提交结果（包含 AI 评审状态）
 */
export const submitPlan = (nodeInstanceId: number, resourceId: number, fileName: string) =>
  http.post<ApiResult<PlanSubmitResponse>>(
    `/api/student/nodes/${nodeInstanceId}/submit`,
    { resource_id: resourceId, file_name: fileName }
  ).then(r => r.data)
