<template>
  <div class="node-card">
    <div class="icon-wrap">📖</div>
    <h2>{{ config?.name || '资源阅读' }}</h2>
    <div class="file-card">
      <span class="file-icon">📄</span>
      <span class="file-name">{{ config?.config?.resource_name || config?.config?.resourceName || config?.config?.topic || config?.config?.class_material_id || '教学资源' }}</span>
    </div>
    <n-button type="primary" style="margin-top:16px" @click="emit('next')">阅读完成，下一步</n-button>
  </div>
</template>
<script setup lang="ts">
import { NButton } from 'naive-ui'
defineProps<{ config?: Record<string, any>; taskId?: number }>()
const emit = defineEmits<{ next: [] }>()
</script>
<style scoped>
.node-card { text-align: center; padding: 40px; background: #fff; border-radius: 16px; border: 1px solid #E2E8F0; max-width: 500px; margin: 0 auto; }
.icon-wrap { font-size: 48px; margin-bottom: 16px; }
h2 { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 16px 0; }
.file-card { display: flex; align-items: center; gap: 12px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 16px; margin: 12px 0; justify-content: center; }
.file-icon { font-size: 28px; }
.file-name { font-weight: 600; color: #1E293B; font-size: 14px; }
</style>
