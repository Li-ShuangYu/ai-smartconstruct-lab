<template>
  <div style="height: 100vh;" class="p-6 bg-slate-50">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 font-sans overflow-hidden">
      
      <div class="flex justify-between items-center mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase italic">Node: TEACHER_REVIEW_FINAL</div>
          <h1 class="text-2xl font-bold text-gray-800 flex items-center gap-2">教师总结与成果评定</h1>
        </div>
        
        <div class="flex items-center gap-4">
          <div class="px-4 py-2 bg-white/60 rounded-xl border border-gray-100 shadow-sm text-sm">
            <span class="text-gray-500">已评/总数：</span>
            <span class="font-black text-indigo-600">{{ reviewedCount }} / {{ students.length }}</span>
          </div>
          <button @click="nextNode" class="hero-send-btn px-6 py-2.5 rounded-xl text-sm font-bold shadow-lg transition-all active:scale-95 flex items-center gap-2">
            点击进入下一节点
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" /></svg>
          </button>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1] flex flex-col gap-5 min-w-[320px]">
          <div class="flex-1 bg-white/60 border border-gray-100 rounded-2xl p-6 shadow-sm flex flex-col overflow-hidden">
            <h3 class="text-sm font-bold text-indigo-900 mb-4 flex items-center gap-2 shrink-0">
              <svg class="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
              AI 班级学情洞察
            </h3>
            
            <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 space-y-6">
              <div class="bg-orange-50/50 border border-orange-100 rounded-xl p-4">
                <h4 class="text-[10px] font-black text-orange-700 mb-2 uppercase tracking-widest">班级共性问题</h4>
                <p class="text-xs text-gray-700 leading-relaxed">{{ aiInsights.commonIssues }}</p>
              </div>

              <div class="bg-indigo-50/50 border border-indigo-100 rounded-xl p-4">
                <h4 class="text-[10px] font-black text-indigo-700 mb-2 uppercase tracking-widest">优秀案例推荐理由</h4>
                <p class="text-xs text-gray-700 leading-relaxed">{{ aiInsights.excellentReason }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="flex-[2.2] flex flex-col min-w-0 h-full gap-4">
          
          <div class="flex-1 bg-white/60 border border-gray-100 rounded-2xl shadow-sm overflow-hidden flex flex-col">
            <div class="grid grid-cols-12 gap-4 px-6 py-3 border-b border-gray-100 bg-gray-50/50 text-[10px] font-bold text-gray-400 uppercase tracking-widest shrink-0">
              <div class="col-span-1 text-center">排名</div>
              <div class="col-span-5 text-left">学生 (点击查看产出物)</div>
              <div class="col-span-2 text-center">得分</div>
              <div class="col-span-4 text-right">操作评定</div>
            </div>

            <div class="flex-1 overflow-y-auto p-2 space-y-1 custom-scrollbar">
              <div 
                v-for="(student, index) in rankedStudents" 
                :key="student.id"
                class="grid grid-cols-12 gap-4 px-4 py-2.5 items-center bg-white border border-transparent rounded-xl hover:border-indigo-100 hover:shadow-sm transition-all group"
              >
                <div class="col-span-1 flex justify-center">
                  <span v-if="index === 0" class="text-xl" title="金牌">🥇</span>
                  <span v-else-if="index === 1" class="text-xl" title="银牌">🥈</span>
                  <span v-else-if="index === 2" class="text-xl" title="铜牌">🥉</span>
                  <span v-else class="text-xs font-mono font-bold text-gray-400">{{ index + 1 }}</span>
                </div>

                <div @click="openOutputs(student)" class="col-span-5 flex items-center gap-3 cursor-pointer group/item">
                  <div class="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 text-white flex items-center justify-center text-xs font-bold shadow-sm group-hover/item:scale-110 transition-transform">
                    {{ student.name.charAt(0) }}
                  </div>
                  <div class="min-w-0">
                    <span class="text-sm font-bold text-gray-700 group-hover/item:text-indigo-600 transition-colors">{{ student.name }}</span>
                    <span v-if="student.isExcellent" class="ml-2 text-[9px] bg-green-100 text-green-700 px-1.5 py-0.5 rounded font-black uppercase">优秀案例</span>
                  </div>
                </div>

                <div class="col-span-2 text-center">
                  <span class="font-mono font-black text-gray-700">{{ student.score.toFixed(1) }}</span>
                </div>

                <div class="col-span-4 flex justify-end gap-2">
                  <button 
                    @click="markAsExcellent(student)"
                    :disabled="student.isExcellent"
                    class="p-2 rounded-lg border transition-all"
                    :class="student.isExcellent ? 'bg-green-50 border-green-200 text-green-600' : 'bg-white border-gray-200 text-gray-400 hover:text-green-600 hover:border-green-200'"
                    title="设为优秀案例"
                  >
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" /></svg>
                  </button>
                  <button 
                    @click="sendComment(student)"
                    :disabled="student.status === 'sent'"
                    class="px-3 py-1.5 rounded-lg text-xs font-bold transition-all border"
                    :class="student.status === 'sent' 
                      ? 'bg-gray-50 border-gray-100 text-gray-400 cursor-not-allowed' 
                      : 'bg-indigo-50 border-indigo-100 text-indigo-600 hover:bg-indigo-600 hover:text-white'"
                  >
                    {{ student.status === 'sent' ? '已下发评价' : '下发评价' }}
                  </button>
                </div>
              </div>
            </div>

            <div class="p-4 bg-gray-50/80 border-t border-gray-100 flex gap-4 shrink-0">
              <button @click="batchSendComments" class="flex-1 py-3 bg-white border border-indigo-200 text-indigo-600 rounded-xl text-sm font-bold shadow-sm hover:bg-indigo-50 transition-all flex justify-center items-center gap-2">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
                一键下发 AI 点评 ({{ pendingCommentCount }}人)
              </button>
              <button @click="batchMarkExcellent" class="flex-1 py-3 bg-white border border-green-200 text-green-600 rounded-xl text-sm font-bold shadow-sm hover:bg-green-50 transition-all flex justify-center items-center gap-2">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                一键下发优秀案例展示
              </button>
            </div>
          </div>
        </div>

      </div>

      <div v-if="selectedStudent" class="absolute inset-0 z-50 bg-gray-900/40 backdrop-blur-md rounded-[1.5rem] flex items-center justify-center animate-fade-in p-8">
        <div class="bg-white rounded-2xl w-full max-w-5xl h-full shadow-2xl flex flex-col overflow-hidden border border-gray-200">
          
          <div class="px-6 py-4 border-b border-gray-100 flex justify-between items-center bg-gray-50/80 shrink-0">
            <div class="flex items-center gap-4">
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 text-white flex items-center justify-center text-lg font-bold">
                {{ selectedStudent.name.charAt(0) }}
              </div>
              <h3 class="font-bold text-gray-800 text-xl">{{ selectedStudent.name }} 的产出物审查</h3>
            </div>
            <button @click="selectedStudent = null" class="text-gray-400 hover:text-red-500 p-2 rounded-lg transition-colors">
              <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
            </button>
          </div>
          
          <div class="flex-1 overflow-hidden flex flex-col p-6 gap-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 h-1/2 min-h-0">
              <div class="bg-slate-50 rounded-xl p-5 border border-gray-200 flex flex-col overflow-hidden">
                <h4 class="text-xs font-bold text-gray-400 uppercase mb-3 flex items-center gap-2">
                   <svg class="w-4 h-4 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
                   产出一：需求分析说明
                </h4>
                <div class="flex-1 overflow-y-auto custom-scrollbar text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">
                  {{ selectedStudent.outputs.requirement }}
                </div>
              </div>
              <div class="bg-slate-50 rounded-xl p-5 border border-gray-200 flex flex-col overflow-hidden">
                <h4 class="text-xs font-bold text-gray-400 uppercase mb-3 flex items-center gap-2">
                   <svg class="w-4 h-4 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" /></svg>
                   产出二：系统设计方案
                </h4>
                <div class="flex-1 overflow-y-auto custom-scrollbar text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">
                  {{ selectedStudent.outputs.plan }}
                </div>
              </div>
            </div>
            <div class="flex-1 bg-slate-900 rounded-xl overflow-hidden border border-slate-800 flex flex-col min-h-0">
               <div class="bg-slate-800 px-4 py-2 border-b border-slate-700 flex justify-between items-center shrink-0">
                 <span class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">Main_Logic.py</span>
                 <div class="flex gap-1.5">
                   <div class="w-2.5 h-2.5 rounded-full bg-red-500/50"></div>
                   <div class="w-2.5 h-2.5 rounded-full bg-amber-500/50"></div>
                   <div class="w-2.5 h-2.5 rounded-full bg-green-500/50"></div>
                 </div>
               </div>
               <div class="flex-1 overflow-auto custom-scrollbar-dark p-6">
                 <pre class="text-sm font-mono text-indigo-300 leading-relaxed"><code>{{ selectedStudent.outputs.code }}</code></pre>
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

// --- 基础洞察数据 ---
const aiInsights = ref({
  commonIssues: '全班学生在“数组原地反转”环节普遍表现优异，但在“多维数组索引计算”的逻辑推导中，仍有 30% 左右的同学在处理嵌套循环时出现了 Off-by-one 误差。',
  excellentReason: '本次推荐的案例在代码健壮性方面达到了准生产级别，需求分析中不仅考虑了正常流程，还详尽列出了异常输入（如 None、空列表）的规避方案。'
})

// --- 学生数据 ---
const students = ref([
  { 
    id: 1, name: '陈静', score: 9.8, status: 'pending', isExcellent: false,
    outputs: {
      requirement: '1. 需实现动态数组自增\n2. 处理 IndexError 异常\n3. 保证 O(1) 均摊时间复杂度',
      plan: '采用倍增扩容策略，初始容量设定为 8，当负载因子达到 0.75 时触发扩容。',
      code: 'def reverse_array(arr):\n    return arr[::-1] # Pythonic Style'
    }
  },
  { id: 2, name: '张伟', score: 9.5, status: 'pending', isExcellent: false, outputs: { requirement: '略', plan: '略', code: 'print("Hello World")' } },
  { id: 3, name: '孙颖', score: 9.2, status: 'pending', isExcellent: false, outputs: { requirement: '略', plan: '略', code: 'pass' } },
  { id: 4, name: '赵云', score: 8.8, status: 'sent', isExcellent: true, outputs: { requirement: '略', plan: '略', code: 'pass' } },
  { id: 5, name: '吴敏', score: 8.5, status: 'pending', isExcellent: false, outputs: { requirement: '略', plan: '略', code: 'pass' } },
  { id: 6, name: '李明', score: 8.2, status: 'sent', isExcellent: false, outputs: { requirement: '略', plan: '略', code: 'pass' } },
  { id: 7, name: '林峰', score: 7.9, status: 'pending', isExcellent: false, outputs: { requirement: '略', plan: '略', code: 'pass' } }
])

const rankedStudents = computed(() => [...students.value].sort((a, b) => b.score - a.score))
const reviewedCount = computed(() => students.value.filter(s => s.status === 'sent').length)
const pendingCommentCount = computed(() => students.value.filter(s => s.status !== 'sent').length)

// --- 弹窗逻辑 ---
const selectedStudent = ref(null)
const openOutputs = (student) => {
  selectedStudent.value = student
}

// --- 业务操作 ---
const sendComment = (student) => {
  student.status = 'sent'
}

const markAsExcellent = (student) => {
  student.isExcellent = true
}

const batchSendComments = () => {
  if (confirm('确认一键下发 AI 默认生成的点评吗？')) {
    students.value.forEach(s => s.status = 'sent')
  }
}

const batchMarkExcellent = () => {
  if (confirm('确认将排名前 3 的优秀案例同步至全班展示墙吗？')) {
    rankedStudents.value.slice(0, 3).forEach(s => s.isExcellent = true)
  }
}

const nextNode = () => {
  alert('实训环节已结束，正在结算成绩并进入下一课程节点。')
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

.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }

.custom-scrollbar-dark::-webkit-scrollbar { width: 4px; }
.custom-scrollbar-dark::-webkit-scrollbar-thumb { background: #475569; border-radius: 10px; }

.animate-fade-in { animation: fadeIn 0.3s ease-out forwards; }
@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.98) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
</style>