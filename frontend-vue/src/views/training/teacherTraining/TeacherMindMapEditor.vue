<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative">
      
      <div class="flex justify-between items-end mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase flex items-center gap-2">
            <svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
            Node: MINDMAP_DRAW Monitor
          </div>
          <h1 class="text-3xl font-bold text-gray-800 flex items-center gap-3">
             <svg class="text-indigo-500" style="width: 28px; height: 28px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z" /></svg>
             全班画布绘制微缩监控墙
          </h1>
        </div>
        
        <div class="flex items-center gap-4 bg-white/60 px-5 py-2.5 rounded-xl border border-gray-100 shadow-sm">
           <div class="flex flex-col items-center">
             <span class="text-xs text-gray-500">已达标并提交</span>
             <span class="text-xl font-bold text-green-600">{{ submittedCount }} / {{ studentList.length }}</span>
           </div>
           <div class="w-px h-8 bg-gray-200"></div>
           <div class="flex flex-col items-center">
             <span class="text-xs text-gray-500">全班平均节点数</span>
             <span class="text-xl font-bold text-indigo-600">{{ avgNodes }}</span>
           </div>
        </div>
      </div>

      <div class="mb-4 flex items-center shrink-0">
        <div class="relative w-72">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索学生姓名..."
            class="w-full pl-9 pr-4 py-2.5 bg-white/60 border border-gray-200 rounded-xl text-sm text-gray-700 focus:outline-none focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 transition-colors shadow-sm"
          >
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
      </div>

      <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 pb-4">
        <template v-if="paginatedStudents.length > 0">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            <div v-for="student in paginatedStudents" :key="student.id" 
                 class="bg-white border rounded-2xl p-0 flex flex-col shadow-sm transition-all duration-300 hover:shadow-lg relative overflow-hidden group cursor-pointer"
                 :class="student.isSubmitted ? 'border-green-300' : 'border-gray-200 hover:border-indigo-300'"
                 @click="openSpectator(student)">
              
              <div class="px-4 py-2 border-b flex justify-between items-center z-10 bg-white/90 backdrop-blur-sm"
                   :class="student.isSubmitted ? 'border-green-100 bg-green-50/50' : 'border-gray-100'">
                <div class="flex items-center gap-2">
                  <div class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold text-white shadow-sm"
                       :class="student.isSubmitted ? 'bg-green-500' : 'bg-gray-400'">
                    {{ student.name.charAt(0) }}
                  </div>
                  <span class="font-bold text-sm text-gray-700">{{ student.name }}</span>
                </div>
                
                <div class="flex items-center gap-2">
                   <span class="text-xs font-mono font-bold" :class="student.nodeCount >= 6 ? 'text-green-600' : 'text-amber-500'">
                     {{ student.nodeCount }} 节点
                   </span>
                   <svg v-if="student.isSubmitted" class="text-green-500" style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                   <span v-else class="relative flex h-2 w-2">
                     <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75"></span>
                     <span class="relative inline-flex rounded-full h-2 w-2 bg-indigo-500"></span>
                   </span>
                </div>
              </div>

              <div class="h-40 bg-slate-50 relative dot-pattern overflow-hidden opacity-80 group-hover:opacity-100 transition-opacity">
                <div class="absolute inset-0 bg-indigo-900/10 opacity-0 group-hover:opacity-100 transition-opacity z-20 flex items-center justify-center backdrop-blur-[1px]">
                   <span class="bg-indigo-600 text-white px-3 py-1.5 rounded-lg text-xs font-bold shadow-md flex items-center gap-1">
                     <svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM10 7v3m0 0v3m0-3h3m-3 0H7" /></svg>
                     放大监控
                   </span>
                </div>
                
                <div class="absolute inset-0 flex items-center justify-center transform scale-50">
                   <div class="w-24 h-10 bg-indigo-500 rounded-lg absolute z-10" style="top: 50%; left: 50%; transform: translate(-50%, -50%);"></div>
                   
                   <div class="w-px h-16 bg-gray-400 absolute" style="top: 25%; left: 50%;"></div>
                   <div class="w-16 h-8 bg-gray-400 rounded absolute z-10" style="top: 20%; left: 50%; transform: translate(-50%, -50%);"></div>
                   
                   <div class="w-16 h-px bg-gray-400 absolute" style="top: 50%; left: 25%;"></div>
                   <div class="w-16 h-8 bg-gray-400 rounded absolute z-10" style="top: 50%; left: 20%; transform: translate(-50%, -50%);"></div>
                   
                   <div v-if="student.nodeCount > 4" class="w-16 h-px bg-gray-400 absolute" style="top: 50%; left: 65%;"></div>
                   <div v-if="student.nodeCount > 4" class="w-16 h-8 bg-indigo-300 rounded absolute z-10" style="top: 50%; left: 80%; transform: translate(-50%, -50%);"></div>

                   <div v-if="student.nodeCount > 6" class="w-px h-16 bg-gray-400 absolute" style="top: 65%; left: 50%;"></div>
                   <div v-if="student.nodeCount > 6" class="w-16 h-8 bg-purple-300 rounded absolute z-10" style="top: 80%; left: 50%; transform: translate(-50%, -50%);"></div>
                </div>
              </div>
              
            </div>
          </div>
        </template>
        <div v-else class="h-full flex flex-col items-center justify-center text-gray-400 space-y-3 pb-10">
           <svg style="width: 48px; height: 48px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
           <span class="text-sm font-medium">无匹配学生画布</span>
        </div>
      </div>

      <div class="mt-4 pt-4 border-t border-gray-200/60 flex items-center justify-between shrink-0">
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

      <div v-if="spectatingStudent" class="absolute inset-0 z-50 bg-gray-900/60 backdrop-blur-md rounded-[20px] flex items-center justify-center animate-fade-in p-8">
        <div class="bg-white rounded-2xl w-full h-full shadow-2xl flex flex-col overflow-hidden border border-gray-200">
          <div class="h-14 bg-white border-b border-gray-200 flex items-center justify-between px-6 shrink-0">
            <div class="flex items-center gap-3">
              <span class="w-3 h-3 rounded-full bg-red-500 animate-pulse"></span>
              <span class="font-bold text-gray-800 text-lg">正在监控学生画布：{{ spectatingStudent.name }}</span>
              <span class="px-2 py-0.5 bg-gray-100 text-gray-500 text-xs rounded border ml-2">节点数: {{ spectatingStudent.nodeCount }}</span>
            </div>
            <button @click="closeSpectator" class="text-gray-400 hover:text-red-500 hover:bg-red-50 p-1.5 rounded-lg transition-colors">
              <svg style="width: 24px; height: 24px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
            </button>
          </div>
          
          <div class="flex-1 bg-slate-50 relative dot-pattern flex items-center justify-center">
             <div class="absolute inset-0 flex items-center justify-center text-gray-300 font-bold text-4xl opacity-50 pointer-events-none tracking-widest z-0">
               LIVE CANVAS
             </div>
             <div class="w-[600px] h-[400px] border-2 border-indigo-200 rounded-2xl bg-white/80 shadow-lg relative z-10 flex items-center justify-center">
               <span class="text-indigo-400 font-bold text-lg border-2 border-dashed border-indigo-300 px-6 py-4 rounded-xl">
                 [ {{ spectatingStudent.name }} 的 RSA 算法导图实时渲染区 ]
               </span>
             </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const studentList = ref([
  { id: 1, name: '陈同学', nodeCount: 8, isSubmitted: true },
  { id: 2, name: '林同学', nodeCount: 5, isSubmitted: false },
  { id: 3, name: '张同学', nodeCount: 12, isSubmitted: true },
  { id: 4, name: '王同学', nodeCount: 3, isSubmitted: false }, 
  { id: 5, name: '李同学', nodeCount: 7, isSubmitted: true },
  { id: 6, name: '赵同学', nodeCount: 6, isSubmitted: false },
  { id: 7, name: '周同学', nodeCount: 4, isSubmitted: false },
  { id: 8, name: '吴同学', nodeCount: 9, isSubmitted: true },
  { id: 9, name: '孙同学', nodeCount: 10, isSubmitted: true },
  { id: 10, name: '郑同学', nodeCount: 2, isSubmitted: false },
  { id: 11, name: '刘同学', nodeCount: 8, isSubmitted: true },
  { id: 12, name: '钱同学', nodeCount: 5, isSubmitted: false },
])

const submittedCount = computed(() => studentList.value.filter(s => s.isSubmitted).length)
const avgNodes = computed(() => {
  const sum = studentList.value.reduce((acc, curr) => acc + curr.nodeCount, 0)
  return Math.round(sum / studentList.value.length)
})

// ===== 搜索与分页逻辑 =====
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(8) // 两行卡片

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

const spectatingStudent = ref(null)

const openSpectator = (student) => { spectatingStudent.value = student }
const closeSpectator = () => { spectatingStudent.value = null }
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.2); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.4); }

.dot-pattern {
  background-image: radial-gradient(#cbd5e1 1.5px, transparent 1.5px);
  background-size: 24px 24px;
}

.animate-fade-in { animation: fadeIn 0.3s ease-out forwards; }
@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.98); }
  to { opacity: 1; transform: scale(1); }
}
</style>