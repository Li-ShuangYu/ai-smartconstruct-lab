<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10">
      
      <div class="flex flex-col md:flex-row justify-between items-start md:items-end mb-6 pb-4 border-b border-gray-200/50 shrink-0 gap-4">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase flex items-center gap-2">
            <svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" /></svg>
            Node: KNOWLEDGE_EVAL
          </div>
          <h1 class="text-3xl font-bold text-gray-800 mb-2">知识点难度认知自评</h1>
          <p class="text-sm text-gray-600 bg-indigo-50/50 px-4 py-2 rounded-lg border border-indigo-100 max-w-2xl">
            <span class="font-bold text-indigo-600">评价要求：</span>{{ nodeConfig.scenario }}
          </p>
        </div>
        
        <div class="flex items-center gap-3 bg-white/60 px-5 py-2.5 rounded-xl border border-gray-100 shadow-sm">
          <span class="text-sm font-bold text-gray-600">评价完成度：</span>
          <div class="w-32 bg-gray-200 rounded-full h-2.5">
            <div class="bg-indigo-500 h-2.5 rounded-full transition-all duration-500" :style="{ width: progressPercentage + '%' }"></div>
          </div>
          <span class="text-sm font-bold w-12 text-right" :class="isAllEvaluated ? 'text-green-600' : 'text-indigo-600'">
            {{ evaluatedCount }}/{{ totalNodes }}
          </span>
        </div>
      </div>

      <div class="flex-1 bg-white/40 border border-gray-200/60 rounded-2xl p-6 shadow-sm overflow-y-auto custom-scrollbar relative">
        <div class="space-y-3">
          
          <div v-for="node in flattenedNodes" :key="node.id" 
               class="flex items-center justify-between p-3 rounded-xl transition-all border group"
               :class="evaluations[node.id] ? 'bg-white border-gray-100 shadow-sm' : 'bg-gray-50/50 border-transparent hover:bg-indigo-50/30 hover:border-indigo-100'"
               :style="{ paddingLeft: `${node.level * 2 + 1}rem` }">
            
            <div class="flex items-center gap-3 relative">
              <div v-if="node.level > 0" class="absolute -left-6 top-1/2 w-4 h-px bg-gray-300"></div>
              <div v-if="node.level > 0" class="absolute -left-6 -top-4 w-px h-8 bg-gray-300"></div>
              
              <span class="w-2 h-2 rounded-full" :class="node.level === 0 ? 'bg-indigo-500 w-3 h-3' : 'bg-gray-400'"></span>
              <span class="font-bold text-gray-700" :class="node.level === 0 ? 'text-lg' : 'text-sm'">{{ node.title }}</span>
            </div>

            <div class="flex items-center gap-2">
              <label class="cursor-pointer relative">
                <input type="radio" :name="node.id" value="easy" v-model="evaluations[node.id]" class="peer sr-only">
                <div class="px-4 py-1.5 rounded-lg border text-xs font-bold transition-all
                            peer-checked:bg-green-500 peer-checked:text-white peer-checked:border-green-600 peer-checked:shadow-sm
                            border-gray-200 text-gray-500 hover:border-green-300 hover:bg-green-50">
                  容易
                </div>
              </label>
              
              <label class="cursor-pointer relative">
                <input type="radio" :name="node.id" value="medium" v-model="evaluations[node.id]" class="peer sr-only">
                <div class="px-4 py-1.5 rounded-lg border text-xs font-bold transition-all
                            peer-checked:bg-amber-500 peer-checked:text-white peer-checked:border-amber-600 peer-checked:shadow-sm
                            border-gray-200 text-gray-500 hover:border-amber-300 hover:bg-amber-50">
                  适中
                </div>
              </label>

              <label class="cursor-pointer relative">
                <input type="radio" :name="node.id" value="hard" v-model="evaluations[node.id]" class="peer sr-only">
                <div class="px-4 py-1.5 rounded-lg border text-xs font-bold transition-all
                            peer-checked:bg-red-500 peer-checked:text-white peer-checked:border-red-600 peer-checked:shadow-sm
                            border-gray-200 text-gray-500 hover:border-red-300 hover:bg-red-50">
                  困难
                </div>
              </label>
            </div>
            
          </div>

        </div>
      </div>

      <div class="mt-6 shrink-0">
        <button 
          class="hero-send-btn w-full justify-center text-lg py-4 rounded-xl shadow-lg transition-all duration-300"
          :class="isAllEvaluated ? 'hover:shadow-indigo-500/40 hover:-translate-y-1' : 'opacity-50 grayscale cursor-not-allowed'"
          :disabled="!isAllEvaluated"
          @click="submitEvaluation"
        >
          <svg v-if="!isAllEvaluated" style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          <svg v-else style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
          {{ isAllEvaluated ? '评价完成，确认提交' : `还差 ${totalNodes - evaluatedCount} 个节点未评价` }}
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const nodeConfig = ref({
  scenario: '请根据您刚才预习和绘制的过程，客观评价每个知识点的理解难度。这将帮助我们优化后续的教学资源。'
})

// 模拟导图树状数据，并将其扁平化以方便渲染列表
const mapData = [
  { id: 'root', title: 'RSA 密码算法体系', level: 0 },
  { id: 'n1', title: '大素数的生成与测试', level: 1 },
  { id: 'n1-1', title: 'Miller-Rabin 素性检验', level: 2 },
  { id: 'n2', title: '密钥计算过程', level: 1 },
  { id: 'n2-1', title: '模数 n = p × q', level: 2 },
  { id: 'n2-2', title: '欧拉函数 φ(n) = (p-1)(q-1)', level: 2 },
  { id: 'n2-3', title: '公钥 e 的选取', level: 2 },
  { id: 'n2-4', title: '扩展欧几里得算法求私钥 d', level: 2 },
  { id: 'n3', title: '加解密公式', level: 1 },
  { id: 'n3-1', title: '加密: C = M^e mod n', level: 2 },
  { id: 'n3-2', title: '解密: M = C^d mod n', level: 2 },
]

const flattenedNodes = ref(mapData)
const totalNodes = computed(() => flattenedNodes.value.length)

// 存储评价结果的响应式对象 { 'n1': 'easy', 'n2': 'hard', ... }
const evaluations = ref({})

const evaluatedCount = computed(() => Object.keys(evaluations.value).length)
const progressPercentage = computed(() => (evaluatedCount.value / totalNodes.value) * 100)
const isAllEvaluated = computed(() => evaluatedCount.value === totalNodes.value)

const submitEvaluation = () => {
  if (isAllEvaluated.value) {
    alert("自评数据提交成功！即将进入下一步。")
  }
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.2); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.4); }
</style>