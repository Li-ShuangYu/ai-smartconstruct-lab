<template>
  <div style="height: 100%;">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 font-sans">
      
      <div class="flex justify-between items-center mb-4 shrink-0 border-b border-gray-200/50 pb-4">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase italic">Node: AI_SPARRING</div>
          <h2 class="text-xl font-bold text-gray-800">AI 专注对练 (苏格拉底启发式)</h2>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[2.5] flex flex-col bg-white/60 border border-gray-200/60 rounded-2xl overflow-hidden shadow-sm">
          
          <div class="flex-1 overflow-y-auto p-6 space-y-6 custom-scrollbar" ref="chatContainer">
            <div 
              v-for="(msg, index) in messages" 
              :key="index"
              class="flex"
              :class="msg.role === 'user' ? 'justify-end' : 'justify-start'"
            >
              <div v-if="msg.role === 'ai'" class="flex gap-3 max-w-[80%]">
                <div class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center shrink-0">
                  <svg class="w-5 h-5 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
                </div>
                <div>
                  <div class="bg-gray-50 border border-gray-100 text-gray-700 px-5 py-3 rounded-2xl rounded-tl-sm text-sm leading-relaxed whitespace-pre-wrap">
                    {{ msg.content }}
                  </div>
                  <div v-if="msg.scoreDelta" class="mt-1.5 ml-1 text-[11px] font-bold flex items-center gap-1" :class="msg.scoreDelta > 0 ? 'text-green-600' : 'text-red-500'">
                    <svg v-if="msg.scoreDelta > 0" class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" /></svg>
                    <svg v-else class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6" /></svg>
                    表现评分 {{ msg.scoreDelta > 0 ? '+' : '' }}{{ msg.scoreDelta }}
                  </div>
                </div>
              </div>

              <div v-else class="flex gap-3 max-w-[80%] flex-row-reverse">
                <div class="w-8 h-8 rounded-full bg-indigo-500 flex items-center justify-center shrink-0 text-white text-xs font-bold">我</div>
                <div class="bg-indigo-600 text-white px-5 py-3 rounded-2xl rounded-tr-sm text-sm leading-relaxed whitespace-pre-wrap">
                  {{ msg.content }}
                </div>
              </div>
            </div>

            <div v-if="isTyping" class="flex gap-3 max-w-[80%]">
              <div class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center shrink-0">
                <svg class="w-5 h-5 text-indigo-600 animate-pulse" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01" /></svg>
              </div>
              <div class="bg-gray-50 border border-gray-100 px-5 py-3 rounded-2xl rounded-tl-sm flex items-center gap-1">
                <div class="w-2 h-2 bg-gray-300 rounded-full animate-bounce"></div>
                <div class="w-2 h-2 bg-gray-300 rounded-full animate-bounce" style="animation-delay: 0.1s"></div>
                <div class="w-2 h-2 bg-gray-300 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
              </div>
            </div>
          </div>

          <div class="shrink-0 p-4 bg-white border-t border-gray-100">
            <div class="flex items-end gap-3 relative">
              <textarea 
                v-model="userInput"
                @keydown.enter.prevent="sendMessage"
                :disabled="isTyping || isPassed"
                placeholder="在此输入你的回答 (Enter 发送)..."
                class="flex-1 bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/30 focus:border-indigo-500 resize-none custom-scrollbar transition-all disabled:opacity-50"
                rows="2"
              ></textarea>
              <button 
                @click="sendMessage"
                :disabled="!userInput.trim() || isTyping || isPassed"
                class="px-6 py-3 h-[46px] bg-indigo-600 text-white font-bold rounded-xl hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors shrink-0"
              >
                发送
              </button>
            </div>
          </div>
        </div>

        <div class="flex-[1] flex flex-col gap-4">
          
          <div class="flex-1 bg-white/60 border border-gray-200/60 rounded-2xl p-6 shadow-sm flex flex-col">
            <h3 class="text-sm font-bold text-gray-500 uppercase tracking-widest mb-6 border-b border-gray-100 pb-2">对话判定指标</h3>
            
            <div class="flex-1 space-y-8">
              
              <div>
                <div class="flex justify-between items-end mb-2">
                  <span class="text-xs font-bold text-indigo-900">对练综合得分 (需达到 {{ passThreshold }} 分)</span>
                  <span class="text-xl font-mono font-bold" :class="score >= passThreshold ? 'text-green-600' : 'text-indigo-600'">
                    {{ score }}
                  </span>
                </div>
                <div class="w-full h-2 bg-gray-100 rounded-full overflow-hidden relative">
                  <div class="absolute h-full transition-all duration-500"
                       :class="score >= passThreshold ? 'bg-green-500' : 'bg-indigo-500'"
                       :style="{ width: Math.min((score / passThreshold) * 100, 100) + '%' }">
                  </div>
                  <div class="absolute top-0 bottom-0 w-0.5 bg-red-400 z-10" :style="{ left: '100%' }"></div>
                </div>
              </div>

              <div>
                <div class="flex justify-between items-end mb-3">
                  <span class="text-xs font-bold text-indigo-900">核心概念覆盖度 (需全覆盖)</span>
                  <span class="text-sm font-mono font-bold text-indigo-600">{{ coveredConceptsCount }}/{{ concepts.length }}</span>
                </div>
                
                <div class="space-y-2">
                  <div 
                    v-for="(concept, idx) in concepts" 
                    :key="idx"
                    class="flex items-center gap-3 p-2.5 rounded-lg border transition-all"
                    :class="concept.covered ? 'bg-green-50 border-green-200 text-green-700' : 'bg-gray-50 border-gray-200 text-gray-400'"
                  >
                    <svg v-if="concept.covered" class="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                    <div v-else class="w-4 h-4 shrink-0 rounded-full border-2 border-gray-300"></div>
                    <span class="text-xs font-medium">{{ concept.name }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="shrink-0 pt-6 mt-4 border-t border-gray-100">
              <div v-if="isPassed" class="mb-4 p-3 bg-green-50 text-green-700 text-xs font-bold rounded-lg border border-green-200 flex items-center gap-2">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                概念全覆盖且分数达标，可通关！
              </div>
              <div v-else-if="coveredConceptsCount === concepts.length && score < passThreshold" class="mb-4 p-3 bg-amber-50 text-amber-700 text-xs font-bold rounded-lg border border-amber-200 flex items-center gap-2">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
                概念已满，但分数不足，请继续回答以获取加分。
              </div>
              
              <button 
                class="hero-send-btn w-full justify-center py-4 rounded-xl text-base font-bold shadow-lg transition-all flex items-center gap-2"
                :class="{'opacity-50 grayscale cursor-not-allowed': !isPassed, 'hover:shadow-indigo-500/30': isPassed}"
                :disabled="!isPassed"
                @click="nextStep"
              >
                结束对练并继续
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
              </button>
            </div>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue'

// 得分阈值设置
const passThreshold = 80
const score = ref(50) // 初始底分

// 核心数据模型 (增加 scoreDelta 字段)
const messages = reactive([
  { 
    role: 'ai', 
    content: '我们在上一节学习了数组的基础概念。现在考考你：如果你尝试访问一个长度为 5 的列表的第 10 个元素（例如 `arr[9]`），Python 会作何反应？',
    scoreDelta: null
  }
])

// 概念覆盖考核点
const concepts = reactive([
  { name: 'IndexError 异常机制', covered: false },
  { name: '边界防护思维', covered: false }
])

const userInput = ref('')
const isTyping = ref(false)
const chatContainer = ref(null)

const coveredConceptsCount = computed(() => concepts.filter(c => c.covered).length)
// 【核心修改】：通过条件变为了 概念全覆盖 + 分数达标
const isPassed = computed(() => coveredConceptsCount.value === concepts.length && score.value >= passThreshold)

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

// 模拟消息发送逻辑 (对接后端接口)
const sendMessage = () => {
  const text = userInput.value.trim()
  if (!text || isTyping.value || isPassed.value) return

  messages.push({ role: 'user', content: text })
  userInput.value = ''
  scrollToBottom()
  
  isTyping.value = true
  
  // 模拟后端接口响应延迟及评分逻辑
  setTimeout(() => {
    isTyping.value = false
    
    // 情景 1: 回答错误，扣分但不点亮概念
    if (text.includes('返回空') || text.includes('返回 null')) {
      score.value = Math.max(0, score.value - 10)
      messages.push({ 
        role: 'ai', 
        content: '不对哦，Python 不像 JavaScript 会返回 undefined。请再仔细想想，它会抛出什么样的具体错误？',
        scoreDelta: -10
      })
    } 
    // 情景 2: 回答正确，加分并点亮概念
    else {
      if (!concepts[0].covered) {
        concepts[0].covered = true
        score.value += 20
        messages.push({ 
          role: 'ai', 
          content: '回答得非常好！它会直接抛出 IndexError 崩溃。那在实际编写代码时，为了避免整个程序因为这条语句崩溃，我们通常会使用什么语法来进行边界防护？',
          scoreDelta: 20
        })
      } else if (!concepts[1].covered) {
        concepts[1].covered = true
        score.value += 15
        messages.push({ 
          role: 'ai', 
          content: '完美！使用 try...except 或者提前使用 len(arr) 判断长度都是极佳的防护手段。',
          scoreDelta: 15
        })
      }
    }
    
    scrollToBottom()
  }, 1000)
}

const nextStep = () => {
  alert('恭喜！分数达标且概念覆盖完成，进入下一环节！')
}
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

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}
</style>