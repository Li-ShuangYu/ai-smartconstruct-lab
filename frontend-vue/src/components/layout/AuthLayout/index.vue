<template>
  <div class="auth-layout-wrapper">
    <div class="bg-glow"></div>

    <div class="auth-main-card">
      
      <div class="auth-left-side">
        <img src="@/assets/auth-illustration.png" alt="illustration" class="bg-illustration" />
        
        <div class="floating-brand-box">
          <img src="@/assets/AIZG-Logo.png" alt="logo" class="brand-logo" />
          <h1 class="brand-title">AI学苑<span>实训平台</span></h1>
          <p class="brand-subtitle">密码学多智能体协同实训教室</p>
        </div>
      </div>

      <div class="auth-right-side">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>

    </div>

    <footer class="auth-footer">
      &copy; 2026 AI学苑 密码工程学院. All rights reserved.
    </footer>
  </div>
</template>

<style scoped>
.auth-layout-wrapper {
  height: 94vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f3f4f6 0%, #eef2ff 100%);
  position: relative;
  padding: 20px;
  overflow: hidden;
}

.bg-glow {
  position: absolute;
  top: -100px;
  left: 50%;
  transform: translateX(-50%);
  width: 800px;
  height: 400px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.08) 0%, transparent 70%);
  pointer-events: none;
}

.auth-main-card {
  width: 100%;
  max-width: 80vw;
  height: 520px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.1);
  display: flex;
  overflow: hidden;
  z-index: 10;
  border: 1px solid rgba(255, 255, 255, 0.7);
}

/* --- 左侧视觉区 --- */
.auth-left-side {
  flex: 1.4;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background-color: #f8fafc;
}

/* 核心修改 2：图片与右边卡片交汇处的渐变透明处理 */
.auth-left-side::after {
  content: "";
  position: absolute;
  top: 0;
  right: 0;
  width: 150px; /* 渐变区域宽度 */
  height: 100%;
  background: linear-gradient(to right, rgba(255, 255, 255, 0) 0%, #ffffff 100%);
  z-index: 3;
}

.bg-illustration {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  z-index: 1;
}

/* 核心修改 1：悬浮品牌框的模糊与透明效果 */
.floating-brand-box {
  position: relative;
  z-index: 4; /* 确保在渐变遮罩之上 */
  background: rgba(255, 255, 255, 0.55); /* 半透明底色 */
  backdrop-filter: blur(15px); /* 毛玻璃核心 */
  -webkit-backdrop-filter: blur(15px);
  padding: 42px 56px;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.brand-logo {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  margin-bottom: 20px;
}

.brand-title {
  font-size: 26px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 10px 0;
}
.brand-title span { color: #6366F1; margin-left: 4px; }

.brand-subtitle {
  font-size: 14px;
  color: #475569;
  margin: 0;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* --- 右侧路由区 --- */
.auth-right-side {
  flex: 1;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  z-index: 5;
}

.auth-footer {
  margin-top: 32px;
  font-size: 12px;
  color: #94a3b8;
  z-index: 10;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>