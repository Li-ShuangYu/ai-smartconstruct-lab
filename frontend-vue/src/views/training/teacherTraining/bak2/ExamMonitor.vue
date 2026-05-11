<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 md:p-8 flex flex-col z-10">
      
      <div class="flex justify-between items-end mb-6 pb-4 border-b border-gray-200/50">
        <div>
          <div class="mb-1 text-xs font-bold text-red-500 tracking-widest uppercase flex items-center gap-2">
            <span class="w-2 h-2 bg-red-500 rounded-full animate-pulse"></span>
            Node: EXAM (Strict Mode)
          </div>
          <h1 class="text-3xl font-bold text-gray-800 flex items-center gap-3">
             <svg class="text-gray-700" style="width: 28px; height: 28px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" /></svg>
             防作弊云监考大盘
          </h1>
        </div>
        
        <div class="flex gap-4">
          <div class="bg-white/60 px-5 py-2 rounded-xl border border-gray-100 shadow-sm flex flex-col items-center">
            <span class="text-xs text-gray-500">考场进度</span>
            <span class="text-xl font-bold text-indigo-600">{{ submittedCount }} / {{ studentList.length }} 已交</span>
          </div>
          <div class="bg-red-50 px-5 py-2 rounded-xl border border-red-100 shadow-sm flex flex-col items-center">
            <span class="text-xs text-red-600 font-bold">异常警告人次</span>
            <span class="text-xl font-bold text-red-600">{{ abnormalCount }}</span>
          </div>
        </div>
      </div>

      <div class="flex-1 flex flex-col lg:flex-row gap-6 min-h-0">
        
        <div class="flex-[1] bg-white/50 border border-gray-200/60 rounded-2xl flex flex-col overflow-hidden shadow-sm">
          <div class="bg-gray-800 px-4 py-3 border-b border-gray-700 flex items-center justify-between">
            <span class="font-bold text-white text-sm flex items-center gap-2">
               <svg class="text-red-400" style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
               异常切屏告警台
            </span>
            <span class="text-xs text-gray-400">实时滚动</span>
          </div>
          
          <div class="flex-1 overflow-y-auto p-4 space-y-3 custom-scrollbar bg-gray-50/50">
            <div v-if="alertLogs.length === 0" class="text-center text-gray-400 text-sm mt-10">
              考场秩序良好，暂无异常。
            </div>
            <div v-for="log in alertLogs" :key="log.id" class="p-3 bg-red-50 border border-red-100 rounded-xl shadow-sm animate-slide-up">
              <div class="flex justify-between items-start mb-1">
                <span class="font-bold text-red-700 text-sm">{{ log.name }}</span>
                <span class="text-xs text-gray-500">{{ log.time }}</span>
              </div>
              <p class="text-xs text-red-600">{{ log.reason }}</p>
            </div>
          </div>
        </div>

        <div class="flex-[2.5] flex flex-col bg-white/40 border border-gray-100 rounded-2xl shadow-sm overflow-hidden">
          
          <div class="bg-gray-50/80 px-6 py-4 border-b border-gray-200/50 flex items-center justify-between">
            <div class="flex gap-4 text-sm font-medium">
              <span class="flex items-center gap-2 text-gray-600"><span class="w-3 h-3 bg-green-400 rounded-full"></span> 正常作答</span>
              <span class="flex items-center gap-2 text-red-600"><span class="w-3 h-3 bg-red-400 rounded-full animate-pulse"></span> 存在作弊嫌疑</span>
              <span class="flex items-center gap-2 text-indigo-500"><svg style="width: 14px; height: 14px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg> 已交卷</span>
            </div>
          </div>

          <div class="flex-1 overflow-y-auto p-6 custom-scrollbar">
            <div class="grid grid-cols-2 lg:grid-cols-3 gap-4">
              
              <div v-for="student in studentList" :key="student.id" 
                   class="bg-white border rounded-xl p-4 flex flex-col shadow-sm transition-all relative overflow-hidden"
                   :class="student.cheatCount > 0 && !student.isSubmitted ? 'border-red-300 bg-red-50/30 shadow-[0_0_15px_rgba(248,113,113,0.15)]' : 'border-gray-200'">
                
                <div class="flex justify-between items-start mb-4">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold text-white shadow-inner"
                         :class="student.isSubmitted ? 'bg-indigo-400' : (student.cheatCount > 0 ? 'bg-red-500' : 'bg-gray-400')">
                      {{ student.name.charAt(0) }}
                    </div>
                    <div>
                      <div class="font-bold text-gray-800 text-sm">{{ student.name }}</div>
                      <div class="text-[10px] text-gray-500 mt-0.5">作答进度: {{ student.progress }}%</div>
                    </div>
                  </div>
                  
                  <div v-if="student.isSubmitted" class="text-xs font-bold text-indigo-600 bg-indigo-50 px-2 py-1 rounded border border-indigo-100">已交卷</div>
                  <div v-else-if="student.cheatCount > 0" class="text-xs font-bold text-red-600 bg-red-100 px-2 py-1 rounded flex items-center gap-1 border border-red-200">
                    切屏 x {{ student.cheatCount }}
                  </div>
                  <div v-else class="text-xs font-bold text-green-600 bg-green-50 px-2 py-1 rounded border border-green-100">正常</div>
                </div>

                <div class="mt-auto pt-3 border-t flex gap-2" :class="student.cheatCount > 0 ? 'border-red-200/50' : 'border-gray-100'">
                  <button v-if="!student.isSubmitted" 
                          @click="sendWarning(student)"
                          class="flex-1 py-1.5 text-xs font-bold rounded transition-colors"
                          :class="student.cheatCount > 0 ? 'bg-red-600 hover:bg-red-700 text-white shadow-md' : 'bg-gray-50 hover:bg-gray-100 text-gray-600 border border-gray-200'">
                    {{ student.cheatCount > 0 ? '发送严重警告弹窗' : '发送普通提醒' }}
                  </button>
                  <button v-else class="flex-1 py-1.5 text-xs font-bold text-indigo-600 bg-indigo-50 hover:bg-indigo-100 rounded border border-indigo-100">
                    查看答卷结果
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

// 模拟学情数据
const studentList = ref([
  { id: 1, name: '陈同学', progress: 100, isSubmitted: true, cheatCount: 0 },
  { id: 2, name: '林同学', progress: 45, isSubmitted: false, cheatCount: 2 }, // 嫌疑人
  { id: 3, name: '张同学', progress: 80, isSubmitted: false, cheatCount: 0 },
  { id: 4, name: '王同学', progress: 100, isSubmitted: true, cheatCount: 1 },
  { id: 5, name: '李同学', progress: 20, isSubmitted: false, cheatCount: 3 }, // 严重嫌疑
  { id: 6, name: '赵同学', progress: 65, isSubmitted: false, cheatCount: 0 },
])

const submittedCount = computed(() => studentList.value.filter(s => s.isSubmitted).length)
const abnormalCount = computed(() => studentList.value.filter(s => s.cheatCount > 0).length)

// 模拟实时滚动告警日志
const alertLogs = ref([
  { id: 101, name: '李同学', time: '15:20:05', reason: '触发第 3 次切屏/失去焦点，可能在查阅资料。' },
  { id: 102, name: '林同学', time: '15:18:30', reason: '触发第 2 次离开考试页面。' },
  { id: 103, name: '王同学', time: '14:45:12', reason: '触发第 1 次切屏。' },
])

const sendWarning = (student) => {
  if (student.cheatCount > 0) {
    alert(`已强制向 [${student.name}] 屏幕中心下发全屏严重警告弹窗！`)
  } else {
    alert(`已向 [${student.name}] 发送普通专注提醒。`)
  }
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(156, 163, 175, 0.4); border-radius: 4px; }

.animate-slide-up { animation: slideUp 0.3s ease-out forwards; }
@keyframes slideUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>