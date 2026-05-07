<template>
  <div class="taskboard">
    <h2 class="title">任务下发</h2>
    <n-card class="task-card"><h3>{{ groupTask?.title || config?.taskTitle || config?.task_content || '主线任务' }}</h3><p>{{ groupTask?.desc || config?.taskDesc || config?.task_content || '请仔细阅读任务要求并确认接收。' }}</p></n-card>
    <div class="actions"><n-button type="primary" :disabled="!canProceed" @click="handleAccept">{{ accepted ? '已接收' : '确认接收' }}</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton, NCard } from 'naive-ui'
const props = defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const accepted = ref(false)
const groupTask = computed(() => {
  const tasks = props.config?.groupTasks
  if (tasks && props.context?.groupId) return tasks[props.context.groupId]
  return null
})
const canProceed = computed(() => accepted.value)
function handleAccept() { accepted.value = true; emit('step-complete'); emit('update-data', { accepted: true }) }
</script>
<style scoped>
.taskboard { max-width: 600px; margin: 0 auto; padding: 40px 24px; }
.title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 16px 0; }
.task-card { margin-bottom: 24px; } .task-card h3 { margin: 0 0 8px 0; font-size: 16px; } .task-card p { color: #475569; }
.actions { display: flex; justify-content: center; }
</style>
