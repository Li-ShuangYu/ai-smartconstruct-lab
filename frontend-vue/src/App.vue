<template>
  <router-view />
</template>

<script setup lang="ts">
// 根组件当前不需要任何额外逻辑
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