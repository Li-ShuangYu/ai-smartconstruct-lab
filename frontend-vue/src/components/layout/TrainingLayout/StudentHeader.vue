<template>
  <header class="console-header">
    <div @click="navigateToHome" class="brand-group">
      <svg class="brand-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><circle cx="12" cy="12" r="8" stroke-width="2"></circle></svg>
      <span class="brand-title">
        学生工作台
        <span class="stage-badge">| {{ currentStage }}</span>
      </span>
    </div>
    
    <div class="sys-info-group">
      <div class="divider"></div>
      <div class="time-display">
        <svg class="time-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2" ry="2" stroke-width="2"></rect><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 2v4M8 2v4M3 10h18"></path></svg>
        <span>{{ currentTime }}</span>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const currentTime = ref('')

const routeStageMap = {
  '/student/task-receive': { stage: '需求分析阶段' },
  '/student/task-select': { stage: '需求分析阶段' },
  '/student/task-split': { stage: '需求分析阶段' },
  '/student/scheme-upload': { stage: '方案设计阶段' },
  '/student/scheme-detail': { stage: '方案设计阶段' },
  '/student/my-score-result': { stage: '方案设计阶段' },
  '/student/robot-debug': { stage: '冰达机器人代码设计' },
  '/student/deploy': { stage: '冰达机器人代码设计' },
  '/student/debug': { stage: '冰达机器人代码设计' }
}

const currentStage = computed(() => routeStageMap[route.path]?.stage || '控制台大盘')

const formatDateTime = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`
}

let timer = null
onMounted(() => {
  currentTime.value = formatDateTime()
  timer = setInterval(() => { currentTime.value = formatDateTime() }, 1000)
})
onBeforeUnmount(() => { if (timer) clearInterval(timer) })

const navigateToHome = () => router.push('/student/workbench')
</script>

<style scoped>
.console-header {
  flex-shrink: 0; 
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #F1F5F9;
  background: rgba(255, 255, 255, 0.5);
}

.brand-group { display: flex; align-items: center; gap: 10px; cursor: pointer; transition: opacity 0.2s; }
.brand-group:hover { opacity: 0.8; }
.brand-icon { width: 22px; height: 22px; color: #4F46E5; }
.brand-title { font-size: 18px; font-weight: 800; color: #1E293B; display: flex; align-items: center; }
.stage-badge { font-size: 13px; font-weight: 600; color: #64748B; margin-left: 12px; padding: 2px 8px; background: #F8FAFC; border-radius: 6px; border: 1px solid #E2E8F0; }

.sys-info-group { display: flex; align-items: center; gap: 16px; }
.divider { width: 1px; height: 16px; background-color: #E2E8F0; }
.time-display { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; color: #475569; font-family: monospace; }
.time-icon { width: 16px; height: 16px; color: #94A3B8; }
</style>