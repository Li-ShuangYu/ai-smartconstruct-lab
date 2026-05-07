<template>
  <div class="page-container">
    <div v-if="showToast" class="toast-box">
      <svg style="width: 24px; height: 24px; flex-shrink: 0;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
      <span class="font-bold text-lg">{{ toastMsg }}</span>
    </div>

    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-card">
        <div class="spinner border-green"></div>
        <div class="text-center">
          <div class="text-lg font-bold text-slate-800 mb-2">评分完成√</div>
          <div class="text-sm text-slate-500">正在进入总结页面...</div>
        </div>
      </div>
    </div>
    
    <header class="page-header">
      <div class="header-left">
        <div class="status-indicator" :style="{ backgroundColor: currentGroup.themeColor }"></div>
        <div class="flex-row">
          <span class="page-title mr-4">各组方案评分：</span>
          <div class="group-nav">
            <button @click="prevGroup" class="nav-btn"><svg style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M15 19l-7-7 7-7" /></svg></button>
            <span class="nav-title" :style="{ color: currentGroup.themeColor }">{{ currentGroup.name }} - {{ currentGroup.title }}</span>
            <button @click="nextGroup" class="nav-btn"><svg style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 5l7 7-7 7" /></svg></button>
          </div>
          <button @click="generateReview" :disabled="isGenerating" class="btn-icon" title="一键自动评估">
            <svg style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
          </button>
        </div>
      </div>
      <div class="header-right">
        <button @click="backToWorkspace" class="btn-outline mr-3">
          <svg style="width: 16px; height: 16px; flex-shrink: 0; margin-right: 6px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M15 19l-7-7 7-7" /></svg> 返回方案AI评估页
        </button>
        <button @click="completeEvaluation" :disabled="!allReviewsSubmitted" class="btn-primary" :class="allReviewsSubmitted ? '' : 'btn-disabled'">
          {{ allReviewsSubmitted ? '完成评估' : '等待各组方案评分完成' }}
          <svg style="width: 16px; height: 16px; flex-shrink: 0; margin-left: 6px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 5l7 7-7 7" /></svg>
        </button>
      </div>
    </header>

    <main class="main-content layout-grid-12" v-if="showContent">
      <div class="grid-col-3 flex-col gap-3" :class="{ 'opacity-0 transform scale-95': showAnimation }" style="transition: all 0.3s ease;">
        <div class="glass-card p-4 flex-col relative">
          <div class="bg-blur-circle" :style="{ backgroundColor: currentGroup.themeColor }"></div>
          <div class="flex-between mb-2 z-10">
            <span class="tag-sm border" :style="{ backgroundColor: currentGroup.themeColor + '1a', color: currentGroup.themeColor, borderColor: currentGroup.themeColor + '33' }">方案代号</span>
            <span class="font-bold font-mono text-slate-500">{{ currentGroup.codeId }}</span>
          </div>
          <h2 class="text-2xl font-black text-slate-800 z-10 mb-2">{{ currentGroup.subtitle }}</h2>
          <p class="text-sm text-slate-500 z-10 mb-4">{{ currentGroup.desc }}</p>
          <div class="border-t pt-2 flex-between z-10 mb-4 border-slate-200">
            <span class="text-xs font-bold text-slate-500">目标适配硬件</span>
            <span class="text-xl font-black italic" :style="{ color: currentGroup.themeColor }">{{ currentGroup.hardware }}</span>
          </div>
          <div class="grid-cols-2 gap-2 z-10 mt-auto">
            <button class="btn-light"><svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"></path></svg> API接口文档</button>
            <button class="btn-light"><svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path></svg> 下载基础工程</button>
          </div>
        </div>

        <div class="glass-card p-4 flex-1 flex-col border-t-4 min-h-0" :style="{ borderTopColor: currentGroup.themeColor }">
          <div class="text-xs font-black mb-3 flex-row gap-2" :style="{ color: currentGroup.themeColor }">
            <svg style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2-2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" /></svg> 开发需实现的核心算法
          </div>
          <div class="flex-1 flex-col gap-2 overflow-y-auto custom-scrollbar pr-2">
            <div v-for="(algo, index) in currentGroup.algorithms" :key="index" class="algo-box border-l-4" :style="{ borderLeftColor: index === 0 ? currentGroup.themeColor : '#cbd5e1' }">
              <div class="flex-between mb-1">
                <span class="text-xs font-bold text-slate-500">{{ algo.label }}</span>
                <span class="text-sm font-black" :style="{ color: index === 0 ? currentGroup.themeColor : '#1e293b' }">{{ algo.name }}</span>
              </div>
              <div class="text-xs text-slate-600 leading-tight">{{ algo.desc }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="grid-col-5 flex-col gap-3 min-h-0" :class="{ 'opacity-0 transform scale-95': showAnimation }" style="transition: all 0.3s ease;">
        <div class="glass-card p-5 flex-col h-full min-h-0">
          <div class="text-base font-black text-slate-800 mb-5 flex-row gap-2 shrink-0">
            <div class="dot-sm" :style="{ backgroundColor: currentGroup.themeColor, boxShadow: `0 0 8px ${currentGroup.themeColor}80` }"></div>
            系统架构与数据流转参考
          </div>

          <div class="flex-1 flex-col mb-6 relative min-h-0 overflow-y-auto custom-scrollbar pr-2">
            <div class="timeline-line"></div>
            <div v-for="(layer, index) in currentGroup.archLayers" :key="index" class="arch-item border" :class="layer.highlight ? 'bg-white shadow-md z-10 scale-102' : 'bg-slate-50/50 z-0'" :style="{ borderColor: layer.highlight ? currentGroup.themeColor : '#e2e8f0' }">
              <div class="arch-tag border" :style="{ borderColor: layer.highlight ? currentGroup.themeColor + '80' : '#cbd5e1', backgroundColor: layer.highlight ? '#f8fafc' : 'white' }">
                <span class="text-xs font-black" :style="{ color: layer.highlight ? currentGroup.themeColor : '#64748b' }">{{ layer.name }}</span>
              </div>
              <div class="flex-1 text-xs font-medium leading-relaxed z-10" :class="layer.highlight ? 'text-slate-700' : 'text-slate-500'">{{ layer.desc }}</div>
              <div v-if="layer.highlight" class="arch-highlight-bar" :style="{ backgroundColor: currentGroup.themeColor }"></div>
            </div>
          </div>

          <div class="flow-box shrink-0 border">
            <div class="bg-blur-circle-tr" :style="{ backgroundColor: currentGroup.themeColor }"></div>
            <div class="flow-header">
              <span class="flex-row gap-2 text-slate-500"><svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg> Security Flow / 时序节点</span>
              <span class="text-blue-500 hover-underline cursor-pointer flex-row gap-1">查看详细UML图 <svg style="width: 12px; height: 12px; flex-shrink: 0;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"></path></svg></span>
            </div>
            <div class="flow-body z-10 relative">
              <template v-for="(step, index) in currentGroup.flow" :key="index">
                <div class="flow-step border shadow-sm" :style="{ color: currentGroup.themeColor }">{{ step }}</div>
                <svg v-if="index < currentGroup.flow.length - 1" style="width: 16px; height: 16px; flex-shrink: 0;" class="text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path></svg>
              </template>
            </div>
          </div>
        </div>
      </div>

      <div class="grid-col-4 flex-col gap-3 min-h-0" :class="{ 'opacity-0 transform scale-95': showAnimation }" style="transition: all 0.3s ease;">
        <div class="glass-card flex-1 flex-col relative border-t-4" :style="{ borderTopColor: currentGroup.themeColor }">
          
          <div class="card-header-inner flex-between bg-white/50 border-b">
            <div class="text-base font-black flex-row gap-2" :style="{ color: currentGroup.themeColor }">
              <svg style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" /></svg> 教师综合打分与评价
            </div>
            <div>
              <span v-if="currentGroup.review.isSubmitted" class="tag-success border">已完成评估</span>
              <span v-else class="tag-warning border">待评估打分</span>
            </div>
          </div>
          
          <div class="card-body-inner flex-1 flex-col gap-4 custom-scrollbar min-h-0 overflow-y-auto">
            <div class="flex-between border-b pb-3 border-slate-200">
              <span class="text-sm font-medium text-slate-500">系统计算综合得分</span>
              <div class="flex-row items-baseline gap-1">
                <span class="text-3xl font-black" :style="{ color: currentGroup.themeColor }">{{ calculateTotalScore(currentGroup) }}</span>
                <span class="text-lg text-slate-400 font-bold">/100</span>
              </div>
            </div>

            <div class="score-container border p-3">
              <div v-for="dim in dimensions" :key="dim.key" class="flex-between">
                <span class="text-sm font-medium text-slate-600 w-20">{{ dim.label }}</span>
                <input type="range" min="0" max="100" v-model.number="currentGroup.review.scores[dim.key]" class="custom-range flex-1 mx-3" :style="{ '--range-color': currentGroup.themeColor }" :disabled="currentGroup.review.isSubmitted">
                <span class="text-base font-mono font-bold w-8 text-right" :style="{ color: currentGroup.themeColor }">{{ currentGroup.review.scores[dim.key] }}</span>
              </div>
            </div>

            <div class="flex-col gap-2 flex-1 min-h-0">
              <label class="text-sm font-bold text-slate-600 flex-between"><span>评审指导意见</span><span class="text-xs font-normal text-slate-400">支持 Markdown</span></label>
              <textarea v-model="currentGroup.review.comment" :disabled="currentGroup.review.isSubmitted" class="custom-textarea flex-1" placeholder="请输入对该方案架构的优缺点评价、后续迭代建议等内容..."></textarea>
            </div>

            <button @click="submitReview" :disabled="currentGroup.review.isSubmitted" class="btn-large shrink-0 shadow-sm" :style="{ backgroundColor: currentGroup.review.isSubmitted ? '#e2e8f0' : currentGroup.themeColor, color: currentGroup.review.isSubmitted ? '#64748b' : 'white', cursor: currentGroup.review.isSubmitted ? 'not-allowed' : 'pointer' }">
              <svg v-if="!currentGroup.review.isSubmitted" style="width: 20px; height: 20px; flex-shrink: 0; margin-right: 8px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"></path></svg>
              <svg v-else style="width: 20px; height: 20px; flex-shrink: 0; margin-right: 8px; color: #10b981;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"></path></svg>
              {{ currentGroup.review.isSubmitted ? '已成功录入系统' : '确认提交综合评估' }}
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
// [业务逻辑原封不动，与源文件16完全一致]
import { ref, computed, onMounted, onBeforeUnmount, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

const toastMsg = ref('');
const showToast = ref(false);
const showToastMessage = (msg, duration = 1500) => { toastMsg.value = msg; showToast.value = true; return new Promise(resolve => { setTimeout(() => { showToast.value = false; resolve(); }, duration); }); };
const backToWorkspace = () => { router.push('/teacher/ai-evaluate'); };

const dimensions = [ { key: 'security', label: '安全保密性' }, { key: 'integrity', label: '数据完整性' }, { key: 'usability', label: '系统可用性' }, { key: 'innovation', label: '方案创新性' } ];

const groups = reactive([
  { id: 1, name: '第一组', title: '通信安全 + 低功耗专用版', subtitle: '轻量级无人机加密体制', codeId: 'SEC-LOWPOWER-01', themeColor: '#3b82f6', desc: '专注民用小型无人机通信安全，采用 PRESENT 与 ECC 组合...', hardware: 'STM32L432', archLayers: [ { name: '应用层', desc: '飞行控制指令输入输出', highlight: false }, { name: '安全层', desc: '身份认证、数据加解密、密钥协商', highlight: true }, { name: '通信层', desc: 'WiFi / 蓝牙无线数据收发', highlight: false }, { name: '硬件层', desc: 'STM32L432 低功耗单片机', highlight: true } ], flow: ['双向身份认证', '协商一次性会话密钥', '数据加解密与校验', '销毁密钥与休眠'], algorithms: [ { label: '数据加密', name: 'PRESENT 算法', desc: '64bit分组/80bit密钥，SPN结构31轮迭代。' }, { label: '双向身份认证', name: 'ECC (secp256r1)', desc: '无人机与终端基于 ECC 验证双方合法性' } ], review: { scores: { security: 0, integrity: 0, usability: 0, innovation: 0 }, comment: '', isSubmitted: false } },
  { id: 2, name: '第二组', title: '通信安全 + 侧信道防护版', subtitle: '高安全抗物理攻击体制', codeId: 'SEC-SIDECHNL-02', themeColor: '#ef4444', desc: '引入一阶掩码与恒定时间代码实现，抵御DPA攻击。', hardware: 'STM32L432', archLayers: [ { name: '安全核心层', desc: '通信加密、侧信道防护', highlight: true }, { name: '通信接口层', desc: 'WiFi / 蓝牙数据收发', highlight: false }, { name: '硬件支撑层', desc: 'STM32L432 低功耗单片机', highlight: true } ], flow: ['初始化防护单元', '双向认证与密钥协商', '掩码异或与恒定时间加密'], algorithms: [ { label: '侧信道防护', name: 'PRESENT + 掩码机制', desc: '敏感数据与随机掩码异或，无分支跳转。' } ], review: { scores: { security: 0, integrity: 0, usability: 0, innovation: 0 }, comment: '', isSubmitted: false } },
  { id: 3, name: '第三组', title: '通信安全 + 抗重放攻击', subtitle: '抗重放核心防护体制', codeId: 'SEC-NOREPLAY-03', themeColor: '#f59e0b', desc: '基于滑动窗口与严格同步计数机制，丢弃重放的旧数据包。', hardware: 'STM32L432', archLayers: [ { name: '安全层', desc: '数据加解密、身份校验、抗重放计数管理', highlight: true }, { name: '通信层', desc: 'WiFi / 蓝牙数据收发', highlight: true }, { name: '硬件层', desc: 'STM32L432 低功耗单片机', highlight: false } ], flow: ['初始化同步计数器', '身份与同步校验', '附加计数器并加密传输'], algorithms: [ { label: '抗重放防护', name: '滑动窗口计数器', desc: '接收端校验大于本地记录才处理，并丢弃旧包。' } ], review: { scores: { security: 0, integrity: 0, usability: 0, innovation: 0 }, comment: '', isSubmitted: false } },
  { id: 4, name: '第四组', title: '通信安全 + 后量子算法', subtitle: '面向未来算力威胁前瞻体制', codeId: 'SEC-PQUANTUM-04', themeColor: '#8b5cf6', desc: '采用抗量子破解的轻量级后量子密码机制。', hardware: 'STM32L432', archLayers: [ { name: '安全层', desc: '后量子密钥协商、数据加解密', highlight: true }, { name: '通信层', desc: 'WiFi / 蓝牙无线数据收发', highlight: true }, { name: '硬件层', desc: 'STM32L432', highlight: false } ], flow: ['加载后量子参数', 'Kyber 安全密钥协商', '共享密钥加密通信'], algorithms: [ { label: '密钥交换', name: 'CRYSTALS-Kyber', desc: '基于格基难题的轻量级后量子加密算法。' } ], review: { scores: { security: 0, integrity: 0, usability: 0, innovation: 0 }, comment: '', isSubmitted: false } }
]);

const currentGroupId = ref(1); const isGenerating = ref(false); const showContent = ref(false); const showAnimation = ref(false); const isLoading = ref(false);
const currentGroup = computed(() => groups.find(g => g.id === currentGroupId.value));
const studentScored = ref(false); let pollingTimer = null;
const allReviewsSubmitted = computed(() => { return groups.every(group => group.review.isSubmitted); });

const fetchState = async () => { try { const res = await fetch('/api/state'); const state = await res.json(); studentScored.value = state.student_scored_group === 1; } catch (error) {} };

const prevGroup = () => { showAnimation.value = true; setTimeout(() => { currentGroupId.value = currentGroupId.value > 1 ? currentGroupId.value - 1 : 4; showAnimation.value = false; }, 300); };
const nextGroup = () => { showAnimation.value = true; setTimeout(() => { currentGroupId.value = currentGroupId.value < 4 ? currentGroupId.value + 1 : 1; showAnimation.value = false; }, 300); };
const calculateTotalScore = (group) => { if (!group || !group.review) return 0; const s = group.review.scores; return Math.round((s.security + s.integrity + s.usability + s.innovation) / 4); };
const easeInOutQuad = (t) => { return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2; };

const generateReview = async () => {
  if (isGenerating.value) return; isGenerating.value = true; const allGroups = [1, 2, 3, 4];
  for (let targetId of allGroups) {
    const group = groups.find(g => g.id === targetId);
    if (currentGroupId.value !== targetId) { showAnimation.value = true; await new Promise(r => setTimeout(r, 300)); currentGroupId.value = targetId; showAnimation.value = false; await new Promise(r => setTimeout(r, 500)); }
    if (!group.review.isSubmitted) {
      group.review.scores = { security: 0, integrity: 0, usability: 0, innovation: 0 };
      const targetScores = { 1: { security: 90, integrity: 85, usability: 80, innovation: 85 }, 2: { security: 85, integrity: 90, usability: 75, innovation: 90 }, 3: { security: 80, integrity: 75, usability: 90, innovation: 80 }, 4: { security: 95, integrity: 90, usability: 70, innovation: 95 } }[targetId] || { security: 80, integrity: 80, usability: 80, innovation: 80 };
      await new Promise((resolve) => {
        const duration = 800; const delayBetween = 200; const keys = ['security', 'integrity', 'usability', 'innovation']; const initialScores = { ...group.review.scores }; let currentIndex = 0;
        const animateNext = () => { if (currentIndex >= keys.length) { resolve(); return; } const key = keys[currentIndex]; const startTime = Date.now();
          const animate = () => { const elapsed = Date.now() - startTime; const progress = Math.min(elapsed / duration, 1); const easedProgress = easeInOutQuad(progress); group.review.scores[key] = Math.round(initialScores[key] + (targetScores[key] - initialScores[key]) * easedProgress); if (progress < 1) { requestAnimationFrame(animate); } else { currentIndex++; setTimeout(animateNext, delayBetween); } }; animate();
        }; animateNext();
      });
      const reviewComments = { 1: '该方案在低功耗限制下做出了很好的权衡。PRESENT算法的硬件实现资源极小。', 2: '侧信道防护措施综合考虑性能与效率，采用一阶掩码机制实现。', 3: '抗重放攻击机制设计合理，滑动窗口计数器能够有效防止指令劫持。', 4: '采用后量子算法，具有前瞻性，能够抵御未来量子计算的威胁。' };
      const comment = reviewComments[targetId] || '该方案整体设计合理。'; group.review.comment = '';
      await new Promise((resolve) => { let index = 0; const typingInterval = setInterval(() => { if (index < comment.length) { group.review.comment += comment.charAt(index); index++; } else { clearInterval(typingInterval); resolve(); } }, 30); });
      group.review.isSubmitted = true; const finalScore = calculateTotalScore(group); await showToastMessage(`给第${targetId}组评${finalScore}分成功`, 1500);
    }
  }
  try { await fetch('/api/state/update', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ "teacher_scored_group": 1 }) }); } catch (error) {}
  isGenerating.value = false;
};

const submitReview = async () => {
  if (currentGroup.value.review.isSubmitted) return; currentGroup.value.review.isSubmitted = true;
  const finalScore = calculateTotalScore(currentGroup.value); await showToastMessage(`给第${currentGroupId.value}组评${finalScore}分成功`, 1500);
  if (allReviewsSubmitted.value) { try { await fetch('/api/state/update', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ "teacher_scored_group": 1 }) }); } catch (e) {} } 
  else { const nextUnsubmitted = groups.find(g => !g.review.isSubmitted); if (nextUnsubmitted) { showAnimation.value = true; setTimeout(() => { currentGroupId.value = nextUnsubmitted.id; showAnimation.value = false; }, 500); } }
};

const completeEvaluation = () => { if (!allReviewsSubmitted.value) return; isLoading.value = true; setTimeout(() => { router.push('/teacher/group-score-overview'); }, 1500); };
onMounted(() => { if (route.query.groupId) { const id = parseInt(route.query.groupId); if (id >= 1 && id <= 4) currentGroupId.value = id; } setTimeout(() => { showContent.value = true; }, 100); fetchState(); pollingTimer = setInterval(fetchState, 1000); });
onBeforeUnmount(() => { if (pollingTimer) clearInterval(pollingTimer); });
</script>

<style scoped>
.page-container { height: 100%; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; }
.page-header { flex-shrink: 0; height: 64px; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); z-index: 10; }
.header-left { display: flex; align-items: center; gap: 12px; } .header-right { display: flex; align-items: center; }
.status-indicator { width: 6px; height: 24px; border-radius: 4px; }
.page-title { font-size: 18px; font-weight: 800; margin: 0; color: #1e293b; }

.flex-row { display: flex; align-items: center; } .flex-col { display: flex; flex-direction: column; } .flex-between { display: flex; justify-content: space-between; align-items: center; } .flex-1 { flex: 1; min-height: 0; }
.gap-2 { gap: 8px; } .gap-3 { gap: 12px; } .gap-4 { gap: 16px; } .shrink-0 { flex-shrink: 0; }

.group-nav { display: flex; align-items: center; background: white; border-radius: 8px; border: 1px solid #e2e8f0; overflow: hidden; margin-right: 16px; }
.nav-btn { background: transparent; border: none; padding: 6px; color: #94a3b8; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s; } .nav-btn:hover { color: #1e293b; background: #f1f5f9; }
.nav-title { padding: 0 16px; font-weight: 900; font-size: 16px; }

.btn-icon { color: #06b6d4; background: transparent; border: none; cursor: pointer; transition: transform 0.3s; display: flex; align-items: center; justify-content: center; } .btn-icon:hover { transform: rotate(180deg); color: #0891b2; }
.btn-outline { background: white; border: 1px solid #e2e8f0; color: #475569; padding: 8px 16px; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; display: flex; align-items: center; transition: all 0.2s; } .btn-outline:hover { background: #f8fafc; color: #1e293b; }
.btn-primary { background: #10b981; color: white; border: 1px solid #10b981; padding: 8px 16px; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; display: flex; align-items: center; transition: all 0.2s; } .btn-primary:hover { background: #059669; }
.btn-disabled { background: #cbd5e1; border-color: #cbd5e1; color: white; cursor: not-allowed; } .btn-disabled:hover { background: #cbd5e1; }
.btn-light { background: white; border: 1px solid #e2e8f0; color: #64748b; padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: bold; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 4px; transition: all 0.2s; } .btn-light:hover { background: #f8fafc; color: #1e293b; }
.btn-large { padding: 10px; border: none; border-radius: 8px; font-size: 14px; font-weight: bold; display: flex; align-items: center; justify-content: center; transition: all 0.2s; }

.main-content { padding: 24px; overflow: hidden; }
.layout-grid-12 { display: grid; grid-template-columns: repeat(12, 1fr); gap: 24px; height: 100%; min-height: 0; }
.grid-col-3 { grid-column: span 3; } .grid-col-4 { grid-column: span 4; } .grid-col-5 { grid-column: span 5; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
.bg-blur-circle { position: absolute; top: 0; right: 0; width: 96px; height: 96px; border-bottom-left-radius: 100%; opacity: 0.1; }
.bg-blur-circle-tr { position: absolute; top: -24px; right: -24px; width: 96px; height: 96px; filter: blur(20px); opacity: 0.1; }

.p-4 { padding: 16px; } .p-5 { padding: 20px; }
.border-t { border-top: 1px solid #e2e8f0; } .border-b { border-bottom: 1px solid #e2e8f0; } .border-l-4 { border-left-width: 4px; border-left-style: solid; } .border-t-4 { border-top-width: 4px; border-top-style: solid; }
.border { border: 1px solid #e2e8f0; }

.text-xs { font-size: 12px; } .text-sm { font-size: 14px; } .text-base { font-size: 16px; } .text-xl { font-size: 20px; } .text-2xl { font-size: 24px; } .text-3xl { font-size: 30px; }
.font-bold { font-weight: bold; } .font-black { font-weight: 900; } .font-mono { font-family: monospace; } .italic { font-style: italic; }
.text-slate-500 { color: #64748b; } .text-slate-800 { color: #1e293b; } .text-white { color: white; } .text-gray-400 { color: #94a3b8; } .text-gray-500 { color: #64748b; } .text-gray-300 { color: #cbd5e1; }

.tag-sm { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }
.tag-success { background: #dcfce7; color: #10b981; border-color: #bbf7d0; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }
.tag-warning { background: #fef3c7; color: #f59e0b; border-color: #fde68a; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }

.algo-box { background: white; padding: 12px; border: 1px solid #e2e8f0; border-radius: 8px; display: flex; flex-direction: column; justify-content: center; }
.grid-cols-2 { display: grid; grid-template-columns: 1fr 1fr; }

.timeline-line { position: absolute; top: 24px; bottom: 24px; left: 48px; width: 2px; background: #e2e8f0; z-index: 0; }
.arch-item { padding: 12px; border-radius: 12px; display: flex; align-items: center; gap: 16px; margin-bottom: 12px; transition: all 0.3s; position: relative; }
.arch-tag { width: 80px; height: 28px; display: flex; align-items: center; justify-content: center; border-radius: 6px; background: white; z-index: 10; }
.arch-highlight-bar { position: absolute; right: 0; top: 0; bottom: 0; width: 6px; border-radius: 0 12px 12px 0; opacity: 0.8; z-index: 10; }
.scale-102 { transform: scale(1.02); }
.bg-slate-50\/50 { background: rgba(248,250,252,0.5); }

.flow-box { background: white; border-radius: 12px; padding: 16px; }
.flow-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-size: 12px; font-weight: 900; text-transform: uppercase; letter-spacing: 1px; }
.flow-body { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; }
.flow-step { background: #f8fafc; padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: bold; font-family: monospace; }
.hover-underline:hover { text-decoration: underline; } .text-blue-500 { color: #3b82f6; }

.card-header-inner { padding: 12px 20px; } .card-body-inner { padding: 20px; }
.score-container { background: white; border-radius: 12px; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.custom-range { -webkit-appearance: none; height: 6px; background: #e2e8f0; border-radius: 3px; outline: none; }
.custom-range::-webkit-slider-thumb { -webkit-appearance: none; width: 16px; height: 16px; border-radius: 50%; background: var(--range-color); cursor: pointer; box-shadow: 0 0 10px var(--range-color); }
.custom-range:disabled::-webkit-slider-thumb { background: #94a3b8; box-shadow: none; cursor: not-allowed; }
.custom-textarea { width: 100%; background: white; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; font-size: 14px; color: #1e293b; resize: none; outline: none; transition: border-color 0.2s; }
.custom-textarea:focus { border-color: #3b82f6; }
.custom-textarea:disabled { opacity: 0.6; cursor: not-allowed; background: #f8fafc; }

.toast-box { position: fixed; top: 80px; left: 50%; transform: translateX(-50%); background: white; border: 1px solid #10b981; color: #10b981; padding: 12px 24px; border-radius: 8px; display: flex; align-items: center; gap: 12px; z-index: 100; box-shadow: 0 4px 15px rgba(16,185,129,0.2); }
.loading-overlay { position: fixed; inset: 0; background: rgba(255,255,255,0.8); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 50; }
.loading-card { background: white; border: 1px solid #e2e8f0; border-radius: 12px; padding: 32px; display: flex; flex-direction: column; align-items: center; gap: 16px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
.spinner { width: 48px; height: 48px; border: 4px solid #f1f5f9; border-top-color: currentColor; border-radius: 50%; animation: spin 1s linear infinite; } .border-green { border-top-color: #10b981; }

.custom-scrollbar::-webkit-scrollbar { width: 6px; } .custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 3px; }
.animate-fade-in-up { animation: fadeInUp 0.5s ease-out forwards; opacity: 0; }
@keyframes fadeInUp { to { opacity: 1; transform: translateY(0); } }
@keyframes spin { 100% { transform: rotate(360deg); } }
</style>