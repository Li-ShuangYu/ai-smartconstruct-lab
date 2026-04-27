<template>
  <header class="workbench-header">
    <div class="header-left">
      <div class="logo-area" @click="switchToTeacher">
        <img src="@/assets/AIZG-Logo.png" alt="logo" class="logo-img" />
        <span class="logo-text">AI 学苑 / 智学版</span>
      </div>
      
      <nav class="top-nav">
        <div 
          class="nav-item" 
          :class="{ active: route.path.includes('/student/workbench') }"
          @click="navigateTo('/student/workbench')"
        >
          实训首页
        </div>
        <div 
          class="nav-item" 
          :class="{ active: route.path.includes('/student/classes') }"
          @click="navigateTo('/student/classes')"
        >
          我的班级
        </div>
        <div 
          class="nav-item" 
          :class="{ active: route.path.includes('/student/notifications') }"
          @click="navigateTo('/student/notifications')"
        >
          消息通知
        </div>
        <div 
          class="nav-item" 
          :class="{ active: route.path.includes('/student/profile') }"
          @click="navigateTo('/student/profile')"
        >
          个人中心
        </div>
      </nav>
    </div>

    <div class="header-right">
      <button class="icon-btn">
        <IconBell class="icon" />
      </button>
      
      <div class="user-profile" @click="navigateTo('/student/profile')">
        <div class="avatar">T</div>
        <span class="user-name">Tom 同学</span>
      </div>
    </div>  
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 假设你在路由 meta 中定义了 title
const currentRouteTitle = computed(() => route.meta.title || '工作台')

const switchToTeacher = () => {
  router.push('/teacher')
}

// 统一的路由跳转方法
const navigateTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
.workbench-header {
  height: 64px;
  /* 严禁被 Flex 容器压缩！ */
  flex-shrink: 0; 
  
  background-color: var(--color-surface, #ffffff);
  border-bottom: 1px solid var(--color-border, #E5E7EB);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-sizing: border-box;
  width: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px; /* 调整间距以配合导航栏 */
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  margin-right: 16px; /* 增加与右侧导航的分割区 */
}

.logo-img {
  width: 35px;
  height: 35px;
  border-radius: 6px;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #111827);
  letter-spacing: 0.5px;
}

/* 导航栏样式设定：无多余留白 */
.top-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  padding: 6px 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary, #6B7280);
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.nav-item:hover {
  background-color: var(--color-border, #E5E7EB); /* 悬浮颜色，可根据需要调整为更深的暗色变量 */
  color: var(--text-primary, #111827);
}

.nav-item.active {
  background-color: var(--color-primary-light, #EEF2FF);
  color: var(--color-primary, #4F46E5);
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-secondary, #6B7280);
  padding: 4px;
  display: flex;
  align-items: center;
}

.icon-btn:hover {
  color: var(--text-primary, #111827);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.2s ease;
}

.user-profile:hover {
  background-color: var(--color-border, #E5E7EB);
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--color-primary, #4F46E5);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary, #111827);
}
</style>