<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10">
      
      <div class="flex justify-between items-end mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase flex items-center gap-2">
            <svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
            Node: STUDENT_PEER_REVIEW Monitor
          </div>
          <h1 class="text-3xl font-bold text-gray-800">互评数据散点图与异常防刷监控</h1>
        </div>
        
        <div class="flex items-center gap-4 bg-white/60 px-5 py-3 rounded-2xl border border-gray-100 shadow-sm">
           <div class="flex flex-col items-center">
             <span class="text-xs text-gray-500">已产出评价数据</span>
             <span class="text-xl font-bold text-indigo-600">{{ scatterData.length }} 份</span>
           </div>
           <div class="w-px h-8 bg-gray-200"></div>
           <div class="flex flex-col items-center">
             <span class="text-xs text-gray-500">系统识别异常评价</span>
             <span class="text-xl font-bold text-red-600">{{ anomalyCount }} 份</span>
           </div>
        </div>
      </div>

      <div class="flex-1 flex flex-col lg:flex-row gap-6 min-h-0">
        
        <div class="flex-[1.5] bg-white/50 border border-gray-200/60 rounded-2xl p-6 shadow-inner flex flex-col relative overflow-hidden">
          <h3 class="font-bold text-gray-700 text-sm mb-6 flex justify-between items-center z-10">
            <span>多维度打分分布矩阵 (代码规范 vs 创新性)</span>
            <div class="flex gap-3 text-xs font-normal">
              <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 rounded-full bg-indigo-500 opacity-60"></span> 正常评价</span>
              <span class="flex items-center gap-1"><span class="w-2.5 h-2.5 rounded-full bg-red-500 shadow-[0_0_6px_rgba(239,68,68,0.8)]"></span> 疑似恶意刷分</span>
            </div>
          </h3>
          
          <div class="flex-1 relative ml-8 mb-8 border-l-2 border-b-2 border-gray-300">
             
             <div class="absolute -left-12 top-0 bottom-0 flex flex-col justify-between text-[10px] text-gray-400 font-mono py-1">
               <span>100</span><span>75</span><span>50</span><span>25</span><span>0</span>
             </div>
             <div class="absolute -left-16 top-1/2 -rotate-90 origin-center text-xs font-bold text-gray-500 tracking-widest whitespace-nowrap">
               创新性得分
             </div>

             <div class="absolute left-0 right-0 -bottom-6 flex justify-between text-[10px] text-gray-400 font-mono px-1">
               <span>0</span><span>25</span><span>50</span><span>75</span><span>100</span>
             </div>
             <div class="absolute left-1/2 -bottom-10 -translate-x-1/2 text-xs font-bold text-gray-500 tracking-widest whitespace-nowrap">
               代码规范得分
             </div>

             <div class="absolute inset-0 grid grid-cols-4 grid-rows-4 pointer-events-none">
               <div v-for="i in 16" :key="i" class="border-t border-r border-gray-100/50"></div>
             </div>

             <div v-for="point in validScatterData" :key="point.id" 
                  class="absolute w-4 h-4 rounded-full -translate-x-1/2 translate-y-1/2 transition-all duration-500 cursor-pointer group shadow-sm hover:scale-150 hover:z-20"
                  :class="point.isAnomaly ? 'bg-red-500 shadow-[0_0_8px_rgba(239,68,68,0.8)] z-10' : 'bg-indigo-500/60 hover:bg-indigo-600'"
                  :style="{ left: `${point.codeScore}%`, bottom: `${point.innovateScore}%` }">
               
               <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-48 bg-gray-800 text-white text-xs p-3 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none shadow-xl">
                 <div class="font-bold text-indigo-300 border-b border-gray-600 pb-1 mb-1">评价流水号: #{{ point.id }}</div>
                 <div>打分人: {{ point.reviewer }}</div>
                 <div>受评人: {{ point.reviewee }}</div>
                 <div class="mt-1 font-mono text-gray-300">代码: {{ point.codeScore }} | 创新: {{ point.innovateScore }}</div>
                 <div v-if="point.isAnomaly" class="mt-1 text-red-400 font-bold border-t border-gray-600 pt-1">系统预警：分值极端</div>
               </div>
             </div>

          </div>
        </div>

        <div class="flex-[1] flex flex-col bg-white/40 border border-gray-100 rounded-2xl shadow-sm overflow-hidden">
          
          <div class="bg-red-50/50 px-4 py-3 border-b border-red-100/50 flex items-center justify-between shrink-0">
            <span class="font-bold text-red-800 text-sm flex items-center gap-2">
               <svg style="width: 16px; height: 16px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
               恶意评价拦截池
            </span>
          </div>

          <div class="flex-1 overflow-y-auto p-4 custom-scrollbar bg-gray-50/30">
            <div v-if="anomalies.length === 0" class="h-full flex flex-col items-center justify-center text-gray-400 text-sm">
              <svg style="width: 48px; height: 48px; flex-shrink: 0;" class="mb-3 opacity-50 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              暂未发现极端异常评分
            </div>
            
            <transition-group name="list" tag="div" class="space-y-3">
              <div v-for="item in anomalies" :key="item.id" class="bg-white border border-red-200 p-4 rounded-xl shadow-sm relative group overflow-hidden">
                <div class="absolute top-0 left-0 w-1 h-full bg-red-500"></div>
                
                <div class="flex justify-between items-start mb-2 pl-2">
                  <div class="text-xs font-bold text-gray-700">
                    <span class="text-red-600">{{ item.reviewer }}</span> 评 <span class="text-indigo-600">{{ item.reviewee }}</span>
                  </div>
                  <span class="text-[10px] font-mono bg-red-100 text-red-600 px-1.5 py-0.5 rounded border border-red-200">
                    代码:{{ item.codeScore }} 创新:{{ item.innovateScore }}
                  </span>
                </div>
                
                <p class="text-xs text-gray-500 mb-3 pl-2 italic">评语: "{{ item.comment || '无' }}"</p>
                
                <div class="pl-2 border-t border-gray-100 pt-3 flex gap-2">
                  <button @click="invalidateReview(item.id)" class="flex-1 py-1.5 bg-red-50 hover:bg-red-100 text-red-600 border border-red-200 rounded text-xs font-bold transition-colors">
                    一键作废退回
                  </button>
                  <button @click="ignoreAnomaly(item.id)" class="flex-1 py-1.5 bg-gray-50 hover:bg-gray-100 text-gray-600 border border-gray-200 rounded text-xs font-bold transition-colors">
                    忽略放行
                  </button>
                </div>
              </div>
            </transition-group>
          </div>
          
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 模拟散点图互评数据
const scatterData = ref([
  { id: 101, reviewer: '陈同学', reviewee: '李同学', codeScore: 85, innovateScore: 70, comment: '架构很清晰，SM4 轮函数抽离得很好。', isAnomaly: false },
  { id: 102, reviewer: '林同学', reviewee: '王同学', codeScore: 60, innovateScore: 65, comment: '勉强能跑，代码有点乱。', isAnomaly: false },
  { id: 103, reviewer: '赵同学', reviewee: '陈同学', codeScore: 90, innovateScore: 95, comment: '运用了位运算优化，非常牛。', isAnomaly: false },
  { id: 104, reviewer: '周同学', reviewee: '赵同学', codeScore: 75, innovateScore: 50, comment: '一般般。', isAnomaly: false },
  { id: 105, reviewer: '吴同学', reviewee: '林同学', codeScore: 40, innovateScore: 45, comment: '没有注释。', isAnomaly: false },
  
  // 模拟异常数据 (全 0 或 全 100 等极端恶意值)
  { id: 901, reviewer: '张同学', reviewee: '李同学', codeScore: 0, innovateScore: 0, comment: '垃圾', isAnomaly: true },
  { id: 902, reviewer: '张同学', reviewee: '王同学', codeScore: 5, innovateScore: 0, comment: '看不懂', isAnomaly: true },
  { id: 903, reviewer: '李同学', reviewee: '张同学', codeScore: 100, innovateScore: 100, comment: '兄弟互刷满分', isAnomaly: true },
])

// 过滤掉被作废的数据，保证图表动态更新
const validScatterData = computed(() => scatterData.value.filter(p => p.isActive !== false))

const anomalies = computed(() => validScatterData.value.filter(p => p.isAnomaly))
const anomalyCount = computed(() => anomalies.value.length)

const invalidateReview = (id) => {
  const item = scatterData.value.find(i => i.id === id)
  if (item) {
    item.isActive = false // 从图表和列表中隐藏作废
    alert(`已将 ${item.reviewer} 的恶意评价作废，并强制要求其重评。`)
  }
}

const ignoreAnomaly = (id) => {
  const item = scatterData.value.find(i => i.id === id)
  if (item) {
    item.isAnomaly = false // 解除红点预警，变回普通蓝点
  }
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(239, 68, 68, 0.3); border-radius: 4px; }

/* 异常列表移除动画 */
.list-leave-active { transition: all 0.5s ease; }
.list-leave-to { opacity: 0; transform: translateX(30px); }
</style>