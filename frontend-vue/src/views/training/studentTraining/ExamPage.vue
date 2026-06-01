<template>
  <div class="exam-page">
    <!-- Timer Bar -->
    <div class="exam-page__timer-bar">
      <div class="exam-page__timer">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span
          class="exam-page__timer-value"
          :class="{ 'exam-page__timer-value--warning': timeLeftSeconds < 300 }"
        >
          {{ formattedTime }}
        </span>
      </div>
      <button
        class="exam-page__submit-btn"
        :disabled="isSubmitted"
        @click="handleSubmit"
      >
        {{ isSubmitted ? '已交卷' : '交卷' }}
      </button>
    </div>

    <div class="exam-page__body">
      <!-- Question Navigation Sidebar -->
      <aside class="exam-page__sidebar">
        <h3 class="exam-page__sidebar-title">题目导航</h3>
        <div class="exam-page__sidebar-grid">
          <button
            v-for="(q, index) in questions"
            :key="q.id"
            class="exam-page__sidebar-item"
            :class="{
              'exam-page__sidebar-item--active': currentIndex === index,
              'exam-page__sidebar-item--answered': isAnswered(index)
            }"
            @click="currentIndex = index"
          >
            {{ index + 1 }}
          </button>
        </div>

        <div class="exam-page__sidebar-stats">
          <div class="exam-page__stat">
            <span class="exam-page__stat-value">{{ answeredCount }}</span>
            <span class="exam-page__stat-label">已答</span>
          </div>
          <div class="exam-page__stat">
            <span class="exam-page__stat-value">{{ questions.length - answeredCount }}</span>
            <span class="exam-page__stat-label">未答</span>
          </div>
        </div>
      </aside>

      <!-- Answer Area -->
      <main class="exam-page__main">
        <div class="exam-page__question">
          <div class="exam-page__question-header">
            <span class="exam-page__question-badge">
              {{ questionTypeLabel(currentQuestion.type) }}
            </span>
            <span class="exam-page__question-score" v-if="currentQuestion.score">
              {{ currentQuestion.score }} 分
            </span>
          </div>

          <h2 class="exam-page__question-title">
            <span class="exam-page__question-num">{{ currentIndex + 1 }}.</span>
            {{ currentQuestion.title }}
          </h2>

          <!-- Single / Judge -->
          <div
            v-if="currentQuestion.type === 'single' || currentQuestion.type === 'judge'"
            class="exam-page__options"
          >
            <label
              v-for="opt in currentQuestion.options"
              :key="opt.value"
              class="exam-page__option"
              :class="{ 'exam-page__option--selected': answers[currentIndex] === opt.value }"
            >
              <input
                type="radio"
                :value="opt.value"
                v-model="answers[currentIndex]"
                class="exam-page__option-input"
                :disabled="isSubmitted"
              />
              <span class="exam-page__option-indicator"></span>
              <span class="exam-page__option-text">{{ opt.label }}</span>
            </label>
          </div>

          <!-- Multi -->
          <div
            v-else-if="currentQuestion.type === 'multi'"
            class="exam-page__options"
          >
            <label
              v-for="opt in currentQuestion.options"
              :key="opt.value"
              class="exam-page__option"
              :class="{ 'exam-page__option--selected': (answers[currentIndex] as string[]).includes(opt.value) }"
            >
              <input
                type="checkbox"
                :value="opt.value"
                v-model="answers[currentIndex]"
                class="exam-page__option-input"
                :disabled="isSubmitted"
              />
              <span class="exam-page__option-indicator exam-page__option-indicator--checkbox"></span>
              <span class="exam-page__option-text">{{ opt.label }}</span>
            </label>
          </div>

          <!-- Essay -->
          <div v-else-if="currentQuestion.type === 'essay'" class="exam-page__essay">
            <textarea
              v-model="answers[currentIndex]"
              class="exam-page__textarea"
              placeholder="请输入您的答案..."
              rows="10"
              :disabled="isSubmitted"
            ></textarea>
          </div>

          <!-- Fill -->
          <div v-else class="exam-page__fill">
            <input
              v-model="answers[currentIndex]"
              type="text"
              class="exam-page__fill-input"
              placeholder="请输入答案..."
              :disabled="isSubmitted"
            />
          </div>
        </div>

        <!-- Question Navigation -->
        <div class="exam-page__nav">
          <button
            class="exam-page__nav-btn"
            :disabled="currentIndex === 0"
            @click="currentIndex--"
          >
            ← 上一题
          </button>
          <button
            class="exam-page__nav-btn"
            :disabled="currentIndex === questions.length - 1"
            @click="currentIndex++"
          >
            下一题 →
          </button>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

interface ExamOption {
  label: string
  value: string
}

interface ExamQuestion {
  id: number
  type: 'single' | 'multi' | 'judge' | 'fill' | 'essay'
  title: string
  score?: number
  options?: ExamOption[]
}

interface ExamConfig {
  exam_duration_minutes?: number
  total_score?: number
  questions?: ExamQuestion[]
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: ExamConfig
}>()

const emit = defineEmits<{
  complete: []
  navigate: [direction: 'next']
}>()

/** Current question index */
const currentIndex = ref<number>(0)

/** Submission state */
const isSubmitted = ref<boolean>(false)

/** Countdown timer (seconds) */
const examDurationMinutes = computed<number>(() =>
  props.nodeConfig?.exam_duration_minutes ?? 30
)
const timeLeftSeconds = ref<number>(examDurationMinutes.value * 60)

let timerInterval: ReturnType<typeof setInterval> | null = null

/** Formatted time display */
const formattedTime = computed<string>(() => {
  const minutes = Math.floor(timeLeftSeconds.value / 60)
  const seconds = timeLeftSeconds.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

/** Placeholder questions */
const placeholderQuestions: ExamQuestion[] = [
  {
    id: 1,
    type: 'single',
    title: '以下哪个是 Python 中创建空列表的正确方式？',
    score: 5,
    options: [
      { label: 'A. list = {}', value: 'A' },
      { label: 'B. list = []', value: 'B' },
      { label: 'C. list = ()', value: 'C' },
      { label: 'D. list = ""', value: 'D' }
    ]
  },
  {
    id: 2,
    type: 'multi',
    title: '以下哪些是 Python 的内置数据类型？',
    score: 10,
    options: [
      { label: 'A. int', value: 'A' },
      { label: 'B. float', value: 'B' },
      { label: 'C. array', value: 'C' },
      { label: 'D. dict', value: 'D' }
    ]
  },
  {
    id: 3,
    type: 'judge',
    title: 'Python 中的元组（tuple）是不可变序列。',
    score: 5,
    options: [
      { label: '正确', value: 'T' },
      { label: '错误', value: 'F' }
    ]
  },
  {
    id: 4,
    type: 'fill',
    title: '获取列表长度的内置函数是 ____。',
    score: 5
  },
  {
    id: 5,
    type: 'essay',
    title: '请简述 Python 中列表推导式的语法和使用场景。',
    score: 15
  }
]

/** Questions from config or placeholder */
const questions = computed<ExamQuestion[]>(() =>
  props.nodeConfig?.questions ?? placeholderQuestions
)

/** Answers array */
const answers = ref<(string | string[])[]>(
  (props.nodeConfig?.questions ?? placeholderQuestions).map(q =>
    q.type === 'multi' ? [] : ''
  )
)

/** Current question */
const currentQuestion = computed<ExamQuestion>(() =>
  questions.value[currentIndex.value] ?? { id: 0, type: 'single', title: '', score: 0 }
)

/** Answered count */
const answeredCount = computed<number>(() =>
  answers.value.filter((_, i) => isAnswered(i)).length
)

/** Check if answered */
function isAnswered(index: number): boolean {
  const ans = answers.value[index]
  if (Array.isArray(ans)) return ans.length > 0
  return String(ans).trim().length > 0
}

/** Question type label */
function questionTypeLabel(type: string): string {
  const labels: Record<string, string> = {
    single: '单选',
    multi: '多选',
    judge: '判断',
    fill: '填空',
    essay: '简答'
  }
  return labels[type] ?? type
}

/** Handle exam submission */
async function handleSubmit() {
  if (isSubmitted.value) return

  const unanswered = questions.value.length - answeredCount.value
  if (unanswered > 0) {
    const confirmed = window.confirm(`还有 ${unanswered} 道题未作答，确定交卷吗？`)
    if (!confirmed) return
  }

  isSubmitted.value = true
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }

  emit('complete')
}

/** Auto-submit when time runs out */
function autoSubmit() {
  if (!isSubmitted.value) {
    isSubmitted.value = true
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
    emit('complete')
  }
}

onMounted(() => {
  timerInterval = setInterval(() => {
    if (timeLeftSeconds.value > 0) {
      timeLeftSeconds.value--
    } else {
      autoSubmit()
    }
  }, 1000)
})

onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
})
</script>

<style scoped>
.exam-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.exam-page__timer-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem var(--spacing-md, 1rem);
  border-bottom: 1px solid var(--color-gray-100, #f1f5f9);
  background: var(--color-white, #ffffff);
  flex-shrink: 0;
}

.exam-page__timer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--color-gray-600, #475569);
}

.exam-page__timer-value {
  font-size: 1.25rem;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--color-gray-800, #1e293b);
}

.exam-page__timer-value--warning {
  color: var(--color-red-600, #dc2626);
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.exam-page__submit-btn {
  padding: 0.5rem 1.5rem;
  font-size: 0.875rem;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border: none;
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.exam-page__submit-btn:hover:not(:disabled) {
  box-shadow: 0 4px 12px -2px rgba(239, 68, 68, 0.4);
}

.exam-page__submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.exam-page__body {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* Sidebar */
.exam-page__sidebar {
  width: 200px;
  padding: var(--spacing-md, 1rem);
  border-right: 1px solid var(--color-gray-100, #f1f5f9);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.exam-page__sidebar-title {
  font-size: 0.8125rem;
  font-weight: 700;
  color: var(--color-gray-700, #334155);
  margin-bottom: var(--spacing-sm, 0.75rem);
}

.exam-page__sidebar-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.375rem;
  flex: 1;
  overflow-y: auto;
  align-content: start;
}

.exam-page__sidebar-item {
  width: 2.25rem;
  height: 2.25rem;
  border-radius: var(--radius-sm, 0.25rem);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  background: var(--color-white, #ffffff);
  color: var(--color-gray-500, #64748b);
  font-size: 0.6875rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.1s ease;
}

.exam-page__sidebar-item--active {
  border-color: var(--color-primary-500, #6366f1);
  background: var(--color-primary-50, #eef2ff);
  color: var(--color-primary-700, #4338ca);
}

.exam-page__sidebar-item--answered {
  background: var(--color-primary-500, #6366f1);
  border-color: var(--color-primary-500, #6366f1);
  color: white;
}

.exam-page__sidebar-stats {
  display: flex;
  gap: var(--spacing-md, 1rem);
  padding-top: var(--spacing-sm, 0.75rem);
  border-top: 1px solid var(--color-gray-100, #f1f5f9);
  margin-top: var(--spacing-sm, 0.75rem);
}

.exam-page__stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.exam-page__stat-value {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
}

.exam-page__stat-label {
  font-size: 0.6875rem;
  color: var(--color-gray-400, #94a3b8);
}

/* Main */
.exam-page__main {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: var(--spacing-lg, 1.5rem);
  overflow-y: auto;
  min-width: 0;
}

.exam-page__question {
  flex: 1;
}

.exam-page__question-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: var(--spacing-md, 1rem);
}

.exam-page__question-badge {
  font-size: 0.6875rem;
  font-weight: 700;
  padding: 0.125rem 0.5rem;
  border-radius: var(--radius-sm, 0.25rem);
  background: var(--color-primary-50, #eef2ff);
  color: var(--color-primary-600, #4f46e5);
}

.exam-page__question-score {
  font-size: 0.75rem;
  color: var(--color-gray-500, #64748b);
}

.exam-page__question-title {
  font-size: 1.0625rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  line-height: 1.6;
  margin-bottom: var(--spacing-lg, 1.5rem);
}

.exam-page__question-num {
  color: var(--color-primary-500, #6366f1);
  margin-right: 0.25rem;
}

/* Options */
.exam-page__options {
  display: flex;
  flex-direction: column;
  gap: 0.625rem;
}

.exam-page__option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.exam-page__option:hover {
  border-color: var(--color-primary-200, #c7d2fe);
}

.exam-page__option--selected {
  border-color: var(--color-primary-500, #6366f1);
  background: var(--color-primary-50, #eef2ff);
}

.exam-page__option-input {
  display: none;
}

.exam-page__option-indicator {
  width: 1rem;
  height: 1rem;
  border-radius: 50%;
  border: 2px solid var(--color-gray-300, #cbd5e1);
  flex-shrink: 0;
}

.exam-page__option-indicator--checkbox {
  border-radius: var(--radius-sm, 0.25rem);
}

.exam-page__option--selected .exam-page__option-indicator {
  border-color: var(--color-primary-500, #6366f1);
  background: var(--color-primary-500, #6366f1);
}

.exam-page__option-text {
  font-size: 0.875rem;
  color: var(--color-gray-700, #334155);
}

/* Essay & Fill */
.exam-page__textarea {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  font-size: 0.875rem;
  line-height: 1.6;
  color: var(--color-gray-700, #334155);
  resize: vertical;
  outline: none;
}

.exam-page__textarea:focus {
  border-color: var(--color-primary-400, #818cf8);
}

.exam-page__fill-input {
  width: 100%;
  max-width: 400px;
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  font-size: 0.875rem;
  color: var(--color-gray-700, #334155);
  outline: none;
}

.exam-page__fill-input:focus {
  border-color: var(--color-primary-400, #818cf8);
}

/* Navigation */
.exam-page__nav {
  display: flex;
  justify-content: space-between;
  padding-top: var(--spacing-md, 1rem);
  border-top: 1px solid var(--color-gray-100, #f1f5f9);
  margin-top: var(--spacing-lg, 1.5rem);
}

.exam-page__nav-btn {
  padding: 0.5rem 1rem;
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-gray-600, #475569);
  background: var(--color-gray-50, #f8fafc);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.exam-page__nav-btn:hover:not(:disabled) {
  background: var(--color-gray-100, #f1f5f9);
}

.exam-page__nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}
</style>
