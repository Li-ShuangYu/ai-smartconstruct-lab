<template>
  <div style="height: 100vh;" class="p-6 bg-slate-50">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative overflow-hidden font-sans">
      
      <div class="flex justify-between items-center mb-6 shrink-0 border-b border-gray-200/50 pb-4">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase italic">Node: TASK_DISPATCH_CONTROL</div>
          <h2 class="text-2xl font-bold text-gray-800 flex items-center gap-2">任务工单下发与监控</h2>
          <p class="text-sm text-gray-500 mt-1">配置实训任务内容并一键下发，实时追踪全班同学的查收进度。</p>
        </div>
        <div class="flex items-center gap-4">
          <div class="px-4 py-2 bg-indigo-50/80 rounded-lg border border-indigo-100 flex items-center gap-3">
            <span class="text-sm text-indigo-600 font-medium">任务查收进度：</span>
            <span class="text-xl font-bold" :class="receivedCount === students.length ? 'text-green-600' : 'text-indigo-600'">
              {{ receivedCount }} <span class="text-sm text-indigo-400 font-normal">/ {{ students.length }}</span>
            </span>
          </div>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1.2] flex flex-col gap-4 min-w-[360px] h-full border-r border-gray-200/60 pr-6">
          <div class="flex justify-between items-center mb-1">
            <h3 class="font-bold text-gray-700 flex items-center gap-2">
              <svg class="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" /></svg>
              实训工单配置
            </h3>
            <button @click="isEditing = !isEditing" class="text-xs font-bold text-indigo-600 bg-indigo-50 hover:bg-indigo-100 px-3 py-1.5 rounded transition-colors">
              {{ isEditing ? '保存修改' : '修改工单' }}
            </button>
          </div>

          <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 space-y-4">
            
            <div class="bg-white/60 p-4 rounded-xl border border-gray-200/60 shadow-sm space-y-3">
              <div>
                <label class="text-xs font-bold text-gray-500 mb-1 block">任务标题</label>
                <input v-if="isEditing" v-model="taskData.title" type="text" class="w-full border border-gray-300 rounded p-2 text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
                <h1 v-else class="text-lg font-bold text-gray-800">{{ taskData.title }}</h1>
              </div>
              <div>
                <label class="text-xs font-bold text-gray-500 mb-1 block">截止时间</label>
                <input v-if="isEditing" v-model="taskData.deadline" type="text" class="w-full border border-gray-300 rounded p-2 text-sm focus:ring-2 focus:ring-indigo-500 outline-none">
                <span v-else class="px-3 py-1 bg-red-50 text-red-600 rounded text-xs font-bold border border-red-100 inline-block">
                  截止: {{ taskData.deadline }}
                </span>
              </div>
            </div>

            <div class="bg-white/60 p-4 rounded-xl border border-gray-200/60 shadow-sm">
              <label class="text-xs font-bold text-gray-500 mb-2 block">详细要求</label>
              <textarea v-if="isEditing" v-model="taskData.requirements" rows="6" class="w-full border border-gray-300 rounded p-2 text-sm focus:ring-2 focus:ring-indigo-500 outline-none custom-scrollbar resize-none"></textarea>
              <div v-else class="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">{{ taskData.requirements }}</div>
            </div>

            <div class="bg-indigo-50/50 p-4 rounded-xl border border-indigo-100 shadow-sm">
              <div class="flex justify-between items-center mb-3">
                <h4 class="text-sm font-bold text-indigo-800">附件资源库</h4>
                <button v-if="isEditing" @click="handleUpload" class="text-[10px] font-bold bg-white border border-indigo-200 text-indigo-600 px-2 py-1 rounded hover:bg-indigo-50">
                  + 上传新文件
                </button>
              </div>
              <div class="flex flex-col gap-2">
                <div v-for="(file, index) in taskData.materials" :key="index" class="flex items-center justify-between bg-white px-3 py-2.5 rounded-lg border border-indigo-50 hover:border-indigo-200 transition-colors">
                  <div class="flex items-center gap-2 min-w-0">
                    <div class="text-lg shrink-0">{{ getFileIcon(file.type) }}</div>
                    <span class="text-xs font-medium text-gray-700 truncate">{{ file.name }}</span>
                  </div>
                  <button v-if="isEditing" @click="removeFile(index)" class="text-red-400 hover:text-red-600 shrink-0 ml-2 p-1" title="删除文件">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                  </button>
                </div>
                <div v-if="taskData.materials.length === 0" class="text-center py-2 text-xs text-indigo-300">
                  暂无附件资源
                </div>
              </div>
            </div>

          </div>

          <div class="shrink-0 pt-2">
            <button 
               @click="handleDispatch"
               :disabled="isDispatched"
               class="hero-send-btn w-full justify-center py-4 rounded-xl text-base font-bold shadow-lg transition-all flex items-center gap-2 active:scale-[0.98]"
               :class="isDispatched ? 'opacity-60 cursor-not-allowed grayscale' : 'hover:shadow-indigo-500/40'"
             >
               {{ isDispatched ? '工单已下发，正在监控查收状态' : '一键下发任务工单' }}
               <svg v-if="!isDispatched" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" /></svg>
            </button>
          </div>
        </div>

        <div class="flex-[2] flex flex-col h-full bg-white/40 rounded-xl border border-gray-200/60 shadow-inner p-5">
          
          <div class="flex justify-between items-center mb-4 shrink-0">
            <h3 class="font-bold text-gray-700 flex items-center gap-2">
              <svg class="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" /></svg>
              学生查收状态大盘
            </h3>
            
            <button 
              v-if="isDispatched"
              @click="handleRemind"
              :disabled="unreceivedCount === 0"
              class="px-4 py-2 rounded-lg text-sm font-bold border flex items-center gap-2 transition-all"
              :class="unreceivedCount > 0 
                ? 'bg-orange-50 text-orange-600 border-orange-200 hover:bg-orange-100 shadow-sm' 
                : 'bg-gray-50 text-gray-400 border-gray-200 cursor-not-allowed'"
            >
              <span v-if="unreceivedCount > 0" class="flex h-2.5 w-2.5 relative">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-orange-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-2.5 w-2.5 bg-orange-500"></span>
              </span>
              一键发送查收提醒 ({{ unreceivedCount }}人)
            </button>
          </div>

          <div class="flex-1 overflow-y-auto custom-scrollbar pr-2">
            
            <div v-if="!isDispatched" class="h-full flex flex-col items-center justify-center text-center">
              <div class="w-16 h-16 bg-indigo-50 rounded-full flex items-center justify-center mb-4 text-indigo-300">
                <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" /></svg>
              </div>
              <h3 class="text-lg font-bold text-gray-800 mb-2">等待任务下发</h3>
              <p class="text-gray-500 text-sm">配置好左侧工单后，点击“一键下发任务工单”<br/>即可在此处实时监控全班查收状态。</p>
            </div>

            <div v-else class="grid grid-cols-4 lg:grid-cols-5 gap-4 pb-4">
              <div 
                v-for="student in students" 
                :key="student.id"
                class="p-4 rounded-xl border transition-all duration-300 relative overflow-hidden flex flex-col items-center gap-3"
                :class="student.received 
                  ? 'bg-green-50/50 border-green-200 shadow-sm' 
                  : 'bg-white border-gray-100 opacity-80'"
              >
                <div class="absolute top-2 right-2">
                  <div v-if="student.received" class="w-2 h-2 rounded-full bg-green-500 shadow-[0_0_5px_#22c55e]"></div>
                  <div v-else class="w-2 h-2 rounded-full bg-gray-300"></div>
                </div>

                <div class="relative">
                  <div 
                    class="w-12 h-12 rounded-full flex items-center justify-center text-white font-bold text-lg transition-all"
                    :class="student.received 
                      ? 'bg-gradient-to-br from-green-400 to-emerald-500 shadow-md' 
                      : 'bg-gradient-to-br from-gray-300 to-gray-400'"
                  >
                    {{ student.name.charAt(0) }}
                  </div>
                </div>
                
                <div class="text-center">
                  <h4 class="text-sm font-bold text-gray-800">{{ student.name }}</h4>
                  <p class="text-[10px] mt-1 font-bold" :class="student.received ? 'text-green-600' : 'text-gray-400'">
                    {{ student.received ? '已查收并确认' : '待查收' }}
                  </p>
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
import { ref, reactive, computed } from 'vue'

// ==== 状态管理 ====
const isEditing = ref(false)
const isDispatched = ref(false)

// ==== 任务工单数据 ====
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
  ]
})

// ==== 学生数据模拟 ====
const students = ref([
  { id: 1, name: '张伟', received: false },
  { id: 2, name: '李娜', received: false },
  { id: 3, name: '王磊', received: false },
  { id: 4, name: '刘洋', received: false },
  { id: 5, name: '陈静', received: false },
  { id: 6, name: '杨帆', received: false },
  { id: 7, name: '赵云', received: false },
  { id: 8, name: '孙颖', received: false },
  { id: 9, name: '周杰', received: false },
  { id: 10, name: '吴敏', received: false },
  { id: 11, name: '郑强', received: false },
  { id: 12, name: '王芳', received: false },
  { id: 13, name: '冯涛', received: false },
  { id: 14, name: '陈思', received: false },
  { id: 15, name: '李明', received: false },
  { id: 16, name: '林峰', received: false }
])

// ==== 计算属性 ====
const receivedCount = computed(() => students.value.filter(s => s.received).length)
const unreceivedCount = computed(() => students.value.length - receivedCount.value)

// ==== 方法 ====
const getFileIcon = (type) => {
  if (type === 'word') return '📄'
  if (type === 'code') return '💻'
  return '📎'
}

const handleUpload = () => {
  // 模拟上传
  const newFile = { name: `新增参考资料_${Math.floor(Math.random() * 100)}.pdf`, type: 'pdf' }
  taskData.materials.push(newFile)
}

const removeFile = (index) => {
  if(confirm('确认删除该附件吗？')) {
    taskData.materials.splice(index, 1)
  }
}

const handleDispatch = () => {
  if (isEditing.value) {
    alert("请先保存工单修改，再下发任务！")
    return
  }
  if (confirm('确认将当前工单下发给全班吗？')) {
    isDispatched.value = true
    // 模拟学生陆续查收的动态效果
    students.value.forEach((stu, index) => {
      setTimeout(() => {
        // 随机让 80% 的学生查收
        if (Math.random() > 0.2) {
          stu.received = true
        }
      }, Math.random() * 5000 + 1000)
    })
  }
}

const handleRemind = () => {
  alert(`已向 ${unreceivedCount.value} 名未查收学生发送强制闪烁弹窗提醒！`)
  // 模拟提醒后学生查收
  setTimeout(() => {
    students.value.forEach(stu => {
      if (!stu.received && Math.random() > 0.3) {
         stu.received = true
      }
    })
  }, 1500)
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
  box-shadow: 0 10px 20px -5px rgba(99, 102, 241, 0.4);
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