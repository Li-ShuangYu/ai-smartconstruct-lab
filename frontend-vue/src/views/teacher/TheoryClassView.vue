<template>
  <div class="theory-class-view">
    <!-- Header -->
    <header class="tc-header">
      <div class="tc-header-left">
        <button class="tc-back-btn" @click="handleBack">← 返回</button>
        <h1 class="tc-title">{{ pageTitle }}</h1>
        <n-tag v-if="topic" type="info" size="small" bordered>{{ topic }}</n-tag>
        <n-tag v-if="slides.length > 0" size="small" bordered>{{ currentIndex + 1 }} / {{ slides.length }}</n-tag>
      </div>
      <div class="tc-header-right">
        <label class="auto-play-label">
          <input type="checkbox" v-model="autoPlaying" /> 自动播放
        </label>
      </div>
    </header>

    <!-- Loading -->
    <n-spin :show="loading" class="tc-body">
      <div v-if="!loading && slides.length === 0" class="tc-empty">
        <p>暂无理论课内容</p>
        <p class="tc-empty-hint" v-if="aiStatus === 1">⏳ AI 正在处理中，请稍后再查看</p>
        <p class="tc-empty-hint" v-else-if="aiStatus === 0">⏳ 请先发布模板触发 AI 处理</p>
        <p class="tc-empty-hint" v-else-if="aiStatus === 3">⚠️ AI 处理失败，请重试</p>
      </div>

      <!-- Slide Viewer -->
      <div v-if="slides.length > 0" class="tc-slide-container">
        <div class="tc-slide-area">
          <transition name="slide-fade" mode="out-in">
            <div class="tc-slide" :key="currentIndex">
              <!-- Theory Slide -->
              <template v-if="currentSlide.type === 'theory' || currentSlide.type === 'intro'">
                <div class="slide-header">
                  <span class="slide-badge theory-badge">📖 理论</span>
                  <h2 class="slide-title">{{ currentSlide.title }}</h2>
                </div>
                <div class="slide-content" v-html="renderContent(currentSlide.content)"></div>
                <div v-if="currentSlide.bullet_points && currentSlide.bullet_points.length" class="slide-bullets">
                  <div v-for="(bp, i) in currentSlide.bullet_points" :key="i" class="bullet-item">
                    <span class="bullet-dot">•</span>
                    <span>{{ bp }}</span>
                  </div>
                </div>
              </template>

              <!-- Code Slide -->
              <template v-else-if="currentSlide.type === 'code'">
                <div class="slide-header">
                  <span class="slide-badge code-badge">💻 代码</span>
                  <h2 class="slide-title">{{ currentSlide.title }}</h2>
                </div>
                <pre class="code-block"><code>{{ currentSlide.code || currentSlide.content }}</code></pre>
                <div v-if="currentSlide.output" class="code-output">
                  <div class="output-label">📤 输出结果</div>
                  <pre class="output-content">{{ currentSlide.output }}</pre>
                </div>
              </template>

              <!-- Quiz Slide -->
              <template v-else-if="currentSlide.type === 'quiz'">
                <div class="slide-header">
                  <span class="slide-badge quiz-badge">📝 练习</span>
                  <h2 class="slide-title">{{ currentSlide.title }}</h2>
                </div>
                <div class="quiz-list">
                  <div v-for="(q, i) in (currentSlide.questions || currentSlide.quiz || [])" :key="i" class="quiz-item">
                    <div class="quiz-q">{{ Number(i) + 1 }}. {{ q.question || q.content }}</div>
                    <div v-if="q.options" class="quiz-options">
                      <div v-for="(opt, oi) in q.options" :key="oi" class="quiz-option">
                        <span class="option-key">{{ String.fromCharCode(65 + Number(oi)) }}.</span>
                        <span>{{ typeof opt === 'object' ? (opt.label || opt.content || '') : opt }}</span>
                      </div>
                    </div>
                    <details class="quiz-answer">
                      <summary>查看答案</summary>
                      <div class="answer-content">{{ q.answer || q.standardAnswer || '—' }}</div>
                    </details>
                  </div>
                </div>
              </template>

              <!-- Summary Slide -->
              <template v-else-if="currentSlide.type === 'summary'">
                <div class="slide-header">
                  <span class="slide-badge summary-badge">📌 小结</span>
                  <h2 class="slide-title">{{ currentSlide.title }}</h2>
                </div>
                <div class="slide-content" v-html="renderContent(currentSlide.content)"></div>
                <div v-if="currentSlide.key_points" class="summary-keypoints">
                  <div v-for="(kp, i) in currentSlide.key_points" :key="i" class="kp-item">✦ {{ kp }}</div>
                </div>
              </template>

              <!-- Fallback -->
              <template v-else>
                <div class="slide-header">
                  <h2 class="slide-title">{{ currentSlide.title || '内容' }}</h2>
                </div>
                <div class="slide-content" v-html="renderContent(currentSlide.content || JSON.stringify(currentSlide, null, 2))"></div>
              </template>
            </div>
          </transition>
        </div>

        <!-- Navigation -->
        <div class="tc-navbar">
          <button class="tc-nav-btn" :disabled="currentIndex === 0" @click="goPrev">‹ 上一页</button>
          <div class="tc-dots">
            <span v-for="(_, i) in slides" :key="i" class="tc-dot" :class="{ active: i === currentIndex }" @click="goTo(i)"></span>
          </div>
          <button class="tc-nav-btn" :disabled="currentIndex >= slides.length - 1" @click="goNext">下一页 ›</button>
        </div>
      </div>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { getTemplatePreview } from '@/services/modules/orchestration.service'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const templateId = computed(() => String(route.params.templateId))
const nodeId = computed(() => String(route.params.nodeId))

const loading = ref(true)
const pageTitle = ref('理论课堂')
const topic = ref('')
const aiStatus = ref(0)
const slides = ref<any[]>([])
const currentIndex = ref(0)
const autoPlaying = ref(false)
let playTimer: ReturnType<typeof setInterval> | undefined

const currentSlide = computed(() => slides.value[currentIndex.value] || {})

onMounted(async () => {
  await loadData()
  // Keyboard shortcuts
  window.addEventListener('keydown', handleKeydown)
})
onUnmounted(() => {
  stopAutoPlay()
  window.removeEventListener('keydown', handleKeydown)
})

async function loadData() {
  loading.value = true
  try {
    const res = await getTemplatePreview(templateId.value)
    if (res.code === 200) {
      const preview = res.data
      aiStatus.value = preview.ai_status || 0
      pageTitle.value = preview.template_name || '理论课堂'

      // Find the node across all phases
      for (const phase of preview.phases || []) {
        const node = (phase.nodes || []).find((n: any) => n.node_id === nodeId.value)
        if (node) {
          topic.value = (node.config_summary as any)?.topic || ''
          const content = node.ai_generated_content as any
          if (content) {
            slides.value = content.slides || []
            // If no slides but has topic and we're handling quiz_questions separately
            if (slides.value.length === 0 && content.knowledge_points) {
              // Build a simple slide from topic
              slides.value = [{
                type: 'theory',
                title: topic.value || '知识内容',
                content: (content.summary || content.content || '') as string,
                bullet_points: content.knowledge_points || []
              }]
            }
            // If there are quiz questions, append as a quiz slide
            if (content.quiz_questions && content.quiz_questions.length > 0) {
              slides.value.push({
                type: 'quiz',
                title: '课堂练习',
                questions: content.quiz_questions
              })
            }
          }
          break
        }
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

function renderContent(text: string): string {
  if (!text) return ''
  // Simple markdown-like rendering: convert newlines to <br>, bold markers
  return text
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
}

function goTo(index: number) {
  if (index >= 0 && index < slides.value.length) {
    currentIndex.value = index
  }
}
function goPrev() { goTo(currentIndex.value - 1) }
function goNext() { goTo(currentIndex.value + 1) }

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'ArrowLeft') goPrev()
  else if (e.key === 'ArrowRight') goNext()
  else if (e.key === ' ') { e.preventDefault(); autoPlaying.value = !autoPlaying.value }
}

watchAutoPlay()

function watchAutoPlay() {
  // Use a simple effect via setInterval
}
// Handle autoPlay with a watcher-like pattern
import { watch } from 'vue'
watch(autoPlaying, (val) => {
  if (val) {
    playTimer = setInterval(() => {
      if (currentIndex.value < slides.value.length - 1) {
        currentIndex.value++
      } else {
        autoPlaying.value = false
      }
    }, 5000)
  } else {
    stopAutoPlay()
  }
})

function stopAutoPlay() {
  if (playTimer) { clearInterval(playTimer); playTimer = undefined }
}

function handleBack() {
  router.push(`/teacher/template-preview/${templateId.value}`)
}
</script>

<style scoped>
.theory-class-view {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #0F172A;
  color: #E2E8F0;
  font-family: system-ui, sans-serif;
}
.tc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: #1E293B;
  border-bottom: 1px solid #334155;
}
.tc-header-left { display: flex; align-items: center; gap: 12px; }
.tc-back-btn {
  background: #334155; color: #94A3B8; border: none; padding: 6px 14px;
  border-radius: 6px; cursor: pointer; font-size: 13px; font-weight: 600;
}
.tc-back-btn:hover { background: #475569; color: #E2E8F0; }
.tc-title { font-size: 18px; font-weight: 700; margin: 0; }
.tc-header-right { display: flex; align-items: center; gap: 12px; }
.auto-play-label { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #94A3B8; cursor: pointer; }
.auto-play-label input { accent-color: #4F46E5; }

.tc-body { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.tc-empty { text-align: center; padding: 80px 20px; color: #64748B; }
.tc-empty-hint { font-size: 13px; margin-top: 8px; }

.tc-slide-container { flex: 1; display: flex; flex-direction: column; padding: 24px; }
.tc-slide-area { flex: 1; display: flex; align-items: center; justify-content: center; overflow-y: auto; }
.tc-slide {
  background: #1E293B; border-radius: 12px; padding: 40px;
  max-width: 800px; width: 100%; min-height: 400px;
  border: 1px solid #334155; box-shadow: 0 8px 32px rgba(0,0,0,0.3);
}

.slide-header { margin-bottom: 24px; }
.slide-badge { display: inline-block; padding: 4px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; margin-bottom: 12px; }
.theory-badge { background: #1E3A5F; color: #60A5FA; }
.code-badge { background: #1A3A2A; color: #34D399; }
.quiz-badge { background: #3A2A1A; color: #FBBF24; }
.summary-badge { background: #2A1A3A; color: #A78BFA; }
.slide-title { font-size: 24px; font-weight: 700; margin: 0; color: #F1F5F9; }
.slide-content { font-size: 15px; line-height: 1.8; color: #CBD5E1; }
.slide-content :deep(strong) { color: #F1F5F9; }

.slide-bullets { margin-top: 20px; }
.bullet-item { display: flex; gap: 10px; padding: 8px 0; font-size: 14px; color: #94A3B8; border-bottom: 1px solid #1E293B; }
.bullet-dot { color: #4F46E5; font-weight: bold; }

.code-block { background: #0F172A; padding: 20px; border-radius: 8px; overflow-x: auto; font-size: 14px; line-height: 1.6; }
.code-block code { font-family: 'Consolas', 'Monaco', monospace; color: #E2E8F0; white-space: pre-wrap; }
.code-output { margin-top: 16px; background: #0F172A; border: 1px solid #334155; border-radius: 8px; }
.output-label { padding: 8px 16px; font-size: 12px; color: #64748B; border-bottom: 1px solid #334155; }
.output-content { padding: 16px; margin: 0; font-family: 'Consolas', monospace; font-size: 13px; color: #34D399; white-space: pre-wrap; }

.quiz-list { display: flex; flex-direction: column; gap: 20px; }
.quiz-item { padding: 16px; background: #0F172A; border-radius: 8px; }
.quiz-q { font-weight: 600; margin-bottom: 12px; color: #E2E8F0; }
.quiz-options { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; }
.quiz-option { display: flex; gap: 8px; font-size: 14px; color: #94A3B8; padding: 4px 0; }
.option-key { font-weight: 700; color: #64748B; }
.quiz-answer { font-size: 13px; }
.quiz-answer summary { cursor: pointer; color: #4F46E5; font-weight: 600; padding: 4px 0; }
.answer-content { margin-top: 8px; padding: 12px; background: #1A3A2A; border-radius: 6px; color: #34D399; }

.summary-keypoints { margin-top: 20px; }
.kp-item { padding: 8px 0; font-size: 14px; color: #A78BFA; border-bottom: 1px solid #1E293B; }

.tc-navbar {
  display: flex; align-items: center; justify-content: center; gap: 24px;
  padding: 16px 0; margin-top: 16px;
}
.tc-nav-btn {
  background: #334155; color: #E2E8F0; border: none; padding: 8px 20px;
  border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 600;
  transition: 0.2s;
}
.tc-nav-btn:hover:not(:disabled) { background: #4F46E5; }
.tc-nav-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.tc-dots { display: flex; gap: 8px; }
.tc-dot { width: 8px; height: 8px; border-radius: 50%; background: #475569; cursor: pointer; transition: 0.2s; }
.tc-dot.active { background: #4F46E5; width: 24px; border-radius: 4px; }

.slide-fade-enter-active, .slide-fade-leave-active { transition: all 0.3s ease; }
.slide-fade-enter-from { opacity: 0; transform: translateX(40px); }
.slide-fade-leave-to { opacity: 0; transform: translateX(-40px); }
</style>