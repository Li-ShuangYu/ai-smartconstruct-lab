<template>
  <div class="exam" :class="{ fullscreen: isFullscreen }">
    <div class="countdown-bar"><span>剩余时间</span><span class="time">{{ formatTime(remaining) }}</span></div>
    <n-alert v-if="!isFullscreen" type="warning" title="严肃考试模式" style="margin-bottom:16px">请勿切换页面或退出全屏！</n-alert>
    <div v-for="(q, qi) in questions" :key="qi" class="question">
      <p class="q-title">{{ qi + 1 }}. {{ q.title }}</p>
      <n-radio-group v-model:value="answers[qi]"><n-space vertical><n-radio v-for="o in q.options" :key="o" :value="o">{{ o }}</n-radio></n-space></n-radio-group>
    </div>
    <div class="actions"><n-button type="primary" :disabled="!canProceed" @click="handleSubmit" size="large">交卷</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { NButton, NRadioGroup, NRadio, NSpace, NAlert } from 'naive-ui'
const props = defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const duration = (props.config as any)?.duration || 30
const remaining = ref(duration * 60); const submitted = ref(false); const isFullscreen = ref(true); const answers = ref<string[]>([])
let timer: any
const canProceed = computed(() => submitted.value || remaining.value <= 0)
const questions = Array.from({ length: 5 }, (_, i) => ({ title: ['期末考试第1题', '期末考试第2题', '期末考试第3题', '期末考试第4题', '期末考试第5题'][i], options: ['A', 'B', 'C', 'D'] }))
function formatTime(s: number) { const m = Math.floor(s / 60), sec = s % 60; return `${m}:${sec.toString().padStart(2, '0')}` }
function handleSubmit() { submitted.value = true; if (timer) clearInterval(timer); emit('step-complete'); emit('update-data', { answers: answers.value }) }
onMounted(() => { timer = setInterval(() => { if (remaining.value > 0) remaining.value--; else if (!submitted.value) { submitted.value = true; emit('step-complete') } }, 1000) })
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>
<style scoped>
.exam { max-width: 700px; margin: 0 auto; padding: 24px; } .exam.fullscreen { background: #fff; min-height: 100vh; }
.countdown-bar { display: flex; justify-content: space-between; background: #FEE2E2; padding: 10px 16px; border-radius: 8px; margin-bottom: 20px; font-weight: 600; color: #991B1B; }
.time { font-family: monospace; font-size: 20px; font-weight: 800; }
.question { background: #fff; border: 1px solid #E2E8F0; border-radius: 10px; padding: 16px; margin-bottom: 12px; }
.q-title { font-weight: 700; color: #1E293B; margin: 0 0 10px 0; }
.actions { display: flex; justify-content: center; margin-top: 20px; }
</style>
