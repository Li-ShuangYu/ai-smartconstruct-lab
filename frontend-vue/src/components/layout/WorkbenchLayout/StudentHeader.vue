<template>
  <header class="workbench-header">
    <div class="header-left">
      <div class="logo-area" @click="switchToHome">
        <img src="@/assets/AIZG-Logo.png" alt="logo" class="logo-img" />
        <span class="logo-text">AI 学苑 / 智学版</span>
      </div>
      
      <nav class="top-nav">
        <div 
          class="nav-item" 
          :class="{ active: route.path.includes('/student/workbench') }"
          @click="navigateTo('/student/workbench')"
        >
          实训工作台
        </div>
        <div 
          class="nav-item" 
          :class="{ active: route.path.includes('/student/my-class') }"
          @click="navigateTo('/student/my-class')"
        >
          我的班级
        </div>
        <div 
          class="nav-item" 
          :class="{ active: route.path.includes('/student/courselist') }"
          @click="navigateTo('/student/courselist')"
        >
          课程空间
        </div>
        <div 
          class="nav-item" 
          :class="{ active: route.path.includes('/student/notifications') }"
          @click="navigateTo('/student/notifications')"
        >
          消息通知
        </div>
      </nav>
    </div>

    <div class="header-right">
      <button class="icon-btn">
        <IconBell class="icon" />
      </button>
      
      <div class="user-menu-container" ref="userMenuRef">
        <div class="user-profile" @click="toggleDropdown" :class="{ 'is-active': isDropdownOpen }">
          <div class="avatar">T</div>
          <span class="user-name">Tom 同学</span>
          <svg class="chevron-icon" :class="{ 'rotate': isDropdownOpen }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="6 9 12 15 18 9"></polyline>
          </svg>
        </div>

        <transition name="dropdown-fade">
          <div class="dropdown-menu" v-if="isDropdownOpen">
            <div class="dropdown-item" @click="handleAction('/student/profile')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
              个人中心
            </div>
            <div class="dropdown-item" @click="handleAction('/student/settings')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
              设置
            </div>
            
            <div class="dropdown-divider"></div>
            
            <div class="dropdown-item danger" @click="handleLogout">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
              退出登录
            </div>
          </div>
        </transition>
      </div>

    </div>  
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const switchToHome = () => {
  router.push('/student')
}

// 统一的路由跳转方法
const navigateTo = (path: string) => {
  router.push(path)
}

// ================= 下拉菜单逻辑 =================
const isDropdownOpen = ref(false)
const userMenuRef = ref<HTMLElement | null>(null)

const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value
}

// 点击页面其他区域关闭下拉菜单
const closeDropdown = (e: MouseEvent) => {
  if (userMenuRef.value && !userMenuRef.value.contains(e.target as Node)) {
    isDropdownOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', closeDropdown)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeDropdown)
})

// 点击菜单项后的动作
const handleAction = (path: string) => {
  isDropdownOpen.value = false
  navigateTo(path)
}

// 退出登录逻辑
const handleLogout = () => {
  isDropdownOpen.value = false
  // 此处后续可补充清空 Token 或状态的逻辑
  router.push('/auth/login') // 假设有登录页路由
}
</script>

<style scoped>
.workbench-header {
  height: 64px;
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
  gap: 24px;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  margin-right: 16px;
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
  background-color: var(--color-border, #E5E7EB); 
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

/* ================= 用户菜单区 ================= */
.user-menu-container {
  position: relative;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.user-profile:hover, .user-profile.is-active {
  background-color: var(--color-border, #F3F4F6);
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

/* 箭头动画 */
.chevron-icon {
  width: 14px;
  height: 14px;
  color: #6B7280;
  transition: transform 0.3s ease;
}

.chevron-icon.rotate {
  transform: rotate(180deg);
}

/* 下拉菜单面板 */
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  width: 160px;
  background-color: #ffffff;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  padding: 6px 0;
  z-index: 100;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #4B5563;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dropdown-item svg {
  width: 16px;
  height: 16px;
  color: #9CA3AF;
  transition: color 0.2s;
}

.dropdown-item:hover {
  background-color: #F9FAFB;
  color: #111827;
}

.dropdown-item:hover svg {
  color: #4F46E5;
}

/* 分割线 */
.dropdown-divider {
  height: 1px;
  background-color: #F3F4F6;
  margin: 4px 0;
}

/* 退出登录等危险操作 */
.dropdown-item.danger:hover {
  background-color: #FEF2F2;
  color: #DC2626;
}

.dropdown-item.danger:hover svg {
  color: #DC2626;
}

/* 下拉动画 */
.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>