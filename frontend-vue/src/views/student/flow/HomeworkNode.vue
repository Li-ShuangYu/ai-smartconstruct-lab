<template>
  <div class="node-card">
    <div class="icon-wrap">📝</div>
    <h2>{{ config?.name || '作业/考试' }}</h2>
    <div class="countdown">剩余时间 {{ min }}:{{ sec.toString().padStart(2, '0') }}</div>
    <div class="question" v-for="(q, qi) in questions" :key="qi">
      <p class="q-title">题目 {{ qi + 1 }}：{{ q.title }}</p>
      <n-radio-group :name="`q${qi}`">
        <n-space vertical><n-radio v-for="opt in q.options" :key="opt" :value="opt">{{ opt }}</n-radio></n-space>
      </n-radio-group>
    </div>
    <n-button type="primary" style="margin-top:16px" @click="emit('next')">提交答案，下一步</n-button>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { NButton, NRadioGroup, NRadio, NSpace } from 'naive-ui'
const props = defineProps<{ config?: Record<string, any>; taskId?: number }>()
const emit = defineEmits<{ next: [] }>()
const totalSec = ref((props.config?.config?.time_limit_mins || 10) * 60)
const min = ref(Math.floor(totalSec.value / 60))
const sec = ref(totalSec.value % 60)
let timer: any
onMounted(() => { timer = setInterval(() => { if (totalSec.value > 0) { totalSec.value--; min.value = Math.floor(totalSec.value / 60); sec.value = totalSec.value % 60 } }, 1000) })
onUnmounted(() => { if (timer) clearInterval(timer) })
const questions = [
  { title: '下列哪个属于对称加密算法？', options: ['A. RSA', 'B. SM4', 'C. ECC', 'D. DH'] },
  { title: '分组密码的工作模式不包括？', options: ['A. ECB', 'B. CBC', 'C. CTR', 'D. HTTP'] }
]
</script>
<style scoped>
.node-card { text-align: center; padding: 40px; background: #fff; border-radius: 16px; border: 1px solid #E2E8F0; max-width: 560px; margin: 0 auto; }
.icon-wrap { font-size: 48px; margin-bottom: 16px; }
h2 { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 12px 0; }
.countdown { background: #FEF3C7; padding: 8px 16px; border-radius: 8px; font-weight: 800; color: #92400E; font-family: monospace; font-size: 20px; display: inline-block; margin-bottom: 16px; }
.question { background: #F8FAFC; border-radius: 10px; padding: 16px; margin-top: 12px; text-align: left; }
.q-title { font-weight: 700; color: #1E293B; font-size: 14px; margin: 0 0 10px 0; }
</style>
