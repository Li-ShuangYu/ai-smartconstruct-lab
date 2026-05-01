<template>
  <div class="page-wrapper">
    <div class="ide-layout">
      <!-- 左侧：部署配置 -->
      <div class="vscode-panel deploy-config-panel">
        <div class="chat-header">
          <div class="chat-title">
            <svg class="icon icon-sm icon-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path></svg>
            <span>部署控制台</span>
          </div>
        </div>
        <div class="chat-body">
          <div class="config-card">
            <h4 class="label">待部署版本</h4>
            <div class="version-tag" :class="isState2 ? 'v-error' : 'v-primary'">
              {{ isState2 ? 'V2.0_Fixed' : 'V1.0.tar.gz' }}
            </div>
            <p class="desc">{{ isState2 ? '包含雷达与摄像头权限修复' : '标准加密通信模块' }}</p>
          </div>
          <div class="config-card">
            <h4 class="label">目标设备</h4>
            <div class="fake-select">冰达 NanoCar (192.168.1.100)</div>
          </div>
          <div class="flex-grow"></div>
          <button @click="startDeploy" :disabled="isDeploying" class="deploy-hero-btn" :class="isState2 ? 'btn-danger' : 'btn-success'">
            {{ isDeploying ? '正在传输...' : '建立连接并下发' }}
          </button>
        </div>
      </div>

      <!-- 右侧：硬件可视化 -->
      <div class="vscode-panel telemetry-panel">
        <div class="terminal-header">
          <div class="terminal-tabs"><div class="t-tab active">实时遥测</div></div>
        </div>
        <div class="telemetry-grid">
          <div class="stat-item"><span>CPU</span><strong>12%</strong></div>
          <div class="stat-item"><span>TEMP</span><strong :class="isState2 ? 'text-error' : ''">45°C</strong></div>
        </div>
        <div class="radar-view">
          <div class="radar-circle">
            <div class="radar-scanner"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 部署遮罩 -->
    <transition name="fade">
      <div v-if="isDeploying" class="modal-mask">
        <div class="vscode-panel terminal-window deploy-terminal">
           <div class="terminal-header"><span>SECURE_SSH_TRANSFER</span></div>
           <div class="terminal-body">
             <div v-for="log in deployLogs" class="term-line t-success">$ {{ log }}</div>
           </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const demoState = ref(1)
const isDeploying = ref(false)
const deployProgress = ref(0)
const deployLogs = ref([])

onMounted(() => {
  const state = localStorage.getItem('demo_state')
  demoState.value = state === '2' ? 2 : 1
})

const isState2 = computed(() => demoState.value === 2)

const startDeploy = () => {
  isDeploying.value = true
  deployLogs.value = []
  deployProgress.value = 0

  const sequence = [
    { time: 0, log: 'Initiating SSH connection to 192.168.1.100:22...' },
    { time: 300, log: 'Authentication successful. User: root. Target: NanoCar.' },
    { time: 600, log: 'Checking target environment dependencies...' },
    { time: 900, log: 'Preparing deployment directory: /opt/ros/workspace/src/' },
    { time: 1200, log: `Pushing payload ${isState2.value ? 'V2.0_Fixed.tar.gz' : 'V1.0.tar.gz'} [====>   ] 42.8MB...` },
    { time: 1600, log: 'Extracting files and resolving symlinks...' },
    { time: 1900, log: 'Applying execution permissions (+x)...' },
    { time: 2100, log: isState2.value ? 'WARNING: Patching previous vulnerable node...' : 'Configuring ROS launch files...' },
    { time: 2300, log: 'Restarting ROS Master & Cryptographic nodes...' },
    { time: 2500, log: 'Deployment completed successfully. Synchronizing state...' }
  ]

  sequence.forEach(step => {
    setTimeout(() => {
      deployLogs.value.push(step.log)
      deployProgress.value = (step.time / 2500) * 100
    }, step.time)
  })

  setTimeout(() => {
    router.push('/training/student-training/student-debug')
  }, 3000) 
}
</script>

<style scoped>
.page-container {height: 100%;min-height: 400px;padding: 0px; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; }
.layout-grid-deploy { display: grid; grid-template-columns: 400px 1fr; height: 100%; padding: 24px; gap: 24px; }

.glass-card { background: rgba(255,255,255,0.8); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; }
.border-top-thick { border-top-width: 4px; border-top-style: solid; }
.shadow-lg { box-shadow: 0 10px 15px -3px rgba(0,0,0,0.05); }

.flex-col { display: flex; flex-direction: column; } .flex-row { display: flex; align-items: center; } .flex-between { display: flex; justify-content: space-between; align-items: center; }
.flex-1 { flex: 1; }
.gap-1\.5 { gap: 6px; } .gap-2 { gap: 8px; } .gap-6 { gap: 24px; } .p-5 { padding: 20px; }

.card-header { padding: 16px 20px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.5); }
.card-body { padding: 24px; flex: 1; display: flex; flex-direction: column; }
.text-lg { font-size: 18px; } .text-sm { font-size: 14px; } .text-xs { font-size: 12px; }
.font-bold { font-weight: bold; } .font-mono { font-family: monospace; }
.uppercase { text-transform: uppercase; } .tracking-widest { letter-spacing: 0.1em; }

.text-slate-800 { color: #1e293b; } .text-slate-600 { color: #475569; } .text-slate-500 { color: #64748b; } .text-slate-400 { color: #94a3b8; }
.text-blue { color: #3b82f6; } .bg-blue { background: #3b82f6; } .border-blue { border-color: #3b82f6; } .hover\:bg-blue-dark:hover { background: #2563eb; } .border-blue-light { border-color: #bfdbfe; }
.text-red { color: #ef4444; } .bg-red { background: #ef4444; } .border-red { border-color: #ef4444; } .hover\:bg-red-dark:hover { background: #dc2626; } .border-red-light { border-color: #fecaca; }
.text-green { color: #10b981; } .bg-green { background: #10b981; } .bg-green-light { background: #dcfce7; }

.status-badge { font-size: 12px; padding: 4px 8px; border-radius: 4px; font-weight: bold; }
.badge-blue { background: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }
.badge-red { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
.tag-sm { padding: 2px 6px; border-radius: 4px; font-size: 11px; font-weight: bold; }

.fake-select { width: 100%; background: #f8fafc; border: 1px solid #cbd5e1; border-radius: 8px; padding: 12px; font-size: 14px; color: #334155; }

.btn-deploy { display: flex; align-items: center; justify-content: center; gap: 8px; width: 100%; padding: 16px; border: none; border-radius: 12px; color: white; font-size: 16px; font-weight: bold; cursor: pointer; transition: background 0.2s; }
.btn-disabled { background: #cbd5e1; cursor: not-allowed; }

.right-col { display: flex; flex-direction: column; flex: 1; border-radius: 16px; border: 1px solid #e2e8f0; background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); overflow: hidden; }
.stat-box { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.stat-label { font-size: 12px; color: #64748b; font-weight: bold; margin-bottom: 4px; }
.stat-val { font-size: 24px; font-weight: 900; font-family: monospace; color: #1e293b; line-height: 1; }

.radar-anim-wrapper { position: relative; width: 400px; height: 400px; display: flex; align-items: center; justify-content: center; }
.circle-dashed { position: absolute; inset: 0; border: 2px dashed; border-radius: 50%; animation: spin 20s linear infinite; }
.circle-solid { position: absolute; inset: 24px; border: 1px solid; border-radius: 50%; opacity: 0.5; }
.anim-svg { filter: drop-shadow(0 10px 15px rgba(0,0,0,0.1)); }

.floating-panel { position: absolute; background: rgba(255,255,255,0.8); border: 1px solid; border-radius: 8px; padding: 8px; font-family: monospace; font-size: 12px; color: #475569; backdrop-filter: blur(4px); }
.top-panel { top: 40px; right: -20px; }
.bottom-panel { bottom: 40px; left: -20px; }

.deploy-mask { position: absolute; inset: 0; background: rgba(15, 23, 42, 0.7); backdrop-filter: blur(8px); display: flex; align-items: center; justify-content: center; z-index: 50; }
.deploy-terminal { width: 600px; background: #0f172a; border: 1px solid; border-radius: 12px; overflow: hidden; display: flex; flex-direction: column; }
.deploy-header { padding: 8px 16px; display: flex; justify-content: space-between; align-items: center; }
.deploy-body { height: 260px; padding: 16px; overflow: hidden; display: flex; flex-direction: column; justify-content: flex-end; }
.deploy-footer { padding: 16px; }

.progress-bg-dark { height: 6px; background: #1e293b; border-radius: 3px; overflow: hidden; }
.progress-bar-glow { height: 100%; transition: width 0.3s linear; }
.shadow-blue { box-shadow: 0 0 10px #3b82f6; } .shadow-red { box-shadow: 0 0 10px #ef4444; }

.spinner { animation: spin 1s linear infinite; border: 3px solid rgba(255,255,255,0.3); border-top-color: white; border-radius: 50%; }
.animate-pulse { animation: pulse 2s infinite; }
.animate-fade-in-up { animation: fadeInUp 0.2s ease-out forwards; }

@keyframes spin { 100% { transform: rotate(360deg); } }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>