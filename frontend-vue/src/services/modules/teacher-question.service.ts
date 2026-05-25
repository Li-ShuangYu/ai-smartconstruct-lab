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

export interface BizQuestion {
  id?: string
  bankId: string
  questionType: number
  content: string
  standardAnswer: string
  defaultScore: number
  sortNum: number
  createdAt?: string
  updatedAt?: string
}

export const getQuestionBanks = () =>
  http.get<ApiResult<QuestionBankVO[]>>('/api/teacher/question-banks').then(r => r.data)

export const createQuestionBank = (data: { bankName: string; isPublic: number }) =>
  http.post<ApiResult<void>>('/api/teacher/question-banks', data).then(r => r.data)

export const updateQuestionBank = (id: string, data: { bankName: string; isPublic: number }) =>
  http.put<ApiResult<void>>(`/api/teacher/question-banks/${id}`, data).then(r => r.data)

export const deleteQuestionBank = (id: string) =>
  http.delete<ApiResult<void>>(`/api/teacher/question-banks/${id}`).then(r => r.data)

export const getQuestions = (bankId: string) =>
  http.get<ApiResult<BizQuestion[]>>('/api/teacher/questions', { params: { bankId } }).then(r => r.data)

export const getQuestion = (id: string) =>
  http.get<ApiResult<BizQuestion>>(`/api/teacher/questions/${id}`).then(r => r.data)

export const createQuestion = (data: any) =>
  http.post<ApiResult<void>>('/api/teacher/questions', data).then(r => r.data)

export const updateQuestion = (id: string, data: any) =>
  http.put<ApiResult<void>>(`/api/teacher/questions/${id}`, data).then(r => r.data)

export const deleteQuestion = (id: string) =>
  http.delete<ApiResult<void>>(`/api/teacher/questions/${id}`).then(r => r.data)