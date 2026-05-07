<template>
  <div class="page-wrapper">
    <div class="ide-layout">
      
      <!-- ================= 左侧：代码编辑与终端 ================= -->
      <div class="editor-column">
        
        <!-- 顶部提示条 -->
        <transition name="fade-down">
          <div v-if="appState === 3" class="vscode-alert alert-success">
            <svg class="icon icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            <span>闭环验证已完成，机器人当前运行状态良好，按迹寻路与图传均无异常</span>
          </div>
        </transition>
        <transition name="fade-down">
          <div v-if="appState === 2 && !diffActionHandled" class="vscode-alert alert-error">
            <svg class="icon icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            <span>Run Error: [rospy.exceptions.ROSException] Failed to subscribe to topic '/scan'. Device permission denied.</span>
          </div>
        </transition>

        <!-- 编辑器主体 -->
        <div class="vscode-panel editor-panel">
          <div class="vscode-tabs-header">
            <div class="mac-controls">
              <div class="mac-dot dot-close"></div><div class="mac-dot dot-min"></div><div class="mac-dot dot-max"></div>
            </div>
            <div class="tabs-scroll-area">
              <div class="vscode-tab active">
                <svg class="icon icon-sm tab-icon-py" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15h-2v-2h2v2zm0-4h-2V7h2v6zm4 4h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
                nanocar_track.py<span class="tab-close">×</span>
              </div>
            </div>
            <div class="editor-actions">
              <svg class="icon icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h.01M12 12h.01M19 12h.01M6 12a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0z"></path></svg>
            </div>
          </div>
          
          <div class="vscode-breadcrumbs">
            src <span class="sep">></span> nanocar_ros <span class="sep">></span> scripts <span class="sep">></span> 
            <span class="file-name">nanocar_track.py</span> <span class="sep">></span> <span class="class-name">NanoCarTracker</span>
          </div>

          <transition name="fade">
            <div v-if="showDiffBar" class="vscode-diff-bar">
              <div class="diff-msg">
                <svg class="icon icon-sm icon-success" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                AI 变更已完成，请确认是否采纳。
              </div>
              <div class="diff-actions">
                <button @click="handleRejectDiff" class="vscode-btn btn-secondary">拒绝</button>
                <button @click="handleAcceptDiff" class="vscode-btn btn-primary">保留变更 (Accept)</button>
              </div>
            </div>
          </transition>

          <div class="vscode-editor-body custom-scrollbar" ref="codeScrollRef">
            <div v-if="codeLines.length === 0" class="editor-empty">
              <svg class="icon icon-lg" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15h-2v-2h2v2zm0-4h-2V7h2v6zm4 4h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
              <span>等待 Builder 生成代码...</span>
            </div>
            
            <div v-for="(line, idx) in codeLines" :key="idx" :class="['code-line', line.diffType ? `line-${line.diffType}` : '']">
              <div class="line-num">{{ line.diffType === 'added' ? '+' : line.diffType === 'removed' ? '-' : (idx + 1) }}</div>
              <div class="line-content" v-html="line.content"></div>
            </div>
          </div>
        </div>

        <div class="vscode-panel terminal-panel">
          <div class="terminal-header">
            <div class="terminal-tabs">
              <div class="t-tab">问题</div><div class="t-tab">输出</div><div class="t-tab">调试控制台</div><div class="t-tab active">终端</div>
            </div>
            <div class="terminal-actions"><span class="badge-bash">bash</span><span class="terminal-close">×</span></div>
          </div>
          <div class="terminal-body custom-scrollbar" ref="terminalScrollRef">
            <div v-for="(log, idx) in terminalLogs" :key="idx" :class="['term-line', log.type]">{{ log.text }}</div>
            <div v-if="isTerminalRunning" class="cursor-blink"></div>
          </div>
        </div>
      </div>

      <!-- ================= 右侧：AI 对话 ================= -->
      <div class="vscode-panel chat-panel">
        <div class="chat-header">
          <div class="chat-title">
            <svg class="icon icon-sm icon-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 10l-2 1m0 0l-2-1m2 1v2.5M20 7l-2 1m2-1l-2-1m2 1v2.5M14 4l-2-1-2 1M4 7l2-1M4 7l2 1M4 7v2.5M12 21l-2-1m2 1l2-1m-2 1v-2.5M6 18l-2-1v-2.5M18 18l2-1v-2.5"></path></svg>
            <span>AI Builder</span>
          </div>
          <span class="chat-tag">CODE AGENT</span>
        </div>

        <div class="chat-body custom-scrollbar" ref="chatScrollRef">
          <div v-for="(block, idx) in chatBlocks" :key="idx" class="chat-block">
            <div v-if="block.role === 'user'" class="msg-user"><div class="bubble-user">{{ block.content }}</div></div>
            <div v-else class="msg-ai">
              <div v-if="block.type === 'agent'" class="agent-card">
                <div class="agent-info">
                  <svg class="icon icon-sm icon-warning" viewBox="0 0 24 24" fill="currentColor"><path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"/></svg>
                  <span class="agent-filename">{{ block.filename }}</span>
                </div>
                <div class="agent-status">
                  <div class="agent-diff">
                    <span v-if="block.status === 'success' && block.add > 0" class="diff-add">+{{ block.add }}</span>
                    <span v-if="block.status === 'success' && block.del > 0" class="diff-del">-{{ block.del }}</span>
                  </div>
                  <svg v-if="block.status === 'loading'" class="icon icon-sm icon-accent spinner" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                  <svg v-else-if="block.status === 'success'" class="icon icon-sm icon-success" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path></svg>
                </div>
              </div>
              <div v-if="block.type === 'text'" class="ai-text">
                <div v-html="block.content" class="rich-text-content"></div>
                <span v-if="block.isTyping" class="cursor-blink"></span>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-footer">
          <div class="input-container">
            <textarea v-model="currentInput" :disabled="isGenerating" rows="3" class="chat-textarea custom-scrollbar" placeholder="询问 @Builder 或输入调整指令..."></textarea>
            <div class="input-actions">
              <div class="input-hints"><span class="kbd">Shift</span> + <span class="kbd">Enter</span> 换行</div>
              <button @click="handleSend" :disabled="isGenerating || !currentInput.trim()" class="vscode-btn btn-primary btn-sm">
                <svg class="icon icon-xs" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"></path></svg>
                发送
              </button>
            </div>
          </div>
          <button @click="navigateToDeploy" :disabled="!canDeploy" :class="['deploy-hero-btn', canDeploy ? 'deploy-active' : 'deploy-disabled']">
            <svg class="icon icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"></path></svg>
            打包至部署页
          </button>
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
const scrollToBottom = async () => { await nextTick(); if (chatScrollRef.value) chatScrollRef.value.scrollTop = chatScrollRef.value.scrollHeight }
const scrollCodeToBottom = async () => { await nextTick(); if (codeScrollRef.value) codeScrollRef.value.scrollTop = codeScrollRef.value.scrollHeight }
const scrollToDiff = async () => { await nextTick(); if (codeScrollRef.value) codeScrollRef.value.scrollTop = 1500 }
const scrollTerminalToBottom = async () => { await nextTick(); if (terminalScrollRef.value) terminalScrollRef.value.scrollTop = terminalScrollRef.value.scrollHeight }

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
      { role: 'ai', type: 'text', content: "<p>已生成 <code>nanocar_track.py</code>，包含完整的 PID 寻迹逻辑与 Web 图传推流。请运行查看。</p>" }
    ]
  } else if (appState.value === 3) {
    codeLines.value = getV2FullCode()
    terminalLogs.value = getV2TerminalSuccess()
    chatBlocks.value = [
      { role: 'user', content: "修复完成，确认采纳。" },
      { role: 'ai', type: 'text', content: "<p><b>已完成！</b>雷达话题与摄像头权限已修正，编译通过。机器人目前正在按照既定路线寻迹前行，手机端与电脑端图传均已连通。</p>" }
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
  await streamRichText("好的，我将为您梳理冰达机器人按迹寻路及图传的完整步骤，并开始生成代码...")
  await pushAgent("规划项目结构与环境", "", 1500)
  codeLines.value = [] 
  await streamCodeLines(getV1FullCode().slice(0, 45), 15)

  await streamRichText("环境规划完毕，开始为您编写核心的ROS寻迹节点逻辑...")
  await pushAgent("编写 ROS 寻迹底层代码", "nanocar_track.py", 2500, 120, 0)
  await streamCodeLines(getV1FullCode().slice(45, 140), 10)

  await streamRichText("寻迹节点完成，正在补充摄像头图传与通信模块...")
  await pushAgent("编写图传与多端无线通信代码", "nanocar_track.py", 2000, 80, 0)
  await streamCodeLines(getV1FullCode().slice(140), 10)

  await streamRichText("所有代码生成完毕，正在进行系统自动预检...")
  const checkAgent = { role: 'ai', type: 'agent', filename: 'AI 代码预检', path: '', status: 'loading' }
  chatBlocks.value.push(checkAgent)
  await scrollToBottom()
  await streamTerminal(getTerminalBuildLogs(), 150) 
  checkAgent.status = 'success'

  const summaryHtml = `
    <h3 style="font-size:14px; font-weight:bold; color:var(--vscode-text); margin-bottom:8px; margin-top:4px;">已完成！</h3>
    <p style="margin-bottom:12px; color:var(--vscode-text);">我已经生成了完整的 <code>nanocar_track.py</code> 寻迹节点。该代码集成了 PID 视觉寻迹控制与图像无线转发机制。</p>
    <h4 style="font-weight:bold; color:var(--vscode-text); margin-bottom:4px;">修改内容：</h4>
    <ul style="padding-left:20px; list-style-type:disc; margin-bottom:12px; color:var(--vscode-text-muted);">
        <li>创建并配置了 <code>NanoCarTracker</code> 核心控制类。</li>
        <li>利用 OpenCV 实现了路网轮廓识别与质心偏移计算。</li>
        <li>整合了雷达话题的紧急避障防撞。</li>
        <li>实现了图像压缩发布用于手机端和电脑端图传。</li>
    </ul>
    <h4 style="font-weight:bold; color:var(--vscode-text); margin-bottom:4px;">操作步骤：</h4>
    <ol style="padding-left:20px; list-style-type:decimal; color:var(--vscode-text-muted);">
        <li style="margin-bottom:4px;"><b>机器人端：</b>赋予脚本执行权限：<br><code>chmod +x src/nanocar_ros/scripts/nanocar_track.py</code></li>
        <li style="margin-bottom:4px;"><b>启动节点：</b><br><code>roslaunch nanocar_track start.launch</code></li>
        <li><b>多端查看：</b>手机/电脑浏览器访问 <code>http://&lt;机器人IP&gt;:8080/stream_viewer</code></li>
    </ol>
  `
  await streamRichText(summaryHtml)
  isGenerating.value = false
  canDeploy.value = true
}

const runState2Sequence = async () => {
  await streamRichText("收到报错信息，正在回顾项目上下文并分析错误原因...")
  await pushAgent("召回上下文与报错分析", "", 1800)

  await streamRichText("分析完毕，确认雷达话题名不匹配，正在为您修改相关代码...")
  await pushAgent("修改寻迹节点话题订阅", "nanocar_track.py", 2000, 3, 2)
  codeLines.value = getV2CodeDiff_Part1()
  await scrollToDiff()

  await streamRichText("雷达节点修复完毕，同步排查关联的权限配置...")
  await pushAgent("补充设备 udev 权限代码", "nanocar_track.py", 2000, 7, 1)
  codeLines.value = getV2CodeDiff_Full()
  await scrollToDiff()

  await streamRichText("代码修正已完成，正在执行二次预检...")
  const checkAgent = { role: 'ai', type: 'agent', filename: 'AI 二次预检', path: '', status: 'loading' }
  chatBlocks.value.push(checkAgent)
  await scrollToBottom()
  await streamTerminal(getV2TerminalPrecheck(), 120) 
  checkAgent.status = 'success'

  showDiffBar.value = true
  await streamRichText("已修复雷达话题匹配问题，并增加摄像头权限...")
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
    { text: '$ source devel/setup.bash', type: 't-normal' },
    { text: '$ roslaunch nanocar_track start.launch', type: 't-normal' },
    { text: '[INFO] [1712481691.124]: Setting /dev/video0 permissions...', type: 't-success' },
    { text: '[INFO] [1712481691.562]: Successfully subscribed to /rplidar_scan', type: 't-success' },
    { text: '[INFO] [1712481692.015]: Video streaming started on port 8080.', type: 't-success' },
    { text: '✅ [SYSTEM] All subsystems operational.', type: 't-success' }
  ], 100)
  await streamRichText("<p>变更已应用并验证通过。系统当前运行良好，可随时打包代码。</p>")
  isGenerating.value = false
  canDeploy.value = true
}

const handleRejectDiff = () => {
  showDiffBar.value = false
  codeLines.value = getV1FullCode()
  isGenerating.value = false
}

const navigateToDeploy = () => { if (appState.value !== 3) { router.push('/student-debug/deploy') } }

// 纯 CSS 语法高亮
const highlightSyntax = (text) => {
  return text
    .replace(/(#.*)/g, '<span class="hl-comment">$1</span>')
    .replace(/\b(import|from|class|def|if|try|except|pass|self|__init__|__name__|not|and|return)\b/g, '<span class="hl-keyword">$1</span>')
    .replace(/(".*?"|'.*?')/g, '<span class="hl-string">$1</span>')
    .replace(/\b([a-zA-Z_]\w*)(?=\()/g, '<span class="hl-func">$1</span>')
}

// ================== 原版最完整代码常量的 CSS 化 ==================
const getV1FullCode = () => {
  const code = [
    { content: '<span class="hl-keyword">#!/usr/bin/env python</span>' },
    { content: '<span class="hl-keyword"># -*- coding: utf-8 -*-</span>' },
    { content: '<span class="hl-string">"""</span>' },
    { content: '<span class="hl-string"> NanoCar ROS1 Track & Stream Node</span>' },
    { content: '<span class="hl-string"> @Author: Builder AI</span>' },
    { content: '<span class="hl-string"> @Description: Line tracking using AstraPro and obstacle avoidance via RPLidar</span>' },
    { content: '<span class="hl-string">"""</span>' },
    { content: '' },
    { content: '<span class="hl-keyword">import</span> rospy' },
    { content: '<span class="hl-keyword">import</span> cv2' },
    { content: '<span class="hl-keyword">import</span> numpy <span class="hl-keyword">as</span> np' },
    { content: '<span class="hl-keyword">import</span> os' },
    { content: '<span class="hl-keyword">import</span> sys' },
    { content: '<span class="hl-keyword">import</span> threading' },
    { content: '<span class="hl-keyword">import</span> math' },
    { content: '<span class="hl-keyword">from</span> sensor_msgs.msg <span class="hl-keyword">import</span> Image, LaserScan' },
    { content: '<span class="hl-keyword">from</span> geometry_msgs.msg <span class="hl-keyword">import</span> Twist, PoseStamped' },
    { content: '<span class="hl-keyword">from</span> std_msgs.msg <span class="hl-keyword">import</span> String, Int32, Float32' },
    { content: '<span class="hl-keyword">from</span> nav_msgs.msg <span class="hl-keyword">import</span> Odometry' },
    { content: '<span class="hl-keyword">from</span> cv_bridge <span class="hl-keyword">import</span> CvBridge, CvBridgeError' },
    { content: '<span class="hl-keyword">from</span> dynamic_reconfigure.server <span class="hl-keyword">import</span> Server' },
    { content: '' },
    { content: '<span class="hl-keyword">class</span> <span class="hl-func">NanoCarTracker</span>:' },
    { content: '    <span class="hl-keyword">def</span> <span class="hl-func">__init__</span>(<span class="hl-keyword">self</span>):' },
    { content: '        rospy.init_node(<span class="hl-string">"nanocar_track_node"</span>, anonymous=<span class="hl-keyword">True</span>)' },
    { content: '        <span class="hl-keyword">self</span>.bridge = CvBridge()' },
    { content: '        ' },
    { content: '        <span class="hl-comment"># ===============================</span>' },
    { content: '        <span class="hl-comment"># Parameter Initialization</span>' },
    { content: '        <span class="hl-comment"># ===============================</span>' },
    { content: '        <span class="hl-keyword">self</span>.kp = rospy.get_param(<span class="hl-string">"~kp"</span>, 0.005)' },
    { content: '        <span class="hl-keyword">self</span>.ki = rospy.get_param(<span class="hl-string">"~ki"</span>, 0.000)' },
    { content: '        <span class="hl-keyword">self</span>.kd = rospy.get_param(<span class="hl-string">"~kd"</span>, 0.001)' },
    { content: '        <span class="hl-keyword">self</span>.last_error = 0' },
    { content: '        <span class="hl-keyword">self</span>.integral_error = 0' },
    { content: '        <span class="hl-keyword">self</span>.max_angular_z = 1.5' },
    { content: '        ' },
    { content: '        <span class="hl-comment"># State variables</span>' },
    { content: '        <span class="hl-keyword">self</span>.safe_distance = 0.3  <span class="hl-comment"># meters</span>' },
    { content: '        <span class="hl-keyword">self</span>.obstacle_detected = <span class="hl-keyword">False</span>' },
    { content: '        <span class="hl-keyword">self</span>.current_linear_velocity = 0.2' },
    { content: '        <span class="hl-keyword">self</span>.is_active = <span class="hl-keyword">True</span>' },
    { content: '        ' },
    { content: '        <span class="hl-comment"># Publishers</span>' },
    { content: '        <span class="hl-keyword">self</span>.cmd_pub = rospy.Publisher(<span class="hl-string">"cmd_vel"</span>, Twist, queue_size=10)' },
    { content: '        <span class="hl-keyword">self</span>.image_pub = rospy.Publisher(<span class="hl-string">"camera/processed/image_raw"</span>, Image, queue_size=2)' },
    { content: '        <span class="hl-keyword">self</span>.diag_pub = rospy.Publisher(<span class="hl-string">"diagnostics/tracker_status"</span>, String, queue_size=1)' },
    { content: '        ' },
    { content: '        <span class="hl-comment"># ===============================</span>' },
    { content: '        <span class="hl-comment"># Subscribers</span>' },
    { content: '        <span class="hl-comment"># ===============================</span>' },
    { content: '        <span class="hl-comment"># 错误订阅点：使用了默认的 /scan</span>' },
    { content: '        rospy.Subscriber(<span class="hl-string">"<span style=\'color:red; font-weight:bold;\'>/scan</span>"</span>, LaserScan, <span class="hl-keyword">self</span>.lidar_callback)' },
    { content: '        rospy.Subscriber(<span class="hl-string">"camera/rgb/image_raw"</span>, Image, <span class="hl-keyword">self</span>.image_callback)' },
    { content: '        rospy.Subscriber(<span class="hl-string">"odom"</span>, Odometry, <span class="hl-keyword">self</span>.odom_callback)' },
    { content: '        ' },
    { content: '        <span class="hl-comment"># 初始化底层硬件</span>' },
    { content: '        <span class="hl-keyword">self</span>.init_hardware()' },
    { content: '        ' },
    { content: '        rospy.loginfo(<span class="hl-string">"NanoCar Tracker Initialized Successfully."</span>)' },
    { content: '' },
    { content: '    <span class="hl-keyword">def</span> <span class="hl-func">init_hardware</span>(<span class="hl-keyword">self</span>):' },
    { content: '        <span class="hl-comment"># 缺少权限授予代码</span>' },
    { content: '        rospy.loginfo(<span class="hl-string">"Checking camera /dev/video0 access..."</span>)' },
    { content: '        <span class="hl-keyword">if not</span> os.path.exists(<span class="hl-string">"/dev/video0"</span>):' },
    { content: '            rospy.logwarn(<span class="hl-string">"Warning: /dev/video0 not found!"</span>)' },
    { content: '' }
  ]
  const filler = [
    '    <span class="hl-keyword">def</span> <span class="hl-func">odom_callback</span>(<span class="hl-keyword">self</span>, msg):',
    '        <span class="hl-comment"># Store current odometry for potential dead reckoning fallback</span>',
    '        <span class="hl-keyword">self</span>.current_pose = msg.pose.pose',
    '',
    '    <span class="hl-keyword">def</span> <span class="hl-func">lidar_callback</span>(<span class="hl-keyword">self</span>, msg):',
    '        <span class="hl-comment"># Process Lidar data to find front obstacles from rplidar_super</span>',
    '        ranges_len = <span class="hl-keyword">len</span>(msg.ranges)',
    '        <span class="hl-keyword">if</span> ranges_len == 0: <span class="hl-keyword">return</span>',
    '        ',
    '        <span class="hl-comment"># Front 40 degrees cone (-20 to +20 degrees)</span>',
    '        idx_offset = <span class="hl-keyword">int</span>((20 / 360.0) * ranges_len)',
    '        front_ranges = msg.ranges[-idx_offset:] + msg.ranges[:idx_offset]',
    '        valid_ranges = [r <span class="hl-keyword">for</span> r <span class="hl-keyword">in</span> front_ranges <span class="hl-keyword">if</span> r > 0.01 <span class="hl-keyword">and</span> <span class="hl-keyword">not</span> math.isinf(r)]',
    '        ',
    '        <span class="hl-keyword">if</span> valid_ranges <span class="hl-keyword">and</span> min(valid_ranges) < <span class="hl-keyword">self</span>.safe_distance:',
    '            <span class="hl-keyword">if not</span> <span class="hl-keyword">self</span>.obstacle_detected:',
    '                rospy.logwarn(<span class="hl-string">"Obstacle detected! Stopping..."</span>)',
    '            <span class="hl-keyword">self</span>.obstacle_detected = <span class="hl-keyword">True</span>',
    '            <span class="hl-keyword">self</span>.diag_pub.publish(<span class="hl-string">"STATUS: OBSTACLE_AVOIDANCE_ACTIVE"</span>)',
    '        <span class="hl-keyword">else</span>:',
    '            <span class="hl-keyword">if</span> <span class="hl-keyword">self</span>.obstacle_detected:',
    '                rospy.loginfo(<span class="hl-string">"Path clear. Resuming..."</span>)',
    '            <span class="hl-keyword">self</span>.obstacle_detected = <span class="hl-keyword">False</span>',
    '            <span class="hl-keyword">self</span>.diag_pub.publish(<span class="hl-string">"STATUS: TRACKING_NORMAL"</span>)',
    '',
    '    <span class="hl-keyword">def</span> <span class="hl-func">image_callback</span>(<span class="hl-keyword">self</span>, data):',
    '        <span class="hl-keyword">if not</span> <span class="hl-keyword">self</span>.is_active: <span class="hl-keyword">return</span>',
    '        <span class="hl-keyword">try</span>:',
    '            cv_image = <span class="hl-keyword">self</span>.bridge.imgmsg_to_cv2(data, <span class="hl-string">"bgr8"</span>)',
    '        <span class="hl-keyword">except</span> CvBridgeError <span class="hl-keyword">as</span> e:',
    '            rospy.logerr(<span class="hl-string">"CV Bridge Error: %s"</span> % e)',
    '            <span class="hl-keyword">return</span>',
    '',
    '        <span class="hl-comment"># Halt if obstacle is in front</span>',
    '        <span class="hl-keyword">if</span> <span class="hl-keyword">self</span>.obstacle_detected:',
    '            <span class="hl-keyword">self</span>.stop_robot()',
    '            <span class="hl-keyword">self</span>.publish_annotated_image(cv_image, text=<span class="hl-string">"BLOCKED"</span>)',
    '            <span class="hl-keyword">return</span>',
    '',
    '        <span class="hl-comment"># Image Processing Core Logic</span>',
    '        blurred = cv2.GaussianBlur(cv_image, (5, 5), 0)',
    '        hsv = cv2.cvtColor(blurred, cv2.COLOR_BGR2HSV)',
    '        lower_black = np.array([0, 0, 0])',
    '        upper_black = np.array([180, 255, 50])',
    '        mask = cv2.inRange(hsv, lower_black, upper_black)',
    '',
    '        h, w, d = cv_image.shape',
    '        search_top = <span class="hl-keyword">int</span>(3*h/4)',
    '        search_bot = <span class="hl-keyword">int</span>(h)',
    '        mask[0:search_top, 0:w] = 0',
    '        mask[search_bot:h, 0:w] = 0',
    '',
    '        M = cv2.moments(mask)',
    '        <span class="hl-keyword">if</span> M[<span class="hl-string">\'m00\'</span>] > 0:',
    '            cx = <span class="hl-keyword">int</span>(M[<span class="hl-string">\'m10\'</span>]/M[<span class="hl-string">\'m00\'</span>])',
    '            cy = <span class="hl-keyword">int</span>(M[<span class="hl-string">\'m01\'</span>]/M[<span class="hl-string">\'m00\'</span>])',
    '            cv2.circle(cv_image, (cx, cy), 20, (0,0,255), -1)',
    '            ',
    '            error = cx - w/2',
    '            <span class="hl-keyword">self</span>.integral_error += error',
    '            <span class="hl-keyword">self</span>.integral_error = max(min(<span class="hl-keyword">self</span>.integral_error, 1000), -1000)',
    '            ',
    '            angular_z = (<span class="hl-keyword">self</span>.kp * error) + (<span class="hl-keyword">self</span>.ki * <span class="hl-keyword">self</span>.integral_error) + (<span class="hl-keyword">self</span>.kd * (error - <span class="hl-keyword">self</span>.last_error))',
    '            <span class="hl-keyword">self</span>.last_error = error',
    '            angular_z = max(min(angular_z, <span class="hl-keyword">self</span>.max_angular_z), -<span class="hl-keyword">self</span>.max_angular_z)',
    '            ',
    '            twist = Twist()',
    '            twist.linear.x = <span class="hl-keyword">self</span>.current_linear_velocity',
    '            twist.angular.z = -<span class="hl-keyword">float</span>(angular_z)',
    '            <span class="hl-keyword">self</span>.cmd_pub.publish(twist)',
    '            <span class="hl-keyword">self</span>.publish_annotated_image(cv_image, text=<span class="hl-string">"TRACKING"</span>)',
    '        <span class="hl-keyword">else</span>:',
    '            <span class="hl-keyword">self</span>.stop_robot()',
    '            <span class="hl-keyword">self</span>.publish_annotated_image(cv_image, text=<span class="hl-string">"LOST_LINE"</span>)',
    '',
    '    <span class="hl-keyword">def</span> <span class="hl-func">publish_annotated_image</span>(<span class="hl-keyword">self</span>, image, text=<span class="hl-string">""</span>):',
    '        font = cv2.FONT_HERSHEY_SIMPLEX',
    '        cv2.putText(image, text, (10, 30), font, 1, (0, 255, 0), 2, cv2.LINE_AA)',
    '        <span class="hl-keyword">try</span>:',
    '            msg = <span class="hl-keyword">self</span>.bridge.cv2_to_imgmsg(image, <span class="hl-string">"bgr8"</span>)',
    '            <span class="hl-keyword">self</span>.image_pub.publish(msg)',
    '        <span class="hl-keyword">except</span> CvBridgeError <span class="hl-keyword">as</span> e:',
    '            rospy.logerr(e)',
    '',
    '    <span class="hl-keyword">def</span> <span class="hl-func">stop_robot</span>(<span class="hl-keyword">self</span>):',
    '        twist = Twist()',
    '        twist.linear.x = 0; twist.angular.z = 0',
    '        <span class="hl-keyword">self</span>.cmd_pub.publish(twist)',
    '',
    '<span class="hl-keyword">def</span> <span class="hl-func">main</span>():',
    '    <span class="hl-keyword">try</span>:',
    '        tracker = NanoCarTracker()',
    '        rospy.spin()',
    '    <span class="hl-keyword">except</span> rospy.ROSInterruptException:',
    '        rospy.logwarn(<span class="hl-string">"Tracker node terminated."</span>)',
    '',
    '<span class="hl-keyword">if</span> __name__ == <span class="hl-string">"__main__"</span>:',
    '    main()'
  ]
  for(let line of filler) code.push({ content: line })
  return code
}

const getV2CodeDiff_Part1 = () => {
  const code = getV1FullCode()
  code.splice(58, 2, 
    { content: '        <span class="hl-comment"># 错误订阅点：使用了默认的 /scan</span>', diffType: 'removed' },
    { content: '        rospy.Subscriber(<span class="hl-string">"<span style=\'color:red; font-weight:bold;\'>/scan</span>"</span>, LaserScan, <span class="hl-keyword">self</span>.lidar_callback)', diffType: 'removed' },
    { content: '        <span class="hl-comment"># 修改：使用冰达 NanoCar 真实的雷达话题，并增加队列保护</span>', diffType: 'added' },
    { content: '        rospy.Subscriber(<span class="hl-string">"/rplidar_scan"</span>, LaserScan, <span class="hl-keyword">self</span>.lidar_callback, queue_size=10)', diffType: 'added' },
    { content: '        rospy.loginfo(<span class="hl-string">"[Fix] Successfully mapped to /rplidar_scan"</span>)', diffType: 'added' }
  )
  return code
}

const getV2CodeDiff_Full = () => {
  const code = getV2CodeDiff_Part1()
  const initIdx = code.findIndex(c => c.content.includes('def init_hardware(self):'))
  if (initIdx !== -1) {
    code.splice(initIdx + 1, 1, 
      { content: '        <span class="hl-comment"># 缺少权限授予代码</span>', diffType: 'removed' },
      { content: '        <span class="hl-comment"># 增加：赋予设备节点最高权限，解决 Permission Denied 异常</span>', diffType: 'added' },
      { content: '        <span class="hl-keyword">try</span>:', diffType: 'added' },
      { content: '            os.system(<span class="hl-string">"echo nanocar | sudo -S chmod 777 /dev/video0"</span>)', diffType: 'added' },
      { content: '            os.system(<span class="hl-string">"echo nanocar | sudo -S chmod 777 /dev/rplidar"</span>)', diffType: 'added' },
      { content: '            rospy.loginfo(<span class="hl-string">"[Hardware] Device permissions granted via udev fallback."</span>)', diffType: 'added' },
      { content: '        <span class="hl-keyword">except</span> Exception <span class="hl-keyword">as</span> e:', diffType: 'added' },
      { content: '            rospy.logerr(<span class="hl-string">"[Hardware] Failed to grant permissions: %s"</span> % e)', diffType: 'added' }
    )
  }
  return code
}

const getV2FullCode = () => { return getV2CodeDiff_Full().filter(c => c.diffType !== 'removed').map(c => { return { content: c.content } }) }

// 终端数据
const getTerminalBuildLogs = () => {
    let logs = [
        { text: '$ source /opt/ros/melodic/setup.bash', type: 't-normal' },
        { text: '$ catkin_make', type: 't-normal' },
        { text: 'Base path: /home/nanocar/nanocar_ws', type: 't-normal' },
        { text: 'Source space: /home/nanocar/nanocar_ws/src', type: 't-normal' },
        { text: 'Build space: /home/nanocar/nanocar_ws/build', type: 't-normal' },
        { text: 'Devel space: /home/nanocar/nanocar_ws/devel', type: 't-normal' },
        { text: '#### Running command: "make cmake_check_build_system" in "/home/nanocar/nanocar_ws/build"', type: 't-normal' }
    ]
    for(let i=1; i<=15; i++) { logs.push({ text: `[ ${Math.floor(i*6)}%] Built target subsystem_component_${i}`, type: 't-success' }) }
    logs.push({ text: '[ 98%] Built target nanocar_ros', type: 't-success' })
    logs.push({ text: '[100%] Built target web_video_server', type: 't-success' })
    logs.push({ text: '✓ 依赖树完整，预检通过。', type: 't-success' })
    return logs
}
const getV1TerminalError = () => {
    const logs = getTerminalBuildLogs()
    logs.push({ text: '$ roslaunch nanocar_track start.launch', type: 't-normal' })
    logs.push({ text: '... logging to /home/nanocar/.ros/log/...', type: 't-normal' })
    logs.push({ text: 'started roslaunch server http://nanocar:41951/', type: 't-normal' })
    logs.push({ text: 'NODES: nanocar_track_node (nanocar_ros/nanocar_track.py)', type: 't-normal' })
    logs.push({ text: '[ERROR] [1712481690.123456]: Cannot connect to Lidar topic /scan. Retrying...', type: 't-error' })
    logs.push({ text: '[ERROR] [1712481690.124888]: IOError: [Errno 13] Permission denied: \'/dev/video0\'', type: 't-error' })
    logs.push({ text: '[FATAL] [1712481690.125001]: Node crashed with rospy.exceptions.ROSException.', type: 't-error' })
    return logs
}
const getV2TerminalPrecheck = () => [
    { text: '$ rosrun rospy_tutorials precheck.py nanocar_track.py', type: 't-normal' },
    { text: 'Validating topic subscriptions...', type: 't-normal' },
    { text: '↳ /rplidar_scan detected. OK.', type: 't-success' },
    { text: 'Validating system level bindings...', type: 't-normal' },
    { text: '↳ udev rule fallback (sudo chmod 777) found. OK.', type: 't-success' },
    { text: '✓ 预检完成，代码健康状态：良好', type: 't-success' }
]
const getV2TerminalSuccess = () => [
    { text: '$ source devel/setup.bash', type: 't-normal' },
    { text: '$ roslaunch nanocar_track start.launch', type: 't-normal' },
    { text: '[INFO] [1712481691.124]: Setting /dev/video0 permissions... (sudo chmod 777)', type: 't-normal' },
    { text: '[INFO] [1712481691.562]: Successfully subscribed to /rplidar_scan', type: 't-success' },
    { text: '[INFO] [1712481692.015]: Lidar obstacle avoidance running. Safe distance: 0.3m', type: 't-success' },
    { text: '[INFO] [1712481692.100]: Video stream captured. Resolution 640x480.', type: 't-success' },
    { text: '[INFO] [1712481692.500]: Web video server started on port 8080.', type: 't-success' },
    { text: '✅ [SYSTEM] All subsystems operational. Tracking line...', type: 't-success' }
]
</script>

<style scoped>
/* ================= VS Code Light 核心变量 ================= */
:root {
  --vscode-bg: #ffffff;
  --vscode-panel-bg: #f3f3f3;
  --vscode-border: #e4e6e8;
  --vscode-tab-active: #ffffff;
  --vscode-tab-inactive: #ececec;
  --vscode-text: #333333;
  --vscode-text-muted: #8a8a8a;
  --vscode-accent: #007acc;
  --vscode-error: #e51400;
  --vscode-success: #107c10;
  --vscode-code-bg: #ffffff;
  --vscode-terminal-bg: #ffffff;
}

/* ================= 布局骨架 ================= */
.page-wrapper { padding: 16px; height: calc(100vh - 64px); box-sizing: border-box; background: var(--color-background); }
.ide-layout { display: flex; gap: 16px; height: 100%; min-height: 0; }

/* ================= 图标与通用按钮 ================= */
.icon { flex-shrink: 0; display: inline-block; vertical-align: middle; }
.icon-xs { width: 14px; height: 14px; }
.icon-sm { width: 16px; height: 16px; }
.icon-md { width: 20px; height: 20px; }
.icon-lg { width: 36px; height: 36px; }

.icon-accent { color: var(--vscode-accent); }
.icon-success { color: var(--vscode-success); }
.icon-warning { color: #d7ba7d; }

.vscode-btn {
  padding: 6px 14px; font-size: 13px; border-radius: 2px; cursor: pointer; display: flex; align-items: center; gap: 6px; border: 1px solid transparent; transition: all 0.2s;
}
.btn-sm { padding: 4px 10px; font-size: 12px; }
.btn-primary { background: #007acc; color: white; border-color: #005a9e; font-weight: bold; box-shadow: 0 2px 4px rgba(0, 122, 204, 0.2); }
.btn-primary:hover { background: #0062a3; box-shadow: 0 4px 12px rgba(0, 122, 204, 0.3); transform: translateY(-1px); }
.btn-primary:active { transform: translateY(0); }
.btn-primary:disabled { background: #a6a6a6; cursor: not-allowed; box-shadow: none; border-color: #a6a6a6; transform: none;}
.btn-secondary { background: #e4e6e8; color: var(--vscode-text); border: 1px solid #c9cbd0; }
.btn-secondary:hover { background: #d1d5db; }

/* ================= 核心面板 ================= */
.vscode-panel { display: flex; flex-direction: column; background: var(--vscode-bg); border: 1px solid var(--vscode-border); border-radius: 4px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }

/* ================= 左侧专区 ================= */
.editor-column { display: flex; flex-direction: column; gap: 16px; flex: 2; min-width: 0; }
.vscode-alert { padding: 8px 12px; border-radius: 4px; display: flex; align-items: center; font-size: 13px; border-left: 4px solid; background: var(--vscode-bg); border-top: 1px solid var(--vscode-border); border-right: 1px solid var(--vscode-border); border-bottom: 1px solid var(--vscode-border); box-shadow: 0 2px 6px rgba(0,0,0,0.05); flex-shrink: 0; }
.vscode-alert svg { margin-right: 10px; }
.alert-success { border-left-color: var(--vscode-success); }
.alert-error { border-left-color: var(--vscode-error); color: var(--vscode-error); }

/* 编辑器头部 Tabs */
.editor-panel { flex: 1; }
.vscode-tabs-header { display: flex; align-items: center; background: var(--vscode-panel-bg); min-height: 35px; padding-right: 12px; }
.mac-controls { display: flex; gap: 6px; padding: 0 12px; }
.mac-dot { width: 10px; height: 10px; border-radius: 50%; } .dot-close { background: #ff5f56; } .dot-min { background: #ffbd2e; } .dot-max { background: #27c93f; }
.tabs-scroll-area { display: flex; height: 100%; flex: 1; overflow-x: auto; }
.vscode-tab { display: flex; align-items: center; padding: 0 12px; height: 35px; font-size: 13px; font-family: -apple-system, sans-serif; color: var(--vscode-text-muted); background: var(--vscode-tab-inactive); border-right: 1px solid var(--vscode-border); cursor: pointer; user-select: none; }
.vscode-tab.active { background: var(--vscode-bg); color: var(--vscode-text); border-top: 2px solid var(--vscode-accent); padding-top: -2px; }
.tab-icon-py { color: #3776ab; margin-right: 6px; }
.tab-close { margin-left: 8px; font-size: 16px; width: 20px; height: 20px; display: flex; justify-content: center; align-items: center; border-radius: 4px; }
.tab-close:hover { background: rgba(0,0,0,0.08); }
.editor-actions { color: var(--vscode-text-muted); cursor: pointer; display: flex; align-items: center; } .editor-actions:hover { color: var(--vscode-text); }

/* 面包屑 */
.vscode-breadcrumbs { padding: 4px 16px; font-size: 13px; font-family: Consolas, "Courier New", monospace; color: var(--vscode-text-muted); border-bottom: 1px solid var(--vscode-border); display: flex; align-items: center; }
.sep { margin: 0 6px; } .file-name { color: var(--vscode-text); } .class-name { color: #795e26; }

/* Code Diff */
.vscode-diff-bar { display: flex; justify-content: space-between; align-items: center; padding: 8px 16px; background: #f0f4f9; border-bottom: 1px solid var(--vscode-accent); }
.diff-msg { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--vscode-text); }
.diff-actions { display: flex; gap: 8px; }

/* Code Editor */
.vscode-editor-body { flex: 1; overflow-y: auto; padding: 8px 0; font-family: 'Fira Code', Consolas, "Courier New", monospace; font-size: 14px; line-height: 1.5; background: var(--vscode-code-bg); }
.editor-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: var(--vscode-text-muted); font-size: 14px; } .editor-empty svg { margin-bottom: 16px; opacity: 0.3; }
.code-line { display: flex; padding: 0 16px; } .code-line:hover { background: #f5f5f5; }
.line-num { width: 40px; text-align: right; padding-right: 16px; color: #237893; user-select: none; flex-shrink: 0; }
.line-content { white-space: pre; word-break: break-all; color: var(--vscode-text); }
.line-added { background: rgba(16, 124, 16, 0.15); } .line-added .line-num { color: #107c10; font-weight: bold; }
.line-removed { background: rgba(229, 20, 0, 0.15); text-decoration: line-through; opacity: 0.8; } .line-removed .line-num { color: #e51400; font-weight: bold; }

:deep(.hl-comment) { color: #008000; } :deep(.hl-keyword) { color: #0000ff; } :deep(.hl-string) { color: #a31515; } :deep(.hl-func) { color: #795e26; }

/* 终端面板 */
.terminal-panel { height: 30%; min-height: 200px; flex-shrink: 0; border-radius: 6px; }
.terminal-header { display: flex; justify-content: space-between; align-items: center; padding: 0 16px; border-bottom: 1px solid var(--vscode-border); }
.terminal-tabs { display: flex; gap: 20px; }
.t-tab { padding: 8px 0; font-size: 12px; color: var(--vscode-text-muted); cursor: pointer; border-bottom: 1px solid transparent; text-transform: uppercase; letter-spacing: 0.5px;}
.t-tab.active { color: var(--vscode-text); border-bottom-color: var(--vscode-text); font-weight: 600; }
.terminal-actions { display: flex; align-items: center; gap: 12px; }
.badge-bash { font-size: 12px; font-family: monospace; color: var(--vscode-text); } .terminal-close { cursor: pointer; color: var(--vscode-text-muted); font-size: 18px; }
.terminal-body { padding: 12px 16px; font-family: Consolas, "Courier New", monospace; font-size: 13px; line-height: 1.6; background: var(--vscode-terminal-bg); flex: 1; overflow-y: auto; }
.term-line { word-break: break-all; margin-bottom: 2px;} .t-normal { color: var(--vscode-text); } .t-success { color: #107c10; font-weight:bold; } .t-error { color: #e51400; font-weight:bold;}

/* ================= 右侧：聊天面板 ================= */
.chat-panel { width: 380px; flex-shrink: 0; }
.chat-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid var(--vscode-border); }
.chat-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: bold; color: var(--vscode-text); }
.chat-tag { font-size: 10px; font-weight: 600; color: var(--vscode-text-muted); border: 1px solid var(--vscode-border); padding: 2px 6px; border-radius: 12px; }

.chat-body { flex: 1; padding: 16px; display: flex; flex-direction: column; gap: 16px; overflow-y: auto; }
.chat-block { display: flex; flex-direction: column; width: 100%; animation: fadeInUp 0.3s ease-out forwards; }
.msg-user { align-self: flex-end; max-width: 90%; }
.bubble-user { background: #f0f4fa; color: var(--vscode-text); padding: 10px 14px; border-radius: 8px; border-bottom-right-radius: 0; font-size: 13px; line-height: 1.5; border: 1px solid #e1e7f0; }

.msg-ai { align-self: flex-start; max-width: 95%; display: flex; flex-direction: column; gap: 10px; width: 100%; }
.agent-card { border: 1px solid var(--vscode-border); border-radius: 6px; padding: 10px 12px; display: flex; justify-content: space-between; align-items: center; background: #fafafa; }
.agent-info { display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 600; color: var(--vscode-text); }
.agent-status { display: flex; align-items: center; gap: 12px; }
.agent-diff { font-family: monospace; font-size: 12px; } .diff-add { color: #107c10; margin-right: 4px;} .diff-del { color: #e51400; }
.ai-text { font-size: 13px; line-height: 1.6; color: var(--vscode-text); }
.rich-text-content :deep(code) { background: #f3f3f3; border: 1px solid var(--vscode-border); padding: 2px 4px; border-radius: 3px; font-family: Consolas, monospace; color: var(--vscode-error); font-size: 12px; }

/* 底部输入框 */
.chat-footer { padding: 16px; border-top: 1px solid var(--vscode-border); background: var(--vscode-bg); }
.input-container { border: 1px solid var(--vscode-border); border-radius: 6px; padding: 10px; transition: border-color 0.2s; background: var(--vscode-bg); }
.input-container:focus-within { border-color: var(--vscode-accent); outline: 1px solid var(--vscode-accent); }
.chat-textarea { width: 100%; background: transparent; border: none; outline: none; resize: none; font-size: 13px; color: var(--vscode-text); font-family: inherit;}
.input-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.input-hints { font-size: 11px; color: var(--vscode-text-muted); }
.kbd { background: #f3f3f3; padding: 2px 4px; border-radius: 3px; border: 1px solid var(--vscode-border); font-family: monospace; }

.deploy-hero-btn { width: 100%; margin-top: 12px; padding: 10px; border: none; border-radius: 2px; font-weight: bold; font-size: 13px; display: flex; justify-content: center; align-items: center; gap: 8px; cursor: pointer; transition: 0.2s; }
.deploy-active { background: #107c10; color: white; border: 1px solid #0b580b; box-shadow: 0 2px 4px rgba(16,124,16,0.2);}
.deploy-active:hover { background: #0b580b; }
.deploy-disabled { background: #f3f3f3; border: 1px solid var(--vscode-border); color: var(--vscode-text-muted); cursor: not-allowed; }

/* ================= 辅助工具与动画 ================= */
.custom-scrollbar::-webkit-scrollbar { width: 8px; height: 8px; } .custom-scrollbar::-webkit-scrollbar-thumb { background: #c1c1c1; border-radius: 0; } .custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #a8a8a8; } .custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.cursor-blink { display: inline-block; width: 8px; height: 14px; background: var(--vscode-text); animation: blink 1s step-end infinite; margin-left: 2px; vertical-align: middle; }
.spinner { animation: spin 1s linear infinite; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } } @keyframes spin { 100% { transform: rotate(360deg); } } @keyframes fadeInUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
.fade-down-enter-active, .fade-down-leave-active { transition: all 0.3s ease; } .fade-down-enter-from, .fade-down-leave-to { opacity: 0; transform: translateY(-10px); }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; } .fade-enter-from, .fade-leave-to { opacity: 0; }
</style>