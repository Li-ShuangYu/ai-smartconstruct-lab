<template>
  <div class="node-page-layout">
    <!-- Top Progress Bar -->
    <div class="node-page-layout__progress">
      <div
        class="node-page-layout__progress-fill"
        :style="{ width: `${overallProgress}%` }"
      ></div>
    </div>

    <!-- Header -->
    <header class="node-page-layout__header">
      <div class="node-page-layout__header-left">
        <span class="node-page-layout__node-type">{{ nodeTypeName }}</span>
        <h2 class="node-page-layout__node-name">{{ nodeName }}</h2>
      </div>
      <div class="node-page-layout__header-right">
        <span class="node-page-layout__progress-text">
          整体进度 {{ overallProgress }}%
        </span>
      </div>
    </header>

    <!-- Loading State -->
    <div v-if="isLoading" class="node-page-layout__loading">
      <div class="node-page-layout__spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- Content Slot -->
    <main v-else class="node-page-layout__content">
      <slot
        :node-instance-id="nodeInstanceId"
        :node-config="nodeConfig"
        :on-complete="handleComplete"
      ></slot>
    </main>

    <!-- Navigation Footer -->
    <footer class="node-page-layout__footer">
      <button
        class="node-page-layout__nav-btn node-page-layout__nav-btn--prev"
        :disabled="!hasPrevNode"
        @click="handleNavigate('prev')"
      >
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        上一节点
      </button>

      <div class="node-page-layout__footer-info">
        <span class="node-page-layout__phase-name">{{ currentPhaseName }}</span>
      </div>

      <button
        class="node-page-layout__nav-btn node-page-layout__nav-btn--next"
        :disabled="!hasNextNode || !isCurrentNodeComplete"
        @click="handleNavigate('next')"
      >
        下一节点
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </button>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStudentFlowStore } from '@/stores/modules/studentFlow.store'

const props = defineProps<{
  /** Current node instance ID */
  nodeInstanceId: number
  /** Node type identifier */
  nodeType: string
  /** Display name of the node */
  nodeName: string
  /** Node configuration data */
  nodeConfig: Record<string, unknown>
  /** Whether there is a previous node */
  hasPrevNode?: boolean
  /** Whether there is a next node */
  hasNextNode?: boolean
}>()

const emit = defineEmits<{
  navigate: [direction: 'prev' | 'next']
  complete: []
}>()

const studentFlowStore = useStudentFlowStore()

/** Loading state */
const isLoading = ref<boolean>(false)

/** Whether the current node is marked complete */
const isCurrentNodeComplete = ref<boolean>(false)

/** Overall progress from store */
const overallProgress = computed<number>(() => studentFlowStore.overallProgress)

/** Current phase name */
const currentPhaseName = computed<string>(() =>
  studentFlowStore.currentPhase?.phase_name ?? ''
)

/** Node type display name mapping */
const nodeTypeNames: Record<string, string> = {
  start: '开始实训',
  resource_read: '资源阅读',
  homework: '课后作业',
  exam: '考核考试',
  end: '结束实训',
  grouping: '学生分组',
  coding_class: '编码实训',
  learning_survey: '学情调查',
  task_distribute: '任务下发',
  req_upload: '需求上传',
  plan_upload: '方案上传',
  mindmap_preview: '思维导图预习',
  mindmap_draw: '思维导图绘制',
  knowledge_eval: '知识点评价',
  student_peer_review: '学生互评',
  inter_group_review: '组间互评',
  teacher_eval: '教师总评'
}

const nodeTypeName = computed<string>(() =>
  nodeTypeNames[props.nodeType] ?? props.nodeType
)

/** Enter node on mount */
onMounted(async () => {
  isLoading.value = true
  try {
    await studentFlowStore.enterNode(props.nodeInstanceId)
  } finally {
    isLoading.value = false
  }
})

/** Track exit on unmount (no explicit completeNode — that's on submit) */
onUnmounted(() => {
  // Activity state is updated by the store on navigation
})

/** Handle node completion (called by child component) */
async function handleComplete() {
  await studentFlowStore.completeNode(props.nodeInstanceId)
  isCurrentNodeComplete.value = true
  emit('complete')
}

/** Handle navigation */
function handleNavigate(direction: 'prev' | 'next') {
  emit('navigate', direction)
}
</script>

<style scoped>
.node-page-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background: var(--color-gray-50, #f8fafc);
}

/* Progress Bar */
.node-page-layout__progress {
  height: 3px;
  background: var(--color-gray-100, #f1f5f9);
  flex-shrink: 0;
}

.node-page-layout__progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  transition: width 0.5s ease;
}

/* Header */
.node-page-layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem var(--spacing-md, 1rem);
  background: var(--color-white, #ffffff);
  border-bottom: 1px solid var(--color-gray-100, #f1f5f9);
  flex-shrink: 0;
}

.node-page-layout__header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.node-page-layout__node-type {
  font-size: 0.6875rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 0.125rem 0.5rem;
  border-radius: var(--radius-sm, 0.25rem);
  background: var(--color-primary-50, #eef2ff);
  color: var(--color-primary-600, #4f46e5);
}

.node-page-layout__node-name {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
}

.node-page-layout__header-right {
  display: flex;
  align-items: center;
}

.node-page-layout__progress-text {
  font-size: 0.75rem;
  color: var(--color-gray-500, #64748b);
}

/* Loading */
.node-page-layout__loading {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
  font-size: 0.875rem;
}

.node-page-layout__spinner {
  width: 2rem;
  height: 2rem;
  border: 3px solid var(--color-gray-200, #e2e8f0);
  border-top-color: var(--color-primary-500, #6366f1);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Content */
.node-page-layout__content {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* Footer */
.node-page-layout__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.625rem var(--spacing-md, 1rem);
  background: var(--color-white, #ffffff);
  border-top: 1px solid var(--color-gray-100, #f1f5f9);
  flex-shrink: 0;
}

.node-page-layout__nav-btn {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.5rem 1rem;
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-gray-600, #475569);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.node-page-layout__nav-btn:hover:not(:disabled) {
  background: var(--color-gray-50, #f8fafc);
  border-color: var(--color-gray-300, #cbd5e1);
}

.node-page-layout__nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.node-page-layout__nav-btn--next {
  background: var(--color-primary-50, #eef2ff);
  border-color: var(--color-primary-200, #c7d2fe);
  color: var(--color-primary-700, #4338ca);
}

.node-page-layout__nav-btn--next:hover:not(:disabled) {
  background: var(--color-primary-100, #e0e7ff);
}

.node-page-layout__footer-info {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
}

.node-page-layout__phase-name {
  font-weight: 500;
}
</style>
