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
              <span class="status-dot" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span>
            </div>
            <h3 class="card-title">{{ item.taskName }}</h3>
            <footer class="card-actions">
              <span class="deadline">⏱️ {{ formatTime(item.startTime) }}</span>
              <button class="mini-btn" @click="$router.push(`/training/student-training/welcome?taskId=${item.id}`)">
                {{ item.status === 1 ? '继续' : (item.status === 0 ? '开始' : '查看') }}
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
import { NPagination, NSpin } from 'naive-ui'
import { getStudentTrainingTasks } from '@/services/modules/student-dashboard.service'
import { getProfile } from '@/services/modules/student-dashboard.service'
import type { StudentTrainingTask, StudentProfile } from '@/services/types/dashboard.types'

const loading = ref(false)
const profile = ref<StudentProfile>({ userId: 0, username: '' })
const tasks = ref<StudentTrainingTask[]>([])
const activeTab = ref('all')
const page = ref(1)
const pageSize = ref(6)
const total = ref(0)

const tabs = [
  { label: '全部实训', key: 'all' },
  { label: '进行中', key: 'ongoing' },
  { label: '已结束', key: 'ended' }
]

const stats = computed(() => {
  const all = tasks.value
  return { ongoing: all.filter(i => i.status === 1).length, ended: all.filter(i => i.status === 2).length }
})

function statusLabel(s: number) {
  if (s === 0) return '待开始'
  if (s === 1) return '进行中'
  return '已完成'
}
function statusClass(s: number) {
  if (s === 0) return 'not_started'
  if (s === 1) return 'ongoing'
  return 'ended'
}
function formatTime(t?: string) {
  if (!t) return '-'
  return t.slice(0, 10)
}

async function loadTasks() {
  loading.value = true
  try {
    let statusParam: number | undefined
    if (activeTab.value === 'ongoing') statusParam = 1
    else if (activeTab.value === 'ended') statusParam = 2
    const res = await getStudentTrainingTasks(page.value, pageSize.value, statusParam)
    if (res.code === 200) {
      tasks.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function switchTab(key: string) {
  activeTab.value = key
  page.value = 1
  loadTasks()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  page.value = 1
  loadTasks()
}

onMounted(async () => {
  const profRes = await getProfile()
  if (profRes.code === 200) profile.value = profRes.data
  loadTasks()
})
</script>

<style scoped>
.workbench-wrapper { min-height: 100vh; background-color: #f8fafc; padding: 40px 20px 50px; }
.hero-section { text-align: center; margin-bottom: 40px; }
.logo-text { font-size: 26px; font-weight: 800; color: #1e293b; margin: 0 0 8px 0; }
.slogan { color: #64748b; font-size: 14px; margin: 0; }
.content-container { max-width: 1100px; margin: 0 auto; }
.filter-nav { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; padding: 0 10px; }
.nav-left { display: flex; gap: 24px; }
.nav-item { color: #64748b; font-weight: 600; cursor: pointer; padding-bottom: 4px; border-bottom: 2px solid transparent; }
.nav-item.active { color: #6366f1; border-bottom-color: #6366f1; }
.nav-right-stats { font-size: 13px; color: #94a3b8; }
.nav-right-stats b { color: #1e293b; margin-left: 4px; }
.divider { margin: 0 12px; opacity: 0.3; }
.glass-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; min-height: 400px; align-content: start; }
.glass-card { background: rgba(255,255,255,0.85); backdrop-filter: blur(10px); border-radius: 20px; padding: 20px; border: 1px solid rgba(255,255,255,0.4); transition: transform 0.3s ease; }
.glass-card:hover { transform: translateY(-5px); }
.card-top { display: flex; justify-content: space-between; margin-bottom: 12px; }
.course-tag { font-size: 12px; color: #6366f1; background: #eef2ff; padding: 2px 8px; border-radius: 6px; font-weight: 600; }
.status-dot { font-size: 12px; color: #64748b; }
.status-dot.ongoing { color: #10b981; }
.status-dot.ended { color: #94a3b8; }
.status-dot.not_started { color: #f59e0b; }
.card-title { font-size: 16px; font-weight: 700; color: #1e293b; margin: 0 0 20px; line-height: 1.4; }
.card-actions { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #f1f5f9; padding-top: 16px; }
.deadline { font-size: 12px; color: #94a3b8; }
.mini-btn { border: 1px solid #e2e8f0; background: white; padding: 6px 16px; border-radius: 8px; font-size: 12px; font-weight: 600; cursor: pointer; }
.pagination-wrapper { margin-top: 24px; display: flex; justify-content: center; background: rgba(255,255,255,0.85); border-radius: 12px; padding: 12px; }
.empty-state { grid-column: span 3; text-align: center; padding: 60px 0; color: #94A3B8; border-radius: 20px; border: 1px dashed #cbd5e1; }
</style>
