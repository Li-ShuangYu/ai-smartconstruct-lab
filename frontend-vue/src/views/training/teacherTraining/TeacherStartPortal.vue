<template>
  <div class="teacher-start-portal">
    <!-- Header -->
    <header class="teacher-start-portal__header">
      <div class="teacher-start-portal__header-left">
        <span class="teacher-start-portal__node-badge">START · 教师中控</span>
        <h1 class="teacher-start-portal__title">{{ taskTitle }}</h1>
      </div>
      <div class="teacher-start-portal__header-stats">
        <div class="teacher-start-portal__stat-card">
          <span class="teacher-start-portal__stat-label">班级就位</span>
          <span class="teacher-start-portal__stat-value">
            <span class="teacher-start-portal__stat-ready">{{ readyCount }}</span>
            <span class="teacher-start-portal__stat-sep">/</span>
            <span class="teacher-start-portal__stat-total">{{ totalCount }}</span>
          </span>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <div class="teacher-start-portal__body">
      <!-- Left: AI Welcome + Student Grid -->
      <div class="teacher-start-portal__left">
        <!-- AI Welcome -->
        <section class="teacher-start-portal__ai-welcome">
          <div class="teacher-start-portal__ai-icon">⚡</div>
          <div class="teacher-start-portal__ai-content">
            <h3 class="teacher-start-portal__ai-title">AI 助教播报</h3>
            <p class="teacher-start-portal__ai-text">{{ aiWelcomeMessage }}</p>
          </div>
        </section>

        <!-- Student Entry Status Grid -->
        <section class="teacher-start-portal__students">
          <div class="teacher-start-portal__students-header">
            <h3 class="teacher-start-portal__section-title">班级就位情况</h3>
            <div class="teacher-start-portal__legend">
              <span class="teacher-start-portal__legend-item teacher-start-portal__legend-item--ready">
                已就绪 ({{ readyCount }})
              </span>
              <span class="teacher-start-portal__legend-item teacher-start-portal__legend-item--pending">
                未就绪 ({{ totalCount - readyCount }})
              </span>
            </div>
          </div>
          <div class="teacher-start-portal__student-grid">
            <div
              v-for="student in students"
              :key="student.id"
              class="teacher-start-portal__student-card"
              :class="{ 'teacher-start-portal__student-card--ready': student.ready }"
            >
              <div class="teacher-start-portal__student-avatar" :class="{ 'teacher-start-portal__student-avatar--ready': student.ready }">
                {{ student.name.charAt(0) }}
              </div>
              <span class="teacher-start-portal__student-name">{{ student.name }}</span>
              <span class="teacher-start-portal__student-status" :class="student.ready ? 'teacher-start-portal__student-status--ready' : ''">
                {{ student.ready ? '✓' : '…' }}
              </span>
            </div>
          </div>
        </section>
      </div>

      <!-- Right: Actions -->
      <div class="teacher-start-portal__right">
        <section class="teacher-start-portal__control-panel">
          <div class="teacher-start-portal__control-icon">🎛️</div>
          <h2 class="teacher-start-portal__control-title">实训主控中心</h2>
          <p class="teacher-start-portal__control-desc">
            当前有 <strong>{{ totalCount - readyCount }}</strong> 名学生未就位
          </p>

          <!-- Nudge Button -->
          <button
            class="teacher-start-portal__nudge-btn"
            :disabled="readyCount === totalCount"
            @click="handleNudge"
          >
            🔔 {{ readyCount === totalCount ? '全员已就绪' : '一键催办未就位学生' }}
          </button>

          <!-- Start Button -->
          <button
            class="teacher-start-portal__start-btn"
            @click="handleStart"
          >
            ⚡ 正式开启实训
          </button>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface StudentEntry {
  id: number
  name: string
  ready: boolean
}

interface TeacherStartPortalConfig {
  display?: {
    task_title?: string
    ai_welcome_message?: string
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: TeacherStartPortalConfig
}>()

const emit = defineEmits<{
  complete: []
}>()

const taskTitle = computed<string>(() =>
  props.nodeConfig.display?.task_title ?? 'Python 数组实训 - 教师中控台'
)

const aiWelcomeMessage = ref<string>(
  props.nodeConfig.display?.ai_welcome_message ??
  '尊敬的老师，您好。欢迎进入教师中控台。当前系统已连接，您可以实时监控全班同学的就位状态。全员准备完毕后，点击右侧按钮即可一键拉起实训环境。'
)

/** Placeholder students */
const students = ref<StudentEntry[]>([
  { id: 1, name: '张伟', ready: true },
  { id: 2, name: '李娜', ready: true },
  { id: 3, name: '王磊', ready: true },
  { id: 4, name: '刘洋', ready: false },
  { id: 5, name: '陈静', ready: true },
  { id: 6, name: '杨帆', ready: false },
  { id: 7, name: '赵云', ready: true },
  { id: 8, name: '孙颖', ready: true },
  { id: 9, name: '周杰', ready: false },
  { id: 10, name: '吴敏', ready: true },
  { id: 11, name: '郑强', ready: true },
  { id: 12, name: '王芳', ready: false },
  { id: 13, name: '冯涛', ready: true },
  { id: 14, name: '陈思', ready: true },
  { id: 15, name: '李明', ready: false },
  { id: 16, name: '林峰', ready: true },
  { id: 17, name: '黄丽', ready: false },
  { id: 18, name: '许杰', ready: true },
  { id: 19, name: '何琳', ready: true },
  { id: 20, name: '吕晨', ready: false },
  { id: 21, name: '施雨', ready: true },
  { id: 22, name: '张辉', ready: true },
  { id: 23, name: '朱婷', ready: false },
  { id: 24, name: '秦浩', ready: true },
  { id: 25, name: '许晴', ready: false },
  { id: 26, name: '戚帅', ready: true },
  { id: 27, name: '谢晨', ready: true },
  { id: 28, name: '邹雨', ready: false },
  { id: 29, name: '柏晨', ready: true },
  { id: 30, name: '韦昌', ready: false },
  { id: 31, name: '汪磊', ready: true },
  { id: 32, name: '沈颖', ready: false }
])

const totalCount = computed<number>(() => students.value.length)
const readyCount = computed<number>(() => students.value.filter(s => s.ready).length)

function handleNudge() {
  const unready = totalCount.value - readyCount.value
  if (unready > 0) {
    // In production: call POST /api/teacher/tasks/{taskId}/nudge
    console.log(`Nudging ${unready} students`)
  }
}

function handleStart() {
  // In production: call POST /api/teacher/training-tasks/{id}/start
  emit('complete')
}
</script>

<style scoped>
.teacher-start-portal {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  padding: var(--spacing-lg, 1.5rem);
  gap: var(--spacing-lg, 1.5rem);
}

.teacher-start-portal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.teacher-start-portal__header-left {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.teacher-start-portal__node-badge {
  font-size: 0.6875rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--color-primary-500, #6366f1);
}

.teacher-start-portal__title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
}

.teacher-start-portal__stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.5rem 1.25rem;
  background: var(--color-primary-50, #eef2ff);
  border: 1px solid var(--color-primary-100, #e0e7ff);
  border-radius: var(--radius-md, 0.5rem);
}

.teacher-start-portal__stat-label {
  font-size: 0.6875rem;
  color: var(--color-primary-600, #4f46e5);
  font-weight: 500;
}

.teacher-start-portal__stat-value {
  display: flex;
  align-items: baseline;
}

.teacher-start-portal__stat-ready {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.teacher-start-portal__stat-sep {
  font-size: 1rem;
  color: var(--color-gray-400, #94a3b8);
  margin: 0 0.125rem;
}

.teacher-start-portal__stat-total {
  font-size: 1rem;
  color: var(--color-gray-500, #64748b);
}

.teacher-start-portal__body {
  display: flex;
  flex: 1;
  gap: var(--spacing-lg, 1.5rem);
  min-height: 0;
  overflow: hidden;
}

.teacher-start-portal__left {
  flex: 1.5;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md, 1rem);
  overflow-y: auto;
}

.teacher-start-portal__right {
  flex: 0.8;
  display: flex;
  flex-direction: column;
}

.teacher-start-portal__ai-welcome {
  display: flex;
  gap: var(--spacing-md, 1rem);
  padding: var(--spacing-lg, 1.5rem);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-left: 4px solid var(--color-primary-500, #6366f1);
  border-radius: var(--radius-lg, 0.75rem);
}

.teacher-start-portal__ai-icon {
  font-size: 1.5rem;
  width: 2.5rem;
  height: 2.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary-500, #6366f1);
  border-radius: var(--radius-md, 0.5rem);
  flex-shrink: 0;
}

.teacher-start-portal__ai-content {
  flex: 1;
}

.teacher-start-portal__ai-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: 0.375rem;
}

.teacher-start-portal__ai-text {
  font-size: 0.875rem;
  color: var(--color-gray-600, #475569);
  line-height: 1.6;
  font-style: italic;
}

.teacher-start-portal__students {
  flex: 1;
  min-height: 0;
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
  display: flex;
  flex-direction: column;
}

.teacher-start-portal__students-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md, 1rem);
}

.teacher-start-portal__section-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
}

.teacher-start-portal__legend {
  display: flex;
  gap: var(--spacing-md, 1rem);
}

.teacher-start-portal__legend-item {
  font-size: 0.75rem;
  display: flex;
  align-items: center;
  gap: 0.375rem;
}

.teacher-start-portal__legend-item--ready::before {
  content: '';
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background: #22c55e;
}

.teacher-start-portal__legend-item--pending::before {
  content: '';
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background: var(--color-gray-300, #cbd5e1);
}

.teacher-start-portal__student-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 0.75rem;
  flex: 1;
  overflow-y: auto;
}

.teacher-start-portal__student-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.375rem;
  padding: 0.625rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  transition: all 0.2s ease;
}

.teacher-start-portal__student-card--ready {
  border-color: rgba(34, 197, 94, 0.3);
  background: rgba(34, 197, 94, 0.03);
}

.teacher-start-portal__student-avatar {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 700;
  opacity: 0.5;
}

.teacher-start-portal__student-avatar--ready {
  opacity: 1;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.2);
}

.teacher-start-portal__student-name {
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--color-gray-700, #334155);
  text-align: center;
}

.teacher-start-portal__student-status {
  font-size: 0.625rem;
  color: var(--color-gray-400, #94a3b8);
}

.teacher-start-portal__student-status--ready {
  color: #22c55e;
  font-weight: 700;
}

.teacher-start-portal__control-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-xl, 2rem);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
}

.teacher-start-portal__control-icon {
  font-size: 2.5rem;
}

.teacher-start-portal__control-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
}

.teacher-start-portal__control-desc {
  font-size: 0.875rem;
  color: var(--color-gray-500, #64748b);
  text-align: center;
}

.teacher-start-portal__nudge-btn {
  width: 100%;
  padding: 0.875rem;
  font-size: 0.9375rem;
  font-weight: 600;
  color: #d97706;
  background: var(--color-white, #ffffff);
  border: 2px dashed rgba(245, 158, 11, 0.4);
  border-radius: var(--radius-lg, 0.75rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.teacher-start-portal__nudge-btn:hover:not(:disabled) {
  background: rgba(245, 158, 11, 0.05);
}

.teacher-start-portal__nudge-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  color: var(--color-gray-400, #94a3b8);
  border-color: var(--color-gray-200, #e2e8f0);
}

.teacher-start-portal__start-btn {
  width: 100%;
  padding: 1.125rem;
  font-size: 1.125rem;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  border: none;
  border-radius: var(--radius-lg, 0.75rem);
  cursor: pointer;
  transition: all 0.2s ease;
}

.teacher-start-portal__start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px -5px rgba(99, 102, 241, 0.4);
}
</style>
