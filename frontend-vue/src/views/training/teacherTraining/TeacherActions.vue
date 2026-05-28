<template>
  <div class="teacher-actions">
    <!-- Nudge Students -->
    <section class="teacher-actions__section">
      <h3 class="teacher-actions__section-title">催办学生</h3>
      <p class="teacher-actions__section-desc">向未完成当前节点的学生发送提醒通知。</p>
      <div class="teacher-actions__nudge-options">
        <label class="teacher-actions__radio-option">
          <input
            type="radio"
            name="nudge_scope"
            value="all"
            class="teacher-actions__radio-input"
            :checked="nudgeScope === 'all'"
            @change="nudgeScope = 'all'"
          />
          <span class="teacher-actions__radio-label">催办所有未完成学生</span>
        </label>
        <label class="teacher-actions__radio-option">
          <input
            type="radio"
            name="nudge_scope"
            value="current_node"
            class="teacher-actions__radio-input"
            :checked="nudgeScope === 'current_node'"
            @change="nudgeScope = 'current_node'"
          />
          <span class="teacher-actions__radio-label">仅催办当前节点未完成学生</span>
        </label>
      </div>
      <button class="teacher-actions__btn teacher-actions__btn--nudge" @click="handleNudge">
        🔔 发送催办通知
      </button>
    </section>

    <!-- Force Complete Node -->
    <section class="teacher-actions__section">
      <h3 class="teacher-actions__section-title">强制完成节点</h3>
      <p class="teacher-actions__section-desc">将指定学生的当前节点标记为已完成（不影响评分）。</p>
      <div class="teacher-actions__select-wrap">
        <select class="teacher-actions__select" v-model="selectedNodeId">
          <option value="">选择节点...</option>
          <option
            v-for="node in availableNodes"
            :key="node.id"
            :value="node.id"
          >
            {{ node.name }}
          </option>
        </select>
      </div>
      <button
        class="teacher-actions__btn teacher-actions__btn--force"
        :disabled="!selectedNodeId"
        @click="handleForceComplete"
      >
        ⚡ 强制完成选中节点
      </button>
      <p class="teacher-actions__warning">
        ⚠️ 此操作不可撤销，学生将跳过该节点的正常完成流程。
      </p>
    </section>

    <!-- Manually Unlock Phase -->
    <section class="teacher-actions__section">
      <h3 class="teacher-actions__section-title">手动解锁阶段</h3>
      <p class="teacher-actions__section-desc">跳过阶段完成条件，手动解锁下一阶段。</p>
      <div class="teacher-actions__phase-list">
        <div
          v-for="phase in phaseList"
          :key="phase.id"
          class="teacher-actions__phase-item"
          :class="{
            'teacher-actions__phase-item--locked': !phase.unlocked,
            'teacher-actions__phase-item--active': phase.active
          }"
        >
          <span class="teacher-actions__phase-name">{{ phase.name }}</span>
          <span class="teacher-actions__phase-status">
            {{ phase.unlocked ? '已解锁' : '锁定中' }}
          </span>
        </div>
      </div>
      <button
        class="teacher-actions__btn teacher-actions__btn--unlock"
        :disabled="!hasLockedPhase"
        @click="handleUnlockPhase"
      >
        🔓 解锁下一阶段
      </button>
    </section>

    <!-- Operation Log -->
    <section class="teacher-actions__section">
      <h3 class="teacher-actions__section-title">操作记录</h3>
      <div class="teacher-actions__log-list">
        <div
          v-for="log in operationLogs"
          :key="log.id"
          class="teacher-actions__log-item"
        >
          <span class="teacher-actions__log-time">{{ log.time }}</span>
          <span class="teacher-actions__log-action">{{ log.action }}</span>
          <span class="teacher-actions__log-detail">{{ log.detail }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface AvailableNode {
  id: string
  name: string
}

interface PhaseItem {
  id: string
  name: string
  unlocked: boolean
  active: boolean
}

interface OperationLog {
  id: string
  time: string
  action: string
  detail: string
}

const nudgeScope = ref<'all' | 'current_node'>('all')
const selectedNodeId = ref<string>('')

/** Placeholder available nodes */
const availableNodes = computed<AvailableNode[]>(() => [
  { id: 'n1', name: '课件阅读' },
  { id: 'n2', name: '学情调查' },
  { id: 'n3', name: '编码实训' },
  { id: 'n4', name: '课后作业' }
])

/** Placeholder phase list */
const phaseList = computed<PhaseItem[]>(() => [
  { id: 'p1', name: '课前阶段', unlocked: true, active: false },
  { id: 'p2', name: '课中阶段', unlocked: true, active: true },
  { id: 'p3', name: '课后阶段', unlocked: false, active: false }
])

const hasLockedPhase = computed<boolean>(() =>
  phaseList.value.some(p => !p.unlocked)
)

/** Placeholder operation logs */
const operationLogs = computed<OperationLog[]>(() => [
  { id: 'l1', time: '15:30', action: '催办通知', detail: '向12名未完成学生发送催办' },
  { id: 'l2', time: '15:15', action: '强制完成', detail: '张伟 - 课件阅读节点' },
  { id: 'l3', time: '14:50', action: '解锁阶段', detail: '手动解锁课中阶段' }
])

function handleNudge() {
  // POST /api/teacher/tasks/{taskId}/nudge
  console.log('Nudge:', nudgeScope.value)
}

function handleForceComplete() {
  if (!selectedNodeId.value) return
  // POST /api/teacher/nodes/{nodeInstanceId}/force-complete
  console.log('Force complete:', selectedNodeId.value)
}

function handleUnlockPhase() {
  // POST /api/teacher/tasks/{taskId}/unlock-phase
  console.log('Unlock next phase')
}
</script>

<style scoped>
.teacher-actions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
}

.teacher-actions__section {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.teacher-actions__section-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: 0.375rem;
}

.teacher-actions__section-desc {
  font-size: 0.8125rem;
  color: var(--color-gray-500, #64748b);
  margin-bottom: var(--spacing-md, 1rem);
}

.teacher-actions__nudge-options {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: var(--spacing-md, 1rem);
}

.teacher-actions__radio-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.teacher-actions__radio-input {
  accent-color: var(--color-primary-500, #6366f1);
}

.teacher-actions__radio-label {
  font-size: 0.875rem;
  color: var(--color-gray-700, #334155);
}

.teacher-actions__btn {
  width: 100%;
  padding: 0.75rem;
  font-size: 0.9375rem;
  font-weight: 600;
  border: none;
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.teacher-actions__btn--nudge {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.teacher-actions__btn--nudge:hover {
  background: rgba(245, 158, 11, 0.15);
}

.teacher-actions__btn--force {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.teacher-actions__btn--force:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.15);
}

.teacher-actions__btn--force:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.teacher-actions__btn--unlock {
  background: var(--color-primary-50, #eef2ff);
  color: var(--color-primary-600, #4f46e5);
  border: 1px solid var(--color-primary-200, #c7d2fe);
}

.teacher-actions__btn--unlock:hover:not(:disabled) {
  background: var(--color-primary-100, #e0e7ff);
}

.teacher-actions__btn--unlock:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.teacher-actions__warning {
  font-size: 0.75rem;
  color: #dc2626;
  margin-top: 0.5rem;
}

.teacher-actions__select-wrap {
  margin-bottom: var(--spacing-md, 1rem);
}

.teacher-actions__select {
  width: 100%;
  padding: 0.625rem 0.75rem;
  font-size: 0.875rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  color: var(--color-gray-700, #334155);
  background: var(--color-white, #ffffff);
}

.teacher-actions__phase-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: var(--spacing-md, 1rem);
}

.teacher-actions__phase-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-sm, 0.25rem);
}

.teacher-actions__phase-item--active {
  border-color: var(--color-primary-300, #a5b4fc);
  background: var(--color-primary-50, #eef2ff);
}

.teacher-actions__phase-item--locked {
  opacity: 0.6;
}

.teacher-actions__phase-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-gray-700, #334155);
}

.teacher-actions__phase-status {
  font-size: 0.6875rem;
  font-weight: 600;
  color: var(--color-gray-500, #64748b);
}

.teacher-actions__log-list {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.teacher-actions__log-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.375rem 0;
  font-size: 0.8125rem;
  border-bottom: 1px solid var(--color-gray-100, #f1f5f9);
}

.teacher-actions__log-time {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
  min-width: 3rem;
}

.teacher-actions__log-action {
  font-weight: 600;
  color: var(--color-gray-700, #334155);
}

.teacher-actions__log-detail {
  color: var(--color-gray-500, #64748b);
}
</style>
