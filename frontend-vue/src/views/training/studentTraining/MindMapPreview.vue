<template>
  <div class="page-wrapper flex items-center justify-center w-full h-full min-h-[calc(100vh-100px)]">
    
    <div class="glass-card w-full max-w-7xl p-6 md:p-8 flex flex-col z-10 h-[800px]">
      
      <div class="flex justify-between items-end mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase flex items-center gap-2">
            <svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" /></svg>
            Node: MINDMAP_PREVIEW
          </div>
          <h1 class="text-3xl font-bold text-gray-800">思维导图预习：{{ nodeConfig.scenario }}</h1>
        </div>
        
        <div class="flex items-center gap-3">
          <span class="text-sm font-bold text-gray-600">必读节点完成度：</span>
          <div class="w-32 bg-gray-200 rounded-full h-2.5">
            <div class="bg-indigo-500 h-2.5 rounded-full transition-all duration-500" :style="{ width: progressPercentage + '%' }"></div>
          </div>
          <span class="text-sm font-bold text-indigo-600 w-10 text-right">{{ readMandatoryCount }}/{{ mandatoryTotal }}</span>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[1.5] bg-white/40 border border-gray-200/60 rounded-2xl p-6 shadow-sm overflow-auto custom-scrollbar relative">
          <div class="absolute top-4 right-4 flex gap-3 text-xs font-medium text-gray-500 bg-white/80 px-3 py-1.5 rounded-lg border border-gray-100 shadow-sm">
             <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-red-400"></span> 必读节点</span>
             <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-green-400"></span> 已阅读</span>
          </div>

          <div class="flex items-center mt-10 ml-4">
             <div class="px-6 py-3 bg-gradient-to-r from-indigo-500 to-purple-500 text-white font-bold rounded-xl shadow-md z-10">
               {{ mapData.title }}
             </div>
             
             <div class="ml-8 border-l-2 border-indigo-200 pl-8 space-y-6 py-4 relative">
                <div class="absolute top-0 left-0 w-8 h-px bg-indigo-200" style="top: 2rem;"></div>
                
                <div v-for="node in mapData.children" :key="node.id" class="relative">
                  <div class="absolute -left-8 top-1/2 w-8 h-px bg-indigo-200"></div>
                  
                  <div class="flex items-center cursor-pointer group" @click="selectNode(node)">
                    <div class="px-4 py-2 bg-white border-2 rounded-lg shadow-sm transition-all duration-300 flex items-center gap-2 group-hover:-translate-y-0.5 group-hover:shadow-md"
                         :class="[
                           selectedNode?.id === node.id ? 'border-indigo-500 ring-2 ring-indigo-200' : 'border-gray-200 hover:border-indigo-300',
                           readNodes.has(node.id) ? 'opacity-70' : ''
                         ]">
                       
                       <span class="w-2.5 h-2.5 rounded-full" 
                             :class="readNodes.has(node.id) ? 'bg-green-400' : (node.isMandatory ? 'bg-red-400 animate-pulse' : 'bg-gray-300')">
                       </span>
                       
                       <span class="font-bold text-gray-700 text-sm">{{ node.title }}</span>
                    </div>
                  </div>
                  
                  <div v-if="node.children && node.children.length > 0" class="ml-12 border-l-2 border-gray-100 pl-6 mt-3 space-y-3 relative">
                    <div v-for="child in node.children" :key="child.id" class="relative flex items-center cursor-pointer group" @click="selectNode(child)">
                      <div class="absolute -left-6 top-1/2 w-6 h-px bg-gray-200"></div>
                      <div class="px-3 py-1.5 bg-gray-50 border border-gray-200 rounded-md text-xs font-medium text-gray-600 transition-all hover:bg-indigo-50 hover:text-indigo-600 hover:border-indigo-200"
                           :class="[
                             selectedNode?.id === child.id ? 'bg-indigo-100 border-indigo-400 text-indigo-700 font-bold' : '',
                             readNodes.has(child.id) ? 'text-gray-400 line-through' : ''
                           ]">
                         <span v-if="child.isMandatory && !readNodes.has(child.id)" class="text-red-400 mr-1">*</span>
                         {{ child.title }}
                      </div>
                    </div>
                  </div>

                </div>
             </div>
          </div>
        </div>

        <div class="flex-[1] flex flex-col gap-4">
          
          <div class="flex-1 bg-white/60 border border-gray-100 rounded-2xl p-6 shadow-sm overflow-y-auto custom-scrollbar">
            <template v-if="selectedNode">
              <div class="mb-4 flex items-center gap-2">
                <span v-if="selectedNode.isMandatory" class="px-2 py-0.5 bg-red-50 text-red-500 border border-red-100 text-[10px] font-bold rounded-full">必读核心</span>
                <span v-if="readNodes.has(selectedNode.id)" class="px-2 py-0.5 bg-green-50 text-green-600 border border-green-100 text-[10px] font-bold rounded-full">已阅</span>
              </div>
              
              <h2 class="text-2xl font-bold text-gray-800 mb-4">{{ selectedNode.title }}</h2>
              
              <div class="prose prose-sm text-gray-600 leading-relaxed whitespace-pre-wrap">
                {{ selectedNode.content || '该节点暂无详细说明。' }}
              </div>
              
              <div v-if="selectedNode.hasImage" class="mt-6 w-full h-40 bg-gray-100 border border-dashed border-gray-300 rounded-xl flex items-center justify-center text-gray-400 text-sm">
                 <svg style="width: 24px; height: 24px; flex-shrink: 0; margin-right: 8px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                 知识点配图 / 图表
              </div>
            </template>
            
            <template v-else>
              <div class="h-full flex flex-col items-center justify-center text-gray-400">
                <svg style="width: 64px; height: 64px; flex-shrink: 0; margin-bottom: 16px; opacity: 0.5;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
                <p>请点击左侧导图节点，阅读详细知识点</p>
              </div>
            </template>
          </div>

          <div class="shrink-0">
             <div v-if="isAllMandatoryRead" class="mb-3 flex items-center justify-center gap-2 text-sm text-green-600 bg-green-50 p-3 rounded-xl border border-green-100 shadow-sm animate-fade-in">
                <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                <span class="font-bold">所有必读核心节点均已查阅完毕</span>
             </div>
             
             <button 
                class="hero-send-btn w-full justify-center text-base py-4 rounded-xl shadow-lg transition-all duration-300"
                :class="isAllMandatoryRead ? 'hover:shadow-indigo-500/40 hover:-translate-y-1' : 'opacity-50 grayscale cursor-not-allowed'"
                :disabled="!isAllMandatoryRead"
             >
                进入下一实训环节
                <svg style="width: 16px; height: 16px; flex-shrink: 0; margin-left: 4px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
             </button>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const nodeConfig = ref({
  scenario: '国密 SM4 算法全景概览'
})

// 模拟系统绑定的初始导图结构
const mapData = ref({
  id: 'root',
  title: 'SM4 密码算法',
  children: [
    { 
      id: 'n1', title: '算法基础属性', isMandatory: true, 
      content: 'SM4 是一个分组密码算法。其分组长度为 128 比特，密钥长度也为 128 比特。这意味着它每次处理 16 个字节的明文块。',
      children: [
        { id: 'n1-1', title: '分组长度 (128bit)', isMandatory: false, content: '固定为 128 bit，无法变长。' },
        { id: 'n1-2', title: '加解密结构对称', isMandatory: true, content: 'SM4 加密算法与密钥扩展算法都采用 32 轮非线性迭代结构。解密算法与加密算法的结构完全相同，只是轮密钥的使用顺序相反。' }
      ]
    },
    {
      id: 'n2', title: '核心组成部分', isMandatory: true,
      content: '算法主要由轮函数、非线性变换（S盒）和线性变换构成。',
      hasImage: true,
      children: [
        { id: 'n2-1', title: '轮函数 F', isMandatory: true, content: 'F(X0, X1, X2, X3, rk) = X0 ⊕ T(X1 ⊕ X2 ⊕ X3 ⊕ rk)。这是实现每一轮混淆和扩散的公式。' },
        { id: 'n2-2', title: '非线性变换 τ (S盒)', isMandatory: true, content: 'τ 是由 4 个并置的 S 盒构成的非线性置换，这是 SM4 安全性的核心，提供唯一的非线性来源。' },
        { id: 'n2-3', title: '线性变换 L', isMandatory: false, content: 'L 变换主要是循环移位和异或，用于提供扩散效应，使 S 盒的局部影响迅速扩散到整个分组。' }
      ]
    },
    {
      id: 'n3', title: '工作模式', isMandatory: false,
      content: '作为分组密码，SM4 可用于各种标准工作模式，如 ECB, CBC, CTR, GCM 等。',
      children: []
    }
  ]
})

const selectedNode = ref(null)
const readNodes = ref(new Set())

// 递归获取所有必读节点 ID
const getMandatoryIds = (node) => {
  let ids = []
  if (node.isMandatory) ids.push(node.id)
  if (node.children) {
    node.children.forEach(child => {
      ids = ids.concat(getMandatoryIds(child))
    })
  }
  return ids
}

const mandatoryNodesList = computed(() => getMandatoryIds(mapData.value))
const mandatoryTotal = computed(() => mandatoryNodesList.value.length)

const readMandatoryCount = computed(() => {
  let count = 0
  mandatoryNodesList.value.forEach(id => {
    if (readNodes.value.has(id)) count++
  })
  return count
})

const isAllMandatoryRead = computed(() => readMandatoryCount.value === mandatoryTotal.value)

const selectNode = (node) => {
  selectedNode.value = node
  readNodes.value.add(node.id)
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.2); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.4); }

.animate-fade-in { animation: fadeIn 0.4s ease-out forwards; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>