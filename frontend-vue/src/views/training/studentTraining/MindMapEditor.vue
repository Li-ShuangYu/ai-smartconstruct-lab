<template>
  <div style="height: 100%;">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative overflow-hidden">
      
      <div class="flex justify-between items-center mb-6 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: MINDMAP_DRAWING</div>
          <h2 class="text-2xl font-bold text-gray-800">思维导图绘制练习</h2>
        </div>
        
        <div class="flex items-center gap-6">
          <div class="flex items-center gap-3 bg-white/50 px-4 py-2 rounded-xl border border-indigo-100 shadow-sm">
            <div class="flex flex-col items-end">
              <p class="text-[10px] text-gray-400 uppercase font-bold tracking-tighter">当前节点数</p>
              <p class="font-mono font-bold text-indigo-600">
                <span class="text-xl">{{ currentNodeCount }}</span>
                <span class="text-xs text-gray-400 ml-1">/ {{ minRequiredNodes }}</span>
              </p>
            </div>
            <div class="w-10 h-10 flex items-center justify-center">
              <svg class="w-8 h-8" :class="currentNodeCount >= minRequiredNodes ? 'text-green-500' : 'text-indigo-300'" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>

          <div class="flex gap-3">
            <button @click="handleSave" class="px-6 py-3 rounded-xl border border-indigo-200 text-indigo-600 font-bold bg-white hover:bg-indigo-50 transition-all flex items-center gap-2">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" /></svg>
              暂存草稿
            </button>
            <button 
              @click="handleSubmit"
              class="hero-send-btn px-8 py-3 rounded-xl shadow-lg transition-all flex items-center gap-2"
              :class="{'opacity-50 grayscale cursor-not-allowed': !isReadyToSubmit, 'hover:shadow-indigo-500/30': isReadyToSubmit}"
              :disabled="!isReadyToSubmit"
            >
              提交实训成果
              <svg style="width: 18px; height: 18px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
            </button>
          </div>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">
        
        <div class="flex-[3] flex flex-col bg-gray-50/50 rounded-2xl border border-gray-200/60 shadow-inner overflow-hidden relative group">
          
          <div class="absolute top-4 left-1/2 -translate-x-1/2 flex items-center gap-1 p-1.5 bg-white/90 backdrop-blur shadow-xl border border-indigo-100 rounded-2xl z-20">
            <button @mousedown.stop.prevent @click.stop.prevent="execCommand('INSERT_CHILD_NODE')" class="toolbar-btn" title="插入子节点 (Tab)">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
              <span class="text-[10px] mt-0.5">子节点</span>
            </button>
            <button @mousedown.stop.prevent @click.stop.prevent="execCommand('INSERT_NODE')" class="toolbar-btn" title="插入同级节点 (Enter)">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 14v6m-3-3h6M6 10h2a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v2a2 2 0 002 2z" /></svg>
              <span class="text-[10px] mt-0.5">同级节点</span>
            </button>
            <div class="w-px h-8 bg-gray-100 mx-1"></div>
            <button @mousedown.stop.prevent @click.stop.prevent="execCommand('REMOVE_NODE')" class="toolbar-btn text-red-400 hover:text-red-500" title="删除节点 (Del)">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
              <span class="text-[10px] mt-0.5">删除</span>
            </button>
            <div class="w-px h-8 bg-gray-100 mx-1"></div>
            <button @mousedown.stop.prevent @click.stop.prevent="execCommand('BACK')" class="toolbar-btn" title="撤销 (Ctrl+Z)">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" /></svg>
            </button>
            <button @mousedown.stop.prevent @click.stop.prevent="execCommand('FORWARD')" class="toolbar-btn" title="重做 (Ctrl+Y)">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 10h-10a8 8 0 00-8 8v2m18-10l-6 6m6-6l-6-6" /></svg>
            </button>
          </div>

          <div ref="mindMapContainer" class="w-full h-full outline-none"></div>

          <div class="absolute bottom-4 left-4 flex gap-2 z-10">
            <button @click="handleZoomIn" class="p-2 bg-white rounded-lg border border-gray-200 shadow-sm hover:bg-indigo-50 text-gray-600 transition-colors">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
            </button>
            <button @click="handleZoomOut" class="p-2 bg-white rounded-lg border border-gray-200 shadow-sm hover:bg-indigo-50 text-gray-600 transition-colors">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" /></svg>
            </button>
            <button @click="handleFitView" class="p-2 bg-white rounded-lg border border-gray-200 shadow-sm hover:bg-indigo-50 text-gray-600 transition-colors">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4h16v16H4V4z" /></svg>
            </button>
          </div>
        </div>

        <div class="flex-[1] min-w-[320px] max-w-[400px] flex flex-col gap-4">
          <div class="flex-1 bg-white/60 border border-gray-200/60 rounded-2xl p-6 shadow-sm flex flex-col overflow-hidden">
            
            <div v-if="activeNodeId" class="h-full flex flex-col">
              <div class="shrink-0 mb-6 flex justify-between items-end">
                <div>
                  <span class="px-2 py-1 bg-indigo-500 text-white text-[10px] font-bold rounded uppercase tracking-wider">Node Editor</span>
                  <h3 class="text-xl font-bold text-gray-800 mt-2">编辑知识点</h3>
                </div>
                <button 
                  @click="saveCurrentNode" 
                  class="px-5 py-2 bg-indigo-600 text-white text-sm font-bold rounded-lg shadow-md hover:bg-indigo-700 transition-colors flex items-center gap-1"
                >
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                  确定
                </button>
              </div>
              
              <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 mb-4 space-y-6">
                <div class="space-y-2">
                  <label class="text-xs font-bold text-gray-400 uppercase tracking-widest">节点名称 (显示文本)</label>
                  <input 
                    v-model="formData.text" 
                    @blur="saveCurrentNode"
                    @keydown.enter.prevent="saveCurrentNode"
                    type="text" 
                    placeholder="请输入节点标题..."
                    class="w-full px-4 py-3 bg-white border border-gray-100 rounded-xl focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 outline-none transition-all text-sm font-medium"
                  />
                </div>
                
                <div class="space-y-2">
                  <label class="text-xs font-bold text-gray-400 uppercase tracking-widest">知识点详细内容</label>
                  <textarea 
                    v-model="formData.note"
                    @blur="saveCurrentNode"
                    rows="8"
                    placeholder="请输入该知识点的详细解析，保存后将会在节点右上角生成文档Tag..."
                    class="w-full px-4 py-3 bg-white border border-gray-100 rounded-xl focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 outline-none transition-all text-sm leading-relaxed custom-scrollbar resize-none"
                  ></textarea>
                </div>
              </div>
            </div>

            <div v-else class="h-full flex flex-col items-center justify-center text-center">
              <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mb-4 text-gray-300">
                <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122" /></svg>
              </div>
              <p class="text-gray-400 text-sm italic">点击画布中的节点<br/>开始完善知识点内容</p>
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

const minRequiredNodes = 8
const currentNodeCount = ref(1)
const mindMapContainer = ref(null)
let mindMapInstance = null

// 视图层双向绑定数据
const activeNodeId = ref(null)
const formData = ref({ text: '', note: '' })

// 核心修复：状态机互斥锁，防止程序保存时触发节点重绘导致的死循环
let isInternalStateUpdate = false

const initialData = {
  data: {
    text: 'Python 数组实训成果',
    note: '我的实训总结'
  },
  children: []
}

onMounted(() => {
  mindMapInstance = new MindMap({
    el: mindMapContainer.value,
    direction: 'right',
    theme: 'fresh-purple',
    data: initialData,
    fit: true,
    mouseScaleBehavior: 'zoom',
    readonly: false,
    themeConfig: {
      // 强制设定 shape 为 'rectangle'，彻底解决 3 级节点无边框问题
      root: { shape: 'rectangle', fillColor: '#6366f1', color: '#ffffff', borderColor: '#4f46e5', borderWidth: 2 },
      second: { shape: 'rectangle', fillColor: '#ffffff', borderColor: '#549688', color: '#334155', borderWidth: 2 },
      node: { shape: 'rectangle', fillColor: '#ffffff', borderColor: '#549688', color: '#334155', borderWidth: 1 },
      // 配置 note 标签显示 - 使用简单配置
      note: {
        show: true
      }
    }
  })

  // 监听画布节点主动切换
  mindMapInstance.on('node_active', (node, activeNodeList) => {
    // 切换节点前先保存当前编辑的内容
    if (activeNodeId.value && activeNodeList.length > 0) {
      const newActiveId = activeNodeList[0].uid
      if (activeNodeId.value !== newActiveId) {
        saveCurrentNode()
      }
    }

    if (activeNodeList.length > 0) {
      const currentActive = activeNodeList[0]
      activeNodeId.value = currentActive.uid
      formData.value.text = currentActive.nodeData.data.text || ''
      formData.value.note = currentActive.nodeData.data.note || ''
    } else {
      // 点击画布空白处时，先保存再关闭侧边栏
      saveCurrentNode()
      activeNodeId.value = null
    }
  })

  mindMapInstance.on('data_change', (data) => {
    currentNodeCount.value = countNodes(data)
  })
})

onBeforeUnmount(() => {
  if (mindMapInstance) mindMapInstance.destroy()
})

// 画布基础指令
const execCommand = (cmd) => {
  if (!mindMapInstance) return
  
  // 对于需要选中节点的命令，检查是否有选中的节点
  const activeNodes = mindMapInstance.renderer.activeNodeList
  if ((cmd === 'INSERT_CHILD_NODE' || cmd === 'INSERT_NODE') && activeNodes.length === 0) {
    // 如果没有选中节点，自动选中根节点
    const rootNode = mindMapInstance.renderer.rootNode
    if (rootNode) {
      mindMapInstance.execCommand('CLEAR_ACTIVE_NODE')
      mindMapInstance.renderer.addActiveNode(rootNode)
    }
  }
  
  mindMapInstance.execCommand(cmd)
}

// 保存当前节点内容
const saveCurrentNode = () => {
  if (!mindMapInstance || !activeNodeId.value) return
  
  const targetNode = mindMapInstance.renderer.findNodeByUid(activeNodeId.value)
  if (!targetNode) return

  // Diff 检查：防止无意义的覆盖
  const oldText = targetNode.nodeData.data.text || ''
  const oldNote = targetNode.nodeData.data.note || ''
  
  // 只有当内容真正改变时才保存
  if (oldText !== formData.value.text || oldNote !== formData.value.note) {
    // 直接修改节点数据
    targetNode.nodeData.data.text = formData.value.text
    targetNode.nodeData.data.note = formData.value.note
    
    // 触发数据变更事件，确保视图更新
    if (mindMapInstance.renderer) {
      mindMapInstance.renderer.render()
    }
  }
}

// 切换节点前保存当前内容
const saveBeforeSwitch = () => {
  if (activeNodeId.value) {
    saveCurrentNode()
  }
}

const handleZoomIn = () => mindMapInstance?.view.enlarge()
const handleZoomOut = () => mindMapInstance?.view.narrow()
const handleFitView = () => mindMapInstance?.view.fit()

const isReadyToSubmit = computed(() => currentNodeCount.value >= minRequiredNodes)

const handleSave = () => alert('草稿暂存成功')
const handleSubmit = () => alert('实训成果提交成功！')

function countNodes(node) {
  let count = 1
  if (node.children && node.children.length > 0) {
    node.children.forEach(child => { count += countNodes(child) })
  }
  return count
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

.toolbar-btn {
  @apply flex flex-col items-center justify-center w-14 h-14 rounded-xl text-gray-500 hover:bg-indigo-50 hover:text-indigo-600 transition-all;
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
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

:deep(.smm-canvas) {
  outline: none !important;
}

:deep(.smm-node.active) {
  outline: 2px solid #6366f1 !important;
  outline-offset: 2px;
}
</style>