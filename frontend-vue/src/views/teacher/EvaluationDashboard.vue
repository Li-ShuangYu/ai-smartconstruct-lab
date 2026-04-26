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
              <span class="project-tag">{{ item.category }}</span>
              <span class="project-date">{{ item.date }}</span>
            </div>
            <h4 class="project-name">{{ item.name }}</h4>
            <div class="project-metrics">
              <div class="metric">
                <span class="m-label">平均分</span>
                <span class="m-value">{{ item.avgScore }}</span>
              </div>
              <div class="metric">
                <span class="m-label">完成率</span>
                <span class="m-value">{{ item.completion }}%</span>
              </div>
            </div>
            <div class="skill-tags">
              <span v-for="skill in item.skills" :key="skill" class="skill-tag">{{ skill }}</span>
            </div>
            <button class="view-detail-btn" @click="handleDetail(item)">查看详细报告</button>
          </div>
        </div>

        <div v-if="filteredProjects.length === 0" class="empty-state">
          未找到相关实训项目记录
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchQuery = ref('')

// 模拟实训项目数据
const evaluatedProjects = ref([
  { id: 1, name: '无人机通信抗重放攻击实训', category: '通信安全', date: '2026-04-10', avgScore: 92.4, completion: 100, skills: ['SM4', '时间戳验证', '防重放'] },
  { id: 2, name: '国密算法 SM3 杂凑分析演练', category: '密码基础', date: '2026-04-12', avgScore: 85.1, completion: 98, skills: ['SM3', '碰撞性分析'] },
  { id: 3, name: 'PQC 后量子签名算法实操', category: '前沿技术', date: '2026-04-15', avgScore: 78.6, completion: 85, skills: ['Dilithium', '抗量子计算'] },
  { id: 4, name: '端对端加密即时通讯设计', category: '系统集成', date: '2026-04-18', avgScore: 89.2, completion: 92, skills: ['密钥交换', '协议设计'] },
  { id: 5, name: 'SM2 公钥密码身份认证方案', category: '身份鉴别', date: '2026-04-20', avgScore: 91.5, completion: 100, skills: ['SM2', '数字签名'] },
  { id: 6, name: '随机数发生器安全性检测', category: '算法验证', date: '2026-04-22', avgScore: 88.0, completion: 95, skills: ['NIST测试', '熵源分析'] }
])

// 搜索过滤逻辑
const filteredProjects = computed(() => {
  return evaluatedProjects.value.filter(p => 
    p.name.includes(searchQuery.value) || p.category.includes(searchQuery.value)
  )
  
})

const handleDetail = (item: any) => {
  router.push(`/teacher/class-competency/${item.id}`)
}
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
</style>