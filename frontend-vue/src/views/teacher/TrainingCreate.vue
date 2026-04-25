<template>
  <div class="training-create-wrapper">
    <aside class="material-sidebar">
      <div v-for="cat in MATERIAL_CATEGORIES" :key="cat.type" class="cat-group">
        <h4 class="cat-title">{{ cat.label }}</h4>
        <div class="node-list">
          <div 
            v-for="item in cat.items" 
            :key="item.type"
            class="draggable-node"
            draggable="true"
            @dragstart="onDragStart($event, item)"
          >
            <span class="icon">{{ item.icon }}</span>
            <span class="label">{{ item.label }}</span>
          </div>
        </div>
      </div>
    </aside>

    <main class="canvas-container" @drop="onDrop" @dragover.prevent>
      <VueFlow
        v-model:nodes="store.nodes"
        v-model:edges="store.edges"
        :default-edge-options="{ type: 'default', animated: true, style: { strokeWidth: 2, stroke: '#818CF8' } }"
        :node-types="nodeTypes"
        @connect="onConnect"
        @pane-ready="onPaneReady"
        @node-click="onNodeClick"
      >
        <Background pattern-color="#D1D5DB" :gap="24" size="1.5" />
        <Controls position="bottom-right" />
      </VueFlow>
    </main>

    <aside class="property-panel">
      <PropertyEditor />
    </aside>
  </div>
</template>

<script setup lang="ts">
import { markRaw } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { useTrainingStore } from '@/stores/modules/training.store'
import { MATERIAL_CATEGORIES } from './data/node-templates'
import PropertyEditor from './components/PropertyEditor.vue'
import StandardNode from './components/StandardNode.vue'

const store = useTrainingStore()
const { addEdges } = useVueFlow()

const nodeTypes = {
  standard: markRaw(StandardNode)
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

  const item = JSON.parse(data)
  const { left, top } = (event.target as HTMLElement).getBoundingClientRect()
  
  const newNode = {
    id: `node_${Date.now()}`,
    type: 'standard', 
    position: { x: event.clientX - left, y: event.clientY - top },
    data: { ...item.defaultData, type: item.type, label: item.label, icon: item.icon }
  }

  store.nodes.push(newNode)
  store.nodeConfigMap[newNode.id] = { ...item.defaultData }
}

// 自由优雅的连线逻辑：移除 type: 'step'
const onConnect = (params: any) => {
  addEdges({ ...params, animated: true })
}

const onNodeClick = ({ node }: any) => {
  store.selectedNodeId = node.id
}

const onPaneReady = (instance: any) => {
  store.flowInstance = instance
}
</script>

<style scoped>
/* 沉浸式全屏布局，彻底摆脱 128px 的高度限制 */
.training-create-wrapper {
  display: flex;
  height: 100vh;
  width: 100vw;
  background: #F9FAFB;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* 侧边栏优雅化 */
.material-sidebar { 
  width: 260px; 
  border-right: 1px solid rgba(229, 231, 235, 0.5); 
  padding: 24px 16px; 
  background: rgba(255, 255, 255, 0.7); 
  backdrop-filter: blur(12px);
  z-index: 10; 
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.cat-title { 
  font-size: 12px; 
  color: #6B7280; 
  margin: 0 0 12px 4px; 
  text-transform: uppercase; 
  letter-spacing: 0.5px;
  font-weight: 600;
}

.node-list { 
  display: flex; 
  flex-direction: column; 
  gap: 10px; 
}

/* 拖拽项呼吸感交互 */
.draggable-node { 
  padding: 12px 16px; 
  background: #ffffff; 
  border: 1px solid #F3F4F6; 
  border-radius: 12px; 
  font-size: 13px; 
  cursor: grab; 
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); 
  display: flex; 
  align-items: center; 
  gap: 12px; 
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
  color: #374151;
  font-weight: 500;
}

.draggable-node:hover { 
  border-color: #C7D2FE; 
  color: #4F46E5; 
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(79, 70, 229, 0.08);
}

.draggable-node .icon {
  font-size: 16px;
}

.canvas-container { 
  flex: 1; 
  background: transparent; 
  position: relative; 
}

/* 右侧面板优雅化 */
.property-panel { 
  width: 340px; 
  border-left: 1px solid rgba(229, 231, 235, 0.5); 
  background: rgba(255, 255, 255, 0.8); 
  backdrop-filter: blur(12px);
  padding: 24px; 
  z-index: 10;
  box-shadow: -2px 0 12px rgba(0, 0, 0, 0.02);
}
</style>