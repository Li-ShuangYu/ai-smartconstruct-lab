import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'

export interface QuestionBankVO {
  id: string
  creatorId: string
  bankName: string
  isPublic: number
  createdAt?: string
  updatedAt?: string
  isCreator: boolean
}

export const getQuestionBanks = () =>
  http.get<ApiResult<QuestionBankVO[]>>('/api/teacher/question-banks').then(r => r.data)

export const createQuestionBank = (data: { bankName: string; isPublic: number }) =>
  http.post<ApiResult<void>>('/api/teacher/question-banks', data).then(r => r.data)

export const updateQuestionBank = (id: string, data: { bankName: string; isPublic: number }) =>
  http.put<ApiResult<void>>(`/api/teacher/question-banks/${id}`, data).then(r => r.data)

export const deleteQuestionBank = (id: string) =>
  http.delete<ApiResult<void>>(`/api/teacher/question-banks/${id}`).then(r => r.data)