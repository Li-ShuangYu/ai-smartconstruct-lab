<template>
  <aside class="workbench-sidebar">
    <div class="logo-area">
      <div class="logo-mark"></div>
      <span class="logo-text">AI 学苑 / 教研版</span>
    </div>

    <nav class="nav-menu">
      <router-link 
        v-for="item in menuItems" 
        :key="item.path" 
        :to="item.path"
        class="nav-item"
        active-class="nav-item--active"
      >
        <component :is="item.icon" class="nav-icon" />
        <span class="nav-label">{{ item.label }}</span>
      </router-link>
    </nav>
  </aside>
</template>

<script setup lang="ts">
// 这里假设你已经有了基础的 svg icon 组件
import { ref } from 'vue'

// 路由配置映射，符合你 teacher.ts 路由模块的规划
const menuItems = ref([
  { path: '/teacher/workbench', label: '工作台概览', icon: 'IconDashboard' },
  { path: '/teacher/training', label: '实训编排与管理', icon: 'IconWorkflow' },
  { path: '/teacher/assignment', label: '作业与题库', icon: 'IconBook' },
  { path: '/teacher/evaluation', label: '多维学情评价', icon: 'IconChart' }
])
</script>

<style scoped>
.workbench-sidebar {
  width: 260px;
  height: 100vh;
  background-color: var(--color-surface, #ffffff);
  border-right: 1px solid var(--color-border, #E5E7EB);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  box-sizing: border-box;
}

.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid var(--color-border-light, #F3F4F6);
}

.logo-mark {
  width: 24px;
  height: 24px;
  background-color: var(--color-primary, #4F46E5);
  border-radius: 6px;
  margin-right: 12px;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #111827);
  letter-spacing: 0.5px;
}

.nav-menu {
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 16px;
  border-radius: 8px;
  color: var(--text-secondary, #4B5563);
  text-decoration: none;
  transition: all 0.2s ease;
}

.nav-item:hover {
  background-color: var(--color-hover, #F3F4F6);
}

.nav-item--active {
  background-color: var(--color-primary-light, #EEF2FF);
  color: var(--color-primary, #4F46E5);
  font-weight: 500;
}

.nav-icon {
  width: 20px;
  height: 20px;
  margin-right: 12px;
}

.nav-label {
  font-size: 14px;
}
</style>