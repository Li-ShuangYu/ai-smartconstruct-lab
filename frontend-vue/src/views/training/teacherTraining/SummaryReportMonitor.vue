<template>
  <div class="page-wrapper flex items-center justify-center w-full h-full min-h-[calc(100vh-100px)]">
    
    <div class="glass-card w-full max-w-[1400px] p-6 md:p-8 flex flex-col z-10 h-[850px] relative overflow-hidden">
      
      <div class="flex justify-between items-end mb-8 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase flex items-center gap-2">
            <svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" /></svg>
            Node: END Monitor
          </div>
          <h1 class="text-3xl font-bold text-gray-800">班级宏观分析与实训归档</h1>
        </div>
        
        <div class="text-sm font-bold text-gray-500 bg-white/60 px-4 py-2 rounded-lg border border-gray-100">
          批次：<span class="text-indigo-600">2026春季·密码学综合实践 (房间号 #9902)</span>
        </div>
      </div>

      <div class="flex-1 flex flex-col lg:flex-row gap-6 min-h-0 mb-6">
        
        <div class="flex-[1.2] flex flex-col gap-6">
          
          <div class="grid grid-cols-2 gap-4">
            <div class="bg-white/60 border border-gray-100 rounded-2xl p-6 shadow-sm flex flex-col justify-center">
              <span class="text-sm text-gray-500 mb-2">班级平均总分</span>
              <div class="text-4xl font-black text-indigo-600">86.5 <span class="text-lg text-gray-400 font-bold">分</span></div>
            </div>
            <div class="bg-white/60 border border-gray-100 rounded-2xl p-6 shadow-sm flex flex-col justify-center">
              <span class="text-sm text-gray-500 mb-2">结业通关率</span>
              <div class="text-4xl font-black text-green-500">93.7 <span class="text-lg text-gray-400 font-bold">%</span></div>
            </div>
          </div>

          <div class="flex-1 bg-white/50 border border-gray-200/60 rounded-2xl p-6 shadow-sm flex flex-col">
            <h3 class="font-bold text-gray-700 text-sm mb-6">最终成绩正态分布概览</h3>
            <div class="flex-1 flex items-end gap-2 justify-between px-4 pb-6 border-b border-gray-200 relative">
               
               <div v-for="bar in distribution" :key="bar.range" class="flex flex-col items-center w-full group">
                 <div class="w-full max-w-[40px] bg-indigo-200 rounded-t-sm transition-all group-hover:bg-indigo-400 relative flex items-end justify-center"
                      :style="{ height: `${bar.percent}%` }">
                   <span class="absolute -top-6 text-xs font-bold text-indigo-600 opacity-0 group-hover:opacity-100 transition-opacity">
                     {{ bar.count }}人
                   </span>
                 </div>
                 <span class="text-[10px] text-gray-500 mt-2 whitespace-nowrap">{{ bar.range }}</span>
               </div>

            </div>
          </div>
        </div>

        <div class="flex-[1.8] flex flex-col bg-white/40 border border-gray-100 rounded-2xl shadow-sm overflow-hidden p-6">
          <h3 class="font-bold text-gray-800 text-lg mb-6 border-b border-gray-200 pb-3">AI 教学复盘简报</h3>
          
          <div class="space-y-6 overflow-y-auto custom-scrollbar pr-2">
            <div class="p-4 bg-green-50/50 border border-green-100 rounded-xl">
              <h4 class="font-bold text-green-700 text-sm mb-2 flex items-center gap-2">
                <svg style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" /></svg>
                高光时刻 (Highlights)
              </h4>
              <p class="text-sm text-gray-600 leading-relaxed">
                全班在 <span class="font-bold text-green-600">SM4 轮函数代码实现</span> 环节表现优异，代码查重率低且创新性互评得分高。其中，陈同学、赵同学的代码被多次置顶推荐。
              </p>
            </div>

            <div class="p-4 bg-amber-50/50 border border-amber-100 rounded-xl">
              <h4 class="font-bold text-amber-700 text-sm mb-2 flex items-center gap-2">
                <svg style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
                薄弱环节 (Weaknesses)
              </h4>
              <p class="text-sm text-gray-600 leading-relaxed">
                根据【防作弊监考大盘】与【知识难度自评】交叉分析，<span class="font-bold text-amber-600">扩展欧几里得算法</span> 依然是核心盲区，部分学生在考核时出现频繁切屏查阅资料的现象。
              </p>
            </div>
            
            <button class="w-full py-3 bg-white border border-gray-300 text-gray-700 font-bold rounded-lg shadow-sm hover:bg-gray-50 flex items-center justify-center gap-2 transition-colors">
              <svg style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
              导出完整班级学情分析报告 (Excel/PDF)
            </button>
          </div>
        </div>
      </div>

      <div class="mt-auto shrink-0 bg-red-50/30 border border-red-200 rounded-2xl p-6 flex items-center justify-between">
        <div>
          <h3 class="text-red-700 font-bold text-lg flex items-center gap-2">
            <svg style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" /></svg>
            危险操作区 (Danger Zone)
          </h3>
          <p class="text-sm text-red-500/80 mt-1">此操作将封存本次实训的所有数据记录，并结束整个房间的生命周期，学生将无法再进入。</p>
        </div>
        
        <button 
          @click="archiveAndClose" 
          class="px-8 py-3 bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl shadow-lg transition-all focus:ring-4 focus:ring-red-200 flex items-center gap-2"
        >
          <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" /></svg>
          一键归档数据并结束实训
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 模拟成绩分布数据 (percent 用于控制柱状图高度)
const distribution = ref([
  { range: '<60分', count: 2, percent: 10 },
  { range: '60-70分', count: 4, percent: 20 },
  { range: '70-80分', count: 6, percent: 40 },
  { range: '80-90分', count: 12, percent: 80 },
  { range: '90-100分', count: 8, percent: 60 },
])

const archiveAndClose = () => {
  const confirmText = prompt("此操作不可逆！如果确认要归档并关闭房间，请在下方输入 'CLOSE':")
  if (confirmText === 'CLOSE') {
    alert("数据已成功归档！房间生命周期已结束，即将返回教师控制台大盘。")
  } else if (confirmText !== null) {
    alert("输入有误，操作已取消。")
  }
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.2); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.4); }
</style>