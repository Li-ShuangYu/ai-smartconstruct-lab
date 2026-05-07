<template>
  <div class="plan-upload">
    <h2 class="title">方案上传</h2>
    <p class="hint">{{ config?.uploadReq || '请上传方案文件' }}（支持格式: {{ config?.format || 'pdf, docx, zip' }}）</p>
    <div v-if="canUpload" class="upload-area">
      <n-upload :multiple="false" action="#" @change="handleFileChange"><n-button>选择文件</n-button></n-upload>
      <p v-if="fileName" class="file-name">已选择: {{ fileName }}</p>
    </div>
    <div v-else class="wait-area"><n-alert type="info">等待组长 {{ groupLeader }} 提交方案文件</n-alert><div class="preview">文件预览区（组长提交后显示）</div></div>
    <div class="actions"><n-button type="primary" :disabled="!canProceed" @click="handleNext">提交，下一步</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton, NUpload, NAlert } from 'naive-ui'
const props = defineProps<{ config?: Record<string, any>; context?: Record<string, any> }>()
const emit = defineEmits<{ (e: 'step-complete'): void; (e: 'update-data', data: any): void }>()
const fileName = ref(''); const fileUrl = ref('')
const isGrouped = computed(() => props.context?.isGrouped === true)
const isLeader = computed(() => props.context?.isLeader === true)
const canUpload = computed(() => !isGrouped.value || isLeader.value)
const groupLeader = ref('指定组长')
const canProceed = computed(() => !!fileUrl.value || (!canUpload.value))
function handleFileChange(data: any) { const f = data?.file?.file || data?.file; if (f) { fileName.value = f.name || 'file'; fileUrl.value = 'mock-url'; emit('update-data', { fileName: fileName.value, fileUrl: fileUrl.value }); emit('step-complete') } }
function handleNext() { emit('step-complete') }
</script>
<style scoped>
.plan-upload { max-width: 600px; margin: 0 auto; padding: 24px; }
.title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 8px 0; }
.hint { color: #64748B; margin: 0 0 16px 0; }
.upload-area { text-align: center; } .file-name { color: #4F46E5; font-weight: 600; margin-top: 8px; }
.wait-area { text-align: center; } .preview { height: 80px; background: #F8FAFC; border: 1px dashed #CBD5E1; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #94A3B8; margin-top: 12px; }
.actions { display: flex; justify-content: center; margin-top: 24px; }
</style>
