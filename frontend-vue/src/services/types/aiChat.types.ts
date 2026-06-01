/**
 * AI 对话功能类型定义
 *
 * 对应后端 biz_ai_session / biz_ai_message 实体及相关 DTO。
 *
 * @module services/types/aiChat
 */

// ─── 会话类型 ─────────────────────────────────────────────────────────────────

/**
 * 会话类型枚举：
 * 1-全局悬浮 2-AI对练 3-理论实训 4-编码实训 5-答疑
 */
export type AiSessionType = 1 | 2 | 3 | 4 | 5

// ─── 请求 ─────────────────────────────────────────────────────────────────────

/** 发送消息请求 */
export interface AiChatRequest {
  /** 会话ID（可空，为空时创建新会话） */
  sessionId?: number | null
  /** 消息内容 */
  content: string
  /** 会话类型，默认 1（全局悬浮） */
  sessionType?: AiSessionType
  /** 关联实训任务ID（可空） */
  taskId?: number | null
  /** 关联节点实例ID（可空） */
  nodeInstanceId?: number | null
}

// ─── 响应 ─────────────────────────────────────────────────────────────────────

/** 发送消息响应 */
export interface AiChatResponse {
  /** 会话ID */
  sessionId: string
  /** AI 回复消息ID */
  messageId: string
  /** AI 回复内容 */
  content: string
  /** 消耗 Token 数 */
  tokensUsed: number | null
}

/** 消息视图对象 */
export interface AiMessageVO {
  /** 消息ID */
  id: string
  /** 角色：user / assistant */
  role: 'user' | 'assistant'
  /** 消息内容 */
  content: string
  /** 消息生成时间 */
  createdAt: string
}

/** 会话视图对象（列表项，不含消息） */
export interface AiSessionSummary {
  /** 会话ID */
  id: string
  /** 会话标题 */
  title: string
  /** 会话类型 */
  sessionType: AiSessionType
  /** AI生成的会话摘要 */
  summary: string | null
  /** 会话建立时间 */
  createdAt: string
}

/** 会话详情（含消息列表） */
export interface AiSessionVO extends AiSessionSummary {
  /** 消息列表（按时间升序） */
  messages: AiMessageVO[]
}
