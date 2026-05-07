<template>
  <div class="heatmap">
    <h2 class="title">知识点评级</h2>
    <p class="hint">请对以下知识点进行难度评级</p>
    <div v-for="(item, i) in items" :key="i" class="row">
      <span class="label">{{ item }}</span>
      <n-radio-group v-model:value="ratings[i]"><n-radio value="easy">易</n-radio><n-radio value="medium">中</n-radio><n-radio value="hard">难</n-radio></n-radio-group>
    </div>
    <div class="hint">已评级 {{ ratedCount }} / {{ items.length }}</div>
    <div class="actions"><n-button type="primary" :disabled="!canProceed" @click="handleSubmit">提交评级</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton, NRadioGroup, NRadio } from 'naive-ui'
defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const items = ['对称加密算法', '非对称加密算法', '哈希算法', '数字签名', '密钥管理', '安全协议']
const ratings = ref<string[]>(Array(items.length).fill(''))
const ratedCount = computed(() => ratings.value.filter(r => r !== '').length)
const canProceed = computed(() => ratedCount.value === items.length)
function handleSubmit() { emit('step-complete'); emit('update-data', { ratings: ratings.value }) }
</script>
<style scoped>
.heatmap { max-width: 600px; margin: 0 auto; padding: 24px; }
.title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 8px 0; }
.hint { color: #64748B; margin: 0 0 16px 0; }
.row { display: flex; align-items: center; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #F1F5F9; }
.label { font-weight: 600; color: #1E293B; }
.actions { display: flex; justify-content: center; margin-top: 20px; }
</style>
