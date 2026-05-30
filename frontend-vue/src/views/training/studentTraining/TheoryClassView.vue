<template>
  <div class="theory-class">
    <!-- Slide Content Area -->
    <div class="theory-class__content">
      <div v-if="currentSlide" class="theory-class__slide">
        <!-- Intro / Theory Slide -->
        <template v-if="currentSlide.type === 'intro' || currentSlide.type === 'theory'">
          <h2 class="theory-class__slide-title">{{ currentSlide.title }}</h2>
          <p class="theory-class__slide-text">{{ currentSlide.content }}</p>
          <ul v-if="currentSlide.bullet_points?.length" class="theory-class__bullet-list">
            <li
              v-for="(point, idx) in currentSlide.bullet_points"
              :key="idx"
              class="theory-class__bullet-item"
            >
              {{ point }}
            </li>
          </ul>
        </template>

        <!-- Code Slide -->
        <template v-if="currentSlide.type === 'code'">
          <h2 class="theory-class__slide-title">{{ currentSlide.title }}</h2>
          <p class="theory-class__slide-text">{{ currentSlide.content }}</p>
          <div class="theory-class__code-block">
            <div class="theory-class__code-header">
              <span class="theory-class__code-lang">Python</span>
            </div>
            <pre class="theory-class__code-content"><code>{{ currentSlide.code }}</code></pre>
          </div>
          <div v-if="currentSlide.output" class="theory-class__output-block">
            <div class="theory-class__output-header">运行结果</div>
            <pre class="theory-class__output-content">{{ currentSlide.output }}</pre>
          </div>
        </template>

        <!-- Quiz Slide -->
        <template v-if="currentSlide.type === 'quiz'">
          <h2 class="theory-class__slide-title">{{ currentSlide.title }}</h2>
          <p class="theory-class__slide-text">{{ currentSlide.content }}</p>
          <div
            v-for="(q, qIdx) in currentSlide.questions"
            :key="qIdx"
            class="theory-class__quiz-question"
          >
            <p class="theory-class__quiz-stem">{{ qIdx + 1 }}. {{ q.question }}</p>
            <div class="theory-class__quiz-options">
              <button
                v-for="(option, oIdx) in q.options"
                :key="oIdx"
                class="theory-class__quiz-option"
                :class="{
                  'theory-class__quiz-option--selected': quizAnswers[qIdx] === option,
                  'theory-class__quiz-option--correct': quizSubmitted && option === q.answer,
                  'theory-class__quiz-option--incorrect': quizSubmitted && quizAnswers[qIdx] === option && option !== q.answer
                }"
                :disabled="quizSubmitted"
                @click="selectAnswer(qIdx, option)"
              >
                {{ option }}
              </button>
            </div>
          </div>
          <div class="theory-class__quiz-actions">
            <button
              v-if="!quizSubmitted"
              class="theory-class__quiz-submit"
              :disabled="!allQuestionsAnswered"
              @click="submitQuiz"
            >
              提交答案
            </button>
            <div v-else class="theory-class__quiz-result">
              <span class="theory-class__quiz-score">
                得分：{{ quizScore }} / {{ currentSlide.questions?.length ?? 0 }}
              </span>
            </div>
          </div>
        </template>

        <!-- Summary Slide -->
        <template v-if="currentSlide.type === 'summary'">
          <h2 class="theory-class__slide-title">{{ currentSlide.title }}</h2>
          <p class="theory-class__slide-text">{{ currentSlide.content }}</p>
          <div v-if="currentSlide.key_points?.length" class="theory-class__key-points">
            <h4 class="theory-class__key-points-title">关键知识点</h4>
            <ul class="theory-class__key-points-list">
              <li
                v-for="(point, idx) in currentSlide.key_points"
                :key="idx"
                class="theory-class__key-point-item"
              >
                {{ point }}
              </li>
            </ul>
          </div>
        </template>
      </div>
    </div>

    <!-- Navigation Bar -->
    <div class="theory-class__nav">
      <button
        class="theory-class__nav-btn theory-class__nav-btn--prev"
        :disabled="currentIndex === 0"
        @click="goToPrev"
      >
        ← 上一页
      </button>

      <span class="theory-class__progress">
        {{ currentIndex + 1 }} / {{ totalSlides }}
      </span>

      <button
        v-if="!isLastSlide"
        class="theory-class__nav-btn theory-class__nav-btn--next"
        :disabled="isQuizSlideNotSubmitted"
        @click="goToNext"
      >
        下一页 →
      </button>
      <button
        v-else
        class="theory-class__nav-btn theory-class__nav-btn--complete"
        :disabled="isQuizSlideNotSubmitted"
        @click="handleComplete"
      >
        完成 ✓
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Slide } from '@/services/types/studentTraining'

interface TheoryClassConfig {
  slides: Slide[]
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: TheoryClassConfig
}>()

const emit = defineEmits<{
  complete: []
}>()

// ─── State ────────────────────────────────────────────────────────────────────

const currentIndex = ref(0)
const quizAnswers = ref<Record<number, string>>({})
const quizSubmitted = ref(false)
const quizScore = ref(0)

// ─── Computed ─────────────────────────────────────────────────────────────────

const slides = computed<Slide[]>(() => props.nodeConfig.slides ?? [])

const totalSlides = computed<number>(() => slides.value.length)

const currentSlide = computed<Slide | undefined>(() => slides.value[currentIndex.value])

const isLastSlide = computed<boolean>(() => currentIndex.value === totalSlides.value - 1)

const allQuestionsAnswered = computed<boolean>(() => {
  if (!currentSlide.value || currentSlide.value.type !== 'quiz') return false
  const questions = currentSlide.value.questions ?? []
  return questions.length > 0 && questions.every((_, idx) => quizAnswers.value[idx] !== undefined)
})

const isQuizSlideNotSubmitted = computed<boolean>(() => {
  return currentSlide.value?.type === 'quiz' && !quizSubmitted.value
})

// ─── Methods ──────────────────────────────────────────────────────────────────

function goToPrev() {
  if (currentIndex.value > 0) {
    currentIndex.value--
    resetQuizState()
  }
}

function goToNext() {
  if (currentIndex.value < totalSlides.value - 1) {
    currentIndex.value++
    resetQuizState()
  }
}

function resetQuizState() {
  quizAnswers.value = {}
  quizSubmitted.value = false
  quizScore.value = 0
}

function selectAnswer(questionIndex: number, option: string) {
  quizAnswers.value = { ...quizAnswers.value, [questionIndex]: option }
}

function submitQuiz() {
  if (!currentSlide.value || currentSlide.value.type !== 'quiz') return
  const questions = currentSlide.value.questions ?? []
  let score = 0
  questions.forEach((q, idx) => {
    if (quizAnswers.value[idx] === q.answer) {
      score++
    }
  })
  quizScore.value = score
  quizSubmitted.value = true
}

function handleComplete() {
  emit('complete')
}
</script>

<style scoped>
.theory-class {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background: var(--color-white, #ffffff);
  border-radius: var(--radius-lg, 0.75rem);
}

.theory-class__content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: var(--spacing-xl, 2rem) var(--spacing-lg, 1.5rem);
}

.theory-class__slide {
  max-width: 48rem;
  margin: 0 auto;
}

/* ─── Slide Title & Text ─────────────────────────────────────────────────── */

.theory-class__slide-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-md, 1rem);
}

.theory-class__slide-text {
  font-size: 1rem;
  line-height: 1.75;
  color: var(--color-gray-600, #475569);
  margin-bottom: var(--spacing-md, 1rem);
}

/* ─── Bullet Points (intro/theory) ───────────────────────────────────────── */

.theory-class__bullet-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.theory-class__bullet-item {
  position: relative;
  padding-left: 1.25rem;
  font-size: 0.9375rem;
  line-height: 1.6;
  color: var(--color-gray-700, #334155);
}

.theory-class__bullet-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.6em;
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background: var(--color-primary-400, #818cf8);
}

/* ─── Code Block ─────────────────────────────────────────────────────────── */

.theory-class__code-block {
  margin-top: var(--spacing-md, 1rem);
  border-radius: var(--radius-md, 0.5rem);
  overflow: hidden;
  border: 1px solid var(--color-gray-200, #e2e8f0);
}

.theory-class__code-header {
  display: flex;
  align-items: center;
  padding: 0.5rem 1rem;
  background: var(--color-gray-800, #1e293b);
  border-bottom: 1px solid var(--color-gray-700, #334155);
}

.theory-class__code-lang {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-gray-400, #94a3b8);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.theory-class__code-content {
  margin: 0;
  padding: 1rem;
  background: var(--color-gray-900, #0f172a);
  color: var(--color-gray-100, #f1f5f9);
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 0.8125rem;
  line-height: 1.6;
  overflow-x: auto;
  white-space: pre-wrap;
}

.theory-class__output-block {
  margin-top: var(--spacing-sm, 0.75rem);
  border-radius: var(--radius-md, 0.5rem);
  overflow: hidden;
  border: 1px solid var(--color-gray-200, #e2e8f0);
}

.theory-class__output-header {
  padding: 0.375rem 1rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-gray-500, #64748b);
  background: var(--color-gray-50, #f8fafc);
  border-bottom: 1px solid var(--color-gray-200, #e2e8f0);
}

.theory-class__output-content {
  margin: 0;
  padding: 0.75rem 1rem;
  background: var(--color-gray-50, #f8fafc);
  color: var(--color-gray-700, #334155);
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 0.8125rem;
  line-height: 1.5;
  white-space: pre-wrap;
}

/* ─── Quiz ───────────────────────────────────────────────────────────────── */

.theory-class__quiz-question {
  margin-bottom: var(--spacing-lg, 1.5rem);
}

.theory-class__quiz-stem {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-sm, 0.75rem);
}

.theory-class__quiz-options {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.theory-class__quiz-option {
  display: block;
  width: 100%;
  text-align: left;
  padding: 0.75rem 1rem;
  font-size: 0.9375rem;
  color: var(--color-gray-700, #334155);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.theory-class__quiz-option:hover:not(:disabled) {
  border-color: var(--color-primary-300, #a5b4fc);
  background: var(--color-primary-50, #eef2ff);
}

.theory-class__quiz-option--selected {
  border-color: var(--color-primary-500, #6366f1);
  background: var(--color-primary-50, #eef2ff);
  color: var(--color-primary-700, #4338ca);
  font-weight: 600;
}

.theory-class__quiz-option--correct {
  border-color: var(--color-success-500, #22c55e);
  background: var(--color-success-50, #f0fdf4);
  color: var(--color-success-700, #15803d);
}

.theory-class__quiz-option--incorrect {
  border-color: var(--color-error-500, #ef4444);
  background: var(--color-error-50, #fef2f2);
  color: var(--color-error-700, #b91c1c);
}

.theory-class__quiz-option:disabled {
  cursor: default;
}

.theory-class__quiz-actions {
  margin-top: var(--spacing-md, 1rem);
  display: flex;
  align-items: center;
}

.theory-class__quiz-submit {
  padding: 0.625rem 1.5rem;
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--color-white, #ffffff);
  background: var(--color-primary-500, #6366f1);
  border: none;
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.theory-class__quiz-submit:hover:not(:disabled) {
  background: var(--color-primary-600, #4f46e5);
}

.theory-class__quiz-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.theory-class__quiz-result {
  padding: 0.5rem 1rem;
  background: var(--color-success-50, #f0fdf4);
  border: 1px solid var(--color-success-200, #bbf7d0);
  border-radius: var(--radius-md, 0.5rem);
}

.theory-class__quiz-score {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-success-700, #15803d);
}

/* ─── Key Points (summary) ───────────────────────────────────────────────── */

.theory-class__key-points {
  margin-top: var(--spacing-md, 1rem);
  padding: var(--spacing-md, 1rem);
  background: var(--color-primary-50, #eef2ff);
  border: 1px solid var(--color-primary-100, #e0e7ff);
  border-radius: var(--radius-md, 0.5rem);
}

.theory-class__key-points-title {
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--color-primary-700, #4338ca);
  margin-bottom: 0.5rem;
}

.theory-class__key-points-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.theory-class__key-point-item {
  position: relative;
  padding-left: 1.25rem;
  font-size: 0.9375rem;
  line-height: 1.5;
  color: var(--color-primary-800, #3730a3);
}

.theory-class__key-point-item::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: var(--color-primary-500, #6366f1);
  font-weight: 700;
}

/* ─── Navigation Bar ─────────────────────────────────────────────────────── */

.theory-class__nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md, 1rem) var(--spacing-lg, 1.5rem);
  border-top: 1px solid var(--color-gray-200, #e2e8f0);
  background: var(--color-gray-50, #f8fafc);
}

.theory-class__progress {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-gray-500, #64748b);
}

.theory-class__nav-btn {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
  font-weight: 600;
  border: 1px solid var(--color-gray-300, #cbd5e1);
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  transition: all 0.15s ease;
  background: var(--color-white, #ffffff);
  color: var(--color-gray-700, #334155);
}

.theory-class__nav-btn:hover:not(:disabled) {
  border-color: var(--color-primary-300, #a5b4fc);
  color: var(--color-primary-600, #4f46e5);
}

.theory-class__nav-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.theory-class__nav-btn--complete {
  background: var(--color-primary-500, #6366f1);
  color: var(--color-white, #ffffff);
  border-color: var(--color-primary-500, #6366f1);
}

.theory-class__nav-btn--complete:hover:not(:disabled) {
  background: var(--color-primary-600, #4f46e5);
  border-color: var(--color-primary-600, #4f46e5);
  color: var(--color-white, #ffffff);
}
</style>
