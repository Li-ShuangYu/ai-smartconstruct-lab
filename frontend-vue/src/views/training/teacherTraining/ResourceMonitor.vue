<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative">
      
      <div class="flex justify-between items-end mb-6 shrink-0">
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

      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6 shrink-0">
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

      <div class="bg-white/40 rounded-xl border border-gray-200/60 overflow-hidden flex flex-col" >
        
        <div class="px-6 py-3 bg-white/60 border-b border-gray-200 flex justify-between items-center shrink-0">
          <div class="relative w-72">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索学生姓名..."
              class="w-full pl-9 pr-4 py-2 bg-white border border-gray-200 rounded-lg text-sm text-gray-700 focus:outline-none focus:border-indigo-400 focus:ring-1 focus:ring-indigo-400 transition-colors shadow-sm"
            >
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
        </div>

        <div class="grid grid-cols-12 gap-4 px-6 py-3 border-b border-gray-200/50 bg-gray-50/50 text-xs font-semibold text-gray-500 uppercase shrink-0">
          <div class="col-span-3">学生姓名</div>
          <div class="col-span-5">阅读完成度 (百分比)</div>
          <div class="col-span-2 text-center">停留时长</div>
          <div class="col-span-2 text-right">状态判定</div>
        </div>
        
        <div class=" overflow-y-auto p-2 space-y-1 custom-scrollbar">
          <template v-if="paginatedStudents.length > 0">
            <div v-for="student in paginatedStudents" :key="student.id" class="grid grid-cols-12 gap-4 px-4 py-3 items-center rounded-lg hover:bg-white/60 transition-colors border border-transparent hover:border-gray-100">
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
          </template>
          <div v-else class="h-full flex flex-col items-center justify-center text-gray-400 space-y-3 pb-6">
             <svg style="width: 40px; height: 40px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
             <span class="text-sm font-medium">暂无匹配学生</span>
          </div>
        </div>

        <div class="px-6 py-3 border-t border-gray-200/60 bg-white/40 flex items-center justify-between shrink-0">
          <button @click="prevPage" :disabled="currentPage === 1" class="px-4 py-2 rounded-lg text-sm font-bold transition-colors" :class="currentPage === 1 ? 'text-gray-400 cursor-not-allowed bg-transparent' : 'text-indigo-600 bg-indigo-50 hover:bg-indigo-100'">
            上一页
          </button>
          <div class="text-sm font-bold text-gray-500">
            第 <span class="text-indigo-600 mx-1">{{ currentPage }}</span> 页 / 共 <span class="mx-1">{{ totalPages || 1 }}</span> 页
          </div>
          <button @click="nextPage" :disabled="currentPage >= totalPages || totalPages === 0" class="px-4 py-2 rounded-lg text-sm font-bold transition-colors" :class="currentPage >= totalPages || totalPages === 0 ? 'text-gray-400 cursor-not-allowed bg-transparent' : 'text-indigo-600 bg-indigo-50 hover:bg-indigo-100'">
            下一页
          </button>
        </div>

      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const nodeConfig = ref({
  resourceId: 'doc-crypto-101',
  resourceName: '密码学基础与SM4算法导论.pdf'
})

// 模拟学生数据 (扩充至10条演示分页)
const studentList = ref([
  { id: 1, name: '陈同学', progress: 100, timeSpent: 125, isCompleted: true },
  { id: 2, name: '林同学', progress: 45, timeSpent: 30, isCompleted: false },
  { id: 3, name: '张同学', progress: 100, timeSpent: 89, isCompleted: true },
  { id: 4, name: '王同学', progress: 10, timeSpent: 12, isCompleted: false },
  { id: 5, name: '李同学', progress: 80, timeSpent: 55, isCompleted: false },
  { id: 6, name: '赵同学', progress: 100, timeSpent: 180, isCompleted: true },
  { id: 7, name: '孙同学', progress: 100, timeSpent: 90, isCompleted: true },
  { id: 8, name: '周同学', progress: 50, timeSpent: 40, isCompleted: false },
  { id: 9, name: '吴同学', progress: 100, timeSpent: 200, isCompleted: true },
  { id: 10, name: '郑同学', progress: 20, timeSpent: 15, isCompleted: false },
])

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

// ===== 搜索与分页逻辑 =====
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(3) 

const filteredStudents = computed(() => {
  if (!searchQuery.value.trim()) return studentList.value
  const lowerSearch = searchQuery.value.trim().toLowerCase()
  return studentList.value.filter(s => s.name.toLowerCase().includes(lowerSearch))
})

const totalPages = computed(() => Math.ceil(filteredStudents.value.length / pageSize.value))

const paginatedStudents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStudents.value.slice(start, end)
})

watch(searchQuery, () => { currentPage.value = 1 })

const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++ }
// =========================

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

.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.2); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.4); }
</style>