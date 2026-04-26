<template>
  <div class="not-found-container">
    <h1 class="not-found-title">404</h1>
    <p class="not-found-message">页面不存在</p>
    <router-link to="/" class="back-home">返回首页</router-link>
  </div>
</template>

<script setup lang="ts">
// 404 页面组件
</script>

<style scoped>
.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  text-align: center;
  background-color: #f9fafb;
}

.not-found-title {
  font-size: 96px;
  font-weight: 800;
  color: #4f46e5;
  margin: 0;
}

.not-found-message {
  font-size: 24px;
  color: #6b7280;
  margin: 24px 0 32px;
}

.back-home {
  background-color: #4f46e5;
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: background-color 0.2s;
}

.back-home:hover {
  background-color: #4338ca;
}
</style>