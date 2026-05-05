<template>
  <div class="page-container relative">
    
    <!-- Toast 通知 -->
    <div class="toast-container z-50">
      <transition-group name="toast">
        <div v-for="toast in toasts" :key="toast.id" class="toast-item">
          <span class="toast-dot" :class="toast.colorClass"></span>
          <span class="tracking-wide">{{ toast.msg }}</span>
        </div>
      </transition-group>
    </div>

    <!-- 发布全屏动画 -->
    <div v-if="isPublishing" class="deploy-mask">
      <div class="deploy-card border-cyan">
        <div class="spinner-xl border-cyan mb-6"></div>
        <div class="flex-between w-full mb-3 px-2">
          <div class="text-cyan font-bold tracking-widest flex-row">
            <span class="mr-2 font-mono">>_</span> 系统任务发布中
          </div>
          <div class="text-cyan font-mono text-3xl font-black">{{ Math.round(publishProgress) }}%</div>
        </div>
        <div class="progress-bg w-full"><div class="progress-bar bg-cyan" :style="{ width: publishProgress + '%' }"></div></div>
        <div class="mt-5 w-full bg-slate-100 rounded border border-slate-300 p-2 text-xs font-mono text-slate-500 opacity-80 h-12 overflow-hidden">
          <div class="animate-pulse">
            <p>> ESTABLISHING_SECURE_CHANNEL ... <span class="text-green-600">OK</span></p>
            <p>> UPLOADING_TASKS_TO_NODES ... <span class="text-green-600">{{ Math.round(publishProgress) }}%</span></p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 顶部状态栏 -->
    <div class="glass-card p-4 shrink-0 flex-row gap-6 overflow-x-auto custom-scrollbar whitespace-nowrap mb-4">
      <div class="flex-row gap-2"><span class="text-slate-500 font-bold">课程名称：</span><span class="text-slate-800 font-bold">无人机密码系统设计实战</span></div>
      <div class="flex-row gap-2"><span class="text-slate-500 font-bold">实现阶段：</span><span class="text-slate-800 font-bold">第一阶段：需求分析</span></div>
      <div class="flex-row gap-2"><span class="text-slate-500 font-bold">学生人数：</span><span class="text-slate-800 font-bold">12 人</span></div>
      <div class="flex-row gap-2"><span class="text-slate-500 font-bold">当前里程：</span><span class="text-slate-800 font-bold">无人机通信加密设计</span></div>
      <div class="flex-row gap-2"><span class="text-slate-500 font-bold">分组设置：</span><span class="text-slate-800 font-bold">4 个实战小组</span></div>
      <div class="flex-row gap-2">
        <span class="text-slate-500 font-bold">课时时长：</span>
        <div class="flex-row gap-3">
          <span class="text-slate-800 font-bold">45 分钟</span>
          <button @click="handleRefresh" :disabled="isAnimating" class="btn-icon">
            <svg style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 主体区域 -->
    <div class="flex-1 flex gap-4 min-h-0">
      <div class="w-1-3 glass-card p-4 flex-col gap-3">
        <div class="flex-row mb-1"><div class="w-1-5 h-4 bg-green mr-2 rounded-full"></div><h2 class="font-bold text-slate-800">【系统需求点梳理】</h2></div>
        <div class="flex-1 flex-col gap-3 min-h-0">
          <div class="demand-box border-l-blue"><div class="text-blue font-bold mb-1">【需求点一】</div><div class="text-slate-600 text-sm leading-relaxed">高保密性要求。无人机飞控指令与传输数据需具备极强的抗窃听能力。</div></div>
          <div class="demand-box border-l-red"><div class="text-red font-bold mb-1">【需求点二】</div><div class="text-slate-600 text-sm leading-relaxed">高完整性要求。需建立数据校验机制，防止飞行坐标、控制指令在传输过程中被恶意篡改。</div></div>
          <div class="demand-box border-l-yellow"><div class="text-yellow font-bold mb-1">【需求点三】</div><div class="text-slate-600 text-sm leading-relaxed">低时延高可用要求。无人机高速移动场景下，加密握手与加解密处理时延需控制在毫秒级。</div></div>
          <div class="demand-box border-l-purple"><div class="text-purple font-bold mb-1">【需求点四】</div><div class="text-slate-600 text-sm leading-relaxed">低功耗适配要求。受限于无人机机载电池容量，加密机制需严格控制额外功耗。</div></div>
        </div>
      </div>

      <div class="flex-1 glass-card p-4 flex-col gap-4">
        <div class="flex-col gap-2 shrink-0">
          <div class="flex-row"><div class="w-1-5 h-4 bg-green mr-2 rounded-full"></div><h2 class="font-bold text-slate-800">【主线任务汇总】</h2></div>
          <div class="main-task-box">
            {{ mainTaskContent }}<span v-if="isTypingMain" class="text-cyan font-bold ml-1 animate-pulse">|</span>
          </div>
        </div>

        <div class="flex-1 flex-col gap-2 min-h-0">
          <div class="flex-row mt-1"><div class="w-1-5 h-4 bg-blue mr-2 rounded-full"></div><h2 class="font-bold text-slate-800">【支线任务分配】</h2></div>
          <div class="grid-cols-2 gap-3 flex-1 min-h-0 pb-2">
            <div class="sub-task-box" :class="showGroup1 ? 'border-blue shadow-blue' : 'border-slate'">
              <div class="font-bold mb-2 flex-row text-slate-800"><span v-if="showGroup1" class="tag-sm bg-blue text-white mr-2">组1</span>支线方向 1：低功耗优化</div>
              <div class="sub-task-content" :class="showGroup1 ? 'border-blue-light' : 'border-slate-light'">
                <textarea v-model="task1Content" class="custom-textarea" rows="4"></textarea>
                <span v-if="task1Content.length > 0 && task1Content.length < originalT1.length" class="type-cursor text-cyan">█</span>
              </div>
            </div>
            <div class="sub-task-box" :class="showGroup2 ? 'border-red shadow-red' : 'border-slate'">
              <div class="font-bold mb-2 flex-row text-slate-800"><span v-if="showGroup2" class="tag-sm bg-red text-white mr-2">组2</span>支线方向 2：侧信道防护</div>
              <div class="sub-task-content" :class="showGroup2 ? 'border-red-light' : 'border-slate-light'">
                <textarea v-model="task2Content" class="custom-textarea" rows="4"></textarea>
                <span v-if="task2Content.length > 0 && task2Content.length < originalT2.length" class="type-cursor text-cyan">█</span>
              </div>
            </div>
            <div class="sub-task-box" :class="showGroup3 ? 'border-yellow shadow-yellow' : 'border-slate'">
              <div class="font-bold mb-2 flex-row text-slate-800"><span v-if="showGroup3" class="tag-sm bg-yellow text-white mr-2">组3</span>支线方向 3：抗重放攻击</div>
              <div class="sub-task-content" :class="showGroup3 ? 'border-yellow-light' : 'border-slate-light'">
                <textarea v-model="task3Content" class="custom-textarea" rows="4"></textarea>
                <span v-if="task3Content.length > 0 && task3Content.length < originalT3.length" class="type-cursor text-cyan">█</span>
              </div>
            </div>
            <div class="sub-task-box" :class="showGroup4 ? 'border-purple shadow-purple' : 'border-slate'">
              <div class="font-bold mb-2 flex-row text-slate-800"><span v-if="showGroup4" class="tag-sm bg-purple text-white mr-2">组4</span>支线方向 4：后量子算法适配</div>
              <div class="sub-task-content" :class="showGroup4 ? 'border-purple-light' : 'border-slate-light'">
                <textarea v-model="task4Content" class="custom-textarea" rows="4"></textarea>
                <span v-if="task4Content.length > 0 && task4Content.length < originalT4.length" class="type-cursor text-cyan">█</span>
              </div>
            </div>
          </div>
        </div>

        <div class="flex-center mt-2 shrink-0">
          <button @click="handlePublish" :disabled="isPublishing || step < 2 || !allGroupsSelected" class="btn-publish" :class="(!isPublishing && step >= 2 && allGroupsSelected) ? 'btn-publish-active' : 'btn-publish-disabled'">
            <div class="btn-progress" :style="{ width: publishProgress + '%' }"></div>
            <span class="relative z-10 flex-row">
              <svg v-if="!isPublishing" style="width: 20px; height: 20px; flex-shrink: 0; margin-right: 4px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path></svg>
              {{ isPublishing ? '任务发布中...' : (allGroupsSelected ? '确认发布任务' : '等待学生选组完成...') }}
            </span>
          </button>
        </div>
      </div>
    </div>
    
    <div v-if="showSuccessModal" class="modal-overlay">
      <div class="modal-card border-cyan shadow-cyan">
        <button @click="closeModal" class="modal-close"><svg style="width: 20px; height: 20px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg></button>
        <h2 class="text-xl font-bold text-cyan mb-4 text-center tracking-widest">任务发布成功</h2>
        <div class="flex-col gap-4 mb-6">
          <div class="bg-slate-50 border border-slate-200 rounded-lg p-3"><div class="text-cyan font-bold text-sm mb-1">【主线任务】</div><div class="text-slate-600 text-xs">完成无人机通信加密系统全流程设计，输出可落地的加密安全方案。</div></div>
          <div>
            <div class="text-slate-800 font-bold text-sm mb-2">【支线任务分配】</div>
            <div class="grid-cols-2 gap-2">
              <div class="bg-slate-50 border border-blue-200 rounded-lg p-2"><div class="text-blue font-bold text-xs mb-1">组1：低功耗优化</div><div class="text-slate-500 text-xs truncate">{{ task1Content }}</div></div>
              <div class="bg-slate-50 border border-red-200 rounded-lg p-2"><div class="text-red font-bold text-xs mb-1">组2：侧信道防护</div><div class="text-slate-500 text-xs truncate">{{ task2Content }}</div></div>
              <div class="bg-slate-50 border border-yellow-200 rounded-lg p-2"><div class="text-yellow font-bold text-xs mb-1">组3：抗重放攻击</div><div class="text-slate-500 text-xs truncate">{{ task3Content }}</div></div>
              <div class="bg-slate-50 border border-purple-200 rounded-lg p-2"><div class="text-purple font-bold text-xs mb-1">组4：后量子适配</div><div class="text-slate-500 text-xs truncate">{{ task4Content }}</div></div>
            </div>
          </div>
        </div>
        <div class="flex-center"><button @click="confirmJump" class="btn-cyan">确认并跳转至AI任务梳理页</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
// [SCRIPT 代码原封不动，完美保留，与源文件20一致]
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const originalMain = '完成无人机通信加密系统全流程设计，覆盖需求分析、方案选型、仿真验证全环节，输出可落地的加密安全方案，满足上述 4 项需求点的全部要求。'
const originalT1 = '聚焦加密算法轻量化选型与密钥管理流程优化，在保障安全的前提下，最大限度降低加密机制带来的额外功耗。'
const originalT2 = '针对无人机嵌入式设备的物理泄露风险，设计轻量级掩码防护机制，抵御侧信道攻击，提升系统物理安全。'
const originalT3 = '设计基于随机数 + 序列号的双向认证机制，杜绝非法重放攻击，保障无人机通信链路的接入安全与指令合法性。'
const originalT4 = '评估后量子加密算法在无人机场景的适配性，筛选轻量化格密码方案，保障系统在量子计算场景下的长期安全。'

const step = ref(0) 
const isAnimating = ref(false)

const mainTaskContent = ref('')
const task1Content = ref('')
const task2Content = ref('')
const task3Content = ref('')
const task4Content = ref('')

const isTypingMain = ref(false)
const showGroupBadges = ref(false) 
const showGroup1 = ref(false)
const showGroup2 = ref(false)
const showGroup3 = ref(false)
const showGroup4 = ref(false)

const allGroupsSelected = computed(() => { return showGroup1.value && showGroup2.value && showGroup3.value && showGroup4.value })

let pollingTimer = null

const fetchState = async () => {
  try {
    const res = await fetch('/api/state')
    const state = await res.json()
    if (state.task_selected_g1 === 1 && !showGroup1.value) { showGroup1.value = true; addToast('组1 成功接取任务：支线方向1 (低功耗优化)', 'bg-green') }
    if (state.task_selected_g2 === 1 && !showGroup2.value) { showGroup2.value = true; addToast('组2 成功接取任务：支线方向2 (侧信道防护)', 'bg-blue') }
    if (state.task_selected_g3 === 1 && !showGroup3.value) { showGroup3.value = true; addToast('组3 成功接取任务：支线方向3 (抗重放攻击)', 'bg-yellow') }
    if (state.task_selected_g4 === 1 && !showGroup4.value) { showGroup4.value = true; addToast('组4 成功接取任务：支线方向4 (后量子算法适配)', 'bg-purple') }
  } catch (error) {}
}

onMounted(() => { fetchState(); pollingTimer = setInterval(fetchState, 1000); })
onUnmounted(() => { if (pollingTimer) clearInterval(pollingTimer) })

const typeText = (targetRef, fullText, isMain = false, onComplete = null) => {
  if (isMain) isTypingMain.value = true
  let i = 0
  const speed = 25 
  const timer = setInterval(() => {
    if (i <= fullText.length) { targetRef.value = fullText.substring(0, i); i++; } 
    else { clearInterval(timer); targetRef.value = fullText; if (isMain) isTypingMain.value = false; if (onComplete) onComplete(); }
  }, speed)
}

const triggerTypingEffect = (callback) => {
  typeText(mainTaskContent, originalMain, true, () => {
    typeText(task1Content, originalT1, false, () => {
      typeText(task2Content, originalT2, false, () => {
        typeText(task3Content, originalT3, false, () => {
          typeText(task4Content, originalT4, false, callback)
        })
      })
    })
  })
}

const toasts = ref([])
let toastId = 0
const addToast = (msg, colorClass) => {
  const id = toastId++
  toasts.value.push({ id, msg, colorClass })
  setTimeout(() => { toasts.value = toasts.value.filter(t => t.id !== id) }, 4500)
}

const handleRefresh = async () => {
  if (isAnimating.value) return
  if (step.value === 0) {
    step.value = 1; isAnimating.value = true
    try { await fetch('/api/state/update', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ task_published: 1 }) }) } catch (e) {}
    triggerTypingEffect(() => { isAnimating.value = false; step.value = 2; })
  } else if (step.value === 2) {
    step.value = 0; mainTaskContent.value = ''; task1Content.value = ''; task2Content.value = ''; task3Content.value = ''; task4Content.value = '';
    showGroupBadges.value = false; showGroup1.value = false; showGroup2.value = false; showGroup3.value = false; showGroup4.value = false; toasts.value = []
  }
}

const isPublishing = ref(false)
const publishProgress = ref(0)
const showSuccessModal = ref(false)

const handlePublish = () => {
  if (isPublishing.value || step.value < 2) return
  isPublishing.value = true; publishProgress.value = 0
  const duration = 3000; const interval = 50; const steps = duration / interval; const increment = 100 / steps;
  let currentStep = 0
  const progressInterval = setInterval(() => {
    currentStep++
    publishProgress.value = Math.min(100, currentStep * increment)
    if (currentStep >= steps) {
      clearInterval(progressInterval)
      setTimeout(() => { showSuccessModal.value = true; setTimeout(() => { isPublishing.value = false; publishProgress.value = 0; }, 300) }, 200)
    }
  }, interval)
}

const closeModal = () => { showSuccessModal.value = false }
const confirmJump = () => { router.push('/teacher/task-split') }
</script>

<style scoped>
.page-container { height: 100%; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; padding: 24px; }
.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }

.flex-1 { flex: 1; min-height: 0; } .flex-col { display: flex; flex-direction: column; } .flex-row { display: flex; align-items: center; } .flex-center { display: flex; align-items: center; justify-content: center; } .flex-between { display: flex; justify-content: space-between; align-items: center; }
.gap-2 { gap: 8px; } .gap-3 { gap: 12px; } .gap-4 { gap: 16px; } .gap-6 { gap: 24px; } .shrink-0 { flex-shrink: 0; }
.w-1-3 { width: 30%; } .w-1-5 { width: 6px; }
.p-4 { padding: 16px; }

.font-bold { font-weight: bold; } .text-sm { font-size: 14px; } .text-xs { font-size: 12px; }
.text-slate-800 { color: #1e293b; } .text-slate-600 { color: #475569; } .text-slate-500 { color: #64748b; }
.text-cyan { color: #06b6d4; } .text-green { color: #10b981; } .text-blue { color: #3b82f6; } .text-red { color: #ef4444; } .text-yellow { color: #f59e0b; } .text-purple { color: #8b5cf6; }
.bg-cyan { background: #06b6d4; } .bg-green { background: #10b981; } .bg-blue { background: #3b82f6; } .bg-red { background: #ef4444; } .bg-yellow { background: #f59e0b; } .bg-purple { background: #8b5cf6; }

.btn-icon { color: #06b6d4; background: transparent; border: none; cursor: pointer; transition: transform 0.3s; } .btn-icon:hover { transform: rotate(180deg); } .btn-icon:disabled { opacity: 0.5; cursor: not-allowed; }

.demand-box { background: rgba(255,255,255,0.5); border: 1px solid #e2e8f0; border-left-width: 4px; border-left-style: solid; border-radius: 8px; padding: 12px; }
.border-l-blue { border-left-color: #3b82f6; } .border-l-red { border-left-color: #ef4444; } .border-l-yellow { border-left-color: #f59e0b; } .border-l-purple { border-left-color: #8b5cf6; }

.main-task-box { background: white; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; font-size: 14px; color: #475569; line-height: 1.6; min-height: 70px; }
.grid-cols-2 { display: grid; grid-template-columns: 1fr 1fr; }

.sub-task-box { background: white; border: 1px solid; border-radius: 8px; padding: 12px; display: flex; flex-direction: column; transition: all 0.3s; }
.border-slate { border-color: #e2e8f0; } .border-blue { border-color: #3b82f6; } .border-red { border-color: #ef4444; } .border-yellow { border-color: #f59e0b; } .border-purple { border-color: #8b5cf6; }
.shadow-blue { box-shadow: 0 0 10px rgba(59,130,246,0.2); } .shadow-red { box-shadow: 0 0 10px rgba(239,68,68,0.2); } .shadow-yellow { box-shadow: 0 0 10px rgba(245,158,11,0.2); } .shadow-purple { box-shadow: 0 0 10px rgba(139,92,246,0.2); }
.tag-sm { font-size: 12px; padding: 2px 6px; border-radius: 4px; font-weight: bold; }
.sub-task-content { flex: 1; background: #f8fafc; border: 1px solid; border-radius: 6px; padding: 8px; position: relative; }
.border-slate-light { border-color: #e2e8f0; } .border-blue-light { border-color: #bfdbfe; } .border-red-light { border-color: #fecaca; } .border-yellow-light { border-color: #fde68a; } .border-purple-light { border-color: #e9d5ff; }
.custom-textarea { width: 100%; height: 100%; background: transparent; border: none; resize: none; outline: none; font-size: 13px; color: #475569; line-height: 1.5; }
.type-cursor { position: absolute; right: 12px; bottom: 12px; }

.btn-publish { display: flex; align-items: center; justify-content: center; padding: 12px 32px; border: 1px solid #06b6d4; border-radius: 8px; font-size: 16px; font-weight: bold; background: #06b6d4; color: white; cursor: pointer; position: relative; overflow: hidden; transition: all 0.2s; }
.btn-publish-active:hover { transform: scale(1.02); }
.btn-publish-disabled { opacity: 0.6; cursor: not-allowed; }
.btn-progress { position: absolute; left: 0; top: 0; bottom: 0; background: rgba(255,255,255,0.3); transition: width 0.1s linear; }

/* 弹窗与 Toast */
.toast-container { position: fixed; top: 80px; right: 24px; display: flex; flex-direction: column; gap: 12px; pointer-events: none; }
.toast-item { background: rgba(255,255,255,0.9); border: 1px solid #e2e8f0; box-shadow: 0 4px 6px rgba(0,0,0,0.1); padding: 12px 16px; border-radius: 8px; font-size: 14px; font-weight: bold; color: #1e293b; display: flex; align-items: center; backdrop-filter: blur(4px); }
.toast-dot { width: 10px; height: 10px; border-radius: 50%; margin-right: 12px; }

.deploy-mask { position: fixed; inset: 0; z-index: 999; display: flex; align-items: center; justify-content: center; background: rgba(255,255,255,0.8); backdrop-filter: blur(5px); }
.deploy-card { width: 500px; background: white; border: 1px solid #06b6d4; box-shadow: 0 0 30px rgba(6,182,212,0.2); border-radius: 16px; padding: 32px; display: flex; flex-direction: column; align-items: center; }
.spinner-xl { width: 64px; height: 64px; border: 4px solid #e2e8f0; border-top-color: currentColor; border-radius: 50%; animation: spin 1s linear infinite; }
.progress-bg { width: 100%; height: 8px; background: #e2e8f0; border-radius: 4px; overflow: hidden; margin-top: 16px; }
.progress-bar { height: 100%; transition: width 0.1s linear; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 50; }
.modal-card { width: 500px; background: white; border: 1px solid #06b6d4; border-radius: 16px; padding: 24px; position: relative; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
.modal-close { position: absolute; top: 12px; right: 12px; background: transparent; border: none; color: #94a3b8; cursor: pointer; } .modal-close:hover { color: #1e293b; }
.btn-cyan { background: #06b6d4; color: white; font-weight: bold; border: none; padding: 10px 24px; border-radius: 8px; cursor: pointer; transition: background 0.2s; } .btn-cyan:hover { background: #0891b2; }
.truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.custom-scrollbar::-webkit-scrollbar { width: 0; height: 0; }
.animate-pulse { animation: pulse 2s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }
@keyframes spin { 100% { transform: rotate(360deg); } }
.toast-enter-active, .toast-leave-active { transition: all 0.3s; }
.toast-enter-from { opacity: 0; transform: translateX(50px); }
.toast-leave-to { opacity: 0; transform: translateY(-20px); }
</style>