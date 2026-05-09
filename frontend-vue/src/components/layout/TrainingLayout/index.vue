<template>
  <n-message-provider>
    <div class="workbench-layout-wrapper">
      <!-- 系统发光背景 -->
      <div class="bg-glow"></div>

      <!-- 核心玻璃拟态主卡片 -->
      <div class="console-main-card glass-card">
        
        <!-- 1. 顶部动态 Header (这里调用纯净版 Header，就不会套娃了) -->
        <StudentHeader v-if="isStudentSide" />
        <TeacherHeader v-else />

        <!-- 2. 主内容路由区 (自动填充剩余空间并独立滚动) -->
        <main class="console-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </main>

      </div>
    </div>
  </n-message-provider>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { NMessageProvider } from 'naive-ui'
import StudentHeader from './StudentHeader.vue'
import TeacherHeader from './TeacherHeader.vue'

const route = useRoute()

// 智能判断当前端点环境
const isStudentSide = computed(() => route.path.includes('/student'))
</script>

<style scoped>
/* 容器全屏撑开，继承系统底色和光晕 */
.workbench-layout-wrapper {
  height: 100vh;
  padding: 12px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  box-sizing: border-box;
  background: linear-gradient(135deg, #f3f4f6 0%, #eef2ff 100%);
  position: relative;
  overflow: hidden;
}

.bg-glow {
  position: absolute;
  top: -100px;
  left: 50%;
  transform: translateX(-50%);
  width: 800px;
  height: 400px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.15) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
}

/* 主面板：白底毛玻璃拟物化 */
.console-main-card {

  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column; /* 核心：垂直 Flex 布局，保证 Header 在上，Content 在下 */
  overflow: hidden;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 10px 30px -5px rgba(15, 23, 42, 0.05);
  z-index: 10;
}

/* 内容区：占据剩余全部空间并允许内部滚动，绝不挤压 Header */
.console-content {
  height: 88vh;
  /* flex: 1; */
  overflow-y: auto;
  position: relative;
  background: transparent; 
}

/* 路由切换动画 */
.fade-enter-active, .fade-leave-active { 
  transition: opacity 0.2s ease, transform 0.2s ease; 
}
.fade-enter-from, .fade-leave-to { 
  opacity: 0; 
  transform: translateY(4px);
}
</style>