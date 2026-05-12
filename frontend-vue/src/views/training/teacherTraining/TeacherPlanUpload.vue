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
           <div class="w-px h-8 bg-gray-200"></div>
           <button @click="batchDownload" 
                   class="flex items-center gap-2 px-4 py-2 bg-indigo-500 text-white text-sm font-bold rounded-lg hover:bg-indigo-600 transition-colors shadow-sm">
             <svg style="width: 16px; height: 16px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
             批量下载
           </button>
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

    <div v-if="isPreviewModalOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/60 backdrop-blur-sm">
      <div class="bg-white w-[80vw] h-[80vh] rounded-2xl shadow-2xl flex flex-col relative overflow-hidden">
        
        <button @click="closePreviewModal" class="absolute top-4 right-4 text-gray-400 hover:text-gray-700 transition-colors z-10 bg-white/80 rounded-full p-1">
          <svg style="width: 24px; height: 24px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
        </button>

        <div class="flex flex-1 overflow-hidden p-6 gap-6 pt-12">
          
          <div class="w-1/2 bg-gray-50 border border-gray-200 rounded-xl flex flex-col items-center justify-center overflow-hidden">
            <svg class="text-gray-300 mb-4" style="width: 64px; height: 64px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
            <p class="text-gray-700 font-bold text-lg mb-2">{{ currentPreviewStudent?.fileName }}</p>
            <p class="text-gray-400 text-sm">（文件在线预览渲染区域）</p>
          </div>

          <div class="w-1/2 flex flex-col gap-4">
            
            <div class="flex-1 bg-indigo-50/50 border border-indigo-100 rounded-xl p-5 overflow-y-auto custom-scrollbar">
              <h3 class="text-indigo-600 font-bold mb-4 flex items-center gap-2 text-base">
                <svg style="width: 20px; height: 20px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
                AI 方案结构分析与标注
              </h3>
              <div class="text-sm text-gray-700 space-y-4 leading-relaxed">
                <p><strong>🔹 结构完整度评分：</strong>85/100</p>
                <p><strong>🔹 亮点标注：</strong>方案详尽覆盖了 Python 数组（List）的基础概念，包含创建、索引和基础遍历。结构层次清晰，适合初学者过渡。</p>
                <p><strong>⚠️ 漏洞预警（知识盲区）：</strong>
                  <br>1. 未涉及多维数组（嵌套列表）的应用场景。
                  <br>2. 缺乏切片（Slicing）高阶用法的代码示例与内存引用说明。
                  <br>3. 未提及列表推导式（List Comprehension）等性能优化写法。
                </p>
                <p><strong>💡 AI 批阅建议：</strong>建议重点考查学生对内存分配机制（深浅拷贝）的理解，可要求补充切片的高频运用示例以完善全局认知。</p>
              </div>
            </div>

            <div class="shrink-0 flex flex-col gap-3">
              <textarea 
                v-model="teacherFeedback" 
                placeholder="请输入您的专业批阅与修改建议..."
                class="w-full h-32 p-4 bg-white border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 resize-none shadow-sm custom-scrollbar"
              ></textarea>
              <div class="flex justify-end">
                <button @click="submitFeedback" 
                        class="px-6 py-2.5 bg-indigo-500 text-white text-sm font-bold rounded-lg hover:bg-indigo-600 transition-colors shadow-sm flex items-center gap-2">
                  <svg style="width: 18px; height: 18px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                  提交批阅
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
import { ref, computed, watch } from 'vue'

const studentList = ref([
  { id: 1, name: '陈同学', status: 'submitted', fileName: '陈同学-Python数组学习方案.pdf', uploadTime: '10:42:15' },
  { id: 2, name: '林同学', status: 'submitted', fileName: 'Python数组知识点整理_林.docx', uploadTime: '10:35:10' },
  { id: 3, name: '张同学', status: 'pending', fileName: '', uploadTime: '' },
  { id: 4, name: '王同学', status: 'rejected', fileName: '', uploadTime: '' },
  { id: 5, name: '李同学', status: 'submitted', fileName: '李_数组学习总结v2.pdf', uploadTime: '10:50:01' },
  { id: 6, name: '赵同学', status: 'pending', fileName: '', uploadTime: '' },
  { id: 7, name: '周同学', status: 'submitted', fileName: '周_列表操作详解.docx', uploadTime: '10:11:44' },
  { id: 8, name: '吴同学', status: 'submitted', fileName: 'Python数组方案v3.pdf', uploadTime: '10:55:00' },
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

// ===== 浮窗相关逻辑 (新增) =====
const isPreviewModalOpen = ref(false)
const currentPreviewStudent = ref(null)
const teacherFeedback = ref('')

const previewPlan = (student) => {
  currentPreviewStudent.value = student
  teacherFeedback.value = '' // 打开时清空输入框
  isPreviewModalOpen.value = true
}

const closePreviewModal = () => {
  isPreviewModalOpen.value = false
  setTimeout(() => {
    currentPreviewStudent.value = null
  }, 300) // 等待可能存在的过渡动画
}

const submitFeedback = () => {
  if (!teacherFeedback.value.trim()) {
    alert('提示：批阅意见不可为空，请补充反馈内容。')
    return
  }
  // 此处可接入后端API提交批注
  alert(`已成功提交对 ${currentPreviewStudent.value.name} 同学的批阅意见！\n内容预览: ${teacherFeedback.value}`)
  closePreviewModal()
}
// =========================

const rejectPlan = (student) => {
  const reason = prompt(`请输入打回 ${student.name} 同学方案的理由：`, '方案内容不完整或需要补充数组核心知识点')
  if (reason) {
    student.status = 'rejected'
    student.fileName = '' 
  }
}

const batchDownload = () => {
  const submittedStudents = studentList.value.filter(s => s.status === 'submitted')
  if (submittedStudents.length === 0) {
    alert('暂无已提交的方案文件可下载')
    return
  }
  alert(`正在打包下载 ${submittedStudents.length} 位同学的方案文件...\n\n${submittedStudents.map(s => s.name + ': ' + s.fileName).join('\n')}`)
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.2); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.4); }
</style>