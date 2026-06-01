<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 flex flex-col md:flex-row gap-6 z-10 relative overflow-hidden">
      
      <div class="flex-[3] flex flex-col min-h-0 gap-4">
        <div class="flex justify-between items-end shrink-0">
          <div>
            <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: VIDEO_WATCH</div>
            <h2 class="text-2xl font-bold text-gray-800">{{ videoTitle }}</h2>
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
          <video
            ref="videoRef"
            class="absolute inset-0 w-full h-full object-contain"
            src="/src/assets/docs/冒泡排序演示视频.mp4"
            loop
            muted
            playsinline
            autoplay
          ></video>

          <div class="absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-black/80 to-transparent flex flex-col gap-2">
            <div class="w-full h-1.5 bg-white/20 rounded-full cursor-pointer relative overflow-hidden">
               <div class="absolute h-full bg-indigo-500 transition-all" :style="{ width: `${progress}%` }"></div>
               <div class="absolute h-full bg-white/30" :style="{ width: `${watchProgress}%` }"></div>
            </div>

            <div class="flex justify-between items-center text-white">
              <div class="flex items-center gap-4">
                <button class="hover:text-indigo-400"><svg class="w-5 h-5 fill-current" viewBox="0 0 24 24"><path d="M8 5v14l11-7z" /></svg></button>
              </div>
              <div class="flex items-center gap-6 text-sm font-medium">
                <div class="flex items-center gap-2">
                  <span>倍速</span>
                  <select class="bg-transparent border-none focus:ring-0 text-indigo-400 cursor-pointer" v-model="playbackRate">
                    <option v-for="rate in speedOptions" :key="rate" :value="rate">{{ rate }}x</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
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
              v-for="(point, index) in knowledgePointsData" 
              :key="index"
              @click="seekTo(point.time)"
              class="group cursor-pointer p-3 rounded-xl border transition-all relative overflow-hidden"
              :class="[
                currentTime >= point.time && (index === knowledgePointsData.length - 1 || currentTime < knowledgePointsData[index+1].time)
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
              <div v-if="currentTime >= point.time && (index === knowledgePointsData.length - 1 || currentTime < knowledgePointsData[index+1].time)" 
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
            @click="handleComplete"
            class="hero-send-btn w-full justify-center text-base py-3.5 rounded-xl shadow-lg transition-all"
          >
            <span class="flex items-center gap-2">
              完成观看
            </span>
          </button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  nodeInstanceId: { type: Number, default: null },
  nodeConfig: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['complete'])

const speedOptions = computed(() => props.nodeConfig?.speedOptions || [1.0, 1.25, 1.5, 2.0])
const videoTitle = computed(() => props.nodeConfig?.video_title || 'Python 数据结构核心概念解析')
const videoName = computed(() => props.nodeConfig?.video_name || '冒泡排序演示视频.mp4')

const knowledgePointsData = computed(() => props.nodeConfig?.knowledge_points || [
  { time: 0, title: '课程导学', desc: '本课程内容概览' },
  { time: 60, title: '冒泡排序原理', desc: '相邻元素两两比较交换' },
  { time: 180, title: '算法复杂度分析', desc: '时间与空间复杂度详解' },
  { time: 360, title: '代码实现演示', desc: 'Python代码逐步实现' },
  { time: 540, title: '优化与变种', desc: '冒泡排序的改进思路' },
  { time: 720, title: '总结', desc: '知识点回顾与练习' }
])

// 播放状态管理
const currentTime = ref(500)
const duration = ref(600)
const playbackRate = ref(1.0)
const showSubtitles = ref(true)
const isVerifying = ref(false)
const watchTime = ref(500)

const isCompleted = ref(false)

const videoRef = ref<HTMLVideoElement | null>(null)

const currentSubtitle = computed(() => {
  if (currentTime.value < 85) return '冒泡排序是最基础的排序算法之一...'
  if (currentTime.value < 240) return '通过两两比较相邻元素，将较大的元素逐步"冒泡"到末尾。'
  return '优化思路：当某轮未发生交换时，说明数组已有序，可提前终止。'
})

const progress = computed(() => (currentTime.value / duration.value) * 100)
const watchProgress = computed(() => (watchTime.value / duration.value) * 100)

const formattedCurrentTime = computed(() => formatTime(currentTime.value))
const formattedDuration = computed(() => formatTime(duration.value))

function formatTime(seconds: number) {
  const m = Math.floor(seconds / 60).toString().padStart(2, '0')
  const s = Math.floor(seconds % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}

const seekTo = (time: number) => { currentTime.value = time }

const handleComplete = () => {
  emit('complete')
}

let videoTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  videoTimer = setInterval(() => {
    currentTime.value += playbackRate.value
    if (currentTime.value > watchTime.value) {
      watchTime.value = currentTime.value
    }
  }, 1000)
})

onUnmounted(() => {
  if (videoTimer) clearInterval(videoTimer)
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