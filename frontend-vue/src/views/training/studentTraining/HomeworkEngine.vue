<template>
  <div class="homework-engine">
    <!-- Question Navigation Sidebar -->
    <aside class="homework-engine__nav">
      <div class="homework-engine__nav-header">
        <h3 class="homework-engine__nav-title">答题进度</h3>
        <span class="homework-engine__nav-count">
          {{ answeredCount }} / {{ questions.length }}
        </span>
      </div>

      <div class="homework-engine__nav-grid">
        <button
          v-for="(q, index) in questions"
          :key="q.id"
          class="homework-engine__nav-item"
          :class="{
            'homework-engine__nav-item--active': currentIndex === index,
            'homework-engine__nav-item--answered': isAnswered(index)
          }"
          @click="currentIndex = index"
        >
          {{ index + 1 }}
        </button>
      </div>

      <div class="homework-engine__nav-legend">
        <span class="homework-engine__legend-item">
          <span class="homework-engine__legend-dot homework-engine__legend-dot--answered"></span>
          已答
        </span>
        <span class="homework-engine__legend-item">
          <span class="homework-engine__legend-dot homework-engine__legend-dot--unanswered"></span>
          未答
        </span>
      </div>
    </aside>

    <!-- Main Answer Area -->
    <main class="homework-engine__main">
      <!-- Question Display -->
      <div class="homework-engine__question">
        <div class="homework-engine__question-meta">
          <span class="homework-engine__question-type">
            {{ questionTypeLabel(currentQuestion.type) }}
          </span>
          <span class="homework-engine__question-number">
            第 {{ currentIndex + 1 }} 题 / 共 {{ questions.length }} 题
          </span>
        </div>

        <h2 class="homework-engine__question-title">
          {{ currentQuestion.title }}
        </h2>

        <!-- Single Choice / Judge -->
        <div
          v-if="currentQuestion.type === 'single' || currentQuestion.type === 'judge'"
          class="homework-engine__options"
        >
          <label
            v-for="opt in currentQuestion.options"
            :key="opt.value"
            class="homework-engine__option"
            :class="{ 'homework-engine__option--selected': answers[currentIndex] === opt.value }"
          >
            <input
              type="radio"
              :value="opt.value"
              v-model="answers[currentIndex]"
              class="homework-engine__option-input"
            />
            <span class="homework-engine__option-radio"></span>
            <span class="homework-engine__option-label">{{ opt.label }}</span>
          </label>
        </div>

        <!-- Multiple Choice -->
        <div
          v-else-if="currentQuestion.type === 'multi'"
          class="homework-engine__options"
        >
          <label
            v-for="opt in currentQuestion.options"
            :key="opt.value"
            class="homework-engine__option"
            :class="{ 'homework-engine__option--selected': (answers[currentIndex] as string[]).includes(opt.value) }"
          >
            <input
              type="checkbox"
              :value="opt.value"
              v-model="answers[currentIndex]"
              class="homework-engine__option-input"
            />
            <span class="homework-engine__option-checkbox"></span>
            <span class="homework-engine__option-label">{{ opt.label }}</span>
          </label>
        </div>

        <!-- Essay / Subjective -->
        <div v-else-if="currentQuestion.type === 'essay'" class="homework-engine__essay">
          <textarea
            v-model="answers[currentIndex]"
            class="homework-engine__textarea"
            placeholder="请输入您的答案..."
            rows="8"
          ></textarea>
        </div>

        <!-- Fill in the blank -->
        <div v-else-if="currentQuestion.type === 'fill'" class="homework-engine__fill">
          <input
            v-model="answers[currentIndex]"
            type="text"
            class="homework-engine__fill-input"
            placeholder="请输入答案..."
          />
        </div>
      </div>

      <!-- Navigation Buttons -->
      <div class="homework-engine__footer">
        <button
          class="homework-engine__btn homework-engine__btn--secondary"
          :disabled="currentIndex === 0"
          @click="currentIndex--"
        >
          上一题
        </button>

        <button
          v-if="currentIndex < questions.length - 1"
          class="homework-engine__btn homework-engine__btn--primary"
          @click="currentIndex++"
        >
          下一题
        </button>

        <button
          v-else
          class="homework-engine__btn homework-engine__btn--submit"
          :disabled="isSubmitted"
          @click="handleSubmit"
        >
          {{ isSubmitted ? '已提交' : '提交作业' }}
        </button>
      </div>

      <!-- AI Grading Results (shown after submission) -->
      <div v-if="isSubmitted && aiGradingResult" class="homework-engine__grading">
        <div class="homework-engine__grading-header">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <h3>AI 批改结果</h3>
        </div>
        <div class="homework-engine__grading-score">
          <span class="homework-engine__grading-label">得分</span>
          <span class="homework-engine__grading-value">{{ aiGradingResult.score }}</span>
          <span class="homework-engine__grading-total">/ {{ aiGradingResult.total }}</span>
        </div>
        <p class="homework-engine__grading-comment">{{ aiGradingResult.comment }}</p>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface QuestionOption {
  label: string
  value: string
}

interface Question {
  id: number
  type: 'single' | 'multi' | 'judge' | 'fill' | 'essay'
  title: string
  options?: QuestionOption[]
}

interface AiGradingResult {
  score: number
  total: number
  comment: string
}

interface HomeworkConfig {
  questions?: Question[]
  ai_grading_enabled?: boolean
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: HomeworkConfig
}>()

const emit = defineEmits<{
  complete: []
  navigate: [direction: 'next']
}>()

/** Current question index */
const currentIndex = ref<number>(0)

/** Submission state */
const isSubmitted = ref<boolean>(false)

/** AI grading result (populated after submission) */
const aiGradingResult = ref<AiGradingResult | null>(null)

/** Placeholder questions for scaffold rendering */
const placeholderQuestions: Question[] = [
  {
    id: 1,
    type: 'single',
    title: '在 Python 中，创建一个空列表的正确方式是？',
    options: [
      { label: 'A. list = {}', value: 'A' },
      { label: 'B. list = []', value: 'B' },
      { label: 'C. list = ()', value: 'C' }
    ]
  },
  {
    id: 2,
    type: 'judge',
    title: 'Python 中的列表（List）是可变序列。',
    options: [
      { label: '正确', value: 'T' },
      { label: '错误', value: 'F' }
    ]
  },
  {
    id: 3,
    type: 'multi',
    title: '以下哪些方法可以向列表中添加元素？',
    options: [
      { label: 'A. append()', value: 'A' },
      { label: 'B. extend()', value: 'B' },
      { label: 'C. insert()', value: 'C' },
      { label: 'D. add()', value: 'D' }
    ]
  },
  {
    id: 4,
    type: 'fill',
    title: '若 arr = [1, 2, 3, 4, 5]，则 arr[1:3] 的结果是：'
  },
  {
    id: 5,
    type: 'essay',
    title: '请简述 Python 列表与元组在可变性方面的主要区别。'
  }
]

/** Questions list from config or placeholder */
const questions = computed<Question[]>(() =>
  props.nodeConfig?.questions ?? placeholderQuestions
)

/** Answers array initialized per question type */
const answers = ref<(string | string[])[]>(
  (props.nodeConfig?.questions ?? placeholderQuestions).map(q =>
    q.type === 'multi' ? [] : ''
  )
)

/** Current question */
const currentQuestion = computed<Question>(() =>
  questions.value[currentIndex.value] ?? { id: 0, type: 'single', title: '' }
)

/** Count of answered questions */
const answeredCount = computed<number>(() =>
  answers.value.filter((ans, i) => isAnswered(i)).length
)

/** Check if a question is answered */
function isAnswered(index: number): boolean {
  const ans = answers.value[index]
  if (Array.isArray(ans)) return ans.length > 0
  return String(ans).trim().length > 0
}

/** Get question type label */
function questionTypeLabel(type: string): string {
  const labels: Record<string, string> = {
    single: '单选题',
    multi: '多选题',
    judge: '判断题',
    fill: '填空题',
    essay: '简答题'
  }
  return labels[type] ?? '未知题型'
}

/** Handle submission */
async function handleSubmit() {
  if (isSubmitted.value) return
  isSubmitted.value = true

  // Simulate AI grading result
  aiGradingResult.value = {
    score: 85,
    total: 100,
    comment: 'AI批改完成。整体表现良好，基础概念掌握扎实。建议加强对切片操作和列表推导式的理解。'
  }

  emit('complete')
}
</script>

<style scoped>
.homework-engine {
  display: flex;
  height: 100%;
  overflow: hidden;
}

.homework-engine__nav {
  width: 220px;
  padding: var(--spacing-md, 1rem);
  border-right: 1px solid var(--color-gray-100, #f1f5f9);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.homework-engine__nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md, 1rem);
}

.homework-engine__nav-title {
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--color-gray-700, #334155);
}

.homework-engine__nav-count {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-primary-600, #4f46e5);
}

.homework-engine__nav-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.5rem;
  flex: 1;
  overflow-y: auto;
  align-content: start;
}

.homework-engine__nav-item {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: var(--radius-md, 0.5rem);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  background: var(--color-white, #ffffff);
  color: var(--color-gray-500, #64748b);
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
}

.homework-engine__nav-item:hover {
  border-color: var(--color-primary-300, #a5b4fc);
}

.homework-engine__nav-item--active {
  border-color: var(--color-primary-500, #6366f1);
  box-shadow: 0 0 0 2px var(--color-primary-100, #e0e7ff);
}

.homework-engine__nav-item--answered {
  background: var(--color-primary-500, #6366f1);
  border-color: var(--color-primary-500, #6366f1);
  color: white;
}

.homework-engine__nav-legend {
  display: flex;
  gap: var(--spacing-md, 1rem);
  padding-top: var(--spacing-sm, 0.75rem);
  border-top: 1px solid var(--color-gray-100, #f1f5f9);
  margin-top: var(--spacing-sm, 0.75rem);
}

.homework-engine__legend-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.6875rem;
  color: var(--color-gray-500, #64748b);
}

.homework-engine__legend-dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
}

.homework-engine__legend-dot--answered {
  background: var(--color-primary-500, #6366f1);
}

.homework-engine__legend-dot--unanswered {
  background: var(--color-gray-200, #e2e8f0);
}

/* Main area */
.homework-engine__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: var(--spacing-lg, 1.5rem);
  overflow-y: auto;
  min-width: 0;
}

.homework-engine__question {
  flex: 1;
}

.homework-engine__question-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-md, 1rem);
}

.homework-engine__question-type {
  font-size: 0.6875rem;
  font-weight: 700;
  text-transform: uppercase;
  padding: 0.125rem 0.5rem;
  border-radius: var(--radius-sm, 0.25rem);
  background: var(--color-primary-50, #eef2ff);
  color: var(--color-primary-600, #4f46e5);
}

.homework-engine__question-number {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
}

.homework-engine__question-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  line-height: 1.6;
  margin-bottom: var(--spacing-lg, 1.5rem);
}

/* Options */
.homework-engine__options {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.homework-engine__option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.homework-engine__option:hover {
  border-color: var(--color-primary-300, #a5b4fc);
  background: var(--color-primary-50, #eef2ff);
}

.homework-engine__option--selected {
  border-color: var(--color-primary-500, #6366f1);
  background: var(--color-primary-50, #eef2ff);
}

.homework-engine__option-input {
  display: none;
}

.homework-engine__option-radio {
  width: 1.125rem;
  height: 1.125rem;
  border-radius: 50%;
  border: 2px solid var(--color-gray-300, #cbd5e1);
  flex-shrink: 0;
  position: relative;
}

.homework-engine__option--selected .homework-engine__option-radio {
  border-color: var(--color-primary-500, #6366f1);
  background: var(--color-primary-500, #6366f1);
}

.homework-engine__option--selected .homework-engine__option-radio::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: white;
}

.homework-engine__option-checkbox {
  width: 1.125rem;
  height: 1.125rem;
  border-radius: var(--radius-sm, 0.25rem);
  border: 2px solid var(--color-gray-300, #cbd5e1);
  flex-shrink: 0;
}

.homework-engine__option--selected .homework-engine__option-checkbox {
  border-color: var(--color-primary-500, #6366f1);
  background: var(--color-primary-500, #6366f1);
}

.homework-engine__option-label {
  font-size: 0.875rem;
  color: var(--color-gray-700, #334155);
}

/* Essay & Fill */
.homework-engine__textarea {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  font-size: 0.875rem;
  line-height: 1.6;
  color: var(--color-gray-700, #334155);
  resize: vertical;
  outline: none;
  transition: border-color 0.15s ease;
}

.homework-engine__textarea:focus {
  border-color: var(--color-primary-400, #818cf8);
  box-shadow: 0 0 0 3px var(--color-primary-50, #eef2ff);
}

.homework-engine__fill-input {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  font-size: 0.875rem;
  color: var(--color-gray-700, #334155);
  outline: none;
  transition: border-color 0.15s ease;
}

.homework-engine__fill-input:focus {
  border-color: var(--color-primary-400, #818cf8);
  box-shadow: 0 0 0 3px var(--color-primary-50, #eef2ff);
}

/* Footer */
.homework-engine__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--spacing-md, 1rem);
  border-top: 1px solid var(--color-gray-100, #f1f5f9);
  margin-top: var(--spacing-lg, 1.5rem);
}

.homework-engine__btn {
  padding: 0.625rem 1.25rem;
  font-size: 0.875rem;
  font-weight: 600;
  border-radius: var(--radius-md, 0.5rem);
  border: none;
  cursor: pointer;
  transition: all 0.15s ease;
}

.homework-engine__btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.homework-engine__btn--secondary {
  background: var(--color-gray-100, #f1f5f9);
  color: var(--color-gray-600, #475569);
}

.homework-engine__btn--secondary:hover:not(:disabled) {
  background: var(--color-gray-200, #e2e8f0);
}

.homework-engine__btn--primary {
  background: var(--color-primary-50, #eef2ff);
  color: var(--color-primary-600, #4f46e5);
}

.homework-engine__btn--primary:hover {
  background: var(--color-primary-100, #e0e7ff);
}

.homework-engine__btn--submit {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: white;
}

.homework-engine__btn--submit:hover:not(:disabled) {
  box-shadow: 0 4px 12px -2px rgba(99, 102, 241, 0.4);
}

/* Grading Results */
.homework-engine__grading {
  margin-top: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-md, 1rem);
  background: var(--color-green-50, #f0fdf4);
  border: 1px solid var(--color-green-200, #bbf7d0);
  border-radius: var(--radius-lg, 0.75rem);
}

.homework-engine__grading-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--color-green-700, #15803d);
  font-weight: 700;
  margin-bottom: 0.75rem;
}

.homework-engine__grading-score {
  display: flex;
  align-items: baseline;
  gap: 0.25rem;
  margin-bottom: 0.5rem;
}

.homework-engine__grading-label {
  font-size: 0.75rem;
  color: var(--color-gray-500, #64748b);
  margin-right: 0.5rem;
}

.homework-engine__grading-value {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--color-green-600, #16a34a);
}

.homework-engine__grading-total {
  font-size: 0.875rem;
  color: var(--color-gray-400, #94a3b8);
}

.homework-engine__grading-comment {
  font-size: 0.8125rem;
  line-height: 1.6;
  color: var(--color-gray-600, #475569);
}
</style>
