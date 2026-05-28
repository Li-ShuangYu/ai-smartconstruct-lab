<template>
  <div class="teacher-node-detail">
    <!-- Node Header -->
    <header class="teacher-node-detail__header">
      <div class="teacher-node-detail__header-left">
        <span class="teacher-node-detail__type-badge">{{ nodeTypeName }}</span>
        <h2 class="teacher-node-detail__title">{{ nodeName }}</h2>
      </div>
      <div class="teacher-node-detail__header-right">
        <div class="teacher-node-detail__metric">
          <span class="teacher-node-detail__metric-value">{{ completedCount }}</span>
          <span class="teacher-node-detail__metric-label">已完成</span>
        </div>
        <div class="teacher-node-detail__metric">
          <span class="teacher-node-detail__metric-value">{{ inProgressCount }}</span>
          <span class="teacher-node-detail__metric-label">进行中</span>
        </div>
        <div class="teacher-node-detail__metric">
          <span class="teacher-node-detail__metric-value">{{ avgDuration }}</span>
          <span class="teacher-node-detail__metric-label">平均用时(分)</span>
        </div>
      </div>
    </header>

    <!-- Student Progress List -->
    <section class="teacher-node-detail__progress">
      <h3 class="teacher-node-detail__section-title">学生进度列表</h3>
      <div class="teacher-node-detail__table-wrap">
        <table class="teacher-node-detail__table">
          <thead>
            <tr>
              <th>学生</th>
              <th>状态</th>
              <th>进入时间</th>
              <th>用时</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="student in studentProgress"
              :key="student.studentId"
            >
              <td>
                <div class="teacher-node-detail__student-cell">
                  <div class="teacher-node-detail__student-avatar">{{ student.name.charAt(0) }}</div>
                  <span>{{ student.name }}</span>
                </div>
              </td>
              <td>
                <span class="teacher-node-detail__status-tag" :class="`teacher-node-detail__status-tag--${student.status}`">
                  {{ statusLabel(student.status) }}
                </span>
              </td>
              <td class="teacher-node-detail__time-cell">{{ student.enteredAt ?? '-' }}</td>
              <td class="teacher-node-detail__time-cell">{{ student.duration ?? '-' }}</td>
              <td>
                <div class="teacher-node-detail__actions-cell">
                  <button
                    v-if="student.status !== 'completed'"
                    class="teacher-node-detail__action-btn"
                    @click="forceComplete(student.studentId)"
                  >
                    强制完成
                  </button>
                  <button
                    class="teacher-node-detail__action-btn teacher-node-detail__action-btn--review"
                    @click="manualReview(student.studentId)"
                  >
                    手动评阅
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Key Metrics -->
    <section class="teacher-node-detail__metrics">
      <h3 class="teacher-node-detail__section-title">关键指标</h3>
      <div class="teacher-node-detail__metrics-grid">
        <div class="teacher-node-detail__metric-card">
          <span class="teacher-node-detail__metric-card-value">{{ completionRate }}%</span>
          <span class="teacher-node-detail__metric-card-label">完成率</span>
        </div>
        <div class="teacher-node-detail__metric-card">
          <span class="teacher-node-detail__metric-card-value">{{ avgScore }}</span>
          <span class="teacher-node-detail__metric-card-label">平均分</span>
        </div>
        <div class="teacher-node-detail__metric-card">
          <span class="teacher-node-detail__metric-card-value">{{ avgDuration }}min</span>
          <span class="teacher-node-detail__metric-card-label">平均用时</span>
        </div>
        <div class="teacher-node-detail__metric-card">
          <span class="teacher-node-detail__metric-card-value">{{ submissionCount }}</span>
          <span class="teacher-node-detail__metric-card-label">提交次数</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type StudentStatus = 'pending' | 'in_progress' | 'completed'

interface StudentProgressItem {
  studentId: number
  name: string
  status: StudentStatus
  enteredAt: string | null
  duration: string | null
}

interface TeacherNodeDetailConfig {
  display?: {
    node_name?: string
    node_type_name?: string
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: TeacherNodeDetailConfig
}>()

const emit = defineEmits<{
  complete: []
}>()

const nodeName = computed<string>(() =>
  props.nodeConfig.display?.node_name ?? '编码实训'
)

const nodeTypeName = computed<string>(() =>
  props.nodeConfig.display?.node_type_name ?? '编码实训'
)

/** Placeholder student progress */
const studentProgress = computed<StudentProgressItem[]>(() => [
  { studentId: 1, name: '张伟', status: 'completed', enteredAt: '14:05', duration: '32min' },
  { studentId: 2, name: '李娜', status: 'completed', enteredAt: '14:08', duration: '28min' },
  { studentId: 3, name: '王磊', status: 'in_progress', enteredAt: '14:12', duration: '20min' },
  { studentId: 4, name: '刘洋', status: 'in_progress', enteredAt: '14:15', duration: '17min' },
  { studentId: 5, name: '陈静', status: 'in_progress', enteredAt: '14:20', duration: '12min' },
  { studentId: 6, name: '杨帆', status: 'pending', enteredAt: null, duration: null },
  { studentId: 7, name: '赵云', status: 'pending', enteredAt: null, duration: null }
])

const completedCount = computed<number>(() =>
  studentProgress.value.filter(s => s.status === 'completed').length
)

const inProgressCount = computed<number>(() =>
  studentProgress.value.filter(s => s.status === 'in_progress').length
)

const avgDuration = computed<number>(() => 25)
const completionRate = computed<number>(() =>
  Math.round((completedCount.value / studentProgress.value.length) * 100)
)
const avgScore = computed<number>(() => 78)
const submissionCount = computed<number>(() => completedCount.value + 3)

function statusLabel(status: StudentStatus): string {
  switch (status) {
    case 'completed': return '已完成'
    case 'in_progress': return '进行中'
    case 'pending': return '未开始'
  }
}

function forceComplete(studentId: number) {
  console.log('Force complete for student:', studentId)
}

function manualReview(studentId: number) {
  console.log('Manual review for student:', studentId)
}
</script>

<style scoped>
.teacher-node-detail {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
}

.teacher-node-detail__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg, 1.5rem);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
}

.teacher-node-detail__header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.teacher-node-detail__type-badge {
  font-size: 0.6875rem;
  font-weight: 700;
  padding: 0.25rem 0.625rem;
  border-radius: var(--radius-sm, 0.25rem);
  background: var(--color-primary-50, #eef2ff);
  color: var(--color-primary-600, #4f46e5);
}

.teacher-node-detail__title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
}

.teacher-node-detail__header-right {
  display: flex;
  gap: var(--spacing-md, 1rem);
}

.teacher-node-detail__metric {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.375rem 0.75rem;
}

.teacher-node-detail__metric-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.teacher-node-detail__metric-label {
  font-size: 0.6875rem;
  color: var(--color-gray-500, #64748b);
}

.teacher-node-detail__section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-md, 1rem);
}

.teacher-node-detail__progress {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.teacher-node-detail__table-wrap {
  overflow-x: auto;
}

.teacher-node-detail__table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.8125rem;
}

.teacher-node-detail__table th {
  text-align: left;
  padding: 0.625rem 0.75rem;
  font-weight: 600;
  color: var(--color-gray-500, #64748b);
  border-bottom: 1px solid var(--color-gray-200, #e2e8f0);
  font-size: 0.75rem;
}

.teacher-node-detail__table td {
  padding: 0.625rem 0.75rem;
  border-bottom: 1px solid var(--color-gray-100, #f1f5f9);
  color: var(--color-gray-700, #334155);
}

.teacher-node-detail__student-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.teacher-node-detail__student-avatar {
  width: 1.75rem;
  height: 1.75rem;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.6875rem;
  font-weight: 700;
}

.teacher-node-detail__status-tag {
  font-size: 0.6875rem;
  font-weight: 600;
  padding: 0.125rem 0.5rem;
  border-radius: var(--radius-sm, 0.25rem);
}

.teacher-node-detail__status-tag--completed {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
}

.teacher-node-detail__status-tag--in_progress {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}

.teacher-node-detail__status-tag--pending {
  background: var(--color-gray-100, #f1f5f9);
  color: var(--color-gray-500, #64748b);
}

.teacher-node-detail__time-cell {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
}

.teacher-node-detail__actions-cell {
  display: flex;
  gap: 0.375rem;
}

.teacher-node-detail__action-btn {
  padding: 0.25rem 0.5rem;
  font-size: 0.6875rem;
  font-weight: 500;
  color: var(--color-gray-600, #475569);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-sm, 0.25rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.teacher-node-detail__action-btn:hover {
  background: var(--color-gray-50, #f8fafc);
}

.teacher-node-detail__action-btn--review {
  color: var(--color-primary-600, #4f46e5);
  border-color: var(--color-primary-200, #c7d2fe);
}

.teacher-node-detail__metrics {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.teacher-node-detail__metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md, 1rem);
}

.teacher-node-detail__metric-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-md, 1rem);
  background: var(--color-gray-50, #f8fafc);
  border-radius: var(--radius-md, 0.5rem);
}

.teacher-node-detail__metric-card-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.teacher-node-detail__metric-card-label {
  font-size: 0.75rem;
  color: var(--color-gray-500, #64748b);
  margin-top: 0.25rem;
}
</style>
