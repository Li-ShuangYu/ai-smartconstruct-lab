<template>
  <div class="page-wrapper w-full min-h-screen">
    
    <div class="glass-card w-full h-full p-8 flex flex-col z-10 relative">
      
      <div class="flex flex-col md:flex-row justify-between items-start md:items-end mb-8 pb-6 border-b border-gray-200/50 gap-4">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: TASK_DISTRIBUTE Monitor</div>
          <h1 class="text-3xl font-bold text-gray-800 flex items-center gap-3">
             <svg class="text-indigo-500" style="width: 28px; height: 28px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg>
             任务下发查收大盘
          </h1>
          <p class="text-sm text-gray-500 mt-2">当前分发任务：<span class="font-bold text-gray-700">{{ taskTitle }}</span></p>
        </div>
        
        <div class="flex items-center gap-6 bg-white/50 px-6 py-3 rounded-2xl border border-gray-100 shadow-sm">
           <div class="text-center">
             <div class="text-xs text-gray-500 mb-1">已查收</div>
             <div class="text-2xl font-bold text-green-500">{{ receivedCount }}</div>
           </div>
           <div class="w-px h-10 bg-gray-200"></div>
           <div class="text-center">
             <div class="text-xs text-gray-500 mb-1">未查收</div>
             <div class="text-2xl font-bold text-amber-500">{{ pendingCount }}</div>
           </div>
        </div>
      </div>

      <div class="mb-6 flex justify-end">
        <button 
          @click="remindAllPending"
          class="px-6 py-2.5 bg-amber-50 hover:bg-amber-100 text-amber-600 border border-amber-200 rounded-lg text-sm font-bold flex items-center gap-2 transition-colors shadow-sm"
          :disabled="pendingCount === 0"
          :class="{'opacity-50 cursor-not-allowed': pendingCount === 0}"
        >
          <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /></svg>
          一键闪烁提醒未查收学生
        </button>
      </div>

      <div class="flex-1 overflow-y-auto pr-2 custom-scrollbar">
        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 pb-4">
          
          <div v-for="student in studentList" :key="student.id" 
               class="bg-white/60 border rounded-xl p-4 flex flex-col justify-between shadow-sm transition-all duration-300"
               :class="student.hasReceived ? 'border-green-100' : 'border-amber-200 hover:border-amber-300 hover:shadow-md relative overflow-hidden'">
            
            <div v-if="!student.hasReceived" class="absolute top-0 left-0 w-1 h-full bg-amber-400"></div>

            <div class="flex items-center gap-3 mb-4">
              <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold border"
                   :class="student.hasReceived ? 'bg-green-50 text-green-600 border-green-100' : 'bg-amber-50 text-amber-600 border-amber-100'">
                {{ student.name.charAt(0) }}
              </div>
              <div>
                <div class="font-bold text-gray-700 text-sm">{{ student.name }}</div>
                <div class="text-xs mt-0.5 flex items-center gap-1" :class="student.hasReceived ? 'text-green-500' : 'text-amber-500'">
                   <span class="relative flex h-2 w-2" v-if="!student.hasReceived">
                    <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
                    <span class="relative inline-flex rounded-full h-2 w-2 bg-amber-500"></span>
                  </span>
                  <svg v-else style="width: 10px; height: 10px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                  {{ student.hasReceived ? '已查收' : '待查收' }}
                </div>
              </div>
            </div>

            <button v-if="!student.hasReceived" 
                    @click="remindSingle(student)"
                    class="w-full py-1.5 text-xs font-bold text-amber-600 bg-amber-50/50 hover:bg-amber-100 border border-amber-200/50 rounded transition-colors">
              单点提醒
            </button>
            <div v-else class="w-full py-1.5 text-xs text-center text-green-500 font-medium">
              任务进行中...
            </div>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const taskTitle = ref('SM4 密码模块系统集成与联调任务')

// 模拟学生查收数据
const studentList = ref([
  { id: 1, name: '陈同学', hasReceived: true },
  { id: 2, name: '林同学', hasReceived: true },
  { id: 3, name: '张同学', hasReceived: false },
  { id: 4, name: '王同学', hasReceived: true },
  { id: 5, name: '李同学', hasReceived: false },
  { id: 6, name: '赵同学', hasReceived: true },
  { id: 7, name: '周同学', hasReceived: false },
  { id: 8, name: '吴同学', hasReceived: true },
])

const receivedCount = computed(() => studentList.value.filter(s => s.hasReceived).length)
const pendingCount = computed(() => studentList.value.length - receivedCount.value)

const remindAllPending = () => {
  if (pendingCount.value === 0) return
  alert(`已向 ${pendingCount.value} 名未查收学生的主界面发送强提醒闪烁弹窗！`)
}

const remindSingle = (student) => {
  alert(`已向学生 [${student.name}] 发送独立提醒！`)
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.2); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.4); }
</style>