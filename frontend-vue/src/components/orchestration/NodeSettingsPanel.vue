<template>
  <aside
    class="node-settings-panel"
    :class="{ 'is-visible': !!selectedNode }"
  >
    <div v-if="selectedNode" class="panel-content">
      <!-- Panel Header -->
      <div class="panel-header">
        <div class="panel-header-info">
          <span class="panel-node-icon">{{ getNodeIcon(selectedNode.node_type) }}</span>
          <div class="panel-header-text">
            <h3 class="panel-title">{{ selectedNode.node_name }}</h3>
            <span class="panel-node-type">{{ selectedNode.node_type }}</span>
          </div>
        </div>
        <button class="panel-close-btn" @click="closePanel" title="关闭">✕</button>
      </div>

      <!-- is_required toggle -->
      <div class="panel-required-toggle">
        <label class="toggle-label">
          <span>必须完成节点</span>
          <input
            type="checkbox"
            :checked="selectedNode.is_required"
            @change="toggleRequired"
          />
          <span class="toggle-switch"></span>
        </label>
      </div>

      <!-- Teacher Manual Settings Zone -->
      <section class="settings-zone manual-zone">
        <div class="zone-header">
          <span class="zone-icon">✏️</span>
          <h4 class="zone-title">教师手动设置区</h4>
        </div>
        <div class="zone-body">
          <NodeTypeSettings
            :node="selectedNode"
            zone="manual"
            :ai-flags="currentAiFlags"
            :validation-errors="validationErrors"
            @update-config="handleConfigUpdate"
          />
        </div>
      </section>

      <!-- AI Auto-Generated Zone -->
      <section class="settings-zone ai-zone">
        <div class="zone-header">
          <span class="zone-icon">🤖</span>
          <h4 class="zone-title">AI自动生成区</h4>
        </div>
        <div class="zone-body">
          <NodeTypeSettings
            :node="selectedNode"
            zone="ai"
            :ai-flags="currentAiFlags"
            :validation-errors="validationErrors"
            @update-config="handleConfigUpdate"
            @override-field="handleOverrideField"
            @restore-field="handleRestoreField"
          />
        </div>
      </section>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useOrchestrationStore } from '@/stores/modules/orchestration.store'
import { MATERIAL_CATEGORIES } from '@/views/teacher/data/node-templates'
import { getAiSpecForNodeType } from './nodeAiSpecs'
import { useNodeValidation } from './useNodeValidation'
import NodeTypeSettings from './NodeTypeSettings.vue'
import type { OrchestrationNode } from '@/services/types/orchestration'

const store = useOrchestrationStore()

const selectedNode = computed(() => store.selectedNode)

/** Get AI flags for the current node type from ai_spec registry */
const currentAiFlags = computed<string[]>(() => {
  if (!selectedNode.value) return []
  const spec = getAiSpecForNodeType(selectedNode.value.node_type)
  return spec?.ai_flags ?? []
})

/** Real-time validation with 500ms debounce (Step 5) */
const { validationErrors } = useNodeValidation(selectedNode)

function getNodeIcon(nodeType: string): string {
  for (const cat of MATERIAL_CATEGORIES) {
    const item = cat.items.find(i => i.type === nodeType)
    if (item) return item.icon
  }
  return '📦'
}

function closePanel() {
  store.clearSelection()
}

function toggleRequired() {
  if (!selectedNode.value) return
  const node = selectedNode.value
  // Toggle is_required directly on the node
  for (const phase of store.phases) {
    const found = phase.nodes.find(n => n.node_id === node.node_id)
    if (found) {
      found.is_required = !found.is_required
      return
    }
  }
}

function handleConfigUpdate(dimension: string, field: string, value: unknown) {
  if (!selectedNode.value) return
  const node = selectedNode.value
  const config = { ...node.config }
  const dimObj = { ...(config[dimension as keyof typeof config] as Record<string, unknown>) }
  dimObj[field] = value
  ;(config as Record<string, unknown>)[dimension] = dimObj
  store.updateNodeConfig(node.node_id, config)
}

/** Step 3: Teacher override — mark field as overridden */
function handleOverrideField(fieldName: string) {
  if (!selectedNode.value) return
  store.overrideAiField(selectedNode.value.node_id, fieldName)
}

/** Step 4: Restore AI field — clear override */
function handleRestoreField(fieldName: string) {
  if (!selectedNode.value) return
  store.restoreAiField(selectedNode.value.node_id, fieldName)
}
</script>

<style scoped>
.node-settings-panel {
  width: 0;
  overflow: hidden;
  background: var(--vt-c-white, #ffffff);
  border-left: 1px solid var(--color-border, rgba(60, 60, 60, 0.12));
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  height: 100%;
}

.node-settings-panel.is-visible {
  width: 360px;
}

.panel-content {
  width: 360px;
  height: 100%;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.panel-content::-webkit-scrollbar {
  width: 4px;
}

.panel-content::-webkit-scrollbar-thumb {
  background: var(--color-border, rgba(60, 60, 60, 0.12));
  border-radius: 4px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--color-border, rgba(60, 60, 60, 0.12));
  position: sticky;
  top: 0;
  background: var(--vt-c-white, #ffffff);
  z-index: 2;
}

.panel-header-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.panel-node-icon {
  font-size: 20px;
}

.panel-header-text {
  display: flex;
  flex-direction: column;
}

.panel-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-heading, #2c3e50);
  margin: 0;
}

.panel-node-type {
  font-size: 11px;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
  margin-top: 2px;
}

.panel-close-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.panel-close-btn:hover {
  background: var(--color-background-mute, #f2f2f2);
  color: var(--color-text, #2c3e50);
}

.panel-required-toggle {
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border, rgba(60, 60, 60, 0.12));
}

.toggle-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  font-size: 13px;
  color: var(--color-text, #2c3e50);
  font-weight: 500;
}

.toggle-label input[type="checkbox"] {
  display: none;
}

.toggle-switch {
  width: 36px;
  height: 20px;
  background: var(--color-border, rgba(60, 60, 60, 0.12));
  border-radius: 10px;
  position: relative;
  transition: background 0.2s;
}

.toggle-switch::after {
  content: '';
  position: absolute;
  width: 16px;
  height: 16px;
  background: var(--vt-c-white, #ffffff);
  border-radius: 50%;
  top: 2px;
  left: 2px;
  transition: transform 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}

.toggle-label input:checked + .toggle-switch {
  background: #4f46e5;
}

.toggle-label input:checked + .toggle-switch::after {
  transform: translateX(16px);
}

/* Settings Zones */
.settings-zone {
  padding: 16px;
}

.settings-zone + .settings-zone {
  border-top: 1px solid var(--color-border, rgba(60, 60, 60, 0.12));
}

.zone-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.zone-icon {
  font-size: 14px;
}

.zone-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-heading, #2c3e50);
  margin: 0;
}

.manual-zone .zone-header {
  padding-bottom: 8px;
  border-bottom: 2px solid #e0e7ff;
}

.ai-zone .zone-header {
  padding-bottom: 8px;
  border-bottom: 2px solid #d1fae5;
}

.zone-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
