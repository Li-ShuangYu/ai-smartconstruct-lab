<template>
  <div class="my-class-container">
    <header class="class-summary-bar">
      <div class="class-title-area">
        <div class="avatar-icon">🏫</div>
        <div>
          <h1 class="class-name">{{ classInfo.name }}</h1>
          <span class="class-desc">班主任: {{ classInfo.headTeacher }} | 总人数: {{ classInfo.studentCount }}人</span>
        </div>
      </div>
      <div class="class-stats">
        <div class="stat-item">
          <span class="stat-label">进行中实训</span>
          <span class="stat-value highlight">{{ classInfo.activeTrainings }}</span>
        </div>
        <div class="v-divider"></div>
        <div class="stat-item">
          <span class="stat-label">班级活跃度</span>
          <span class="stat-value">98%</span>
        </div>
      </div>
    </header>

    <div class="content-grid">
      <section class="task-section">
        <h2 class="section-title">班级专属实训</h2>
        <div class="task-list">
          <div v-for="task in trainings" :key="task.id" class="task-row">
            <div class="task-main">
              <span class="status-dot" :class="task.status"></span>
              <span class="task-name">{{ task.name }}</span>
            </div>
            <div class="task-meta">
              <span class="task-time">截止: {{ task.deadline }}</span>
              <button class="text-btn">进入实训</button>
            </div>
          </div>
        </div>
      </section>

      <section class="classmates-section">
        <h2 class="section-title">同班同学 ({{ classmates.length }})</h2>
        <div class="avatar-grid">
          <div v-for="student in classmates" :key="student.id" class="student-item">
            <div class="student-avatar">{{ student.name.charAt(0) }}</div>
            <span class="student-name" :title="student.name">{{ student.name }}</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const classInfo = ref({
  name: '24级密码工程01班',
  headTeacher: '付老师',
  studentCount: 32,
  activeTrainings: 2
})

const trainings = ref([
  { id: 1, name: '无人机通信抗重放 (Anti-Replay) 对抗演练', deadline: '2026-05-10', status: 'active' },
  { id: 2, name: 'auth-0 鉴权容器调试与排错', deadline: '2026-05-15', status: 'pending' },
  { id: 3, name: 'SM4 算法加解密实操', deadline: '2026-04-20', status: 'ended' }
])

// 模拟高密度学生名单
const classmates = ref([
  { id: 1, name: '涅槃' }, { id: 2, name: 'Tom' }, { id: 3, name: 'Alice' },
  { id: 4, name: 'Bob' }, { id: 5, name: 'Charlie' }, { id: 6, name: 'David' },
  { id: 7, name: 'Eve' }, { id: 8, name: 'Frank' }, { id: 9, name: 'Grace' },
  { id: 10, name: 'Helen' }, { id: 11, name: 'Ivan' }, { id: 12, name: 'Jack' }
])
</script>

<style scoped>
.my-class-container { padding: 24px; display: flex; flex-direction: column; gap: 20px; box-sizing: border-box; }

/* 顶部信息栏：极其紧凑的单行 Flex */
.class-summary-bar { background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px; padding: 16px 24px; display: flex; justify-content: space-between; align-items: center; }
.class-title-area { display: flex; align-items: center; gap: 16px; }
.avatar-icon { font-size: 32px; background: #F1F5F9; width: 48px; height: 48px; display: flex; align-items: center; justify-content: center; border-radius: 8px; }
.class-name { font-size: 18px; font-weight: 800; color: #0F172A; margin: 0 0 4px 0; }
.class-desc { font-size: 13px; color: #64748B; }

.class-stats { display: flex; align-items: center; gap: 24px; }
.stat-item { display: flex; flex-direction: column; align-items: flex-end; }
.stat-label { font-size: 12px; color: #94A3B8; font-weight: 600; margin-bottom: 2px; }
.stat-value { font-size: 20px; font-weight: 800; color: #1E293B; line-height: 1; }
.stat-value.highlight { color: #4F46E5; }
.v-divider { width: 1px; height: 32px; background: #E2E8F0; }

/* 主体内容区：双列网格结构，杜绝留白 */
.content-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; align-items: start; }
.section-title { font-size: 15px; font-weight: 700; color: #1E293B; margin: 0 0 16px 0; padding-bottom: 8px; border-bottom: 1px solid #E2E8F0; }

/* 实训列表严格单行对齐 */
.task-section { background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px; padding: 20px; }
.task-list { display: flex; flex-direction: column; }
.task-row { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #F1F5F9; }
.task-row:last-child { border-bottom: none; padding-bottom: 0; }
.task-main { display: flex; align-items: center; gap: 10px; flex: 1; overflow: hidden; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.status-dot.active { background: #10B981; }
.status-dot.pending { background: #F59E0B; }
.status-dot.ended { background: #94A3B8; }
.task-name { font-size: 14px; font-weight: 600; color: #334155; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.task-meta { display: flex; align-items: center; gap: 16px; flex-shrink: 0; }
.task-time { font-size: 12px; color: #94A3B8; }
.text-btn { background: none; border: none; color: #4F46E5; font-size: 13px; font-weight: 700; cursor: pointer; padding: 0; }
.text-btn:hover { color: #4338CA; text-decoration: underline; }

/* 高密度头像网格 */
.classmates-section { background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 8px; padding: 20px; }
.avatar-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(64px, 1fr)); gap: 12px; }
.student-item { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.student-avatar { width: 40px; height: 40px; border-radius: 50%; background: #EEF2FF; color: #4F46E5; font-weight: 700; font-size: 16px; display: flex; align-items: center; justify-content: center; }
.student-name { font-size: 12px; color: #475569; width: 100%; text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>