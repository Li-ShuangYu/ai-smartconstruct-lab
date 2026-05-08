<template>
  <div class="page-wrapper flex items-center justify-center w-full h-full min-h-[calc(100vh-100px)]">
    
    <div class="glass-card w-full max-w-7xl p-6 md:p-8 flex flex-col z-10 h-[800px]">
      
      <div class="flex justify-between items-end mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase flex items-center gap-2">
            <svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
            Node: MINDMAP_PREVIEW Monitor
          </div>
          <h1 class="text-3xl font-bold text-gray-800">全班导图预习热力图监控</h1>
        </div>
        
        <div class="flex items-center gap-2 bg-white/60 px-4 py-2 rounded-xl border border-gray-100 shadow-sm">
           <span class="text-xs text-gray-500 mr-2">点击热度：</span>
           <div class="w-4 h-4 rounded bg-gray-200 border border-gray-300" title="极少查阅 (冷区)"></div>
           <div class="w-4 h-4 rounded bg-blue-200 border border-blue-300" title="偶尔查阅"></div>
           <div class="w-4 h-4 rounded bg-yellow-200 border border-yellow-300" title="常规查阅"></div>
           <div class="w-4 h-4 rounded bg-orange-400 border border-orange-500" title="重点查阅"></div>
           <div class="w-4 h-4 rounded bg-red-500 border border-red-600 shadow-[0_0_8px_rgba(239,68,68,0.5)]" title="高频查阅 (热区)"></div>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1.5] bg-[#fafafa] border border-gray-200/60 rounded-2xl p-6 shadow-inner overflow-auto custom-scrollbar relative">
          
          <div class="absolute top-4 left-4 text-xs font-bold text-gray-400">全局热力图态势 (实时同步)</div>

          <div class="flex items-center mt-10 ml-4">
             <div class="px-6 py-3 bg-gray-800 text-white font-bold rounded-xl shadow-md z-10">
               {{ heatMapData.title }}
             </div>
             
             <div class="ml-8 border-l-2 border-gray-300 pl-8 space-y-6 py-4 relative">
                <div class="absolute top-0 left-0 w-8 h-px bg-gray-300" style="top: 2rem;"></div>
                
                <div v-for="node in heatMapData.children" :key="node.id" class="relative">
                  <div class="absolute -left-8 top-1/2 w-8 h-px bg-gray-300"></div>
                  
                  <div class="flex items-center group">
                    <div class="px-4 py-2 border rounded-lg shadow-sm flex items-center gap-2 transition-transform hover:-translate-y-0.5"
                         :class="getHeatColorClass(node.heatScore)">
                       <span class="font-bold text-sm">{{ node.title }}</span>
                       <span class="text-[10px] bg-white/50 px-1.5 py-0.5 rounded-full ml-2">{{ node.clickRate }}%</span>
                    </div>
                  </div>
                  
                  <div v-if="node.children && node.children.length > 0" class="ml-12 border-l-2 border-gray-200 pl-6 mt-3 space-y-3 relative">
                    <div v-for="child in node.children" :key="child.id" class="relative flex items-center group">
                      <div class="absolute -left-6 top-1/2 w-6 h-px bg-gray-200"></div>
                      <div class="px-3 py-1.5 border rounded-md text-xs font-medium transition-all"
                           :class="getHeatColorClass(child.heatScore)">
                         {{ child.title }}
                         <span class="text-[10px] opacity-70 ml-1">({{ child.clickRate }}%)</span>
                      </div>
                    </div>
                  </div>

                </div>
             </div>
          </div>
        </div>

        <div class="flex-[1] flex flex-col gap-6">
          
          <div class="flex-[1] bg-white/60 border border-red-100 rounded-2xl p-5 shadow-sm flex flex-col">
            <h3 class="text-sm font-bold text-red-700 mb-4 flex items-center gap-2">
              <svg style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
              严重被忽略的知识盲区
            </h3>
            <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 space-y-3">
              <div v-for="node in neglectedNodes" :key="node.id" class="p-3 bg-red-50 border border-red-100 rounded-xl">
                <div class="flex justify-between items-center mb-1">
                  <span class="font-bold text-red-800 text-sm">{{ node.title }}</span>
                  <span class="text-xs font-bold text-red-500 bg-white px-2 py-0.5 rounded">点击率: {{ node.clickRate }}%</span>
                </div>
                <p class="text-xs text-red-600/80 mt-1">作为{{ node.isMandatory ? '必读核心' : '扩展节点' }}，该节点当前受关注度极低。</p>
              </div>
            </div>
          </div>

          <div class="flex-[1] bg-white/60 border border-indigo-100 rounded-2xl p-5 shadow-sm flex flex-col">
            <h3 class="text-sm font-bold text-indigo-900 mb-4 flex items-center gap-2">
              <svg style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
              整体预习进度大盘
            </h3>
            <div class="space-y-4">
              <div>
                <div class="flex justify-between text-xs mb-1 text-gray-500">
                  <span>全班必读节点平均完成度</span>
                  <span class="font-bold text-indigo-600">72%</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2">
                  <div class="bg-indigo-500 h-2 rounded-full" style="width: 72%"></div>
                </div>
              </div>
              <div class="grid grid-cols-2 gap-3 mt-4">
                <div class="bg-indigo-50 p-3 rounded-lg border border-indigo-100 text-center">
                   <div class="text-[10px] text-gray-500 mb-1">已达标人数</div>
                   <div class="text-xl font-bold text-indigo-700">24 <span class="text-xs text-indigo-400 font-normal">/32</span></div>
                </div>
                <div class="bg-amber-50 p-3 rounded-lg border border-amber-100 text-center">
                   <div class="text-[10px] text-gray-500 mb-1">未达标人数</div>
                   <div class="text-xl font-bold text-amber-600">8 <span class="text-xs text-amber-400 font-normal">/32</span></div>
                </div>
              </div>
            </div>
            
            <div class="mt-auto pt-4 text-center">
               <span class="text-xs text-gray-400 italic">*本环节设定为：无强干预，仅供教师观测。</span>
            </div>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 模拟带有热力数据的导图结构 (heatScore 0-100, clickRate 百分比)
const heatMapData = ref({
  id: 'root', title: 'SM4 密码算法',
  children: [
    { 
      id: 'n1', title: '算法基础属性', heatScore: 85, clickRate: 95, isMandatory: true,
      children: [
        { id: 'n1-1', title: '分组长度 (128bit)', heatScore: 60, clickRate: 65, isMandatory: false },
        { id: 'n1-2', title: '加解密结构对称', heatScore: 92, clickRate: 98, isMandatory: true }
      ]
    },
    {
      id: 'n2', title: '核心组成部分', heatScore: 75, clickRate: 80, isMandatory: true,
      children: [
        { id: 'n2-1', title: '轮函数 F', heatScore: 45, clickRate: 48, isMandatory: true }, // 注意：必读但点击率低！
        { id: 'n2-2', title: '非线性变换 τ (S盒)', heatScore: 98, clickRate: 100, isMandatory: true }, // 超高频
        { id: 'n2-3', title: '线性变换 L', heatScore: 15, clickRate: 12, isMandatory: false } // 盲区
      ]
    },
    {
      id: 'n3', title: '工作模式', heatScore: 5, clickRate: 8, isMandatory: false,
      children: []
    }
  ]
})

// 根据热力分数分配 CSS 类，模拟热力图颜色
const getHeatColorClass = (score) => {
  if (score >= 90) return 'bg-red-500 text-white border-red-600 shadow-[0_0_12px_rgba(239,68,68,0.4)]'
  if (score >= 70) return 'bg-orange-400 text-white border-orange-500'
  if (score >= 40) return 'bg-yellow-200 text-yellow-800 border-yellow-300'
  if (score >= 20) return 'bg-blue-100 text-blue-800 border-blue-200'
  return 'bg-gray-100 text-gray-500 border-gray-200' // 冷区
}

// 扁平化数据以找出被忽视的节点
const getNeglectedNodes = () => {
  const nodes = []
  const traverse = (node) => {
    if (node.id !== 'root' && node.clickRate < 50) {
      nodes.push(node)
    }
    if (node.children) {
      node.children.forEach(traverse)
    }
  }
  traverse(heatMapData.value)
  return nodes.sort((a, b) => a.clickRate - b.clickRate) // 点击率从小到大排序
}

const neglectedNodes = computed(() => getNeglectedNodes())
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(156, 163, 175, 0.4); border-radius: 4px; }
</style>