<template>
  <div class="group-review">
    <h2 class="title">组间互评</h2>
    <p class="hint">评价维度: {{ config?.dimension || '方案可行性, 汇报表现, 创新性' }}</p>
    <n-alert v-if="!context?.isGrouped" type="warning" title="非分组模式" style="margin-bottom:16px">此节点仅在分组模式下可用</n-alert>
    <template v-else>
      <div v-for="(g, gi) in otherGroups" :key="gi" class="group-card">
        <h3>{{ g.name }}</h3>
        <div v-for="(dim, di) in dimensions" :key="di" class="dim-row"><span>{{ dim }}</span><n-rate v-model:value="groupScores[gi][di]" size="medium" /></div>
      </div>
      <div class="hint">已完成 {{ completedCount }} / {{ otherGroups.length }} 组</div>
      <div class="actions"><n-button type="primary" :disabled="!canProceed" @click="handleSubmit">提交互评</n-button></div>
    </template>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton, NRate, NAlert } from 'naive-ui'
const props = defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const dims = (props.config?.dimension || '方案可行性,汇报表现,创新性').split(/[,，]/)
const dimensions = dims.map((d: string) => d.trim())
const otherGroups = [{ name: '第一组' }, { name: '第二组' }, { name: '第四组' }]
const groupScores = ref(otherGroups.map(() => dimensions.map(() => 0)))
const completedCount = computed(() => groupScores.value.filter(s => s.every((v: number) => v > 0)).length)
const canProceed = computed(() => props.context?.isGrouped && completedCount.value >= otherGroups.length)
function handleSubmit() { emit('step-complete'); emit('update-data', { scores: groupScores.value }) }
</script>
<style scoped>
.group-review { max-width: 700px; margin: 0 auto; padding: 24px; }
.title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 8px 0; }
.hint { color: #64748B; margin: 0 0 16px 0; }
.group-card { background: #fff; border: 1px solid #E2E8F0; border-radius: 10px; padding: 16px; margin-bottom: 12px; }
.group-card h3 { margin: 0 0 10px 0; font-size: 15px; }
.dim-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.dim-row span { font-size: 13px; color: #475569; }
.actions { display: flex; justify-content: center; margin-top: 20px; }
</style>
