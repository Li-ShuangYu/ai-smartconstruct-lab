<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 flex flex-col md:flex-row gap-6 z-10 relative overflow-hidden">
      
      <div class="flex-[3] flex flex-col min-h-0 gap-4">
        <div class="flex justify-between items-end shrink-0">
          <div>
            <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: VIDEO_WATCH</div>
            <h2 class="text-2xl font-bold text-gray-800">Python 数组操作深度解析</h2>
          </div>
          <div class="flex items-center gap-3 text-sm text-gray-500 bg-white/50 px-3 py-1.5 rounded-lg border border-gray-100">
            <span class="flex items-center gap-1">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              已看: {{ formattedCurrentTime }}
            </span>
            <span class="text-gray-300">|</span>
            <span>总长: {{ formattedDuration }}</span>
          </div>
        </div>

        <div class="flex-1 bg-black rounded-2xl overflow-hidden shadow-2xl relative group border-4 border-white/10">
          <div class="absolute inset-0 flex items-center justify-center bg-gradient-to-br from-slate-900 to-indigo-900">
             <div class="text-center group-hover:scale-110 transition-transform duration-500">
                <div class="w-20 h-20 bg-white/10 rounded-full flex items-center justify-center backdrop-blur-md border border-white/20 mb-4 mx-auto cursor-pointer">
                   <svg class="w-10 h-10 text-white fill-current" viewBox="0 0 24 24"><path d="M8 5v14l11-7z" /></svg>
                </div>
                <p class="text-indigo-200 text-sm tracking-widest font-light">正在播放：高级数组切片技巧.mp4</p>
             </div>
          </div>

          <div v-if="showSubtitles" class="absolute bottom-20 left-0 right-0 px-10 text-center pointer-events-none">
            <span class="px-4 py-1.5 bg-black/60 backdrop-blur-sm text-white rounded-lg text-lg border border-white/10">
              {{ currentSubtitle }}
            </span>
          </div>

          <div class="absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-black/80 to-transparent flex flex-col gap-2">
            <div class="w-full h-1.5 bg-white/20 rounded-full cursor-pointer relative overflow-hidden group/progress">
               <div class="absolute h-full bg-indigo-500 transition-all" :style="{ width: `${progress}%` }"></div>
               <div class="absolute h-full bg-white/30" :style="{ width: `${watchProgress}%` }"></div>
            </div>
            
            <div class="flex justify-between items-center text-white">
              <div class="flex items-center gap-4">
                <button class="hover:text-indigo-400"><svg class="w-5 h-5 fill-current" viewBox="0 0 24 24"><path d="M8 5v14l11-7z" /></svg></button>
                <button class="hover:text-indigo-400"><svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" /></svg></button>
              </div>
              <div class="flex items-center gap-6 text-sm font-medium">
                <div class="flex items-center gap-2">
                  <span>倍速</span>
                  <select class="bg-transparent border-none focus:ring-0 text-indigo-400 cursor-pointer" v-model="playbackRate">
                    <option v-for="rate in nodeConfig.speedOptions" :key="rate" :value="rate">{{ rate }}x</option>
                  </select>
                </div>
                <button @click="showSubtitles = !showSubtitles" :class="showSubtitles ? 'text-indigo-400' : 'text-white/60'">字幕</button>
                <button class="hover:text-indigo-400">全屏</button>
              </div>
            </div>
          </div>

          <Transition name="fade">
            <div v-if="isVerifying" class="absolute inset-0 bg-indigo-900/40 backdrop-blur-md flex items-center justify-center z-50">
              <div class="bg-white p-8 rounded-2xl shadow-2xl text-center max-w-xs border border-indigo-100">
                <div class="w-16 h-16 bg-amber-100 text-amber-600 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
                </div>
                <h3 class="font-bold text-gray-800 text-lg mb-2">学习确认</h3>
                <p class="text-sm text-gray-500 mb-6">点击下方按钮证明您正在认真观看，倒计时结束将自动暂停</p>
                <button @click="closeVerification" class="w-full py-3 bg-indigo-600 text-white rounded-xl font-bold hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200">
                  我正在观看
                </button>
              </div>
            </div>
          </Transition>
        </div>
      </div>

      <div class="flex-[1] min-w-[300px] flex flex-col h-full gap-4">
        <div class="flex flex-col flex-1 border border-gray-200/60 bg-white/40 rounded-xl overflow-hidden shadow-inner min-h-0">
          <div class="h-12 border-b border-gray-200/60 bg-white/50 backdrop-blur-md flex items-center px-4 shrink-0 justify-between">
            <span class="font-bold text-gray-700 text-sm flex items-center gap-2">
               <svg class="text-indigo-500 w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
               AI 知识切片
            </span>
            <span class="text-[10px] text-gray-400 uppercase font-mono">Real-time Analysis</span>
          </div>

          <div class="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar">
            <div 
              v-for="(point, index) in knowledgePoints" 
              :key="index"
              @click="seekTo(point.time)"
              class="group cursor-pointer p-3 rounded-xl border transition-all relative overflow-hidden"
              :class="[
                currentTime >= point.time && (index === knowledgePoints.length - 1 || currentTime < knowledgePoints[index+1].time)
                ? 'bg-indigo-50 border-indigo-200 ring-1 ring-indigo-100'
                : 'bg-white/60 border-transparent hover:border-gray-200 hover:bg-white/80'
              ]"
            >
              <div class="flex gap-3 relative z-10">
                <div class="w-20 h-12 bg-slate-200 rounded-lg overflow-hidden shrink-0 border border-white relative">
                   <div class="absolute inset-0 bg-gradient-to-br from-indigo-500/20 to-purple-500/20"></div>
                   <div class="absolute bottom-1 right-1 bg-black/60 text-[9px] text-white px-1 rounded">{{ formatTime(point.time) }}</div>
                </div>
                <div class="flex-1 min-w-0">
                  <h4 class="text-xs font-bold text-gray-800 truncate">{{ point.title }}</h4>
                  <p class="text-[10px] text-gray-500 mt-1 line-clamp-1 italic">{{ point.desc }}</p>
                </div>
              </div>
              <div v-if="currentTime >= point.time && (index === knowledgePoints.length - 1 || currentTime < knowledgePoints[index+1].time)" 
                class="absolute left-0 top-0 bottom-0 w-1 bg-indigo-500"></div>
            </div>
          </div>
        </div>

        <div class="shrink-0 space-y-4 pt-2">
          <div class="p-3 bg-indigo-50/50 rounded-xl border border-indigo-100">
             <div class="flex justify-between text-[11px] mb-1.5">
                <span class="text-indigo-600 font-bold uppercase tracking-tight">学习完成度</span>
                <span class="text-indigo-600 font-bold">{{ Math.round(watchProgress) }}%</span>
             </div>
             <div class="w-full bg-indigo-200/30 rounded-full h-1.5 overflow-hidden">
                <div class="bg-indigo-500 h-full transition-all duration-700" :style="{ width: `${watchProgress}%` }"></div>
             </div>
          </div>

          <button 
            class="hero-send-btn w-full justify-center text-base py-3.5 rounded-xl shadow-lg transition-all flex flex-col items-center"
            :class="{'opacity-50 grayscale cursor-not-allowed': !isUnlocked, 'hover:shadow-indigo-500/30': isUnlocked}"
            :disabled="!isUnlocked"
          >
            <span class="flex items-center gap-2">
              完成本节观看
              <svg style="width: 16px; height: 16px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
            </span>
            <span v-if="!isUnlocked" class="text-[10px] opacity-70 mt-1 font-normal">需观看至 {{ nodeConfig.unlockThreshold }}% 解锁</span>
          </button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

// 节点编排配置
const nodeConfig = {
  speedOptions: [1.0, 1.25, 1.5, 2.0],
  unlockThreshold: 80, // 80% 解锁
  verificationInterval: 300 // 5分钟弹窗一次(秒)
}

// 播放状态管理
const currentTime = ref(125) // 模拟当前播放到125秒
const duration = ref(600) // 模拟总长10分钟
const playbackRate = ref(1.0)
const showSubtitles = ref(true)
const isVerifying = ref(false)
const watchTime = ref(125) // 实际观看时长（防跳过计算）

// AI 知识点数据
const knowledgePoints = [
  { time: 0, title: '课程简介与环境准备', desc: '快速搭建 Python 数组实验环境' },
  { time: 85, title: '数组的底层存储机制', desc: '图解连续内存分配与索引计算' },
  { time: 240, title: '常用切片操作 (Slicing)', desc: '掌握 [start:stop:step] 核心逻辑' },
  { time: 420, title: '数组扩容与性能损耗', desc: '理解动态列表的摊还时间复杂度' },
  { time: 540, title: '总结与课后练习说明', desc: '本节核心要点复盘' }
]

// 模拟字幕逻辑
const currentSubtitle = computed(() => {
  if (currentTime.value < 85) return "接下来我们将深入探讨 Python 数组的索引机制..."
  if (currentTime.value < 240) return "注意：索引 0 指向的是内存中的起始偏移量。"
  return "通过这种切片方式，我们可以极大地提高代码的可读性。"
})

// 计算属性
const progress = computed(() => (currentTime.value / duration.value) * 100)
const watchProgress = computed(() => (watchTime.value / duration.value) * 100)
const isUnlocked = computed(() => watchProgress.value >= nodeConfig.unlockThreshold)

const formattedCurrentTime = computed(() => formatTime(currentTime.value))
const formattedDuration = computed(() => formatTime(duration.value))

// 工具函数
function formatTime(seconds) {
  const m = Math.floor(seconds / 60).toString().padStart(2, '0')
  const s = Math.floor(seconds % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}

const seekTo = (time) => {
  currentTime.value = time
  // 逻辑：如果点击的是已看过的部分，更新当前时间；
  // 如果是未看过的，可根据业务需求限制或允许
}

const closeVerification = () => {
  isVerifying.value = false
}

// 模拟计时器逻辑
let videoTimer = null
let verificationTimer = null

onMounted(() => {
  // 模拟播放进度
  videoTimer = setInterval(() => {
    if (!isVerifying.value) {
      currentTime.value += playbackRate.value
      if (currentTime.value > watchTime.value) {
        watchTime.value = currentTime.value // 只有往前播才算观看进度
      }
    }
  }, 1000)

  // 模拟随机防挂机检测
  verificationTimer = setInterval(() => {
    isVerifying.value = true
  }, nodeConfig.verificationInterval * 1000)
})

onUnmounted(() => {
  clearInterval(videoTimer)
  clearInterval(verificationTimer)
})
</script>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 1.5rem;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.07);
}

.hero-send-btn {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: white;
}

.hero-send-btn:not(:disabled):hover {
  transform: translateY(-2px);
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

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>