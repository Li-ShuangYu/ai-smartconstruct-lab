<template>
  <div class="learning-survey">
    <!-- Survey Header -->
    <section class="learning-survey__header">
      <h2 class="learning-survey__title">{{ surveyTitle }}</h2>
      <p class="learning-survey__desc">{{ surveyDescription }}</p>
      <div class="learning-survey__progress-bar">
        <div
          class="learning-survey__progress-fill"
          :style="{ width: `${progressPercent}%` }"
        ></div>
      </div>
      <span class="learning-survey__progress-text">
        已完成 {{ answeredCount }} / {{ questions.length }} 题
      </span>
    </section>

    <!-- Questions -->
    <section class="learning-survey__questions">
      <div
        v-for="(question, index) in questions"
        :key="question.id"
        class="learning-survey__question"
      >
        <div class="learning-survey__question-header">
          <span class="learning-survey__question-num">{{ index + 1 }}</span>
          <span class="learning-survey__question-text">{{ question.title }}</span>
          <span v-if="question.required" class="learning-survey__required">*</span>
        </div>

        <!-- Radio type -->
        <div v-if="question.type === 'radio'" class="learning-survey__options">
          <label
            v-for="option in question.options"
            :key="option.value"
            class="learning-survey__radio-option"
            :class="{ 'learning-survey__radio-option--selected': answers[question.id] === option.value }"
          >
            <input
              type="radio"
              :name="`q_${question.id}`"
              :value="option.value"
              class="learning-survey__radio-input"
              @change="setAnswer(question.id, option.value)"
            />
            <span class="learning-survey__radio-dot"></span>
            <span class="learning-survey__option-label">{{ option.label }}</span>
          </label>
        </div>

        <!-- Checkbox type -->
        <div v-if="question.type === 'checkbox'" class="learning-survey__options">
          <label
            v-for="option in question.options"
            :key="option.value"
            class="learning-survey__checkbox-option"
            :class="{ 'learning-survey__checkbox-option--selected': isChecked(question.id, option.value) }"
          >
            <input
              type="checkbox"
              :value="option.value"
              class="learning-survey__checkbox-input"
              :checked="isChecked(question.id, option.value)"
              @change="toggleCheckbox(question.id, option.value)"
            />
            <span class="learning-survey__checkbox-box"></span>
            <span class="learning-survey__option-label">{{ option.label }}</span>
          </label>
        </div>

        <!-- Scale type (1-5 or 1-10) -->
        <div v-if="question.type === 'scale'" class="learning-survey__scale">
          <span class="learning-survey__scale-label">{{ question.scaleMin ?? '非常不同意' }}</span>
          <div class="learning-survey__scale-dots">
            <button
              v-for="n in (question.scaleMax ?? 5)"
              :key="n"
              class="learning-survey__scale-dot"
              :class="{ 'learning-survey__scale-dot--active': answers[question.id] === n }"
              @click="setAnswer(question.id, n)"
            >
              {{ n }}
            </button>
          </div>
          <span class="learning-survey__scale-label">{{ question.scaleMaxLabel ?? '非常同意' }}</span>
        </div>

        <!-- Text type -->
        <div v-if="question.type === 'text'" class="learning-survey__text-wrap">
          <textarea
            class="learning-survey__textarea"
            :placeholder="question.placeholder ?? '请输入您的回答...'"
            :value="(answers[question.id] as string) ?? ''"
            rows="3"
            @input="setAnswer(question.id, ($event.target as HTMLTextAreaElement).value)"
          ></textarea>
        </div>
      </div>
    </section>

    <!-- Submit -->
    <section class="learning-survey__footer">
      <button
        class="learning-survey__submit-btn"
        :disabled="!canSubmit"
        @click="handleSubmit"
      >
        提交问卷
      </button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface SurveyOption {
  value: string
  label: string
}

interface SurveyQuestion {
  id: string
  title: string
  type: 'radio' | 'checkbox' | 'scale' | 'text'
  required: boolean
  options?: SurveyOption[]
  scaleMax?: number
  scaleMin?: string
  scaleMaxLabel?: string
  placeholder?: string
}

interface SurveyNodeConfig {
  display?: {
    survey_title?: string
    survey_description?: string
  }
  data_collection?: {
    questions?: SurveyQuestion[]
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: SurveyNodeConfig
}>()

const emit = defineEmits<{
  complete: []
}>()

const surveyTitle = computed<string>(() =>
  props.nodeConfig.display?.survey_title ?? '学情调查问卷'
)

const surveyDescription = computed<string>(() =>
  props.nodeConfig.display?.survey_description ?? '请认真填写以下问卷，帮助我们了解您的学习情况。'
)

/** Questions from config or placeholder */
const questions = computed<SurveyQuestion[]>(() =>
  props.nodeConfig.data_collection?.questions ?? placeholderQuestions
)

const placeholderQuestions: SurveyQuestion[] = [
  {
    id: 'q1',
    title: '您之前是否有编程经验？',
    type: 'radio',
    required: true,
    options: [
      { value: 'none', label: '完全没有' },
      { value: 'basic', label: '了解基础概念' },
      { value: 'intermediate', label: '有一定项目经验' },
      { value: 'advanced', label: '熟练掌握' }
    ]
  },
  {
    id: 'q2',
    title: '您希望通过本次实训掌握哪些技能？（可多选）',
    type: 'checkbox',
    required: true,
    options: [
      { value: 'syntax', label: '编程语法' },
      { value: 'algorithm', label: '算法思维' },
      { value: 'project', label: '项目实战' },
      { value: 'teamwork', label: '团队协作' }
    ]
  },
  {
    id: 'q3',
    title: '您对本课程的学习信心程度',
    type: 'scale',
    required: true,
    scaleMax: 5,
    scaleMin: '完全没信心',
    scaleMaxLabel: '非常有信心'
  },
  {
    id: 'q4',
    title: '您对本次实训有什么期望或建议？',
    type: 'text',
    required: false,
    placeholder: '请输入您的期望或建议...'
  }
]

/** Answers map: questionId -> answer value */
const answers = ref<Record<string, string | number | string[]>>({})

/** Set answer for a question */
function setAnswer(questionId: string, value: string | number) {
  answers.value[questionId] = value
}

/** Check if a checkbox option is checked */
function isChecked(questionId: string, optionValue: string): boolean {
  const current = answers.value[questionId]
  if (Array.isArray(current)) {
    return current.includes(optionValue)
  }
  return false
}

/** Toggle checkbox option */
function toggleCheckbox(questionId: string, optionValue: string) {
  const current = answers.value[questionId]
  if (Array.isArray(current)) {
    const idx = current.indexOf(optionValue)
    if (idx >= 0) {
      current.splice(idx, 1)
    } else {
      current.push(optionValue)
    }
  } else {
    answers.value[questionId] = [optionValue]
  }
}

/** Count of answered questions */
const answeredCount = computed<number>(() => {
  return questions.value.filter(q => {
    const answer = answers.value[q.id]
    if (answer === undefined || answer === null || answer === '') return false
    if (Array.isArray(answer) && answer.length === 0) return false
    return true
  }).length
})

/** Progress percentage */
const progressPercent = computed<number>(() => {
  if (questions.value.length === 0) return 0
  return Math.round((answeredCount.value / questions.value.length) * 100)
})

/** Can submit: all required questions answered */
const canSubmit = computed<boolean>(() => {
  return questions.value
    .filter(q => q.required)
    .every(q => {
      const answer = answers.value[q.id]
      if (answer === undefined || answer === null || answer === '') return false
      if (Array.isArray(answer) && answer.length === 0) return false
      return true
    })
})

/** Handle submit */
function handleSubmit() {
  if (!canSubmit.value) return
  emit('complete')
}
</script>

<style scoped>
.learning-survey {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
}

.learning-survey__header {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.learning-survey__title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: 0.5rem;
}

.learning-survey__desc {
  font-size: 0.875rem;
  color: var(--color-gray-500, #64748b);
  margin-bottom: var(--spacing-md, 1rem);
}

.learning-survey__progress-bar {
  height: 4px;
  background: var(--color-gray-100, #f1f5f9);
  border-radius: 2px;
  overflow: hidden;
}

.learning-survey__progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  transition: width 0.3s ease;
}

.learning-survey__progress-text {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
  margin-top: 0.5rem;
  display: block;
}

.learning-survey__questions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md, 1rem);
}

.learning-survey__question {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.learning-survey__question-header {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  margin-bottom: var(--spacing-md, 1rem);
}

.learning-survey__question-num {
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

.learning-survey__question-text {
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  flex: 1;
}

.learning-survey__required {
  color: #ef4444;
  font-weight: 700;
}

.learning-survey__options {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.learning-survey__radio-option,
.learning-survey__checkbox-option {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  padding: 0.625rem 0.875rem;
  border: 1px solid var(--color-gray-100, #f1f5f9);
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.learning-survey__radio-option:hover,
.learning-survey__checkbox-option:hover {
  border-color: var(--color-primary-200, #c7d2fe);
  background: var(--color-primary-50, #eef2ff);
}

.learning-survey__radio-option--selected,
.learning-survey__checkbox-option--selected {
  border-color: var(--color-primary-300, #a5b4fc);
  background: var(--color-primary-50, #eef2ff);
}

.learning-survey__radio-input,
.learning-survey__checkbox-input {
  display: none;
}

.learning-survey__radio-dot {
  width: 1rem;
  height: 1rem;
  border-radius: 50%;
  border: 2px solid var(--color-gray-300, #cbd5e1);
  flex-shrink: 0;
  position: relative;
}

.learning-survey__radio-option--selected .learning-survey__radio-dot {
  border-color: var(--color-primary-500, #6366f1);
}

.learning-survey__radio-option--selected .learning-survey__radio-dot::after {
  content: '';
  position: absolute;
  top: 3px;
  left: 3px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary-500, #6366f1);
}

.learning-survey__checkbox-box {
  width: 1rem;
  height: 1rem;
  border-radius: 3px;
  border: 2px solid var(--color-gray-300, #cbd5e1);
  flex-shrink: 0;
}

.learning-survey__checkbox-option--selected .learning-survey__checkbox-box {
  border-color: var(--color-primary-500, #6366f1);
  background: var(--color-primary-500, #6366f1);
}

.learning-survey__option-label {
  font-size: 0.875rem;
  color: var(--color-gray-700, #334155);
}

.learning-survey__scale {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm, 0.75rem);
}

.learning-survey__scale-label {
  font-size: 0.75rem;
  color: var(--color-gray-500, #64748b);
  white-space: nowrap;
}

.learning-survey__scale-dots {
  display: flex;
  gap: 0.5rem;
  flex: 1;
  justify-content: center;
}

.learning-survey__scale-dot {
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 50%;
  border: 2px solid var(--color-gray-200, #e2e8f0);
  background: var(--color-white, #ffffff);
  color: var(--color-gray-600, #475569);
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.learning-survey__scale-dot:hover {
  border-color: var(--color-primary-300, #a5b4fc);
}

.learning-survey__scale-dot--active {
  border-color: var(--color-primary-500, #6366f1);
  background: var(--color-primary-500, #6366f1);
  color: white;
}

.learning-survey__text-wrap {
  width: 100%;
}

.learning-survey__textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  font-size: 0.875rem;
  color: var(--color-gray-800, #1e293b);
  resize: vertical;
  font-family: inherit;
}

.learning-survey__textarea:focus {
  outline: none;
  border-color: var(--color-primary-300, #a5b4fc);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.learning-survey__footer {
  padding-top: var(--spacing-md, 1rem);
  border-top: 1px solid var(--color-gray-100, #f1f5f9);
}

.learning-survey__submit-btn {
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

.learning-survey__submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px -4px rgba(99, 102, 241, 0.4);
}

.learning-survey__submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
