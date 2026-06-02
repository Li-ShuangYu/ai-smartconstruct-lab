<template>
  <div class="summary-wrapper">
    <n-spin :show="loading">
      <!-- Error State -->
      <div v-if="error" class="summary-error">
        <div class="summary-error__icon">⚠️</div>
        <p class="summary-error__text">{{ error }}</p>
        <button class="summary-error__retry" @click="loadSummary">重试</button>
      </div>

      <!-- Summary Content -->
      <template v-else-if="overview">
        <header class="summary-header">
          <div class="summary-header__badge">✅</div>
          <h1 class="summary-header__title">{{ overview.task_name }}</h1>
          <p class="summary-header__subtitle">实训已完成</p>
        </header>

        <!-- Score Section -->
        <section class="summary-section">
          <h2 class="summary-section__title">总分</h2>
          <div class="score-display">
            <span class="score-display__value">
              {{ '—' }}
            </span>
            <span class="score-display__label">分</span>
          </div>
        </section>

        <!-- Phase Completion Status -->
        <section class="summary-section">
          <h2 class="summary-section__title">各阶段完成状态</h2>
          <div class="phase-list">
            <div
              v-for="(phase, index) in overview.phases"
              :key="phase.phase_id"
              class="phase-item"
            >
              <div class="phase-item__index">{{ index + 1 }}</div>
              <div class="phase-item__info">
                <span class="phase-item__name">{{ phase.phase_name }}</span>
                <span class="phase-item__stats">
                  {{ phase.completed_nodes }}/{{ phase.total_nodes }} 节点完成
                </span>
              </div>
              <div class="phase-item__status" :class="phaseStatusClass(phase)">
                {{ phaseStatusLabel(phase) }}
              </div>
            </div>
          </div>
        </section>

        <!-- Overall Progress -->
        <section class="summary-section">
          <h2 class="summary-section__title">整体进度</h2>
          <div class="overall-progress">
            <div class="overall-progress__bar">
              <div
                class="overall-progress__fill"
                :style="{ width: `${progressPercent}%` }"
              ></div>
            </div>
            <span class="overall-progress__text">{{ progressPercent }}%</span>
          </div>
        </section>

        <!-- Actions -->
        <footer class="summary-footer">
          <button class="summary-footer__btn" @click="goBack">返回工作台</button>
          <button class="summary-footer__btn summary-footer__btn--secondary" @click="handleRestart" :disabled="resetting">
            {{ resetting ? '重置中...' : '重新学习' }}
          </button>
        </footer>
      </template>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NSpin, useMessage } from 'naive-ui'
import { getTaskOverview, resetTraining, startTraining } from '@/services/modules/studentTraining.service'
import { useStudentFlowStore } from '@/stores/modules/studentFlow.store'
import type { StudentTaskOverview, PhaseProgress } from '@/services/types/studentTraining'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref<string | null>(null)
const overview = ref<StudentTaskOverview | null>(null)
const resetting = ref(false)

const message = useMessage()
const flowStore = useStudentFlowStore()

const taskId = computed(() => {
  const raw = route.params.taskId
  return (typeof raw === 'string' ? raw : Array.isArray(raw) ? raw[0] : '') || ''
})

/** 整体进度百分比 */
const progressPercent = computed<number>(() => {
  if (!overview.value) return 0
  const allNodes = overview.value.phases.flatMap(p => p.nodes)
  if (allNodes.length === 0) return 0
  const completed = allNodes.filter(n => n.status === 2).length
  return Math.round((completed / allNodes.length) * 100)
})

/** 阶段状态样式类 */
function phaseStatusClass(phase: PhaseProgress): string {
  if (phase.is_complete) return 'phase-item__status--complete'
  if (phase.completed_nodes > 0) return 'phase-item__status--in-progress'
  return 'phase-item__status--not-started'
}

/** 阶段状态标签 */
function phaseStatusLabel(phase: PhaseProgress): string {
  if (phase.is_complete) return '已完成'
  if (phase.completed_nodes > 0) return '进行中'
  return '未开始'
}

/** 加载总结数据 */
async function loadSummary(): Promise<void> {
  if (!taskId.value) {
    error.value = '无效的实训任务ID'
    return
  }
  loading.value = true
  error.value = null
  try {
    const res = await getTaskOverview(taskId.value)
    if (res.code === 200 && res.data) {
      overview.value = res.data
    } else {
      error.value = '加载实训总结失败'
    }
  } catch {
    error.value = '网络请求失败，请重试'
  } finally {
    loading.value = false
  }
}

/** 重新学习：重置实训进度后重新开始 */
async function handleRestart(): Promise<void> {
  resetting.value = true
  try {
    const resetRes = await resetTraining(taskId.value)
    if (resetRes.code !== 200) {
      message.error(resetRes.message || '重置失败')
      return
    }

    flowStore.reset()
    const startRes = await startTraining(taskId.value)
    if (startRes.code === 200) {
      message.success('实训已重新开始，正在进入...')
      router.push(`/student/training/${taskId.value}/execute`)
    } else {
      message.error(startRes.message || '开始实训失败')
    }
  } catch {
    message.error('重新开始请求失败，请重试')
  } finally {
    resetting.value = false
  }
}

/** 返回工作台 */
function goBack(): void {
  router.push('/student/workbench')
}

onMounted(() => {
  loadSummary()
})
</script>

<style scoped>
.summary-wrapper {
  max-width: 700px;
  margin: 0 auto;
  padding: 40px 24px;
  min-height: calc(100vh - 64px);
}

/* Error State */
.summary-error {
  text-align: center;
  padding: 60px 0;
}

.summary-error__icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.summary-error__text {
  font-size: 14px;
  color: var(--color-gray-500, #64748b);
  margin-bottom: 16px;
}

.summary-error__retry {
  padding: 8px 24px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary-600, #4f46e5);
  background: var(--color-primary-50, #eef2ff);
  border: 1px solid var(--color-primary-200, #c7d2fe);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s ease;
}

.summary-error__retry:hover {
  background: var(--color-primary-100, #e0e7ff);
}

/* Header */
.summary-header {
  text-align: center;
  margin-bottom: 32px;
}

.summary-header__badge {
  font-size: 56px;
  margin-bottom: 12px;
}

.summary-header__title {
  font-size: 24px;
  font-weight: 800;
  color: var(--color-heading, #1e293b);
  margin: 0 0 8px;
}

.summary-header__subtitle {
  font-size: 14px;
  color: var(--color-emerald-600, #059669);
  font-weight: 600;
  margin: 0;
}

/* Sections */
.summary-section {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--color-gray-100, #f1f5f9);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
}

.summary-section__title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-heading, #1e293b);
  margin: 0 0 16px;
}

/* Score */
.score-display {
  text-align: center;
  padding: 16px 0;
}

.score-display__value {
  font-size: 48px;
  font-weight: 800;
  color: var(--color-primary-600, #4f46e5);
}

.score-display__label {
  font-size: 16px;
  color: var(--color-gray-500, #64748b);
  margin-left: 4px;
}

/* Phase List */
.phase-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.phase-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--color-gray-50, #f8fafc);
  border-radius: 10px;
}

.phase-item__index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-primary-100, #e0e7ff);
  color: var(--color-primary-700, #4338ca);
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.phase-item__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.phase-item__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-heading, #1e293b);
}

.phase-item__stats {
  font-size: 12px;
  color: var(--color-gray-400, #94a3b8);
}

.phase-item__status {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 10px;
  flex-shrink: 0;
}

.phase-item__status--complete {
  color: var(--color-emerald-700, #047857);
  background: var(--color-emerald-50, #ecfdf5);
}

.phase-item__status--in-progress {
  color: var(--color-amber-700, #b45309);
  background: var(--color-amber-50, #fffbeb);
}

.phase-item__status--not-started {
  color: var(--color-gray-500, #64748b);
  background: var(--color-gray-100, #f1f5f9);
}

/* Overall Progress */
.overall-progress {
  display: flex;
  align-items: center;
  gap: 12px;
}

.overall-progress__bar {
  flex: 1;
  height: 10px;
  background: var(--color-gray-100, #f1f5f9);
  border-radius: 5px;
  overflow: hidden;
}

.overall-progress__fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary-400, #818cf8), var(--color-primary-600, #4f46e5));
  border-radius: 5px;
  transition: width 0.5s ease;
}

.overall-progress__text {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
  min-width: 48px;
  text-align: right;
}

/* Footer */
.summary-footer {
  text-align: center;
  padding-top: 24px;
}

.summary-footer__btn {
  padding: 12px 32px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, var(--color-primary-500, #6366f1), var(--color-primary-600, #4f46e5));
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.summary-footer__btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(99, 102, 241, 0.4);
}

.summary-footer__btn--secondary {
  background: white;
  color: var(--color-primary-600, #4f46e5);
  border: 2px solid var(--color-primary-300, #a5b4fc);
  margin-left: 12px;
}

.summary-footer__btn--secondary:hover {
  background: var(--color-primary-50, #eef2ff);
  box-shadow: none;
}

.summary-footer__btn--secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
