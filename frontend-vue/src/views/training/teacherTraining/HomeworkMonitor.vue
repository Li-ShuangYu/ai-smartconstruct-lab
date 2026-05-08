<template>
  <div class="page-wrapper flex items-center justify-center w-full h-full min-h-[calc(100vh-100px)]">
    
    <div class="glass-card w-full max-w-[1400px] p-6 md:p-8 flex flex-col z-10 h-[800px]">
      
      <div class="flex justify-between items-end mb-6 pb-4 border-b border-gray-200/50">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: HOMEWORK Monitor</div>
          <h1 class="text-3xl font-bold text-gray-800 flex items-center gap-3">
             <svg class="text-indigo-500" style="width: 28px; height: 28px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" /></svg>
             在线作业实时监考
          </h1>
        </div>
        
        <div class="flex items-center gap-4 bg-red-50/80 px-5 py-2.5 rounded-xl border border-red-100 shadow-sm">
           <span class="text-sm font-bold text-red-700">全班统一剩余倒计时：</span>
           <span class="font-mono text-2xl font-black text-red-600">{{ formattedGlobalTime }}</span>
        </div>
      </div>

      <div class="flex-1 flex flex-col lg:flex-row gap-6 min-h-0">
        
        <div class="flex-[1.2] flex flex-col gap-6">
          
          <div class="bg-white/50 border border-gray-200/60 rounded-2xl p-6 shadow-sm flex items-center justify-between h-48">
            <div class="flex flex-col h-full justify-center">
              <h3 class="font-bold text-gray-700 text-sm mb-1">实时交卷进度</h3>
              <p class="text-xs text-gray-500 mb-4">全班总人数：{{ totalStudents }}人</p>
              <div class="text-3xl font-bold text-indigo-600">{{ submittedCount }} <span class="text-lg text-gray-400">已交</span></div>
            </div>
            
            <div class="relative w-32 h-32 flex items-center justify-center">
              <svg class="w-full h-full transform -rotate-90" style="width: 128px; height: 128px; flex-shrink: 0;" viewBox="0 0 100 100">
                <circle cx="50" cy="50" r="40" stroke="#e5e7eb" stroke-width="8" fill="none" />
                <circle cx="50" cy="50" r="40" stroke="#6366f1" stroke-width="8" fill="none" stroke-linecap="round"
                        :stroke-dasharray="251.2" :stroke-dashoffset="251.2 - (submitRate / 100) * 251.2" 
                        class="transition-all duration-1000 ease-out" />
              </svg>
              <div class="absolute text-xl font-black text-indigo-700">{{ submitRate }}%</div>
            </div>
          </div>

          <div class="flex-1 bg-white/50 border border-gray-200/60 rounded-2xl p-6 shadow-sm overflow-hidden flex flex-col">
            <h3 class="font-bold text-gray-700 text-sm mb-4">各题错误率实时统计 (AI预批改)</h3>
            <div class="flex-1 overflow-y-auto pr-2 custom-scrollbar space-y-4">
              <div v-for="q in questionStats" :key="q.id">
                <div class="flex justify-between text-xs mb-1">
                  <span class="font-bold text-gray-600 truncate max-w-[70%]" :title="q.title">Q{{ q.id }}: {{ q.title }}</span>
                  <span class="font-bold" :class="q.errorRate > 50 ? 'text-red-500' : 'text-gray-500'">{{ q.errorRate }}% 错误</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2">
                  <div class="h-2 rounded-full transition-all duration-500" 
                       :class="q.errorRate > 50 ? 'bg-red-400' : 'bg-amber-400'"
                       :style="{ width: q.errorRate + '%' }"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex-[1.8] flex flex-col bg-white/40 border border-gray-100 rounded-2xl shadow-sm overflow-hidden">
          
          <div class="bg-indigo-50/50 px-4 py-3 border-b border-indigo-100/50 flex items-center justify-between">
            <span class="font-bold text-indigo-900 text-sm flex items-center gap-2">
               <svg style="width: 16px; height: 16px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" /></svg>
               全班作答明细
            </span>
            <div class="flex gap-2">
              <button @click="extendTime" class="px-3 py-1.5 bg-white border border-gray-300 text-gray-700 rounded text-xs font-bold hover:bg-gray-50 transition-colors shadow-sm">
                延长 5 分钟
              </button>
              <button @click="forceCollect" class="px-3 py-1.5 bg-red-50 border border-red-200 text-red-600 rounded text-xs font-bold hover:bg-red-100 transition-colors shadow-sm">
                强制立即收卷
              </button>
            </div>
          </div>

          <div class="grid grid-cols-12 gap-4 px-6 py-3 border-b border-gray-200/50 bg-gray-50/50 text-xs font-bold text-gray-500 uppercase tracking-wider">
            <div class="col-span-3">学生姓名</div>
            <div class="col-span-3">状态</div>
            <div class="col-span-3">AI 预估得分</div>
            <div class="col-span-3 text-right">操作</div>
          </div>
          
          <div class="flex-1 overflow-y-auto p-2 space-y-1 custom-scrollbar">
            <div v-for="student in studentList" :key="student.id" class="grid grid-cols-12 gap-4 px-4 py-3 items-center bg-white border border-gray-100 rounded-xl shadow-sm hover:shadow-md transition-shadow">
              
              <div class="col-span-3 flex items-center gap-3">
                <div class="w-8 h-8 rounded-full bg-indigo-50 text-indigo-700 flex items-center justify-center text-xs font-bold border border-indigo-100">
                  {{ student.name.charAt(0) }}
                </div>
                <span class="text-sm font-bold text-gray-700">{{ student.name }}</span>
              </div>
              
              <div class="col-span-3">
                <span class="text-xs font-bold px-2 py-1 rounded border flex items-center gap-1 w-fit"
                      :class="student.isSubmitted ? 'bg-green-50 text-green-600 border-green-200' : 'bg-amber-50 text-amber-600 border-amber-200'">
                  <span v-if="!student.isSubmitted" class="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse"></span>
                  <svg v-else style="width: 10px; height: 10px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                  {{ student.isSubmitted ? '已交卷' : '答题中...' }}
                </span>
              </div>

              <div class="col-span-3 font-mono font-bold">
                <span v-if="student.isSubmitted" :class="student.score >= 80 ? 'text-green-600' : 'text-amber-600'">
                  {{ student.score }} / 100
                </span>
                <span v-else class="text-gray-400 text-xs">作答中暂无</span>
              </div>

              <div class="col-span-3 flex justify-end">
                <button v-if="student.isSubmitted" class="text-xs text-indigo-500 hover:text-indigo-700 font-bold underline">
                  查看答卷
                </button>
              </div>
            </div>
          </div>

        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

// 全局倒计时
const globalTimeLeft = ref(15 * 60 + 30) // 模拟还剩 15分30秒
let timer = null

const formattedGlobalTime = computed(() => {
  const m = Math.floor(globalTimeLeft.value / 60).toString().padStart(2, '0')
  const s = (globalTimeLeft.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

onMounted(() => { timer = setInterval(() => { if (globalTimeLeft.value > 0) globalTimeLeft.value-- }, 1000) })
onUnmounted(() => { if (timer) clearInterval(timer) })

const extendTime = () => { globalTimeLeft.value += 5 * 60; alert("已为全班延长 5 分钟答题时间。") }
const forceCollect = () => { 
  if (confirm("确认强制立即收卷吗？所有未交卷学生的进度将被锁定。")) {
    globalTimeLeft.value = 0
    studentList.value.forEach(s => {
      if (!s.isSubmitted) { s.isSubmitted = true; s.score = Math.floor(Math.random() * 40) + 40; }
    })
  }
}

// 模拟学情数据
const studentList = ref([
  { id: 1, name: '陈同学', isSubmitted: true, score: 90 },
  { id: 2, name: '林同学', isSubmitted: true, score: 85 },
  { id: 3, name: '张同学', isSubmitted: false, score: 0 },
  { id: 4, name: '王同学', isSubmitted: true, score: 60 },
  { id: 5, name: '李同学', isSubmitted: false, score: 0 },
  { id: 6, name: '赵同学', isSubmitted: false, score: 0 },
])

const totalStudents = computed(() => studentList.value.length)
const submittedCount = computed(() => studentList.value.filter(s => s.isSubmitted).length)
const submitRate = computed(() => Math.round((submittedCount.value / totalStudents.value) * 100))

// 模拟错题统计数据
const questionStats = ref([
  { id: 1, title: 'SM4 密码算法的分组长度是多少？', errorRate: 15 },
  { id: 2, title: '轮函数 F 包含的非线性变换 τ 是由几个 S 盒构成的？', errorRate: 65 }, // 高错题
  { id: 3, title: '关于 SM4 算法密钥扩展的说法正确的是？', errorRate: 45 },
  { id: 4, title: 'SM4 属于非对称加密算法。', errorRate: 5 },
  { id: 5, title: '简述非线性变换和线性变换的作用。', errorRate: 30 },
])
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.2); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.4); }
</style>