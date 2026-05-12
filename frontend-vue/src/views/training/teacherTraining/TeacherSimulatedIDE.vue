<template>
  <div style="height: 100vh;" class="p-6 bg-slate-50">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative overflow-hidden font-sans">
      
      <div class="flex justify-between items-center mb-6 shrink-0 border-b border-gray-200/50 pb-4">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase italic">Node: CODE_PRACTICE_MONITOR</div>
          <h2 class="text-2xl font-bold text-gray-800 flex items-center gap-2">编码实训实时监控</h2>
          <p class="text-sm text-gray-500 mt-1">监控全班代码提交频次，下钻查看实时代码、运行终端日志与 AI 伴学答疑情况。</p>
        </div>
        
        <div class="flex items-center gap-4">
          <div class="px-4 py-2 bg-indigo-50/80 rounded-lg border border-indigo-100 flex items-center gap-3 shadow-sm">
            <span class="text-sm text-indigo-600 font-medium">全班总提交：</span>
            <span class="text-xl font-bold text-indigo-700">{{ totalSubmissions }} <span class="text-sm font-normal text-indigo-400">次</span></span>
          </div>
          <div class="px-4 py-2 bg-green-50/80 rounded-lg border border-green-100 flex items-center gap-3 shadow-sm">
            <span class="text-sm text-green-600 font-medium">通关人数：</span>
            <span class="text-xl font-bold text-green-600">{{ passedCount }} <span class="text-sm font-normal text-green-400">/ {{ students.length }}</span></span>
          </div>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1.8] flex flex-col min-w-0 h-full border-r border-gray-200/60 pr-6">
          <div class="flex justify-between items-center mb-4 shrink-0">
            <h3 class="font-bold text-gray-700 flex items-center gap-2">
              <svg class="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" /></svg>
              学生编码与提交大盘
            </h3>
            <div class="flex items-center gap-3 text-xs">
              <span class="flex items-center gap-1 text-gray-500"><span class="w-2 h-2 rounded-full bg-green-500"></span> 已通关</span>
              <span class="flex items-center gap-1 text-gray-500"><span class="w-2 h-2 rounded-full bg-indigo-400"></span> 编码中</span>
              <span class="flex items-center gap-1 text-gray-500"><span class="w-2 h-2 rounded-full bg-orange-400"></span> 频繁报错预警</span>
            </div>
          </div>

          <div class="flex-1 overflow-y-auto custom-scrollbar pr-2">
            <div class="grid grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
              <div 
                v-for="student in students" 
                :key="student.id"
                @click="selectStudent(student)"
                class="p-4 rounded-xl border transition-all duration-300 cursor-pointer relative overflow-hidden flex flex-col items-center gap-3 group"
                :class="[
                  selectedStudent?.id === student.id ? 'bg-indigo-50/80 shadow-md transform scale-[1.02]' : 'bg-white/60 hover:bg-gray-50/80',
                  student.passed ? 'border-green-200' : (isStuck(student) ? 'border-orange-300 shadow-[0_0_10px_rgba(251,146,60,0.15)]' : 'border-gray-100 hover:border-indigo-200')
                ]"
              >
                <div class="absolute top-0 left-0 w-full flex justify-between px-3 py-2">
                   <span v-if="student.passed" class="text-green-500"><svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg></span>
                   <span v-else-if="isStuck(student)" class="text-orange-500 animate-pulse" title="提交超8次未过，疑似卡壳"><svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg></span>
                   <span v-else class="text-indigo-300"><svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" /></svg></span>
                   
                   <span class="text-[10px] font-mono font-bold" :class="isStuck(student) ? 'text-orange-600 bg-orange-100 px-1 rounded' : 'text-gray-400'">{{ student.submissions }}次</span>
                </div>

                <div class="relative mt-4">
                  <div 
                    class="w-14 h-14 rounded-full flex items-center justify-center text-white font-bold text-xl transition-all"
                    :class="student.passed ? 'bg-gradient-to-br from-green-400 to-emerald-500' : (isStuck(student) ? 'bg-gradient-to-br from-orange-400 to-red-500' : 'bg-gradient-to-br from-indigo-400 to-purple-500')"
                  >
                    {{ student.name.charAt(0) }}
                  </div>
                </div>
                
                <div class="text-center w-full">
                  <h4 class="text-sm font-bold text-gray-800">{{ student.name }}</h4>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex-[1.2] flex flex-col h-full min-w-[360px] max-w-[450px]">
          
          <div v-if="!selectedStudent" class="flex-1 flex flex-col h-full animate-fade-in bg-white/60 border border-gray-200/60 rounded-2xl p-6 shadow-sm">
            <div class="flex items-center gap-2 mb-6 shrink-0">
              <svg class="w-6 h-6 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
              <h3 class="font-bold text-indigo-900 text-lg">全班提交情况概览</h3>
            </div>

            <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 space-y-6">
              <div class="grid grid-cols-2 gap-4">
                <div class="bg-gradient-to-br from-indigo-50 to-white border border-indigo-100 p-4 rounded-xl shadow-sm text-center">
                  <div class="text-xs text-gray-500 font-bold mb-1">平均提交次数</div>
                  <div class="text-2xl font-black text-indigo-600">{{ avgSubmissions }}<span class="text-sm font-normal text-gray-500 ml-1">次/人</span></div>
                </div>
                <div class="bg-gradient-to-br from-orange-50 to-white border border-orange-100 p-4 rounded-xl shadow-sm text-center">
                  <div class="text-xs text-gray-500 font-bold mb-1">卡壳高危预警</div>
                  <div class="text-2xl font-black text-orange-600">{{ stuckStudents.length }}<span class="text-sm font-normal text-gray-500 ml-1">人</span></div>
                </div>
              </div>

              <div v-if="stuckStudents.length > 0">
                <h4 class="text-sm font-bold text-orange-700 mb-3 flex items-center gap-2">
                  <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2L1 21h22M12 6l7.5 13h-15M11 10h2v5h-2M11 16h2v2h-2"/></svg>
                  重点关注名单 (提交超8次)
                </h4>
                <div class="space-y-2">
                  <div v-for="stu in stuckStudents" :key="stu.id" @click="selectStudent(stu)" class="flex justify-between items-center p-3 bg-orange-50/50 border border-orange-100 rounded-lg cursor-pointer hover:bg-orange-100 transition-colors">
                    <span class="text-sm font-bold text-gray-700">{{ stu.name }}</span>
                    <span class="text-xs font-bold text-orange-600">已提交 {{ stu.submissions }} 次</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="flex-1 flex flex-col h-full animate-fade-in bg-white border border-gray-200/60 rounded-2xl shadow-lg overflow-hidden">
            
            <div class="p-4 border-b border-gray-100 bg-gray-50/80 shrink-0">
              <button @click="selectedStudent = null" class="text-xs font-bold text-indigo-500 hover:text-indigo-700 flex items-center gap-1 mb-3 transition-colors">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
                返回
              </button>
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-full flex items-center justify-center text-white font-bold bg-indigo-500 shadow-sm text-xs">
                    {{ selectedStudent.name.charAt(0) }}
                  </div>
                  <div>
                    <h3 class="font-bold text-gray-800 text-sm">{{ selectedStudent.name }}</h3>
                    <p class="text-[10px] text-gray-500 font-mono">共提交: {{ selectedStudent.submissions }} 次</p>
                  </div>
                </div>
                <span class="px-2 py-1 rounded text-[10px] font-bold" :class="selectedStudent.passed ? 'bg-green-100 text-green-700' : 'bg-amber-100 text-amber-600'">
                  {{ selectedStudent.passed ? '测试用例全部通过' : '编码调试中' }}
                </span>
              </div>
            </div>

            <div class="flex border-b border-gray-200 shrink-0">
              <button 
                v-for="tab in tabs" :key="tab.id"
                @click="activeTab = tab.id"
                class="flex-1 py-3 text-xs font-bold transition-colors border-b-2"
                :class="activeTab === tab.id ? 'border-indigo-500 text-indigo-600 bg-indigo-50/30' : 'border-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-50'"
              >
                {{ tab.name }}
              </button>
            </div>

            <div v-if="activeTab === 'code'" class="flex-1 overflow-y-auto p-4 bg-slate-900 custom-scrollbar-dark">
              <pre class="text-[11px] font-mono leading-relaxed text-blue-300"><code><span class="text-gray-500"># {{ selectedStudent.name }} 的最新代码提交</span>
<span class="text-purple-400">def</span> <span class="text-blue-400">process_scores</span>(scores):
    <span class="text-gray-500"># 找出最高和最低</span>
    max_score = <span class="text-indigo-300">max</span>(scores)
    min_score = <span class="text-indigo-300">min</span>(scores)
    
    <span class="text-gray-500"># 翻转并提取最后3个录入的成绩</span>
    {{ selectedStudent.codeSnippet }}
    
    <span class="text-purple-400">return</span> max_score, min_score, recent_three
</code></pre>
            </div>

            <div v-if="activeTab === 'logs'" class="flex-1 overflow-y-auto p-4 bg-black custom-scrollbar-dark">
              <div class="font-mono text-[10px] leading-loose">
                <div class="text-gray-400 mb-2">> 执行测试用例...</div>
                <template v-if="selectedStudent.passed">
                   <div class="text-green-400">[PASS] Test Case 1: normal array</div>
                   <div class="text-green-400">[PASS] Test Case 2: all same scores</div>
                   <div class="text-green-400">[PASS] Test Case 3: negative index slicing</div>
                   <div class="text-green-500 font-bold mt-4">✓ All tests passed successfully.</div>
                </template>
                <template v-else>
                   <div class="text-green-400">[PASS] Test Case 1: find max/min</div>
                   <div class="text-red-400">[FAIL] Test Case 2: slicing logic</div>
                   <div class="text-red-400 mt-2">TypeError: list indices must be integers or slices, not float</div>
                   <div class="text-gray-500 mt-1">  Line 7: recent_three = reversed_scores[0:3.5]</div>
                   <div class="text-red-500 font-bold mt-4">✗ Execution failed.</div>
                </template>
              </div>
            </div>

            <div v-if="activeTab === 'qa'" class="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar bg-gray-50">
              <div v-if="selectedStudent.qaLogs.length === 0" class="h-full flex items-center justify-center text-xs text-gray-400 italic">
                该生暂无与 AI 导师的对话记录。
              </div>
              <div v-else v-for="(msg, idx) in selectedStudent.qaLogs" :key="idx" class="flex" :class="msg.role === 'user' ? 'justify-end' : 'justify-start'">
                <div v-if="msg.role === 'user'" class="bg-indigo-500 text-white p-2.5 rounded-xl rounded-tr-sm max-w-[85%] text-xs shadow-sm">
                  {{ msg.text }}
                </div>
                <div v-else class="bg-white border border-gray-200 text-gray-700 p-2.5 rounded-xl rounded-tl-sm max-w-[85%] text-xs shadow-sm">
                  <span class="font-bold text-indigo-600 block mb-1">AI 导师</span>
                  {{ msg.text }}
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

const tabs = [
  { id: 'code', name: '实时代码' },
  { id: 'logs', name: '运行终端' },
  { id: 'qa', name: 'AI 答疑记录' }
]
const activeTab = ref('code')

// 阈值判断：大于 8 次未通关视为卡壳
const isStuck = (student) => !student.passed && student.submissions >= 8

// 模拟学生数据
const students = ref([
  { 
    id: 1, name: '张伟', passed: true, submissions: 2, 
    codeSnippet: 'reversed_scores = scores[::-1]\n    recent_three = reversed_scores[:3]',
    qaLogs: [] 
  },
  { 
    id: 2, name: '李娜', passed: false, submissions: 9, // 卡壳
    codeSnippet: 'reversed_scores = reversed(scores)\n    recent_three = reversed_scores[0:3.5]',
    qaLogs: [
      { role: 'user', text: '为什么切片会报 TypeError？' },
      { role: 'ai', text: '仔细看看你的切片语法 `[0:3.5]`。列表的索引和步长必须是什么类型的数据？可以包含小数吗？' }
    ] 
  },
  { 
    id: 3, name: '王磊', passed: true, submissions: 4, 
    codeSnippet: 'scores.reverse()\n    recent_three = scores[0:3]',
    qaLogs: [] 
  },
  { id: 4, name: '刘洋', passed: false, submissions: 1, codeSnippet: 'pass', qaLogs: [] },
  { id: 5, name: '陈静', passed: true, submissions: 1, codeSnippet: 'return max(scores), min(scores), scores[::-1][:3]', qaLogs: [] },
  { 
    id: 6, name: '杨帆', passed: false, submissions: 12, // 严重卡壳
    codeSnippet: 'scores = scores.sort()\n    recent_three = scores[3:]',
    qaLogs: [
      { role: 'user', text: '老师，我调用 sort 之后 scores 变成 None 了，为什么啊？' },
      { role: 'ai', text: '这是一个非常经典的坑。`list.sort()` 是一个“原地排序”方法，它直接修改原列表，并返回 `None`。如果你需要返回一个新的翻转列表，应该用哪个内置函数呢？（提示：拼写和 sort 很像，结尾是 ed）' }
    ] 
  },
  { id: 7, name: '赵云', passed: true, submissions: 3, codeSnippet: 'reversed_scores = scores[::-1]\n    recent_three = reversed_scores[:3]', qaLogs: [] },
  { id: 8, name: '孙颖', passed: true, submissions: 5, codeSnippet: 'scores.reverse()\n    recent_three = scores[0:3]', qaLogs: [] },
  { id: 9, name: '周杰', passed: false, submissions: 6, codeSnippet: '# 正在写代码', qaLogs: [] },
  { id: 10, name: '吴敏', passed: true, submissions: 2, codeSnippet: 'reversed_scores = scores[::-1]\n    recent_three = reversed_scores[:3]', qaLogs: [] },
  { id: 11, name: '郑强', passed: true, submissions: 1, codeSnippet: 'return max(scores), min(scores), scores[::-1][:3]', qaLogs: [] },
  { id: 12, name: '王芳', passed: false, submissions: 2, codeSnippet: 'pass', qaLogs: [] },
  { id: 13, name: '冯涛', passed: true, submissions: 3, codeSnippet: 'reversed_scores = scores[::-1]\n    recent_three = reversed_scores[:3]', qaLogs: [] },
  { id: 14, name: '陈思', passed: true, submissions: 2, codeSnippet: 'reversed_scores = scores[::-1]\n    recent_three = reversed_scores[:3]', qaLogs: [] },
  { id: 15, name: '李明', passed: false, submissions: 4, codeSnippet: '# 思考中', qaLogs: [] },
  { id: 16, name: '林峰', passed: true, submissions: 1, codeSnippet: 'return max(scores), min(scores), scores[::-1][:3]', qaLogs: [] }
])

const passedCount = computed(() => students.value.filter(s => s.passed).length)
const totalSubmissions = computed(() => students.value.reduce((sum, s) => sum + s.submissions, 0))
const avgSubmissions = computed(() => Math.round((totalSubmissions.value / students.value.length) * 10) / 10)
const stuckStudents = computed(() => students.value.filter(isStuck))

const selectedStudent = ref(null)

const selectStudent = (student) => {
  selectedStudent.value = student
  activeTab.value = 'code' // 每次切人默认看代码
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

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

/* 深色滚动条（适配代码区和终端） */
.custom-scrollbar-dark::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar-dark::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar-dark::-webkit-scrollbar-thumb {
  background: #475569;
  border-radius: 10px;
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateX(10px); }
  to { opacity: 1; transform: translateX(0); }
}
</style>