<template>
  <div class="page-wrapper flex items-center justify-center w-full h-full min-h-[calc(100vh-100px)]">
    
    <div class="glass-card w-full max-w-6xl p-6 md:p-8 flex flex-col lg:flex-row gap-8 z-10 h-[700px]">
      
      <div class="flex-[1.2] flex flex-col h-full relative border-r border-gray-200/50 pr-0 lg:pr-8">
        <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: REQ_UPLOAD</div>
        <h1 class="text-3xl font-bold mb-3 text-gray-800">需求场景上传</h1>
        <p class="text-gray-500 text-sm mb-6 leading-relaxed bg-indigo-50/50 p-4 rounded-xl border border-indigo-100/50">
          <span class="font-bold text-indigo-600 block mb-1">场景描述：</span>
          {{ nodeConfig.scenario }}
        </p>

        <div class="flex-1 flex flex-col">
          <label class="font-bold text-gray-700 text-sm mb-2 flex items-center gap-2">
            <svg class="text-indigo-500" style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" /></svg>
            请输入您的需求文本
          </label>
          <textarea 
            v-model="inputText" 
            :disabled="isSubmitted"
            placeholder="请结合上述场景，详细描述您认为系统必须具备的功能或安全需求..." 
            class="flex-1 w-full bg-white/70 border border-gray-200 rounded-xl p-4 text-gray-700 outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100/50 transition-all resize-none custom-scrollbar mb-4 shadow-inner disabled:bg-gray-50 disabled:text-gray-400 disabled:cursor-not-allowed"
          ></textarea>
          
          <div class="text-right text-xs text-gray-400 mb-4">
            已输入: <span :class="inputText.length > 0 ? 'text-indigo-500 font-bold' : ''">{{ inputText.length }}</span> 字符
          </div>

          <div v-if="!isSubmitted">
            <button 
              class="hero-send-btn w-full justify-center py-3.5 rounded-xl shadow-lg transition-all duration-300"
              :class="!inputText.trim() ? 'opacity-50 cursor-not-allowed grayscale' : 'hover:shadow-indigo-500/40'"
              @click="submitRequirement"
              :disabled="!inputText.trim()"
            >
              <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" /></svg>
              提交需求并生成词云
            </button>
          </div>
          <div v-else class="animate-fade-in space-y-3">
             <div class="flex items-center gap-2 text-sm text-green-600 bg-green-50 p-3 rounded-xl border border-green-100 shadow-sm">
                <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                <span>需求已提交！满足前置条件。</span>
             </div>
             <button class="w-full py-3.5 bg-gray-800 hover:bg-gray-700 text-white font-bold rounded-xl shadow-lg transition-all flex items-center justify-center gap-2">
               进入下一实训环节
               <svg style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
             </button>
          </div>
        </div>
      </div>

      <div class="flex-[1.8] flex flex-col h-full bg-white/40 border border-gray-100 rounded-2xl p-6 shadow-sm overflow-hidden">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-bold text-gray-800 flex items-center gap-2">
            <svg class="text-purple-500" style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8h2a2 2 0 012 2v6a2 2 0 01-2 2h-2v4l-4-4H9a1.994 1.994 0 01-1.414-.586m0 0L11 14h4a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2v4l.586-.586z" /></svg>
            全班实时需求词云
          </h2>
          <span class="text-xs text-indigo-500 bg-indigo-50 px-3 py-1 rounded-full border border-indigo-100 flex items-center gap-1.5 animate-pulse">
            <span class="w-2 h-2 rounded-full bg-indigo-500"></span> Live Sync
          </span>
        </div>

        <div class="flex-1 flex flex-wrap content-center justify-center gap-x-6 gap-y-4 p-4 relative">
           <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-indigo-50/50 via-transparent to-transparent pointer-events-none"></div>
           
           <span v-for="(word, index) in mockWordCloud" :key="index"
                 class="transition-all duration-700 hover:scale-125 cursor-default"
                 :class="[
                   isSubmitted && word.isNew ? 'animate-pop-in' : '',
                   word.colorClass
                 ]"
                 :style="{ fontSize: word.size + 'px', fontWeight: word.weight }">
             {{ word.text }}
           </span>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const nodeConfig = ref({
  scenario: '在开发面向政企客户的安全办公协同系统时，需设计一套符合国密标准的加密传输方案。请描述你在该场景下认为核心的安全需求或技术痛点。'
})

const inputText = ref('')
const isSubmitted = ref(false)

// 模拟初始词云数据
const mockWordCloud = ref([
  { text: 'SM4加密', size: 42, weight: 900, colorClass: 'text-indigo-600' },
  { text: '性能瓶颈', size: 24, weight: 600, colorClass: 'text-purple-500' },
  { text: '密钥协商', size: 36, weight: 800, colorClass: 'text-blue-500' },
  { text: '硬件加速', size: 18, weight: 500, colorClass: 'text-gray-500' },
  { text: '国密改造', size: 28, weight: 700, colorClass: 'text-indigo-400' },
  { text: '合规性', size: 32, weight: 800, colorClass: 'text-pink-500' },
  { text: 'SM3摘要', size: 20, weight: 600, colorClass: 'text-gray-600' },
  { text: '安全通道', size: 26, weight: 700, colorClass: 'text-purple-400' },
])

const submitRequirement = () => {
  if (!inputText.value.trim()) return
  isSubmitted.value = true
  
  // 模拟：提交后，将用户的关键词动态插入到全班词云中
  setTimeout(() => {
    mockWordCloud.value.push({
      text: '我的最新需求',
      size: 48,
      weight: 900,
      colorClass: 'text-indigo-600 drop-shadow-md',
      isNew: true // 用于触发动画
    })
  }, 500)
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.3); border-radius: 4px; }

.animate-fade-in { animation: fadeIn 0.4s ease-out forwards; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-pop-in { animation: popIn 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards; }
@keyframes popIn {
  0% { opacity: 0; transform: scale(0.5); }
  100% { opacity: 1; transform: scale(1); }
}
</style>