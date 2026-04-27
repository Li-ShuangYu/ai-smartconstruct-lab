<template>
  <div class="training-create-wrapper">
    <aside class="material-sidebar" :class="{ 'is-collapsed': isSidebarCollapsed }">
      <div class="sidebar-inner">
        <h1 class="panel-main-title">实训流程组件仓库</h1>
        <div class="category-container">
          <div v-for="cat in MATERIAL_CATEGORIES" :key="cat.type" class="cat-group">
            <div class="cat-header" @click="toggleCategory(cat.type)">
              <span class="cat-label">{{ cat.label }}</span>
              <i class="pure-chevron" :class="{ 'is-rotated': expandedCats.includes(cat.type) }"></i>
            </div>
            
            <div class="drawer-animator" :class="{ 'is-open': expandedCats.includes(cat.type) }">
              <div class="node-list">
                <div 
                  v-for="item in cat.items" 
                  :key="item.type"
                  class="draggable-node"
                  draggable="true"
                  @dragstart="onDragStart($event, item)"
                >
                  <span class="node-icon">{{ item.icon }}</span>
                  <span class="node-label">{{ item.label }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="sidebar-toggle" @click="isSidebarCollapsed = !isSidebarCollapsed">
        {{ isSidebarCollapsed ? '▶' : '◀' }}
      </div>
    </aside>

    <main class="canvas-container" @drop="onDrop" @dragover.prevent>
      <div class="canvas-top-toolbar">
        <button class="glass-btn" :disabled="!canUndo" @click="undo" title="撤回">↩ 撤回</button>
        <button class="glass-btn" :disabled="!canRedo" @click="redo" title="恢复">↪ 恢复</button>
        <div class="divider"></div>
        <button class="secondary-action-btn" @click="handleDraft">💾 暂存草稿</button>
        <button class="primary-action-btn" @click="handleSave">🚀 发布实训</button>
      </div>

     <VueFlow
        v-model:nodes="store.nodes"
        v-model:edges="store.edges"
        :node-types="nodeTypes"
        :default-edge-options="edgeOptions"
        :is-valid-connection="checkValidConnection" @connect="onConnect"
        @pane-ready="onPaneReady"
        @node-drag-stop="recordHistory"
      >
        <Background pattern-color="#CBD5E1" :gap="24" :size="1.5" />
        <Controls position="bottom-right" class="minimal-controls" />
      </VueFlow>
    </main>

    <transition name="modal-fade">
      <div v-if="showSuccessModal" class="modal-overlay">
        <div class="modal-card">
          <div class="modal-icon-wrapper">
            <span class="modal-icon">✅</span>
          </div>
          <h3 class="modal-title">{{ successTitle }}</h3>
          <p class="modal-desc">{{ successMessage }}</p>
          <button class="primary-action-btn modal-btn" @click="confirmAndRedirect">
            返回实训列表 ({{ countdown }}s)
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, markRaw, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { VueFlow, useVueFlow, MarkerType } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { useTrainingStore } from '@/stores/modules/training.store'
import { MATERIAL_CATEGORIES } from './data/node-templates'
import StandardNode from './components/StandardNode.vue'

const store = useTrainingStore()
const router = useRouter() // 引入路由控制跳转

const { addEdges, toObject, screenToFlowCoordinate } = useVueFlow()

const nodeTypes: Record<string, any> = {
  standard: markRaw(StandardNode)
}

// 将原本的 edgeOptions 替换为以下代码
const edgeOptions = {
  type: 'default', // 'default' 在 VueFlow 中对应优雅的贝塞尔曲线
  animated: true,
  style: { strokeWidth: 2, stroke: '#818CF8' },
  markerEnd: {
    type: MarkerType.ArrowClosed,
    width: 20,
    height: 20,
    color: '#818CF8',
  },
}


// 新增连接校验函数：从物理上禁止异常连线
const checkValidConnection = (connection: any) => {
  // 1. 禁止节点连向自己 (自环)
  if (connection.source === connection.target) return false;
  
  // 2. 严格防止端点类型错乱 (Source 只能连 Target)
  // 因为我们在 StandardNode 里面已经加上了 id="source" 和 id="target"
  if (connection.sourceHandle === 'target' || connection.targetHandle === 'source') {
    return false;
  }

  return true;
}

// 侧边栏收起状态
const isSidebarCollapsed = ref(false)

// --- 撤回/恢复系统 ---
const historyStack = ref<string[]>([])
const redoStack = ref<string[]>([])
const canUndo = computed(() => historyStack.value.length > 0)
const canRedo = computed(() => redoStack.value.length > 0)

const recordHistory = () => {
  const state = JSON.stringify(toObject())
  historyStack.value.push(state)
  if (historyStack.value.length > 30) historyStack.value.shift()
  redoStack.value = []
}

const undo = () => {
  if (!canUndo.value) return
  redoStack.value.push(JSON.stringify(toObject()))
  const lastState = JSON.parse(historyStack.value.pop()!)
  store.nodes = lastState.nodes
  store.edges = lastState.edges
}

const redo = () => {
  if (!canRedo.value) return
  historyStack.value.push(JSON.stringify(toObject()))
  const nextState = JSON.parse(redoStack.value.pop()!)
  store.nodes = nextState.nodes
  store.edges = nextState.edges
}

// --- 交互逻辑 ---
const expandedCats = ref(MATERIAL_CATEGORIES.map(c => c.type))
const toggleCategory = (type: string) => {
  const index = expandedCats.value.indexOf(type)
  if (index > -1) expandedCats.value.splice(index, 1)
  else expandedCats.value.push(type)
}

const onDragStart = (event: DragEvent, item: any) => {
  if (event.dataTransfer) {
    event.dataTransfer.setData('application/vueflow', JSON.stringify(item))
    event.dataTransfer.effectAllowed = 'move'
  }
}

const onDrop = (event: DragEvent) => {
  const data = event.dataTransfer?.getData('application/vueflow')
  if (!data) return
  recordHistory()
  const item = JSON.parse(data)
  
  const position = screenToFlowCoordinate({
    x: event.clientX,
    y: event.clientY,
  })
  
  const newNode = {
    id: `node_${Date.now()}`,
    type: 'standard', 
    position: position, 
    data: { 
      type: item.type, 
      label: item.label, 
      icon: item.icon,
      modelSelect: 'qwen-max',
      temperature: 0.7,
      maxTokens: 2048,
      ...item.defaultData 
    }
  }
  store.nodes.push(newNode)
}

const onConnect = (params: any) => {
  recordHistory()
  addEdges({ ...params, animated: true })
}
const onPaneReady = (instance: any) => { store.flowInstance = instance }

// --- 弹窗与提交逻辑 ---
const showSuccessModal = ref(false)
const successTitle = ref('')
const successMessage = ref('')
const countdown = ref(3)
let timer: any = null

const triggerSuccess = (title: string, message: string) => {
  successTitle.value = title
  successMessage.value = message
  showSuccessModal.value = true
  countdown.value = 3
  
  // 开启倒计时自动跳转
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      confirmAndRedirect()
    }
  }, 1000)
}

const confirmAndRedirect = () => {
  if (timer) clearInterval(timer)
  showSuccessModal.value = false
  router.push('/teacher/training-publish?templateId=1')
}

// 暂存草稿触发
const handleDraft = () => {
  triggerSuccess('暂存成功', '您的实训编排草稿已安全保存至工作台。')
}

// 发布触发
const handleSave = () => {
  triggerSuccess('发布成功', '实训编排方案已正式发布，正在前往开始实训界面。。。')
}

// 组件卸载时清理定时器，防止内存泄漏
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.training-create-wrapper {
  display: flex;
  height: calc(100vh - 64px); 
  background: #F8FAFC;
  font-size: 15px;
  color: #334155;
  overflow: hidden; 
  box-sizing: border-box;
  position: relative; /* 为弹窗提供绝对定位的基准 */
}

.panel-main-title {
  font-size: 22px;
  font-weight: 800;
  color: #0F172A;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 侧边栏：加入平滑的宽度过渡动画 */
.material-sidebar { 
  width: 320px; 
  background: #FFFFFF; 
  border-right: 1px solid #E2E8F0;
  z-index: 20;
  box-shadow: 4px 0 20px rgba(0,0,0,0.02);
  display: flex;
  flex-direction: column;
  position: relative;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

/* 收起状态的侧边栏 */
.material-sidebar.is-collapsed {
  width: 0;
  border-right: none;
}

/* 侧边栏内部容器：保持内容固定，避免收起时文字换行扭曲 */
.sidebar-inner {
  width: 320px;
  height: 100%;
  padding: 24px 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  opacity: 1;
  transition: opacity 0.2s ease;
  overflow: hidden;
}

/* 侧边栏收起时，隐藏内部内容 */
.material-sidebar.is-collapsed .sidebar-inner {
  opacity: 0;
  pointer-events: none;
}

/* 侧边栏折叠手柄 */
.sidebar-toggle {
  position: absolute;
  right: -16px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 48px;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-left: none;
  border-radius: 0 6px 6px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #94A3B8;
  font-size: 10px;
  box-shadow: 2px 0 6px rgba(0,0,0,0.03);
  z-index: 21;
  transition: all 0.2s;
}

.sidebar-toggle:hover {
  color: #4F46E5;
  background: #F8FAFC;
}

.category-container {
  flex: 1;
  padding-right: 8px;
  overflow-y: auto;
}

/* 1. 极致细的品牌色滚动条 */
.category-container::-webkit-scrollbar {
  width: 4px;
}
.category-container::-webkit-scrollbar-track {
  background: transparent;
}
.category-container::-webkit-scrollbar-thumb {
  background: rgba(79, 70, 229, 0.3); /* 默认半透明品牌色 */
  border-radius: 4px;
  transition: background 0.2s ease;
}
.category-container::-webkit-scrollbar-thumb:hover {
  background: #4F46E5; /* 悬停时显示纯正的品牌色 */
}

.cat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 4px;
  cursor: pointer;
}
.cat-label { font-weight: 600; color: #475569; font-size: 15px; }

.pure-chevron {
  width: 8px;
  height: 8px;
  border-right: 2px solid #94A3B8;
  border-bottom: 2px solid #94A3B8;
  transform: rotate(-45deg);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.pure-chevron.is-rotated { transform: rotate(45deg); }

.drawer-animator {
  display: grid;
  grid-template-rows: 0fr;
  transition: grid-template-rows 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.drawer-animator.is-open {
  grid-template-rows: 1fr;
}
.node-list {
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 4px;
}

.draggable-node {
  padding: 14px 16px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  cursor: grab;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s;
  margin-top: 4px;
}
.draggable-node:hover {
  background: #FFFFFF;
  border-color: #818CF8;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.08);
  transform: translateY(-2px);
}

.canvas-container { flex: 1; position: relative; height: 100%; }

.canvas-top-toolbar {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 50;
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  padding: 8px 16px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  border: 1px solid #E2E8F0;
}

.divider { width: 1px; height: 20px; background: #E2E8F0; margin: 0 4px; }

.glass-btn {
  padding: 6px 12px;
  background: transparent;
  border: none;
  font-size: 15px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}
.glass-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.glass-btn:hover:not(:disabled) { background: #F1F5F9; color: #0F172A; }

.secondary-action-btn {
  padding: 8px 16px;
  background: #FFFFFF;
  color: #475569;
  border: 1px solid #CBD5E1;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.secondary-action-btn:hover {
  background: #F8FAFC;
  border-color: #94A3B8;
  color: #0F172A;
}

.primary-action-btn {
  padding: 8px 18px;
  background: #0F172A;
  color: #FFF;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.primary-action-btn:hover { background: #334155; }

/* 4. 修改控制器位置到右下角 */
:deep(.minimal-controls) {
  display: flex;
  background: rgba(255, 255, 255, 0.95);
  padding: 4px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
  border: 1px solid #E2E8F0;
  /* 移除之前的 left: 24px，改为 right: 24px */
  right: 24px !important; 
  bottom: 24px !important;
}
:deep(.vue-flow__controls-button) {
  border: none;
  background: transparent;
  width: 34px;
  height: 34px;
  border-radius: 6px;
}
:deep(.vue-flow__controls-button:hover) { background: #F1F5F9; }

/* 2. 弹窗：取消变暗遮罩，只有一点点模糊 */
.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: transparent; /* 完全透明，不再变暗！ */
  backdrop-filter: blur(2px); /* 极其轻微的一点点模糊 */
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-card {
  background: #FFFFFF;
  padding: 40px;
  border-radius: 20px;
  /* 增加了弹窗本身的阴影，使其在透明背景下更立体 */
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(0,0,0,0.05);
  text-align: center;
  max-width: 360px;
  width: 90%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.modal-icon-wrapper {
  width: 64px;
  height: 64px;
  background: #DCFCE7; 
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
}

.modal-icon {
  font-size: 28px;
}

.modal-title {
  font-size: 20px;
  font-weight: 800;
  color: #0F172A;
  margin: 0 0 12px 0;
}

.modal-desc {
  font-size: 15px;
  color: #64748B;
  margin: 0 0 32px 0;
  line-height: 1.5;
}

.modal-btn {
  width: 100%; 
  padding: 12px 0;
  border-radius: 10px;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}
</style>