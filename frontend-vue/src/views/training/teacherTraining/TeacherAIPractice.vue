<template>
  <div style="height: 100vh;" class="p-6 bg-slate-50">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative overflow-hidden font-sans">
      
      <div class="flex justify-between items-center mb-6 shrink-0 border-b border-gray-200/50 pb-4">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase italic">Node: AI_SPARRING_MONITOR</div>
          <h2 class="text-2xl font-bold text-gray-800 flex items-center gap-2">AI 专注对练实时监控</h2>
          <p class="text-sm text-gray-500 mt-1">监控全班与 AI 的互动情况，查看苏格拉底式对话的引导轨迹与概念掌握度。</p>
        </div>
        
        <div class="flex items-center gap-4">
          <div class="px-4 py-2 bg-indigo-50/80 rounded-lg border border-indigo-100 flex items-center gap-3">
            <span class="text-sm text-indigo-600 font-medium">全班通关人数：</span>
            <span class="text-xl font-bold" :class="passedCount === totalStudents ? 'text-green-600' : 'text-indigo-600'">
              {{ passedCount }} <span class="text-sm text-indigo-400 font-normal">/ {{ totalStudents }}</span>
            </span>
          </div>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1.8] flex flex-col min-w-0 h-full border-r border-gray-200/60 pr-6">
          <div class="flex justify-between items-center mb-4 shrink-0">
            <h3 class="font-bold text-gray-700 flex items-center gap-2">
              <svg class="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" /></svg>
              学生对练状态矩阵
            </h3>
          </div>

          <div class="flex-1 overflow-y-auto custom-scrollbar pr-2">
            <div class="grid grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
              <div 
                v-for="student in students" 
                :key="student.id"
                @click="selectStudent(student)"
                class="p-4 rounded-xl border transition-all duration-300 cursor-pointer relative overflow-hidden flex flex-col items-center gap-3 group"
                :class="[
                  selectedStudent?.id === student.id 
                    ? 'bg-indigo-50/80 border-indigo-400 shadow-md transform scale-[1.02]' 
                    : 'bg-white/60 border-gray-100 hover:border-indigo-200 hover:bg-gray-50/80'
                ]"
              >
                <div v-if="student.passed" class="absolute top-2 right-2 text-green-500">
                  <svg class="w-5 h-5 drop-shadow-[0_0_5px_rgba(34,197,94,0.5)]" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                </div>
                <div v-else class="absolute top-2 right-2 text-indigo-300">
                  <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
                </div>

                <div class="relative">
                  <div 
                    class="w-14 h-14 rounded-full flex items-center justify-center text-white font-bold text-xl transition-all"
                    :class="student.passed ? 'bg-gradient-to-br from-green-400 to-emerald-500 shadow-[0_0_15px_rgba(52,211,153,0.4)]' : 'bg-gradient-to-br from-indigo-400 to-purple-500'"
                  >
                    {{ student.name.charAt(0) }}
                  </div>
                </div>
                
                <div class="text-center w-full">
                  <h4 class="text-sm font-bold text-gray-800" :class="{'text-green-600': student.passed}">{{ student.name }}</h4>
                  <div class="flex justify-center items-center gap-2 mt-1">
                    <span class="text-[10px] bg-gray-100 text-gray-500 px-1.5 py-0.5 rounded font-mono">{{ student.score }}分</span>
                    <span class="text-[10px] text-gray-400">{{ student.turns }}轮</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex-[1.2] flex flex-col h-full min-w-[340px] max-w-[420px]">
          
          <div v-if="!selectedStudent" class="flex-1 flex flex-col h-full animate-fade-in bg-white/60 border border-gray-200/60 rounded-2xl p-6 shadow-sm">
            <div class="flex items-center gap-2 mb-8 shrink-0">
              <svg class="w-6 h-6 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
              <h3 class="font-bold text-indigo-900 text-lg">全班对练数据分布</h3>
            </div>

            <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 space-y-8">
              
              <div>
                <h4 class="text-sm font-bold text-gray-600 mb-4 flex justify-between">
                  通关耗时分布
                  <span class="text-xs font-normal text-gray-400">平均 {{ avgTime }}</span>
                </h4>
                <div class="space-y-4">
                  <div v-for="(item, idx) in timeDistribution" :key="'time'+idx" class="relative">
                    <div class="flex justify-between text-xs text-gray-500 mb-1">
                      <span>{{ item.label }}</span>
                      <span class="font-bold">{{ item.count }} 人</span>
                    </div>
                    <div class="w-full bg-gray-100 rounded-full h-2 overflow-hidden">
                      <div class="h-full bg-indigo-400 rounded-full transition-all" :style="{ width: `${(item.count / totalStudents) * 100}%` }"></div>
                    </div>
                  </div>
                </div>
              </div>

              <div>
                <h4 class="text-sm font-bold text-gray-600 mb-4 flex justify-between">
                  对话轮数分布
                  <span class="text-xs font-normal text-gray-400">平均 {{ avgTurns }} 轮</span>
                </h4>
                <div class="space-y-4">
                  <div v-for="(item, idx) in turnsDistribution" :key="'turn'+idx" class="relative">
                    <div class="flex justify-between text-xs text-gray-500 mb-1">
                      <span>{{ item.label }}</span>
                      <span class="font-bold">{{ item.count }} 人</span>
                    </div>
                    <div class="w-full bg-gray-100 rounded-full h-2 overflow-hidden">
                      <div class="h-full bg-purple-400 rounded-full transition-all" :style="{ width: `${(item.count / totalStudents) * 100}%` }"></div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="p-4 bg-indigo-50 border border-indigo-100 rounded-xl mt-4">
                <p class="text-xs text-indigo-600 leading-relaxed italic">
                  💡 提示：点击左侧任意学生卡片，即可下钻查看该生的详细 AI 对话引导轨迹与知识点掌握情况。
                </p>
              </div>

            </div>
          </div>

          <div v-else class="flex-1 flex flex-col h-full animate-fade-in bg-white/60 border border-gray-200/60 rounded-2xl shadow-sm overflow-hidden">
            
            <div class="p-5 border-b border-gray-100 bg-gradient-to-b from-gray-50 to-white shrink-0">
              <button @click="selectedStudent = null" class="text-xs font-bold text-indigo-500 hover:text-indigo-700 flex items-center gap-1 mb-4 transition-colors">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
                返回班级大盘
              </button>
              
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-full flex items-center justify-center text-white font-bold bg-indigo-500 shadow-sm">
                    {{ selectedStudent.name.charAt(0) }}
                  </div>
                  <div>
                    <h3 class="font-bold text-gray-800">{{ selectedStudent.name }}</h3>
                    <span class="text-[10px] px-2 py-0.5 rounded font-bold mt-1 inline-block" :class="selectedStudent.passed ? 'bg-green-100 text-green-700' : 'bg-orange-100 text-orange-600'">
                      {{ selectedStudent.passed ? '已通关' : '对练中' }}
                    </span>
                  </div>
                </div>
                
                <div class="flex gap-4 text-center">
                  <div>
                    <p class="text-[10px] text-gray-400">综合得分</p>
                    <p class="font-bold" :class="selectedStudent.passed ? 'text-green-600' : 'text-indigo-600'">{{ selectedStudent.score }}</p>
                  </div>
                  <div>
                    <p class="text-[10px] text-gray-400">耗时</p>
                    <p class="font-bold text-gray-700">{{ selectedStudent.time }}</p>
                  </div>
                  <div>
                    <p class="text-[10px] text-gray-400">轮数</p>
                    <p class="font-bold text-gray-700">{{ selectedStudent.turns }}</p>
                  </div>
                </div>
              </div>
            </div>

            <div class="flex-1 overflow-y-auto p-5 space-y-5 custom-scrollbar bg-gray-50/50">
              <div 
                v-for="(msg, index) in selectedStudent.chatHistory" 
                :key="index"
                class="flex"
                :class="msg.role === 'user' ? 'justify-end' : 'justify-start'"
              >
                <div v-if="msg.role === 'ai'" class="flex gap-3 max-w-[85%]">
                  <div class="w-7 h-7 rounded-full bg-indigo-100 flex items-center justify-center shrink-0 mt-1">
                    <svg class="w-4 h-4 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
                  </div>
                  <div>
                    <div class="bg-white border border-gray-100 text-gray-700 px-4 py-3 rounded-2xl rounded-tl-sm text-xs leading-relaxed whitespace-pre-wrap shadow-sm">
                      {{ msg.content }}
                    </div>
                    <div v-if="msg.scoreDelta" class="mt-1 ml-1 text-[10px] font-bold flex items-center gap-1" :class="msg.scoreDelta > 0 ? 'text-green-600' : 'text-red-500'">
                      表现评分 {{ msg.scoreDelta > 0 ? '+' : '' }}{{ msg.scoreDelta }}
                    </div>
                  </div>
                </div>

                <div v-else class="flex gap-3 max-w-[85%] flex-row-reverse">
                  <div class="w-7 h-7 rounded-full bg-indigo-500 flex items-center justify-center shrink-0 text-white text-[10px] font-bold mt-1">生</div>
                  <div class="bg-indigo-500 text-white px-4 py-3 rounded-2xl rounded-tr-sm text-xs leading-relaxed whitespace-pre-wrap shadow-sm">
                    {{ msg.content }}
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

const totalStudents = ref(32)

// 班级统计分布数据
const avgTime = '08m 15s'
const avgTurns = 5

const timeDistribution = [
  { label: '5分钟以内 (快速)', count: 12 },
  { label: '5-10分钟 (正常)', count: 15 },
  { label: '10分钟以上 (卡壳)', count: 5 }
]

const turnsDistribution = [
  { label: '3-4轮 (精准答对)', count: 10 },
  { label: '5-8轮 (启发引导)', count: 18 },
  { label: '8轮以上 (重点关注)', count: 4 }
]

// 模拟学生对练数据与聊天记录
const students = ref([
  { 
    id: 1, name: '张伟', passed: true, score: 90, time: '05m 20s', turns: 4,
    chatHistory: [
      { role: 'ai', content: '我们在上一节学习了数组的基础概念。现在考考你：如果你尝试访问一个长度为 5 的列表的第 10 个元素，Python 会作何反应？', scoreDelta: null },
      { role: 'user', content: '会越界，报错。' },
      { role: 'ai', content: '思路正确。能说出具体的报错类型吗？', scoreDelta: 0 },
      { role: 'user', content: 'IndexError 异常。' },
      { role: 'ai', content: '回答得非常好！那在实际编写代码时，为了避免整个程序因为这条语句崩溃，我们通常会使用什么语法来进行边界防护？', scoreDelta: +20 },
      { role: 'user', content: '用 try...except 捕获，或者先用 len() 判断一下长度。' },
      { role: 'ai', content: '完美！概念已全部掌握，可以通关了。', scoreDelta: +20 }
    ]
  },
  { 
    id: 2, name: '李娜', passed: true, score: 85, time: '06m 40s', turns: 5, chatHistory: [] 
  },
  { 
    id: 3, name: '王磊', passed: false, score: 60, time: '12m 10s', turns: 8,
    chatHistory: [
      { role: 'ai', content: '如果你尝试访问一个长度为 5 的列表的第 10 个元素，Python 会作何反应？', scoreDelta: null },
      { role: 'user', content: '返回空值 null 或者 undefined 吧？' },
      { role: 'ai', content: '不对哦，Python 不像 JavaScript 会返回 undefined。请再仔细想想，它会抛出什么样的具体错误？', scoreDelta: -10 },
      { role: 'user', content: '那就是编译错误？程序跑不起来？' },
      { role: 'ai', content: 'Python 是动态解释型语言，这里的错误是在运行时发生的。想一想，当你访问不存在的索引时，它的名字叫什么？', scoreDelta: -5 },
      { role: 'user', content: 'IndexError！' },
      { role: 'ai', content: '终于答对了！为了避免这个报错导致程序崩溃，你会怎么做边界防护？', scoreDelta: +15 }
    ]
  },
  { id: 4, name: '刘洋', passed: true, score: 95, time: '04m 10s', turns: 3, chatHistory: [] },
  { id: 5, name: '陈静', passed: true, score: 100, time: '03m 50s', turns: 2, chatHistory: [] },
  { id: 6, name: '杨帆', passed: false, score: 50, time: '08m 20s', turns: 6, chatHistory: [] },
  { id: 7, name: '赵云', passed: true, score: 90, time: '05m 50s', turns: 4, chatHistory: [] },
  { id: 8, name: '孙颖', passed: true, score: 85, time: '07m 10s', turns: 5, chatHistory: [] },
  { id: 9, name: '周杰', passed: false, score: 70, time: '09m 45s', turns: 7, chatHistory: [] },
  { id: 10, name: '吴敏', passed: true, score: 95, time: '04m 30s', turns: 3, chatHistory: [] },
  { id: 11, name: '郑强', passed: true, score: 80, time: '08m 00s', turns: 6, chatHistory: [] },
  { id: 12, name: '王芳', passed: false, score: 40, time: '14m 15s', turns: 9, chatHistory: [] },
  { id: 13, name: '冯涛', passed: true, score: 100, time: '03m 40s', turns: 2, chatHistory: [] },
  { id: 14, name: '陈思', passed: true, score: 90, time: '05m 15s', turns: 4, chatHistory: [] },
  { id: 15, name: '李明', passed: true, score: 85, time: '06m 50s', turns: 5, chatHistory: [] },
  { id: 16, name: '林峰', passed: true, score: 95, time: '04m 45s', turns: 3, chatHistory: [] }
])

const passedCount = computed(() => students.value.filter(s => s.passed).length)

const selectedStudent = ref(null)

const selectStudent = (student) => {
  // 如果学生没有模拟的对话数据，自动生成一点用于演示
  if (!student.chatHistory || student.chatHistory.length === 0) {
    student.chatHistory = [
      { role: 'ai', content: '你好，准备好开始对练了吗？', scoreDelta: null },
      { role: 'user', content: '准备好了' },
      { role: 'ai', content: '很好，请解释一下为什么 Python 列表的 append 操作通常比 insert(0) 快？', scoreDelta: null }
    ]
  }
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
  animation: fadeIn 0.3s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateX(10px); }
  to { opacity: 1; transform: translateX(0); }
}
</style>