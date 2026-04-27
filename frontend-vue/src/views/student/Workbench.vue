<template>
  <div class="open-maic-wrapper">
    <div class="bg-glow"></div>

    <header class="hero-section">
  <div class="brand">
    <img src="@/assets/AIZG-Logo.png" alt="logo" class="logo-img" />
    <h1 class="logo-text">AI学苑<span>实训平台</span></h1>
  </div>
  <p class="slogan">密码学多智能体协同实训教室 · 深度学习与交互</p>

  <div class="main-interaction-card">
    <div class="chat-conversation-flow">
      <div class="chat-bubble ai-bubble">
        <div class="ai-avatar">AI</div>
        <div class="bubble-content">
          <p>嗨，Tom同学！我是你的AI助教。今天你想了解关于**抗重放攻击**的实训，还是想直接进入**SM4编程演练**？</p>
        </div>
      </div>
      <div class="chat-suggestions">
        <span class="suggestion-chip">“解释抗重放机制”</span>
        <span class="suggestion-chip">“SM4编程第一步”</span>
        <span class="suggestion-chip">“查看我的进度”</span>
      </div>
    </div>

    <div class="chat-input-wrapper">
      <div class="input-container">
        <input type="text" placeholder="向 AI 助教提问，或输入实训内容关键字..." />
        <div class="input-actions">
          <button class="btn-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"></path><path d="M19 10v2a7 7 0 0 1-14 0v-2"></path></svg>
          </button>
          <button class="btn-send">
            <span>发送消息</span>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="22" y1="2" x2="11" y2="13"></line><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg>
          </button>
        </div>
      </div>
      <div class="lab-entry-area">
        <button class="btn-enter-lab">进入课堂</button>
      </div>
    </div>
  </div>
</header>

    <div class="content-container">
      <nav class="filter-nav">
        <div class="nav-left">
          <div 
            v-for="tab in tabs" 
            :key="tab.key" 
            class="nav-item" 
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </div>
        </div>
        <div class="nav-right-stats">
          <span>进行中 <b>3</b></span>
          <span class="divider">|</span>
          <span>已完成 <b>12</b></span>
        </div>
      </nav>

      <main class="glass-grid">
        <div v-for="item in filteredList" :key="item.id" class="glass-card">
          <div class="card-top">
            <span class="course-tag">{{ item.course }}</span>
            <span class="status-dot" :class="item.status">{{ getStatusText(item.status) }}</span>
          </div>
          
          <h3 class="card-title">{{ item.title }}</h3>
          
          <div class="progress-wrap">
            <div class="progress-labels">
              <span>实训进度</span>
              <span>{{ item.progress }}%</span>
            </div>
            <div class="progress-bar-bg">
              <div class="progress-fill" :style="{ width: item.progress + '%' }"></div>
            </div>
          </div>

          <footer class="card-actions">
            <span class="deadline">⏱️ {{ item.deadline }}</span>
            <button class="mini-btn" @click="handleAction(item)">
              {{ item.status === 'ongoing' ? '继续' : '查看' }}
            </button>
          </footer>
        </div>
      </main>
    </div>

    <div class="bottom-announcement">
      <span class="version-tag">AI学苑实训平台 —— 深度交互模式</span>
      <span class="update-text">全新交互模式 + 主页上线 + 实训同步更新</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const activeTab = ref('all')
const tabs = [
  { label: '全部实训', key: 'all' },
  { label: '进行中', key: 'ongoing' },
  { label: '已结束', key: 'ended' }
]

const trainingList = ref([
  { id: 1, title: '无人机抗重放攻击模拟演练', course: '通信安全实务', status: 'ongoing', progress: 65, deadline: '2026-05-10' },
  { id: 2, title: 'SM4 算法加密解密编程实战', course: '国密算法基础', status: 'not_started', progress: 0, deadline: '2026-05-15' },
  { id: 3, title: 'PQC 后量子签名算法原理', course: '前沿密码学', status: 'ended', progress: 100, deadline: '2026-04-20' },
  { id: 4, title: '数字签名与身份认证流程', course: '身份鉴别技术', status: 'ongoing', progress: 20, deadline: '2026-05-12' }
])

const filteredList = computed(() => {
  if (activeTab.value === 'all') return trainingList.value
  return trainingList.value.filter(i => i.status === activeTab.value)
})

const getStatusText = (status: string) => {
  const map: any = { ongoing: '进行中', not_started: '待开始', ended: '已结束' }
  return map[status]
}

const handleAction = (item: any) => {
  console.log('Action for:', item.title)
}
</script>

<style scoped>
/* 核心容器：沉浸式背景 */
.open-maic-wrapper {
  min-height: 100vh;
  background-color: #f8fafc;
  background-image: radial-gradient(at 0% 0%, rgba(99, 102, 241, 0.05) 0px, transparent 50%),
                    radial-gradient(at 100% 0%, rgba(168, 85, 247, 0.05) 0px, transparent 50%);
  padding: 40px 20px 100px;
  position: relative;
  overflow: hidden;
}
.logo-img {
  width: 45;
  height: 45px;
  border-radius: 10px;
}
.bg-glow {
  position: absolute;
  top: -100px;
  left: 50%;
  transform: translateX(-50%);
  width: 600px;
  height: 300px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.08) 0%, transparent 70%);
  pointer-events: none;
}

/* Hero 区域 */
.hero-section {
  text-align: center;
  margin-bottom: 60px;
}
.brand {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.logo-icon {
  background: linear-gradient(135deg, #6366f1, #a855f7);
  color: white;
  padding: 8px 10px;
  border-radius: 12px;
  font-weight: 800;
  font-size: 20px;
}
.logo-text {
  font-size: 32px;
  font-weight: 800;
  color: #1e293b;
  margin: 0;
}
.logo-text span { color: #6366f1; }
.slogan { color: #64748b; font-size: 16px; }

/* 中央交互卡片 */
.main-interaction-card {
  max-width: 850px;
  height: 42vh; /* 稍微调高一点 */
  margin: 40px auto 0;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(25px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 32px;
  padding: 32px;
  box-shadow: 0 25px 50px -12px rgba(99, 102, 241, 0.15);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

/* 对话流区域 */
.chat-conversation-flow {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.chat-bubble {
  display: flex;
  gap: 16px;
  max-width: 90%;
  animation: fadeIn 0.5s ease-out;
}

.ai-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  border-radius: 10px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
  flex-shrink: 0;
}

.bubble-content {
  background: white;
  padding: 16px 20px;
  border-radius: 0 20px 20px 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.bubble-content p {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  color: #334155;
  text-align: left;
}

/* 引导词设计 */
.chat-suggestions {
  display: flex;
  gap: 10px;
  margin-left: 52px;
  flex-wrap: wrap;
}

.suggestion-chip {
  background: rgba(99, 102, 241, 0.05);
  border: 1px solid rgba(99, 102, 241, 0.15);
  color: #6366f1;
  padding: 6px 14px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.suggestion-chip:hover {
  background: #6366f1;
  color: white;
  transform: translateY(-2px);
}

/* 输入区域设计 */
.chat-input-wrapper {
  margin-top: 24px;
  display: flex;
  gap: 16px;
  align-items: center;
}

.input-container {
  flex: 1;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 18px;
  padding: 8px 8px 8px 20px;
  display: flex;
  align-items: center;
  transition: border-color 0.3s;
}

.input-container:focus-within {
  border-color: #6366f1;
}

.input-container input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: #1e293b;
}

.input-actions {
  display: flex;
  gap: 8px;
}

.btn-icon {
  background: transparent;
  border: none;
  color: #94a3b8;
  padding: 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-icon:hover {
  background: #f1f5f9;
  color: #6366f1;
}

.btn-icon svg { width: 20px; height: 20px; }

.btn-send {
  background: #6366f1;
  color: white;
  border: none;
  padding: 8px 18px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-send:hover {
  background: #4f46e5;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.btn-send svg { width: 16px; height: 16px; }

.btn-enter-lab {
  background: #1e293b;
  color: white;
  border: none;
  height: 52px;
  padding: 0 24px;
  border-radius: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-enter-lab:hover {
  background: #0f172a;
  transform: scale(1.05);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}


/* 内容列表区域 */
.content-container {
  max-width: 1100px;
  margin: 0 auto;
}
.filter-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 10px;
}
.nav-left { display: flex; gap: 24px; }
.nav-item {
  color: #64748b;
  font-weight: 600;
  cursor: pointer;
  padding-bottom: 4px;
  border-bottom: 2px solid transparent;
}
.nav-item.active {
  color: #6366f1;
  border-bottom-color: #6366f1;
}
.nav-right-stats {
  font-size: 13px;
  color: #94a3b8;
}
.nav-right-stats b { color: #1e293b; margin-left: 4px; }
.divider { margin: 0 12px; opacity: 0.3; }

/* 玻璃卡片网格 */
.glass-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  transition: transform 0.3s ease;
}
.glass-card:hover { transform: translateY(-5px); }

.card-top { display: flex; justify-content: space-between; margin-bottom: 12px; }
.course-tag { font-size: 12px; color: #6366f1; background: #eef2ff; padding: 2px 8px; border-radius: 6px; font-weight: 600; }
.status-dot { font-size: 12px; display: flex; align-items: center; gap: 4px; }
.status-dot.ongoing { color: #10b981; }

.card-title { font-size: 17px; font-weight: 700; color: #1e293b; margin: 0 0 20px; line-height: 1.4; }

.progress-wrap { margin-bottom: 20px; }
.progress-labels { display: flex; justify-content: space-between; font-size: 11px; color: #64748b; margin-bottom: 6px; }
.progress-bar-bg { height: 6px; background: #f1f5f9; border-radius: 10px; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #6366f1, #a855f7); border-radius: 10px; }

.card-actions { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #f1f5f9; pt: 16px; margin-top: 10px; padding-top: 16px; }
.deadline { font-size: 12px; color: #94a3b8; }
.mini-btn { border: 1px solid #e2e8f0; background: white; padding: 6px 16px; border-radius: 8px; font-size: 12px; font-weight: 600; cursor: pointer; }

/* 底部状态条 */
.bottom-announcement {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.9);
  padding: 10px 24px;
  border-radius: 100px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
  display: flex;
  gap: 16px;
  align-items: center;
  font-size: 13px;
  white-space: nowrap;
}
.version-tag { color: #6366f1; font-weight: 700; }
.update-text { color: #64748b; }
</style>