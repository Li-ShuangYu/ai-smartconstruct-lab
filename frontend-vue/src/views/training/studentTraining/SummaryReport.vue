<template>
  <div class="summary">
    <div class="celebration"><span class="party-icon">🎉</span><h1>恭喜完成实训！</h1></div>
    <n-card class="quote-card"><p>{{ config?.desc || '感谢你完成本次实训任务。你的努力与坚持值得称赞，愿你带着收获的知识与技能继续前行。' }}</p></n-card>
    <div class="actions">
      <n-button type="primary" size="large" @click="handleDownload">下载实训报告</n-button>
    </div>
    <div class="actions" style="margin-top:12px"><n-button @click="handleExit">返回工作台</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { NButton, NCard } from 'naive-ui'
import { useRouter } from 'vue-router'
const props = defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const router = useRouter()
const canProceed = computed(() => true)
function handleDownload() { emit('update-data', { reportDownloaded: true }); emit('step-complete') }
function handleExit() { router.push('/student/workbench') }
</script>
<style scoped>
.summary { max-width: 500px; margin: 0 auto; padding: 60px 24px; text-align: center; }
.celebration { margin-bottom: 24px; } .party-icon { font-size: 64px; display: block; margin-bottom: 12px; }
.celebration h1 { font-size: 28px; font-weight: 800; color: #0F172A; margin: 0; }
.quote-card { margin-bottom: 24px; } .quote-card p { color: #475569; font-size: 15px; line-height: 1.7; }
.actions { display: flex; justify-content: center; }
</style>
