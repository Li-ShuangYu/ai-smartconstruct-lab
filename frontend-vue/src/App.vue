<template>
  <div class="app-container">
    <router-view />
    <!-- AI 悬浮球助手（除登录注册页面外都显示） -->
    <AIFloatingAssistant v-if="showAssistant" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AIFloatingAssistant from '@/components/AIFloatingAssistant/AIFloatingAssistant.vue'

const route = useRoute()

// 判断是否显示 AI 助手（排除登录注册页面）
const showAssistant = computed(() => !route.path.startsWith('/auth'))
</script>

<style>
/* 可以在这里引入你的全局重置样式，或者在 main.ts 中引入 */
@import './assets/styles/reset.css';
@import './assets/styles/variables.css';

/* 确保根节点撑满全屏，消除默认边距 */
html, body, #app {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
}
</style>