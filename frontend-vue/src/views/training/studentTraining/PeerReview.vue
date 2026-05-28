<template>
  <div class="peer-review">
    <!-- Header -->
    <section class="peer-review__header">
      <h2 class="peer-review__title">学生互评</h2>
      <p class="peer-review__desc">请对同学的作品进行多维度评价，给出客观公正的评分和建议。</p>
    </section>

    <!-- Target Student Info -->
    <section class="peer-review__target">
      <div class="peer-review__target-avatar">{{ targetStudent.name.charAt(0) }}</div>
      <div class="peer-review__target-info">
        <span class="peer-review__target-name">{{ targetStudent.name }}</span>
        <span class="peer-review__target-work">{{ targetStudent.workTitle }}</span>
      </div>
    </section>

    <!-- Dimension Scores -->
    <section class="peer-review__dimensions">
      <h3 class="peer-review__section-title">评分维度</h3>
      <div class="peer-review__dimension-list">
        <div
          v-for="dimension in dimensions"
          :key="dimension.id"
          class="peer-review__dimension"
        >
          <div class="peer-review__dimension-header">
            <span class="peer-review__dimension-name">{{ dimension.name }}</span>
            <span class="peer-review__dimension-value">
              {{ scores[dimension.id] ?? 0 }}/{{ dimension.maxScore }}
            </span>
          </div>
          <p class="peer-review__dimension-desc">{{ dimension.description }}</p>
          <input
            type="range"
            class="peer-review__slider"
            :min="0"
            :max="dimension.maxScore"
            :value="scores[dimension.id] ?? 0"
            @input="setScore(dimension.id, Number(($event.target as HTMLInputElement).value))"
          />
        </div>
      </div>
    </section>

    <!-- Comments -->
    <section class="peer-review__comments">
      <h3 class="peer-review__section-title">评价意见</h3>
      <textarea
        class="peer-review__comment-input"
        placeholder="请输入您对该同学作品的评价意见和改进建议..."
        :value="comment"
        rows="4"
        @input="comment = ($event.target as HTMLTextAreaElement).value"
      ></textarea>
    </section>

    <!-- Total Score -->
    <section class="peer-review__total">
      <span class="peer-review__total-label">总分：</span>
      <span class="peer-review__total-value">{{ totalScore }}</span>
      <span class="peer-review__total-max">/{{ maxTotalScore }}</span>
    </section>

    <!-- Submit -->
    <section class="peer-review__footer">
      <button
        class="peer-review__submit-btn"
        :disabled="totalScore === 0"
        @click="handleSubmit"
      >
        提交互评
      </button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface ReviewDimension {
  id: string
  name: string
  description: string
  maxScore: number
}

interface TargetStudent {
  name: string
  workTitle: string
}

interface PeerReviewConfig {
  display?: {
    title?: string
  }
  evaluation?: {
    dimensions?: ReviewDimension[]
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: PeerReviewConfig
}>()

const emit = defineEmits<{
  complete: []
}>()

/** Placeholder target student */
const targetStudent = computed<TargetStudent>(() => ({
  name: '李娜',
  workTitle: '需求分析文档 v2.0'
}))

/** Dimensions from config or placeholder */
const dimensions = computed<ReviewDimension[]>(() =>
  props.nodeConfig.evaluation?.dimensions ?? placeholderDimensions
)

const placeholderDimensions: ReviewDimension[] = [
  { id: 'd1', name: '内容完整性', description: '文档是否涵盖了所有必要的需求描述', maxScore: 25 },
  { id: 'd2', name: '逻辑清晰度', description: '需求描述是否逻辑清晰、条理分明', maxScore: 25 },
  { id: 'd3', name: '技术可行性', description: '提出的方案是否技术上可行', maxScore: 25 },
  { id: 'd4', name: '文档规范性', description: '格式、排版、用语是否规范', maxScore: 25 }
]

/** Scores: dimensionId -> score */
const scores = ref<Record<string, number>>({})

/** Comment text */
const comment = ref<string>('')

function setScore(dimensionId: string, value: number) {
  scores.value[dimensionId] = value
}

const totalScore = computed<number>(() =>
  Object.values(scores.value).reduce((sum, v) => sum + v, 0)
)

const maxTotalScore = computed<number>(() =>
  dimensions.value.reduce((sum, d) => sum + d.maxScore, 0)
)

function handleSubmit() {
  emit('complete')
}
</script>

<style scoped>
.peer-review {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
}

.peer-review__header {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.peer-review__title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: 0.5rem;
}

.peer-review__desc {
  font-size: 0.875rem;
  color: var(--color-gray-500, #64748b);
}

.peer-review__target {
  display: flex;
  align-items: center;
  gap: var(--spacing-md, 1rem);
  padding: var(--spacing-md, 1rem) var(--spacing-lg, 1.5rem);
  background: var(--color-primary-50, #eef2ff);
  border: 1px solid var(--color-primary-100, #e0e7ff);
  border-radius: var(--radius-lg, 0.75rem);
}

.peer-review__target-avatar {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  font-weight: 700;
}

.peer-review__target-info {
  display: flex;
  flex-direction: column;
}

.peer-review__target-name {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
}

.peer-review__target-work {
  font-size: 0.8125rem;
  color: var(--color-gray-500, #64748b);
}

.peer-review__section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-md, 1rem);
}

.peer-review__dimensions {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.peer-review__dimension-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md, 1rem);
}

.peer-review__dimension {
  padding-bottom: var(--spacing-sm, 0.75rem);
  border-bottom: 1px solid var(--color-gray-100, #f1f5f9);
}

.peer-review__dimension:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.peer-review__dimension-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
}

.peer-review__dimension-name {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
}

.peer-review__dimension-value {
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.peer-review__dimension-desc {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
  margin-bottom: 0.5rem;
}

.peer-review__slider {
  width: 100%;
  height: 6px;
  appearance: none;
  background: var(--color-gray-100, #f1f5f9);
  border-radius: 3px;
  outline: none;
  cursor: pointer;
}

.peer-review__slider::-webkit-slider-thumb {
  appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-primary-500, #6366f1);
  cursor: pointer;
}

.peer-review__comments {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.peer-review__comment-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  font-size: 0.875rem;
  color: var(--color-gray-800, #1e293b);
  resize: vertical;
  font-family: inherit;
}

.peer-review__comment-input:focus {
  outline: none;
  border-color: var(--color-primary-300, #a5b4fc);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.peer-review__total {
  display: flex;
  align-items: baseline;
  justify-content: center;
  padding: var(--spacing-md, 1rem);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
}

.peer-review__total-label {
  font-size: 1rem;
  color: var(--color-gray-600, #475569);
}

.peer-review__total-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.peer-review__total-max {
  font-size: 1rem;
  color: var(--color-gray-400, #94a3b8);
}

.peer-review__footer {
  padding-top: var(--spacing-md, 1rem);
}

.peer-review__submit-btn {
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

.peer-review__submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px -4px rgba(99, 102, 241, 0.4);
}

.peer-review__submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
