<template>
  <div class="training-create-wrapper">
    <aside class="material-sidebar">
      <h1 class="panel-main-title">流程组件仓库</h1>
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
    </aside>

    <main class="canvas-container" @drop="onDrop" @dragover.prevent>
      <div class="canvas-top-toolbar">
        <button class="glass-btn" :disabled="!canUndo" @click="undo" title="撤回">↩ 撤回</button>
        <button class="glass-btn" :disabled="!canRedo" @click="redo" title="恢复">↪ 恢复</button>
        <div class="divider"></div>
        <button class="primary-action-btn" @click="handleSave">🚀 发布实训编排</button>
      </div>

      <VueFlow
        v-model:nodes="store.nodes"
        v-model:edges="store.edges"
        :node-types="nodeTypes"
        :default-edge-options="edgeOptions"
        @connect="onConnect"
        @pane-ready="onPaneReady"
        @node-drag-stop="recordHistory"
      >
        <Background pattern-color="#CBD5E1" :gap="24" :size="1.5" />
        
        <Controls position="bottom-left" class="minimal-controls" />
      </VueFlow>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, markRaw, computed } from 'vue'
import { VueFlow, useVueFlow, MarkerType } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { useTrainingStore } from '@/stores/modules/training.store'
import { MATERIAL_CATEGORIES } from './data/node-templates'
import StandardNode from './components/StandardNode.vue'
const store = useTrainingStore()
const { addEdges, toObject } = useVueFlow()
const nodeTypes: Record<string, any> = {
  standard: markRaw(StandardNode)
}

// --- 连线方向性配置：带有箭头的平滑曲线 ---
const edgeOptions = {
  type: 'smoothstep', 
  animated: true,
  style: { strokeWidth: 2, stroke: '#818CF8' },
  markerEnd: {
    type: MarkerType.ArrowClosed,
    width: 20,
    height: 20,
    color: '#818CF8',
  },
}

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

// --- 左侧抽屉逻辑 ---
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
  const { left, top } = (event.currentTarget as HTMLElement).getBoundingClientRect()
  
  // 注入真实的初始配置数据，代替占位符
  const newNode = {
    id: `node_${Date.now()}`,
    type: 'standard', 
    position: { x: event.clientX - left, y: event.clientY - top },
    data: { 
      type: item.type, 
      label: item.label, 
      icon: item.icon,
      // 预设真实可用的业务参数
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
const handleSave = () => { alert('实训方案配置已生成') }


</script>

<style scoped>
/* 15px 基础字号规范 */
.training-create-wrapper {
  display: flex;
  height: 100vh;
  width: 100vw;
  background: #F8FAFC;
  font-size: 15px;
  color: #334155;
  overflow: hidden;
}

.panel-main-title {
  font-size: 18px;
  font-weight: 800;
  color: #0F172A;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.material-sidebar { 
  width: 320px; /* 加宽以适应复杂的节点名称 */
  background: #FFFFFF; 
  border-right: 1px solid #E2E8F0;
  padding: 24px 20px;
  z-index: 20;
  box-shadow: 4px 0 20px rgba(0,0,0,0.02);
  display: flex;
  flex-direction: column;
}


.category-container {
  flex: 1;
  overflow-y: auto;
  /* 为滚动条预留一点右侧内边距，避免紧贴边缘 */
  padding-right: 4px; 
}

/* --- 滚动条极致优化 --- */
.category-container::-webkit-scrollbar {
  width: 6px; /* 极窄设计，不抢视觉焦点 */
}

.category-container::-webkit-scrollbar-track {
  background: transparent; /* 轨道全透明 */
}

.category-container::-webkit-scrollbar-thumb {
  background: #E2E8F0; /* 默认浅色 */
  border-radius: 10px;
  transition: background 0.3s;
}

.category-container::-webkit-scrollbar-thumb:hover {
  background: #CBD5E1; /* 悬停时稍微加深，提供交互反馈 */
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

/* 高性能 CSS Grid 丝滑折叠动画 */
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
  margin-top: 4px; /* 防止被 overflow 切断阴影 */
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
  right: 24px; /* 移至右上角 */
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

/* 控件定位于左下角 */
:deep(.minimal-controls) {
  display: flex;
  background: rgba(255, 255, 255, 0.95);
  padding: 4px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.08);
  border: 1px solid #E2E8F0;
  left: 24px !important; 
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
</style>