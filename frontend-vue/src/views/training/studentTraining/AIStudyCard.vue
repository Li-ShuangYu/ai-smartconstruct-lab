<template>
  <div style="height: 100%;">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative overflow-hidden font-sans">
      
      <div class="flex justify-between items-center mb-6 shrink-0 border-b border-gray-200/50 pb-4">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase italic">Node: THEORY_PRACTICE</div>
          <h2 class="text-2xl font-bold text-gray-800">Python 数组深度理论实训</h2>
        </div>
        
        <div class="flex items-center gap-6">
          <div class="flex flex-col items-end gap-1">
            <div class="flex items-center gap-2">
              <span class="text-[10px] font-bold text-gray-400">已解锁进度</span>
              <span class="text-sm font-mono font-bold text-indigo-600">{{ completedCount }} / {{ cards.length }}</span>
            </div>
            <div class="w-48 h-1.5 bg-gray-100 rounded-full overflow-hidden">
              <div class="h-full bg-indigo-500 transition-all duration-500" :style="{ width: (completedCount / cards.length) * 100 + '%' }"></div>
            </div>
          </div>
          
          <button 
            @click="handleNextNode"
            class="hero-send-btn px-6 py-2.5 rounded-xl text-sm font-bold shadow-lg transition-all flex items-center gap-2"
            :class="{
              'opacity-50 grayscale cursor-not-allowed': completedCount < cards.length || isWaiting,
              'hover:shadow-indigo-500/30': completedCount >= cards.length && !isWaiting && isTeacherConfirmed
            }"
            :disabled="completedCount < cards.length || isWaiting"
          >
            {{ isTeacherConfirmed ? '进入下一节点' : (isWaiting ? '等待教师进入下一节点' : '完成卡片学习') }}
            <svg v-if="!isWaiting" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" /></svg>
            <svg v-else class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
          </button>
        </div>
      </div>

      <div class="flex-1 flex gap-8 min-h-0">
        
        <div class="flex-[1.5] flex flex-col gap-6 overflow-y-auto custom-scrollbar pr-4 pb-20">
          <div 
            v-for="(card, index) in cards" 
            :key="card.id"
            class="learning-card group"
            :class="[
              card.status === 'locked' ? 'opacity-40 grayscale pointer-events-none scale-95' : 'opacity-100',
              card.status === 'completed' ? 'border-green-100 bg-green-50/10' : 'border-gray-100 bg-white/60'
            ]"
          >
            <div class="absolute -left-3 top-6 w-8 h-8 rounded-lg bg-white shadow-md border border-gray-100 flex items-center justify-center font-bold text-xs text-indigo-500 z-10">
              {{ index + 1 }}
            </div>

            <div v-if="card.type === 'content'" class="p-8">
              <h3 class="text-lg font-bold text-gray-800 mb-4">{{ card.title }}</h3>
              <div class="text-sm text-gray-600 leading-loose whitespace-pre-wrap mb-6">{{ card.content }}</div>
              <button 
                v-if="card.status === 'active'"
                @click="completeCard(index)"
                class="px-5 py-2 bg-indigo-600 text-white rounded-lg text-xs font-bold hover:bg-indigo-700 transition-colors"
              >
                已读完，下一步
              </button>
              <div v-if="card.status === 'completed'" class="flex items-center gap-2 text-green-600 text-xs font-bold">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                已完成阅读
              </div>
            </div>

            <div v-else-if="card.type === 'quiz'" class="p-8">
              <div class="flex justify-between items-start mb-4">
                <h3 class="text-lg font-bold text-gray-800">思考练习</h3>
                <span class="px-2 py-1 bg-amber-100 text-amber-700 rounded text-[10px] font-bold">交互问答</span>
              </div>
              <p class="text-sm text-gray-700 font-medium mb-6">{{ card.question }}</p>
              
              <div class="space-y-3 mb-6">
                <div 
                  v-for="(opt, optIdx) in card.options" 
                  :key="optIdx"
                  @click="submitQuiz(index, optIdx)"
                  class="p-4 rounded-xl border text-sm transition-all cursor-pointer flex justify-between items-center"
                  :class="[
                    card.selected === optIdx ? (optIdx === card.answer ? 'bg-green-50 border-green-200 text-green-700' : 'bg-red-50 border-red-200 text-red-700') : 'bg-white border-gray-100 hover:border-indigo-200'
                  ]"
                >
                  <span>{{ opt }}</span>
                  <svg v-if="card.selected === optIdx && optIdx === card.answer" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                </div>
              </div>

              <div v-if="card.status === 'wrong'" class="p-4 bg-red-50/50 border border-red-100 rounded-xl mb-4">
                <p class="text-xs text-red-600 leading-relaxed font-medium">
                  <strong>AI 解析：</strong>{{ card.explanation }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="flex-[1] flex flex-col gap-4">
          <div class="flex-1 bg-slate-900 rounded-2xl flex flex-col overflow-hidden shadow-xl border border-slate-800">
            <div class="p-4 bg-slate-800 flex items-center justify-between border-b border-slate-700">
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 bg-indigo-500 rounded-lg flex items-center justify-center">
                  <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
                </div>
                <div>
                  <h4 class="text-white text-xs font-bold">启发式 AI 导师</h4>
                  <p class="text-[10px] text-indigo-400">正在引导学习...</p>
                </div>
              </div>
              <button class="text-slate-400 hover:text-white transition-colors" @click="showWrongBook = true">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" /></svg>
              </button>
            </div>

            <div class="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar-dark">
              <div v-for="(msg, idx) in chatHistory" :key="idx" class="flex" :class="msg.role === 'user' ? 'justify-end' : 'justify-start'">
                <div 
                  class="max-w-[85%] p-3 rounded-xl text-xs leading-relaxed"
                  :class="msg.role === 'user' ? 'bg-indigo-600 text-white' : 'bg-slate-800 text-slate-300 border border-slate-700'"
                >
                  {{ msg.text }}
                </div>
              </div>
            </div>

            <div class="p-4 bg-slate-800 border-t border-slate-700">
              <div class="relative">
                <input 
                  v-model="userQuestion"
                  @keyup.enter="askAI"
                  type="text" 
                  placeholder="遇到困难？向 AI 提问获取启发..."
                  class="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-3 text-xs text-white focus:ring-1 focus:ring-indigo-500 outline-none pr-10"
                />
                <button @click="askAI" class="absolute right-2 top-2 p-1.5 text-indigo-400 hover:text-indigo-300 transition-colors">
                  <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" /></svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>

    <div v-if="showWrongBook" class="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center p-6">
      <div class="bg-white w-full max-w-lg rounded-3xl shadow-2xl flex flex-col max-h-[80vh] overflow-hidden">
        <div class="p-6 border-b border-gray-100 flex justify-between items-center bg-indigo-50/50">
          <h3 class="font-bold text-gray-800 flex items-center gap-2">
            <svg class="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" /></svg>
            我的错题本 ({{ wrongBook.length }})
          </h3>
          <button @click="showWrongBook = false" class="text-gray-400 hover:text-gray-600">
            <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
          </button>
        </div>
        <div class="flex-1 overflow-y-auto p-6 space-y-4 custom-scrollbar">
          <div v-if="wrongBook.length === 0" class="text-center py-10 text-gray-400 italic text-sm">暂无错题记录，继续保持！</div>
          <div v-for="(item, idx) in wrongBook" :key="idx" class="p-4 border border-red-50 rounded-2xl bg-red-50/20">
             <p class="text-sm font-bold text-gray-700 mb-2">{{ item.question }}</p>
             <p class="text-[10px] text-red-500 font-bold mb-1">您的错误选择: {{ item.selectedText }}</p>
             <p class="text-xs text-gray-500 leading-relaxed italic bg-white p-3 rounded-lg border border-red-100">AI 解析：{{ item.explanation }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const completedCount = ref(0)
const showWrongBook = ref(false)
const userQuestion = ref('')
const wrongBook = reactive([])

// 按钮状态管理
const isWaiting = ref(false) // 是否正在等待教师确认
const isTeacherConfirmed = ref(false) // 教师是否已确认

const cards = reactive([
  { 
    id: 1, 
    type: 'content', 
    title: 'Python 数组的物理存储', 
    status: 'active', 
    content: '在 Python 中，我们最常使用的“数组”其实是 List。它在内存中是连续分配的，每个元素存储的是对象的引用地址。这意味着 List 可以存放不同类型的数据，但也带来了一定的内存开销。' 
  },
  { 
    id: 2, 
    type: 'quiz', 
    question: '在内存管理上，Python 列表（List）存放的是什么？', 
    options: ['数据本身', '数据的引用地址', '随机指针', '二进制代码'], 
    answer: 1, 
    selected: null, 
    status: 'locked',
    explanation: 'Python List 是一个动态数组，存储的是指向真实对象的指针（引用），而不是对象本身，所以它才能支持异构数据类型。' 
  },
  { 
    id: 3, 
    type: 'content', 
    title: '索引的时间复杂度', 
    status: 'locked', 
    content: '由于数组在内存中是连续的，通过索引（Index）访问元素的时间复杂度是 O(1)。无论数组有多大，访问第 1 个元素和第 10000 个元素的速度是一样快的。' 
  },
  { 
    id: 4, 
    type: 'quiz', 
    question: '对一个拥有 100 万个元素的列表执行 list[999999] 操作，其复杂度是？', 
    options: ['O(n)', 'O(log n)', 'O(1)', 'O(n^2)'], 
    answer: 2, 
    selected: null, 
    status: 'locked',
    explanation: '数组支持随机访问（Random Access），寻址计算公式为：地址 = 起始地址 + 索引 * 偏移量。这是一个常数阶操作。' 
  }
])

const chatHistory = reactive([
  { role: 'assistant', text: '你好！我是你的实训引导员。在阅读卡片时有任何逻辑不通的地方，随时可以问我。我会尽量通过启发的方式引导你思考。' }
])

const completeCard = (index) => {
  cards[index].status = 'completed'
  completedCount.value++
  if (index + 1 < cards.length) {
    cards[index + 1].status = 'active'
  }
}

const submitQuiz = (cardIdx, optIdx) => {
  const card = cards[cardIdx]
  if (card.status === 'completed') return
  
  card.selected = optIdx
  if (optIdx === card.answer) {
    card.status = 'completed'
    completedCount.value++
    if (cardIdx + 1 < cards.length) {
      cards[cardIdx + 1].status = 'active'
    }
    chatHistory.push({ role: 'assistant', text: '回答正确！看来你已经理解了这个概念。' })
  } else {
    card.status = 'wrong'
    // 自动记录错题
    if (!wrongBook.some(w => w.question === card.question)) {
      wrongBook.push({
        question: card.question,
        selectedText: card.options[optIdx],
        explanation: card.explanation
      })
    }
    chatHistory.push({ role: 'assistant', text: '这个选择有点偏差。你可以思考一下：寻址公式中，索引是如何直接映射到物理地址的？' })
  }
}

const askAI = () => {
  if (!userQuestion.value) return
  chatHistory.push({ role: 'user', text: userQuestion.value })
  // 模拟 AI 启发式回答
  setTimeout(() => {
    chatHistory.push({ role: 'assistant', text: '这是一个很好的观察。在 Python 的底层 C 源码中，ob_item 是一个指向指针数组的指针。你可以尝试从“连续内存空间”这个维度再推导一下。' })
  }, 1000)
  userQuestion.value = ''
}

const handleNextNode = () => {
  if (completedCount.value < cards.length || isWaiting.value) return
  
  if (!isTeacherConfirmed.value) {
    // 第一次点击：标记完成，进入等待状态
    isWaiting.value = true
    
    // 模拟1秒后教师确认
    setTimeout(() => {
      isWaiting.value = false
      isTeacherConfirmed.value = true
    }, 1000)
  } else {
    // 教师确认后：进入下一节点
    router.push('/student/training/ai-practice')
  }
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

.learning-card {
  position: relative;
  border-radius: 1.5rem;
  border-width: 1px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.learning-card:hover {
  transform: translateX(4px);
  box-shadow: 0 12px 24px -10px rgba(99, 102, 241, 0.15);
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

.custom-scrollbar-dark::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar-dark::-webkit-scrollbar-thumb {
  background: #475569;
  border-radius: 10px;
}

.whitespace-pre-wrap {
  white-space: pre-wrap;
}
</style>