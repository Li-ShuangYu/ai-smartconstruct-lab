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
          <span class="title">教师工具</span>
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
              <div class="msg-item" v-for="i in 2" :key="i">
                <span class="msg-sender">学生张三</span>
                <span class="msg-content">老师，关于任务下发环节有些疑问想请教...</span>
              </div>
            </div>

            <div v-if="activeModal === 'send'" class="send-form">
              <div class="target-selection">
                <label><input type="radio" value="global" v-model="msgTarget" /> 全局发送 (所有学生)</label>
                <label><input type="radio" value="specific" v-model="msgTarget" /> 指定学生</label>
              </div>
              
              <div v-if="msgTarget === 'specific'" class="student-select">
                <select class="form-select">
                  <option value="">-- 请选择学生 --</option>
                  <option value="1">张三</option>
                  <option value="2">李四</option>
                </select>
              </div>

              <textarea placeholder="输入通知或消息内容..." rows="4"></textarea>
              
              <div class="file-upload">
                <label class="upload-btn">
                  <input type="file" hidden />
                  📎 附带文件
                </label>
                <span class="upload-tip">支持上传课件/参考资料</span>
              </div>
              
              <button class="submit-btn">发送</button>
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
const msgTarget = ref('global') // 默认全局发送

const toggleSidebar = () => {
  isExpanded.value = !isExpanded.value
}

const buttons = [
  { id: 'messages', label: '我的消息' },
  { id: 'send', label: '发消息' }
]

const currentModalTitle = computed(() => {
  const btn = buttons.find(b => b.id === activeModal.value)
  return btn ? btn.label : ''
})

const openModal = (id) => {
  activeModal.value = id
  isExpanded.value = false
}

const closeModal = () => {
  activeModal.value = null
  msgTarget.value = 'global' // 重置表单状态
}
</script>

<style scoped>
/* 共享样式：突起触点、侧边栏、弹窗基础、过渡等均与学生端保持一致 */
.sidebar-trigger {
  position: fixed; left: 0; top: 50%; transform: translateY(-50%);
  width: 24px; height: 60px; background: #fff; border: 1px solid #E2E8F0;
  border-left: none; border-radius: 0 8px 8px 0; display: flex; align-items: center;
  justify-content: center; cursor: pointer; box-shadow: 4px 0 10px rgba(0, 0, 0, 0.05);
  z-index: 40; color: #64748B; transition: all 0.3s;
}
.sidebar-trigger:hover { background: #F8FAFC; color: #4F46E5; }
.trigger-hidden { left: -30px; }

.floating-sidebar {
  position: fixed; left: 20px; top: 50%; transform: translateY(-50%);
  width: 140px; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(8px);
  border: 1px solid #E2E8F0; border-radius: 12px; box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  z-index: 50; overflow: hidden; display: flex; flex-direction: column;
}
.sidebar-header { padding: 12px 16px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #F1F5F9; font-weight: bold; color: #1E293B; font-size: 14px; }
.close-icon { width: 16px; height: 16px; cursor: pointer; color: #94A3B8; }
.close-icon:hover { color: #4F46E5; }

.sidebar-menu { display: flex; flex-direction: column; padding: 8px; gap: 4px; }
.menu-btn { background: transparent; border: none; text-align: left; padding: 10px 12px; border-radius: 6px; cursor: pointer; color: #475569; font-size: 13px; font-weight: 500; transition: all 0.2s; }
.menu-btn:hover { background: #EEF2FF; color: #4F46E5; }

.modal-overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.4); backdrop-filter: blur(2px); z-index: 100; display: flex; align-items: center; justify-content: center; }
.modal-container { background: #fff; width: 500px; border-radius: 12px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); overflow: hidden; }
.modal-header { padding: 16px 20px; border-bottom: 1px solid #E2E8F0; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 16px; }
.modal-close { background: none; border: none; font-size: 24px; cursor: pointer; color: #94A3B8; }
.modal-body { padding: 20px; }

/* 教师专属发消息样式 */
.send-form { display: flex; flex-direction: column; gap: 14px; }
.target-selection { display: flex; gap: 16px; font-size: 14px; color: #1E293B; }
.target-selection label { cursor: pointer; display: flex; align-items: center; gap: 4px; }
.form-select { width: 100%; padding: 8px; border: 1px solid #E2E8F0; border-radius: 6px; outline: none; }
.send-form textarea { width: 100%; padding: 12px; border: 1px solid #E2E8F0; border-radius: 8px; outline: none; resize: vertical; }
.send-form textarea:focus, .form-select:focus { border-color: #4F46E5; }
.file-upload { display: flex; align-items: center; gap: 12px; }
.upload-btn { background: #F1F5F9; padding: 6px 12px; border-radius: 6px; cursor: pointer; font-size: 13px; color: #475569; }
.submit-btn { align-self: flex-end; background: #4F46E5; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }

/* 消息列表基础样式 */
.msg-item { padding: 12px; border-bottom: 1px solid #F1F5F9; display: flex; flex-direction: column; gap: 4px; }
.msg-sender { font-size: 12px; font-weight: bold; color: #4F46E5; }
.msg-content { font-size: 14px; color: #475569; }

.slide-fade-enter-active, .slide-fade-leave-active { transition: all 0.3s ease; }
.slide-fade-enter-from, .slide-fade-leave-to { opacity: 0; transform: translate(-20px, -50%); }
.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.2s; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
</style>