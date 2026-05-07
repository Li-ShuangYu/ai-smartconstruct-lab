<template>
  <div class="node-card">
    <div class="icon-wrap">👥</div>
    <h2>{{ config?.name || '学生分组' }}</h2>
    <p>您被分配到了 <b>第 {{ randomGroup }} 组</b></p>
    <p class="hint">分组数: {{ config?.config?.group_count || config?.config?.groupCount || 4 }}，方式: {{ config?.config?.group_method || config?.config?.groupMethod || '随机' }}</p>
    <n-button type="primary" @click="emit('next')">确认分组</n-button>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { NButton } from 'naive-ui'
const props = defineProps<{ config?: Record<string, any>; taskId?: number }>()
const emit = defineEmits<{ next: [] }>()
const randomGroup = ref(Math.floor(Math.random() * (props.config?.config?.group_count || 4)) + 1)
</script>
<style scoped>
.node-card { text-align: center; padding: 40px; background: #fff; border-radius: 16px; border: 1px solid #E2E8F0; max-width: 500px; margin: 0 auto; }
.icon-wrap { font-size: 48px; margin-bottom: 16px; }
h2 { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 12px 0; }
.hint { color: #64748B; font-size: 13px; margin: 8px 0 20px; }
</style>
