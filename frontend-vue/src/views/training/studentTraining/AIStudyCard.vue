<template>
  <div class="study-card">
    <h2 class="title">{{ config?.topic || '理论学习' }}</h2>
    <n-card class="topic-card"><p>本次理论实训聚焦于 <b>{{ config?.topic || '密码学基础理论' }}</b>，请浏览以下知识点并与 AI 互动。</p></n-card>
    <div class="chat-box">
      <div class="chat-messages" ref="chatRef">
        <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role]"><span>{{ m.text }}</span></div>
      </div>
      <div class="chat-input"><n-input v-model:value="input" placeholder="向 AI 助教提问..." @keyup.enter="sendMsg" /><n-button size="small" @click="sendMsg" style="margin-left:8px">发送</n-button></div>
    </div>
    <div class="hint">已对话 {{ chatRounds }} 轮（需 {{ minRounds }} 轮）</div>
    <div class="actions"><n-button type="primary" :disabled="!canProceed" @click="handleNext">学习完成，下一步</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { NButton, NCard, NInput } from 'naive-ui'
const props = defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const minRounds = 3; const chatRounds = ref(0); const input = ref(''); const chatRef = ref<HTMLElement | null>(null)
const messages = ref<{ role: string; text: string }[]>([{ role: 'ai', text: `你好！我是 AI 助教，关于「${props.config?.topic || '当前主题'}」有什么想了解的吗？` }])
const canProceed = computed(() => chatRounds.value >= minRounds)
function sendMsg() { if (!input.value.trim()) return; messages.value.push({ role: 'user', text: input.value }); chatRounds.value++; input.value = ''; emit('update-data', { chatRounds: chatRounds.value })
  setTimeout(() => { messages.value.push({ role: 'ai', text: `关于「${props.config?.topic || '这个问题'}」，我的理解是...（模拟回复 ${chatRounds.value}/${minRounds}）` }); nextTick(() => { if (chatRef.value) chatRef.value.scrollTop = chatRef.value.scrollHeight }) }, 600) }
function handleNext() { emit('step-complete') }
</script>
<style scoped>
.study-card { max-width: 700px; margin: 0 auto; padding: 24px; }
.title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 16px 0; }
.topic-card { margin-bottom: 16px; }
.chat-box { border: 1px solid #E2E8F0; border-radius: 12px; overflow: hidden; margin-bottom: 12px; }
.chat-messages { height: 240px; overflow-y: auto; padding: 12px; background: #F8FAFC; }
.msg { margin-bottom: 8px; padding: 8px 12px; border-radius: 8px; max-width: 85%; font-size: 13px; line-height: 1.5; }
.msg.ai { background: #EEF2FF; color: #4F46E5; }
.msg.user { background: #4F46E5; color: white; margin-left: auto; }
.chat-input { display: flex; padding: 8px; background: white; }
.hint { color: #64748B; font-size: 13px; text-align: center; margin-bottom: 16px; }
.actions { display: flex; justify-content: center; }
</style>
