<template>
  <div class="page-wrapper w-full min-h-screen">
    
    <div class="glass-card w-full h-full p-8 md:p-12 flex flex-col items-center justify-center z-10 relative overflow-hidden transition-all duration-700">
      
      <div v-if="!isPublished" class="flex flex-col items-center justify-center text-center animate-fade-in w-full">
        
        <div class="relative w-40 h-40 mb-8 flex items-center justify-center">
          <div class="absolute inset-0 border-4 border-indigo-200 rounded-full border-t-indigo-500 animate-spin" style="animation-duration: 3s;"></div>
          <div class="absolute inset-2 border-4 border-purple-200 rounded-full border-b-purple-500 animate-spin" style="animation-duration: 2s; animation-direction: reverse;"></div>
          <svg style="width: 48px; height: 48px; flex-shrink: 0;" class="text-indigo-600 animate-pulse" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg>
        </div>
        
        <h1 class="text-3xl font-bold text-gray-800 mb-4 tracking-wider">实训数据结算中</h1>
        <p class="text-gray-500 max-w-lg leading-relaxed">
          系统正在汇总您在本次实训中前 15 个节点的所有交互数据（包括资源阅读、编码实训、课后作业及防作弊考核等）。<br>
          <span class="text-indigo-500 font-bold mt-2 inline-block">请耐心等待教师进行最终的综合打分与评语下发。</span>
        </p>
      </div>

      <div v-else class="w-full animate-pop-in flex flex-col h-full">
        <div class="absolute -top-32 -right-32 w-96 h-96 bg-indigo-400 rounded-full mix-blend-multiply filter blur-3xl opacity-20 pointer-events-none"></div>
        <div class="absolute -bottom-32 -left-32 w-96 h-96 bg-purple-400 rounded-full mix-blend-multiply filter blur-3xl opacity-20 pointer-events-none"></div>
        
        <div class="text-center mb-10">
          <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-green-400 to-emerald-600 text-white rounded-full shadow-lg mb-4">
             <svg style="width: 32px; height: 32px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
          </div>
          <h1 class="text-4xl font-black text-gray-800 tracking-tight">实训结业报告</h1>
          <p class="text-gray-500 mt-2 font-medium">SM4 密码学综合应用实践</p>
        </div>

        <div class="flex flex-col md:flex-row gap-8 flex-1">
          <div class="flex-[1] flex flex-col items-center justify-center bg-white/50 rounded-3xl border border-indigo-50 shadow-sm p-8 relative overflow-hidden">
            <div class="absolute top-0 w-full h-2 bg-gradient-to-r from-indigo-500 to-purple-500"></div>
            <span class="text-sm font-bold text-gray-400 uppercase tracking-widest mb-2">FINAL SCORE</span>
            <div class="text-7xl font-black text-gradient-primary drop-shadow-sm mb-4">{{ reportData.finalScore }}</div>
            
            <div class="w-full space-y-3 mt-6">
              <div v-for="(val, key) in reportData.breakdown" :key="key" class="flex justify-between items-center text-sm">
                <span class="text-gray-600 font-medium">{{ key }}</span>
                <div class="flex items-center gap-3 flex-1 ml-4">
                  <div class="flex-1 bg-gray-200 rounded-full h-1.5"><div class="bg-indigo-400 h-1.5 rounded-full" :style="{ width: val + '%' }"></div></div>
                  <span class="font-bold text-gray-700 w-8 text-right">{{ val }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="flex-[1.5] flex flex-col bg-white/60 border border-gray-100 rounded-3xl p-8 shadow-sm">
            <h3 class="text-lg font-bold text-indigo-900 mb-6 flex items-center gap-2 border-b border-gray-200 pb-3">
               <svg style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" /></svg>
               导师综合评语
            </h3>
            
            <div class="flex-1 relative">
              <svg style="width: 48px; height: 48px; flex-shrink: 0;" class="absolute -top-4 -left-4 text-indigo-100 z-0" fill="currentColor" viewBox="0 0 24 24"><path d="M14.017 21v-7.391c0-5.704 3.731-9.57 8.983-10.609l.995 2.151c-2.432.917-3.995 3.638-3.995 5.849h4v10h-9.983zm-14.017 0v-7.391c0-5.704 3.748-9.57 9-10.609l.996 2.151c-2.433.917-3.996 3.638-3.996 5.849h4v10h-10z"/></svg>
              <p class="relative z-10 text-gray-700 leading-loose text-lg italic px-6 pt-4">
                {{ reportData.teacherComment }}
              </p>
            </div>
            
            <div class="mt-6 flex justify-end items-center gap-3">
              <div class="text-right">
                <div class="font-bold text-gray-800">王教授 (密码学导师)</div>
                <div class="text-xs text-gray-500">2026-05-08 16:30 签发</div>
              </div>
              <div class="w-12 h-12 bg-indigo-100 rounded-full border-2 border-white shadow-sm flex items-center justify-center text-indigo-500 font-bold">王</div>
            </div>
          </div>
        </div>

        <button class="hero-send-btn w-full justify-center py-4 mt-8 text-lg rounded-xl" @click="finishTraining">
          确认查收，结束本次实训
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 模拟发布状态，实际开发中通过 WebSocket 或轮询获取
const isPublished = ref(false)

const reportData = ref({
  finalScore: 92,
  breakdown: {
    '客观题考核': 95,
    '代码规范与逻辑': 88,
    '预习与互动活跃度': 90,
    '同伴互评得分': 94
  },
  teacherComment: '在本次 SM4 算法实训中表现优异。特别是你的轮函数代码实现，运用了巧妙的位运算优化，极大地提升了执行效率。在理论考核中也展现了扎实的基础。希望在后续的非对称加密课程中继续保持这种探索精神！'
})

// 用于演示的后门：点击背景空白处切换发布状态
// 实际使用时请删除
setTimeout(() => {
  // 模拟老师5秒后发布了成绩
  isPublished.value = true
}, 5000)

const finishTraining = () => {
  alert("实训圆满结束，即将返回系统首页！")
}
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.8s ease-out forwards; }
@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.animate-pop-in { animation: popIn 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards; }
@keyframes popIn {
  0% { opacity: 0; transform: translateY(20px) scale(0.95); }
  100% { opacity: 1; transform: translateY(0) scale(1); }
}
</style>