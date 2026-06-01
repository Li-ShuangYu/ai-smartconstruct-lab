/**
 * AI 对话 Store
 *
 * 管理 AI 悬浮窗对话的全局状态，包括：
 * - 当前会话 ID 和消息列表
 * - 历史会话列表
 * - 发送消息的 loading 状态
 *
 * @module stores/modules/aiChat
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { AiMessageVO, AiSessionSummary, AiSessionType } from '@/services/types/aiChat.types'
import * as aiChatService from '@/services/modules/aiChat.service'

export const useAiChatStore = defineStore('aiChat', () => {
  // ─── 状态 ──────────────────────────────────────────────────────────────────

  /** 当前会话 ID（null 表示尚未建立会话） */
  const currentSessionId = ref<string | null>(null)

  /** 当前会话的消息列表 */
  const messages = ref<AiMessageVO[]>([])

  /** 历史会话列表 */
  const sessions = ref<AiSessionSummary[]>([])

  /** 是否正在等待 AI 回复 */
  const loading = ref(false)

  /** 是否正在加载历史会话 */
  const sessionsLoading = ref(false)

  // ─── Actions ───────────────────────────────────────────────────────────────

  /**
   * 发送消息
   *
   * 乐观更新：先将用户消息追加到列表，再等待 AI 回复。
   */
  async function sendMessage(
    content: string,
    sessionType: AiSessionType = 1,
    taskId?: number | null
  ): Promise<void> {
    if (!content.trim() || loading.value) return

    loading.value = true

    // 乐观追加用户消息
    const tempUserMsg: AiMessageVO = {
      id: `temp-${Date.now()}`,
      role: 'user',
      content: content.trim(),
      createdAt: new Date().toISOString()
    }
    messages.value.push(tempUserMsg)

    try {
      const result = await aiChatService.sendMessage({
        sessionId: currentSessionId.value ? Number(currentSessionId.value) : null,
        content: content.trim(),
        sessionType,
        taskId
      })

      if (result.code === 200 && result.data) {
        // 更新会话 ID（首次对话时后端创建新会话）
        currentSessionId.value = result.data.sessionId

        // 追加 AI 回复
        const assistantMsg: AiMessageVO = {
          id: result.data.messageId,
          role: 'assistant',
          content: result.data.content,
          createdAt: new Date().toISOString()
        }
        messages.value.push(assistantMsg)
      } else {
        // 追加错误提示
        messages.value.push({
          id: `err-${Date.now()}`,
          role: 'assistant',
          content: result.message || '抱歉，AI 助教暂时无法响应，请稍后再试。',
          createdAt: new Date().toISOString()
        })
      }
    } catch {
      messages.value.push({
        id: `err-${Date.now()}`,
        role: 'assistant',
        content: '网络异常，请检查连接后重试。',
        createdAt: new Date().toISOString()
      })
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载历史会话列表
   */
  async function loadSessions(): Promise<void> {
    sessionsLoading.value = true
    try {
      const result = await aiChatService.listSessions()
      if (result.code === 200 && result.data) {
        sessions.value = result.data
      }
    } catch {
      // 静默失败，不影响主流程
    } finally {
      sessionsLoading.value = false
    }
  }

  /**
   * 切换到指定历史会话（加载消息）
   */
  async function switchSession(sessionId: string): Promise<void> {
    try {
      const result = await aiChatService.getSession(sessionId)
      if (result.code === 200 && result.data) {
        currentSessionId.value = sessionId
        messages.value = result.data.messages
      }
    } catch {
      // 静默失败
    }
  }

  /**
   * 开启新会话（清空当前消息）
   */
  function newSession(): void {
    currentSessionId.value = null
    messages.value = []
  }

  /**
   * 删除历史会话
   */
  async function removeSession(sessionId: string): Promise<void> {
    try {
      await aiChatService.deleteSession(sessionId)
      sessions.value = sessions.value.filter((s) => s.id !== sessionId)
      if (currentSessionId.value === sessionId) {
        newSession()
      }
    } catch {
      // 静默失败
    }
  }

  return {
    currentSessionId,
    messages,
    sessions,
    loading,
    sessionsLoading,
    sendMessage,
    loadSessions,
    switchSession,
    newSession,
    removeSession
  }
})
