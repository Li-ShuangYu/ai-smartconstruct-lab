<template>
  <div class="mm-editor">
    <div class="toolbar"><n-button size="small" @click="addNode">+ 新增节点</n-button><n-button size="small" @click="removeNode" style="margin-left:8px">- 删除</n-button><span class="node-count">节点数: {{ nodes.length }}</span></div>
    <div class="canvas"><div v-for="(n, i) in nodes" :key="i" :class="['canvas-node', { selected: selectedIdx === i }]" @click="selectedIdx = i" :style="{ left: n.x + 'px', top: n.y + 'px' }">{{ n.label }}</div></div>
    <div class="hint">节点数需 ≥ {{ minNodes }} 才能提交（当前 {{ nodes.length }}）</div>
    <div class="actions"><n-button @click="handleSave">暂存</n-button><n-button type="primary" :disabled="!canProceed" @click="handleSubmit" style="margin-left:12px">提交导图</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton } from 'naive-ui'
defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const minNodes = 4; const selectedIdx = ref<number | null>(null)
const nodes = ref([{ label: '中心主题', x: 350, y: 200 }, { label: '分支A', x: 200, y: 120 }, { label: '分支B', x: 500, y: 120 }])
const canProceed = computed(() => nodes.value.length >= minNodes)
function addNode() { nodes.value.push({ label: `新节点${nodes.value.length + 1}`, x: 100 + Math.random() * 500, y: 80 + Math.random() * 300 }) }
function removeNode() { if (selectedIdx.value !== null && nodes.value.length > 1) { nodes.value.splice(selectedIdx.value, 1); selectedIdx.value = null } }
function handleSave() { emit('update-data', { nodes: nodes.value }) }
function handleSubmit() { emit('step-complete'); emit('update-data', { nodes: nodes.value }) }
</script>
<style scoped>
.mm-editor { max-width: 800px; margin: 0 auto; padding: 24px; }
.toolbar { display: flex; align-items: center; margin-bottom: 16px; }
.node-count { margin-left: auto; font-size: 13px; color: #64748B; }
.canvas { position: relative; height: 400px; border: 1px solid #E2E8F0; border-radius: 8px; background: #FAFBFC; overflow: hidden; margin-bottom: 12px; }
.canvas-node { position: absolute; padding: 8px 16px; background: #EEF2FF; border: 2px solid #818CF8; border-radius: 8px; cursor: pointer; font-size: 13px; font-weight: 600; transition: all .2s; }
.canvas-node.selected { background: #4F46E5; color: white; }
.hint { color: #64748B; font-size: 13px; text-align: center; margin-bottom: 16px; }
.actions { display: flex; justify-content: center; }
</style>
