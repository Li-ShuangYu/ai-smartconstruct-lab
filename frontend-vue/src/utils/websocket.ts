type MessageHandler = (data: any) => void

export class TrainingWSClient {
  private url: string
  private ws: WebSocket | null = null
  private handlers = new Map<string, MessageHandler[]>()
  private pingTimer: ReturnType<typeof setInterval> | null = null
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null
  private reconnectAttempts = 0
  private maxReconnect = 10
  private destroyed = false

  constructor(url: string) { this.url = url }

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

  on(type: string, fn: MessageHandler) {
    if (!this.handlers.has(type)) this.handlers.set(type, [])
    this.handlers.get(type)!.push(fn)
  }

  off(type: string, fn?: MessageHandler) {
    if (!fn) { this.handlers.delete(type); return }
    const arr = this.handlers.get(type)
    if (arr) { const i = arr.indexOf(fn); if (i >= 0) arr.splice(i, 1) }
  }

  send(type: string, payload: any) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify({ type, payload }))
    }
  }

  private startPing() {
    this.stopPing()
    this.pingTimer = setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) this.ws.send(JSON.stringify({ type: 'PING' }))
    }, 25000)
  }
  private stopPing() { if (this.pingTimer) { clearInterval(this.pingTimer); this.pingTimer = null } }

  private scheduleReconnect() {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer)
    const delay = Math.min(30000, 1000 * Math.pow(2, this.reconnectAttempts))
    this.reconnectTimer = setTimeout(() => {
      this.reconnectAttempts++
      this.connect().catch(() => {})
    }, delay)
  }

  close() {
    this.destroyed = true
    this.stopPing()
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer)
    if (this.ws) { this.ws.onclose = null; this.ws.close(); this.ws = null }
    this.handlers.clear()
  }
}
