<template>
  <div style="height: 100%;">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10">
      <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: AI PRACTICE</div>
      <h1 class="text-3xl font-bold mb-4 text-gradient-primary">AI练习监控</h1>
      <p class="text-gray-600 dark:text-gray-300 mb-4 text-sm leading-relaxed">
        监控学生AI对练互动过程，查看对话记录和学习进度
      </p>
      
      <div class="flex-1 bg-white/40 dark:bg-[#1e293b]/50 rounded-xl border border-dashed border-gray-300 dark:border-gray-700 flex flex-col items-center justify-center relative">
        <svg class="w-16 h-16 text-indigo-300 dark:text-indigo-700 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.879 7.519c1.171-1.025 3.071-1.025 4.242 0 1.172 1.025 1.172 2.687 0 3.712-.203.179-.43.326-.67.442-.745.361-1.45.999-1.45 1.827v.75M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9 5.25h.008v.008H12v-.008z" />
        </svg>
        <span class="text-sm text-gray-400 font-medium tracking-widest">AI练习监控面板</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const nodeConfig = reactive({
  desc: 'AI练习监控说明'
})
</script>