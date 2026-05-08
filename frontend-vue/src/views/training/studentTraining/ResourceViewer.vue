<template>
  <div class="page-wrapper w-full min-h-screen">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10">
      
      <div class="flex-[3] flex flex-col border border-gray-200/60 bg-white/40 rounded-xl overflow-hidden shadow-inner relative">
        <div class="h-14 border-b border-gray-200/60 bg-white/50 backdrop-blur-md flex items-center justify-between px-5">
          <div class="flex items-center gap-3">
            <svg class="text-indigo-500" style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <span class="font-bold text-gray-700 text-lg">{{ nodeConfig.resourceName }}</span>
          </div>
          <div class="flex items-center gap-4 text-sm text-gray-500">
            <button class="hover:text-indigo-600 transition-colors" @click="prevPage" :disabled="currentPage === 1">上一页</button>
            <span class="font-medium bg-gray-100/80 px-3 py-1 rounded-md">{{ currentPage }} / {{ totalPages }}</span>
            <button class="hover:text-indigo-600 transition-colors" @click="nextPage" :disabled="currentPage === totalPages">下一页</button>
          </div>
        </div>

        <div class="flex-1 flex flex-col items-center justify-center bg-gray-50/30 overflow-y-auto p-8 group">
          <svg class="text-gray-300 mb-4 transition-transform group-hover:scale-105" style="width: 64px; height: 64px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor">
             <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
          </svg>
          <p class="text-gray-400 font-medium tracking-wider">正在渲染 PDF 资源内容...</p>
        </div>
      </div>

      <div class="flex-[1] min-w-[280px] flex flex-col justify-between border-t md:border-t-0 md:border-l border-gray-200/60 pt-6 md:pt-0 md:pl-6">
        
        <div>
          <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: RESOURCE_READ</div>
          <h2 class="text-2xl font-bold mb-6 text-gray-800">资源阅读</h2>

          <div class="bg-white/50 border border-gray-100 rounded-xl p-5 mb-6 shadow-sm">
            <h3 class="text-sm font-semibold text-gray-600 mb-4 flex items-center gap-2">
               <svg class="text-indigo-400" style="width: 16px; height: 16px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
               学习进度追踪
            </h3>
            
            <div class="mb-4">
              <div class="flex justify-between text-xs mb-1">
                <span class="text-gray-500">阅读覆盖率</span>
                <span class="font-bold text-indigo-600">{{ readPercentage }}%</span>
              </div>
              <div class="w-full bg-gray-200 rounded-full h-2">
                <div class="bg-gradient-to-r from-indigo-400 to-purple-500 h-2 rounded-full transition-all duration-300" :style="{ width: readPercentage + '%' }"></div>
              </div>
            </div>

            <div>
              <div class="flex justify-between text-xs mb-1">
                <span class="text-gray-500">有效停留时长</span>
                <span class="font-bold text-gray-700">{{ formattedTime }} / {{ requiredTimeStr }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="space-y-4">
          <div v-if="isConditionMet" class="flex items-center gap-2 text-sm text-green-600 bg-green-50 p-3 rounded-lg border border-green-100">
             <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
             <span>前置条件已达成，可进入下一环节</span>
          </div>
          <div v-else class="flex items-center gap-2 text-sm text-amber-600 bg-amber-50 p-3 rounded-lg border border-amber-100">
             <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
             <span>请继续阅读并保持页面停留</span>
          </div>

          <button 
            class="hero-send-btn w-full justify-center text-base py-3.5 rounded-xl shadow-lg transition-all"
            :class="{'opacity-50 grayscale cursor-not-allowed': !isConditionMet, 'hover:shadow-indigo-500/30': isConditionMet}"
            :disabled="!isConditionMet"
          >
            完成学习并继续
             <svg style="width: 16px; height: 16px; margin-left: 4px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
          </button>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

// 节点编排配置
const nodeConfig = ref({
  resourceId: 'doc-crypto-101',
  resourceName: '密码学基础与SM4算法导论.pdf'
})

// 阅读状态模拟
const currentPage = ref(1)
const totalPages = ref(10)
const timeSpent = ref(0) // 单位：秒
const requiredTime = ref(60) // 设定阈值：60秒

// 进度计算 (模拟通过翻页增加阅读覆盖率)
const readPercentage = computed(() => {
  return Math.min(Math.round((currentPage.value / totalPages.value) * 100), 100)
})

const isConditionMet = computed(() => {
  return readPercentage.value === 100 || timeSpent.value >= requiredTime.value
})

const formattedTime = computed(() => {
  const m = Math.floor(timeSpent.value / 60).toString().padStart(2, '0')
  const s = (timeSpent.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

const requiredTimeStr = computed(() => {
  const m = Math.floor(requiredTime.value / 60).toString().padStart(2, '0')
  const s = (requiredTime.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

// 模拟翻页操作
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++ }
const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }

// 模拟时长计时器
let timer = null
onMounted(() => {
  timer = setInterval(() => { timeSpent.value++ }, 1000)
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>