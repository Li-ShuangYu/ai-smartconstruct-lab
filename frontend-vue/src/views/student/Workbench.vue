<template>
  <div class="workbench-wrapper">
    <header class="hero-section">
      <div class="brand">
        <h1 class="logo-text">{{ profile.realName || profile.username }} 的学习空间</h1>
      </div>
      <p class="slogan">班级: {{ profile.className || '-' }} | 学号: {{ profile.studentNo || '-' }}</p>
    </header>

    <div class="content-container">
      <nav class="filter-nav">
        <div class="nav-left">
          <div v-for="tab in tabs" :key="tab.key" class="nav-item" :class="{ active: activeTab === tab.key }" @click="switchTab(tab.key)">{{ tab.label }}</div>
        </div>
        <div class="nav-right-stats">
          <span>进行中 <b>{{ stats.ongoing }}</b></span>
          <span class="divider">|</span>
          <span>已完成 <b>{{ stats.ended }}</b></span>
        </div>
      </nav>

      <n-spin :show="loading">
        <main class="glass-grid">
          <div v-for="item in tasks" :key="item.id" class="glass-card">
            <div class="card-top">
              <span class="course-tag">实训任务</span>
              <span class="status-badge" :class="statusClass(item.status)">
                {{ statusLabel(item.status) }}
              </span>
            </div>
            <h3 class="card-title">{{ item.taskName }}</h3>

            <!-- 进行中：显示进度条和百分比 -->
            <div v-if="item.status === 1" class="progress-section">
              <div class="progress-bar">
                <div
                  class="progress-bar__fill"
                  :style="{ width: `${getTaskProgress(item.id)}%` }"
                ></div>
              </div>
              <span class="progress-text">{{ getTaskProgress(item.id) }}%</span>
            </div>

            <footer class="card-actions">
              <span class="deadline">⏱️ {{ formatTime(item.startTime) }}</span>
              <button
                class="mini-btn"
                :class="actionBtnClass(item.status)"
                :disabled="actionLoading === item.id"
                @click="handleAction(item)"
              >
                {{ actionLabel(item) }}
              </button>
            </footer>
          </div>
          <div v-if="!loading && tasks.length === 0" class="empty-state">暂无实训任务</div>
        </main>
      </n-spin>

      <div class="pagination-wrapper" v-if="total > 0">
        <n-pagination
          v-model:page="page"
          :page-size="pageSize"
          :item-count="total"
          :page-sizes="[6, 12, 18]"
          show-size-picker
          @update:page="loadTasks"
          @update:page-size="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NPagination, NSpin } from 'naive-ui'
import { getStudentTrainingTasks } from '@/services/modules/student-dashboard.service'
import { getProfile } from '@/services/modules/student-dashboard.service'
import { startTraining, getTaskOverview } from '@/services/modules/studentTraining.service'
import type { StudentTrainingTask, StudentProfile } from '@/services/types/dashboard.types'

const loading = ref(false)
const actionLoading = ref<number | null>(null)
const router = useRouter()
const profile = ref<StudentProfile>({ userId: 0, username: '' })
const tasks = ref<StudentTrainingTask[]>([])
const activeTab = ref('all')
const page = ref(1)
const pageSize = ref(6)
const total = ref(0)

/** 缓存每个进行中任务的进度百分比 */
const taskProgressMap = ref<Record<number, number>>({})

const tabs = [
  { label: '全部实训', key: 'all' },
  { label: '进行中', key: 'ongoing' },
  { label: '已结束', key: 'ended' }
]

const stats = computed(() => {
  const all = tasks.value
  return { ongoing: all.filter(i => i.status === 1).length, ended: all.filter(i => i.status === 2).length }
})

function statusLabel(s: number): string {
  if (s === 0) return '待开始'
  if (s === 1) return '进行中'
  return '已完成'
}

function statusClass(s: number): string {
  if (s === 0) return 'not-started'
  if (s === 1) return 'ongoing'
  return 'ended'
}

function actionBtnClass(s: number): string {
  if (s === 0) return 'mini-btn--primary'
  if (s === 1) return 'mini-btn--success'
  return 'mini-btn--default'
}

function actionLabel(item: StudentTrainingTask): string {
  if (item.status === 0) return '开始实训'
  if (item.status === 1) return '继续实训'
  return '查看总结'
}

function formatTime(t?: string): string {
  if (!t) return '-'
  return t.slice(0, 10)
}

/** 获取任务进度百分比 */
function getTaskProgress(taskId: number): number {
  return taskProgressMap.value[taskId] ?? 0
}

/** 加载进行中任务的进度数据 */
async function loadProgressForOngoingTasks(): Promise<void> {
  const ongoingTasks = tasks.value.filter(t => t.status === 1)
  const progressPromises = ongoingTasks.map(async (task) => {
    try {
      const res = await getTaskOverview(task.id)
      if (res.code === 200 && res.data) {
        const allNodes = res.data.phases.flatMap(p => p.nodes)
        if (allNodes.length === 0) return
        const completed = allNodes.filter(n => n.status === 2).length
        taskProgressMap.value[task.id] = Math.round((completed / allNodes.length) * 100)
      }
    } catch {
      // 静默处理进度加载失败
    }
  })
  await Promise.all(progressPromises)
}

async function loadTasks(): Promise<void> {
  loading.value = true
  try {
    let statusParam: number | undefined
    if (activeTab.value === 'ongoing') statusParam = 1
    else if (activeTab.value === 'ended') statusParam = 2
    const res = await getStudentTrainingTasks(page.value, pageSize.value, statusParam)
    if (res.code === 200) {
      tasks.value = res.data.records
      total.value = res.data.total
      // 加载进行中任务的进度
      await loadProgressForOngoingTasks()
    }
  } finally {
    loading.value = false
  }
}

function switchTab(key: string): void {
  activeTab.value = key
  page.value = 1
  loadTasks()
}

function handleSizeChange(size: number): void {
  pageSize.value = size
  page.value = 1
  loadTasks()
}

/**
 * 处理实训操作按钮点击
 * - status=0: 开始实训（Participation 0→1），然后进入执行页面
 * - status=1: 恢复位置，直接进入执行页面
 * - status=2: 查看总结页面
 */
async function handleAction(item: StudentTrainingTask): Promise<void> {
  if (item.status === 0) {
    // 开始实训：调用 startTraining API，然后导航到执行页面
    actionLoading.value = item.id
    try {
      const res = await startTraining(item.id)
      if (res.code === 200) {
        // 成功开始，导航到实训执行页面
        router.push(`/student/training/${item.id}/execute`)
      }
    } finally {
      actionLoading.value = null
    }
  } else if (item.status === 1) {
    // 进行中：恢复到上次中断位置（路由守卫会处理位置恢复）
    router.push(`/student/training/${item.id}/execute`)
  } else {
    // 已完成：查看总结页面
    router.push(`/student/training/${item.id}/summary`)
  }
}

onMounted(async () => {
  const profRes = await getProfile()
  if (profRes.code === 200) profile.value = profRes.data
  loadTasks()
})
</script>

<style scoped>
.workbench-wrapper {
  min-height: 100vh;
  background-color: var(--color-background, #f8fafc);
  padding: 40px 20px 50px;
}

.hero-section {
  text-align: center;
  margin-bottom: 40px;
}

.logo-text {
  font-size: 26px;
  font-weight: 800;
  color: var(--color-heading, #1e293b);
  margin: 0 0 8px 0;
}

.slogan {
  color: var(--color-text, #64748b);
  font-size: 14px;
  margin: 0;
}

.content-container {
  max-width: 1100px;
  margin: 0 auto;
}

.filter-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 10px;
}

.nav-left {
  display: flex;
  gap: 24px;
}

.nav-item {
  color: var(--color-text, #64748b);
  font-weight: 600;
  cursor: pointer;
  padding-bottom: 4px;
  border-bottom: 2px solid transparent;
}

.nav-item.active {
  color: var(--color-primary-500, #6366f1);
  border-bottom-color: var(--color-primary-500, #6366f1);
}

.nav-right-stats {
  font-size: 13px;
  color: var(--color-gray-400, #94a3b8);
}

.nav-right-stats b {
  color: var(--color-heading, #1e293b);
  margin-left: 4px;
}

.divider {
  margin: 0 12px;
  opacity: 0.3;
}

.glass-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  min-height: 400px;
  align-content: start;
}

.glass-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  transition: transform 0.3s ease;
}

.glass-card:hover {
  transform: translateY(-5px);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.course-tag {
  font-size: 12px;
  color: var(--color-primary-500, #6366f1);
  background: var(--color-primary-50, #eef2ff);
  padding: 2px 8px;
  border-radius: 6px;
  font-weight: 600;
}

.status-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 10px;
}

.status-badge.not-started {
  color: var(--color-amber-600, #d97706);
  background: var(--color-amber-50, #fffbeb);
}

.status-badge.ongoing {
  color: var(--color-emerald-600, #059669);
  background: var(--color-emerald-50, #ecfdf5);
}

.status-badge.ended {
  color: var(--color-gray-500, #64748b);
  background: var(--color-gray-100, #f1f5f9);
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-heading, #1e293b);
  margin: 0 0 16px;
  line-height: 1.4;
}

/* 进度条区域 */
.progress-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: var(--color-gray-100, #f1f5f9);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar__fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-emerald-400, #34d399), var(--color-emerald-500, #10b981));
  border-radius: 3px;
  transition: width 0.4s ease;
}

.progress-text {
  font-size: 12px;
  font-weight: 700;
  color: var(--color-emerald-600, #059669);
  min-width: 36px;
  text-align: right;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--color-gray-100, #f1f5f9);
  padding-top: 16px;
}

.deadline {
  font-size: 12px;
  color: var(--color-gray-400, #94a3b8);
}

.mini-btn {
  border: 1px solid var(--color-gray-200, #e2e8f0);
  background: white;
  padding: 6px 16px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.mini-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.mini-btn--primary {
  background: var(--color-primary-500, #6366f1);
  color: white;
  border-color: var(--color-primary-500, #6366f1);
}

.mini-btn--primary:hover:not(:disabled) {
  background: var(--color-primary-600, #4f46e5);
}

.mini-btn--success {
  background: var(--color-emerald-500, #10b981);
  color: white;
  border-color: var(--color-emerald-500, #10b981);
}

.mini-btn--success:hover:not(:disabled) {
  background: var(--color-emerald-600, #059669);
}

.mini-btn--default:hover:not(:disabled) {
  border-color: var(--color-primary-300, #a5b4fc);
  color: var(--color-primary-600, #4f46e5);
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 12px;
  padding: 12px;
}

.empty-state {
  grid-column: span 3;
  text-align: center;
  padding: 60px 0;
  color: var(--color-gray-400, #94a3b8);
  border-radius: 20px;
  border: 1px dashed var(--color-gray-300, #cbd5e1);
}
</style>
