<template>
  <div class="terminal-wrapper">
    <div class="terminal-header">
      <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="#6366f1" stroke-width="2"><polyline points="4 17 10 11 4 5"/><line x1="12" y1="19" x2="20" y2="19"/></svg>
      <span class="terminal-title">终端输出</span>
      <div class="terminal-actions">
        <span v-if="exitCode !== null" class="exit-badge" :class="exitCode === 0 ? 'ok' : 'err'">
          退出码 {{ exitCode }}
        </span>
        <button class="term-btn" @click="sendAllToAi" :disabled="!lines.length">添加终端信息至会话</button>
        <button class="term-btn" @click="clear" :disabled="!lines.length">清空</button>
        <button v-if="running" class="term-btn term-btn--stop" @click="$emit('stop')">停止</button>
      </div>
    </div>
    <div class="terminal-body" ref="termRef">
      <div v-if="!lines.length" class="term-placeholder">点击「运行」查看输出结果</div>
      <div v-for="(l, i) in lines" :key="i" class="term-line" :class="l.type">
        <span v-if="l.type === 'info'" class="term-prefix">ℹ</span>
        <span v-else-if="l.type === 'error'" class="term-prefix err-prefix">✕</span>
        <span class="term-text">{{ l.text }}</span>
        <button v-if="l.text.trim() && l.type !== 'info'" class="term-add-btn"
          @click="$emit('sendToAi', l.text)" title="发给 AI">↗</button>
      </div>
      <div v-if="running" class="term-line term-line--running">
        <span class="running-dot"></span>
        <span class="term-text">运行中...</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'

export interface TermLine {
  text: string
  type: 'out' | 'error' | 'info'
}

const props = withDefaults(defineProps<{
  lines?: TermLine[]
  running?: boolean
  exitCode?: number | null
}>(), {
  lines: () => [],
  running: false,
  exitCode: null,
})

const emit = defineEmits<{
  clear: []
  stop: []
  sendToAi: [text: string]
}>()

const termRef = ref<HTMLDivElement | null>(null)

function clear() {
  emit('clear')
}

function sendAllToAi() {
  const text = props.lines.map(line => line.text).join('\n').trim()
  if (text) emit('sendToAi', text)
}

watch(() => props.lines.length, () => {
  nextTick(() => {
    if (termRef.value) {
      termRef.value.scrollTop = termRef.value.scrollHeight
    }
  })
})
</script>

<style scoped>
.terminal-wrapper {
  height: 180px;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  border-radius: 0 0 8px 8px;
  font-family: 'Cascadia Code', 'Consolas', 'Monaco', monospace;
}

.terminal-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #f1f5f9;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.terminal-title {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
}

.terminal-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.exit-badge {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: 600;
}

.exit-badge.ok {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
}

.exit-badge.err {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.term-btn {
  font-size: 10px;
  font-weight: 500;
  color: #64748b;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 3px;
  transition: all 0.15s;
}

.term-btn:hover:not(:disabled) {
  background: #e2e8f0;
  color: #475569;
}

.term-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.term-btn--stop {
  color: #dc2626;
}

.term-btn--stop:hover {
  background: rgba(239, 68, 68, 0.1);
}

.terminal-body {
  flex: 1;
  padding: 8px 12px;
  overflow-y: auto;
  font-size: 12px;
  line-height: 1.5;
}

.term-placeholder {
  color: #cbd5e1;
  font-style: italic;
  font-size: 12px;
}

.term-line {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-bottom: 2px;
  color: #334155;
}

.term-line.info {
  color: #6366f1;
}

.term-line.error {
  color: #dc2626;
}

.term-prefix {
  flex-shrink: 0;
  font-weight: 700;
}

.err-prefix {
  color: #dc2626;
}

.term-text {
  flex: 1;
  word-break: break-all;
  white-space: pre-wrap;
}

.term-add-btn {
  opacity: 0;
  visibility: hidden;
  background: #6366f1;
  border: none;
  color: #fff;
  font-size: 10px;
  padding: 2px 4px;
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.15s;
  flex-shrink: 0;
}

.term-line:hover .term-add-btn {
  opacity: 1;
  visibility: visible;
}

.term-add-btn:hover {
  background: #4f46e5;
}

.term-line--running {
  color: #6366f1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.running-dot {
  width: 6px;
  height: 6px;
  background: #6366f1;
  border-radius: 50%;
  animation: pulse 1s infinite;
  flex-shrink: 0;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
