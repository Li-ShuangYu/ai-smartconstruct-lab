<template>
  <div class="portal">
    <div class="hero"><span class="hero-icon">🚀</span><h2>实训开始</h2></div>
    <n-card class="info-card"><p>{{ config?.desc || '欢迎参加本次实训任务，请仔细阅读以下说明后点击准备就绪。' }}</p></n-card>
    <div class="actions"><n-button type="primary" size="large" :disabled="!canProceed" @click="handleReady" :loading="loading">{{ ready ? '已就绪' : '准备就绪' }}</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton, NCard } from 'naive-ui'
const props = defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const ready = ref(false); const loading = ref(false)
const canProceed = computed(() => ready.value)
function handleReady() { loading.value = true; setTimeout(() => { loading.value = false; ready.value = true; emit('step-complete'); emit('update-data', { ready: true }) }, 600) }
</script>
<style scoped>
.portal { text-align: center; padding: 60px 40px; max-width: 600px; margin: 0 auto; }
.hero { margin-bottom: 32px; } .hero-icon { font-size: 56px; display: block; margin-bottom: 12px; }
.hero h2 { font-size: 26px; font-weight: 800; color: #0F172A; margin: 0; }
.info-card { margin-bottom: 32px; text-align: left; } .info-card p { color: #475569; font-size: 15px; line-height: 1.7; }
.actions { display: flex; justify-content: center; }
</style>
