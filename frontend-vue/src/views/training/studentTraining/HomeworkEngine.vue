<template>
  <div class="page-wrapper w-full min-h-[calc(100vh-100px)] overflow-y-auto custom-scrollbar">

    <div class="glass-card w-full max-w-6xl p-6 flex flex-col z-10 h-full max-h-[calc(100vh-120px)]">

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

        <div class="sidebar-card">
          <h3 class="text-sm font-bold text-gray-700 mb-4 border-b border-gray-200/50 pb-2">答题卡</h3>
          <div class="sidebar-grid">
            <button v-for="(q, index) in questions" :key="q.id"
                    @click="currentQIndex = index"
                    class="w-10 h-10 rounded-lg text-sm font-bold flex items-center justify-center transition-all border shrink-0"
                    :class="[
                      currentQIndex === index ? 'ring-2 ring-indigo-400 ring-offset-1 border-indigo-400' : '',
                      isAnswered(index) ? 'bg-indigo-500 text-white border-indigo-500' : 'bg-white text-gray-500 border-gray-200 hover:bg-gray-50'
                    ]">
              {{ index + 1 }}
            </button>
          </div>
          <div class="sidebar-legend">
            <div class="flex items-center gap-2"><span class="w-3 h-3 rounded bg-indigo-500"></span> 已答</div>
            <div class="flex items-center gap-2"><span class="w-3 h-3 rounded border border-gray-300 bg-white"></span> 未答</div>
          </div>
        </div>

        <div class="flex-1 bg-white/60 border border-gray-100 rounded-2xl p-8 flex flex-col shadow-sm overflow-y-auto custom-scrollbar relative">
          <div class="absolute top-8 right-8 text-xs font-bold px-2 py-1 rounded bg-indigo-50 text-indigo-500 border border-indigo-100">
            {{ typeLabel }}
          </div>

          <h2 class="text-lg font-bold text-gray-800 mb-6 pr-16 leading-relaxed">
            <span class="text-indigo-500 mr-2">{{ currentQIndex + 1 }}.</span>
            {{ currentQuestion.title }}
          </h2>

          <div v-if="currentQuestion.type === 'single' || currentQuestion.type === 'judge'" class="space-y-3">
            <label v-for="(opt, oIndex) in currentQuestion.options" :key="oIndex"
                   class="flex items-center p-4 border rounded-xl cursor-pointer transition-all hover:bg-indigo-50/30"
                   :class="isSelected(opt.value) ? 'border-indigo-400 bg-indigo-50/50' : 'border-gray-200 bg-white'">
              <input type="radio"
                     :name="'q' + currentQuestion.id"
                     :value="opt.value"
                     v-model="answers[currentQIndex]"
                     class="w-4 h-4 text-indigo-600 border-gray-300 focus:ring-indigo-500">
              <span class="ml-3 text-gray-700 font-medium">{{ opt.label }}</span>
            </label>
          </div>

          <div v-else-if="currentQuestion.type === 'multi'" class="space-y-3">
            <label v-for="(opt, oIndex) in currentQuestion.options" :key="oIndex"
                   class="flex items-center p-4 border rounded-xl cursor-pointer transition-all hover:bg-indigo-50/30"
                   :class="isSelected(opt.value) ? 'border-indigo-400 bg-indigo-50/50' : 'border-gray-200 bg-white'">
              <input type="checkbox"
                     :name="'q' + currentQuestion.id"
                     :value="opt.value"
                     v-model="answers[currentQIndex]"
                     class="w-4 h-4 text-indigo-600 border-gray-300 focus:ring-indigo-500">
              <span class="ml-3 text-gray-700 font-medium">{{ opt.label }}</span>
            </label>
          </div>

          <div v-else-if="currentQuestion.type === 'text'" class="flex-1 flex flex-col min-h-0">
            <textarea v-model="answers[currentQIndex]"
                      placeholder="请在此输入您的答案..."
                      class="flex-1 w-full min-h-[120px] bg-white border border-gray-200 rounded-xl p-4 text-gray-700 outline-none focus:border-indigo-400 focus:ring-4 focus:ring-indigo-100/50 resize-none custom-scrollbar"></textarea>
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

const emit = defineEmits(['complete'])

// 模拟配置
const config = ref({
  timeLimitMins: 60,
  questionCount: 30
})

// 30 道题库数据
const questions = ref([
  { id: 1, type: 'single', title: '以下哪种数据结构最适合实现浏览器的前进/后退功能？', options: [{label: 'A. 数组', value: 'A'}, {label: 'B. 链表', value: 'B'}, {label: 'C. 栈', value: 'C'}, {label: 'D. 队列', value: 'D'}] },
  { id: 2, type: 'single', title: 'Python 中 list 的 pop(0) 操作的时间复杂度是多少？', options: [{label: 'A. O(1)', value: 'A'}, {label: 'B. O(log n)', value: 'B'}, {label: 'C. O(n)', value: 'C'}, {label: 'D. O(n²)', value: 'D'}] },
  { id: 3, type: 'single', title: '下列关于哈希表的说法中，错误的是？', options: [{label: 'A. 平均查找时间复杂度为 O(1)', value: 'A'}, {label: 'B. 最坏查找时间复杂度为 O(n)', value: 'B'}, {label: 'C. 哈希函数越复杂性能越好', value: 'C'}, {label: 'D. 负载因子越大冲突概率越高', value: 'D'}] },
  { id: 4, type: 'single', title: '快速排序的平均时间复杂度是多少？', options: [{label: 'A. O(n)', value: 'A'}, {label: 'B. O(n log n)', value: 'B'}, {label: 'C. O(n²)', value: 'C'}, {label: 'D. O(log n)', value: 'D'}] },
  { id: 5, type: 'judge', title: 'Python 中的列表（List）是可变序列。', options: [{label: 'A. 正确', value: 'T'}, {label: 'B. 错误', value: 'F'}] },
  { id: 6, type: 'single', title: '下列哪种排序算法是稳定的？', options: [{label: 'A. 快速排序', value: 'A'}, {label: 'B. 堆排序', value: 'B'}, {label: 'C. 归并排序', value: 'C'}, {label: 'D. 选择排序', value: 'D'}] },
  { id: 7, type: 'multi', title: '以下哪些方法可以向 Python 列表中添加元素？（多选）', options: [{label: 'A. append()', value: 'A'}, {label: 'B. extend()', value: 'B'}, {label: 'C. insert()', value: 'C'}, {label: 'D. add()', value: 'D'}] },
  { id: 8, type: 'single', title: '二叉树的层序遍历通常使用什么数据结构辅助实现？', options: [{label: 'A. 栈', value: 'A'}, {label: 'B. 队列', value: 'B'}, {label: 'C. 优先队列', value: 'C'}, {label: 'D. 哈希表', value: 'D'}] },
  { id: 9, type: 'single', title: '在 Python 中，创建一个空字典的正确方式是？', options: [{label: 'A. dict = []', value: 'A'}, {label: 'B. dict = ()', value: 'B'}, {label: 'C. dict = {}', value: 'C'}, {label: 'D. dict = set()', value: 'D'}] },
  { id: 10, type: 'judge', title: '二分查找要求数据是有序的。', options: [{label: 'A. 正确', value: 'T'}, {label: 'B. 错误', value: 'F'}] },
  { id: 11, type: 'single', title: '动态规划的核心思想不包括以下哪项？', options: [{label: 'A. 最优子结构', value: 'A'}, {label: 'B. 重叠子问题', value: 'B'}, {label: 'C. 贪心选择性质', value: 'C'}, {label: 'D. 状态转移方程', value: 'D'}] },
  { id: 12, type: 'multi', title: '以下哪些数据结构的查找时间复杂度为 O(1)？（多选）', options: [{label: 'A. 数组（已知索引）', value: 'A'}, {label: 'B. 哈希表（平均）', value: 'B'}, {label: 'C. 二叉搜索树', value: 'C'}, {label: 'D. 链表', value: 'D'}] },
  { id: 13, type: 'single', title: 'Python 中 collections.deque 的底层实现是？', options: [{label: 'A. 动态数组', value: 'A'}, {label: 'B. 双向链表', value: 'B'}, {label: 'C. 循环数组', value: 'C'}, {label: 'D. 哈希表', value: 'D'}] },
  { id: 14, type: 'single', title: '堆排序的时间复杂度是？', options: [{label: 'A. O(n)', value: 'A'}, {label: 'B. O(n log n)', value: 'B'}, {label: 'C. O(n²)', value: 'C'}, {label: 'D. O(log n)', value: 'D'}] },
  { id: 15, type: 'single', title: '递归算法必须满足的条件是？', options: [{label: 'A. 有终止条件', value: 'A'}, {label: 'B. 有循环', value: 'B'}, {label: 'C. 使用全局变量', value: 'C'}, {label: 'D. 不能有参数', value: 'D'}] },
  { id: 16, type: 'judge', title: 'Python 的 dict 在 Python 3.7+ 中保持插入顺序。', options: [{label: 'A. 正确', value: 'T'}, {label: 'B. 错误', value: 'F'}] },
  { id: 17, type: 'single', title: '在无向图中，深度优先搜索的时间复杂度是？', options: [{label: 'A. O(V+E)', value: 'A'}, {label: 'B. O(V×E)', value: 'B'}, {label: 'C. O(V²)', value: 'C'}, {label: 'D. O(E²)', value: 'D'}] },
  { id: 18, type: 'single', title: '下列哪种情况最适合使用贪心算法？', options: [{label: 'A. 背包问题', value: 'A'}, {label: 'B. 区间调度问题', value: 'B'}, {label: 'C. 最短路径（含负权边）', value: 'C'}, {label: 'D. 编辑距离', value: 'D'}] },
  { id: 19, type: 'multi', title: '以下哪些属于 Python 的内置数据类型？（多选）', options: [{label: 'A. list', value: 'A'}, {label: 'B. array', value: 'B'}, {label: 'C. tuple', value: 'C'}, {label: 'D. set', value: 'D'}] },
  { id: 20, type: 'single', title: '链表相比数组的主要优势是？', options: [{label: 'A. 随机访问快', value: 'A'}, {label: 'B. 插入删除快', value: 'B'}, {label: 'C. 内存占用少', value: 'C'}, {label: 'D. 缓存友好', value: 'D'}] },
  { id: 21, type: 'single', title: 'Python 中列表推导式 [x*2 for x in range(5)] 的结果是？', options: [{label: 'A. [0,2,4,6,8]', value: 'A'}, {label: 'B. [1,2,3,4,5]', value: 'B'}, {label: 'C. [0,1,2,3,4]', value: 'C'}, {label: 'D. [2,4,6,8,10]', value: 'D'}] },
  { id: 22, type: 'judge', title: '平衡二叉搜索树的查找时间复杂度为 O(log n)。', options: [{label: 'A. 正确', value: 'T'}, {label: 'B. 错误', value: 'F'}] },
  { id: 23, type: 'single', title: '下列关于字符串切片 "Python"[::-1] 的结果是？', options: [{label: 'A. "Python"', value: 'A'}, {label: 'B. "nohtyP"', value: 'B'}, {label: 'C. "Pyt"', value: 'C'}, {label: 'D. "hon"', value: 'D'}] },
  { id: 24, type: 'single', title: '算法的时间复杂度 O(n log n) 通常出现在哪种算法中？', options: [{label: 'A. 冒泡排序', value: 'A'}, {label: 'B. 归并排序', value: 'B'}, {label: 'C. 计数排序', value: 'C'}, {label: 'D. 选择排序', value: 'D'}] },
  { id: 25, type: 'multi', title: '下列关于 Python 函数参数的说法，正确的有？（多选）', options: [{label: 'A. 支持默认参数', value: 'A'}, {label: 'B. 支持可变参数 *args', value: 'B'}, {label: 'C. 支持关键字参数 **kwargs', value: 'C'}, {label: 'D. 不支持匿名函数', value: 'D'}] },
  { id: 26, type: 'single', title: '在 Python 中，global 关键字的作用是？', options: [{label: 'A. 定义全局函数', value: 'A'}, {label: 'B. 声明使用全局变量', value: 'B'}, {label: 'C. 创建全局包', value: 'C'}, {label: 'D. 导入全局模块', value: 'D'}] },
  { id: 27, type: 'single', title: '下列哪个不是 Python 的 PEP 8 命名规范？', options: [{label: 'A. 类名使用 CamelCase', value: 'A'}, {label: 'B. 函数名使用 snake_case', value: 'B'}, {label: 'C. 常量使用 ALL_CAPS', value: 'C'}, {label: 'D. 变量名使用 camelCase', value: 'D'}] },
  { id: 28, type: 'judge', title: 'Python 的装饰器本质上是返回函数的高阶函数。', options: [{label: 'A. 正确', value: 'T'}, {label: 'B. 错误', value: 'F'}] },
  { id: 29, type: 'single', title: '文件读写操作完成后，推荐使用哪种方式关闭文件？', options: [{label: 'A. file.close()', value: 'A'}, {label: 'B. with 语句', value: 'B'}, {label: 'C. 程序退出自动关闭', value: 'C'}, {label: 'D. 不需要手动关闭', value: 'D'}] },
  { id: 30, type: 'text', title: '请简述 Python 中列表和元组的主要区别，并说明各自适合的使用场景。' }
])

const currentQIndex = ref(0)
const currentQuestion = computed(() => questions.value[currentQIndex.value] || questions.value[0])

const typeLabel = computed(() => {
  const t = currentQuestion.value.type
  if (t === 'single') return '单选题'
  if (t === 'multi') return '多选题'
  if (t === 'judge') return '判断题'
  return '简答题'
})

// 答案存储 (单选/判断存字符串，多选存数组，简答存字符串)
const answers = ref(questions.value.map(q => q.type === 'multi' ? [] : ''))

const isAnswered = (index) => {
  if (index < 0 || index >= questions.value.length) return false
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
  if (confirm(msg)) {
    alert("交卷成功！")
    emit('complete')
  }
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.3); border-radius: 4px; }

.sidebar-card {
  width: 240px;
  background: rgba(255,255,255,0.4);
  border: 1px solid #f1f5f9;
  border-radius: 1rem;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  flex-shrink: 0;
  max-height: 100%;
  overflow: hidden;
}

.sidebar-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  overflow-y: auto;
  padding-right: 4px;
  align-content: start;
}

.sidebar-legend {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
  flex-shrink: 0;
}
</style>