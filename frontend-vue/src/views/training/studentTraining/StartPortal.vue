<template>
  <div class="start-portal">
    <!-- 页面标题 -->
    <h2 class="start-portal__main-title">实训流程概览</h2>

    <!-- AI导师欢迎卡片 -->
    <div class="start-portal__welcome-card">
      <div class="welcome-card__icon">
        <svg width="24" height="24" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" />
        </svg>
      </div>
      <div class="welcome-card__content">
        <h4 class="welcome-card__title">AI 导师欢迎您</h4>
        <p class="welcome-card__text">{{ displayWelcomeText }}</p>
      </div>
    </div>

    <!-- 三阶段横向并排 -->
    <div class="start-portal__flow">
      <!-- 课前阶段 -->
      <div class="flow-section">
        <div class="flow-section__header flow-section__header--pre">
          <span class="flow-section__tag">阶段一</span>
          <h3 class="flow-section__title">课前阶段</h3>
        </div>
        <div class="flow-section__nodes">
          <div class="flow-node-item">
            <div class="flow-node-item__card">
              <span class="flow-node-item__num">1</span>
              <span class="flow-node-item__name">资源阅读</span>
              <span class="flow-node-item__time">15 分钟</span>
            </div>
            <div class="flow-node-item__line"></div>
          </div>
          <div class="flow-node-item">
            <div class="flow-node-item__card">
              <span class="flow-node-item__num">2</span>
              <span class="flow-node-item__name">视频观看</span>
              <span class="flow-node-item__time">20 分钟</span>
            </div>
            <div class="flow-node-item__line"></div>
          </div>
          <div class="flow-node-item">
            <div class="flow-node-item__card">
              <span class="flow-node-item__num">3</span>
              <span class="flow-node-item__name">导图预习</span>
              <span class="flow-node-item__time">10 分钟</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 横向箭头 -->
      <div class="flow-arrow">
        <svg width="32" height="32" fill="none" viewBox="0 0 24 24" stroke="#6366f1" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
        </svg>
      </div>

      <!-- 课中阶段 -->
      <div class="flow-section">
        <div class="flow-section__header flow-section__header--mid">
          <span class="flow-section__tag">阶段二</span>
          <h3 class="flow-section__title">课中阶段</h3>
        </div>
        <div class="flow-section__nodes">
          <div class="flow-node-item">
            <div class="flow-node-item__card">
              <span class="flow-node-item__num">4</span>
              <span class="flow-node-item__name">理论实训</span>
              <span class="flow-node-item__time">20 分钟</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 横向箭头 -->
      <div class="flow-arrow">
        <svg width="32" height="32" fill="none" viewBox="0 0 24 24" stroke="#6366f1" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
        </svg>
      </div>

      <!-- 课后阶段 -->
      <div class="flow-section">
        <div class="flow-section__header flow-section__header--post">
          <span class="flow-section__tag">阶段三</span>
          <h3 class="flow-section__title">课后阶段</h3>
        </div>
        <div class="flow-section__nodes">
          <div class="flow-node-item">
            <div class="flow-node-item__card">
              <span class="flow-node-item__num">5</span>
              <span class="flow-node-item__name">任务下发</span>
              <span class="flow-node-item__time">5 分钟</span>
            </div>
            <div class="flow-node-item__line"></div>
          </div>
          <div class="flow-node-item">
            <div class="flow-node-item__card">
              <span class="flow-node-item__num">6</span>
              <span class="flow-node-item__name">编码实训</span>
              <span class="flow-node-item__time">45 分钟</span>
            </div>
            <div class="flow-node-item__line"></div>
          </div>
          <div class="flow-node-item">
            <div class="flow-node-item__card">
              <span class="flow-node-item__num">7</span>
              <span class="flow-node-item__name">方案上传</span>
              <span class="flow-node-item__time">15 分钟</span>
            </div>
            <div class="flow-node-item__line"></div>
          </div>
          <div class="flow-node-item">
            <div class="flow-node-item__card">
              <span class="flow-node-item__num">8</span>
              <span class="flow-node-item__name">课后作业</span>
              <span class="flow-node-item__time">30 分钟</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 开始实训按钮 -->
    <div class="start-portal__action">
      <button class="start-portal__start-btn" :disabled="isExpired" @click="handleStart">
        <svg width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" />
        </svg>
        {{ isExpired ? '实训已过期' : '开始实训' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useStudentFlowStore } from '@/stores/modules/studentFlow.store'

interface StartNodeConfig {
  welcome_text?: string
  ai_welcome_text?: string
  topic_name?: string
  description?: string
}

const props = defineProps<{
  nodeInstanceId: number
  nodeConfig: StartNodeConfig
}>()

const emit = defineEmits<{
  complete: []
  navigate: [direction: 'next']
}>()

const studentFlowStore = useStudentFlowStore()
const isExpired = computed<boolean>(() => studentFlowStore.isExpired)

/** AI 欢迎文字（优先取 ai_welcome_text → welcome_text → description 兜底） */
const displayWelcomeText = computed<string>(() => {
  if (props.nodeConfig?.ai_welcome_text) return props.nodeConfig.ai_welcome_text
  if (props.nodeConfig?.welcome_text) return props.nodeConfig.welcome_text
  return props.nodeConfig?.description ?? '欢迎参加本次实训，请按照流程逐步完成各环节学习。'
})

async function handleStart() {
  emit('complete')
  emit('navigate', 'next')
}
</script>

<style scoped>
.start-portal {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 24px 24px;
  height: 100%;
  overflow-y: auto;
}

.start-portal__main-title {
  font-size: 22px;
  font-weight: 800;
  color: #1e293b;
  text-align: center;
  margin: 0 0 24px;
}

/* ===== AI导师欢迎卡片 ===== */
.start-portal__welcome-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  width: 100%;
  max-width: 900px;
  padding: 16px 20px;
  margin-bottom: 28px;
  background: #eef2ff;
  border: 1px solid #e0e7ff;
  border-left: 4px solid #6366f1;
  border-radius: 12px;
}

.welcome-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #6366f1;
  color: #fff;
  flex-shrink: 0;
}

.welcome-card__content {
  flex: 1;
  min-width: 0;
}

.welcome-card__title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px;
}

.welcome-card__text {
  font-size: 13px;
  line-height: 1.6;
  color: #475569;
  margin: 0;
  font-style: italic;
}

/* ===== 三阶段横向并排 ===== */
.start-portal__flow {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 0;
  flex: 1;
  width: 100%;
  max-width: 900px;
}

/* 单个阶段 */
.flow-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  min-width: 0;
}

/* 阶段头部 */
.flow-section__header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 100%;
  padding: 16px 12px;
  border-radius: 12px;
  margin-bottom: 16px;
}
.flow-section__header--pre {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
}
.flow-section__header--mid {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}
.flow-section__header--post {
  background: linear-gradient(135deg, #10b981, #059669);
}

.flow-section__tag {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 2px 10px;
  border-radius: 4px;
  background: rgba(255,255,255,0.2);
  color: #fff;
}

.flow-section__title {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  margin: 0;
  text-align: center;
}

/* 阶段内节点纵向排列 */
.flow-section__nodes {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.flow-node-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

/* 纵向连接线 */
.flow-node-item__line {
  width: 2px;
  height: 16px;
  background: linear-gradient(180deg, #c7d2fe, #e0e7ff);
}

/* 节点卡片 */
.flow-node-item__card {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 10px 14px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  transition: all 0.15s;
}
.flow-node-item__card:hover {
  border-color: #a5b4fc;
  box-shadow: 0 2px 8px -2px rgba(99,102,241,0.1);
}

.flow-node-item__num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #eef2ff;
  color: #6366f1;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.flow-node-item__name {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.flow-node-item__time {
  font-size: 11px;
  color: #94a3b8;
  flex-shrink: 0;
}

/* 阶段间横向箭头 */
.flow-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 6px 0;
  flex-shrink: 0;
}

/* 底部按钮 */
.start-portal__action {
  width: 100%;
  max-width: 900px;
  padding-top: 24px;
  margin-top: auto;
}

.start-portal__start-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 14px 24px;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.start-portal__start-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 24px -4px rgba(99,102,241,0.4);
}
.start-portal__start-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>