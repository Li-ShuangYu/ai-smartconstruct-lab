<template>
  <div class="resource-viewer">
    <!-- Top Progress Bar -->
    <div class="resource-viewer__progress-bar">
      <div class="resource-viewer__progress-fill" :style="{ width: `${readingProgress}%` }"></div>
    </div>

    <div class="resource-viewer__layout">
      <!-- Left: Main Document Area -->
      <div class="resource-viewer__left">
        <!-- Document Header -->
        <div class="resource-viewer__doc-header">
          <div class="resource-viewer__doc-header-left">
            <svg class="resource-viewer__doc-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <div>
              <span class="resource-viewer__doc-title">{{ resourceName }}</span>
              <span class="resource-viewer__doc-meta">{{ resourceType }} · {{ wordCount }} 字</span>
            </div>
          </div>
          <span class="resource-viewer__doc-badge">只读</span>
        </div>

        <!-- Document Viewer (fixed height, scrollable) -->
        <div class="resource-viewer__doc-viewer" ref="docViewerRef" @scroll="handleScroll">
          <div class="resource-viewer__doc-paper">
            <div class="resource-viewer__markdown" v-html="renderedMarkdown"></div>
          </div>
        </div>

        <!-- Knowledge Points -->
        <div class="resource-viewer__knowledge-section">
          <h4 class="resource-viewer__knowledge-title">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
            </svg>
            关键知识点
          </h4>
          <div class="resource-viewer__knowledge-list">
            <div v-for="(point, i) in knowledgePoints" :key="i" class="resource-viewer__knowledge-tag">
              <span class="resource-viewer__knowledge-dot"></span>
              {{ point.name || point.label }}
            </div>
          </div>
        </div>
      </div>

      <!-- Right: AI Sidebar -->
      <aside class="resource-viewer__sidebar">
        <div class="resource-viewer__sidebar-header">
          <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
          <h3>AI 智能分析</h3>
        </div>
        <div class="resource-viewer__sidebar-body">
          <div class="resource-viewer__ai-card">
            <div class="resource-viewer__ai-card-title">核心摘要</div>
            <p class="resource-viewer__ai-card-text">{{ aiSummary }}</p>
          </div>
          <div v-if="aiKeyPoints.length > 0" class="resource-viewer__ai-card">
            <div class="resource-viewer__ai-card-title">重点提炼</div>
            <ul class="resource-viewer__ai-bullets">
              <li v-for="(pt, i) in aiKeyPoints" :key="i">{{ pt }}</li>
            </ul>
          </div>
        </div>
      </aside>
    </div>

    <!-- Bottom Action Bar -->
    <div class="resource-viewer__actions">
      <div class="resource-viewer__actions-left">
        <span class="resource-viewer__reading-status">
          <span class="resource-viewer__status-dot" :class="{ 'resource-viewer__status-dot--done': readingProgress >= 100 }"></span>
          阅读进度 {{ Math.round(readingProgress) }}%
        </span>
      </div>
      <button class="resource-viewer__complete-btn" @click="handleComplete">
        已完成阅读，进入下一阶段
        <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import mdContent from '@/assets/docs/Python算法入门.md?raw'

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: {
    resource_name?: string
    resource_type?: string
    enable_ai_summary?: boolean
    enable_ai_key_points?: boolean
    ai_summary?: string
    ai_key_points?: string[]
    knowledge_points?: Array<{ name?: string; label?: string }>
  }
}>()

const emit = defineEmits<{
  complete: []
}>()

const readingProgress = ref(0)
const renderedMarkdown = ref('')
const docViewerRef = ref<HTMLElement | null>(null)
let progressTimer: ReturnType<typeof setInterval> | null = null

const resourceName = computed(() => props.nodeConfig.resource_name ?? 'Python算法入门')
const resourceType = computed(() => props.nodeConfig.resource_type ?? 'PDF')
const wordCount = computed(() => mdContent.replace(/[#*\n`\s]/g, '').length)

const aiSummary = computed(() =>
  props.nodeConfig.ai_summary ?? '本文档系统介绍了Python核心算法：从基础的时间复杂度分析，到排序（冒泡、选择、插入、快排）、查找（线性、二分）、递归、动态规划、贪心及图算法等经典内容，配有完整的代码示例和运行结果。'
)
const aiKeyPoints = computed<string[]>(() =>
  props.nodeConfig.ai_key_points ?? [
    'O(n²)与O(nlogn)排序算法的适用场景选择',
    '递归三要素：终止条件、递归调用、问题分解',
    '动态规划的核心：状态转移方程与备忘录优化'
  ]
)
const knowledgePoints = computed(() =>
  (props.nodeConfig.knowledge_points ?? []).length > 0
    ? props.nodeConfig.knowledge_points
    : [
      { name: '时间复杂度分析' },
      { name: '冒泡排序原理' },
      { name: '快速排序分治思想' },
      { name: '递归与递推关系' },
      { name: '动态规划状态转移' },
      { name: '贪心算法局部最优' }
    ]
)

function renderMarkdown(text: string): string {
  let html = text
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/\n/g, '<br>')
  return '<p>' + html + '</p>'
}

function handleScroll() {
  const el = docViewerRef.value
  if (!el) return
  const scrollPct = (el.scrollTop / (el.scrollHeight - el.clientHeight)) * 100
  readingProgress.value = Math.min(Math.round(scrollPct), 100)
}

function handleComplete() {
  emit('complete')
}

onMounted(() => {
  renderedMarkdown.value = renderMarkdown(mdContent)
  progressTimer = setInterval(() => {
    if (readingProgress.value < 100) {
      readingProgress.value = Math.min(readingProgress.value + 1, 100)
    }
  }, 1000)
})

onUnmounted(() => {
  if (progressTimer) clearInterval(progressTimer)
})
</script>

<style scoped>
.resource-viewer {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f1f5f9;
  position: relative;
}

/* ── Progress Bar ── */
.resource-viewer__progress-bar {
  height: 4px;
  background: #e2e8f0;
  flex-shrink: 0;
}
.resource-viewer__progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  transition: width 0.3s ease;
  border-radius: 0 2px 2px 0;
}

/* ── Main Layout ── */
.resource-viewer__layout {
  flex: 1;
  display: flex;
  min-height: 0;
  overflow: hidden;
}

.resource-viewer__left {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

/* ── Document Header ── */
.resource-viewer__doc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}
.resource-viewer__doc-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.resource-viewer__doc-icon {
  width: 28px;
  height: 28px;
  color: #6366f1;
}
.resource-viewer__doc-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  display: block;
}
.resource-viewer__doc-meta {
  font-size: 11px;
  color: #94a3b8;
}
.resource-viewer__doc-badge {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
  background: #f1f5f9;
  padding: 2px 10px;
  border-radius: 4px;
}

/* ── Document Viewer (fixed height paper) ── */
.resource-viewer__doc-viewer {
  flex: 1;
  overflow-y: auto;
  padding: 24px 48px;
  min-height: 0;
  max-height: calc(100vh - 320px);
}
.resource-viewer__doc-paper {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 4px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.08), 0 1px 2px rgba(0,0,0,0.06);
  padding: 40px 48px;
}

/* ── Markdown Content ── */
.resource-viewer__markdown {
  font-size: 14px;
  line-height: 1.9;
  color: #334155;
}
.resource-viewer__markdown h1 {
  font-size: 24px; font-weight: 800; color: #0f172a;
  margin: 0 0 16px; padding-bottom: 8px;
  border-bottom: 2px solid #e2e8f0;
}
.resource-viewer__markdown h2 {
  font-size: 18px; font-weight: 700; color: #1e293b;
  margin: 24px 0 12px;
}
.resource-viewer__markdown h3 {
  font-size: 15px; font-weight: 600; color: #334155;
  margin: 16px 0 8px;
}
.resource-viewer__markdown p { margin: 10px 0; }
.resource-viewer__markdown strong { color: #0f172a; font-weight: 700; }
.resource-viewer__markdown code {
  background: #f1f5f9;
  color: #6366f1;
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 13px;
  font-family: 'Consolas', 'Courier New', monospace;
}
.resource-viewer__markdown pre {
  background: #0f172a;
  color: #e2e8f0;
  padding: 16px 20px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
}
.resource-viewer__markdown pre code {
  background: transparent;
  color: inherit;
  padding: 0;
  font-size: 13px;
  line-height: 1.7;
}
.resource-viewer__markdown li { margin: 4px 0; padding-left: 4px; }

/* ── Knowledge Points Section ── */
.resource-viewer__knowledge-section {
  padding: 12px 24px;
  background: white;
  border-top: 1px solid #e2e8f0;
  flex-shrink: 0;
}
.resource-viewer__knowledge-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 10px;
}
.resource-viewer__knowledge-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.resource-viewer__knowledge-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: #eef2ff;
  border: 1px solid #e0e7ff;
  border-radius: 20px;
  font-size: 12px;
  color: #4338ca;
  cursor: default;
  transition: background 0.15s;
}
.resource-viewer__knowledge-tag:hover {
  background: #e0e7ff;
}
.resource-viewer__knowledge-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: #6366f1; flex-shrink: 0;
}

/* ── Right Sidebar ── */
.resource-viewer__sidebar {
  width: 280px;
  background: white;
  border-left: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
}
.resource-viewer__sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-bottom: 1px solid #e2e8f0;
  color: #6366f1;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}
.resource-viewer__sidebar-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.resource-viewer__ai-card {
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  padding: 12px;
}
.resource-viewer__ai-card-title {
  font-size: 11px;
  font-weight: 700;
  color: #475569;
  text-transform: uppercase;
  margin-bottom: 6px;
}
.resource-viewer__ai-card-text {
  font-size: 13px;
  line-height: 1.6;
  color: #334155;
  margin: 0;
}
.resource-viewer__ai-bullets {
  list-style: none; padding: 0; margin: 0;
}
.resource-viewer__ai-bullets li {
  font-size: 13px; color: #334155;
  padding: 3px 0 3px 14px; position: relative;
}
.resource-viewer__ai-bullets li::before {
  content: '•'; position: absolute; left: 0; color: #6366f1;
}

/* ── Bottom Action Bar ── */
.resource-viewer__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: white;
  border-top: 1px solid #e2e8f0;
  flex-shrink: 0;
}
.resource-viewer__actions-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.resource-viewer__reading-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
}
.resource-viewer__status-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #f59e0b;
}
.resource-viewer__status-dot--done {
  background: #10b981;
}
.resource-viewer__complete-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 28px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}
.resource-viewer__complete-btn:hover {
  box-shadow: 0 4px 14px -2px rgba(99, 102, 241, 0.5);
}
</style>
