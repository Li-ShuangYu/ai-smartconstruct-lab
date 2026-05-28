<template>
  <div class="training-monitor">
    <!-- Header -->
    <header class="training-monitor__header">
      <div class="training-monitor__header-left">
        <h1 class="training-monitor__title">实训监控面板</h1>
        <span class="training-monitor__task-name">{{ taskName }}</span>
      </div>
      <div class="training-monitor__header-right">
        <div class="training-monitor__stat-badge">
          <span class="training-monitor__stat-label">总人数</span>
          <span class="training-monitor__stat-value">{{ totalStudents }}</span>
        </div>
        <div class="training-monitor__stat-badge training-monitor__stat-badge--active">
          <span class="training-monitor__stat-label">在线</span>
          <span class="training-monitor__stat-value">{{ onlineStudents }}</span>
        </div>
        <div class="training-monitor__stat-badge training-monitor__stat-badge--complete">
          <span class="training-monitor__stat-label">已完成</span>
          <span class="training-monitor__stat-value">{{ completedStudents }}</span>
        </div>
      </div>
    </header>

    <!-- Phase Progress Overview -->
    <section class="training-monitor__phases">
      <h2 class="training-monitor__section-title">阶段进度概览</h2>
      <div class="training-monitor__phase-list">
        <div
          v-for="phase in phases"
          :key="phase.phaseId"
          class="training-monitor__phase-card"
          :class="{ 'training-monitor__phase-card--active': phase.isActive }"
        >
          <div class="training-monitor__phase-header">
            <span class="training-monitor__phase-name">{{ phase.phaseName }}</span>
            <span class="training-monitor__phase-progress">{{ phase.completedCount }}/{{ totalStudents }}</span>
          </div>
          <div class="training-monitor__phase-bar">
            <div
              class="training-monitor__phase-fill"
              :style="{ width: `${phase.progressPercent}%` }"
            ></div>
          </div>
          <span class="training-monitor__phase-percent">{{ phase.progressPercent }}%</span>
        </div>
      </div>
    </section>

    <!-- Per-Node Student Distribution -->
    <section class="training-monitor__nodes">
      <h2 class="training-monitor__section-title">节点学生分布</h2>
      <div class="training-monitor__node-grid">
        <div
          v-for="node in nodeDistribution"
          :key="node.nodeInstanceId"
          class="training-monitor__node-card"
        >
          <div class="training-monitor__node-header">
            <span class="training-monitor__node-type-badge">{{ node.nodeTypeName }}</span>
            <span class="training-monitor__node-name">{{ node.nodeName }}</span>
          </div>
          <div class="training-monitor__node-stats">
            <div class="training-monitor__node-stat">
              <span class="training-monitor__node-stat-value training-monitor__node-stat-value--pending">
                {{ node.pendingCount }}
              </span>
              <span class="training-monitor__node-stat-label">未开始</span>
            </div>
            <div class="training-monitor__node-stat">
              <span class="training-monitor__node-stat-value training-monitor__node-stat-value--progress">
                {{ node.inProgressCount }}
              </span>
              <span class="training-monitor__node-stat-label">进行中</span>
            </div>
            <div class="training-monitor__node-stat">
              <span class="training-monitor__node-stat-value training-monitor__node-stat-value--done">
                {{ node.completedCount }}
              </span>
              <span class="training-monitor__node-stat-label">已完成</span>
            </div>
          </div>
          <div class="training-monitor__node-actions">
            <button class="training-monitor__node-action-btn" @click="viewNodeDetail(node.nodeInstanceId)">
              查看详情
            </button>
            <button class="training-monitor__node-action-btn training-monitor__node-action-btn--nudge" @click="nudgeNode(node.nodeInstanceId)">
              催办
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Quick Actions -->
    <section class="training-monitor__actions">
      <h2 class="training-monitor__section-title">快捷操作</h2>
      <div class="training-monitor__action-grid">
        <button class="training-monitor__action-card" @click="nudgeAll">
          <span class="training-monitor__action-icon">🔔</span>
          <span class="training-monitor__action-label">全员催办</span>
        </button>
        <button class="training-monitor__action-card" @click="forceUnlockPhase">
          <span class="training-monitor__action-icon">🔓</span>
          <span class="training-monitor__action-label">手动解锁阶段</span>
        </button>
        <button class="training-monitor__action-card" @click="exportProgress">
          <span class="training-monitor__action-icon">📊</span>
          <span class="training-monitor__action-label">导出进度</span>
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface PhaseProgress {
  phaseId: string
  phaseName: string
  isActive: boolean
  completedCount: number
  progressPercent: number
}

interface NodeDistribution {
  nodeInstanceId: number
  nodeName: string
  nodeTypeName: string
  pendingCount: number
  inProgressCount: number
  completedCount: number
}

/** Placeholder data */
const taskName = ref<string>('Python 数组实训 - 2025春季班')
const totalStudents = ref<number>(32)
const onlineStudents = ref<number>(28)
const completedStudents = ref<number>(5)

const phases = computed<PhaseProgress[]>(() => [
  { phaseId: 'p1', phaseName: '课前阶段', isActive: false, completedCount: 30, progressPercent: 94 },
  { phaseId: 'p2', phaseName: '课中阶段', isActive: true, completedCount: 12, progressPercent: 38 },
  { phaseId: 'p3', phaseName: '课后阶段', isActive: false, completedCount: 0, progressPercent: 0 }
])

const nodeDistribution = computed<NodeDistribution[]>(() => [
  { nodeInstanceId: 1, nodeName: '开始实训', nodeTypeName: '开始', pendingCount: 0, inProgressCount: 2, completedCount: 30 },
  { nodeInstanceId: 2, nodeName: '课件阅读', nodeTypeName: '资源阅读', pendingCount: 2, inProgressCount: 5, completedCount: 25 },
  { nodeInstanceId: 3, nodeName: '学情调查', nodeTypeName: '学情调查', pendingCount: 4, inProgressCount: 8, completedCount: 20 },
  { nodeInstanceId: 4, nodeName: '任务下发', nodeTypeName: '任务下发', pendingCount: 10, inProgressCount: 10, completedCount: 12 },
  { nodeInstanceId: 5, nodeName: '编码实训', nodeTypeName: '编码实训', pendingCount: 15, inProgressCount: 12, completedCount: 5 },
  { nodeInstanceId: 6, nodeName: '课后作业', nodeTypeName: '作业', pendingCount: 25, inProgressCount: 5, completedCount: 2 }
])

function viewNodeDetail(nodeInstanceId: number) {
  // Navigate to node detail view
  console.log('View detail:', nodeInstanceId)
}

function nudgeNode(nodeInstanceId: number) {
  console.log('Nudge node:', nodeInstanceId)
}

function nudgeAll() {
  console.log('Nudge all students')
}

function forceUnlockPhase() {
  console.log('Force unlock next phase')
}

function exportProgress() {
  console.log('Export progress data')
}
</script>

<style scoped>
.training-monitor {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
  background: var(--color-gray-50, #f8fafc);
}

.training-monitor__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg, 1.5rem);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
}

.training-monitor__header-left {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.training-monitor__title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
}

.training-monitor__task-name {
  font-size: 0.875rem;
  color: var(--color-gray-500, #64748b);
}

.training-monitor__header-right {
  display: flex;
  gap: var(--spacing-md, 1rem);
}

.training-monitor__stat-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.5rem 1rem;
  background: var(--color-gray-50, #f8fafc);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
}

.training-monitor__stat-badge--active {
  background: rgba(34, 197, 94, 0.05);
  border-color: rgba(34, 197, 94, 0.2);
}

.training-monitor__stat-badge--complete {
  background: rgba(99, 102, 241, 0.05);
  border-color: rgba(99, 102, 241, 0.2);
}

.training-monitor__stat-label {
  font-size: 0.6875rem;
  color: var(--color-gray-500, #64748b);
}

.training-monitor__stat-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
}

.training-monitor__stat-badge--active .training-monitor__stat-value {
  color: #16a34a;
}

.training-monitor__stat-badge--complete .training-monitor__stat-value {
  color: var(--color-primary-600, #4f46e5);
}

.training-monitor__section-title {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-md, 1rem);
}

.training-monitor__phases {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.training-monitor__phase-list {
  display: flex;
  gap: var(--spacing-md, 1rem);
}

.training-monitor__phase-card {
  flex: 1;
  padding: var(--spacing-md, 1rem);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  transition: border-color 0.15s ease;
}

.training-monitor__phase-card--active {
  border-color: var(--color-primary-300, #a5b4fc);
  background: var(--color-primary-50, #eef2ff);
}

.training-monitor__phase-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.training-monitor__phase-name {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
}

.training-monitor__phase-progress {
  font-size: 0.75rem;
  color: var(--color-gray-500, #64748b);
}

.training-monitor__phase-bar {
  height: 6px;
  background: var(--color-gray-100, #f1f5f9);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 0.375rem;
}

.training-monitor__phase-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 3px;
  transition: width 0.5s ease;
}

.training-monitor__phase-percent {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-primary-600, #4f46e5);
}

.training-monitor__nodes {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.training-monitor__node-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-md, 1rem);
}

.training-monitor__node-card {
  padding: var(--spacing-md, 1rem);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
}

.training-monitor__node-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.training-monitor__node-type-badge {
  font-size: 0.625rem;
  font-weight: 700;
  padding: 0.125rem 0.375rem;
  border-radius: var(--radius-sm, 0.25rem);
  background: var(--color-gray-100, #f1f5f9);
  color: var(--color-gray-600, #475569);
}

.training-monitor__node-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
}

.training-monitor__node-stats {
  display: flex;
  gap: var(--spacing-sm, 0.75rem);
  margin-bottom: 0.75rem;
}

.training-monitor__node-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  padding: 0.375rem;
  background: var(--color-gray-50, #f8fafc);
  border-radius: var(--radius-sm, 0.25rem);
}

.training-monitor__node-stat-value {
  font-size: 1.125rem;
  font-weight: 700;
}

.training-monitor__node-stat-value--pending {
  color: var(--color-gray-400, #94a3b8);
}

.training-monitor__node-stat-value--progress {
  color: #f59e0b;
}

.training-monitor__node-stat-value--done {
  color: #22c55e;
}

.training-monitor__node-stat-label {
  font-size: 0.625rem;
  color: var(--color-gray-500, #64748b);
}

.training-monitor__node-actions {
  display: flex;
  gap: 0.5rem;
}

.training-monitor__node-action-btn {
  flex: 1;
  padding: 0.375rem 0.5rem;
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--color-gray-600, #475569);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-sm, 0.25rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.training-monitor__node-action-btn:hover {
  background: var(--color-gray-50, #f8fafc);
}

.training-monitor__node-action-btn--nudge {
  color: #d97706;
  border-color: rgba(245, 158, 11, 0.3);
}

.training-monitor__node-action-btn--nudge:hover {
  background: rgba(245, 158, 11, 0.05);
}

.training-monitor__actions {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.training-monitor__action-grid {
  display: flex;
  gap: var(--spacing-md, 1rem);
}

.training-monitor__action-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: var(--spacing-md, 1rem);
  background: var(--color-gray-50, #f8fafc);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.training-monitor__action-card:hover {
  background: var(--color-primary-50, #eef2ff);
  border-color: var(--color-primary-200, #c7d2fe);
}

.training-monitor__action-icon {
  font-size: 1.5rem;
}

.training-monitor__action-label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-gray-700, #334155);
}
</style>
