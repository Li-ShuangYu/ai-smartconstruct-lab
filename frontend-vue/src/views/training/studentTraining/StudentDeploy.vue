<template>
  <div class="page-wrapper center-layout">
    <div class="vscode-panel deploy-card">
      <div class="panel-titlebar flex-center font-bold">
        部署中控台 (DE-OPS)
      </div>
      <div class="panel-body flex-col gap-6">
        
        <!-- Target Payload -->
        <div class="info-group">
          <div class="info-label">Target Payload</div>
          <div class="info-box">
            <div class="flex-row gap-2 font-bold mb-1" :class="isState2 ? 'text-error' : 'text-accent'">
              {{ isState2 ? 'V2.0_Fixed.tar.gz' : 'V1.0.tar.gz' }}
              <span class="badge" :class="isState2 ? 'badge-error' : 'badge-primary'">{{ isState2 ? 'HOTFIX' : 'STABLE' }}</span>
            </div>
            <div class="text-xs text-muted">{{ isState2 ? '[警告] 包含 1 处高优先级的漏洞修复（侧信道泄露修补），请尽快下发覆盖受感染节点。' : '基于最新 AI 推演生成的标准加密通信模块，已通过静态检查。' }}</div>
            <div class="flex-between text-xs font-mono mt-3 pt-2 border-t text-muted">
              <span>SIZE: 42.8 MB</span><span>CHECKSUM: a8f9c2...11b</span>
            </div>
          </div>
        </div>

        <!-- Target Device -->
        <div class="info-group">
          <div class="info-label">Target Device</div>
          <div class="device-box font-mono">冰达机器人 NanoCar (IP: 192.168.1.100)</div>
          <div class="text-xs mt-2 text-success flex-row gap-2 font-bold">
            <span class="status-dot"></span> SSH 端口已开启 (Port: 22)
          </div>
        </div>

        <!-- Action -->
        <button @click="startDeploy" :disabled="isDeploying" class="vscode-btn btn-primary btn-lg mt-2">
          <span v-if="!isDeploying" class="flex-row gap-2">
            <svg class="icon icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path></svg>
            建立连接并下发部署
          </span>
          <span v-else class="flex-row gap-2">
            <svg class="icon icon-sm spinner" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg> 
            系统接管中...
          </span>
        </button>

      </div>
    </div>

    <!-- 部署终端弹窗 -->
    <transition name="fade">
      <div v-if="isDeploying" class="modal-overlay">
        <div class="vscode-panel deploy-terminal">
          <div class="panel-titlebar flex-between">
            <span>SECURE_SHELL_DEPLOYMENT</span>
            <div class="mac-controls">
               <div class="mac-dot dot-close"></div><div class="mac-dot dot-min"></div><div class="mac-dot dot-max"></div>
            </div>
          </div>
          <div class="terminal-body-dark custom-scrollbar">
            <div v-for="(log, index) in deployLogs" :key="index" class="term-line animate-fade-in-up">
              <span class="text-muted">[{{ new Date().toISOString().substring(11, 19) }}]</span> 
              <span :class="log.includes('WARNING') || log.includes('ERROR') ? 't-error' : 't-success'">
                $ {{ log }}
              </span>
            </div>
          </div>
          <div class="progress-footer">
            <div class="flex-between text-xs font-mono mb-2"><span>PROGRESS</span><span>{{ deployProgress.toFixed(0) }}%</span></div>
            <div class="progress-track"><div class="progress-fill" :style="{ width: deployProgress + '%' }"></div></div>
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
  demoState.value = localStorage.getItem('demo_state') === '2' ? 2 : 1 
})
const isState2 = computed(() => demoState.value === 2)

const startDeploy = () => {
  isDeploying.value = true
  deployLogs.value = []
  deployProgress.value = 0
  
  // 完美保留了完整的部署动画逻辑与时间轴
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
      deployLogs.value.push(step.log); 
      deployProgress.value = (step.time / 2500) * 100 
    }, step.time)
  })

  // 原汁原味的路由跳转配置
  setTimeout(() => { 
    router.push('/training/student-training/student-debug') 
  }, 3000) 
}
</script>

<style scoped>
/* ================= VS Code Light 核心变量 ================= */
:root {
  --vscode-bg: #ffffff; 
  --vscode-panel-bg: #f3f3f3; 
  --vscode-border: #e4e6e8;
  --vscode-text: #333333; 
  --vscode-text-muted: #8a8a8a; 
  --vscode-accent: #007acc;
  --vscode-success: #107c10; 
  --vscode-error: #e51400;
}

.page-wrapper { 
  padding: 16px; 
  height: calc(100vh - 64px); 
  box-sizing: border-box; 
  background: var(--vscode-bg); 
  color: var(--vscode-text); 
  font-family: -apple-system, sans-serif;
}
.center-layout { 
  display: flex; 
  align-items: center; 
  justify-content: center; 
}

/* ================= Panel 骨架 ================= */
.vscode-panel { 
  background: var(--vscode-bg); 
  border: 1px solid var(--vscode-border); 
  border-radius: 4px; 
  display: flex; 
  flex-direction: column; 
  overflow: hidden; 
  box-shadow: 0 4px 12px rgba(0,0,0,0.08); 
}
.panel-titlebar { 
  height: 35px; 
  background: var(--vscode-panel-bg); 
  border-bottom: 1px solid var(--vscode-border); 
  display: flex; 
  align-items: center; 
  padding: 0 16px; 
  font-size: 12px; 
  color: #616161; 
  font-family: monospace;
}
.flex-center { justify-content: center; }
.panel-body { padding: 24px; width: 420px; }
.flex-col { display: flex; flex-direction: column; } 
.flex-row { display: flex; align-items: center; } 
.flex-between { display: flex; justify-content: space-between; align-items: center; } 
.gap-2 { gap: 8px; } 
.gap-6 { gap: 24px; } 
.mt-2 { margin-top: 8px; } 
.mt-3 { margin-top: 12px; }

/* ================= 信息展示区 ================= */
.info-label { font-size: 11px; font-weight: bold; color: var(--vscode-text-muted); text-transform: uppercase; margin-bottom: 8px; }
.info-box { border: 1px solid var(--vscode-border); border-radius: 2px; padding: 12px; background: #fafafa; }
.device-box { background: #f3f3f3; padding: 10px 12px; border: 1px solid var(--vscode-border); border-radius: 2px; font-size: 13px; }

/* ================= 排版与色彩 ================= */
.text-xs { font-size: 12px; } 
.text-sm { font-size: 13px; } 
.font-bold { font-weight: bold; } 
.font-mono { font-family: Consolas, monospace; }
.text-muted { color: var(--vscode-text-muted); } 
.text-accent { color: var(--vscode-accent); } 
.text-error { color: var(--vscode-error); } 
.text-success { color: var(--vscode-success); }
.border-t { border-top: 1px solid var(--vscode-border); }

/* Badges & 状态灯 */
.badge { padding: 2px 6px; border-radius: 2px; font-size: 10px; font-weight: bold; color: white; }
.badge-primary { background: var(--vscode-accent); } 
.badge-error { background: var(--vscode-error); animation: pulse 2s infinite; }
.status-dot { width: 8px; height: 8px; background: var(--vscode-success); border-radius: 50%; animation: pulse 2s infinite;}

.mac-controls { display: flex; gap: 6px; }
.mac-dot { width: 10px; height: 10px; border-radius: 50%; } 
.dot-close { background: #ff5f56; } .dot-min { background: #ffbd2e; } .dot-max { background: #27c93f; }

/* ================= 主按钮 ================= */
.vscode-btn { 
  padding: 8px 16px; 
  font-size: 13px; 
  border-radius: 2px; 
  cursor: pointer; 
  border: 1px solid transparent; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  transition: 0.2s; 
}
.btn-primary { 
  background: var(--vscode-accent); 
  color: white; 
  border-color: #005a9e; 
  font-weight: bold;
}
.btn-primary:hover:not(:disabled) { background: #0062a3; }
.btn-primary:disabled { 
  background: var(--vscode-border); 
  color: var(--vscode-text-muted); 
  border-color: var(--vscode-border); 
  cursor: not-allowed; 
}
.btn-lg { padding: 12px 24px; font-size: 14px; }

/* ================= 终端遮罩与动画 ================= */
.modal-overlay { 
  position: fixed; 
  inset: 0; 
  background: rgba(255,255,255,0.7); 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  z-index: 50; 
}
.deploy-terminal { 
  width: 550px; 
  height: 380px; 
  box-shadow: 0 8px 24px rgba(0,0,0,0.15); 
}
.terminal-body-dark { 
  background: #1e1e1e; 
  color: #cccccc; 
  flex: 1; 
  overflow-y: auto; 
  padding: 16px; 
  font-family: Consolas, monospace; 
  font-size: 13px; 
  line-height: 1.6; 
}
.term-line { margin-bottom: 4px; }
.t-error { color: #f48771; font-weight: bold; }
.t-success { color: #89d185; }

.progress-footer { 
  padding: 16px; 
  border-top: 1px solid var(--vscode-border); 
  background: var(--vscode-panel-bg); 
}
.progress-track { 
  width: 100%; 
  height: 6px; 
  background: #d1d5db; 
  border-radius: 3px; 
  overflow: hidden; 
}
.progress-fill { 
  height: 100%; 
  background: var(--vscode-accent); 
  transition: width 0.3s; 
}

/* Utils */
.icon { flex-shrink: 0; display: inline-block; vertical-align: middle; fill: none; stroke: currentColor; stroke-width: 2; } 
.icon-sm { width: 16px; height: 16px; }
.spinner { animation: spin 1s linear infinite; }
.custom-scrollbar::-webkit-scrollbar { width: 8px; } 
.custom-scrollbar::-webkit-scrollbar-thumb { background: #424242; }

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
@keyframes spin { 100% { transform: rotate(360deg); } }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

.animate-fade-in-up { animation: fadeInUp 0.2s ease-out forwards; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; } 
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>