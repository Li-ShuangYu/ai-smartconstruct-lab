<template>
  <div class="task-select-container">
    
    <!-- 顶部 Toast 提示 -->
    <transition name="toast">
      <div v-if="showToast" class="toast-message">
        <svg class="icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
        <span>任务已确认！即将进入方案设计阶段。</span>
      </div>
    </transition>

    <!-- 顶部导航栏 -->
    <transition name="fade-in">
      <header v-if="showContent" class="page-header">
        <div class="header-left">
          <div class="status-indicator" :style="{ backgroundColor: selectedGroupId ? getColorFromStorage('bg') : '#cbd5e1' }"></div>
          <h1 class="page-title">需求分析 - 任务接收与选择</h1>
        </div>
        <div class="header-right">
          <button 
            @click="confirmTask" 
            :disabled="!selectedGroupId || isReceiving"
            class="action-btn"
            :class="(selectedGroupId && !isReceiving) ? 'btn-active' : 'btn-disabled'"
          >
            <svg v-if="selectedGroupId" class="icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7" /></svg>
            <svg v-else class="icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
            {{ isReceiving ? '等待接收任务' : (selectedGroupId ? '确认选择' : '请先选择任务') }}
          </button>
        </div>
      </header>
    </transition>

    <!-- 主内容区 -->
    <main v-if="showContent" class="main-content">
      
      <!-- 状态一：等待接收动画遮罩 (不再用玄学类名，纯CSS控制居中和大小) -->
      <transition name="fade">
        <div v-if="isReceiving" class="receiving-mask">
          <div class="receiving-card">
            <div class="spinner-box"></div>
            <h2 class="receiving-title">等待教师下发任务...</h2>
            <p class="receiving-desc">系统正在同步主线与支线任务分配信息</p>
            <button @click="isReceiving = false" class="debug-btn">[开发调试：强制跳过等待状态]</button>
          </div>
        </div>
      </transition>

      <!-- 状态二：任务分配与选择 -->
      <transition name="fade-up">
        <div v-if="!isReceiving" class="task-layout">
          
          <!-- 主线任务横幅 -->
          <div class="main-task-banner glass-card">
            <div class="banner-border"></div>
            <div class="banner-title">【主线任务汇总】</div>
            <div class="banner-content">
              完成无人机通信加密系统全流程设计，覆盖需求分析、方案选型、仿真验证全环节，输出可落地的加密安全方案，满足所有业务需求点。
            </div>
          </div>

          <div class="branch-task-area">
            <h2 class="area-title">
              <div class="title-dot"></div>
              【支线任务分配】请选择您的主攻方向
            </h2>
            
            <!-- 支线任务 4宫格选择区 -->
            <div class="task-grid">
              <div 
                v-for="(task, idx) in branchTasks" :key="idx"
                class="task-card glass-card"
                :class="{'is-selected': selectedGroupId === idx + 1}"
                :style="{ borderColor: (selectedGroupId === idx + 1 || hoveredGroup === idx + 1) ? getColorFromStorage('border') : '' }"
                @mouseenter="hoveredGroup = idx + 1"
                @mouseleave="hoveredGroup = null"
                @click="selectGroup(idx + 1)"
              >
                <div class="card-header">
                  <h2 class="card-title">支线方向 {{ idx + 1 }}：{{ task.title }}</h2>
                  <div v-if="selectedGroupId === idx + 1" class="check-badge" :style="{ backgroundColor: getColorFromStorage('bg') }">
                    <svg class="icon-sm text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path></svg>
                  </div>
                </div>
                
                <div class="card-body">
                  <p class="card-desc">{{ task.desc }}</p>
                  
                  <div class="card-footer">
                    <div class="ai-insight-box">
                      <span class="ai-title" :style="{ color: (selectedGroupId === idx + 1 || hoveredGroup === idx + 1) ? getColorFromStorage('text') : '#94a3b8' }">
                        <svg class="icon-sm" style="margin-right:6px;" fill="currentColor" viewBox="0 0 20 20"><path d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v2H7a1 1 0 100 2h2v2a1 1 0 102 0v-2h2a1 1 0 100-2h-2V7z"></path></svg>
                        AI助教分析
                      </span>
                      <p class="ai-desc">{{ task.aiInsight }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </main>

    <!-- 确认成功全屏弹窗 -->
    <transition name="fade">
      <div v-if="showSuccessModal" class="modal-overlay">
        <div class="modal-card">
          <div class="modal-icon-wrapper">
            <svg class="icon-lg text-green" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"></path></svg>
          </div>
          <h3 class="modal-title">任务已锁定</h3>
          <p class="modal-desc">数据已同步，即将开启密码学方案设计流程</p>
          <button @click="goToNextStage" class="modal-btn">
            进入方案详情页
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const showContent = ref(false);
const isReceiving = ref(true); 
const showSuccessModal = ref(false);
const showToast = ref(false);
const hoveredGroup = ref(null);
const selectedGroupId = ref(null);

const savedColorTheme = ref(null);
const currentGroupId = ref(null); 
let pollingTimer = null;

const branchTasks = [
  {
    title: '低功耗优化',
    desc: '聚焦加密算法轻量化选型与密钥管理流程优化，在保障安全的前提下，最大限度降低加密机制带来的额外功耗。',
    aiInsight: '推荐采用PRESENT轻量级加密算法，优化密钥更新流程，平衡安全性与功耗需求。'
  },
  {
    title: '侧信道防护',
    desc: '针对无人机嵌入式设备的物理泄露风险，设计轻量级掩码防护机制，抵御侧信道攻击，提升系统物理安全。',
    aiInsight: '建议采用AES算法结合轻量级掩码防护技术，增强侧信道攻击抵御能力。'
  },
  {
    title: '抗重放攻击',
    desc: '设计基于随机数 + 序列号的双向认证机制，杜绝非法重放攻击，保障无人机通信链路的接入安全与指令合法性。',
    aiInsight: '推荐使用AES加密算法配合动态随机数抗重放认证机制，确保指令唯一性。'
  },
  {
    title: '后量子算法适配',
    desc: '评估后量子加密算法在无人机场景的适配性，筛选轻量化格密码方案，保障系统在量子计算场景下的长期安全。',
    aiInsight: '建议采用抗量子加密算法，优化适配流程，平衡安全性与算力消耗。'
  }
];

const fetchState = async () => {
  try {
    const res = await fetch('/api/state');
    const state = await res.json();
    if (state.task_published === 1 && isReceiving.value) {
      isReceiving.value = false;
    }
  } catch (error) {}
};

onMounted(() => {
  const storedInfo = localStorage.getItem('selectedGroupInfo');
  if (storedInfo) {
    const groupInfo = JSON.parse(storedInfo);
    savedColorTheme.value = groupInfo.colorTheme;
    currentGroupId.value = groupInfo.groupId; 
  }
  
  setTimeout(() => { showContent.value = true; }, 100);

  fetchState();
  pollingTimer = setInterval(fetchState, 1000);
});

onUnmounted(() => {
  if (pollingTimer) clearInterval(pollingTimer);
});

const selectGroup = (groupId) => {
  if (isReceiving.value) return;
  if (currentGroupId.value && groupId !== currentGroupId.value) return;
  selectedGroupId.value = groupId;
};

// 提取颜色的核心方法
const getColorFromStorage = (type) => {
  const defaultColors = { bg: '#2563eb', border: '#3b82f6', text: '#2563eb' };
  if (!savedColorTheme.value) return defaultColors[type];
  
  const theme = savedColorTheme.value;
  // 解析 Tailwind 类名为实际的 Hex 颜色
  const colorMap = {
    'bg-blue-600': '#2563eb', 'border-blue-500': '#3b82f6', 'text-blue-400': '#60a5fa',
    'bg-red-600': '#dc2626', 'border-red-500': '#ef4444', 'text-red-400': '#f87171',
    'bg-amber-500': '#f59e0b', 'border-amber-500': '#f59e0b', 'text-amber-400': '#fbbf24',
    'bg-purple-600': '#9333ea', 'border-purple-500': '#a855f7', 'text-purple-400': '#c084fc'
  };

  if (type === 'bg') return colorMap[theme.badgeBgClass] || defaultColors.bg;
  if (type === 'border') return colorMap[theme.activeBorderClass] || defaultColors.border;
  if (type === 'text') return colorMap[theme.textClass] || defaultColors.text;
  return defaultColors.bg;
};

const goToNextStage = () => {
  showSuccessModal.value = false;
  router.push('/training/student-training/student-task-split');
};

const confirmTask = async () => {
  if (selectedGroupId.value && !isReceiving.value) {
    try {
      const stateKey = `task_selected_g${selectedGroupId.value}`;
      await fetch('/api/state/update', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ [stateKey]: 1 })
      });
    } catch (error) {}
    showSuccessModal.value = true;
  }
};
</script>

<style scoped>
/* 核心容器布局 */
.task-select-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: transparent;
  color: #1e293b;
  overflow: hidden;
}

/* 顶部导航栏 */
.page-header {
  flex-shrink: 0;
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid #e2e8f0;
  background-color: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}
.header-left { display: flex; align-items: center; gap: 12px; }
.status-indicator { width: 6px; height: 24px; border-radius: 4px; }
.page-title { font-size: 20px; font-weight: 800; margin: 0; }

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: bold;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-active { background-color: #10b981; color: white; }
.btn-active:hover { background-color: #059669; }
.btn-disabled { background-color: #f1f5f9; border-color: #e2e8f0; color: #94a3b8; cursor: not-allowed; }

/* SVG 尺寸强制约束（防爆炸） */
.icon-sm { width: 16px; height: 16px; flex-shrink: 0; }
.icon-lg { width: 48px; height: 48px; flex-shrink: 0; }
.text-white { color: #ffffff; }
.text-green { color: #10b981; }

/* 主内容区 */
.main-content {
  flex: 1;
  position: relative;
  overflow-y: auto;
  padding: 24px;
}

/* 玻璃拟态基础卡片 */
.glass-card {
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid #e2e8f0;
  border-radius: 16px;
}

/* 等待遮罩层 */
.receiving-mask {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(5px);
  z-index: 20;
}
.receiving-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 24px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  width: 400px;
}
.spinner-box {
  width: 60px; height: 60px;
  border: 4px solid #f1f5f9;
  border-top-color: #10b981;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 24px;
}
.receiving-title { font-size: 20px; font-weight: 800; margin: 0 0 8px 0; color: #1e293b; }
.receiving-desc { font-size: 14px; color: #64748b; margin: 0; }
.debug-btn { margin-top: 24px; background: none; border: none; font-size: 12px; color: #94a3b8; cursor: pointer; text-decoration: underline; }

/* 任务布局 */
.task-layout { max-width: 1200px; margin: 0 auto; display: flex; flex-direction: column; gap: 24px; height: 100%; }

/* 主线任务横幅 */
.main-task-banner {
  display: flex; align-items: center; padding: 12px; position: relative; overflow: hidden;
}
.banner-border { position: absolute; left: 0; top: 0; bottom: 0; width: 6px; background-color: #3b82f6; }
.banner-title { font-size: 16px; font-weight: 800; padding-left: 12px; width: 150px; flex-shrink: 0; }
.banner-content { flex: 1; background: #f8fafc; padding: 12px 20px; border-radius: 8px; border: 1px solid #e2e8f0; font-size: 14px; color: #475569; }

/* 支线任务区域 */
.branch-task-area { flex: 1; display: flex; flex-direction: column; }
.area-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 800; margin: 0 0 16px 0; }
.title-dot { width: 6px; height: 16px; background-color: #10b981; border-radius: 4px; }

.task-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; padding-bottom: 24px; }

/* 支线任务卡片 */
.task-card {
  padding: 24px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 220px;
}
.task-card:hover { border-color: #cbd5e1; transform: translateY(-2px); box-shadow: 0 10px 15px -3px rgba(0,0,0,0.05); }
.task-card.is-selected { border-width: 2px; transform: scale(1.01); box-shadow: 0 10px 20px -5px rgba(0,0,0,0.1); }

.card-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; padding-bottom: 12px; margin-bottom: 12px; }
.card-title { font-size: 16px; font-weight: 800; margin: 0; color: #1e293b; }
.check-badge { width: 24px; height: 24px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; }

.card-body { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.card-desc { font-size: 14px; color: #475569; line-height: 1.6; margin: 0 0 16px 0; }

.ai-insight-box { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 12px; }
.ai-title { font-size: 13px; font-weight: 800; display: flex; align-items: center; margin-bottom: 6px; }
.ai-desc { font-size: 12px; color: #64748b; margin: 0; line-height: 1.5; }

/* 全屏弹窗 */
.modal-overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(8px); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal-card { background: white; border-radius: 24px; padding: 40px; text-align: center; width: 400px; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.25); }
.modal-icon-wrapper { width: 80px; height: 80px; background: #ecfdf5; border: 4px solid #d1fae5; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 24px; }
.modal-title { font-size: 24px; font-weight: 800; margin: 0 0 8px 0; color: #1e293b; }
.modal-desc { font-size: 14px; color: #64748b; margin: 0 0 32px 0; }
.modal-btn { width: 100%; padding: 14px; background: #10b981; color: white; border: none; border-radius: 12px; font-size: 16px; font-weight: bold; cursor: pointer; transition: background 0.2s; }
.modal-btn:hover { background: #059669; }

.toast-message { position: fixed; top: 80px; left: 50%; transform: translateX(-50%); background: #10b981; color: white; padding: 12px 24px; border-radius: 12px; display: flex; align-items: center; gap: 8px; font-weight: bold; box-shadow: 0 10px 15px -3px rgba(16, 185, 129, 0.3); z-index: 50; }

@keyframes spin { 100% { transform: rotate(360deg); } }
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.fade-up-enter-active { transition: all 0.5s ease; }
.fade-up-enter-from { opacity: 0; transform: translateY(20px); }
.toast-enter-active, .toast-leave-active { transition: all 0.3s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translate(-50%, -20px); }
</style>