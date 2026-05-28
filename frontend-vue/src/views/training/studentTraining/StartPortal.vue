<template>
  <div class="start-portal">
    <!-- AI Welcome Section -->
    <section class="start-portal__welcome">
      <div class="start-portal__welcome-icon">
        <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M13 10V3L4 14h7v7l9-11h-7z" />
        </svg>
      </div>
      <div class="start-portal__welcome-content">
        <h3 class="start-portal__welcome-title">AI 导师欢迎您</h3>
        <p class="start-portal__welcome-text">{{ displayWelcomeText }}</p>
      </div>
    </section>

    <!-- Flow Overview Section -->
    <section class="start-portal__overview">
      <h2 class="start-portal__section-title">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M4 6h16M4 12h16m-7 6h7" />
        </svg>
        实训流程概览
      </h2>

      <div class="start-portal__phases">
        <div
          v-for="(phase, phaseIndex) in flowOverview"
          :key="phase.phase_id"
          class="start-portal__phase"
        >
          <div class="start-portal__phase-header">
            <span class="start-portal__phase-badge">阶段 {{ phaseIndex + 1 }}</span>
            <h4 class="start-portal__phase-name">{{ phase.phase_name }}</h4>
            <span class="start-portal__phase-duration">
              预计 {{ phaseTotalDuration(phase) }} 分钟
            </span>
          </div>

          <div class="start-portal__node-list">
            <div
              v-for="(node, nodeIndex) in phase.nodes"
              :key="node.node_id"
              class="start-portal__node-item"
            >
              <div class="start-portal__node-index">{{ nodeIndex + 1 }}</div>
              <div class="start-portal__node-info">
                <span class="start-portal__node-name">{{ node.node_name }}</span>
                <span class="start-portal__node-duration">
                  {{ node.estimated_duration_minutes }} 分钟
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Start Button -->
    <section class="start-portal__action">
      <button
        class="start-portal__start-btn"
        :disabled="isExpired"
        @click="handleStart"
      >
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M13 10V3L4 14h7v7l9-11h-7z" />
        </svg>
        {{ isExpired ? '实训已过期' : '开始实训' }}
      </button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useStudentFlowStore } from '@/stores/modules/studentFlow.store'

interface FlowNode {
  node_id: string
  node_name: string
  node_type: string
  estimated_duration_minutes: number
}

interface FlowPhase {
  phase_id: string
  phase_name: string
  sort_num: number
  nodes: FlowNode[]
}

interface StartNodeConfig {
  welcome_text?: string
  ai_welcome_text?: string
  topic_name?: string
  description?: string
  flow_overview_json?: FlowPhase[]
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: StartNodeConfig
}>()

const emit = defineEmits<{
  complete: []
  navigate: [direction: 'next']
}>()

const studentFlowStore = useStudentFlowStore()

/** AI welcome text with fallback to teacher description */
const displayWelcomeText = computed<string>(() => {
  if (props.nodeConfig.ai_welcome_text) {
    return props.nodeConfig.ai_welcome_text
  }
  if (props.nodeConfig.welcome_text) {
    return props.nodeConfig.welcome_text
  }
  return props.nodeConfig.description ?? '欢迎参加本次实训，请按照流程逐步完成各环节学习。'
})

/** Flow overview phases from config */
const flowOverview = computed<FlowPhase[]>(() => {
  return props.nodeConfig.flow_overview_json ?? placeholderPhases.value
})

/** Whether the training is expired */
const isExpired = computed<boolean>(() => studentFlowStore.isExpired)

/** Placeholder phases for scaffold rendering */
const placeholderPhases = ref<FlowPhase[]>([
  {
    phase_id: 'phase_pre',
    phase_name: '课前阶段',
    sort_num: 1,
    nodes: [
      { node_id: 'n1', node_name: '开始实训', node_type: 'start', estimated_duration_minutes: 5 },
      { node_id: 'n2', node_name: '课件阅读', node_type: 'resource_read', estimated_duration_minutes: 20 },
      { node_id: 'n3', node_name: '学情调查', node_type: 'learning_survey', estimated_duration_minutes: 10 }
    ]
  },
  {
    phase_id: 'phase_mid',
    phase_name: '课中阶段',
    sort_num: 2,
    nodes: [
      { node_id: 'n4', node_name: '任务下发', node_type: 'task_distribute', estimated_duration_minutes: 5 },
      { node_id: 'n5', node_name: '编码实训', node_type: 'coding_class', estimated_duration_minutes: 45 },
      { node_id: 'n6', node_name: '课后作业', node_type: 'homework', estimated_duration_minutes: 30 }
    ]
  },
  {
    phase_id: 'phase_post',
    phase_name: '课后阶段',
    sort_num: 3,
    nodes: [
      { node_id: 'n7', node_name: '学生互评', node_type: 'student_peer_review', estimated_duration_minutes: 15 },
      { node_id: 'n8', node_name: '结束实训', node_type: 'end', estimated_duration_minutes: 5 }
    ]
  }
])

/** Calculate total duration for a phase */
function phaseTotalDuration(phase: FlowPhase): number {
  return phase.nodes.reduce((sum, n) => sum + n.estimated_duration_minutes, 0)
}

/** Handle start button click */
async function handleStart() {
  emit('complete')
  emit('navigate', 'next')
}

onMounted(() => {
  // Enter node tracking is handled by NodePageLayout wrapper
})
</script>

<style scoped>
.start-portal {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
}

.start-portal__welcome {
  display: flex;
  gap: var(--spacing-md, 1rem);
  padding: var(--spacing-lg, 1.5rem);
  background: var(--color-primary-50, #eef2ff);
  border: 1px solid var(--color-primary-100, #e0e7ff);
  border-left: 4px solid var(--color-primary-500, #6366f1);
  border-radius: var(--radius-lg, 0.75rem);
}

.start-portal__welcome-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: var(--radius-md, 0.5rem);
  background: var(--color-primary-500, #6366f1);
  color: white;
  flex-shrink: 0;
}

.start-portal__welcome-content {
  flex: 1;
}

.start-portal__welcome-title {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: 0.5rem;
}

.start-portal__welcome-text {
  font-size: 0.9375rem;
  line-height: 1.6;
  color: var(--color-gray-600, #475569);
  font-style: italic;
}

.start-portal__overview {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.start-portal__section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-md, 1rem);
}

.start-portal__phases {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md, 1rem);
}

.start-portal__phase {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  overflow: hidden;
}

.start-portal__phase-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm, 0.75rem);
  padding: var(--spacing-sm, 0.75rem) var(--spacing-md, 1rem);
  background: var(--color-gray-50, #f8fafc);
  border-bottom: 1px solid var(--color-gray-100, #f1f5f9);
}

.start-portal__phase-badge {
  font-size: 0.6875rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 0.125rem 0.5rem;
  border-radius: var(--radius-sm, 0.25rem);
  background: var(--color-primary-100, #e0e7ff);
  color: var(--color-primary-700, #4338ca);
}

.start-portal__phase-name {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  flex: 1;
}

.start-portal__phase-duration {
  font-size: 0.75rem;
  color: var(--color-gray-500, #64748b);
}

.start-portal__node-list {
  padding: var(--spacing-sm, 0.75rem) var(--spacing-md, 1rem);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.start-portal__node-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm, 0.75rem);
  padding: 0.5rem 0;
}

.start-portal__node-index {
  width: 1.5rem;
  height: 1.5rem;
  border-radius: 50%;
  background: var(--color-gray-100, #f1f5f9);
  color: var(--color-gray-500, #64748b);
  font-size: 0.6875rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.start-portal__node-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
}

.start-portal__node-name {
  font-size: 0.875rem;
  color: var(--color-gray-700, #334155);
}

.start-portal__node-duration {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
}

.start-portal__action {
  padding-top: var(--spacing-md, 1rem);
  border-top: 1px solid var(--color-gray-100, #f1f5f9);
}

.start-portal__start-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.875rem 1.5rem;
  font-size: 1rem;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  border: none;
  border-radius: var(--radius-lg, 0.75rem);
  cursor: pointer;
  transition: all 0.2s ease;
}

.start-portal__start-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px -4px rgba(99, 102, 241, 0.4);
}

.start-portal__start-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
