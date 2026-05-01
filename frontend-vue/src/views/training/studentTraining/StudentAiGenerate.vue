<template>
  <div class="page-wrapper">
    <div class="ide-layout">
      <!-- 左侧：代码预览区 -->
      <div class="editor-column">
        <div class="vscode-panel editor-panel">
          <div class="vscode-tabs-header">
            <div class="mac-controls">
              <div class="mac-dot dot-close"></div>
              <div class="mac-dot dot-min"></div>
              <div class="mac-dot dot-max"></div>
            </div>
            <div class="vscode-tab active">
              <svg class="icon icon-sm tab-icon-py" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15h-2v-2h2v2zm0-4h-2V7h2v6zm4 4h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
              generated_logic.py
            </div>
          </div>
          <div class="vscode-editor-body custom-scrollbar" ref="codeScrollRef">
            <div v-if="codeLines.length === 0" class="editor-empty">
              <svg class="icon icon-lg" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
              <span>等待 AI 规划任务...</span>
            </div>
            <div v-for="(line, idx) in codeLines" :key="idx" class="code-line">
              <div class="line-num">{{ idx + 1 }}</div>
              <div class="line-content" v-html="line.content"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：AI 生成控制台 -->
      <div class="vscode-panel chat-panel">
        <div class="chat-header">
          <div class="chat-title">
            <svg class="icon icon-sm icon-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 002-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
            <span>任务规划器</span>
          </div>
        </div>
        <div class="chat-body custom-scrollbar" ref="chatScrollRef">
          <div v-for="(block, idx) in chatBlocks" :key="idx" class="chat-block">
            <div :class="block.role === 'user' ? 'msg-user' : 'msg-ai'">
              <div v-if="block.type === 'agent'" class="agent-card">
                 <span>{{ block.filename }}</span>
                 <svg v-if="block.status === 'loading'" class="icon icon-sm spinner" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
              </div>
              <div v-else class="ai-text" v-html="block.content"></div>
            </div>
          </div>
        </div>
        <div class="chat-footer">
          <div class="input-container highlight-input">
            <textarea v-model="currentInput" rows="3" class="chat-textarea" placeholder="输入任务需求..."></textarea>
            <div class="input-actions">
              <span class="input-hints">Shift+Enter 换行</span>
              <button @click="handleSend" class="hero-send-btn">
                <svg class="icon icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M5 10l7-7m0 0l7 7m-7-7v18"></path></svg>
                发送需求
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const appState = ref(1)
const isGenerating = ref(false)
const canDeploy = ref(false)
const chatBlocks = ref([])
const currentInput = ref('')
const chatScrollRef = ref(null)
const codeScrollRef = ref(null)
const terminalScrollRef = ref(null)

const codeLines = ref([])
const terminalLogs = ref([])
const isTerminalRunning = ref(false)
const showDiffBar = ref(false)
const diffActionHandled = ref(false)

const delay = (ms) => new Promise(res => setTimeout(res, ms))

const scrollToBottom = async () => { await nextTick(); if (chatScrollRef.value) { chatScrollRef.value.scrollTop = chatScrollRef.value.scrollHeight } }
const scrollCodeToBottom = async () => { await nextTick(); if (codeScrollRef.value) { codeScrollRef.value.scrollTop = codeScrollRef.value.scrollHeight } }
const scrollToDiff = async () => { await nextTick(); if (codeScrollRef.value) { codeScrollRef.value.scrollTop = 1500 } }
const scrollTerminalToBottom = async () => { await nextTick(); if (terminalScrollRef.value) { terminalScrollRef.value.scrollTop = terminalScrollRef.value.scrollHeight } }

const streamRichText = async (htmlContent) => {
  const block = { role: 'ai', type: 'text', content: '', isTyping: true }
  chatBlocks.value.push(block)
  await scrollToBottom()
  
  const chunkSize = 15
  for (let i = 0; i < htmlContent.length; i += chunkSize) {
    block.content += htmlContent.slice(i, i + chunkSize)
    await delay(10)
    if (i % 60 === 0) await scrollToBottom()
  }
  block.content = htmlContent
  block.isTyping = false
  await scrollToBottom()
}

const streamCodeLines = async (linesToAppend, delayMs = 15) => {
  for (const line of linesToAppend) {
    codeLines.value.push(line)
    await delay(delayMs)
    if(codeLines.value.length % 5 === 0) await scrollCodeToBottom()
  }
  await scrollCodeToBottom()
}

const streamTerminal = async (logs, delayMs = 120) => {
  isTerminalRunning.value = true
  for (const log of logs) {
    terminalLogs.value.push(log)
    await delay(delayMs)
    await scrollTerminalToBottom()
  }
  isTerminalRunning.value = false
}

const pushAgent = async (filename, path, waitTime, addLines = 0, delLines = 0) => {
  const block = { role: 'ai', type: 'agent', filename, path, status: 'loading', add: addLines, del: delLines }
  chatBlocks.value.push(block)
  await scrollToBottom()
  await delay(waitTime)
  block.status = 'success'
  await scrollToBottom()
}

onMounted(() => {
  const savedState = localStorage.getItem('demo_state') || '1'
  appState.value = parseInt(savedState, 10)

  if (appState.value === 1) {
    currentInput.value = "请重新给我一个冰达机器人如何按迹寻路，并实时回传镜头拍摄的图像。要有完整详细的步骤，包括机器人端，电脑端和手机端分别都有哪些具体操作。"
  } else if (appState.value === 2) {
    codeLines.value = getV1FullCode()
    terminalLogs.value = getV1TerminalError()
    currentInput.value = "根据报错，雷达话题应为 /rplidar_scan，请优化代码并赋予摄像头 /dev/video0 权限"
    chatBlocks.value = [
      { role: 'user', content: "生成冰达机器人寻迹与图传的完整代码..." },
      { role: 'ai', type: 'text', content: "<p>已生成 <code>nanocar_track.py</code>，请运行查看。</p>" }
    ]
  } else if (appState.value === 3) {
    codeLines.value = getV2FullCode()
    terminalLogs.value = getV2TerminalSuccess()
    chatBlocks.value = [
      { role: 'user', content: "修复完成，确认采纳。" },
      { role: 'ai', type: 'text', content: "<p><b>已完成！</b>机器人目前正在按照既定路线寻迹前行。</p>" }
    ]
    canDeploy.value = true
  }
})

const handleSend = async () => {
  if (isGenerating.value) return
  const query = currentInput.value
  currentInput.value = ''
  isGenerating.value = true
  canDeploy.value = false
  chatBlocks.value.push({ role: 'user', content: query })
  await scrollToBottom()
  if (appState.value === 1) await runState1Sequence()
  else if (appState.value === 2) await runState2Sequence()
}

const runState1Sequence = async () => {
  await streamRichText("好的，开始生成代码...")
  await pushAgent("规划项目结构与环境", "", 1500)
  codeLines.value = [] 
  await streamCodeLines(getV1FullCode().slice(0, 45), 15)
  await streamRichText("正在编写核心的ROS寻迹节点逻辑...")
  await pushAgent("编写 ROS 寻迹底层代码", "nanocar_track.py", 2500, 120, 0)
  await streamCodeLines(getV1FullCode().slice(45, 140), 10)
  await streamRichText("正在补充摄像头图传与通信模块...")
  await pushAgent("编写图传代码", "nanocar_track.py", 2000, 80, 0)
  await streamCodeLines(getV1FullCode().slice(140), 10)
  await streamRichText("代码生成完毕，正在进行系统预检...")
  const checkAgent = { role: 'ai', type: 'agent', filename: 'AI 代码预检', path: '', status: 'loading' }
  chatBlocks.value.push(checkAgent)
  await scrollToBottom()
  await streamTerminal(getTerminalBuildLogs(), 150)
  checkAgent.status = 'success'
  await streamRichText("<p>已经生成了完整的代码，请去部署页面测试。</p>")
  isGenerating.value = false
  canDeploy.value = true
}

const runState2Sequence = async () => {
  await streamRichText("收到报错信息，正在分析错误原因...")
  await pushAgent("召回报错分析", "", 1800)
  await streamRichText("确认雷达话题名不匹配，正在为您修改代码...")
  await pushAgent("修改寻迹节点话题订阅", "nanocar_track.py", 2000, 3, 2)
  codeLines.value = getV2CodeDiff_Part1()
  await scrollToDiff()
  await streamRichText("同步排查关联的权限配置...")
  await pushAgent("补充设备 udev 权限代码", "nanocar_track.py", 2000, 7, 1)
  codeLines.value = getV2CodeDiff_Full()
  await scrollToDiff()
  await streamRichText("正在执行二次预检...")
  const checkAgent = { role: 'ai', type: 'agent', filename: 'AI 二次预检', path: '', status: 'loading' }
  chatBlocks.value.push(checkAgent)
  await scrollToBottom()
  await streamTerminal(getV2TerminalPrecheck(), 120) 
  checkAgent.status = 'success'
  showDiffBar.value = true
  await streamRichText("已修复问题，请确认是否采纳。")
  await scrollToDiff()
  isGenerating.value = false
}

const handleAcceptDiff = async () => {
  showDiffBar.value = false
  diffActionHandled.value = true
  codeLines.value = getV2FullCode() 
  isGenerating.value = true
  await pushAgent("应用变更并重载 ROS 环境", "", 1500)
  await streamTerminal([
    { text: '$ source devel/setup.bash', color: 'text-slate-600' },
    { text: '$ roslaunch nanocar_track start.launch', color: 'text-slate-600' },
    { text: '[INFO]: Setting /dev/video0 permissions...', color: 'text-green-600' },
    { text: '✅ [SYSTEM] All subsystems operational.', color: 'text-blue-600 font-bold' }
  ], 100)
  await streamRichText("<p>变更已应用，可随时打包代码。</p>")
  isGenerating.value = false
  canDeploy.value = true
}

const handleRejectDiff = () => {
  showDiffBar.value = false
  codeLines.value = getV1FullCode()
  isGenerating.value = false
}

const navigateToDeploy = () => { if (appState.value !== 3) { router.push('/training/student-training/student-deploy') } }

// ---------------- 浅色高亮与 Diff 样式计算 ----------------
const highlightedCode = computed(() => {
  if (!rawCode.value) return ''
  let html = rawCode.value
    .replace(/</g, "&lt;").replace(/>/g, "&gt;")
    .replace(/(#.*)/g, '<span style="color: #16a34a;">$1</span>') // 绿色注释
    .replace(/\b(import|from|class|def|if|try|except|pass|self|__init__|__name__|not|and|return)\b/g, '<span style="color: #2563eb;">$1</span>') // 蓝色关键字
    .replace(/(".*?"|'.*?')/g, '<span style="color: #b45309;">$1</span>') // 橙色字符串
    .replace(/\b([a-zA-Z_]\w*)(?=\()/g, '<span style="color: #7c3aed;">$1</span>') // 紫色函数名
    .replace(/\b(rospy|cv2|np|os)\b/g, '<span style="color: #0284c7;">$1</span>') // 青色库名
  return html
})

const getLineBgColor = (line) => {
  if (line.diffType === 'added') return 'bg-green-50 border-l-[3px] border-green-500'
  if (line.diffType === 'removed') return 'bg-red-50 border-l-[3px] border-red-500 line-through opacity-70'
  return 'border-l-[3px] border-transparent hover:bg-slate-50'
}
const getLineNumColor = (line) => {
  if (line.diffType === 'added') return 'text-green-600'
  if (line.diffType === 'removed') return 'text-red-600'
  return 'text-slate-400'
}
const getLineTextColor = (line) => {
  if (line.diffType === 'added') return 'text-green-700'
  if (line.diffType === 'removed') return 'text-red-700'
  return 'text-slate-700'
}

// ===== 模拟长篇代码生成库 (替换为带 html span 的纯文本数组) =====
const getV1FullCode = () => {
  const code = [
    { content: '<span style="color:#2563eb">#!/usr/bin/env python</span>' },
    { content: '<span style="color:#2563eb">import</span> rospy' },
    { content: '<span style="color:#2563eb">import</span> cv2' },
    { content: '<span style="color:#2563eb">import</span> numpy <span style="color:#2563eb">as</span> np' },
    { content: '<span style="color:#2563eb">class</span> <span style="color:#7c3aed">NanoCarTracker</span>:' },
    { content: '    <span style="color:#2563eb">def</span> <span style="color:#7c3aed">__init__</span>(<span style="color:#2563eb">self</span>):' },
    { content: '        rospy.init_node(<span style="color:#b45309">"nanocar_track_node"</span>)' },
    { content: '        <span style="color:#16a34a"># 错误订阅点：使用了默认的 /scan</span>' },
    { content: '        rospy.Subscriber(<span style="color:#b45309">"<span style="color:#dc2626; font-weight:bold">/scan</span>"</span>, LaserScan, <span style="color:#2563eb">self</span>.lidar_callback)' },
    { content: '    <span style="color:#2563eb">def</span> <span style="color:#7c3aed">init_hardware</span>(<span style="color:#2563eb">self</span>):' },
    { content: '        <span style="color:#16a34a"># 缺少权限授予代码</span>' },
    { content: '        rospy.loginfo(<span style="color:#b45309">"Checking camera /dev/video0 access..."</span>)' }
  ]
  for(let i=0; i<150; i++) code.push({ content: '        <span style="color:#16a34a"># Standard logic loop...</span>' })
  return code
}

const getV2CodeDiff_Part1 = () => {
  const code = getV1FullCode()
  code.splice(7, 2, 
    { content: '        <span style="color:#16a34a"># 错误订阅点：使用了默认的 /scan</span>', diffType: 'removed' },
    { content: '        rospy.Subscriber(<span style="color:#b45309">"<span style="color:#dc2626; font-weight:bold">/scan</span>"</span>, LaserScan, <span style="color:#2563eb">self</span>.lidar_callback)', diffType: 'removed' },
    { content: '        <span style="color:#16a34a"># 修改：使用真实的雷达话题</span>', diffType: 'added' },
    { content: '        rospy.Subscriber(<span style="color:#b45309">"/rplidar_scan"</span>, LaserScan, <span style="color:#2563eb">self</span>.lidar_callback)', diffType: 'added' }
  )
  return code
}

const getV2CodeDiff_Full = () => {
  const code = getV2CodeDiff_Part1()
  code.splice(12, 1, 
    { content: '        <span style="color:#16a34a"># 缺少权限授予代码</span>', diffType: 'removed' },
    { content: '        <span style="color:#16a34a"># 增加：赋予设备权限</span>', diffType: 'added' },
    { content: '        os.system(<span style="color:#b45309">"sudo chmod 777 /dev/video0"</span>)', diffType: 'added' }
  )
  return code
}

const getV2FullCode = () => { return getV2CodeDiff_Full().filter(c => c.diffType !== 'removed').map(c => ({ content: c.content })) }

// 终端颜色映射到浅色系
const getTerminalBuildLogs = () => [ { text: '$ catkin_make', color: 'text-slate-600' }, { text: '[100%] Built target', color: 'text-green-600' } ]
const getV2TerminalPrecheck = () => [ { text: 'Checking syntax...', color: 'text-slate-600' }, { text: '[OK] Syntax check passed.', color: 'text-green-600' } ]
const getV1TerminalError = () => [ { text: '[ERROR]: Cannot connect to /scan.', color: 'text-red-600 font-bold' }, { text: '[ERROR]: Permission denied: /dev/video0', color: 'text-red-600 font-bold' } ]
const getV2TerminalSuccess = () => [ { text: '[INFO]: Successfully subscribed to /rplidar_scan', color: 'text-green-600' } ]
</script>

<style scoped>
.page-container { height: 100%; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; }
.layout-grid-debug { display: grid; grid-template-columns: 2fr 1fr; gap: 24px; height: 100%; padding: 24px; }

/* ================= 左侧浅色 IDE区 ================= */
.code-terminal-col { display: flex; flex-direction: column; gap: 20px; overflow: hidden; }
.alert-success { background: #dcfce7; border: 1px solid #22c55e; color: #15803d; padding: 14px 20px; border-radius: 12px; font-weight: bold; font-size: 14px; display: flex; align-items: center; }
.alert-error { background: #fee2e2; border: 1px solid #ef4444; color: #b91c1c; padding: 14px 20px; border-radius: 12px; font-weight: bold; font-size: 14px; display: flex; align-items: center; }

/* 编辑器容器 */
.ide-window { flex: 2; display: flex; flex-direction: column; background: rgba(255,255,255,0.8); backdrop-filter: blur(10px); border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); border: 1px solid #e2e8f0; position: relative; }
.ide-header { background: rgba(255,255,255,0.5); display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid #e2e8f0; }
.flex-row { display: flex; align-items: center; } .gap-2 { gap: 8px; } .gap-3 { gap: 12px; }
.mac-dot { width: 12px; height: 12px; border-radius: 50%; }

/* Tab 样式 */
.ide-tabs { display: flex; margin-bottom: -13px; margin-left: 16px; }
.ide-tab { background: white; color: #334155; padding: 8px 16px; font-size: 13px; font-family: monospace; border-top: 2px solid #3b82f6; border-left: 1px solid #e2e8f0; border-right: 1px solid #e2e8f0; border-radius: 6px 6px 0 0; display: flex; align-items: center; font-weight: bold; }
.tab-close { color: #94a3b8; cursor: pointer; margin-left: 8px; font-weight: normal; } .tab-close:hover { color: #ef4444; }

.ide-actions { color: #64748b; }
.ide-breadcrumb { background: #f8fafc; color: #64748b; font-size: 12px; font-family: monospace; padding: 8px 16px; border-bottom: 1px solid #e2e8f0; }
.breadcrumb-sep { margin: 0 6px; color: #cbd5e1; }
.text-slate-600 { color: #475569; } .text-blue-600 { color: #2563eb; }

/* Diff 提示栏 */
.diff-bar { background: #eff6ff; border-bottom: 1px solid #3b82f6; padding: 10px 16px; display: flex; justify-content: space-between; align-items: center; }
.diff-msg { color: #1e293b; font-size: 13px; font-weight: bold; }
.kbd-light { background: white; border: 1px solid #cbd5e1; padding: 2px 6px; border-radius: 4px; font-size: 11px; margin-left: 6px; font-family: monospace; color: #64748b; }
.btn-diff-reject { background: white; color: #64748b; border: 1px solid #cbd5e1; padding: 6px 12px; border-radius: 6px; font-size: 13px; cursor: pointer; display: flex; align-items: center; transition: all 0.2s; }
.btn-diff-reject:hover { background: #f1f5f9; color: #1e293b; }
.btn-diff-accept { background: #3b82f6; color: white; border: none; padding: 6px 16px; border-radius: 6px; font-size: 13px; cursor: pointer; font-weight: bold; display: flex; align-items: center; transition: background 0.2s; }
.btn-diff-accept:hover { background: #2563eb; }

/* 代码区 */
.code-editor { flex: 1; overflow-y: auto; padding: 16px 0; background: white; font-family: monospace; font-size: 14px; line-height: 1.6; }
.code-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #94a3b8; font-weight: bold; letter-spacing: 1px; }
.code-line { display: flex; width: 100%; transition: background-color 0.1s; }
.line-num { width: 48px; text-align: right; padding-right: 16px; color: #94a3b8; user-select: none; flex-shrink: 0; }
.line-content { white-space: pre; padding-left: 8px; word-break: break-all; color: #334155; }

/* 终端面板 */
.terminal-window { flex: 1; display: flex; flex-direction: column; background: rgba(255,255,255,0.8); backdrop-filter: blur(10px); border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); border: 1px solid #e2e8f0; }
.terminal-header { background: #f8fafc; display: flex; justify-content: space-between; align-items: center; padding: 0 16px; border-bottom: 1px solid #e2e8f0; }
.terminal-tabs { display: flex; gap: 20px; }
.tab { padding: 10px 0; color: #64748b; font-size: 13px; cursor: pointer; font-weight: bold; }
.tab.active { color: #1e293b; border-bottom: 2px solid #3b82f6; }
.terminal-actions { padding-right: 8px; }
.terminal-body { flex: 1; overflow-y: auto; padding: 16px; font-family: monospace; font-size: 13px; line-height: 1.7; background: white; }
.cursor-blink { display: inline-block; width: 8px; height: 16px; background: #334155; animation: blink 1s step-end infinite; vertical-align: middle; margin-left: 4px; }
.shrink-0 { flex-shrink: 0; }

/* ================= 右侧白底毛玻璃聊天区 ================= */
.chat-col { display: flex; flex-direction: column; height: 100%; }
.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; display: flex; flex-direction: column; }
.shadow-lg { box-shadow: 0 10px 15px -3px rgba(0,0,0,0.05); }
.shadow-sm { box-shadow: 0 2px 4px rgba(0,0,0,0.02); }
.shadow-md { box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
.card-header { padding: 16px 20px; border-bottom: 1px solid #e2e8f0; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.flex-col { display: flex; flex-direction: column; } .flex-1 { flex: 1; }
.gap-4 { gap: 16px; } .p-4 { padding: 16px; } .p-5 { padding: 20px; }
.bg-white\/50 { background: rgba(255,255,255,0.5); } .bg-white\/60 { background: rgba(255,255,255,0.6); }

.chat-block { display: flex; flex-direction: column; width: 100%; margin-bottom: 24px; }
.chat-user { align-self: flex-end; max-width: 90%; }
.bubble-user { background: #f1f5f9; color: #1e293b; padding: 14px 18px; border-radius: 16px 16px 4px 16px; font-size: 14px; border: 1px solid #e2e8f0; line-height: 1.6; }
.chat-ai { align-self: flex-start; max-width: 95%; }

.agent-box { background: white; border: 1px solid #e2e8f0; border-radius: 10px; padding: 14px; margin-bottom: 12px; }
.agent-content { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.ai-text-content { font-size: 14px; color: #334155; line-height: 1.7; padding-left: 4px; }
/* 聊天框内的代码片段保持浅色风格的底色 */
.rich-text :deep(code) { background: #f1f5f9; padding: 2px 6px; border-radius: 4px; font-family: monospace; color: #dc2626; border: 1px solid #e2e8f0; }
.cursor-blink-dark { display: inline-block; width: 6px; height: 14px; background: #334155; animation: blink 1s step-end infinite; vertical-align: middle; margin-left: 4px; }

.input-wrapper { background: white; border: 1px solid #cbd5e1; border-radius: 12px; padding: 16px; transition: border-color 0.2s; }
.input-wrapper:focus-within { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.input-header { margin-bottom: 8px; }
.custom-textarea { width: 100%; border: none; outline: none; resize: none; font-size: 14px; color: #1e293b; background: transparent; line-height: 1.5; }
.btn-send { background: #3b82f6; color: white; border: none; padding: 8px 20px; border-radius: 8px; font-weight: bold; cursor: pointer; font-size: 13px; display: flex; align-items: center; transition: background 0.2s; }
.btn-send:hover:not(:disabled) { background: #2563eb; }
.btn-send:disabled { background: #cbd5e1; cursor: not-allowed; }
.btn-large { width: 100%; padding: 16px; border: none; border-radius: 12px; font-weight: bold; font-size: 15px; cursor: pointer; display: flex; justify-content: center; align-items: center; }

/* 实用类 */
.text-lg { font-size: 18px; } .text-sm { font-size: 14px; } .text-xs { font-size: 12px; }
.font-bold { font-weight: bold; } .font-mono { font-family: monospace; }
.text-slate-800 { color: #1e293b; } .text-slate-700 { color: #334155; } .text-slate-500 { color: #64748b; } .text-slate-400 { color: #94a3b8; }
.text-green { color: #10b981; } .text-red { color: #ef4444; }
.bg-green { background: #10b981; } .hover-bg-green-dark:hover { background: #059669; }
.bg-slate-200 { background: #e2e8f0; } .bg-slate-800 { background: #1e293b; }
.tag-outline { border: 1px solid; padding: 4px 8px; border-radius: 6px; font-size: 12px; font-weight: bold; background: white; }

.spinner { animation: spin 1s linear infinite; border-radius: 50%; }

.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 3px; }

.fade-in { animation: fadeIn 0.4s ease-out forwards; }
.fade-down-enter-active, .fade-down-leave-active { transition: all 0.4s ease; }
.fade-down-enter-from, .fade-down-leave-to { opacity: 0; transform: translateY(-10px); }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }
@keyframes spin { 100% { transform: rotate(360deg); } }
</style>