<template>
  <div class="class-list-container">
    <header class="page-header">
      <div class="header-info">
        <h1 class="panel-main-title">我的班级</h1>
        <p class="subtitle">查看已加入的行政班级及同学动态</p>
      </div>
    </header>

    <main class="class-grid">
      <div v-for="cls in classes" :key="cls.id" class="class-card">
        <div class="class-cover" :style="{ background: cls.color }">
          <span class="class-icon">🏫</span>
        </div>
        <div class="class-content">
          <h3 class="class-name">{{ cls.name }}</h3>
          <div class="class-info">
            <span>授课教师：{{ cls.teacher }}</span>
            <span>班级人数：{{ cls.studentCount }} 人</span>
          </div>
          <button class="enter-btn" @click="handleEnter(cls)">进入班级详情</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const classes = ref([
  { id: 1, name: '24级密码工程01班', teacher: '陈教授', studentCount: 32, color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { id: 2, name: '24级信息安全专项班', teacher: '林博士', studentCount: 28, color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }
])

const handleEnter = (cls: any) => {
  alert(`进入班级：${cls.name}`)
}
</script>

<style scoped>
.class-list-container { padding: 32px; }
.class-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px; }

.class-card { background: white; border-radius: 20px; overflow: hidden; border: 1px solid #E2E8F0; transition: 0.3s; }
.class-card:hover { transform: translateY(-4px); box-shadow: 0 15px 35px rgba(0,0,0,0.05); }

.class-cover { height: 100px; display: flex; align-items: center; justify-content: center; position: relative; }
.class-icon { font-size: 40px; }

.class-content { padding: 24px; }
.class-name { font-size: 18px; font-weight: 800; color: #1E293B; margin: 0 0 12px 0; }
.class-info { display: flex; flex-direction: column; gap: 6px; font-size: 14px; color: #64748B; margin-bottom: 24px; }

.enter-btn { width: 100%; padding: 12px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; color: #475569; font-weight: 700; cursor: pointer; transition: 0.2s; }
.enter-btn:hover { background: #4F46E5; color: white; border-color: #4F46E5; }
</style>