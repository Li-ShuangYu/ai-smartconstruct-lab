<template>
  <div class="notifications-container">
    <div class="toolbar">
      <div class="tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.value"
          class="tab-btn"
          :class="{ active: currentTab === tab.value }"
          @click="currentTab = tab.value"
        >
          {{ tab.label }}
          <span v-if="tab.unread > 0" class="badge">{{ tab.unread }}</span>
        </button>
      </div>
      <div class="actions">
        <button class="action-btn" @click="markAllAsRead">全部已读</button>
      </div>
    </div>

    <div class="message-list">
      <div class="list-header">
        <span class="col-type">类型</span>
        <span class="col-title">通知内容</span>
        <span class="col-time">发送时间</span>
        <span class="col-action">操作</span>
      </div>
      
      <template v-if="filteredMessages.length > 0">
        <div 
          v-for="msg in filteredMessages" 
          :key="msg.id" 
          class="message-row"
          :class="{ 'is-read': msg.isRead }"
        >
          <div class="col-type">
            <span class="type-tag" :class="msg.type">{{ getTypeName(msg.type) }}</span>
          </div>
          <div class="col-title">
            <span class="msg-text">{{ msg.content }}</span>
          </div>
          <div class="col-time">{{ msg.time }}</div>
          <div class="col-action">
            <button v-if="!msg.isRead" class="text-btn" @click="markAsRead(msg.id)">标为已读</button>
            <button class="text-btn" @click="viewDetail(msg)">查看详情</button>
          </div>
        </div>
      </template>
      
      <div v-else class="empty-state">
        暂无该类消息
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

// 消息类型定义
type MsgType = 'reminder' | 'evaluation' | 'system'

interface Message {
  id: string
  type: MsgType
  content: string
  time: string
  isRead: boolean
}

// 模拟数据：对应流程图中的"教师催交、个人评价等"
const messages = ref<Message[]>([
  { id: '1', type: 'reminder', content: '付老师 提醒您尽快提交实训任务：UAV通信加密系统对抗演练。', time: '2026-04-27 10:30', isRead: false },
  { id: '2', type: 'evaluation', content: '涅槃 刚刚对您的实训报告《抗重放机制分析》给出了评分及评价。', time: '2026-04-26 15:45', isRead: false },
  { id: '3', type: 'system', content: '密盾智学平台将于今晚 24:00 进行例行维护，请保存好工作台进度。', time: '2026-04-25 09:00', isRead: true },
  { id: '4', type: 'reminder', content: '系统自动催交：您的容器调试实验报告距离截止时间仅剩 2 小时。', time: '2026-04-24 14:20', isRead: true }
])

const currentTab = ref<string>('all')

// 标签页配置
const tabs = computed(() => {
  const unreadCount = (type?: MsgType) => messages.value.filter(m => !m.isRead && (!type || m.type === type)).length
  return [
    { label: '全部消息', value: 'all', unread: unreadCount() },
    { label: '教师催交', value: 'reminder', unread: unreadCount('reminder') },
    { label: '个人评价', value: 'evaluation', unread: unreadCount('evaluation') },
    { label: '系统通知', value: 'system', unread: unreadCount('system') }
  ]
})

// 根据 Tab 过滤消息
const filteredMessages = computed(() => {
  if (currentTab.value === 'all') return messages.value
  return messages.value.filter(msg => msg.type === currentTab.value)
})

const getTypeName = (type: MsgType) => {
  const map: Record<MsgType, string> = {
    reminder: '催交',
    evaluation: '评价',
    system: '系统'
  }
  return map[type]
}

// 操作方法
const markAsRead = (id: string) => {
  const msg = messages.value.find(m => m.id === id)
  if (msg) msg.isRead = true
}

const markAllAsRead = () => {
  messages.value.forEach(msg => msg.isRead = true)
}

const viewDetail = (msg: Message) => {
  console.log('查看消息详情:', msg)
  if (!msg.isRead) markAsRead(msg.id)
  // 此处可扩展弹窗或路由跳转逻辑
}
</script>

<style scoped>
.notifications-container {
  padding: 24px;
  background-color: var(--color-surface, #ffffff);
  border-radius: 8px;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 顶部工具栏：紧凑单行 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border, #E5E7EB);
}

.tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  background: none;
  border: 1px solid transparent;
  padding: 6px 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary, #6B7280);
  cursor: pointer;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.tab-btn:hover {
  background-color: var(--color-border, #f3f4f6);
  color: var(--text-primary, #111827);
}

.tab-btn.active {
  background-color: var(--color-primary-light, #EEF2FF);
  color: var(--color-primary, #4F46E5);
  font-weight: 600;
}

.badge {
  background-color: #EF4444;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  line-height: 1;
}

.action-btn {
  padding: 6px 16px;
  font-size: 14px;
  border: 1px solid var(--color-border, #D1D5DB);
  background-color: #fff;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background-color: #F9FAFB;
  border-color: #9CA3AF;
}

/* 列表容器 */
.message-list {
  flex: 1;
  overflow-y: auto;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 6px;
}

/* 核心：严格的 CSS Grid 布局控制每行元素，拒绝过多留白 */
.list-header, .message-row {
  display: grid;
  grid-template-columns: 80px 1fr 160px 140px; /* 精确分配每列宽度 */
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border, #E5E7EB);
}

.list-header {
  background-color: #F9FAFB;
  font-weight: 600;
  font-size: 13px;
  color: var(--text-secondary, #4B5563);
  position: sticky;
  top: 0;
  z-index: 1;
}

.message-row {
  font-size: 14px;
  color: var(--text-primary, #111827);
  transition: background-color 0.2s;
}

.message-row:hover {
  background-color: #F9FAFB;
}

.message-row:last-child {
  border-bottom: none;
}

/* 已读状态表现 */
.message-row.is-read {
  color: var(--text-secondary, #9CA3AF);
}

.message-row.is-read .type-tag {
  opacity: 0.6;
}

/* 列内容样式 */
.col-type { text-align: center; }
.col-title { 
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap; 
  padding-right: 16px;
}
.col-time { color: var(--text-secondary, #6B7280); font-size: 13px; }
.col-action { display: flex; gap: 12px; }

/* 标签样式 */
.type-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
.type-tag.reminder { background-color: #FEF2F2; color: #DC2626; border: 1px solid #FECACA; }
.type-tag.evaluation { background-color: #F0FDF4; color: #16A34A; border: 1px solid #BBF7D0; }
.type-tag.system { background-color: #EFF6FF; color: #2563EB; border: 1px solid #BFDBFE; }

/* 操作按钮样式 */
.text-btn {
  background: none;
  border: none;
  color: var(--color-primary, #4F46E5);
  cursor: pointer;
  font-size: 13px;
  padding: 0;
}

.text-btn:hover {
  text-decoration: underline;
}

.empty-state {
  padding: 48px;
  text-align: center;
  color: var(--text-secondary, #9CA3AF);
  font-size: 14px;
}
</style>