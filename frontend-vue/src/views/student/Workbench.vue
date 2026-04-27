<template>
  <div class="student-workbench">
    <header class="page-header">
      <div class="header-info">
        <h1 class="panel-main-title">我的实训工作台</h1>
        <p class="subtitle">欢迎回来，今天也要继续精进密码学技能</p>
      </div>
      <div class="quick-stats">
        <div class="stat-box">
          <span class="num">3</span>
          <span class="label">进行中</span>
        </div>
        <div class="stat-box">
          <span class="num">12</span>
          <span class="label">已完成</span>
        </div>
      </div>
    </header>

    <nav class="filter-tabs">
      <div 
        v-for="tab in tabs" 
        :key="tab.key" 
        class="tab-item" 
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
        <span class="count" v-if="getCount(tab.key)">{{ getCount(tab.key) }}</span>
      </div>
    </nav>

    <main class="card-grid">
      <div v-for="item in filteredList" :key="item.id" class="training-card">
        <div class="card-status" :class="item.status">{{ getStatusText(item.status) }}</div>
        <div class="card-body">
          <h3 class="training-title">{{ item.title }}</h3>
          <p class="course-name">所属课程：{{ item.course }}</p>
          
          <div class="progress-section">
            <div class="progress-info">
              <span>实训进度</span>
              <span>{{ item.progress }}%</span>
            </div>
            <div class="progress-bar">
              <div class="progress-inner" :style="{ width: item.progress + '%' }"></div>
            </div>
          </div>

          <div class="meta-info">
            <span class="time">⏱️ 截止时间：{{ item.deadline }}</span>
          </div>
        </div>
        <footer class="card-footer">
          <button 
            class="action-btn" 
            :class="item.status === 'ongoing' ? 'primary' : 'secondary'"
            @click="handleAction(item)"
          >
            {{ item.status === 'ongoing' ? '继续实训' : (item.status === 'not_started' ? '进入详情' : '查看报告') }}
          </button>
        </footer>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeTab = ref('all')

const tabs = [
  { label: '全部实训', key: 'all' },
  { label: '未开始', key: 'not_started' },
  { label: '进行中', key: 'ongoing' },
  { label: '已结束', key: 'ended' }
]

const trainingList = ref([
  { id: 1, title: '无人机抗重放攻击模拟演练', course: '通信安全实务', status: 'ongoing', progress: 65, deadline: '2026-05-10' },
  { id: 2, title: 'SM4 算法加密解密编程实战', course: '国密算法基础', status: 'not_started', progress: 0, deadline: '2026-05-15' },
  { id: 3, title: 'PQC 后量子签名算法原理', course: '前沿密码学', status: 'ended', progress: 100, deadline: '2026-04-20' },
  { id: 4, title: '数字签名与身份认证流程', course: '身份鉴别技术', status: 'ongoing', progress: 20, deadline: '2026-05-12' }
])

const filteredList = computed(() => {
  if (activeTab.value === 'all') return trainingList.value
  return trainingList.value.filter(i => i.status === activeTab.value)
})

const getCount = (key: string) => {
  if (key === 'all') return 0
  return trainingList.value.filter(i => i.status === key).length
}

const getStatusText = (status: string) => {
  const map: any = { ongoing: '进行中', not_started: '未开始', ended: '已结束' }
  return map[status]
}

const handleAction = (item: any) => {
  if (item.status === 'ongoing') {
    router.push(`/student/student-cabin/${item.id}`)
  } else {
    alert('跳转至实训详情页')
  }
}
</script>

<style scoped>
.student-workbench { padding: 32px; min-height: 100%; box-sizing: border-box; }

.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 32px; }
.panel-main-title { font-size: 24px; font-weight: 800; color: #0F172A; margin: 0 0 8px 0; }
.subtitle { font-size: 14px; color: #64748B; margin: 0; }

.quick-stats { display: flex; gap: 24px; }
.stat-box { display: flex; flex-direction: column; align-items: flex-end; }
.stat-box .num { font-size: 24px; font-weight: 800; color: #1E293B; }
.stat-box .label { font-size: 12px; color: #94A3B8; font-weight: 700; }

/* Tabs 样式 */
.filter-tabs { display: flex; gap: 32px; border-bottom: 1px solid #E2E8F0; margin-bottom: 24px; }
.tab-item { padding: 12px 4px; font-size: 15px; font-weight: 700; color: #64748B; cursor: pointer; border-bottom: 3px solid transparent; transition: all 0.2s; position: relative; }
.tab-item.active { color: #4F46E5; border-bottom-color: #4F46E5; }
.tab-item .count { position: absolute; top: 4px; right: -18px; background: #EEF2FF; color: #4F46E5; font-size: 11px; padding: 1px 6px; border-radius: 10px; }

/* 网格布局 */
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 24px; }
.training-card { background: white; border: 1px solid #E2E8F0; border-radius: 16px; padding: 24px; position: relative; transition: all 0.3s; display: flex; flex-direction: column; }
.training-card:hover { transform: translateY(-4px); border-color: #818CF8; box-shadow: 0 12px 30px rgba(79, 70, 229, 0.06); }

.card-status { position: absolute; top: 16px; right: 16px; font-size: 11px; font-weight: 800; padding: 3px 10px; border-radius: 6px; }
.card-status.ongoing { background: #F0FDF4; color: #16A34A; }
.card-status.not_started { background: #F8FAFC; color: #64748B; }
.card-status.ended { background: #F1F5F9; color: #94A3B8; }

.training-title { font-size: 18px; font-weight: 800; color: #1E293B; margin: 0 0 12px 0; line-height: 1.4; height: 50px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.course-name { font-size: 13px; color: #64748B; margin-bottom: 20px; }

.progress-section { margin-bottom: 20px; }
.progress-info { display: flex; justify-content: space-between; font-size: 12px; font-weight: 700; color: #475569; margin-bottom: 8px; }
.progress-bar { height: 6px; background: #F1F5F9; border-radius: 10px; overflow: hidden; }
.progress-inner { height: 100%; background: #4F46E5; transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1); }

.meta-info { font-size: 12px; color: #94A3B8; font-weight: 500; margin-bottom: 24px; }

.card-footer { margin-top: auto; }
.action-btn { width: 100%; padding: 12px; border-radius: 10px; font-weight: 700; cursor: pointer; transition: 0.2s; border: 1px solid transparent; }
.action-btn.primary { background: #4F46E5; color: white; }
.action-btn.primary:hover { background: #4338CA; }
.action-btn.secondary { background: white; border-color: #E2E8F0; color: #475569; }
.action-btn.secondary:hover { background: #F8FAFC; }
</style>