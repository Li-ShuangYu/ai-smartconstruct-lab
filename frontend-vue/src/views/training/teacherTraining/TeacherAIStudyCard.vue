<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10">
      
      <div class="flex justify-between items-end mb-6 pb-4 border-b border-gray-200/50">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: THEORY_CLASS Monitor</div>
          <h1 class="text-3xl font-bold text-gray-800 flex items-center gap-3">
             <svg class="text-indigo-500" style="width: 28px; height: 28px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" /></svg>
             理论实训：AI 伴学监控大盘
          </h1>
        </div>
        <div class="text-sm text-gray-500 flex items-center gap-2 bg-indigo-50 px-4 py-2 rounded-lg border border-indigo-100">
          <span class="relative flex h-3 w-3">
            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75"></span>
            <span class="relative inline-flex rounded-full h-3 w-3 bg-indigo-500"></span>
          </span>
          实训进行中，当前活跃学生：<span class="font-bold text-indigo-700">32/32</span>
        </div>
      </div>

      <div class="flex-1 flex flex-col md:flex-row gap-6 min-h-0">
        
        <div class="flex-[1.5] bg-white/40 border border-gray-200/60 rounded-2xl flex flex-col overflow-hidden shadow-sm">
          <div class="bg-gray-50/80 px-4 py-3 border-b border-gray-100 flex items-center justify-between">
            <span class="font-bold text-gray-700 text-sm flex items-center gap-2">
               <svg class="text-indigo-400" style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
               全班实时提问日志
            </span>
            <span class="text-xs text-indigo-500 animate-pulse">Live Updating...</span>
          </div>
          
          <div class="flex-1 overflow-y-auto p-4 space-y-3 custom-scrollbar">
            <div v-for="log in realtimeLogs" :key="log.id" class="p-3 bg-white border border-gray-100 rounded-xl shadow-sm hover:shadow-md transition-shadow animate-slide-up">
              <div class="flex justify-between items-center mb-2">
                <div class="flex items-center gap-2 text-xs">
                  <span class="font-bold text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded">{{ log.studentName }}</span>
                  <span class="text-gray-400">{{ log.time }}</span>
                </div>
                <span class="text-[10px] text-gray-400 border border-gray-200 px-1.5 rounded">卡片 {{ log.cardId }}</span>
              </div>
              <p class="text-sm text-gray-700 leading-relaxed font-medium">"{{ log.question }}"</p>
            </div>
          </div>
        </div>

        <div class="flex-[1] flex flex-col gap-6">
          
          <div class="bg-white/40 border border-gray-200/60 rounded-2xl p-5 shadow-sm h-1/2 flex flex-col">
             <h3 class="font-bold text-gray-700 text-sm mb-4">高频提问词云墙</h3>
             <div class="flex-1 flex flex-wrap content-center justify-center gap-x-4 gap-y-2 p-2 relative">
                <span class="text-3xl font-black text-indigo-600">非线性变换</span>
                <span class="text-xl font-bold text-purple-500">S盒</span>
                <span class="text-sm font-medium text-gray-400">初始密钥</span>
                <span class="text-2xl font-extrabold text-blue-500">轮函数</span>
                <span class="text-lg font-semibold text-indigo-400">扩散</span>
                <span class="text-base text-gray-500">混淆原理</span>
                <span class="text-xs text-gray-400">怎么分组</span>
                <span class="text-xl font-bold text-pink-500">异或操作</span>
             </div>
          </div>

          <div class="bg-indigo-50/50 border border-indigo-100 rounded-2xl p-5 shadow-sm h-1/2 flex flex-col">
            <h3 class="font-bold text-indigo-900 text-sm mb-3 flex items-center gap-2">
              <svg style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM10 7v3m0 0v3m0-3h3m-3 0H7" /></svg>
              认知盲区抽查
            </h3>
            <p class="text-xs text-indigo-600/70 mb-4">选择提问频繁的学生，查阅其与 AI 的完整对话记录。</p>
            
            <div class="mb-4">
              <select v-model="selectedStudent" class="w-full bg-white border border-indigo-200 text-gray-700 text-sm rounded-lg focus:ring-indigo-500 focus:border-indigo-500 block p-2.5 outline-none shadow-sm">
                <option value="" disabled>-- 选择要抽查的学生 --</option>
                <option v-for="stu in suspectStudents" :key="stu.id" :value="stu.id">
                  {{ stu.name }} (共提问 {{ stu.qCount }} 次)
                </option>
              </select>
            </div>

            <button class="hero-send-btn w-full justify-center shadow-md py-3" :disabled="!selectedStudent" @click="checkStudentLogs">
              查阅该生完整 AI 问答日志
            </button>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 模拟全班实时滚动提问
const realtimeLogs = ref([
  { id: 1, studentName: '李娜', time: '10:42:05', cardId: 2, question: '这里的32轮非线性迭代到底是什么意思？能用大白话解释吗？' },
  { id: 2, studentName: '王强', time: '10:41:30', cardId: 3, question: 'S盒的作用就是为了混淆吗？它和位移有什么区别？' },
  { id: 3, studentName: '赵芳', time: '10:40:15', cardId: 1, question: '分组长度128比特，如果最后一段不够128比特怎么办？' },
  { id: 4, studentName: '陈伟', time: '10:39:50', cardId: 3, question: '线性变换 L 主要是为了实现扩散，对吧？' },
  { id: 5, studentName: '刘洋', time: '10:38:00', cardId: 2, question: '异或操作的作用是什么？' },
  { id: 6, studentName: '王芳', time: '10:37:00', cardId: 1, question: '分组长度128比特，如果最后一段不够128比特怎么办？' },
])

// 模拟疑似有认知困难/提问频繁的学生列表
const selectedStudent = ref('')
const suspectStudents = ref([
  { id: 's1', name: '李娜', qCount: 8 },
  { id: 's2', name: '王强', qCount: 5 },
  { id: 's3', name: '刘洋', qCount: 4 }
])

const checkStudentLogs = () => {
  if (!selectedStudent.value) return
  const student = suspectStudents.value.find(s => s.id === selectedStudent.value)
  alert(`打开学生【${student.name}】的详细 AI 问答面板...`)
  // 实际开发中这里会打开一个 Dialog 或侧边抽屉，展示完整聊天记录
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.3); border-radius: 4px; }

.animate-slide-up { animation: slideUp 0.4s ease-out forwards; }
@keyframes slideUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>