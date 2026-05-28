<template>
  <div class="mindmap-editor">
    <!-- Toolbar -->
    <header class="mindmap-editor__toolbar">
      <div class="mindmap-editor__toolbar-left">
        <h2 class="mindmap-editor__title">思维导图绘制</h2>
      </div>
      <div class="mindmap-editor__toolbar-actions">
        <button class="mindmap-editor__tool-btn" @click="addNode">
          ➕ 添加节点
        </button>
        <button class="mindmap-editor__tool-btn" @click="deleteSelected">
          🗑️ 删除
        </button>
        <button class="mindmap-editor__tool-btn mindmap-editor__tool-btn--submit" @click="handleSubmit">
          📤 提交评估
        </button>
      </div>
    </header>

    <!-- Canvas Area -->
    <div class="mindmap-editor__canvas-wrapper">
      <div class="mindmap-editor__canvas" @click="handleCanvasClick">
        <!-- Rendered nodes -->
        <div
          v-for="node in mapNodes"
          :key="node.id"
          class="mindmap-editor__node"
          :class="{
            'mindmap-editor__node--selected': selectedNodeId === node.id,
            'mindmap-editor__node--root': node.isRoot
          }"
          :style="{ left: `${node.x}px`, top: `${node.y}px` }"
          draggable="true"
          @mousedown.stop="startDrag(node.id, $event)"
          @click.stop="selectNode(node.id)"
        >
          <input
            v-if="editingNodeId === node.id"
            class="mindmap-editor__node-input"
            :value="node.label"
            @blur="finishEdit"
            @keydown.enter="finishEdit"
            @input="updateNodeLabel(node.id, ($event.target as HTMLInputElement).value)"
          />
          <span v-else class="mindmap-editor__node-label" @dblclick="startEdit(node.id)">
            {{ node.label }}
          </span>
        </div>

        <!-- Connection lines (SVG) -->
        <svg class="mindmap-editor__connections">
          <line
            v-for="edge in edges"
            :key="`${edge.from}-${edge.to}`"
            :x1="getNodeCenter(edge.from).x"
            :y1="getNodeCenter(edge.from).y"
            :x2="getNodeCenter(edge.to).x"
            :y2="getNodeCenter(edge.to).y"
            class="mindmap-editor__edge"
          />
        </svg>
      </div>
    </div>

    <!-- AI Evaluation Result -->
    <section v-if="aiEvaluation" class="mindmap-editor__evaluation">
      <h3 class="mindmap-editor__eval-title">
        <span class="mindmap-editor__ai-badge">AI</span>
        结构评估结果
      </h3>
      <div class="mindmap-editor__eval-content">
        <div class="mindmap-editor__eval-score">
          <span class="mindmap-editor__eval-score-value">{{ aiEvaluation.score }}</span>
          <span class="mindmap-editor__eval-score-label">/100</span>
        </div>
        <div class="mindmap-editor__eval-dimensions">
          <div
            v-for="dim in aiEvaluation.dimensions"
            :key="dim.name"
            class="mindmap-editor__eval-dim"
          >
            <span class="mindmap-editor__eval-dim-name">{{ dim.name }}</span>
            <span class="mindmap-editor__eval-dim-score">{{ dim.score }}分</span>
          </div>
        </div>
        <p class="mindmap-editor__eval-comment">{{ aiEvaluation.comment }}</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface MapNode {
  id: string
  label: string
  x: number
  y: number
  isRoot: boolean
  parentId: string | null
}

interface MapEdge {
  from: string
  to: string
}

interface EvalDimension {
  name: string
  score: number
}

interface AiEvaluation {
  score: number
  dimensions: EvalDimension[]
  comment: string
}

interface MindMapEditorConfig {
  display?: {
    topic?: string
  }
  ai_processing?: {
    enable_ai_structure_eval?: boolean
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: MindMapEditorConfig
}>()

const emit = defineEmits<{
  complete: []
}>()

/** Canvas nodes */
const mapNodes = ref<MapNode[]>([
  { id: 'root', label: '中心主题', x: 300, y: 200, isRoot: true, parentId: null },
  { id: 'n1', label: '分支一', x: 150, y: 100, isRoot: false, parentId: 'root' },
  { id: 'n2', label: '分支二', x: 450, y: 100, isRoot: false, parentId: 'root' },
  { id: 'n3', label: '分支三', x: 150, y: 300, isRoot: false, parentId: 'root' },
  { id: 'n4', label: '子节点', x: 50, y: 50, isRoot: false, parentId: 'n1' }
])

/** Edges derived from parent relationships */
const edges = computed<MapEdge[]>(() =>
  mapNodes.value
    .filter(n => n.parentId !== null)
    .map(n => ({ from: n.parentId!, to: n.id }))
)

const selectedNodeId = ref<string | null>(null)
const editingNodeId = ref<string | null>(null)
const dragNodeId = ref<string | null>(null)
const dragOffset = ref<{ x: number; y: number }>({ x: 0, y: 0 })

/** Placeholder AI evaluation */
const aiEvaluation = ref<AiEvaluation | null>({
  score: 75,
  dimensions: [
    { name: '结构完整性', score: 80 },
    { name: '层次合理性', score: 70 },
    { name: '覆盖度', score: 72 },
    { name: '逻辑关联', score: 78 }
  ],
  comment: '思维导图结构基本合理，建议增加更多子节点以提高知识覆盖度，部分分支层次可进一步细化。'
})

function selectNode(nodeId: string) {
  selectedNodeId.value = nodeId
}

function startEdit(nodeId: string) {
  editingNodeId.value = nodeId
}

function finishEdit() {
  editingNodeId.value = null
}

function updateNodeLabel(nodeId: string, label: string) {
  const node = mapNodes.value.find(n => n.id === nodeId)
  if (node) {
    node.label = label
  }
}

function addNode() {
  const parentId = selectedNodeId.value ?? 'root'
  const parent = mapNodes.value.find(n => n.id === parentId)
  const newId = `n_${Date.now()}`
  mapNodes.value.push({
    id: newId,
    label: '新节点',
    x: (parent?.x ?? 300) + 100 + Math.random() * 50,
    y: (parent?.y ?? 200) + 80 + Math.random() * 50,
    isRoot: false,
    parentId
  })
  selectedNodeId.value = newId
  editingNodeId.value = newId
}

function deleteSelected() {
  if (!selectedNodeId.value || selectedNodeId.value === 'root') return
  mapNodes.value = mapNodes.value.filter(n => n.id !== selectedNodeId.value && n.parentId !== selectedNodeId.value)
  selectedNodeId.value = null
}

function startDrag(nodeId: string, event: MouseEvent) {
  dragNodeId.value = nodeId
  const node = mapNodes.value.find(n => n.id === nodeId)
  if (node) {
    dragOffset.value = { x: event.clientX - node.x, y: event.clientY - node.y }
  }
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

function onDrag(event: MouseEvent) {
  if (!dragNodeId.value) return
  const node = mapNodes.value.find(n => n.id === dragNodeId.value)
  if (node) {
    node.x = event.clientX - dragOffset.value.x
    node.y = event.clientY - dragOffset.value.y
  }
}

function stopDrag() {
  dragNodeId.value = null
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

function handleCanvasClick() {
  selectedNodeId.value = null
}

function getNodeCenter(nodeId: string): { x: number; y: number } {
  const node = mapNodes.value.find(n => n.id === nodeId)
  if (!node) return { x: 0, y: 0 }
  return { x: node.x + 50, y: node.y + 16 }
}

function handleSubmit() {
  emit('complete')
}
</script>

<style scoped>
.mindmap-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.mindmap-editor__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem var(--spacing-md, 1rem);
  background: var(--color-white, #ffffff);
  border-bottom: 1px solid var(--color-gray-200, #e2e8f0);
}

.mindmap-editor__title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
}

.mindmap-editor__toolbar-actions {
  display: flex;
  gap: 0.5rem;
}

.mindmap-editor__tool-btn {
  padding: 0.375rem 0.75rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-gray-600, #475569);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-sm, 0.25rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.mindmap-editor__tool-btn:hover {
  background: var(--color-gray-50, #f8fafc);
}

.mindmap-editor__tool-btn--submit {
  background: var(--color-primary-500, #6366f1);
  color: white;
  border-color: var(--color-primary-500, #6366f1);
}

.mindmap-editor__tool-btn--submit:hover {
  background: var(--color-primary-600, #4f46e5);
}

.mindmap-editor__canvas-wrapper {
  flex: 1;
  min-height: 0;
  overflow: auto;
  background: var(--color-gray-50, #f8fafc);
}

.mindmap-editor__canvas {
  position: relative;
  width: 800px;
  height: 500px;
  min-width: 100%;
  min-height: 100%;
}

.mindmap-editor__connections {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.mindmap-editor__edge {
  stroke: var(--color-gray-300, #cbd5e1);
  stroke-width: 2;
}

.mindmap-editor__node {
  position: absolute;
  padding: 0.375rem 0.75rem;
  background: var(--color-white, #ffffff);
  border: 2px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  cursor: grab;
  user-select: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
  min-width: 80px;
  text-align: center;
}

.mindmap-editor__node:hover {
  border-color: var(--color-primary-300, #a5b4fc);
}

.mindmap-editor__node--selected {
  border-color: var(--color-primary-500, #6366f1);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

.mindmap-editor__node--root {
  background: var(--color-primary-50, #eef2ff);
  border-color: var(--color-primary-300, #a5b4fc);
  font-weight: 700;
}

.mindmap-editor__node-label {
  font-size: 0.8125rem;
  color: var(--color-gray-800, #1e293b);
}

.mindmap-editor__node-input {
  font-size: 0.8125rem;
  border: none;
  outline: none;
  background: transparent;
  text-align: center;
  width: 100%;
  color: var(--color-gray-800, #1e293b);
}

.mindmap-editor__evaluation {
  padding: var(--spacing-md, 1rem) var(--spacing-lg, 1.5rem);
  background: var(--color-white, #ffffff);
  border-top: 1px solid var(--color-gray-200, #e2e8f0);
  max-height: 200px;
  overflow-y: auto;
}

.mindmap-editor__eval-title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.mindmap-editor__ai-badge {
  font-size: 0.5625rem;
  font-weight: 700;
  padding: 0.0625rem 0.25rem;
  border-radius: 2px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
}

.mindmap-editor__eval-content {
  display: flex;
  gap: var(--spacing-md, 1rem);
  align-items: flex-start;
}

.mindmap-editor__eval-score {
  display: flex;
  align-items: baseline;
}

.mindmap-editor__eval-score-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.mindmap-editor__eval-score-label {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
}

.mindmap-editor__eval-dimensions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.mindmap-editor__eval-dim {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.5rem;
  background: var(--color-gray-50, #f8fafc);
  border-radius: var(--radius-sm, 0.25rem);
  font-size: 0.75rem;
}

.mindmap-editor__eval-dim-name {
  color: var(--color-gray-600, #475569);
}

.mindmap-editor__eval-dim-score {
  font-weight: 600;
  color: var(--color-primary-600, #4f46e5);
}

.mindmap-editor__eval-comment {
  font-size: 0.8125rem;
  color: var(--color-gray-500, #64748b);
  line-height: 1.5;
  flex: 1;
}
</style>
