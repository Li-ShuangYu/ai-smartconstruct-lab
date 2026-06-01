/**
 * AI 对话服务
 *
 * 封装 AI 悬浮窗对话相关的 API 调用。
 *
 * @module services/modules/aiChat
 */
import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type {
  AiChatRequest,
  AiChatResponse,
  AiSessionSummary,
  AiSessionVO
} from '@/services/types/aiChat.types'

/** 发送消息给 AI 助教 */
export const sendMessage = (data: AiChatRequest) =>
  http
    .post<ApiResult<AiChatResponse>>('/api/student/ai/chat', data)
    .then((r) => r.data)

/** 获取历史会话列表 */
export const listSessions = () =>
  http
    .get<ApiResult<AiSessionSummary[]>>('/api/student/ai/sessions')
    .then((r) => r.data)

/** 获取会话详情（含消息） */
export const getSession = (sessionId: string | number) =>
  http
    .get<ApiResult<AiSessionVO>>(`/api/student/ai/sessions/${sessionId}`)
    .then((r) => r.data)

/** 软删除会话 */
export const deleteSession = (sessionId: string | number) =>
  http
    .delete<ApiResult<void>>(`/api/student/ai/sessions/${sessionId}`)
    .then((r) => r.data)
