<template>
  <header class="workbench-header">
    <div class="header-left">
      <div class="logo-area" @click="switchToAdmin">
        <img src="@/assets/AIZG-Logo.png" alt="logo" class="logo-img" />
        <span class="logo-text">管理员系统</span>
      </div>
      <!-- <h2 class="page-title">{{ currentRouteTitle }}</h2> -->
    </div>

    <div class="header-right">
      <button class="icon-btn">
        <span class="icon">🔔</span>
      </button>

      <div class="user-profile">
        <div class="avatar">管</div>
        <span class="user-name">Admin</span>
      </div>

      <button class="logout-btn" @click="loginout" title="退出登录">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
      </button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/modules/auth.store'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const currentRouteTitle = computed(() => route.meta.title || '工作台')

const switchToAdmin = () => {
  router.push('/admin')
}

const loginout = () => {
  authStore.logout()
  router.push('/auth/login')
}
</script>

<style scoped>
.workbench-header {
  height: 64px;
  /* 加上这一行：严禁被 Flex 容器压缩！ */
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
  gap: 32px;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
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

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #111827);
  margin: 0;
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

.logout-btn {
  background: none;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  padding: 6px 10px;
  cursor: pointer;
  color: #6B7280;
  display: flex;
  align-items: center;
  transition: all 0.2s;
}

.logout-btn:hover {
  color: #DC2626;
  border-color: #FECACA;
  background: #FEF2F2;
}
</style>