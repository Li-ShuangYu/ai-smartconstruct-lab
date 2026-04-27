<template>
  <div class="workbench-layout">
    <SuperHeader v-if="renderFullscreen" />
    
    <template v-else>
      <StudentHeader v-if="isStudentSide" />
      <TeacherHeader v-else />
    </template>
    
    <div class="main-container">
      <Sidebar v-if="!renderFullscreen && !isStudentSide" />
      
      <main :class="['content-area', { 'is-fullscreen': renderFullscreen }]">
        <router-view v-slot="{ Component, route }">
          <transition 
            name="fade" 
            mode="out-in"
            @after-leave="applyLayoutChange"
          >
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './Sidebar.vue'
import TeacherHeader from './TeacherHeader.vue'      
import StudentHeader from './StudentHeader.vue' 
import SuperHeader from './SuperHeader.vue'

const route = useRoute()

// 1. 判断是否处于学生端页面
const isStudentSide = computed(() => route.path.startsWith('/student'))

// 2. 实际控制页面布局渲染的变量
const renderFullscreen = ref(route.meta.hideSidebar === true)

// 3. 暂存即将切换的状态（用于动画结束后的同步）
let pendingLayoutState: boolean | null = null

// 4. 监听路由 meta，处理全屏模式切换
watch(
  () => route.meta.hideSidebar,
  (newVal) => {
    const isNowFullscreen = newVal === true
    if (isNowFullscreen !== renderFullscreen.value) {
      pendingLayoutState = isNowFullscreen
    }
  }
)

// 5. 动画钩子：确保旧页面彻底消失后再切换外层 Header/Sidebar，防止排版崩坏
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
/* 布局样式保持不变 */
.workbench-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: #F9FAFB;
}

.main-container {
  flex: 1;
  display: flex;
  min-width: 0;
}

.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
  box-sizing: border-box;
}

/* 只有真正的沉浸式模式（renderFullscreen）下才取消内边距 */
.content-area.is-fullscreen {
  padding: 0;
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