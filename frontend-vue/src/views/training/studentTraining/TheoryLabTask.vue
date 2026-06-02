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
        <template v-if="currentSlide.type === 'content' || currentSlide.type === 'summary'">
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
          :disabled="completing"
        >{{ completing ? '完成中…' : '完成学习 ✓' }}</button>
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
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTheoryTask } from '@/services/modules/training.service'
import { enterNode, completeNode } from '@/services/modules/studentTraining.service'
import { useStudentFlowStore } from '@/stores/modules/studentFlow.store'
import type { TheoryTask, TheorySlide } from '@/services/types/training.types'

const route = useRoute()
const router = useRouter()
const store = useStudentFlowStore()

// ─── Props ────────────────────────────────────────────────────────────────────
const props = defineProps<{
  nodeInstanceId?: string
  nodeConfig?: Record<string, unknown>
}>()

const emit = defineEmits<{
  (e: 'complete'): void
}>()

// ─── Route params ─────────────────────────────────────────────────────────────
const taskId = ref('')
const internalNodeInstanceId = ref('')

interface Slide {
  title: string
  content: string
  type: 'theory' | 'intro' | 'code' | 'quiz' | 'content' | 'summary'
  code?: string
  options?: string[]
  answer?: number
  key_points?: string[]
  questions?: Array<{
    question: string
    options: string[]
    answer: string | number
  }>
}

const slides = ref<Slide[]>([])
const pageTitle = ref('理论实训课堂')
const currentIndex = ref(0)
const quizAnswer = ref<number | null>(null)
const quizSubmitted = ref(false)
const showAiChat = ref(true)
const aiMessages = ref<{ role: string; content: string }[]>([])
const aiInput = ref('')
const aiStreaming = ref(false)
const aiMsgRef = ref<HTMLDivElement | null>(null)
const completing = ref(false)

const currentSlide = computed<Slide>(() => slides.value[currentIndex.value] || { title: '', content: '', type: 'content' })

// 获取节点实例ID（优先从props获取，其次从路由参数获取）
const effectiveNodeInstanceId = computed(() => props.nodeInstanceId || internalNodeInstanceId.value)

// 获取节点配置（优先从props获取）
const effectiveNodeConfig = computed(() => props.nodeConfig || {})

function typeLabel(type: string): string {
  const map: Record<string, string> = { theory: '理论', intro: '导入', code: '代码', quiz: '测验', content: '内容', summary: '总结' }
  return map[type] || type
}

function normalizeSlides(rawSlides: unknown): Slide[] {
  if (!Array.isArray(rawSlides)) return getDemoTaskData()

  return rawSlides.flatMap((raw, index) => {
    const item = raw as Partial<Slide>
    const type = item.type ?? 'content'

    if (type === 'quiz' && Array.isArray(item.questions) && item.questions.length > 0) {
      return item.questions.map((question, questionIndex) => ({
        title: item.title || `知识测验 ${questionIndex + 1}`,
        content: question.question,
        type: 'quiz' as const,
        options: question.options,
        answer: typeof question.answer === 'number'
          ? question.answer
          : question.options.findIndex(option => option === question.answer)
      }))
    }

    if ((type === 'summary' || item.title?.includes('总结')) && Array.isArray(item.key_points) && item.key_points.length > 0 && !item.content) {
      return [{
        title: item.title || '本节要点总结',
        content: item.key_points.map(point => `- ${point}`).join('\n'),
        type: 'summary' as const
      }]
    }

    if (type === 'quiz') {
      return [{
        title: item.title || `知识测验 ${index + 1}`,
        content: item.content || '请选择正确答案',
        type: 'quiz' as const,
        options: item.options && item.options.length > 0 ? item.options : ['选项 A', '选项 B', '选项 C', '选项 D'],
        answer: typeof item.answer === 'number' ? item.answer : 0
      }]
    }

    return [{
      title: item.title || `第 ${index + 1} 页`,
      content: item.content || (Array.isArray(item.key_points) ? item.key_points.map(point => `- ${point}`).join('\n') : '暂无内容'),
      type: type === 'summary' ? 'summary' : (type as Slide['type']),
      code: item.code,
      options: item.options,
      answer: item.answer
    }]
  })
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
  if (effectiveNodeInstanceId.value) {
    // 如果来自实训流程，返回实训执行页面
    router.push({
      path: `/student/training/${taskId.value}/execute`
    })
  } else {
    router.push('/training/student-training/theory-lab-home')
  }
}

/**
 * 完成学习并返回实训流程
 */
/**
 * 检查课中阶段是否完成
 */
function isMidPhaseCompleted(): boolean {
  const midPhaseIds = ['phase_theory']
  for (const pid of midPhaseIds) {
    const phase = store.phases.find(p => p.phase_id === pid)
    if (phase && phase.nodes.some(n => n.status !== 2 && n.node_type !== 'start' && n.node_type !== 'end')) {
      return false
    }
  }
  return true
}

async function handleComplete() {
  if (!effectiveNodeInstanceId.value) {
    // 如果不是来自实训流程，只显示提示
    alert('恭喜完成本次理论学习！')
    router.push('/training/student-training/theory-lab-home')
    return
  }

  completing.value = true
  try {
    // 使用 store 的 completeNode 方法完成节点，确保状态正确更新
    const result = await store.completeNode(effectiveNodeInstanceId.value)

    if (result) {
      // 检查课中阶段是否完成
      if (isMidPhaseCompleted()) {
        // 课中阶段完成，触发complete事件，让父组件处理阶段完成逻辑
        emit('complete')
      } else {
        // 完成成功，触发complete事件，让父组件处理导航
        emit('complete')
      }
    } else {
      alert('完成节点失败，请重试')
    }
  } catch (e: unknown) {
    console.error('Failed to complete node:', e)
    alert('完成节点失败，请重试')
  } finally {
    completing.value = false
  }
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
    const lastMsg = aiMessages.value[aiMessages.value.length - 1]
    if (lastMsg) {
      lastMsg.content = fullText || '(AI 响应为空)'
    }
  } catch (e: unknown) {
    const lastMsg = aiMessages.value[aiMessages.value.length - 1]
    if (lastMsg) {
      lastMsg.content = `连接失败: ${(e as Error).message}`
    }
  }
  aiStreaming.value = false
  nextTick(() => {
    if (aiMsgRef.value) aiMsgRef.value.scrollTop = aiMsgRef.value.scrollHeight
  })
}

/**
 * 获取演示数据
 */
function getDemoTaskData(): Slide[] {
  return [
    {
      title: 'Python 基础数据类型',
      content: 'Python 提供了多种内置数据类型，包括数字、字符串、列表、元组、字典等。理解这些数据类型是学习 Python 的基础。\n\n**学习目标：**\n- 理解 Python 的基本数据类型\n- 掌握各类数据的特点和用途\n- 学会常见的数据操作方法',
      type: 'intro'
    },
    {
      title: '数字类型',
      content: 'Python 支持三种数字类型：整数(int)、浮点数(float)和复数(complex)。\n\n**整数**：没有小数部分的数字，如 1, 2, -3。\n\n**浮点数**：带有小数部分的数字，如 3.14, 2.0, -0.5。\n\n**复数**：由实部和虚部组成，如 3+4j。\n\n**常用操作：**\n- `+` 加法\n- `-` 减法\n- `*` 乘法\n- `/` 除法\n- `//` 整除\n- `%` 取模',
      type: 'theory'
    },
    {
      title: '字符串操作',
      content: '字符串是字符序列，可以使用单引号或双引号定义。',
      type: 'code',
      code: '# 字符串定义\nname = "Hello World"\n\n# 字符串长度\nprint(len(name))  # 输出: 11\n\n# 字符串切片\nprint(name[0:5])  # 输出: Hello\n\n# 字符串拼接\ngreeting = "Hi" + " " + "there"\nprint(greeting)   # 输出: Hi there\n\n# 字符串格式化\nage = 25\nmessage = f"My age is {age}"\nprint(message)   # 输出: My age is 25'
    },
    {
      title: '列表和元组',
      content: '**列表**(list)是可变的有序序列，使用方括号 [] 定义。\n\n**元组**(tuple)是不可变的有序序列，使用圆括号 () 定义。\n\n**列表适合**需要频繁修改的数据。\n**元组适合**不需要修改的数据，如配置参数。\n\n**列表示例：**\n```\nfruits = ["apple", "banana", "cherry"]\nfruits.append("orange")  # 添加元素\n```\n\n**元组示例：**\n```\ncoordinates = (10, 20)\nprint(coordinates[0])  # 输出: 10\n```',
      type: 'theory'
    },
    {
      title: '知识测验一',
      content: '以下哪种数据类型是不可变的？',
      type: 'quiz',
      options: ['列表 (list)', '字典 (dict)', '元组 (tuple)', '集合 (set)'],
      answer: 2
    },
    {
      title: '知识测验二',
      content: 'Python中，哪个符号用于表示整除操作？',
      type: 'quiz',
      options: ['/', '//', '%', '*'],
      answer: 1
    },
    {
      title: '知识测验三',
      content: '以下哪个方法可以向列表末尾添加元素？',
      type: 'quiz',
      options: ['add()', 'append()', 'insert()', 'push()'],
      answer: 1
    },
    {
      title: '本节要点总结',
      content: '## 📚 本节要点总结\n\n### 一、数字类型\n- **整数(int)**：用于表示整数，如 1, 2, -3\n- **浮点数(float)**：用于表示小数，如 3.14, 2.0\n- **复数(complex)**：用于科学计算，如 3+4j\n\n### 二、字符串\n- 使用单引号或双引号定义\n- 支持切片操作 `name[0:5]`\n- 支持格式化字符串 `f"Hello {name}"`\n\n### 三、列表与元组\n- **列表**：可变序列，用 `[]` 定义\n- **元组**：不可变序列，用 `()` 定义\n- 元组比列表更安全，适合存储不变的数据\n\n### 四、核心要点\n1. 选择合适的数据类型可以提高代码效率\n2. 元组适合存储配置类数据\n3. 列表适合存储需要动态修改的数据\n4. 字符串是不可变的，修改会创建新对象',
      type: 'content'
    }
  ]
}

/**
 * 从节点配置加载数据
 */
async function loadFromNodeConfig() {
  try {
    // 如果已经从props获取了配置，直接使用
    if (effectiveNodeConfig.value && effectiveNodeConfig.value.slides) {
      slides.value = normalizeSlides(effectiveNodeConfig.value.slides)
      pageTitle.value = (effectiveNodeConfig.value.title as string) || '理论实训课堂'
      return
    }

    // 否则从接口获取
    const result = await enterNode(effectiveNodeInstanceId.value)
    const config = result.data.node_config as unknown as any

    // 检查节点配置中是否有 slides 数据
    if (config && config.slides) {
      slides.value = normalizeSlides(config.slides)
      pageTitle.value = config.title || '理论实训课堂'
    } else {
      // 回退到演示数据
      slides.value = getDemoTaskData()
      pageTitle.value = 'Python 基础数据类型'
    }
  } catch (error) {
    console.error('Failed to load node config:', error)
    slides.value = getDemoTaskData()
    pageTitle.value = 'Python 基础数据类型'
  }
}

/**
 * 初始化任务数据
 */
async function loadTask() {
  try {
    const urlParams = new URLSearchParams(window.location.search)
    taskId.value = urlParams.get('taskId') || 'demo-theory-001'
    internalNodeInstanceId.value = urlParams.get('nodeInstanceId') || ''

    // 如果有节点实例ID（从props或URL参数），从节点配置加载
    if (effectiveNodeInstanceId.value) {
      await loadFromNodeConfig()
      return
    }

    // 否则从独立接口加载
    const response = await getTheoryTask(taskId.value)

    if (response.code === 200 && response.data) {
      slides.value = normalizeSlides(response.data.slides)
      pageTitle.value = response.data.title || '理论实训课堂'
    } else {
      // 使用模拟数据
      slides.value = getDemoTaskData()
      pageTitle.value = 'Python 基础数据类型'
    }
  } catch (error) {
    console.error('Failed to load task:', error)
    slides.value = [
      {
        title: '加载失败',
        content: '无法加载学习资料，请稍后重试。',
        type: 'content'
      }
    ]
  }
}

onMounted(() => {
  loadTask()
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
  color: #64748b; font-size: 12px; cursor: pointer;
}
.lab-ai-btn:hover { background: #f1f5f9; }
.lab-ai-btn.active { background: #e0e7ff; border-color: #6366f1; color: #6366f1; }

/* ── Body ── */
.lab-body { flex: 1; display: flex; overflow: hidden; }

/* ── Sidebar ── */
.lab-sidebar {
  width: 220px; background: #ffffff;
  border-right: 1px solid #e2e8f0;
  padding: 16px 0; overflow-y: auto;
}
.lab-sidebar-title {
  padding: 0 16px 12px; font-size: 11px; font-weight: 600;
  color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px;
}
.lab-outline-item {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 16px; cursor: pointer;
  transition: background 0.2s;
}
.lab-outline-item:hover { background: #f8fafc; }
.lab-outline-item.active { background: #e0e7ff; }
.lab-outline-item.completed .lab-outline-dot { background: #22c55e; }
.lab-outline-dot {
  width: 8px; height: 8px; border-radius: 50%; background: #cbd5e1;
  flex-shrink: 0;
}
.lab-outline-item.active .lab-outline-dot { background: #6366f1; }
.lab-outline-label { flex: 1; font-size: 13px; color: #334155; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.lab-outline-type {
  font-size: 10px; padding: 2px 6px; border-radius: 4px;
  background: #f1f5f9; color: #64748b; flex-shrink: 0;
}

/* ── Content ── */
.lab-content {
  flex: 1; padding: 24px 32px; overflow-y: auto;
}
.lab-slide-title {
  font-size: 20px; font-weight: 600; color: #1e293b;
  margin: 0 0 16px;
}
.lab-slide-text {
  font-size: 14px; line-height: 1.7; color: #475569;
  margin-bottom: 20px;
}
.lab-slide-text :deep(p) { margin: 0 0 12px; }
.lab-slide-text :deep(strong) { font-weight: 600; color: #1e293b; }
.lab-slide-text :deep(code) {
  padding: 2px 6px; background: #f1f5f9; border-radius: 4px;
  font-family: 'Cascadia Code', 'Consolas', monospace; font-size: 13px;
}
.lab-slide-text :deep(pre) {
  background: #1e293b; color: #e2e8f0; padding: 16px;
  border-radius: 8px; overflow-x: auto; margin: 12px 0;
}
.lab-slide-text :deep(pre code) {
  background: transparent; padding: 0; color: inherit;
}
.lab-slide-text :deep(.lab-md-code) {
  background: #1e293b; color: #e2e8f0; padding: 16px;
  border-radius: 8px; overflow-x: auto; margin: 12px 0;
}
.lab-slide-text :deep(.lab-md-code code) {
  background: transparent; padding: 0; color: inherit;
}

.lab-code-block {
  background: #1e293b; color: #e2e8f0; padding: 16px;
  border-radius: 8px; overflow-x: auto;
  font-family: 'Cascadia Code', 'Consolas', monospace;
  font-size: 13px; line-height: 1.6;
}
.lab-code-block code {
  background: transparent; padding: 0;
}

/* ── Quiz ── */
.lab-quiz-options { display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px; }
.lab-quiz-option {
  display: flex; align-items: center;
  padding: 12px 16px; background: #ffffff;
  border: 2px solid #e2e8f0; border-radius: 8px;
  font-size: 14px; color: #334155; cursor: pointer;
  transition: all 0.2s; text-align: left;
}
.lab-quiz-option:hover:not(:disabled) { border-color: #cbd5e1; background: #f8fafc; }
.lab-quiz-option--selected { border-color: #6366f1; background: #f0f5ff; }
.lab-quiz-option--correct { border-color: #22c55e; background: #f0fdf4; color: #166534; }
.lab-quiz-option--wrong { border-color: #ef4444; background: #fef2f2; color: #991b1b; }
.lab-quiz-option:disabled { cursor: default; }
.lab-quiz-actions { display: flex; justify-content: flex-end; }
.lab-quiz-submit {
  padding: 8px 20px; background: #6366f1; color: #ffffff;
  border: none; border-radius: 6px; font-size: 13px; cursor: pointer;
}
.lab-quiz-submit:hover:not(:disabled) { background: #4f46e5; }
.lab-quiz-submit:disabled { opacity: 0.5; cursor: not-allowed; }
.lab-quiz-result {
  padding: 12px 16px; border-radius: 8px; font-weight: 600;
}

/* ── Nav ── */
.lab-nav { display: flex; gap: 12px; justify-content: flex-end; margin-top: 24px; padding-top: 24px; border-top: 1px solid #e2e8f0; }
.lab-nav-btn {
  padding: 8px 20px; background: #f1f5f9; color: #64748b;
  border: none; border-radius: 6px; font-size: 13px; cursor: pointer;
}
.lab-nav-btn:hover:not(:disabled) { background: #e2e8f0; color: #334155; }
.lab-nav-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.lab-nav-btn--primary { background: #6366f1; color: #ffffff; }
.lab-nav-btn--primary:hover { background: #4f46e5; }
.lab-nav-btn--complete { background: #22c55e; color: #ffffff; }
.lab-nav-btn--complete:hover { background: #16a34a; }

/* ── AI Panel ── */
.lab-ai-panel {
  width: 350px; background: #ffffff; border-left: 1px solid #e2e8f0;
  display: flex; flex-direction: column;
}
.lab-ai-header {
  padding: 12px 16px; border-bottom: 1px solid #e2e8f0;
  display: flex; align-items: center; justify-content: space-between;
  font-size: 13px; font-weight: 500; color: #334155;
}
.lab-ai-header button {
  width: 24px; height: 24px; display: flex; align-items: center; justify-content: center;
  border: none; background: transparent; color: #94a3b8; cursor: pointer;
}
.lab-ai-header button:hover { color: #64748b; }
.lab-ai-messages { flex: 1; overflow-y: auto; padding: 12px; }
.lab-ai-empty { text-align: center; padding: 24px 12px; color: #94a3b8; }
.lab-ai-empty p { margin: 0 0 16px; font-size: 13px; }
.lab-ai-chips { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.lab-ai-chip {
  padding: 6px 12px; background: #f1f5f9; color: #64748b;
  border: none; border-radius: 20px; font-size: 12px; cursor: pointer;
}
.lab-ai-chip:hover { background: #e2e8f0; color: #334155; }
.lab-ai-msg { display: flex; gap: 8px; margin-bottom: 12px; }
.lab-ai-msg.assistant { flex-direction: row-reverse; }
.lab-ai-av {
  width: 28px; height: 28px; border-radius: 50%;
  background: #e2e8f0; display: flex; align-items: center; justify-content: center;
  font-size: 12px; flex-shrink: 0;
}
.lab-ai-msg.user .lab-ai-av { background: #6366f1; color: #ffffff; }
.lab-ai-text {
  max-width: 75%; padding: 8px 12px; border-radius: 12px;
  font-size: 13px; line-height: 1.5;
}
.lab-ai-msg.user .lab-ai-text {
  background: #6366f1; color: #ffffff; border-radius: 12px 12px 4px 12px;
}
.lab-ai-msg.assistant .lab-ai-text {
  background: #f1f5f9; color: #334155; border-radius: 12px 12px 12px 4px;
}
.lab-thinking-dot {
  display: inline-block; width: 6px; height: 6px;
  border-radius: 50%; background: #94a3b8;
  margin-right: 2px; animation: thinking 1.4s infinite ease-in-out;
}
.lab-thinking-dot:nth-child(2) { animation-delay: 0.2s; }
.lab-thinking-dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes thinking {
  0%, 80%, 100% { opacity: 0.4; transform: scale(0.8); }
  40% { opacity: 1; transform: scale(1); }
}
.lab-ai-input-area {
  padding: 12px; border-top: 1px solid #e2e8f0;
  display: flex; gap: 8px;
}
.lab-ai-input {
  flex: 1; padding: 8px 12px; border: 1px solid #e2e8f0;
  border-radius: 8px; font-size: 13px; outline: none;
}
.lab-ai-input:focus { border-color: #6366f1; }
.lab-ai-send {
  padding: 8px 16px; background: #6366f1; color: #ffffff;
  border: none; border-radius: 8px; font-size: 13px; cursor: pointer;
}
.lab-ai-send:hover:not(:disabled) { background: #4f46e5; }
.lab-ai-send:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
