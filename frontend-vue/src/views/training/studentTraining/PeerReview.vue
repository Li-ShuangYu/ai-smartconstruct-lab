<template>
  <div style="height: 100%;">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 font-sans">
      
      <div class="flex justify-between items-center mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase italic">Node: PEER_REVIEW</div>
          <h1 class="text-2xl font-bold text-gray-800">学生匿名互评</h1>
        </div>
        
        <div class="flex items-center gap-6">
          <div class="flex items-center gap-3">
            <span class="text-xs font-bold text-gray-500 uppercase">互评进度</span>
            <div class="flex gap-1.5">
              <div 
                v-for="(peer, index) in peers" :key="peer.id"
                class="w-8 h-2 rounded-full transition-all"
                :class="peer.status === 'completed' ? 'bg-green-500' : 'bg-gray-200'"
              ></div>
            </div>
            <span class="font-mono font-bold text-indigo-600 ml-2">{{ completedCount }} / {{ peers.length }}</span>
          </div>
          
          <button 
            @click="submitAllReviews" 
            class="hero-send-btn px-8 py-2.5 rounded-xl shadow-lg transition-all flex items-center gap-2"
            :class="{'opacity-50 grayscale cursor-not-allowed': !isAllReviewed, 'hover:shadow-indigo-500/30': isAllReviewed}"
            :disabled="!isAllReviewed"
          >
            提交全部互评
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
          </button>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="w-56 flex flex-col bg-white/40 border border-gray-100 rounded-2xl p-4 shadow-sm shrink-0">
          <h3 class="text-xs font-bold text-gray-500 uppercase tracking-widest mb-4 px-2">分配名单</h3>
          
          <div class="flex-1 overflow-y-auto space-y-2 custom-scrollbar">
            <button 
              v-for="(peer, index) in peers" :key="peer.id"
              @click="activePeerId = peer.id"
              class="w-full flex items-center justify-between p-3 rounded-xl transition-all border text-left"
              :class="[
                activePeerId === peer.id ? 'bg-indigo-50 border-indigo-200 ring-1 ring-indigo-100 shadow-sm' : 'bg-white border-transparent hover:border-gray-200 hover:bg-gray-50'
              ]"
            >
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold font-mono"
                     :class="peer.status === 'completed' ? 'bg-green-100 text-green-600' : 'bg-indigo-100 text-indigo-600'">
                  {{ peer.alias.split(' ')[1] }}
                </div>
                <span class="text-sm font-bold text-gray-700">{{ peer.alias }}</span>
              </div>
              <svg v-if="peer.status === 'completed'" class="w-4 h-4 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
            </button>
          </div>
        </div>

        <div class="flex-[1.8] bg-white/60 border border-gray-100 rounded-2xl flex flex-col shadow-sm overflow-hidden relative">
          <div class="flex items-center bg-gray-50/80 border-b border-gray-100 p-2 shrink-0">
            <button 
              v-for="tab in ['plan', 'code']" :key="tab"
              @click="currentViewTab = tab"
              class="px-6 py-2 text-sm font-bold rounded-lg transition-all"
              :class="currentViewTab === tab ? 'bg-white text-indigo-600 shadow-sm border border-gray-200/60' : 'text-gray-500 hover:text-gray-700'"
            >
              {{ tab === 'plan' ? '分析方案与要求' : '提交的源代码' }}
            </button>
          </div>

          <div class="flex-1 overflow-y-auto p-6 custom-scrollbar">
            <div v-if="currentViewTab === 'plan'" class="space-y-6">
              <div class="bg-indigo-50/50 p-5 rounded-xl border border-indigo-100/50">
                <h4 class="text-xs font-bold text-indigo-800 uppercase tracking-widest mb-2 flex items-center gap-2">
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" /></svg>
                  原始任务要求
                </h4>
                <p class="text-sm text-indigo-900/80 leading-relaxed">{{ activePeer.plan.requirement }}</p>
              </div>
              
              <div>
                <h4 class="text-sm font-bold text-gray-700 mb-3 border-l-4 border-indigo-500 pl-3">该同学提交的方案</h4>
                <div class="p-5 bg-white border border-gray-200 rounded-xl text-sm text-gray-700 leading-loose whitespace-pre-wrap shadow-inner min-h-[200px]">
                  {{ activePeer.plan.content }}
                </div>
              </div>
            </div>

            <div v-if="currentViewTab === 'code'" class="h-full flex flex-col">
              <h4 class="text-sm font-bold text-gray-700 mb-3 border-l-4 border-indigo-500 pl-3">该同学提交的代码</h4>
              <div class="flex-1 bg-slate-900 rounded-xl overflow-hidden shadow-inner flex flex-col relative group">
                <div class="bg-slate-800 px-4 py-2 flex items-center justify-between border-b border-slate-700 shrink-0">
                  <div class="flex items-center gap-2">
                    <div class="w-3 h-3 rounded-full bg-red-500"></div>
                    <div class="w-3 h-3 rounded-full bg-amber-500"></div>
                    <div class="w-3 h-3 rounded-full bg-green-500"></div>
                  </div>
                  <span class="text-xs font-mono text-slate-400">solution.py</span>
                </div>
                <div class="flex-1 overflow-auto custom-scrollbar-dark p-4">
                  <pre class="text-sm font-mono text-slate-300 leading-relaxed"><code>{{ activePeer.code }}</code></pre>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="w-80 bg-white/60 border border-gray-100 rounded-2xl flex flex-col shadow-sm shrink-0">
          <div class="p-5 border-b border-gray-100 bg-gray-50/50 rounded-t-2xl shrink-0">
            <h3 class="font-bold text-gray-800 flex items-center gap-2">
              <svg class="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" /></svg>
              同行评审表
            </h3>
            <p class="text-[10px] text-gray-400 mt-1">请客观评估 {{ activePeer.alias }} 的产出</p>
          </div>

          <div class="flex-1 overflow-y-auto p-5 custom-scrollbar space-y-6">
            <div v-for="dim in dimensions" :key="dim.key" class="space-y-2">
              <div class="flex justify-between items-end">
                <label class="text-xs font-bold text-gray-700">{{ dim.label }}</label>
                <span class="text-xs font-bold" :class="activePeer.review.scores[dim.key] > 0 ? 'text-indigo-600' : 'text-gray-300'">
                  {{ activePeer.review.scores[dim.key] || 0 }} / 5 分
                </span>
              </div>
              <div class="flex justify-between gap-2">
                <button 
                  v-for="score in 5" :key="score"
                  @click="activePeer.review.scores[dim.key] = score"
                  class="flex-1 py-1.5 rounded border transition-all text-sm font-bold"
                  :class="activePeer.review.scores[dim.key] === score 
                    ? 'bg-indigo-50 border-indigo-500 text-indigo-600 shadow-sm' 
                    : 'bg-white border-gray-200 text-gray-400 hover:border-indigo-200'"
                >
                  {{ score }}
                </button>
              </div>
            </div>

            <div class="space-y-2 pt-4 border-t border-gray-100">
              <label class="text-xs font-bold text-gray-700 flex items-center justify-between">
                综合评语建议
                <span class="text-[10px] font-normal text-gray-400">必填项</span>
              </label>
              <textarea 
                v-model="activePeer.review.comment"
                placeholder="指出方案的亮点或提出优化建议..."
                class="w-full bg-gray-50 border border-gray-200 rounded-xl p-3 text-sm text-gray-700 outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100/30 resize-none h-32 custom-scrollbar"
              ></textarea>
            </div>
          </div>

          <div class="p-5 border-t border-gray-100 bg-gray-50/50 rounded-b-2xl shrink-0">
            <button 
              @click="saveCurrentReview"
              class="w-full py-3 rounded-xl font-bold transition-all shadow-sm flex justify-center items-center gap-2"
              :class="isCurrentReviewValid ? 'bg-indigo-600 text-white hover:bg-indigo-700 hover:shadow-indigo-200' : 'bg-gray-200 text-gray-400 cursor-not-allowed'"
              :disabled="!isCurrentReviewValid"
            >
              {{ activePeer.status === 'completed' ? '已评审，更新修改' : '保存该同学评审' }}
              <svg v-if="activePeer.status === 'completed'" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
            </button>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

const currentViewTab = ref('plan') // 'plan' | 'code'

// 评分维度定义
const dimensions = [
  { key: 'logic', label: '逻辑正确性' },
  { key: 'standard', label: '代码规范度' },
  { key: 'completeness', label: '方案完整性' }
]

// 模拟分配的 3 个匿名同学数据
const peers = reactive([
  {
    id: 'p1',
    alias: '匿名同学 A',
    status: 'pending', // 'pending' | 'completed'
    plan: {
      requirement: '找出成绩数组中的最高分和最低分，并处理可能出现的空数组异常。',
      content: '方案思路：\n1. 首先判断数组长度是否为 0，如果是，则直接返回 None。\n2. 使用 Python 内置的 max() 和 min() 函数获取最值。\n3. 如果需要自己实现，可以设置两个变量初始值为 arr[0]，遍历一遍数组进行比对。我这里选择了直接使用内置函数以提高效率。'
    },
    code: `def get_extremes(arr):\n    if not arr:\n        return None, None\n    \n    highest = max(arr)\n    lowest = min(arr)\n    return highest, lowest\n\n# 测试代码\nscores = [85, 92, 78, 99, 60]\nprint(get_extremes(scores))`,
    review: { scores: { logic: 0, standard: 0, completeness: 0 }, comment: '' }
  },
  {
    id: 'p2',
    alias: '匿名同学 B',
    status: 'pending',
    plan: {
      requirement: '找出成绩数组中的最高分和最低分，并处理可能出现的空数组异常。',
      content: '我没有使用内置函数，而是通过一个 for 循环遍历所有的元素。这样即使在不允许使用 max/min 的情况下也能工作。如果数组为空，使用 try-except 来捕获索引错误。'
    },
    code: `def get_extremes(arr):\n    try:\n        h = arr[0]\n        l = arr[0]\n        for s in arr:\n            if s > h: h = s\n            if s < l: l = s\n        return h, l\n    except IndexError:\n        return "数组为空"`,
    review: { scores: { logic: 0, standard: 0, completeness: 0 }, comment: '' }
  },
  {
    id: 'p3',
    alias: '匿名同学 C',
    status: 'pending',
    plan: {
      requirement: '找出成绩数组中的最高分和最低分，并处理可能出现的空数组异常。',
      content: '先把数组排个序，然后取第一个和最后一个就行了，简单粗暴。'
    },
    code: `def get_extremes(arr):\n    if len(arr) == 0:\n        return 0, 0\n    arr.sort()\n    return arr[-1], arr[0]`,
    review: { scores: { logic: 0, standard: 0, completeness: 0 }, comment: '' }
  }
])

const activePeerId = ref(peers[0].id)

const activePeer = computed(() => {
  return peers.find(p => p.id === activePeerId.value)
})

const completedCount = computed(() => {
  return peers.filter(p => p.status === 'completed').length
})

const isAllReviewed = computed(() => {
  return completedCount.value === peers.length
})

// 校验当前同学的表单是否填写完整（所有维度分数 > 0 且评语不为空）
const isCurrentReviewValid = computed(() => {
  const r = activePeer.value.review
  const hasScores = dimensions.every(d => r.scores[d.key] > 0)
  const hasComment = r.comment.trim().length > 0
  return hasScores && hasComment
})

const saveCurrentReview = () => {
  if (!isCurrentReviewValid.value) return
  activePeer.value.status = 'completed'
  
  // 自动切换到下一个未评审的同学
  const nextPending = peers.find(p => p.status === 'pending')
  if (nextPending) {
    activePeerId.value = nextPending.id
    currentViewTab.value = 'plan' // 切换同学时重置 Tab 到方案
  }
}

const submitAllReviews = () => {
  if (!isAllReviewed.value) return
  alert('全部互评已提交！评价数据已匿名同步至系统。')
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

.hero-send-btn:not(:disabled):hover {
  transform: translateY(-2px);
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
</style>