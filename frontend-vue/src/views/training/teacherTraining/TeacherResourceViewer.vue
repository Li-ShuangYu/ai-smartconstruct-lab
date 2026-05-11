<template>
  <div style="height: 100vh;" class="p-6 bg-slate-50">
    <div class="glass-card w-full h-full p-6 flex gap-6 z-10 overflow-hidden">
      
      <main class="flex-[3] flex flex-col h-full min-w-0 border-r border-gray-200/60 pr-6">
        
        <div class="mb-4 shrink-0 flex justify-between items-end">
          <div>
            <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: READING_MONITOR</div>
            <h2 class="text-2xl font-bold text-gray-800">资源阅读节点监控台</h2>
            <p class="text-sm text-gray-500 mt-2">实时监控学生课件阅读进度、时长及 AI 伴学提问情况。</p>
          </div>
          <div class="flex gap-4">
            <div class="px-4 py-2 bg-indigo-50/80 rounded-lg border border-indigo-100 flex items-center gap-3">
              <span class="text-sm text-indigo-600 font-medium">全班阅读达标人数：</span>
              <span class="text-xl font-bold text-indigo-700">{{ completedStudentsCount }}<span class="text-sm text-indigo-400 font-normal"> / {{ students.length }}</span></span>
            </div>
          </div>
        </div>

        <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 mt-2">
          <div class="grid grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
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
              <div v-if="student.progress >= 100" class="absolute top-2 right-2 text-green-500">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              </div>

              <div class="relative">
                <div 
                  class="w-14 h-14 rounded-full flex items-center justify-center text-white font-bold text-xl shadow-sm transition-all"
                  :class="student.progress >= 100 ? 'bg-gradient-to-br from-green-400 to-emerald-500 shadow-green-500/30' : (student.progress > 0 ? 'bg-gradient-to-br from-indigo-400 to-purple-500 shadow-indigo-500/30' : 'bg-gray-300 text-gray-500')"
                >
                  {{ student.name.charAt(0) }}
                </div>
              </div>
              
              <div class="text-center w-full">
                <h4 class="text-sm font-bold text-gray-800">{{ student.name }}</h4>
                <div class="flex justify-between items-center w-full mt-2 mb-1 px-1">
                  <span class="text-[10px] text-gray-500">{{ student.readTime }}</span>
                  <span class="text-[10px] font-bold" :class="student.progress >= 100 ? 'text-green-600' : 'text-indigo-600'">{{ student.progress }}%</span>
                </div>
                <div class="w-full h-1.5 bg-gray-200 rounded-full overflow-hidden">
                  <div 
                    class="h-full transition-all duration-500"
                    :class="student.progress >= 100 ? 'bg-green-500' : 'bg-indigo-500'"
                    :style="{ width: `${student.progress}%` }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      <aside class="w-[360px] shrink-0 flex flex-col h-full">
        
        <div v-if="!selectedStudent" class="flex-1 flex flex-col h-full animate-fade-in">
          <div class="flex items-center gap-2 mb-6 shrink-0">
            <svg class="w-6 h-6 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
            <h3 class="font-bold text-indigo-900 text-lg">全班阅读数据监控</h3>
          </div>

          <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 space-y-6">
            <div class="grid grid-cols-2 gap-4">
              <div class="bg-gradient-to-br from-indigo-50 to-white border border-indigo-100 p-4 rounded-xl shadow-sm text-center">
                <div class="text-xs text-gray-500 font-bold mb-1">平均阅读时长</div>
                <div class="text-2xl font-black text-indigo-600">24<span class="text-sm font-normal text-gray-500 ml-1">min</span></div>
              </div>
              <div class="bg-gradient-to-br from-purple-50 to-white border border-purple-100 p-4 rounded-xl shadow-sm text-center">
                <div class="text-xs text-gray-500 font-bold mb-1">AI 提问总人次</div>
                <div class="text-2xl font-black text-purple-600">128<span class="text-sm font-normal text-gray-500 ml-1">次</span></div>
              </div>
            </div>

            <div class="bg-white/60 border border-gray-200 p-5 rounded-xl">
              <h4 class="text-sm font-bold text-gray-700 mb-3 flex items-center gap-2">
                <svg class="w-4 h-4 text-orange-500" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2L1 21h22M12 6l7.5 13h-15M11 10h2v5h-2M11 16h2v2h-2"/></svg>
                本节课高频疑问 (AI 提取)
              </h4>
              <div class="flex flex-wrap gap-2 mt-2">
                <span v-for="(q, idx) in globalHighFreqQuestions" :key="idx" class="text-xs px-3 py-1.5 bg-orange-50 border border-orange-100 text-orange-700 rounded-full cursor-pointer hover:bg-orange-100 transition-colors">
                  {{ q }}
                </span>
              </div>
              <p class="text-[10px] text-gray-400 mt-4 leading-relaxed bg-gray-50 p-2 rounded border border-gray-100">
                💡 AI 洞察：超过 40% 的学生在“多维数组内存映射”部分停留时间较长并产生密集提问，建议后续实训加强该知识点演示。
              </p>
            </div>
          </div>
          
          <div class="mt-4 pt-4 border-t border-gray-200 shrink-0 text-center">
            <p class="text-sm text-gray-400">请在左侧点击具体学生查看详情</p>
          </div>
        </div>

        <div v-else class="flex-1 flex flex-col h-full animate-fade-in">
          <div class="shrink-0 mb-4">
            <button @click="selectedStudent = null" class="text-xs font-bold text-gray-500 hover:text-indigo-600 flex items-center gap-1 mb-4 transition-colors">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
              返回全班视图
            </button>
            
            <div class="flex items-center gap-4 bg-white/60 p-4 rounded-xl border border-gray-200 shadow-sm">
              <div class="w-12 h-12 rounded-full flex items-center justify-center text-white font-bold text-lg bg-gradient-to-br from-indigo-400 to-purple-500 shadow-md">
                {{ selectedStudent.name.charAt(0) }}
              </div>
              <div>
                <h3 class="font-bold text-gray-800 text-lg">{{ selectedStudent.name }}</h3>
                <p class="text-xs text-gray-500 mt-0.5">阅读时长: <span class="font-bold text-gray-700">{{ selectedStudent.readTime }}</span></p>
              </div>
            </div>
          </div>

          <div class="flex-1 flex flex-col min-h-0 bg-white/50 border border-gray-200 rounded-xl overflow-hidden shadow-sm">
            <div class="bg-gray-50/80 border-b border-gray-200 px-4 py-2.5 flex justify-between items-center shrink-0">
              <span class="text-xs font-bold text-gray-600 flex items-center gap-1.5">
                <svg class="w-4 h-4 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" /></svg>
                AI 伴学提问记录
              </span>
              <span class="text-[10px] bg-purple-100 text-purple-700 px-2 py-0.5 rounded-full font-bold">{{ selectedStudent.questions.length }} 条</span>
            </div>
            
            <div class="flex-1 overflow-y-auto custom-scrollbar p-4 space-y-4">
              <div v-if="selectedStudent.questions.length === 0" class="h-full flex items-center justify-center text-sm text-gray-400 italic">
                该生暂无提问记录
              </div>
              
              <div v-for="(qa, idx) in selectedStudent.questions" :key="idx" class="space-y-2">
                <div class="flex flex-col items-end">
                  <div class="bg-indigo-500 text-white text-xs px-3 py-2 rounded-2xl rounded-tr-sm max-w-[85%] shadow-sm">
                    {{ qa.q }}
                  </div>
                  <span class="text-[9px] text-gray-400 mt-1 mr-1">{{ qa.time }}</span>
                </div>
                <div class="flex flex-col items-start">
                  <div class="bg-gray-100 text-gray-700 text-xs px-3 py-2 rounded-2xl rounded-tl-sm max-w-[90%] border border-gray-200">
                    <span class="font-bold text-purple-600 mr-1">AI:</span>{{ qa.a }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="shrink-0 mt-4 bg-white/60 p-4 rounded-xl border border-gray-200 shadow-sm space-y-3">
            <div class="flex justify-between items-center text-sm">
              <span class="font-bold text-gray-700">当前系统判定进度：</span>
              <span class="font-bold text-lg" :class="selectedStudent.progress >= 100 ? 'text-green-600' : 'text-indigo-600'">{{ selectedStudent.progress }}%</span>
            </div>
            
            <div class="flex gap-3 pt-2">
              <button 
                @click="forceComplete(selectedStudent)"
                :disabled="selectedStudent.progress >= 100"
                class="flex-1 py-2.5 rounded-lg text-sm font-bold transition-all border flex justify-center items-center gap-1"
                :class="selectedStudent.progress >= 100 
                  ? 'bg-gray-100 text-gray-400 border-gray-200 cursor-not-allowed' 
                  : 'bg-green-50 text-green-700 border-green-200 hover:bg-green-100 hover:border-green-300 active:scale-95 shadow-sm'"
              >
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                强制标为完成
              </button>
              
              <button 
                @click="resetProgress(selectedStudent)"
                class="px-4 py-2.5 rounded-lg text-sm font-bold bg-white text-red-500 border border-red-200 hover:bg-red-50 active:scale-95 transition-all shadow-sm"
                title="清空该生阅读进度"
              >
                重置
              </button>
            </div>
          </div>
        </div>

      </aside>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 模拟学生名单及提问数据
const students = ref([
  { id: 1, name: '张伟', progress: 100, readTime: '32m 15s', questions: [
    { q: 'Python 列表的底层是链表还是数组？', a: 'Python 列表底层实现是动态数组（C语言中的指针数组），并不是链表。', time: '10:05' },
    { q: '那它 append 的时候会频繁分配内存吗？', a: '不会，它采用过度分配（Over-allocation）策略，空间不够时会一次性申请更大的内存块。', time: '10:08' }
  ]},
  { id: 2, name: '李娜', progress: 100, readTime: '28m 40s', questions: [] },
  { id: 3, name: '王磊', progress: 85, readTime: '22m 10s', questions: [
    { q: 'PEP8 规范里单行代码最大长度是多少？', a: 'PEP8 建议单行代码最大长度限制为 79 个字符。', time: '10:15' }
  ]},
  { id: 4, name: '刘洋', progress: 45, readTime: '12m 05s', questions: [] },
  { id: 5, name: '陈静', progress: 100, readTime: '35m 50s', questions: [
    { q: 'Docker 拉取镜像一直超时怎么办？', a: '建议配置国内镜像加速器，如阿里云、清华源等，在 daemon.json 中修改 registry-mirrors。', time: '09:55' }
  ]},
  { id: 6, name: '杨帆', progress: 20, readTime: '05m 30s', questions: [] },
  { id: 7, name: '赵云', progress: 100, readTime: '40m 10s', questions: [] },
  { id: 8, name: '孙颖', progress: 95, readTime: '25m 00s', questions: [
    { q: 'PPT里的内存视图中，为什么地址不是连续递增的？', a: '因为Python列表中存储的是对象的引用（指针），指针本身在数组中是连续存放的，但它们指向的实际对象在堆内存中可以是分散的。', time: '10:20' }
  ]},
  { id: 9, name: '周杰', progress: 60, readTime: '15m 45s', questions: [] },
  { id: 10, name: '吴敏', progress: 100, readTime: '31m 20s', questions: [] },
  { id: 11, name: '郑强', progress: 100, readTime: '29m 10s', questions: [] },
  { id: 12, name: '王芳', progress: 10, readTime: '02m 15s', questions: [] },
  { id: 13, name: '冯涛', progress: 100, readTime: '30m 00s', questions: [] },
  { id: 14, name: '陈思', progress: 100, readTime: '27m 45s', questions: [] },
  { id: 15, name: '李明', progress: 75, readTime: '18m 20s', questions: [] },
  { id: 16, name: '林峰', progress: 100, readTime: '33m 30s', questions: [] }
])

const globalHighFreqQuestions = [
  '列表底层原理', '动态扩容机制', 'Docker 镜像加速配置', 'PEP8 代码行数限制', '深浅拷贝的区别'
]

const selectedStudent = ref(null)

const completedStudentsCount = computed(() => {
  return students.value.filter(s => s.progress >= 100).length
})

const selectStudent = (student) => {
  selectedStudent.value = student
}

// 教师主控操作
const forceComplete = (student) => {
  if (confirm(`确认将【${student.name}】的阅读进度强制标为完成吗？`)) {
    student.progress = 100
  }
}

const resetProgress = (student) => {
  if (confirm(`确认重置【${student.name}】的阅读进度吗？这会导致该生需要重新阅读。`)) {
    student.progress = 0
    student.readTime = '0m 0s'
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

/* 简单的淡入动画 */
.animate-fade-in {
  animation: fadeIn 0.3s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateX(10px); }
  to { opacity: 1; transform: translateX(0); }
}
</style>