<template>
  <div class="workbench-layout">
    <SuperHeader v-if="hideSidebar" />
    
    <Header v-else />
    
    <div class="main-container">
      <Sidebar v-if="!hideSidebar" />
      
      <main :class="['content-area', { 'is-fullscreen': hideSidebar }]">
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
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import SuperHeader from './SuperHeader.vue'

const route = useRoute()

// 定义计算属性：如果路由 meta 中带有 hideSidebar: true，则使用 SuperHeader
const hideSidebar = computed(() => {
  return route.meta.hideSidebar === true
})
</script>

<style>
/* 不加 scoped：这会让这套品牌色滚动条在整个系统内生效 
  以后你写的任何新页面，都不用再单独写滚动条样式了！
*/

/* Firefox 兼容 */
* {
  scrollbar-width: thin;
  scrollbar-color: rgba(79, 70, 229, 0.4) transparent;
}

html {
  overflow-y: overlay; /* 让滚动条不挤压页面宽度 */
}

body {
  -ms-overflow-style: -ms-autohiding-scrollbar; /* IE/Edge 兼容 */
}

/* WebKit 内核 (Chrome, Edge, Safari) 极致美化 */
::-webkit-scrollbar {
  width: 6px;  /* 细细的垂直滚动条 */
  height: 6px; /* 细细的水平滚动条 */
}

::-webkit-scrollbar-track {
  background: transparent; /* 轨道全透明，显得极其干净 */
}

::-webkit-scrollbar-thumb {
  background: rgba(79, 70, 229, 0.25); /* 默认是很柔和的半透明品牌色 */
  border-radius: 6px;
  transition: background-color 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
  background: #4F46E5; /* 鼠标悬停时，立刻变成纯正的品牌主色调 */
}
</style>

<style scoped>
.workbench-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: #F9FAFB;
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
  z-index: -1; 
  pointer-events: none; 
}

/* 右下角辅助色晕染 */
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
  min-width: 0;
  background: transparent; 
}

.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
  box-sizing: border-box;
  /* 此处的灰色滚动条已被移除，现在由上方的全局样式接管！ */
}

/* 当没有侧边栏时，取消内边距，让画布/大屏绝对贴边满屏 */
.content-area.is-fullscreen {
  padding: 0;
}

/* 页面切换路由时的过渡动画 */
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