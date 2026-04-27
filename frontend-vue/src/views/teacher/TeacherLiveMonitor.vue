<template>
  <div class="monitor-wrapper">
    <header class="monitor-header">
      <div class="header-left">
        <h1 class="panel-main-title">
          <span class="pulse-dot"></span>
          实训全链路监控中心 (AI学苑平台)
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

              <div class="task-progress-area">
                <div class="task-info">
                  <span class="sub-task" :title="stu.subTask">当前: {{ stu.subTask }}</span>
                  <span class="progress-text">{{ stu.progress }}%</span>
                </div>
                <div class="progress-bg">
                  <div class="progress-fill" :style="{ width: stu.progress + '%' }" :class="stu.alertType"></div>
                </div>
              </div>

              <div class="log-snippet" :class="{ 'log-error': stu.alertType === 'fail_loop' }">
                <span class="log-icon">_></span>
                <span class="log-text" :title="stu.latestLog">{{ stu.latestLog }}</span>
              </div>

              <div class="card-bottom">
                <div class="metrics-group">
                  <div class="metric" title="停留时长">
                    <span class="icon">⏱️</span> {{ stu.timeSpent }}m
                  </div>
                  <div class="metric" title="容器 CPU 占用">
                    <span class="icon">💻</span> {{ stu.cpu }}
                  </div>
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

// 严格保持原有的 7 个环节
const stages = ref([
  { id: 's1', name: '环节一：选型分组' },
  { id: 's2', name: '环节二：需求分析' },
  { id: 's3', name: '环节三：任务下发' },
  { id: 's4', name: '环节四：方案上传' },
  { id: 's5', name: '环节五：代码编写' },
  { id: 's6', name: '环节六：测试与调试' },
  { id: 's7', name: '环节七：项目评估' }
]);

// 将丰富后的学员数据合理映射到这 7 个环节中
const students = ref([
  { 
    id: '1', name: '胡凯亮', currentStage: 's3', alertType: null, timeSpent: 12, progress: 65, 
    subTask: '解析抗重放(Anti-Replay)任务书', cpu: '14%', 
    latestLog: 'Info: 成功拉取靶机 K8s 配置文件' 
  },
  { 
    id: '2', name: '靳雨静', currentStage: 's6', alertType: 'fail_loop', timeSpent: 45, progress: 20, 
    subTask: '测试 Timestamp 与 Nonce 校验逻辑', cpu: '98%', 
    latestLog: 'Error: Nonce(随机数)验证失败, 陷入重放保护死循环' 
  }, 
  { 
    id: '3', name: '刘炎', currentStage: 's2', alertType: 'idle', timeSpent: 18, progress: 40, 
    subTask: '分析 auth-0 鉴权流程漏洞', cpu: '2%', 
    latestLog: 'Warn: 终端挂起, 长时间未检测到键盘输入' 
  },     
  { 
    id: '4', name: '宋清源', currentStage: 's5', alertType: null, timeSpent: 8, progress: 90, 
    subTask: '结合 SM4 算法加密数据负载', cpu: '22%', 
    latestLog: 'Success: 加密载荷构造完成, 准备发送' 
  },
  { 
    id: '5', name: '王梦', currentStage: 's1', alertType: null, timeSpent: 3, progress: 100, 
    subTask: '确定 UAV 抗重放防御模型', cpu: '5%', 
    latestLog: 'System: 组队完成，分配沙箱资源...' 
  }
]);

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
      const targetScrollLeft = laneElement.offsetLeft - (scrollContainer.clientWidth / 2) + (laneElement.clientWidth / 2);
      scrollContainer.scrollTo({ left: targetScrollLeft, behavior: 'smooth' });
    }
  }, 50);
};

const getStudentsInStage = (stageId: string) => students.value.filter(s => s.currentStage === stageId);

const getStatusText = (alertType: string | null) => {
  if (alertType === 'idle') return '挂机告警';
  if (alertType === 'fail_loop') return '异常死循环';
  return '推进中';
};

const intervene = (stu: any) => alert(`[系统日志]: 正在向服务器请求建立与 ${stu.name} 的远端容器屏幕接管通道...`);
const broadcast = () => alert(`请输入需要下发给所有容器终端的广播内容`);
</script>

<style scoped>
.monitor-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 80vh;
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

.pipeline-step { display: flex; align-items: center; flex: 1 0 auto; min-width: 200px; cursor: pointer; transition: all 0.3s ease; }
.step-connector { flex: 1; min-width: 32px; height: 2px; background: #E2E8F0; margin: 0 16px; transition: all 0.3s ease; }
.step-node { width: 28px; height: 28px; border-radius: 50%; background: #F1F5F9; color: #64748B; display: flex; justify-content: center; align-items: center; font-size: 13px; font-weight: 800; border: 2px solid #E2E8F0; transition: all 0.3s ease; }
.step-label { margin-left: 10px; font-size: 14px; font-weight: 600; color: #475569; transition: all 0.3s ease; white-space: nowrap; }

.pipeline-step.is-active .step-node { background: #4F46E5; border-color: #4F46E5; color: #FFFFFF; box-shadow: 0 0 15px rgba(79, 70, 229, 0.4), 0 0 0 4px rgba(79, 70, 229, 0.15); }
.pipeline-step.is-active .step-label { color: #4F46E5; font-weight: 800; text-shadow: 0 0 10px rgba(79, 70, 229, 0.2); }
.pipeline-step:hover:not(.is-active) .step-node { border-color: #818CF8; color: #4F46E5; }

/* --- 泳道区域 Kanban --- */
.swimlanes-container {
  flex: 1; display: grid; grid-auto-columns: minmax(320px, 1fr); grid-auto-flow: column; gap: 20px; padding: 24px; overflow-x: auto;
}
.swimlanes-container::-webkit-scrollbar { height: 8px; }
.swimlanes-container::-webkit-scrollbar-track { background: transparent; margin: 0 24px; }
.swimlanes-container::-webkit-scrollbar-thumb { background: rgba(79, 70, 229, 0.3); border-radius: 10px; }
.swimlanes-container::-webkit-scrollbar-thumb:hover { background: #4F46E5; }

.lane { background: #FFFFFF; border-radius: 14px; border: 1px solid #E2E8F0; display: flex; flex-direction: column; height: 100%; transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); box-shadow: 0 4px 15px rgba(0,0,0,0.03); }
.lane.is-focused { border-color: #4F46E5; box-shadow: 0 15px 35px rgba(79, 70, 229, 0.15), 0 0 0 2px rgba(79, 70, 229, 0.3); transform: translateY(-2px); }

.lane-header { padding: 16px 20px; border-bottom: 1px solid #F1F5F9; display: flex; justify-content: space-between; align-items: center; background: #F8FAFC; border-radius: 14px 14px 0 0; cursor: pointer; transition: background 0.2s; }
.lane-header:hover { background: #EEF2FF; }
.lane.is-focused .lane-header { background: #EEF2FF; border-bottom-color: #C7D2FE; }
.lane.is-focused .stage-name { color: #4F46E5; }
.stage-name { font-size: 15px; font-weight: 700; color: #1E293B; margin: 0; transition: color 0.2s;}
.student-count { background: #EEF2FF; color: #4F46E5; padding: 2px 10px; border-radius: 12px; font-size: 13px; font-weight: 700; }

.lane-body { padding: 16px; flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 16px; }
.lane-body::-webkit-scrollbar { width: 4px; }
.lane-body::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 10px; }

/* --- 学生卡片增强版 --- */
.stu-card { background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 12px; padding: 16px; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); box-shadow: 0 2px 6px rgba(0,0,0,0.02); display: flex; flex-direction: column; gap: 12px;}
.stu-card.is-normal { border-left: 4px solid #10B981; }
.stu-card.is-normal:hover { box-shadow: 0 10px 25px rgba(16, 185, 129, 0.08); transform: translateY(-2px); border-color: #A7F3D0; }
.stu-card.is-alert { background: #FEF2F2; border-left: 4px solid #EF4444; border-color: #FECACA; animation: alert-border-pulse 2s infinite; }
.stu-card.is-alert:hover { transform: translateY(-2px); }

/* 卡片头部 */
.card-top { display: flex; justify-content: space-between; align-items: center; }
.stu-info { display: flex; align-items: center; gap: 10px; }
.avatar { width: 32px; height: 32px; background: #EEF2FF; color: #4F46E5; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; }
.stu-name { font-weight: 700; font-size: 15px; color: #0F172A; }

.status-badge { font-size: 11px; font-weight: 700; padding: 4px 8px; border-radius: 6px; }
.status-badge.null { color: #059669; background: #D1FAE5; }
.status-badge.idle { color: #D97706; background: #FEF3C7; }
.status-badge.fail_loop { color: #DC2626; background: #FEE2E2; }

/* 任务与进度 */
.task-progress-area { display: flex; flex-direction: column; gap: 6px; }
.task-info { display: flex; justify-content: space-between; align-items: center; font-size: 12px; }
.sub-task { color: #475569; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 180px; }
.progress-text { color: #94A3B8; font-weight: 700; font-family: monospace; }
.progress-bg { height: 6px; background: #F1F5F9; border-radius: 4px; overflow: hidden; }
.progress-fill { height: 100%; border-radius: 4px; transition: width 0.3s ease; }
.progress-fill.null { background: #10B981; }
.progress-fill.idle { background: #F59E0B; }
.progress-fill.fail_loop { background: #EF4444; }

/* 终端/日志区域 */
.log-snippet { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 6px; padding: 6px 10px; font-family: monospace; font-size: 11px; color: #64748B; display: flex; gap: 6px; align-items: flex-start; }
.log-snippet.log-error { background: #FFF1F2; border-color: #FECDD3; color: #BE123C; }
.log-icon { font-weight: 900; color: #94A3B8; }
.log-error .log-icon { color: #FB7185; }
.log-text { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; flex: 1; }

/* 卡片底部指标与按钮 */
.card-bottom { display: flex; justify-content: space-between; align-items: center; border-top: 1px dashed #E2E8F0; padding-top: 12px; margin-top: 2px;}
.metrics-group { display: flex; gap: 12px; }
.metric { font-size: 12px; color: #64748B; font-weight: 600; display: flex; align-items: center; gap: 4px;}
.metric .icon { font-size: 13px; }

.action-btn { background: #F8FAFC; border: 1px solid #E2E8F0; padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; color: #4F46E5; cursor: pointer; transition: all 0.2s; }
.action-btn:hover { background: #EEF2FF; border-color: #C7D2FE; }
.is-alert .action-btn { background: #EF4444; color: #FFF; border: none; }
.is-alert .action-btn:hover { background: #DC2626; box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2); }

.empty-state { text-align: center; padding: 40px 0; color: #94A3B8; font-size: 14px; font-weight: 500; border: 2px dashed #E2E8F0; border-radius: 10px; }

@keyframes header-pulse { 0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.4); } 70% { box-shadow: 0 0 0 8px rgba(16, 185, 129, 0); } 100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); } }
@keyframes alert-border-pulse { 0% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.2); } 70% { box-shadow: 0 0 0 8px rgba(239, 68, 68, 0); } 100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); } }

.list-enter-active, .list-leave-active { transition: all 0.5s ease; }
.list-enter-from, .list-leave-to { opacity: 0; transform: translateX(30px); }
</style>