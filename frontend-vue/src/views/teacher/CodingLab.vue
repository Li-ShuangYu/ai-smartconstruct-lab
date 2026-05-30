<template>
  <div class="coding-lab">
    <!-- Header -->
    <header class="cl-header">
      <div class="cl-header-left">
        <button class="cl-back-btn" @click="handleBack">← 返回</button>
        <h1 class="cl-title">{{ pageTitle }}</h1>
        <n-tag v-if="taskTopic" type="info" size="small" bordered>{{ taskTopic }}</n-tag>
        <n-tag v-if="language" type="success" size="small" bordered>{{ language }}</n-tag>
      </div>
      <div class="cl-header-right">
        <span class="ai-status-dot" :class="aiOnline ? 'online' : 'offline'"></span>
        <span class="ai-status-text">{{ aiOnline ? 'AI 已连接' : 'AI 离线' }}</span>
        <button class="cl-action-btn" @click="runCode" :disabled="running">
          {{ running ? '运行中...' : '▶ 运行代码' }}
        </button>
        <button class="cl-action-btn secondary" @click="showAiPanel = !showAiPanel">💬 AI 助手</button>
      </div>
    </header>

    <!-- Loading -->
    <n-spin :show="loading" class="cl-body">
      <div v-if="!loading && !hasData" class="cl-empty">
        <p>暂无编码实训数据</p>
        <p class="cl-empty-hint" v-if="aiStatus === 1">⏳ AI 正在处理中，请稍后再查看</p>
        <p class="cl-empty-hint" v-else-if="aiStatus === 0">⏳ 请先发布模板触发 AI 处理</p>
        <p class="cl-empty-hint" v-else-if="aiStatus === 3">⚠️ AI 处理失败，请重试</p>
      </div>

      <div v-if="!loading && hasData" class="cl-main">
        <!-- Task Intro Panel -->
        <div class="cl-task-panel" v-if="taskDescription">
          <div class="cl-task-header">
            <span class="cl-task-badge">📋 实训任务</span>
            <button class="cl-toggle-btn" @click="showTask = !showTask">{{ showTask ? '收起' : '展开' }}</button>
          </div>
          <div v-if="showTask" class="cl-task-body">
            <div class="cl-task-desc">{{ taskDescription }}</div>
            <div v-if="hints.length" class="cl-task-hints">
              <div class="hints-title">💡 提示</div>
              <div v-for="(h, i) in hints" :key="i" class="hint-item">{{ i + 1 }}. {{ h }}</div>
            </div>
            <div v-if="testCases.length" class="cl-task-tests">
              <div class="tests-title">🧪 测试用例</div>
              <div v-for="(tc, i) in testCases" :key="i" class="test-item">
                <span class="test-label">{{ tc.description || '用例' + (i + 1) }}</span>
                <span class="test-input">输入: {{ tc.input || tc.params || '—' }}</span>
                <span class="test-expected">期望: {{ tc.expected || tc.output || '—' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="cl-editor-area">
          <!-- Code Editor -->
          <div class="cl-editor">
            <div class="cl-editor-toolbar">
              <span class="cl-editor-title">代码编辑器</span>
              <span class="cl-lang-badge">{{ language || 'python' }}</span>
            </div>
            <textarea
              v-model="code"
              class="cl-code-textarea"
              :placeholder="'# 在此编写' + (language || 'Python') + '代码...'"
              spellcheck="false"
            ></textarea>
          </div>

          <!-- Terminal -->
          <div class="cl-terminal">
            <div class="cl-terminal-header">
              <span>📟 终端输出</span>
              <button class="cl-clear-btn" @click="clearTerminal">清除</button>
            </div>
            <div class="cl-terminal-body" ref="terminalRef">
              <div v-for="(line, i) in terminalLines" :key="i" class="terminal-line" :class="line.type">
                <span class="line-prefix">{{ line.type === 'error' ? '✘' : line.type === 'info' ? 'ℹ' : '>' }}</span>
                <span class="line-text">{{ line.text }}</span>
              </div>
              <div v-if="terminalLines.length === 0" class="terminal-placeholder">点击「运行代码」查看输出</div>
            </div>
          </div>
        </div>

        <!-- AI Assistant Panel -->
        <transition name="panel-slide">
          <div v-if="showAiPanel" class="cl-ai-panel">
            <div class="cl-ai-header">
              <span>🤖 AI 编程助手</span>
              <button class="cl-close-btn" @click="showAiPanel = false">✕</button>
            </div>
            <div class="cl-ai-messages" ref="aiMessagesRef">
              <div v-for="(msg, i) in aiMessages" :key="i" class="ai-msg" :class="msg.role">
                <div class="ai-msg-header">{{ msg.role === 'assistant' ? 'AI' : '我' }}</div>
                <div class="ai-msg-content" v-html="renderAiContent(msg.content)"></div>
              </div>
              <div v-if="aiStreaming" class="ai-msg assistant">
                <div class="ai-msg-header">AI</div>
                <div class="ai-msg-content typing-indicator">...</div>
              </div>
            </div>
            <div class="cl-ai-input-bar">
              <textarea
                v-model="aiInput"
                class="ai-input"
                placeholder="向 AI 提问..."
                rows="2"
                @keydown.enter.prevent="sendAiMessage"
              ></textarea>
              <button class="ai-send-btn" @click="sendAiMessage" :disabled="!aiInput.trim() || aiStreaming">发送</button>
            </div>
          </div>
        </transition>
      </div>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { getTemplatePreview } from '@/services/modules/orchestration.service'
import http from '@/services/api'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const templateId = computed(() => String(route.params.templateId))
const nodeId = computed(() => String(route.params.nodeId))

const loading = ref(true)
const pageTitle = ref('编码实训')
const taskTopic = ref('')
const language = ref('python')
const aiStatus = ref(0)
const code = ref('')
const taskDescription = ref('')
const hints = ref<string[]>([])
const testCases = ref<any[]>([])
const showTask = ref(true)
const hasData = ref(false)

// Terminal
const terminalLines = ref<{ text: string; type: string }[]>([])
const terminalRef = ref<HTMLElement | null>(null)
const running = ref(false)

// AI Panel
const showAiPanel = ref(true)
const aiOnline = ref(true)
const aiInput = ref('')
const aiMessages = ref<{ role: string; content: string }[]>([])
const aiStreaming = ref(false)
const aiMessagesRef = ref<HTMLElement | null>(null)

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getTemplatePreview(templateId.value)
    if (res.code === 200) {
      const preview = res.data
      aiStatus.value = preview.ai_status || 0
      pageTitle.value = (preview.template_name || '编码实训') + ' - 编码实训'

      for (const phase of preview.phases || []) {
        const node = (phase.nodes || []).find((n: any) => n.node_id === nodeId.value)
        if (node) {
          const content = node.ai_generated_content as any
          const summary = node.config_summary as any
          taskTopic.value = summary?.topic || ''
          language.value = 'python'

          if (content) {
            const task = content.coding_task || content
            code.value = task.code_template || task.template || ''
            taskDescription.value = task.description || task.desc || task.title || ''
            hints.value = task.hints || []
            testCases.value = task.test_cases || task.tests || []
            language.value = task.language || 'python'
            hasData.value = true
          }
          break
        }
      }
      if (!hasData.value) {
        hasData.value = aiStatus.value >= 2
      }
    } else {
      message.error(res.message || '获取数据失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message || '获取数据失败')
  } finally {
    loading.value = false
  }
}

// Terminal
function addTerminalLine(text: string, type: string = 'info') {
  terminalLines.value.push({ text, type })
  nextTick(() => {
    if (terminalRef.value) {
      terminalRef.value.scrollTop = terminalRef.value.scrollHeight
    }
  })
}
function clearTerminal() { terminalLines.value = [] }

async function runCode() {
  if (!code.value.trim()) {
    addTerminalLine('代码不能为空', 'error')
    return
  }
  running.value = true
  addTerminalLine('正在运行代码...', 'info')
  try {
    const res = await http.post('/api/execute-code', {
      code: code.value,
      language: language.value
    })
    const data = res.data
    if (data.output) {
      data.output.split('\n').forEach((line: string) => addTerminalLine(line, 'stdout'))
    }
    if (data.error) {
      data.error.split('\n').forEach((line: string) => addTerminalLine(line, 'error'))
    }
    addTerminalLine('运行完成 (exit: 0)', 'info')
  } catch (e: any) {
    addTerminalLine(e?.response?.data?.message || '运行失败', 'error')
  } finally {
    running.value = false
  }
}

// AI Chat
async function sendAiMessage() {
  if (!aiInput.value.trim() || aiStreaming.value) return
  const msg = aiInput.value.trim()
  aiInput.value = ''
  aiMessages.value.push({ role: 'user', content: msg })
  aiStreaming.value = true
  scrollAiMessages()

  try {
    const context = `当前代码:\n\`\`\`${language.value}\n${code.value}\n\`\`\`\n\n${msg}`
    const res = await http.post('/api/ai/chat', {
      messages: [
        { role: 'system', content: '你是一个编程实训助教。帮助学生理解代码、调试错误、优化代码。' },
        { role: 'user', content: context }
      ]
    })
    const data = res.data
    if (data.choices && data.choices[0]) {
      aiMessages.value.push({ role: 'assistant', content: data.choices[0].message?.content || '(无响应)' })
    } else if (data.content) {
      aiMessages.value.push({ role: 'assistant', content: data.content })
    } else {
      aiMessages.value.push({ role: 'assistant', content: 'AI 暂时无法响应，请重试' })
    }
  } catch (e: any) {
    aiMessages.value.push({ role: 'assistant', content: '⚠️ AI 请求失败: ' + (e?.response?.data?.message || '网络错误') })
  } finally {
    aiStreaming.value = false
    scrollAiMessages()
  }
}

function scrollAiMessages() {
  nextTick(() => {
    if (aiMessagesRef.value) {
      aiMessagesRef.value.scrollTop = aiMessagesRef.value.scrollHeight
    }
  })
}

function renderAiContent(content: string): string {
  if (!content) return ''
  // Simple code block rendering
  return content
    .replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre class="ai-code-block"><code>$2</code></pre>')
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
}

function handleBack() {
  router.push(`/teacher/template-preview/${templateId.value}`)
}
</script>

<style scoped>
.coding-lab {
  height: 100vh; display: flex; flex-direction: column;
  background: #0F172A; color: #E2E8F0; font-family: system-ui, sans-serif;
}
.cl-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 24px; background: #1E293B; border-bottom: 1px solid #334155; flex-shrink: 0;
}
.cl-header-left { display: flex; align-items: center; gap: 12px; }
.cl-back-btn {
  background: #334155; color: #94A3B8; border: none; padding: 6px 14px;
  border-radius: 6px; cursor: pointer; font-size: 13px; font-weight: 600;
}
.cl-back-btn:hover { background: #475569; color: #E2E8F0; }
.cl-title { font-size: 18px; font-weight: 700; margin: 0; }
.cl-header-right { display: flex; align-items: center; gap: 12px; }
.ai-status-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.ai-status-dot.online { background: #22C55E; }
.ai-status-dot.offline { background: #EF4444; }
.ai-status-text { font-size: 12px; color: #94A3B8; }
.cl-action-btn {
  background: #4F46E5; color: #FFF; border: none; padding: 6px 16px;
  border-radius: 6px; cursor: pointer; font-size: 13px; font-weight: 600;
  transition: 0.2s;
}
.cl-action-btn:hover:not(:disabled) { background: #4338CA; }
.cl-action-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.cl-action-btn.secondary { background: #334155; color: #E2E8F0; }
.cl-action-btn.secondary:hover { background: #475569; }

.cl-body { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.cl-empty { text-align: center; padding: 80px 20px; color: #64748B; }
.cl-empty-hint { font-size: 13px; margin-top: 8px; }

.cl-main { flex: 1; display: flex; overflow: hidden; }
.cl-task-panel { background: #1E293B; border-bottom: 1px solid #334155; flex-shrink: 0; }
.cl-task-header { display: flex; justify-content: space-between; align-items: center; padding: 8px 16px; }
.cl-task-badge { font-size: 13px; font-weight: 700; color: #60A5FA; }
.cl-toggle-btn { background: none; border: none; color: #94A3B8; cursor: pointer; font-size: 12px; }
.cl-task-body { padding: 0 16px 16px; }
.cl-task-desc { font-size: 14px; color: #CBD5E1; line-height: 1.6; white-space: pre-wrap; }
.cl-task-hints, .cl-task-tests { margin-top: 12px; }
.hints-title, .tests-title { font-size: 13px; font-weight: 700; color: #FBBF24; margin-bottom: 8px; }
.hint-item { font-size: 13px; color: #94A3B8; padding: 4px 0; }
.test-item { display: flex; gap: 12px; font-size: 12px; padding: 6px; background: #0F172A; border-radius: 4px; margin-bottom: 4px; }
.test-label { color: #34D399; font-weight: 600; }
.test-input, .test-expected { color: #94A3B8; }

.cl-editor-area { flex: 1; display: flex; flex-direction: column; }
.cl-editor { flex: 1; display: flex; flex-direction: column; }
.cl-editor-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 6px 16px; background: #1E293B; border-bottom: 1px solid #334155; }
.cl-editor-title { font-size: 12px; color: #94A3B8; font-weight: 600; text-transform: uppercase; }
.cl-lang-badge { font-size: 11px; background: #1A3A2A; color: #34D399; padding: 2px 8px; border-radius: 4px; }
.cl-code-textarea {
  flex: 1; background: #0F172A; color: #E2E8F0; border: none; outline: none;
  padding: 16px; font-family: 'Consolas', 'Monaco', monospace; font-size: 14px; line-height: 1.6;
  resize: none; tab-size: 2;
}
.cl-code-textarea::placeholder { color: #475569; }

.cl-terminal { height: 180px; background: #0F172A; border-top: 1px solid #334155; display: flex; flex-direction: column; }
.cl-terminal-header { display: flex; justify-content: space-between; align-items: center; padding: 6px 16px; background: #1E293B; font-size: 12px; color: #94A3B8; }
.cl-clear-btn { background: none; border: none; color: #EF4444; cursor: pointer; font-size: 12px; }
.cl-terminal-body { flex: 1; overflow-y: auto; padding: 8px 16px; font-family: 'Consolas', monospace; font-size: 13px; }
.terminal-line { display: flex; gap: 8px; padding: 2px 0; }
.line-prefix { color: #64748B; font-weight: bold; width: 16px; }
.terminal-line.error .line-text { color: #EF4444; }
.terminal-line.stdout .line-text { color: #E2E8F0; }
.terminal-line.info .line-text { color: #60A5FA; }
.terminal-placeholder { color: #475569; font-style: italic; }

.cl-ai-panel { width: 360px; background: #1E293B; border-left: 1px solid #334155; display: flex; flex-direction: column; flex-shrink: 0; }
.cl-ai-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid #334155; font-weight: 600; font-size: 14px; }
.cl-close-btn { background: none; border: none; color: #94A3B8; cursor: pointer; font-size: 16px; }
.cl-ai-messages { flex: 1; overflow-y: auto; padding: 12px; }
.ai-msg { margin-bottom: 12px; }
.ai-msg-header { font-size: 11px; font-weight: 700; color: #64748B; margin-bottom: 4px; text-transform: uppercase; }
.ai-msg.user .ai-msg-header { color: #60A5FA; }
.ai-msg.assistant .ai-msg-header { color: #34D399; }
.ai-msg-content { font-size: 14px; line-height: 1.5; color: #CBD5E1; }
.ai-msg-content :deep(.ai-code-block) { background: #0F172A; padding: 12px; border-radius: 6px; overflow-x: auto; margin: 4px 0; }
.ai-msg-content :deep(.ai-code-block code) { font-family: 'Consolas', monospace; font-size: 13px; color: #E2E8F0; }
.typing-indicator { color: #475569; }
.cl-ai-input-bar { display: flex; padding: 8px; gap: 8px; border-top: 1px solid #334155; }
.ai-input { flex: 1; background: #0F172A; color: #E2E8F0; border: 1px solid #334155; border-radius: 6px; padding: 8px 12px; font-family: inherit; font-size: 13px; resize: none; outline: none; }
.ai-input:focus { border-color: #4F46E5; }
.ai-send-btn { background: #4F46E5; color: #FFF; border: none; border-radius: 6px; padding: 0 16px; cursor: pointer; font-weight: 600; }
.ai-send-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.panel-slide-enter-active, .panel-slide-leave-active { transition: all 0.3s ease; }
.panel-slide-enter-from, .panel-slide-leave-to { width: 0 !important; opacity: 0; padding: 0; overflow: hidden; }
</style>