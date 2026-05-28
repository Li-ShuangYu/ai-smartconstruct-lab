<template>
  <div class="knowledge-eval">
    <!-- Header -->
    <section class="knowledge-eval__header">
      <h2 class="knowledge-eval__title">知识点评价</h2>
      <p class="knowledge-eval__desc">请对以下知识点进行难度评价，并提出您的疑问。</p>
    </section>

    <!-- Knowledge Point List -->
    <section class="knowledge-eval__list">
      <div
        v-for="(point, index) in knowledgePoints"
        :key="point.id"
        class="knowledge-eval__item"
      >
        <div class="knowledge-eval__item-header">
          <span class="knowledge-eval__item-index">{{ index + 1 }}</span>
          <span class="knowledge-eval__item-name">{{ point.name }}</span>
          <span class="knowledge-eval__item-category">{{ point.category }}</span>
        </div>

        <!-- Difficulty Rating -->
        <div class="knowledge-eval__rating">
          <span class="knowledge-eval__rating-label">难度评价：</span>
          <div class="knowledge-eval__stars">
            <button
              v-for="star in 5"
              :key="star"
              class="knowledge-eval__star"
              :class="{ 'knowledge-eval__star--active': (ratings[point.id] ?? 0) >= star }"
              @click="setRating(point.id, star)"
            >
              ★
            </button>
          </div>
          <span class="knowledge-eval__rating-text">{{ ratingLabel(ratings[point.id] ?? 0) }}</span>
        </div>

        <!-- Question Input -->
        <div class="knowledge-eval__question">
          <textarea
            class="knowledge-eval__question-input"
            :placeholder="getPlaceholder(point.name)"
            :value="questions[point.id] ?? ''"
            rows="2"
            @input="onQuestionInput(point.id, $event)"
          ></textarea>
        </div>
      </div>
    </section>

    <!-- Summary -->
    <section class="knowledge-eval__summary">
      <div class="knowledge-eval__summary-stats">
        <div class="knowledge-eval__stat">
          <span class="knowledge-eval__stat-value">{{ ratedCount }}</span>
          <span class="knowledge-eval__stat-label">已评价</span>
        </div>
        <div class="knowledge-eval__stat">
          <span class="knowledge-eval__stat-value">{{ questionCount }}</span>
          <span class="knowledge-eval__stat-label">已提问</span>
        </div>
        <div class="knowledge-eval__stat">
          <span class="knowledge-eval__stat-value">{{ averageDifficulty }}</span>
          <span class="knowledge-eval__stat-label">平均难度</span>
        </div>
      </div>
    </section>

    <!-- Submit -->
    <section class="knowledge-eval__footer">
      <button
        class="knowledge-eval__submit-btn"
        :disabled="ratedCount === 0"
        @click="handleSubmit"
      >
        提交评价
      </button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface KnowledgePoint {
  id: string
  name: string
  category: string
}

interface KnowledgeEvalConfig {
  display?: {
    title?: string
  }
  data_collection?: {
    knowledge_points?: KnowledgePoint[]
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: KnowledgeEvalConfig
}>()

const emit = defineEmits<{
  complete: []
}>()

/** Knowledge points from config or placeholder */
const knowledgePoints = computed<KnowledgePoint[]>(() =>
  props.nodeConfig.data_collection?.knowledge_points ?? placeholderPoints
)

const placeholderPoints: KnowledgePoint[] = [
  { id: 'kp1', name: '变量与数据类型', category: '基础语法' },
  { id: 'kp2', name: '条件判断语句', category: '流程控制' },
  { id: 'kp3', name: '循环结构', category: '流程控制' },
  { id: 'kp4', name: '函数定义与调用', category: '函数' },
  { id: 'kp5', name: '列表操作', category: '数据结构' },
  { id: 'kp6', name: '字典与集合', category: '数据结构' },
  { id: 'kp7', name: '面向对象编程', category: '高级特性' },
  { id: 'kp8', name: '异常处理', category: '高级特性' }
]

/** Ratings: pointId -> 1-5 */
const ratings = ref<Record<string, number>>({})

/** Questions: pointId -> text */
const questions = ref<Record<string, string>>({})

function setRating(pointId: string, value: number) {
  ratings.value[pointId] = value
}

function setQuestion(pointId: string, text: string) {
  questions.value[pointId] = text
}

function onQuestionInput(pointId: string, event: Event) {
  const target = event.target as HTMLTextAreaElement
  setQuestion(pointId, target.value)
}

function getPlaceholder(name: string): string {
  return `关于"${name}"的疑问或建议...`
}

function ratingLabel(value: number): string {
  switch (value) {
    case 1: return '很简单'
    case 2: return '较简单'
    case 3: return '适中'
    case 4: return '较难'
    case 5: return '很难'
    default: return '未评价'
  }
}

const ratedCount = computed<number>(() =>
  Object.keys(ratings.value).filter(k => (ratings.value[k] ?? 0) > 0).length
)

const questionCount = computed<number>(() =>
  Object.keys(questions.value).filter(k => (questions.value[k] ?? '').trim().length > 0).length
)

const averageDifficulty = computed<string>(() => {
  const values = Object.values(ratings.value).filter(v => v > 0)
  if (values.length === 0) return '-'
  const avg = values.reduce((sum, v) => sum + v, 0) / values.length
  return avg.toFixed(1)
})

function handleSubmit() {
  emit('complete')
}
</script>

<style scoped>
.knowledge-eval {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
}

.knowledge-eval__header {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.knowledge-eval__title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: 0.5rem;
}

.knowledge-eval__desc {
  font-size: 0.875rem;
  color: var(--color-gray-500, #64748b);
}

.knowledge-eval__list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm, 0.75rem);
}

.knowledge-eval__item {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-md, 1rem) var(--spacing-lg, 1.5rem);
}

.knowledge-eval__item-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.knowledge-eval__item-index {
  width: 1.5rem;
  height: 1.5rem;
  border-radius: 50%;
  background: var(--color-primary-100, #e0e7ff);
  color: var(--color-primary-700, #4338ca);
  font-size: 0.6875rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.knowledge-eval__item-name {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  flex: 1;
}

.knowledge-eval__item-category {
  font-size: 0.6875rem;
  font-weight: 600;
  padding: 0.125rem 0.5rem;
  border-radius: var(--radius-sm, 0.25rem);
  background: var(--color-gray-100, #f1f5f9);
  color: var(--color-gray-500, #64748b);
}

.knowledge-eval__rating {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.knowledge-eval__rating-label {
  font-size: 0.8125rem;
  color: var(--color-gray-600, #475569);
}

.knowledge-eval__stars {
  display: flex;
  gap: 0.25rem;
}

.knowledge-eval__star {
  font-size: 1.25rem;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--color-gray-300, #cbd5e1);
  transition: color 0.15s ease;
  padding: 0;
}

.knowledge-eval__star--active {
  color: #f59e0b;
}

.knowledge-eval__star:hover {
  color: #fbbf24;
}

.knowledge-eval__rating-text {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
}

.knowledge-eval__question-input {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  font-size: 0.8125rem;
  color: var(--color-gray-800, #1e293b);
  resize: vertical;
  font-family: inherit;
}

.knowledge-eval__question-input:focus {
  outline: none;
  border-color: var(--color-primary-300, #a5b4fc);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.knowledge-eval__summary {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-md, 1rem) var(--spacing-lg, 1.5rem);
}

.knowledge-eval__summary-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-md, 1rem);
}

.knowledge-eval__stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.knowledge-eval__stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.knowledge-eval__stat-label {
  font-size: 0.75rem;
  color: var(--color-gray-500, #64748b);
}

.knowledge-eval__footer {
  padding-top: var(--spacing-md, 1rem);
}

.knowledge-eval__submit-btn {
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

.knowledge-eval__submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px -4px rgba(99, 102, 241, 0.4);
}

.knowledge-eval__submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
