import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type { PageResult } from '@/services/types/admin.types'
import type { CreateTemplateRequest, CreateTemplateResponse, TrainingTemplate } from '@/services/types/template.types'

export const getTemplates = (page = 1, pageSize = 10, aiStatus?: number) =>
  http.get<ApiResult<PageResult<TrainingTemplate>>>('/api/teacher/templates', { params: { page, pageSize, aiStatus } }).then(r => r.data)

export const createTemplate = (data: CreateTemplateRequest) =>
  http.post<ApiResult<CreateTemplateResponse>>('/api/teacher/templates', data).then(r => r.data)

export const deleteTemplate = (id: number) =>
  http.delete<ApiResult<void>>(`/api/teacher/templates/${id}`).then(r => r.data)
