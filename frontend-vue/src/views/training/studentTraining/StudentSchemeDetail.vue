<template>
  <div class="page-container">
    <header class="page-header">
      <div class="header-left">
        <div class="status-indicator" :style="{ backgroundColor: getHexColor('bg') }"></div>
        <h1 class="page-title">各组方案互评：{{ currentGroup.name }} - {{ currentGroup.title }}</h1>
      </div>
      <div class="header-right flex-row gap-3">
        <div class="group-switcher">
          <button @click="prevGroup" class="switch-btn">◁</button>
          <button @click="nextGroup" class="switch-btn">▷</button>
        </div>
        <button @click="backToWorkspace" class="btn-outline">返回</button>
        <button @click="completeEvaluation" class="btn-primary" :disabled="!allReviewsSubmitted">打分完成</button>
      </div>
    </header>

    <main v-if="showContent" class="main-content">
      <div class="layout-grid-12">
        
        <!-- 左侧基础信息 (占3列) -->
        <div class="grid-col-3 flex-col gap-4">
          <div class="glass-card p-5 flex-col">
            <div class="flex-between mb-4">
              <span class="tag-outline" :style="{ borderColor: getHexColor('border'), color: getHexColor('text') }">方案代号</span>
              <span class="font-mono font-bold text-slate-500">{{ currentGroup.codeId }}</span>
            </div>
            <h2 class="text-xl font-bold mb-2">{{ currentGroup.subtitle }}</h2>
            <p class="text-sm text-slate-500 mb-6">{{ currentGroup.desc }}</p>
            <div class="border-t pt-3 mb-6 flex-between">
              <span class="text-xs font-bold text-slate-500">目标硬件</span>
              <span class="font-bold italic" :style="{ color: getHexColor('text') }">{{ currentGroup.hardware }}</span>
            </div>
          </div>

          <div class="glass-card p-5 border-top-thick" :style="{ borderTopColor: getHexColor('border') }">
            <h3 class="text-sm font-bold mb-4" :style="{ color: getHexColor('text') }">核心算法</h3>
            <div class="flex-col gap-3">
              <div v-for="(algo, idx) in currentGroup.algorithms" :key="idx" class="algo-box border-l-thick" :style="{ borderLeftColor: getHexColor('border') }">
                <div class="text-xs font-bold text-slate-500 mb-1">{{ algo.label }}</div>
                <div class="text-sm font-bold text-slate-800 mb-1">{{ algo.name }}</div>
                <div class="text-xs text-slate-600 leading-tight">{{ algo.desc }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 中间架构 (占5列) -->
        <div class="grid-col-5 flex-col gap-4">
          <div class="glass-card p-6 flex-1 flex-col">
            <h3 class="text-lg font-bold mb-6 flex-row gap-2">
              <div class="dot-sm" :style="{ backgroundColor: getHexColor('bg') }"></div>
              系统架构与数据流转参考
            </h3>
            
            <div class="arch-list flex-1">
              <div v-for="(layer, idx) in currentGroup.archLayers" :key="idx" class="arch-item" :class="{'is-highlight': layer.highlight}" :style="{ borderColor: layer.highlight ? getHexColor('border') : '#e2e8f0' }">
                <div class="arch-name" :style="{ color: layer.highlight ? getHexColor('text') : '#64748b' }">{{ layer.name }}</div>
                <div class="arch-desc text-sm">{{ layer.desc }}</div>
              </div>
            </div>

            <div class="flow-box mt-4">
              <div class="text-xs font-bold text-slate-500 mb-2">Security Flow / 时序节点</div>
              <div class="flex-row flex-wrap gap-2">
                <template v-for="(step, idx) in currentGroup.flow" :key="idx">
                  <div class="flow-step" :style="{ color: getHexColor('text') }">{{ step }}</div>
                  <span v-if="idx < currentGroup.flow.length - 1" class="text-slate-400">→</span>
                </template>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧互评面板 (占4列) -->
        <div class="grid-col-4 flex-col gap-4">
          <div class="glass-card flex-1 flex-col border-top-thick" :style="{ borderTopColor: getHexColor('border') }">
            <div class="card-header flex-between">
              <h3 class="font-bold text-slate-800">小组互评</h3>
              <span class="status-badge" :class="currentGroup.review.isSubmitted ? 'badge-success' : 'badge-warn'">
                {{ currentGroup.review.isSubmitted ? '已评估' : '待打分' }}
              </span>
            </div>
            <div class="card-body gap-4 overflow-y-auto">
              
              <div class="score-list">
                <div v-for="dim in dimensions" :key="dim.key" class="score-item">
                  <span class="score-label">{{ dim.label }}</span>
                  <input type="range" min="0" max="100" v-model.number="currentGroup.review.scores[dim.key]" class="custom-range" :disabled="currentGroup.review.isSubmitted">
                  <span class="score-val" :style="{ color: getHexColor('text') }">{{ currentGroup.review.scores[dim.key] }}</span>
                </div>
              </div>

              <div class="flex-between border-b pb-4">
                <span class="font-bold text-slate-600">综合评分</span>
                <span class="text-4xl font-black" :style="{ color: getHexColor('text') }">{{ calculateTotalScore(currentGroup) }}</span>
              </div>

              <div class="flex-col gap-2 flex-1">
                <label class="text-sm font-bold text-slate-700">互评意见 (Markdown)</label>
                <textarea v-model="currentGroup.review.comment" :disabled="currentGroup.review.isSubmitted" class="custom-textarea flex-1"></textarea>
              </div>

              <button @click="submitReview" :disabled="currentGroup.review.isSubmitted" class="btn-large text-white" :style="{ backgroundColor: currentGroup.review.isSubmitted ? '#cbd5e1' : getHexColor('bg') }">
                {{ currentGroup.review.isSubmitted ? '已提交互评' : '提交互评' }}
              </button>

            </div>
          </div>
        </div>

      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const showContent = ref(false);
const savedColorTheme = ref(null);
const currentGroupId = ref(1);
const studentGroupId = ref(null);

const dimensions = [
  { key: 'security', label: '安全保密性' },
  { key: 'integrity', label: '数据完整性' },
  { key: 'usability', label: '系统可用性' },
  { key: 'innovation', label: '方案创新性' }
];

const groups = reactive([
  {
    id: 1, name: '第一组', title: '通信安全 + 低功耗专用版', subtitle: '轻量级无人机加密体制', codeId: 'SEC-LOWPOWER-01', themeColor: '#3b82f6', 
    desc: '专注民用小型无人机通信安全，采用 PRESENT 与 ECC 组合...', hardware: 'STM32L432',
    archLayers: [
      { name: '应用层', desc: '飞行控制指令输入输出', highlight: false },
      { name: '安全层', desc: '身份认证、数据加解密、密钥协商', highlight: true },
      { name: '硬件层', desc: 'STM32L432 低功耗单片机', highlight: true }
    ],
    flow: ['双向身份认证', '协商一次性会话密钥', '数据加解密与校验', '销毁密钥与休眠'],
    algorithms: [ { label: '数据加密', name: 'PRESENT 算法', desc: '64bit分组/80bit密钥，SPN结构31轮迭代。' } ],
    review: { scores: { security: 0, integrity: 0, usability: 0, innovation: 0 }, comment: '', isSubmitted: false }
  },
  {
    id: 2, name: '第二组', title: '通信安全 + 侧信道防护版', subtitle: '高安全抗物理攻击体制', codeId: 'SEC-SIDECHNL-02', themeColor: '#ef4444', 
    desc: '引入一阶掩码与恒定时间代码实现，抵御DPA攻击。', hardware: 'STM32L432',
    archLayers: [
      { name: '安全核心层', desc: '通信加密、侧信道防护', highlight: true },
      { name: '硬件支撑层', desc: 'STM32L432 低功耗单片机', highlight: true }
    ],
    flow: ['初始化防护单元', '双向认证与密钥协商', '掩码异或与恒定时间加密'],
    algorithms: [ { label: '侧信道防护', name: 'PRESENT + 掩码机制', desc: '敏感数据与随机掩码异或，无分支跳转。' } ],
    review: { scores: { security: 0, integrity: 0, usability: 0, innovation: 0 }, comment: '', isSubmitted: false }
  },
  {
    id: 3, name: '第三组', title: '通信安全 + 抗重放攻击', subtitle: '抗重放核心防护体制', codeId: 'SEC-NOREPLAY-03', themeColor: '#f59e0b', 
    desc: '基于滑动窗口与严格同步计数机制，丢弃重放的旧数据包。', hardware: 'STM32L432',
    archLayers: [
      { name: '安全层', desc: '数据加解密、身份校验、抗重放计数管理', highlight: true }
    ],
    flow: ['初始化同步计数器', '身份与同步校验', '附加计数器并加密传输'],
    algorithms: [ { label: '抗重放防护', name: '滑动窗口计数器', desc: '接收端校验大于本地记录才处理，并丢弃旧包。' } ],
    review: { scores: { security: 0, integrity: 0, usability: 0, innovation: 0 }, comment: '', isSubmitted: false }
  },
  {
    id: 4, name: '第四组', title: '通信安全 + 后量子算法', subtitle: '面向未来算力威胁前瞻体制', codeId: 'SEC-PQUANTUM-04', themeColor: '#8b5cf6', 
    desc: '采用抗量子破解的轻量级后量子密码机制。', hardware: 'STM32L432',
    archLayers: [
      { name: '安全层', desc: '后量子密钥协商、数据加解密', highlight: true }
    ],
    flow: ['加载后量子参数', 'Kyber 安全密钥协商', '共享密钥加密通信'],
    algorithms: [ { label: '密钥交换', name: 'CRYSTALS-Kyber', desc: '基于格基难题的轻量级后量子加密算法。' } ],
    review: { scores: { security: 0, integrity: 0, usability: 0, innovation: 0 }, comment: '', isSubmitted: false }
  }
]);

const currentGroup = computed(() => groups.find(g => g.id === currentGroupId.value));
const allReviewsSubmitted = computed(() => groups.every(g => g.id === studentGroupId.value || g.review.isSubmitted));

const getHexColor = (type) => {
  const defaultColors = { bg: '#2563eb', border: '#3b82f6', text: '#60a5fa' };
  if (!savedColorTheme.value) return currentGroup.value.themeColor; 
  return currentGroup.value.themeColor; // 这里使用写死在 group 里的 themeColor，因为切换组需要变色
};

onMounted(() => {
  const storedInfo = localStorage.getItem('selectedGroupInfo');
  if (storedInfo) {
    const groupInfo = JSON.parse(storedInfo);
    savedColorTheme.value = groupInfo.colorTheme;
    studentGroupId.value = parseInt(groupInfo.groupId);
    currentGroupId.value = studentGroupId.value;
  }
  setTimeout(() => { showContent.value = true; }, 100);
});

const prevGroup = () => { currentGroupId.value = currentGroupId.value > 1 ? currentGroupId.value - 1 : 4; };
const nextGroup = () => { currentGroupId.value = currentGroupId.value < 4 ? currentGroupId.value + 1 : 1; };

const calculateTotalScore = (group) => {
  if (!group || !group.review) return 0;
  const s = group.review.scores;
  return Math.round((s.security + s.integrity + s.usability + s.innovation) / 4);
};

const backToWorkspace = () => { router.push('/training/student-training/student-scheme-upload'); };
const completeEvaluation = () => {
  if (!allReviewsSubmitted.value) return;
  router.push('/training/student-training/student-my-score-result');
};

const submitReview = async () => {
  if (currentGroup.value.review.isSubmitted) return;
  currentGroup.value.review.isSubmitted = true;
};
</script>

<style scoped>
.page-container { height: 100%;min-height: 400px;padding: 0px; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; }
.page-header { flex-shrink: 0; height: 64px; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); z-index: 10; }
.header-left { display: flex; align-items: center; gap: 12px; }
.status-indicator { width: 6px; height: 24px; border-radius: 4px; }
.page-title { font-size: 18px; font-weight: 800; margin: 0; }

.header-right { display: flex; align-items: center; }
.group-switcher { display: flex; background: white; border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden; margin-right: 12px; }
.switch-btn { padding: 6px 12px; border: none; background: transparent; cursor: pointer; font-weight: bold; color: #64748b; }
.switch-btn:hover { background: #f1f5f9; color: #1e293b; }

.flex-row { display: flex; align-items: center; }
.flex-col { display: flex; flex-direction: column; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.flex-wrap { flex-wrap: wrap; }
.gap-2 { gap: 8px; } .gap-3 { gap: 12px; } .gap-4 { gap: 16px; }
.flex-1 { flex: 1; }

.btn-outline { background: white; border: 1px solid #e2e8f0; padding: 8px 16px; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; color: #334155; }
.btn-primary { color: white; padding: 8px 24px; border: none; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; }
.btn-primary:disabled { background: #cbd5e1; cursor: not-allowed; }
.btn-large { padding: 14px; border: none; border-radius: 12px; font-size: 16px; font-weight: bold; cursor: pointer; color: white; width: 100%; }

.main-content { flex: 1; overflow-y: auto; padding: 24px; }
.layout-grid-12 { display: grid; grid-template-columns: repeat(12, 1fr); gap: 24px; height: 100%; }
.grid-col-3 { grid-column: span 3; }
.grid-col-4 { grid-column: span 4; }
.grid-col-5 { grid-column: span 5; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; }
.border-top-thick { border-top-width: 4px; border-top-style: solid; }
.p-5 { padding: 20px; } .p-6 { padding: 24px; }
.border-t { border-top: 1px solid #e2e8f0; } .border-b { border-bottom: 1px solid #e2e8f0; }
.pb-4 { padding-bottom: 16px; } .pt-3 { padding-top: 12px; } .mt-4 { margin-top: 16px; }

.tag-outline { border: 1px solid; padding: 4px 8px; border-radius: 6px; font-size: 12px; font-weight: bold; background: white; }
.text-xl { font-size: 20px; } .text-lg { font-size: 18px; } .text-sm { font-size: 14px; } .text-xs { font-size: 12px; } .text-4xl { font-size: 36px; }
.font-bold { font-weight: bold; } .font-black { font-weight: 900; } .font-mono { font-family: monospace; } .italic { font-style: italic; }
.text-slate-800 { color: #1e293b; } .text-slate-700 { color: #334155; } .text-slate-600 { color: #475569; } .text-slate-500 { color: #64748b; } .text-slate-400 { color: #94a3b8; }

.algo-box { background: white; border: 1px solid #e2e8f0; padding: 12px; border-radius: 8px; }
.border-l-thick { border-left-width: 4px; border-left-style: solid; }
.dot-sm { width: 8px; height: 8px; border-radius: 50%; }

.arch-list { display: flex; flex-direction: column; gap: 12px; }
.arch-item { display: flex; align-items: center; gap: 16px; padding: 12px; border: 1px solid #e2e8f0; border-radius: 12px; background: white; }
.arch-item.is-highlight { border-width: 2px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); border-style: solid; }
.arch-name { width: 100px; font-weight: bold; font-size: 14px; }

.flow-box { background: #f8fafc; border: 1px solid #e2e8f0; padding: 16px; border-radius: 12px; }
.flow-step { background: white; border: 1px solid #e2e8f0; padding: 4px 8px; border-radius: 6px; font-size: 12px; font-weight: bold; font-family: monospace; }

.card-header { padding: 16px 20px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.5); }
.status-badge { font-size: 12px; padding: 4px 8px; border-radius: 4px; font-weight: bold; }
.badge-success { background: #dcfce7; color: #047857; }
.badge-warn { background: #fef3c7; color: #b45309; }
.card-body { padding: 20px; flex: 1; display: flex; flex-direction: column; }

.score-list { background: white; border: 1px solid #e2e8f0; padding: 16px; border-radius: 12px; display: flex; flex-direction: column; gap: 16px; }
.score-item { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.score-label { width: 80px; font-size: 14px; font-weight: bold; color: #475569; }
.score-val { width: 30px; text-align: right; font-weight: bold; font-family: monospace; font-size: 16px; }
.custom-range { flex: 1; height: 6px; border-radius: 3px; background: #e2e8f0; outline: none; -webkit-appearance: none; }
.custom-range::-webkit-slider-thumb { -webkit-appearance: none; width: 16px; height: 16px; border-radius: 50%; background: #3b82f6; cursor: pointer; }

.custom-textarea { width: 100%; border: 1px solid #cbd5e1; border-radius: 8px; padding: 12px; font-size: 14px; resize: none; background: white; color: #1e293b; }
.custom-textarea:disabled { background: #f8fafc; color: #94a3b8; }
</style>