<template>
  <aside
    class="node-palette"
    :class="{
      'is-collapsed': isCollapsed,
      'is-drag-over-delete': isHoveringDelete
    }"
    @dragover.prevent="onPaletteDragOver"
    @dragleave="onPaletteDragLeave"
    @drop="onPaletteDrop"
  >
    <div class="palette-inner" v-show="!isCollapsed">
      <h2 class="palette-title">实训节点库</h2>
      <div class="palette-categories">
        <div v-for="cat in NODE_CATEGORIES" :key="cat.type" class="cat-group">
          <div class="cat-header" @click="toggleCategory(cat.type)">
            <span class="cat-label">{{ cat.label }}</span>
            <i class="chevron-icon" :class="{ 'is-rotated': expandedCats.includes(cat.type) }"></i>
          </div>
          <div class="cat-drawer" :class="{ 'is-open': expandedCats.includes(cat.type) }">
            <div class="cat-drawer-inner">
              <div class="node-list">
                <div
                  v-for="item in cat.items"
                  :key="item.type"
                  class="palette-node"
                  draggable="true"
                  @dragstart="onDragStart($event, item)"
                  @dragend="onDragEnd"
                >
                  <span class="palette-node-icon">{{ item.icon }}</span>
                  <span class="palette-node-label">{{ item.label }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="delete-overlay" v-show="isHoveringDelete">
      <div class="trash-icon">🗑️</div>
      <span>松开鼠标删除该节点</span>
    </div>

    <div class="palette-toggle" @click="isCollapsed = !isCollapsed">
      {{ isCollapsed ? '▶' : '◀' }}
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { MATERIAL_CATEGORIES } from '@/views/teacher/data/node-templates'

const emit = defineEmits<{
  'node-drag-start': [event: DragEvent, nodeType: string, nodeLabel: string]
  'node-drag-end': []
  'node-delete-drop': [nodeId: string]
}>()

// Map MATERIAL_CATEGORIES to our format
const NODE_CATEGORIES = MATERIAL_CATEGORIES.map(cat => ({
  type: cat.type,
  label: cat.label,
  items: cat.items.map(item => ({
    type: item.type,
    label: item.label,
    icon: item.icon
  }))
}))

const isCollapsed = ref(false)
const expandedCats = ref(NODE_CATEGORIES.map(c => c.type))
const isHoveringDelete = ref(false)

function toggleCategory(type: string) {
  const idx = expandedCats.value.indexOf(type)
  if (idx > -1) {
    expandedCats.value.splice(idx, 1)
  } else {
    expandedCats.value.push(type)
  }
}

function onDragStart(event: DragEvent, item: { type: string; label: string }) {
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'copy'
    event.dataTransfer.setData('application/orchestration-node', JSON.stringify({
      nodeType: item.type,
      nodeLabel: item.label
    }))
  }
  emit('node-drag-start', event, item.type, item.label)
}

function onDragEnd() {
  isHoveringDelete.value = false
  emit('node-drag-end')
}

function onPaletteDragOver(event: DragEvent) {
  if (isCollapsed.value) return
  // Only show delete overlay if dragging from canvas (not from palette)
  const data = event.dataTransfer?.types
  if (data?.includes('application/orchestration-canvas-node')) {
    isHoveringDelete.value = true
  }
}

function onPaletteDragLeave(event: DragEvent) {
  if ((event.currentTarget as HTMLElement).contains(event.relatedTarget as Node)) return
  isHoveringDelete.value = false
}

function onPaletteDrop(event: DragEvent) {
  const nodeId = event.dataTransfer?.getData('application/orchestration-canvas-node')
  if (nodeId) {
    emit('node-delete-drop', nodeId)
  }
  isHoveringDelete.value = false
}
</script>

<style scoped>
.node-palette {
  width: 260px;
  background: var(--vt-c-white, #ffffff);
  border-right: 1px solid var(--color-border, rgba(60, 60, 60, 0.12));
  display: flex;
  flex-direction: column;
  position: relative;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  z-index: 10;
}

.node-palette.is-collapsed {
  width: 0;
  border-right: none;
}

.node-palette.is-drag-over-delete {
  background: #fef2f2;
  border-color: #ef4444;
}

.palette-inner {
  width: 260px;
  height: 100%;
  padding: 16px 12px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.palette-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-heading, #2c3e50);
  margin-bottom: 12px;
  padding: 0 4px;
}

.palette-categories {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

.palette-categories::-webkit-scrollbar {
  width: 4px;
}

.palette-categories::-webkit-scrollbar-thumb {
  background: var(--color-border, rgba(60, 60, 60, 0.12));
  border-radius: 4px;
}

.cat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 4px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
}

.cat-header:hover {
  background: var(--color-background-soft, #f8f8f8);
}

.cat-label {
  font-weight: 600;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
  font-size: 13px;
}

.chevron-icon {
  width: 6px;
  height: 6px;
  border-right: 2px solid var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
  border-bottom: 2px solid var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
  transform: rotate(-45deg);
  transition: transform 0.3s ease;
}

.chevron-icon.is-rotated {
  transform: rotate(45deg);
}

.cat-drawer {
  display: grid;
  grid-template-rows: 0fr;
  transition: grid-template-rows 0.3s ease-out;
}

.cat-drawer.is-open {
  grid-template-rows: 1fr;
}

.cat-drawer-inner {
  min-height: 0;
  overflow: hidden;
}

.node-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 4px 0 10px 0;
}

.palette-node {
  padding: 8px 12px;
  background: var(--color-background-soft, #f8f8f8);
  border: 1px dashed var(--color-border, rgba(60, 60, 60, 0.12));
  border-radius: 6px;
  cursor: grab;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
  font-size: 13px;
  color: var(--color-text, #2c3e50);
}

.palette-node:hover {
  background: #eef2ff;
  border-color: #818cf8;
  color: #4f46e5;
  transform: translateX(3px);
}

.palette-node:active {
  cursor: grabbing;
  transform: scale(0.97);
}

.palette-node-icon {
  font-size: 15px;
}

.palette-node-label {
  font-weight: 500;
}

/* Delete overlay */
.delete-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ef4444;
  font-weight: bold;
  font-size: 14px;
  border: 4px dashed #fca5a5;
  background: rgba(254, 242, 242, 0.9);
  z-index: 50;
}

.trash-icon {
  font-size: 36px;
  margin-bottom: 8px;
  animation: bounce 1s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

/* Toggle button */
.palette-toggle {
  position: absolute;
  right: -16px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 48px;
  background: var(--vt-c-white, #ffffff);
  border: 1px solid var(--color-border, rgba(60, 60, 60, 0.12));
  border-left: none;
  border-radius: 0 6px 6px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
  font-size: 10px;
  z-index: 21;
  transition: color 0.2s;
}

.palette-toggle:hover {
  color: #4f46e5;
}
</style>
