<template>
  <div
    class="orchestration-canvas"
    @dragover.prevent="onCanvasDragOver"
    @drop="onCanvasDrop"
    @dragleave="onCanvasDragLeave"
    @click="handleCanvasClick"
  >
    <div v-if="nodes.length === 0" class="canvas-empty-state">
      <div class="empty-icon">📋</div>
      <p class="empty-text">从左侧拖拽节点到此处开始编排</p>
      <p class="empty-hint">当前阶段：{{ activePhase?.phase_name ?? '未选择' }}</p>
    </div>

    <div v-else class="canvas-flow">
      <div class="flow-list">
        <div
          v-for="(node, index) in nodes"
          :key="node.node_id"
          class="flow-item-wrapper"
          @dragover.prevent="onNodeDragOver($event, index)"
        >
          <div
            class="flow-node"
            :class="{
              'is-active': selectedNodeId === node.node_id,
              'is-dragging': draggingNodeId === node.node_id,
              'has-validation-warning': nodeHasValidationWarning(node)
            }"
            draggable="true"
            @dragstart="onNodeDragStart($event, node)"
            @dragend="onNodeDragEnd"
            @click.stop="selectNode(node.node_id)"
            @mouseenter="onNodeMouseEnter($event, node)"
            @mouseleave="onNodeMouseLeave"
          >
            <div class="flow-node-index">{{ index + 1 }}</div>
            <div class="flow-node-content">
              <div class="flow-node-title">
                <span class="flow-node-icon">{{ getNodeIcon(node.node_type) }}</span>
                {{ node.node_name }}
              </div>
              <div class="flow-node-meta">
                <span class="flow-node-type">{{ node.node_type }}</span>
                <span v-if="node.is_required" class="flow-node-required">必须</span>
              </div>
            </div>
            <div class="flow-node-actions">
              <!-- Step 5: Validation warning icon -->
              <span
                v-if="nodeHasValidationWarning(node)"
                class="node-warning-icon"
                title="存在必填字段未填写"
              >⚠️</span>
              <button
                class="node-action-btn"
                title="上移"
                :disabled="index === 0"
                @click.stop="moveNode(index, -1)"
              >↑</button>
              <button
                class="node-action-btn"
                title="下移"
                :disabled="index === nodes.length - 1"
                @click.stop="moveNode(index, 1)"
              >↓</button>
              <button
                class="node-action-btn node-action-btn--danger"
                title="删除"
                @click.stop="deleteNode(node.node_id)"
              >✕</button>
            </div>
          </div>

          <!-- Connector -->
          <div
            v-if="index < nodes.length - 1"
            class="flow-connector"
            :class="{ 'is-drop-target': dropIndex === index }"
          >
            <template v-if="dropIndex === index">
              <div class="drop-placeholder">✨ 释放到此处</div>
            </template>
            <template v-else>
              <div class="connector-line"></div>
              <div class="connector-arrow">▼</div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- Step 6: Node hover tooltip -->
    <NodeHoverTooltip
      :node-type="hoverNodeType"
      :is-hovering="isNodeHovering"
      :position="hoverPosition"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useOrchestrationStore } from '@/stores/modules/orchestration.store'
import { MATERIAL_CATEGORIES } from '@/views/teacher/data/node-templates'
import { getRequiredFieldsForNodeType } from './nodeFieldDefinitions'
import NodeHoverTooltip from './NodeHoverTooltip.vue'
import type { OrchestrationNode, NodeConfig6D } from '@/services/types/orchestration'

const store = useOrchestrationStore()

const activePhase = computed(() => store.activePhase)
const nodes = computed(() => store.activePhaseNodes)
const selectedNodeId = computed(() => store.selectedNodeId)

const draggingNodeId = ref('')
const dropIndex = ref(-1)

// Step 6: Hover tooltip state
const isNodeHovering = ref(false)
const hoverNodeType = ref('')
const hoverPosition = ref({ x: 0, y: 0 })

function onNodeMouseEnter(event: MouseEvent, node: OrchestrationNode) {
  isNodeHovering.value = true
  hoverNodeType.value = node.node_type
  const rect = (event.currentTarget as HTMLElement).getBoundingClientRect()
  const canvasRect = (event.currentTarget as HTMLElement).closest('.orchestration-canvas')?.getBoundingClientRect()
  if (canvasRect) {
    hoverPosition.value = {
      x: rect.left - canvasRect.left + rect.width / 2,
      y: rect.top - canvasRect.top
    }
  }
}

function onNodeMouseLeave() {
  isNodeHovering.value = false
}

/** Step 5: Check if a node has validation warnings (missing required fields) */
function nodeHasValidationWarning(node: OrchestrationNode): boolean {
  const requiredFields = getRequiredFieldsForNodeType(node.node_type)
  for (const fieldDef of requiredFields) {
    const dimension = node.config[fieldDef.dimension as keyof typeof node.config] as Record<string, unknown> | undefined
    if (!dimension) return true
    const value = dimension[fieldDef.field]
    if (value === undefined || value === null || value === '') return true
  }
  return false
}

function getNodeIcon(nodeType: string): string {
  for (const cat of MATERIAL_CATEGORIES) {
    const item = cat.items.find(i => i.type === nodeType)
    if (item) return item.icon
  }
  return '📦'
}

function selectNode(nodeId: string) {
  store.selectNode(nodeId)
}

function handleCanvasClick() {
  store.clearSelection()
}

function deleteNode(nodeId: string) {
  store.removeNode(nodeId)
}

function moveNode(index: number, direction: number) {
  const phase = store.activePhase
  if (!phase) return
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= phase.nodes.length) return
  const nodesCopy = [...phase.nodes]
  const moved = nodesCopy.splice(index, 1)[0]
  if (!moved) return
  nodesCopy.splice(newIndex, 0, moved)
  phase.nodes = nodesCopy
}

// --- Drag from palette onto canvas ---
function onCanvasDragOver(event: DragEvent) {
  if (event.dataTransfer?.types.includes('application/orchestration-node')) {
    event.dataTransfer.dropEffect = 'copy'
  }
}

function onCanvasDragLeave(event: DragEvent) {
  if (!(event.currentTarget as HTMLElement).contains(event.relatedTarget as Node)) {
    dropIndex.value = -1
  }
}

function onCanvasDrop(event: DragEvent) {
  const data = event.dataTransfer?.getData('application/orchestration-node')
  if (data) {
    const { nodeType, nodeLabel } = JSON.parse(data)
    const newNode = createDefaultNode(nodeType, nodeLabel, { x: 0, y: 0 })
    store.addNode(newNode)
  }
  dropIndex.value = -1
}

function onNodeDragOver(event: DragEvent, index: number) {
  if (draggingNodeId.value) {
    dropIndex.value = index
  }
}

function onNodeDragStart(event: DragEvent, node: OrchestrationNode) {
  draggingNodeId.value = node.node_id
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('application/orchestration-canvas-node', node.node_id)
  }
}

function onNodeDragEnd() {
  // Handle reorder if dropIndex is set
  if (dropIndex.value >= 0 && draggingNodeId.value) {
    const phase = store.activePhase
    if (phase) {
      const fromIndex = phase.nodes.findIndex(n => n.node_id === draggingNodeId.value)
      if (fromIndex >= 0 && fromIndex !== dropIndex.value) {
        const nodesCopy = [...phase.nodes]
        const moved = nodesCopy.splice(fromIndex, 1)[0]
        if (moved) {
          const targetIndex = fromIndex < dropIndex.value ? dropIndex.value : dropIndex.value + 1
          nodesCopy.splice(targetIndex, 0, moved)
          phase.nodes = nodesCopy
        }
      }
    }
  }
  draggingNodeId.value = ''
  dropIndex.value = -1
}

function createDefaultNode(
  nodeType: string,
  nodeName: string,
  position: { x: number; y: number }
): OrchestrationNode {
  const config: NodeConfig6D = {
    display: {},
    db_mapping: {},
    ai_processing: {},
    orchestration_settings: {},
    data_collection: {},
    evaluation: {}
  }

  return {
    node_id: `node_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    node_type: nodeType,
    node_name: nodeName,
    config,
    position,
    is_required: true,
    ai_overrides: {}
  }
}
</script>

<style scoped>
.orchestration-canvas {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 0;
  position: relative;
}

.canvas-empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.empty-icon {
  font-size: 48px;
  opacity: 0.5;
}

.empty-text {
  font-size: 15px;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
  font-weight: 500;
}

.empty-hint {
  font-size: 13px;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
  opacity: 0.7;
}

.canvas-flow {
  width: 100%;
  max-width: 520px;
}

.flow-list {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.flow-item-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.flow-node {
  width: 100%;
  background: var(--vt-c-white, #ffffff);
  border: 2px solid var(--color-border, rgba(60, 60, 60, 0.12));
  border-radius: 10px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.flow-node:hover {
  border-color: var(--color-border-hover, rgba(60, 60, 60, 0.29));
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.flow-node.is-active {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.flow-node.is-dragging {
  opacity: 0.4;
}

.flow-node-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--color-background-mute, #f2f2f2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
  flex-shrink: 0;
}

.flow-node.is-active .flow-node-index {
  background: #eef2ff;
  color: #4f46e5;
}

.flow-node-content {
  flex: 1;
  min-width: 0;
}

.flow-node-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text, #2c3e50);
  display: flex;
  align-items: center;
  gap: 6px;
}

.flow-node-icon {
  font-size: 16px;
}

.flow-node-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.flow-node-type {
  font-size: 11px;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
}

.flow-node-required {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
  background: #dbeafe;
  color: #1d4ed8;
  font-weight: 500;
}

.flow-node-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.flow-node:hover .flow-node-actions {
  opacity: 1;
}

.node-action-btn {
  width: 24px;
  height: 24px;
  border: 1px solid var(--color-border, rgba(60, 60, 60, 0.12));
  border-radius: 4px;
  background: var(--vt-c-white, #ffffff);
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
  transition: all 0.15s;
}

.node-action-btn:hover:not(:disabled) {
  border-color: #4f46e5;
  color: #4f46e5;
}

.node-action-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.node-action-btn--danger:hover:not(:disabled) {
  border-color: #ef4444;
  color: #ef4444;
  background: #fef2f2;
}

/* Connector */
.flow-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4px 0;
  min-height: 32px;
  justify-content: center;
}

.connector-line {
  width: 2px;
  height: 16px;
  background: var(--color-border, rgba(60, 60, 60, 0.12));
}

.connector-arrow {
  font-size: 10px;
  color: var(--color-border-hover, rgba(60, 60, 60, 0.29));
}

.flow-connector.is-drop-target {
  padding: 8px 0;
}

.drop-placeholder {
  padding: 8px 16px;
  border: 2px dashed #4f46e5;
  border-radius: 8px;
  background: #eef2ff;
  color: #4f46e5;
  font-size: 13px;
  font-weight: 500;
}

/* Step 5: Validation warning styles */
.flow-node.has-validation-warning {
  border-color: #f59e0b;
}

.flow-node.has-validation-warning:not(.is-active) {
  box-shadow: 0 0 0 2px rgba(245, 158, 11, 0.15);
}

.node-warning-icon {
  font-size: 14px;
  margin-right: 4px;
  animation: pulse-warning 2s infinite;
}

@keyframes pulse-warning {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
