<template>
  <div class="workbench-container">
    <header class="dashboard-header">
      <div class="greeting-area">
        <h1 class="title">早安，Jovi 老师。</h1>
        <p class="subtitle">今天有 12 份架构设计作业待批改，2 个实训正在进行中。</p>
      </div>
      <div class="metrics-grid">
        <div class="metric-card">
          <span class="label">本周活跃学生</span>
          <span class="value">148</span>
        </div>
        <div class="metric-card">
          <span class="label">待办任务</span>
          <span class="value highlight">12</span>
        </div>
      </div>
    </header>

    <section class="section-block">
      <h2 class="section-title">快捷入口</h2>
      <div class="actions-grid">
        <div class="action-card primary-action" @click="goToAction('/teacher/training/create')">
          <div class="icon-wrapper"><span class="icon">✨</span></div>
          <div class="action-info">
            <h3>创建节点编排</h3>
            <p>通过图形化拖拽，编排新的教学实训流</p>
          </div>
        </div>
        <div class="action-card" @click="goToAction('/teacher/training/start')">
          <div class="icon-wrapper"><span class="icon">▶️</span></div>
          <div class="action-info">
            <h3>启动实训班次</h3>
            <p>基于已有编排，下发任务并开启一堂课</p>
          </div>
        </div>
        <div class="action-card" @click="goToAction('/teacher/assignment/create')">
          <div class="icon-wrapper"><span class="icon">📝</span></div>
          <div class="action-info">
            <h3>发布新作业</h3>
            <p>包含思维导图与代码补全题型</p>
          </div>
        </div>
      </div>
    </section>

    <section class="section-block">
      <h2 class="section-title">近期实训记录</h2>
      <div class="history-grid">
        <div class="history-card" v-for="i in 3" :key="i">
          <div class="card-header">
            <span class="tag">进行中</span>
            <span class="time">更新于 2 小时前</span>
          </div>
          <h3 class="course-name">密码系统设计 - 基础理论与实践 (2026春季)</h3>
          <p class="course-desc">包含 4 个节点：前置测试 -> 视频学习 -> 容器化部署 -> 互评。</p>
          <div class="card-footer">
            <span class="stats">参与人数: 45/50</span>
            <button class="btn-text">查看详情 →</button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

// 统一的路由跳转函数
const goToAction = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
/* 遵循 OpenMaic 极简、方正、去拟物化风格 */
.workbench-container {
  display: flex;
  flex-direction: column;
  gap: 32px; /* 模块间大留白 */
  max-width: 1200px;
  margin: 0 auto;
}

/* 顶部欢迎与指标 */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--color-border, #E5E7EB);
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary, #111827);
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: var(--text-secondary, #6B7280);
  margin: 0;
}

.metrics-grid {
  display: flex;
  gap: 24px;
}

.metric-card {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.metric-card .label {
  font-size: 12px;
  color: var(--text-secondary, #6B7280);
  margin-bottom: 4px;
}

.metric-card .value {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary, #111827);
}

.metric-card .value.highlight {
  color: var(--color-danger, #EF4444);
}

/* 通用区块标题 */
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #111827);
  margin: 0 0 16px 0;
}

/* 快捷操作网格 */
.actions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.action-card {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  background-color: #fff;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 8px; /* 轻微圆角，保持方正感 */
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-card:hover {
  border-color: var(--color-primary, #4F46E5);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.primary-action {
  background-color: var(--color-primary-light, #EEF2FF);
  border-color: var(--color-primary-light, #EEF2FF);
}

.icon-wrapper {
  font-size: 24px;
  margin-right: 16px;
}

.action-info h3 {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: var(--text-primary, #111827);
}

.action-info p {
  font-size: 13px;
  color: var(--text-secondary, #6B7280);
  margin: 0;
  line-height: 1.4;
}

/* 历史记录网格 */
.history-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.history-card {
  background-color: #fff;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.tag {
  font-size: 12px;
  background-color: #ECFDF5;
  color: #059669;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.time {
  font-size: 12px;
  color: var(--text-secondary, #9CA3AF);
}

.course-name {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--text-primary, #111827);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-desc {
  font-size: 13px;
  color: var(--text-secondary, #6B7280);
  margin: 0 0 16px 0;
  line-height: 1.5;
  flex: 1;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-light, #F3F4F6);
}

.stats {
  font-size: 13px;
  color: var(--text-secondary, #6B7280);
}

.btn-text {
  background: none;
  border: none;
  color: var(--color-primary, #4F46E5);
  font-size: 13px;
  cursor: pointer;
  padding: 0;
}
.btn-text:hover {
  text-decoration: underline;
}
</style>