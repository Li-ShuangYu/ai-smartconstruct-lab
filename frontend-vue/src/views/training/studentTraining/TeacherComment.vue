<template>
  <div style="height: 100%;">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10">
      <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: TEACHER COMMENT</div>
      <h1 class="text-3xl font-bold mb-4 text-gradient-primary">教师点评</h1>
      <p class="text-gray-600 dark:text-gray-300 mb-4 text-sm leading-relaxed">
        查看教师的点评和反馈，提升学习效果
      </p>

      <div class="flex-1 bg-white/40 dark:bg-[#1e293b]/50 rounded-xl border border-dashed border-gray-300 dark:border-gray-700 flex flex-col items-center justify-center relative">
        <svg class="w-16 h-16 text-indigo-300 dark:text-indigo-700 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" />
        </svg>
        <span class="text-sm text-gray-400 font-medium tracking-widest">教师点评页面</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const nodeConfig = reactive({
  desc: '教师点评说明'
})
</script>
