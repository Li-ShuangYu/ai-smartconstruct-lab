<template>
  <div style="height: 100%;">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 font-sans">
      
      <div class="flex justify-between items-center mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase italic">Node: HOMEWORK</div>
          <h1 class="text-2xl font-bold text-gray-800">课后检测：Python 数组专项练习</h1>
        </div>
        
        <div class="flex items-center gap-6">
          <div class="flex items-center gap-3 px-5 py-2 rounded-xl border shadow-sm transition-colors duration-300"
               :class="timeLeft < 300 ? 'bg-red-50 border-red-200 text-red-600' : 'bg-white/60 border-gray-200 text-gray-700'">
            <svg :class="timeLeft < 300 ? 'animate-pulse' : ''" class="w-5 h-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span class="font-mono font-bold text-xl">{{ formattedTime }}</span>
          </div>
          
          <button @click="submitExam" class="hero-send-btn px-8 py-2.5 shadow-lg shadow-indigo-200 active:scale-95 transition-all">
            确认交卷
          </button>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="w-72 bg-white/40 border border-gray-100 rounded-2xl p-5 flex flex-col shadow-sm">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-sm font-bold text-gray-700 uppercase tracking-wider">答题进度</h3>
            <span class="text-xs font-bold text-indigo-500">{{ answeredCount }} / {{ questions.length }}</span>
          </div>
          
          <div class="flex-1 overflow-y-auto custom-scrollbar pr-1">
            <div class="grid grid-cols-4 gap-2.5">
              <button 
                v-for="(q, index) in questions" :key="q.id"
                @click="currentQIndex = index"
                class="w-12 h-12 rounded-xl text-xs font-bold flex flex-col items-center justify-center transition-all border relative"
                :class="[
                  currentQIndex === index ? 'ring-2 ring-indigo-400 ring-offset-2 border-indigo-400' : '',
                  isAnswered(index) ? 'bg-indigo-600 text-white border-indigo-600 shadow-md shadow-indigo-100' : 'bg-white text-gray-400 border-gray-100 hover:border-indigo-200'
                ]"
              >
                {{ index + 1 }}
                <div v-if="isAnswered(index)" class="absolute -top-1 -right-1 w-3 h-3 bg-green-400 border-2 border-white rounded-full"></div>
              </button>
            </div>
          </div>

          <div class="mt-6 pt-4 border-t border-gray-100 space-y-2">
            <div class="flex items-center justify-between text-[10px] font-bold text-gray-400 uppercase">
              <div class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded bg-indigo-600"></span> 已答题目</div>
              <div class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded bg-white border border-gray-200"></span> 未完成</div>
            </div>
          </div>
        </div>

        <div class="flex-1 bg-white/60 border border-gray-100 rounded-2xl p-10 flex flex-col shadow-sm overflow-hidden relative">
          
          <div class="absolute top-10 right-10 flex items-center gap-2">
            <span class="px-3 py-1 bg-indigo-50 text-indigo-500 text-[10px] font-black rounded-lg border border-indigo-100 uppercase tracking-tighter">
              {{ typeLabelMap[currentQuestion.type] }}
            </span>
          </div>

          <div class="flex-1 overflow-y-auto custom-scrollbar pr-4">
            <h2 class="text-xl font-bold text-gray-800 mb-8 leading-relaxed">
              <span class="text-indigo-500 font-mono italic mr-3">Q{{ currentQIndex + 1 }}.</span>
              {{ currentQuestion.title }}
            </h2>

            <div v-if="currentQuestion.type === 'single' || currentQuestion.type === 'judge'" class="space-y-3">
              <label v-for="(opt, oIndex) in currentQuestion.options" :key="oIndex" 
                     class="flex items-center p-4 border rounded-2xl cursor-pointer transition-all group"
                     :class="answers[currentQIndex] === opt.value ? 'border-indigo-500 bg-indigo-50/30 ring-1 ring-indigo-500/10' : 'border-gray-100 bg-white/80 hover:border-indigo-200'">
                <div class="w-5 h-5 rounded-full border-2 flex items-center justify-center mr-4 transition-all"
                     :class="answers[currentQIndex] === opt.value ? 'border-indigo-500 bg-indigo-500' : 'border-gray-200 group-hover:border-indigo-300'">
                  <div v-if="answers[currentQIndex] === opt.value" class="w-2 h-2 bg-white rounded-full"></div>
                </div>
                <input type="radio" :value="opt.value" v-model="answers[currentQIndex]" class="hidden">
                <span class="text-sm font-medium" :class="answers[currentQIndex] === opt.value ? 'text-indigo-900' : 'text-gray-600'">{{ opt.label }}</span>
              </label>
            </div>

            <div v-else-if="currentQuestion.type === 'multi'" class="space-y-3">
              <label v-for="(opt, oIndex) in currentQuestion.options" :key="oIndex" 
                     class="flex items-center p-4 border rounded-2xl cursor-pointer transition-all"
                     :class="answers[currentQIndex].includes(opt.value) ? 'border-indigo-500 bg-indigo-50/30' : 'border-gray-100 bg-white/80 hover:border-indigo-200'">
                <div class="w-5 h-5 rounded border-2 flex items-center justify-center mr-4 transition-all"
                     :class="answers[currentQIndex].includes(opt.value) ? 'bg-indigo-500 border-indigo-500' : 'border-gray-200'">
                  <svg v-if="answers[currentQIndex].includes(opt.value)" class="w-3.5 h-3.5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="4"><path d="M5 13l4 4L19 7" /></svg>
                </div>
                <input type="checkbox" :value="opt.value" v-model="answers[currentQIndex]" class="hidden">
                <span class="text-sm font-medium">{{ opt.label }}</span>
              </label>
            </div>

            <div v-else-if="currentQuestion.type === 'fill'" class="pt-4">
              <input 
                v-model="answers[currentQIndex]" 
                type="text"
                placeholder="请在此输入简短的答案文本..."
                class="w-full bg-white border border-gray-200 rounded-2xl p-5 text-gray-700 font-mono outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100/30 transition-all shadow-inner"
              />
            </div>

            <div v-else-if="currentQuestion.type === 'essay'" class="h-64 pt-4">
              <textarea 
                v-model="answers[currentQIndex]" 
                placeholder="请输入详细的分析或代码逻辑..." 
                class="w-full h-full bg-white border border-gray-200 rounded-2xl p-6 text-gray-700 text-sm leading-relaxed outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100/30 resize-none custom-scrollbar shadow-inner"
              ></textarea>
            </div>
          </div>

          <div class="mt-8 pt-6 border-t border-gray-100 flex justify-between items-center shrink-0">
            <button @click="prevQuestion" :disabled="currentQIndex === 0" class="px-6 py-2.5 text-sm font-bold text-gray-400 hover:bg-gray-100 rounded-xl transition-all disabled:opacity-30 disabled:cursor-not-allowed">
              上一题
            </button>
            <div class="text-xs font-bold text-gray-300 font-mono">STEP {{ currentQIndex + 1 }} OF {{ questions.length }}</div>
            <button v-if="currentQIndex < questions.length - 1" @click="nextQuestion" class="px-8 py-2.5 bg-indigo-50 text-indigo-600 font-bold hover:bg-indigo-100 rounded-xl transition-all">
              下一题
            </button>
            <button v-else @click="submitExam" class="hero-send-btn px-8 py-2.5 rounded-xl">
              结束并交卷
            </button>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const typeLabelMap = { 
  single: '单项选择', 
  multi: '多项选择', 
  fill: '在线填空', 
  judge: '是非判断', 
  essay: '综合简答' 
}

// 模拟 20 道涵盖 Python 数组的题目
const questions = ref([
  { id: 1, type: 'single', title: '在 Python 中，创建一个空列表的正确方式是？', options: [{label: 'A. list = {}', value: 'A'}, {label: 'B. list = []', value: 'B'}, {label: 'C. list = ()', value: 'C'}] },
  { id: 2, type: 'judge', title: 'Python 中的列表（List）是可变序列，意味着你可以修改其元素值。', options: [{label: '正确 (True)', value: 'T'}, {label: '错误 (False)', value: 'F'}] },
  { id: 3, type: 'single', title: '若 arr = [10, 20, 30]，执行 arr.append(40) 后，arr 的内容是？', options: [{label: 'A. [40, 10, 20, 30]', value: 'A'}, {label: 'B. [10, 20, 30, 40]', value: 'B'}, {label: 'C. [10, 40, 20, 30]', value: 'C'}] },
  { id: 4, type: 'fill', title: '若 arr = [1, 2, 3, 4, 5]，则 arr[1:3] 的运行结果是：' },
  { id: 5, type: 'multi', title: '以下哪些方法可以向 Python 列表中添加元素？（多选）', options: [{label: 'A. append()', value: 'A'}, {label: 'B. extend()', value: 'B'}, {label: 'C. insert()', value: 'C'}, {label: 'D. add()', value: 'D'}] },
  { id: 6, type: 'single', title: 'arr = [1, 2, 3]，执行 arr * 2 的结果是？', options: [{label: 'A. [1, 2, 3, 1, 2, 3]', value: 'A'}, {label: 'B. [2, 4, 6]', value: 'B'}, {label: 'C. 报错', value: 'C'}] },
  { id: 7, type: 'fill', title: '使用哪个内置函数可以获取列表的长度（元素个数）？' },
  { id: 8, type: 'judge', title: '在 Python 列表中，索引 -1 代表列表的第一个元素。', options: [{label: '正确', value: 'T'}, {label: '错误', value: 'F'}] },
  { id: 9, type: 'single', title: '如何删除列表 arr 中的最后一个元素并返回该元素？', options: [{label: 'A. arr.remove()', value: 'A'}, {label: 'B. arr.pop()', value: 'B'}, {label: 'C. del arr[-1]', value: 'C'}] },
  { id: 10, type: 'essay', title: '请简述 Python 列表（List）与 元组（Tuple）在关于“可变性”方面的主要区别。' },
  { id: 11, type: 'single', title: 'arr = [1, 5, 2, 4]，执行 arr.sort() 后，arr[0] 的值是？', options: [{label: '1', value: 'A'}, {label: '4', value: 'B'}, {label: '5', value: 'C'}] },
  { id: 12, type: 'fill', title: '将列表 [1, 2] 变成 [1, 2, 3, 4]，应该使用哪个方法连接 [3, 4]？' },
  { id: 13, type: 'judge', title: '切片操作 arr[::2] 表示每隔一个元素提取一次。', options: [{label: '正确', value: 'T'}, {label: '错误', value: 'F'}] },
  { id: 14, type: 'single', title: '执行 [i for i in range(3)] 的结果是？', options: [{label: 'A. [0, 1, 2]', value: 'A'}, {label: 'B. [1, 2, 3]', value: 'B'}, {label: 'C. [0, 1, 2, 3]', value: 'C'}] },
  { id: 15, type: 'multi', title: '以下关于切片 arr[start:stop:step] 的描述正确的是？', options: [{label: 'A. start 是起始索引', value: 'A'}, {label: 'B. stop 是结束索引（不含）', value: 'B'}, {label: 'C. step 是步长', value: 'C'}] },
  { id: 16, type: 'fill', title: '在数组中查找元素 5 的第一个索引位置，应该使用哪个方法？' },
  { id: 17, type: 'judge', title: '列表可以包含不同类型的元素，例如 [1, "hello", 3.14]。', options: [{label: '正确', value: 'T'}, {label: '错误', value: 'F'}] },
  { id: 18, type: 'single', title: '清空列表所有元素的方法是？', options: [{label: 'A. clear()', value: 'A'}, {label: 'B. clean()', value: 'B'}, {label: 'C. empty()', value: 'C'}] },
  { id: 19, type: 'single', title: 'arr = [1, 2]，执行 arr.insert(0, 0) 后，arr[0] 是？', options: [{label: '0', value: 'A'}, {label: '1', value: 'B'}, {label: '2', value: 'C'}] },
  { id: 20, type: 'essay', title: '请编写一个 Python 表达式，演示如何使用切片技术将列表 [1, 2, 3, 4, 5] 进行原地反转。' }
])

const currentQIndex = ref(0)
const currentQuestion = computed(() => questions.value[currentQIndex.value])

// 答案存储逻辑：根据题型初始化
const answers = ref(questions.value.map(q => q.type === 'multi' ? [] : ''))

const isAnswered = (index) => {
  const ans = answers.value[index]
  return Array.isArray(ans) ? ans.length > 0 : String(ans).trim().length > 0
}
const answeredCount = computed(() => answers.value.filter((_, i) => isAnswered(i)).length)

const nextQuestion = () => { if (currentQIndex.value < questions.value.length - 1) currentQIndex.value++ }
const prevQuestion = () => { if (currentQIndex.value > 0) currentQIndex.value-- }

// 倒计时逻辑
const timeLeft = ref(30 * 60) // 30分钟
let timer = null
const formattedTime = computed(() => {
  const m = Math.floor(timeLeft.value / 60).toString().padStart(2, '0')
  const s = (timeLeft.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

onMounted(() => {
  timer = setInterval(() => { if (timeLeft.value > 0) timeLeft.value-- }, 1000)
})
onUnmounted(() => { if (timer) clearInterval(timer) })

const submitExam = () => {
  const remaining = questions.value.length - answeredCount.value
  if (remaining > 0) {
    if (!confirm(`您还有 ${remaining} 道题未作答，确定要提前交卷吗？`)) return
  }
  alert("交卷成功！系统正在通过 AI 进行批阅...")
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