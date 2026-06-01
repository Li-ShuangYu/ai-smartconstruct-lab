<template>
  <div class="task-board">
    <!-- Task Overview -->
    <section class="task-board__overview">
      <div class="task-board__overview-header">
        <h2 class="task-board__title">{{ taskTitle }}</h2>
        <div class="task-board__progress-badge">
          <span class="task-board__progress-value">{{ completedSubTasks }}/{{ subTasks.length }}</span>
          <span class="task-board__progress-label">已完成</span>
        </div>
      </div>
      <p class="task-board__description">{{ taskDescription }}</p>
      <div class="task-board__progress-bar">
        <div
          class="task-board__progress-fill"
          :style="{ width: `${taskProgressPercent}%` }"
        ></div>
      </div>
    </section>

    <!-- Sub-task List -->
    <section class="task-board__list">
      <h3 class="task-board__section-title">子任务列表</h3>
      <div class="task-board__items">
        <div
          v-for="(task, index) in subTasks"
          :key="task.id"
          class="task-board__item"
          :class="`task-board__item--${task.status}`"
        >
          <div class="task-board__item-left">
            <div class="task-board__item-check">
              <span v-if="task.status === 'completed'" class="task-board__check-icon">✓</span>
              <span v-else-if="task.status === 'in_progress'" class="task-board__progress-icon">◐</span>
              <span v-else class="task-board__pending-icon">{{ index + 1 }}</span>
            </div>
            <div class="task-board__item-content">
              <span class="task-board__item-name">{{ task.name }}</span>
              <span class="task-board__item-desc">{{ task.description }}</span>
            </div>
          </div>
          <div class="task-board__item-right">
            <span class="task-board__item-assignee">{{ task.assignee }}</span>
            <span class="task-board__item-status-tag" :class="`task-board__item-status-tag--${task.status}`">
              {{ statusLabel(task.status) }}
            </span>
          </div>
        </div>
      </div>
    </section>

    <!-- Deadline Info -->
    <section class="task-board__deadline">
      <div class="task-board__deadline-icon">⏰</div>
      <div class="task-board__deadline-info">
        <span class="task-board__deadline-label">截止时间</span>
        <span class="task-board__deadline-value">{{ deadline }}</span>
      </div>
    </section>

    <!-- Complete Button -->
    <div class="task-board__actions">
      <button class="task-board__complete-btn" @click="handleComplete">查看下一阶段</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const emit = defineEmits<{ complete: [] }>()

type SubTaskStatus = 'pending' | 'in_progress' | 'completed'

interface SubTask {
  id: string
  name: string
  description: string
  assignee: string
  status: SubTaskStatus
}

interface TaskBoardConfig {
  display?: {
    task_title?: string
    task_description?: string
    deadline?: string
  }
  data_collection?: {
    sub_tasks?: SubTask[]
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: TaskBoardConfig
}>()

const taskTitle = computed<string>(() =>
  props.nodeConfig.display?.task_title ?? '项目任务分配'
)

const taskDescription = computed<string>(() =>
  props.nodeConfig.display?.task_description ?? '请按照分配的子任务完成各自的工作，注意截止时间。'
)

const deadline = computed<string>(() =>
  props.nodeConfig.display?.deadline ?? '2025-03-15 23:59'
)

/** Sub-tasks from config or placeholder */
const subTasks = computed<SubTask[]>(() =>
  props.nodeConfig.data_collection?.sub_tasks ?? placeholderSubTasks
)

const placeholderSubTasks: SubTask[] = [
  { id: 't1', name: '需求分析文档', description: '编写项目需求分析报告', assignee: '张伟', status: 'completed' },
  { id: 't2', name: '系统设计', description: '完成系统架构设计图', assignee: '李娜', status: 'in_progress' },
  { id: 't3', name: '数据库设计', description: '设计数据库ER图和表结构', assignee: '王磊', status: 'in_progress' },
  { id: 't4', name: '前端开发', description: '实现用户界面原型', assignee: '刘洋', status: 'pending' },
  { id: 't5', name: '后端开发', description: '实现核心业务逻辑', assignee: '陈静', status: 'pending' },
  { id: 't6', name: '测试报告', description: '编写测试用例并执行', assignee: '张伟', status: 'pending' }
]

const completedSubTasks = computed<number>(() =>
  subTasks.value.filter(t => t.status === 'completed').length
)

const taskProgressPercent = computed<number>(() => {
  if (subTasks.value.length === 0) return 0
  return Math.round((completedSubTasks.value / subTasks.value.length) * 100)
})

function statusLabel(status: SubTaskStatus): string {
  switch (status) {
    case 'completed': return '已完成'
    case 'in_progress': return '进行中'
    case 'pending': return '待开始'
  }
}

function handleComplete() {
  emit('complete')
}
</script>

<style scoped>
.task-board {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
}

.task-board__overview {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.task-board__overview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.task-board__title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
}

.task-board__progress-badge {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 0.75rem;
  background: var(--color-primary-50, #eef2ff);
  border-radius: var(--radius-md, 0.5rem);
}

.task-board__progress-value {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.task-board__progress-label {
  font-size: 0.75rem;
  color: var(--color-primary-500, #6366f1);
}

.task-board__description {
  font-size: 0.875rem;
  color: var(--color-gray-500, #64748b);
  margin-bottom: var(--spacing-md, 1rem);
}

.task-board__progress-bar {
  height: 6px;
  background: var(--color-gray-100, #f1f5f9);
  border-radius: 3px;
  overflow: hidden;
}

.task-board__progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.task-board__list {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.task-board__section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-md, 1rem);
}

.task-board__items {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.task-board__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-gray-100, #f1f5f9);
  border-radius: var(--radius-md, 0.5rem);
  transition: border-color 0.15s ease;
}

.task-board__item--completed {
  border-left: 3px solid #22c55e;
  background: rgba(34, 197, 94, 0.03);
}

.task-board__item--in_progress {
  border-left: 3px solid #f59e0b;
  background: rgba(245, 158, 11, 0.03);
}

.task-board__item--pending {
  border-left: 3px solid var(--color-gray-300, #cbd5e1);
}

.task-board__item-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.task-board__item-check {
  width: 1.75rem;
  height: 1.75rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  flex-shrink: 0;
}

.task-board__check-icon {
  color: #22c55e;
  font-size: 1rem;
}

.task-board__progress-icon {
  color: #f59e0b;
  font-size: 1rem;
}

.task-board__pending-icon {
  color: var(--color-gray-400, #94a3b8);
  background: var(--color-gray-100, #f1f5f9);
  width: 1.75rem;
  height: 1.75rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.task-board__item-content {
  display: flex;
  flex-direction: column;
}

.task-board__item-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
}

.task-board__item-desc {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
}

.task-board__item-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.task-board__item-assignee {
  font-size: 0.75rem;
  color: var(--color-gray-500, #64748b);
}

.task-board__item-status-tag {
  font-size: 0.6875rem;
  font-weight: 600;
  padding: 0.125rem 0.5rem;
  border-radius: var(--radius-sm, 0.25rem);
}

.task-board__item-status-tag--completed {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
}

.task-board__item-status-tag--in_progress {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}

.task-board__item-status-tag--pending {
  background: var(--color-gray-100, #f1f5f9);
  color: var(--color-gray-500, #64748b);
}

.task-board__deadline {
  display: flex;
  align-items: center;
  gap: var(--spacing-md, 1rem);
  padding: var(--spacing-md, 1rem) var(--spacing-lg, 1.5rem);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
}

.task-board__deadline-icon {
  font-size: 1.5rem;
}

.task-board__deadline-info {
  display: flex;
  flex-direction: column;
}

.task-board__deadline-label {
  font-size: 0.75rem;
  color: var(--color-gray-500, #64748b);
}

.task-board__deadline-value {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
}

.task-board__actions {
  text-align: center;
  padding-top: 8px;
}

.task-board__complete-btn {
  padding: 10px 40px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.task-board__complete-btn:hover {
  box-shadow: 0 4px 12px -2px rgba(99, 102, 241, 0.4);
}
</style>
