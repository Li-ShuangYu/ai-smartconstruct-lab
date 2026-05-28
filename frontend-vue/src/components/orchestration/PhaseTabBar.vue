<template>
  <div class="phase-tab-bar">
    <div class="phase-tabs-scroll">
      <div class="phase-tabs">
        <div
          v-for="phase in phases"
          :key="phase.phase_id"
          class="phase-tab"
          :class="{ 'is-active': phase.phase_id === activePhaseId }"
          @click="handleTabClick(phase.phase_id)"
          @dblclick="startRename(phase)"
          @contextmenu.prevent="openContextMenu($event, phase)"
        >
          <span v-if="renamingPhaseId !== phase.phase_id" class="phase-tab-label">
            {{ phase.phase_name }}
          </span>
          <input
            v-else
            ref="renameInputRef"
            v-model="renameValue"
            class="phase-rename-input"
            maxlength="20"
            @blur="confirmRename"
            @keydown.enter="confirmRename"
            @keydown.escape="cancelRename"
            @click.stop
          />
          <span class="phase-node-count">{{ phase.nodes.length }}</span>
        </div>
      </div>
    </div>

    <button
      class="phase-add-btn"
      :disabled="phases.length >= 10"
      :title="phases.length >= 10 ? '最多支持10个阶段' : '添加阶段'"
      @click="showAddDialog = true"
    >
      +
    </button>

    <!-- Context Menu -->
    <Teleport to="body">
      <div
        v-if="contextMenu.visible"
        class="phase-context-menu"
        :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
        @click.stop
      >
        <div class="context-menu-item" @click="handleRenameFromMenu">
          ✏️ 重命名
        </div>
        <div class="context-menu-item" @click="handleTimeConfig">
          🕐 时间设置
        </div>
        <div
          class="context-menu-item context-menu-item--danger"
          :class="{ 'is-disabled': phases.length <= 1 }"
          @click="handleDeleteFromMenu"
        >
          🗑️ 删除阶段
        </div>
      </div>
      <div
        v-if="contextMenu.visible"
        class="phase-context-overlay"
        @click="closeContextMenu"
        @contextmenu.prevent="closeContextMenu"
      />
    </Teleport>

    <!-- Add Phase Dialog -->
    <NModal v-model:show="showAddDialog" preset="dialog" title="添加阶段" :show-icon="false">
      <div class="add-phase-form">
        <label class="add-phase-label">阶段名称（1-20字符）</label>
        <NInput
          v-model:value="newPhaseName"
          placeholder="请输入阶段名称"
          maxlength="20"
          show-count
          @keydown.enter="confirmAddPhase"
        />
      </div>
      <template #action>
        <NButton @click="showAddDialog = false">取消</NButton>
        <NButton type="primary" :disabled="!isNewPhaseNameValid" @click="confirmAddPhase">
          确定
        </NButton>
      </template>
    </NModal>

    <!-- Time Config Dialog -->
    <NModal v-model:show="showTimeDialog" preset="dialog" title="阶段时间设置" :show-icon="false">
      <div class="time-config-form" v-if="timeConfigPhase">
        <div class="time-config-field">
          <label class="time-config-label">计划开始时间（可选）</label>
          <NDatePicker
            v-model:value="timeStartValue"
            type="datetime"
            clearable
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </div>
        <div class="time-config-field">
          <label class="time-config-label">计划结束时间（可选）</label>
          <NDatePicker
            v-model:value="timeEndValue"
            type="datetime"
            clearable
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </div>
      </div>
      <template #action>
        <NButton @click="showTimeDialog = false">取消</NButton>
        <NButton type="primary" @click="confirmTimeConfig">确定</NButton>
      </template>
    </NModal>

    <!-- Delete Confirmation -->
    <PhaseDeleteConfirm
      v-model:show="showDeleteConfirm"
      :phase-name="deleteTargetPhase?.phase_name ?? ''"
      :node-count="deleteTargetPhase?.nodes.length ?? 0"
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { NModal, NInput, NButton, NDatePicker } from 'naive-ui'
import { useOrchestrationStore } from '@/stores/modules/orchestration.store'
import PhaseDeleteConfirm from './PhaseDeleteConfirm.vue'
import type { Phase } from '@/services/types/orchestration'

const store = useOrchestrationStore()

const phases = computed(() => store.phases)
const activePhaseId = computed(() => store.activePhaseId)

// --- Tab click ---
function handleTabClick(phaseId: string) {
  store.setActivePhase(phaseId)
}

// --- Rename ---
const renamingPhaseId = ref<string>('')
const renameValue = ref('')
const renameInputRef = ref<HTMLInputElement[]>([])

function startRename(phase: Phase) {
  renamingPhaseId.value = phase.phase_id
  renameValue.value = phase.phase_name
  nextTick(() => {
    const input = renameInputRef.value?.[0]
    if (input) {
      input.focus()
      input.select()
    }
  })
}

function confirmRename() {
  if (!renamingPhaseId.value) return
  const trimmed = renameValue.value.trim()
  if (trimmed.length >= 1 && trimmed.length <= 20) {
    store.renamePhase(renamingPhaseId.value, trimmed)
  }
  renamingPhaseId.value = ''
}

function cancelRename() {
  renamingPhaseId.value = ''
}

// --- Context Menu ---
const contextMenu = ref({ visible: false, x: 0, y: 0, phase: null as Phase | null })

function openContextMenu(event: MouseEvent, phase: Phase) {
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    phase
  }
}

function closeContextMenu() {
  contextMenu.value.visible = false
}

function handleRenameFromMenu() {
  if (contextMenu.value.phase) {
    startRename(contextMenu.value.phase)
  }
  closeContextMenu()
}

function handleDeleteFromMenu() {
  if (phases.value.length <= 1) return
  if (contextMenu.value.phase) {
    deleteTargetPhase.value = contextMenu.value.phase
    showDeleteConfirm.value = true
  }
  closeContextMenu()
}

function handleTimeConfig() {
  if (contextMenu.value.phase) {
    timeConfigPhase.value = contextMenu.value.phase
    timeStartValue.value = contextMenu.value.phase.plan_start_time
      ? new Date(contextMenu.value.phase.plan_start_time).getTime()
      : null
    timeEndValue.value = contextMenu.value.phase.plan_end_time
      ? new Date(contextMenu.value.phase.plan_end_time).getTime()
      : null
    showTimeDialog.value = true
  }
  closeContextMenu()
}

// --- Add Phase ---
const showAddDialog = ref(false)
const newPhaseName = ref('')

const isNewPhaseNameValid = computed(() => {
  const trimmed = newPhaseName.value.trim()
  return trimmed.length >= 1 && trimmed.length <= 20
})

function confirmAddPhase() {
  if (!isNewPhaseNameValid.value) return
  store.addPhase(newPhaseName.value.trim())
  newPhaseName.value = ''
  showAddDialog.value = false
}

// --- Delete Phase ---
const showDeleteConfirm = ref(false)
const deleteTargetPhase = ref<Phase | null>(null)

function confirmDelete() {
  if (deleteTargetPhase.value) {
    store.removePhase(deleteTargetPhase.value.phase_id)
  }
  deleteTargetPhase.value = null
  showDeleteConfirm.value = false
}

// --- Time Config ---
const showTimeDialog = ref(false)
const timeConfigPhase = ref<Phase | null>(null)
const timeStartValue = ref<number | null>(null)
const timeEndValue = ref<number | null>(null)

function confirmTimeConfig() {
  if (!timeConfigPhase.value) return
  const phase = store.phases.find(p => p.phase_id === timeConfigPhase.value!.phase_id)
  if (phase) {
    phase.plan_start_time = timeStartValue.value
      ? new Date(timeStartValue.value).toISOString()
      : undefined
    phase.plan_end_time = timeEndValue.value
      ? new Date(timeEndValue.value).toISOString()
      : undefined
  }
  showTimeDialog.value = false
  timeConfigPhase.value = null
}
</script>

<style scoped>
.phase-tab-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--color-background, #f8fafc);
  border-bottom: 1px solid var(--color-border, rgba(60, 60, 60, 0.12));
  min-height: 48px;
}

.phase-tabs-scroll {
  flex: 1;
  overflow-x: auto;
  overflow-y: hidden;
}

.phase-tabs-scroll::-webkit-scrollbar {
  height: 3px;
}

.phase-tabs-scroll::-webkit-scrollbar-thumb {
  background: var(--color-border-hover, rgba(60, 60, 60, 0.29));
  border-radius: 3px;
}

.phase-tabs {
  display: flex;
  gap: 4px;
  white-space: nowrap;
}

.phase-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text, #2c3e50);
  background: transparent;
  border: 1px solid transparent;
  transition: all 0.2s ease;
  user-select: none;
}

.phase-tab:hover {
  background: var(--color-background-mute, #f2f2f2);
}

.phase-tab.is-active {
  background: var(--vt-c-white, #ffffff);
  border-color: var(--color-border-hover, rgba(60, 60, 60, 0.29));
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  color: #4f46e5;
  font-weight: 600;
}

.phase-tab-label {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.phase-node-count {
  font-size: 11px;
  padding: 1px 5px;
  border-radius: 8px;
  background: var(--color-background-mute, #f2f2f2);
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
}

.phase-tab.is-active .phase-node-count {
  background: #eef2ff;
  color: #4f46e5;
}

.phase-rename-input {
  width: 100px;
  padding: 2px 6px;
  border: 1px solid #4f46e5;
  border-radius: 4px;
  font-size: 13px;
  outline: none;
  background: var(--vt-c-white, #ffffff);
  color: var(--color-text, #2c3e50);
}

.phase-add-btn {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: 1px dashed var(--color-border-hover, rgba(60, 60, 60, 0.29));
  background: transparent;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
  transition: all 0.2s;
  flex-shrink: 0;
}

.phase-add-btn:hover:not(:disabled) {
  border-color: #4f46e5;
  color: #4f46e5;
  background: #eef2ff;
}

.phase-add-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* Context Menu */
.phase-context-overlay {
  position: fixed;
  inset: 0;
  z-index: 999;
}

.phase-context-menu {
  position: fixed;
  z-index: 1000;
  background: var(--vt-c-white, #ffffff);
  border: 1px solid var(--color-border, rgba(60, 60, 60, 0.12));
  border-radius: 8px;
  padding: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  min-width: 140px;
}

.context-menu-item {
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  color: var(--color-text, #2c3e50);
  transition: background 0.15s;
}

.context-menu-item:hover {
  background: var(--color-background-mute, #f2f2f2);
}

.context-menu-item--danger {
  color: #ef4444;
}

.context-menu-item--danger:hover {
  background: #fef2f2;
}

.context-menu-item.is-disabled {
  opacity: 0.4;
  cursor: not-allowed;
  pointer-events: none;
}

/* Add Phase Form */
.add-phase-form {
  padding: 8px 0;
}

.add-phase-label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
}

/* Time Config Form */
.time-config-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 8px 0;
}

.time-config-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.time-config-label {
  font-size: 13px;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
}
</style>
