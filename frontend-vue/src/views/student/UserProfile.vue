<template>
  <div class="profile-container">
    <h1 class="panel-main-title">个人中心</h1>
    
    <div class="profile-layout">
      <aside class="profile-sidebar">
        <div class="avatar-card">
          <div class="avatar-wrapper">
            <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" alt="avatar" />
            <div class="edit-overlay">📸</div>
          </div>
          <h2 class="user-name">楼茂密</h2>
          <p class="user-role">24级密码工程01班 | 学生</p>
          <div class="user-id">学号: 20240915001</div>
        </div>
      </aside>

      <main class="settings-main">
        <div class="settings-card">
          <h3 class="section-title">安全设置</h3>
          <div class="form-group">
            <label>修改登录密码</label>
            <div class="input-with-action">
              <input type="password" value="********" readonly class="base-input" />
              <button class="text-btn">立即修改</button>
            </div>
          </div>

          <h3 class="section-title" style="margin-top: 40px;">系统支持</h3>
          <div class="form-group">
            <label>问题反馈与建议</label>
            <textarea class="base-textarea" placeholder="如果您在使用过程中遇到 Bug 或有改进建议，请告诉我们..."></textarea>
          </div>
          <button class="primary-btn">提交反馈</button>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.profile-container { padding: 32px; max-width: 1000px; margin: 0 auto; }
.profile-layout { display: grid; grid-template-columns: 280px 1fr; gap: 32px; margin-top: 24px; }

.avatar-card { background: white; border-radius: 20px; padding: 40px 24px; text-align: center; border: 1px solid #E2E8F0; }
.avatar-wrapper { width: 100px; height: 100px; border-radius: 50%; margin: 0 auto 20px; background: #EEF2FF; position: relative; overflow: hidden; cursor: pointer; }
.avatar-wrapper img { width: 100%; height: 100%; }
.edit-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; color: white; opacity: 0; transition: 0.3s; }
.avatar-wrapper:hover .edit-overlay { opacity: 1; }

.user-name { font-size: 20px; font-weight: 800; margin: 0 0 8px 0; }
.user-role { font-size: 14px; color: #64748B; margin: 0 0 16px 0; }
.user-id { font-size: 12px; color: #94A3B8; font-family: monospace; }

.settings-card { background: white; border-radius: 20px; padding: 32px; border: 1px solid #E2E8F0; }
.section-title { font-size: 17px; font-weight: 700; color: #0F172A; margin: 0 0 24px 0; padding-left: 12px; border-left: 4px solid #4F46E5; }

.form-group { display: flex; flex-direction: column; gap: 8px; margin-bottom: 20px; }
.form-group label { font-size: 14px; font-weight: 700; color: #475569; }

.base-input { padding: 12px 16px; border: 1px solid #E2E8F0; border-radius: 10px; font-size: 14px; background: #F8FAFC; }
.input-with-action { display: flex; gap: 12px; }
.input-with-action .base-input { flex: 1; }

.base-textarea { padding: 12px 16px; border: 1px solid #E2E8F0; border-radius: 10px; font-size: 14px; min-height: 120px; resize: vertical; }

.text-btn { background: none; border: none; color: #4F46E5; font-weight: 700; cursor: pointer; }
.primary-btn { background: #0F172A; color: white; border: none; padding: 12px 24px; border-radius: 10px; font-weight: 700; cursor: pointer; transition: 0.2s; }
.primary-btn:hover { background: #334155; }
</style>