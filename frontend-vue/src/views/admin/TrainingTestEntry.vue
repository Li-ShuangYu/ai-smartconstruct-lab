<template>
  <div class="admin-page">
    <div class="page-header">
      <h1>实训页面测试入口</h1>
      <p class="text-gray-500 text-sm mt-2">快速访问各实训页面进行预览</p>
    </div>

    <!-- 学生端实训入口 -->
    <div class="mb-8">
      <h2 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
        <svg class="w-5 h-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
        </svg>
        学生端实训页面
      </h2>
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
        <button
          v-for="item in studentRoutes"
          :key="item.path"
          class="p-3 bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-100 rounded-xl hover:shadow-md hover:border-blue-300 transition-all text-left group"
          @click="navigateTo(item.path)"
        >
          <div class="text-sm font-medium text-gray-800 group-hover:text-indigo-600 transition-colors">
            {{ item.meta.title }}
          </div>
          <div class="text-xs text-gray-400 mt-1 truncate">{{ item.path }}</div>
        </button>
      </div>
    </div>

    <!-- 教师端实训入口 -->
    <div>
      <h2 class="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
        <svg class="w-5 h-5 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
        </svg>
        教师端实训监控页面
      </h2>
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
        <button
          v-for="item in teacherRoutes"
          :key="item.path"
          class="p-3 bg-gradient-to-br from-purple-50 to-pink-50 border border-purple-100 rounded-xl hover:shadow-md hover:border-purple-300 transition-all text-left group"
          @click="navigateTo(item.path)"
        >
          <div class="text-sm font-medium text-gray-800 group-hover:text-purple-600 transition-colors">
            {{ item.meta.title }}
          </div>
          <div class="text-xs text-gray-400 mt-1 truncate">{{ item.path }}</div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

// 学生端实训路由列表
const studentRoutes = [
  { path: '/training/student-training/start-portal', meta: { title: '实训入口' } },
  { path: '/training/student-training/resource-viewer', meta: { title: '资源查看器' } },
  { path: '/training/student-training/video-player', meta: { title: '视频播放' } },
  { path: '/training/student-training/mind-map-preview', meta: { title: '思维导图预览' } },
  { path: '/training/student-training/task-board', meta: { title: '任务看板' } },
  { path: '/training/student-training/ai-study-card', meta: { title: 'AI学习卡片' } },
  { path: '/training/student-training/ai-practice', meta: { title: 'AI练习' } },
  { path: '/training/student-training/mind-map-editor', meta: { title: '思维导图编辑' } },
  { path: '/training/student-training/requirement-cloud', meta: { title: '需求云图' } },
  { path: '/training/student-training/plan-upload', meta: { title: '方案上传' } },
  { path: '/training/student-training/simulated-ide', meta: { title: '模拟IDE' } },
  { path: '/training/student-training/homework-engine', meta: { title: '作业引擎' } },
  { path: '/training/student-training/peer-review', meta: { title: '同伴互评' } },
  { path: '/training/student-training/teacher-comment', meta: { title: '教师点评' } },
  { path: '/training/student-training/summary-report', meta: { title: '总结报告' } },
]

// 教师端实训路由列表
const teacherRoutes = [
  { path: '/training/teacher-training/start-portal', meta: { title: '实训入口监控' } },
  { path: '/training/teacher-training/resource-viewer', meta: { title: '资源查看监控' } },
  { path: '/training/teacher-training/video-player', meta: { title: '视频播放监控' } },
  { path: '/training/teacher-training/mind-map-preview', meta: { title: '思维导图预览监控' } },
  { path: '/training/teacher-training/task-board', meta: { title: '任务看板监控' } },
  { path: '/training/teacher-training/ai-study-card', meta: { title: 'AI学习卡片监控' } },
  { path: '/training/teacher-training/ai-practice', meta: { title: 'AI练习监控' } },
  { path: '/training/teacher-training/mind-map-editor', meta: { title: '思维导图编辑监控' } },
  { path: '/training/teacher-training/requirement-cloud', meta: { title: '需求云图监控' } },
  { path: '/training/teacher-training/plan-upload', meta: { title: '方案上传监控' } },
  { path: '/training/teacher-training/simulated-ide', meta: { title: '模拟IDE监控' } },
  { path: '/training/teacher-training/homework-engine', meta: { title: '作业引擎监控' } },
  { path: '/training/teacher-training/peer-review', meta: { title: '同伴互评监控' } },
  { path: '/training/teacher-training/teacher-comment', meta: { title: '教师点评监控' } },
  { path: '/training/teacher-training/summary-report', meta: { title: '总结报告监控' } },
]

const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.admin-page {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  min-height: 100%;
}

.page-header h1 {
  font-size: 20px;
  font-weight: 800;
  color: #0F172A;
  margin: 0;
}
</style>
