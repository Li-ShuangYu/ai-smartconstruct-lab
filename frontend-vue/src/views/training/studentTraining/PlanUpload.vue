<template>
  <div class="plan-upload">
    <!-- Loading State -->
    <div v-if="store.loading" class="plan-upload__loading">
      <div class="plan-upload__loading-spinner"></div>
      <p class="plan-upload__loading-text">加载 AI 分析数据中...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="store.error && !store.hasAnalysis" class="plan-upload__error">
      <p class="plan-upload__error-text">{{ store.error }}</p>
      <button class="plan-upload__retry-btn" @click="loadData">重试</button>
    </div>

    <!-- Main Content -->
    <template v-else>
      <!-- Upload Section -->
      <section class="plan-upload__upload">
        <h2 class="plan-upload__title">方案文档上传</h2>
        <div
          class="plan-upload__dropzone"
          :class="{ 'plan-upload__dropzone--active': isDragging }"
          @dragover.prevent="isDragging = true"
          @dragleave="isDragging = false"
          @drop.prevent="handleDrop"
        >
          <div class="plan-upload__dropzone-icon">📋</div>
          <p class="plan-upload__dropzone-text">拖拽方案文件到此处，或点击上传</p>
          <p class="plan-upload__dropzone-hint">支持 .doc, .docx, .pdf, .pptx 格式</p>
          <input
            type="file"
            class="plan-upload__file-input"
            accept=".doc,.docx,.pdf,.pptx"
            @change="handleFileSelect"
          />
        </div>
        <div v-if="uploadedFile" class="plan-upload__file-info">
          <span class="plan-upload__file-name">{{ uploadedFile.name }}</span>
          <span class="plan-upload__file-size">{{ formatFileSize(uploadedFile.size) }}</span>
          <button class="plan-upload__file-remove" @click="removeFile">✕</button>
        </div>
      </section>

      <!-- AI Feasibility Radar Chart -->
      <section v-if="store.hasAnalysis" class="plan-upload__radar">
        <h3 class="plan-upload__section-title">
          <span class="plan-upload__ai-badge">AI</span>
          可行性分析雷达图
        </h3>
        <div class="plan-upload__radar-chart">
          <!-- Simplified radar visualization using CSS -->
          <div class="plan-upload__radar-grid">
            <div
              v-for="dimension in store.dimensions"
              :key="dimension.name"
              class="plan-upload__radar-axis"
            >
              <div class="plan-upload__radar-bar">
                <div
                  class="plan-upload__radar-fill"
                  :style="{ height: `${dimension.score}%` }"
                ></div>
              </div>
              <span class="plan-upload__radar-label">{{ dimension.name }}</span>
              <span class="plan-upload__radar-score">{{ dimension.score }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Empty State: No AI Analysis Yet -->
      <section v-else class="plan-upload__empty">
        <div class="plan-upload__empty-icon">🔍</div>
        <p class="plan-upload__empty-title">暂无 AI 可行性分析</p>
        <p class="plan-upload__empty-hint">上传方案文档后，AI 将自动进行可行性分析</p>
      </section>

      <!-- Dimension Scores Detail (only when analysis available) -->
      <section v-if="store.hasAnalysis" class="plan-upload__scores">
        <h3 class="plan-upload__section-title">维度评分详情</h3>
        <div class="plan-upload__score-list">
          <div
            v-for="dimension in store.dimensions"
            :key="dimension.name"
            class="plan-upload__score-item"
          >
            <div class="plan-upload__score-header">
              <span class="plan-upload__score-name">{{ dimension.name }}</span>
              <span class="plan-upload__score-value">{{ dimension.score }}/100</span>
            </div>
            <div class="plan-upload__score-bar">
              <div
                class="plan-upload__score-fill"
                :style="{ width: `${dimension.score}%` }"
                :class="getScoreClass(dimension.score)"
              ></div>
            </div>
            <p class="plan-upload__score-feedback">{{ dimension.feedback }}</p>
          </div>
        </div>
      </section>

      <!-- Submission Status (after successful submit) -->
      <section v-if="store.submitResult" class="plan-upload__status">
        <h3 class="plan-upload__section-title">提交状态</h3>
        <div class="plan-upload__status-card">
          <div class="plan-upload__status-row">
            <span class="plan-upload__status-label">提交状态</span>
            <span class="plan-upload__status-value plan-upload__status-value--success">已提交</span>
          </div>
          <div class="plan-upload__status-row">
            <span class="plan-upload__status-label">AI 评审</span>
            <span class="plan-upload__status-value plan-upload__status-value--pending">
              {{ store.submitResult.aiReviewStatus === 'pending' ? '等待评审中...' : store.submitResult.aiReviewStatus }}
            </span>
          </div>
        </div>
      </section>

      <!-- Error message during submission -->
      <div v-if="store.error && !store.loading" class="plan-upload__submit-error">
        <p class="plan-upload__error-text">{{ store.error }}</p>
      </div>

      <!-- Submit -->
      <section class="plan-upload__footer">
        <button
          class="plan-upload__submit-btn"
          :disabled="!uploadedFile || store.submitting"
          @click="handleSubmit"
        >
          {{ store.submitting ? '提交中...' : '提交方案' }}
        </button>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { usePlanUploadStore } from '@/stores/modules/planUpload.store'

interface PlanUploadConfig {
  display?: {
    title?: string
  }
  ai_processing?: {
    enable_ai_feasibility?: boolean
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: PlanUploadConfig
}>()

const emit = defineEmits<{
  complete: []
}>()

const store = usePlanUploadStore()

const isDragging = ref<boolean>(false)
const uploadedFile = ref<File | null>(null)

/** Load AI analysis data and submission state on mount */
onMounted(() => {
  loadData()
})

/** Clean up store on unmount */
onUnmounted(() => {
  store.reset()
})

function loadData() {
  store.loadNodeState(props.nodeInstanceId)
}

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

function getScoreClass(score: number): string {
  if (score >= 80) return 'plan-upload__score-fill--high'
  if (score >= 60) return 'plan-upload__score-fill--medium'
  return 'plan-upload__score-fill--low'
}

async function handleSubmit() {
  if (!uploadedFile.value) return

  const success = await store.uploadAndSubmit(uploadedFile.value)
  if (success) {
    emit('complete')
  }
}
</script>

<style scoped>
.plan-upload {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
}

.plan-upload__title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-md, 1rem);
}

.plan-upload__upload {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.plan-upload__dropzone {
  border: 2px dashed var(--color-gray-300, #cbd5e1);
  border-radius: var(--radius-md, 0.5rem);
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.plan-upload__dropzone--active {
  border-color: var(--color-primary-400, #818cf8);
  background: var(--color-primary-50, #eef2ff);
}

.plan-upload__dropzone-icon {
  font-size: 2.5rem;
  margin-bottom: 0.75rem;
}

.plan-upload__dropzone-text {
  font-size: 0.9375rem;
  color: var(--color-gray-600, #475569);
  font-weight: 500;
}

.plan-upload__dropzone-hint {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
  margin-top: 0.25rem;
}

.plan-upload__file-input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.plan-upload__file-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-top: var(--spacing-sm, 0.75rem);
  padding: 0.5rem 0.75rem;
  background: var(--color-gray-50, #f8fafc);
  border-radius: var(--radius-sm, 0.25rem);
}

.plan-upload__file-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-gray-700, #334155);
  flex: 1;
}

.plan-upload__file-size {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
}

.plan-upload__file-remove {
  background: none;
  border: none;
  color: var(--color-gray-400, #94a3b8);
  cursor: pointer;
}

.plan-upload__section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-md, 1rem);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.plan-upload__ai-badge {
  font-size: 0.625rem;
  font-weight: 700;
  padding: 0.125rem 0.375rem;
  border-radius: var(--radius-sm, 0.25rem);
  background: linear-gradient(135deg, var(--color-primary-500, #6366f1), var(--color-primary-600, #4f46e5));
  color: var(--color-white, #ffffff);
}

.plan-upload__radar {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.plan-upload__radar-chart {
  padding: var(--spacing-md, 1rem);
}

.plan-upload__radar-grid {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 160px;
  padding: 0 var(--spacing-md, 1rem);
}

.plan-upload__radar-axis {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.plan-upload__radar-bar {
  width: 2rem;
  height: 120px;
  background: var(--color-gray-100, #f1f5f9);
  border-radius: var(--radius-sm, 0.25rem);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
}

.plan-upload__radar-fill {
  width: 100%;
  background: linear-gradient(180deg, var(--color-primary-500, #6366f1), var(--color-primary-600, #4f46e5));
  border-radius: var(--radius-sm, 0.25rem);
  transition: height 0.5s ease;
}

.plan-upload__radar-label {
  font-size: 0.6875rem;
  color: var(--color-gray-600, #475569);
  text-align: center;
  white-space: nowrap;
}

.plan-upload__radar-score {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.plan-upload__scores {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.plan-upload__score-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md, 1rem);
}

.plan-upload__score-item {
  padding-bottom: var(--spacing-sm, 0.75rem);
  border-bottom: 1px solid var(--color-gray-100, #f1f5f9);
}

.plan-upload__score-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.plan-upload__score-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.375rem;
}

.plan-upload__score-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-gray-700, #334155);
}

.plan-upload__score-value {
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.plan-upload__score-bar {
  height: 6px;
  background: var(--color-gray-100, #f1f5f9);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.plan-upload__score-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.plan-upload__score-fill--high {
  background: var(--color-success, #22c55e);
}

.plan-upload__score-fill--medium {
  background: var(--color-warning, #f59e0b);
}

.plan-upload__score-fill--low {
  background: var(--color-error, #ef4444);
}

.plan-upload__score-feedback {
  font-size: 0.8125rem;
  color: var(--color-gray-500, #64748b);
  line-height: 1.5;
}

.plan-upload__footer {
  padding-top: var(--spacing-md, 1rem);
}

.plan-upload__submit-btn {
  width: 100%;
  padding: 0.875rem;
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-white, #ffffff);
  background: linear-gradient(135deg, var(--color-primary-500, #6366f1) 0%, var(--color-primary-600, #4f46e5) 100%);
  border: none;
  border-radius: var(--radius-lg, 0.75rem);
  cursor: pointer;
  transition: all 0.2s ease;
}

.plan-upload__submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px -4px rgba(99, 102, 241, 0.4);
}

.plan-upload__submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Loading State */
.plan-upload__loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  gap: var(--spacing-md, 1rem);
}

.plan-upload__loading-spinner {
  width: 2.5rem;
  height: 2.5rem;
  border: 3px solid var(--color-gray-200, #e2e8f0);
  border-top-color: var(--color-primary-500, #6366f1);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.plan-upload__loading-text {
  font-size: 0.875rem;
  color: var(--color-gray-500, #64748b);
}

/* Error State */
.plan-upload__error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  gap: var(--spacing-md, 1rem);
}

.plan-upload__error-text {
  font-size: 0.875rem;
  color: var(--color-error, #ef4444);
  text-align: center;
}

.plan-upload__retry-btn {
  padding: 0.5rem 1.25rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-primary-600, #4f46e5);
  background: var(--color-primary-50, #eef2ff);
  border: 1px solid var(--color-primary-200, #c7d2fe);
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  transition: background 0.2s ease;
}

.plan-upload__retry-btn:hover {
  background: var(--color-primary-100, #e0e7ff);
}

/* Empty State */
.plan-upload__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2.5rem;
  background: var(--color-white, #ffffff);
  border: 1px dashed var(--color-gray-300, #cbd5e1);
  border-radius: var(--radius-lg, 0.75rem);
  gap: 0.5rem;
}

.plan-upload__empty-icon {
  font-size: 2rem;
  opacity: 0.6;
}

.plan-upload__empty-title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-gray-600, #475569);
}

.plan-upload__empty-hint {
  font-size: 0.8125rem;
  color: var(--color-gray-400, #94a3b8);
}

/* Submission Status */
.plan-upload__status {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.plan-upload__status-card {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.plan-upload__status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.plan-upload__status-label {
  font-size: 0.875rem;
  color: var(--color-gray-600, #475569);
}

.plan-upload__status-value {
  font-size: 0.875rem;
  font-weight: 600;
}

.plan-upload__status-value--success {
  color: var(--color-success, #22c55e);
}

.plan-upload__status-value--pending {
  color: var(--color-warning, #f59e0b);
}

/* Submit Error */
.plan-upload__submit-error {
  padding: 0.75rem;
  background: var(--color-error-50, #fef2f2);
  border: 1px solid var(--color-error-200, #fecaca);
  border-radius: var(--radius-md, 0.5rem);
}
</style>
