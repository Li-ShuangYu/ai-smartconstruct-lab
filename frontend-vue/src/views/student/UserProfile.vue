<template>
  <div class="profile-container">
    <h1 class="panel-main-title">个人中心 (学情与账号设置)</h1>
    
    <div class="profile-layout">
      <aside class="profile-sidebar">
        <div class="avatar-card sidebar-block">
          <div class="avatar-wrapper">
            <div class="text-avatar">T</div>
            <div class="edit-overlay">📸</div>
          </div>
          <h2 class="user-name">{{ userInfo.name }}</h2>
          <p class="user-role">{{ userInfo.className }} | {{ userInfo.role }}</p>
          <div class="user-id">学号: {{ userInfo.id }}</div>
        </div>

        <div class="resource-card sidebar-block">
          <h3 class="mini-title">专属实训舱位状态</h3>
          <div class="resource-item">
            <span class="res-label">容器状态</span>
            <span class="res-value status-active">Running</span>
          </div>
          <div class="resource-item">
            <span class="res-label">计算节点</span>
            <span class="res-value">K8s-Node-155</span>
          </div>
          <div class="resource-progress">
            <div class="res-label-row">
              <span>CPU 占用</span><span>{{ userInfo.resource.cpu }}</span>
            </div>
            <div class="progress-bg"><div class="progress-fill" style="width: 12%;"></div></div>
          </div>
          <div class="resource-progress">
            <div class="res-label-row">
              <span>内存占用</span><span>{{ userInfo.resource.mem }}</span>
            </div>
            <div class="progress-bg"><div class="progress-fill" style="width: 45%;"></div></div>
          </div>
        </div>
      </aside>

      <main class="settings-main">
        <div class="stats-grid">
          <div class="stat-box">
            <span class="stat-title">累计实训时长</span>
            <div class="stat-data"><b>{{ userInfo.stats.hours }}</b> 小时</div>
          </div>
          <div class="stat-box">
            <span class="stat-title">已完成实训</span>
            <div class="stat-data"><b>{{ userInfo.stats.completed }}</b> 个</div>
          </div>
          <div class="stat-box">
            <span class="stat-title">综合能力评分</span>
            <div class="stat-data highlight"><b>{{ userInfo.stats.score }}</b> 分</div>
          </div>
          <div class="stat-box">
            <span class="stat-title">AI 助教求助</span>
            <div class="stat-data"><b>{{ userInfo.stats.aiCalls }}</b> 次</div>
          </div>
        </div>

        <div class="settings-card">
          <h3 class="section-title">密码学能力认证徽章</h3>
          <div class="badges-container">
            <div class="badge-item" v-for="(badge, index) in userInfo.badges" :key="index">
              <span class="badge-icon">{{ badge.icon }}</span>
              <span class="badge-name">{{ badge.name }}</span>
            </div>
            <div class="badge-item empty">
              <span class="badge-icon">🔒</span>
              <span class="badge-name">更多待解锁</span>
            </div>
          </div>

          <div class="settings-two-col">
            <div class="setting-col">
              <h3 class="section-title">偏好设置</h3>
              <div class="form-group">
                <label>AI 助教交互模式</label>
                <select class="base-select">
                  <option>文本与图表主导 (沉浸式)</option>
                  <option>数字人语音主导 (陪伴式)</option>
                  <option>仅关键节点提示 (极简式)</option>
                </select>
              </div>
              <div class="form-group">
                <label>默认代码编辑器主题</label>
                <select class="base-select">
                  <option>VS Code Dark (推荐)</option>
                  <option>GitHub Light</option>
                  <option>Monokai</option>
                </select>
              </div>
            </div>

            <div class="setting-col">
              <h3 class="section-title">安全设置</h3>
              <div class="form-group">
                <label>修改登录密码</label>
                <div class="input-with-action">
                  <input type="password" value="********" readonly class="base-input" />
                  <button class="text-btn">立即修改</button>
                </div>
              </div>
              <div class="form-group">
                <label>终端 SSH 密钥对绑定</label>
                <div class="input-with-action">
                  <input type="text" value="已绑定 (id_rsa.pub)" readonly class="base-input success-input" />
                  <button class="text-btn">重新配置</button>
                </div>
              </div>
            </div>
          </div>

          <h3 class="section-title" style="margin-top: 32px;">系统支持与反馈</h3>
          <div class="form-group">
            <label>问题反馈 (容器异常、题目报错等)</label>
            <textarea class="base-textarea" placeholder="如果您在实训过程中遇到靶机无响应、AI 评价不准确或环境 Bug，请详细描述..."></textarea>
          </div>
          <div class="submit-action">
            <button class="primary-btn">提交工单</button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const userInfo = ref({
  name: 'Tom 同学',
  className: '24级密码工程01班',
  role: '学生',
  id: '20240915001',
  resource: { cpu: '12%', mem: '1.2GB' },
  stats: { hours: 128, completed: 15, score: 92, aiCalls: 342 },
  badges: [
    { name: '抗重放防御先锋', icon: '🛡️' },
    { name: 'SM4 算法达人', icon: '🔑' },
    { name: '容器排错能手', icon: '🐳' }
  ]
})
</script>

<style scoped>
.profile-container { 
  max-width: 1100px; 
  margin: 0 auto; 
  padding: 24px;
  box-sizing: border-box;
}
.panel-main-title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 24px 0; }

.profile-layout { 
  display: grid; 
  grid-template-columns: 280px 1fr; 
  gap: 24px; 
  align-items: start;
}

/* 左侧栏样式 */
.profile-sidebar { display: flex; flex-direction: column; gap: 20px; }
.sidebar-block { background: #FFFFFF; border-radius: 12px; padding: 24px; border: 1px solid #E2E8F0; }

.avatar-card { text-align: center; }
.avatar-wrapper { 
  width: 80px; height: 80px; border-radius: 50%; margin: 0 auto 16px; 
  background: linear-gradient(135deg, #4F46E5, #818CF8); position: relative; overflow: hidden; cursor: pointer; 
}
.text-avatar { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 32px; font-weight: 800; color: white; }
.edit-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; font-size: 20px; opacity: 0; transition: 0.2s; }
.avatar-wrapper:hover .edit-overlay { opacity: 1; }

.user-name { font-size: 18px; font-weight: 800; margin: 0 0 6px 0; color: #1E293B; }
.user-role { font-size: 13px; color: #64748B; margin: 0 0 12px 0; }
.user-id { font-size: 12px; color: #94A3B8; font-family: monospace; background: #F8FAFC; padding: 4px 8px; border-radius: 6px; display: inline-block;}

/* 资源卡片 */
.mini-title { font-size: 14px; font-weight: 700; color: #1E293B; margin: 0 0 16px 0; padding-bottom: 8px; border-bottom: 1px solid #F1F5F9; }
.resource-item { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 12px; }
.res-label { color: #64748B; }
.res-value { font-weight: 600; color: #334155; }
.status-active { color: #10B981; background: #D1FAE5; padding: 2px 8px; border-radius: 4px; font-size: 12px; }

.resource-progress { margin-bottom: 12px; }
.res-label-row { display: flex; justify-content: space-between; font-size: 12px; color: #64748B; margin-bottom: 4px; }
.progress-bg { height: 6px; background: #F1F5F9; border-radius: 4px; overflow: hidden; }
.progress-fill { height: 100%; background: #4F46E5; border-radius: 4px; }

/* 右侧主区域 */
.settings-main { display: flex; flex-direction: column; gap: 20px; }

/* 学情大盘网格 */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-box { background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 12px; padding: 16px; display: flex; flex-direction: column; justify-content: center;}
.stat-title { font-size: 13px; color: #64748B; margin-bottom: 8px; font-weight: 500;}
.stat-data { font-size: 14px; color: #475569; display: flex; align-items: baseline; gap: 4px;}
.stat-data b { font-size: 24px; color: #1E293B; line-height: 1; }
.stat-data.highlight b { color: #4F46E5; }

/* 设置主体卡片 */
.settings-card { background: #FFFFFF; border-radius: 12px; padding: 32px; border: 1px solid #E2E8F0; }
.section-title { font-size: 16px; font-weight: 700; color: #0F172A; margin: 0 0 20px 0; padding-left: 10px; border-left: 4px solid #4F46E5; }

/* 徽章区 */
.badges-container { display: flex; gap: 16px; margin-bottom: 32px; flex-wrap: wrap;}
.badge-item { background: #F8FAFC; border: 1px solid #E2E8F0; padding: 8px 16px; border-radius: 50px; display: flex; align-items: center; gap: 8px; }
.badge-icon { font-size: 16px; }
.badge-name { font-size: 13px; font-weight: 600; color: #475569; }
.badge-item.empty { background: transparent; border-style: dashed; opacity: 0.6; }

/* 偏好与安全双列 */
.settings-two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; }
.setting-col { display: flex; flex-direction: column; }

/* 表单元素 */
.form-group { display: flex; flex-direction: column; gap: 8px; margin-bottom: 20px; }
.form-group label { font-size: 13px; font-weight: 700; color: #475569; }

.base-input, .base-select, .base-textarea { 
  padding: 10px 14px; border: 1px solid #E2E8F0; border-radius: 8px; 
  font-size: 13px; color: #1E293B; background: #FFFFFF; outline: none; transition: border-color 0.2s;
}
.base-input:focus, .base-select:focus, .base-textarea:focus { border-color: #4F46E5; }
.base-input[readonly] { background: #F8FAFC; color: #94A3B8; }
.success-input[readonly] { color: #10B981; background: #ECFDF5; border-color: #A7F3D0; font-weight: 600;}

.input-with-action { display: flex; gap: 12px; align-items: center;}
.input-with-action .base-input { flex: 1; }

.base-textarea { min-height: 100px; resize: vertical; }

.text-btn { background: none; border: none; color: #4F46E5; font-weight: 700; cursor: pointer; font-size: 13px; padding: 0;}
.text-btn:hover { color: #4338CA; text-decoration: underline;}

.submit-action { display: flex; justify-content: flex-end; margin-top: 16px;}
.primary-btn { background: #4F46E5; color: white; border: none; padding: 10px 24px; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.primary-btn:hover { background: #4338CA; }
</style>