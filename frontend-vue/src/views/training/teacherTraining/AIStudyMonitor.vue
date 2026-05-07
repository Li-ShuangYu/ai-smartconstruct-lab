<template>
  <div class="monitor"><h3 class="title">AI 对话监控</h3>
    <div v-if="questions && questions.length" class="feed"><div v-for="q in questions" :key="q.id" class="q-card"><p>{{ q.studentName }}: {{ q.content }}</p><n-button size="tiny" type="primary" @click="$emit('intervene','broadcast',{question:q})">全班广播</n-button></div></div>
    <n-empty v-else description="暂无提问数据" />
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { NButton, NEmpty } from 'naive-ui'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const questions = computed(() => (props.liveData?.questions as any[]) || [])
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.feed{max-height:360px;overflow-y:auto}.q-card{padding:10px;border-bottom:1px solid #F1F5F9;display:flex;justify-content:space-between;align-items:center}.q-card p{margin:0;font-size:13px;color:#475569;flex:1}</style>
