<template>
  <div class="requirement-cloud">
    <!-- Upload Section -->
    <section class="requirement-cloud__upload">
      <h2 class="requirement-cloud__title">需求文档上传</h2>
      <div
        class="requirement-cloud__dropzone"
        :class="{ 'requirement-cloud__dropzone--active': isDragging }"
        @dragover.prevent="isDragging = true"
        @dragleave="isDragging = false"
        @drop.prevent="handleDrop"
      >
        <div class="requirement-cloud__dropzone-icon">📄</div>
        <p class="requirement-cloud__dropzone-text">拖拽文件到此处，或点击上传</p>
        <p class="requirement-cloud__dropzone-hint">支持 .doc, .docx, .pdf, .txt 格式</p>
        <input
          type="file"
          class="requirement-cloud__file-input"
          accept=".doc,.docx,.pdf,.txt"
          @change="handleFileSelect"
        />
      </div>
      <div v-if="uploadedFile" class="requirement-cloud__file-info">
        <span class="requirement-cloud__file-name">{{ uploadedFile.name }}</span>
        <span class="requirement-cloud__file-size">{{ formatFileSize(uploadedFile.size) }}</span>
        <button class="requirement-cloud__file-remove" @click="removeFile">✕</button>
      </div>
    </section>

    <!-- AI Review Feedback -->
    <section v-if="aiReviewResult" class="requirement-cloud__review">
      <h3 class="requirement-cloud__section-title">
        <span class="requirement-cloud__ai-badge">AI</span>
        需求评审反馈
      </h3>
      <div class="requirement-cloud__review-content">
        <div class="requirement-cloud__review-score">
          <span class="requirement-cloud__score-value">{{ aiReviewResult.score }}</span>
          <span class="requirement-cloud__score-label">综合评分</span>
        </div>
        <div class="requirement-cloud__review-details">
          <div
            v-for="item in aiReviewResult.dimensions"
            :key="item.name"
            class="requirement-cloud__dimension"
          >
            <span class="requirement-cloud__dimension-name">{{ item.name }}</span>
            <div class="requirement-cloud__dimension-bar">
              <div
                class="requirement-cloud__dimension-fill"
                :style="{ width: `${item.score}%` }"
              ></div>
            </div>
            <span class="requirement-cloud__dimension-score">{{ item.score }}%</span>
          </div>
        </div>
        <p class="requirement-cloud__review-comment">{{ aiReviewResult.comment }}</p>
      </div>
    </section>

    <!-- Word Cloud -->
    <section class="requirement-cloud__wordcloud">
      <h3 class="requirement-cloud__section-title">关键词云</h3>
      <div class="requirement-cloud__cloud-container">
        <span
          v-for="word in wordCloudData"
          :key="word.text"
          class="requirement-cloud__word"
          :style="{ fontSize: `${word.size}px`, opacity: word.weight }"
        >
          {{ word.text }}
        </span>
      </div>
    </section>

    <!-- History -->
    <section class="requirement-cloud__history">
      <h3 class="requirement-cloud__section-title">提交历史</h3>
      <div class="requirement-cloud__history-list">
        <div
          v-for="record in historyRecords"
          :key="record.id"
          class="requirement-cloud__history-item"
        >
          <span class="requirement-cloud__history-time">{{ record.submittedAt }}</span>
          <span class="requirement-cloud__history-file">{{ record.fileName }}</span>
          <span class="requirement-cloud__history-score">{{ record.score }}分</span>
        </div>
      </div>
    </section>

    <!-- Submit Button -->
    <section class="requirement-cloud__footer">
      <button
        class="requirement-cloud__submit-btn"
        :disabled="!uploadedFile"
        @click="handleSubmit"
      >
        提交需求文档
      </button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface ReviewDimension {
  name: string
  score: number
}

interface AiReviewResult {
  score: number
  dimensions: ReviewDimension[]
  comment: string
}

interface WordCloudItem {
  text: string
  size: number
  weight: number
}

interface HistoryRecord {
  id: string
  submittedAt: string
  fileName: string
  score: number
}

interface RequirementCloudConfig {
  display?: {
    title?: string
  }
  ai_processing?: {
    enable_ai_pre_review?: boolean
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: RequirementCloudConfig
}>()

const emit = defineEmits<{
  complete: []
}>()

const isDragging = ref<boolean>(false)
const uploadedFile = ref<File | null>(null)

/** Placeholder AI review result */
const aiReviewResult = computed<AiReviewResult | null>(() => ({
  score: 82,
  dimensions: [
    { name: '完整性', score: 85 },
    { name: '清晰度', score: 78 },
    { name: '可行性', score: 90 },
    { name: '规范性', score: 75 }
  ],
  comment: '需求文档整体结构清晰，功能描述较为完整。建议补充非功能性需求描述，并细化用户角色权限矩阵。'
}))

/** Placeholder word cloud data */
const wordCloudData = computed<WordCloudItem[]>(() => [
  { text: '用户管理', size: 24, weight: 1 },
  { text: '数据分析', size: 20, weight: 0.9 },
  { text: '权限控制', size: 18, weight: 0.85 },
  { text: '接口设计', size: 22, weight: 0.95 },
  { text: '性能优化', size: 16, weight: 0.75 },
  { text: '安全认证', size: 19, weight: 0.88 },
  { text: '日志审计', size: 14, weight: 0.65 },
  { text: '消息通知', size: 17, weight: 0.8 },
  { text: '文件存储', size: 15, weight: 0.7 },
  { text: '报表导出', size: 16, weight: 0.72 }
])

/** Placeholder history records */
const historyRecords = computed<HistoryRecord[]>(() => [
  { id: 'h1', submittedAt: '2025-03-10 14:30', fileName: '需求分析v2.docx', score: 82 },
  { id: 'h2', submittedAt: '2025-03-08 09:15', fileName: '需求分析v1.docx', score: 68 }
])

function handleDrop(event: DragEvent) {
  isDragging.value = false
  const files = event.dataTransfer?.files
  if (files && files.length > 0) {
    const file = files[0]
    if (file) uploadedFile.value = file
  }
}

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    const file = target.files[0]
    if (file) uploadedFile.value = file
  }
}

function removeFile() {
  uploadedFile.value = null
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function handleSubmit() {
  if (!uploadedFile.value) return
  emit('complete')
}
</script>

<style scoped>
.requirement-cloud {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
}

.requirement-cloud__title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-md, 1rem);
}

.requirement-cloud__upload {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.requirement-cloud__dropzone {
  border: 2px dashed var(--color-gray-300, #cbd5e1);
  border-radius: var(--radius-md, 0.5rem);
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.requirement-cloud__dropzone--active {
  border-color: var(--color-primary-400, #818cf8);
  background: var(--color-primary-50, #eef2ff);
}

.requirement-cloud__dropzone-icon {
  font-size: 2.5rem;
  margin-bottom: 0.75rem;
}

.requirement-cloud__dropzone-text {
  font-size: 0.9375rem;
  color: var(--color-gray-600, #475569);
  font-weight: 500;
}

.requirement-cloud__dropzone-hint {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
  margin-top: 0.25rem;
}

.requirement-cloud__file-input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.requirement-cloud__file-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-top: var(--spacing-sm, 0.75rem);
  padding: 0.5rem 0.75rem;
  background: var(--color-gray-50, #f8fafc);
  border-radius: var(--radius-sm, 0.25rem);
}

.requirement-cloud__file-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-gray-700, #334155);
  flex: 1;
}

.requirement-cloud__file-size {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
}

.requirement-cloud__file-remove {
  background: none;
  border: none;
  color: var(--color-gray-400, #94a3b8);
  cursor: pointer;
  font-size: 0.875rem;
}

.requirement-cloud__section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-md, 1rem);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.requirement-cloud__ai-badge {
  font-size: 0.625rem;
  font-weight: 700;
  padding: 0.125rem 0.375rem;
  border-radius: var(--radius-sm, 0.25rem);
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
}

.requirement-cloud__review {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.requirement-cloud__review-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md, 1rem);
}

.requirement-cloud__review-score {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
}

.requirement-cloud__score-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.requirement-cloud__score-label {
  font-size: 0.875rem;
  color: var(--color-gray-500, #64748b);
}

.requirement-cloud__review-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.requirement-cloud__dimension {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.requirement-cloud__dimension-name {
  font-size: 0.8125rem;
  color: var(--color-gray-600, #475569);
  width: 4rem;
  flex-shrink: 0;
}

.requirement-cloud__dimension-bar {
  flex: 1;
  height: 6px;
  background: var(--color-gray-100, #f1f5f9);
  border-radius: 3px;
  overflow: hidden;
}

.requirement-cloud__dimension-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.requirement-cloud__dimension-score {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-gray-600, #475569);
  width: 2.5rem;
  text-align: right;
}

.requirement-cloud__review-comment {
  font-size: 0.875rem;
  color: var(--color-gray-600, #475569);
  line-height: 1.6;
  padding: 0.75rem;
  background: var(--color-gray-50, #f8fafc);
  border-radius: var(--radius-md, 0.5rem);
  border-left: 3px solid var(--color-primary-300, #a5b4fc);
}

.requirement-cloud__wordcloud {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.requirement-cloud__cloud-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  justify-content: center;
  align-items: center;
  min-height: 120px;
  padding: var(--spacing-md, 1rem);
}

.requirement-cloud__word {
  color: var(--color-primary-600, #4f46e5);
  font-weight: 600;
  cursor: default;
  transition: transform 0.15s ease;
}

.requirement-cloud__word:hover {
  transform: scale(1.1);
}

.requirement-cloud__history {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.requirement-cloud__history-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.requirement-cloud__history-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md, 1rem);
  padding: 0.5rem 0.75rem;
  background: var(--color-gray-50, #f8fafc);
  border-radius: var(--radius-sm, 0.25rem);
}

.requirement-cloud__history-time {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
}

.requirement-cloud__history-file {
  font-size: 0.875rem;
  color: var(--color-gray-700, #334155);
  flex: 1;
}

.requirement-cloud__history-score {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-primary-600, #4f46e5);
}

.requirement-cloud__footer {
  padding-top: var(--spacing-md, 1rem);
}

.requirement-cloud__submit-btn {
  width: 100%;
  padding: 0.875rem;
  font-size: 1rem;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  border: none;
  border-radius: var(--radius-lg, 0.75rem);
  cursor: pointer;
  transition: all 0.2s ease;
}

.requirement-cloud__submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px -4px rgba(99, 102, 241, 0.4);
}

.requirement-cloud__submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
