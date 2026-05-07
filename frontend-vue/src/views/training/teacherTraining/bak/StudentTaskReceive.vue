<template>
  <div class="page-container">
    <transition name="toast">
      <div v-if="showToast" class="toast-msg toast-success">
        <svg class="icon-md" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
        <span>需求确认完毕！已成功提交至教师端进行初评。</span>
      </div>
    </transition>

    <header v-if="showContent" class="page-header">
      <div class="header-left">
        <div class="status-indicator bg-green"></div>
        <h1 class="page-title">需求分析阶段：需求提交</h1>
      </div>
    </header>

    <main v-if="showContent" class="main-content">
      <div class="layout-grid-1-2">
        
        <!-- 左侧前置思考 -->
        <section class="glass-card flex-col">
          <div class="card-header">
            <h2 class="card-title">需求前置思考</h2>
          </div>
          <div class="card-body gap-4">
            <div class="info-box border-l-green">
              <div class="text-lg font-bold">{{ taskInfo.title }}</div>
              <div class="tag-green mt-2">目标对象：无人机通信链路</div>
            </div>
            <div class="flex-col gap-3">
              <div v-for="metric in taskInfo.metrics" :key="metric.title" class="metric-box">
                <div class="metric-title text-green">
                  <component :is="metric.icon" class="icon-sm" />
                  {{ metric.title }}
                </div>
                <div class="metric-desc">{{ metric.desc }}</div>
              </div>
            </div>
          </div>
        </section>

        <!-- 右侧需求提报 -->
        <section class="glass-card flex-col relative overflow-hidden">
          <div class="card-header flex-between">
            <h2 class="card-title">学生需求提交</h2>
            <span class="text-xs text-slate-500">建议与AI多模态交互辅助完成</span>
          </div>
          <div class="card-body gap-5 flex-1 flex-col">
            <div>
              <label class="form-label">主线方案设计方向</label>
              <div class="readonly-box">主线任务：基于无人机通信场景，完成密码系统的一般流程设计。</div>
            </div>
            
            <div class="flex-1 flex-col relative">
              <label class="form-label">填写需求：</label>
              <div class="relative flex-1">
                <textarea 
                  v-model="currentGroup.branchContent"
                  class="custom-textarea"
                  :readonly="currentGroup.isSubmitted || currentGroup.isOptimizing"
                  placeholder="在此输入本组需求分析结果..."
                ></textarea>
                <div v-if="currentGroup.isOptimizing" class="ai-mask">
                  <div class="ai-mask-content text-green">
                    <div class="spinner border-green mb-2"></div>
                    AI整理优化中...
                  </div>
                </div>
              </div>
            </div>

            <div class="flex-between mt-2">
              <div class="text-sm font-bold text-slate-600 flex-1">{{ statusText }}</div>
              <button 
                @click="handleButtonClick" 
                :disabled="currentGroup.isSubmitted || currentGroup.isLoading || currentGroup.isOptimizing"
                class="btn-primary"
              >
                {{ currentGroup.isSubmitted ? '需求分类已生成' : (currentGroup.isOptimized ? '提交并分类' : '呼叫 AI助手：整理并优化') }}
              </button>
            </div>
          </div>
          
          <transition name="fade">
            <div v-if="currentGroup.isSubmitted" class="success-mask">
              <div class="text-2xl font-bold text-green mb-2">AI分类图表已生成</div>
              <div class="text-sm text-slate-500 text-center leading-relaxed">
                需求已通过 <strong>AI需求分类工具</strong> 备案，并同步至教师端。<br>请准备进入“方案设计”阶段...
              </div>
            </div>
          </transition>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter(); 
const LockIcon = { template: '<svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>' };
const ShieldIcon = { template: '<svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>' };
const BoltIcon = { template: '<svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>' };
const ChipIcon = { template: '<svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m14-6h2m-2 6h2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z"/></svg>' };

const showToast = ref(false);
const showContent = ref(false);
const currentGroupIndex = ref(0);

onMounted(() => { 
  const storedInfo = localStorage.getItem('selectedGroupInfo');
  if (storedInfo) {
    const groupInfo = JSON.parse(storedInfo);
    if (groupInfo.groupId) currentGroupIndex.value = parseInt(groupInfo.groupId) - 1;
  }
  setTimeout(() => { showContent.value = true; }, 100);
});

const taskInfo = reactive({
  title: '无人机通信加密设计',
  metrics: [
    { title: '机密性与完整性', desc: '避免非法设备接入和数据篡改，支持多种数据类型指令加密。', icon: LockIcon },
    { title: '防重放防侧信道', desc: '规避无人机易遭受侧信道攻击、重放攻击的系统级隐患。', icon: ShieldIcon },
    { title: '时延敏感与可用性', desc: '无人机高速移动，通信加密时延需严格控制以防偏离航线。', icon: BoltIcon },
    { title: '算力与功耗限制', desc: '机载设备功耗有限，加密算法需兼顾算力消耗与低功耗需求。', icon: ChipIcon },
  ]
});

const defaultBranchContent = [
  '通过AI资料推送工具查阅，结合本组分析，需求与设计如下：\n1. 核心约束：密码系统需满足机密性、完整性、可用性；考虑到无人机算力有限且时延敏感，机载设备功耗不能太高是首要约束。\n2. 方案初设：对比多种轻量级算法，主线通信拟采用 PRESENT 算法，因其功耗低、算力需求小。\n3. 支线策略：在工程验证阶段，进一步简化密钥更新流程，重点满足"密钥更新低功耗需求"。',
  '结合 AI 工具推送资料与本组讨论，需求与设计如下：\n1. 核心约束：无人机极易遭受侧信道攻击与重放攻击，且通信加密时延不能太长，以免影响飞控指令。\n2. 方案初设：主线采用安全性更高的 AES 加密算法进行数据链路加密。\n3. 支线策略：配合主线加密，设计并引入轻量级掩码防护技术，以实现侧信道防护。',
  '依据 AI 资料检索与补充，本组梳理的需求与初步方案如下：\n1. 核心约束：重点强化身份认证，坚决避免非法设备接入；加密算法不能过于复杂，必须适配无人机算力基线。\n2. 方案初设：主线拟采用 AES 加密保障指令机密性，兼顾安全性与实现复杂度。\n3. 支线策略：结合随机数生成器与序列号(Sequence Number)机制，建立抗重放攻击机制。',
  '参考 AI 工具推送的后量子技术参数，本组确立如下需求清单与方向：\n1. 核心约束：系统必须能够支持指令和数据传输等多种数据类型的加密；全面梳理算法适配的算力及兼容性指标。\n2. 方案初设：前瞻性地将后量子加密算法用于主线通信加密的尝试，适配未来的算力演进。\n3. 支线策略：突破点在于通过简化后量子算法的适配流程来大幅降低当前无人机的算力消耗。'
];

const groupsData = reactive([
  { id: 'G1', branchContent: '', isLoading: false, isOptimizing: false, isOptimized: false, isSubmitted: false },
  { id: 'G2', branchContent: '', isLoading: false, isOptimizing: false, isOptimized: false, isSubmitted: false },
  { id: 'G3', branchContent: '', isLoading: false, isOptimizing: false, isOptimized: false, isSubmitted: false },
  { id: 'G4', branchContent: '', isLoading: false, isOptimizing: false, isOptimized: false, isSubmitted: false }
]);

const currentGroup = computed(() => groupsData[currentGroupIndex.value]);

const statusText = computed(() => {
  if (currentGroup.value.isOptimizing) return 'AI助手正在整理优化您的需求...';
  if (currentGroup.value.isLoading) return '正在呼叫 AI助教...生成需求分类可视化图表';
  if (currentGroup.value.isSubmitted) return '需求清单已提交给 AI需求分类工具备案';
  if (currentGroup.value.isOptimized) return '需求已优化完成，请提交';
  return '请填写需求后呼叫 AI助手 整理优化';
});

const typeText = (targetText) => {
  let currentIndex = 0;
  currentGroup.value.branchContent = '';
  const typeInterval = setInterval(() => {
    if (currentIndex < targetText.length) {
      currentGroup.value.branchContent += targetText.charAt(currentIndex);
      currentIndex++;
    } else {
      clearInterval(typeInterval);
      currentGroup.value.isOptimizing = false;
      currentGroup.value.isOptimized = true;
    }
  }, 30);
};

const handleButtonClick = () => {
  if (!currentGroup.value.isOptimized) {
    currentGroup.value.isOptimizing = true;
    setTimeout(() => { typeText(defaultBranchContent[currentGroupIndex.value]); }, 1000);
  } else {
    handleSubmit();
  }
};

const handleSubmit = async () => {
  currentGroup.value.isLoading = true;
  try {
    const groupId = currentGroupIndex.value + 1; 
    const stateKey = `demand_g${groupId}_submitted`;
    const requestBody = {}; requestBody[stateKey] = 1;
    await fetch('/api/state/update', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(requestBody) });
  } catch (error) {}

  setTimeout(() => {
    currentGroup.value.isLoading = false;
    currentGroup.value.isSubmitted = true;
    showToast.value = true;
    setTimeout(() => { router.push('/training/student-training/student-task-select'); }, 2000);
  }, 1500);
};
</script>

<style scoped>
.page-container { height: 100%; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; }
.page-header { flex-shrink: 0; height: 64px; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); z-index: 10; }
.header-left { display: flex; align-items: center; gap: 12px; }
.status-indicator { width: 6px; height: 24px; border-radius: 4px; }
.page-title { font-size: 18px; font-weight: 800; margin: 0; }

.main-content { flex: 1; overflow-y: auto; padding: 24px; }
.layout-grid-1-2 { display: grid; grid-template-columns: 1fr 2fr; gap: 24px; height: 100%; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; }
.flex-col { display: flex; flex-direction: column; } .flex-1 { flex: 1; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.relative { position: relative; }

.card-header { padding: 16px 20px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.5); }
.card-title { font-size: 16px; font-weight: 800; margin: 0; color: #1e293b; }
.card-body { padding: 20px; display: flex; flex-direction: column; }
.gap-3 { gap: 12px; } .gap-4 { gap: 16px; } .gap-5 { gap: 20px; }

.info-box { background: white; border: 1px solid #e2e8f0; padding: 16px; border-radius: 12px; }
.border-l-green { border-left: 4px solid #23b586; }
.tag-green { display: inline-block; padding: 4px 8px; background: #ecfdf5; color: #047857; border-radius: 4px; font-size: 12px; font-weight: bold; }
.metric-box { background: white; padding: 12px; border-radius: 12px; border: 1px solid #e2e8f0; }
.metric-title { font-size: 14px; font-weight: bold; display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.metric-desc { font-size: 12px; color: #64748b; line-height: 1.5; }

.form-label { display: block; font-size: 14px; font-weight: bold; color: #334155; margin-bottom: 8px; }
.readonly-box { background: #f8fafc; border: 1px solid #e2e8f0; padding: 12px; border-radius: 8px; font-size: 14px; color: #475569; }
.custom-textarea { width: 100%; height: 100%; min-height: 150px; background: white; border: 1px solid #cbd5e1; border-radius: 8px; padding: 16px; resize: none; color: #1e293b; font-size: 14px; box-sizing: border-box; }
.custom-textarea:focus { outline: none; border-color: #23b586; box-shadow: 0 0 0 2px rgba(35,181,134,0.2); }

.btn-primary { background: #23b586; color: white; padding: 12px 32px; border: none; border-radius: 8px; font-weight: bold; cursor: pointer; transition: all 0.2s; box-shadow: 0 4px 6px rgba(35,181,134,0.3); }
.btn-primary:disabled { background: #cbd5e1; box-shadow: none; cursor: not-allowed; }
.btn-primary:hover:not(:disabled) { background: #059669; }

.text-lg { font-size: 18px; } .text-sm { font-size: 14px; } .text-xs { font-size: 12px; } .text-2xl { font-size: 24px; }
.font-bold { font-weight: bold; } .text-green { color: #23b586; } .text-slate-800 { color: #1e293b; } .text-slate-600 { color: #475569; } .text-slate-500 { color: #64748b; } .mt-2 { margin-top: 8px; }

.icon-sm { width: 16px; height: 16px; flex-shrink: 0; }
.icon-md { width: 24px; height: 24px; flex-shrink: 0; }
.spinner { width: 32px; height: 32px; border: 4px solid #f1f5f9; border-top-color: #23b586; border-radius: 50%; animation: spin 1s linear infinite; }

.toast-msg { position: fixed; top: 80px; left: 50%; transform: translateX(-50%); color: white; padding: 12px 24px; border-radius: 12px; display: flex; align-items: center; gap: 8px; font-weight: bold; z-index: 50; }
.toast-success { background: #23b586; }
.ai-mask, .success-mask { position: absolute; inset: 0; background: rgba(255,255,255,0.9); backdrop-filter: blur(4px); display: flex; flex-direction: column; align-items: center; justify-content: center; border-radius: 12px; z-index: 20; }
.ai-mask-content { display: flex; flex-direction: column; align-items: center; font-weight: bold; }

@keyframes spin { 100% { transform: rotate(360deg); } }
.toast-enter-active, .toast-leave-active { transition: all 0.3s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translate(-50%, -20px); }
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>