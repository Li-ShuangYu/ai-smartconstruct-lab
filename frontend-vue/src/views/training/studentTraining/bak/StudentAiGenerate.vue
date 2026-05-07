<template>
  <div class="page-wrapper">
    <!-- 顶部状态栏 -->
    <header class="vscode-header">
      <div class="header-left">
        <svg class="icon icon-md icon-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 10l-2 1m0 0l-2-1m2 1v2.5M20 7l-2 1m2-1l-2-1m2 1v2.5M14 4l-2-1-2 1M4 7l2-1M4 7l2 1M4 7v2.5M12 21l-2-1m2 1l2-1m-2 1v-2.5M6 18l-2-1v-2.5M18 18l2-1v-2.5"></path></svg>
        <span class="header-title">冰达 NanoCar 智能调试控制台</span>
        <span class="header-badge">ROS1 Melodic</span>
      </div>
      <div class="header-right">
        <span class="status-label">设备连接状态:</span>
        <div class="status-indicator">
          <span :class="['status-dot', hwActive ? 'dot-active' : 'dot-waiting']"></span>
          <span :class="hwActive ? 'text-success' : 'text-warning'">{{ hwActive ? 'ONLINE (ACTIVE)' : 'WAITING' }}</span>
        </div>
      </div>
    </header>

    <main class="ide-layout">
      <!-- 左侧：AI 控制与遥测 -->
      <aside class="sidebar-column">
        <div class="vscode-panel">
          <div class="panel-titlebar">
            <svg class="icon icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
            {{ theme === 'cyan' ? 'AI 任务下发控制台' : 'AI 异常捕获与重构中心' }}
          </div>
          <div class="panel-content flex-col">
            <textarea v-model="activePrompt" class="vscode-textarea custom-scrollbar" readonly></textarea>
            <button v-if="!hasGeneratedV1" @click="generateV1Code" class="vscode-btn btn-primary w-full mt-3">
              一键生成 ROS 巡线与图传代码
            </button>
            <div v-else class="status-banner mt-3" :class="theme === 'cyan' ? 'banner-info' : 'banner-error'">
              {{ theme === 'cyan' ? '当前状态：任务执行中' : '当前状态：代码深度重构中' }}
            </div>
          </div>
        </div>

        <div class="vscode-panel flex-1">
          <div class="panel-titlebar">环境与硬件遥测</div>
          <div class="panel-content flex-col justify-center">
            <div class="telemetry-group">
              <div class="telemetry-row"><span>Jetson Nano CPU</span><span>{{ mockCpu }}%</span></div>
              <div class="progress-track"><div class="progress-fill bg-accent" :style="{ width: mockCpu + '%' }"></div></div>
            </div>
            <div class="telemetry-group mt-3">
              <div class="telemetry-row"><span>MEM Usage</span><span>{{ mockRam }} / 4GB</span></div>
              <div class="progress-track"><div class="progress-fill bg-purple" :style="{ width: (mockRam / 4 * 100) + '%' }"></div></div>
            </div>
            <div class="radar-container mt-4">
              <div class="radar-ring">
                <div class="radar-ring-inner"></div>
                <div :class="['radar-sweep', hwActive ? 'sweep-active' : 'sweep-inactive']"></div>
                <div v-if="hwActive" class="radar-target"></div>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- 右侧：代码编辑与终端 -->
      <section class="editor-column">
        <div class="vscode-panel flex-1">
          <div class="vscode-tabs-header">
            <div class="mac-controls">
               <div class="mac-dot dot-close"></div><div class="mac-dot dot-min"></div><div class="mac-dot dot-max"></div>
            </div>
            <div class="vscode-tab active">
              <svg class="icon icon-sm icon-py" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15h-2v-2h2v2zm0-4h-2V7h2v6zm4 4h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
              line_follow_stream.py
            </div>
            <div class="flex-1"></div>
            <button v-if="canDeployV1" @click="deployV1" class="vscode-btn btn-success mr-2">下载部署到机器人</button>
            <button v-if="canDeployV2" @click="deployV2" class="vscode-btn btn-success mr-2">重新部署 V2 代码</button>
          </div>
          
          <div class="vscode-editor-body custom-scrollbar" ref="codeContainer">
            <div v-if="isCodeLoading" class="editor-overlay"><svg class="icon icon-lg icon-accent spinner" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg></div>
            <pre class="code-pre"><code v-html="highlightedCode"></code></pre>
          </div>
        </div>

        <div class="vscode-panel terminal-panel">
          <div class="terminal-titlebar">
            <span>root@nanocar:~/catkin_ws$ bash</span>
          </div>
          <div class="terminal-body custom-scrollbar" ref="terminalContainer">
            <div v-for="(log, idx) in terminalLogs" :key="idx" :class="['term-line', log.type]">{{ log.text }}</div>
          </div>
        </div>
      </section>

      <!-- 弹窗遮罩 -->
      <transition name="fade">
        <div v-if="isDetecting" class="modal-overlay">
          <div class="flex-col align-center">
            <div class="spinner-xl mb-4"></div>
            <h2 class="modal-title">AI 正在深度诊断环境与日志...</h2>
          </div>
        </div>
      </transition>

      <transition name="fade">
        <div v-if="showErrorPopup" class="modal-overlay">
          <div class="vscode-modal border-error">
            <div class="modal-header text-error">
              <svg class="icon icon-md mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
              检测到致命异常
            </div>
            <p class="modal-desc bg-error-light">分析终端日志发现：雷达话题配置错误（应为 /rplidar_scan），且摄像头节点 astrapro_nodelet 因权限被拒绝导致加载失败。</p>
            <button @click="handleRewrite" class="vscode-btn btn-error w-full mt-3">AI 去优化并重写代码</button>
          </div>
        </div>
      </transition>

      <transition name="fade">
        <div v-if="showSuccessPopup" class="modal-overlay">
          <div class="vscode-modal border-success">
            <div class="modal-header text-success">
              <svg class="icon icon-md mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
              诊断通过，部署成功
            </div>
            <p class="modal-desc bg-success-light">AI检测确认：所有硬件节点已正常启动，视频流推流正常，机器人准备就绪。</p>
            <button @click="handleFinish" class="vscode-btn btn-success w-full mt-3">继续任务 (监控机器人运行)</button>
          </div>
        </div>
      </transition>

    </main>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'

const theme = ref('cyan')
const activePrompt = ref('请重新给我一个冰达机器人如何按迹寻路，并实时回传镜头拍摄的图像。要有完整详细的步骤，包括机器人端，电脑端和手机端分别都有哪些具体操作。同时我需要将命令写入冰达机器人，让机器人按事先写好的路线走，并将拍摄的图像无线回传电脑，手机。冰达机器人型号是NanoCar（melodic，ROS1），雷达是rplidar_super，摄像头是astrapro，输出为代码。')
const hasGeneratedV1 = ref(false)
const isCodeLoading = ref(false) 
const canDeployV1 = ref(false)
const canDeployV2 = ref(false)
const isDetecting = ref(false) 
const showErrorPopup = ref(false)
const showSuccessPopup = ref(false)
const hwActive = ref(false)

const rawCode = ref('')
const terminalLogs = ref([])
const codeContainer = ref(null)
const terminalContainer = ref(null)

const mockCpu = ref(15)
const mockRam = ref(1.6)

const codeV1 = `#!/usr/bin/env python
# [Generated by AI] - NanoCar Line Follow & Video Stream (V1)
import rospy
import cv2
import numpy as np
from sensor_msgs.msg import Image, LaserScan
from geometry_msgs.msg import Twist
from cv_bridge import CvBridge, CvBridgeError

class NanoCarLineFollower:
    def __init__(self):
        rospy.init_node('line_follower_stream', anonymous=True)
        self.bridge = CvBridge()
        self.cmd_pub = rospy.Publisher('/cmd_vel', Twist, queue_size=10)
        
        # 订阅摄像头图像 (标准话题)
        rospy.loginfo("Subscribing to camera topic...")
        self.image_sub = rospy.Subscriber('/camera/rgb/image_raw', Image, self.image_cb)
        
        # 订阅雷达数据用于避障保护
        # BUG: 这里使用了默认的 /scan 话题，但冰达实际为 /rplidar_scan
        rospy.loginfo("Subscribing to lidar topic...")
        self.lidar_sub = rospy.Subscriber('/scan', LaserScan, self.lidar_cb)

        self.twist = Twist()
        self.safe_distance = 0.3 # 30cm安全距离

    def image_cb(self, msg):
        try:
            cv_image = self.bridge.imgmsg_to_cv2(msg, "bgr8")
            # 转换为HSV进行黄线识别
            hsv = cv2.cvtColor(cv_image, cv2.COLOR_BGR2HSV)
            lower_yellow = np.array([20, 100, 100])
            upper_yellow = np.array([30, 255, 255])
            mask = cv2.inRange(hsv, lower_yellow, upper_yellow)
            
            # 计算质心并发布速度
            M = cv2.moments(mask)
            if M['m00'] > 0:
                cx = int(M['m10']/M['m00'])
                err = cx - cv_image.shape[1]/2
                self.twist.linear.x = 0.2
                self.twist.angular.z = -float(err) / 100
                self.cmd_pub.publish(self.twist)
                
            # 显示推流画面
            cv2.imshow("NanoCar Camera Stream", cv_image)
            cv2.waitKey(3)
        except CvBridgeError as e:
            rospy.logerr(e)
        
    def lidar_cb(self, msg):
        # 简单的雷达避障逻辑
        front_distances = msg.ranges[len(msg.ranges)//2 - 10 : len(msg.ranges)//2 + 10]
        if min(front_distances) < self.safe_distance:
            self.twist.linear.x = 0
            self.twist.angular.z = 0
            self.cmd_pub.publish(self.twist)
            rospy.logwarn("Obstacle detected! Stopping.")

if __name__ == '__main__':
    try:
        rospy.loginfo("Starting NanoCar Line Follower Node...")
        follower = NanoCarLineFollower()
        rospy.spin()
    except rospy.ROSInterruptException:
        pass`

const codeV2 = `#!/usr/bin/env python
# [Generated by AI] - NanoCar Line Follow & Video Stream (V2 FIXED)
import rospy
import cv2
import numpy as np
import os
from sensor_msgs.msg import Image, LaserScan
from geometry_msgs.msg import Twist
from cv_bridge import CvBridge, CvBridgeError

class NanoCarLineFollower:
    def __init__(self):
        rospy.init_node('line_follower_stream', anonymous=True)
        self.bridge = CvBridge()
        self.cmd_pub = rospy.Publisher('/cmd_vel', Twist, queue_size=10)
        
        # [FIX]: 修复摄像头权限问题，确保 astrapro 节点能读取设备
        rospy.loginfo("Applying hardware permissions...")
        os.system("sudo chmod 777 /dev/video*")
        
        # [FIX]: 修改为正确的 astrapro 图像话题
        self.image_sub = rospy.Subscriber('/astrapro/camera/rgb/image_raw', Image, self.image_cb)
        
        # [FIX]: 修改为冰达 rplidar_super 的正确雷达话题
        rospy.loginfo("Subscribing to correct lidar topic...")
        self.lidar_sub = rospy.Subscriber('/rplidar_scan', LaserScan, self.lidar_cb)

        self.twist = Twist()
        self.safe_distance = 0.3 

    def image_cb(self, msg):
        try:
            cv_image = self.bridge.imgmsg_to_cv2(msg, "bgr8")
            hsv = cv2.cvtColor(cv_image, cv2.COLOR_BGR2HSV)
            lower_yellow = np.array([20, 100, 100])
            upper_yellow = np.array([30, 255, 255])
            mask = cv2.inRange(hsv, lower_yellow, upper_yellow)
            
            M = cv2.moments(mask)
            if M['m00'] > 0:
                cx = int(M['m10']/M['m00'])
                err = cx - cv_image.shape[1]/2
                self.twist.linear.x = 0.2
                self.twist.angular.z = -float(err) / 100
                self.cmd_pub.publish(self.twist)
                
            # [FIX]: 降低分辨率进行无线推流优化
            stream_img = cv2.resize(cv_image, (320, 240))
            cv2.imshow("NanoCar Wireless Stream", stream_img)
            cv2.waitKey(3)
        except CvBridgeError as e:
            rospy.logerr(e)
        
    def lidar_cb(self, msg):
        # 防撞保护
        if not msg.ranges: return
        front_distances = msg.ranges[len(msg.ranges)//2 - 10 : len(msg.ranges)//2 + 10]
        if front_distances and min(front_distances) < self.safe_distance:
            self.twist.linear.x = 0
            self.twist.angular.z = 0
            self.cmd_pub.publish(self.twist)

if __name__ == '__main__':
    try:
        rospy.loginfo("Starting NanoCar Line Follower Node...")
        follower = NanoCarLineFollower()
        rospy.spin()
    except rospy.ROSInterruptException:
        pass`

const highlightedCode = computed(() => {
  if (!rawCode.value) return ''
  let html = rawCode.value
    .replace(/</g, "&lt;").replace(/>/g, "&gt;")
    .replace(/(#.*)/g, '<span class="hl-comment">$1</span>')
    .replace(/\b(import|from|class|def|if|try|except|pass|self|__init__|__name__|not|and|return)\b/g, '<span class="hl-keyword">$1</span>')
    .replace(/(".*?"|'.*?')/g, '<span class="hl-string">$1</span>')
    .replace(/\b([a-zA-Z_]\w*)(?=\()/g, '<span class="hl-func">$1</span>')
  return html
})

const typeWriter = (text) => {
  return new Promise((resolve) => {
    rawCode.value = ''
    let i = 0
    const chunkSize = 12 
    const step = () => {
      rawCode.value += text.slice(i, i + chunkSize)
      i += chunkSize
      if (codeContainer.value) codeContainer.value.scrollTop = codeContainer.value.scrollHeight
      if (i < text.length) requestAnimationFrame(step)
      else resolve()
    }
    requestAnimationFrame(step)
  })
}

const printTerminal = async (logs) => {
  terminalLogs.value = []
  for (const log of logs) {
    await new Promise(r => setTimeout(r, log.delay))
    terminalLogs.value.push({
      text: `[${new Date().toLocaleTimeString('en-US', {hour12:false, fractionalSecondDigits: 3})}] ${log.text}`,
      type: log.type
    })
    nextTick(() => {
      if (terminalContainer.value) terminalContainer.value.scrollTop = terminalContainer.value.scrollHeight
    })
  }
}

const generateV1Code = async () => {
  hasGeneratedV1.value = true
  isCodeLoading.value = true
  await new Promise(r => setTimeout(r, 1500))
  isCodeLoading.value = false
  await typeWriter(codeV1)
  canDeployV1.value = true
}

const deployV1 = async () => {
  canDeployV1.value = false
  const v1Logs = [
    { text: 'Connecting to root@192.168.3.114 via SSH...', type: 'info', delay: 300 },
    { text: 'Authentication successful.', type: 'success', delay: 400 },
    { text: 'Writing line_follow_stream.py to workspace...', type: 'info', delay: 500 },
    { text: 'Executing catkin_make --pkg nanocar_core...', type: 'info', delay: 800 },
    { text: '[100%] Built target line_follower_stream', type: 'success', delay: 600 },
    { text: 'Sourcing /opt/ros/melodic/setup.bash...', type: 'info', delay: 200 },
    { text: 'Launching roslaunch nanocar_core core.launch...', type: 'info', delay: 500 },
    { text: 'Node [/line_follower_stream] started.', type: 'info', delay: 400 },
    { text: '[FATAL] [16234234.123]: Cannot find /scan topic for rplidar_super! Check hardware connection.', type: 'error', delay: 1000 },
    { text: '[FATAL] [16234234.145]: astrapro camera nodelet load failed.', type: 'error', delay: 200 },
    { text: 'Exception thrown: Permission denied on /dev/video0.', type: 'error', delay: 100 },
    { text: 'Process [/line_follower_stream] died [pid 14521, exit code 255].', type: 'warning', delay: 300 }
  ]
  await printTerminal(v1Logs)
  isDetecting.value = true
  await new Promise(r => setTimeout(r, 2000))
  isDetecting.value = false
  showErrorPopup.value = true
}

const handleRewrite = async () => {
  showErrorPopup.value = false 
  theme.value = 'red' 
  activePrompt.value = "发现报错：雷达话题和摄像头权限问题。请修改雷达话题为 /rplidar_scan 并在脚本中确保摄像头节点 astrapro_nodelet 被正确加载赋予权限。"
  await new Promise(r => setTimeout(r, 800))
  isCodeLoading.value = true
  await new Promise(r => setTimeout(r, 1000))
  isCodeLoading.value = false
  await typeWriter(codeV2)
  canDeployV2.value = true
}

const deployV2 = async () => {
  canDeployV2.value = false
  const v2Logs = [
    { text: 'Re-connecting to workspace...', type: 'info', delay: 300 },
    { text: 'Overwriting line_follow_stream.py with AI patch V2...', type: 'info', delay: 500 },
    { text: 'Recompiling workspace...', type: 'info', delay: 800 },
    { text: 'Executing sudo chmod 777 /dev/video*...', type: 'info', delay: 500 },
    { text: 'Permissions granted.', type: 'success', delay: 300 },
    { text: 'Launching roslaunch nanocar_core core.launch...', type: 'info', delay: 500 },
    { text: '[SUCCESS]: /rplidar_scan topic is active and publishing (10Hz).', type: 'success', delay: 1000 },
    { text: '[SUCCESS]: astrapro camera loaded successfully at /dev/video0.', type: 'success', delay: 400 },
    { text: '[SUCCESS]: Video stream broadcasting on port 8080.', type: 'success', delay: 300 },
    { text: 'Robot path following routine active. Hardware OK.', type: 'success', delay: 200 }
  ]
  await printTerminal(v2Logs)
  isDetecting.value = true
  await new Promise(r => setTimeout(r, 2000))
  isDetecting.value = false
  showSuccessPopup.value = true
}

const handleFinish = async () => {
  showSuccessPopup.value = false
  theme.value = 'cyan' 
  activePrompt.value = "系统运行状态极佳。当前执行任务：自动巡线及实时图传反馈。"
  await new Promise(r => setTimeout(r, 500))
  hwActive.value = true
}

onMounted(() => {
  setInterval(() => {
    const baseCpu = hwActive.value ? 65 : 15;
    const baseRam = hwActive.value ? 2.8 : 1.6;
    mockCpu.value = Math.max(5, Math.min(100, baseCpu + Math.floor(Math.random() * 15 - 7)))
    mockRam.value = Number((baseRam + Math.random() * 0.2).toFixed(1))
  }, 1500)
})
</script>

<style scoped>
:root {
  --vscode-bg: #ffffff; --vscode-panel-bg: #f3f3f3; --vscode-border: #e4e6e8;
  --vscode-text: #333333; --vscode-text-muted: #8a8a8a; --vscode-accent: #007acc;
  --vscode-success: #107c10; --vscode-error: #e51400; --vscode-code-bg: #ffffff;
}
.page-wrapper { padding: 16px; height: calc(100vh - 64px); box-sizing: border-box; display: flex; flex-direction: column; background: #ffffff; color: var(--vscode-text); font-family: -apple-system, sans-serif;}

/* Header */
.vscode-header { display: flex; justify-content: space-between; align-items: center; padding: 0 16px 16px 16px; border-bottom: 1px solid var(--vscode-border); margin-bottom: 16px; flex-shrink: 0; }
.header-left, .header-right { display: flex; align-items: center; gap: 12px; }
.header-title { font-size: 16px; font-weight: bold; }
.header-badge { border: 1px solid var(--vscode-border); padding: 2px 6px; border-radius: 4px; font-size: 11px; font-family: monospace; }
.status-indicator { display: flex; align-items: center; gap: 6px; font-size: 12px; font-weight: bold; }
.status-dot { width: 10px; height: 10px; border-radius: 50%; }
.dot-active { background: var(--vscode-success); animation: pulse 2s infinite; }
.dot-waiting { background: #d7ba7d; }

/* Layout */
.ide-layout { display: flex; gap: 16px; height: 100%; min-height: 0; }
.sidebar-column { width: 360px; display: flex; flex-direction: column; gap: 16px; flex-shrink: 0; }
.editor-column { flex: 1; display: flex; flex-direction: column; gap: 16px; min-width: 0; }
.flex-col { display: flex; flex-direction: column; } .flex-1 { flex: 1; min-height: 0;} .w-full { width: 100%; } .mt-3 { margin-top: 12px; } .mt-4 { margin-top: 16px; }
.align-center { align-items: center; } .justify-center { justify-content: center; }

/* Panels */
.vscode-panel { background: var(--vscode-bg); border: 1px solid var(--vscode-border); border-radius: 4px; display: flex; flex-direction: column; overflow: hidden; box-shadow: 0 2px 6px rgba(0,0,0,0.03); }
.panel-titlebar { height: 35px; background: var(--vscode-panel-bg); border-bottom: 1px solid var(--vscode-border); display: flex; align-items: center; padding: 0 12px; font-size: 12px; font-weight: 600; color: #616161; gap: 8px;}
.panel-content { padding: 16px; flex: 1; overflow-y: auto; }

/* Controls */
.vscode-textarea { width: 100%; height: 120px; border: 1px solid var(--vscode-border); border-radius: 2px; padding: 8px; font-size: 13px; color: var(--vscode-text); resize: none; outline: none; background: #fafafa; font-family: inherit;}
.vscode-textarea:focus { border-color: var(--vscode-accent); }
.vscode-btn { padding: 6px 14px; font-size: 13px; font-weight: bold; border-radius: 2px; cursor: pointer; border: 1px solid transparent; display: inline-flex; align-items: center; justify-content: center; transition: 0.2s; }
.btn-primary { background: var(--vscode-accent); color: white; border-color: #005a9e; } .btn-primary:hover { background: #0062a3; }
.btn-success { background: var(--vscode-success); color: white; } .btn-success:hover { background: #0b580b; }
.btn-error { background: var(--vscode-error); color: white; } .btn-error:hover { background: #bd1000; }

.status-banner { padding: 10px; border-radius: 2px; font-size: 12px; font-weight: bold; text-align: center; }
.banner-info { background: #e6f4ff; color: var(--vscode-accent); border: 1px solid #bce0fd; }
.banner-error { background: #fde7e9; color: var(--vscode-error); border: 1px solid #f9c2c6; }

/* Telemetry */
.telemetry-row { display: flex; justify-content: space-between; font-size: 11px; font-family: monospace; color: var(--vscode-text-muted); margin-bottom: 4px; }
.progress-track { width: 100%; height: 6px; background: var(--vscode-border); border-radius: 3px; overflow: hidden; }
.progress-fill { height: 100%; transition: width 0.3s; }
.bg-accent { background: var(--vscode-accent); } .bg-purple { background: #795e26; }

/* Radar */
.radar-container { display: flex; justify-content: center; align-items: center; padding: 16px 0; }
.radar-ring { width: 100px; height: 100px; border-radius: 50%; border: 1px solid var(--vscode-border); position: relative; overflow: hidden; }
.radar-ring-inner { position: absolute; inset: 20px; border-radius: 50%; border: 1px dashed var(--vscode-border); }
.radar-sweep { position: absolute; width: 50px; height: 50px; right: 50%; bottom: 50%; transform-origin: bottom right; animation: spin 2s linear infinite; }
.sweep-active { background: conic-gradient(from 0deg, transparent 70%, rgba(0, 122, 204, 0.4) 100%); }
.sweep-inactive { background: conic-gradient(from 0deg, transparent 70%, rgba(0,0,0,0.05) 100%); }
.radar-target { position: absolute; width: 6px; height: 6px; background: var(--vscode-accent); border-radius: 50%; top: 20px; right: 30px; animation: pulse 1s infinite; }

/* Editor */
.vscode-tabs-header { display: flex; align-items: center; background: var(--vscode-panel-bg); height: 35px; padding-right: 8px; border-bottom: 1px solid var(--vscode-border);}
.mac-controls { display: flex; gap: 6px; padding: 0 12px; }
.mac-dot { width: 10px; height: 10px; border-radius: 50%; } .dot-close { background: #ff5f56; } .dot-min { background: #ffbd2e; } .dot-max { background: #27c93f; }
.vscode-tab { display: flex; align-items: center; padding: 0 16px; height: 100%; font-size: 13px; color: var(--vscode-text); background: var(--vscode-bg); border-right: 1px solid var(--vscode-border); border-top: 2px solid var(--vscode-accent); }
.icon-py { color: #3776ab; margin-right: 6px; }

.vscode-editor-body { flex: 1; overflow-y: auto; background: var(--vscode-code-bg); padding: 12px 16px; font-family: Consolas, monospace; font-size: 13px; line-height: 1.5; position: relative; }
.code-pre { margin: 0; white-space: pre-wrap; word-break: break-all; color: var(--vscode-text); }
.editor-overlay { position: absolute; inset: 0; background: rgba(255,255,255,0.7); display: flex; align-items: center; justify-content: center; }

/* Highlighting */
:deep(.hl-comment) { color: #008000; }
:deep(.hl-keyword) { color: #0000ff; }
:deep(.hl-string) { color: #a31515; }
:deep(.hl-func) { color: #795e26; }

/* Terminal */
.terminal-panel { height: 35%; flex-shrink: 0; }
.terminal-titlebar { background: var(--vscode-panel-bg); padding: 8px 12px; font-size: 12px; font-family: monospace; border-bottom: 1px solid var(--vscode-border); color: var(--vscode-text-muted); }
.terminal-body { flex: 1; overflow-y: auto; background: #ffffff; padding: 12px; font-family: Consolas, monospace; font-size: 12px; line-height: 1.6; }
.term-line { margin-bottom: 2px; }
.info { color: var(--vscode-text); } .success { color: var(--vscode-success); } .error { color: var(--vscode-error); font-weight: bold; } .warning { color: #d7ba7d; }

/* Modals */
.modal-overlay { position: fixed; inset: 0; background: rgba(255,255,255,0.8); backdrop-filter: blur(2px); display: flex; align-items: center; justify-content: center; z-index: 50; }
.vscode-modal { background: var(--vscode-bg); border: 1px solid var(--vscode-border); border-top: 4px solid; border-radius: 4px; padding: 20px; width: 400px; box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
.border-error { border-top-color: var(--vscode-error); } .border-success { border-top-color: var(--vscode-success); }
.modal-header { display: flex; align-items: center; font-size: 16px; font-weight: bold; margin-bottom: 12px; }
.text-error { color: var(--vscode-error); } .text-success { color: var(--vscode-success); }
.modal-desc { font-size: 13px; line-height: 1.5; padding: 8px; border-radius: 2px; color: var(--vscode-text); }
.bg-error-light { background: #fde7e9; border: 1px solid #f9c2c6; } .bg-success-light { background: #eaf6ea; border: 1px solid #c3e6c3; }

/* Utils */
.icon { width: 16px; height: 16px; flex-shrink: 0; vertical-align: middle; }
.icon-sm { width: 18px; height: 18px; } .icon-md { width: 20px; height: 20px; } .icon-lg { width: 32px; height: 32px; }
.icon-accent { color: var(--vscode-accent); }
.spinner { animation: spin 1s linear infinite; } .spinner-xl { width: 40px; height: 40px; border: 3px solid #eee; border-top-color: var(--vscode-accent); border-radius: 50%; animation: spin 1s linear infinite; }
.custom-scrollbar::-webkit-scrollbar { width: 8px; height: 8px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #c1c1c1; }
@keyframes spin { 100% { transform: rotate(360deg); } }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; } .fade-enter-from, .fade-leave-to { opacity: 0; }
</style>