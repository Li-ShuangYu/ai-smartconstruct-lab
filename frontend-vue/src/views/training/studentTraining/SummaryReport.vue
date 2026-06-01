<template>
  <div class="summary-report">
    <!-- Header -->
    <header class="summary-report__header">
      <div class="summary-report__badge-icon">
        <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
        </svg>
      </div>
      <h1 class="summary-report__title">实训学习报告</h1>
      <p class="summary-report__subtitle">恭喜完成本次实训！以下是您的学习成果总结。</p>
    </header>

    <!-- AI Learning Report -->
    <section class="summary-report__section">
      <h2 class="summary-report__section-title">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M13 10V3L4 14h7v7l9-11h-7z" />
        </svg>
        AI 学习报告
      </h2>
      <div class="summary-report__ai-report">
        <p class="summary-report__ai-text">{{ aiReport }}</p>
      </div>
    </section>

    <!-- Radar Chart (Ability Dimensions) -->
    <section class="summary-report__section">
      <h2 class="summary-report__section-title">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z" />
        </svg>
        能力雷达图
      </h2>
      <div class="summary-report__radar">
        <div class="summary-report__radar-placeholder">
          <svg class="w-12 h-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
              d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
              d="M20.488 9H15V3.512A9.025 9.025 0 0120.488 9z" />
          </svg>
          <p>雷达图渲染区域</p>
        </div>
        <!-- Dimension scores list -->
        <div class="summary-report__dimensions">
          <div
            v-for="dim in abilityDimensions"
            :key="dim.name"
            class="summary-report__dimension"
          >
            <span class="summary-report__dimension-name">{{ dim.name }}</span>
            <div class="summary-report__dimension-bar">
              <div
                class="summary-report__dimension-fill"
                :style="{ width: `${dim.score}%` }"
              ></div>
            </div>
            <span class="summary-report__dimension-score">{{ dim.score }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Achievement Badges -->
    <section class="summary-report__section">
      <h2 class="summary-report__section-title">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
        </svg>
        成就徽章
      </h2>
      <div class="summary-report__badges">
        <div
          v-for="badge in achievementBadges"
          :key="badge.id"
          class="summary-report__badge-card"
          :class="{ 'summary-report__badge-card--earned': badge.earned }"
        >
          <div class="summary-report__badge-emoji">{{ badge.icon }}</div>
          <span class="summary-report__badge-name">{{ badge.name }}</span>
          <span class="summary-report__badge-desc">{{ badge.description }}</span>
        </div>
      </div>
    </section>

    <!-- Satisfaction Survey -->
    <section v-if="showSurvey" class="summary-report__section">
      <h2 class="summary-report__section-title">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        满意度调查
      </h2>
      <div class="summary-report__survey">
        <div
          v-for="item in surveyItems"
          :key="item.id"
          class="summary-report__survey-item"
        >
          <label class="summary-report__survey-label">{{ item.label }}</label>
          <div class="summary-report__survey-stars">
            <button
              v-for="star in 5"
              :key="star"
              class="summary-report__star"
              :class="{ 'summary-report__star--active': (surveyAnswers[item.id] ?? 0) >= star }"
              @click="surveyAnswers[item.id] = star"
            >
              ★
            </button>
          </div>
        </div>

        <div class="summary-report__survey-comment">
          <label class="summary-report__survey-label">其他建议</label>
          <textarea
            v-model="surveyComment"
            class="summary-report__survey-textarea"
            placeholder="请输入您的建议或反馈..."
            rows="3"
          ></textarea>
        </div>

        <button
          class="summary-report__survey-submit"
          :disabled="isSurveySubmitted"
          @click="handleSurveySubmit"
        >
          {{ isSurveySubmitted ? '已提交' : '提交评价' }}
        </button>
      </div>
    </section>

    <!-- Complete Button -->
    <div class="summary-report__actions">
      <button class="summary-report__complete-btn" @click="handleCompleteAction">实训完成，进入下一阶段</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'

interface AbilityDimension {
  name: string
  score: number
}

interface AchievementBadge {
  id: string
  name: string
  description: string
  icon: string
  earned: boolean
}

interface SurveyItem {
  id: string
  label: string
}

interface SummaryConfig {
  enable_ai_report?: boolean
  enable_badges?: boolean
  enable_satisfaction_survey?: boolean
  ai_report_text?: string
  ability_dimensions?: AbilityDimension[]
  badges?: AchievementBadge[]
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: SummaryConfig
}>()

const emit = defineEmits<{
  complete: []
}>()

/** AI report text */
const aiReport = computed<string>(() =>
  props.nodeConfig.ai_report_text ??
  '您在本次实训中表现优秀。在编码实践环节展现了扎实的基础功底，代码结构清晰、逻辑严谨。建议在算法优化和设计模式方面继续深入学习，以进一步提升工程能力。'
)

/** Ability dimensions for radar chart */
const abilityDimensions = computed<AbilityDimension[]>(() =>
  props.nodeConfig.ability_dimensions ?? [
    { name: '编码能力', score: 85 },
    { name: '逻辑思维', score: 78 },
    { name: '知识掌握', score: 92 },
    { name: '协作能力', score: 70 },
    { name: '创新思维', score: 65 },
    { name: '文档能力', score: 80 }
  ]
)

/** Achievement badges */
const achievementBadges = computed<AchievementBadge[]>(() =>
  props.nodeConfig.badges ?? [
    { id: 'b1', name: '全勤达人', description: '按时完成所有节点', icon: '🏆', earned: true },
    { id: 'b2', name: '代码之星', description: '编码实训获得90+', icon: '⭐', earned: true },
    { id: 'b3', name: '互评先锋', description: '互评质量排名前10%', icon: '🎯', earned: false },
    { id: 'b4', name: '速度之王', description: '最快完成实训', icon: '⚡', earned: false }
  ]
)

/** Whether to show satisfaction survey */
const showSurvey = computed<boolean>(() =>
  props.nodeConfig.enable_satisfaction_survey !== false
)

/** Survey items */
const surveyItems: SurveyItem[] = [
  { id: 'content', label: '实训内容质量' },
  { id: 'difficulty', label: '难度适中程度' },
  { id: 'ai_help', label: 'AI辅助有效性' },
  { id: 'overall', label: '整体满意度' }
]

/** Survey answers (star ratings) */
const surveyAnswers = reactive<Record<string, number>>({})

/** Survey comment */
const surveyComment = ref<string>('')

/** Survey submission state */
const isSurveySubmitted = ref<boolean>(false)

/** Handle survey submission */
function handleSurveySubmit() {
  isSurveySubmitted.value = true
  emit('complete')
}

function handleCompleteAction() {
  emit('complete')
}
</script>

<style scoped>
.summary-report {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
}

.summary-report__header {
  text-align: center;
  padding: var(--spacing-lg, 1.5rem) 0;
}

.summary-report__badge-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 4rem;
  height: 4rem;
  border-radius: 50%;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: var(--color-amber-600, #d97706);
  margin-bottom: 0.75rem;
}

.summary-report__title {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: 0.5rem;
}

.summary-report__subtitle {
  font-size: 0.875rem;
  color: var(--color-gray-500, #64748b);
}

/* Sections */
.summary-report__section {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-100, #f1f5f9);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-md, 1rem) var(--spacing-lg, 1.5rem);
}

.summary-report__section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-md, 1rem);
}

/* AI Report */
.summary-report__ai-report {
  background: var(--color-primary-50, #eef2ff);
  border: 1px solid var(--color-primary-100, #e0e7ff);
  border-radius: var(--radius-md, 0.5rem);
  padding: var(--spacing-md, 1rem);
}

.summary-report__ai-text {
  font-size: 0.875rem;
  line-height: 1.7;
  color: var(--color-gray-700, #334155);
}

/* Radar */
.summary-report__radar {
  display: flex;
  gap: var(--spacing-lg, 1.5rem);
  align-items: flex-start;
}

.summary-report__radar-placeholder {
  width: 200px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  background: var(--color-gray-50, #f8fafc);
  border: 1px dashed var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  color: var(--color-gray-400, #94a3b8);
  font-size: 0.75rem;
  flex-shrink: 0;
}

.summary-report__dimensions {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.625rem;
}

.summary-report__dimension {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.summary-report__dimension-name {
  width: 5rem;
  font-size: 0.8125rem;
  color: var(--color-gray-600, #475569);
  flex-shrink: 0;
}

.summary-report__dimension-bar {
  flex: 1;
  height: 0.5rem;
  background: var(--color-gray-100, #f1f5f9);
  border-radius: var(--radius-full, 9999px);
  overflow: hidden;
}

.summary-report__dimension-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: var(--radius-full, 9999px);
  transition: width 0.5s ease;
}

.summary-report__dimension-score {
  width: 2rem;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
  text-align: right;
}

/* Badges */
.summary-report__badges {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 0.75rem;
}

.summary-report__badge-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.375rem;
  padding: var(--spacing-md, 1rem);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  text-align: center;
  opacity: 0.4;
  filter: grayscale(1);
  transition: all 0.2s ease;
}

.summary-report__badge-card--earned {
  opacity: 1;
  filter: none;
  border-color: var(--color-amber-200, #fde68a);
  background: var(--color-amber-50, #fffbeb);
}

.summary-report__badge-emoji {
  font-size: 1.5rem;
}

.summary-report__badge-name {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--color-gray-700, #334155);
}

.summary-report__badge-desc {
  font-size: 0.6875rem;
  color: var(--color-gray-400, #94a3b8);
}

/* Survey */
.summary-report__survey {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md, 1rem);
}

.summary-report__survey-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.summary-report__survey-label {
  font-size: 0.875rem;
  color: var(--color-gray-700, #334155);
}

.summary-report__survey-stars {
  display: flex;
  gap: 0.25rem;
}

.summary-report__star {
  font-size: 1.25rem;
  color: var(--color-gray-200, #e2e8f0);
  background: none;
  border: none;
  cursor: pointer;
  transition: color 0.1s ease;
}

.summary-report__star--active {
  color: var(--color-amber-400, #fbbf24);
}

.summary-report__star:hover {
  color: var(--color-amber-300, #fcd34d);
}

.summary-report__survey-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  font-size: 0.8125rem;
  color: var(--color-gray-700, #334155);
  resize: vertical;
  outline: none;
}

.summary-report__survey-textarea:focus {
  border-color: var(--color-primary-400, #818cf8);
}

.summary-report__survey-submit {
  align-self: flex-end;
  padding: 0.625rem 1.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  border: none;
  border-radius: var(--radius-md, 0.5rem);
  cursor: pointer;
  transition: all 0.15s ease;
}

.summary-report__survey-submit:hover:not(:disabled) {
  box-shadow: 0 4px 12px -2px rgba(99, 102, 241, 0.4);
}

.summary-report__survey-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.summary-report__actions {
  text-align: center;
  padding: 16px 0;
}

.summary-report__complete-btn {
  padding: 12px 48px;
  font-size: 15px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #10b981, #059669);
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.summary-report__complete-btn:hover {
  box-shadow: 0 4px 12px -2px rgba(16, 185, 129, 0.4);
}
</style>
