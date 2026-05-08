<template>
  <div class="page-wrapper flex items-center justify-center w-full h-full min-h-[calc(100vh-100px)]">
    
    <div class="glass-card w-full max-w-5xl p-8 flex flex-col z-10 h-[700px]">
      
      <div class="flex justify-between items-end mb-6">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: RESOURCE_READ</div>
          <h1 class="text-3xl font-bold text-gray-800 flex items-center gap-3">
             <svg class="text-indigo-500" style="width: 28px; height: 28px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
             资源阅读监控
          </h1>
        </div>
        <div class="text-sm text-gray-500 bg-white/50 px-4 py-2 rounded-lg border border-gray-100 shadow-sm">
          正在监控目标：<span class="font-semibold text-gray-700">{{ nodeConfig.resourceName }}</span>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div class="bg-white/60 p-5 rounded-xl border border-gray-100 shadow-sm flex flex-col justify-center">
          <span class="text-sm text-gray-500 mb-1">班级平均阅读完成度</span>
          <div class="flex items-end gap-2">
            <span class="text-3xl font-bold text-gradient-primary">{{ avgProgress }}%</span>
          </div>
          <div class="w-full bg-gray-200 rounded-full h-1.5 mt-3">
            <div class="bg-indigo-500 h-1.5 rounded-full" :style="{ width: avgProgress + '%' }"></div>
          </div>
        </div>

        <div class="bg-white/60 p-5 rounded-xl border border-gray-100 shadow-sm flex flex-col justify-center">
          <span class="text-sm text-gray-500 mb-1">班级平均阅读时长</span>
          <div class="flex items-end gap-2">
            <span class="text-3xl font-bold text-gray-700">{{ avgTimeStr }}</span>
          </div>
          <span class="text-xs text-green-500 mt-2 flex items-center gap-1">
             <svg style="width: 12px; height: 12px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" /></svg>
             学习状态良好
          </span>
        </div>

        <div class="bg-white/60 p-5 rounded-xl border border-gray-100 shadow-sm flex flex-col justify-center">
          <span class="text-sm text-gray-500 mb-1">达标人数 / 总人数</span>
          <div class="flex items-end gap-2">
            <span class="text-3xl font-bold text-gray-700">{{ completedCount }}</span>
            <span class="text-lg text-gray-400 mb-1">/ {{ studentList.length }}</span>
          </div>
        </div>
      </div>

      <div class="flex-1 bg-white/40 rounded-xl border border-gray-200/60 overflow-hidden flex flex-col">
        <div class="grid grid-cols-12 gap-4 px-6 py-3 border-b border-gray-200/50 bg-gray-50/50 text-xs font-semibold text-gray-500 uppercase">
          <div class="col-span-3">学生姓名</div>
          <div class="col-span-5">阅读完成度 (百分比)</div>
          <div class="col-span-2 text-center">停留时长</div>
          <div class="col-span-2 text-right">状态判定</div>
        </div>
        
        <div class="flex-1 overflow-y-auto p-2 space-y-1">
          <div v-for="student in studentList" :key="student.id" class="grid grid-cols-12 gap-4 px-4 py-3 items-center rounded-lg hover:bg-white/60 transition-colors border border-transparent hover:border-gray-100">
            <div class="col-span-3 flex items-center gap-3">
              <div class="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-100 to-purple-100 text-indigo-700 flex items-center justify-center text-xs font-bold">
                {{ student.name.charAt(0) }}
              </div>
              <span class="text-sm font-medium text-gray-700">{{ student.name }}</span>
            </div>
            
            <div class="col-span-5 flex items-center gap-3">
              <div class="w-full bg-gray-200 rounded-full h-2">
                <div class="h-2 rounded-full transition-all duration-500" 
                     :class="student.progress === 100 ? 'bg-green-400' : 'bg-indigo-400'"
                     :style="{ width: student.progress + '%' }"></div>
              </div>
              <span class="text-xs font-semibold text-gray-500 w-8">{{ student.progress }}%</span>
            </div>

            <div class="col-span-2 text-center text-sm font-mono text-gray-600">
              {{ formatTime(student.timeSpent) }}
            </div>

            <div class="col-span-2 flex justify-end">
              <span v-if="student.isCompleted" class="px-2 py-1 bg-green-50 text-green-600 border border-green-100 rounded text-xs font-medium flex items-center gap-1">
                <svg style="width: 12px; height: 12px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                已达标
              </span>
              <span v-else class="px-2 py-1 bg-amber-50 text-amber-600 border border-amber-100 rounded text-xs font-medium flex items-center gap-1">
                <svg style="width: 12px; height: 12px; animation: spin 2s linear infinite;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
                阅读中
              </span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const nodeConfig = ref({
  resourceId: 'doc-crypto-101',
  resourceName: '密码学基础与SM4算法导论.pdf'
})

// 模拟学生阅读监控数据
const studentList = ref([
  { id: 1, name: '陈同学', progress: 100, timeSpent: 125, isCompleted: true },
  { id: 2, name: '林同学', progress: 45, timeSpent: 30, isCompleted: false },
  { id: 3, name: '张同学', progress: 100, timeSpent: 89, isCompleted: true },
  { id: 4, name: '王同学', progress: 10, timeSpent: 12, isCompleted: false },
  { id: 5, name: '李同学', progress: 80, timeSpent: 55, isCompleted: false },
  { id: 6, name: '赵同学', progress: 100, timeSpent: 180, isCompleted: true },
])

// 统计计算
const avgProgress = computed(() => {
  const sum = studentList.value.reduce((acc, curr) => acc + curr.progress, 0)
  return Math.round(sum / studentList.value.length)
})

const avgTimeStr = computed(() => {
  const sum = studentList.value.reduce((acc, curr) => acc + curr.timeSpent, 0)
  return formatTime(Math.round(sum / studentList.value.length))
})

const completedCount = computed(() => {
  return studentList.value.filter(s => s.isCompleted).length
})

// 格式化工具
const formatTime = (seconds) => {
  const m = Math.floor(seconds / 60).toString().padStart(2, '0')
  const s = (seconds % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}
</script>

<style scoped>
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>