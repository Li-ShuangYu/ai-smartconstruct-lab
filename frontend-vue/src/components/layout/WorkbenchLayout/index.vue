<template>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <div class="workbench-layout">
    <SuperHeader v-if="renderFullscreen" />
    <template v-else>
      <StudentHeader v-if="isStudentSide" />
      <AdminHeader v-else-if="isAdminSide" />
      <TeacherHeader v-else />
    </template>
    
    <div class="main-container">
      <div class="bg-glow"></div>
      
      <AdminSidebar v-if="isAdminSide && !renderFullscreen" />
      
      <Sidebar v-if="!isAdminSide && !isStudentSide && !renderFullscreen" />

      <main :class="['content-area', { 'is-fullscreen': renderFullscreen }]">
        <n-message-provider>
          <router-view v-slot="{ Component, route }">
            <transition
              name="fade"
              mode="out-in"
              @after-leave="applyLayoutChange"
            >
              <component :is="Component" :key="route.path" />
            </transition>
          </router-view>
        </n-message-provider>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { NMessageProvider } from 'naive-ui'
import Sidebar from './Sidebar.vue'
import AdminSidebar from './AdminSidebar.vue'
import TeacherHeader from './TeacherHeader.vue'
import StudentHeader from './StudentHeader.vue'
import SuperHeader from './SuperHeader.vue'
import AdminHeader from './AdminHeader.vue'

const route = useRoute()

const isStudentSide = computed(() => route.path.startsWith('/student'))
const isAdminSide = computed(() => route.path.startsWith('/admin'))
const renderFullscreen = ref(route.meta.hideSidebar === true)
let pendingLayoutState: boolean | null = null

watch(
  () => route.meta.hideSidebar,
  (newVal) => {
    const isNowFullscreen = newVal === true
    if (isNowFullscreen !== renderFullscreen.value) {
      pendingLayoutState = isNowFullscreen
    }
  }
)

const applyLayoutChange = () => {
  if (pendingLayoutState !== null) {
    renderFullscreen.value = pendingLayoutState
    pendingLayoutState = null
  }
}
</script>

<style>
/* 全局滚动条样式保持不变 */
* {
  scrollbar-width: thin;
  scrollbar-color: rgba(79, 70, 229, 0.4) transparent;
}
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-thumb { background: rgba(79, 70, 229, 0.25); border-radius: 6px; }
::-webkit-scrollbar-thumb:hover { background: #4F46E5; }
</style>

<style scoped>
/* 原有的 workbench-layout 保持不变 */
.workbench-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: #F9FAFB; /* 也可以考虑将这里的背景色去掉，完全由 main-container 接管 */
}

.main-container {
  flex: 1;
  display: flex;
  min-width: 0;
  
  /* --- 新增：沉浸式双角渐变背景 --- */
  background-color: #f8fafc;
  background-image: 
    radial-gradient(at 0% 0%, rgba(99, 102, 241, 0.05) 0px, transparent 50%),
    radial-gradient(at 100% 0%, rgba(168, 85, 247, 0.05) 0px, transparent 50%);
  
  /* --- 新增：为 bg-glow 提供定位基准并防止溢出 --- */
  position: relative;
  overflow: hidden; 
}

/* --- 新增：顶部高光光晕样式 --- */
.bg-glow {
  position: absolute;
  top: -50px; /* 根据你的 Header 高度可适当调整，原先是 -100px */
  left: 50%;
  transform: translateX(-50%);
  width: 600px;
  height: 300px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.08) 0%, transparent 70%);
  pointer-events: none; /* 关键：防止光晕遮挡 Sidebar 或主内容区的点击事件 */
  z-index: 0;
}

.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 32px 32px 0px 32px ;
  box-sizing: border-box;
  
  /* --- 新增：确保内容层级在光晕之上 --- */
  position: relative;
  z-index: 1;
}

/* 只有真正的沉浸式模式（renderFullscreen）下才取消内边距 */
.content-area.is-fullscreen {
  padding: 0;
}

/* 过渡动画保持不变 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(4px);
}
</style>