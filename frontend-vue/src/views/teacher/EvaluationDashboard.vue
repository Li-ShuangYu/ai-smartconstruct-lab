<template>
  <div class="evaluation-container">
    <header class="page-header">
      <div class="header-info">
        <h1 class="panel-main-title">实训评价记录</h1>
        <p class="subtitle">全量实训考核结果与多维数据统计</p>
      </div>
      <div class="filter-area">
        <input type="text" v-model="searchQuery" placeholder="搜索项目名称..." class="search-input" />
      </div>
    </header>

    <main class="evaluation-content">
      <section class="project-section">
        <div class="project-grid">
          <div v-for="item in filteredProjects" :key="item.id" class="project-card">
            <div class="card-top">
              <span class="project-tag">{{ item.templateName || '实训项目' }}</span>
              <span class="project-date">{{ formatDate(item.createdAt) }}</span>
            </div>
            <h4 class="project-name">{{ item.taskName }}</h4>
            <div class="project-metrics">
              <div class="metric">
                <span class="m-label">状态</span>
                <span class="m-value status">{{ statusLabel(item.status) }}</span>
              </div>
              <div class="metric">
                <span class="m-label">范围</span>
                <span class="m-value">{{ scopeLabel(item.dispatchScope) }}</span>
              </div>
            </div>
            <div class="skill-tags">
              <span v-if="item.dispatchTargetName" class="skill-tag">{{ item.dispatchTargetName }}</span>
              <span v-if="item.hasGroup === 1" class="skill-tag">分组实训</span>
              <span v-if="item.isInClass === 1" class="skill-tag">课堂模式</span>
            </div>
            <button class="view-detail-btn" @click="handleDetail(item)">查看详细报告</button>
          </div>
        </div>

        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <span>加载中...</span>
        </div>

        <div v-else-if="filteredProjects.length === 0" class="empty-state">
          未找到相关实训项目记录
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTrainingTasks } from '@/services/modules/teacher-dashboard.service'
import type { TrainingTaskItem } from '@/services/types/dashboard.types'

const router = useRouter()
const searchQuery = ref('')
const loading = ref(false)
const evaluatedProjects = ref<TrainingTaskItem[]>([])

// 搜索过滤逻辑
const filteredProjects = computed(() => {
  return evaluatedProjects.value.filter(p => 
    p.taskName.includes(searchQuery.value) || 
    (p.templateName && p.templateName.includes(searchQuery.value))
  )
})

function statusLabel(status: number): string {
  const map: Record<number, string> = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return map[status] || '未知'
}

function scopeLabel(scope?: number): string {
  const map: Record<number, string> = {
    1: '班级',
    2: '课程'
  }
  return map[scope || 0] || '未知'
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const handleDetail = (item: TrainingTaskItem) => {
  router.push({
    path: '/training/teacher-training/summary-report',
    query: { taskId: String(item.id) }
  })
}

async function loadProjects() {
  loading.value = true
  try {
    const result = await getTrainingTasks(1, 50)
    if (result.code === 200 && result.data) {
      evaluatedProjects.value = result.data.records || []
    }
  } catch (error) {
    console.error('加载实训项目失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.evaluation-container {
  background: transparent;
  font-size: 15px;
  color: #334155;
  box-sizing: border-box;
}

.page-header {
  margin-top: -24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-bottom: 1px solid #E2E8F0;

  padding-bottom: 24px;
}

.panel-main-title {
  font-size: 24px;
  font-weight: 800;
  color: #0F172A;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #64748B;
  margin: 0;
}

.header-stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.stat-label { font-size: 12px; color: #94A3B8; font-weight: 700; text-transform: uppercase; }
.stat-value { font-size: 24px; font-weight: 800; color: #1E293B; }
.stat-item.primary .stat-value { color: #4F46E5; }

/* 项目列表区布局 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-title {
  font-size: 17px;
  font-weight: 700;
  color: #0F172A;
  margin: 0;
  padding-left: 12px;
  border-left: 4px solid #4F46E5;
}

.search-input {
  padding: 10px 16px;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  font-size: 14px;
  width: 280px;
  background: #FFFFFF;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #818CF8;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.project-grid {
    padding-top: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  max-width: 100%;
  overflow-y: auto;
  max-height: calc(100vh - 200px);padding-bottom: 4px;
  padding-right: 8px;
}

.project-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
}

.project-card:hover {
  transform: translateY(-4px);
  border-color: #818CF8;
  box-shadow: 0 12px 30px rgba(79, 70, 229, 0.08);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.project-tag {
  background: #EEF2FF;
  color: #4F46E5;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
}

.project-date { font-size: 12px; color: #94A3B8; font-weight: 500; }

.project-name {
  font-size: 18px;
  font-weight: 800;
  color: #1E293B;
  margin: 0 0 20px 0;
  line-height: 1.4;
  height: 50px; /* 保持高度一致 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-metrics {
  display: flex;
  gap: 32px;
  margin-bottom: 20px;
  background: #F8FAFC;
  padding: 12px;
  border-radius: 10px;
}

.metric { display: flex; flex-direction: column; }
.m-label { font-size: 11px; color: #64748B; font-weight: 700; margin-bottom: 4px; }
.m-value { font-size: 18px; font-weight: 800; color: #0F172A; }

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
  flex: 1;
}

.skill-tag {
  font-size: 12px;
  background: #F1F5F9;
  color: #475569;
  padding: 3px 10px;
  border-radius: 6px;
  font-weight: 500;
}

.view-detail-btn {
  width: 100%;
  padding: 12px;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  color: #475569;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.view-detail-btn:hover {
  background: #0F172A;
  color: #FFFFFF;
  border-color: #0F172A;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #94A3B8;
  font-weight: 500;
}

.loading-state {
  text-align: center;
  padding: 80px 0;
  color: #94A3B8;
  font-weight: 500;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #E2E8F0;
  border-top-color: #4F46E5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>