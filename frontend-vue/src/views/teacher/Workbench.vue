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
        <div class="action-card primary-action" @click="goToAction('/teacher/training-create')">
          <div class="icon-wrapper icon-blue">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v18M3 12h18"/></svg>
          </div>
          <div class="action-info">
            <h3>创建实训</h3>
            <p>通过图形化拖拽，编排新的教学实训模板</p>
          </div>
        </div>
        
        <div class="action-card" @click="goToAction('/teacher/training-manage')">
          <div class="icon-wrapper icon-green">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>
          </div>
          <div class="action-info">
            <h3>开启实训</h3>
            <p>选择班级下发新的实训任务</p>
          </div>
        </div>
        
        <div class="action-card" @click="goToAction('/teacher/evaluation')">
          <div class="icon-wrapper icon-orange">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
          </div>
          <div class="action-info">
            <h3>综合评价</h3>
            <p>实训结束后三方总结与多维综合评价</p>
          </div>
        </div>
      </div>
    </section>

    <section class="section-block">
      <h2 class="section-title">近期实训记录</h2>
      <div class="history-grid">
        <div class="history-card" v-for="record in historyRecords" :key="record.id">
          <div class="card-header">
            <span class="tag" :class="record.statusType">{{ record.status }}</span>
            <span class="time">{{ record.time }}</span>
          </div>
          <h3 class="course-name" :title="record.name">{{ record.name }}</h3>
          <p class="course-desc">{{ record.desc }}</p>
          <div class="card-footer">
            <span class="stats">参与人数: {{ record.participants }}</span>
            <button class="btn-text">查看详情 →</button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 统一的路由跳转函数
const goToAction = (path: string) => {
  router.push(path)
}

// 模拟多样化的实训数据，融入密码工程真实场景
const historyRecords = ref([
  { 
    id: 1, 
    status: '已就绪', 
    statusType: 'active', 
    time: '更新于 2 小时前', 
    name: '无人机通信抗重放加密对抗演练', 
    desc: '包含 4 个节点：环境初始化 -> 流量截获分析 -> 抗重放机制部署 -> 攻防自动评测。', 
    participants: '0/50' 
  },
  { 
    id: 2, 
    status: '进行中', 
    statusType: 'warning', 
    time: '更新于 昨天 15:30', 
    name: '后量子密码学(PQC)算法设计与实现', 
    desc: '学生方案已全部提交完成，系统 AI 辅助评价已生成，等待下一步操作。', 
    participants: '6/32' 
  },
  { 
    id: 3, 
    status: '已结束', 
    statusType: 'default', 
    time: '更新于 2026-04-25', 
    name: '服务器运维与 auth-0 鉴权容器调试', 
    desc: '针对 155/158 机器的 Kubernetes Pod 状态排查与 Docker 容器日志抓取分析实训。', 
    participants: '30/30' 
  }
])
</script>

<style scoped>
/* 遵循 B端 系统极简、方正、去拟物化风格 */
.workbench-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 顶部欢迎与指标 */
.dashboard-header {
  margin-top: -10px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--color-border, #E5E7EB);
}

.title {
  font-size: 32px; /* 调整为更合理的系统字号，原70px过大 */
  font-weight: 800;
  color: var(--text-primary, #0F172A);
  margin: 0 0 8px 0;
  letter-spacing: 0.5px;
}

.subtitle {
  font-size: 15px;
  color: var(--text-secondary, #64748B);
  margin: 0;
}

.metrics-grid {
  display: flex;
  gap: 32px;
}

.metric-card {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.metric-card .label {
  font-size: 13px;
  color: var(--text-secondary, #64748B);
  margin-bottom: 4px;
  font-weight: 500;
}

.metric-card .value {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary, #0F172A);
  line-height: 1;
}

.metric-card .value.highlight {
  color: var(--color-danger, #EF4444);
}

/* 通用区块标题 */
.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary, #0F172A);
  margin: 0 0 16px 0;
}

/* 快捷操作网格 */
.actions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.action-card {
  display: flex;
  align-items: flex-start;
  padding: 24px;
  background-color: #fff;
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 12px; 
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}

.action-card:hover {
  border-color: #818CF8;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px -4px rgba(79, 70, 229, 0.1);
}

.primary-action {
  background-color: #FAFAFF;
}

/* 图标容器统一设计：柔和底色 + 对应主题色 */
.icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 10px;
  margin-right: 16px;
  flex-shrink: 0;
}

.icon-blue { background-color: #EEF2FF; color: #4F46E5; }
.icon-green { background-color: #ECFDF5; color: #059669; }
.icon-orange { background-color: #FFF7ED; color: #EA580C; }

.action-info {
  flex: 1;
}

.action-info h3 {
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 6px 0;
  color: #1E293B;
}

.action-info p {
  font-size: 13px;
  color: #64748B;
  margin: 0;
  line-height: 1.5;
}

/* 历史记录网格 */
.history-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.history-card {
  background-color: #fff;
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s ease;
}

.history-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

/* 状态标签样式体系 */
.tag {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 600;
}
.tag.active { background-color: #DCFCE7; color: #166534; }
.tag.warning { background-color: #FEF3C7; color: #92400E; }
.tag.default { background-color: #F1F5F9; color: #475569; }

.time {
  font-size: 12px;
  color: #94A3B8;
  font-weight: 500;
}

.course-name {
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 10px 0;
  color: #1E293B;
  /* 强制单行，超长省略 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-desc {
  font-size: 13px;
  color: #64748B;
  margin: 0 0 20px 0;
  line-height: 1.6;
  flex: 1;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #F1F5F9;
}

.stats {
  font-size: 13px;
  color: #64748B;
  font-weight: 500;
}

.btn-text {
  background: none;
  border: none;
  color: #4F46E5;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
  transition: color 0.2s;
}
.btn-text:hover {
  color: #38bdf8;
}
</style>