<template>
  <div class="training-detail">
    <header class="page-header">
      <h1 class="panel-main-title">实训详情</h1>
    </header>
    <div class="content">
      <p>实训详情页面开发中...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
const trainingId = route.params.id
</script>

<style scoped>
.training-detail {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.content {
  background: white;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
</style>