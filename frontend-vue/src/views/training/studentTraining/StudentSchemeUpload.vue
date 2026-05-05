<template>
  <div class="page-container">
    <transition name="toast">
      <div v-if="uiState.showToast" class="toast-msg toast-success">
        <span class="font-bold">{{ currentGroup.name }} 方案提交成功！已同步至教师端。</span>
      </div>
    </transition>

    <header class="page-header">
      <div class="header-left">
        <div class="status-indicator" :style="{ backgroundColor: currentGroup.themeColor }"></div>
        <h1 class="page-title">方案设计 - 验证评估</h1>
        <div class="group-tag" :style="{ color: currentGroup.themeColor, borderColor: currentGroup.themeColor }">
          {{ currentGroup.name }} - {{ currentGroupTitle }}
        </div>
      </div>
      <div class="header-right flex-row gap-3">
        <button @click="goToTaskSplit" class="btn-outline">返回任务详情</button>
        <button 
          @click="goToSchemeDetail" 
          :disabled="!currentGroup.state.isSubmitted || currentGroup.state.evalStatus !== 'finished'"
          class="btn-primary"
          :class="{'opacity-50': !currentGroup.state.isSubmitted || currentGroup.state.evalStatus !== 'finished'}"
          :style="{ backgroundColor: (currentGroup.state.isSubmitted && currentGroup.state.evalStatus === 'finished') ? '#23b586' : '#94a3b8' }"
        >
          进入下一环节
        </button>
      </div>
    </header>

    <main class="main-content">
      <div class="layout-grid-1-1">
        
        <!-- 左侧上传框 -->
        <transition name="fade-up">
          <div v-if="showContent" class="glass-card flex-col border-top-thick" :style="{ borderTopColor: currentGroup.themeColor }">
            <div class="card-header flex-between">
              <h2 class="card-title">本组方案提报</h2>
              <span class="status-badge" :class="currentGroup.state.isSubmitted ? 'badge-success' : 'badge-default'">
                {{ currentGroup.state.isSubmitted ? '已提交，待评审' : '未提交' }}
              </span>
            </div>
            
            <div class="card-body gap-5 justify-center">
              <input ref="fileInput" type="file" style="display: none;" accept=".doc,.docx" @change="handleFileSelect">
              
              <div class="upload-area" @click="triggerFileUpload">
                <div v-if="!currentGroup.state.hasFile" class="flex-col items-center">
                  <div class="upload-title">点击上传方案文件</div>
                  <div class="upload-desc">支持 Word 格式 (.doc/.docx)</div>
                </div>
                <div v-else class="flex-col items-center">
                  <div class="upload-title text-green">{{ currentGroup.fileName }}</div>
                  <div class="upload-desc">大小: {{ currentGroup.fileSize }}</div>
                </div>
              </div>

              <button 
                @click="handleUpload" 
                :disabled="currentGroup.state.isUploading || currentGroup.state.isSubmitted || !currentGroup.state.hasFile"
                class="btn-large"
                :style="{ backgroundColor: currentGroup.state.isSubmitted ? '#23b586' : currentGroup.themeColor, opacity: (!currentGroup.state.hasFile || currentGroup.state.isUploading) ? 0.6 : 1 }"
              >
                {{ currentGroup.state.isSubmitted ? '方案已送达教师控制台' : '确认提交方案' }}
              </button>
            </div>
          </div>
        </transition>

        <!-- 右侧雷达图与评分 -->
        <transition name="fade-up">
          <div v-if="showContent" class="glass-card flex-col relative border-top-thick" style="border-top-color: #23b586; animation-delay: 0.1s;">
            <div class="card-header flex-between">
              <h2 class="card-title">AI 架构评审反馈</h2>
              <button 
                v-if="currentGroup.state.evalStatus === 'waiting'" 
                @click="startEvaluation" 
                :disabled="!currentGroup.state.isSubmitted"
                class="btn-small"
                :style="{ backgroundColor: currentGroup.state.isSubmitted ? '#23b586' : '#cbd5e1', color: 'white' }"
              >
                接收评估
              </button>
            </div>

            <!-- 等待状态 -->
            <div v-if="currentGroup.state.evalStatus === 'waiting'" class="overlay-mask">
              <div class="font-bold text-lg text-slate-500">暂无反馈数据</div>
              <div class="text-sm text-slate-400 mt-2">{{ currentGroup.state.isSubmitted ? '等待教师端下发指令...' : '请先在左侧提交方案' }}</div>
            </div>

            <!-- 加载状态 -->
            <div v-if="currentGroup.state.evalStatus === 'loading'" class="overlay-mask">
              <div class="font-bold text-lg text-slate-700 mb-4 animate-pulse">AI 深度推演中...</div>
              <div class="progress-bg">
                <div class="progress-bar" :style="{ width: currentGroup.state.evalProgress + '%', backgroundColor: currentGroup.themeColor }"></div>
              </div>
            </div>

            <!-- 完成状态 -->
            <transition name="fade">
              <div v-if="currentGroup.state.evalStatus === 'finished'" class="card-body">
                <div class="radar-container">
                  <div :id="`radar-chart-${currentGroupId}`" class="radar-dom"></div>
                  <div class="score-display border-l">
                    <div class="text-sm font-bold text-slate-500">综合安全评分</div>
                    <div class="score-number" :style="{ color: currentGroup.themeColor }">{{ currentGroup.aiScore }}</div>
                  </div>
                </div>
                <div class="ai-conclusion-box mt-4">
                  <p v-html="currentGroup.aiConclusion" class="text-sm text-slate-700 leading-relaxed"></p>
                  <div class="text-sm text-slate-500 mt-2 pt-2 border-t border-slate-200">
                    <span class="font-bold text-amber-500">建议：</span>{{ currentGroup.aiSuggestion }}
                  </div>
                </div>
              </div>
            </transition>

          </div>
        </transition>

      </div>
    </main>
  </div>
</template>

<style scoped>
.page-container { height: 100%;min-height: 400px;padding: 0px; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; }
.page-header { flex-shrink: 0; height: 64px; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); z-index: 10; }
.header-left { display: flex; align-items: center; gap: 12px; }
.status-indicator { width: 6px; height: 24px; border-radius: 4px; }
.page-title { font-size: 18px; font-weight: 800; margin: 0; }
.group-tag { border: 1px solid; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; background: white; }

.header-right { display: flex; align-items: center; }
.flex-row { display: flex; align-items: center; }
.flex-col { display: flex; flex-direction: column; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.gap-3 { gap: 12px; } .gap-5 { gap: 20px; }

.btn-outline { background: white; border: 1px solid #e2e8f0; padding: 8px 16px; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; }
.btn-primary { color: white; padding: 8px 24px; border: none; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; }
.btn-small { padding: 6px 12px; border: none; border-radius: 6px; font-size: 12px; font-weight: bold; cursor: pointer; }
.btn-large { padding: 16px; border: none; border-radius: 12px; font-size: 16px; font-weight: bold; cursor: pointer; color: white; width: 100%; }
.opacity-50 { opacity: 0.5; cursor: not-allowed; }

.main-content { flex: 1; overflow-y: auto; padding: 24px; }
.layout-grid-1-1 { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; height: 100%; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; }
.border-top-thick { border-top-width: 4px; }
.card-header { padding: 16px 20px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.5); }
.card-title { font-size: 16px; font-weight: 800; margin: 0; }
.status-badge { font-size: 12px; padding: 4px 8px; border-radius: 4px; font-weight: bold; }
.badge-success { background: #dcfce7; color: #047857; }
.badge-default { background: #f1f5f9; color: #64748b; border: 1px solid #e2e8f0; }

.card-body { padding: 24px; flex: 1; display: flex; flex-direction: column; }
.justify-center { justify-content: center; }
.items-center { align-items: center; }

.upload-area { border: 2px dashed #cbd5e1; border-radius: 16px; flex: 1; display: flex; align-items: center; justify-content: center; background: rgba(255,255,255,0.5); cursor: pointer; transition: background 0.2s; }
.upload-area:hover { background: #f8fafc; }
.upload-title { font-size: 18px; font-weight: bold; color: #334155; margin-bottom: 8px; }
.upload-desc { font-size: 14px; color: #64748b; }
.text-green { color: #23b586; }

.overlay-mask { position: absolute; inset: 0; top: 58px; background: rgba(255,255,255,0.9); display: flex; flex-direction: column; align-items: center; justify-content: center; z-index: 20; }
.font-bold { font-weight: bold; } .text-lg { font-size: 18px; } .text-sm { font-size: 14px; } .text-slate-500 { color: #64748b; } .text-slate-700 { color: #334155; } .text-slate-400 { color: #94a3b8; }
.animate-pulse { animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; }

.progress-bg { width: 70%; height: 8px; background: #e2e8f0; border-radius: 4px; overflow: hidden; }
.progress-bar { height: 100%; transition: width 2s linear; }

.radar-container { flex: 1; display: flex; align-items: center; min-height: 250px; }
.radar-dom { flex: 2; height: 100%; }
.score-display { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.border-l { border-left: 1px solid #e2e8f0; }
.score-number { font-size: 64px; font-weight: 900; line-height: 1; margin-top: 8px; }

.ai-conclusion-box { background: #f8fafc; border: 1px solid #e2e8f0; padding: 16px; border-radius: 12px; }
.mt-4 { margin-top: 16px; } .mt-2 { margin-top: 8px; } .pt-2 { padding-top: 8px; }
.border-t { border-top: 1px solid #e2e8f0; }
.text-amber-500 { color: #f59e0b; }

.toast-msg { position: fixed; top: 80px; left: 50%; transform: translateX(-50%); padding: 12px 24px; border-radius: 12px; color: white; font-weight: bold; z-index: 50; }
.toast-success { background: #23b586; }

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }
.fade-up-enter-active { transition: all 0.5s ease; }
.fade-up-enter-from { opacity: 0; transform: translateY(20px); }
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.toast-enter-active, .toast-leave-active { transition: all 0.3s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translate(-50%, -20px); }
</style>

<script setup>
import { reactive, ref, computed, nextTick, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';

const router = useRouter();

const showContent = ref(false); 
const currentGroupId = ref('g1');
const fileInput = ref(null); 
let chartInstance = null; 

const uiState = reactive({
  showToast: false
});

const groups = reactive([
  {
    id: 'g1',
    name: '第一组',
    themeColor: '#3b82f6', 
    tags: ['防窃听(高保密)', '防篡改(高完整)', '国密标准'],
    fileName: '',
    fileSize: '',
    aiScore: 92,
    aiLevel: '良好 (B+)',
    radarData: [85, 80, 95, 95, 80], 
    aiConclusion: "本组采用 <span style='color:#3b82f6;font-weight:bold;'>present轻量级加密算法</span>，安全性高，性能稳定。重点解决了加密算法在无人机环境中的应用问题。",
    aiSuggestion: "建议进一步优化密钥管理流程，可在不增加系统复杂度的前提下提升动态安全性。",
    state: { isUploading: false, isSubmitted: false, evalStatus: 'waiting', evalProgress: 0, hasFile: false }
  },
  {
    id: 'g2',
    name: '第二组',
    themeColor: '#ef4444', 
    tags: ['防窃听(高保密)', '防篡改(高完整)', '非对称加密'],
    fileName: '',
    fileSize: '',
    aiScore: 78,
    aiLevel: '良好 (B+)',
    radarData: [95, 90, 80, 80, 90],
    aiConclusion: "本组采用 <span style='color:#ef4444;font-weight:bold;'>侧信道防护方案</span>，在保障基础通信安全的同时，提供了更高的密钥安全性，工程可行性与安全性均表现优异。",
    aiSuggestion: "RSA 算法计算开销较大，建议在代码实现层进行优化以降低计算时延。",
    state: { isUploading: false, isSubmitted: false, evalStatus: 'waiting', evalProgress: 0, hasFile: false }
  },
  {
    id: 'g3',
    name: '第三组',
    themeColor: '#f59e0b', 
    tags: ['防窃听(高保密)', '防篡改(高完整)', '低功耗'],
    fileName: '',
    fileSize: '',
    aiScore: 85,
    aiLevel: '良好 (B+)',
    radarData: [90, 95, 85, 85, 85],
    aiConclusion: "本组采用 <span style='color:#f59e0b;font-weight:bold;'>抗重放攻击方案</span>，有效降低了系统功耗，同时保证了加密性能。身份认证逻辑严密，完整性校验机制十分完善。",
    aiSuggestion: "流密码的密钥管理是关键，建议在仿真验证时引入更安全的密钥更新机制。",
    state: { isUploading: false, isSubmitted: false, evalStatus: 'waiting', evalProgress: 0, hasFile: false }
  },
  {
    id: 'g4',
    name: '第四组',
    themeColor: '#8b5cf6', 
    tags: ['防窃听(高保密)', '防篡改(高完整)', '分布式防护'],
    fileName: '',
    fileSize: '',
    aiScore: 88,
    aiLevel: '良好 (B+)',
    radarData: [95, 95, 75, 70, 98],
    aiConclusion: "本组突破性采用 <span style='color:#8b5cf6;font-weight:bold;'>后量子密码防护方案</span>，利用抗量子算法全面抵御未来算力威胁，确保系统长效安全。创新性尤为突出，方案体系十分完整。",
aiSuggestion: "后量子算法带来了较大的内存与计算开销，建议探索引入硬件加速机制，以平衡极高安全性与节点性能。",
    state: { isUploading: false, isSubmitted: false, evalStatus: 'waiting', evalProgress: 0, hasFile: false }
  }
]);

// 修复 1：增加计算属性的安全回退，避免白屏
const currentGroup = computed(() => {
  return groups.find(g => g.id === currentGroupId.value) || groups[0];
});

const currentGroupTitle = computed(() => {
  const storedInfo = localStorage.getItem('selectedGroupInfo');
  if (storedInfo) {
    try {
      const groupInfo = JSON.parse(storedInfo);
      return groupInfo.groupName || '';
    } catch (e) {}
  }
  return '';
});

onMounted(() => {
  const storedInfo = localStorage.getItem('selectedGroupInfo');
  if (storedInfo) {
    try {
      const groupInfo = JSON.parse(storedInfo);
      if (groupInfo.groupId) {
        // 修复 2：安全地拼接 groupId，防止出现 "gg1" 的情况
        let gId = String(groupInfo.groupId);
        if (!gId.startsWith('g')) {
          gId = 'g' + gId;
        }
        currentGroupId.value = gId;
      }
    } catch(e) {
      console.error('JSON Parse error', e);
    }
  }
  
  setTimeout(() => {
    showContent.value = true;
  }, 100);
});

const triggerFileUpload = () => {
  if (currentGroup.value.state.isSubmitted) return;
  fileInput.value.click();
};

const handleFileSelect = (event) => {
  const file = event.target.files[0];
  if (file) {
    currentGroup.value.state.hasFile = true;
    currentGroup.value.fileName = file.name;
    currentGroup.value.fileSize = formatFileSize(file.size);
  }
};

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
};

const handleUpload = async () => {
  const group = currentGroup.value;
  group.state.isUploading = true;
  
  try {
    // 修复 3：使用 Vue 的 ref 取代 querySelector，防止变量重复声明报错
    const file = fileInput.value?.files[0];
    
    if (file) {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('groupId', currentGroupId.value);
      
      await fetch('/api/upload', {
        method: 'POST',
        body: formData
      });
    }
    
    const groupId = currentGroupId.value.replace('g', ''); 
    const stateKey = `scheme_uploaded_g${groupId}`;
    
    await fetch('/api/state/update', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ [stateKey]: 1 })
    });
  } catch (error) {
    console.error('推送状态失败:', error);
  }
  
  setTimeout(() => {
    group.state.isUploading = false;
    group.state.isSubmitted = true;
    
    uiState.showToast = true;
    setTimeout(() => { uiState.showToast = false; }, 3000);
  }, 800);
};

const startEvaluation = () => {
  const group = currentGroup.value;
  if (!group.state.isSubmitted || group.state.evalStatus !== 'waiting') return;
  
  group.state.evalStatus = 'loading';
  
  nextTick(() => {
    setTimeout(() => { group.state.evalProgress = 100; }, 50);
  });

  setTimeout(() => {
    group.state.evalStatus = 'finished';
    nextTick(() => {
      initRadarChart();
    });
  }, 2050);
};

const goToSchemeDetail = () => {
  if (!currentGroup.value.state.isSubmitted || currentGroup.value.state.evalStatus !== 'finished') {
    return;
  }
  router.push('/training/student-training/student-aiGenerate');
};

const goToTaskSplit = () => {
  router.push('/training/student-training/student-task-split');
};

const initRadarChart = () => {
  const domId = `radar-chart-${currentGroupId.value}`;
  const chartDom = document.getElementById(domId);
  if (!chartDom) return;
  
  chartInstance = echarts.init(chartDom);
  const group = currentGroup.value;
  
  const option = {
    radar: {
      indicator: [
        { name: '保密性', max: 100 },
        { name: '完整性', max: 100 },
        { name: '可用性', max: 100 },
        { name: '成本控制', max: 100 },
        { name: '创新性', max: 100 }
      ],
      radius: '65%',
      splitNumber: 4,
      axisName: { color: '#d1d5db', fontSize: 12 },
      splitLine: { lineStyle: { color: ['#2d353e'] } },
      splitArea: { show: false },
      axisLine: { lineStyle: { color: '#2d353e' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: group.radarData,
        name: group.name,
        itemStyle: { color: group.themeColor },
        areaStyle: { 
          color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
            { color: group.themeColor, offset: 0, opacity: 0.1 },
            { color: group.themeColor, offset: 1, opacity: 0.4 }
          ])
        },
        lineStyle: { width: 3, color: group.themeColor },
        symbolSize: 6
      }]
    }]
  };
  chartInstance.setOption(option);
};

const handleResize = () => {
  if (chartInstance) chartInstance.resize();
};

onMounted(() => {
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  if (chartInstance) chartInstance.dispose();
});
</script>

