<template>
  <div class="plan-upload">
    <section class="plan-upload__panel plan-upload__panel--form">
      <div class="plan-upload__header">
        <div>
          <p class="plan-upload__eyebrow">方案上传</p>
          <h2 class="plan-upload__title">{{ pageTitle }}</h2>
        </div>
        <button class="plan-upload__next-btn" @click="handleNextNode">进入下一节点</button>
      </div>

      <div
        class="plan-upload__dropzone"
        :class="{ 'plan-upload__dropzone--active': isDragging }"
        @dragover.prevent="isDragging = true"
        @dragleave="isDragging = false"
        @drop.prevent="handleDrop"
      >
        <input
          type="file"
          class="plan-upload__file-input"
          accept=".doc,.docx,.pdf,.pptx"
          @change="handleFileSelect"
        />
        <div class="plan-upload__dropzone-icon">DOC</div>
        <p class="plan-upload__dropzone-text">拖拽方案文件到这里，或点击选择文件</p>
        <p class="plan-upload__dropzone-hint">支持 .doc / .docx / .pdf / .pptx</p>
      </div>

      <div v-if="uploadedFile" class="plan-upload__file-info">
        <div class="plan-upload__file-meta">
          <span class="plan-upload__file-name">{{ uploadedFile.name }}</span>
          <span class="plan-upload__file-size">{{ formatFileSize(uploadedFile.size) }}</span>
        </div>
        <button class="plan-upload__file-remove" @click="removeFile">移除</button>
      </div>

      <div class="plan-upload__actions">
        <button
          class="plan-upload__submit-btn"
          :disabled="!uploadedFile || submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : hasSubmitted ? '重新提交方案' : '提交方案并生成 AI 评审' }}
        </button>
      </div>

      <div class="plan-upload__note">
        <span class="plan-upload__note-label">演示模式</span>
        上传后会自动生成一段 AI 智能评审内容；不上传也可以直接进入下一节点。
      </div>
    </section>

    <section class="plan-upload__panel plan-upload__panel--review">
      <div class="plan-upload__review-head">
        <div>
          <p class="plan-upload__eyebrow">AI 智能评审</p>
          <h3 class="plan-upload__section-title">可行性分析结果</h3>
        </div>
        <span class="plan-upload__status" :class="reviewStatusClass">{{ reviewStatusText }}</span>
      </div>

      <div v-if="reviewLoading" class="plan-upload__review-loading">
        <div class="plan-upload__loading-spinner"></div>
        <span>{{ loadingText }}</span>
      </div>

      <template v-else-if="reviewStarted">
        <div class="plan-upload__score-grid">
          <div v-for="dimension in mockDimensions" :key="dimension.name" class="plan-upload__score-item">
            <div class="plan-upload__score-header">
              <span>{{ dimension.name }}</span>
              <strong>{{ dimension.score }}</strong>
            </div>
            <div class="plan-upload__score-bar">
              <div class="plan-upload__score-fill" :style="{ width: `${dimension.score}%` }"></div>
            </div>
          </div>
        </div>

        <div class="plan-upload__stream-card">
          <h4 class="plan-upload__stream-title">评审意见</h4>
          <p class="plan-upload__stream-text">{{ streamedReview }}<span v-if="streaming" class="plan-upload__cursor"></span></p>
        </div>
      </template>

      <div v-else class="plan-upload__empty">
        <h4 class="plan-upload__empty-title">等待方案上传</h4>
        <p class="plan-upload__empty-text">提交方案后，这里会模拟 AI 加载并流式输出评审内容。</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref } from 'vue'

interface PlanUploadConfig {
  display?: {
    title?: string
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number | string
  nodeConfig: PlanUploadConfig
}>()

const emit = defineEmits<{
  complete: []
}>()

const isDragging = ref(false)
const uploadedFile = ref<File | null>(null)
const submitting = ref(false)
const hasSubmitted = ref(false)
const reviewStarted = ref(false)
const reviewLoading = ref(false)
const streaming = ref(false)
const streamedReview = ref('')
const loadingText = ref('AI 正在读取方案结构...')

let loadingTimer: number | undefined
let streamTimer: number | undefined

const pageTitle = computed(() => props.nodeConfig.display?.title || '方案文档上传')

const mockDimensions = [
  { name: '需求匹配度', score: 88 },
  { name: '技术可行性', score: 84 },
  { name: '实施完整性', score: 79 },
  { name: '风险控制', score: 76 },
  { name: '表达清晰度', score: 91 }
]

const mockReview = [
  '该方案整体结构清晰，能围绕实训目标展开，需求描述、技术路线和交付物之间的对应关系较明确。',
  '技术实现部分具备可操作性，建议进一步补充关键模块的输入输出边界，以及异常场景下的处理策略。',
  '进度安排基本合理，但风险控制还可以更具体，例如增加数据准备失败、接口不可用、模型响应超时等场景的备用方案。',
  '综合判断：方案可进入下一阶段实施。建议在编码实训前，把核心函数、测试样例和验收标准再压实一轮。'
].join('\n\n')

const reviewStatusText = computed(() => {
  if (reviewLoading || streaming.value) return '评审中'
  if (reviewStarted.value) return '已完成'
  return '待提交'
})

const reviewStatusClass = computed(() => ({
  'plan-upload__status--loading': reviewLoading.value || streaming.value,
  'plan-upload__status--done': reviewStarted.value && !reviewLoading.value && !streaming.value
}))

function handleDrop(event: DragEvent) {
  isDragging.value = false
  const file = event.dataTransfer?.files?.[0]
  if (file) uploadedFile.value = file
}

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) uploadedFile.value = file
}

function removeFile() {
  uploadedFile.value = null
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

async function handleSubmit() {
  if (!uploadedFile.value || submitting.value) return

  submitting.value = true
  hasSubmitted.value = true
  reviewStarted.value = true
  reviewLoading.value = true
  streaming.value = false
  streamedReview.value = ''
  loadingText.value = 'AI 正在读取方案结构...'
  clearTimers()

  window.setTimeout(() => {
    loadingText.value = 'AI 正在生成可行性评审...'
  }, 700)

  loadingTimer = window.setTimeout(() => {
    reviewLoading.value = false
    startMockStream()
  }, 1400)
}

function startMockStream() {
  streaming.value = true
  let index = 0
  streamTimer = window.setInterval(() => {
    streamedReview.value = mockReview.slice(0, index)
    index += 2
    if (index > mockReview.length) {
      streamedReview.value = mockReview
      streaming.value = false
      submitting.value = false
      clearTimers()
    }
  }, 24)
}

function handleNextNode() {
  emit('complete')
}

function clearTimers() {
  if (loadingTimer) {
    window.clearTimeout(loadingTimer)
    loadingTimer = undefined
  }
  if (streamTimer) {
    window.clearInterval(streamTimer)
    streamTimer = undefined
  }
}

onUnmounted(() => {
  clearTimers()
})
</script>

<style scoped>
.plan-upload {
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(0, 1.05fr);
  gap: var(--spacing-lg, 24px);
  width: 100%;
  height: 100%;
  min-height: 0;
  padding: var(--spacing-lg, 24px);
  overflow: hidden;
  box-sizing: border-box;
  background: var(--color-background, #f8fafc);
}

.plan-upload__panel {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md, 16px);
  padding: var(--spacing-lg, 24px);
  overflow: hidden;
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 8px);
}

.plan-upload__header,
.plan-upload__review-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--spacing-md, 16px);
  flex-shrink: 0;
}

.plan-upload__eyebrow {
  margin: 0 0 4px;
  font-size: 12px;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.plan-upload__title,
.plan-upload__section-title {
  margin: 0;
  font-size: 20px;
  line-height: 1.3;
  color: var(--color-heading, #1e293b);
}

.plan-upload__next-btn,
.plan-upload__submit-btn {
  border: none;
  border-radius: var(--radius-md, 8px);
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.plan-upload__next-btn {
  flex-shrink: 0;
  padding: 10px 16px;
  color: var(--color-white, #ffffff);
  background: var(--color-success, #22c55e);
}

.plan-upload__submit-btn {
  width: 100%;
  padding: 13px 16px;
  color: var(--color-white, #ffffff);
  background: var(--color-primary-600, #4f46e5);
}

.plan-upload__next-btn:hover,
.plan-upload__submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.plan-upload__submit-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.plan-upload__dropzone {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 210px;
  padding: var(--spacing-lg, 24px);
  text-align: center;
  border: 2px dashed var(--color-gray-300, #cbd5e1);
  border-radius: var(--radius-md, 8px);
  background: var(--color-gray-50, #f8fafc);
  transition: border-color 0.15s ease, background 0.15s ease;
}

.plan-upload__dropzone--active {
  border-color: var(--color-primary-500, #6366f1);
  background: var(--color-primary-50, #eef2ff);
}

.plan-upload__file-input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.plan-upload__dropzone-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  margin-bottom: var(--spacing-md, 16px);
  border-radius: var(--radius-md, 8px);
  color: var(--color-primary-700, #4338ca);
  background: var(--color-primary-100, #e0e7ff);
  font-size: 13px;
  font-weight: 800;
}

.plan-upload__dropzone-text {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--color-gray-700, #334155);
}

.plan-upload__dropzone-hint,
.plan-upload__note,
.plan-upload__empty-text {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--color-gray-500, #64748b);
}

.plan-upload__file-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-md, 16px);
  padding: 12px 14px;
  border-radius: var(--radius-md, 8px);
  background: var(--color-gray-50, #f8fafc);
}

.plan-upload__file-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.plan-upload__file-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 700;
  color: var(--color-gray-700, #334155);
}

.plan-upload__file-size {
  font-size: 12px;
  color: var(--color-gray-400, #94a3b8);
}

.plan-upload__file-remove {
  flex-shrink: 0;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-sm, 6px);
  padding: 6px 10px;
  background: var(--color-white, #ffffff);
  color: var(--color-gray-600, #475569);
  cursor: pointer;
}

.plan-upload__actions {
  margin-top: auto;
}

.plan-upload__note {
  padding: 12px;
  border-radius: var(--radius-md, 8px);
  background: var(--color-primary-50, #eef2ff);
}

.plan-upload__note-label {
  font-weight: 800;
  color: var(--color-primary-700, #4338ca);
}

.plan-upload__status {
  flex-shrink: 0;
  padding: 5px 10px;
  border-radius: var(--radius-sm, 6px);
  font-size: 12px;
  font-weight: 800;
  color: var(--color-gray-600, #475569);
  background: var(--color-gray-100, #f1f5f9);
}

.plan-upload__status--loading {
  color: var(--color-warning, #f59e0b);
  background: var(--color-warning-50, #fffbeb);
}

.plan-upload__status--done {
  color: var(--color-success-700, #15803d);
  background: var(--color-success-50, #f0fdf4);
}

.plan-upload__review-loading,
.plan-upload__empty {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: var(--spacing-md, 16px);
  border: 1px dashed var(--color-gray-300, #cbd5e1);
  border-radius: var(--radius-md, 8px);
  background: var(--color-gray-50, #f8fafc);
  color: var(--color-gray-600, #475569);
}

.plan-upload__loading-spinner {
  width: 34px;
  height: 34px;
  border: 3px solid var(--color-gray-200, #e2e8f0);
  border-top-color: var(--color-primary-600, #4f46e5);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.plan-upload__score-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--spacing-md, 16px);
  flex-shrink: 0;
}

.plan-upload__score-item {
  padding: 14px;
  border-radius: var(--radius-md, 8px);
  background: var(--color-gray-50, #f8fafc);
}

.plan-upload__score-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--color-gray-700, #334155);
}

.plan-upload__score-bar {
  height: 7px;
  overflow: hidden;
  border-radius: 4px;
  background: var(--color-gray-200, #e2e8f0);
}

.plan-upload__score-fill {
  height: 100%;
  border-radius: 4px;
  background: var(--color-primary-600, #4f46e5);
  transition: width 0.3s ease;
}

.plan-upload__stream-card {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 16px;
  border-radius: var(--radius-md, 8px);
  background: var(--color-gray-50, #f8fafc);
}

.plan-upload__stream-title,
.plan-upload__empty-title {
  margin: 0 0 10px;
  font-size: 15px;
  color: var(--color-heading, #1e293b);
}

.plan-upload__stream-text {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
  font-size: 14px;
  color: var(--color-gray-700, #334155);
}

.plan-upload__cursor {
  display: inline-block;
  width: 7px;
  height: 1em;
  margin-left: 2px;
  vertical-align: -2px;
  background: var(--color-primary-600, #4f46e5);
  animation: blink 1s infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

@media (max-width: 900px) {
  .plan-upload {
    grid-template-columns: 1fr;
    overflow: auto;
  }

  .plan-upload__panel {
    overflow: visible;
  }
}
</style>
