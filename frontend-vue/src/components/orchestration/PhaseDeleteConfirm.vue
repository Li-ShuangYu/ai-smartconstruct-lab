<template>
  <NModal
    :show="show"
    preset="dialog"
    type="warning"
    title="确认删除阶段"
    :positive-text="confirmText"
    negative-text="取消"
    @positive-click="handleConfirm"
    @negative-click="handleCancel"
    @update:show="(val: boolean) => emit('update:show', val)"
  >
    <div class="delete-confirm-content">
      <p class="delete-confirm-message">
        确定要删除阶段「<strong>{{ phaseName }}</strong>」吗？
      </p>
      <div v-if="nodeCount > 0" class="delete-confirm-warning">
        <span class="warning-icon">⚠️</span>
        <span class="warning-text">
          该阶段内包含 <strong>{{ nodeCount }}</strong> 个节点及其连线，删除后将一并移除且无法恢复。
        </span>
      </div>
      <p v-else class="delete-confirm-hint">
        该阶段为空，删除后不影响其他内容。
      </p>
    </div>
  </NModal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NModal } from 'naive-ui'

const props = defineProps<{
  show: boolean
  phaseName: string
  nodeCount: number
}>()

const emit = defineEmits<{
  'update:show': [value: boolean]
  confirm: []
}>()

const confirmText = computed(() =>
  props.nodeCount > 0 ? `删除阶段及 ${props.nodeCount} 个节点` : '删除阶段'
)

function handleConfirm() {
  emit('confirm')
}

function handleCancel() {
  emit('update:show', false)
}
</script>

<style scoped>
.delete-confirm-content {
  padding: 4px 0;
}

.delete-confirm-message {
  font-size: 14px;
  color: var(--color-text, #2c3e50);
  margin-bottom: 12px;
}

.delete-confirm-warning {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 6px;
}

.warning-icon {
  flex-shrink: 0;
  font-size: 16px;
}

.warning-text {
  font-size: 13px;
  color: #92400e;
  line-height: 1.5;
}

.delete-confirm-hint {
  font-size: 13px;
  color: var(--vt-c-text-light-2, rgba(60, 60, 60, 0.66));
}
</style>
