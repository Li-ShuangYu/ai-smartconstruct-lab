<template>
  <div class="page-wrapper w-full min-h-screen">
    
    <div class="glass-card w-full h-full p-8 flex flex-col md:flex-row gap-10 z-10">
      
      <div class="flex-1 flex flex-col justify-center">
        <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: START</div>
        <h1 class="text-4xl font-bold mb-4 text-gradient-primary">准备阶段</h1>
        <p class="text-gray-600 dark:text-gray-300 mb-6 text-base leading-relaxed">
          {{ nodeConfig.desc || '实训流程的说明：本环节为实训准备阶段，请确认本地网络正常后，点击就位。' }}
        </p>

        <div class="w-full h-56 bg-white/40 dark:bg-black/20 rounded-xl border border-dashed border-indigo-200 dark:border-indigo-800/50 flex flex-col items-center justify-center relative overflow-hidden group">
          <svg class="text-indigo-300 mb-2 group-hover:scale-110 transition-transform" style="width: 40px; height: 40px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
          </svg>
          <span class="text-sm text-indigo-400 font-medium tracking-widest z-10">实训全局引导图</span>
        </div>
      </div>

      <div class="w-full md:w-72 flex flex-col justify-center border-t md:border-t-0 md:border-l border-gray-200 dark:border-gray-700/50 pt-6 md:pt-0 md:pl-10">
        
        <div class="mb-8 w-full p-4 rounded-xl bg-green-50 dark:bg-green-900/10 border border-green-100 dark:border-green-800/30 flex items-start gap-3">
            <svg class="text-green-500 mt-0.5" style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
          <div>
            <h4 class="text-sm font-semibold text-green-700 dark:text-green-400">网络状态正常</h4>
            <p class="text-xs text-green-600/70 dark:text-green-500/70 mt-1">满足下一步前置条件</p>
          </div>
        </div>

        <button 
          class="hero-send-btn w-full justify-center text-base py-3.5 rounded-xl shadow-lg hover:shadow-indigo-500/30 transition-all"
          :class="{'opacity-60 cursor-not-allowed': isReady}"
          @click="handleReady"
          :disabled="isReady"
        >
          <svg v-if="!isReady" class="icon-sm" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
          {{ isReady ? '已就位，等待指令...' : '准备就绪 / 进入实训' }}
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

// 节点编排配置注入
const nodeConfig = reactive({
  desc: '实训流程的说明：本次实训将涵盖密码学基础配置与多节点协同实验，请确认网络畅通。'
})

const isReady = ref(false)

const handleReady = () => {
  if (isReady.value) return
  // 模拟发送就位状态给教师端
  isReady.value = true
}
</script>