<template>
  <div class="page-wrapper w-full min-h-screen relative overflow-hidden">
    
    <div class="absolute inset-0 pointer-events-none overflow-hidden">
      <div class="absolute top-10 left-1/4 w-32 h-32 bg-yellow-300 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-pulse"></div>
      <div class="absolute bottom-20 right-1/4 w-40 h-40 bg-pink-300 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-pulse" style="animation-delay: 1s;"></div>
      <div class="absolute top-1/3 right-1/3 w-48 h-48 bg-indigo-300 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-pulse" style="animation-delay: 2s;"></div>
    </div>

    <div class="glass-card w-full h-full p-10 md:p-14 flex flex-col items-center justify-center z-10 text-center relative border border-white/50 shadow-2xl animate-pop-in">
      
      <div class="w-24 h-24 mb-6 rounded-full bg-gradient-to-br from-yellow-100 to-amber-100 flex items-center justify-center shadow-inner border border-amber-200">
        <svg style="width: 48px; height: 48px; flex-shrink: 0;" class="text-amber-500 drop-shadow-sm" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" /></svg>
      </div>

      <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: END</div>
      <h1 class="text-4xl font-black mb-6 text-gray-800 tracking-tight">实训圆满结束！</h1>
      
      <div class="bg-indigo-50/50 border border-indigo-100 rounded-2xl p-6 mb-10 w-full relative">
        <svg style="width: 24px; height: 24px; flex-shrink: 0;" class="absolute top-4 left-4 text-indigo-200" fill="currentColor" viewBox="0 0 24 24"><path d="M14.017 21v-7.391c0-5.704 3.731-9.57 8.983-10.609l.995 2.151c-2.432.917-3.995 3.638-3.995 5.849h4v10h-9.983zm-14.017 0v-7.391c0-5.704 3.748-9.57 9-10.609l.996 2.151c-2.433.917-3.996 3.638-3.996 5.849h4v10h-10z"/></svg>
        <p class="text-gray-700 text-lg leading-relaxed relative z-10 px-6 font-medium">
          {{ nodeConfig.desc || '恭喜您完成了本次“密码学综合应用实践”的所有环节！系统已为您生成专属的实训学习报告，记录了您的代码产出、互评得分与教师寄语。' }}
        </p>
      </div>

      <div class="flex flex-col sm:flex-row gap-4 w-full justify-center">
        <button 
          @click="downloadReport" 
          class="flex-1 flex items-center justify-center gap-2 px-8 py-4 bg-white border-2 border-indigo-500 text-indigo-600 font-bold rounded-xl hover:bg-indigo-50 hover:shadow-lg transition-all"
        >
          <svg style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
          下载我的实训报告 (PDF)
        </button>
        
        <button 
          @click="exitRoom" 
          class="hero-send-btn flex-1 !py-4 justify-center text-lg shadow-lg hover:shadow-indigo-500/40"
        >
          <svg style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" /></svg>
          退出实训流转
        </button>
      </div>

      <div class="mt-8 text-xs text-gray-400">
        当前即为终点。退出后您仍可在“历史归档”中查阅本次记录。
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 节点编排配置
const nodeConfig = ref({
  desc: '恭喜您完成了本次“密码学综合应用实践”的所有环节！无论是在 SM4 的代码实现还是理论探索中，你都展现出了优秀的极客素养。系统已为您生成专属的实训学习报告，请妥善保存。'
})

const downloadReport = () => {
  alert("正在请求后端生成个人 PDF 报告，请稍候...")
}

const exitRoom = () => {
  if(confirm("确定要退出当前的实训房间吗？")) {
    alert("已退出实训，跳转至个人仪表盘！")
  }
}
</script>

<style scoped>
.animate-pop-in { animation: popIn 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards; }
@keyframes popIn {
  0% { opacity: 0; transform: scale(0.9) translateY(30px); }
  100% { opacity: 1; transform: scale(1) translateY(0); }
}
</style>