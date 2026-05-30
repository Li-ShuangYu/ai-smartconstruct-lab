<template>
  <div class="training-preview">
    <!-- 加载状态 -->
    <div v-if="store.loading" class="loading-container">
      <n-spin size="large" />
      <p class="loading-text">加载实训数据中...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="store.error" class="error-container">
      <div class="error-icon">⚠️</div>
      <p class="error-message">{{ store.error }}</p>
      <n-button type="primary" @click="handleRetry">重试</n-button>
    </div>

    <!-- 正常内容 -->
    <template v-else-if="store.taskOverview">
      <header class="preview-header">
        <h1 class="preview-title">{{ store.taskOverview.task_name }}</h1>
        <div class="preview-meta">
          <span class="meta-item">共 {{ totalPhases }} 个阶段</span>
          <span class="meta-divider">·</span>
          <span class="meta-item">{{ totalNodes }} 个节点</span>
          <span class="meta-divider">·</span>
          <span class="meta-item">进度 {{ store.overallProgress }}%</span>
        </div>
        <n-progress
          type="line"
          :percentage="store.overallProgress"
          :show-indicator="false"
          class="overall-progress"
        />
      </header>

      <main class="phases-list">
        <div
          v-for="phase in sortedPhases"
          :key="phase.phase_id"
          class="phase-card"
          :class="{
            'phase-card--locked': !phase.is_unlocked,
            'phase-card--complete': phase.is_complete,
            'phase-card--active': phase.is_unlocked && !phase.is_complete
          }"
          @click="handlePhaseClick(phase)"
        >
          <div class="phase-header">
            <div class="phase-status-icon">
              <span v-if="phase.is_complete" class="icon-complete">✅</span>
              <span v-else-if="!phase.is_unlocked" class="icon-locked">🔒</span>
              <span v-else class="icon-active">▶️</span>
            </div>
            <div class="phase-info">
              <h3 class="phase-name">{{ phase.phase_name }}</h3>
              <p class="phase-stats">
                {{ phase.completed_nodes }} / {{ phase.total_nodes }} 节点已完成
              </p>
            </div>
            <div class="phase-progress-badge">
              {{ phasePercentage(phase) }}%
            </div>
          </div>

          <div class="phase-nodes">
            <div
              v-for="node in sortedNodes(phase)"
              :key="node.node_instance_id"
              class="node-item"
              :class="{
                'node-item--completed': node.status === 2,
                'node-item--active': node.status === 1,
                'node-item--skipped': node.status === 3
              }"
            >
              <span class="node-dot" />
              <span class="node-name">{{ node.node_name }}</span>
              <span class="node-type-badge">{{ nodeTypeLabel(node.node_type) }}</span>
              <span v-if="node.is_required" class="node-required">必修</span>
            </div>
          </div>
        </div>
      </main>

      <footer class="preview-footer">
        <n-button @click="router.back()">返回</n-button>
        <n-button
          v-if="store.taskOverview.participation.status === 0"
          type="primary"
          size="large"
          :loading="starting"
          @click="handleStart"
        >
          开始实训
        </n-button>
        <n-button
          v-else-if="store.taskOverview.participation.status === 1"
          type="primary"
          size="large"
          @click="handleContinue"
        >
          继续实训
        </n-button>
        <n-button
          v-else-if="store.taskOverview.participation.status === 2"
          type="info"
          size="large"
          @click="handleContinue"
        >
          查看实训
        </n-button>
      </footer>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 学生实训预览页面
 *
 * 展示实训任务的所有阶段列表、解锁状态和进度。
 * 学生可以点击已解锁的阶段进入实训执行页面。
 *
 * Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6
 *
 * @component TrainingPreview.vue
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NSpin, NProgress, useMessage } from 'naive-ui'
import { useStudentFlowStore } from '@/stores/modules/studentFlow.store'
import type { PhaseProgress, NodeInstanceProgress } from '@/services/types/studentTraining'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const store = useStudentFlowStore()

const starting = ref(false)

/** 从路由获取 taskId */
const taskId = computed(() => {
  const id = route.params.taskId || route.query.taskId
  return Number(id) || 0
})

/** 总阶段数 */
const totalPhases = computed(() => store.phases.length)

/** 总节点数 */
const totalNodes = computed(() =>
  store.phases.reduce((sum, p) => sum + p.total_nodes, 0)
)

/** 按 sort_num 排序的阶段列表 */
const sortedPhases = computed(() =>
  [...store.phases].sort((a, b) => a.sort_num - b.sort_num)
)

/** 节点类型标签映射 */
const nodeTypeLabels: Record<string, string> = {
  start: '起点',
  end: '终点',
  grouping: '分组',
  resource_read: '资源阅读',
  theory_class: '理论实训',
  coding_class: '编码实训',
  learning_survey: '学情调查',
  task_distribute: '任务下发',
  req_upload: '需求上传',
  plan_upload: '方案上传',
  homework: '课后作业',
  exam: '考核考试',
  mindmap_preview: '思维导图预习',
  mindmap_draw: '思维导图绘制',
  knowledge_eval: '知识评价',
  student_peer_review: '学生互评',
  inter_group_review: '组间互评',
  teacher_eval: '教师总评'
}

function nodeTypeLabel(nodeType: string): string {
  return nodeTypeLabels[nodeType] || nodeType
}

/** 获取阶段内按 sort_num 排序的节点 */
function sortedNodes(phase: PhaseProgress): NodeInstanceProgress[] {
  return [...phase.nodes].sort((a, b) => a.sort_num - b.sort_num)
}

/** 获取阶段完成百分比 */
function phasePercentage(phase: PhaseProgress): number {
  return store.phaseCompletionPercentage[phase.phase_id] ?? 0
}

/** 阶段点击处理 */
function handlePhaseClick(phase: PhaseProgress): void {
  if (!phase.is_unlocked) {
    // 找到未完成的前序阶段
    const sorted = sortedPhases.value
    const phaseIndex = sorted.findIndex(p => p.phase_id === phase.phase_id)
    const blockerPhase = sorted.slice(0, phaseIndex).find(p => !p.is_complete)
    if (blockerPhase) {
      message.warning(`请先完成「${blockerPhase.phase_name}」阶段`)
    } else {
      message.warning('该阶段尚未解锁')
    }
    return
  }

  // 已解锁：导航到执行页面，定位到该阶段的第一个未完成节点
  const nodes = sortedNodes(phase)
  const targetNode = nodes.find(n => n.status !== 2) || nodes[0]
  if (targetNode) {
    router.push({
      path: `/student/training/${taskId.value}/execute`,
      query: {
        phaseId: phase.phase_id,
        nodeId: String(targetNode.node_instance_id)
      }
    })
  }
}

/** 开始实训 */
async function handleStart(): Promise<void> {
  starting.value = true
  try {
    await store.startTraining()
    if (!store.error) {
      router.push({
        path: `/student/training/${taskId.value}/execute`
      })
    } else {
      message.error(store.error)
    }
  } finally {
    starting.value = false
  }
}

/** 继续实训 */
function handleContinue(): void {
  router.push({
    path: `/student/training/${taskId.value}/execute`
  })
}

/** 重试加载 */
function handleRetry(): void {
  if (taskId.value) {
    store.loadTaskOverview(taskId.value)
  }
}

onMounted(() => {
  if (taskId.value) {
    store.loadTaskOverview(taskId.value)
  }
})
</script>

<style scoped>
.training-preview {
  max-width: 900px;
  margin: 0 auto;
  padding: var(--section-gap, 32px) 24px;
  min-height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
}

/* 加载状态 */
.loading-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.loading-text {
  color: var(--color-text);
  opacity: 0.6;
  font-size: 14px;
}

/* 错误状态 */
.error-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.error-icon {
  font-size: 48px;
}

.error-message {
  color: var(--color-text);
  font-size: 15px;
  text-align: center;
  max-width: 400px;
}

/* 头部 */
.preview-header {
  text-align: center;
  margin-bottom: 32px;
}

.preview-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--color-heading);
  margin: 0 0 8px 0;
}

.preview-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
}

.meta-item {
  color: var(--color-text);
  opacity: 0.7;
  font-size: 14px;
}

.meta-divider {
  color: var(--color-border);
}

.overall-progress {
  max-width: 400px;
  margin: 0 auto;
}

/* 阶段列表 */
.phases-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.phase-card {
  background: var(--color-background-soft, #ffffff);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s;
}

.phase-card:hover {
  border-color: var(--color-border-hover);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.phase-card--locked {
  opacity: 0.6;
  cursor: not-allowed;
}

.phase-card--locked:hover {
  box-shadow: none;
  border-color: var(--color-border);
}

.phase-card--complete {
  border-left: 4px solid #10b981;
}

.phase-card--active {
  border-left: 4px solid #6366f1;
}

.phase-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.phase-status-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.phase-info {
  flex: 1;
}

.phase-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-heading);
  margin: 0 0 4px 0;
}

.phase-stats {
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.6;
  margin: 0;
}

.phase-progress-badge {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  background: var(--color-background-mute, #f2f2f2);
  padding: 4px 10px;
  border-radius: 12px;
}

/* 节点列表 */
.phase-nodes {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-left: 32px;
}

.node-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text);
  opacity: 0.8;
}

.node-item--completed {
  opacity: 0.5;
  text-decoration: line-through;
}

.node-item--active {
  opacity: 1;
  font-weight: 600;
}

.node-item--skipped {
  opacity: 0.4;
  text-decoration: line-through;
}

.node-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-border-hover);
  flex-shrink: 0;
}

.node-item--completed .node-dot {
  background: #10b981;
}

.node-item--active .node-dot {
  background: #6366f1;
}

.node-name {
  flex: 1;
}

.node-type-badge {
  font-size: 11px;
  color: var(--color-text);
  opacity: 0.5;
  background: var(--color-background-mute, #f2f2f2);
  padding: 2px 6px;
  border-radius: 4px;
}

.node-required {
  font-size: 11px;
  color: #ef4444;
  font-weight: 500;
}

/* 底部 */
.preview-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-top: 1px solid var(--color-border);
  margin-top: 32px;
}
</style>
