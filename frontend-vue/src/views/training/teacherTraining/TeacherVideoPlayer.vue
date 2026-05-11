<template>
  <div style="height: 100vh;" class="p-6 bg-slate-50">
    <div class="glass-card w-full h-full p-6 flex gap-6 z-10 overflow-hidden">
      
      <main class="flex-[3] flex flex-col h-full min-w-0 border-r border-gray-200/60 pr-6">
        
        <div class="mb-4 shrink-0 flex justify-between items-end">
          <div>
            <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: VIDEO_MONITOR</div>
            <h2 class="text-2xl font-bold text-gray-800">视频观看监控台</h2>
            <p class="text-sm text-gray-500 mt-2">实时监控全班学生的视频观看进度与累计观看时长。</p>
          </div>
          <div class="flex gap-4">
            <div class="px-4 py-2 bg-indigo-50/80 rounded-lg border border-indigo-100 flex items-center gap-3 shadow-sm">
              <span class="text-sm text-indigo-600 font-medium">全班已达标人数：</span>
              <span class="text-xl font-bold text-indigo-700">
                {{ completedStudentsCount }}
                <span class="text-sm text-indigo-400 font-normal"> / {{ students.length }}</span>
              </span>
            </div>
          </div>
        </div>

        <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 mt-2">
          <div class="grid grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-5">
            <div 
              v-for="student in students" 
              :key="student.id"
              class="p-4 rounded-xl border border-gray-100 transition-all duration-300 relative overflow-hidden flex flex-col items-center gap-3 bg-white/60 hover:bg-gray-50/80"
            >
              
              <div v-if="student.progress >= 100" class="absolute top-2 right-2 text-green-500 drop-shadow-[0_0_5px_rgba(34,197,94,0.8)]">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              </div>

              <div class="relative mt-2">
                <div 
                  class="w-14 h-14 rounded-full flex items-center justify-center text-white font-bold text-xl transition-all duration-500 z-10 relative"
                  :class="student.progress >= 100 
                    ? 'bg-gradient-to-br from-green-400 to-emerald-500 shadow-[0_0_15px_rgba(52,211,153,0.6)] ring-2 ring-green-200' 
                    : 'bg-gradient-to-br from-indigo-400 to-purple-500 shadow-sm'"
                >
                  {{ student.name.charAt(0) }}
                </div>
                <div v-if="student.progress >= 100" class="absolute inset-0 bg-green-400 rounded-full blur-md opacity-40 animate-pulse z-0"></div>
              </div>
              
              <div class="text-center w-full">
                <h4 
                  class="text-sm font-bold transition-colors duration-300"
                  :class="student.progress >= 100 ? 'text-green-600 drop-shadow-[0_0_8px_rgba(74,222,128,0.5)]' : 'text-gray-800'"
                >
                  {{ student.name }}
                </h4>
                
                <div class="flex justify-between items-center w-full mt-2 mb-1 px-1">
                  <span class="text-[10px] text-gray-500">{{ student.watchTime }}</span>
                  <span class="text-[10px] font-bold" :class="student.progress >= 100 ? 'text-green-600' : 'text-indigo-600'">{{ student.progress }}%</span>
                </div>

                <div class="w-full h-1.5 bg-gray-200 rounded-full overflow-hidden">
                  <div 
                    class="h-full transition-all duration-500"
                    :class="student.progress >= 100 ? 'bg-green-500 shadow-[0_0_10px_rgba(34,197,94,0.8)]' : 'bg-indigo-500'"
                    :style="{ width: `${student.progress}%` }"
                  ></div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </main>

      <aside class="w-[360px] shrink-0 flex flex-col h-full">
        
        <div class="flex items-center gap-2 mb-6 shrink-0">
          <svg class="w-6 h-6 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
          <h3 class="font-bold text-indigo-900 text-lg">大盘统计</h3>
        </div>

        <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 flex flex-col gap-6">
          
          <div class="grid grid-cols-2 gap-4 shrink-0">
            <div class="bg-gradient-to-br from-indigo-50 to-white border border-indigo-100 p-4 rounded-xl shadow-sm text-center">
              <div class="text-xs text-gray-500 font-bold mb-1">平均观看时长</div>
              <div class="text-2xl font-black text-indigo-600">{{ averageWatchTime }}<span class="text-sm font-normal text-gray-500 ml-1">min</span></div>
            </div>
            <div class="bg-gradient-to-br from-orange-50 to-white border border-orange-100 p-4 rounded-xl shadow-sm text-center">
              <div class="text-xs text-gray-500 font-bold mb-1">未完成人数</div>
              <div class="text-2xl font-black text-orange-600">{{ incompleteStudentsCount }}<span class="text-sm font-normal text-gray-500 ml-1">人</span></div>
            </div>
          </div>

          <div class="flex-1 flex flex-col bg-white/50 border border-gray-200 rounded-xl overflow-hidden shadow-sm p-5">
            <h4 class="text-sm font-bold text-gray-700 mb-4 flex items-center gap-2">
              <svg class="w-4 h-4 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5.882V19.24a1.76 1.76 0 01-3.417.592l-2.147-6.15M18 13a3 3 0 100-6M5.436 13.683A4.001 4.001 0 017 6h1.832c4.1 0 7.625-1.234 9.168-3v14c-1.543-1.766-5.067-3-9.168-3H7a3.988 3.988 0 01-1.564-.317z" /></svg>
              下发系统通知
            </h4>
            
            <textarea 
              v-model="broadcastMessage"
              placeholder="请输入要通知的内容，例如：请抓紧时间观看视频，本节课即将结束。"
              class="w-full flex-1 min-h-[120px] p-4 text-sm border border-gray-200 rounded-xl bg-white/80 focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all resize-none mb-5 custom-scrollbar"
            ></textarea>
            
            <div class="flex flex-col gap-3 shrink-0">
              <button 
                @click="sendReminder('incomplete')"
                :disabled="incompleteStudentsCount === 0"
                class="w-full py-3 rounded-xl text-sm font-bold transition-all border flex justify-center items-center gap-2 shadow-sm"
                :class="incompleteStudentsCount === 0 
                  ? 'bg-gray-100 text-gray-400 border-gray-200 cursor-not-allowed' 
                  : 'bg-orange-50 text-orange-700 border-orange-200 hover:bg-orange-100 active:scale-95'"
              >
                发给未完成学生 ({{ incompleteStudentsCount }}人)
              </button>
              
              <button 
                @click="sendReminder('all')"
                class="hero-send-btn w-full py-3.5 rounded-xl text-sm font-bold transition-all active:scale-95 shadow-lg flex justify-center items-center gap-2"
              >
                发给全班所有人
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" /></svg>
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

// 简化后的学生数据（仅保留基础信息、观看进度和时长）
const students = ref([
  { id: 1, name: '张伟', progress: 100, watchTime: '15m 30s' },
  { id: 2, name: '李娜', progress: 100, watchTime: '16m 10s' },
  { id: 3, name: '王磊', progress: 85, watchTime: '12m 40s' },
  { id: 4, name: '刘洋', progress: 20, watchTime: '03m 15s' },
  { id: 5, name: '陈静', progress: 100, watchTime: '18m 05s' },
  { id: 6, name: '杨帆', progress: 60, watchTime: '09m 20s' },
  { id: 7, name: '赵云', progress: 100, watchTime: '14m 50s' },
  { id: 8, name: '孙颖', progress: 45, watchTime: '06m 30s' },
  { id: 9, name: '周杰', progress: 90, watchTime: '13m 10s' },
  { id: 10, name: '吴敏', progress: 100, watchTime: '17m 20s' },
  { id: 11, name: '郑强', progress: 100, watchTime: '15m 00s' },
  { id: 12, name: '王芳', progress: 10, watchTime: '01m 45s' },
  { id: 13, name: '冯涛', progress: 100, watchTime: '16m 40s' },
  { id: 14, name: '陈思', progress: 100, watchTime: '15m 55s' },
  { id: 15, name: '李明', progress: 75, watchTime: '11m 20s' },
  { id: 16, name: '林峰', progress: 100, watchTime: '19m 10s' }
])

const broadcastMessage = ref('')

// 计算已达标人数
const completedStudentsCount = computed(() => {
  return students.value.filter(s => s.progress >= 100).length
})

// 计算未完成人数
const incompleteStudentsCount = computed(() => {
  return students.value.length - completedStudentsCount.value
})

// 模拟计算平均观看时长（固定返回用于演示展示格式）
const averageWatchTime = ref('13') 

// 发送通知逻辑
const sendReminder = (target) => {
  if (!broadcastMessage.value.trim()) {
    alert("请输入要下发的通知内容！")
    return
  }
  
  if (target === 'incomplete') {
    alert(`已向 ${incompleteStudentsCount.value} 名未完成学生下发通知：\n"${broadcastMessage.value}"`)
  } else {
    alert(`已向全班下发通知：\n"${broadcastMessage.value}"`)
  }
  
  broadcastMessage.value = ''
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

.hero-send-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -5px rgba(99, 102, 241, 0.5);
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