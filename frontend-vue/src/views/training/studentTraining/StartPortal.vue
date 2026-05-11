<template>
  <div class="training-container p-6 h-full flex flex-col gap-6 font-sans text-slate-800 dark:text-slate-200">
    <div class="flex justify-between items-end">
      <div>
        <div class="text-xs font-bold text-indigo-400 tracking-widest uppercase mb-1">NODE: START</div>
        <h1 class="text-3xl font-bold text-gradient-primary italic shrink-0 pr-2">Python 数组实训</h1>
      </div>
      <div class="flex items-center gap-4">
        <div class="px-4 py-2 bg-indigo-50 dark:bg-indigo-900/20 border border-indigo-100 dark:border-indigo-800/50 rounded-lg">
          <span class="text-sm text-indigo-600 dark:text-indigo-400 font-medium">班级就位人数：</span>
          <span class="text-xl font-bold text-indigo-600">{{ readyCount }}</span>
          <span class="text-sm text-slate-400"> / {{ totalCount }}</span>
        </div>
      </div>
    </div>

    <div class="flex flex-1 gap-6 overflow-hidden">
      <div class="flex-[1.5] flex flex-col gap-6 overflow-y-auto pr-2">
        <div class="glass-card p-6 border-l-4 border-l-indigo-500 relative overflow-hidden">
          <div class="absolute -right-4 -top-4 opacity-10">
            <svg width="120" height="120" viewBox="0 0 24 24" fill="currentColor">
              <path d="M21 11.5a8.38 8.38 0 01-.9 3.8 8.5 8.5 0 01-7.6 4.7 8.38 8.38 0 01-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 01-.9-3.8 8.5 8.5 0 014.7-7.6 8.38 8.38 0 013.8-.9h.5a8.48 8.48 0 018 8v.5z" />
            </svg>
          </div>
          <div class="flex items-center gap-2 mb-3">
            <div class="p-1.5 bg-indigo-500 rounded-lg text-white">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <h3 class="font-bold text-lg">AI 导师欢迎您</h3>
          </div>
          <p class="text-slate-600 dark:text-slate-300 leading-relaxed italic text-lg">
            “{{ aiWelcomeMessage }}”
          </p>
        </div>

        <div class="glass-card flex-1 p-6 flex flex-col min-h-[300px]">
          <div class="flex justify-between items-center mb-4">
            <h3 class="font-bold">班级就位情况</h3>
            <div class="px-3 py-1 bg-indigo-50 dark:bg-indigo-900/20 border border-indigo-100 dark:border-indigo-800/50 rounded-lg text-sm">
              <span class="text-indigo-600 dark:text-indigo-400 font-medium">班级就位人数：</span>
              <span class="font-bold text-indigo-600">{{ readyCount }}</span>
              <span class="text-slate-400"> / {{ totalCount }}</span>
            </div>
          </div>
          <div class="flex-1 overflow-y-auto custom-scrollbar pr-2">
            <div class="grid grid-cols-4 gap-3">
              <div v-for="student in students" :key="student.id" class="flex flex-col items-center gap-2 p-3 rounded-xl transition-all duration-300"
                :class="student.ready 
                  ? 'bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800/40 shadow-[0_0_15px_rgba(34,197,94,0.15)]' 
                  : 'bg-slate-50 dark:bg-slate-900/30 border border-slate-200 dark:border-slate-700/50'">
                <div class="relative">
                  <div class="w-12 h-12 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 flex items-center justify-center text-white font-bold text-lg shadow-lg"
                    :class="student.ready ? 'shadow-green-500/30 shadow-xl' : 'grayscale opacity-60'">
                    {{ student.name.charAt(0) }}
                  </div>
                  <div v-if="student.ready" class="absolute -bottom-0.5 -right-0.5 w-4 h-4 bg-green-500 rounded-full border-2 border-white dark:border-slate-900 animate-pulse"></div>
                  <div v-else class="absolute -bottom-0.5 -right-0.5 w-4 h-4 bg-slate-300 dark:bg-slate-600 rounded-full border-2 border-white dark:border-slate-900"></div>
                </div>
                <span class="text-xs font-medium text-center truncate w-full"
                  :class="student.ready ? 'text-green-700 dark:text-green-400' : 'text-slate-400'">
                  {{ student.name }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="flex-1 flex flex-col gap-6">
        <div class="glass-card flex-1 p-6 flex flex-col overflow-hidden">
          <h3 class="font-bold mb-6 flex items-center gap-2">
            <svg class="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16m-7 6h7" />
            </svg>
            实训节点流程
            <span class="ml-auto text-xs text-slate-400 font-normal">实训节点总数：{{ nodes.length }}</span>
          </h3>
          <div class="flex-1 overflow-y-auto custom-scrollbar pr-4">
            <div class="relative border-l-2 border-slate-100 dark:border-slate-800 ml-3 space-y-8">
              <div v-for="(node, index) in nodes" :key="index" class="relative pl-8">
                <div 
                  class="absolute left-[-9px] top-1 w-4 h-4 rounded-full border-2 transition-all duration-300"
                  :class="[
                    index === 0 ? 'bg-indigo-500 border-indigo-200 scale-125 ring-4 ring-indigo-500/20' : 'bg-white dark:bg-slate-900 border-slate-300 dark:border-slate-700'
                  ]"
                ></div>
                <div :class="{'text-indigo-600 font-bold': index === 0, 'text-slate-500': index !== 0}">
                  <div class="text-sm">{{ node }}</div>
                  <div v-if="index === 0" class="text-[10px] uppercase tracking-tighter opacity-70">Current Node</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="glass-card p-6 flex flex-col gap-4">
          
          
          <button 
            @click="handleReady"
            :disabled="isReady"
            class="hero-send-btn w-full justify-center py-4 rounded-xl text-lg font-bold shadow-xl transition-all active:scale-95 flex items-center gap-3"
            :class="{'opacity-50 grayscale cursor-not-allowed': isReady}"
          >
            <svg v-if="!isReady" class="w-6 h-6 animate-pulse" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
            <svg v-else class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            {{ isReady ? '等待导师开启实训...' : '准备就绪' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

const nodes = [
  '开始实训', '课件阅读', '视频观看', '思维导图预习与评价', '任务下发',
  '理论实训', 'AI 对练', '思维导图绘制练习', '需求上传', '方案上传',
  '编码实训', '课后作业', '学生匿名互评', '教师点评', '结束实训'
]

const readyCount = ref(24)
const totalCount = ref(32)
const isReady = ref(false)

const students = [
  { id: 1, name: '张伟', ready: true },
  { id: 2, name: '李娜', ready: true },
  { id: 3, name: '王磊', ready: true },
  { id: 4, name: '刘洋', ready: false },
  { id: 5, name: '陈静', ready: true },
  { id: 6, name: '杨帆', ready: false },
  { id: 7, name: '赵云', ready: true },
  { id: 8, name: '孙颖', ready: true },
  { id: 9, name: '周杰', ready: false },
  { id: 10, name: '吴敏', ready: true },
  { id: 11, name: '郑强', ready: true },
  { id: 12, name: '王芳', ready: false },
  { id: 13, name: '冯涛', ready: true },
  { id: 14, name: '陈思', ready: true },
  { id: 15, name: '李明', ready: false },
  { id: 16, name: '林峰', ready: true },
  { id: 17, name: '黄丽', ready: false },
  { id: 18, name: '许杰', ready: true },
  { id: 19, name: '何琳', ready: true },
  { id: 20, name: '吕晨', ready: false },
  { id: 21, name: '施雨', ready: true },
  { id: 22, name: '张辉', ready: true },
  { id: 23, name: '朱婷', ready: false },
  { id: 24, name: '秦浩', ready: true },
  { id: 25, name: '许晴', ready: false },
  { id: 26, name: '戚帅', ready: true },
  { id: 27, name: '谢晨', ready: true },
  { id: 28, name: '邹雨', ready: false },
  { id: 29, name: '柏晨', ready: true },
  { id: 30, name: '韦昌', ready: false },
  { id: 31, name: '汪磊', ready: true },
  { id: 32, name: '沈颖', ready: false },
]

// AI 生成的个性化欢迎语
const aiWelcomeMessage = ref("你好，同学！欢迎来到《Python 数组实训》环节。数组（Array）是编程世界的基石，掌握它将为你开启高效数据处理的大门。今天，我们将从零构建你的第一个动态列表，准备好与这些方括号 [] 深度共鸣了吗？")

const handleReady = () => {
  if (isReady.value) return
  isReady.value = true
  readyCount.value++
}

onMounted(() => {
  // 模拟 AI 打印效果
  const fullText = aiWelcomeMessage.value
  aiWelcomeMessage.value = ''
  let i = 0
  const timer = setInterval(() => {
    aiWelcomeMessage.value += fullText[i]
    i++
    if (i >= fullText.length) clearInterval(timer)
  }, 30)
})
</script>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 1.5rem;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.07);
}

.dark .glass-card {
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.text-gradient-primary {
  background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-send-btn {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: white;
}

.hero-send-btn:hover {
  box-shadow: 0 10px 20px -5px rgba(99, 102, 241, 0.5);
  transform: translateY(-2px);
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}
.dark .custom-scrollbar::-webkit-scrollbar-thumb {
  background: #334155;
}
</style>