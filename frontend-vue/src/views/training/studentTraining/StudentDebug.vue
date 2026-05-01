<template>
  <div class="page-wrapper">
    <div class="ide-layout">
      
      <!-- ================= 左侧：代码编辑与终端 ================= -->
      <div class="editor-column">
        
        <!-- 顶部提示条 (VS Code 风格的 Notification) -->
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
          
          <!-- Tabs 栏 -->
          <div class="vscode-tabs-header">
            <div class="mac-controls">
              <div class="mac-dot dot-close"></div>
              <div class="mac-dot dot-min"></div>
              <div class="mac-dot dot-max"></div>
            </div>
            <div class="tabs-scroll-area">
              <div class="vscode-tab active">
                <svg class="icon icon-sm tab-icon-py" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15h-2v-2h2v2zm0-4h-2V7h2v6zm4 4h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
                nanocar_track.py
                <span class="tab-close">×</span>
              </div>
            </div>
            <div class="editor-actions">
              <svg class="icon icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 12h.01M12 12h.01M19 12h.01M6 12a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0z"></path></svg>
            </div>
          </div>
          
          <!-- 面包屑导航 -->
          <div class="vscode-breadcrumbs">
            src <span class="sep">></span> nanocar_ros <span class="sep">></span> scripts <span class="sep">></span> 
            <span class="file-name">nanocar_track.py</span> <span class="sep">></span> 
            <span class="class-name">NanoCarTracker</span>
          </div>

          <!-- Code Diff 审查条 -->
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

          <!-- 代码编辑区 -->
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

        <!-- 终端面板 -->
        <div class="vscode-panel terminal-panel">
          <div class="terminal-header">
            <div class="terminal-tabs">
              <div class="t-tab">问题</div>
              <div class="t-tab">输出</div>
              <div class="t-tab">调试控制台</div>
              <div class="t-tab active">终端</div>
            </div>
            <div class="terminal-actions">
              <span class="badge-bash">bash</span>
              <span class="terminal-close">×</span>
            </div>
          </div>
          
          <div class="terminal-body custom-scrollbar" ref="terminalScrollRef">
            <div v-for="(log, idx) in terminalLogs" :key="idx" :class="['term-line', log.type]">{{ log.text }}</div>
            <div v-if="isTerminalRunning" class="cursor-blink"></div>
          </div>
        </div>
      </div>

      <!-- ================= 右侧：AI 对话与控制台 ================= -->
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
            
            <div v-if="block.role === 'user'" class="msg-user">
              <div class="bubble-user">{{ block.content }}</div>
            </div>

            <div v-else class="msg-ai">
              <!-- Agent 工具调用卡片 -->
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

              <!-- 普通文本 -->
              <div v-if="block.type === 'text'" class="ai-text">
                <div v-html="block.content" class="rich-text-content"></div>
                <span v-if="block.isTyping" class="cursor-blink"></span>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-footer">
          <div class="input-container">
            <textarea 
              v-model="currentInput"
              :disabled="isGenerating"
              rows="3"
              class="chat-textarea custom-scrollbar"
              placeholder="询问 @Builder 或输入调整指令..."
            ></textarea>
            <div class="input-actions">
              <div class="input-hints">
                <span class="kbd">Shift</span> + <span class="kbd">Enter</span> 换行
              </div>
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
      { role: 'ai', type: 'text', content: "<p>已生成 <code>nanocar_track.py</code>，包含完整的 PID 寻迹逻辑。请运行查看。</p>" }
    ]
  } else if (appState.value === 3) {
    codeLines.value = getV2FullCode()
    terminalLogs.value = getV2TerminalSuccess()
    chatBlocks.value = [
      { role: 'user', content: "修复完成，确认采纳。" },
      { role: 'ai', type: 'text', content: "<p><b>已完成！</b>雷达话题与摄像头权限已修正，编译通过。</p>" }
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
  await streamRichText("好的，我将为您生成代码...")
  await pushAgent("规划项目结构与环境", "", 1500)
  codeLines.value = [] 
  await streamCodeLines(getV1FullCode().slice(0, 45), 15)
  await streamRichText("核心节点完成，正在补充摄像头图传模块...")
  await pushAgent("编写 ROS 寻迹底层代码", "nanocar_track.py", 2500, 120, 0)
  await streamCodeLines(getV1FullCode().slice(45, 140), 10)
  await streamTerminal(getTerminalBuildLogs(), 150) 
  await streamRichText("<p>完整的 <code>nanocar_track.py</code> 已经生成。请执行后续部署。</p>")
  isGenerating.value = false
  canDeploy.value = true
}

const runState2Sequence = async () => {
  await streamRichText("收到报错信息，正在分析错误原因...")
  await pushAgent("修改寻迹节点话题订阅", "nanocar_track.py", 2000, 3, 2)
  codeLines.value = getV2CodeDiff_Part1()
  await scrollToDiff()
  await pushAgent("补充设备 udev 权限代码", "nanocar_track.py", 2000, 7, 1)
  codeLines.value = getV2CodeDiff_Full()
  await scrollToDiff()
  await streamTerminal(getV2TerminalPrecheck(), 120) 
  showDiffBar.value = true
  await streamRichText("已修复雷达话题匹配问题，请确认是否采纳。")
  await scrollToDiff()
  isGenerating.value = false
}

const handleAcceptDiff = async () => {
  showDiffBar.value = false
  diffActionHandled.value = true
  codeLines.value = getV2FullCode() 
  isGenerating.value = true
  await pushAgent("应用变更并重载 ROS 环境", "", 1500)
  await streamTerminal([{ text: '✅ [SYSTEM] All subsystems operational.', type: 't-success' }], 100)
  await streamRichText("<p>变更已应用并验证通过。系统当前运行良好，可随时打包代码。</p>")
  isGenerating.value = false
  canDeploy.value = true
}

const handleRejectDiff = () => {
  showDiffBar.value = false
  codeLines.value = getV1FullCode()
  isGenerating.value = false
}

const navigateToDeploy = () => { if (appState.value !== 3) { router.push('/training/student-training/student-deploy') } }

// VS Code 浅色主题纯 CSS 语法高亮映射
const highlightSyntax = (text) => {
  return text
    .replace(/(#.*)/g, '<span class="hl-comment">$1</span>')
    .replace(/\b(import|from|class|def|if|try|except|pass|self|__init__|__name__|not|and|return)\b/g, '<span class="hl-keyword">$1</span>')
    .replace(/(".*?"|'.*?')/g, '<span class="hl-string">$1</span>')
    .replace(/\b([a-zA-Z_]\w*)(?=\()/g, '<span class="hl-func">$1</span>')
}

const getV1FullCode = () => {
  const baseCode = [
    '#!/usr/bin/env python',
    'import rospy',
    'import cv2',
    'class NanoCarTracker:',
    '    def __init__(self):',
    '        rospy.init_node("nanocar_track_node")',
    '        # 错误订阅点：使用了默认的 /scan',
    '        rospy.Subscriber("/scan", LaserScan, self.lidar_callback)'
  ]
  for(let i=0; i<150; i++) baseCode.push('        # Normal execution loop logic...')
  return baseCode.map(text => ({ content: highlightSyntax(text) }))
}
const getV2CodeDiff_Part1 = () => {
  const code = getV1FullCode()
  code.splice(6, 2, 
    { content: highlightSyntax('        # 错误订阅点：使用了默认的 /scan'), diffType: 'removed' },
    { content: highlightSyntax('        rospy.Subscriber("/scan", LaserScan, self.lidar_callback)'), diffType: 'removed' },
    { content: highlightSyntax('        # 修改：使用真实的雷达话题'), diffType: 'added' },
    { content: highlightSyntax('        rospy.Subscriber("/rplidar_scan", LaserScan, self.lidar_callback)'), diffType: 'added' }
  )
  return code
}
const getV2CodeDiff_Full = () => getV2CodeDiff_Part1()
const getV2FullCode = () => getV2CodeDiff_Full().filter(c => c.diffType !== 'removed')

const getTerminalBuildLogs = () => [{ text: '$ catkin_make', type: 't-normal' }, { text: '[100%] Built target nanocar_ros', type: 't-success' }]
const getV1TerminalError = () => [{ text: '[ERROR] Cannot connect to Lidar topic /scan.', type: 't-error' }]
const getV2TerminalPrecheck = () => [{ text: '[OK] Syntax check passed.', type: 't-success' }]
const getV2TerminalSuccess = () => [{ text: 'Successfully subscribed to /rplidar_scan', type: 't-success' }]
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
.page-wrapper {
  /* 去除所有的内边距，让 IDE 感觉更饱满 */
  padding: 16px;
  height: calc(100vh - 64px); 
  box-sizing: border-box;
  background: var(--color-background); /* 依然沿用你系统的底层背景 */
}

.ide-layout {
  display: flex;
  gap: 16px;
  height: 100%;
  min-height: 0;
}

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
  padding: 6px 14px;
  font-size: 13px;
  border-radius: 2px; /* VS Code 是直角/微圆角 */
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  border: 1px solid transparent;
}
.btn-sm { padding: 4px 10px; font-size: 12px; }
.btn-primary { background: var(--vscode-accent); color: white; }
.btn-primary:hover { background: #0062a3; }
.btn-primary:disabled { background: #a6a6a6; cursor: not-allowed; }
.btn-secondary { background: #e4e6e8; color: var(--vscode-text); border: 1px solid #c9cbd0; }
.btn-secondary:hover { background: #d1d5db; }

/* ================= 核心面板 (极致扁平化，去除大阴影) ================= */
.vscode-panel {
  display: flex;
  flex-direction: column;
  background: var(--vscode-bg);
  border: 1px solid var(--vscode-border);
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04); /* 极弱的阴影，只为了区分层级 */
}

/* ================= 左侧专区 ================= */
.editor-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 2;
  min-width: 0;
}

/* VS Code 风格的 Notification */
.vscode-alert {
  padding: 8px 12px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  font-size: 13px;
  border-left: 4px solid;
  background: var(--vscode-bg);
  border-top: 1px solid var(--vscode-border);
  border-right: 1px solid var(--vscode-border);
  border-bottom: 1px solid var(--vscode-border);
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  flex-shrink: 0;
}
.vscode-alert svg { margin-right: 10px; }
.alert-success { border-left-color: var(--vscode-success); }
.alert-error { border-left-color: var(--vscode-error); color: var(--vscode-error); }

/* 编辑器头部 Tabs */
.editor-panel { flex: 1; }
.vscode-tabs-header {
  display: flex;
  align-items: center;
  background: var(--vscode-panel-bg);
  min-height: 35px;
  padding-right: 12px;
}
.mac-controls { display: flex; gap: 6px; padding: 0 12px; }
.mac-dot { width: 10px; height: 10px; border-radius: 50%; }
.dot-close { background: #ff5f56; } .dot-min { background: #ffbd2e; } .dot-max { background: #27c93f; }

.tabs-scroll-area {
  display: flex;
  height: 100%;
  flex: 1;
  overflow-x: auto;
}
.vscode-tab {
  display: flex;
  align-items: center;
  padding: 0 12px;
  height: 35px;
  font-size: 13px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: var(--vscode-text-muted);
  background: var(--vscode-tab-inactive);
  border-right: 1px solid var(--vscode-border);
  cursor: pointer;
  user-select: none;
}
.vscode-tab.active {
  background: var(--vscode-bg);
  color: var(--vscode-text);
  border-top: 2px solid var(--vscode-accent);
  padding-top: -2px; /* 抵消 border 高度 */
}
.tab-icon-py { color: #3776ab; margin-right: 6px; }
.tab-close { margin-left: 8px; font-size: 16px; width: 20px; height: 20px; display: flex; justify-content: center; align-items: center; border-radius: 4px; }
.tab-close:hover { background: rgba(0,0,0,0.08); }

.editor-actions { color: var(--vscode-text-muted); cursor: pointer; display: flex; align-items: center; }
.editor-actions:hover { color: var(--vscode-text); }

/* 面包屑 */
.vscode-breadcrumbs {
  padding: 4px 16px;
  font-size: 13px;
  font-family: Consolas, "Courier New", monospace;
  color: var(--vscode-text-muted);
  border-bottom: 1px solid var(--vscode-border);
  display: flex;
  align-items: center;
}
.sep { margin: 0 6px; }
.file-name { color: var(--vscode-text); }
.class-name { color: #795e26; } /* VS Code 浅色主题的类名颜色 */

/* Code Diff */
.vscode-diff-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #f0f4f9;
  border-bottom: 1px solid var(--vscode-accent);
}
.diff-msg { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--vscode-text); }
.diff-actions { display: flex; gap: 8px; }

/* Code Editor */
.vscode-editor-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  font-family: 'Fira Code', Consolas, "Courier New", monospace;
  font-size: 14px;
  line-height: 1.5;
  background: var(--vscode-code-bg);
}
.editor-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: var(--vscode-text-muted); font-size: 14px; }
.editor-empty svg { margin-bottom: 16px; opacity: 0.3; }

.code-line { display: flex; padding: 0 16px; }
.code-line:hover { background: #f5f5f5; }
.line-num { width: 40px; text-align: right; padding-right: 16px; color: #237893; user-select: none; flex-shrink: 0; }
.line-content { white-space: pre; word-break: break-all; color: var(--vscode-text); }

/* Code Diff Colors (VS Code Light Diff style) */
.line-added { background: rgba(16, 124, 16, 0.15); }
.line-added .line-num { color: #107c10; font-weight: bold; }
.line-removed { background: rgba(229, 20, 0, 0.15); text-decoration: line-through; opacity: 0.8; }
.line-removed .line-num { color: #e51400; font-weight: bold; }

/* VS Code 浅色语法高亮 */
:deep(.hl-comment) { color: #008000; }
:deep(.hl-keyword) { color: #0000ff; }
:deep(.hl-string) { color: #a31515; }
:deep(.hl-func) { color: #795e26; }

/* ================= 终端面板 ================= */
.terminal-panel { height: 30%; min-height: 200px; flex-shrink: 0; border-radius: 6px; }
.terminal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid var(--vscode-border);
}
.terminal-tabs { display: flex; gap: 20px; }
.t-tab { padding: 8px 0; font-size: 12px; color: var(--vscode-text-muted); cursor: pointer; border-bottom: 1px solid transparent; text-transform: uppercase; letter-spacing: 0.5px;}
.t-tab.active { color: var(--vscode-text); border-bottom-color: var(--vscode-text); font-weight: 600; }
.terminal-actions { display: flex; align-items: center; gap: 12px; }
.badge-bash { font-size: 12px; font-family: monospace; color: var(--vscode-text); }
.terminal-close { cursor: pointer; color: var(--vscode-text-muted); font-size: 18px; }

.terminal-body {
  padding: 12px 16px;
  font-family: Consolas, "Courier New", monospace;
  font-size: 13px;
  line-height: 1.6;
  background: var(--vscode-terminal-bg);
  flex: 1;
  overflow-y: auto;
}
.term-line { word-break: break-all; margin-bottom: 2px;}
.t-normal { color: var(--vscode-text); }
.t-success { color: #107c10; }
.t-error { color: #e51400; }


/* ================= 右侧：聊天面板 (GitHub Copilot Chat 风格) ================= */
.chat-panel { width: 380px; flex-shrink: 0; }
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--vscode-border);
}
.chat-title { display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: bold; color: var(--vscode-text); }
.chat-tag { font-size: 10px; font-weight: 600; color: var(--vscode-text-muted); border: 1px solid var(--vscode-border); padding: 2px 6px; border-radius: 12px; }

.chat-body {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow-y: auto;
}
.chat-block { display: flex; flex-direction: column; width: 100%; animation: fadeInUp 0.3s ease-out forwards; }

.msg-user { align-self: flex-end; max-width: 90%; }
.bubble-user { background: #f0f4fa; color: var(--vscode-text); padding: 10px 14px; border-radius: 8px; border-bottom-right-radius: 0; font-size: 13px; line-height: 1.5; border: 1px solid #e1e7f0; }

.msg-ai { align-self: flex-start; max-width: 95%; display: flex; flex-direction: column; gap: 10px; width: 100%; }
.agent-card { border: 1px solid var(--vscode-border); border-radius: 6px; padding: 10px 12px; display: flex; justify-content: space-between; align-items: center; background: #fafafa; }
.agent-info { display: flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 600; color: var(--vscode-text); }
.agent-status { display: flex; align-items: center; gap: 12px; }
.agent-diff { font-family: monospace; font-size: 12px; }
.diff-add { color: #107c10; margin-right: 4px;} .diff-del { color: #e51400; }

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

.deploy-hero-btn { width: 100%; margin-top: 12px; padding: 10px; border: none; border-radius: 4px; font-weight: bold; font-size: 13px; display: flex; justify-content: center; align-items: center; gap: 8px; cursor: pointer; transition: 0.2s; }
.deploy-active { background: #107c10; color: white; }
.deploy-active:hover { background: #0b580b; }
.deploy-disabled { background: #f3f3f3; border: 1px solid var(--vscode-border); color: var(--vscode-text-muted); cursor: not-allowed; }

/* ================= 辅助工具与动画 ================= */
.custom-scrollbar::-webkit-scrollbar { width: 8px; height: 8px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #c1c1c1; border-radius: 0; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #a8a8a8; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }

.cursor-blink { display: inline-block; width: 8px; height: 14px; background: var(--vscode-text); animation: blink 1s step-end infinite; margin-left: 2px; vertical-align: middle; }
.spinner { animation: spin 1s linear infinite; }

@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }
@keyframes spin { 100% { transform: rotate(360deg); } }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

.fade-down-enter-active, .fade-down-leave-active { transition: all 0.3s ease; }
.fade-down-enter-from, .fade-down-leave-to { opacity: 0; transform: translateY(-10px); }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>