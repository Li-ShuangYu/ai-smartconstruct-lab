<template>
  <div class="viewer">
    <div class="top-bar"><h2>{{ config?.resourceName || '教学资源' }}</h2><span class="progress-text">已读 {{ readProgress }}%</span></div>
    <div class="content-area">
      <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <button class="toggle-btn" @click="sidebarCollapsed = !sidebarCollapsed">{{ sidebarCollapsed ? '▶' : '◀' }}</button>
        <div v-if="!sidebarCollapsed" class="outline">
          <p v-for="i in 5" :key="i" class="outline-item" @click="simulateRead">第{{ i }}节</p>
        </div>
      </div>
      <div class="doc-viewer">
        <div class="doc-page"><span class="doc-icon">📄</span><p>资源内容展示区域</p><p class="doc-hint">点击左侧目录模拟阅读进度</p></div>
      </div>
    </div>
    <div class="actions"><n-button type="primary" :disabled="!canProceed" @click="handleNext">阅读完成，下一步</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton } from 'naive-ui'
defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const readProgress = ref(0); const sidebarCollapsed = ref(false)
const canProceed = computed(() => readProgress.value >= 100)
function simulateRead() { readProgress.value = Math.min(100, readProgress.value + 20); emit('update-data', { readProgress: readProgress.value }); if (readProgress.value >= 100) emit('step-complete') }
function handleNext() { emit('step-complete') }
</script>
<style scoped>
.viewer { display: flex; flex-direction: column; height: 100%; }
.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; border-bottom: 1px solid #E2E8F0; }
.top-bar h2 { font-size: 18px; font-weight: 700; margin: 0; }
.progress-text { color: #4F46E5; font-weight: 700; }
.content-area { flex: 1; display: flex; overflow: hidden; }
.sidebar { width: 200px; border-right: 1px solid #E2E8F0; background: #F8FAFC; transition: width .2s; position: relative; flex-shrink: 0; }
.sidebar.collapsed { width: 30px; }
.toggle-btn { position: absolute; right: 2px; top: 8px; background: none; border: none; cursor: pointer; font-size: 12px; }
.outline { padding: 12px; }
.outline-item { padding: 8px; cursor: pointer; border-radius: 6px; font-size: 13px; color: #475569; }
.outline-item:hover { background: #EEF2FF; }
.doc-viewer { flex: 1; display: flex; align-items: center; justify-content: center; }
.doc-page { text-align: center; color: #64748B; }
.doc-icon { font-size: 48px; display: block; margin-bottom: 16px; }
.doc-hint { font-size: 13px; }
.actions { display: flex; justify-content: center; padding: 16px; border-top: 1px solid #E2E8F0; }
</style>
