<template>
  <div class="peer-review">
    <h2 class="title">学生互评</h2>
    <p class="hint">评价维度: {{ config?.dimension || '代码规范, 创新性, 完整性' }}</p>
    <div v-for="(p, pi) in peers" :key="pi" class="peer-card">
      <h3>{{ p.name }} - {{ p.work }}</h3>
      <div v-for="(dim, di) in dimensions" :key="di" class="dim-row"><span>{{ dim }}</span><n-rate v-model:value="scores[pi][di]" size="medium" /></div>
      <n-input v-model:value="comments[pi]" type="textarea" placeholder="评语..." size="small" style="margin-top:8px" />
    </div>
    <div class="hint">已完成 {{ completedCount }} / {{ requiredCount }} 份</div>
    <div class="actions"><n-button type="primary" :disabled="!canProceed" @click="handleSubmit">提交互评</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton, NRate, NInput } from 'naive-ui'
const props = defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const dims = (props.config?.dimension || '代码规范,创新性,完整性').split(/[,，]/)
const dimensions = dims.map((d: string) => d.trim())
const requiredCount = 3
const peers = Array.from({ length: requiredCount }, (_, i) => ({ name: `同学${String.fromCharCode(65 + i)}`, work: `作品${i + 1}` }))
const scores = ref(peers.map(() => dimensions.map(() => 0)))
const comments = ref(peers.map(() => ''))
const completedCount = computed(() => scores.value.filter(s => s.every((v: number) => v > 0)).length)
const canProceed = computed(() => completedCount.value >= requiredCount)
function handleSubmit() { emit('step-complete'); emit('update-data', { scores: scores.value, comments: comments.value }) }
</script>
<style scoped>
.peer-review { max-width: 700px; margin: 0 auto; padding: 24px; }
.title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 8px 0; }
.hint { color: #64748B; margin: 0 0 16px 0; }
.peer-card { background: #fff; border: 1px solid #E2E8F0; border-radius: 10px; padding: 16px; margin-bottom: 12px; }
.peer-card h3 { margin: 0 0 10px 0; font-size: 15px; }
.dim-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.dim-row span { font-size: 13px; color: #475569; }
.actions { display: flex; justify-content: center; margin-top: 20px; }
</style>
