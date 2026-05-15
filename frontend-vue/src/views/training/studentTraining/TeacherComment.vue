<template>
  <div style="height: 100%;">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 font-sans">
      
      <div class="flex justify-between items-center mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase italic">Node: TEACHER_REVIEW</div>
          <h1 class="text-2xl font-bold text-gray-800">教师点评与实训复盘</h1>
        </div>
        
        <div class="flex items-center gap-4">
          <div class="px-4 py-2 bg-indigo-50 border border-indigo-100 rounded-xl flex items-center gap-2">
            <span class="text-xs font-bold text-indigo-500 uppercase tracking-widest">本次实训得分</span>
            <span class="text-xl font-mono font-black text-indigo-600">{{ personalFeedback.score }}</span>
          </div>
          <button 
            @click="finishTraining"
            class="hero-send-btn px-8 py-2.5 rounded-xl shadow-lg transition-all flex items-center gap-2"
            :class="{
              'opacity-50 grayscale cursor-not-allowed': isWaiting,
              'hover:shadow-indigo-500/30': !isWaiting && isTeacherConfirmed
            }"
            :disabled="isWaiting"
          >
            {{ isTeacherConfirmed ? '进入下一节点' : (isWaiting ? '等待教师进入下一节点' : '结束实训') }}
            <svg v-if="!isWaiting" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
            <svg v-else class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
          </button>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1.2] flex flex-col gap-6 overflow-y-auto custom-scrollbar pr-2">
          
          <div class="bg-gradient-to-br from-indigo-600 to-indigo-800 rounded-2xl p-6 shadow-lg text-white relative overflow-hidden shrink-0">
            <svg class="absolute right-0 top-0 w-32 h-32 opacity-10 -translate-y-4 translate-x-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2L9 9l-7 3 7 3 3 7 3-7 7-3-7-3-3-7z"/></svg>
            <h3 class="text-sm font-bold text-indigo-200 uppercase tracking-widest mb-4 flex items-center gap-2">
              <svg class="w-5 h-5 text-indigo-300" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" /></svg>
              导师专属点评
            </h3>
            <div class="text-sm leading-loose whitespace-pre-wrap font-medium">
              {{ personalFeedback.comment }}
            </div>
          </div>

          <div class="bg-white/60 border border-gray-200/60 rounded-2xl p-6 shadow-sm flex-1">
            <h3 class="text-sm font-bold text-gray-700 uppercase tracking-widest mb-4 flex items-center gap-2">
              <svg class="w-5 h-5 text-amber-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
              全班共性问题梳理
            </h3>
            <div class="space-y-3">
              <div 
                v-for="(issue, idx) in commonIssues" :key="idx"
                class="flex items-start gap-3 p-3 bg-amber-50/50 border border-amber-100 rounded-xl text-sm text-gray-700"
              >
                <div class="mt-0.5 text-amber-500 font-black">{{ idx + 1 }}.</div>
                <div class="leading-relaxed">{{ issue }}</div>
              </div>
            </div>
          </div>
          
        </div>

        <div class="flex-[1.8] bg-white/60 border border-gray-100 rounded-2xl flex flex-col shadow-sm overflow-hidden relative">
          
          <div class="bg-gray-50/80 border-b border-gray-100 p-4 shrink-0">
            <h3 class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-3 px-1">优秀标杆展示 (Top 3)</h3>
            <div class="flex gap-3">
              <button 
                v-for="(student, index) in excellentWorks" :key="student.id"
                @click="activeStudentId = student.id"
                class="flex-1 flex items-center gap-2 px-4 py-2.5 rounded-xl transition-all border text-left"
                :class="activeStudentId === student.id ? 'bg-indigo-50 border-indigo-200 ring-1 ring-indigo-100 shadow-sm' : 'bg-white border-transparent hover:border-gray-200'"
              >
                <div class="w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold font-mono"
                     :class="activeStudentId === student.id ? 'bg-indigo-500 text-white' : 'bg-gray-200 text-gray-500'">
                  {{ index + 1 }}
                </div>
                <span class="text-sm font-bold text-gray-700">{{ student.name }}</span>
              </button>
            </div>
          </div>

          <div class="flex items-center bg-white border-b border-gray-100 px-4 py-2 shrink-0 gap-2">
            <button 
              v-for="tab in ['req', 'plan', 'code']" :key="tab"
              @click="currentViewTab = tab"
              class="px-5 py-1.5 text-xs font-bold rounded-lg transition-all"
              :class="currentViewTab === tab ? 'bg-indigo-50 text-indigo-600 border border-indigo-100' : 'text-gray-500 hover:text-gray-700 bg-gray-50'"
            >
              {{ tab === 'req' ? '1. 需求分析' : (tab === 'plan' ? '2. 设计方案' : '3. 核心代码') }}
            </button>
          </div>

          <div class="flex-1 overflow-y-auto p-6 custom-scrollbar bg-slate-50/30">
            
            <div v-if="currentViewTab === 'req'" class="space-y-4">
              <h4 class="text-sm font-bold text-gray-700 flex items-center gap-2">
                <div class="w-1 h-4 bg-indigo-500 rounded-full"></div> 对任务需求的理解
              </h4>
              <div class="p-6 bg-white border border-gray-200 rounded-2xl text-sm text-gray-600 leading-relaxed shadow-sm">
                {{ activeStudent.req }}
              </div>
            </div>

            <div v-if="currentViewTab === 'plan'" class="space-y-4">
              <h4 class="text-sm font-bold text-gray-700 flex items-center gap-2">
                <div class="w-1 h-4 bg-indigo-500 rounded-full"></div> 架构与设计方案
              </h4>
              <div class="p-6 bg-white border border-gray-200 rounded-2xl text-sm text-gray-700 leading-loose whitespace-pre-wrap shadow-sm min-h-[200px]">
                {{ activeStudent.plan }}
              </div>
            </div>

            <div v-if="currentViewTab === 'code'" class="h-full flex flex-col">
              <h4 class="text-sm font-bold text-gray-700 flex items-center gap-2 mb-3">
                <div class="w-1 h-4 bg-indigo-500 rounded-full"></div> 最终实现代码
              </h4>
              <div class="flex-1 bg-slate-900 rounded-2xl overflow-hidden shadow-inner flex flex-col relative">
                <div class="bg-slate-800 px-4 py-2 flex items-center justify-between border-b border-slate-700 shrink-0">
                  <div class="flex items-center gap-2">
                    <div class="w-2.5 h-2.5 rounded-full bg-red-500/80"></div>
                    <div class="w-2.5 h-2.5 rounded-full bg-amber-500/80"></div>
                    <div class="w-2.5 h-2.5 rounded-full bg-green-500/80"></div>
                  </div>
                  <span class="text-[10px] font-mono text-slate-500 tracking-tighter">main.py</span>
                </div>
                <div class="flex-1 overflow-auto custom-scrollbar-dark p-6">
                  <pre class="text-xs font-mono text-slate-300 leading-loose"><code>{{ activeStudent.code }}</code></pre>
                </div>
              </div>
            </div>

          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 按钮状态管理
const isWaiting = ref(false) // 是否正在等待教师确认
const isTeacherConfirmed = ref(false) // 教师是否已确认

// 接口极简对齐：教师对当前学生的个人评价
const personalFeedback = ref({
  score: 92,
  comment: '整体表现优秀，代码逻辑清晰，切片操作掌握得很扎实。\n\n亮点：在互评环节能准确指出同学边界处理的遗漏。\n改进点：在实际开发中，如果数组极大，尽量避免频繁的 append 扩容，可以考虑初始化定长数组或使用生成器优化内存开销。继续保持！'
})

// 接口极简对齐：班级共性问题数组
const commonIssues = ref([
  '多数同学在处理空数组（[]）时未做前置判断，直接索引 arr[0] 导致程序崩溃。',
  '负数切片逻辑混淆，尤其是 start 和 stop 为负数时的步长方向理解不到位。',
  '部分代码命名不规范，过度使用 a, b, c 等无意义单字符作为变量名。'
])

// 接口极简对齐：3名优秀学生的作品展示（按 需求、方案、代码 结构化存放）
const excellentWorks = ref([
  {
    id: 'e1',
    name: '张三',
    req: '通过一趟遍历，同时找出数组的最大值和最小值，要求时间复杂度为 O(N)，并处理传入数组为空的异常。',
    plan: '1. 前置拦截：使用 if not arr 直接捕获空数组返回 None。\n2. 初始化极值：将数组的第一个元素同时赋给 max_val 和 min_val。\n3. 遍历比对：从第二个元素开始遍历，分别更新极值，最终返回 Tuple。',
    code: `def get_min_max(arr):\n    if not arr:\n        return None, None\n        \n    min_val = max_val = arr[0]\n    for num in arr[1:]:\n        if num < min_val:\n            min_val = num\n        if num > max_val:\n            max_val = num\n            \n    return min_val, max_val`
  },
  {
    id: 'e2',
    name: '李四',
    req: '利用 Python 特性，用最简洁的代码实现数组的原地反转与切片提取。',
    plan: '核心思路是充分利用 Python 强大的切片语法 [::-1] 进行逆序，不引入多余的辅助变量，保证代码 Pythonic。',
    code: `def process_array(arr):\n    # 使用切片直接返回反转后的前三个元素\n    return arr[::-1][:3]\n\n# 优雅且无惧索引越界\nprint(process_array([1, 2, 3, 4, 5]))`
  },
  {
    id: 'e3',
    name: '王五',
    req: '编写一个函数，接收包含各种脏数据（如字符串、None）的数组，提取其中的合法整数并求和。',
    plan: '由于输入不可控，重点在清洗数据。\n使用 isinstance() 判断类型，并使用列表推导式 (List Comprehension) 进行过滤，最后使用 sum() 求和。',
    code: `def sum_valid_integers(arr):\n    if not isinstance(arr, list):\n        return 0\n        \n    # 列表推导式过滤脏数据\n    clean_nums = [x for x in arr if isinstance(x, int)]\n    return sum(clean_nums)`
  }
])

const activeStudentId = ref(excellentWorks.value[0].id)
const currentViewTab = ref('req')

const activeStudent = computed(() => {
  return excellentWorks.value.find(s => s.id === activeStudentId.value)
})

const finishTraining = () => {
  if (isWaiting.value) return
  
  if (!isTeacherConfirmed.value) {
    // 第一次点击：进入等待状态
    isWaiting.value = true
    
    // 模拟1秒后教师确认
    setTimeout(() => {
      isWaiting.value = false
      isTeacherConfirmed.value = true
    }, 1000)
  } else {
    // 教师确认后：进入下一节点（返回首页）
    router.push('/student/training/start')
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

.hero-send-btn {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: white;
}

.hero-send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -5px rgba(99, 102, 241, 0.4);
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