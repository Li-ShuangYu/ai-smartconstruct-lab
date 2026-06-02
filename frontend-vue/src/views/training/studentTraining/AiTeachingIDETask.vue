<template>
  <div class="ide-root">
    <!-- ── 顶部栏 ── -->
    <header class="hdr">
      <div class="hdr-left">
        <button class="hdr-back" @click="resetSession" title="重新开始">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5m7-7-7 7 7 7"/></svg>
        </button>
        <div class="hdr-brand">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="#6366f1"><path d="M12 2l2 7h7l-5.5 4 2 7L12 16l-5.5 4 2-7L3 9h7z"/></svg>
          <span class="hdr-title">AI 编程实训</span>
        </div>
        <div class="hdr-lang-badge">
          <span class="lang-dot" :style="{ background: langColor }"></span>
          {{ langLabel }}
        </div>
      </div>
      <div class="hdr-center">
        <button class="hdr-btn run" @click="executeCode" :disabled="running">
          <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg>
          {{ running ? '运行中…' : '运行' }}
        </button>
        <button class="hdr-btn" @click="clearCode">
          <svg viewBox="0 0 24 24" width="13" height="13" stroke="currentColor" stroke-width="2" fill="none"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
          清空
        </button>
        <button class="hdr-btn" @click="resetSession">
          <svg viewBox="0 0 24 24" width="13" height="13" stroke="currentColor" stroke-width="2" fill="none"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 102.13-9.36L1 10"/></svg>
          重新开始
        </button>
        <button v-if="nodeInstanceId" class="hdr-btn complete" @click="handleComplete" :disabled="completing">
          <svg viewBox="0 0 24 24" width="13" height="13" fill="currentColor"><polyline points="20 6 9 17 4 12"/></svg>
          {{ completing ? '完成中…' : '完成任务' }}
        </button>
      </div>
      <div class="hdr-right">
        <div class="ai-status-pill" :class="aiStatus">
          <span class="status-dot"></span>
          {{ aiStatusLabel }}
        </div>
        <button class="hdr-btn" :class="{ active: showAiPanel }" @click="showAiPanel = !showAiPanel">
          <svg viewBox="0 0 24 24" width="13" height="13" stroke="currentColor" stroke-width="2" fill="none"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
          AI 辅导
        </button>
      </div>
    </header>

    <!-- ── 主体 ── -->
    <div class="body" ref="bodyRef">
      <!-- 左侧：编辑器 + 终端 -->
      <div class="editor-pane" :style="{ width: showAiPanel ? leftPct + '%' : '100%' }">
        <!-- 编辑器头部 -->
        <div class="code-header">
          <div class="code-header-left">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#6366f1" stroke-width="2"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
            <span class="code-title">代码编辑器</span>
            <span class="code-lang-badge">{{ langLabel }}</span>
          </div>
          <div class="code-header-right">
            <button class="code-action-btn" @click="copyCode" title="复制代码">
              <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg>
            </button>
          </div>
        </div>

        <!-- CodeEditor 组件 -->
        <div class="code-area">
          <CodeEditor ref="editorRef" :language="language" v-model="codeContent" />
        </div>

        <!-- Diff 对比面板 -->
        <div class="diff-panel" :class="{ visible: diffVisible }">
          <div class="diff-header">
            <span class="diff-title">AI 建议的修改</span>
            <div class="diff-actions">
              <button class="diff-btn" @click="applyDiff">
                <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 19v-7m0 0l-3 3m3-3l3 3"/></svg>
                应用修改
              </button>
              <button class="diff-btn" @click="diffVisible = false">
                <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                关闭
              </button>
            </div>
          </div>
          <div class="diff-content" v-html="diffHtml"></div>
        </div>

        <!-- 终端面板 -->
        <div class="term-area">
          <TerminalPanel
            ref="termRef"
            :lines="termLines"
            :running="running"
            :exit-code="lastExitCode"
            @clear="clearTerminal"
            @send-to-ai="addTerminalToAi"
          />
        </div>
      </div>

      <!-- 右侧：AI 对话面板 -->
      <div class="ai-panel" :class="{ visible: showAiPanel }">
        <div class="ai-panel-resizer" @mousedown="startResize"></div>
        <div class="ai-header">
          <div class="ai-title">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#6366f1" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
            AI 智能辅导
          </div>
          <button class="ai-clear" @click="clearMessages" title="清空对话">
            <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>

        <!-- AI 消息列表 -->
        <div class="ai-messages" ref="aiMessagesRef">
          <div v-for="(m, idx) in messages" :key="idx" class="ai-msg" :class="m.role">
            <div class="ai-av">{{ m.role === 'user' ? '👤' : '🤖' }}</div>
            <div class="ai-bd">
              <div class="ai-msg-text" v-html="renderMd(m.text)"></div>
              <div v-if="m.blocks.length" class="ai-msg-blocks">
                <div v-for="(block, bid) in m.blocks" :key="bid" class="ai-mcb">
                  <div class="ai-mcb-h">{{ block.lang || 'code' }}</div>
                  <pre><code>{{ block.code }}</code></pre>
                  <button class="ai-mcb-apply" @click="applyCode(block.code)">应用代码</button>
                </div>
              </div>
              <span v-if="m.streaming" class="ai-cursor">▊</span>
            </div>
          </div>

          <div v-if="aiStreaming && messages.length > 0 && messages[messages.length-1]?.role === 'assistant' && !messages[messages.length-1]?.text" class="ai-msg assistant">
            <div class="ai-av">🤖</div>
            <div class="ai-bd"><div class="ai-thinking"><span></span><span></span><span></span></div></div>
          </div>
        </div>

        <div class="ai-input-area">
          <div class="ai-input-row" :class="{ focused: aiInputFocused }">
            <textarea ref="aiInputRef" v-model="aiInput" class="ai-input"
              placeholder="输入问题，或选中代码后点击 ↗ 发给 AI…"
              rows="3" @keydown="aiKeydown"
              @focus="aiInputFocused = true" @blur="aiInputFocused = false" />
            <button class="ai-send-btn" :disabled="!aiInput.trim() || aiStreaming" @click="aiAsk()">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
            </button>
          </div>
          <div class="ai-input-hint">Enter 发送 · Shift+Enter 换行</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CodeEditor from './CodeEditor.vue'
import TerminalPanel from './TerminalPanel.vue'
import type { TermLine } from './TerminalPanel.vue'
import { getCodeTask } from '@/services/modules/training.service'
import { enterNode } from '@/services/modules/studentTraining.service'
import { useStudentFlowStore } from '@/stores/modules/studentFlow.store'
import type { CodeTask } from '@/services/types/training.types'
import type { CodingTaskConfig } from '@/services/types/studentTraining'

const route = useRoute()
const router = useRouter()
const store = useStudentFlowStore()

const props = defineProps<{
  nodeInstanceId?: string | number | null
  nodeConfig?: Record<string, unknown> | null
}>()

const emit = defineEmits<{
  complete: []
}>()

// ── Route params ──────────────────────────────────────────────────────────────
const taskId = ref('')
const nodeInstanceId = ref('')

// ── Language config ──────────────────────────────────────────────────────────
const langOptions = [
  { value: 'python',     label: 'Python',     color: '#3b82f6' },
  { value: 'javascript', label: 'JavaScript', color: '#f59e0b' },
  { value: 'java',       label: 'Java',       color: '#ef4444' },
  { value: 'cpp',        label: 'C++',        color: '#8b5cf6' },
]
const language  = ref('python')
const langLabel  = computed(() => langOptions.find(l => l.value === language.value)?.label ?? language.value)
const langColor  = computed(() => langOptions.find(l => l.value === language.value)?.color ?? '#6366f1')

// ── Task data ───────────────────────────────────────────────────────────────
const taskData = ref<CodeTask | null>(null)
const loading = ref(true)

// ── Editor state ────────────────────────────────────────────────────────────
const editorRef = ref<InstanceType<typeof CodeEditor> | null>(null)
const codeContent = ref('')
const diffVisible = ref(false)
const diffHtml = ref('')

// ── Terminal state ──────────────────────────────────────────────────────────
const termRef = ref<InstanceType<typeof TerminalPanel> | null>(null)
const termLines = ref<TermLine[]>([])
const running = ref(false)
const lastExitCode = ref<number | null>(null)
const completing = ref(false)

// ── AI Panel state ──────────────────────────────────────────────────────────
const showAiPanel = ref(true)
const leftPct = ref(65)
const bodyRef = ref<HTMLElement | null>(null)
const aiMessagesRef = ref<HTMLElement | null>(null)
const aiInputRef = ref<HTMLTextAreaElement | null>(null)
const aiInput = ref('')
const aiInputFocused = ref(false)
const aiStreaming = ref(false)

interface AiMessage {
  role: 'user' | 'assistant'
  text: string
  blocks: { lang: string; code: string }[]
  streaming?: boolean
}
const messages = ref<AiMessage[]>([])

const aiStatus = ref<'online' | 'thinking' | 'offline'>('online')
const aiStatusLabel = computed(() => {
  const map = { online: '在线', thinking: '思考中', offline: '离线' }
  return map[aiStatus.value]
})

// ── Resize handlers ─────────────────────────────────────────────────────────
let isResizing = false
function startResize(e: MouseEvent) {
  isResizing = true
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
}
function onResize(e: MouseEvent) {
  if (!isResizing || !bodyRef.value) return
  const rect = bodyRef.value.getBoundingClientRect()
  const pct = (e.clientX / rect.width) * 100
  leftPct.value = Math.max(30, Math.min(70, pct))
}
function stopResize() {
  isResizing = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
}

// ── Load task data ──────────────────────────────────────────────────────────
async function loadTask() {
  loading.value = true
  try {
    taskId.value = getRouteTaskId()
    nodeInstanceId.value = getRouteNodeInstanceId()
    
    if (nodeInstanceId.value && props.nodeConfig) {
      loadFromCodingConfig(props.nodeConfig, nodeInstanceId.value)
      return
    }

    // 如果有节点实例ID，先进入节点并获取配置
    if (nodeInstanceId.value) {
      await loadFromNodeConfig()
      return
    }
    
    // 否则使用独立的任务接口
    const response = await getCodeTask(taskId.value)
    
    if (response.code === 200 && response.data) {
      taskData.value = response.data
    } else {
      // 使用模拟数据
      taskData.value = getDemoTaskData()
    }

    initTaskData()

  } catch (error) {
    console.error('Failed to load task:', error)
    taskData.value = getDemoTaskData()
    initTaskData()
  }
  loading.value = false
}

function getRouteTaskId(): string {
  const queryTaskId = route.query.taskId
  if (typeof queryTaskId === 'string' && queryTaskId) return queryTaskId

  const paramTaskId = route.params.taskId
  if (typeof paramTaskId === 'string' && paramTaskId) return paramTaskId
  if (Array.isArray(paramTaskId) && paramTaskId[0]) return paramTaskId[0]

  return 'demo-001'
}

function getRouteNodeInstanceId(): string {
  if (props.nodeInstanceId !== null && props.nodeInstanceId !== undefined) {
    return String(props.nodeInstanceId)
  }

  const queryNodeInstanceId = route.query.nodeInstanceId
  if (typeof queryNodeInstanceId === 'string' && queryNodeInstanceId) return queryNodeInstanceId

  const queryNodeId = route.query.nodeId
  if (typeof queryNodeId === 'string' && queryNodeId) return queryNodeId

  return ''
}

function isCodingTaskConfig(value: unknown): value is CodingTaskConfig {
  return (
    typeof value === 'object' &&
    value !== null &&
    'code_template' in value &&
    typeof value.code_template === 'string'
  )
}

function loadFromCodingConfig(config: Record<string, unknown>, id: string): void {
  if (!isCodingTaskConfig(config)) {
    taskData.value = getDemoTaskData()
    initTaskData()
    loading.value = false
    return
  }

  taskData.value = {
    id,
    title: config.description?.substring(0, 50) || '编码实训任务',
    description: config.description || '请完成以下编码任务',
    initial_code: config.code_template,
    initial_question: config.hints?.join('\n') || '请完成代码实现',
    language: config.language || 'python',
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString()
  }
  initTaskData()
  loading.value = false
}

/**
 * 从节点配置加载任务数据
 */
async function loadFromNodeConfig() {
  try {
    // 进入节点
    const enterResult = await enterNode(nodeInstanceId.value)
    const nodeConfig = enterResult.data.node_config
    
    // 如果节点配置有编码任务数据，使用它
    if (nodeConfig) {
      loadFromCodingConfig(nodeConfig, nodeInstanceId.value)
    } else {
      // 回退到演示数据
      taskData.value = getDemoTaskData()
      initTaskData()
      loading.value = false
    }
  } catch (error) {
    console.error('Failed to load node config:', error)
    taskData.value = getDemoTaskData()
    initTaskData()
    loading.value = false
  }
}

/**
 * 初始化任务数据到界面
 */
function initTaskData() {
  if (!taskData.value) return
  
  // 设置语言
  language.value = taskData.value.language
  
  // 设置初始代码
  codeContent.value = taskData.value.initial_code

  // 添加初始AI消息
  messages.value = [
    { 
      role: 'assistant', 
      text: `**任务：${taskData.value.title}**\n\n${taskData.value.description}\n\n${taskData.value.initial_question}`, 
      blocks: [] 
    },
  ]
}

/**
 * 获取演示数据
 */
function getDemoTaskData(): CodeTask {
  return {
    id: 'demo-001',
    title: 'Python 入门：计算圆的面积',
    description: '本次实训任务是实现一个计算圆面积的函数。你需要完成 calculate_circle_area 函数，使其能够根据给定的半径计算圆的面积。',
    initial_code: `import math

def calculate_circle_area(radius):
    """计算圆的面积"""
    # TODO: 请完成这个函数
    pass

# 测试用例
if __name__ == "__main__":
    print(calculate_circle_area(5))  # 预期输出: 78.53981633974483
    print(calculate_circle_area(0))   # 预期输出: 0.0
    print(calculate_circle_area(10)) # 预期输出: 314.1592653589793`,
    initial_question: '请完成 calculate_circle_area 函数，使其能够正确计算圆的面积。',
    language: 'python',
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString()
  }
}

// ── Code execution ──────────────────────────────────────────────────────────
async function executeCode() {
  if (running.value) return
  if (!codeContent.value.trim()) {
    termLines.value = [{ type: 'error', text: '没有可运行的代码' }]
    lastExitCode.value = 1
    return
  }

  running.value = true
  lastExitCode.value = null
  termLines.value = [{ type: 'info', text: `>>> 开始运行 ${langLabel.value} 代码...` }]
  
  try {
    const response = await fetch('/ai/execute', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        code: codeContent.value,
        language: language.value,
        filename: 'main'
      })
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    const result = await response.json() as {
      stdout?: string
      stderr?: string
      exit_code?: number
    }

    appendTerminalOutput(result.stdout, 'out')
    appendTerminalOutput(result.stderr, 'error')
    lastExitCode.value = typeof result.exit_code === 'number' ? result.exit_code : null
    termLines.value.push({
      type: 'info',
      text: lastExitCode.value === 0 ? '执行成功' : '执行结束'
    })
  } catch (error) {
    lastExitCode.value = 1
    termLines.value.push({ type: 'error', text: `执行失败: ${(error as Error).message}` })
  } finally {
    running.value = false
  }
}

function appendTerminalOutput(output: string | undefined, type: TermLine['type']) {
  if (!output) return
  const lines = output.trimEnd().split('\n')
  for (const line of lines) {
    termLines.value.push({ type, text: line })
  }
}

function clearCode() {
  codeContent.value = ''
}

function copyCode() {
  navigator.clipboard.writeText(codeContent.value)
}

function resetSession() {
  loadTask()
}

function clearTerminal() {
  termLines.value = []
  lastExitCode.value = null
}

function addTerminalToAi(text: string) {
  const terminalText = text.trim()
  if (!terminalText) return

  const addition = `终端信息：\n\`\`\`text\n${terminalText}\n\`\`\``
  aiInput.value = aiInput.value.trim()
    ? `${aiInput.value.trim()}\n\n${addition}`
    : addition
  showAiPanel.value = true
  nextTick(() => aiInputRef.value?.focus())
}

/**
 * 完成节点
 */
async function handleComplete() {
  if (!nodeInstanceId.value || completing.value) return

  if (props.nodeInstanceId !== null && props.nodeInstanceId !== undefined) {
    emit('complete')
    return
  }
  
  completing.value = true
  try {
    // 使用 store 的 completeNode 方法完成节点，确保状态正确更新
    const result = await store.completeNode(nodeInstanceId.value)
    
    if (result) {
      // 完成成功，导航到下一个节点
      const nextNodeId = store.navigateToNextNode()
      
      // 返回实训执行页面
      router.push({
        path: `/student/training/${taskId.value}/execute`,
        query: nextNodeId ? { nodeId: nextNodeId } : {}
      })
    } else {
      alert('完成节点失败，请重试')
    }
  } catch (error) {
    console.error('Failed to complete node:', error)
    alert('完成节点失败，请重试')
  } finally {
    completing.value = false
  }
}

// ── AI chat functions ───────────────────────────────────────────────────────
function extractCodeBlocks(text: string) {
  const r: { lang: string; code: string }[] = []
  const re = /```(\w*)\n([\s\S]*?)```/g; let m
  while ((m = re.exec(text)) !== null) {
    const lang = m[1] || ''
    const code = m[2] ? m[2].trim() : ''
    r.push({ lang, code })
  }
  return r
}

function esc(t: string) { return t.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;') }

function renderMd(text: string): string {
  if (!text) return ''
  let h = esc(text)
  const blocks: { lang: string; code: string }[] = []
  h = h.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
    const i = blocks.length; blocks.push({ lang: lang || '', code: code.trim() }); return `%%B${i}%%`
  })
  h = h.replace(/`([^`]+)`/g, '<code class="ai-ic">$1</code>')
  h = h.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  h = h.replace(/^### (.+)$/gm, '<h4>$1</h4>').replace(/^## (.+)$/gm, '<h3>$1</h3>').replace(/^# (.+)$/gm, '<h2>$1</h2>')
  h = h.replace(/^- (.+)$/gm, '<li>$1</li>').replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')
  h = h.replace(/\n\n/g, '</p><p>').replace(/\n/g, '<br>')
  h = h.replace(/%%B(\d+)%%/g, (_, i) => {
    const b = blocks[+i]
    if (!b) return ''
    return `<div class="ai-mcb"><div class="ai-mcb-h">${b.lang || 'code'}</div><pre><code>${b.code}</code></pre></div>`
  })
  return '<p>' + h + '</p>'
}

function aiScroll() {
  nextTick(() => {
    if (aiMessagesRef.value) {
      aiMessagesRef.value.scrollTop = aiMessagesRef.value.scrollHeight
    }
  })
}

function aiKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    aiAsk()
  }
}

async function aiAsk() {
  const ctx = aiInput.value.trim()
  if (!ctx || aiStreaming.value) return
  
  aiInput.value = ''
  const ix = messages.value.length
  messages.value.push({ role: 'user', text: ctx, blocks: [] })
  messages.value.push({ role: 'assistant', text: '', streaming: true, blocks: [] })
  aiStreaming.value = true
  aiStatus.value = 'thinking'
  aiScroll()

  try {
    const hist = messages.value.slice(0, ix).filter(m => m.text && !m.streaming).slice(-8)
      .map(m => ({ role: m.role, content: m.text }))
    const res = await fetch('/ai/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ messages: [...hist, { role: 'user', content: ctx }] }),
    })
    const msg = messages.value[ix + 1]
    if (!msg) return
    if (!res.ok) { msg.text = `错误 ${res.status}`; msg.streaming = false; aiStreaming.value = false; aiStatus.value = 'online'; return }

    const reader = res.body!.getReader()
    const dec = new TextDecoder()
    let buf = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buf += dec.decode(value, { stream: true })
      const lines = buf.split('\n')
      buf = lines.pop() ?? ''
      for (const line of lines) {
        if (!line.startsWith('data: ')) continue
        const s = line.slice(6).trim()
        if (!s) continue
        try {
          const d = JSON.parse(s)
          if (d.done) break
          if (d.content) { msg.text += d.content; msg.blocks = extractCodeBlocks(msg.text); aiScroll() }
        } catch { /* ignore */ }
      }
    }
    msg.streaming = false
    msg.blocks = extractCodeBlocks(msg.text)
  } catch (e: unknown) {
    const msg = messages.value[ix + 1]
    if (msg) {
      msg.text = `连接失败: ${(e as Error).message}`
      msg.streaming = false
    }
  }
  aiStreaming.value = false
  aiStatus.value = 'online'
  aiScroll()
}

function clearMessages() {
  messages.value = []
}

function applyCode(code: string) {
  codeContent.value = code
  diffVisible.value = false
}

function applyDiff() {
  // Apply diff changes to code
  diffVisible.value = false
}

onMounted(() => {
  loadTask()
})

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
})
</script>

<style scoped>
.ide-root {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px);
  background: #f1f5f9;
}

/* ── Header ──────────────────────────────────────────────────────────────── */
.hdr {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.hdr-left, .hdr-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hdr-center {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hdr-back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.hdr-back:hover {
  background: #f1f5f9;
  color: #334155;
}

.hdr-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1e293b;
}

.hdr-title {
  font-size: 14px;
}

.hdr-lang-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 20px;
  font-size: 12px;
  color: #64748b;
}

.lang-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.hdr-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: none;
  border-radius: 8px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.hdr-btn:hover {
  background: #e2e8f0;
  color: #334155;
}

.hdr-btn.run {
  background: #22c55e;
  color: #fff;
}

.hdr-btn.run:hover:not(:disabled) {
  background: #16a34a;
}

.hdr-btn.complete {
  background: #6366f1;
  color: #fff;
}

.hdr-btn.complete:hover:not(:disabled) {
  background: #4f46e5;
}

.hdr-btn.active {
  background: #e0e7ff;
  color: #6366f1;
}

.hdr-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ai-status-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
}

.ai-status-pill.online {
  background: #dcfce7;
  color: #16a34a;
}

.ai-status-pill.thinking {
  background: #fef3c7;
  color: #d97706;
}

.ai-status-pill.offline {
  background: #fee2e2;
  color: #dc2626;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

/* ── Body ────────────────────────────────────────────────────────────────── */
.body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ── Editor Pane ─────────────────────────────────────────────────────────── */
.editor-pane {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-right: 1px solid #e2e8f0;
  transition: width 0.3s;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.code-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.code-title {
  font-size: 13px;
  color: #475569;
}

.code-lang-badge {
  padding: 2px 8px;
  background: #e0e7ff;
  border-radius: 4px;
  font-size: 11px;
  color: #6366f1;
}

.code-header-right {
  display: flex;
  gap: 4px;
}

.code-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s;
}

.code-action-btn:hover {
  background: #e2e8f0;
  color: #64748b;
}

.code-area {
  flex: 1;
  min-height: 0;
}

/* ── Diff Panel ──────────────────────────────────────────────────────────── */
.diff-panel {
  display: none;
  padding: 12px;
  background: #fffbeb;
  border-top: 1px solid #fef3c7;
}

.diff-panel.visible {
  display: block;
}

.diff-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.diff-title {
  font-size: 13px;
  font-weight: 500;
  color: #92400e;
}

.diff-actions {
  display: flex;
  gap: 8px;
}

.diff-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border: none;
  border-radius: 6px;
  background: #f59e0b;
  color: #fff;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.diff-btn:hover {
  background: #d97706;
}

.diff-btn:last-child {
  background: #e2e8f0;
  color: #64748b;
}

.diff-btn:last-child:hover {
  background: #cbd5e1;
}

.diff-content {
  font-family: 'Cascadia Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
}

/* ── Terminal Area ───────────────────────────────────────────────────────── */
.term-area {
  height: 180px;
  border-top: 1px solid #e2e8f0;
}

/* ── AI Panel ────────────────────────────────────────────────────────────── */
.ai-panel {
  display: none;
  flex-direction: column;
  width: 35%;
  background: #fff;
}

.ai-panel.visible {
  display: flex;
}

.ai-panel-resizer {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  cursor: col-resize;
  background: transparent;
}

.ai-panel-resizer:hover {
  background: #cbd5e1;
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #334155;
}

.ai-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.2s;
}

.ai-clear:hover {
  background: #e2e8f0;
  color: #64748b;
}

/* ── AI Messages ─────────────────────────────────────────────────────────── */
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.ai-msg {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.ai-msg.user {
  flex-direction: row-reverse;
}

.ai-msg.user .ai-bd {
  background: #6366f1;
  color: #fff;
  border-radius: 12px 12px 4px 12px;
}

.ai-av {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #e2e8f0;
  font-size: 14px;
}

.ai-bd {
  max-width: 85%;
  padding: 10px 12px;
  background: #f1f5f9;
  border-radius: 12px 12px 12px 4px;
}

.ai-msg-text {
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-msg-text :deep(.ai-ic) {
  padding: 2px 4px;
  background: rgba(99, 102, 241, 0.1);
  border-radius: 3px;
  font-family: 'Cascadia Code', monospace;
  font-size: 12px;
}

.ai-msg-text :deep(strong) {
  font-weight: 600;
}

.ai-msg-text :deep(h2), .ai-msg-text :deep(h3), .ai-msg-text :deep(h4) {
  margin: 8px 0;
  font-weight: 600;
}

.ai-msg-text :deep(h2) { font-size: 16px; }
.ai-msg-text :deep(h3) { font-size: 14px; }
.ai-msg-text :deep(h4) { font-size: 13px; }

.ai-msg-text :deep(ul) {
  margin: 8px 0;
  padding-left: 20px;
}

.ai-msg-text :deep(li) {
  margin: 4px 0;
}

.ai-msg-blocks {
  margin-top: 10px;
}

.ai-mcb {
  margin-top: 8px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.ai-mcb-h {
  padding: 4px 8px;
  background: #f8fafc;
  font-size: 11px;
  color: #64748b;
  border-bottom: 1px solid #e2e8f0;
}

.ai-mcb pre {
  margin: 0;
  padding: 8px;
  background: #1e293b;
  overflow-x: auto;
}

.ai-mcb code {
  font-family: 'Cascadia Code', 'Consolas', monospace;
  font-size: 12px;
  color: #e2e8f0;
  line-height: 1.5;
}

.ai-mcb-apply {
  display: block;
  width: 100%;
  padding: 6px;
  background: #6366f1;
  color: #fff;
  border: none;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.ai-mcb-apply:hover {
  background: #4f46e5;
}

.ai-cursor {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.ai-thinking {
  display: flex;
  gap: 4px;
}

.ai-thinking span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #94a3b8;
  animation: thinking 1.4s infinite ease-in-out;
}

.ai-thinking span:nth-child(1) { animation-delay: -0.32s; }
.ai-thinking span:nth-child(2) { animation-delay: -0.16s; }
.ai-thinking span:nth-child(3) { animation-delay: 0s; }

@keyframes thinking {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

/* ── AI Input ────────────────────────────────────────────────────────────── */
.ai-input-area {
  padding: 12px;
  border-top: 1px solid #e2e8f0;
}

.ai-input-row {
  display: flex;
  gap: 8px;
  padding: 6px;
  background: #f8fafc;
  border-radius: 12px;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.ai-input-row.focused {
  background: #fff;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.ai-input {
  flex: 1;
  min-height: 44px;
  padding: 8px;
  border: none;
  background: transparent;
  font-family: inherit;
  font-size: 13px;
  resize: none;
  outline: none;
}

.ai-input::placeholder {
  color: #94a3b8;
}

.ai-send-btn {
  align-self: flex-end;
  padding: 8px 12px;
  border: none;
  border-radius: 8px;
  background: #6366f1;
  color: #fff;
  cursor: pointer;
  transition: all 0.2s;
}

.ai-send-btn:hover:not(:disabled) {
  background: #4f46e5;
}

.ai-send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ai-input-hint {
  margin-top: 6px;
  font-size: 11px;
  color: #94a3b8;
  text-align: center;
}
</style>
