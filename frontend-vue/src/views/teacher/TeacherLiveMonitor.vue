<template>
  <div class="monitor-wrapper">
    <header class="monitor-header">
      <div class="header-left">
        <h1 class="panel-main-title">
          <span class="pulse-dot"></span>
          实训全链路监控中心 (密盾智学平台)
        </h1>
        <span class="online-status">当前在线: {{ students.length }} 人</span>
      </div>
      
      <div class="global-actions">
        <button class="glass-btn warn-btn" @click="broadcast">
          <span class="icon">📢</span> 全局广播
        </button>
        <button class="glass-btn danger-btn">
          <span class="icon">🛑</span> 强制熔断
        </button>
      </div>
    </header>

    <main class="swimlanes-container">
      <div class="lane" v-for="stage in stages" :key="stage.id">
        <div class="lane-header">
          <h3 class="stage-name">{{ stage.name }}</h3>
          <span class="student-count">{{ getStudentsInStage(stage.id).length }}</span>
        </div>
        
        <div class="lane-body">
          <transition-group name="list">
            <div 
              v-for="stu in getStudentsInStage(stage.id)" 
              :key="stu.id" 
              class="stu-card"
              :class="[stu.alertType ? 'is-alert' : 'is-normal']"
            >
              <div class="card-top">
                <div class="stu-info">
                  <div class="avatar">{{ stu.name.charAt(0) }}</div>
                  <span class="stu-name">{{ stu.name }}</span>
                </div>
                <div class="status-badge" :class="stu.alertType">
                  {{ getStatusText(stu.alertType) }}
                </div>
              </div>

              <div class="card-bottom">
                <div class="metric">
                  <span class="label">停留时长</span>
                  <span class="value">{{ stu.timeSpent }} 分钟</span>
                </div>
                <button class="action-btn" @click="intervene(stu)">
                  {{ stu.alertType ? '立即接管' : '查看屏幕' }}
                </button>
              </div>
            </div>
          </transition-group>

          <div v-if="getStudentsInStage(stage.id).length === 0" class="empty-state">
            暂无学员在此环节
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

// 按照 4 个 Group 同屏展示的要求，构建标准的密码学/抗重放实操流程
const stages = ref([
  { id: 's1', name: '环节一：理论与加密协议拟制' },
  { id: 's2', name: '环节二：SM3/SM4 算法选型配置' },
  { id: 's3', name: '环节三：无人机抗重放代码实操' },
  { id: 's4', name: '环节四：AI 安全性综合评估' }
]);

// 模拟实时流转数据
const students = ref([
  { id: '1', name: '胡凯亮', currentStage: 's3', alertType: null, timeSpent: 12 },
  { id: '2', name: '靳雨静', currentStage: 's4', alertType: 'fail_loop', timeSpent: 45 }, 
  { id: '3', name: '刘炀', currentStage: 's2', alertType: 'idle', timeSpent: 18 },     
  { id: '4', name: '宋清源', currentStage: 's3', alertType: null, timeSpent: 8 },
  { id: '5', name: '王梦', currentStage: 's1', alertType: null, timeSpent: 3 }
]);

const getStudentsInStage = (stageId: string) => students.value.filter(s => s.currentStage === stageId);

const getStatusText = (alertType: string | null) => {
  if (alertType === 'idle') return '⚠️ 长时间未操作';
  if (alertType === 'fail_loop') return '🚨 评估死循环';
  return '🟢 正常推进';
};

const intervene = (stu: any) => {
  alert(`[系统日志]: 正在向服务器请求建立与 ${stu.name} 的远端容器屏幕接管通道...`);
};

const broadcast = () => {
  alert(`请输入需要下发给所有容器终端的广播内容`);
};
</script>

<style scoped>
/* 全局布局与字体规范 (15px/18px) */
.monitor-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #F1F5F9;
  font-family: Inter, system-ui, sans-serif;
  font-size: 15px;
  color: #334155;
  overflow: hidden;
  box-sizing: border-box;
}

/* --- 顶部 Header (玻璃态美学) --- */
.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #E2E8F0;
  box-shadow: 0 4px 20px rgba(0,0,0,0.02);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.panel-main-title {
  font-size: 18px;
  font-weight: 800;
  color: #0F172A;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.pulse-dot {
  width: 10px;
  height: 10px;
  background-color: #10B981;
  border-radius: 50%;
  box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7);
  animation: header-pulse 2s infinite;
}

.online-status {
  font-size: 14px;
  color: #64748B;
  background: #F1F5F9;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 600;
}

.global-actions {
  display: flex;
  gap: 12px;
}

.glass-btn {
  padding: 8px 16px;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.warn-btn { background: #FFFBEB; color: #D97706; border-color: #FDE68A; }
.warn-btn:hover { background: #FEF3C7; transform: translateY(-1px); }

.danger-btn { background: #FEF2F2; color: #DC2626; border-color: #FECACA; }
.danger-btn:hover { background: #FEE2E2; transform: translateY(-1px); }

/* --- 泳道区域 (4 Group 均分并排) --- */
.swimlanes-container {
  flex: 1;
  display: grid;
  /* 强制 4 列等宽展示，满足大屏一屏阅览的需求 */
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 24px;
  overflow-x: auto;
  overflow-y: hidden;
}

.lane {
  background: #FFFFFF;
  border-radius: 14px;
  border: 1px solid #E2E8F0;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 15px rgba(0,0,0,0.03);
  max-height: 100%;
}

.lane-header {
  padding: 16px 20px;
  border-bottom: 1px solid #F1F5F9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #F8FAFC;
  border-radius: 14px 14px 0 0;
}

.stage-name {
  font-size: 15px;
  font-weight: 700;
  color: #1E293B;
  margin: 0;
}

.student-count {
  background: #EEF2FF;
  color: #4F46E5;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
}

.lane-body {
  padding: 16px;
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 定制细条滚动条 */
.lane-body::-webkit-scrollbar { width: 6px; }
.lane-body::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 10px; }

/* --- 学生卡片 (高保真交互) --- */
.stu-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
}

.stu-card.is-normal {
  border-left: 4px solid #10B981;
}
.stu-card.is-normal:hover {
  box-shadow: 0 10px 25px rgba(16, 185, 129, 0.08);
  transform: translateY(-2px);
  border-color: #A7F3D0;
}

/* 预警状态卡片特调 */
.stu-card.is-alert {
  background: #FEF2F2;
  border-left: 4px solid #EF4444;
  border-color: #FECACA;
  animation: alert-border-pulse 2s infinite;
}
.stu-card.is-alert:hover {
  transform: translateY(-2px);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.stu-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 32px;
  height: 32px;
  background: #EEF2FF;
  color: #4F46E5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.stu-name {
  font-weight: 700;
  font-size: 16px;
  color: #0F172A;
}

.status-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 6px;
}
.status-badge.null { color: #059669; background: #D1FAE5; }
.status-badge.idle { color: #D97706; background: #FEF3C7; }
.status-badge.fail_loop { color: #DC2626; background: #FEE2E2; }

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px dashed #E2E8F0;
  padding-top: 12px;
}

.metric { display: flex; flex-direction: column; gap: 2px; }
.metric .label { font-size: 12px; color: #94A3B8; }
.metric .value { font-size: 13px; font-weight: 600; color: #475569; }

.action-btn {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #4F46E5;
  cursor: pointer;
  transition: all 0.2s;
}
.action-btn:hover { background: #EEF2FF; border-color: #C7D2FE; }
.is-alert .action-btn { background: #EF4444; color: #FFF; border: none; }
.is-alert .action-btn:hover { background: #DC2626; box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2); }

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #94A3B8;
  font-size: 14px;
  font-weight: 500;
  border: 2px dashed #E2E8F0;
  border-radius: 10px;
}

/* --- 动画 --- */
@keyframes header-pulse {
  0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4); }
  70% { box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); }
  100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
}
@keyframes alert-border-pulse {
  0% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.2); }
  70% { box-shadow: 0 0 0 8px rgba(239, 68, 68, 0); }
  100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); }
}

/* 列表过渡动画 */
.list-enter-active, .list-leave-active { transition: all 0.5s ease; }
.list-enter-from, .list-leave-to { opacity: 0; transform: translateX(30px); }
</style>