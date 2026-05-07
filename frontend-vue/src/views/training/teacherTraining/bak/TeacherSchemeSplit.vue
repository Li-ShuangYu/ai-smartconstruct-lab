<template>
  <div class="page-container">
    <header class="page-header">
      <div class="header-left">
        <div class="status-indicator bg-green"></div>
        <h1 class="page-title">无人机通信加密 - 4 组方案分屏</h1>
      </div>
      <div class="header-right flex-row gap-3">
        <button 
          @click="goToAiEvaluate" 
          :disabled="!allSchemesUploaded"
          class="btn-agent"
          :class="{'btn-agent-active': allSchemesUploaded, 'btn-agent-disabled': !allSchemesUploaded}"
        >
          <svg class="icon-sm mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path></svg>
          {{ allSchemesUploaded ? '架构评审 Agent' : '等待所有组上传方案...' }}
        </button>
        <button disabled class="btn-listening">
          <div class="listening-dot"></div>
          方案自动监听中...
        </button>
        <button class="btn-primary">
          <svg class="icon-sm mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path></svg>
          导出方案
        </button>
      </div>
    </header>

    <main class="main-content layout-grid-2x2">
      <template v-for="(group, index) in allGroupData" :key="group.id">
        <!-- 等待上传状态 -->
        <div v-if="!uploadedStates[index]" class="glass-card flex-center">
          <div class="waiting-animation">
            <div class="spinner"></div>
            <div class="waiting-text text-green">
              <span v-for="(char, charIndex) in '等待学生提交方案。。。'" :key="charIndex" class="waiting-char" :style="{ animationDelay: `${charIndex * 0.1 + (index + 1) * 0.2}s` }">{{ char }}</span>
            </div>
            <div class="text-sm font-bold text-slate-500">第 {{ index + 1 }} 组</div>
          </div>
        </div>

        <!-- 已上传状态 -->
        <div v-else class="glass-card flex-col slide-in">
          <div class="card-header flex-between border-b" :style="{ borderBottomColor: group.color + '40', backgroundColor: group.color + '0a' }">
            <h2 class="font-bold flex-row gap-2 text-lg" :style="{ color: group.color }">
              <span class="tag-sm" :style="{ backgroundColor: group.color + '20' }">{{ group.name }}</span> 
              {{ group.title }}
            </h2>
            <span class="tag-success">已提交锁定</span>
          </div>

          <div class="card-body flex-row gap-4 p-4 min-h-0">
            <div class="file-box">
              <div class="file-icon-wrapper">
                <svg style="width: 56px; height: 56px; color: #2563eb;" viewBox="0 0 24 24" fill="currentColor"><path d="M4 2.992C4 2.444 4.448 2 5.006 2h9.988a1 1 0 01.708.293l4 4a1 1 0 01.298.71V21.01C20 21.556 19.552 22 18.994 22H5.006A.996.996 0 014 21.008V2.992z"/></svg>
                <div class="file-w-text">W</div>
              </div>
              <span class="file-name">{{ group.fileName }}</span>
              <span class="text-xs text-slate-400 mt-2">.docx 文档</span>
            </div>

            <div class="scheme-list custom-scrollbar">
              <div v-for="(scheme, sIndex) in group.schemes" :key="sIndex" class="scheme-item fade-in-up" :style="{ animationDelay: `${sIndex * 0.15}s` }">
                <div class="text-xs text-slate-500 mb-2">方案名称：<span class="font-bold text-slate-800">{{ scheme.name }}</span></div>
                <div class="text-sm text-slate-600 mb-3 leading-relaxed">{{ scheme.content }}</div>
                <div class="flex justify-end border-t border-slate-100 pt-2"><button class="btn-text">查看详情</button></div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
// [SCRIPT 代码原封不动，完美保留，与源文件17一致]
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const uploadedStates = ref([false, false, false, false]);
const allSchemesUploaded = computed(() => uploadedStates.value.every(state => state === true));
let pollingTimer = null;

const allGroupData = [
  { id: 1, name: '第一组', title: '低功耗优化方案', color: '#3b82f6', fileName: '无人机present轻量级密码系统设计.docx', schemes: [ { name: '低功耗优化方案', content: '采用 PRESENT 轻量级加密算法，结合简化密钥更新流程，有效降低系统功耗，适合无人机等嵌入式设备。' }, { name: '密钥管理优化方案', content: '采用分层密钥管理策略，结合预共享密钥和会话密钥，减少密钥更新频率，降低功耗。' } ] },
  { id: 2, name: '第二组', title: '侧信道防护方案', color: '#ef4444', fileName: '侧信道是AES+一阶掩码组合方案.docx', schemes: [ { name: '侧信道防护方案', content: '采用掩码技术和随机化执行路径，有效防护功耗分析和时序攻击，保障密码运算的物理安全性。' }, { name: '防重放攻击机制', content: '基于时间戳和挑战-响应机制，实现高效的防重放攻击保护。' } ] },
  { id: 3, name: '第三组', title: '抗重放攻击方案', color: '#f59e0b', fileName: '通信安全+抗重放攻击组合设计.docx', schemes: [ { name: '抗重放攻击方案', content: '基于动态随机数和序列号机制，确保抗重放攻击防护的准确性，防止恶意重放攻击。' }, { name: '身份认证机制', content: '强化身份认证流程，避免非法设备接入通信链路，提升系统整体安全性。' } ] },
  { id: 4, name: '第四组', title: '后量子算法适配方案', color: '#8b5cf6', fileName: '基于后量子密码的无人机密码系统设计.docx', schemes: [ { name: '后量子算法适配方案', content: '评估后量子加密算法的算力消耗与兼容性，简化算法适配流程，降低对无人机的算力负担。' }, { name: '低算力优化', content: '针对后量子算法进行轻量化优化，确保在有限算力条件下的有效运行。' } ] }
];

const goToAiEvaluate = async () => {
  try { await fetch('/api/state/update', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ai_evaluated: 1 }) }); } catch (error) {}
  router.push('/teacher/ai-evaluate');
};

const resetBackendState = async () => {
  try { await fetch('/api/state/update', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ scheme_uploaded_g1: 0, scheme_uploaded_g2: 0, scheme_uploaded_g3: 0, scheme_uploaded_g4: 0 }) }); } catch (error) {}
};

const fetchState = async () => {
  try {
    const res = await fetch('/api/state'); const state = await res.json();
    uploadedStates.value[0] = state.scheme_uploaded_g1 === 1;
    uploadedStates.value[1] = state.scheme_uploaded_g2 === 1;
    uploadedStates.value[2] = state.scheme_uploaded_g3 === 1;
    uploadedStates.value[3] = state.scheme_uploaded_g4 === 1;
  } catch (error) {}
};

onMounted(() => { resetBackendState(); fetchState(); pollingTimer = setInterval(fetchState, 1000); });
onUnmounted(() => { if (pollingTimer) clearInterval(pollingTimer); });
</script>

<style scoped>
.page-container { height: 100%; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; }
.page-header { flex-shrink: 0; height: 64px; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); z-index: 10; }
.header-left { display: flex; align-items: center; gap: 12px; }
.status-indicator { width: 6px; height: 24px; border-radius: 4px; }
.page-title { font-size: 18px; font-weight: 800; margin: 0; }
.header-right { display: flex; align-items: center; }
.flex-row { display: flex; align-items: center; } .flex-col { display: flex; flex-direction: column; } .flex-center { display: flex; align-items: center; justify-content: center; } .flex-between { display: flex; justify-content: space-between; align-items: center; }
.gap-2 { gap: 8px; } .gap-3 { gap: 12px; } .gap-4 { gap: 16px; }

.bg-green { background-color: #10b981; } .text-green { color: #10b981; }
.btn-primary { background: #10b981; color: white; padding: 8px 24px; border: none; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; display: flex; align-items: center; transition: background 0.2s; }
.btn-primary:hover { background: #059669; }
.btn-listening { background: white; color: #64748b; padding: 8px 16px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; font-weight: bold; display: flex; align-items: center; cursor: not-allowed; }
.listening-dot { width: 8px; height: 8px; background-color: #10b981; border-radius: 50%; margin-right: 8px; animation: pulse 2s infinite; }
.btn-agent { padding: 8px 24px; border-radius: 8px; font-size: 14px; font-weight: bold; display: flex; align-items: center; transition: all 0.3s; border: none; }
.btn-agent-active { background: white; color: #06b6d4; border: 1px solid #06b6d4; cursor: pointer; box-shadow: 0 0 10px rgba(6,182,212,0.2); }
.btn-agent-active:hover { background: #06b6d4; color: white; box-shadow: 0 0 15px rgba(6,182,212,0.4); }
.btn-agent-disabled { background: #f1f5f9; color: #94a3b8; border: 1px solid #e2e8f0; cursor: not-allowed; }

.main-content { flex: 1; padding: 24px; overflow: hidden; }
.layout-grid-2x2 { display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: 24px; height: 100%; }
.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; }

.waiting-animation { display: flex; flex-direction: column; align-items: center; gap: 24px; position: relative; }
.spinner { width: 64px; height: 64px; border: 4px solid #f1f5f9; border-top-color: currentColor; border-radius: 50%; animation: spin 1s linear infinite; }
.waiting-text { font-size: 18px; font-weight: bold; display: flex; gap: 2px; }
.waiting-char { display: inline-block; animation: charJump 2s ease-in-out infinite; opacity: 0.6; }

.card-header { padding: 12px 20px; }
.text-lg { font-size: 18px; } .font-bold { font-weight: bold; }
.tag-sm { padding: 4px 8px; border-radius: 6px; font-size: 12px; font-weight: bold; }
.tag-success { background: #dcfce7; color: #047857; padding: 4px 8px; border-radius: 6px; font-size: 12px; font-weight: bold; border: 1px solid #bbf7d0; }

.card-body { flex: 1; min-height: 0; align-items: stretch; }
.file-box { width: 140px; display: flex; flex-direction: column; align-items: center; justify-content: center; background: white; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px; cursor: pointer; transition: all 0.2s; flex-shrink: 0; }
.file-box:hover { border-color: #3b82f6; box-shadow: 0 4px 12px rgba(59,130,246,0.1); }
.file-icon-wrapper { position: relative; margin-bottom: 12px; }
.file-w-text { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 20px; margin-top: 8px; }
.file-name { font-size: 13px; font-weight: bold; color: #475569; text-align: center; word-break: break-all; line-height: 1.4; transition: color 0.2s; }
.file-box:hover .file-name { color: #1e293b; }

.scheme-list { flex: 1; display: flex; flex-direction: column; gap: 12px; overflow-y: auto; padding-right: 8px; }
.scheme-item { background: white; border: 1px solid #e2e8f0; border-radius: 10px; padding: 16px; transition: border-color 0.2s; }
.scheme-item:hover { border-color: #cbd5e1; }
.btn-text { color: #10b981; background: transparent; border: none; font-size: 13px; font-weight: bold; cursor: pointer; }
.btn-text:hover { color: #059669; text-decoration: underline; }

.icon-sm { width: 20px; height: 20px; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; } .custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 3px; }
@keyframes spin { 100% { transform: rotate(360deg); } }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }
@keyframes charJump { 0%, 100% { transform: translateY(0); opacity: 0.6; } 25% { transform: translateY(-8px); opacity: 1; } 50% { transform: translateY(0); opacity: 0.8; } }
.slide-in { animation: slideIn 0.5s ease-out forwards; } @keyframes slideIn { from { opacity: 0; transform: translateY(20px) scale(0.98); } to { opacity: 1; transform: translateY(0) scale(1); } }
.fade-in-up { animation: fadeInUp 0.4s ease-out forwards; opacity: 0; } @keyframes fadeInUp { to { opacity: 1; transform: translateY(0); } }
</style>