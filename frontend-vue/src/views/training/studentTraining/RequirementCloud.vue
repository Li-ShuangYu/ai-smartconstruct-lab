<template>
  <div class="req-cloud">
    <h2 class="title">需求上传</h2>
    <n-card class="scenario-card"><p>{{ config?.scenario || '请描述业务需求场景' }}</p></n-card>
    <n-input type="textarea" v-model:value="content" placeholder="请输入需求内容..." :autosize="{ minRows: 5, maxRows: 12 }" style="margin-bottom:16px" />
    <div v-if="context?.groupId" class="group-tag"><n-tag>组 {{ context.groupId }}</n-tag></div>
    <div class="wordcloud-placeholder"><span>☁️ 动态词云占位区域</span></div>
    <div class="actions"><n-button type="primary" :disabled="!canProceed" @click="handleSubmit" :loading="submitting">提交需求</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton, NCard, NInput, NTag } from 'naive-ui'
defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const content = ref(''); const submitting = ref(false)
const canProceed = computed(() => content.value.trim().length > 0)
function handleSubmit() { submitting.value = true; setTimeout(() => { submitting.value = false; emit('step-complete'); emit('update-data', { content: content.value }) }, 500) }
</script>
<style scoped>
.req-cloud { max-width: 700px; margin: 0 auto; padding: 24px; }
.title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 16px 0; }
.scenario-card { margin-bottom: 16px; } .scenario-card p { color: #475569; }
.group-tag { margin-bottom: 12px; }
.wordcloud-placeholder { height: 100px; background: #F8FAFC; border: 1px dashed #CBD5E1; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #94A3B8; margin-bottom: 16px; }
.actions { display: flex; justify-content: center; }
</style>
