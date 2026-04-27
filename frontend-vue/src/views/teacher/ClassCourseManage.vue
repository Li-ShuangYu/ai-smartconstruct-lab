<template>
  <div class="manage-container">
    <header class="page-header">
      <div class="header-info">
        <h1 class="panel-main-title">教学组织管理</h1>
        <p class="subtitle">管理行政班级与教学课程，支持学生跨课程选课与批量下发</p>
      </div>
      <div class="header-actions">
        <button class="primary-btn" @click="handleCreate">
          {{ currentTab === 'class' ? '+ 新建行政班级' : '+ 创建教学课程' }}
        </button>
      </div>
    </header>
    
    <div class="toolbar-wrapper">
      <nav class="tabs-nav">
        <div 
          class="tab-item" 
          :class="{ active: currentTab === 'class' }" 
          @click="currentTab = 'class'"
        >行政班级 (Class)</div>
        <div 
          class="tab-item" 
          :class="{ active: currentTab === 'course' }" 
          @click="currentTab = 'course'"
        >教学课程 (Course)</div>
      </nav>
      
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input 
          type="text" 
          v-model="searchQuery" 
          :placeholder="currentTab === 'class' ? '搜索班级名称...' : '搜索课程名称/授课教师...'" 
          class="search-input" 
        />
      </div>
    </div>

    <main class="list-content">
      <div class="data-grid">
        <transition name="card-fade" mode="out-in">
          <div :key="currentTab" class="grid-wrapper">
            <div v-for="item in filteredData" :key="item.id" class="data-card">
              <div class="card-header">
                <span class="type-badge" :class="currentTab">
                  {{ currentTab === 'class' ? '行政单位' : '教学单元' }}
                </span>
                <span class="id-tag">#{{ item.id }}</span>
              </div>

              <div class="card-main">
                <h3 class="item-name">{{ item.name }}</h3>
                <div class="item-meta">
                  <div class="meta-row">
                    <span class="meta-label">{{ currentTab === 'class' ? '班级人数' : '已选课人数' }}</span>
                    <span class="meta-value">{{ item.studentCount }} 人</span>
                  </div>
                  <div class="meta-row" v-if="currentTab === 'course'">
                    <span class="meta-label">授课教师</span>
                    <span class="meta-value">{{ item.teacher }}</span>
                  </div>
                  <div class="meta-row">
                    <span class="meta-label">创建时间</span>
                    <span class="meta-value">{{ item.createDate }}</span>
                  </div>
                </div>
              </div>

              <footer class="card-footer">
                <button class="action-btn" @click="handleManage(item)">
                  {{ currentTab === 'class' ? '管理名册' : '管理选课' }}
                </button>
                <div class="v-divider"></div>
                <button class="action-btn delete" @click="handleDelete(item)">移除</button>
              </footer>
            </div>
          </div>
        </transition>
      </div>

      <div v-if="filteredData.length === 0" class="empty-state">
        <div class="empty-icon">📂</div>
        <p>暂无匹配的{{ currentTab === 'class' ? '班级' : '课程' }}数据</p>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

type TabKey = 'class' | 'course'
const currentTab = ref<TabKey>('class')
const searchQuery = ref('')

// 模拟数据结构：体现解耦逻辑
const classData = ref([
  { id: 1001, name: '24级密码工程01班', studentCount: 32, createDate: '2026-03-01' },
  { id: 1002, name: '24级密码工程02班', studentCount: 28, createDate: '2026-03-01' },
  { id: 1003, name: '25级信息安全专项班', studentCount: 45, createDate: '2026-04-10' },
  { id: 1004, name: '26级信息安全专项班', studentCount: 45, createDate: '2026-04-10' }
])

const courseData = ref([
  { id: 2001, name: '对称密码学应用实务', teacher: '陈教授', studentCount: 120, createDate: '2026-03-15' },
  { id: 2002, name: '后量子密码理论 (选修)', teacher: '林博士', studentCount: 45, createDate: '2026-03-20' },
  { id: 2003, name: '无人机抗重放通信协议', teacher: '张高工', studentCount: 32, createDate: '2026-04-05' },
  { id: 2004, name: '英语', teacher: 'Mr.Lou', studentCount: 32, createDate: '2026-04-05' }
])

// 过滤逻辑
const filteredData = computed(() => {
  const list = currentTab.value === 'class' ? classData.value : courseData.value
  const query = searchQuery.value.toLowerCase().trim()
  if (!query) return list
  return list.filter(i => i.name.toLowerCase().includes(query))
})

const handleCreate = () => alert(`跳转至创建${currentTab.value === 'class' ? '班级' : '课程'}页面`)
const handleManage = (item: any) => alert(`进入 [${item.name}] 的详细管理页面`)
const handleDelete = (item: any) => {
  if (confirm(`确定要移除 [${item.name}] 吗？`)) {
    if (currentTab.value === 'class') {
      classData.value = classData.value.filter(i => i.id !== item.id)
    } else {
      courseData.value = courseData.value.filter(i => i.id !== item.id)
    }
  }
}
</script>

<style scoped>
.manage-container {
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
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

/* ==== 优化后的工具栏布局 ==== */
.toolbar-wrapper {
  display: flex;
  justify-content: space-between; /* 两端对齐：左边 Tabs，右边搜索框 */
  align-items: flex-end; /* 底部对齐，让搜索框和 Tab 贴合底边线 */
  border-bottom: 1px solid #E2E8F0; /* 统一贯穿的底部分割线 */
  margin-top: 24px;
  /* margin-bottom: 24px; */
}

.tabs-nav {
  display: flex;
  gap: 32px;
}

.tab-item {
  padding: 12px 4px;
  font-size: 15px;
  font-weight: 700;
  color: #64748B;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
  margin-bottom: -1px; /* 核心魔法：让高亮下划线盖住贯穿的灰线 */
}

.tab-item.active {
  color: #4F46E5;
  border-bottom-color: #4F46E5;
}

/* 搜索框 */
.search-box { 
  position: relative; 
  width: 320px;
  margin-bottom: 8px; /* 稍微抬高一点点，使其在视觉上和 Tab 文字中心对齐 */
}
.search-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: #94A3B8; font-size: 14px; }
.search-input {
  width: 100%; padding: 10px 10px 10px 40px; border: 1px solid #E2E8F0; border-radius: 10px; font-size: 14px; transition: 0.2s; box-sizing: border-box;
}
.search-input:focus { outline: none; border-color: #818CF8; box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1); }

/* 数据网格布局 */
.data-grid {
  padding-top: 24px;
  height: calc(58vh);
  overflow-y: auto;
  padding-bottom: 24px;
}

.data-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  height: 267.8px;
  box-sizing: border-box;
}

.data-card:hover {
  transform: translateY(-4px);
  border-color: #818CF8;
  box-shadow: 0 12px 30px rgba(79, 70, 229, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.type-badge {
  font-size: 11px;
  font-weight: 800;
  padding: 3px 8px;
  border-radius: 4px;
}
.type-badge.class { background: #F0F9FF; color: #0369A1; }
.type-badge.course { background: #F5F3FF; color: #4F46E5; }

.id-tag { font-size: 12px; color: #CBD5E1; font-family: monospace; }

.card-main { flex: 1; margin-bottom: 24px; }
.item-name { font-size: 18px; font-weight: 800; color: #1E293B; margin: 0 0 16px 0; line-height: 1.4; }

.item-meta { display: flex; flex-direction: column; gap: 8px; }
.meta-row { display: flex; justify-content: space-between; font-size: 14px; }
.meta-label { color: #94A3B8; }
.meta-value { color: #475569; font-weight: 600; }

.card-footer {
  display: flex;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #F1F5F9;
  gap: 8px;
}

.action-btn {
  flex: 1;
  background: none;
  border: none;
  font-size: 14px;
  font-weight: 700;
  color: #64748B;
  cursor: pointer;
  transition: 0.2s;
}

.action-btn:hover { color: #4F46E5; }
.action-btn.delete:hover { color: #EF4444; }

.v-divider { width: 1px; height: 16px; background: #E2E8F0; }

.empty-state {
  text-align: center;
  padding: 100px 0;
  color: #94A3B8;
}
.empty-icon { font-size: 48px; margin-bottom: 16px; }

.grid-wrapper {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.card-fade-enter-active,
.card-fade-leave-active {
  transition: opacity 0.4s cubic-bezier(0.22, 1, 0.36, 1), transform 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}
.card-fade-enter-from,
.card-fade-leave-to {
  opacity: 0;
  transform: translateY(5px) scale(0.98);
}

.grid-wrapper {
  min-height: 100%;
}
</style>