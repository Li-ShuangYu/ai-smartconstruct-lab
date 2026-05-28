<template>
  <div class="teacher-comment">
    <!-- Header -->
    <section class="teacher-comment__header">
      <h2 class="teacher-comment__title">教师总评</h2>
      <p class="teacher-comment__desc">查看教师对您本次实训的综合评价和最终成绩。</p>
    </section>

    <!-- Score Summary -->
    <section class="teacher-comment__score-summary">
      <div class="teacher-comment__total-score">
        <span class="teacher-comment__score-value">{{ totalScore }}</span>
        <span class="teacher-comment__score-max">/100</span>
      </div>
      <div class="teacher-comment__score-label">综合成绩</div>
      <div class="teacher-comment__grade-badge" :class="gradeClass">
        {{ gradeLabel }}
      </div>
    </section>

    <!-- Dimension Breakdown -->
    <section class="teacher-comment__breakdown">
      <h3 class="teacher-comment__section-title">成绩构成</h3>
      <div class="teacher-comment__dimension-list">
        <div
          v-for="dimension in scoreDimensions"
          :key="dimension.name"
          class="teacher-comment__dimension"
        >
          <div class="teacher-comment__dimension-header">
            <span class="teacher-comment__dimension-name">{{ dimension.name }}</span>
            <span class="teacher-comment__dimension-weight">权重 {{ dimension.weight }}%</span>
          </div>
          <div class="teacher-comment__dimension-bar-wrap">
            <div class="teacher-comment__dimension-bar">
              <div
                class="teacher-comment__dimension-fill"
                :style="{ width: `${dimension.score}%` }"
              ></div>
            </div>
            <span class="teacher-comment__dimension-score">{{ dimension.score }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Teacher Comments -->
    <section class="teacher-comment__comments">
      <h3 class="teacher-comment__section-title">教师评语</h3>
      <div class="teacher-comment__comment-card">
        <div class="teacher-comment__teacher-info">
          <div class="teacher-comment__teacher-avatar">👨‍🏫</div>
          <div class="teacher-comment__teacher-detail">
            <span class="teacher-comment__teacher-name">{{ teacherName }}</span>
            <span class="teacher-comment__comment-time">{{ commentTime }}</span>
          </div>
        </div>
        <p class="teacher-comment__comment-text">{{ teacherCommentText }}</p>
      </div>
    </section>

    <!-- Improvement Suggestions -->
    <section class="teacher-comment__suggestions">
      <h3 class="teacher-comment__section-title">改进建议</h3>
      <ul class="teacher-comment__suggestion-list">
        <li
          v-for="(suggestion, index) in suggestions"
          :key="index"
          class="teacher-comment__suggestion-item"
        >
          {{ suggestion }}
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface ScoreDimension {
  name: string
  score: number
  weight: number
}

interface TeacherCommentConfig {
  display?: {
    teacher_name?: string
    comment_time?: string
  }
  evaluation?: {
    total_score?: number
    dimensions?: ScoreDimension[]
    comment?: string
    suggestions?: string[]
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: TeacherCommentConfig
}>()

const totalScore = computed<number>(() =>
  props.nodeConfig.evaluation?.total_score ?? 82
)

const scoreDimensions = computed<ScoreDimension[]>(() =>
  props.nodeConfig.evaluation?.dimensions ?? [
    { name: '课堂参与', score: 90, weight: 15 },
    { name: '作业完成', score: 85, weight: 25 },
    { name: '编码实训', score: 78, weight: 30 },
    { name: '团队协作', score: 88, weight: 15 },
    { name: '互评质量', score: 72, weight: 15 }
  ]
)

const teacherName = computed<string>(() =>
  props.nodeConfig.display?.teacher_name ?? '王老师'
)

const commentTime = computed<string>(() =>
  props.nodeConfig.display?.comment_time ?? '2025-03-15 16:30'
)

const teacherCommentText = computed<string>(() =>
  props.nodeConfig.evaluation?.comment ??
  '该同学在本次实训中表现良好，课堂参与积极，编码能力有明显提升。在团队协作方面展现了较好的沟通能力。建议在算法思维和代码规范方面继续加强练习。'
)

const suggestions = computed<string[]>(() =>
  props.nodeConfig.evaluation?.suggestions ?? [
    '加强算法基础训练，特别是排序和搜索算法的理解',
    '注意代码注释和文档编写的规范性',
    '多参与开源项目，提升实际工程经验',
    '建议课后复习面向对象设计模式'
  ]
)

const gradeLabel = computed<string>(() => {
  if (totalScore.value >= 90) return '优秀'
  if (totalScore.value >= 80) return '良好'
  if (totalScore.value >= 70) return '中等'
  if (totalScore.value >= 60) return '及格'
  return '不及格'
})

const gradeClass = computed<string>(() => {
  if (totalScore.value >= 90) return 'teacher-comment__grade-badge--excellent'
  if (totalScore.value >= 80) return 'teacher-comment__grade-badge--good'
  if (totalScore.value >= 70) return 'teacher-comment__grade-badge--medium'
  if (totalScore.value >= 60) return 'teacher-comment__grade-badge--pass'
  return 'teacher-comment__grade-badge--fail'
})
</script>

<style scoped>
.teacher-comment {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
}

.teacher-comment__header {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.teacher-comment__title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: 0.5rem;
}

.teacher-comment__desc {
  font-size: 0.875rem;
  color: var(--color-gray-500, #64748b);
}

.teacher-comment__score-summary {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-xl, 2rem);
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
}

.teacher-comment__total-score {
  display: flex;
  align-items: baseline;
}

.teacher-comment__score-value {
  font-size: 3rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.teacher-comment__score-max {
  font-size: 1.25rem;
  color: var(--color-gray-400, #94a3b8);
}

.teacher-comment__score-label {
  font-size: 0.875rem;
  color: var(--color-gray-500, #64748b);
  margin-top: 0.25rem;
}

.teacher-comment__grade-badge {
  margin-top: 0.75rem;
  padding: 0.25rem 1rem;
  border-radius: var(--radius-md, 0.5rem);
  font-size: 0.875rem;
  font-weight: 700;
}

.teacher-comment__grade-badge--excellent {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
}

.teacher-comment__grade-badge--good {
  background: rgba(99, 102, 241, 0.1);
  color: #4f46e5;
}

.teacher-comment__grade-badge--medium {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}

.teacher-comment__grade-badge--pass {
  background: rgba(249, 115, 22, 0.1);
  color: #ea580c;
}

.teacher-comment__grade-badge--fail {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.teacher-comment__section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-md, 1rem);
}

.teacher-comment__breakdown {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.teacher-comment__dimension-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.teacher-comment__dimension-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.25rem;
}

.teacher-comment__dimension-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-gray-700, #334155);
}

.teacher-comment__dimension-weight {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
}

.teacher-comment__dimension-bar-wrap {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.teacher-comment__dimension-bar {
  flex: 1;
  height: 8px;
  background: var(--color-gray-100, #f1f5f9);
  border-radius: 4px;
  overflow: hidden;
}

.teacher-comment__dimension-fill {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.teacher-comment__dimension-score {
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
  min-width: 2rem;
  text-align: right;
}

.teacher-comment__comments {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.teacher-comment__comment-card {
  padding: var(--spacing-md, 1rem);
  background: var(--color-gray-50, #f8fafc);
  border-radius: var(--radius-md, 0.5rem);
  border-left: 3px solid var(--color-primary-400, #818cf8);
}

.teacher-comment__teacher-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.teacher-comment__teacher-avatar {
  font-size: 1.5rem;
}

.teacher-comment__teacher-detail {
  display: flex;
  flex-direction: column;
}

.teacher-comment__teacher-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
}

.teacher-comment__comment-time {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
}

.teacher-comment__comment-text {
  font-size: 0.9375rem;
  color: var(--color-gray-600, #475569);
  line-height: 1.7;
}

.teacher-comment__suggestions {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.teacher-comment__suggestion-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.teacher-comment__suggestion-item {
  font-size: 0.875rem;
  color: var(--color-gray-600, #475569);
  padding: 0.5rem 0.75rem;
  background: var(--color-gray-50, #f8fafc);
  border-radius: var(--radius-sm, 0.25rem);
  border-left: 2px solid var(--color-primary-300, #a5b4fc);
  line-height: 1.5;
}
</style>
