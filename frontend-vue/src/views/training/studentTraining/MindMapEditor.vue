<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 overflow-hidden border-2 border-white/40 shadow-xl relative overflow-auto">
      
      <div class="bg-white/80 backdrop-blur-md  border-b border-gray-200/50 flex justify-between items-center shrink-0 z-20">
        <div class="flex items-center gap-4">
          <div class="px-2 py-1 bg-indigo-100 text-indigo-700 rounded text-xs font-bold tracking-widest">Node: MINDMAP_DRAW</div>
          <div class="h-6 w-px bg-gray-300"></div>
          <div class="flex items-center gap-2">
            <button class="p-2 text-gray-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors" title="新增子节点" @click="addNode">
              <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
            </button>
            <button class="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors" title="删除选中节点">
              <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
            </button>
            <div class="h-4 w-px bg-gray-300 mx-1"></div>
            <button class="p-2 text-gray-400 hover:text-gray-700 rounded-lg transition-colors" title="撤销">
              <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" /></svg>
            </button>
            <button class="p-2 text-gray-400 hover:text-gray-700 rounded-lg transition-colors" title="重做">
              <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 10h-10a8 8 0 00-8 8v2M21 10l-6 6m6-6l-6-6" /></svg>
            </button>
          </div>
        </div>
        
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-2 px-3 py-1.5 rounded-lg border text-sm font-bold"
               :class="canSubmit ? 'bg-green-50 border-green-200 text-green-700' : 'bg-amber-50 border-amber-200 text-amber-600'">
            <span v-if="!canSubmit" class="relative flex h-2.5 w-2.5">
              <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
              <span class="relative inline-flex rounded-full h-2.5 w-2.5 bg-amber-500"></span>
            </span>
            <svg v-else style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
            节点数: {{ nodeCount }} / {{ minNodesRequired }}
          </div>
          
          <button @click="submitCanvas" 
                  class="hero-send-btn px-6 py-2 shadow-md transition-all"
                  :class="canSubmit && !isSubmitted ? 'hover:shadow-indigo-500/40' : 'opacity-50 grayscale cursor-not-allowed'"
                  :disabled="!canSubmit || isSubmitted">
            {{ isSubmitted ? '已提交画布' : '完成并提交' }}
          </button>
        </div>
      </div>

      <div class="flex-1 relative bg-slate-50 overflow-hidden dot-pattern">
        
        <div class="absolute top-6 right-6 w-80 bg-white/90 backdrop-blur-md border border-indigo-100 rounded-2xl p-5 shadow-xl z-20 transition-all hover:bg-white">
          <h3 class="text-sm font-bold text-indigo-900 mb-2 flex items-center gap-2">
            <svg style="width: 16px; height: 16px; flex-shrink: 0;" class="text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            绘制场景要求
          </h3>
          <p class="text-sm text-gray-600 leading-relaxed">{{ nodeConfig.scenario }}</p>
          <div class="mt-4 pt-3 border-t border-gray-100 text-xs text-gray-500">
            提示：至少包含 <span class="font-bold text-indigo-600">{{ minNodesRequired }}</span> 个节点方可提交。
          </div>
        </div>

        <div class="absolute inset-0 flex items-center justify-center transform scale-110">
           <svg class="absolute inset-0 w-full h-full pointer-events-none" style="z-index: 0;">
              <path d="M 600 400 L 400 250" stroke="#cbd5e1" stroke-width="2" fill="none" />
              <path d="M 600 400 L 800 250" stroke="#cbd5e1" stroke-width="2" fill="none" />
              <path d="M 600 400 L 600 550" stroke="#cbd5e1" stroke-width="2" fill="none" />
              <path v-if="nodeCount > 4" d="M 400 250 L 250 150" stroke="#cbd5e1" stroke-width="2" fill="none" stroke-dasharray="4" />
              <path v-if="nodeCount > 5" d="M 800 250 L 950 150" stroke="#cbd5e1" stroke-width="2" fill="none" stroke-dasharray="4" />
           </svg>

           <div class="absolute px-6 py-3 bg-gradient-to-br from-indigo-600 to-purple-600 text-white font-bold rounded-xl shadow-lg cursor-pointer hover:ring-4 ring-indigo-200 transition-all z-10" style="top: 400px; left: 600px; transform: translate(-50%, -50%);">
             RSA 算法核心流程
           </div>
           
           <div class="absolute px-5 py-2 bg-white border-2 border-indigo-200 text-gray-700 font-bold rounded-lg shadow-sm cursor-pointer hover:border-indigo-400 z-10" style="top: 250px; left: 400px; transform: translate(-50%, -50%);">
             1. 密钥生成 (Key Gen)
           </div>

           <div class="absolute px-5 py-2 bg-white border-2 border-indigo-200 text-gray-700 font-bold rounded-lg shadow-sm cursor-pointer hover:border-indigo-400 z-10" style="top: 250px; left: 800px; transform: translate(-50%, -50%);">
             2. 加密过程 (Encryption)
           </div>

           <div class="absolute px-5 py-2 bg-white border-2 border-indigo-200 text-gray-700 font-bold rounded-lg shadow-sm cursor-pointer hover:border-indigo-400 z-10" style="top: 550px; left: 600px; transform: translate(-50%, -50%);">
             3. 解密过程 (Decryption)
           </div>

           <transition name="pop">
             <div v-if="nodeCount > 4" class="absolute px-4 py-1.5 bg-gray-50 border border-gray-300 text-gray-600 text-sm font-medium rounded cursor-text z-10" style="top: 150px; left: 250px; transform: translate(-50%, -50%); outline: 2px solid #818cf8;">
               选择大素数 p, q
             </div>
           </transition>

           <transition name="pop">
             <div v-if="nodeCount > 5" class="absolute px-4 py-1.5 bg-gray-50 border border-gray-300 text-gray-600 text-sm font-medium rounded cursor-text z-10" style="top: 150px; left: 950px; transform: translate(-50%, -50%);">
               C = M^e mod n
             </div>
           </transition>

        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const nodeConfig = ref({
  scenario: '请结合理论课程，从头绘制 RSA 算法的核心流程。必须包含：密钥生成步骤（选取素数、计算n、欧拉函数、公私钥d和e）、加密公式以及解密公式。',
  baseMap: 'root_only' // 仅给一个根节点
})

// 模拟状态
const minNodesRequired = ref(6)
const nodeCount = ref(4) // 初始模拟画布上有4个节点
const isSubmitted = ref(false)

const canSubmit = computed(() => nodeCount.value >= minNodesRequired.value)

// 模拟点击添加节点
const addNode = () => {
  if (nodeCount.value < 8) {
    nodeCount.value++
  }
}

const submitCanvas = () => {
  if (canSubmit.value && !isSubmitted.value) {
    if (confirm("提交后将锁定画布并进入下一步，确定提交吗？")) {
      isSubmitted.value = true
      alert("画布提交成功！")
    }
  }
}
</script>

<style scoped>
/* 生成极客风点阵白板背景 */
.dot-pattern {
  background-image: radial-gradient(#cbd5e1 1.5px, transparent 1.5px);
  background-size: 24px 24px;
}

.pop-enter-active { transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.pop-enter-from { opacity: 0; transform: translate(-50%, -50%) scale(0.5) !important; }
.pop-enter-to { opacity: 1; transform: translate(-50%, -50%) scale(1) !important; }
</style>