<template>
  <Transition name="tooltip-fade">
    <div
      v-if="visible"
      class="node-hover-tooltip"
      :style="tooltipStyle"
    >
      <div class="tooltip-content">
        <span class="tooltip-icon">🤖</span>
        <span class="tooltip-text">{{ tooltipText }}</span>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, computed, watch, onBeforeUnmount } from 'vue'
import { getAiProcessingSummary } from './nodeAiSpecs'

const props = defineProps<{
  nodeType: string
  isHovering: boolean
  position: { x: number; y: number }
}>()

const visible = ref(false)
let hoverTimer: ReturnType<typeof setTimeout> | null = null

/** Step 6: 200ms delay before showing tooltip */
watch(
  () => props.isHovering,
  (hovering) => {
    if (hovering) {
      hoverTimer = setTimeout(() => {
        visible.value = true
      }, 200)
    } else {
      if (hoverTimer) {
        clearTimeout(hoverTimer)
        hoverTimer = null
      }
      visible.value = false
    }
  }
)

onBeforeUnmount(() => {
  if (hoverTimer) {
    clearTimeout(hoverTimer)
  }
})

const tooltipText = computed(() => {
  const summary = getAiProcessingSummary(props.nodeType)
  if (!summary) return '无AI处理需求'
  return `${summary.fieldCount}个字段需AI处理，预计${summary.estimatedSeconds}秒`
})

const tooltipStyle = computed(() => ({
  left: `${props.position.x}px`,
  top: `${props.position.y - 40}px`
}))
</script>

<style scoped>
.node-hover-tooltip {
  position: absolute;
  z-index: 100;
  pointer-events: none;
}

.tooltip-content {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(30, 30, 30, 0.9);
  color: #ffffff;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.tooltip-icon {
  font-size: 12px;
}

.tooltip-text {
  font-weight: 500;
}

.tooltip-fade-enter-active,
.tooltip-fade-leave-active {
  transition: opacity 0.15s ease;
}

.tooltip-fade-enter-from,
.tooltip-fade-leave-to {
  opacity: 0;
}
</style>
