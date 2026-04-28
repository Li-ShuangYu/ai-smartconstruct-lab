<template>
  <div class="course-container">
    <header class="page-header">
      <h1 class="panel-main-title">课程空间</h1>
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input 
          type="text" 
          v-model="searchQuery"
          placeholder="搜索课程名称或教师..." 
          class="search-input" 
        />
      </div>
    </header>

    <main class="data-grid">
      <div v-for="course in paginatedList" :key="course.id" class="data-card">
        <div class="card-header">
          <span class="type-badge">{{ course.type }}</span>
          <span class="id-tag">#{{ course.courseCode }}</span>
        </div>
        <h3 class="item-name" :title="course.name">{{ course.name }}</h3>
        <div class="item-meta">
          <div class="meta-row">
            <span class="meta-label">授课教师</span>
            <span class="meta-value">{{ course.teacher }}</span>
          </div>
          <div class="meta-row">
            <span class="meta-label">课程学分</span>
            <span class="meta-value">{{ course.credit }}</span>
          </div>
        </div>
        <footer class="card-footer">
          <button class="action-btn primary-text" @click="enterCourse(course)">进入课程区</button>
        </footer>
      </div>

      <div v-if="paginatedList.length === 0" class="empty-state">
        暂无匹配的课程数据
      </div>
    </main>

    <div class="pagination-wrapper" v-if="filteredList.length > 0">
      <Pagination 
        :total="filteredList.length"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[4, 8, 12, 16]" 
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import Pagination from '@/views/common/Pagination.vue'

// 搜索与分页状态
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(8) // 设定每页默认显示 8 个

// 扩充的模拟课程数据
const courseList = ref([
  { id: 1, courseCode: 'C-2026-01', name: '密码系统设计与理论', teacher: '付老师', credit: '4.0', type: '专业必修' },
  { id: 2, courseCode: 'C-2026-02', name: 'UAV通信加密专论', teacher: '陈教授', credit: '2.5', type: '专业选修' },
  { id: 3, courseCode: 'C-2026-03', name: '现代密码工程基础', teacher: '林博士', credit: '3.0', type: '专业必修' },
  { id: 4, courseCode: 'C-2026-04', name: '网络安全协议分析', teacher: '张高工', credit: '2.0', type: '专业必修' },
  { id: 5, courseCode: 'C-2026-05', name: '公钥密码学导论', teacher: '王教授', credit: '3.5', type: '专业必修' },
  { id: 6, courseCode: 'C-2026-06', name: '侧信道攻击与防御', teacher: '李研究员', credit: '2.0', type: '专业选修' },
  { id: 7, courseCode: 'C-2026-07', name: '硬件安全基础', teacher: '赵博士', credit: '2.5', type: '专业必修' },
  { id: 8, courseCode: 'C-2026-08', name: '量子计算与密码', teacher: '陈教授', credit: '2.0', type: '专业选修' },
  { id: 9, courseCode: 'C-2026-09', name: '区块链技术与应用', teacher: '周博士', credit: '3.0', type: '专业选修' },
  { id: 10, courseCode: 'C-2026-10', name: '可信计算理论', teacher: '付老师', credit: '2.5', type: '专业必修' }
])

// 1. 优先处理搜索过滤
const filteredList = computed(() => {
  const query = searchQuery.value.toLowerCase().trim()
  if (!query) return courseList.value
  return courseList.value.filter(c => 
    c.name.toLowerCase().includes(query) || 
    c.teacher.toLowerCase().includes(query)
  )
})

// 监听搜索词变化，如果用户搜索了新内容，自动回到第一页
watch(searchQuery, () => {
  currentPage.value = 1
})

// 2. 再处理分页截取
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

const enterCourse = (course: any) => {
  console.log('进入课程:', course.name)
}
</script>

<style scoped>
.course-container { padding: 20px; box-sizing: border-box; }
.page-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #E2E8F0; padding-bottom: 16px; margin-bottom: 24px; }
.panel-main-title { font-size: 30px; font-weight: 800; color: #0F172A; margin: 0; }

/* 搜索框样式 */
.search-box { position: relative; width: 280px; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94A3B8; font-size: 13px; }
.search-input { width: 100%; padding: 8px 12px 8px 32px; border: 1px solid #E2E8F0; border-radius: 6px; font-size: 13px; outline: none; box-sizing: border-box; transition: 0.2s;}
.search-input:focus { border-color: #4F46E5; box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);}

/* 网格布局：添加 min-height 和 align-content 防止跨页跳动 */
.data-grid { 
  display: grid; 
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); 
  gap: 16px; 
  height: 414px;
  align-content: start;
}

.data-card { background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px; padding: 16px; display: flex; flex-direction: column; transition: all 0.2s; }
.data-card:hover { border-color: #818CF8; box-shadow: 0 4px 12px rgba(79, 70, 229, 0.08); transform: translateY(-2px); }

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.type-badge { background: #EEF2FF; color: #4F46E5; font-size: 11px; font-weight: 700; padding: 2px 6px; border-radius: 4px; }
.id-tag { font-size: 12px; color: #94A3B8; font-family: monospace; }

/* 课程名称：超长隐藏 */
.item-name { font-size: 16px; font-weight: 800; color: #1E293B; margin: 0 0 16px 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;}

.item-meta { display: flex; flex-direction: column; gap: 8px; flex: 1; margin-bottom: 16px; }
.meta-row { display: flex; justify-content: space-between; font-size: 13px; }
.meta-label { color: #64748B; }
.meta-value { color: #1E293B; font-weight: 600; }

.card-footer { border-top: 1px solid #F1F5F9; padding-top: 12px; display: flex; justify-content: flex-end; }
.action-btn { background: none; border: none; font-size: 13px; font-weight: 700; cursor: pointer; padding: 0; }
.action-btn.primary-text { color: #4F46E5; }
.action-btn:hover { color: #4338CA; text-decoration: underline;}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 0;
  color: #94A3B8;
  font-size: 14px;
  background-color: #F8FAFC;
  border-radius: 8px;
  border: 1px dashed #E2E8F0;
}

/* 分页容器靠右对齐，与顶部搜索框呼应 */
.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>