<template>
  <div class="grouping-page">
    <!-- Group Info Header -->
    <section class="grouping-page__header">
      <div class="grouping-page__group-badge">
        <span class="grouping-page__group-icon">👥</span>
        <div class="grouping-page__group-info">
          <h2 class="grouping-page__group-name">{{ groupInfo.groupName }}</h2>
          <span class="grouping-page__group-id">小组编号: {{ groupInfo.groupId }}</span>
        </div>
      </div>
      <div class="grouping-page__group-meta">
        <span class="grouping-page__member-count">成员 {{ members.length }} 人</span>
        <span class="grouping-page__method-tag">{{ groupMethodLabel }}</span>
      </div>
    </section>

    <!-- Member List -->
    <section class="grouping-page__members">
      <h3 class="grouping-page__section-title">小组成员</h3>
      <div class="grouping-page__member-grid">
        <div
          v-for="member in members"
          :key="member.studentId"
          class="grouping-page__member-card"
          :class="{ 'grouping-page__member-card--leader': member.isLeader }"
        >
          <div class="grouping-page__avatar">
            {{ member.name.charAt(0) }}
          </div>
          <div class="grouping-page__member-info">
            <span class="grouping-page__member-name">{{ member.name }}</span>
            <span v-if="member.isLeader" class="grouping-page__leader-badge">
              ⭐ 组长
            </span>
            <span v-else class="grouping-page__member-role">组员</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Group Task Summary -->
    <section class="grouping-page__task-summary">
      <h3 class="grouping-page__section-title">小组任务概览</h3>
      <div class="grouping-page__task-stats">
        <div class="grouping-page__stat-item">
          <span class="grouping-page__stat-value">{{ groupInfo.totalTasks }}</span>
          <span class="grouping-page__stat-label">总任务数</span>
        </div>
        <div class="grouping-page__stat-item">
          <span class="grouping-page__stat-value">{{ groupInfo.completedTasks }}</span>
          <span class="grouping-page__stat-label">已完成</span>
        </div>
        <div class="grouping-page__stat-item">
          <span class="grouping-page__stat-value">{{ groupProgressPercent }}%</span>
          <span class="grouping-page__stat-label">完成率</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface GroupMember {
  studentId: number
  name: string
  isLeader: boolean
}

interface GroupNodeConfig {
  display?: {
    group_name?: string
    group_id?: string
  }
  orchestration_settings?: {
    group_method?: 'random' | 'manual' | 'ai_balanced'
    group_size?: number
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: GroupNodeConfig
}>()

/** Placeholder group info */
const groupInfo = computed(() => ({
  groupName: props.nodeConfig.display?.group_name ?? '第一小组',
  groupId: props.nodeConfig.display?.group_id ?? 'G-001',
  totalTasks: 6,
  completedTasks: 2
}))

/** Placeholder members */
const members = computed<GroupMember[]>(() => [
  { studentId: 1, name: '张伟', isLeader: true },
  { studentId: 2, name: '李娜', isLeader: false },
  { studentId: 3, name: '王磊', isLeader: false },
  { studentId: 4, name: '刘洋', isLeader: false },
  { studentId: 5, name: '陈静', isLeader: false }
])

/** Group method label */
const groupMethodLabel = computed<string>(() => {
  const method = props.nodeConfig.orchestration_settings?.group_method
  switch (method) {
    case 'random': return '随机分组'
    case 'manual': return '手动分组'
    case 'ai_balanced': return 'AI均衡分组'
    default: return '随机分组'
  }
})

/** Group progress percentage */
const groupProgressPercent = computed<number>(() => {
  const total = groupInfo.value.totalTasks
  if (total === 0) return 0
  return Math.round((groupInfo.value.completedTasks / total) * 100)
})
</script>

<style scoped>
.grouping-page {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
}

.grouping-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg, 1.5rem);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
}

.grouping-page__group-badge {
  display: flex;
  align-items: center;
  gap: var(--spacing-md, 1rem);
}

.grouping-page__group-icon {
  font-size: 2rem;
}

.grouping-page__group-info {
  display: flex;
  flex-direction: column;
}

.grouping-page__group-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
}

.grouping-page__group-id {
  font-size: 0.75rem;
  color: var(--color-gray-500, #64748b);
}

.grouping-page__group-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm, 0.75rem);
}

.grouping-page__member-count {
  font-size: 0.875rem;
  color: var(--color-gray-600, #475569);
  font-weight: 500;
}

.grouping-page__method-tag {
  font-size: 0.6875rem;
  font-weight: 600;
  padding: 0.25rem 0.625rem;
  border-radius: var(--radius-sm, 0.25rem);
  background: var(--color-primary-50, #eef2ff);
  color: var(--color-primary-600, #4f46e5);
}

.grouping-page__section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-sm, 0.75rem);
}

.grouping-page__members {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.grouping-page__member-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--spacing-sm, 0.75rem);
}

.grouping-page__member-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm, 0.75rem);
  padding: var(--spacing-sm, 0.75rem);
  border: 1px solid var(--color-gray-100, #f1f5f9);
  border-radius: var(--radius-md, 0.5rem);
  transition: border-color 0.15s ease;
}

.grouping-page__member-card--leader {
  border-color: var(--color-primary-200, #c7d2fe);
  background: var(--color-primary-50, #eef2ff);
}

.grouping-page__avatar {
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 700;
  flex-shrink: 0;
}

.grouping-page__member-info {
  display: flex;
  flex-direction: column;
}

.grouping-page__member-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
}

.grouping-page__leader-badge {
  font-size: 0.6875rem;
  color: var(--color-primary-600, #4f46e5);
  font-weight: 600;
}

.grouping-page__member-role {
  font-size: 0.6875rem;
  color: var(--color-gray-400, #94a3b8);
}

.grouping-page__task-summary {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.grouping-page__task-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-md, 1rem);
}

.grouping-page__stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-md, 1rem);
  background: var(--color-gray-50, #f8fafc);
  border-radius: var(--radius-md, 0.5rem);
}

.grouping-page__stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.grouping-page__stat-label {
  font-size: 0.75rem;
  color: var(--color-gray-500, #64748b);
  margin-top: 0.25rem;
}
</style>
