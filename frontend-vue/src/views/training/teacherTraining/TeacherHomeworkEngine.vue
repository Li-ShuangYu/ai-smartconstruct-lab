<template>
  <div style="height: 100vh;" class="p-6 bg-slate-50">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative overflow-hidden font-sans">
      
      <div class="flex justify-between items-end mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: HOMEWORK_MONITOR</div>
          <h1 class="text-3xl font-bold text-gray-800 flex items-center gap-3">
             <svg class="text-indigo-500" style="width: 28px; height: 28px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" /></svg>
             课后检测实时监考
          </h1>
        </div>
        
        <div class="flex items-center gap-4 bg-red-50/80 px-5 py-2.5 rounded-xl border border-red-100 shadow-sm">
           <span class="text-sm font-bold text-red-700">全班统一剩余倒计时：</span>
           <span class="font-mono text-2xl font-black text-red-600">{{ formattedGlobalTime }}</span>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1.2] flex flex-col gap-6 overflow-hidden pr-2">
          <div class="bg-white/60 border border-gray-200/60 rounded-2xl p-6 shadow-sm flex items-center justify-between shrink-0">
            <div class="flex flex-col h-full justify-center">
              <h3 class="font-bold text-gray-700 text-sm mb-1">实时交卷与批改进度</h3>
              <p class="text-xs text-gray-500 mb-4">全班总人数：{{ totalStudents }}人</p>
              <div class="text-3xl font-bold text-indigo-600">{{ submittedOrGradedCount }} <span class="text-lg text-gray-400">已交</span></div>
            </div>
            
            <div class="relative w-28 h-28 flex items-center justify-center">
              <svg class="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
                <circle cx="50" cy="50" r="40" stroke="#e5e7eb" stroke-width="8" fill="none" />
                <circle cx="50" cy="50" r="40" stroke="#6366f1" stroke-width="8" fill="none" stroke-linecap="round"
                        :stroke-dasharray="251.2" :stroke-dashoffset="251.2 - (submitRate / 100) * 251.2" 
                        class="transition-all duration-1000 ease-out" />
              </svg>
              <div class="absolute text-xl font-black text-indigo-700">{{ submitRate }}%</div>
            </div>
          </div>

          <div class="flex-1 bg-white/60 border border-gray-200/60 rounded-2xl p-6 shadow-sm overflow-hidden flex flex-col">
            <h3 class="font-bold text-gray-700 text-sm mb-4">各题错误率实时统计 (AI预批改)</h3>
            <div class="flex-1 overflow-y-auto pr-3 custom-scrollbar space-y-5">
              <div v-for="q in questionStats" :key="q.id">
                <div class="flex justify-between text-xs mb-1.5">
                  <span class="font-bold text-gray-600 truncate max-w-[75%]" :title="q.title">Q{{ q.id }}: {{ q.title }}</span>
                  <span class="font-bold" :class="q.errorRate > 50 ? 'text-red-500' : 'text-amber-500'">{{ q.errorRate }}% 错误</span>
                </div>
                <div class="w-full bg-gray-100 rounded-full h-1.5 overflow-hidden">
                  <div class="h-full rounded-full transition-all duration-500" 
                       :class="q.errorRate > 50 ? 'bg-red-400' : 'bg-amber-400'"
                       :style="{ width: q.errorRate + '%' }"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex-[2] flex flex-col bg-white/40 border border-gray-100 rounded-2xl shadow-sm overflow-hidden">
          
          <div class="bg-indigo-50/50 px-5 py-4 border-b border-indigo-100/50 flex items-center justify-between shrink-0">
            <span class="font-bold text-indigo-900 text-sm flex items-center gap-2">
               <svg style="width: 18px; height: 18px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" /></svg>
               全班作答监控明细
            </span>
            <div class="flex gap-3">
              <button @click="extendTime" class="px-4 py-2 bg-white border border-gray-200 text-gray-700 rounded-lg text-xs font-bold hover:bg-gray-50 hover:text-indigo-600 transition-colors shadow-sm">
                延长 5 分钟
              </button>
              <button @click="forceCollect" class="px-4 py-2 bg-red-50 border border-red-200 text-red-600 rounded-lg text-xs font-bold hover:bg-red-100 transition-colors shadow-sm">
                强制立即收卷
              </button>
              <button @click="exportReport" class="px-4 py-2 bg-indigo-600 border border-indigo-700 text-white rounded-lg text-xs font-bold hover:bg-indigo-700 transition-colors shadow-sm flex items-center gap-1">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
                导出成绩与报告
              </button>
            </div>
          </div>

          <div class="px-6 py-3 bg-white/60 border-b border-gray-200/50 flex justify-between items-center shrink-0">
            <div class="relative w-72">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索学生姓名..."
                class="w-full pl-9 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm text-gray-700 focus:outline-none focus:bg-white focus:border-indigo-400 transition-colors"
              >
              <svg class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
          </div>

          <div class="grid grid-cols-12 gap-4 px-8 py-3 border-b border-gray-200/50 bg-gray-50/50 text-xs font-bold text-gray-500 uppercase tracking-wider shrink-0">
            <div class="col-span-3">学生姓名</div>
            <div class="col-span-3">当前状态</div>
            <div class="col-span-3">AI 判定得分</div>
            <div class="col-span-3 text-right">操作管理</div>
          </div>
          
          <div class="flex-1 overflow-y-auto p-3 space-y-2 custom-scrollbar">
            <template v-if="paginatedStudents.length > 0">
              <div v-for="student in paginatedStudents" :key="student.id" class="grid grid-cols-12 gap-4 px-5 py-4 items-center bg-white border border-gray-100 rounded-xl shadow-sm hover:shadow-md hover:border-indigo-100 transition-all">
                
                <div class="col-span-3 flex items-center gap-3">
                  <div class="w-8 h-8 rounded-full text-white flex items-center justify-center text-sm font-bold shadow-sm"
                       :class="getAvatarColor(student.status)">
                    {{ student.name.charAt(0) }}
                  </div>
                  <span class="text-sm font-bold text-gray-800">{{ student.name }}</span>
                </div>
                
                <div class="col-span-3">
                  <span class="text-xs font-bold px-2.5 py-1 rounded-md border flex items-center gap-1.5 w-fit"
                        :class="getStatusStyle(student.status)">
                    <span v-if="student.status === 'doing'" class="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse"></span>
                    <svg v-else-if="student.status === 'graded'" class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                    <svg v-else class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                    {{ getStatusText(student.status) }}
                  </span>
                </div>

                <div class="col-span-3 font-mono font-bold text-base">
                  <span v-if="student.status === 'graded'" :class="student.score >= 80 ? 'text-green-600' : 'text-amber-600'">
                    {{ student.score }} <span class="text-xs text-gray-400 font-normal">/ 100</span>
                  </span>
                  <span v-else-if="student.status === 'submitted'" class="text-indigo-400 text-xs flex items-center gap-1">
                    <svg class="w-3 h-3 animate-spin" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
                    批阅中...
                  </span>
                  <span v-else class="text-gray-300 text-xs">暂无成绩</span>
                </div>

                <div class="col-span-3 flex justify-end">
                  <button v-if="student.status !== 'doing'" 
                          @click="openPaperReview(student)" 
                          class="px-4 py-1.5 bg-indigo-50 text-indigo-600 border border-indigo-100 hover:bg-indigo-100 hover:border-indigo-200 rounded text-xs font-bold transition-colors">
                    查看答卷
                  </button>
                  <span v-else class="text-xs text-gray-400 italic">作答中不可查看</span>
                </div>
              </div>
            </template>
            <div v-else class="h-full flex flex-col items-center justify-center text-gray-400 space-y-2 pb-6">
               <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
               <span class="text-sm font-medium">无匹配学生</span>
            </div>
          </div>

          <div class="px-6 py-3 border-t border-gray-200/60 bg-white/40 flex items-center justify-between shrink-0">
            <button @click="prevPage" :disabled="currentPage === 1" class="px-4 py-1.5 rounded-lg text-xs font-bold transition-colors" :class="currentPage === 1 ? 'text-gray-400 cursor-not-allowed' : 'text-indigo-600 bg-indigo-50 hover:bg-indigo-100'">
              上一页
            </button>
            <div class="text-xs font-bold text-gray-500">
              第 <span class="text-indigo-600">{{ currentPage }}</span> 页 / 共 {{ totalPages || 1 }} 页
            </div>
            <button @click="nextPage" :disabled="currentPage >= totalPages || totalPages === 0" class="px-4 py-1.5 rounded-lg text-xs font-bold transition-colors" :class="currentPage >= totalPages || totalPages === 0 ? 'text-gray-400 cursor-not-allowed' : 'text-indigo-600 bg-indigo-50 hover:bg-indigo-100'">
              下一页
            </button>
          </div>

        </div>

      </div>
      
      <div v-if="viewingStudent" class="absolute inset-0 z-50 bg-gray-900/60 backdrop-blur-sm rounded-[1.5rem] flex items-center justify-center animate-fade-in p-6">
        <div class="bg-slate-50 rounded-2xl w-full h-full shadow-2xl flex flex-col overflow-hidden border border-gray-200">
          
          <div class="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 shrink-0 z-10 shadow-sm">
            <div class="flex items-center gap-4">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full flex items-center justify-center text-white font-bold text-lg" :class="getAvatarColor(viewingStudent.status)">
                  {{ viewingStudent.name.charAt(0) }}
                </div>
                <div>
                  <h3 class="font-bold text-gray-800 text-lg">{{ viewingStudent.name }} 的答卷</h3>
                  <p class="text-xs text-gray-500">
                    状态: {{ getStatusText(viewingStudent.status) }} 
                    <span v-if="viewingStudent.status === 'graded'" class="ml-2 font-bold" :class="viewingStudent.score >= 80 ? 'text-green-600' : 'text-red-500'">得分: {{ viewingStudent.score }}</span>
                  </p>
                </div>
              </div>
            </div>
            <button @click="closePaperReview" class="text-gray-400 hover:text-red-500 hover:bg-red-50 p-2 rounded-lg transition-colors border border-transparent hover:border-red-100">
              <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
            </button>
          </div>
          
          <div class="flex-1 overflow-y-auto p-8 custom-scrollbar">
            <div class="max-w-4xl mx-auto space-y-6">
              
              <div v-for="(ans, index) in mockPaperDetails" :key="index" class="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm">
                
                <div class="flex items-start gap-3 mb-4">
                  <span class="px-2 py-1 bg-indigo-50 text-indigo-600 text-[10px] font-black rounded border border-indigo-100">Q{{ index + 1 }}</span>
                  <h4 class="text-sm font-bold text-gray-800 leading-relaxed">{{ ans.questionTitle }}</h4>
                </div>

                <div class="grid grid-cols-2 gap-6 bg-gray-50 p-4 rounded-xl border border-gray-100 mb-4">
                  <div>
                    <span class="text-[10px] text-gray-400 font-bold uppercase mb-1 block">学生作答</span>
                    <div class="text-sm font-medium" :class="ans.isCorrect ? 'text-green-700' : 'text-red-600 line-through opacity-80'">
                      {{ ans.studentAnswer || '未作答' }}
                    </div>
                  </div>
                  <div>
                    <span class="text-[10px] text-gray-400 font-bold uppercase mb-1 block">正确答案</span>
                    <div class="text-sm font-medium text-gray-800">{{ ans.correctAnswer }}</div>
                  </div>
                </div>

                <div v-if="viewingStudent.status === 'graded'" class="flex items-start gap-3 bg-indigo-50/50 p-4 rounded-xl border border-indigo-100">
                   <div class="w-6 h-6 rounded-full bg-indigo-100 flex items-center justify-center shrink-0">
                     <svg class="w-4 h-4 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
                   </div>
                   <div>
                     <span class="text-xs font-bold text-indigo-800 mb-1 block">AI 批阅意见</span>
                     <p class="text-xs text-indigo-900/80 leading-relaxed">{{ ans.aiComment }}</p>
                   </div>
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

// ================= 全局倒计时 =================
const globalTimeLeft = ref(15 * 60 + 30)
let timer = null

const formattedGlobalTime = computed(() => {
  const m = Math.floor(globalTimeLeft.value / 60).toString().padStart(2, '0')
  const s = (globalTimeLeft.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

onMounted(() => { timer = setInterval(() => { if (globalTimeLeft.value > 0) globalTimeLeft.value-- }, 1000) })
onUnmounted(() => { if (timer) clearInterval(timer) })

const extendTime = () => { globalTimeLeft.value += 5 * 60; alert("已为全班延长 5 分钟答题时间。") }

const forceCollect = () => { 
  if (confirm("确认强制立即收卷吗？所有未交卷学生的进度将被锁定，进入批改队列。")) {
    globalTimeLeft.value = 0
    studentList.value.forEach(s => {
      if (s.status === 'doing') { 
        s.status = 'submitted' // 强制交卷，状态变为待批改
      }
    })
  }
}

const exportReport = () => {
  alert("正在生成《Python 数组专项练习》成绩单与错题分析报告 (Excel/PDF格式)...")
}

// ================= 题库错题统计 (映射真实的Python题目) =================
const questionStats = ref([
  { id: 1, title: '在 Python 中，创建一个空列表的正确方式是？', errorRate: 15 },
  { id: 3, title: '若 arr = [10, 20, 30]，执行 arr.append(40) 后，arr 的内容是？', errorRate: 5 }, 
  { id: 5, title: '以下哪些方法可以向 Python 列表中添加元素？（多选）', errorRate: 45 },
  { id: 10, title: '请简述 Python 列表（List）与 元组（Tuple）在关于“可变性”方面的主要区别。', errorRate: 65 },
  { id: 20, title: '请编写一个 Python 表达式，演示如何使用切片技术将列表 [1, 2, 3, 4, 5] 进行原地反转。', errorRate: 72 },
])

// ================= 学生学情数据 (三态：doing, submitted, graded) =================
const studentList = ref([
  { id: 1, name: '陈静', status: 'graded', score: 95 },
  { id: 2, name: '林峰', status: 'submitted', score: null },
  { id: 3, name: '张伟', status: 'doing', score: null },
  { id: 4, name: '王磊', status: 'graded', score: 60 },
  { id: 5, name: '李娜', status: 'doing', score: null },
  { id: 6, name: '赵云', status: 'doing', score: null },
  { id: 7, name: '孙颖', status: 'graded', score: 100 },
  { id: 8, name: '周杰', status: 'graded', score: 85 },
  { id: 9, name: '吴敏', status: 'submitted', score: null },
  { id: 10, name: '郑强', status: 'graded', score: 75 },
  { id: 11, name: '刘洋', status: 'doing', score: null },
  { id: 12, name: '钱多多', status: 'submitted', score: null },
])

const totalStudents = computed(() => studentList.value.length)
const submittedOrGradedCount = computed(() => studentList.value.filter(s => s.status !== 'doing').length)
const submitRate = computed(() => Math.round((submittedOrGradedCount.value / totalStudents.value) * 100))

// 状态工具函数
const getStatusText = (status) => {
  if (status === 'doing') return '答题中'
  if (status === 'submitted') return '待批阅'
  return '已批改'
}
const getStatusStyle = (status) => {
  if (status === 'doing') return 'bg-amber-50 text-amber-600 border-amber-200'
  if (status === 'submitted') return 'bg-blue-50 text-blue-600 border-blue-200'
  return 'bg-green-50 text-green-600 border-green-200'
}
const getAvatarColor = (status) => {
  if (status === 'doing') return 'bg-gradient-to-br from-amber-400 to-orange-500'
  if (status === 'submitted') return 'bg-gradient-to-br from-blue-400 to-indigo-500'
  return 'bg-gradient-to-br from-green-400 to-emerald-500'
}

// ================= 搜索与分页逻辑 =================
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(8) 

const filteredStudents = computed(() => {
  if (!searchQuery.value.trim()) return studentList.value
  const lowerSearch = searchQuery.value.trim().toLowerCase()
  return studentList.value.filter(s => s.name.toLowerCase().includes(lowerSearch))
})

const totalPages = computed(() => Math.ceil(filteredStudents.value.length / pageSize.value))
const paginatedStudents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredStudents.value.slice(start, start + pageSize.value)
})

watch(searchQuery, () => { currentPage.value = 1 })
const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++ }

// ================= 答卷审阅模态框 =================
const viewingStudent = ref(null)
const mockPaperDetails = ref([])

const openPaperReview = (student) => {
  viewingStudent.value = student
  // 根据点击的学生生成模拟作答数据
  mockPaperDetails.value = [
    {
      questionTitle: '在 Python 中，创建一个空列表的正确方式是？',
      studentAnswer: 'B. list = []',
      correctAnswer: 'B. list = []',
      isCorrect: true,
      aiComment: '基础扎实，对列表的字面量声明语法掌握准确。'
    },
    {
      questionTitle: '以下哪些方法可以向 Python 列表中添加元素？（多选）',
      studentAnswer: student.score >= 80 ? 'A. append(), B. extend(), C. insert()' : 'A. append()',
      correctAnswer: 'A. append(), B. extend(), C. insert()',
      isCorrect: student.score >= 80,
      aiComment: student.score >= 80 ? '完全正确，熟练掌握了列表扩展的不同方法。' : '回答不全。除了 append()，还可以使用 extend() 合并序列，以及使用 insert() 在指定位置插入。'
    },
    {
      questionTitle: '请简述 Python 列表（List）与 元组（Tuple）在关于“可变性”方面的主要区别。',
      studentAnswer: student.score >= 80 ? '列表是可变的（mutable），可以直接修改元素；元组是不可变的（immutable），一旦创建内容无法更改。' : '列表可以变，元组不能变。',
      correctAnswer: '列表(List)是动态且可变的(Mutable)，可以在原地址修改数据；元组(Tuple)是静态且不可变的(Immutable)，初始化后不可修改。',
      isCorrect: true, // 算对，但点评不同
      aiComment: student.score >= 80 ? '概念解析非常清晰，准确点出了 mutable 和 immutable 的核心区别。' : '大方向正确，但建议在答题时补充“原地址修改数据”等专业术语以提升严谨性。'
    },
    {
      questionTitle: '请编写一个 Python 表达式，演示如何使用切片技术将列表 [1, 2, 3, 4, 5] 进行原地反转。',
      studentAnswer: student.score >= 80 ? 'arr[::-1]' : 'arr.reverse()',
      correctAnswer: 'arr[::-1]',
      isCorrect: student.score >= 80,
      aiComment: student.score >= 80 ? '完美！步长设为 -1 是非常经典的 Pythonic 写法。' : '注意审题，题目要求使用“切片技术”。arr.reverse() 属于列表内置方法，而切片的正解应为 arr[::-1]。'
    }
  ]
}

const closePaperReview = () => {
  viewingStudent.value = null
  mockPaperDetails.value = []
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

.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.2); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.4); }

.animate-fade-in { animation: fadeIn 0.3s ease-out forwards; }
@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.98) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
</style>