type MessageHandler = (data: any) => void

/**
 * 实训WebSocket客户端
 *
 * 提供WebSocket连接管理、消息订阅、自动重连等功能
 *
 * @example
 * const client = new TrainingWSClient('ws://localhost:8080/ws/training')
 * await client.connect()
 * client.on('NODE_STATUS', (data) => { console.log('节点状态更新', data) })
 * client.send('HEARTBEAT', { timestamp: Date.now() })
 * client.close()
 *
 * @class TrainingWSClient
 */
export class TrainingWSClient {
  /** WebSocket服务器地址 */
  private url: string
  /** WebSocket实例 */
  private ws: WebSocket | null = null
  /** 消息处理器映射表：消息类型 -> 处理器数组 */
  private handlers = new Map<string, MessageHandler[]>()
  /** 心跳定时器 */
  private pingTimer: ReturnType<typeof setInterval> | null = null
  /** 重连定时器 */
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null
  /** 已重连次数 */
  private reconnectAttempts = 0
  /** 最大重连次数 */
  private maxReconnect = 10
  /** 是否已销毁 */
  private destroyed = false

  constructor(url: string) { this.url = url }

  /**
   * 建立WebSocket连接
   *
   * @returns Promise<void> 连接成功时resolve
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.destroyed) return reject(new Error('Client destroyed'))
      try {
        this.ws = new WebSocket(this.url)
        this.ws.onopen = () => {
          this.reconnectAttempts = 0
          this.startPing()
          resolve()
        }
        this.ws.onmessage = (e) => {
          try {
            const msg = JSON.parse(e.data)
            if (msg.type === 'PONG') return
            const handlers = this.handlers.get(msg.type) || []
            for (const fn of handlers) fn(msg.payload || msg)
          } catch { /* ignore malformed */ }
        }
        this.ws.onerror = () => { if (this.ws?.readyState !== WebSocket.OPEN) reject(new Error('连接失败')) }
        this.ws.onclose = () => { this.stopPing(); if (!this.destroyed && this.reconnectAttempts < this.maxReconnect) this.scheduleReconnect() }
      } catch (e) { reject(e) }
    })
  }

  /**
   * 订阅消息
   *
   * @param type 消息类型
   * @param fn 消息处理器
   */
  on(type: string, fn: MessageHandler) {
    if (!this.handlers.has(type)) this.handlers.set(type, [])
    this.handlers.get(type)!.push(fn)
  }

  /**
   * 取消订阅消息
   *
   * @param type 消息类型
   * @param fn 可选，指定要移除的处理器，不传则移除该类型所有处理器
   */
  off(type: string, fn?: MessageHandler) {
    if (!fn) { this.handlers.delete(type); return }
    const arr = this.handlers.get(type)
    if (arr) { const i = arr.indexOf(fn); if (i >= 0) arr.splice(i, 1) }
  }

  /**
   * 发送消息
   *
   * @param type 消息类型
   * @param payload 消息载荷
   */
  send(type: string, payload: any) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify({ type, payload }))
    }
  }

  /** 启动心跳 */
  private startPing() {
    this.stopPing()
    this.pingTimer = setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) this.ws.send(JSON.stringify({ type: 'PING' }))
    }, 25000)
  }

  /** 停止心跳 */
  private stopPing() { if (this.pingTimer) { clearInterval(this.pingTimer); this.pingTimer = null } }

  /** 调度重连（指数退避策略） */
  private scheduleReconnect() {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer)
    const delay = Math.min(30000, 1000 * Math.pow(2, this.reconnectAttempts))
    this.reconnectTimer = setTimeout(() => {
      this.reconnectAttempts++
      this.connect().catch(() => {})
    }, delay)
  }

  /** 关闭连接并释放资源 */
  close() {
    this.destroyed = true
    this.stopPing()
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer)
    if (this.ws) { this.ws.onclose = null; this.ws.close(); this.ws = null }
    this.handlers.clear()
  }
}
