<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 flex flex-col md:flex-row gap-6 z-10">
      
      <div class="flex-[3] flex flex-col h-full relative">
        <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: THEORY_CLASS</div>
        <h1 class="text-3xl font-bold mb-4 text-gradient-primary">{{ nodeConfig.topic }}</h1>
        
        <div class="flex items-center gap-2 mb-4">
          <div class="flex-1 bg-gray-200/50 rounded-full h-2 overflow-hidden">
            <div class="bg-gradient-to-r from-indigo-400 to-purple-500 h-2 rounded-full transition-all duration-300" :style="{ width: progressPercentage + '%' }"></div>
          </div>
          <span class="text-xs font-bold text-indigo-600 w-10 text-right">{{ currentCardIndex + 1 }} / {{ cards.length }}</span>
        </div>

        <div class="flex-1 bg-white/60 border border-indigo-50 rounded-2xl p-8 flex flex-col shadow-sm relative overflow-hidden group">
          <svg class="absolute -bottom-10 -right-10 text-indigo-50 opacity-50 group-hover:scale-110 transition-transform duration-700" style="width: 250px; height: 250px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg>
          
          <div class="relative z-10 flex-1">
            <h2 class="text-2xl font-bold text-gray-800 mb-6">{{ currentCard.title }}</h2>
            <div class="text-gray-600 leading-relaxed space-y-4 text-lg">
              <p>{{ currentCard.content }}</p>
            </div>
          </div>

          <div class="relative z-10 flex justify-between items-center mt-6 pt-6 border-t border-indigo-100">
            <button class="px-4 py-2 text-indigo-600 font-medium hover:bg-indigo-50 rounded-lg transition-colors disabled:opacity-30" @click="prevCard" :disabled="currentCardIndex === 0">
              &larr; 上一张
            </button>
            <button class="hero-send-btn px-6 py-2 shadow-md" @click="nextCard">
              {{ currentCardIndex === cards.length - 1 ? '完成阅读' : '下一张 &rarr;' }}
            </button>
          </div>
        </div>
      </div>

      <div class="flex-[2] min-w-[320px] flex flex-col bg-white/40 border border-gray-100 rounded-2xl overflow-hidden shadow-sm">
        
        <div class="bg-indigo-50/50 p-4 border-b border-indigo-100/50 flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-gradient-to-tr from-indigo-500 to-purple-500 flex items-center justify-center shadow-inner">
             <svg class="text-white" style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
          </div>
          <div>
            <h3 class="font-bold text-gray-800 text-sm">AI 理论助教</h3>
            <p class="text-xs text-indigo-500">随时为你解答卡片中的疑惑</p>
          </div>
        </div>

        <div class="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar bg-gray-50/30">
          <div v-for="(msg, index) in chatHistory" :key="index" class="flex flex-col" :class="msg.role === 'user' ? 'items-end' : 'items-start'">
            <span class="text-[10px] text-gray-400 mb-1 px-1">{{ msg.role === 'user' ? '你' : 'AI 助教' }}</span>
            <div class="max-w-[85%] p-3 rounded-2xl text-sm" 
                 :class="msg.role === 'user' ? 'bg-indigo-500 text-white rounded-tr-sm shadow-md' : 'bg-white text-gray-700 rounded-tl-sm border border-gray-100 shadow-sm'">
              {{ msg.content }}
            </div>
          </div>
        </div>

        <div class="p-4 bg-white/60 border-t border-gray-100">
          <div class="flex gap-2">
            <input 
              v-model="inputText" 
              @keyup.enter="sendMessage"
              type="text" 
              placeholder="向 AI 提问关于本页内容的疑惑..." 
              class="flex-1 bg-white border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 transition-all input-container"
            >
            <button @click="sendMessage" class="hero-send-btn !p-2.5 !rounded-xl" :disabled="!inputText.trim()">
               <svg class="text-white" style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" /></svg>
            </button>
          </div>
        </div>

      </div>
    </div>

    <div class="fixed bottom-8 right-8 z-50 animate-fade-in" v-if="isAllRead">
       <button class="hero-send-btn !px-8 !py-4 shadow-xl hover:shadow-indigo-500/40 text-lg rounded-full flex items-center gap-3">
         <svg class="text-white" style="width: 24px; height: 24px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
         卡片阅读完毕，进入下一环节
       </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const nodeConfig = ref({
  topic: 'SM4 密码算法底层原理',
  aiPrompt: '你是一个密码学专家...'
})

// 模拟卡片数据
const cards = ref([
  { id: 1, title: '分组密码基本概念', content: '分组密码是将明文消息编码表示后的数字序列，划分成长度为 n 的组，每个组分别在密钥的控制下变换成等长的输出数字序列...' },
  { id: 2, title: 'SM4 算法结构', content: 'SM4 密码算法是一个分组算法。该算法的分组长度为 128 比特，密钥长度为 128 比特。加密算法与密钥扩展算法都采用 32 轮非线性迭代结构...' },
  { id: 3, title: '轮函数 (Round Function)', content: 'SM4 的轮函数 F 包含一个非线性变换 τ 和一个线性变换 L。这是算法实现混淆和扩散的核心部分...' }
])

const currentCardIndex = ref(0)
const viewedCards = ref(new Set([0])) // 记录已看过的卡片索引

const currentCard = computed(() => cards.value[currentCardIndex.value])
const progressPercentage = computed(() => ((viewedCards.value.size) / cards.value.length) * 100)
const isAllRead = computed(() => viewedCards.value.size === cards.value.length)

const nextCard = () => {
  if (currentCardIndex.value < cards.value.length - 1) {
    currentCardIndex.value++
    viewedCards.value.add(currentCardIndex.value)
  }
}

const prevCard = () => {
  if (currentCardIndex.value > 0) currentCardIndex.value--
}

// AI 聊天逻辑
const inputText = ref('')
const chatHistory = ref([
  { role: 'assistant', content: '你好！我是你的 AI 理论助教。关于左侧的知识卡片，如果你有任何看不懂的地方，随时问我。' }
])

const sendMessage = () => {
  if (!inputText.value.trim()) return
  chatHistory.value.push({ role: 'user', content: inputText.value })
  const userMsg = inputText.value
  inputText.value = ''
  
  // 模拟 AI 回复
  setTimeout(() => {
    chatHistory.value.push({ role: 'assistant', content: `关于你说的"${userMsg}"，在SM4算法中，这主要是为了保证非线性变换的安全性...` })
  }, 1000)
}
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.5s ease-out forwards; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
/* 细长滚动条 */
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.3); border-radius: 4px; }
</style>