<template>
  <div class="global-ai-assistant">
    <!-- 悬浮球 -->
    <div
      v-show="!isOpen"
      ref="floatBall"
      class="float-ball"
      :style="{
        left: position.x + 'px',
        top: position.y + 'px',
        transition: isDragging ? 'none' : 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
      }"
      @mousedown="startDrag"
      @touchstart.passive="startDrag"
    >
      <svg class="ball-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 5c.67 0 1.35.09 2 .26 1.78-2 5.03-2.84 5.5-2.84a.5.5 0 0 1 .5.5v3.08A9 9 0 0 1 12 21a9 9 0 0 1-8-15.01V2.92a.5.5 0 0 1 .5-.5c.47 0 3.72.84 5.5 2.84.65-.17 1.33-.26 2-.26z" />
        <path d="M8 14v.5" /><path d="M16 14v.5" />
        <path d="M11.25 16.25h1.5L12 17l-.75-.75z" />
      </svg>
      <span class="ball-badge"></span>
    </div>

    <!-- 对话窗口 -->
    <transition name="pop">
      <div v-show="isOpen" class="chat-window">

        <!-- 标题栏 -->
        <div class="chat-header">
          <div class="header-left">
            <button class="icon-btn" title="历史对话" @click="toggleHistory">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16m-7 6h7"/></svg>
            </button>
            <span class="header-title">喵喵 AI 助教</span>
          </div>
          <div class="header-right">
            <button class="icon-btn" title="新建对话" @click="handleNewSession">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/></svg>
            </button>
            <button class="icon-btn" title="关闭" @click="closeChat">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>
        </div>

        <!-- 主体区域 -->
        <div class="chat-body">

          <!-- 历史会话侧边栏 -->
          <div class="history-panel" :class="{ open: showHistory }">
            <div class="history-header">
              <span>历史对话</span>
              <button class="icon-btn-sm" @click="handleNewSession">+ 新建</button>
            </div>
            <div v-if="store.sessionsLoading" class="history-loading">加载中…</div>
            <div v-else-if="store.sessions.length === 0" class="history-empty">暂无历史对话</div>
            <div v-else class="history-list custom-scrollbar">
              <button
                v-for="session in store.sessions"
                :key="session.id"
                class="history-item"
                :class="{ active: store.currentSessionId === session.id }"
                @click="handleSwitchSession(session.id)"
              >
                <span class="history-item-title">{{ session.title || '未命名对话' }}</span>
                <button
                  class="history-delete"
                  title="删除"
                  @click.stop="handleDeleteSession(session.id)"
                >×</button>
              </button>
            </div>
          </div>

          <!-- 消息区域 -->
          <div
            ref="messageContainer"
            class="message-list custom-scrollbar"
            @click="showHistory = false"
          >
            <!-- 欢迎语（无消息时显示） -->
            <div v-if="store.messages.length === 0" class="welcome-msg">
              <div class="msg-bubble assistant-bubble">
                同学你好！我是你的专属 AI 助教，遇到任何实训问题都可以问我哦 🐱
              </div>
            </div>

            <!-- 消息列表 -->
            <template v-for="msg in store.messages" :key="msg.id">
              <!-- 用户消息 -->
              <div v-if="msg.role === 'user'" class="msg-row user-row">
                <div class="msg-bubble user-bubble">{{ msg.content }}</div>
              </div>
              <!-- AI 消息 -->
              <div v-else class="msg-row assistant-row">
                <span class="msg-label">喵喵 AI</span>
                <div class="msg-bubble assistant-bubble">{{ msg.content }}</div>
              </div>
            </template>

            <!-- AI 思考中 -->
            <div v-if="store.loading" class="msg-row assistant-row">
              <span class="msg-label">喵喵 AI</span>
              <div class="msg-bubble assistant-bubble thinking">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-area">
          <div class="input-wrapper">
            <textarea
              ref="inputRef"
              v-model="inputText"
              rows="1"
              class="chat-textarea custom-scrollbar"
              placeholder="输入你的问题…"
              :disabled="store.loading"
              @keydown.enter.exact.prevent="handleSend"
              @input="autoResize"
            ></textarea>
            <button
              class="send-btn"
              :disabled="!inputText.trim() || store.loading"
              @click="handleSend"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/></svg>
            </button>
          </div>
        </div>

      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useAiChatStore } from '@/stores/modules/aiChat.store'

const store = useAiChatStore()

// ─── UI 状态 ──────────────────────────────────────────────────────────────────
const isOpen = ref(false)
const showHistory = ref(false)
const inputText = ref('')
const inputRef = ref<HTMLTextAreaElement | null>(null)
const messageContainer = ref<HTMLDivElement | null>(null)

// ─── 悬浮球拖拽 ───────────────────────────────────────────────────────────────
const floatBall = ref<HTMLDivElement | null>(null)
const position = ref({ x: 0, y: 0 })
const isDragging = ref(false)
let hasMoved = false
let startMousePos = { x: 0, y: 0 }
let startElPos = { x: 0, y: 0 }

onMounted(() => {
  position.value = {
    x: window.innerWidth - 80,
    y: window.innerHeight * 0.7
  }
})

const getClientPos = (e: MouseEvent | TouchEvent): { x: number; y: number } => {
  if ('touches' in e) {
    const touch0 = e.touches.item(0)
    if (touch0) {
      return { x: touch0.clientX, y: touch0.clientY }
    }
  }
  const mouseEvent = e as MouseEvent
  return { x: mouseEvent.clientX, y: mouseEvent.clientY }
}

const startDrag = (e: MouseEvent | TouchEvent) => {
  e.preventDefault()
  isDragging.value = true
  hasMoved = false

  const { x: clientX, y: clientY } = getClientPos(e)
  startMousePos = { x: clientX, y: clientY }
  startElPos = { ...position.value }

  window.addEventListener('mousemove', onDrag)
  window.addEventListener('touchmove', onDrag, { passive: false })
  window.addEventListener('mouseup', endDrag)
  window.addEventListener('touchend', endDrag)
}

const onDrag = (e: MouseEvent | TouchEvent) => {
  if (!isDragging.value) return
  const { x: clientX, y: clientY } = getClientPos(e)

  const deltaX = clientX - startMousePos.x
  const deltaY = clientY - startMousePos.y

  if (Math.abs(deltaX) > 3 || Math.abs(deltaY) > 3) hasMoved = true

  const newX = Math.max(0, Math.min(startElPos.x + deltaX, window.innerWidth - 56))
  const newY = Math.max(0, Math.min(startElPos.y + deltaY, window.innerHeight - 56))
  position.value = { x: newX, y: newY }
}

const endDrag = () => {
  isDragging.value = false
  window.removeEventListener('mousemove', onDrag)
  window.removeEventListener('touchmove', onDrag)
  window.removeEventListener('mouseup', endDrag)
  window.removeEventListener('touchend', endDrag)

  if (!hasMoved) {
    openChat()
    return
  }

  // 吸边
  const centerX = window.innerWidth / 2
  position.value.x = position.value.x < centerX ? 10 : window.innerWidth - 66
}

onUnmounted(() => {
  window.removeEventListener('mousemove', onDrag)
  window.removeEventListener('touchmove', onDrag)
  window.removeEventListener('mouseup', endDrag)
  window.removeEventListener('touchend', endDrag)
})

// ─── 窗口控制 ─────────────────────────────────────────────────────────────────
const openChat = () => {
  isOpen.value = true
  store.loadSessions()
  nextTick(() => scrollToBottom())
}

const closeChat = () => {
  isOpen.value = false
  showHistory.value = false
}

const toggleHistory = () => {
  showHistory.value = !showHistory.value
}

// ─── 消息操作 ─────────────────────────────────────────────────────────────────
const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text || store.loading) return

  inputText.value = ''
  resetTextareaHeight()
  showHistory.value = false

  await store.sendMessage(text)
  await nextTick()
  scrollToBottom()
}

const handleNewSession = () => {
  store.newSession()
  showHistory.value = false
  nextTick(() => inputRef.value?.focus())
}

const handleSwitchSession = async (sessionId: string) => {
  await store.switchSession(sessionId)
  showHistory.value = false
  await nextTick()
  scrollToBottom()
}

const handleDeleteSession = async (sessionId: string) => {
  await store.removeSession(sessionId)
}

// ─── 辅助 ─────────────────────────────────────────────────────────────────────
const scrollToBottom = () => {
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
  }
}

const autoResize = () => {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

const resetTextareaHeight = () => {
  if (inputRef.value) inputRef.value.style.height = 'auto'
}

// 新消息时自动滚底
watch(
  () => store.messages.length,
  async () => {
    await nextTick()
    scrollToBottom()
  }
)
</script>

<style scoped>
/* ─── 悬浮球 ─────────────────────────────────────────────────────────────── */
.float-ball {
  position: fixed;
  z-index: 9999;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #9333ea);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.18);
  transition: transform 0.2s;
}
.float-ball:hover { transform: scale(1.1); }
.float-ball:active { cursor: grabbing; }

.ball-icon { width: 28px; height: 28px; }

.ball-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: #ef4444;
  border: 2px solid #fff;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* ─── 对话窗口 ───────────────────────────────────────────────────────────── */
.chat-window {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 380px;
  height: 600px;
  z-index: 9998;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ─── 标题栏 ─────────────────────────────────────────────────────────────── */
.chat-header {
  height: 52px;
  background: linear-gradient(90deg, #6366f1, #9333ea);
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.header-title {
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
}

.icon-btn {
  width: 30px;
  height: 30px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, color 0.15s;
  padding: 0;
}
.icon-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}
.icon-btn svg { width: 18px; height: 18px; }

/* ─── 主体 ───────────────────────────────────────────────────────────────── */
.chat-body {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: #f8fafc;
}

/* ─── 历史侧边栏 ─────────────────────────────────────────────────────────── */
.history-panel {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 65%;
  background: rgba(255, 255, 255, 0.97);
  border-right: 1px solid #e2e8f0;
  box-shadow: 4px 0 16px rgba(0, 0, 0, 0.08);
  z-index: 10;
  display: flex;
  flex-direction: column;
  transform: translateX(-100%);
  transition: transform 0.25s ease;
}
.history-panel.open { transform: translateX(0); }

.history-header {
  padding: 10px 12px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.icon-btn-sm {
  font-size: 11px;
  color: #6366f1;
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  transition: background 0.15s;
}
.icon-btn-sm:hover { background: #ede9fe; }

.history-loading,
.history-empty {
  padding: 16px 12px;
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
}

.history-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border: none;
  background: none;
  border-radius: 8px;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s;
  gap: 6px;
}
.history-item:hover { background: #f1f5f9; }
.history-item.active { background: #ede9fe; }

.history-item-title {
  font-size: 12px;
  color: #374151;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-delete {
  font-size: 14px;
  color: #94a3b8;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0 2px;
  line-height: 1;
  flex-shrink: 0;
  transition: color 0.15s;
}
.history-delete:hover { color: #ef4444; }

/* ─── 消息列表 ───────────────────────────────────────────────────────────── */
.message-list {
  height: 100%;
  overflow-y: auto;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.welcome-msg { display: flex; flex-direction: column; align-items: flex-start; }

.msg-row { display: flex; flex-direction: column; }
.user-row { align-items: flex-end; }
.assistant-row { align-items: flex-start; gap: 2px; }

.msg-label {
  font-size: 10px;
  color: #94a3b8;
  margin-left: 4px;
  margin-bottom: 2px;
}

.msg-bubble {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

.user-bubble {
  background: linear-gradient(135deg, #6366f1, #9333ea);
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.assistant-bubble {
  background: #fff;
  color: #374151;
  border: 1px solid #e2e8f0;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

/* 思考动画 */
.thinking {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 12px 16px;
}
.thinking span {
  width: 6px;
  height: 6px;
  background: #94a3b8;
  border-radius: 50%;
  animation: bounce 1.2s infinite;
}
.thinking span:nth-child(2) { animation-delay: 0.2s; }
.thinking span:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-6px); }
}

/* ─── 输入区域 ───────────────────────────────────────────────────────────── */
.chat-input-area {
  padding: 10px 12px;
  background: #fff;
  border-top: 1px solid #f1f5f9;
  flex-shrink: 0;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 6px 6px 6px 12px;
  transition: border-color 0.15s;
}
.input-wrapper:focus-within { border-color: #6366f1; }

.chat-textarea {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  font-size: 13px;
  color: #374151;
  resize: none;
  min-height: 22px;
  max-height: 120px;
  line-height: 1.5;
  font-family: inherit;
}
.chat-textarea::placeholder { color: #94a3b8; }
.chat-textarea:disabled { opacity: 0.6; cursor: not-allowed; }

.send-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: #6366f1;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.15s, transform 0.1s;
}
.send-btn:hover:not(:disabled) { background: #4f46e5; }
.send-btn:active:not(:disabled) { transform: scale(0.92); }
.send-btn:disabled { background: #c7d2fe; cursor: not-allowed; }
.send-btn svg { width: 16px; height: 16px; }

/* ─── 滚动条 ─────────────────────────────────────────────────────────────── */
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }

/* ─── 弹出动画 ───────────────────────────────────────────────────────────── */
.pop-enter-active,
.pop-leave-active {
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  transform-origin: bottom right;
}
.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: scale(0.8) translateY(20px);
}
</style>
