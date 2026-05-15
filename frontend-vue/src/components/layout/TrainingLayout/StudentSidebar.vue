<template>
  <div class="sidebar-wrapper">
    <div 
      class="sidebar-trigger" 
      :class="{ 'trigger-hidden': isExpanded }"
      @click="toggleSidebar"
    >
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="9 18 15 12 9 6"></polyline>
      </svg>
    </div>

    <transition name="slide-fade">
      <div v-if="isExpanded" class="floating-sidebar">
        <div class="sidebar-header">
          <span class="title">工具栏</span>
          <svg @click="toggleSidebar" class="close-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"></polyline>
          </svg>
        </div>
        <div class="sidebar-menu">
          <button v-for="btn in buttons" :key="btn.id" class="menu-btn" @click="openModal(btn.id)">
            {{ btn.label }}
          </button>
        </div>
      </div>
    </transition>

    <transition name="modal-fade">
      <div v-if="activeModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal-container">
          <div class="modal-header">
            <h3>{{ currentModalTitle }}</h3>
            <button class="modal-close" @click="closeModal">&times;</button>
          </div>
          
          <div class="modal-body">
            <div v-if="activeModal === 'messages'" class="msg-list">
              <div class="msg-item" v-for="i in 3" :key="i">
                <span class="msg-sender">教师系统</span>
                <span class="msg-content">您有一项新的实训任务待查看。</span>
              </div>
            </div>

            <div v-if="activeModal === 'send'" class="send-form">
              <textarea placeholder="输入发送给老师的消息内容..." rows="4"></textarea>
              <div class="file-upload">
                <label class="upload-btn">
                  <input type="file" hidden />
                  📎 附带文件
                </label>
                <span class="upload-tip">支持上传附件</span>
              </div>
              <button class="submit-btn">发送消息</button>
            </div>

            <div v-if="activeModal === 'content'" class="training-content">
              <h4>当前实训任务：智能小车控制系统开发</h4>
              <p>请根据需求文档，完成底层驱动代码的编写与仿真测试...</p>
            </div>

            <div v-if="activeModal === 'progress'" class="progress-container">
              <ul class="timeline">
                <li v-for="(node, index) in progressNodes" :key="index" :class="['timeline-item', { active: index <= currentStep }]">
                  <div class="timeline-marker"></div>
                  <div class="timeline-content">{{ node }}</div>
                </li>
              </ul>
            </div>

            <div v-if="activeModal === 'notebook'" class="notebook-layout">
              <div class="note-list">
                <div class="note-item active">环境配置笔记</div>
                <div class="note-item">Bug排查记录</div>
              </div>
              <div class="note-detail">
                <textarea class="note-editor" placeholder="在此记录你的实训笔记..."></textarea>
              </div>
            </div>

            <div v-if="activeModal === 'leave'" class="leave-confirm">
              <p>确定要暂离当前实训环境吗？进度已自动保存。</p>
              <div class="action-btns">
                <button class="btn-cancel" @click="closeModal">取消</button>
                <button class="btn-confirm" @click="confirmLeave">确定暂离</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const isExpanded = ref(false)
const activeModal = ref(null)
const currentStep = ref(4) // 模拟当前进度在第5步(索引4)

const toggleSidebar = () => {
  isExpanded.value = !isExpanded.value
}

const buttons = [
  { id: 'messages', label: '我的消息' },
  { id: 'send', label: '发消息' },
  { id: 'content', label: '实训内容' },
  { id: 'progress', label: '实训进度' },
  { id: 'notebook', label: 'notebook智记' },
  { id: 'leave', label: '暂离实训' }
]

const progressNodes = [
  '开始实训', '课件阅读', '视频观看', '思维导图预习与评价', '任务下发',
  '理论实训', 'AI 对练', '思维导图绘制练习', '需求上传', '方案上传',
  '编码实训', '课后作业', '学生匿名互评', '教师点评', '结束实训'
]

const currentModalTitle = computed(() => {
  const btn = buttons.find(b => b.id === activeModal.value)
  return btn ? btn.label : ''
})

const openModal = (id) => {
  activeModal.value = id
  isExpanded.value = false // 点击按钮后自动收起侧边栏，保持界面清爽
}

const closeModal = () => {
  activeModal.value = null
}

const confirmLeave = () => {
  console.log('执行暂离逻辑')
  closeModal()
}
</script>

<style scoped>
/* 悬浮触点 */
.sidebar-trigger {
  position: fixed;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 60px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-left: none;
  border-radius: 0 8px 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 4px 0 10px rgba(0, 0, 0, 0.05);
  z-index: 40;
  color: #64748B;
  transition: all 0.3s;
}
.sidebar-trigger:hover { background: #F8FAFC; color: #4F46E5; }
.trigger-hidden { left: -30px; } /* 展开时隐藏突起 */

/* 悬浮侧边栏 */
.floating-sidebar {
  position: fixed;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  width: 140px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  z-index: 50;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #F1F5F9;
  font-weight: bold;
  color: #1E293B;
  font-size: 14px;
}
.close-icon { width: 16px; height: 16px; cursor: pointer; color: #94A3B8; transition: color 0.2s;}
.close-icon:hover { color: #4F46E5; }

.sidebar-menu { display: flex; flex-direction: column; padding: 8px; gap: 4px; }
.menu-btn {
  background: transparent; border: none; text-align: left; padding: 10px 12px;
  border-radius: 6px; cursor: pointer; color: #475569; font-size: 13px; font-weight: 500;
  transition: all 0.2s;
}
.menu-btn:hover { background: #EEF2FF; color: #4F46E5; }

/* 弹窗及遮罩 */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(2px); z-index: 100;
  display: flex; align-items: center; justify-content: center;
}
.modal-container {
  background: #fff; width: 560px; max-height: 80vh; border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); display: flex; flex-direction: column;
  overflow: hidden;
}
.modal-header {
  padding: 16px 20px; border-bottom: 1px solid #E2E8F0;
  display: flex; justify-content: space-between; align-items: center;
}
.modal-header h3 { margin: 0; font-size: 16px; color: #1E293B; }
.modal-close { background: none; border: none; font-size: 24px; line-height: 1; cursor: pointer; color: #94A3B8; }
.modal-body { padding: 20px; overflow-y: auto; }

/* 弹窗内部特有样式 */
.send-form { display: flex; flex-direction: column; gap: 12px; }
.send-form textarea, .note-editor { 
  width: 100%; padding: 12px; border: 1px solid #E2E8F0; border-radius: 8px; outline: none; resize: vertical;
}
.send-form textarea:focus, .note-editor:focus { border-color: #4F46E5; }
.file-upload { display: flex; align-items: center; gap: 12px; }
.upload-btn { background: #F1F5F9; padding: 6px 12px; border-radius: 6px; cursor: pointer; font-size: 13px; color: #475569; }
.upload-tip { font-size: 12px; color: #94A3B8; }
.submit-btn { align-self: flex-end; background: #4F46E5; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }

/* 线性进度条 */
.timeline { list-style: none; padding: 0; margin: 0; position: relative; }
.timeline::before {
  content: ''; position: absolute; left: 7px; top: 0; bottom: 0; width: 2px; background: #E2E8F0;
}
.timeline-item { position: relative; padding-left: 24px; padding-bottom: 16px; font-size: 14px; color: #64748B; }
.timeline-item.active { color: #4F46E5; font-weight: bold; }
.timeline-item:last-child { padding-bottom: 0; }
.timeline-marker {
  position: absolute; left: 3px; top: 5px; width: 10px; height: 10px; border-radius: 50%;
  background: #fff; border: 2px solid #CBD5E1; z-index: 1;
}
.timeline-item.active .timeline-marker { border-color: #4F46E5; background: #4F46E5; }

/* 智记分栏 */
.notebook-layout { display: flex; gap: 16px; height: 300px; }
.note-list { width: 150px; border-right: 1px solid #E2E8F0; display: flex; flex-direction: column; gap: 4px; padding-right: 8px; }
.note-item { padding: 8px; font-size: 13px; border-radius: 6px; cursor: pointer; color: #475569; }
.note-item.active { background: #EEF2FF; color: #4F46E5; font-weight: 500; }
.note-detail { flex: 1; display: flex; }
.note-editor { flex: 1; border: none; resize: none; background: #F8FAFC; }

/* 暂离按钮 */
.action-btns { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; }
.btn-cancel { background: #F1F5F9; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
.btn-confirm { background: #EF4444; color: white; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }

/* 过渡动画 */
.slide-fade-enter-active, .slide-fade-leave-active { transition: all 0.3s ease; }
.slide-fade-enter-from, .slide-fade-leave-to { opacity: 0; transform: translate(-20px, -50%); }
.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.2s; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
</style>