<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative">
      
      <div class="flex justify-between items-end mb-2 pb-4 border-b border-gray-200/50 shrink-0" style="margin-top: -3vh;">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: PLAN_UPLOAD Monitor</div>
          <h1 class="text-3xl font-bold text-gray-800 flex items-center gap-3">
             <svg class="text-indigo-500" style="width: 28px; height: 28px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
             方案文件收取与审核网格
          </h1>
        </div>
        
        <div class="flex items-center gap-4 bg-white/60 px-5 py-3 rounded-2xl border border-gray-100 shadow-sm">
           <div class="text-center">
             <div class="text-xs text-gray-500 mb-1">已提交</div>
             <div class="text-xl font-bold text-green-500">{{ submittedCount }}</div>
           </div>
           <div class="w-px h-8 bg-gray-200"></div>
           <div class="text-center">
             <div class="text-xs text-gray-500 mb-1">待提交</div>
             <div class="text-xl font-bold text-gray-400">{{ pendingCount }}</div>
           </div>
           <div class="w-px h-8 bg-gray-200"></div>
           <div class="text-center">
             <div class="text-xs text-gray-500 mb-1">已驳回</div>
             <div class="text-xl font-bold text-red-500">{{ rejectedCount }}</div>
           </div>
        </div>
      </div>

      <div class="mb-2 flex items-center shrink-0">
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

      <div class="flex-1 overflow-y-auto pr-2 custom-scrollbar pb-4">
        <template v-if="paginatedStudents.length > 0">
          <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5">
            <div v-for="student in paginatedStudents" :key="student.id" 
                 class="bg-white/60 border rounded-2xl p-5 flex flex-col shadow-sm transition-all duration-300 hover:shadow-md relative overflow-hidden"
                 :class="{
                   'border-green-200': student.status === 'submitted',
                   'border-gray-200': student.status === 'pending',
                   'border-red-200 bg-red-50/20': student.status === 'rejected'
                 }">
              
              <div class="flex justify-between items-start">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold border"
                       :class="student.status === 'submitted' ? 'bg-green-50 text-green-600 border-green-100' : 
                               student.status === 'rejected' ? 'bg-red-50 text-red-600 border-red-100' : 'bg-gray-50 text-gray-600 border-gray-200'">
                    {{ student.name.charAt(0) }}
                  </div>
                  <span class="font-bold text-gray-700 text-sm">{{ student.name }}</span>
                </div>
                <span class="text-xs font-bold px-2 py-1 rounded border"
                      :class="{
                        'bg-green-50 text-green-600 border-green-200': student.status === 'submitted',
                        'bg-gray-50 text-gray-400 border-gray-200': student.status === 'pending',
                        'bg-red-50 text-red-600 border-red-200': student.status === 'rejected'
                      }">
                  {{ getStatusText(student.status) }}
                </span>
              </div>

              <div class="flex-1 flex flex-col justify-center min-h-[70px]">
                <template v-if="student.status === 'submitted'">
                  <div class="flex items-center gap-2 p-2 bg-gray-50 border border-gray-100 rounded-lg">
                    <svg class="text-indigo-400" style="width: 24px; height: 24px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" /></svg>
                    <div class="overflow-hidden">
                      <div class="text-xs font-bold text-gray-700 truncate" :title="student.fileName">{{ student.fileName }}</div>
                      <div class="text-[10px] text-gray-400 mt-0.5">{{ student.uploadTime }}</div>
                    </div>
                  </div>
                </template>
                <template v-else-if="student.status === 'rejected'">
                  <p class="text-xs text-red-500 italic">等待学生重新上传...</p>
                </template>
                <template v-else>
                  <p class="text-xs text-gray-400 italic">尚未收到文件</p>
                </template>
              </div>

              <div class="pt-3 border-t border-gray-100/80 flex gap-2" v-if="student.status === 'submitted'">
                <button class="flex-1 py-1.5 text-xs font-bold text-indigo-600 bg-indigo-50 hover:bg-indigo-100 rounded border border-indigo-100 transition-colors flex items-center justify-center gap-1" @click="previewPlan(student)">
                   <svg style="width: 14px; height: 14px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
                   在线查阅
                </button>
                <button class="flex-1 py-1.5 text-xs font-bold text-red-600 bg-red-50 hover:bg-red-100 rounded border border-red-100 transition-colors flex items-center justify-center gap-1" @click="rejectPlan(student)">
                   <svg style="width: 14px; height: 14px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                   驳回打回
                </button>
              </div>
            </div>
          </div>
        </template>
        <div v-else class="h-full flex flex-col items-center justify-center text-gray-400 space-y-3 pb-10">
           <svg style="width: 48px; height: 48px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
           <span class="text-sm font-medium">无匹配学生记录</span>
        </div>
      </div>

      <div class="pt-1 border-t border-gray-200/60 flex items-center justify-between shrink-0">
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
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const studentList = ref([
  { id: 1, name: '陈同学', status: 'submitted', fileName: '陈同学-SM4系统设计方案.pdf', uploadTime: '10:42:15' },
  { id: 2, name: '林同学', status: 'submitted', fileName: '密码学方案_林.docx', uploadTime: '10:35:10' },
  { id: 3, name: '张同学', status: 'pending', fileName: '', uploadTime: '' },
  { id: 4, name: '王同学', status: 'rejected', fileName: '', uploadTime: '' },
  { id: 5, name: '李同学', status: 'submitted', fileName: '李_最终版方案v2.pdf', uploadTime: '10:50:01' },
  { id: 6, name: '赵同学', status: 'pending', fileName: '', uploadTime: '' },
  { id: 7, name: '周同学', status: 'submitted', fileName: '周_接口封装文档.docx', uploadTime: '10:11:44' },
  { id: 8, name: '吴同学', status: 'submitted', fileName: '方案v3.pdf', uploadTime: '10:55:00' },
  { id: 9, name: '郑同学', status: 'pending', fileName: '', uploadTime: '' },
  { id: 10, name: '孙同学', status: 'rejected', fileName: '', uploadTime: '' },
])

const submittedCount = computed(() => studentList.value.filter(s => s.status === 'submitted').length)
const pendingCount = computed(() => studentList.value.filter(s => s.status === 'pending').length)
const rejectedCount = computed(() => studentList.value.filter(s => s.status === 'rejected').length)

// ===== 搜索与分页逻辑 =====
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(8) // 两排

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
  const map = { 'submitted': '已提交', 'pending': '待上传', 'rejected': '被驳回' }
  return map[status]
}

const previewPlan = (student) => {
  alert(`正在打开在线预览模块，查阅文件：${student.fileName}`)
}

const rejectPlan = (student) => {
  const reason = prompt(`请输入打回 ${student.name} 同学方案的理由：`, '方案偏题或缺少关键要求')
  if (reason) {
    student.status = 'rejected'
    student.fileName = '' 
  }
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.2); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.4); }
</style>