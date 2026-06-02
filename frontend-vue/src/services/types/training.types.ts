export interface CreateReq {
  name: string
  description?: string
  // 添加其他必要字段
}

export interface Training {
  id: number
  name: string
  description?: string
  createdAt?: string
  updatedAt?: string
}

// ─── 编码实训任务 ──────────────────────────────────────────────────────────────

/**
 * 编码实训任务数据结构
 * 
 * 对应后端 CodeTask 实体，用于 AI 编程实训任务页面
 */
export interface CodeTask {
  /** 任务唯一标识 */
  id: string
  /** 任务标题 */
  title: string
  /** 任务描述 */
  description: string
  /** 初始代码内容 */
  initial_code: string
  /** AI初始问题 */
  initial_question: string
  /** 编程语言类型 */
  language: string
  /** 创建时间 */
  created_at: string
  /** 更新时间 */
  updated_at: string
}

/**
 * 代码执行请求
 */
export interface CodeRunRequest {
  /** 编程语言 */
  language: string
  /** 代码内容 */
  code: string
}

/**
 * 代码执行响应
 */
export interface CodeRunResponse {
  /** 执行输出 */
  output?: string
  /** 错误信息 */
  error?: string
  /** 退出码 */
  exit_code?: number
}

/**
 * AI流式对话请求
 */
export interface AiChatStreamRequest {
  /** 消息历史 */
  messages: Array<{ role: 'user' | 'assistant'; content: string }>
}

/**
 * AI流式对话响应（单条）
 */
export interface AiChatStreamResponse {
  /** 是否完成 */
  done?: boolean
  /** 内容片段 */
  content?: string
}

// ─── 理论实训任务 ──────────────────────────────────────────────────────────────

/**
 * 理论实训单个学习页面
 */
export interface TheorySlide {
  /** 页面标题 */
  title: string
  /** 页面内容 */
  content: string
  /** 页面类型 */
  type: 'theory' | 'intro' | 'code' | 'quiz' | 'content'
  /** 代码示例（仅 code 类型需要） */
  code?: string
  /** 测验选项（仅 quiz 类型需要） */
  options?: string[]
  /** 测验答案索引（仅 quiz 类型需要） */
  answer?: number
}

/**
 * 理论实训任务数据结构
 * 
 * 对应后端 TheoryTask 实体，用于理论实训课堂页面
 */
export interface TheoryTask {
  /** 任务唯一标识 */
  id: string
  /** 课件标题 */
  title: string
  /** 任务描述 */
  description: string
  /** 学习页面数据 */
  slides: TheorySlide[]
  /** 创建时间 */
  created_at?: string
  /** 更新时间 */
  updated_at?: string
}