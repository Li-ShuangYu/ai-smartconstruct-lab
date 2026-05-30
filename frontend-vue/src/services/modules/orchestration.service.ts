/**
 * 编排系统 API 服务
 *
 * 提供模板 CRUD、发布、重试、预览等编排相关的 API 调用。
 *
 * @module services/modules/orchestration
 */
import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type {
  CanvasJson,
  NodeTypeDef,
  PublishTemplateRequest,
  PublishTemplateResponse,
  RetryTemplateRequest,
  RetryTemplateResponse,
  TemplatePreviewData
} from '@/services/types/orchestration'
import type { NodeAiStatus } from '@/services/types/studentTraining'

/** 保存模板画布数据 */
export const saveTemplateCanvas = (templateId: string, canvasData: CanvasJson) =>
  http.put<ApiResult<void>>(`/api/teacher/templates/${templateId}/canvas`, { canvasData }).then(r => r.data)

/** 发布模板（触发AI处理） */
export const publishTemplate = (templateId: string, data: PublishTemplateRequest) =>
  http.post<ApiResult<PublishTemplateResponse>>(`/api/teacher/templates/${templateId}/publish`, data).then(r => r.data)

/** 重试失败的AI处理 */
export const retryTemplate = (templateId: string, data?: RetryTemplateRequest) =>
  http.post<ApiResult<RetryTemplateResponse>>(`/api/teacher/templates/${templateId}/retry-ai`, data ?? {}).then(r => r.data)

/** 获取模板AI处理状态 */
export const getTemplateAiStatus = (templateId: string) =>
  http.get<ApiResult<{ ai_status: number; error_reason: string | null }>>(`/api/teacher/templates/${templateId}/ai-status`).then(r => r.data)

/** 获取模板各节点AI状态列表 */
export const getNodeAiStatuses = (templateId: string) =>
  http.get<ApiResult<NodeAiStatus[]>>(`/api/teacher/templates/${templateId}/node-ai-statuses`).then(r => r.data)

/** 获取模板预览数据 */
export const getTemplatePreview = (templateId: string) =>
  http.get<ApiResult<TemplatePreviewData>>(`/api/teacher/templates/${templateId}/preview`).then(r => r.data)

/** 获取模板AI处理日志 */
export const getTemplateAiLogs = (templateId: string) =>
  http.get<ApiResult<{ template_id: string; template_name: string; ai_status: number; error_reason: string | null; logs: AiProcessingLogItem[] }>>(`/api/teacher/templates/${templateId}/ai-logs`).then(r => r.data)

/** AI处理日志条目 */
export interface AiProcessingLogItem {
  id: number
  templateId: number
  nodeId: string | null
  nodeType: string | null
  jobId: string | null
  eventType: string
  eventLabel: string
  eventStatus: string
  message: string | null
  detailJson: Record<string, unknown> | null
  createdAt: string
}

/** 获取所有可用节点类型定义 */
export const getNodeTypeDefs = () =>
  http.get<ApiResult<NodeTypeDef[]>>('/api/teacher/node-defs').then(r => r.data)

/** 获取单个节点类型定义 */
export const getNodeTypeDef = (nodeType: string) =>
  http.get<ApiResult<NodeTypeDef>>(`/api/teacher/node-defs/${nodeType}`).then(r => r.data)
