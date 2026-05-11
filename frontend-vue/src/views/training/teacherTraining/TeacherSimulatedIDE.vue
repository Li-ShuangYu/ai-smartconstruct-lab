<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative">
      
      <div class="flex justify-between items-end pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: CODING_CLASS Monitor</div>
          <h1 class="text-3xl font-bold text-gray-800 flex items-center gap-3">
             <svg class="text-indigo-500" style="width: 28px; height: 28px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" /></svg>
             全班 IDE 实训态势感知
          </h1>
        </div>
        
        <div class="flex gap-4">
          <div class="bg-white/60 px-5 py-2 rounded-xl border border-gray-100 shadow-sm flex flex-col items-center justify-center">
            <span class="text-xs text-gray-500">活跃编码人数</span>
            <span class="text-xl font-bold text-indigo-600">{{ activeCount }} / {{ studentList.length }}</span>
          </div>
          <div class="bg-white/60 px-5 py-2 rounded-xl border border-gray-100 shadow-sm flex flex-col items-center justify-center">
            <span class="text-xs text-gray-500">全班平均报错率</span>
            <span class="text-xl font-bold text-amber-500">18.5%</span>
          </div>
        </div>
      </div>

      <div class="flex-1 bg-white/40 border border-gray-200/60 rounded-2xl flex flex-col overflow-hidden shadow-sm">
        
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

        <div class="grid grid-cols-12 gap-4 px-6 py-2 border-b border-gray-200/50 bg-gray-50/50 text-xs font-bold text-gray-500 uppercase tracking-wider shrink-0">
          <div class="col-span-2">学生姓名</div>
          <div class="col-span-3">当前状态 (IDE)</div>
          <div class="col-span-2 text-center">提交次数</div>
          <div class="col-span-3">近期报错频率 / 痛点</div>
          <div class="col-span-2 text-right">干预操作</div>
        </div>
        
        <div class="flex-1 overflow-y-auto p-2 space-y-2 custom-scrollbar">
          <template v-if="paginatedStudents.length > 0">
            <div v-for="student in paginatedStudents" :key="student.id" class="grid grid-cols-12 gap-4 px-4 py-3 items-center bg-white border border-gray-100 rounded-xl shadow-sm hover:shadow-md transition-shadow">
              
              <div class="col-span-2 flex items-center gap-3">
                <div class="w-9 h-9 rounded-full bg-gradient-to-br from-indigo-50 to-indigo-100 text-indigo-700 flex items-center justify-center text-sm font-bold border border-indigo-200">
                  {{ student.name.charAt(0) }}
                </div>
                <span class="text-sm font-bold text-gray-700">{{ student.name }}</span>
              </div>
              
              <div class="col-span-3 flex items-center gap-2">
                <span class="flex h-2.5 w-2.5 relative">
                  <span v-if="student.status === 'coding'" class="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-400 opacity-75"></span>
                  <span class="relative inline-flex rounded-full h-2.5 w-2.5" 
                        :class="{
                          'bg-blue-500': student.status === 'coding',
                          'bg-amber-500': student.status === 'error',
                          'bg-green-500': student.status === 'passed'
                        }"></span>
                </span>
                <span class="text-xs font-bold" 
                      :class="{
                        'text-blue-600': student.status === 'coding',
                        'text-amber-600': student.status === 'error',
                        'text-green-600': student.status === 'passed'
                      }">
                  {{ getStatusText(student.status) }}
                </span>
              </div>

              <div class="col-span-2 text-center text-sm font-mono text-gray-600 font-bold">
                {{ student.submitCount }}
              </div>

              <div class="col-span-3 flex flex-col">
                <div class="w-full bg-gray-100 rounded-full h-1.5 mb-1">
                  <div class="h-1.5 rounded-full" 
                       :class="student.errorRate > 50 ? 'bg-red-400' : 'bg-indigo-300'"
                       :style="{ width: student.errorRate + '%' }"></div>
                </div>
                <span class="text-[10px] text-gray-400 truncate" :title="student.lastError">
                  {{ student.lastError || '暂无明显异常' }}
                </span>
              </div>

              <div class="col-span-2 flex justify-end">
                <button 
                  @click="openSpectator(student)"
                  class="px-4 py-1.5 bg-indigo-50 hover:bg-indigo-100 text-indigo-600 border border-indigo-200 rounded-lg text-xs font-bold flex items-center gap-2 transition-colors"
                  :class="{'ring-2 ring-indigo-400 ring-offset-1': student.status === 'error'}"
                >
                  <svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
                  旁观指导
                </button>
              </div>

            </div>
          </template>
          <div v-else class="h-full flex flex-col items-center justify-center text-gray-400 space-y-3 pb-6">
             <svg style="width: 40px; height: 40px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
             <span class="text-sm font-medium">暂无匹配学生</span>
          </div>
        </div>

        <div class="px-6 py-3 border-t border-gray-200/60 bg-white/40 flex items-center justify-between shrink-0">
          <button @click="prevPage" :disabled="currentPage === 1" class="px-4 py-2 rounded-lg text-sm font-bold transition-colors" :class="currentPage === 1 ? 'text-gray-400 cursor-not-allowed' : 'text-indigo-600 bg-indigo-50 hover:bg-indigo-100'">
            上一页
          </button>
          <div class="text-sm font-bold text-gray-500">
            第 <span class="text-indigo-600 mx-1">{{ currentPage }}</span> 页 / 共 <span class="mx-1">{{ totalPages || 1 }}</span> 页
          </div>
          <button @click="nextPage" :disabled="currentPage >= totalPages || totalPages === 0" class="px-4 py-2 rounded-lg text-sm font-bold transition-colors" :class="currentPage >= totalPages || totalPages === 0 ? 'text-gray-400 cursor-not-allowed' : 'text-indigo-600 bg-indigo-50 hover:bg-indigo-100'">
            下一页
          </button>
        </div>

      </div>

      <div v-if="spectatingStudent" class="absolute inset-0 z-50 bg-gray-900/40 backdrop-blur-sm rounded-[20px] flex items-center justify-center animate-fade-in">
        <div class="bg-white rounded-2xl w-[80%] max-w-4xl h-[70%] shadow-2xl flex flex-col overflow-hidden border border-gray-200">
          <div class="h-12 bg-gray-100 border-b border-gray-200 flex items-center justify-between px-4">
            <div class="flex items-center gap-2 text-sm font-bold text-gray-700">
              <span class="w-2.5 h-2.5 rounded-full bg-red-500 animate-pulse"></span>
              正在旁观：{{ spectatingStudent.name }} 的 IDE
            </div>
            <button @click="spectatingStudent = null" class="text-gray-400 hover:text-red-500 transition-colors">
              <svg style="width: 20px; height: 20px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
            </button>
          </div>
          <div class="flex-1 bg-[#1e1e1e] p-4 flex flex-col relative">
             <div class="absolute inset-0 flex items-center justify-center text-gray-600 font-mono text-xl z-0 pointer-events-none opacity-20">
                Spectator Mode
             </div>
             <textarea disabled class="flex-1 bg-transparent text-[#d4d4d4] font-mono text-sm outline-none resize-none z-10 custom-scrollbar leading-relaxed">
def tau_transform(a):
    # 学生当前写了一半的代码...
    byte1 = (a >> 24) & 0xFF
    # 老师：这里位移操作写错了，应该是...
             </textarea>
             <div class="h-16 bg-[#2d2d2d] rounded-xl mt-4 border border-gray-700 p-2 flex items-center gap-2 z-10">
               <input type="text" placeholder="向该学生发送指导消息..." class="flex-1 bg-[#1e1e1e] border border-gray-600 rounded-lg px-3 py-2 text-sm text-gray-200 outline-none focus:border-indigo-500">
               <button class="bg-indigo-600 hover:bg-indigo-500 text-white px-4 py-2 rounded-lg text-sm font-bold transition-colors">发送</button>
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
  { id: 1, name: '陈同学', status: 'coding', submitCount: 2, errorRate: 20, lastError: 'SyntaxError: invalid syntax' },
  { id: 2, name: '林同学', status: 'error', submitCount: 8, errorRate: 75, lastError: 'TypeError: unsupported operand type' },
  { id: 3, name: '张同学', status: 'passed', submitCount: 1, errorRate: 0, lastError: '' },
  { id: 4, name: '王同学', status: 'coding', submitCount: 4, errorRate: 40, lastError: 'IndexError: list index out of range' },
  { id: 5, name: '李同学', status: 'error', submitCount: 6, errorRate: 85, lastError: 'NameError: name "Sbox" is not defined' },
  { id: 6, name: '赵同学', status: 'coding', submitCount: 3, errorRate: 15, lastError: '' },
  { id: 7, name: '孙同学', status: 'passed', submitCount: 2, errorRate: 0, lastError: '' },
  { id: 8, name: '周同学', status: 'error', submitCount: 9, errorRate: 90, lastError: 'IndentationError: unexpected indent' },
  { id: 9, name: '吴同学', status: 'coding', submitCount: 5, errorRate: 25, lastError: 'KeyError' },
  { id: 10, name: '郑同学', status: 'passed', submitCount: 1, errorRate: 0, lastError: '' },
])

const activeCount = computed(() => {
  return studentList.value.filter(s => s.status === 'coding' || s.status === 'error').length
})

// ===== 搜索与分页逻辑 =====
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(5) 

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

const getStatusText = (status) => {
  const map = {
    'coding': '正在编写代码...',
    'error': '陷入频繁报错',
    'passed': '已通过验证'
  }
  return map[status] || status
}

const spectatingStudent = ref(null)

const openSpectator = (student) => {
  spectatingStudent.value = student
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.2); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.4); }

.animate-fade-in { animation: fadeIn 0.3s ease-out forwards; }
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>