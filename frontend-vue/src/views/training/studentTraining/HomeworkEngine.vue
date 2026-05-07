<template>
  <div class="homework">
    <div class="countdown-bar" v-if="remaining > 0"><span>剩余时间</span><span class="time">{{ formatTime(remaining) }}</span></div>
    <div v-else class="timeout-bar"><n-alert type="warning">时间到！系统已自动交卷</n-alert></div>
    <div v-for="(q, qi) in questionList" :key="qi" class="question">
      <p class="q-title">{{ qi + 1 }}. {{ q.title }}</p>
      <n-radio-group v-if="q.type === 'single'" v-model:value="answers[qi]"><n-space vertical><n-radio v-for="o in q.options" :key="o" :value="o">{{ o }}</n-radio></n-space></n-radio-group>
      <n-checkbox-group v-else-if="q.type === 'multi'" v-model:value="multiAnswers[qi]"><n-space vertical><n-checkbox v-for="o in q.options" :key="o" :value="o">{{ o }}</n-checkbox></n-space></n-checkbox-group>
      <n-input v-else v-model:value="fillAnswers[qi]" placeholder="请输入答案" />
    </div>
    <div class="actions"><n-button @click="handleSave">暂存</n-button><n-button type="primary" :disabled="!canProceed" @click="handleSubmit" style="margin-left:12px">交卷</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { NButton, NRadioGroup, NRadio, NCheckboxGroup, NCheckbox, NInput, NSpace, NAlert } from 'naive-ui'
const props = defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const mins = (props.config as any)?.time_limit_mins || 10
const count = Math.min((props.config as any)?.questionCount || 5, 5)
const remaining = ref(mins * 60); const submitted = ref(false); const answers = ref<string[]>([]); const multiAnswers = ref<string[][]>([]); const fillAnswers = ref<string[]>([])
let timer: any
const canProceed = computed(() => submitted.value || remaining.value <= 0)
const questionList = Array.from({ length: count }, (_, i) => ({ title: ['加密算法的核心目标不包括？', '对称加密的特点是？', '什么是数字签名？', '区块链的核心技术是？', '什么是对称密钥？'][i] || `题目${i + 1}`, type: i < 3 ? 'single' : i === 3 ? 'multi' : 'fill', options: ['A. 保密性', 'B. 完整性', 'C. 可用性', 'D. 性能'] }))
function formatTime(s: number) { const m = Math.floor(s / 60), sec = s % 60; return `${m}:${sec.toString().padStart(2, '0')}` }
function handleSave() { emit('update-data', { saved: true }) }
function handleSubmit() { submitted.value = true; if (timer) clearInterval(timer); emit('step-complete'); emit('update-data', { answers: answers.value, submitted: true }) }
onMounted(() => { timer = setInterval(() => { if (remaining.value > 0) remaining.value--; else if (!submitted.value) { submitted.value = true; emit('step-complete') } }, 1000) })
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>
<style scoped>
.homework { max-width: 700px; margin: 0 auto; padding: 24px; }
.countdown-bar { display: flex; justify-content: space-between; background: #FEF3C7; padding: 10px 16px; border-radius: 8px; margin-bottom: 20px; font-weight: 600; color: #92400E; }
.time { font-family: monospace; font-size: 20px; font-weight: 800; }
.timeout-bar { margin-bottom: 20px; }
.question { background: #fff; border: 1px solid #E2E8F0; border-radius: 10px; padding: 16px; margin-bottom: 12px; }
.q-title { font-weight: 700; color: #1E293B; margin: 0 0 10px 0; }
.actions { display: flex; justify-content: center; margin-top: 20px; }
</style>
