<template>
  <div class="resource-viewer">
    <!-- Main Reading Area -->
    <div class="resource-viewer__main">
      <!-- Reading Progress Bar -->
      <div class="resource-viewer__progress-bar">
        <div
          class="resource-viewer__progress-fill"
          :style="{ width: `${readingProgress}%` }"
        ></div>
      </div>

      <!-- Document Viewer Placeholder -->
      <div class="resource-viewer__document">
        <div class="resource-viewer__document-header">
          <span class="resource-viewer__document-title">{{ resourceName }}</span>
          <span class="resource-viewer__document-type">{{ resourceType }}</span>
        </div>
        <div class="resource-viewer__document-body">
          <div class="resource-viewer__placeholder">
            <svg class="w-16 h-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <p>文档渲染区域</p>
            <p class="resource-viewer__placeholder-hint">
              此处将嵌入 PDF/PPT/文档阅读器
            </p>
          </div>
        </div>
      </div>

      <!-- Knowledge Point Highlights -->
      <div v-if="knowledgePoints.length > 0" class="resource-viewer__knowledge">
        <h4 class="resource-viewer__knowledge-title">
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
          </svg>
          关键知识点
        </h4>
        <div class="resource-viewer__knowledge-list">
          <div
            v-for="(point, index) in knowledgePoints"
            :key="index"
            class="resource-viewer__knowledge-item"
            @click="handleKnowledgePointClick(point, index)"
          >
            <span class="resource-viewer__knowledge-dot"></span>
            <span class="resource-viewer__knowledge-text">{{ point.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- AI Summary Sidebar -->
    <aside
      v-if="showAiSidebar"
      class="resource-viewer__sidebar"
      :class="{ 'resource-viewer__sidebar--collapsed': isSidebarCollapsed }"
    >
      <button
        class="resource-viewer__sidebar-toggle"
        @click="isSidebarCollapsed = !isSidebarCollapsed"
      >
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            :d="isSidebarCollapsed ? 'M15 19l-7-7 7-7' : 'M9 5l7 7-7 7'" />
        </svg>
      </button>

      <div v-show="!isSidebarCollapsed" class="resource-viewer__sidebar-content">
        <div class="resource-viewer__sidebar-header">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
          <h3>AI 摘要</h3>
        </div>

        <!-- Summary Section -->
        <div class="resource-viewer__ai-section">
          <h4 class="resource-viewer__ai-section-title">核心摘要</h4>
          <p class="resource-viewer__ai-section-text">{{ aiSummary }}</p>
        </div>

        <!-- Key Points Section -->
        <div v-if="aiKeyPoints.length > 0" class="resource-viewer__ai-section">
          <h4 class="resource-viewer__ai-section-title">提炼重点</h4>
          <ul class="resource-viewer__ai-points">
            <li v-for="(point, idx) in aiKeyPoints" :key="idx">
              {{ point }}
            </li>
          </ul>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

interface KnowledgePoint {
  name: string
  position_id?: string
}

interface ResourceViewerConfig {
  resource_id?: number
  resource_name?: string
  resource_type?: string
  enable_ai_summary?: boolean
  enable_ai_key_points?: boolean
  min_reading_duration_minutes?: number
  ai_summary?: string
  ai_key_points?: string[]
  knowledge_points?: KnowledgePoint[]
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: ResourceViewerConfig
}>()

const emit = defineEmits<{
  complete: []
  navigate: [direction: 'next']
}>()

/** Reading progress (0-100) */
const readingProgress = ref<number>(0)

/** Sidebar collapsed state */
const isSidebarCollapsed = ref<boolean>(false)

/** Reading timer */
let readingTimer: ReturnType<typeof setInterval> | null = null
const readingDurationSeconds = ref<number>(0)

/** Resource info */
const resourceName = computed<string>(() =>
  props.nodeConfig.resource_name ?? '教学资源文档'
)

const resourceType = computed<string>(() =>
  props.nodeConfig.resource_type ?? 'PDF'
)

/** Whether to show AI sidebar */
const showAiSidebar = computed<boolean>(() =>
  props.nodeConfig.enable_ai_summary === true ||
  props.nodeConfig.enable_ai_key_points === true
)

/** AI summary text */
const aiSummary = computed<string>(() =>
  props.nodeConfig.ai_summary ?? 'AI摘要暂不可用'
)

/** AI key points */
const aiKeyPoints = computed<string[]>(() =>
  props.nodeConfig.ai_key_points ?? []
)

/** Knowledge points */
const knowledgePoints = computed<KnowledgePoint[]>(() =>
  props.nodeConfig.knowledge_points ?? [
    { name: '数组的基本概念与内存分配', position_id: 'p1' },
    { name: '动态数组扩容机制', position_id: 'p2' },
    { name: '时间复杂度分析', position_id: 'p3' }
  ]
)

/** Handle knowledge point click */
function handleKnowledgePointClick(point: KnowledgePoint, _index: number) {
  // Track click for data collection
  console.log('Knowledge point clicked:', point.name)
}

/** Start reading timer */
onMounted(() => {
  readingTimer = setInterval(() => {
    readingDurationSeconds.value++
    // Simulate progress increase
    if (readingProgress.value < 100) {
      readingProgress.value = Math.min(
        readingProgress.value + 0.5,
        100
      )
    }
  }, 1000)
})

/** Cleanup timer */
onUnmounted(() => {
  if (readingTimer) {
    clearInterval(readingTimer)
    readingTimer = null
  }
})
</script>

<style scoped>
.resource-viewer {
  display: flex;
  height: 100%;
  gap: 0;
  overflow: hidden;
}

.resource-viewer__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.resource-viewer__progress-bar {
  height: 4px;
  background: var(--color-gray-100, #f1f5f9);
  flex-shrink: 0;
}

.resource-viewer__progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  transition: width 0.3s ease;
  border-radius: 0 2px 2px 0;
}

.resource-viewer__document {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.resource-viewer__document-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm, 0.75rem) var(--spacing-md, 1rem);
  border-bottom: 1px solid var(--color-gray-100, #f1f5f9);
  flex-shrink: 0;
}

.resource-viewer__document-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-gray-700, #334155);
}

.resource-viewer__document-type {
  font-size: 0.6875rem;
  font-weight: 700;
  text-transform: uppercase;
  padding: 0.125rem 0.5rem;
  border-radius: var(--radius-sm, 0.25rem);
  background: var(--color-gray-100, #f1f5f9);
  color: var(--color-gray-500, #64748b);
}

.resource-viewer__document-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow-y: auto;
}

.resource-viewer__placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
}

.resource-viewer__placeholder p {
  font-size: 0.875rem;
}

.resource-viewer__placeholder-hint {
  font-size: 0.75rem;
  color: var(--color-gray-300, #cbd5e1);
}

.resource-viewer__knowledge {
  padding: var(--spacing-md, 1rem);
  border-top: 1px solid var(--color-gray-100, #f1f5f9);
  flex-shrink: 0;
}

.resource-viewer__knowledge-title {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.8125rem;
  font-weight: 700;
  color: var(--color-gray-700, #334155);
  margin-bottom: 0.5rem;
}

.resource-viewer__knowledge-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.resource-viewer__knowledge-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.25rem 0.75rem;
  background: var(--color-primary-50, #eef2ff);
  border: 1px solid var(--color-primary-100, #e0e7ff);
  border-radius: var(--radius-full, 9999px);
  cursor: pointer;
  transition: background 0.15s ease;
}

.resource-viewer__knowledge-item:hover {
  background: var(--color-primary-100, #e0e7ff);
}

.resource-viewer__knowledge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary-500, #6366f1);
}

.resource-viewer__knowledge-text {
  font-size: 0.75rem;
  color: var(--color-primary-700, #4338ca);
}

/* Sidebar */
.resource-viewer__sidebar {
  width: 280px;
  border-left: 1px solid var(--color-gray-100, #f1f5f9);
  display: flex;
  flex-direction: column;
  position: relative;
  flex-shrink: 0;
  transition: width 0.2s ease;
}

.resource-viewer__sidebar--collapsed {
  width: 2.5rem;
}

.resource-viewer__sidebar-toggle {
  position: absolute;
  top: 0.75rem;
  left: -0.75rem;
  width: 1.5rem;
  height: 1.5rem;
  border-radius: 50%;
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 1;
  color: var(--color-gray-500, #64748b);
}

.resource-viewer__sidebar-toggle:hover {
  background: var(--color-gray-50, #f8fafc);
}

.resource-viewer__sidebar-content {
  padding: var(--spacing-md, 1rem);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md, 1rem);
}

.resource-viewer__sidebar-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--color-primary-600, #4f46e5);
  font-weight: 700;
  font-size: 0.9375rem;
}

.resource-viewer__ai-section {
  background: var(--color-gray-50, #f8fafc);
  border: 1px solid var(--color-gray-100, #f1f5f9);
  border-radius: var(--radius-md, 0.5rem);
  padding: var(--spacing-sm, 0.75rem);
}

.resource-viewer__ai-section-title {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--color-gray-600, #475569);
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.resource-viewer__ai-section-text {
  font-size: 0.8125rem;
  line-height: 1.6;
  color: var(--color-gray-600, #475569);
}

.resource-viewer__ai-points {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.resource-viewer__ai-points li {
  font-size: 0.8125rem;
  color: var(--color-gray-600, #475569);
  padding-left: 0.75rem;
  position: relative;
}

.resource-viewer__ai-points li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--color-primary-400, #818cf8);
  font-weight: 700;
}
</style>
