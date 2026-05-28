<template>
  <div class="orchestration-editor">
    <!-- Left: Node Palette -->
    <NodePalette
      @node-drag-start="onPaletteDragStart"
      @node-drag-end="onPaletteDragEnd"
      @node-delete-drop="onNodeDeleteDrop"
    />

    <!-- Center: Phase Tabs + Canvas -->
    <div class="editor-main">
      <!-- Top toolbar -->
      <div class="editor-toolbar">
        <span class="editor-title">实训流程编排</span>
        <div class="toolbar-spacer"></div>
        <slot name="toolbar-actions"></slot>
      </div>

      <!-- Phase Tab Bar -->
      <PhaseTabBar />

      <!-- Canvas scoped to active phase -->
      <OrchestrationCanvas />
    </div>

    <!-- Right: Node Settings Panel (shown when a node is selected) -->
    <NodeSettingsPanel />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useOrchestrationStore } from '@/stores/modules/orchestration.store'
import PhaseTabBar from './PhaseTabBar.vue'
import NodePalette from './NodePalette.vue'
import OrchestrationCanvas from './OrchestrationCanvas.vue'
import NodeSettingsPanel from './NodeSettingsPanel.vue'

const store = useOrchestrationStore()

// Initialize default 3 phases on mount if store is empty
onMounted(() => {
  if (store.phases.length === 0) {
    initializeDefaultPhases()
  }
})

/**
 * Step 2: Initialize default 3-phase template on new template creation
 * 课前阶段 (sort_num=1), 课中阶段 (sort_num=2), 课后阶段 (sort_num=3)
 */
function initializeDefaultPhases() {
  store.addPhase('课前阶段')
  store.addPhase('课中阶段')
  store.addPhase('课后阶段')
  // Set first phase as active
  const firstPhase = store.phases[0]
  if (firstPhase) {
    store.setActivePhase(firstPhase.phase_id)
  }
}

function onPaletteDragStart(_event: DragEvent, _nodeType: string, _nodeLabel: string) {
  // Could add visual feedback on canvas
}

function onPaletteDragEnd() {
  // Clean up visual feedback
}

function onNodeDeleteDrop(nodeId: string) {
  store.removeNode(nodeId)
}

// Expose initialization for external use
defineExpose({ initializeDefaultPhases })
</script>

<style scoped>
.orchestration-editor {
  display: flex;
  height: 100%;
  background: var(--color-background, #f8fafc);
  overflow: hidden;
}

.editor-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--color-border, rgba(60, 60, 60, 0.12));
  z-index: 5;
}

.editor-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-heading, #2c3e50);
}

.toolbar-spacer {
  flex: 1;
}
</style>
