<template>
  <div class="page-wrapper w-full min-h-screen">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10">
      
      <div class="flex justify-between items-end mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-red-500 tracking-widest uppercase flex items-center gap-2">
            <svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
            Node: KNOWLEDGE_EVAL Monitor
          </div>
          <h1 class="text-3xl font-bold text-gray-800">全班知识盲区标红预警地图</h1>
          <p class="text-sm text-gray-500 mt-2">基于 {{ studentTotal }} 名学生主观自评数据实时聚合计算</p>
        </div>
        
        <div class="flex items-center gap-4 bg-white/60 px-5 py-3 rounded-2xl border border-gray-100 shadow-sm">
           <div class="flex flex-col items-center">
             <span class="text-xs text-gray-500">已提交自评</span>
             <span class="text-xl font-bold text-green-600">{{ submittedCount }} / {{ studentTotal }}</span>
           </div>
           <div class="w-px h-8 bg-gray-200"></div>
           <div class="flex flex-col items-center">
             <span class="text-xs text-gray-500">检测出重度盲区</span>
             <span class="text-xl font-bold text-red-600">{{ severeBlindSpots.length }} 个</span>
           </div>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1.5] bg-[#fafafa] border border-gray-200/60 rounded-2xl p-6 shadow-inner overflow-auto custom-scrollbar relative dot-pattern">
          
          <div class="absolute top-4 right-4 bg-white/80 backdrop-blur-sm p-3 rounded-lg border border-gray-100 shadow-sm text-xs space-y-2 z-20">
             <div class="font-bold text-gray-700 mb-1">困难指数图例</div>
             <div class="flex items-center gap-2"><span class="w-3 h-3 bg-red-600 rounded-sm shadow-[0_0_8px_rgba(220,38,38,0.5)]"></span> &gt; 60% 认为困难 (重度预警)</div>
             <div class="flex items-center gap-2"><span class="w-3 h-3 bg-amber-400 rounded-sm"></span> 30% - 60% 认为困难</div>
             <div class="flex items-center gap-2"><span class="w-3 h-3 bg-green-400 rounded-sm"></span> &lt; 30% 认为困难 (掌握良好)</div>
          </div>

          <div class="flex items-center mt-12 ml-4">
             <div class="px-6 py-3 bg-gray-800 text-white font-bold rounded-xl shadow-md z-10">
               RSA 密码算法体系
             </div>
             
             <div class="ml-8 border-l-2 border-gray-300 pl-8 space-y-8 py-4 relative">
                <div class="absolute top-0 left-0 w-8 h-px bg-gray-300" style="top: 2rem;"></div>
                
                <div v-for="node in aggregatedData" :key="node.id" class="relative">
                  <div class="absolute -left-8 top-1/2 w-8 h-px bg-gray-300"></div>
                  
                  <div class="flex items-center">
                    <div class="px-4 py-2 border-2 rounded-lg shadow-sm flex flex-col transition-all relative overflow-hidden"
                         :class="getAlertClass(node.hardRate)">
                       <div v-if="node.hardRate > 60" class="absolute inset-0 bg-red-500 opacity-10 animate-pulse"></div>
                       <span class="font-bold text-sm z-10">{{ node.title }}</span>
                    </div>
                  </div>
                  
                  <div v-if="node.children" class="ml-12 border-l-2 border-gray-200 pl-6 mt-4 space-y-4 relative">
                    <div v-for="child in node.children" :key="child.id" class="relative flex items-center group cursor-pointer" @click="selectNode(child)">
                      <div class="absolute -left-6 top-1/2 w-6 h-px bg-gray-200"></div>
                      
                      <div class="flex flex-col bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden transition-all group-hover:border-indigo-400 group-hover:shadow-md"
                           :class="selectedNode?.id === child.id ? 'ring-2 ring-indigo-400' : ''">
                        
                        <div class="h-1.5 w-full" :class="getBarColorClass(child.hardRate)"></div>
                        
                        <div class="px-4 py-2 flex items-center justify-between gap-6 relative">
                           <span class="font-bold text-gray-700 text-sm z-10">{{ child.title }}</span>
                           
                           <div class="flex items-center text-[10px] font-bold w-24 h-4 rounded-full overflow-hidden bg-gray-100">
                             <div class="h-full bg-red-500" :style="{ width: child.hardRate + '%' }" title="困难"></div>
                             <div class="h-full bg-amber-400" :style="{ width: child.mediumRate + '%' }" title="适中"></div>
                             <div class="h-full bg-green-400" :style="{ width: child.easyRate + '%' }" title="容易"></div>
                           </div>
                           
                           <div v-if="child.hardRate > 60" class="absolute -right-1 -top-1 w-3 h-3 bg-red-500 rounded-full animate-ping"></div>
                        </div>
                      </div>
                      
                    </div>
                  </div>

                </div>
             </div>
          </div>
        </div>

        <div class="flex-[1] flex flex-col gap-6">
          
          <div class="flex-1 bg-white/60 border border-gray-100 rounded-2xl p-6 shadow-sm overflow-y-auto custom-scrollbar flex flex-col">
            <template v-if="selectedNode">
              <h3 class="text-xl font-bold text-gray-800 mb-2 border-b border-gray-200 pb-3">{{ selectedNode.title }}</h3>
              
              <div class="my-4 space-y-4">
                <div>
                  <div class="flex justify-between text-sm mb-1"><span class="text-red-600 font-bold">认为困难 (Hard)</span><span class="font-bold">{{ selectedNode.hardRate }}%</span></div>
                  <div class="w-full bg-red-50 rounded-full h-2"><div class="bg-red-500 h-2 rounded-full" :style="{ width: selectedNode.hardRate + '%' }"></div></div>
                </div>
                <div>
                  <div class="flex justify-between text-sm mb-1"><span class="text-amber-600 font-bold">认为适中 (Medium)</span><span class="font-bold">{{ selectedNode.mediumRate }}%</span></div>
                  <div class="w-full bg-amber-50 rounded-full h-2"><div class="bg-amber-400 h-2 rounded-full" :style="{ width: selectedNode.mediumRate + '%' }"></div></div>
                </div>
                <div>
                  <div class="flex justify-between text-sm mb-1"><span class="text-green-600 font-bold">认为容易 (Easy)</span><span class="font-bold">{{ selectedNode.easyRate }}%</span></div>
                  <div class="w-full bg-green-50 rounded-full h-2"><div class="bg-green-500 h-2 rounded-full" :style="{ width: selectedNode.easyRate + '%' }"></div></div>
                </div>
              </div>

              <div v-if="selectedNode.hardRate > 60" class="mt-4 p-4 bg-red-50 border border-red-100 rounded-xl">
                 <h4 class="text-sm font-bold text-red-700 mb-2 flex items-center gap-2">
                   <svg style="width: 16px; height: 16px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                   AI 课后复盘建议
                 </h4>
                 <p class="text-xs text-red-600/80 leading-relaxed">
                   该知识点成为全班重灾区。建议在下节课采用具体的数学数值推导（例如选取极小的素数 p=3, q=11）带领学生在黑板上手动演算一遍过程，而非仅展示公式。
                 </p>
              </div>
            </template>
            
            <template v-else>
              <div class="h-full flex flex-col items-center justify-center text-gray-400">
                <svg style="width: 64px; height: 64px; flex-shrink: 0; margin-bottom: 16px; opacity: 0.5;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
                <p>点击左侧节点查看详细难度分布</p>
              </div>
            </template>
          </div>

          <div class="shrink-0 pt-4 text-center border-t border-gray-200/50">
             <button class="px-6 py-2.5 bg-white border border-gray-300 text-gray-700 rounded-xl text-sm font-bold hover:bg-gray-50 transition-colors shadow-sm flex items-center justify-center gap-2 w-full">
               <svg style="width: 16px; height: 16px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
               导出班级学情诊断报告 (PDF)
             </button>
             <p class="text-xs text-gray-400 mt-3">* 本节点主要用于数据沉淀与课后教学复盘，无需强干预操作。</p>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const studentTotal = ref(32)
const submittedCount = ref(30)

// 模拟聚合后的难度数据 (hardRate, mediumRate, easyRate 总和 100)
const aggregatedData = ref([
  { 
    id: 'n1', title: '大素数的生成与测试', hardRate: 75,
    children: [
      { id: 'n1-1', title: 'Miller-Rabin 素性检验', hardRate: 85, mediumRate: 10, easyRate: 5 }, // 严重盲区
    ]
  },
  {
    id: 'n2', title: '密钥计算过程', hardRate: 40,
    children: [
      { id: 'n2-1', title: '模数 n = p × q', hardRate: 5, mediumRate: 15, easyRate: 80 }, // 简单
      { id: 'n2-2', title: '欧拉函数 φ(n)', hardRate: 20, mediumRate: 40, easyRate: 40 },
      { id: 'n2-3', title: '公钥 e 的选取', hardRate: 35, mediumRate: 50, easyRate: 15 },
      { id: 'n2-4', title: '扩展欧几里得算法求私钥 d', hardRate: 92, mediumRate: 5, easyRate: 3 }, // 终极盲区
    ]
  },
  {
    id: 'n3', title: '加解密公式', hardRate: 25,
    children: [
      { id: 'n3-1', title: '加密: C = M^e mod n', hardRate: 25, mediumRate: 45, easyRate: 30 },
      { id: 'n3-2', title: '解密: M = C^d mod n', hardRate: 30, mediumRate: 40, easyRate: 30 },
    ]
  }
])

const selectedNode = ref(null)

const selectNode = (node) => {
  selectedNode.value = node
}

// 获取一级节点的外边框颜色
const getAlertClass = (hardRate) => {
  if (hardRate > 60) return 'bg-red-50 border-red-400 text-red-800 shadow-[0_0_15px_rgba(248,113,113,0.3)]'
  if (hardRate > 30) return 'bg-amber-50 border-amber-300 text-amber-800'
  return 'bg-white border-gray-200 text-gray-700'
}

// 获取叶子节点的顶部指示条颜色
const getBarColorClass = (hardRate) => {
  if (hardRate > 60) return 'bg-red-500'
  if (hardRate > 30) return 'bg-amber-400'
  return 'bg-green-400'
}

// 提取重度盲区数量
const severeBlindSpots = computed(() => {
  let spots = []
  aggregatedData.value.forEach(p => {
    if (p.children) {
      p.children.forEach(c => {
        if (c.hardRate > 60) spots.push(c)
      })
    }
  })
  return spots
})
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(156, 163, 175, 0.4); border-radius: 4px; }

.dot-pattern {
  background-image: radial-gradient(#e2e8f0 1.5px, transparent 1.5px);
  background-size: 24px 24px;
}
</style>