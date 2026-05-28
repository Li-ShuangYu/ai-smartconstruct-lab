<template>
  <div class="inter-group-review">
    <!-- Header -->
    <section class="inter-group-review__header">
      <h2 class="inter-group-review__title">组间互评</h2>
      <p class="inter-group-review__desc">请对其他小组的成果进行评审，与组内成员达成共识后提交。</p>
    </section>

    <!-- Target Group Info -->
    <section class="inter-group-review__target">
      <div class="inter-group-review__target-badge">🏆</div>
      <div class="inter-group-review__target-info">
        <span class="inter-group-review__target-name">{{ targetGroup.groupName }}</span>
        <span class="inter-group-review__target-artifact">{{ targetGroup.artifactTitle }}</span>
      </div>
      <span class="inter-group-review__target-members">{{ targetGroup.memberCount }} 人</span>
    </section>

    <!-- Artifact Preview -->
    <section class="inter-group-review__artifact">
      <h3 class="inter-group-review__section-title">成果预览</h3>
      <div class="inter-group-review__artifact-content">
        <p class="inter-group-review__artifact-summary">{{ targetGroup.artifactSummary }}</p>
      </div>
    </section>

    <!-- Dimension Scoring -->
    <section class="inter-group-review__scoring">
      <h3 class="inter-group-review__section-title">维度评分</h3>
      <div class="inter-group-review__dimension-list">
        <div
          v-for="dimension in dimensions"
          :key="dimension.id"
          class="inter-group-review__dimension"
        >
          <div class="inter-group-review__dimension-header">
            <span class="inter-group-review__dimension-name">{{ dimension.name }}</span>
            <span class="inter-group-review__dimension-score">
              {{ scores[dimension.id] ?? 0 }}/{{ dimension.maxScore }}
            </span>
          </div>
          <input
            type="range"
            class="inter-group-review__slider"
            :min="0"
            :max="dimension.maxScore"
            :value="scores[dimension.id] ?? 0"
            @input="setScore(dimension.id, Number(($event.target as HTMLInputElement).value))"
          />
        </div>
      </div>
    </section>

    <!-- Group Consensus -->
    <section class="inter-group-review__consensus">
      <h3 class="inter-group-review__section-title">小组共识</h3>
      <div class="inter-group-review__consensus-status">
        <div
          v-for="member in groupMembers"
          :key="member.id"
          class="inter-group-review__consensus-member"
        >
          <div
            class="inter-group-review__consensus-dot"
            :class="{ 'inter-group-review__consensus-dot--agreed': member.agreed }"
          ></div>
          <span class="inter-group-review__consensus-name">{{ member.name }}</span>
          <span class="inter-group-review__consensus-label">
            {{ member.agreed ? '已确认' : '待确认' }}
          </span>
        </div>
      </div>
      <textarea
        class="inter-group-review__consensus-comment"
        placeholder="请输入小组对该成果的综合评价意见..."
        :value="consensusComment"
        rows="3"
        @input="consensusComment = ($event.target as HTMLTextAreaElement).value"
      ></textarea>
    </section>

    <!-- Submit -->
    <section class="inter-group-review__footer">
      <button
        class="inter-group-review__submit-btn"
        :disabled="!canSubmit"
        @click="handleSubmit"
      >
        提交组间互评
      </button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface ReviewDimension {
  id: string
  name: string
  maxScore: number
}

interface GroupMember {
  id: string
  name: string
  agreed: boolean
}

interface TargetGroup {
  groupName: string
  artifactTitle: string
  artifactSummary: string
  memberCount: number
}

interface InterGroupReviewConfig {
  evaluation?: {
    dimensions?: ReviewDimension[]
  }
  [key: string]: unknown
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: InterGroupReviewConfig
}>()

const emit = defineEmits<{
  complete: []
}>()

/** Placeholder target group */
const targetGroup = computed<TargetGroup>(() => ({
  groupName: '第二小组',
  artifactTitle: '在线商城系统设计方案',
  artifactSummary: '本方案采用微服务架构设计在线商城系统，包含用户服务、商品服务、订单服务、支付服务四大核心模块。前端使用Vue 3 + TypeScript，后端使用Spring Cloud微服务框架。',
  memberCount: 5
}))

/** Dimensions from config or placeholder */
const dimensions = computed<ReviewDimension[]>(() =>
  props.nodeConfig.evaluation?.dimensions ?? [
    { id: 'd1', name: '方案创新性', maxScore: 20 },
    { id: 'd2', name: '技术深度', maxScore: 25 },
    { id: 'd3', name: '实现可行性', maxScore: 25 },
    { id: 'd4', name: '文档质量', maxScore: 15 },
    { id: 'd5', name: '团队协作', maxScore: 15 }
  ]
)

/** Placeholder group members */
const groupMembers = computed<GroupMember[]>(() => [
  { id: 'm1', name: '张伟', agreed: true },
  { id: 'm2', name: '李娜', agreed: true },
  { id: 'm3', name: '王磊', agreed: false },
  { id: 'm4', name: '刘洋', agreed: false }
])

/** Scores: dimensionId -> score */
const scores = ref<Record<string, number>>({})
const consensusComment = ref<string>('')

function setScore(dimensionId: string, value: number) {
  scores.value[dimensionId] = value
}

const canSubmit = computed<boolean>(() => {
  const hasScores = Object.keys(scores.value).length > 0
  const allAgreed = groupMembers.value.every(m => m.agreed)
  return hasScores && (allAgreed || consensusComment.value.trim().length > 0)
})

function handleSubmit() {
  emit('complete')
}
</script>

<style scoped>
.inter-group-review {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 1.5rem);
  padding: var(--spacing-lg, 1.5rem);
  height: 100%;
  overflow-y: auto;
}

.inter-group-review__header {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.inter-group-review__title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: 0.5rem;
}

.inter-group-review__desc {
  font-size: 0.875rem;
  color: var(--color-gray-500, #64748b);
}

.inter-group-review__target {
  display: flex;
  align-items: center;
  gap: var(--spacing-md, 1rem);
  padding: var(--spacing-md, 1rem) var(--spacing-lg, 1.5rem);
  background: var(--color-primary-50, #eef2ff);
  border: 1px solid var(--color-primary-100, #e0e7ff);
  border-radius: var(--radius-lg, 0.75rem);
}

.inter-group-review__target-badge {
  font-size: 1.5rem;
}

.inter-group-review__target-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.inter-group-review__target-name {
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-gray-800, #1e293b);
}

.inter-group-review__target-artifact {
  font-size: 0.8125rem;
  color: var(--color-gray-500, #64748b);
}

.inter-group-review__target-members {
  font-size: 0.75rem;
  color: var(--color-gray-400, #94a3b8);
  padding: 0.25rem 0.5rem;
  background: var(--color-white, #ffffff);
  border-radius: var(--radius-sm, 0.25rem);
}

.inter-group-review__section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-gray-800, #1e293b);
  margin-bottom: var(--spacing-md, 1rem);
}

.inter-group-review__artifact {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.inter-group-review__artifact-summary {
  font-size: 0.875rem;
  color: var(--color-gray-600, #475569);
  line-height: 1.6;
  padding: 0.75rem;
  background: var(--color-gray-50, #f8fafc);
  border-radius: var(--radius-md, 0.5rem);
}

.inter-group-review__scoring {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.inter-group-review__dimension-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md, 1rem);
}

.inter-group-review__dimension {
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--color-gray-100, #f1f5f9);
}

.inter-group-review__dimension:last-child {
  border-bottom: none;
}

.inter-group-review__dimension-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.inter-group-review__dimension-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-gray-700, #334155);
}

.inter-group-review__dimension-score {
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--color-primary-600, #4f46e5);
}

.inter-group-review__slider {
  width: 100%;
  height: 6px;
  appearance: none;
  background: var(--color-gray-100, #f1f5f9);
  border-radius: 3px;
  outline: none;
  cursor: pointer;
}

.inter-group-review__slider::-webkit-slider-thumb {
  appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-primary-500, #6366f1);
  cursor: pointer;
}

.inter-group-review__consensus {
  background: var(--color-white, #ffffff);
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-lg, 0.75rem);
  padding: var(--spacing-lg, 1.5rem);
}

.inter-group-review__consensus-status {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-bottom: var(--spacing-md, 1rem);
}

.inter-group-review__consensus-member {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 0.625rem;
  background: var(--color-gray-50, #f8fafc);
  border-radius: var(--radius-sm, 0.25rem);
}

.inter-group-review__consensus-dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background: var(--color-gray-300, #cbd5e1);
}

.inter-group-review__consensus-dot--agreed {
  background: #22c55e;
}

.inter-group-review__consensus-name {
  font-size: 0.8125rem;
  font-weight: 500;
  color: var(--color-gray-700, #334155);
}

.inter-group-review__consensus-label {
  font-size: 0.6875rem;
  color: var(--color-gray-400, #94a3b8);
}

.inter-group-review__consensus-comment {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-gray-200, #e2e8f0);
  border-radius: var(--radius-md, 0.5rem);
  font-size: 0.875rem;
  color: var(--color-gray-800, #1e293b);
  resize: vertical;
  font-family: inherit;
}

.inter-group-review__consensus-comment:focus {
  outline: none;
  border-color: var(--color-primary-300, #a5b4fc);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.inter-group-review__footer {
  padding-top: var(--spacing-md, 1rem);
}

.inter-group-review__submit-btn {
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

.inter-group-review__submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px -4px rgba(99, 102, 241, 0.4);
}

.inter-group-review__submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
