<template>
  <div class="page-container">
    <!-- 顶部 Toast 提示 -->
    <transition name="toast">
      <div v-if="showToast" class="toast-msg toast-error">
        <svg class="icon-md" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
        <span class="font-bold">等待教师进入方案设计阶段</span>
      </div>
    </transition>
    
    <header v-if="showContent" class="page-header">
      <div class="header-left">
        <div class="status-indicator" :style="{ backgroundColor: getHexColor('bg') }"></div>
        <h1 class="page-title">任务详情</h1>
      </div>
      <div class="header-right flex-row gap-3">
        <button class="btn-outline">
          <svg class="icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4"></path></svg>
          导出任务书
        </button>
        <div class="relative flex-col items-center">
          <button 
            @click="goToSchemeUpload" 
            :disabled="!canUpload"
            @mouseenter="!canUpload && (showTooltip = true)"
            @mouseleave="showTooltip = false"
            class="btn-primary"
            :style="{ backgroundColor: getHexColor('bg') }"
            :class="{ 'opacity-50 cursor-not-allowed': !canUpload }"
          >
            进入方案上传页面
          </button>
          
          <!-- 悬浮提示 -->
          <div v-if="showTooltip && !canUpload" class="tooltip">等待教师进入方案设计阶段</div>
          
          <!-- 🔥 开发调试：强制解锁按钮 🔥 -->
          <button 
            v-if="!canUpload" 
            @click="canUpload = true" 
            class="debug-unlock-btn"
          >
            [开发调试：强制解锁]
          </button>
        </div>
      </div>
    </header>

    <main v-if="showContent" class="main-content">
      <div class="layout-grid-2-1">
        
        <div class="flex-col gap-4">
          <!-- 任务信息卡片 -->
          <div class="glass-card p-6" :style="{ borderTop: '4px solid ' + getHexColor('border') }">
            <div class="flex-between mb-6">
              <div class="flex-row gap-3">
                <div class="badge-square" :style="{ backgroundColor: getHexColor('bg') }">
                  组 {{ groupInfo?.groupId || 1 }}
                </div>
                <h2 class="text-xl font-bold">{{ groupInfo?.groupName || '低功耗优化方向' }}</h2>
              </div>
              <div class="flex-row gap-3">
                <span class="tag-outline">组 {{ groupInfo?.groupId || 1 }} 任务已确认</span>
                <span class="text-xs text-slate-400">ID: {{ taskId }}</span>
              </div>
            </div>

            <div class="flex-col gap-4">
              <div class="task-box">
                <h3 class="task-title">
                  <svg class="icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                  【主线任务：通信加密设计】
                </h3>
                <p class="task-desc">{{ currentTask.mainTask }}</p>
              </div>
              <div class="task-box">
                <h3 class="task-title">
                  <svg class="icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
                  【支线任务：{{ currentTask.subTaskTitle }}】
                </h3>
                <p class="task-desc">{{ currentTask.subTask }}</p>
              </div>
            </div>
          </div>

          <!-- 团队分工卡片 -->
          <div class="glass-card p-6">
            <div class="flex-row gap-3 mb-4">
              <div class="icon-box"><svg class="icon-md text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"></path></svg></div>
              <h3 class="text-lg font-bold">团队协作与分工 (3人)</h3>
            </div>
            
            <div class="grid-cols-3 gap-4">
              <div v-for="role in ['theory', 'practice', 'organize']" :key="role" class="role-card">
                <div class="role-dot" :style="{ backgroundColor: getHexColor('bg') }"></div>
                <h4 class="role-title">{{ role === 'theory' ? '理论型' : role === 'practice' ? '实践型' : '组织型' }}</h4>
                <p class="role-desc">{{ currentTask.roles[role] }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧AI辅助分析 -->
        <div class="glass-card flex-col h-full">
          <div class="card-header flex-row gap-2">
            <svg class="icon-md text-slate-500" fill="currentColor" viewBox="0 0 20 20"><path d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v2H7a1 1 0 100 2h2v2a1 1 0 102 0v-2h2a1 1 0 100-2h-2V7z"></path></svg>
            <h3 class="text-base font-bold">AI 任务辅助分析</h3>
          </div>
          <div class="card-body gap-4 overflow-y-auto custom-scrollbar">
            <div class="ai-box">
              <div class="ai-tag">{{ currentTask.ai.insightTitle }}</div>
              <p class="ai-text">{{ currentTask.ai.insightDesc }}</p>
            </div>
            <div class="point-box">
              <p class="point-header">方案重点难点分析</p>
              <div class="flex-col gap-3">
                <div v-for="(point, idx) in currentTask.analysisPoints" :key="idx" class="point-item">
                  <div class="point-dot" :style="{ backgroundColor: getHexColor('bg') }"></div>
                  <div>
                    <div class="font-bold text-sm">{{ point.title }}</div>
                    <div class="text-xs text-slate-500 mt-1">{{ point.description }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const showContent = ref(false);
const groupInfo = ref(null);
const canUpload = ref(false);
const showTooltip = ref(false);
const showToast = ref(false);
let pollingTimer = null;

// 完整的任务数据源
const taskData = {
  1: {
    mainTask: '实现数据加密传输与双向身份认证，需重点适应无人机算力有限与时延敏感特性。',
    subTaskTitle: '低功耗专项',
    subTask: '机载设备功耗严格受限，需优化加密算法与密钥流程以降低整体算力开销。',
    ai: {
      insightTitle: '算法选型分析',
      insightDesc: 'PRESENT分组密码硬件实现仅需1570个门电路，极度契合资源受限的无人机环境。'
    },
    analysisPoints: [
      { title: '重点', description: 'PRESENT算法的硬件优化实现，S盒采用查找表减少计算开销。' },
      { title: '难点', description: '在保证安全性的前提下简化密钥更新流程，平衡安全性与功耗消耗。' }
    ],
    roles: {
      theory: '负责 PRESENT 算法的数学论证与密码学原理设计，输出理论框架。',
      practice: '负责将理论设计转化为 C/C++ 代码，并在目标硬件上进行功耗压测。',
      organize: '负责把控项目进度，撰写交互文档，并利用 AI 辅助系统校验流程。'
    }
  },
  2: {
    mainTask: '保障无线通信数据机密性与完整性，加密时延需严格控制以确保飞行指令实时传输。',
    subTaskTitle: '侧信道防护专项',
    subTask: '需阻断功耗、时序等物理信息泄露，抵御差分功耗分析等侧信道攻击风险。',
    ai: {
      insightTitle: '威胁模型分析',
      insightDesc: '面临功耗分析与时序攻击双重威胁。需在不显著增加算力负担的前提下，切断密钥物理关联。'
    },
    analysisPoints: [
      { title: '重点', description: 'AES算法结合轻量级掩码技术，每轮操作添加随机掩码隐藏中间状态。' },
      { title: '难点', description: '实现算法消除时序差异，避免分支预测泄露密钥信息。' }
    ],
    roles: {
      theory: '负责 AES 算法的数学论证与密码学原理设计，输出理论框架。',
      practice: '负责将理论设计转化为代码，并在目标硬件上进行功耗压测。',
      organize: '负责把控项目进度，撰写交互文档，并利用 AI 辅助校验流程。'
    }
  },
  3: {
    mainTask: '全程加密控制指令与飞行数据，强化身份认证，算法需高度适配机载算力限制。',
    subTaskTitle: '抗重放专项',
    subTask: '需设置严格的滑动窗口与同步校验机制，阻止截取旧数据包干扰，确保指令唯一有效。',
    ai: {
      insightTitle: '机制架构建议',
      insightDesc: '指令重放会导致无人机失控。核心在于建立收发双端的严密状态同步，自动丢弃过期数据。'
    },
    analysisPoints: [
      { title: '重点', description: '滑动窗口维护最近序列号，动态随机数确保指令唯一性。' },
      { title: '难点', description: '建立收发双端的严密状态同步，处理网络中断后的快速恢复。' }
    ],
    roles: {
      theory: '负责抗重放机制的数学论证与原理设计，输出理论框架。',
      practice: '负责将理论设计转化为代码，并在目标硬件上进行功能测试。',
      organize: '负责把控项目进度，撰写交互文档，并利用 AI 辅助校验。'
    }
  },
  4: {
    mainTask: '支持多种数据类型加密，底层通信算法需满足未来量子计算环境下的安全要求。',
    subTaskTitle: '后量子适配专项',
    subTask: '采用抗量子破解的轻量机制，需评估算力消耗并简化适配流程，降低硬件负担。',
    ai: {
      insightTitle: '算力演进分析',
      insightDesc: 'Shor算法对传统公钥体系构成威胁。采用基于格的密码学可有效抵御未来暴力破解。'
    },
    analysisPoints: [
      { title: '重点', description: 'Kyber密钥封装结合Dilithium数字签名，构建完整的后量子密码体系。' },
      { title: '难点', description: '在资源受限的平台上实现后量子算法，平衡安全性与性能开销。' }
    ],
    roles: {
      theory: '负责后量子算法的数学论证与密码学原理设计，输出理论框架。',
      practice: '负责将理论设计转化为代码，并在目标硬件上进行性能测试。',
      organize: '负责把控项目进度，撰写交互文档，并利用 AI 辅助校验流程。'
    }
  }
};

const fetchState = async () => {
  try {
    const res = await fetch('/api/state');
    const state = await res.json();
    canUpload.value = state.teacher_go_scheme === 1;
  } catch (error) {}
};

onMounted(() => {
  const storedInfo = localStorage.getItem('selectedGroupInfo');
  if (storedInfo) {
    groupInfo.value = JSON.parse(storedInfo);
  }
  setTimeout(() => { showContent.value = true; }, 100);

  fetchState();
  pollingTimer = setInterval(fetchState, 1000);
});

onUnmounted(() => {
  if (pollingTimer) clearInterval(pollingTimer);
});

const currentTask = computed(() => {
  const groupId = groupInfo.value?.groupId || 1;
  return taskData[groupId] || taskData[1];
});

const taskId = computed(() => {
  const groupId = groupInfo.value?.groupId || 1;
  const prefixes = { 1: 'LWP', 2: 'SCP', 3: 'RPL', 4: 'PQC' };
  return `TSK-2026-${prefixes[groupId] || 'LWP'}`;
});

// 解析颜色为 Hex
const getHexColor = (type) => {
  const defaultColors = { bg: '#2563eb', border: '#3b82f6', text: '#60a5fa' };
  if (!groupInfo.value?.colorTheme?.activeBorderClass) return defaultColors[type];
  
  const borderClass = groupInfo.value.colorTheme.activeBorderClass;
  const colorMap = {
    'border-blue-500': { bg: '#2563eb', border: '#3b82f6', text: '#60a5fa' },
    'border-red-500': { bg: '#dc2626', border: '#ef4444', text: '#f87171' },
    'border-amber-500': { bg: '#d97706', border: '#f59e0b', text: '#fbbf24' },
    'border-purple-500': { bg: '#9333ea', border: '#a855f7', text: '#c084fc' },
    'border-green-500': { bg: '#16a34a', border: '#22c55e', text: '#4ade80' },
    'border-yellow-500': { bg: '#ca8a04', border: '#eab308', text: '#facc15' },
    'border-cyan-500': { bg: '#0891b2', border: '#06b6d4', text: '#22d3ee' },
    'border-orange-500': { bg: '#ea580c', border: '#f97316', text: '#fb923c' }
  };
  return colorMap[borderClass]?.[type] || defaultColors[type];
};

const goToSchemeUpload = () => {
  if (!canUpload.value) {
    showToast.value = true;
    setTimeout(() => { showToast.value = false; }, 3000);
    return;
  }
  router.push('/training/student-training/student-scheme-upload');
};
</script>

<style scoped>
.page-container { min-height: 0px;padding: 0px; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; }
.page-header { flex-shrink: 0; height: 64px; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); z-index: 10; }
.header-left { display: flex; align-items: center; gap: 12px; }
.status-indicator { width: 6px; height: 24px; border-radius: 4px; }
.page-title { font-size: 18px; font-weight: 800; margin: 0; }
.main-content { flex: 1; overflow-y: auto; padding: 24px; }
.layout-grid-2-1 { display: grid; grid-template-columns: 2fr 1fr; gap: 24px; height: 100%; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; }
.flex-col { display: flex; flex-direction: column; }
.flex-row { display: flex; align-items: center; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.gap-2 { gap: 8px; } .gap-3 { gap: 12px; } .gap-4 { gap: 16px; }
.p-6 { padding: 24px; }
.relative { position: relative; }

.btn-outline { background: white; border: 1px solid #e2e8f0; padding: 8px 16px; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; display: flex; align-items: center; gap: 8px; color: #334155; }
.btn-primary { color: white; padding: 8px 24px; border: none; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; display: flex; align-items: center; gap: 8px; transition: opacity 0.2s;}
.opacity-50 { opacity: 0.5; } .cursor-not-allowed { cursor: not-allowed; }

.badge-square { width: 40px; height: 40px; border-radius: 10px; color: white; display: flex; align-items: center; justify-content: center; font-weight: bold; }
.text-xl { font-size: 20px; } .font-bold { font-weight: bold; }
.tag-outline { border: 1px solid #cbd5e1; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; background: white; color: #475569; }

.task-box { background: white; border: 1px solid #e2e8f0; padding: 16px; border-radius: 12px; }
.task-title { font-size: 14px; font-weight: bold; display: flex; align-items: center; gap: 8px; margin: 0 0 8px 0; color: #334155; }
.task-desc { font-size: 14px; color: #475569; margin: 0; padding-left: 24px; line-height: 1.5; }

.icon-box { width: 32px; height: 32px; background: white; border: 1px solid #e2e8f0; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.text-lg { font-size: 18px; }
.grid-cols-3 { display: grid; grid-template-columns: repeat(3, 1fr); }
.role-card { background: white; border: 1px solid #e2e8f0; padding: 16px; border-radius: 12px; position: relative; }
.role-dot { position: absolute; right: 12px; top: 12px; width: 8px; height: 8px; border-radius: 50%; }
.role-title { font-size: 14px; font-weight: bold; margin: 0 0 8px 0; }
.role-desc { font-size: 12px; color: #64748b; margin: 0; line-height: 1.5; }

.card-header { padding: 16px 20px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.5); }
.card-body { padding: 20px; display: flex; flex-direction: column; }
.ai-box { background: white; border: 1px solid #e2e8f0; padding: 16px; border-radius: 12px; }
.ai-tag { font-size: 12px; font-weight: bold; color: #64748b; text-transform: uppercase; margin-bottom: 8px; }
.ai-text { font-size: 14px; color: #334155; margin: 0; line-height: 1.5; }
.point-box { background: #f8fafc; border: 1px solid #e2e8f0; padding: 16px; border-radius: 12px; }
.point-header { font-size: 12px; font-weight: bold; color: #475569; margin: 0 0 12px 0; }
.point-item { display: flex; align-items: flex-start; gap: 8px; }
.point-dot { width: 6px; height: 6px; border-radius: 50%; margin-top: 6px; flex-shrink: 0; }
.text-sm { font-size: 14px; } .text-xs { font-size: 12px; } .text-slate-400 { color: #94a3b8; } .text-slate-500 { color: #64748b; }

.icon-sm { width: 16px; height: 16px; flex-shrink: 0; }
.icon-md { width: 20px; height: 20px; flex-shrink: 0; }

.debug-unlock-btn { position: absolute; top: 110%; font-size: 11px; color: #94a3b8; text-decoration: underline; background: none; border: none; cursor: pointer; white-space: nowrap; }
.debug-unlock-btn:hover { color: #3b82f6; }

.toast-msg { position: fixed; top: 80px; left: 50%; transform: translateX(-50%); color: white; padding: 12px 24px; border-radius: 12px; display: flex; align-items: center; gap: 8px; font-weight: bold; z-index: 50; }
.toast-error { background: #ef4444; box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3); }
.tooltip { position: absolute; top: 100%; left: 50%; transform: translateX(-50%); background: #1e293b; color: white; padding: 6px 12px; border-radius: 6px; font-size: 12px; white-space: nowrap; margin-top: 8px; z-index: 50; }
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }

.toast-enter-active, .toast-leave-active { transition: all 0.3s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translate(-50%, -20px); }
</style>