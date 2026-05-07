<template>
  <div class="node-card">
    <div class="icon-wrap">📤</div>
    <h2>{{ config?.name || '文件上传' }}</h2>
    <p class="hint">{{ config?.config?.scenario || config?.config?.upload_content || config?.config?.scene_content || '请按要求上传文件' }}</p>
    <n-upload :multiple="false" action="#" style="margin-top:16px"><n-button>选择文件</n-button></n-upload>
    <p class="hint" style="margin-top:8px">支持格式: {{ config?.config?.format || 'pdf, docx, zip' }}</p>
    <n-button type="primary" style="margin-top:16px" @click="emit('next')">已上传，下一步</n-button>
  </div>
</template>
<script setup lang="ts">
import { NButton, NUpload } from 'naive-ui'
defineProps<{ config?: Record<string, any>; taskId?: number }>()
const emit = defineEmits<{ next: [] }>()
</script>
<style scoped>
.node-card { text-align: center; padding: 40px; background: #fff; border-radius: 16px; border: 1px solid #E2E8F0; max-width: 500px; margin: 0 auto; }
.icon-wrap { font-size: 48px; margin-bottom: 16px; }
h2 { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 12px 0; }
.hint { color: #64748B; font-size: 13px; margin: 8px 0; }
</style>
