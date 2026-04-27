<template>
  <div class="course-container">
    <header class="page-header">
      <h1 class="panel-main-title">我的课程</h1>
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input type="text" placeholder="搜索课程名称..." class="search-input" />
      </div>
    </header>

    <main class="data-grid">
      <div v-for="course in courseList" :key="course.id" class="data-card">
        <div class="card-header">
          <span class="type-badge">{{ course.type }}</span>
          <span class="id-tag">#{{ course.courseCode }}</span>
        </div>
        <h3 class="item-name">{{ course.name }}</h3>
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
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const courseList = ref([
  { id: 1, courseCode: 'C-2026-01', name: '密码系统设计与理论', teacher: '付老师', credit: '4.0', type: '专业必修' },
  { id: 2, courseCode: 'C-2026-02', name: 'UAV通信加密专论', teacher: '陈教授', credit: '2.5', type: '专业选修' },
  { id: 3, courseCode: 'C-2026-03', name: '现代密码工程基础', teacher: '林博士', credit: '3.0', type: '专业必修' }
])

const enterCourse = (course: any) => {
  console.log('进入课程:', course.name)
}
</script>

<style scoped>
.course-container { padding: 24px; box-sizing: border-box; }
.page-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #E2E8F0; padding-bottom: 16px; margin-bottom: 24px; }
.panel-main-title { font-size: 20px; font-weight: 800; color: #0F172A; margin: 0; }
.search-box { position: relative; width: 280px; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94A3B8; font-size: 13px; }
.search-input { width: 100%; padding: 8px 12px 8px 32px; border: 1px solid #E2E8F0; border-radius: 6px; font-size: 13px; outline: none; box-sizing: border-box; }
.search-input:focus { border-color: #4F46E5; }

.data-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.data-card { background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px; padding: 16px; display: flex; flex-direction: column; transition: all 0.2s; }
.data-card:hover { border-color: #818CF8; box-shadow: 0 4px 12px rgba(79, 70, 229, 0.08); transform: translateY(-2px); }

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.type-badge { background: #EEF2FF; color: #4F46E5; font-size: 11px; font-weight: 700; padding: 2px 6px; border-radius: 4px; }
.id-tag { font-size: 12px; color: #94A3B8; font-family: monospace; }

.item-name { font-size: 16px; font-weight: 800; color: #1E293B; margin: 0 0 16px 0; }
.item-meta { display: flex; flex-direction: column; gap: 8px; flex: 1; margin-bottom: 16px; }
.meta-row { display: flex; justify-content: space-between; font-size: 13px; }
.meta-label { color: #64748B; }
.meta-value { color: #1E293B; font-weight: 600; }

.card-footer { border-top: 1px solid #F1F5F9; padding-top: 12px; display: flex; justify-content: flex-end; }
.action-btn { background: none; border: none; font-size: 13px; font-weight: 700; cursor: pointer; padding: 0; }
.action-btn.primary-text { color: #4F46E5; }
.action-btn:hover { color: #4338CA; }
</style>