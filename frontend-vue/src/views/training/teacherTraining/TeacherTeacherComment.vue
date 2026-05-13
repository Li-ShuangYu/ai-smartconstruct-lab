<template>
  <div style="height: 100vh;" class="p-6 bg-slate-50">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 font-sans overflow-hidden">
      
      <div class="flex justify-between items-end mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase italic">Node: TEACHER_REVIEW</div>
          <h1 class="text-2xl font-bold text-gray-800">教师总结与点评下发</h1>
        </div>
        
        <div class="flex items-center gap-4 bg-white/60 px-5 py-2.5 rounded-xl border border-gray-100 shadow-sm">
          <span class="text-sm font-bold text-gray-600">已点评人数：</span>
          <span class="text-xl font-black" :class="reviewedCount === students.length ? 'text-green-600' : 'text-indigo-600'">
            {{ reviewedCount }} <span class="text-sm text-gray-400 font-normal">/ {{ students.length }}</span>
          </span>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1.2] flex flex-col gap-5 min-w-[360px]">
          
          <div class="flex-1 bg-white/60 border border-gray-100 rounded-2xl p-6 shadow-sm flex flex-col overflow-hidden">
            <h3 class="text-sm font-bold text-indigo-900 mb-4 flex items-center gap-2 shrink-0">
              <svg class="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
              AI 班级学情洞察报告
            </h3>
            
            <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 space-y-6">
              
              <div class="bg-orange-50/50 border border-orange-100 rounded-xl p-4">
                <h4 class="text-xs font-bold text-orange-700 mb-2 flex items-center gap-1">
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
                  班级共性问题总结
                </h4>
                <p class="text-sm text-gray-700 leading-relaxed">{{ aiInsights.commonIssues }}</p>
              </div>

              <div class="bg-green-50/50 border border-green-100 rounded-xl p-4">
                <h4 class="text-xs font-bold text-green-700 mb-3 flex items-center gap-1">
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                  优秀案例推荐
                </h4>
                <div class="space-y-3">
                  <div v-for="(caseItem, idx) in aiInsights.excellentCases" :key="idx" class="bg-white p-3 rounded-lg border border-green-200/50 shadow-sm">
                    <div class="flex justify-between items-center mb-1">
                      <span class="text-sm font-bold text-gray-800">{{ caseItem.studentName }}</span>
                      <span class="text-[10px] bg-green-100 text-green-700 px-2 py-0.5 rounded font-bold">{{ caseItem.tag }}</span>
                    </div>
                    <p class="text-xs text-gray-600">{{ caseItem.reason }}</p>
                  </div>
                </div>
              </div>

            </div>
          </div>

          <div class="bg-white/60 border border-gray-100 rounded-2xl p-5 shadow-sm shrink-0 flex flex-col gap-3">
            <button 
              @click="batchSendComments"
              class="hero-send-btn w-full justify-center py-3.5 rounded-xl text-sm font-bold shadow-lg transition-all flex items-center gap-2 active:scale-95"
            >
              一键批量下发 AI 默认点评
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" /></svg>
            </button>
            <button 
              @click="exportComments"
              class="w-full py-3 bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 hover:text-indigo-600 rounded-xl text-sm font-bold transition-all shadow-sm flex justify-center items-center gap-2"
            >
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
              批量导出点评与成绩报表
            </button>
          </div>

        </div>

        <div class="flex-[1.8] flex flex-col bg-white/60 border border-gray-100 rounded-2xl shadow-sm overflow-hidden min-w-[400px]">
          <div class="bg-gray-50/80 px-6 py-4 border-b border-gray-100 flex justify-between items-center shrink-0">
            <h3 class="font-bold text-gray-800 flex items-center gap-2">
              <svg class="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" /></svg>
              学生个性化点评编辑区
            </h3>
            <span class="text-[10px] text-gray-400 font-bold bg-white px-2 py-1 rounded border border-gray-200">
              * 文本框已默认填充 AI 预生成点评，修改后独立发送即可
            </span>
          </div>

          <div class="flex-1 overflow-y-auto p-5 custom-scrollbar space-y-4">
            <div 
              v-for="student in students" 
              :key="student.id"
              class="bg-white border rounded-xl p-4 transition-all"
              :class="student.status === 'sent' ? 'border-green-200 shadow-sm' : 'border-gray-200 hover:border-indigo-200'"
            >
              <div class="flex justify-between items-center mb-3">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold text-white shadow-sm"
                       :class="student.status === 'sent' ? 'bg-green-500' : 'bg-indigo-400'">
                    {{ student.name.charAt(0) }}
                  </div>
                  <span class="text-sm font-bold text-gray-800">{{ student.name }}</span>
                </div>
                <span class="text-[10px] font-bold px-2 py-1 rounded"
                      :class="student.status === 'sent' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'">
                  {{ student.status === 'sent' ? '已下发' : '待下发' }}
                </span>
              </div>
              
              <div class="relative">
                <textarea 
                  v-model="student.comment"
                  :disabled="student.status === 'sent'"
                  rows="3"
                  class="w-full bg-gray-50 border border-gray-100 rounded-lg p-3 text-sm text-gray-700 outline-none focus:bg-white focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100/50 resize-none custom-scrollbar disabled:bg-gray-50 disabled:text-gray-500 disabled:cursor-not-allowed"
                ></textarea>
                
                <div class="flex justify-end mt-2">
                  <button 
                    v-if="student.status !== 'sent'"
                    @click="sendIndividualComment(student)"
                    class="px-4 py-1.5 bg-indigo-50 text-indigo-600 hover:bg-indigo-100 border border-indigo-100 rounded text-xs font-bold transition-colors"
                  >
                    独立下发
                  </button>
                  <button 
                    v-else
                    @click="student.status = 'pending'"
                    class="px-4 py-1.5 bg-white text-gray-500 hover:text-indigo-600 border border-gray-200 rounded text-xs font-bold transition-colors"
                  >
                    撤回修改
                  </button>
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

// --- AI 洞察数据 ---
const aiInsights = ref({
  commonIssues: '全班在处理“动态数组扩容边界”问题时普遍存在认知盲区，约有 45% 的学生在互评代码和理论答题中未能正确处理越界异常（IndexError）。建议在后续课程中增加针对切片步长越界的专项调试训练。',
  excellentCases: [
    { studentName: '陈静', tag: '架构清晰', reason: '在需求分析与架构设计环节，代码逻辑解耦彻底，将数据校验与核心算法分离。' },
    { studentName: '张伟', tag: '代码优雅', reason: '熟练运用 Pythonic 切片语法完成列表反转，并完美兼容了空数组异常处理。' }
  ]
})

// --- 学生列表与 AI 草稿数据 ---
const students = ref([
  { id: 1, name: '陈静', status: 'pending', comment: '表现非常出色！代码逻辑严谨，架构设计清晰，特别是对空数组的防御性编程做得很好。继续保持。' },
  { id: 2, name: '张伟', status: 'pending', comment: '基础扎实，能够熟练运用切片技巧。但在互评阶段给分略显随意，希望后续能更严谨地对待代码规范度评估。' },
  { id: 3, name: '孙颖', status: 'pending', comment: '理论知识掌握不错，但在编写核心源代码时，存在冗余的循环逻辑，建议多利用内置函数来提升效率。' },
  { id: 4, name: '赵云', status: 'pending', comment: '任务完成度很高。但在处理边界条件时漏掉了一个潜在的 IndexError，建议复习一下动态数组的扩容机制。' },
  { id: 5, name: '吴敏', status: 'sent', comment: '整体表现平稳。思维导图绘制得非常详实，但代码部分的注释较少，建议后续注意代码可读性。' },
  { id: 6, name: '李明', status: 'pending', comment: '本节实训进步明显。虽然在 AI 对练环节卡壳了较长时间，但最终掌握了核心概念，值得表扬！' },
  { id: 7, name: '林峰', status: 'pending', comment: '基本完成了各项任务。后续需要重点巩固 Python 列表的底层内存分配原理，这部分在理论答题中失分较多。' }
])

const reviewedCount = computed(() => students.value.filter(s => s.status === 'sent').length)

// --- 交互操作 ---
const batchSendComments = () => {
  const pendingStudents = students.value.filter(s => s.status !== 'sent')
  if (pendingStudents.length === 0) {
    alert('全班均已下发点评！')
    return
  }
  if (confirm(`确认将当前文本框内的点评一键下发给 ${pendingStudents.length} 名未点评学生吗？`)) {
    students.value.forEach(s => s.status = 'sent')
  }
}

const sendIndividualComment = (student) => {
  if (!student.comment.trim()) {
    alert('点评内容不能为空！')
    return
  }
  student.status = 'sent'
}

const exportComments = () => {
  alert('正在生成《全班点评与综合成绩报表.xlsx》，即将开始下载...')
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

.hero-send-btn {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: white;
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
</style>