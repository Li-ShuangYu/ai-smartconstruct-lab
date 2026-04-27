<template>
  <div class="manage-container">
    <header class="page-header">
      <div class="header-info">
        <h1 class="panel-main-title">班级管理</h1>
        <p class="subtitle">统一管理年级、行政班级，以及各班级下的学生名册</p>
      </div>
      <div class="header-actions">
        <button class="primary-btn" @click="handleCreate">
          + 新建年级 / 班级
        </button>
      </div>
    </header>
    
    <div class="toolbar-wrapper">
      <div class="statistics-info">
        <span class="info-label">当前系统共录入</span>
        <span class="info-value">{{ classData.length }}</span>
        <span class="info-label">个行政班级</span>
      </div>
      
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索班级或年级名称..." 
          class="search-input" 
        />
      </div>
    </div>

    <main class="list-content">
      <div class="data-grid">
        <transition-group name="card-fade" tag="div" class="grid-wrapper">
          <div v-for="item in filteredData" :key="item.id" class="data-card">
            <div class="card-header">
              <span class="type-badge">行政班级</span>
              <span class="id-tag">#{{ item.id }}</span>
            </div>

            <div class="card-main">
              <h3 class="item-name">{{ item.name }}</h3>
              <div class="item-meta">
                <div class="meta-row">
                  <span class="meta-label">班级人数</span>
                  <span class="meta-value">{{ item.studentCount }} 人</span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">创建时间</span>
                  <span class="meta-value">{{ item.createDate }}</span>
                </div>
              </div>
            </div>

            <footer class="card-footer">
              <button class="action-btn" @click="handleEditClass(item)">班级设置</button>
              <div class="v-divider"></div>
              <button class="action-btn primary-text" @click="handleManageStudents(item)">管理学生</button>
              <div class="v-divider"></div>
              <button class="action-btn delete" @click="handleDelete(item)">移除</button>
            </footer>
          </div>
        </transition-group>
      </div>

      <div v-if="filteredData.length === 0" class="empty-state">
        <div class="empty-icon">📂</div>
        <p>暂无匹配的班级数据</p>
      </div>
    </main>
    <Pagination 
  v-model:current-page="currentPage"
  v-model:page-size="pageSize"
  :total="totalCount"
  @change="loadData"
/>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
// 引入刚刚写好的分页组件
import Pagination from '@/views/common/Pagination.vue'

// 定义分页所需的状态
const totalCount = ref(120) // 总数据量（通常由后端接口返回）
const currentPage = ref(1)  // 当前页码
const pageSize = ref(10)    // 每页条数

// 处理分页变化触发的方法
const loadData = (paginationParams: { page: number, limit: number }) => {
  console.log(`正在请求第 ${paginationParams.page} 页数据，每页 ${paginationParams.limit} 条`)
  // 在这里调用你的后端接口，比如：
  // api.getClassList({ page: paginationParams.page, size: paginationParams.limit }).then(...)
}
const searchQuery = ref('')

// 模拟数据结构：聚焦于行政班级
const classData = ref([
  { id: 1001, name: '24级密码工程01班', studentCount: 32, createDate: '2026-03-01' },
  { id: 1002, name: '24级密码工程02班', studentCount: 28, createDate: '2026-03-01' },
  { id: 1003, name: '25级信息安全专项班', studentCount: 45, createDate: '2026-04-10' },
  { id: 1004, name: '26级信息安全专项班', studentCount: 45, createDate: '2026-04-10' }
])

// 过滤逻辑
const filteredData = computed(() => {
  const query = searchQuery.value.toLowerCase().trim()
  if (!query) return classData.value
  return classData.value.filter(i => i.name.toLowerCase().includes(query))
})

// === 操作逻辑 ===
const handleCreate = () => {
  console.log('触发：创建年级/班级')
  alert('跳转至新建年级/班级表单')
}

const handleEditClass = (item: any) => {
  console.log('触发：管理班级属性', item.id)
  alert(`编辑 [${item.name}] 的年级/班级属性`)
}

const handleManageStudents = (item: any) => {
  console.log('触发：管理班级的学生', item.id)
  alert(`进入 [${item.name}] 的学生名单与分配界面`)
}

const handleDelete = (item: any) => {
  if (confirm(`确定要彻底移除 [${item.name}] 及其挂载的学生关联吗？`)) {
    classData.value = classData.value.filter(i => i.id !== item.id)
  }
}
</script>

<style scoped>
.manage-container {
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.panel-main-title { font-size: 24px; font-weight: 800; color: #0F172A; margin: 0 0 8px 0; }
.subtitle { font-size: 14px; color: #64748B; margin: 0; }

.primary-btn {
  background: #4F46E5; color: white; border: none; padding: 10px 24px; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s;
}
.primary-btn:hover { background: #4338CA; transform: translateY(-1px); }

/* ==== 优化后的工具栏：紧凑单行对齐 ==== */
.toolbar-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #E2E8F0;

  padding-bottom: 16px;

}

.statistics-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
  background: #F8FAFC;
  padding: 6px 16px;
  border-radius: 6px;
}
.info-label { font-size: 14px; color: #64748B; font-weight: 500; }
.info-value { font-size: 18px; color: #4F46E5; font-weight: 800; }

.search-box { 
  position: relative; 
  width: 320px;
}
.search-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: #94A3B8; font-size: 14px; }
.search-input {
  width: 100%; padding: 10px 10px 10px 40px; border: 1px solid #E2E8F0; border-radius: 8px; font-size: 14px; transition: 0.2s; box-sizing: border-box; background-color: #FAFAFA;
}
.search-input:focus { outline: none; border-color: #818CF8; background-color: #fff; box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1); }

/* ==== 网格布局 ==== */
.data-grid {
  padding-top: 24px;
  height: calc(100vh - 240px); /* 视口高度自适应，防止出现多余底部留白 */
  overflow-y: auto;
  padding-bottom: 24px;
  height: 53vh;
  overflow: auto;
}

.grid-wrapper {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  align-items: start;
}

.data-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  box-sizing: border-box;
}

.data-card:hover {
  transform: translateY(-4px);
  border-color: #818CF8;
  box-shadow: 0 12px 30px rgba(79, 70, 229, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.type-badge { background: #F0F9FF; color: #0369A1; font-size: 11px; font-weight: 800; padding: 3px 8px; border-radius: 4px; }
.id-tag { font-size: 12px; color: #94A3B8; font-family: monospace; font-weight: 600; }

.card-main { flex: 1; margin-bottom: 20px; }
.item-name { font-size: 18px; font-weight: 800; color: #1E293B; margin: 0 0 12px 0; line-height: 1.4; }

.item-meta { display: flex; flex-direction: column; gap: 8px; }
.meta-row { display: flex; justify-content: space-between; font-size: 13px; }
.meta-label { color: #64748B; }
.meta-value { color: #334155; font-weight: 700; }

.card-footer {
  display: flex;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid #F1F5F9;
  gap: 6px;
}

.action-btn {
  flex: 1;
  background: none;
  border: none;
  font-size: 13px;
  font-weight: 700;
  color: #64748B;
  cursor: pointer;
  transition: 0.2s;
  padding: 6px 0;
  border-radius: 6px;
}

.action-btn:hover { background: #F1F5F9; color: #1E293B; }
.action-btn.primary-text { color: #4F46E5; }
.action-btn.primary-text:hover { background: #EEF2FF; color: #4338CA; }
.action-btn.delete:hover { background: #FEF2F2; color: #EF4444; }

.v-divider { width: 1px; height: 14px; background: #E2E8F0; }

.empty-state { text-align: center; padding: 80px 0; color: #94A3B8; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }

.card-fade-enter-active,
.card-fade-leave-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.card-fade-enter-from,
.card-fade-leave-to { opacity: 0; transform: translateY(10px) scale(0.98); }
</style>