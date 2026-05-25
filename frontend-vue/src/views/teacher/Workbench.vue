<template>
  <div class="workbench-container">
    <header class="dashboard-header">
      <div class="greeting-area">
        <h1 class="title">欢迎回来，{{ profile.realName || profile.username }} 老师。</h1>
        <p class="subtitle">进行中实训 {{ stats.ongoingTasks }} 个，累计课程 {{ stats.totalCourses }} 门。</p>
      </div>
      <div class="metrics-grid">
        <div class="metric-card">
          <span class="label">累计实训任务</span>
          <span class="value">{{ stats.totalTasks }}</span>
        </div>
        <div class="metric-card">
          <span class="label">进行中</span>
          <span class="value highlight">{{ stats.ongoingTasks }}</span>
        </div>
      </div>
    </header>

    <section class="section-block">
      <h2 class="section-title">快捷入口</h2>
      <div class="actions-grid">
        <div class="action-card primary-action" @click="$router.push('/teacher/training-create')">
          <div class="icon-wrapper icon-blue">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v18M3 12h18"/></svg>
          </div>
          <div class="action-info">
            <h3>创建实训模板</h3>
            <p>通过图形化拖拽，编排新的教学实训模板</p>
          </div>
        </div>
        <div class="action-card" @click="$router.push('/teacher/training-manage')">
          <div class="icon-wrapper icon-green">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>
          </div>
          <div class="action-info">
            <h3>实训管理</h3>
            <p>查看和下发班级实训任务</p>
          </div>
        </div>
        <div class="action-card" @click="$router.push('/teacher/class-course-manage')">
          <div class="icon-wrapper icon-orange">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          </div>
          <div class="action-info">
            <h3>课程管理</h3>
            <p>管理教学课程，设置选课规则</p>
          </div>
        </div>
      </div>
    </section>

    <section class="section-block">
      <h2 class="section-title">近期实训任务</h2>
      <n-spin :show="loading">
        <div class="history-grid">
          <div class="history-card" v-for="task in recentTasks" :key="task.id">
            <div class="card-header">
              <div class="tags-row">
                <span class="tag" :class="statusClass(task.status)">{{ statusLabel(task.status) }}</span>
                <span class="tag" :class="trainingTypeClass(task.isInClass)">{{ trainingTypeLabel(task.isInClass) }}</span>
              </div>
              <span class="time">{{ formatTime(task.createdAt) }}</span>
            </div>
            <h3 class="course-name" :title="task.taskName">{{ task.taskName }}</h3>
            <p class="target-name">{{ getTargetName(task) }}</p>
            <div class="card-footer">
              <button class="btn-text" @click="$router.push('/teacher/training-manage')">查看详情 →</button>
            </div>
          </div>
          <div v-if="!loading && recentTasks.length === 0" class="empty-state">暂无实训任务</div>
        </div>
      </n-spin>
    </section>
  </div>
</template>

<script setup lang="ts">
/**
 * 教师工作台页面
 * 
 * 教师端主页面，提供以下功能：
 * - 显示教师个人欢迎信息和统计数据
 * - 快捷入口（创建实训、实训管理、课程管理）
 * - 近期实训任务列表
 * 
 * @component Workbench.vue
 */
import { ref, onMounted } from 'vue'
import { NSpin } from 'naive-ui'
import { getDashboardStats, getProfile, getTrainingTasks } from '@/services/modules/teacher-dashboard.service'
import type { TeacherStats, TeacherProfile, TrainingTaskItem } from '@/services/types/dashboard.types'

// === 响应式状态 ===

/** 加载状态 */
const loading = ref(false)

/** 教师统计数据 */
const stats = ref<TeacherStats>({ ongoingTasks: 0, totalCourses: 0, totalTasks: 0 })

/** 教师个人资料 */
const profile = ref<TeacherProfile>({ userId: 0, username: '' })

/** 近期实训任务列表 */
const recentTasks = ref<TrainingTaskItem[]>([])

// === 辅助方法 ===

/**
 * 获取状态标签文本
 * 
 * @param s 状态码：0=未开始，1=进行中，2=已结束
 * @returns 状态文本
 */
function statusLabel(s: number) {
  if (s === 0) return '未开始'
  if (s === 1) return '进行中'
  return '已结束'
}

/**
 * 获取状态样式类名
 * 
 * @param s 状态码
 * @returns CSS类名
 */
function statusClass(s: number) {
  if (s === 0) return 'default'
  if (s === 1) return 'warning'
  return 'active'
}

/**
 * 获取实训类型标签文本
 * 
 * @param isInClass 是否课堂实训：0=课后实训，1=课堂实训
 * @returns 类型文本
 */
function trainingTypeLabel(isInClass?: number) {
  return isInClass === 1 ? '课堂实训' : '课后实训'
}

/**
 * 获取实训类型样式类名
 * 
 * @param isInClass 是否课堂实训
 * @returns CSS类名
 */
function trainingTypeClass(isInClass?: number) {
  return isInClass === 1 ? 'class-training' : 'after-class'
}

/**
 * 获取下发目标名称
 * 
 * @param task 实训任务
 * @returns 目标名称（班级/课程）
 */
function getTargetName(task: TrainingTaskItem) {
  if (task.dispatchTargetName) {
    const scope = task.dispatchScope === 1 ? '班级：' : task.dispatchScope === 2 ? '课程：' : ''
    return scope + task.dispatchTargetName
  }
  return '-'
}

/**
 * 格式化时间显示
 * 
 * @param t 时间字符串
 * @returns 格式化后的时间文本
 */
function formatTime(t?: string) {
  if (!t) return ''
  return '更新于 ' + t.slice(0, 16).replace('T', ' ')
}

// === 生命周期 ===

/**
 * 组件挂载时加载数据
 * 
 * 并行请求统计数据、个人资料和近期任务列表
 */
onMounted(async () => {
  loading.value = true
  try {
    const [statsRes, profileRes, tasksRes] = await Promise.all([
      getDashboardStats(), getProfile(), getTrainingTasks(1, 5)
    ])
    if (statsRes.code === 200) stats.value = statsRes.data
    if (profileRes.code === 200) profile.value = profileRes.data
    if (tasksRes.code === 200) recentTasks.value = tasksRes.data.records
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.workbench-container { display: flex; flex-direction: column; gap: 32px; max-width: 1200px; margin: 0 auto; }
.dashboard-header { margin-top: -10px; display: flex; justify-content: space-between; align-items: flex-end; padding-bottom: 24px; border-bottom: 1px solid #E5E7EB; }
.title { font-size: 32px; font-weight: 800; color: #0F172A; margin: 0 0 8px 0; }
.subtitle { font-size: 15px; color: #64748B; margin: 0; }
.metrics-grid { display: flex; gap: 32px; }
.metric-card { display: flex; flex-direction: column; align-items: flex-end; }
.metric-card .label { font-size: 13px; color: #64748B; margin-bottom: 4px; font-weight: 500; }
.metric-card .value { font-size: 28px; font-weight: 800; color: #0F172A; line-height: 1; }
.metric-card .value.highlight { color: #4F46E5; }
.section-title { font-size: 18px; font-weight: 700; color: #0F172A; margin: 0 0 16px 0; }
.actions-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.action-card { display: flex; align-items: flex-start; padding: 24px; background-color: #fff; border: 1px solid #E2E8F0; border-radius: 12px; cursor: pointer; transition: all 0.2s ease; box-shadow: 0 1px 2px rgba(0,0,0,0.02); }
.action-card:hover { border-color: #818CF8; transform: translateY(-2px); box-shadow: 0 8px 16px -4px rgba(79, 70, 229, 0.1); }
.primary-action { background-color: #FAFAFF; }
.icon-wrapper { display: flex; align-items: center; justify-content: center; width: 48px; height: 48px; border-radius: 10px; margin-right: 16px; flex-shrink: 0; }
.icon-blue { background-color: #EEF2FF; color: #4F46E5; }
.icon-green { background-color: #ECFDF5; color: #059669; }
.icon-orange { background-color: #FFF7ED; color: #EA580C; }
.action-info { flex: 1; }
.action-info h3 { font-size: 16px; font-weight: 700; margin: 0 0 6px 0; color: #1E293B; }
.action-info p { font-size: 13px; color: #64748B; margin: 0; line-height: 1.5; }
.history-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 20px; }
.history-card { background-color: #fff; border: 1px solid #E2E8F0; border-radius: 12px; padding: 20px; display: flex; flex-direction: column; transition: box-shadow 0.2s ease; }
.history-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.tags-row { display: flex; gap: 8px; }
.tag { font-size: 12px; padding: 4px 10px; border-radius: 6px; font-weight: 600; }
.tag.active { background-color: #DCFCE7; color: #166534; }
.tag.warning { background-color: #FEF3C7; color: #92400E; }
.tag.default { background-color: #F1F5F9; color: #475569; }
.tag.class-training { background-color: #E0E7FF; color: #4338CA; }
.tag.after-class { background-color: #F0FDF4; color: #059669; }
.time { font-size: 12px; color: #94A3B8; font-weight: 500; }
.course-name { font-size: 16px; font-weight: 700; margin: 0 0 8px 0; color: #1E293B; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.target-name { font-size: 13px; color: #64748B; margin: 0 0 16px 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.card-footer { display: flex; justify-content: flex-end; padding-top: 16px; border-top: 1px solid #F1F5F9; }
.stats { font-size: 13px; color: #64748B; font-weight: 500; }
.btn-text { background: none; border: none; color: #4F46E5; font-size: 13px; font-weight: 600; cursor: pointer; transition: color 0.2s; }
.btn-text:hover { color: #38bdf8; }
.empty-state { grid-column: 1/-1; text-align: center; padding: 60px; color: #94A3B8; }
</style>
