<template>
  <div class="profile-container">
    <h1 class="panel-main-title">个人中心 (教研与账号设置)</h1>
    
    <div class="profile-layout">
      <aside class="profile-sidebar">
        <div class="avatar-card sidebar-block">
          <div class="avatar-wrapper">
            <div class="text-avatar">J</div>
            <div class="edit-overlay">📸</div>
          </div>
          <h2 class="user-name">{{ userInfo.name }}</h2>
          <p class="user-role">{{ userInfo.department }} | {{ userInfo.role }}</p>
          <div class="user-id">教职工号: {{ userInfo.id }}</div>
        </div>

        <div class="resource-card sidebar-block">
          <h3 class="mini-title">教学资源池调度状态</h3>
          <div class="resource-item">
            <span class="res-label">集群监控状态</span>
            <span class="res-value status-active">Healthy</span>
          </div>
          <div class="resource-item">
            <span class="res-label">活跃实训实例</span>
            <span class="res-value">{{ userInfo.resource.activeInstances }} 个</span>
          </div>
          <div class="resource-progress">
            <div class="res-label-row">
              <span>教学节点 CPU 分配率</span><span>{{ userInfo.resource.cpu }}</span>
            </div>
            <div class="progress-bg"><div class="progress-fill" style="width: 68%;"></div></div>
          </div>
          <div class="resource-progress">
            <div class="res-label-row">
              <span>教学节点内存分配率</span><span>{{ userInfo.resource.mem }}</span>
            </div>
            <div class="progress-bg"><div class="progress-fill warning" style="width: 82%;"></div></div>
          </div>
        </div>
      </aside>

      <main class="settings-main">
        <div class="stats-grid">
          <div class="stat-box">
            <span class="stat-title">累计授课/监控时长</span>
            <div class="stat-data"><b>{{ userInfo.stats.hours }}</b> 小时</div>
          </div>
          <div class="stat-box">
            <span class="stat-title">当前管理班级</span>
            <div class="stat-data"><b>{{ userInfo.stats.classes }}</b> 个</div>
          </div>
          <div class="stat-box">
            <span class="stat-title">发布实训模板</span>
            <div class="stat-data highlight"><b>{{ userInfo.stats.templates }}</b> 个</div>
          </div>
          <div class="stat-box">
            <span class="stat-title">AI 协同处理求助</span>
            <div class="stat-data"><b>{{ userInfo.stats.aiHandled }}</b> 次</div>
          </div>
        </div>

        <div class="settings-card">
          <h3 class="section-title">教研能力认证徽章</h3>
          <div class="badges-container">
            <div class="badge-item" v-for="(badge, index) in userInfo.badges" :key="index">
              <span class="badge-icon">{{ badge.icon }}</span>
              <span class="badge-name">{{ badge.name }}</span>
            </div>
            <div class="badge-item empty">
              <span class="badge-icon">🔒</span>
              <span class="badge-name">更多成就待解锁</span>
            </div>
          </div>

          <div class="settings-two-col">
            <div class="setting-col">
              <h3 class="section-title">教学偏好设置</h3>
              <div class="form-group">
                <label>AI 协同助教接管策略</label>
                <select class="base-select">
                  <option>深度接管 (自动解答80%基础/代码报错)</option>
                  <option>提示引导 (仅提供思路，不直接给代码)</option>
                  <option>高敏预警 (仅在学生卡点超过15分钟时介入)</option>
                </select>
              </div>
              <div class="form-group">
                <label>工作台默认视图</label>
                <select class="base-select">
                  <option>学生学情监控大屏 (推荐)</option>
                  <option>实训模板资源管理</option>
                  <option>K8s 底层容器调度看板</option>
                </select>
              </div>
            </div>

            <div class="setting-col">
              <h3 class="section-title">安全与授权设置</h3>
              <div class="form-group">
                <label>修改管理密码</label>
                <div class="input-with-action">
                  <input type="password" value="********" readonly class="base-input" />
                  <button class="text-btn">立即修改</button>
                </div>
              </div>
              <div class="form-group">
                <label>服务器 SSH / Kubeconfig 绑定</label>
                <div class="input-with-action">
                  <input type="text" value="已授权 (admin_rsa.pub)" readonly class="base-input success-input" />
                  <button class="text-btn">重新签发</button>
                </div>
              </div>
            </div>
          </div>

          <h3 class="section-title" style="margin-top: 32px;">系统运维与技术支持</h3>
          <div class="form-group">
            <label>平台工单 (镜像拉取失败、大模型接口异常、资源配额不足等)</label>
            <textarea class="base-textarea" placeholder="描述您在发布实训或教学监控中遇到的底层故障..."></textarea>
          </div>
          <div class="submit-action">
            <button class="primary-btn">提交运维排查</button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const userInfo = ref({
  name: 'Jovi 老师',
  department: '密码工程教研室',
  role: '高级讲师',
  id: 'T2026001',
  resource: { activeInstances: 142, cpu: '68%', mem: '82%' },
  stats: { hours: 512, classes: 4, templates: 18, aiHandled: 1256 },
  badges: [
    { name: '资深教研专家', icon: '🎓' },
    { name: 'K8s 调度大师', icon: '⚙️' },
    { name: '密码学金牌导师', icon: '🏆' }
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
  /* 教师端头像背景色微调，使用略深沉的渐变体现稳重感 */
  background: linear-gradient(135deg, #4338CA, #6366F1); position: relative; overflow: hidden; cursor: pointer; 
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
.progress-fill.warning { background: #F59E0B; } /* 内存较高，加入警告色反馈 */

/* 右侧主区域 */
.settings-main { display: flex; flex-direction: column; gap: 20px; }

/* 教研数据大盘网格 */
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