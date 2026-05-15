<template>
  <div style="height: 100%;">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative overflow-hidden font-sans">
      
      <div class="flex justify-between items-center mb-6 shrink-0 border-b border-gray-200/50 pb-4">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase italic">Node: TASK_DISPATCH</div>
          <h2 class="text-2xl font-bold text-gray-800 flex items-center gap-2">任务下发</h2>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1.8] flex flex-col gap-4 overflow-y-auto custom-scrollbar pr-2">
          
          <div class="bg-white/60 p-5 rounded-xl border border-gray-200/60 shadow-sm">
            <div class="flex justify-between items-start mb-2">
              <h1 class="text-xl font-bold text-gray-800">{{ taskData.title }}</h1>
              <span class="px-3 py-1 bg-red-50 text-red-600 rounded text-xs font-bold border border-red-100 shrink-0">
                截止: {{ taskData.deadline }}
              </span>
            </div>
          </div>

          <div class="bg-white/60 p-5 rounded-xl border border-gray-200/60 shadow-sm flex-1">
            <h4 class="text-sm font-bold text-gray-500 mb-3 flex items-center gap-2">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" /></svg>
              任务要求
            </h4>
            <div class="text-sm text-gray-700 leading-loose whitespace-pre-wrap">{{ taskData.requirements }}</div>
          </div>

          <div class="bg-indigo-50/50 p-5 rounded-xl border border-indigo-100 shadow-sm shrink-0">
            <h4 class="text-sm font-bold text-indigo-800 mb-3 flex items-center gap-2">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" /></svg>
              任务材料下载
            </h4>
            <div class="flex flex-col gap-2">
              <div v-for="(file, index) in taskData.materials" :key="index" class="flex items-center justify-between bg-white px-4 py-3 rounded-lg border border-indigo-50 hover:border-indigo-200 transition-colors">
                <div class="flex items-center gap-3">
                  <div class="text-xl">{{ getFileIcon(file.type) }}</div>
                  <span class="text-sm font-medium text-gray-700">{{ file.name }}</span>
                </div>
                <button class="text-indigo-600 hover:text-indigo-800 text-sm font-bold flex items-center gap-1 bg-indigo-50 px-3 py-1.5 rounded">
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
                  下载
                </button>
              </div>
            </div>
          </div>

        </div>

        <div class="flex-[1] flex flex-col gap-4">
          
          <div class="flex-1 bg-gradient-to-b from-slate-800 to-slate-900 rounded-xl p-5 shadow-lg flex flex-col">
             <div class="flex items-center gap-2 mb-4 shrink-0">
                <svg class="w-5 h-5 text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
                <h3 class="text-white font-bold text-sm tracking-widest uppercase">AI 任务指引</h3>
             </div>

             <div class="mb-6 shrink-0 bg-white/10 rounded-lg p-3 border border-white/5">
                <p class="text-xs text-indigo-300 font-bold mb-1">难度与预计耗时评估</p>
                <p class="text-sm text-white font-medium">{{ taskData.aiDifficulty }}</p>
             </div>

             <div class="flex-1 overflow-y-auto custom-scrollbar-dark">
                <p class="text-xs text-indigo-300 font-bold mb-2">建议完成步骤</p>
                <div class="text-sm text-gray-300 leading-relaxed whitespace-pre-wrap">{{ taskData.aiSteps }}</div>
             </div>
          </div>

          <div class="shrink-0 pt-2">
            <button 
               @click="handleAccept"
               class="hero-send-btn w-full justify-center py-4 rounded-xl text-base font-bold shadow-lg transition-all flex items-center gap-2"
               :class="{
                 'opacity-50 grayscale cursor-not-allowed': isWaiting,
                 'hover:shadow-indigo-500/30': !isWaiting && (isTaskAccepted || isTeacherConfirmed)
               }"
               :disabled="isWaiting"
             >
               {{ isTeacherConfirmed ? '进入下一节点' : (isWaiting ? '等待教师进入下一节点' : '接收任务') }}
               <svg v-if="!isWaiting" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
               <svg v-else class="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
            </button>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const isTaskAccepted = ref(false)
const isWaiting = ref(false) // 是否正在等待教师确认
const isTeacherConfirmed = ref(false) // 教师是否已确认

// 核心数据模型：直接对应后端接口返回的简单字段
const taskData = reactive({
  title: 'Python 数组入门：学生成绩统计',
  deadline: '2026-06-30 23:59',
  requirements: `本次作业需要你掌握 Python 列表（List）的基础操作。
  
1. 读取代码片段中提供的学生成绩数组。
2. 使用内置函数找出成绩的最高分和最低分。
3. 将数组翻转，并使用切片语法 [0:3] 提取最后录入的3个成绩。
4. 运行代码，将终端的输出结果截图。
5. 将代码和截图粘贴到提供的 Word 模板中，并提交。`,
  materials: [
    { name: '作业提交说明模板.docx', type: 'word' },
    { name: 'score_practice.py', type: 'code' }
  ],
  aiDifficulty: '入门级，预计用时 15 分钟。',
  aiSteps: `1. 点击左侧下载 Word 模板和 Python 代码片段。
2. 使用 IDE 打开 score_practice.py。
3. 在注释提示的区域，编写 max()、min() 和切片代码。
4. 运行并检查终端输出是否正确。
5. 截图并保存至 Word 文档，点击下方按钮进入下一步。`
})

const getFileIcon = (type) => {
  if (type === 'word') return '📄'
  if (type === 'code') return '💻'
  return '📎'
}

const handleAccept = () => {
  if (isWaiting.value) return
  
  if (!isTeacherConfirmed.value) {
    // 第一次点击：标记完成，进入等待状态
    isTaskAccepted.value = true
    isWaiting.value = true
    
    // 模拟1秒后教师确认
    setTimeout(() => {
      isWaiting.value = false
      isTeacherConfirmed.value = true
    }, 1000)
  } else {
    // 教师确认后：进入下一节点
    router.push('/student/training/ai-study-card')
  }
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

.hero-send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -5px rgba(99, 102, 241, 0.4);
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

.custom-scrollbar-dark::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar-dark::-webkit-scrollbar-thumb {
  background: #475569;
  border-radius: 10px;
}
</style>