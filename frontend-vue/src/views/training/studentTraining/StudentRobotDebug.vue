<template>
  <div class="page-wrapper">
    <div class="ide-layout">
      <!-- 左侧：终端日志 -->
      <div class="editor-column">
        <div class="vscode-panel flex-1">
          <div class="panel-titlebar flex-between w-full">
            <span>system_diagnostics.log</span>
            <div class="header-badge">CPU: {{ cpuUsage }}% | MEM: 42%</div>
          </div>
          <div class="terminal-body-dark custom-scrollbar" ref="terminalRef">
            <div v-for="log in logs" :key="log.id" :class="['term-line', log.type]">
              <template v-if="log.type !== 'diff'">
                <span class="log-time" v-if="log.text">[{{ log.time }}]</span> {{ log.text }}
              </template>
              <div v-else class="patch-block">
                <div class="patch-header">// Patch applied to: /opt/ros/lidar_node.cpp</div>
                <div class="patch-del">- node->set_parameter("frame_id", "");</div>
                <div class="patch-add">+ node->set_parameter("frame_id", "laser_link");</div>
              </div>
            </div>
          </div>
          <div class="panel-footer">
            <div v-if="!isFatalTriggered && !isSuccessTriggered" class="loading-status">
              <svg class="icon icon-sm spinner mr-2" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
              系统启动中，等待模块连接...
            </div>
            <button v-else-if="demoState === '1'" @click="triggerAIFix" class="vscode-btn btn-error w-full flex-center">捕获异常：携带日志请求 AI 介入优化</button>
            <button v-else-if="demoState === '2'" @click="completeTask" class="vscode-btn btn-success w-full flex-center">调试成功：完成寻迹闭环任务</button>
          </div>
        </div>
      </div>

      <!-- 右侧：视频流与雷达 -->
      <div class="sidebar-column">
        <!-- 视频监控 -->
        <div class="vscode-panel flex-1">
          <div class="panel-titlebar flex-between w-full">
            <span>前置广角图传 (PORT 8080)</span>
            <span :class="['status-dot', demoState === '2' && isSuccessTriggered ? 'dot-active' : 'dot-error']"></span>
          </div>
          <div class="video-feed">
            <div v-if="demoState === '1' || !isSuccessTriggered" class="no-signal">NO SIGNAL</div>
            <div v-else class="live-stream">
              <div class="rec-indicator">REC [■]</div>
              <div class="track-overlay"></div>
              <div class="crosshair"></div>
            </div>
          </div>
        </div>
        
        <!-- 雷达点云 -->
        <div class="vscode-panel flex-1">
          <div class="panel-titlebar flex-between w-full">
            <span>2D 激光雷达 (/scan)</span>
            <span :class="['status-dot', demoState === '2' && isSuccessTriggered ? 'dot-active' : 'dot-error']"></span>
          </div>
          <div class="radar-feed">
             <div v-if="demoState === '1' || !isSuccessTriggered" class="radar-offline">
               <div class="offline-circle"></div>
             </div>
             <div v-else class="radar-active-box">
                <div class="radar-grid"></div>
                <div class="radar-sweep-lg"></div>
                <div v-for="pt in radarPoints" :key="pt.id" class="point-cloud" :style="{ transform: `translate(${pt.x}px, ${pt.y}px)`, opacity: pt.opacity }"></div>
             </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const demoState = ref(localStorage.getItem('demo_state') || '1')
const logs = ref([])
const terminalRef = ref(null)
const isFatalTriggered = ref(false)
const isSuccessTriggered = ref(false)
const cpuUsage = ref(12)
const radarPoints = ref([])
const autoScrollEnabled = ref(true) 

let logInterval, cpuInterval, radarInterval;

const getTimestamp = () => {
    const now = new Date(); return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}.${now.getMilliseconds().toString().padStart(3, '0')}`
}

const scrollToBottom = async () => { await nextTick(); if (terminalRef.value) terminalRef.value.scrollTop = terminalRef.value.scrollHeight }

const pushLog = (type, text, autoScroll = true) => {
    logs.value.push({ id: Date.now() + Math.random().toString().slice(2,8), type, time: getTimestamp(), text: text })
    if (logs.value.length > 300) logs.value.shift()
    if (autoScroll && autoScrollEnabled.value) scrollToBottom()
}

const startRadarSimulation = () => {
    radarInterval = setInterval(() => {
        const points = []; const numPoints = Math.floor(Math.random() * 4) + 3 
        for (let i = 0; i < numPoints; i++) {
            const angle = Math.random() * Math.PI * 2; const radius = Math.random() * 60 + 15 
            points.push({ id: i, x: Math.cos(angle) * radius, y: Math.sin(angle) * radius, opacity: Math.random() * 0.6 + 0.4 })
        }
        radarPoints.value = points
    }, 700)
}

const startCpuSim = () => { cpuInterval = setInterval(() => { cpuUsage.value = Math.floor(Math.random() * 15 + (demoState.value === '1' ? 80 : 15)) }, 2000) }

const startLogging = () => {
    pushLog('info', '[SYSTEM] Booting Robot Operating System 2 (Humble)...')
    pushLog('info', '[SYSTEM] Loading kernel modules...')
    setTimeout(() => pushLog('info', '[ROS_CORE] Starting roscore and DDS discovery...'), 500)
    setTimeout(() => pushLog('info', '[SENSORS] Probing peripheral devices on I2C/UART/USB buses...'), 1200)
    setTimeout(() => pushLog('info', '[CAMERA] Attempting to open video device /dev/video0...'), 1800)
    setTimeout(() => pushLog('info', '[LIDAR] Attempting to connect to RPLidar on /dev/ttyUSB0...'), 2300)
    setTimeout(() => { if (demoState.value === '1') triggerFailScenario(); else triggerSuccessScenario() }, 3500)
}

const triggerFailScenario = () => {
    isFatalTriggered.value = true
    pushLog('fatal', '[FATAL] Cannot find topic /scan. rplidar_super failed to match.')
    pushLog('error', '[NODE] process has died [pid 1421, exit code 255, cmd /opt/ros/lidar_node].')
    pushLog('warn', '[CAMERA] /dev/video0 device busy or resource temporarily unavailable.')

    const failMsgs = [
        '[WARN] Retrying to connect to /scan...',
        '[ERROR] Nodelet manager stack trace: Failed to find valid parameters for frame base_link.',
        '[WARN] TF_OLD_DATA ignoring data from the past.',
        '[ERROR] Could not grab frame from camera. V4L2 error 11.',
    ]
    logInterval = setInterval(() => {
        const msg = failMsgs[Math.floor(Math.random() * failMsgs.length)]
        pushLog(msg.includes('[ERROR]') ? 'error' : 'warn', msg)
    }, 700)
}

const triggerSuccessScenario = () => {
    isSuccessTriggered.value = true
    pushLog('info', '[AI_AGENT] 分析系统日志完成，定位到异常根因...')
    setTimeout(() => pushLog('info', '[AI_AGENT] 发现异常配置：/scan 节点依赖的坐标系参数丢失。'), 800)
    setTimeout(() => pushLog('info', '[AI_AGENT] 正在生成并应用热更新代码补丁...'), 1800)

    const diffLogId = Date.now() + Math.random().toString().slice(2,8);
    setTimeout(() => { logs.value.push({ id: diffLogId, type: 'diff', text: '' }) }, 2800)

    setTimeout(async () => {
        autoScrollEnabled.value = false; pushLog('success', '[SYSTEM] 变更已完成，请确认是否采纳。', false) 
        await nextTick(); const diffEl = document.getElementById('log-' + diffLogId)
        if (diffEl) terminalRef.value.scrollTo({ top: diffEl.offsetTop - 50, behavior: 'smooth' })
    }, 4000)

    setTimeout(() => { startRadarSimulation() }, 4500)

    const successMsgs = [
        `[INFO] [Odometry] Pose updated: [x: 1.2, y: 0.5, theta: 0.12]`,
        `[INFO] [Camera] Frame processed, current rate 30 FPS.`,
        `[INFO] [Lidar] Scan rate 10.2Hz, 384 points received.`,
        `[DEBUG] Navigation stack: tracking local trajectory...`
    ]
    logInterval = setInterval(() => {
        pushLog('info', successMsgs[Math.floor(Math.random() * successMsgs.length)])
    }, 800)
}

const triggerAIFix = () => { localStorage.setItem('demo_state', '2'); router.push('/student-debug/robot-debug') }
const completeTask = () => { localStorage.setItem('demo_state', '3'); router.push('/student-debug/robot-debug') }

onMounted(() => { startCpuSim(); startLogging() })
onUnmounted(() => { clearInterval(logInterval); clearInterval(cpuInterval); clearInterval(radarInterval) })
</script>

<style scoped>
:root {
  --vscode-bg: #ffffff; --vscode-panel-bg: #f3f3f3; --vscode-border: #e4e6e8;
  --vscode-text: #333333; --vscode-text-muted: #8a8a8a; --vscode-accent: #007acc;
  --vscode-success: #107c10; --vscode-error: #e51400;
}
.page-wrapper { padding: 16px; height: calc(100vh - 64px); box-sizing: border-box; background: var(--vscode-bg); font-family: -apple-system, sans-serif;}
.ide-layout { display: flex; gap: 16px; height: 100%; min-height: 0; }
.editor-column { flex: 2; display: flex; flex-direction: column; min-width: 0; }
.sidebar-column { flex: 1; display: flex; flex-direction: column; gap: 16px; }
.flex-col { display: flex; flex-direction: column; } .flex-1 { flex: 1; min-height: 0;} .flex-between { display: flex; justify-content: space-between; align-items: center; } .flex-center { display: flex; justify-content: center; align-items: center; } .w-full { width: 100%; }

/* Panels & Buttons */
.vscode-panel { background: var(--vscode-bg); border: 1px solid var(--vscode-border); border-radius: 4px; display: flex; flex-direction: column; overflow: hidden; box-shadow: 0 2px 6px rgba(0,0,0,0.03); }
.panel-titlebar { height: 35px; background: var(--vscode-panel-bg); border-bottom: 1px solid var(--vscode-border); display: flex; align-items: center; padding: 0 12px; font-size: 12px; font-weight: 600; color: #616161; }
.panel-footer { padding: 12px; border-top: 1px solid var(--vscode-border); background: var(--vscode-bg); }
.header-badge { font-family: monospace; font-size: 11px; background: #e4e6e8; padding: 2px 6px; border-radius: 4px; color: var(--vscode-text); }
.vscode-btn { padding: 10px 16px; font-size: 13px; font-weight: bold; border-radius: 2px; cursor: pointer; border: 1px solid transparent; transition: 0.2s; }
.btn-success { background: var(--vscode-success); color: white; border-color: #0b580b; } .btn-success:hover { background: #0b580b; }
.btn-error { background: var(--vscode-error); color: white; border-color: #a00e00; } .btn-error:hover { background: #bd1000; }
.loading-status { font-size: 13px; color: var(--vscode-text-muted); display: flex; align-items: center; justify-content: center; padding: 6px;}

/* Terminal */
.terminal-body-dark { background: #1e1e1e; color: #cccccc; flex: 1; overflow-y: auto; padding: 16px; font-family: Consolas, monospace; font-size: 13px; line-height: 1.6; }
.term-line { margin-bottom: 4px; word-break: break-all;}
.log-time { color: #858585; margin-right: 8px; }
.info { color: #cccccc; } .success { color: #89d185; } .error { color: #f48771; font-weight: bold; } .warn { color: #cca700; } .fatal { background: #f48771; color: white; padding: 0 4px; display: inline-block;}

/* Patch Block */
.patch-block { background: #2d2d2d; border: 1px solid #404040; border-radius: 4px; padding: 12px; margin: 12px 0; font-family: Consolas, monospace; }
.patch-header { color: #858585; margin-bottom: 8px; font-size: 12px; }
.patch-del { color: #f48771; background: rgba(244, 135, 113, 0.1); padding: 2px 8px; }
.patch-add { color: #89d185; background: rgba(137, 209, 133, 0.1); padding: 2px 8px; }

/* Video & Radar Feeds */
.video-feed, .radar-feed { flex: 1; background: #ececec; display: flex; align-items: center; justify-content: center; position: relative; overflow: hidden;}
.no-signal, .radar-offline { color: #a6a6a6; font-family: monospace; font-weight: bold; letter-spacing: 2px; }
.offline-circle { width: 100px; height: 100px; border-radius: 50%; border: 2px solid #a6a6a6; opacity: 0.3;}
.live-stream { position: absolute; inset: 0; background: #e4e6e8; }
.rec-indicator { position: absolute; top: 12px; left: 12px; color: var(--vscode-error); font-family: monospace; font-weight: bold; font-size: 12px; animation: pulse 1s infinite; }
.track-overlay { position: absolute; bottom: 0; left: 50%; transform: translateX(-50%) perspective(200px) rotateX(40deg); width: 80px; height: 120px; border-left: 2px dashed #007acc; border-right: 2px dashed #007acc; }
.crosshair { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 20px; height: 20px; border: 2px solid #007acc; border-radius: 50%; }

.radar-active-box { position: relative; width: 160px; height: 160px; border-radius: 50%; border: 1px solid #c9cbd0; background: #ffffff; overflow: hidden;}
.radar-grid { position: absolute; inset: 20px; border-radius: 50%; border: 1px dashed #c9cbd0; }
.radar-sweep-lg { position: absolute; width: 80px; height: 80px; right: 50%; bottom: 50%; transform-origin: bottom right; background: conic-gradient(from 0deg, transparent 70%, rgba(0, 122, 204, 0.2) 100%); animation: spin 2s linear infinite; }
.point-cloud { position: absolute; width: 4px; height: 4px; background: var(--vscode-accent); border-radius: 50%; top: 50%; left: 50%; }

/* Utils */
.icon { width: 16px; height: 16px; fill: none; stroke: currentColor; stroke-width: 2; } .icon-sm { width: 18px; height: 18px; }
.spinner { animation: spin 1s linear infinite; }
.status-dot { width: 10px; height: 10px; border-radius: 50%; } .dot-active { background: var(--vscode-success); animation: pulse 2s infinite; } .dot-error { background: var(--vscode-error); }
.custom-scrollbar::-webkit-scrollbar { width: 8px; height: 8px; } .custom-scrollbar::-webkit-scrollbar-thumb { background: #c1c1c1; } .terminal-body-dark::-webkit-scrollbar-thumb { background: #424242; }
@keyframes spin { 100% { transform: rotate(360deg); } } @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
</style>