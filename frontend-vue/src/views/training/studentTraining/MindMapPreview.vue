<template>
  <div class="mindmap-preview">
    <!-- Header -->
    <header class="mindmap-preview__header">
      <h2 class="mindmap-preview__title">{{ mapTitle }}</h2>
      <div class="mindmap-preview__controls">
        <button class="mindmap-preview__control-btn" @click="expandAll">全部展开</button>
        <button class="mindmap-preview__control-btn" @click="collapseAll">全部折叠</button>
      </div>
    </header>

    <!-- Mind Map Tree -->
    <section class="mindmap-preview__canvas">
      <div class="mindmap-preview__tree">
        <MindMapNode
          :node="rootNode"
          :depth="0"
          :expanded-ids="expandedIds"
          @toggle="toggleNode"
        />
      </div>
    </section>

    <!-- Legend -->
    <footer class="mindmap-preview__legend">
      <span class="mindmap-preview__legend-item">
        <span class="mindmap-preview__legend-dot mindmap-preview__legend-dot--root"></span>
        根节点
      </span>
      <span class="mindmap-preview__legend-item">
        <span class="mindmap-preview__legend-dot mindmap-preview__legend-dot--branch"></span>
        分支节点
      </span>
      <span class="mindmap-preview__legend-item">
        <span class="mindmap-preview__legend-dot mindmap-preview__legend-dot--leaf"></span>
        叶子节点
      </span>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, defineComponent, h } from 'vue'

interface MindMapNodeData {
  id: string
  label: string
  children?: MindMapNodeData[]
}

interface MindMapPreviewConfig {
  display?: {
    map_title?: string
    map_data?: MindMapNodeData
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: MindMapPreviewConfig
}>()

const mapTitle = computed<string>(() =>
  props.nodeConfig.display?.map_title ?? 'Python 数据结构知识图谱'
)

/** Placeholder mind map data */
const rootNode = computed<MindMapNodeData>(() =>
  props.nodeConfig.display?.map_data ?? placeholderMapData
)

const placeholderMapData: MindMapNodeData = {
  id: 'root',
  label: 'Python 数据结构',
  children: [
    {
      id: 'linear',
      label: '线性结构',
      children: [
        { id: 'list', label: '列表 (List)', children: [
          { id: 'list-ops', label: '增删改查操作' },
          { id: 'list-comp', label: '列表推导式' },
          { id: 'list-slice', label: '切片操作' }
        ]},
        { id: 'tuple', label: '元组 (Tuple)', children: [
          { id: 'tuple-immutable', label: '不可变性' },
          { id: 'tuple-unpack', label: '解包操作' }
        ]},
        { id: 'stack', label: '栈 (Stack)' },
        { id: 'queue', label: '队列 (Queue)' }
      ]
    },
    {
      id: 'nonlinear',
      label: '非线性结构',
      children: [
        { id: 'dict', label: '字典 (Dict)', children: [
          { id: 'dict-hash', label: '哈希表原理' },
          { id: 'dict-methods', label: '常用方法' }
        ]},
        { id: 'set', label: '集合 (Set)', children: [
          { id: 'set-ops', label: '集合运算' }
        ]},
        { id: 'tree', label: '树结构' }
      ]
    },
    {
      id: 'algorithm',
      label: '常用算法',
      children: [
        { id: 'sort', label: '排序算法' },
        { id: 'search', label: '搜索算法' },
        { id: 'recursion', label: '递归' }
      ]
    }
  ]
}

/** Track expanded node IDs */
const expandedIds = ref<Set<string>>(new Set(['root', 'linear', 'nonlinear', 'algorithm']))

function toggleNode(nodeId: string) {
  if (expandedIds.value.has(nodeId)) {
    expandedIds.value.delete(nodeId)
  } else {
    expandedIds.value.add(nodeId)
  }
  // Trigger reactivity
  expandedIds.value = new Set(expandedIds.value)
}

function expandAll() {
  const allIds = collectAllIds(rootNode.value)
  expandedIds.value = new Set(allIds)
}

function collapseAll() {
  expandedIds.value = new Set()
}

function collectAllIds(node: MindMapNodeData): string[] {
  const ids: string[] = [node.id]
  if (node.children) {
    for (const child of node.children) {
      ids.push(...collectAllIds(child))
    }
  }
  return ids
}

/** Recursive MindMapNode component */
const MindMapNode = defineComponent({
  name: 'MindMapNode',
  props: {
    node: { type: Object as () => MindMapNodeData, required: true },
    depth: { type: Number, required: true },
    expandedIds: { type: Object as () => Set<string>, required: true }
  },
  emits: ['toggle'],
  setup(nodeProps, { emit: nodeEmit }) {
    return () => {
      const node = nodeProps.node
      const hasChildren = node.children && node.children.length > 0
      const isExpanded = nodeProps.expandedIds.has(node.id)
      const depthClass = `mindmap-node--depth-${Math.min(nodeProps.depth, 3)}`

      const children: ReturnType<typeof h>[] = []

      // Node label
      children.push(
        h('div', {
          class: ['mindmap-node__label', depthClass],
          onClick: () => { if (hasChildren) nodeEmit('toggle', node.id) }
        }, [
          hasChildren
            ? h('span', { class: 'mindmap-node__toggle' }, isExpanded ? '▼' : '▶')
            : h('span', { class: 'mindmap-node__leaf-dot' }, '●'),
          h('span', { class: 'mindmap-node__text' }, node.label)
        ])
      )

      // Children
      if (hasChildren && isExpanded) {
        const childNodes = node.children!.map(child =>
          h(MindMapNode, {
            node: child,
            depth: nodeProps.depth + 1,
            expandedIds: nodeProps.expandedIds,
            onToggle: (id: string) => nodeEmit('toggle', id)
          })
        )
        children.push(h('div', { class: 'mindmap-node__children' }, childNodes))
      }

      return h('div', { class: 'mindmap-node' }, children)
    }
  }
})
</script>

<style scoped>
.mindmap-preview {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  padding: var(--spacing-lg, 1.5rem);
  gap: var(--spacing-md, 1rem);
}

.mindmap-preview__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mindmap-preview__title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
}

.mindmap-preview__controls {
  display: flex;
  gap: 0.5rem;
}

.mindmap-preview__control-btn {
  padding: 0.375rem 0.75rem;
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--color-gray-600, #475569);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-sm, 0.25rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.mindmap-preview__control-btn:hover {
  background: var(--color-gray-50, #f8fafc);
  border-color: var(--color-gray-300, #cbd5e1);
}

.mindmap-preview__canvas {
  flex: 1;
  min-height: 0;
  overflow: auto;
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.mindmap-preview__tree {
  min-width: fit-content;
}

.mindmap-preview__legend {
  display: flex;
  gap: var(--spacing-md, 1rem);
  justify-content: center;
}

.mindmap-preview__legend-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.75rem;
  color: var(--color-gray-500, #64748b);
}

.mindmap-preview__legend-dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
}

.mindmap-preview__legend-dot--root {
  background: var(--color-primary-500, #6366f1);
}

.mindmap-preview__legend-dot--branch {
  background: #f59e0b;
}

.mindmap-preview__legend-dot--leaf {
  background: #22c55e;
}
</style>

<style>
/* Unscoped styles for recursive component */
.mindmap-node {
  margin-left: 0;
}

.mindmap-node__label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem 0.625rem;
  border-radius: 0.375rem;
  cursor: pointer;
  transition: background 0.15s ease;
  user-select: none;
}

.mindmap-node__label:hover {
  background: #f1f5f9;
}

.mindmap-node--depth-0 {
  font-size: 1rem;
  font-weight: 700;
  color: #4f46e5;
}

.mindmap-node--depth-1 {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #1e293b;
}

.mindmap-node--depth-2 {
  font-size: 0.875rem;
  font-weight: 500;
  color: #334155;
}

.mindmap-node--depth-3 {
  font-size: 0.8125rem;
  color: #64748b;
}

.mindmap-node__toggle {
  font-size: 0.625rem;
  color: #94a3b8;
  width: 1rem;
  text-align: center;
}

.mindmap-node__leaf-dot {
  font-size: 0.5rem;
  color: #22c55e;
  width: 1rem;
  text-align: center;
}

.mindmap-node__text {
  flex: 1;
}

.mindmap-node__children {
  margin-left: 1.5rem;
  border-left: 1px solid #e2e8f0;
  padding-left: 0.5rem;
}
</style>
