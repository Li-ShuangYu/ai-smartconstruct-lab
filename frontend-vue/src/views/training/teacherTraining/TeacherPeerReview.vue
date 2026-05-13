<template>
  <div style="height: 100vh;" class="p-6 bg-slate-50">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 font-sans overflow-hidden">
      
      <div class="flex justify-between items-center mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase italic">Node: PEER_REVIEW_MONITOR</div>
          <h1 class="text-2xl font-bold text-gray-800">学生互评实时监控</h1>
        </div>
        
        <div class="flex items-center gap-6 bg-white/60 px-6 py-3 rounded-2xl border border-gray-100 shadow-sm">
          <div class="flex flex-col items-center px-4 border-r border-gray-200">
            <span class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1">已完成评价</span>
            <span class="text-2xl font-black text-green-500">{{ completedCount }}</span>
          </div>
          <div class="flex flex-col items-center px-4 border-r border-gray-200">
            <span class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1">评价进行中</span>
            <span class="text-2xl font-black text-indigo-500">{{ pendingCount }}</span>
          </div>
          <div class="flex flex-col items-center px-4">
            <span class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1">全班总人数</span>
            <span class="text-2xl font-black text-gray-700">{{ totalCount }}</span>
          </div>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1.5] flex flex-col bg-white/60 border border-gray-100 rounded-2xl shadow-sm overflow-hidden">
          <div class="bg-gray-50/80 px-6 py-4 border-b border-gray-100 shrink-0">
            <h3 class="font-bold text-gray-800 flex items-center gap-2">
              <svg class="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.488 9H15V3.512A9.025 9.025 0 0120.488 9z" /></svg>
              全班各项产出物评价多维均分 (满分 10.0)
            </h3>
          </div>
          
          <div class="flex-1 overflow-y-auto p-6 custom-scrollbar">
            <div class="grid grid-cols-1 xl:grid-cols-2 2xl:grid-cols-3 gap-8">
              
              <div v-for="chart in classRadarCharts" :key="chart.id" class="bg-white border border-gray-100 rounded-2xl p-6 shadow-sm flex flex-col items-center transition-all hover:shadow-md">
                <h4 class="text-sm font-bold text-indigo-900 mb-8 bg-indigo-50 px-4 py-1.5 rounded-lg border border-indigo-100">{{ chart.title }}</h4>
                
                <div class="relative w-44 h-44 mb-4">
                  <span class="absolute -top-5 left-1/2 -translate-x-1/2 text-xs font-bold text-gray-600 whitespace-nowrap">逻辑正确 ({{ chart.scores.logic.toFixed(1) }})</span>
                  <span class="absolute -bottom-3 -right-8 text-xs font-bold text-gray-600 whitespace-nowrap">代码规范 ({{ chart.scores.standard.toFixed(1) }})</span>
                  <span class="absolute -bottom-3 -left-8 text-xs font-bold text-gray-600 whitespace-nowrap">方案完整 ({{ chart.scores.completeness.toFixed(1) }})</span>
                  
                  <svg viewBox="0 0 100 100" class="w-full h-full overflow-visible">
                    <polygon points="50,10 84.64,70 15.36,70" fill="none" stroke="#f3f4f6" stroke-width="1"/>
                    <polygon points="50,26 70.78,62 29.22,62" fill="none" stroke="#f3f4f6" stroke-width="1"/>
                    <polygon points="50,42 56.92,54 43.08,54" fill="none" stroke="#f3f4f6" stroke-width="1"/>
                    <line x1="50" y1="50" x2="50" y2="10" stroke="#e5e7eb" stroke-width="1"/>
                    <line x1="50" y1="50" x2="84.64" y2="70" stroke="#e5e7eb" stroke-width="1"/>
                    <line x1="50" y1="50" x2="15.36" y2="70" stroke="#e5e7eb" stroke-width="1"/>
                    <polygon :points="getRadarPoints(chart.scores)" fill="rgba(99, 102, 241, 0.2)" stroke="#6366f1" stroke-width="2" />
                    <circle :cx="getPoint(chart.scores.logic, 'logic').x" :cy="getPoint(chart.scores.logic, 'logic').y" r="2.5" fill="#4f46e5" />
                    <circle :cx="getPoint(chart.scores.standard, 'standard').x" :cy="getPoint(chart.scores.standard, 'standard').y" r="2.5" fill="#4f46e5" />
                    <circle :cx="getPoint(chart.scores.completeness, 'completeness').x" :cy="getPoint(chart.scores.completeness, 'completeness').y" r="2.5" fill="#4f46e5" />
                  </svg>
                </div>
              </div>

            </div>
          </div>
        </div>

        <div class="flex-[1] flex flex-col bg-white/60 border border-gray-100 rounded-2xl shadow-sm overflow-hidden min-w-[340px]">
          <div class="bg-gray-50/80 px-6 py-4 border-b border-gray-100 flex justify-between items-center shrink-0">
            <h3 class="font-bold text-gray-800 flex items-center gap-2">
              <svg class="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" /></svg>
              班级互评均分榜单
            </h3>
          </div>

          <div class="grid grid-cols-12 gap-4 px-6 py-3 border-b border-gray-100 bg-white text-[10px] font-bold text-gray-400 uppercase tracking-wider shrink-0">
            <div class="col-span-2 text-center">排名</div>
            <div class="col-span-6">学生信息</div>
            <div class="col-span-4 text-right">综合均分</div>
          </div>

          <div class="flex-1 overflow-y-auto p-4 custom-scrollbar space-y-2">
            <div 
              v-for="(student, index) in rankedStudents" 
              :key="student.id"
              @click="openDetails(student)"
              class="grid grid-cols-12 gap-4 px-2 py-3 items-center bg-white border rounded-xl transition-all cursor-pointer group"
              :class="index < 3 ? 'border-indigo-100 shadow-sm hover:border-indigo-300' : 'border-gray-50 hover:border-indigo-200 hover:shadow-sm'"
            >
              <div class="col-span-2 flex justify-center">
                <div v-if="index === 0" class="w-6 h-6 rounded-full bg-yellow-100 text-yellow-600 font-black flex items-center justify-center text-xs">1</div>
                <div v-else-if="index === 1" class="w-6 h-6 rounded-full bg-gray-100 text-gray-500 font-black flex items-center justify-center text-xs">2</div>
                <div v-else-if="index === 2" class="w-6 h-6 rounded-full bg-orange-100 text-orange-600 font-black flex items-center justify-center text-xs">3</div>
                <div v-else class="text-sm font-bold text-gray-400">{{ index + 1 }}</div>
              </div>

              <div class="col-span-6 flex items-center gap-3">
                <div class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold text-white shadow-sm"
                     :class="index < 3 ? 'bg-gradient-to-br from-indigo-400 to-purple-500' : 'bg-gray-300'">
                  {{ student.name.charAt(0) }}
                </div>
                <span class="text-sm font-bold text-gray-700 group-hover:text-indigo-600 transition-colors">{{ student.name }}</span>
              </div>

              <div class="col-span-4 text-right pr-2">
                <span class="font-mono text-base font-black" :class="index < 3 ? 'text-indigo-600' : 'text-gray-600'">
                  {{ student.score.toFixed(1) }}
                </span>
                <span class="text-[10px] text-gray-400 ml-0.5">/ 10.0</span>
              </div>
            </div>
          </div>
        </div>

      </div>

      <div v-if="selectedStudent" class="absolute inset-0 z-50 bg-gray-900/40 backdrop-blur-sm rounded-[1.5rem] flex items-center justify-center animate-fade-in p-6">
        <div class="bg-white rounded-2xl w-full max-w-3xl shadow-2xl flex flex-col overflow-hidden border border-gray-200">
          
          <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/80">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full flex items-center justify-center text-white font-bold text-lg bg-gradient-to-br from-indigo-400 to-purple-500 shadow-sm">
                {{ selectedStudent.name.charAt(0) }}
              </div>
              <div>
                <h3 class="font-bold text-gray-800 text-lg">{{ selectedStudent.name }} 的各项产出物得分明细</h3>
                <p class="text-xs text-gray-500 mt-0.5">最终综合均分：<strong class="text-indigo-600 text-sm">{{ selectedStudent.score.toFixed(1) }}</strong></p>
              </div>
            </div>
            <button @click="selectedStudent = null" class="text-gray-400 hover:text-red-500 hover:bg-red-50 p-2 rounded-lg transition-colors">
              <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
            </button>
          </div>
          
          <div class="p-6 bg-slate-50/50">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              
              <div v-for="item in selectedStudent.details" :key="item.title" class="bg-white border border-gray-100 rounded-xl p-5 shadow-sm hover:border-indigo-200 transition-colors">
                <h4 class="font-bold text-sm text-indigo-900 mb-4 pb-2 border-b border-gray-50">{{ item.title }}</h4>
                <div class="space-y-3">
                  <div class="flex justify-between items-center">
                    <span class="text-xs text-gray-500">逻辑正确性</span>
                    <span class="font-mono text-sm font-bold" :class="item.scores.logic >= 8 ? 'text-green-600' : 'text-indigo-600'">{{ item.scores.logic.toFixed(1) }}</span>
                  </div>
                  <div class="flex justify-between items-center">
                    <span class="text-xs text-gray-500">代码规范度</span>
                    <span class="font-mono text-sm font-bold" :class="item.scores.standard >= 8 ? 'text-green-600' : 'text-indigo-600'">{{ item.scores.standard.toFixed(1) }}</span>
                  </div>
                  <div class="flex justify-between items-center">
                    <span class="text-xs text-gray-500">方案完整性</span>
                    <span class="font-mono text-sm font-bold" :class="item.scores.completeness >= 8 ? 'text-green-600' : 'text-indigo-600'">{{ item.scores.completeness.toFixed(1) }}</span>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// --- 全班进度数据 ---
const completedCount = ref(24)
const pendingCount = ref(8)
const totalCount = ref(32)

// --- 3个产出物雷达图数据 ---
const classRadarCharts = ref([
  { id: 1, title: '产出物一：需求分析方案', scores: { logic: 8.5, standard: 7.8, completeness: 9.0 } },
  { id: 2, title: '产出物二：系统架构设计', scores: { logic: 7.5, standard: 8.2, completeness: 8.5 } },
  { id: 3, title: '产出物三：核心源代码', scores: { logic: 9.2, standard: 8.8, completeness: 9.5 } }
])

// --- 雷达图 SVG 计算逻辑 (满分 10.0，半径 40，中心点 50) ---
const getPoint = (score, key) => {
  const r = 40
  const c = 50
  const val = (score / 10.0) * r // 转换为10分制比例
  
  let angle = 0
  if (key === 'logic') angle = -Math.PI / 2           // 顶部 (-90度)
  if (key === 'standard') angle = Math.PI / 6         // 右下 (30度)
  if (key === 'completeness') angle = 5 * Math.PI / 6 // 左下 (150度)
  
  return {
    x: c + val * Math.cos(angle),
    y: c + val * Math.sin(angle)
  }
}

const getRadarPoints = (scores) => {
  const p1 = getPoint(scores.logic, 'logic')
  const p2 = getPoint(scores.standard, 'standard')
  const p3 = getPoint(scores.completeness, 'completeness')
  return `${p1.x},${p1.y} ${p2.x},${p2.y} ${p3.x},${p3.y}`
}

// --- 学生列表与详情数据 (10.0 分制) ---
const studentsData = [
  { 
    id: 1, name: '陈静', score: 9.8,
    details: [
      { title: '需求分析方案', scores: { logic: 9.8, standard: 9.5, completeness: 10.0 } },
      { title: '系统架构设计', scores: { logic: 9.5, standard: 9.8, completeness: 9.8 } },
      { title: '核心源代码', scores: { logic: 10.0, standard: 9.9, completeness: 9.9 } }
    ]
  },
  { 
    id: 2, name: '张伟', score: 9.5,
    details: [
      { title: '需求分析方案', scores: { logic: 9.2, standard: 9.5, completeness: 9.0 } },
      { title: '系统架构设计', scores: { logic: 9.5, standard: 9.8, completeness: 9.5 } },
      { title: '核心源代码', scores: { logic: 9.8, standard: 9.5, completeness: 9.6 } }
    ]
  },
  { 
    id: 3, name: '孙颖', score: 9.2,
    details: [
      { title: '需求分析方案', scores: { logic: 9.0, standard: 9.2, completeness: 9.5 } },
      { title: '系统架构设计', scores: { logic: 9.5, standard: 9.0, completeness: 9.2 } },
      { title: '核心源代码', scores: { logic: 9.2, standard: 9.0, completeness: 9.5 } }
    ]
  },
  { id: 4, name: '赵云', score: 8.8, details: [{ title: '需求分析方案', scores: { logic: 8.5, standard: 9.0, completeness: 8.5 } }, { title: '系统架构设计', scores: { logic: 8.8, standard: 8.5, completeness: 8.9 } }, { title: '核心源代码', scores: { logic: 9.0, standard: 8.8, completeness: 9.0 } }] },
  { id: 5, name: '吴敏', score: 8.5, details: [{ title: '需求分析方案', scores: { logic: 8.0, standard: 8.5, completeness: 8.8 } }, { title: '系统架构设计', scores: { logic: 8.5, standard: 8.6, completeness: 8.5 } }, { title: '核心源代码', scores: { logic: 8.8, standard: 8.5, completeness: 8.5 } }] },
  { id: 6, name: '李明', score: 8.2, details: [{ title: '需求分析方案', scores: { logic: 8.0, standard: 8.2, completeness: 8.0 } }, { title: '系统架构设计', scores: { logic: 8.2, standard: 8.0, completeness: 8.5 } }, { title: '核心源代码', scores: { logic: 8.5, standard: 8.2, completeness: 8.0 } }] },
  { id: 7, name: '林峰', score: 8.0, details: [{ title: '需求分析方案', scores: { logic: 8.0, standard: 8.0, completeness: 7.8 } }, { title: '系统架构设计', scores: { logic: 7.8, standard: 8.2, completeness: 8.0 } }, { title: '核心源代码', scores: { logic: 8.2, standard: 8.0, completeness: 8.0 } }] },
  { id: 8, name: '郑强', score: 7.8, details: [{ title: '需求分析方案', scores: { logic: 7.5, standard: 7.8, completeness: 8.0 } }, { title: '系统架构设计', scores: { logic: 7.8, standard: 7.5, completeness: 7.8 } }, { title: '核心源代码', scores: { logic: 8.0, standard: 7.8, completeness: 7.5 } }] },
  { id: 9, name: '王磊', score: 7.5, details: [{ title: '需求分析方案', scores: { logic: 7.0, standard: 7.5, completeness: 7.2 } }, { title: '系统架构设计', scores: { logic: 7.5, standard: 7.8, completeness: 7.5 } }, { title: '核心源代码', scores: { logic: 7.8, standard: 7.2, completeness: 7.5 } }] },
  { id: 10, name: '周杰', score: 7.0, details: [{ title: '需求分析方案', scores: { logic: 6.8, standard: 7.0, completeness: 7.2 } }, { title: '系统架构设计', scores: { logic: 7.0, standard: 6.8, completeness: 7.0 } }, { title: '核心源代码', scores: { logic: 7.2, standard: 7.0, completeness: 6.8 } }] }
]

const rankedStudents = computed(() => {
  return [...studentsData].sort((a, b) => b.score - a.score)
})

// --- 浮窗控制逻辑 ---
const selectedStudent = ref(null)
const openDetails = (student) => {
  selectedStudent.value = student
}
</script>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 1.5rem;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.07);
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.98) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
</style>