<template>
  <div class="page-wrapper flex items-center justify-center w-full h-full min-h-[calc(100vh-100px)]">
    
    <div class="glass-card w-full max-w-7xl p-0 flex flex-col z-10 h-[800px] overflow-hidden border-2"
         :class="cheatCount > 0 ? 'border-red-400 shadow-[0_0_30px_rgba(248,113,113,0.3)]' : 'border-gray-200/50'">
      
      <div class="bg-gray-800 text-white px-8 py-4 flex justify-between items-center shrink-0">
        <div class="flex items-center gap-4">
          <div class="px-2 py-1 bg-red-500 rounded text-xs font-bold tracking-widest animate-pulse">EXAM MODE</div>
          <h1 class="text-xl font-bold tracking-wide">结课考核：密码学综合应用实践</h1>
        </div>
        
        <div class="flex items-center gap-6">
          <div class="flex items-center gap-2 bg-gray-900 px-4 py-2 rounded-lg border border-gray-700">
            <svg style="width: 18px; height: 18px; flex-shrink: 0;" class="text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <span class="font-mono font-bold text-xl" :class="timeLeft < 600 ? 'text-red-400 animate-pulse' : 'text-white'">
              {{ formattedTime }}
            </span>
          </div>
          
          <button @click="submitExam" class="bg-indigo-600 hover:bg-indigo-500 text-white px-6 py-2.5 rounded-lg font-bold shadow-lg transition-colors flex items-center gap-2">
            <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            确认交卷
          </button>
        </div>
      </div>

      <div v-if="cheatCount > 0" class="bg-red-50 border-b border-red-200 px-8 py-3 flex items-center justify-between shrink-0">
        <div class="flex items-center gap-2 text-red-600 font-bold text-sm">
          <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          警告：系统检测到您有离开考试页面的行为！累计切屏次数：{{ cheatCount }} 次。
        </div>
        <span class="text-xs text-red-500">超过 3 次将被系统强制收卷并记为零分</span>
      </div>

      <div class="flex-1 flex min-h-0 bg-white/60">
        
        <div class="w-72 bg-gray-50/80 border-r border-gray-200 p-6 flex flex-col">
          <div class="mb-4">
            <h3 class="text-sm font-bold text-gray-700 mb-1">答题卡进度</h3>
            <div class="w-full bg-gray-200 rounded-full h-1.5 mb-2">
              <div class="bg-indigo-500 h-1.5 rounded-full transition-all" :style="{ width: progressPercentage + '%' }"></div>
            </div>
            <div class="text-xs text-gray-500 text-right">{{ answeredCount }} / {{ questions.length }}</div>
          </div>
          
          <div class="grid grid-cols-5 gap-2 overflow-y-auto custom-scrollbar flex-1 content-start pb-4">
            <button v-for="(q, index) in questions" :key="q.id"
                    @click="currentQIndex = index"
                    class="h-9 rounded flex items-center justify-center text-xs font-bold transition-all border"
                    :class="[
                      currentQIndex === index ? 'ring-2 ring-indigo-500 ring-offset-1 border-indigo-500' : '',
                      isAnswered(index) ? 'bg-indigo-100 text-indigo-700 border-indigo-200' : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-100'
                    ]">
              {{ index + 1 }}
            </button>
          </div>
        </div>

        <div class="flex-1 p-10 flex flex-col overflow-y-auto custom-scrollbar relative bg-white">
          <div class="mb-6 flex items-center gap-3">
            <span class="px-2 py-1 bg-gray-100 text-gray-600 font-bold text-xs rounded border border-gray-200">
              {{ currentQuestion.type === 'single' ? '单选题' : (currentQuestion.type === 'multi' ? '多选题' : '编程/简答题') }}
            </span>
            <span class="text-gray-400 text-sm font-medium">本题分值: {{ currentQuestion.score }} 分</span>
          </div>
          
          <h2 class="text-xl font-bold text-gray-800 mb-8 leading-relaxed">
            {{ currentQIndex + 1 }}. {{ currentQuestion.title }}
          </h2>

          <div v-if="currentQuestion.type === 'single' || currentQuestion.type === 'multi'" class="space-y-4">
            <label v-for="(opt, oIndex) in currentQuestion.options" :key="oIndex" 
                   class="flex items-start p-5 border rounded-xl cursor-pointer transition-all hover:bg-gray-50"
                   :class="isSelected(opt.value) ? 'border-indigo-500 bg-indigo-50/30 shadow-sm' : 'border-gray-200'">
              <input :type="currentQuestion.type === 'single' ? 'radio' : 'checkbox'" 
                     :value="opt.value" 
                     v-model="answers[currentQIndex]"
                     class="mt-0.5 w-4 h-4 text-indigo-600 border-gray-300 focus:ring-indigo-500">
              <span class="ml-4 text-gray-700 font-medium leading-relaxed">{{ opt.label }}</span>
            </label>
          </div>

          <div v-else-if="currentQuestion.type === 'text'" class="flex-1 flex flex-col">
            <textarea v-model="answers[currentQIndex]" 
                      placeholder="请详细阐述您的解答过程..." 
                      class="flex-1 w-full bg-gray-50 border border-gray-200 rounded-xl p-5 text-gray-700 outline-none focus:border-indigo-500 focus:bg-white transition-colors resize-none custom-scrollbar shadow-inner"></textarea>
          </div>

          <div class="mt-auto pt-8 flex justify-between items-center">
            <button @click="prevQuestion" :disabled="currentQIndex === 0" class="px-6 py-2.5 text-gray-600 font-bold hover:bg-gray-100 rounded-lg transition-colors disabled:opacity-30 disabled:cursor-not-allowed border border-transparent hover:border-gray-200">
              上一题
            </button>
            <button v-if="currentQIndex < questions.length - 1" @click="nextQuestion" class="px-6 py-2.5 text-white bg-gray-800 hover:bg-gray-700 font-bold rounded-lg transition-colors shadow-md">
              下一题
            </button>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const config = ref({
  duration: 120, // 120分钟
  totalScore: 100
})

// 模拟严肃考题
const questions = ref([
  { id: 1, type: 'single', score: 10, title: '在商用密码标准中，SM2 算法主要用于实现什么功能？', options: [{label: 'A. 对称数据加密', value: 'A'}, {label: 'B. 数字签名与密钥交换', value: 'B'}, {label: 'C. 密码杂凑（哈希）', value: 'C'}] },
  { id: 2, type: 'multi', score: 20, title: '关于 SM3 密码杂凑算法，以下描述正确的有哪些？', options: [{label: 'A. 输出长度为 256 比特', value: 'A'}, {label: 'B. 采用 Merkle-Damgard 结构', value: 'B'}, {label: 'C. 压缩函数采用了 64 步迭代', value: 'C'}, {label: 'D. 它可以抵抗生日攻击', value: 'D'}] },
  { id: 3, type: 'text', score: 70, title: '综合应用题：假设您需要设计一个基于 SM4 和 SM2 的安全通信协议，请简述如何结合这两种算法实现数据的机密性与不可否认性。' }
])

const currentQIndex = ref(0)
const currentQuestion = computed(() => questions.value[currentQIndex.value])

// 答案与进度
const answers = ref(questions.value.map(q => q.type === 'multi' ? [] : ''))
const isAnswered = (index) => {
  const ans = answers.value[index]
  return Array.isArray(ans) ? ans.length > 0 : String(ans).trim().length > 0
}
const isSelected = (val) => {
  const ans = answers.value[currentQIndex.value]
  return Array.isArray(ans) ? ans.includes(val) : ans === val
}
const answeredCount = computed(() => answers.value.filter((ans, i) => isAnswered(i)).length)
const progressPercentage = computed(() => (answeredCount.value / questions.value.length) * 100)

const nextQuestion = () => { if (currentQIndex.value < questions.value.length - 1) currentQIndex.value++ }
const prevQuestion = () => { if (currentQIndex.value > 0) currentQIndex.value-- }

// 倒计时
const timeLeft = ref(config.value.duration * 60)
let timer = null
const formattedTime = computed(() => {
  const h = Math.floor(timeLeft.value / 3600).toString().padStart(2, '0')
  const m = Math.floor((timeLeft.value % 3600) / 60).toString().padStart(2, '0')
  const s = (timeLeft.value % 60).toString().padStart(2, '0')
  return `${h}:${m}:${s}`
})

// 防作弊机制：监听标签页切换
const cheatCount = ref(0)
const handleVisibilityChange = () => {
  if (document.hidden) {
    cheatCount.value++
    if (cheatCount.value >= 3) {
      alert("您已严重违规（离开考试页面超过3次），系统将强制收卷！")
      submitExam(true)
    }
  }
}

onMounted(() => {
  timer = setInterval(() => {
    if (timeLeft.value > 0) timeLeft.value--
    else submitExam(true)
  }, 1000)
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

const submitExam = (isForced = false) => {
  if (isForced || confirm('确认提交试卷？提交后将无法修改。')) {
    alert("交卷成功，进入下一环节！")
  }
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(156, 163, 175, 0.5); border-radius: 4px; }
</style>