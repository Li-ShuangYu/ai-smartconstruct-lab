<template>
  <div style="height: 100%;">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10">
      <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: VIDEO PLAYER</div>
      <h1 class="text-3xl font-bold mb-4 text-gradient-primary">视频播放监控</h1>
      <p class="text-gray-600 dark:text-gray-300 mb-4 text-sm leading-relaxed">
        监控学生视频观看进度，查看播放统计数据
      </p>
      
      <div class="flex-1 bg-white/40 dark:bg-[#1e293b]/50 rounded-xl border border-dashed border-gray-300 dark:border-gray-700 flex flex-col items-center justify-center relative">
        <svg class="w-16 h-16 text-indigo-300 dark:text-indigo-700 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span class="text-sm text-gray-400 font-medium tracking-widest">视频播放监控面板</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const nodeConfig = reactive({
  desc: '视频播放监控说明'
})
</script>