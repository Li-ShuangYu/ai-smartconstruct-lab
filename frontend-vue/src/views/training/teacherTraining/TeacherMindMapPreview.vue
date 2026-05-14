<template>
  <div style="height: 100vh;" class="p-6 bg-slate-50">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative overflow-hidden">
      
      <div class="flex justify-between items-center mb-6 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: MINDMAP_MONITOR</div>
          <h2 class="text-2xl font-bold text-gray-800">思维导图评价与难度热力监控</h2>
          <p class="text-sm text-gray-500 mt-1">实时监控全班对各知识点的预习难度反馈，节点颜色代表整体掌握难度。</p>
        </div>
        
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-3 bg-white/50 px-4 py-2 rounded-xl border border-indigo-100 shadow-sm">
            <span class="text-sm font-bold text-gray-600">全班已提交评价：</span>
            <span class="text-xl font-bold text-indigo-600">{{ submittedCount }}<span class="text-sm text-gray-400"> / {{ totalStudents }}</span></span>
          </div>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[3] bg-gray-50/50 rounded-2xl border border-gray-200/60 shadow-inner overflow-hidden relative group">
          <div class="absolute inset-0 opacity-[0.03] pointer-events-none" style="background-image: radial-gradient(#6366f1 1px, transparent 1px); background-size: 30px 30px;"></div>
          
          <div ref="mindMapContainer" class="w-full h-full outline-none cursor-pointer"></div>

          <div class="absolute bottom-4 left-4 bg-white/90 backdrop-blur-sm p-3 rounded-xl border border-gray-200 shadow-lg z-10">
            <p class="text-[10px] font-bold text-gray-500 mb-2 uppercase tracking-widest">知识难度热力指数</p>
            <div class="flex items-center gap-2">
              <span class="text-xs font-bold text-green-600">易</span>
              <div class="w-48 h-2.5 rounded-full bg-gradient-to-r from-green-500 via-yellow-400 to-red-500"></div>
              <span class="text-xs font-bold text-red-600">难</span>
            </div>
            <div class="flex justify-between mt-1 px-5">
              <span class="text-[10px] text-gray-400">0</span>
              <span class="text-[10px] text-gray-400">50</span>
              <span class="text-[10px] text-gray-400">100</span>
            </div>
          </div>

          <div class="absolute bottom-4 right-4 flex gap-2 z-10">
            <button @click="handleZoomIn" class="p-2 bg-white rounded-lg border border-gray-200 shadow-sm hover:bg-indigo-50 text-gray-600 transition-colors" title="放大">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
            </button>
            <button @click="handleZoomOut" class="p-2 bg-white rounded-lg border border-gray-200 shadow-sm hover:bg-indigo-50 text-gray-600 transition-colors" title="缩小">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" /></svg>
            </button>
            <button @click="handleFitView" class="p-2 bg-white rounded-lg border border-gray-200 shadow-sm hover:bg-indigo-50 text-gray-600 transition-colors" title="适应屏幕">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4h16v16H4V4z" /></svg>
            </button>
          </div>
        </div>

        <div class="flex-[1] min-w-[320px] max-w-[400px] flex flex-col gap-4">
          <div class="flex-1 bg-white/60 border border-gray-200/60 rounded-2xl shadow-sm flex flex-col overflow-hidden">
            
            <div v-if="activeNode && activeNode.id !== 'root'" class="h-full flex flex-col">
              
              <div class="p-6 pb-4 border-b border-gray-100 shrink-0 bg-gradient-to-b from-gray-50/50 to-white">
                <span class="px-2 py-1 bg-indigo-100 text-indigo-600 text-[10px] font-bold rounded uppercase tracking-wider">节点分析</span>
                <h3 class="text-xl font-bold text-gray-800 mt-2 mb-4">{{ activeNode.label }}</h3>
                
                <div class="flex items-center gap-4">
                  <div class="relative w-16 h-16 shrink-0">
                    <svg class="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                      <path class="text-gray-100" stroke-width="3" stroke="currentColor" fill="transparent" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                      <path :stroke="getHeatmapColor(getNodeScore(activeNode.id))" stroke-width="3" :stroke-dasharray="`${getNodeScore(activeNode.id)}, 100`" stroke-linecap="round" fill="transparent" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                    </svg>
                    <div class="absolute inset-0 flex flex-col items-center justify-center">
                      <span class="text-lg font-black" :style="{ color: getHeatmapColor(getNodeScore(activeNode.id)) }">{{ getNodeScore(activeNode.id) }}</span>
                    </div>
                  </div>
                  <div>
                    <p class="text-sm font-bold text-gray-700">难度系数: {{ getDifficultyText(getNodeScore(activeNode.id)) }}</p>
                    <p class="text-xs text-gray-500 mt-1">分数越高代表该知识点对全班越困难，需重点讲解。</p>
                  </div>
                </div>
              </div>

              <div class="flex-1 flex flex-col min-h-0">
                <div class="px-6 py-3 bg-gray-50/80 border-b border-gray-100 shrink-0 flex justify-between items-center">
                  <span class="text-xs font-bold text-gray-600">评价明细</span>
                  <span class="text-[10px] text-gray-400">共 {{ getNodeEvaluations(activeNode.id).length }} 人参与</span>
                </div>
                
                <div class="flex-1 overflow-y-auto custom-scrollbar p-2">
                  <div class="space-y-1">
                    <div 
                      v-for="(evalRecord, idx) in getNodeEvaluations(activeNode.id)" 
                      :key="idx"
                      class="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition-colors relative"
                    >
                      <div class="flex items-center gap-2">
                        <div class="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center text-xs font-bold text-gray-600">
                          {{ evalRecord.name.charAt(0) }}
                        </div>
                        <span class="text-sm font-medium text-gray-700">{{ evalRecord.name }}</span>
                        <span 
                          v-if="evalRecord.question"
                          class="question-tag w-5 h-5 rounded-full bg-amber-100 text-amber-600 flex items-center justify-center text-xs font-bold cursor-help hover:bg-amber-200 transition-colors relative"
                          title=""
                        >
                          ?
                          <span class="question-tooltip absolute left-1/2 -translate-x-1/2 bottom-full mb-2 px-3 py-2 bg-gray-800 text-white text-xs rounded-lg opacity-0 invisible hover:opacity-100 hover:visible transition-all whitespace-nowrap z-20 max-w-[240px]">
                            {{ evalRecord.question }}
                          </span>
                        </span>
                      </div>
                      
                      <span 
                        class="px-3 py-1 rounded-full text-xs font-bold"
                        :class="getBadgeClass(evalRecord.rating)"
                      >
                        {{ difficultyMap[evalRecord.rating] }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

            </div>

            <div v-else class="h-full flex flex-col items-center justify-center text-center p-6 bg-gradient-to-b from-white to-gray-50/50">
              <div class="w-20 h-20 bg-indigo-50 rounded-full flex items-center justify-center mb-4 text-indigo-300">
                <svg class="w-10 h-10" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
              </div>
              <h3 class="text-lg font-bold text-gray-800 mb-2">大盘概览模式</h3>
              <p class="text-gray-500 text-sm leading-relaxed">
                当前显示为全班知识点掌握难度热力图。<br/>
                <strong class="text-indigo-500">点击左侧导图的任意知识节点</strong>，<br/>即可查看该节点的详细得分与每个学生的打分记录。
              </p>
            </div>

          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import MindMap from 'simple-mind-map'

// ================= 数据字典与模拟数据 =================
const difficultyMap = { 'easy': '容易', 'medium': '一般', 'hard': '困难' }
const submittedCount = ref(28)
const totalStudents = ref(32)

// 原始结构数据 (与学生端一致)
const rootNode = ref({
  id: 'root',
  label: 'Python 数组实训',
  children: [
    {
      id: 'n1', label: '基础定义',
      children: [
        { id: 'n1-1', label: '连续存储' },
        { id: 'n1-2', label: '静态 vs 动态' }
      ]
    },
    {
      id: 'n2', label: '核心操作',
      children: [
        { id: 'n2-1', label: '高级切片' },
        { id: 'n2-2', label: '原地操作' }
      ]
    }
  ]
})

// 模拟所有学生对各个节点的打分数据
// 数据结构: { nodeId: [ { name: '学生名', rating: 'easy|medium|hard', question?: '疑问内容' }, ... ] }
const mockEvaluations = {
  'n1': [
    { name: '张伟', rating: 'easy' }, { name: '李娜', rating: 'easy', question: '什么是数组的定义？' }, 
    { name: '王磊', rating: 'medium' },
    { name: '刘洋', rating: 'easy' }, { name: '陈静', rating: 'easy' }, { name: '赵云', rating: 'medium', question: '数组和列表有什么区别？' }
  ],
  'n1-1': [
    { name: '张伟', rating: 'medium', question: '连续存储是什么意思？内存布局是怎样的？' }, 
    { name: '李娜', rating: 'hard', question: '不太理解连续存储的优势在哪里' }, 
    { name: '王磊', rating: 'hard' },
    { name: '刘洋', rating: 'medium' }, { name: '陈静', rating: 'hard' }, { name: '赵云', rating: 'medium' }
  ],
  'n1-2': [
    { name: '张伟', rating: 'easy' }, { name: '李娜', rating: 'medium' }, { name: '王磊', rating: 'medium' },
    { name: '刘洋', rating: 'hard', question: '动态数组如何实现自动扩容？' }, 
    { name: '陈静', rating: 'medium' }, { name: '赵云', rating: 'easy' }
  ],
  'n2': [
    { name: '张伟', rating: 'medium' }, { name: '李娜', rating: 'medium', question: '核心操作包括哪些？' }, 
    { name: '王磊', rating: 'hard' },
    { name: '刘洋', rating: 'hard' }, { name: '陈静', rating: 'medium' }, { name: '赵云', rating: 'hard' }
  ],
  'n2-1': [ // 高级切片 (非常难)
    { name: '张伟', rating: 'hard', question: '切片的步长参数怎么用？' }, 
    { name: '李娜', rating: 'hard', question: '负数索引不太明白' }, 
    { name: '王磊', rating: 'hard', question: '切片赋值时内存是怎么处理的？' },
    { name: '刘洋', rating: 'hard' }, { name: '陈静', rating: 'medium' }, { name: '赵云', rating: 'hard', question: '[:-1] 和 [0:-1] 有区别吗？' }
  ],
  'n2-2': [ // 原地操作 (较容易)
    { name: '张伟', rating: 'easy' }, { name: '李娜', rating: 'easy' }, { name: '王磊', rating: 'easy' },
    { name: '刘洋', rating: 'medium' }, { name: '陈静', rating: 'easy' }, { name: '赵云', rating: 'easy' }
  ]
}

// ================= 核心计算逻辑 =================

// 获取某节点的所有打分记录
const getNodeEvaluations = (nodeId) => {
  return mockEvaluations[nodeId] || []
}

// 算法：计算加权难度分 (easy:0, medium:50, hard:100)
const getNodeScore = (nodeId) => {
  const evals = getNodeEvaluations(nodeId)
  if (evals.length === 0) return 0
  let totalScore = 0
  evals.forEach(e => {
    if (e.rating === 'hard') totalScore += 100
    else if (e.rating === 'medium') totalScore += 50
    // easy 为 0
  })
  return Math.round(totalScore / evals.length)
}

// 根据分数值返回 HEX 颜色 (用于简单导图节点渲染和环形图)
const getHeatmapColor = (score) => {
  if (score <= 20) return '#22c55e' // text-green-500
  if (score <= 40) return '#84cc16' // text-lime-500
  if (score <= 60) return '#eab308' // text-yellow-500
  if (score <= 80) return '#f97316' // text-orange-500
  return '#ef4444'                  // text-red-500
}

const getDifficultyText = (score) => {
  if (score <= 33) return '易掌握'
  if (score <= 66) return '需关注'
  return '高难度预警'
}

// 右侧学生列表 Badge 样式
const getBadgeClass = (rating) => {
  if (rating === 'easy') return 'bg-green-100 text-green-700'
  if (rating === 'medium') return 'bg-yellow-100 text-yellow-700'
  if (rating === 'hard') return 'bg-red-100 text-red-700'
  return 'bg-gray-100 text-gray-500'
}

// ================= MindMap 渲染逻辑 =================

const activeNode = ref(null)
const mindMapContainer = ref(null)
let mindMapInstance = null

// 提取所有节点扁平数组
const allNodes = computed(() => {
  const result = []
  const traverse = (node) => {
    result.push(node)
    if (node.children) node.children.forEach(traverse)
  }
  traverse(rootNode.value)
  return result
})

const transformData = (node) => {
  return {
    data: { text: node.label, uid: node.id, ...node },
    children: node.children ? node.children.map(transformData) : []
  }
}

const findNodeById = (id, currentNode = rootNode.value) => {
  if (currentNode.id === id) return currentNode
  if (currentNode.children) {
    for (let child of currentNode.children) {
      const found = findNodeById(id, child)
      if (found) return found
    }
  }
  return null
}

onMounted(() => {
  mindMapInstance = new MindMap({
    el: mindMapContainer.value,
    direction: 'right',
    theme: 'fresh-purple',
    data: transformData(rootNode.value),
    fit: true,
    mouseScaleBehavior: 'zoom',
    readonly: true,
    enableFreeDrag: false,
    
    // 初始化统一样式结构，后续会通过渲染器重写颜色
    themeConfig: {
      root: { expandBtnSize: 0, shape: 'rectangle', fillColor: '#334155', color: '#fff' },
      second: { expandBtnSize: 0, shape: 'rectangle', paddingX: 16, paddingY: 8, borderWidth: 0 },
      node: { expandBtnSize: 0, shape: 'rectangle', paddingX: 14, paddingY: 8, borderWidth: 0 },
      line: { color: '#cbd5e1', width: 2 }
    }
  })

  mindMapInstance.on('node_click', (node, e) => {
    const uid = node.nodeData.data.uid
    const foundNode = findNodeById(uid)
    if (foundNode) {
      activeNode.value = foundNode
    } else {
      activeNode.value = null
    }
  })

  // 渲染完成后，根据算法分数进行热力颜色涂装
  setTimeout(() => {
    allNodes.value.forEach(node => {
      if (node.id === 'root') return // 根节点不涂装
      
      const score = getNodeScore(node.id)
      const bgColor = getHeatmapColor(score)
      const nodeInstance = mindMapInstance.renderer.findNodeByUid(node.id)
      
      if (nodeInstance) {
        nodeInstance.setStyle('fillColor', bgColor)
        nodeInstance.setStyle('color', '#ffffff') // 保证文字反色清晰
      }
    })
  }, 300) // 延迟确保实例树构建完毕
})

onBeforeUnmount(() => {
  if (mindMapInstance) {
    mindMapInstance.destroy()
  }
})

const handleZoomIn = () => mindMapInstance && mindMapInstance.view.enlarge()
const handleZoomOut = () => mindMapInstance && mindMapInstance.view.narrow()
const handleFitView = () => mindMapInstance && mindMapInstance.view.fit()

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
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

/* 覆盖 simple-mind-map 样式 */
:deep(.smm-canvas) {
  outline: none !important;
}
:deep(.smm-expand-btn) {
  display: none !important;
  opacity: 0 !important;
  pointer-events: none !important;
}
:deep(.smm-node.active) {
  outline: 3px solid rgba(99, 102, 241, 0.5) !important;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1) !important;
}

.question-tag:hover .question-tooltip {
  opacity: 1 !important;
  visibility: visible !important;
}

.question-tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 6px solid transparent;
  border-top-color: #1f2937;
}
</style>