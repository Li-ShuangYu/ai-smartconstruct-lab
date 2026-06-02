<template>
  <div class="lab-root">
    <!-- 顶部栏 -->
    <header class="lab-header">
      <div class="lab-header-left">
        <button class="lab-back" @click="goBack" title="返回">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5m7-7-7 7 7 7"/></svg>
        </button>
        <h1 class="lab-title">{{ pageTitle }}</h1>
      </div>
      <div class="lab-header-right">
        <div class="lab-progress">{{ currentIndex + 1 }} / {{ slides.length }}</div>
        <button class="lab-ai-btn" :class="{ active: showAiChat }" @click="showAiChat = !showAiChat">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
          AI 助教
        </button>
      </div>
    </header>

    <div class="lab-body">
      <!-- 左侧大纲导航 -->
      <aside class="lab-sidebar">
        <div class="lab-sidebar-title">大纲</div>
        <div
          v-for="(slide, i) in slides"
          :key="i"
          class="lab-outline-item"
          :class="{ active: currentIndex === i, completed: i < currentIndex }"
          @click="goToSlide(i)"
        >
          <span class="lab-outline-dot"></span>
          <span class="lab-outline-label">{{ slide.title || `第${i + 1}页` }}</span>
          <span class="lab-outline-type">{{ typeLabel(slide.type) }}</span>
        </div>
      </aside>

      <!-- 中间主内容 -->
      <main class="lab-content">
        <!-- Theory / Intro -->
        <template v-if="currentSlide.type === 'theory' || currentSlide.type === 'intro'">
          <h2 class="lab-slide-title">{{ currentSlide.title }}</h2>
          <div class="lab-slide-text" v-html="renderMd(currentSlide.content)"></div>
        </template>

        <!-- Code -->
        <template v-if="currentSlide.type === 'code'">
          <h2 class="lab-slide-title">{{ currentSlide.title }}</h2>
          <div class="lab-slide-text">{{ currentSlide.content }}</div>
          <pre class="lab-code-block"><code>{{ currentSlide.code }}</code></pre>
        </template>

        <!-- Quiz -->
        <template v-if="currentSlide.type === 'quiz'">
          <h2 class="lab-slide-title">{{ currentSlide.title }}</h2>
          <p class="lab-slide-text">{{ currentSlide.content }}</p>
          <div class="lab-quiz-options">
            <button
              v-for="(opt, oi) in currentSlide.options"
              :key="oi"
              class="lab-quiz-option"
              :class="{
                'lab-quiz-option--selected': quizAnswer === oi,
                'lab-quiz-option--correct': quizSubmitted && oi === currentSlide.answer,
                'lab-quiz-option--wrong': quizSubmitted && quizAnswer === oi && oi !== currentSlide.answer
              }"
              :disabled="quizSubmitted"
              @click="quizAnswer = oi"
            >
              {{ opt }}
            </button>
          </div>
          <div v-if="!quizSubmitted" class="lab-quiz-actions">
            <button class="lab-quiz-submit" :disabled="quizAnswer === null" @click="submitQuiz">提交</button>
          </div>
          <div v-else class="lab-quiz-result">
            {{ quizAnswer === currentSlide.answer ? '✓ 回答正确！' : '✕ 回答错误' }}
          </div>
        </template>

        <!-- Content fallback -->
        <template v-if="currentSlide.type === 'content'">
          <h2 class="lab-slide-title">{{ currentSlide.title }}</h2>
          <div class="lab-slide-text" v-html="renderMd(currentSlide.content)"></div>
        </template>

        <!-- 底部导航 -->
        <div class="lab-nav">
          <button class="lab-nav-btn" :disabled="currentIndex === 0" @click="currentIndex--">← 上一页</button>
          <button
            v-if="currentIndex < slides.length - 1"
            class="lab-nav-btn lab-nav-btn--primary"
            @click="currentIndex++"
          >下一页 →</button>
          <button
            v-else
            class="lab-nav-btn lab-nav-btn--complete"
            @click="handleComplete"
          >完成学习 ✓</button>
        </div>
      </main>

      <!-- 右侧AI聊天面板 -->
      <aside class="lab-ai-panel" v-if="showAiChat">
        <div class="lab-ai-header">
          <span>AI 助教</span>
          <button @click="showAiChat = false">✕</button>
        </div>
        <div class="lab-ai-messages" ref="aiMsgRef">
          <div v-if="!aiMessages.length" class="lab-ai-empty">
            <p>当前内容有任何疑问？随时向 AI 助教提问！</p>
            <div class="lab-ai-chips">
              <button class="lab-ai-chip" @click="aiAsk('请解释当前页面的核心概念')">解释概念</button>
              <button class="lab-ai-chip" @click="aiAsk('请给我一个例子')">举例说明</button>
              <button class="lab-ai-chip" @click="aiAsk('能帮我总结当前内容吗？')">总结要点</button>
            </div>
          </div>
          <div v-for="(m, i) in aiMessages" :key="i" class="lab-ai-msg" :class="m.role">
            <div class="lab-ai-av">{{ m.role === 'user' ? '👤' : '🤖' }}</div>
            <div class="lab-ai-text">{{ m.content }}</div>
          </div>
          <div v-if="aiStreaming" class="lab-ai-msg assistant">
            <div class="lab-ai-av">🤖</div>
            <div class="lab-ai-text"><span class="lab-thinking-dot"></span><span class="lab-thinking-dot"></span><span class="lab-thinking-dot"></span></div>
          </div>
        </div>
        <div class="lab-ai-input-area">
          <input
            v-model="aiInput"
            class="lab-ai-input"
            placeholder="输入问题…"
            @keydown.enter.prevent="aiAsk()"
          />
          <button class="lab-ai-send" :disabled="!aiInput.trim() || aiStreaming" @click="aiAsk()">发送</button>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

interface Slide {
  title: string
  content: string
  type: 'theory' | 'intro' | 'code' | 'quiz' | 'content'
  code?: string
  options?: string[]
  answer?: number
}

const slides = ref<Slide[]>([])
const pageTitle = ref('理论实训')
const currentIndex = ref(0)
const quizAnswer = ref<number | null>(null)
const quizSubmitted = ref(false)
const showAiChat = ref(false)
const aiMessages = ref<{ role: string; content: string }[]>([])
const aiInput = ref('')
const aiStreaming = ref(false)
const aiMsgRef = ref<HTMLDivElement | null>(null)

const currentSlide = computed<Slide>(() => slides.value[currentIndex.value] || { title: '', content: '', type: 'content' })

function typeLabel(type: string): string {
  const map: Record<string, string> = { theory: '理论', intro: '导入', code: '代码', quiz: '测验', content: '内容' }
  return map[type] || type
}

function renderMd(text: string): string {
  if (!text) return ''
  let h = text
  
  // 首先处理代码块（防止先转义导致无法匹配）
  h = h.replace(/```(\w*)\n([\s\S]*?)```/g, (_match: string, _lang: string, code: string) => {
    // 转义代码块内的特殊字符
    const escapedCode = code
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
    return `<pre class="lab-md-code"><code>${escapedCode}</code></pre>`
  })
  
  // 再转义普通文本
  h = h.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  
  // 处理行内代码
  h = h.replace(/`([^`]+)`/g, '<code>$1</code>')
  
  // 处理粗体
  h = h.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  
  // 处理换行
  h = h.replace(/\n\n/g, '</p><p>').replace(/\n/g, '<br>')
  
  return '<p>' + h + '</p>'
}

function goToSlide(i: number) {
  currentIndex.value = i
  resetQuiz()
}

function resetQuiz() {
  quizAnswer.value = null
  quizSubmitted.value = false
}

function submitQuiz() {
  if (quizAnswer.value === null) return
  quizSubmitted.value = true
}

function goBack() {
  router.push('/training/student-training/theory-lab-home')
}

function handleComplete() {
  alert('恭喜完成本次理论学习！')
  router.push('/training/student-training/theory-lab-home')
}

async function aiAsk(text?: string) {
  const msg = (text || aiInput.value).trim()
  if (!msg || aiStreaming.value) return
  aiInput.value = ''
  aiMessages.value.push({ role: 'user', content: msg })
  aiMessages.value.push({ role: 'assistant', content: '' })
  aiStreaming.value = true

  try {
    const context = `当前课件标题：${pageTitle.value}\n当前页面内容：${currentSlide.value.title}\n---\n${currentSlide.value.content?.slice(0, 500)}`
    const res = await fetch('/ai/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        messages: [
          { role: 'system', content: `你是一位教学助教，根据以下课堂内容回答问题。${context}` },
          { role: 'user', content: msg },
        ],
      }),
    })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    const reader = res.body!.getReader()
    const dec = new TextDecoder()
    let buf = ''
    let fullText = ''
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
          if (d.content) fullText += d.content
        } catch { /* ignore */ }
      }
    }
    const lastAi = aiMessages.value[aiMessages.value.length - 1]; if (lastAi) lastAi.content = fullText || '(AI 响应为空)'
  } catch (e: unknown) {
    const lastAi = aiMessages.value[aiMessages.value.length - 1]; if (lastAi) lastAi.content = `连接失败: ${(e as Error).message}`
  }
  aiStreaming.value = false
  nextTick(() => {
    if (aiMsgRef.value) aiMsgRef.value.scrollTop = aiMsgRef.value.scrollHeight
  })
}

onMounted(() => {
  try {
    const savedSlides = sessionStorage.getItem('th_slides')
    const savedTitle = sessionStorage.getItem('th_title')
    if (savedSlides) {
      const parsed = JSON.parse(savedSlides)
      slides.value = Array.isArray(parsed) ? parsed : []
    }
    if (savedTitle) pageTitle.value = savedTitle
  } catch { /* ignore */ }
})
</script>

<style scoped>
.lab-root {
  height: 100vh; display: flex; flex-direction: column;
  background: #f8fafc; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

/* ── Header ── */
.lab-header {
  height: 48px; background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 16px; flex-shrink: 0;
}
.lab-header-left { display: flex; align-items: center; gap: 12px; }
.lab-back {
  width: 28px; height: 28px; display: flex; align-items: center; justify-content: center;
  background: transparent; border: none; border-radius: 6px;
  color: #64748b; cursor: pointer;
}
.lab-back:hover { background: #f1f5f9; color: #475569; }
.lab-title { font-size: 15px; font-weight: 600; color: #0f172a; margin: 0; }
.lab-header-right { display: flex; align-items: center; gap: 12px; }
.lab-progress { font-size: 12px; font-weight: 600; color: #94a3b8; padding: 4px 10px; background: #f1f5f9; border-radius: 6px; }
.lab-ai-btn {
  display: flex; align-items: center; gap: 4px; padding: 6px 12px;
  border: 1px solid #e2e8f0; border-radius: 6px; background: transparent;
  color: #64748b; font-size: 12px; font-weight: 500; cursor: pointer;
}
.lab-ai-btn:hover { background: #f1f5f9; }
.lab-ai-btn.active { background: #6366f1; border-color: #6366f1; color: #fff; }

/* ── Body ── */
.lab-body { flex: 1; display: flex; overflow: hidden; }

/* ── Sidebar ── */
.lab-sidebar {
  width: 220px; background: #ffffff; border-right: 1px solid #e2e8f0;
  padding: 16px; overflow-y: auto; flex-shrink: 0;
}
.lab-sidebar-title { font-size: 12px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: .5px; margin-bottom: 12px; }
.lab-outline-item {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 10px; border-radius: 6px; cursor: pointer;
  transition: all .12s; margin-bottom: 4px;
}
.lab-outline-item:hover { background: #f8fafc; }
.lab-outline-item.active { background: rgba(99,102,241,0.06); }
.lab-outline-item.completed { opacity: .6; }
.lab-outline-dot {
  width: 6px; height: 6px; border-radius: 50%; background: #cbd5e1; flex-shrink: 0;
}
.lab-outline-item.active .lab-outline-dot { background: #6366f1; }
.lab-outline-item.completed .lab-outline-dot { background: #22c55e; }
.lab-outline-label { font-size: 12px; color: #475569; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.lab-outline-item.active .lab-outline-label { color: #6366f1; font-weight: 600; }
.lab-outline-type { font-size: 10px; color: #94a3b8; padding: 1px 6px; background: #f1f5f9; border-radius: 3px; }

/* ── Content ── */
.lab-content { flex: 1; padding: 32px; overflow-y: auto; max-width: 800px; margin: 0 auto; width: 100%; }
.lab-slide-title { font-size: 1.5rem; font-weight: 700; color: #0f172a; margin: 0 0 16px; }
.lab-slide-text { font-size: 15px; line-height: 1.8; color: #334155; margin-bottom: 16px; }
.lab-slide-text :deep(code) { background: #f1f5f9; padding: 2px 6px; border-radius: 4px; font-size: 13px; }
.lab-slide-text :deep(pre) { background: #0f172a; color: #e2e8f0; padding: 16px; border-radius: 8px; overflow-x: auto; margin: 12px 0; }
.lab-slide-text :deep(pre code) { background: transparent; padding: 0; color: inherit; }
.lab-slide-text :deep(.lab-md-code) { background: #0f172a; color: #e2e8f0; padding: 16px; border-radius: 8px; overflow-x: auto; margin: 12px 0; }
.lab-slide-text :deep(.lab-md-code code) { background: transparent; padding: 0; color: inherit; }
.lab-code-block { background: #0f172a; color: #e2e8f0; padding: 16px; border-radius: 8px; overflow-x: auto; font-size: 13px; line-height: 1.6; }
.lab-code-block code { background: transparent; padding: 0; }

.lab-quiz-options { display: flex; flex-direction: column; gap: 8px; max-width: 400px; }
.lab-quiz-option {
  padding: 10px 16px; border: 1px solid #e2e8f0; border-radius: 8px;
  background: #fff; color: #334155; font-size: 14px; cursor: pointer;
  text-align: left; transition: all .12s;
}
.lab-quiz-option:hover { border-color: #6366f1; }
.lab-quiz-option--selected { border-color: #6366f1; background: rgba(99,102,241,0.04); }
.lab-quiz-option--correct { border-color: #22c55e; background: rgba(34,197,94,0.06); color: #16a34a; }
.lab-quiz-option--wrong { border-color: #ef4444; background: rgba(239,68,68,0.06); color: #dc2626; }
.lab-quiz-option:disabled { cursor: default; }
.lab-quiz-submit { margin-top: 12px; padding: 8px 24px; background: #6366f1; color: #fff; border: none; border-radius: 6px; cursor: pointer; font-size: 13px; font-weight: 600; }
.lab-quiz-submit:disabled { opacity: .5; cursor: not-allowed; }
.lab-quiz-result { margin-top: 12px; padding: 10px 16px; border-radius: 8px; font-size: 14px; font-weight: 600; background: rgba(34,197,94,0.06); color: #16a34a; }

/* ── Navigation ── */
.lab-nav { display: flex; justify-content: space-between; margin-top: 32px; padding-top: 16px; border-top: 1px solid #e2e8f0; }
.lab-nav-btn {
  padding: 8px 20px; border: 1px solid #e2e8f0; border-radius: 6px;
  background: #fff; color: #64748b; font-size: 13px; font-weight: 500;
  cursor: pointer; transition: all .12s;
}
.lab-nav-btn:hover:not(:disabled) { border-color: #6366f1; color: #6366f1; }
.lab-nav-btn:disabled { opacity: .4; cursor: not-allowed; }
.lab-nav-btn--primary { background: #6366f1; border-color: #6366f1; color: #fff; }
.lab-nav-btn--primary:hover:not(:disabled) { background: #4f46e5; }
.lab-nav-btn--complete { background: #22c55e; border-color: #22c55e; color: #fff; }
.lab-nav-btn--complete:hover { background: #16a34a; }

/* ── AI Panel ── */
.lab-ai-panel {
  width: 320px; background: #ffffff; border-left: 1px solid #e2e8f0;
  display: flex; flex-direction: column; flex-shrink: 0;
}
.lab-ai-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 12px; border-bottom: 1px solid #e2e8f0;
  font-size: 13px; font-weight: 600; color: #0f172a;
}
.lab-ai-header button { background: none; border: none; color: #94a3b8; cursor: pointer; }
.lab-ai-header button:hover { color: #475569; }
.lab-ai-messages { flex: 1; overflow-y: auto; padding: 12px; background: #fafafa; }
.lab-ai-empty { text-align: center; padding: 20px 12px; color: #94a3b8; font-size: 13px; }
.lab-ai-chip {
  margin: 4px; padding: 6px 12px; border: 1px solid #e2e8f0; border-radius: 16px;
  background: #fff; color: #64748b; font-size: 11px; cursor: pointer;
}
.lab-ai-chip:hover { border-color: #6366f1; color: #6366f1; }
.lab-ai-msg { display: flex; gap: 8px; margin-bottom: 12px; }
.lab-ai-av { font-size: 16px; flex-shrink: 0; }
.lab-ai-text { font-size: 13px; line-height: 1.5; color: #334155; background: #f1f5f9; padding: 8px 12px; border-radius: 8px; white-space: pre-wrap; }
.lab-ai-msg.user .lab-ai-text { background: #eef2ff; color: #4338ca; }
.lab-ai-input-area { padding: 10px; border-top: 1px solid #e2e8f0; display: flex; gap: 8px; }
.lab-ai-input { flex: 1; padding: 8px 12px; border: 1px solid #e2e8f0; border-radius: 6px; font-size: 13px; outline: none; }
.lab-ai-input:focus { border-color: #6366f1; }
.lab-ai-send { padding: 8px 16px; background: #6366f1; color: #fff; border: none; border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer; }
.lab-ai-send:disabled { opacity: .5; cursor: not-allowed; }
.lab-thinking-dot { display: inline-block; width: 5px; height: 5px; background: #6366f1; border-radius: 50%; margin: 0 2px; animation: bounce 1.4s infinite; }
.lab-thinking-dot:nth-child(2) { animation-delay: .2s; }
.lab-thinking-dot:nth-child(3) { animation-delay: .4s; }
@keyframes bounce { 0%,80%,100% { transform: scale(.6); } 40% { transform: scale(1); } }
</style>