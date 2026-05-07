<template>
  <div class="mindmap-preview">
    <h2 class="title">思维导图预习</h2>
    <p class="hint">{{ config?.scenario || '请浏览下方思维导图，点击所有节点以完成预习。' }}</p>
    <div class="layout">
      <div class="tree-panel"><n-tree :data="treeData" block-line @update:selected-keys="onNodeClick" /></div>
      <div class="detail-panel"><p v-if="detailNode">详情: {{ detailNode }}</p><p v-else>点击左侧节点查看详情</p></div>
    </div>
    <div class="hint">已浏览 {{ visitedCount }} / {{ totalCount }} 个节点</div>
    <div class="actions"><n-button type="primary" :disabled="!canProceed" @click="handleNext">预习完成，下一步</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton, NTree } from 'naive-ui'
defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const visited = ref(new Set<string>()); const detailNode = ref(''); const totalCount = 6
const treeData = [{ label: '密码学基础', key: '1', children: [{ label: '对称加密', key: '1-1' }, { label: '非对称加密', key: '1-2' }, { label: '哈希算法', key: '1-3' }] }, { label: '工程实践', key: '2', children: [{ label: '密钥管理', key: '2-1' }, { label: '安全协议', key: '2-2' }] }]
const visitedCount = computed(() => visited.value.size)
const canProceed = computed(() => visitedCount.value >= totalCount)
function onNodeClick(keys: string[]) { keys.forEach(k => { visited.value.add(k); detailNode.value = k }) }
function handleNext() { emit('step-complete'); emit('update-data', { visited: [...visited.value] }) }
</script>
<style scoped>
.mindmap-preview { max-width: 800px; margin: 0 auto; padding: 24px; }
.title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 8px 0; }
.hint { color: #64748B; margin: 0 0 16px 0; }
.layout { display: flex; gap: 16px; height: 320px; margin-bottom: 16px; }
.tree-panel { flex: 1; border: 1px solid #E2E8F0; border-radius: 8px; padding: 12px; overflow-y: auto; }
.detail-panel { flex: 1; border: 1px solid #E2E8F0; border-radius: 8px; padding: 16px; display: flex; align-items: center; justify-content: center; color: #64748B; }
.actions { display: flex; justify-content: center; }
</style>
