<template>
  <div class="ide-root">
    <!-- ── 启动弹窗 ── -->
    <div v-if="showStartModal" class="modal-overlay" @click.self="showStartModal = false">
      <div class="modal-box">
        <div class="modal-icon">
          <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="#6366f1" stroke-width="2"><path d="M12 2l2 7h7l-5.5 4 2 7L12 16l-5.5 4 2-7L3 9h7z"/></svg>
        </div>
        <h2>AI 编程实训助手</h2>
        <p>你想编写什么类型的代码？请告诉我你的需求：</p>
        <textarea v-model="userRequest" placeholder="例如：我想学习如何实现一个排序算法、写一个简单的计算器..." />
        <div class="modal-lang-row">
          <span class="modal-lang-label">运行环境：</span>
          <div class="lang-pills">
            <button v-for="l in langOptions" :key="l.value"
              class="lang-pill" :class="{ active: language === l.value }"
              @click="language = l.value">
              {{ l.label }}
            </button>
          </div>
        </div>
        <div class="modal-btns">
          <button class="btn-cancel" @click="router.push('/student/workbench')">返回</button>
          <button class="btn-confirm" :disabled="!userRequest.trim()" @click="startSession">
            开始实训
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- ── 顶部栏 ── -->
    <header class="hdr" v-if="sessionStarted">
      <div class="hdr-left">
        <button class="hdr-back" @click="resetSession" title="重新开始">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5m7-7-7 7 7 7"/></svg>
        </button>
        <div class="hdr-brand">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="#6366f1"><path d="M12 2l2 7h7l-5.5 4 2 7L12 16l-5.5 4 2-7L3 9h7z"/></svg>
          <span class="hdr-title">AI 编程助手</span>
        </div>
        <div class="hdr-lang-badge">
          <span class="lang-dot" :style="{ background: langColor }"></span>
          {{ langLabel }}
        </div>
      </div>
      <div class="hdr-center">
        <button class="hdr-btn run" @click="runCode" :disabled="running">
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
    <div class="body" ref="bodyRef" v-if="sessionStarted">
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
              <button class="diff-btn revert" @click="revertDiff">撤销</button>
              <button class="diff-btn apply" @click="acceptDiff">应用</button>
            </div>
          </div>
          <div class="diff-body">
            <div class="diff-col">
              <div class="diff-col-hdr">改前</div>
              <pre class="diff-pre">{{ diffOrig }}</pre>
            </div>
            <div class="diff-col">
              <div class="diff-col-hdr diff-col-hdr--new">改后</div>
              <pre class="diff-pre diff-pre--new">{{ diffMod }}</pre>
            </div>
          </div>
        </div>

        <!-- Terminal 组件 -->
        <TerminalPanel
          :lines="terminalLines"
          :running="running"
          :exit-code="lastExitCode"
          @clear="clearTerminal"
          @send-to-ai="addTerminalToAI"
        />
      </div>

      <!-- 分割条 -->
      <div class="resize-bar" v-if="showAiPanel" @mousedown="resizeStart"></div>

      <!-- 右侧：AI 面板 -->
      <div class="ai-pane" :style="{ width: (100 - leftPct) + '%' }" v-show="showAiPanel">
        <div class="ai-header">
          <span class="ai-header-title">AI 编程导师</span>
          <button class="ai-clear-btn" @click="clearChat" title="清空对话">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6"/></svg>
          </button>
        </div>

        <div class="ai-messages" ref="aiMsgRef">
          <div v-if="!messages.length" class="ai-empty">
            <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="#c7d2fe" stroke-width="1.5"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
            <p>有编程问题？AI 导师帮你思考</p>
            <div class="ai-chips">
              <button class="ai-chip" @click="aiAsk('解释当前代码的原理')">解释代码</button>
              <button class="ai-chip" @click="aiAsk('帮我优化这段代码')">优化代码</button>
              <button class="ai-chip" @click="aiAsk('检查代码中的问题')">检查问题</button>
              <button class="ai-chip" @click="aiAsk('这段代码如何测试？')">测试建议</button>
            </div>
          </div>

          <div v-for="(m, i) in messages" :key="i" class="ai-msg" :class="m.role">
            <div class="ai-av">{{ m.role === 'user' ? '👤' : '🤖' }}</div>
            <div class="ai-bd">
              <div class="ai-txt" v-html="renderMd(m.text)"></div>
              <div v-for="(b, bi) in m.blocks" :key="bi" class="ai-code-block">
                <div class="ai-code-block-hdr">
                  <span class="ai-code-lang">{{ b.lang || 'code' }}</span>
                  <div class="ai-code-actions">
                    <button class="ai-code-btn" @click="copyText(b.code)">复制</button>
                    <button class="ai-code-btn primary" @click="applyAICode(b.code)">应用到编辑器</button>
                  </div>
                </div>
                <pre class="ai-code-pre"><code>{{ b.code }}</code></pre>
              </div>
              <span v-if="m.streaming" class="ai-cursor">▊</span>
            </div>
          </div>
        </div>

        <div class="ai-input-area">
          <div class="ai-input-row" :class="{ focused: aiInputFocused }">
            <textarea ref="aiInputRef" v-model="aiInput" class="ai-input"
              placeholder="输入问题，或点击终端输出的 ↗ 发给 AI…"
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
import { useRouter } from 'vue-router'
import CodeEditor from './CodeEditor.vue'
import TerminalPanel from './TerminalPanel.vue'
import type { TermLine } from './TerminalPanel.vue'

const router = useRouter()

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

// ── Session state ────────────────────────────────────────────────────────────
const showStartModal = ref(true)
const sessionStarted = ref(false)
const userRequest    = ref('')
const codeContent    = ref('')

// ── CodeEditor ref ──────────────────────────────────────────────────────────
const editorRef = ref<InstanceType<typeof CodeEditor> | null>(null)

function getCode(): string {
  return codeContent.value
}
function setCode(code: string) {
  codeContent.value = code
}

// ── Terminal ─────────────────────────────────────────────────────────────────
const terminalLines = ref<TermLine[]>([])
const termRef       = ref<HTMLDivElement | null>(null)
const running       = ref(false)
const lastExitCode  = ref<number | null>(null)

function termAppend(text: string, type: TermLine['type'] = 'out') {
  terminalLines.value.push({ text, type })
  if (terminalLines.value.length > 500) terminalLines.value = terminalLines.value.slice(-300)
}
function clearTerminal() { terminalLines.value = []; lastExitCode.value = null }

// ── AI chat ──────────────────────────────────────────────────────────────────
interface AiMsg { role: 'user' | 'assistant'; text: string; streaming?: boolean; blocks: { lang: string; code: string }[] }
const messages      = ref<AiMsg[]>([])
const aiInput       = ref('')
const aiStreaming    = ref(false)
const aiStatus      = ref<'checking' | 'online' | 'offline'>('checking')
const aiMsgRef      = ref<HTMLDivElement | null>(null)
const aiInputRef    = ref<HTMLTextAreaElement | null>(null)
const aiInputFocused = ref(false)

const aiStatusLabel = computed(() => {
  switch (aiStatus.value) {
    case 'online': return 'AI 在线'
    case 'offline': return 'AI 离线'
    default: return '检测中…'
  }
})

// ── Layout ───────────────────────────────────────────────────────────────────
const showAiPanel = ref(true)
const leftPct     = ref(60)
const bodyRef     = ref<HTMLDivElement | null>(null)

// ── Diff ─────────────────────────────────────────────────────────────────────
const diffVisible = ref(false)
const diffOrig    = ref('')
const diffMod     = ref('')

// ── Session start ────────────────────────────────────────────────────────────
async function startSession() {
  if (!userRequest.value.trim()) return
  const request = userRequest.value.trim()
  showStartModal.value = false
  sessionStarted.value = true

  const prompt = `你是一位编程导师。学生想学习的内容是：「${request}」

请按以下格式回复：

## 代码
用 ${language.value} 语言生成一个完整的、可直接运行的入门代码示例。要求：
- 代码有清晰的注释说明关键概念
- 包含可运行的测试/演示代码
- 适合初学者理解

## 引导
用 3-5 个问题或提示引导学生思考这段代码的关键概念、运行结果和改进方向。语气要鼓励性、启发式，不要直接给答案。`

  messages.value = []
  aiStreaming.value = true
  // 先插入一个空的 assistant 消息用于流式输出
  const msgIndex = messages.value.length
  messages.value.push({ role: 'assistant', text: '', streaming: true, blocks: [] })

  try {
    const res = await fetch('/ai/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ messages: [{ role: 'user', content: prompt }] }),
    })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    const reader = res.body!.getReader()
    const dec = new TextDecoder()
    let fullText = ''; let buf = ''
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
          if (d.content) {
            fullText += d.content
            // 流式更新 AI 面板中的消息
            const m = messages.value[msgIndex]
            if (m) {
              m.text = fullText
              m.blocks = extractCodeBlocks(fullText)
              aiScroll()
            }
          }
        } catch { /* ignore */ }
      }
    }

    // Parse code block
    let aiCode = ''
    const codeMatch = fullText.match(/##\s*代码[\s\S]*?```\w*\n([\s\S]*?)```/)
    if (codeMatch) aiCode = codeMatch[1]?.trim() ?? ''
    else { const any = fullText.match(/```\w*\n([\s\S]*?)```/); if (any) aiCode = any[1]?.trim() ?? '' }

    // Parse guidance
    let guidance = ''
    const gMatch = fullText.match(/##\s*引导([\s\S]*)$/)
    if (gMatch && gMatch[1]) guidance = gMatch[1].trim()
    else guidance = '让我们开始探索这段代码吧！\n\n1. 先运行看看输出结果\n2. 尝试理解每一行代码的作用\n3. 思考如何修改或扩展功能\n\n有什么问题随时问我！'

    setCode(aiCode || `# 关于「${request}」的学习示例\n# 请在右侧 AI 面板查看引导\n`)
    // 流结束后替换为最终格式的消息
    messages.value = [
      { role: 'assistant', text: `好的！你想学习：**${request}**。代码已生成，请查看左侧编辑器。`, blocks: [] },
      { role: 'assistant', text: guidance, blocks: extractCodeBlocks(guidance) },
    ]
  } catch {
    setCode(`# 关于「${request}」的学习示例\n# （AI 服务暂不可用，以下为基础模板）\n\ndef main():\n    print("欢迎学习：${request}")\n    pass\n\nif __name__ == "__main__":\n    main()`)
    messages.value = [
      { role: 'assistant', text: `好的！你想学习：**${request}**。\n\n（AI 服务暂不可用，已为你准备了基础模板，可直接在编辑器中修改。）`, blocks: [] },
    ]
  }
  aiStreaming.value = false
  aiScroll()
}

// ── Run code ─────────────────────────────────────────────────────────────────
async function runCode() {
  if (running.value) return
  const src = getCode()
  if (!src.trim()) { termAppend('没有可运行的代码', 'error'); return }
  running.value = true; clearTerminal()
  termAppend(`▶ 运行 ${langLabel.value}…`, 'info')
  try {
    const res = await fetch('/ai/execute', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code: src, language: language.value, filename: 'main' }),
    })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const d = await res.json()
    if (d.stdout) for (const line of d.stdout.trimEnd().split('\n')) termAppend(line, 'out')
    if (d.stderr) for (const line of d.stderr.trimEnd().split('\n')) termAppend(line, 'error')
    lastExitCode.value = d.exit_code
    termAppend(d.exit_code === 0 ? '✓ 执行成功' : '✕ 执行失败', 'info')
  } catch (e: unknown) {
    termAppend(`错误: ${(e as Error).message}`, 'error')
    lastExitCode.value = 1
  }
  running.value = false

  // Auto-prompt AI after run
  if (messages.value.length > 0) {
    const lastLines = terminalLines.value.slice(-5).map(l => l.text).join('\n')
    messages.value.push({
      role: 'assistant',
      text: `代码运行完成了。\n\n运行结果：\n\`\`\`\n${lastLines}\n\`\`\`\n\n思考一下：\n1. 输出结果是否符合你的预期？\n2. 如果有错误，你觉得可能是什么原因？\n3. 有没有可以改进的地方？`,
      blocks: [],
    })
    aiScroll()
  }
}

function clearCode() { setCode('') }
function copyCode() { navigator.clipboard.writeText(getCode()).catch(() => {}) }

function resetSession() {
  showStartModal.value = true
  sessionStarted.value = false
  messages.value = []
  userRequest.value = ''
  codeContent.value = ''
  clearTerminal()
  diffVisible.value = false
}

// ── AI chat ──────────────────────────────────────────────────────────────────
function extractCodeBlocks(text: string) {
  const r: { lang: string; code: string }[] = []
  const re = /```(\w*)\n([\s\S]*?)```/g; let m
  while ((m = re.exec(text)) !== null) { if (m[1] !== undefined && m[2] !== undefined) r.push({ lang: m[1], code: m[2].trim() }) }
  return r
}

function esc(t: string) { return t.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;') }

function renderMd(text: string): string {
  if (!text) return ''
  let h = esc(text)
  const blocks: { lang: string; code: string }[] = []
  h = h.replace(/```(\w*)\n([\s\S]*?)```/g, (_, lang, code) => {
    const i = blocks.length; blocks.push({ lang: lang, code: code.trim() }); return `%%B${i}%%`
  })
  h = h.replace(/`([^`]+)`/g, '<code class="ai-ic">$1</code>')
  h = h.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  h = h.replace(/^### (.+)$/gm, '<h4>$1</h4>').replace(/^## (.+)$/gm, '<h3>$1</h3>').replace(/^# (.+)$/gm, '<h2>$1</h2>')
  h = h.replace(/^- (.+)$/gm, '<li>$1</li>').replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')
  h = h.replace(/\n\n/g, '</p><p>').replace(/\n/g, '<br>')
  h = h.replace(/%%B(\d+)%%/g, (_, i) => {
    const b = blocks[+i]; if (!b) return ''
    return `<div class="ai-mcb"><div class="ai-mcb-h">${b.lang || 'code'}</div><pre><code>${b.code}</code></pre></div>`
  })
  return '<p>' + h + '</p>'
}

async function checkAI() {
  try { const r = await fetch('/ai/health'); aiStatus.value = r.ok ? 'online' : 'offline' }
  catch { aiStatus.value = 'offline' }
}

function buildContext(msg: string): string {
  const src = getCode()
  return `## 当前代码（${langLabel.value}）\n\`\`\`${language.value}\n${src || '(空)'}\n\`\`\`\n\n## 学生提问\n${msg}`
}

async function aiAsk(text?: string) {
  const msg = (text ?? aiInput.value).trim()
  if (!msg || aiStreaming.value) return
  aiInput.value = ''
  const ctx = buildContext(msg)
  const ix = messages.value.length
  messages.value.push({ role: 'user', text: msg, blocks: [] })
  messages.value.push({ role: 'assistant', text: '', streaming: true, blocks: [] })
  aiStreaming.value = true; aiScroll()

  try {
    const hist = messages.value.slice(0, ix).filter(m => m.text && !m.role).slice(-8)
      .map(m => ({ role: m.role, content: m.text }))
    const res = await fetch('/ai/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ messages: [...hist, { role: 'user', content: ctx }] }),
    })
    if (!res.ok) { const errMsg = messages.value[ix + 1]; if (errMsg) { errMsg.text = `错误 ${res.status}`; errMsg.streaming = false }; aiStreaming.value = false; return }

    const reader = res.body!.getReader(); const dec = new TextDecoder(); let buf = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buf += dec.decode(value, { stream: true })
      const lines = buf.split('\n'); buf = lines.pop() ?? ''
      for (const line of lines) {
        if (!line.startsWith('data: ')) continue
        const s = line.slice(6).trim(); if (!s) continue
        try {
          const d = JSON.parse(s)
          if (d.done) break
          if (d.content) { const m = messages.value[ix + 1]; if (m) { m.text += d.content; m.blocks = extractCodeBlocks(m.text); aiScroll() } }
        } catch { /* ignore */ }
      }
    }
    const lastMsg = messages.value[ix + 1]; if (lastMsg) { lastMsg.streaming = false; lastMsg.blocks = extractCodeBlocks(lastMsg.text) }
  } catch (e: unknown) {
    const errMsg = messages.value[ix + 1]; if (errMsg) { errMsg.text = `连接失败: ${(e as Error).message}`; errMsg.streaming = false }
  }
  aiStreaming.value = false; aiScroll()
}

function addTerminalToAI(text: string) {
  aiInput.value = (aiInput.value ? aiInput.value + '\n' : '') + `[终端输出] ${text}`
  nextTick(() => aiInputRef.value?.focus())
}

function copyText(t: string) { navigator.clipboard.writeText(t).catch(() => {}) }
function clearChat() { messages.value = [] }
function aiKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); aiAsk() }
}
function aiScroll() { nextTick(() => { if (aiMsgRef.value) aiMsgRef.value.scrollTop = aiMsgRef.value.scrollHeight }) }

// ── Diff ─────────────────────────────────────────────────────────────────────
function applyAICode(newCode: string) {
  if (!newCode) return
  diffOrig.value = getCode(); diffMod.value = newCode
  setCode(newCode); diffVisible.value = true
}
function acceptDiff() { diffVisible.value = false; diffOrig.value = ''; diffMod.value = '' }
function revertDiff() { if (diffOrig.value) setCode(diffOrig.value); diffVisible.value = false; diffOrig.value = ''; diffMod.value = '' }

// ── Resize ───────────────────────────────────────────────────────────────────
function resizeStart(e: MouseEvent) {
  const sx = e.clientX; const sp = leftPct.value; const parent = bodyRef.value; if (!parent) return
  const w = parent.offsetWidth
  const mv = (ev: MouseEvent) => { leftPct.value = Math.max(35, Math.min(80, sp + ((ev.clientX - sx) / w) * 100)) }
  const up = () => { document.removeEventListener('mousemove', mv); document.removeEventListener('mouseup', up); document.body.style.cursor = ''; document.body.style.userSelect = '' }
  document.body.style.cursor = 'col-resize'; document.body.style.userSelect = 'none'
  document.addEventListener('mousemove', mv); document.addEventListener('mouseup', up)
}

// ── Lifecycle ────────────────────────────────────────────────────────────────
onMounted(() => {
  checkAI()
})
onBeforeUnmount(() => {
  // cleanup handled by CodeEditor component
})
</script>

<style scoped>
.ide-root {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* ── Modal ──────────────────────────────────────────────────────────────────── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-box {
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.12);
  color: #1e293b;
}

.modal-icon {
  text-align: center;
  margin-bottom: 16px;
}

.modal-box h2 {
  text-align: center;
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}

.modal-box p {
  text-align: center;
  margin: 0 0 16px;
  color: #64748b;
  font-size: 14px;
}

.modal-box textarea {
  width: 100%;
  height: 80px;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  color: #1e293b;
  font-size: 14px;
  resize: none;
  box-sizing: border-box;
  margin-bottom: 16px;
  font-family: inherit;
}

.modal-box textarea:focus {
  outline: none;
  border-color: #6366f1;
}

.modal-lang-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.modal-lang-label {
  font-size: 13px;
  color: #64748b;
}

.lang-pills {
  display: flex;
  gap: 8px;
}

.lang-pill {
  padding: 6px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  background: transparent;
  color: #64748b;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.lang-pill:hover {
  border-color: #6366f1;
  color: #6366f1;
}

.lang-pill.active {
  background: #6366f1;
  border-color: #6366f1;
  color: #fff;
}

.modal-btns {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel,
.btn-confirm {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: transparent;
  border: 1px solid #e2e8f0;
  color: #64748b;
}

.btn-cancel:hover {
  border-color: #cbd5e1;
  color: #475569;
}

.btn-confirm {
  background: #6366f1;
  border: none;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-confirm:hover:not(:disabled) {
  background: #4f46e5;
}

.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ── Header ─────────────────────────────────────────────────────────────────── */
.hdr {
  height: 48px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.hdr-left,
.hdr-center,
.hdr-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hdr-back {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.hdr-back:hover {
  background: #f1f5f9;
  color: #475569;
}

.hdr-brand {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hdr-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.hdr-lang-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  color: #475569;
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
  background: transparent;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  color: #64748b;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.hdr-btn:hover:not(:disabled) {
  background: #f1f5f9;
  color: #475569;
  border-color: #cbd5e1;
}

.hdr-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.hdr-btn.run {
  background: #22c55e;
  border-color: #22c55e;
  color: #fff;
}

.hdr-btn.run:hover:not(:disabled) {
  background: #16a34a;
}

.hdr-btn.active {
  background: #6366f1;
  border-color: #6366f1;
  color: #fff;
}

.ai-status-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.ai-status-pill.online {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
}

.ai-status-pill.offline {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.ai-status-pill.checking {
  background: rgba(251, 191, 36, 0.1);
  color: #d97706;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.ai-status-pill.online .status-dot { background: #22c55e; }
.ai-status-pill.offline .status-dot { background: #ef4444; }
.ai-status-pill.checking .status-dot { background: #f59e0b; }

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* ── Body ───────────────────────────────────────────────────────────────────── */
.body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.editor-pane {
  display: flex;
  flex-direction: column;
  background: #ffffff;
  overflow: hidden;
}

/* ── Code Editor ────────────────────────────────────────────────────────────── */
.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.code-header-left,
.code-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.code-title {
  font-size: 12px;
  font-weight: 600;
  color: #475569;
}

.code-lang-badge {
  font-size: 10px;
  font-weight: 500;
  padding: 2px 8px;
  background: #e0f2fe;
  color: #0284c7;
  border-radius: 4px;
}

.code-action-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.code-action-btn:hover {
  background: #e2e8f0;
  color: #475569;
}

.code-area {
  flex: 1;
  min-height: 200px;
  overflow: hidden;
}

/* ── Diff Panel ─────────────────────────────────────────────────────────────── */
.diff-panel {
  display: none;
  background: #ffffff;
  border-top: 1px solid #e2e8f0;
}

.diff-panel.visible {
  display: block;
}

.diff-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8fafc;
}

.diff-title {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
}

.diff-actions {
  display: flex;
  gap: 8px;
}

.diff-btn {
  padding: 4px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  background: transparent;
  color: #64748b;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}

.diff-btn:hover {
  background: #f1f5f9;
  color: #475569;
}

.diff-btn.apply {
  background: #6366f1;
  border-color: #6366f1;
  color: #fff;
}

.diff-btn.apply:hover {
  background: #4f46e5;
}

.diff-body {
  display: flex;
  gap: 1px;
  background: #e2e8f0;
}

.diff-col {
  flex: 1;
  padding: 12px;
  background: #ffffff;
}

.diff-col-hdr {
  font-size: 11px;
  color: #64748b;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e2e8f0;
  font-weight: 600;
}

.diff-col-hdr--new {
  color: #16a34a;
}

.diff-pre {
  margin: 0;
  font-family: 'Cascadia Code', 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  line-height: 1.5;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 150px;
  overflow-y: auto;
}

.diff-pre--new {
  color: #15803d;
}

/* ── Resize Bar ─────────────────────────────────────────────────────────────── */
.resize-bar {
  width: 4px;
  background: #e2e8f0;
  cursor: col-resize;
  transition: background 0.2s;
}

.resize-bar:hover {
  background: #6366f1;
}

/* ── AI Panel ───────────────────────────────────────────────────────────────── */
.ai-pane {
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-left: 1px solid #e2e8f0;
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.ai-header-title {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
}

.ai-clear-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.ai-clear-btn:hover {
  background: #f1f5f9;
  color: #475569;
}

.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: #fafafa;
}

.ai-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #94a3b8;
}

.ai-empty svg {
  margin-bottom: 16px;
}

.ai-empty p {
  margin: 0 0 16px;
  font-size: 14px;
  color: #64748b;
}

.ai-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.ai-chip {
  padding: 6px 12px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  color: #475569;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.ai-chip:hover {
  background: #6366f1;
  border-color: #6366f1;
  color: #fff;
}

.ai-msg {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.ai-msg.user {
  flex-direction: row-reverse;
}

.ai-av {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.ai-bd {
  flex: 1;
  max-width: calc(100% - 42px);
}

.ai-txt {
  font-size: 13px;
  line-height: 1.6;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-txt :deep(strong) {
  color: #6366f1;
  font-weight: 600;
}

.ai-txt :deep(code.ai-ic) {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-family: 'Cascadia Code', 'Consolas', monospace;
  color: #6366f1;
}

.ai-txt :deep(h2),
.ai-txt :deep(h3),
.ai-txt :deep(h4) {
  color: #0f172a;
  margin: 12px 0 8px;
}

.ai-txt :deep(ul) {
  margin: 8px 0;
  padding-left: 20px;
}

.ai-txt :deep(li) {
  margin-bottom: 4px;
}

.ai-code-block {
  margin-top: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.ai-code-block-hdr {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px;
  background: #f1f5f9;
  border-bottom: 1px solid #e2e8f0;
}

.ai-code-lang {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
}

.ai-code-actions {
  display: flex;
  gap: 6px;
}

.ai-code-btn {
  font-size: 10px;
  font-weight: 500;
  color: #64748b;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 3px;
}

.ai-code-btn:hover {
  background: #e2e8f0;
  color: #475569;
}

.ai-code-btn.primary {
  color: #6366f1;
}

.ai-code-pre {
  margin: 0;
  padding: 10px;
  font-family: 'Cascadia Code', 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  line-height: 1.5;
  color: #334155;
  overflow-x: auto;
}

.ai-cursor {
  display: inline-block;
  animation: blink 1s infinite;
  color: #6366f1;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

/* ── AI Input ───────────────────────────────────────────────────────────────── */
.ai-input-area {
  padding: 12px;
  border-top: 1px solid #e2e8f0;
  background: #ffffff;
}

.ai-input-row {
  display: flex;
  gap: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  transition: border-color 0.2s;
}

.ai-input-row.focused {
  border-color: #6366f1;
}

.ai-input {
  flex: 1;
  padding: 10px 12px;
  background: transparent;
  border: none;
  color: #1e293b;
  font-size: 13px;
  resize: none;
  line-height: 1.5;
  font-family: inherit;
}

.ai-input:focus {
  outline: none;
}

.ai-input::placeholder {
  color: #94a3b8;
}

.ai-send-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #6366f1;
  border: none;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;
  flex-shrink: 0;
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
  font-size: 10px;
  color: #94a3b8;
  text-align: center;
}
</style>