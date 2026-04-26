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

    <nav class="pipeline-bar">
      <div 
        v-for="(stage, index) in stages" 
        :key="stage.id" 
        class="pipeline-step"
        :class="{ 'is-active': activeStageId === stage.id }"
        @click="focusAndScroll(stage.id)"
      >
        <div class="step-node">{{ index + 1 }}</div>
        <span class="step-label">{{ stage.name }}</span>
        <div v-if="index < stages.length - 1" class="step-connector"></div>
      </div>
    </nav>

    <main class="swimlanes-container" id="kanban-scroll-container">
      <div 
        class="lane" 
        v-for="stage in stages" 
        :key="stage.id"
        :id="`lane-${stage.id}`"
        :class="{ 'is-focused': activeStageId === stage.id }"
      >
        <div class="lane-header" @click="focusAndScroll(stage.id)" title="点击发亮并聚焦">
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
                <button class="action-btn" @click.stop="intervene(stu)">
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

const activeStageId = ref<string | null>(null);

const stages = ref([
  { id: 's1', name: '环节一：理论与协议拟制' },
  { id: 's2', name: '环节二：SM3/SM4 选型' },
  { id: 's3', name: '环节三：核心代码编写' },
  { id: 's4', name: '环节四：抗重放靶机实操' },
  { id: 's5', name: '环节五：漏洞挖掘测试' },
  { id: 's6', name: '环节六：AI 安全性评估' }
]);

const students = ref([
  { id: '1', name: '胡凯亮', currentStage: 's3', alertType: null, timeSpent: 12 },
  { id: '2', name: '靳雨静', currentStage: 's6', alertType: 'fail_loop', timeSpent: 45 }, 
  { id: '3', name: '刘炀', currentStage: 's2', alertType: 'idle', timeSpent: 18 },     
  { id: '4', name: '宋清源', currentStage: 's5', alertType: null, timeSpent: 8 },
  { id: '5', name: '王梦', currentStage: 's1', alertType: null, timeSpent: 3 }
]);

// 🌟 核心修复：用精确的数学计算替换坑人的 scrollIntoView
const focusAndScroll = (stageId: string) => {
  if (activeStageId.value === stageId) {
    activeStageId.value = null;
    return;
  }
  
  activeStageId.value = stageId;

  setTimeout(() => {
    const laneElement = document.getElementById(`lane-${stageId}`);
    const scrollContainer = document.getElementById('kanban-scroll-container');
    
    if (laneElement && scrollContainer) {
      // 计算目标元素居中时，滚动条应该处于的位置 (精确控制，绝不引起页面垂直跳跃)
      const targetScrollLeft = laneElement.offsetLeft - (scrollContainer.clientWidth / 2) + (laneElement.clientWidth / 2);
      
      scrollContainer.scrollTo({
        left: targetScrollLeft,
        behavior: 'smooth'
      });
    }
  }, 50);
};

const getStudentsInStage = (stageId: string) => students.value.filter(s => s.currentStage === stageId);

const getStatusText = (alertType: string | null) => {
  if (alertType === 'idle') return '⚠️ 长时间未操作';
  if (alertType === 'fail_loop') return '🚨 评估死循环';
  return '🟢 正常推进';
};

const intervene = (stu: any) => alert(`[系统日志]: 正在向服务器请求建立与 ${stu.name} 的远端容器屏幕接管通道...`);
const broadcast = () => alert(`请输入需要下发给所有容器终端的广播内容`);
</script>

<style scoped>
/* 🌟 修复：去除死板的 calc 和 overflow: hidden，让它融入全局框架 */
.monitor-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 80vh; /* 保证有一个兜底高度 */
  background: #F1F5F9;
  font-family: Inter, system-ui, sans-serif;
  font-size: 15px;
  color: #334155;
  box-sizing: border-box;
}

/* --- Header --- */
.monitor-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #E2E8F0;
  z-index: 10;
}
.header-left { display: flex; align-items: center; gap: 16px; }
.panel-main-title { font-size: 18px; font-weight: 800; color: #0F172A; margin: 0; display: flex; align-items: center; gap: 10px; }
.pulse-dot { width: 10px; height: 10px; background-color: #10B981; border-radius: 50%; box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7); animation: header-pulse 2s infinite; }
.online-status { font-size: 14px; color: #64748B; background: #F1F5F9; padding: 4px 12px; border-radius: 20px; font-weight: 600; }
.global-actions { display: flex; gap: 12px; }
.glass-btn { padding: 8px 16px; border: 1px solid #E2E8F0; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; display: flex; align-items: center; gap: 6px; transition: all 0.2s; }
.warn-btn { background: #FFFBEB; color: #D97706; border-color: #FDE68A; }
.warn-btn:hover { background: #FEF3C7; transform: translateY(-1px); }
.danger-btn { background: #FEF2F2; color: #DC2626; border-color: #FECACA; }
.danger-btn:hover { background: #FEE2E2; transform: translateY(-1px); }

/* --- 进度条 --- */
.pipeline-bar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding: 16px 32px;
  background: #FFFFFF;
  border-bottom: 1px solid #E2E8F0;
  box-shadow: 0 2px 10px rgba(0,0,0,0.02);
  z-index: 9;
  overflow-x: auto; 
  scrollbar-width: none; 
}
.pipeline-bar::-webkit-scrollbar { display: none; }

.pipeline-step {
  display: flex;
  align-items: center;
  flex: 1 0 auto; 
  min-width: 200px; 
  cursor: pointer;
  transition: all 0.3s ease;
}

.step-connector {
  flex: 1;
  min-width: 32px; 
  height: 2px;
  background: #E2E8F0;
  margin: 0 16px;
  transition: all 0.3s ease;
}

.step-node { width: 28px; height: 28px; border-radius: 50%; background: #F1F5F9; color: #64748B; display: flex; justify-content: center; align-items: center; font-size: 13px; font-weight: 800; border: 2px solid #E2E8F0; transition: all 0.3s ease; }
.step-label { margin-left: 10px; font-size: 14px; font-weight: 600; color: #475569; transition: all 0.3s ease; white-space: nowrap; }

.pipeline-step.is-active .step-node { 
  background: #4F46E5; 
  border-color: #4F46E5; 
  color: #FFFFFF; 
  box-shadow: 0 0 15px rgba(79, 70, 229, 0.4), 0 0 0 4px rgba(79, 70, 229, 0.15); 
}
.pipeline-step.is-active .step-label { color: #4F46E5; font-weight: 800; text-shadow: 0 0 10px rgba(79, 70, 229, 0.2); }
.pipeline-step:hover:not(.is-active) .step-node { border-color: #818CF8; color: #4F46E5; }

/* --- 泳道区域 Kanban --- */
.swimlanes-container {
  flex: 1;
  display: grid;
  grid-auto-columns: minmax(320px, 1fr); 
  grid-auto-flow: column; 
  gap: 20px;
  padding: 24px;
  overflow-x: auto;
  /* 移除 overflow-y 的锁定，让卡片过多时纵向自然延伸 */
}

.swimlanes-container::-webkit-scrollbar { height: 8px; }
.swimlanes-container::-webkit-scrollbar-track { background: transparent; margin: 0 24px; }
.swimlanes-container::-webkit-scrollbar-thumb { background: rgba(79, 70, 229, 0.3); border-radius: 10px; }
.swimlanes-container::-webkit-scrollbar-thumb:hover { background: #4F46E5; }

.lane {
  background: #FFFFFF;
  border-radius: 14px;
  border: 1px solid #E2E8F0;
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(0,0,0,0.03);
}

/* 🌟 修复：让升起动画更柔和，杜绝裁剪和闪烁 */
.lane.is-focused { 
  border-color: #4F46E5; 
  box-shadow: 0 15px 35px rgba(79, 70, 229, 0.15), 0 0 0 2px rgba(79, 70, 229, 0.3); 
  transform: translateY(-2px); 
}

.lane-header { padding: 16px 20px; border-bottom: 1px solid #F1F5F9; display: flex; justify-content: space-between; align-items: center; background: #F8FAFC; border-radius: 14px 14px 0 0; cursor: pointer; transition: background 0.2s; }
.lane-header:hover { background: #EEF2FF; }
.lane.is-focused .lane-header { background: #EEF2FF; border-bottom-color: #C7D2FE; }
.lane.is-focused .stage-name { color: #4F46E5; }

.stage-name { font-size: 15px; font-weight: 700; color: #1E293B; margin: 0; transition: color 0.2s;}
.student-count { background: #EEF2FF; color: #4F46E5; padding: 2px 10px; border-radius: 12px; font-size: 13px; font-weight: 700; }
.lane-body { padding: 16px; flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 16px; }
.lane-body::-webkit-scrollbar { width: 4px; }
.lane-body::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 10px; }

/* --- 学生卡片 --- */
.stu-card { background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 12px; padding: 16px; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); box-shadow: 0 2px 6px rgba(0,0,0,0.02); }
.stu-card.is-normal { border-left: 4px solid #10B981; }
.stu-card.is-normal:hover { box-shadow: 0 10px 25px rgba(16, 185, 129, 0.08); transform: translateY(-2px); border-color: #A7F3D0; }
.stu-card.is-alert { background: #FEF2F2; border-left: 4px solid #EF4444; border-color: #FECACA; animation: alert-border-pulse 2s infinite; }
.stu-card.is-alert:hover { transform: translateY(-2px); }

.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.stu-info { display: flex; align-items: center; gap: 10px; }
.avatar { width: 32px; height: 32px; background: #EEF2FF; color: #4F46E5; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; }
.stu-name { font-weight: 700; font-size: 16px; color: #0F172A; }

.status-badge { font-size: 12px; font-weight: 600; padding: 4px 8px; border-radius: 6px; }
.status-badge.null { color: #059669; background: #D1FAE5; }
.status-badge.idle { color: #D97706; background: #FEF3C7; }
.status-badge.fail_loop { color: #DC2626; background: #FEE2E2; }

.card-bottom { display: flex; justify-content: space-between; align-items: center; border-top: 1px dashed #E2E8F0; padding-top: 12px; }
.metric { display: flex; flex-direction: column; gap: 2px; }
.metric .label { font-size: 12px; color: #94A3B8; }
.metric .value { font-size: 13px; font-weight: 600; color: #475569; }

.action-btn { background: #F8FAFC; border: 1px solid #E2E8F0; padding: 6px 12px; border-radius: 6px; font-size: 13px; font-weight: 600; color: #4F46E5; cursor: pointer; transition: all 0.2s; }
.action-btn:hover { background: #EEF2FF; border-color: #C7D2FE; }
.is-alert .action-btn { background: #EF4444; color: #FFF; border: none; }
.is-alert .action-btn:hover { background: #DC2626; box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2); }

.empty-state { text-align: center; padding: 40px 0; color: #94A3B8; font-size: 14px; font-weight: 500; border: 2px dashed #E2E8F0; border-radius: 10px; }

@keyframes header-pulse { 0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4); } 70% { box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); } 100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); } }
@keyframes alert-border-pulse { 0% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.2); } 70% { box-shadow: 0 0 0 8px rgba(239, 68, 68, 0); } 100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); } }

.list-enter-active, .list-leave-active { transition: all 0.5s ease; }
.list-enter-from, .list-leave-to { opacity: 0; transform: translateX(30px); }
</style>