<template>
  <div class="my-class-container">
    <header class="class-summary-bar">
      <div class="class-title-area">
        <div class="avatar-icon">🏫</div>
        <div>
          <h1 class="class-name">{{ profile.className || '我的班级' }}</h1>
          <span class="class-desc">{{ profile.majorName || '' }} | {{ profile.deptName || '' }} | {{ classmates.length }}人</span>
        </div>
      </div>
      <div class="class-stats">
        <div class="stat-item">
          <span class="stat-label">学号</span>
          <span class="stat-value highlight">{{ profile.studentNo || '-' }}</span>
        </div>
      </div>
    </header>

    <div class="content-grid">
      <section class="task-section">
        <h2 class="section-title">班级实训任务</h2>
        <n-spin :show="taskLoading">
          <div class="task-list">
            <div v-for="task in tasks" :key="task.id" class="task-row">
              <div class="task-main">
                <span class="status-dot" :class="statusClass(task.status)"></span>
                <span class="task-name" :title="task.taskName">{{ task.taskName }}</span>
              </div>
              <div class="task-meta">
                <span class="task-time">{{ formatTime(task.startTime) }}</span>
                <button class="text-btn" @click="$router.push(`/training/student-training/welcome?taskId=${task.id}`)">进入实训</button>
              </div>
            </div>
            <div v-if="!taskLoading && tasks.length === 0" class="empty-text">暂无实训任务</div>
          </div>
        </n-spin>
      </section>

      <section class="classmates-section">
        <h2 class="section-title">同班同学 ({{ classmates.length }})</h2>
        <n-spin :show="memberLoading">
          <div class="avatar-grid">
            <div v-for="s in classmates" :key="s.userId" class="student-item">
              <div class="student-avatar">{{ s.realName.charAt(0) }}</div>
              <span class="student-name" :title="s.realName">{{ s.realName }}</span>
            </div>
            <div v-if="!memberLoading && classmates.length === 0" class="empty-text">暂无数据</div>
          </div>
        </n-spin>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NSpin } from 'naive-ui'
import { getClassmates, getProfile, getStudentTrainingTasks } from '@/services/modules/student-dashboard.service'
import type { StudentProfile, Classmate, StudentTrainingTask } from '@/services/types/dashboard.types'

const taskLoading = ref(false)
const memberLoading = ref(false)
const profile = ref<StudentProfile>({ userId: 0, username: '' })
const classmates = ref<Classmate[]>([])
const tasks = ref<StudentTrainingTask[]>([])

function statusClass(s: number) {
  if (s === 0) return 'pending'
  if (s === 1) return 'active'
  return 'ended'
}
function formatTime(t?: string) {
  if (!t) return '-'
  return t.slice(0, 10)
}

onMounted(async () => {
  const profRes = await getProfile()
  if (profRes.code === 200) profile.value = profRes.data

  memberLoading.value = true
  try {
    const res = await getClassmates()
    if (res.code === 200) classmates.value = res.data
  } finally {
    memberLoading.value = false
  }

  taskLoading.value = true
  try {
    const res = await getStudentTrainingTasks(1, 10)
    if (res.code === 200) tasks.value = res.data.records
  } finally {
    taskLoading.value = false
  }
})
</script>

<style scoped>
.my-class-container { padding: 24px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }
.class-summary-bar { background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px; padding: 16px 24px; display: flex; justify-content: space-between; align-items: center; }
.class-title-area { display: flex; align-items: center; gap: 16px; }
.avatar-icon { font-size: 32px; background: #F1F5F9; width: 48px; height: 48px; display: flex; align-items: center; justify-content: center; border-radius: 8px; }
.class-name { font-size: 18px; font-weight: 800; color: #0F172A; margin: 0 0 4px 0; }
.class-desc { font-size: 13px; color: #64748B; }
.class-stats { display: flex; align-items: center; gap: 24px; }
.stat-item { display: flex; flex-direction: column; align-items: flex-end; }
.stat-label { font-size: 12px; color: #94A3B8; font-weight: 600; }
.stat-value { font-size: 20px; font-weight: 800; color: #1E293B; line-height: 1; }
.stat-value.highlight { color: #4F46E5; }
.content-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; align-items: start; }
.task-section, .classmates-section { background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px; padding: 20px 12px 20px 20px; height: 480px; display: flex; flex-direction: column; box-sizing: border-box; }
.section-title { font-size: 15px; font-weight: 700; color: #1E293B; margin: 0 8px 16px 0; padding-bottom: 8px; border-bottom: 1px solid #E2E8F0; flex-shrink: 0; }
.task-list, .avatar-grid { flex: 1; overflow-y: auto; padding-right: 8px; }
.task-list::-webkit-scrollbar, .avatar-grid::-webkit-scrollbar { width: 4px; }
.task-list::-webkit-scrollbar-thumb, .avatar-grid::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 4px; }
.task-row { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #F1F5F9; }
.task-row:last-child { border-bottom: none; }
.task-main { display: flex; align-items: center; gap: 10px; flex: 1; overflow: hidden; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.status-dot.active { background: #10B981; }
.status-dot.pending { background: #F59E0B; }
.status-dot.ended { background: #94A3B8; }
.task-name { font-size: 14px; font-weight: 600; color: #334155; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding-right: 16px; }
.task-meta { display: flex; align-items: center; gap: 16px; flex-shrink: 0; }
.task-time { font-size: 12px; color: #94A3B8; }
.text-btn { background: none; border: none; color: #4F46E5; font-size: 13px; font-weight: 700; cursor: pointer; }
.text-btn:hover { color: #4338CA; text-decoration: underline; }
.avatar-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(64px, 1fr)); gap: 12px; align-content: start; }
.student-item { display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 6px 0; border-radius: 8px; transition: background 0.2s; }
.student-item:hover { background: #F8FAFC; }
.student-avatar { width: 40px; height: 40px; border-radius: 50%; background: #EEF2FF; color: #4F46E5; font-weight: 700; font-size: 16px; display: flex; align-items: center; justify-content: center; }
.student-name { font-size: 12px; color: #475569; text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 100%; }
.empty-text { text-align: center; color: #94A3B8; padding: 20px; font-size: 13px; }
</style>
