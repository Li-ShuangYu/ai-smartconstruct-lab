<template>
  <div class="page-container">
    <header class="page-header">
      <div class="header-left">
        <svg style="width: 24px; height: 24px; flex-shrink: 0; color: #3b82f6;" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 10l-2 1m0 0l-2-1m2 1v2.5M20 7l-2 1m2-1l-2-1m2 1v2.5M14 4l-2-1-2 1M4 7l2-1M4 7l2 1M4 7v2.5M12 21l-2-1m2 1l2-1m-2 1v-2.5M6 18l-2-1v-2.5M18 18l2-1v-2.5"></path>
        </svg>
        <span class="page-title">冰达 NanoCar 智能调试控制台</span>
        <span class="tag-outline">ROS1 Melodic</span>
      </div>
      <div class="header-right flex-row gap-3">
        <span class="text-sm font-bold text-slate-500">设备连接状态:</span>
        <div class="status-box">
          <span class="status-dot relative flex h-3 w-3">
            <span :class="['animate-ping absolute inline-flex h-full w-full rounded-full opacity-75', hwActive ? 'bg-green' : 'bg-yellow']"></span>
            <span :class="['relative inline-flex rounded-full h-3 w-3', hwActive ? 'bg-green' : 'bg-yellow']"></span>
          </span>
          <span class="text-sm font-bold" :class="hwActive ? 'text-green' : 'text-yellow'">{{ hwActive ? 'ONLINE (ACTIVE)' : 'WAITING' }}</span>
        </div>
      </div>
    </header>

    <main class="main-content layout-grid-aside">
      <aside class="aside-col">
        <div class="glass-card flex-col border-top-thick border-blue">
          <div class="card-header flex-row gap-2">
            <svg style="width: 16px; height: 16px; flex-shrink: 0; color: #3b82f6;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
            <span class="font-bold text-slate-800">AI 任务下发控制台</span>
          </div>
          <div class="card-body gap-4">
            <textarea v-model="activePrompt" class="custom-textarea bg-slate-50 text-slate-700" readonly></textarea>
            <button v-if="!hasGeneratedV1" @click="generateV1Code" class="btn-primary w-full">一键生成 ROS 巡线与图传代码</button>
            <div v-else class="status-banner bg-blue-light text-blue font-bold">当前状态：任务执行中</div>
          </div>
        </div>

        <div class="glass-card flex-col flex-1">
          <div class="card-header"><span class="font-bold text-slate-800">环境与硬件遥测</span></div>
          <div class="card-body gap-6 justify-center">
            <div class="flex-col gap-4">
              <div>
                <div class="flex-between text-xs mb-1 font-mono text-slate-500 font-bold"><span>Jetson Nano CPU</span><span>{{ mockCpu }}%</span></div>
                <div class="progress-bg"><div class="progress-bar bg-blue" :style="{ width: mockCpu + '%' }"></div></div>
              </div>
              <div>
                <div class="flex-between text-xs mb-1 font-mono text-slate-500 font-bold"><span>MEM Usage</span><span>{{ mockRam }} / 4GB</span></div>
                <div class="progress-bg"><div class="progress-bar bg-purple" :style="{ width: (mockRam / 4 * 100) + '%' }"></div></div>
              </div>
            </div>
            <div class="radar-sim-box mt-4">
              <div class="radar-circle">
                <div class="radar-circle-inner-1"></div>
                <div class="radar-circle-inner-2"></div>
                <div :class="['radar-scanner', hwActive ? 'radar-active' : 'radar-inactive']"></div>
                <div v-if="hwActive" class="radar-dot"></div>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <section class="main-col gap-5">
        <div class="glass-card flex-col flex-1 overflow-hidden shadow-sm">
          <div class="code-header flex-between shrink-0">
            <div class="flex-row gap-3">
              <div class="flex-row gap-1.5">
                <div class="w-3 h-3 rounded-full bg-red-500"></div><div class="w-3 h-3 rounded-full bg-yellow-500"></div><div class="w-3 h-3 rounded-full bg-green-500"></div>
              </div>
              <span class="ml-2 text-sm text-slate-300 font-mono font-bold">line_follow_stream.py</span>
            </div>
            <button v-if="canDeployV1" @click="deployV1" class="btn-success btn-sm">
              <svg style="width: 16px; height: 16px; flex-shrink: 0; margin-right: 6px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path></svg>
              一键下载部署到机器人
            </button>
            <button v-if="canDeployV2" @click="deployV2" class="btn-success btn-sm">
              <svg style="width: 16px; height: 16px; flex-shrink: 0; margin-right: 6px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path></svg>
              重新部署 V2 修复代码
            </button>
          </div>
          
          <div class="code-body custom-scrollbar relative" ref="codeContainer">
            <div v-if="isCodeLoading" class="overlay-mask bg-dark-mask">
              <svg style="width: 48px; height: 48px; flex-shrink: 0; color: #3b82f6;" class="spinner" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
            </div>
            <pre class="code-pre"><code class="code-content" v-html="highlightedCode"></code></pre>
          </div>
        </div>

        <div class="terminal-card shrink-0 shadow-sm">
          <div class="terminal-header shrink-0">
            <span class="text-xs text-slate-400 font-mono font-bold">root@nanocar:~/catkin_ws$ bash</span>
          </div>
          <div class="terminal-body custom-scrollbar" ref="terminalContainer">
            <div v-for="(log, idx) in terminalLogs" :key="idx" class="terminal-line" :class="{
                'text-green': log.type === 'success',
                'text-red font-bold': log.type === 'error',
                'text-yellow': log.type === 'warning',
                'text-slate-300': log.type === 'info'
              }">
              {{ log.text }}
            </div>
          </div>
        </div>
      </section>

      <transition name="fade">
        <div v-if="isDetecting" class="modal-overlay">
          <div class="flex-col items-center">
            <div class="spinner-xl border-blue mb-6"></div>
            <h2 class="text-2xl font-black text-slate-800 tracking-widest animate-pulse">AI 正在深度诊断环境与日志...</h2>
          </div>
        </div>
      </transition>

      <transition name="fly-to-left">
        <div v-if="showErrorPopup" class="modal-overlay-transparent">
          <div class="modal-card border-red shadow-red">
            <div class="flex-row mb-4">
              <div class="icon-box-red mr-4"><svg style="width: 24px; height: 24px; flex-shrink: 0;" class="text-red" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg></div>
              <h3 class="text-xl font-bold text-red">检测到致命异常</h3>
            </div>
            <p class="text-sm leading-relaxed mb-6 bg-red-light p-3 rounded font-bold text-red-dark border-red">
              分析终端日志发现：雷达话题配置错误（应为 /rplidar_scan），且摄像头节点 astrapro_nodelet 因权限被拒绝导致加载失败。
            </p>
            <button @click="handleRewrite" class="btn-danger w-full">AI 去优化并重写代码</button>
          </div>
        </div>
      </transition>

      <transition name="fly-to-left">
        <div v-if="showSuccessPopup" class="modal-overlay-transparent">
          <div class="modal-card border-green shadow-green">
            <div class="flex-row mb-4">
              <div class="icon-box-green mr-4"><svg style="width: 24px; height: 24px; flex-shrink: 0;" class="text-green" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg></div>
              <h3 class="text-xl font-bold text-green">诊断通过，部署成功</h3>
            </div>
            <p class="text-sm leading-relaxed mb-6 bg-green-light p-3 rounded font-bold text-green-dark border-green">
              AI检测确认：所有硬件节点已正常启动，视频流推流正常，机器人准备就绪。
            </p>
            <button @click="handleFinish" class="btn-success w-full" style="padding: 12px; font-size: 16px;">继续任务 (监控机器人运行)</button>
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

const codeV1 = `#!/usr/bin/env python\n# [Generated by AI] - NanoCar Line Follow & Video Stream (V1)\nimport rospy\nimport cv2\nimport numpy as np\nfrom sensor_msgs.msg import Image, LaserScan\nfrom geometry_msgs.msg import Twist\nfrom cv_bridge import CvBridge, CvBridgeError\n\nclass NanoCarLineFollower:\n    def __init__(self):\n        rospy.init_node('line_follower_stream', anonymous=True)\n        self.bridge = CvBridge()\n        self.cmd_pub = rospy.Publisher('/cmd_vel', Twist, queue_size=10)\n        \n        # 订阅摄像头图像 (标准话题)\n        rospy.loginfo("Subscribing to camera topic...")\n        self.image_sub = rospy.Subscriber('/camera/rgb/image_raw', Image, self.image_cb)\n        \n        # BUG: 这里使用了默认的 /scan 话题，但冰达实际为 /rplidar_scan\n        rospy.loginfo("Subscribing to lidar topic...")\n        self.lidar_sub = rospy.Subscriber('/scan', LaserScan, self.lidar_cb)\n        self.twist = Twist()\n        self.safe_distance = 0.3 # 30cm安全距离\n\n    def image_cb(self, msg):\n        try:\n            cv_image = self.bridge.imgmsg_to_cv2(msg, "bgr8")\n            hsv = cv2.cvtColor(cv_image, cv2.COLOR_BGR2HSV)\n            lower_yellow = np.array([20, 100, 100])\n            upper_yellow = np.array([30, 255, 255])\n            mask = cv2.inRange(hsv, lower_yellow, upper_yellow)\n            \n            M = cv2.moments(mask)\n            if M['m00'] > 0:\n                cx = int(M['m10']/M['m00'])\n                err = cx - cv_image.shape[1]/2\n                self.twist.linear.x = 0.2\n                self.twist.angular.z = -float(err) / 100\n                self.cmd_pub.publish(self.twist)\n                \n            cv2.imshow("NanoCar Camera Stream", cv_image)\n            cv2.waitKey(3)\n        except CvBridgeError as e:\n            rospy.logerr(e)\n        \n    def lidar_cb(self, msg):\n        front_distances = msg.ranges[len(msg.ranges)//2 - 10 : len(msg.ranges)//2 + 10]\n        if min(front_distances) < self.safe_distance:\n            self.twist.linear.x = 0\n            self.twist.angular.z = 0\n            self.cmd_pub.publish(self.twist)\n            rospy.logwarn("Obstacle detected! Stopping.")\n\nif __name__ == '__main__':\n    try:\n        rospy.loginfo("Starting NanoCar Line Follower Node...")\n        follower = NanoCarLineFollower()\n        rospy.spin()\n    except rospy.ROSInterruptException:\n        pass`

const codeV2 = `#!/usr/bin/env python\n# [Generated by AI] - NanoCar Line Follow & Video Stream (V2 FIXED)\nimport rospy\nimport cv2\nimport numpy as np\nimport os\nfrom sensor_msgs.msg import Image, LaserScan\nfrom geometry_msgs.msg import Twist\nfrom cv_bridge import CvBridge, CvBridgeError\n\nclass NanoCarLineFollower:\n    def __init__(self):\n        rospy.init_node('line_follower_stream', anonymous=True)\n        self.bridge = CvBridge()\n        self.cmd_pub = rospy.Publisher('/cmd_vel', Twist, queue_size=10)\n        \n        # [FIX]: 修复摄像头权限问题\n        rospy.loginfo("Applying hardware permissions...")\n        os.system("sudo chmod 777 /dev/video*")\n        \n        # [FIX]: 修改为正确的 astrapro 图像话题\n        self.image_sub = rospy.Subscriber('/astrapro/camera/rgb/image_raw', Image, self.image_cb)\n        \n        # [FIX]: 修改为正确的雷达话题\n        rospy.loginfo("Subscribing to correct lidar topic...")\n        self.lidar_sub = rospy.Subscriber('/rplidar_scan', LaserScan, self.lidar_cb)\n        self.twist = Twist()\n        self.safe_distance = 0.3 \n\n    def image_cb(self, msg):\n        try:\n            cv_image = self.bridge.imgmsg_to_cv2(msg, "bgr8")\n            hsv = cv2.cvtColor(cv_image, cv2.COLOR_BGR2HSV)\n            lower_yellow = np.array([20, 100, 100])\n            upper_yellow = np.array([30, 255, 255])\n            mask = cv2.inRange(hsv, lower_yellow, upper_yellow)\n            \n            M = cv2.moments(mask)\n            if M['m00'] > 0:\n                cx = int(M['m10']/M['m00'])\n                err = cx - cv_image.shape[1]/2\n                self.twist.linear.x = 0.2\n                self.twist.angular.z = -float(err) / 100\n                self.cmd_pub.publish(self.twist)\n                \n            stream_img = cv2.resize(cv_image, (320, 240))\n            cv2.imshow("NanoCar Wireless Stream", stream_img)\n            cv2.waitKey(3)\n        except CvBridgeError as e:\n            rospy.logerr(e)\n        \n    def lidar_cb(self, msg):\n        if not msg.ranges: return\n        front_distances = msg.ranges[len(msg.ranges)//2 - 10 : len(msg.ranges)//2 + 10]\n        if front_distances and min(front_distances) < self.safe_distance:\n            self.twist.linear.x = 0\n            self.twist.angular.z = 0\n            self.cmd_pub.publish(self.twist)\n\nif __name__ == '__main__':\n    try:\n        rospy.loginfo("Starting NanoCar Line Follower Node...")\n        follower = NanoCarLineFollower()\n        rospy.spin()\n    except rospy.ROSInterruptException:\n        pass`

const highlightedCode = computed(() => {
  if (!rawCode.value) return ''
  let html = rawCode.value
    .replace(/</g, "&lt;").replace(/>/g, "&gt;")
    .replace(/(#.*)/g, '<span style="color: #4ade80; opacity: 0.8;">$1</span>')
    .replace(/\b(import|from|class|def|if|try|except|pass|self|__init__|__name__|not|and|return)\b/g, '<span style="color: #c084fc;">$1</span>')
    .replace(/(".*?"|'.*?')/g, '<span style="color: #fcd34d;">$1</span>')
    .replace(/\b([a-zA-Z_]\w*)(?=\()/g, '<span style="color: #60a5fa;">$1</span>')
    .replace(/\b(rospy|cv2|np|os)\b/g, '<span style="color: #facc15;">$1</span>')
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
  await new Promise(r => setTimeout(r, 2500))
  isDetecting.value = false
  showErrorPopup.value = true
}

const handleRewrite = async () => {
  showErrorPopup.value = false 
  theme.value = 'red' 
  activePrompt.value = "发现报错：雷达话题和摄像头权限问题。请修改雷达话题为 /rplidar_scan 并在脚本中确保摄像头节点 astrapro_nodelet 被正确加载赋予权限。"
  await new Promise(r => setTimeout(r, 1200))
  isCodeLoading.value = true
  await new Promise(r => setTimeout(r, 1500))
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
  await new Promise(r => setTimeout(r, 2500))
  isDetecting.value = false
  showSuccessPopup.value = true
}

const handleFinish = async () => {
  showSuccessPopup.value = false
  theme.value = 'cyan' 
  activePrompt.value = "系统运行状态极佳。当前执行任务：自动巡线及实时图传反馈。"
  await new Promise(r => setTimeout(r, 800))
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
/* 核心容器：白底玻璃化，彻底摆脱黑壳套娃 */
.page-container {height: 100%;min-height: 400px;padding: 0px;display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; }
.page-header { flex-shrink: 0; height: 64px; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); z-index: 10; }
.header-left { display: flex; align-items: center; gap: 12px; }
.page-title { font-size: 18px; font-weight: 800; margin: 0; }
.tag-outline { border: 1px solid #cbd5e1; padding: 4px 8px; border-radius: 6px; font-size: 12px; font-weight: bold; background: white; }
.header-right { display: flex; align-items: center; }
.status-box { display: flex; align-items: center; gap: 8px; }
.status-dot { width: 12px; height: 12px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }

.main-content { flex: 1; overflow-y: auto; padding: 24px; }
.layout-grid-aside { display: grid; grid-template-columns: 420px 1fr; gap: 24px; height: 100%; }

.aside-col { display: flex; flex-direction: column; gap: 20px; }
.main-col { display: flex; flex-direction: column; gap: 20px; flex: 1; min-width: 0; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
.border-top-thick { border-top-width: 4px; border-top-style: solid; }
.flex-col { display: flex; flex-direction: column; } .flex-row { display: flex; align-items: center; } .flex-between { display: flex; justify-content: space-between; align-items: center; }
.flex-1 { flex: 1; } .shrink-0 { flex-shrink: 0; }
.gap-2 { gap: 8px; } .gap-3 { gap: 12px; } .gap-4 { gap: 16px; } .gap-5 { gap: 20px; } .gap-6 { gap: 24px; }
.justify-center { justify-content: center; }

.card-header { padding: 16px 20px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.5); }
.card-body { padding: 20px; display: flex; flex-direction: column; flex: 1; }

.custom-textarea { width: 100%; height: 120px; border: 1px solid #cbd5e1; border-radius: 8px; padding: 12px; font-size: 14px; resize: none; outline: none; }
.btn-primary { background: #3b82f6; color: white; padding: 12px 24px; border: none; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; transition: background 0.2s; }
.btn-primary:hover { background: #2563eb; }
.status-banner { padding: 12px; border-radius: 8px; text-align: center; font-size: 14px; }
.bg-blue-light { background: #dbeafe; } .text-blue { color: #3b82f6; } .border-blue { border-color: #3b82f6; }

.progress-bg { width: 100%; height: 8px; background: #e2e8f0; border-radius: 4px; overflow: hidden; }
.progress-bar { height: 100%; transition: width 0.3s; }
.bg-purple { background: #a855f7; } .bg-green { background: #10b981; } .text-green { color: #10b981; } .bg-yellow { background: #facc15; } .text-yellow { color: #facc15; }
.bg-red { background: #ef4444; } .text-red { color: #ef4444; } .border-red { border-color: #ef4444; }
.bg-red-light { background: #fee2e2; } .text-red-dark { color: #991b1b; } .shadow-red { box-shadow: 0 10px 25px -5px rgba(239, 68, 68, 0.3); }

/* 雷达动画区 */
.radar-sim-box { flex: 1; display: flex; align-items: center; justify-content: center; }
.radar-circle { width: 128px; height: 128px; border-radius: 50%; border: 2px solid #e2e8f0; background: #f8fafc; position: relative; overflow: hidden; }
.radar-circle-inner-1 { position: absolute; inset: 16px; border: 1px solid #cbd5e1; border-radius: 50%; }
.radar-circle-inner-2 { position: absolute; inset: 32px; border: 1px solid #cbd5e1; border-radius: 50%; }
.radar-scanner { position: absolute; width: 64px; height: 64px; right: 50%; bottom: 50%; transform-origin: bottom right; animation: spin 2.5s linear infinite; }
.radar-active { background: conic-gradient(from 0deg, transparent 70%, rgba(16, 185, 129, 0.5) 100%); }
.radar-inactive { background: conic-gradient(from 0deg, transparent 70%, rgba(148, 163, 184, 0.3) 100%); }
.radar-dot { position: absolute; width: 8px; height: 8px; background: #10b981; border-radius: 50%; top: 32px; right: 40px; box-shadow: 0 0 8px #10b981; animation: pulse 2s infinite; }

/* 代码与终端保留深色内核 */
.code-header { background: #1e293b; padding: 10px 16px; border-bottom: 1px solid #334155; }
.btn-success { background: #10b981; color: white; padding: 6px 12px; border: none; border-radius: 6px; font-size: 12px; font-weight: bold; cursor: pointer; display: flex; align-items: center; }
.btn-success:hover { background: #059669; }
.code-body { flex: 1; background: #0f172a; padding: 20px; overflow-y: auto; }
.code-pre { width: 100%; margin: 0; }
.code-content { display: block; width: 100%; font-family: monospace; font-size: 14px; line-height: 1.6; color: #cbd5e1; white-space: pre-wrap; }
.overlay-mask { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; z-index: 10; backdrop-filter: blur(4px); }
.bg-dark-mask { background: rgba(15, 23, 42, 0.8); }

.terminal-card { height: 35%; background: #020617; border-radius: 16px; border: 1px solid #1e293b; display: flex; flex-direction: column; overflow: hidden; }
.terminal-header { background: #0f172a; padding: 8px 16px; border-bottom: 1px solid #1e293b; }
.terminal-body { flex: 1; padding: 16px; font-family: monospace; font-size: 13px; line-height: 1.6; overflow-y: auto; }
.terminal-line { word-break: break-all; margin-bottom: 4px; }

/* 弹窗样式 */
.modal-overlay { position: fixed; inset: 0; background: rgba(255,255,255,0.8); backdrop-filter: blur(8px); display: flex; align-items: center; justify-content: center; z-index: 40; }
.modal-overlay-transparent { position: fixed; inset: 0; display: flex; align-items: center; justify-content: center; z-index: 50; pointer-events: none; }
.modal-card { background: white; border: 1px solid; border-radius: 16px; padding: 32px; width: 500px; pointer-events: auto; }
.spinner-xl { width: 96px; height: 96px; border: 4px solid #e2e8f0; border-top-color: #3b82f6; border-radius: 50%; animation: spin 1.5s linear infinite; }

.icon-box-red { width: 48px; height: 48px; background: #fee2e2; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.btn-danger { background: #ef4444; color: white; padding: 12px 24px; border: none; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; transition: background 0.2s; }
.btn-danger:hover { background: #dc2626; }

.border-green { border-color: #10b981; } .shadow-green { box-shadow: 0 10px 25px -5px rgba(16, 185, 129, 0.3); } .bg-green-light { background: #dcfce7; } .text-green-dark { color: #064e3b; }
.icon-box-green { width: 48px; height: 48px; background: #dcfce7; border-radius: 50%; display: flex; align-items: center; justify-content: center; }

/* SVG 硬性物理约束防爆 */
.icon-sm { width: 16px; height: 16px; flex-shrink: 0; }
.icon-md { width: 24px; height: 24px; flex-shrink: 0; }
.icon-lg { width: 48px; height: 48px; flex-shrink: 0; }
.spinner { border: 4px solid #f1f5f9; border-top-color: #3b82f6; border-radius: 50%; animation: spin 1s linear infinite; }

.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #94a3b8; border-radius: 4px; }
.animate-pulse { animation: pulse 2s infinite; }

@keyframes spin { 100% { transform: rotate(360deg); } }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }
.fade-enter-active, .fade-leave-active { transition: opacity 0.8s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.fly-to-left-enter-active { transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1); }
.fly-to-left-leave-active { transition: all 0.8s cubic-bezier(0.5, 0, 0, 1); }
.fly-to-left-enter-from { opacity: 0; transform: scale(0.8) translateY(30px); }
.fly-to-left-leave-to { opacity: 0; transform: scale(0.3) translate(-80vw, -40vh); }
</style>