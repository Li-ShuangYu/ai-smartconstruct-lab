<template>
  <div style="height: 100%;">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative overflow-hidden">
      
      <div class="flex justify-between items-center mb-6 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: MINDMAP_PREVIEW</div>
          <h2 class="text-2xl font-bold text-gray-800">思维导图预习与自评</h2>
        </div>
        
        <div class="flex items-center gap-6">
          <div class="flex items-center gap-3 bg-white/50 px-4 py-2 rounded-xl border border-indigo-100 shadow-sm">
            <div class="relative w-10 h-10">
              <svg class="w-full h-full" viewBox="0 0 36 36">
                <path class="text-gray-200" stroke-width="3" stroke="currentColor" fill="transparent" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                <path class="text-indigo-500 transition-all duration-500" stroke-width="3" :stroke-dasharray="`${evalProgress}, 100`" stroke-linecap="round" stroke="currentColor" fill="transparent" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              </svg>
              <div class="absolute inset-0 flex items-center justify-center text-[10px] font-bold text-indigo-600">{{ Math.round(evalProgress) }}%</div>
            </div>
            <div class="text-xs">
              <p class="text-gray-500">评估进度</p>
              <p class="font-bold text-gray-700">{{ evaluatedCount }} / {{ totalNodes }} 核心节点</p>
            </div>
          </div>

          <button 
            @click="handleSubmit"
            class="hero-send-btn px-8 py-3 rounded-xl shadow-lg transition-all flex items-center gap-2"
            :class="{'opacity-50 grayscale cursor-not-allowed': !isAllEvaluated, 'hover:shadow-indigo-500/30': isAllEvaluated}"
            :disabled="!isAllEvaluated"
          >
            完成预习提交
            <svg style="width: 18px; height: 18px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
          </button>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[3] bg-gray-50/50 rounded-2xl border border-gray-200/60 shadow-inner overflow-hidden relative group">
          <div class="absolute inset-0 opacity-[0.03] pointer-events-none" style="background-image: radial-gradient(#6366f1 1px, transparent 1px); background-size: 30px 30px;"></div>
          
          <div ref="mindMapContainer" class="w-full h-full outline-none"></div>

          <div class="absolute bottom-4 left-4 flex gap-2 z-10">
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
          <div class="flex-1 bg-white/60 border border-gray-200/60 rounded-2xl p-6 shadow-sm flex flex-col overflow-hidden">
            <div v-if="activeNode" class="h-full flex flex-col">
              <div class="shrink-0 mb-6">
                <span class="px-2 py-1 bg-indigo-100 text-indigo-600 text-[10px] font-bold rounded uppercase tracking-wider">Knowledge Point</span>
                <h3 class="text-xl font-bold text-gray-800 mt-2">{{ activeNode.label }}</h3>
              </div>
              
              <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 mb-6">
                <div class="bg-indigo-50/30 border border-indigo-100/50 rounded-xl p-4 mb-4">
                  <h4 class="text-xs font-bold text-indigo-500 mb-2 flex items-center gap-1">
                    <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                    AI 知识解析
                  </h4>
                  <p class="text-sm text-gray-600 leading-relaxed italic">
                    {{ activeNode.desc || '选中节点以查看详细解析与学习建议。' }}
                  </p>
                </div>
                
                <div class="space-y-4">
                  <h4 class="text-xs font-bold text-gray-400 uppercase tracking-widest">学习要点</h4>
                  <ul class="space-y-2">
                    <li v-for="(item, idx) in activeNode.points" :key="idx" class="flex gap-2 text-sm text-gray-600">
                      <span class="text-indigo-400 font-bold">•</span> {{ item }}
                    </li>
                  </ul>
                </div>
              </div>

              <div class="shrink-0 pt-6 border-t border-gray-100">
                <p class="text-xs font-bold text-gray-500 mb-4 text-center uppercase tracking-tighter">评估您对此知识点的掌握程度</p>
                <div class="grid grid-cols-3 gap-3">
                  <button 
                    v-for="(label, key) in difficultyMap" :key="key"
                    @click="markDifficulty(key)"
                    class="flex flex-col items-center gap-2 py-3 rounded-xl border transition-all"
                    :class="[
                      activeNode.difficulty === key 
                      ? `bg-${getColor(key)}-50 border-${getColor(key)}-200 text-${getColor(key)}-600 shadow-sm scale-[1.02]` 
                      : 'bg-white border-gray-100 text-gray-400 hover:border-gray-200'
                    ]"
                  >
                    <span class="text-lg">{{ getEmoji(key) }}</span>
                    <span class="text-xs font-bold">{{ label }}</span>
                  </button>
                </div>
              </div>
            </div>

            <div v-else class="h-full flex flex-col items-center justify-center text-center">
              <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mb-4 text-gray-300">
                <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122" /></svg>
              </div>
              <p class="text-gray-400 text-sm">请在左侧思维导图中<br/>点击一个知识点进行预习</p>
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

const difficultyMap = { 'easy': '容易', 'medium': '一般', 'hard': '困难' }

// 原始业务数据
const rootNode = ref({
  id: 'root',
  label: 'Python 数组实训',
  children: [
    {
      id: 'n1',
      label: '基础定义',
      desc: '数组在 Python 中通常通过列表(List)或 array 模块实现，是存放相同类型数据的集合。',
      points: ['List 的内存模型', '索引与偏移量关系', '时间复杂度分析'],
      difficulty: null,
      children: [
        { id: 'n1-1', label: '连续存储', points: ['Cache Line 对齐', '物理内存布局'], difficulty: null },
        { id: 'n1-2', label: '静态 vs 动态', points: ['扩容因子', 'Overhead 消耗'], difficulty: null }
      ]
    },
    {
      id: 'n2',
      label: '核心操作',
      desc: '掌握数组的增删改查是算法优化的基础，尤其是切片(Slicing)的高级用法。',
      points: ['浅拷贝 vs 深拷贝', '正向/负向切片', 'Step 步长逻辑'],
      difficulty: null,
      children: [
        { id: 'n2-1', label: '高级切片', points: ['[::2] 隔行取样', '[::-1] 翻转数组'], difficulty: null },
        { id: 'n2-2', label: '原地操作', points: ['pop() 与 remove()', 'del 关键字'], difficulty: null }
      ]
    }
  ]
})

const activeNode = ref(null)
const mindMapContainer = ref(null)
let mindMapInstance = null

// 获取所有核心节点用于计算进度
const allNodes = computed(() => {
  const result = []
  const traverse = (node) => {
    if (node.id !== 'root') result.push(node)
    if (node.children) node.children.forEach(traverse)
  }
  traverse(rootNode.value)
  return result
})

const totalNodes = computed(() => allNodes.value.length)
const evaluatedCount = computed(() => allNodes.value.filter(n => n.difficulty).length)
const evalProgress = computed(() => (evaluatedCount.value / totalNodes.value) * 100)
const isAllEvaluated = computed(() => evaluatedCount.value === totalNodes.value)

// 转换业务数据为 simple-mind-map 所需格式
const transformData = (node) => {
  return {
    data: {
      text: node.label,
      uid: node.id, 
      ...node 
    },
    children: node.children ? node.children.map(transformData) : []
  }
}

// 递归查找业务节点数据
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
    readonly: true, // 预习模式只读
    enableFreeDrag: false,
    themeConfig: {
      node: { expandBtnSize: 0 },
      root: { expandBtnSize: 0 }
    }
  })

  mindMapInstance.on('node_click', (node, e) => {
    const uid = node.nodeData.data.uid
    const foundNode = findNodeById(uid)
    if (foundNode) {
      activeNode.value = foundNode
    }
  })
})

onBeforeUnmount(() => {
  if (mindMapInstance) {
    mindMapInstance.destroy()
  }
})

// --- 控件与业务方法 ---
const handleZoomIn = () => mindMapInstance && mindMapInstance.view.enlarge()
const handleZoomOut = () => mindMapInstance && mindMapInstance.view.narrow()
const handleFitView = () => mindMapInstance && mindMapInstance.view.fit()

const markDifficulty = (level) => {
  if (activeNode.value && activeNode.value.id !== 'root') {
    activeNode.value.difficulty = level
    updateNodeStyle(activeNode.value.id, level)
  }
}

const getColor = (key) => key === 'easy' ? 'green' : (key === 'medium' ? 'indigo' : 'red')
const getEmoji = (key) => key === 'easy' ? '😊' : (key === 'medium' ? '😐' : '😰')

// 【核心修改】通过 simple-mind-map 的内部 API 更新节点渲染样式
const updateNodeStyle = (nodeId, difficulty) => {
  if (!mindMapInstance) return
  
  // simple-mind-map 使用 fillColor 和 color 控制背景和字体色
  const styleMap = {
    'easy': { fillColor: '#22c55e', color: '#ffffff', borderColor: '#16a34a' },
    'medium': { fillColor: '#6366f1', color: '#ffffff', borderColor: '#4f46e5' },
    'hard': { fillColor: '#ef4444', color: '#ffffff', borderColor: '#dc2626' }
  }
  
  const styles = styleMap[difficulty]
  
  // 在 readonly 模式下，获取 renderer 中的节点实例并直接覆盖样式属性
  const nodeInstance = mindMapInstance.renderer.findNodeByUid(nodeId)
  
  if (nodeInstance) {
    nodeInstance.setStyle('fillColor', styles.fillColor)
    nodeInstance.setStyle('color', styles.color)
    nodeInstance.setStyle('borderColor', styles.borderColor)
  }
}

const handleSubmit = () => {
  alert('预习自评提交成功！')
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

.hero-send-btn:not(:disabled):hover {
  transform: translateY(-2px);
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
  height: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

/* 覆盖 simple-mind-map 默认获取焦点时的黑边 */
:deep(.smm-canvas) {
  outline: none !important;
}

/* 强制隐藏 simple-mind-map 的加减号展开按钮组 */
:deep(.smm-expand-btn) {
  display: none !important;
  opacity: 0 !important;
  pointer-events: none !important;
}

/* 隐藏节点处于 active 状态下的自带外边框，让样式更加纯净 */
:deep(.smm-node.active) {
  outline: none !important;
}
</style>