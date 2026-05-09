<template>
  <div style="height:100%">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10">
      
      <div class="flex flex-col md:flex-row justify-between items-start md:items-end mb-6 pb-4 border-b border-gray-200/50 shrink-0 gap-4">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase flex items-center gap-2">
            <svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" /></svg>
            Node: STUDENT_PEER_REVIEW
          </div>
          <h1 class="text-3xl font-bold text-gray-800">同伴产出互评：SM4 代码实现</h1>
        </div>
        
        <div class="flex items-center gap-3 bg-white/60 px-5 py-2.5 rounded-xl border border-gray-100 shadow-sm">
          <span class="text-sm font-bold text-gray-600">互评任务进度：</span>
          <div class="flex gap-2">
            <div v-for="i in requiredCount" :key="i" class="w-8 h-2.5 rounded-full transition-colors duration-300"
                 :class="i <= reviewedCount ? 'bg-green-400' : 'bg-gray-200'"></div>
          </div>
          <span class="text-sm font-bold w-12 text-right" :class="reviewedCount >= requiredCount ? 'text-green-600' : 'text-indigo-600'">
            {{ reviewedCount }}/{{ requiredCount }}
          </span>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1.8] flex flex-col bg-white/40 border border-gray-200/60 rounded-2xl shadow-sm overflow-hidden">
          
          <div class="flex overflow-x-auto border-b border-gray-200/50 bg-gray-50/50 custom-scrollbar shrink-0">
            <button v-for="artifact in pendingArtifacts" :key="artifact.id"
                    @click="selectArtifact(artifact)"
                    class="px-6 py-4 text-sm font-bold border-b-2 transition-colors whitespace-nowrap flex items-center gap-2"
                    :class="selectedArtifact?.id === artifact.id ? 'border-indigo-500 text-indigo-700 bg-white' : 'border-transparent text-gray-500 hover:bg-gray-100'">
               <span class="w-2 h-2 rounded-full" :class="artifact.isReviewed ? 'bg-green-400' : 'bg-amber-400'"></span>
               {{ artifact.author }} 的提交
            </button>
          </div>

          <div class="flex-1 overflow-y-auto p-6 custom-scrollbar bg-[#1e1e1e] relative group">
            <div v-if="selectedArtifact" class="text-gray-300 font-mono text-sm leading-relaxed whitespace-pre-wrap">
              <div class="text-xs text-gray-500 mb-4 border-b border-gray-700 pb-2 flex justify-between">
                <span>文件名: {{ selectedArtifact.fileName }}</span>
                <span>语言: Python</span>
              </div>
              <span class="text-pink-400">def</span> <span class="text-blue-400">sm4_round_function</span>(x0, x1, x2, x3, rk):
                  <span class="text-gray-500"># {{ selectedArtifact.author }} 的轮函数实现</span>
                  {{ selectedArtifact.codeSnippet }}
            </div>
          </div>
        </div>

        <div class="flex-[1.2] flex flex-col gap-4">
          
          <div class="flex-1 bg-white/60 border border-gray-100 rounded-2xl p-6 shadow-sm overflow-y-auto custom-scrollbar flex flex-col">
            <h3 class="text-lg font-bold text-gray-800 mb-6 flex items-center gap-2 border-b border-gray-200 pb-3">
              <svg style="width: 20px; height: 20px; flex-shrink: 0;" class="text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" /></svg>
              维度打分卡
            </h3>
            
            <template v-if="selectedArtifact">
              <div v-if="selectedArtifact.isReviewed" class="absolute inset-0 bg-white/60 backdrop-blur-sm z-10 flex flex-col items-center justify-center rounded-2xl">
                 <svg style="width: 48px; height: 48px; flex-shrink: 0;" class="text-green-500 mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                 <span class="font-bold text-gray-700">您已完成对该同学的评价</span>
              </div>

              <div class="space-y-8 flex-1">
                <div v-for="dim in dimensions" :key="dim" class="space-y-3">
                  <div class="flex justify-between items-end">
                    <label class="font-bold text-gray-700 text-sm">{{ dim }}</label>
                    <span class="font-mono font-bold text-indigo-600 text-lg">{{ currentForm[dim] || 0 }} <span class="text-xs text-gray-400">分</span></span>
                  </div>
                  <input type="range" min="0" max="100" v-model="currentForm[dim]" class="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-indigo-600">
                  <div class="flex justify-between text-xs text-gray-400">
                    <span>需改进</span>
                    <span>优秀</span>
                  </div>
                </div>

                <div class="mt-4 border-t border-gray-100 pt-6">
                  <label class="font-bold text-gray-700 text-sm mb-2 block">综合评语 (可选)</label>
                  <textarea v-model="currentForm.comment" placeholder="写下你对这份代码的建议或鼓励..." class="w-full bg-gray-50 border border-gray-200 rounded-xl p-3 text-sm text-gray-700 outline-none focus:border-indigo-400 focus:bg-white transition-colors resize-none h-24 custom-scrollbar"></textarea>
                </div>
              </div>

              <button @click="submitCurrentReview" class="mt-6 w-full py-3 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl shadow-md transition-all">
                提交对此份作品的评价
              </button>
            </template>
            <template v-else>
               <div class="flex-1 flex items-center justify-center text-gray-400 text-sm">请先在左侧选择要评价的作品</div>
            </template>
          </div>

        </div>
      </div>

      <div class="mt-6 shrink-0 flex items-center justify-between bg-white/40 p-4 rounded-xl border border-gray-100">
         <span class="text-sm text-gray-600">
           <span class="font-bold text-indigo-600">规则要求：</span>请客观公正打分。系统会自动识别恶意刷分行为。
         </span>
         <button 
            class="hero-send-btn px-8 py-3 rounded-xl shadow-lg transition-all duration-300"
            :class="reviewedCount >= requiredCount ? 'hover:shadow-indigo-500/40 hover:-translate-y-1' : 'opacity-50 grayscale cursor-not-allowed'"
            :disabled="reviewedCount < requiredCount"
         >
            完成所有互评，进入下一步
            <svg style="width: 16px; height: 16px; flex-shrink: 0; margin-left: 4px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
         </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

const dimensions = ref(['代码规范', '创新性', '密码学正确性'])
const requiredCount = ref(3)

// 模拟待评产出物
const pendingArtifacts = ref([
  { id: 1, author: '匿名为7A', fileName: 'sm4_core.py', codeSnippet: '\n  temp = x1 ^ x2 ^ x3 ^ rk\n  # 加入了位运算优化\n  return x0 ^ tau_transform(temp)', isReviewed: false },
  { id: 2, author: '匿名为2B', fileName: 'sm4_sbox.py', codeSnippet: '\n  # 查表法实现 S 盒替换\n  res = (SBOX[(a >> 24) & 0xFF] << 24) | ...\n  return res', isReviewed: false },
  { id: 3, author: '匿名为9C', fileName: 'main.py', codeSnippet: '\n  # 基础实现，缺乏异常处理\n  def encrypt(plaintext, key):\n      pass', isReviewed: false }
])

const selectedArtifact = ref(pendingArtifacts.value[0])
const currentForm = reactive({ '代码规范': 50, '创新性': 50, '密码学正确性': 50, comment: '' })

const reviewedCount = computed(() => pendingArtifacts.value.filter(a => a.isReviewed).length)

const selectArtifact = (artifact) => {
  selectedArtifact.value = artifact
  // 重置表单
  currentForm['代码规范'] = 50
  currentForm['创新性'] = 50
  currentForm['密码学正确性'] = 50
  currentForm.comment = ''
}

const submitCurrentReview = () => {
  if (selectedArtifact.value) {
    selectedArtifact.value.isReviewed = true
    alert("该作品评价提交成功！")
  }
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.3); border-radius: 4px; }
</style>