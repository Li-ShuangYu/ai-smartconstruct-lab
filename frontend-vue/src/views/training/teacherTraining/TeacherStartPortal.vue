<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 flex flex-col md:flex-row gap-8 z-10">
      
      <div class="flex-1 flex flex-col h-full pr-4">
        <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: START</div>
        <h1 class="text-3xl font-bold mb-4 text-gradient-primary">开始实训</h1>
        <p class="text-gray-600 dark:text-gray-300 mb-4 text-sm leading-relaxed">
          {{ nodeConfig.desc || '实训流程的说明' }}
        </p>

        <div class="w-full flex-1 bg-white/40 dark:bg-[#1e293b]/50 rounded-xl border border-dashed border-gray-300 dark:border-gray-700 flex flex-col items-center justify-center relative">
          <span class="text-sm text-gray-400 font-medium tracking-widest">实训全局引导图 / 架构拓扑预览</span>
        </div>
      </div>

      <div class="w-full md:w-80 flex flex-col border-t md:border-t-0 md:border-l border-gray-200 dark:border-gray-700/50 pt-6 md:pt-0 md:pl-6 h-full">
        
        <div class="flex items-center justify-between mb-4 shrink-0">
          <h3 class="text-sm font-bold text-gray-800 dark:text-gray-100 flex-single-line">学生就位状态</h3>
          <div class="px-2 py-0.5 rounded text-xs font-medium bg-indigo-50 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 border border-indigo-100 dark:border-indigo-800/50">
            {{ readyCount }} / {{ studentList.length }}
          </div>
        </div>

        <div class="mb-3 shrink-0 relative">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索学生姓名..."
            class="w-full pl-8 pr-3 py-2 bg-white/60 dark:bg-[#1e293b]/50 border border-gray-200 dark:border-gray-700 rounded-lg text-sm text-gray-700 dark:text-gray-200 focus:outline-none focus:border-indigo-400 focus:ring-1 focus:ring-indigo-400 transition-colors shadow-sm"
          >
          <svg class="absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-400" style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>

        <div class="flex-1 overflow-y-auto pr-1 space-y-2 mb-2 custom-scrollbar">
          <template v-if="paginatedStudents.length > 0">
            <div 
              v-for="student in paginatedStudents" 
              :key="student.id" 
              class="flex items-center justify-between p-2.5 bg-white/60 dark:bg-slate-800/60 rounded-lg border border-transparent hover:border-gray-200 dark:hover:border-gray-700 transition-colors"
            >
              <span class="text-sm font-medium text-gray-700 dark:text-gray-200">{{ student.name }}</span>
              <div class="flex items-center gap-1.5">
                <span class="relative flex h-2 w-2">
                  <span v-if="student.isReady" class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                  <span class="relative inline-flex rounded-full h-2 w-2" :class="student.isReady ? 'bg-green-500' : 'bg-gray-400 dark:bg-gray-600'"></span>
                </span>
                <span class="text-xs" :class="student.isReady ? 'text-green-600 dark:text-green-400' : 'text-gray-500'">
                  {{ student.isReady ? '已就位' : '未就位' }}
                </span>
              </div>
            </div>
          </template>
          <div v-else class="h-full flex flex-col items-center justify-center text-gray-400 space-y-2 pb-4">
             <svg style="width: 24px; height: 24px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
             <span class="text-xs">暂无匹配学生</span>
          </div>
        </div>

        <div class="flex items-center justify-between mb-4 px-1 shrink-0">
          <button 
            @click="prevPage" 
            :disabled="currentPage === 1"
            class="text-xs text-gray-500 hover:text-indigo-600 disabled:opacity-40 disabled:hover:text-gray-500 disabled:cursor-not-allowed transition-colors"
          >
            上一页
          </button>
          <span class="text-[10px] font-mono font-bold text-gray-400">{{ currentPage }} / {{ totalPages || 1 }}</span>
          <button 
            @click="nextPage" 
            :disabled="currentPage >= totalPages || totalPages === 0"
            class="text-xs text-gray-500 hover:text-indigo-600 disabled:opacity-40 disabled:hover:text-gray-500 disabled:cursor-not-allowed transition-colors"
          >
            下一页
          </button>
        </div>

        <div class="space-y-3 pt-4 border-t border-gray-100 dark:border-gray-800/50 shrink-0">
          <button 
            class="w-full py-2.5 border border-indigo-400/50 text-indigo-600 dark:text-indigo-400 rounded-lg hover:bg-indigo-50 dark:hover:bg-indigo-900/20 transition-colors text-sm font-bold flex items-center justify-center gap-2"
            @click="handleUrge"
          >
            <svg style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /></svg>
            一键催办 ({{ studentList.length - readyCount }}人)
          </button>
          <button class="hero-send-btn w-full justify-center text-sm py-2.5 rounded-lg" @click="handleForceStart">
            <svg style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
            强制开启全局流程
          </button>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, watch } from 'vue'

const nodeConfig = reactive({
  desc: '实训流程的说明'
})

// 模拟学生数据
const studentList = ref([
  { id: 1, name: '陈同学', isReady: true },
  { id: 2, name: '林同学', isReady: true },
  { id: 3, name: '张同学', isReady: false },
  { id: 4, name: '王同学', isReady: true },
  { id: 5, name: '李同学', isReady: false },
  { id: 6, name: '赵同学', isReady: false },
  { id: 7, name: '孙同学', isReady: true },
  { id: 8, name: '吴同学', isReady: true },
  { id: 9, name: '郑同学', isReady: true },
  { id: 10, name: '钱同学', isReady: true },
])

const readyCount = computed(() => studentList.value.filter(s => s.isReady).length)

// ===== 搜索与分页逻辑 =====
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(6) // 设定每页展示 6 条数据

// 1. 过滤学生列表
const filteredStudents = computed(() => {
  if (!searchQuery.value.trim()) {
    return studentList.value
  }
  const lowerSearch = searchQuery.value.trim().toLowerCase()
  return studentList.value.filter(student => 
    student.name.toLowerCase().includes(lowerSearch)
  )
})

// 2. 计算总页数
const totalPages = computed(() => {
  return Math.ceil(filteredStudents.value.length / pageSize.value)
})

// 3. 计算当前页要展示的学生
const paginatedStudents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStudents.value.slice(start, end)
})

// 监听搜索词变化，如果搜索则自动重置回第一页
watch(searchQuery, () => {
  currentPage.value = 1
})

const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++
}
// =========================

const handleUrge = () => {
  const unready = studentList.value.length - readyCount.value
  if (unready === 0) {
    alert('所有学生已就位，无需催办。')
    return
  }
  alert(`已向 ${unready} 名未就位学生发送催办提醒！`)
}

const handleForceStart = () => {
  if (confirm('确认强制开启实训吗？未就位学生将被自动拉入流程。')) {
    alert('执行: 强制开启全局流程')
  }
}
</script>

<style scoped>
/* 教师端列表自定义细滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(99, 102, 241, 0.2);
  border-radius: 4px;
}
.custom-scrollbar:hover::-webkit-scrollbar-thumb {
  background: rgba(99, 102, 241, 0.4);
}
</style>