<template>
  <div class="workbench-layout">
    <Sidebar />
    <div class="main-container">
      <Header />
      <main class="content-area">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
    
    <Teleport to="body">
      </Teleport>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
</script>

<style scoped>
.workbench-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: #F9FAFB; /* 极简灰白底色 */
  position: relative;
  z-index: 1;
}

/* 左上角主色晕染 */
.workbench-layout::before {
  content: "";
  position: absolute;
  top: -15%;
  left: -5%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(79, 70, 229, 0.06) 0%, rgba(255, 255, 255, 0) 70%);
  z-index: -1; /* 垫在最底层 */
  pointer-events: none; /* 不阻挡任何点击事件 */
}

/* 右下角辅助色/紫罗兰色晕染，形成呼应 */
.workbench-layout::after {
  content: "";
  position: absolute;
  bottom: -20%;
  right: -10%;
  width: 800px;
  height: 800px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.04) 0%, rgba(255, 255, 255, 0) 70%);
  z-index: -1;
  pointer-events: none;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  /* 确保侧边栏和顶部栏之上产生内容区域的毛玻璃质感（可选） */
  background: transparent; 
}

.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
  box-sizing: border-box;
}

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