<template>
  <div class="page-wrapper flex items-center justify-center w-full h-full min-h-[calc(100vh-100px)]">
    
    <div class="glass-card w-full max-w-[1400px] p-6 md:p-8 flex flex-col z-10 h-[850px]">
      
      <div class="flex justify-between items-end mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase flex items-center gap-2">
            <svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" /></svg>
            Node: TEACHER_EVAL
          </div>
          <h1 class="text-3xl font-bold text-gray-800">课堂评价与成绩结算大盘</h1>
          <p class="text-sm text-gray-500 mt-2">已聚合前 15 个节点的实训数据。总评权重系数: <span class="font-bold text-indigo-600">{{ evalConfig.weight }}</span></p>
        </div>
        
        <div class="flex items-center gap-4">
          <div class="bg-white/60 px-5 py-2.5 rounded-xl border border-gray-100 shadow-sm flex items-center gap-4">
             <div class="flex flex-col">
               <span class="text-xs text-gray-500">已点评人数</span>
               <span class="text-xl font-bold text-indigo-600">{{ evaluatedCount }} / {{ studentList.length }}</span>
             </div>
             <div class="w-px h-8 bg-gray-200"></div>
             <button @click="autoFillScores" class="text-xs bg-indigo-50 text-indigo-600 font-bold px-3 py-1.5 rounded-lg border border-indigo-100 hover:bg-indigo-100 transition-colors">
               一键采纳系统建议分
             </button>
          </div>
          
          <button 
             class="hero-send-btn px-8 py-4 shadow-lg text-lg rounded-xl transition-all"
             :class="isAllEvaluated ? 'hover:-translate-y-1 hover:shadow-indigo-500/40 bg-gradient-to-r from-green-500 to-emerald-600' : 'opacity-60 cursor-not-allowed'"
             :disabled="!isAllEvaluated"
             @click="publishGrades"
          >
             <svg style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" /></svg>
             一键发布全班成绩
          </button>
        </div>
      </div>

      <div class="flex-1 bg-white/50 border border-gray-200/60 rounded-2xl shadow-sm flex flex-col overflow-hidden">
        
        <div class="grid grid-cols-12 gap-4 px-6 py-4 bg-gray-50/80 border-b border-gray-200 text-xs font-bold text-gray-500 uppercase tracking-wider shrink-0">
          <div class="col-span-2">学生信息</div>
          <div class="col-span-2 text-center">系统综合建议分</div>
          <div class="col-span-2">最终确认得分</div>
          <div class="col-span-5">导师结业评语</div>
          <div class="col-span-1 text-right">状态</div>
        </div>
        
        <div class="flex-1 overflow-y-auto p-3 space-y-2 custom-scrollbar">
          <div v-for="student in studentList" :key="student.id" 
               class="grid grid-cols-12 gap-4 px-4 py-3 items-start bg-white border border-gray-100 rounded-xl shadow-sm hover:shadow-md transition-shadow group"
               :class="student.isEvaluated ? 'border-green-200 bg-green-50/10' : ''">
            
            <div class="col-span-2 flex items-center gap-3 pt-2">
              <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold shadow-inner"
                   :class="student.isEvaluated ? 'bg-green-100 text-green-700' : 'bg-indigo-50 text-indigo-700'">
                {{ student.name.charAt(0) }}
              </div>
              <div class="font-bold text-gray-800">{{ student.name }}</div>
            </div>
            
            <div class="col-span-2 flex flex-col items-center justify-center pt-1">
              <div class="text-2xl font-black text-gray-300 group-hover:text-gray-400 transition-colors">{{ student.systemScore }}</div>
              <div class="text-[10px] text-gray-400">客观模型计算</div>
            </div>

            <div class="col-span-2 pt-1">
              <div class="relative flex items-center">
                <input type="number" min="0" max="100" v-model="student.finalScore" @input="checkEvaluation(student)"
                       class="w-full bg-gray-50 border border-gray-200 rounded-lg px-3 py-2.5 text-lg font-bold text-indigo-600 outline-none focus:bg-white focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 transition-all text-center">
                <span class="absolute right-3 text-xs font-bold text-gray-400 pointer-events-none">分</span>
              </div>
            </div>

            <div class="col-span-5">
              <textarea v-model="student.comment" @input="checkEvaluation(student)"
                        placeholder="请输入对该生的综合评价..." 
                        class="w-full bg-gray-50 border border-gray-200 rounded-lg px-4 py-2.5 text-sm text-gray-700 outline-none focus:bg-white focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 transition-all resize-none h-14 custom-scrollbar"></textarea>
            </div>

            <div class="col-span-1 flex justify-end pt-3">
              <svg v-if="student.isEvaluated" style="width: 24px; height: 24px; flex-shrink: 0;" class="text-green-500 animate-pop-in" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              <svg v-else style="width: 24px; height: 24px; flex-shrink: 0;" class="text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            </div>

          </div>
        </div>

      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const evalConfig = ref({
  weight: 0.5
})

// 模拟学生结算数据
const studentList = ref([
  { id: 1, name: '陈同学', systemScore: 92, finalScore: null, comment: '', isEvaluated: false },
  { id: 2, name: '林同学', systemScore: 78, finalScore: null, comment: '', isEvaluated: false },
  { id: 3, name: '张同学', systemScore: 85, finalScore: null, comment: '', isEvaluated: false },
  { id: 4, name: '王同学', systemScore: 60, finalScore: null, comment: '', isEvaluated: false },
  { id: 5, name: '李同学', systemScore: 88, finalScore: 90, comment: '代码规范很好，继续保持。', isEvaluated: true }, // 已评
])

// 实时校验当前行是否已完成评价 (有分数且有评语)
const checkEvaluation = (student) => {
  student.isEvaluated = !!(student.finalScore && student.comment.trim())
}

// 一键填充系统建议分
const autoFillScores = () => {
  studentList.value.forEach(s => {
    if (!s.finalScore) {
      s.finalScore = s.systemScore
      // 给个默认评语占位
      if (!s.comment) s.comment = '实训表现达到预期要求，准予结业。'
      checkEvaluation(s)
    }
  })
}

const evaluatedCount = computed(() => studentList.value.filter(s => s.isEvaluated).length)
const isAllEvaluated = computed(() => evaluatedCount.value === studentList.value.length)

const publishGrades = () => {
  if (isAllEvaluated.value) {
    if (confirm("成绩一旦发布将推送到学生端，不可撤回。确认发布吗？")) {
      alert("全班成绩与评语发布成功！实训流程完美闭环！")
    }
  }
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.3); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.5); }

/* 取消 number input 默认的上下箭头 */
input[type=number]::-webkit-inner-spin-button, 
input[type=number]::-webkit-outer-spin-button { 
  -webkit-appearance: none; 
  margin: 0; 
}

.animate-pop-in { animation: popIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards; }
@keyframes popIn {
  0% { opacity: 0; transform: scale(0.5) rotate(-30deg); }
  100% { opacity: 1; transform: scale(1) rotate(0deg); }
}
</style>