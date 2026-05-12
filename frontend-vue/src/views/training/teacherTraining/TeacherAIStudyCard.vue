<template>
  <div style="height: 100vh;" class="p-6 bg-slate-50">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative overflow-hidden font-sans">
      
      <div class="flex justify-between items-center mb-6 shrink-0 border-b border-gray-200/50 pb-4">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase italic">Node: THEORY_MONITOR</div>
          <h2 class="text-2xl font-bold text-gray-800 flex items-center gap-2">理论实训全盘监控</h2>
          <p class="text-sm text-gray-500 mt-1">实时追踪学生学习进度、错题分布及 AI 提问情况。</p>
        </div>
        
        <div class="flex items-center gap-4">
          <div class="px-4 py-2 bg-green-50/80 rounded-lg border border-green-100 flex items-center gap-3">
            <span class="text-sm text-green-600 font-medium">全通关人数：</span>
            <span class="text-xl font-bold text-green-600">{{ completedTotal }} <span class="text-sm text-green-400 font-normal">/ {{ totalStudents }}</span></span>
          </div>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1.8] flex flex-col gap-6 overflow-hidden pr-2">
          
          <div class="bg-white/60 p-5 rounded-2xl border border-gray-200/60 shadow-sm shrink-0">
            <div class="flex justify-between items-center mb-4">
              <h3 class="text-sm font-bold text-gray-700 flex items-center gap-2">
                <svg class="w-4 h-4 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
                实时学习进度分布
              </h3>
            </div>
            
            <div class="flex items-stretch gap-2">
              <div 
                v-for="(stage, idx) in progressStages" 
                :key="idx"
                class="flex-1 relative group"
              >
                <div v-if="idx !== progressStages.length - 1" class="absolute top-1/2 -right-3 w-4 border-t-2 border-dashed border-gray-300 z-0"></div>
                
                <div class="bg-gray-50 border border-gray-100 rounded-xl p-3 text-center relative z-10 hover:border-indigo-300 transition-colors cursor-default"
                     :class="{'bg-green-50/50 border-green-200': stage.isFinal}">
                  <p class="text-[10px] text-gray-400 font-bold mb-1">{{ stage.name }}</p>
                  <p class="text-lg font-black" :class="stage.isFinal ? 'text-green-600' : 'text-indigo-600'">
                    {{ stage.count }}<span class="text-[10px] font-normal text-gray-400 ml-0.5">人</span>
                  </p>
                  <div class="w-full h-1 bg-gray-200 rounded-full mt-2 overflow-hidden">
                    <div class="h-full transition-all" 
                         :class="stage.isFinal ? 'bg-green-400' : 'bg-indigo-400'"
                         :style="{ width: `${(stage.count / totalStudents) * 100}%` }">
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="flex-1 flex flex-col bg-white/60 p-5 rounded-2xl border border-gray-200/60 shadow-sm min-h-0">
            <div class="flex justify-between items-center mb-4 shrink-0">
              <h3 class="text-sm font-bold text-gray-700 flex items-center gap-2">
                <svg class="w-4 h-4 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
                全班错题分布与干扰项分析
              </h3>
            </div>
            
            <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 space-y-4">
              <div 
                v-for="err in errorDistributions" 
                :key="err.id"
                class="p-4 border border-red-50 rounded-xl bg-red-50/10 hover:bg-red-50/30 transition-colors"
              >
                <div class="flex justify-between items-start mb-2">
                  <h4 class="text-sm font-bold text-gray-800 flex-1 pr-4">{{ err.question }}</h4>
                  <div class="shrink-0 text-center">
                    <span class="text-xs font-bold text-red-600 block">错误率</span>
                    <span class="text-lg font-black text-red-500">{{ err.errorRate }}%</span>
                  </div>
                </div>
                
                <div class="mt-3 space-y-2">
                  <div class="flex items-center gap-3 text-xs">
                    <span class="px-2 py-1 bg-green-100 text-green-700 rounded font-bold shrink-0">正确答案</span>
                    <span class="text-gray-600">{{ err.correctAnswer }}</span>
                  </div>
                  <div class="flex items-center gap-3 text-xs">
                    <span class="px-2 py-1 bg-orange-100 text-orange-700 rounded font-bold shrink-0">最高频错选</span>
                    <span class="text-gray-600 line-through opacity-70">{{ err.commonWrong }}</span>
                    <span class="text-orange-600 font-bold ml-auto">{{ err.wrongPercent }}% 的人错选此项</span>
                  </div>
                </div>
                
                <div class="mt-3 p-3 bg-white border border-gray-100 rounded-lg text-xs text-gray-500 italic">
                  💡 <strong>教学建议：</strong>{{ err.suggestion }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex-[1] flex flex-col min-w-[320px] max-w-[400px]">
          <div class="flex-1 bg-slate-900 rounded-2xl flex flex-col overflow-hidden shadow-xl border border-slate-800">
            
            <div class="p-4 bg-slate-800 flex flex-col gap-3 border-b border-slate-700 shrink-0">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <div class="w-8 h-8 bg-indigo-500 rounded-lg flex items-center justify-center">
                    <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" /></svg>
                  </div>
                  <div>
                    <h4 class="text-white text-sm font-bold">AI 伴学大盘监控</h4>
                    <p class="text-[10px] text-indigo-400">实时捕捉学生提问记录</p>
                  </div>
                </div>
              </div>
              
              <div class="pt-2 border-t border-slate-700">
                <p class="text-[10px] font-bold text-slate-400 mb-2 uppercase tracking-widest">全班高频提问概念</p>
                <div class="flex flex-wrap gap-2">
                  <span v-for="(tag, idx) in highFreqTags" :key="idx" class="text-[10px] px-2 py-1 bg-indigo-500/20 text-indigo-300 border border-indigo-500/30 rounded cursor-default">
                    {{ tag }}
                  </span>
                </div>
              </div>
            </div>

            <div class="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar-dark">
              <div v-for="(msg, idx) in realtimeQALogs" :key="idx" class="border-b border-slate-800/50 pb-4 last:border-0">
                <div class="flex justify-start mb-2">
                  <div class="max-w-[90%] bg-indigo-600/20 border border-indigo-500/30 p-2.5 rounded-xl rounded-tl-sm">
                    <div class="flex items-baseline gap-2 mb-1">
                      <span class="text-xs font-bold text-indigo-300">{{ msg.student }}</span>
                      <span class="text-[9px] text-slate-500">{{ msg.time }}</span>
                    </div>
                    <p class="text-xs text-white leading-relaxed">{{ msg.question }}</p>
                  </div>
                </div>
                <div class="flex justify-end">
                  <div class="max-w-[90%] bg-slate-800 border border-slate-700 p-2.5 rounded-xl rounded-tr-sm">
                    <div class="flex items-baseline gap-2 mb-1 justify-end">
                      <span class="text-[9px] text-slate-500">AI 引导回答</span>
                      <span class="text-xs font-bold text-purple-400">Socratic Bot</span>
                    </div>
                    <p class="text-[11px] text-slate-300 leading-relaxed italic">{{ msg.aiResponse }}</p>
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
import { ref } from 'vue'

const totalStudents = ref(32)
const completedTotal = ref(12)

// 阶段进度数据
const progressStages = ref([
  { name: '阅读: 物理存储', count: 2, isFinal: false },
  { name: '答题: 内存管理', count: 5, isFinal: false },
  { name: '阅读: 时间复杂度', count: 4, isFinal: false },
  { name: '答题: 性能分析', count: 9, isFinal: false },
  { name: '全部通关', count: 12, isFinal: true }
])

// 错题分布数据
const errorDistributions = ref([
  {
    id: 1,
    question: '在内存管理上，Python 列表（List）存放的是什么？',
    errorRate: 45,
    correctAnswer: '数据的引用地址',
    commonWrong: '数据本身',
    wrongPercent: 80, // 错的人里有80%选了这个
    suggestion: '大部分学生将 Python 的 List 误解为 C 语言的原始数组。建议在实训总结时画图对比“值传递”与“引用传递”在内存中的物理形态。'
  },
  {
    id: 2,
    question: '对一个拥有 100 万个元素的列表执行 list[999999] 操作，其复杂度是？',
    errorRate: 28,
    correctAnswer: 'O(1)',
    commonWrong: 'O(n)',
    wrongPercent: 65,
    suggestion: '学生混淆了“链表遍历”与“数组计算寻址”。需强调公式：物理地址 = 起始地址 + 索引 * 步长，这是一个基础的数学乘法运算，耗时恒定。'
  }
])

// AI 面板数据
const highFreqTags = ['动态扩容算法', '引用类型 vs 值类型', 'O(1)寻址原理', '底层C语言结构']

const realtimeQALogs = ref([
  {
    student: '刘洋',
    time: '10:42',
    question: '如果存的是引用地址，那怎么知道真正的数字放在内存的哪里？',
    aiResponse: '好问题！引用地址本身就是一块“路牌”。你可以想象一下，Python 在创建数字时把它放在了内存的某个地方，然后把这个“坐标”写在了列表中。所以 CPU 只需要顺着坐标找过去就行了，你觉得这个找的过程需要遍历吗？'
  },
  {
    student: '王磊',
    time: '10:38',
    question: 'O(1) 的意思是不管多大，时间都完全一样吗？一微秒都不差？',
    aiResponse: 'O(1) 在算法层面代表“常数时间”，意味着它不随数据量 n 线性增长。但在物理层面，由于 CPU 缓存命中率（Cache Line）等因素，极微小的时间波动是存在的。但这不影响算法复杂度的定性分析。'
  },
  {
    student: '张伟',
    time: '10:35',
    question: '既然都是引用，那为什么列表里面不能全存二进制代码？',
    aiResponse: '列表本质上存的是 Python 对象的指针（PyObject*）。如果你想存纯粹的二进制连续数据，为了节省内存，通常会用什么结构呢？（提示：可以查一下 Python 的 bytes 或 array 模块）。'
  }
])
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

.custom-scrollbar-dark::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar-dark::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar-dark::-webkit-scrollbar-thumb {
  background: #475569;
  border-radius: 10px;
}
</style>