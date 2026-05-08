<template>
  <div class="page-wrapper w-full min-h-screen">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10">
      
      <div class="flex justify-between items-center mb-6 pb-4 border-b border-gray-200/50">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: HOMEWORK</div>
          <h1 class="text-2xl font-bold text-gray-800">课后作业：AI 智能组卷</h1>
        </div>
        
        <div class="flex items-center gap-6">
          <div class="flex items-center gap-3 px-5 py-2 rounded-xl border shadow-sm transition-colors duration-300"
               :class="timeLeft < 300 ? 'bg-red-50 border-red-200 text-red-600' : 'bg-white/60 border-gray-200 text-gray-700'">
            <svg :class="timeLeft < 300 ? 'animate-pulse' : ''" style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <span class="font-mono font-bold text-xl">{{ formattedTime }}</span>
          </div>
          
          <button @click="saveDraft" class="px-4 py-2 text-sm font-bold text-indigo-600 bg-indigo-50 hover:bg-indigo-100 border border-indigo-100 rounded-lg transition-colors">
            暂存进度
          </button>
          <button @click="submitExam" class="hero-send-btn px-6 py-2 shadow-md">
            确认交卷
          </button>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="w-64 bg-white/40 border border-gray-100 rounded-2xl p-5 flex flex-col shadow-sm">
          <h3 class="text-sm font-bold text-gray-700 mb-4 border-b border-gray-200/50 pb-2">答题卡</h3>
          <div class="grid grid-cols-4 gap-3">
            <button v-for="(q, index) in questions" :key="q.id"
                    @click="currentQIndex = index"
                    class="w-10 h-10 rounded-lg text-sm font-bold flex items-center justify-center transition-all border"
                    :class="[
                      currentQIndex === index ? 'ring-2 ring-indigo-400 ring-offset-1 border-indigo-400' : '',
                      isAnswered(index) ? 'bg-indigo-500 text-white border-indigo-500' : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50'
                    ]">
              {{ index + 1 }}
            </button>
          </div>
          <div class="mt-auto pt-4 border-t border-gray-200/50 flex flex-col gap-2 text-xs text-gray-500">
            <div class="flex items-center gap-2"><span class="w-3 h-3 rounded bg-indigo-500"></span> 已答</div>
            <div class="flex items-center gap-2"><span class="w-3 h-3 rounded border border-gray-300 bg-white"></span> 未答</div>
          </div>
        </div>

        <div class="flex-1 bg-white/60 border border-gray-100 rounded-2xl p-8 flex flex-col shadow-sm overflow-y-auto custom-scrollbar relative">
          <div class="absolute top-8 right-8 text-xs font-bold px-2 py-1 rounded bg-indigo-50 text-indigo-500 border border-indigo-100">
            {{ currentQuestion.type === 'single' ? '单选题' : (currentQuestion.type === 'multi' ? '多选题' : '简答题') }}
          </div>
          
          <h2 class="text-lg font-bold text-gray-800 mb-6 pr-16 leading-relaxed">
            <span class="text-indigo-500 mr-2">{{ currentQIndex + 1 }}.</span>
            {{ currentQuestion.title }}
          </h2>

          <div v-if="currentQuestion.type === 'single' || currentQuestion.type === 'multi'" class="space-y-3">
            <label v-for="(opt, oIndex) in currentQuestion.options" :key="oIndex" 
                   class="flex items-center p-4 border rounded-xl cursor-pointer transition-all hover:bg-indigo-50/30"
                   :class="isSelected(opt.value) ? 'border-indigo-400 bg-indigo-50/50' : 'border-gray-200 bg-white'">
              <input :type="currentQuestion.type === 'single' ? 'radio' : 'checkbox'" 
                     :name="'q' + currentQuestion.id" 
                     :value="opt.value" 
                     v-model="answers[currentQIndex]"
                     class="w-4 h-4 text-indigo-600 border-gray-300 focus:ring-indigo-500">
              <span class="ml-3 text-gray-700 font-medium">{{ opt.label }}</span>
            </label>
          </div>

          <div v-else-if="currentQuestion.type === 'text'" class="flex-1 flex flex-col">
            <textarea v-model="answers[currentQIndex]" 
                      placeholder="请在此输入您的答案..." 
                      class="flex-1 w-full bg-white border border-gray-200 rounded-xl p-4 text-gray-700 outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100/50 resize-none custom-scrollbar"></textarea>
          </div>

          <div class="mt-8 pt-4 border-t border-gray-100 flex justify-between items-center">
            <button @click="prevQuestion" :disabled="currentQIndex === 0" class="px-4 py-2 text-gray-600 font-bold hover:bg-gray-100 rounded-lg transition-colors disabled:opacity-30 disabled:cursor-not-allowed">
              &larr; 上一题
            </button>
            <button v-if="currentQIndex < questions.length - 1" @click="nextQuestion" class="px-4 py-2 text-indigo-600 font-bold hover:bg-indigo-50 rounded-lg transition-colors">
              下一题 &rarr;
            </button>
            <button v-else @click="submitExam" class="hero-send-btn px-6 py-2 shadow-md">
              完成并交卷
            </button>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

// 模拟配置
const config = ref({
  timeLimitMins: 30,
  questionCount: 5
})

// 模拟题库数据
const questions = ref([
  { id: 1, type: 'single', title: 'SM4 密码算法的分组长度是多少？', options: [{label: 'A. 64 bit', value: 'A'}, {label: 'B. 128 bit', value: 'B'}, {label: 'C. 256 bit', value: 'C'}] },
  { id: 2, type: 'single', title: '在 SM4 算法中，轮函数 F 包含的非线性变换 τ 是由几个 S 盒并置构成的？', options: [{label: 'A. 2个', value: 'A'}, {label: 'B. 4个', value: 'B'}, {label: 'C. 8个', value: 'C'}] },
  { id: 3, type: 'multi', title: '以下关于 SM4 算法密钥扩展的说法，正确的是？（多选）', options: [{label: 'A. 加密密钥与解密密钥完全相同', value: 'A'}, {label: 'B. 解密时轮密钥的使用顺序与加密时相反', value: 'B'}, {label: 'C. 密钥扩展算法同样采用了 32 轮非线性迭代', value: 'C'}] },
  { id: 4, type: 'single', title: 'SM4 属于非对称加密算法。', options: [{label: '正确', value: 'T'}, {label: '错误', value: 'F'}] },
  { id: 5, type: 'text', title: '简述 SM4 算法中非线性变换（S盒）和线性变换（L）分别起到的主要密码学作用。' }
])

const currentQIndex = ref(0)
const currentQuestion = computed(() => questions.value[currentQIndex.value])

// 答案存储 (单选存字符串，多选存数组，简答存字符串)
const answers = ref(questions.value.map(q => q.type === 'multi' ? [] : ''))

const isAnswered = (index) => {
  const ans = answers.value[index]
  return Array.isArray(ans) ? ans.length > 0 : String(ans).trim().length > 0
}

const isSelected = (val) => {
  const ans = answers.value[currentQIndex.value]
  return Array.isArray(ans) ? ans.includes(val) : ans === val
}

const nextQuestion = () => { if (currentQIndex.value < questions.value.length - 1) currentQIndex.value++ }
const prevQuestion = () => { if (currentQIndex.value > 0) currentQIndex.value-- }

// 倒计时逻辑
const timeLeft = ref(config.value.timeLimitMins * 60)
let timer = null

const formattedTime = computed(() => {
  const m = Math.floor(timeLeft.value / 60).toString().padStart(2, '0')
  const s = (timeLeft.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

onMounted(() => {
  timer = setInterval(() => {
    if (timeLeft.value > 0) timeLeft.value--
    else { clearInterval(timer); alert("考试时间到，系统已强制交卷！"); }
  }, 1000)
})
onUnmounted(() => { if (timer) clearInterval(timer) })

const saveDraft = () => { alert("进度已保存至云端。") }
const submitExam = () => {
  const unanweredCount = answers.value.filter((ans, i) => !isAnswered(i)).length
  const msg = unanweredCount > 0 ? `您还有 ${unanweredCount} 道题未作答，确定要交卷吗？` : '确认提交试卷？'
  if (confirm(msg)) alert("交卷成功！")
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.3); border-radius: 4px; }
</style>