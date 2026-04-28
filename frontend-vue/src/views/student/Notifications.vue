<template>
  <div class="notifications-container">
    <div class="toolbar">
      <div class="tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.value"
          class="tab-btn"
          :class="{ active: currentTab === tab.value }"
          @click="handleTabChange(tab.value)"
        >
          {{ tab.label }}
          <span v-if="tab.unread > 0" class="badge">{{ tab.unread }}</span>
        </button>
      </div>
      
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索通知内容..." 
          class="search-input" 
        />
      </div>
    </div>

    <div class="message-list">
      <div class="list-header">
        <span class="col-type">类型</span>
        <span class="col-title">通知内容</span>
        <span class="col-time">发送时间</span>
        
        <div class="col-action header-action">
          <span>操作</span>
          <button class="text-btn action-all-read" @click="markAllAsRead">全部已读</button>
        </div>
      </div>
      
      <template v-if="paginatedMessages.length > 0">
        <div 
          v-for="msg in paginatedMessages" 
          :key="msg.id" 
          class="message-row"
          :class="{ 'is-read': msg.isRead }"
        >
          <div class="col-type">
            <span class="type-tag" :class="msg.type">{{ getTypeName(msg.type) }}</span>
          </div>
          <div class="col-title">
            <span class="msg-text" :title="msg.content">{{ msg.content }}</span>
          </div>
          <div class="col-time">{{ msg.time }}</div>
          <div class="col-action">
            <button v-if="!msg.isRead" class="text-btn" @click="markAsRead(msg.id)">标为已读</button>
            <button class="text-btn" @click="viewDetail(msg)">查看详情</button>
          </div>
        </div>
      </template>
      
      <div v-else class="empty-state">
        <span class="empty-icon">📭</span>
        <p>暂无匹配的通知消息</p>
      </div>
    </div>

    <div class="pagination-wrapper" v-if="filteredMessages.length > 0">
      <Pagination 
        :total="filteredMessages.length"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[8, 15, 30]" 
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import Pagination from '@/views/common/Pagination.vue'

type MsgType = 'reminder' | 'evaluation' | 'system'

interface Message {
  id: string
  type: MsgType
  content: string
  time: string
  isRead: boolean
}

// 状态管理
const currentTab = ref<string>('all')
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(8) // 默认每页 8 条

// 扩充模拟数据，以便查看分页效果
const messages = ref<Message[]>([
  { id: '1', type: 'reminder', content: '付老师 提醒您尽快提交实训任务：UAV通信加密系统对抗演练。', time: '2026-04-28 10:30', isRead: false },
  { id: '2', type: 'evaluation', content: '涅槃 刚刚对您的实训报告《抗重放机制分析》给出了评分及评价。', time: '2026-04-28 09:15', isRead: false },
  { id: '3', type: 'system', content: 'AI学苑平台将于今晚 24:00 进行例行升级，新增大模型交互能力。', time: '2026-04-27 18:00', isRead: true },
  { id: '4', type: 'reminder', content: '系统自动催交：您的容器调试实验报告距离截止时间仅剩 2 小时。', time: '2026-04-27 14:20', isRead: true },
  { id: '5', type: 'system', content: '您在《密码系统设计与理论》课程中被分配到第 3 组。', time: '2026-04-26 11:00', isRead: true },
  { id: '6', type: 'evaluation', content: '系统已为您生成近期的「学情数字画像」，点击查看详情。', time: '2026-04-25 16:30', isRead: true },
  { id: '7', type: 'reminder', content: '陈教授 发布了新的实训任务：SM4 算法加密解密编程实战。', time: '2026-04-25 08:00', isRead: true },
  { id: '8', type: 'system', content: '工单提醒：您提交的“容器启动失败”问题已处理完毕。', time: '2026-04-24 15:45', isRead: true },
  { id: '9', type: 'evaluation', content: '您的期中实训测验得分为 92 分，击败了 85% 的同学。', time: '2026-04-22 10:10', isRead: true },
  { id: '10', type: 'system', content: '欢迎加入 AI 学苑智学版！系统为您准备了新手引导任务。', time: '2026-04-20 09:00', isRead: true }
])

// 标签页配置 (动态计算未读数量)
const tabs = computed(() => {
  const unreadCount = (type?: MsgType) => messages.value.filter(m => !m.isRead && (!type || m.type === type)).length
  return [
    { label: '全部消息', value: 'all', unread: unreadCount() },
    { label: '教师催交', value: 'reminder', unread: unreadCount('reminder') },
    { label: '个人评价', value: 'evaluation', unread: unreadCount('evaluation') },
    { label: '系统通知', value: 'system', unread: unreadCount('system') }
  ]
})

// 1. 过滤：按 Tab 分类 + 搜索框模糊匹配
const filteredMessages = computed(() => {
  let list = messages.value
  
  if (currentTab.value !== 'all') {
    list = list.filter(msg => msg.type === currentTab.value)
  }
  
  const query = searchQuery.value.trim().toLowerCase()
  if (query) {
    list = list.filter(msg => msg.content.toLowerCase().includes(query))
  }
  
  return list
})

// 2. 分页：对过滤后的数据进行切片
const paginatedMessages = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredMessages.value.slice(start, end)
})

// 切换 Tab 或 搜索内容改变时，自动回到第一页
const handleTabChange = (val: string) => {
  currentTab.value = val
  currentPage.value = 1
}
watch(searchQuery, () => {
  currentPage.value = 1
})

// 类型文本映射
const getTypeName = (type: MsgType) => {
  const map: Record<MsgType, string> = { reminder: '催交', evaluation: '评价', system: '系统' }
  return map[type]
}

// 操作方法
const markAsRead = (id: string) => {
  const msg = messages.value.find(m => m.id === id)
  if (msg) msg.isRead = true
}

const markAllAsRead = () => {
  // 只标记当前过滤结果下的消息为已读
  filteredMessages.value.forEach(msg => msg.isRead = true)
}

const viewDetail = (msg: Message) => {
  console.log('查看消息详情:', msg)
  if (!msg.isRead) markAsRead(msg.id)
}
</script>

<style scoped>
.notifications-container {
  padding: 24px;
  background-color: var(--color-surface, #ffffff);
  border-radius: 12px; /* 稍微加大一点圆角显得更现代 */
  height: calc(100vh - 120px); /* 让整个容器占满剩余高度 */
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* --- 顶部工具栏 --- */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border, #E5E7EB);
  flex-shrink: 0;
}

.tabs { display: flex; gap: 8px; }

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

.tab-btn:hover { background-color: #f3f4f6; color: #111827; }
.tab-btn.active { background-color: #EEF2FF; color: #4F46E5; font-weight: 600; }

.badge {
  background-color: #EF4444; color: white; font-size: 12px;
  padding: 2px 6px; border-radius: 10px; line-height: 1;
}

/* 搜索框样式 */
.search-box { position: relative; width: 280px; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94A3B8; font-size: 13px; }
.search-input { width: 100%; padding: 8px 12px 8px 32px; border: 1px solid #E2E8F0; border-radius: 6px; font-size: 13px; outline: none; box-sizing: border-box; transition: 0.2s; background: #FAFAFA; }
.search-input:focus { border-color: #4F46E5; background: #FFF; box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1); }

/* --- 列表容器 --- */
.message-list {
  flex: 1;
  overflow-y: auto;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 6px;
  display: flex;
  flex-direction: column;
}

/* 自定义纤细滚动条 */
.message-list::-webkit-scrollbar { width: 4px; }
.message-list::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 4px; }
.message-list::-webkit-scrollbar-thumb:hover { background: #94A3B8; }

/* 核心：扩大了最后一列的宽度，给“全部已读”按钮留出空间 */
.list-header, .message-row {
  display: grid;
  grid-template-columns: 80px 1fr 160px 180px; 
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border, #E5E7EB);
}

.list-header {
  background-color: #F8FAFC;
  font-weight: 600;
  font-size: 13px;
  color: var(--text-secondary, #475569);
  position: sticky;
  top: 0;
  z-index: 10; /* 确保表头吸顶 */
}

/* 表头中操作列的特殊排版 */
.header-action {
  display: flex;
  justify-content: space-between; /* 文字在左，按钮在右 */
  align-items: center;
  width: 100%;
}

.action-all-read {
  font-size: 12px;
  color: #4F46E5;
  background: #EEF2FF;
  padding: 2px 8px;
  border-radius: 4px;
}
.action-all-read:hover {
  background: #E0E7FF;
  text-decoration: none;
}

.message-row {
  font-size: 14px;
  color: var(--text-primary, #111827);
  transition: background-color 0.2s;
}
.message-row:hover { background-color: #F8FAFC; }
.message-row:last-child { border-bottom: none; }

/* 已读状态表现 */
.message-row.is-read { color: var(--text-secondary, #9CA3AF); }
.message-row.is-read .type-tag { opacity: 0.6; }

/* 列内容样式 */
.col-type { text-align: center; }
.col-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding-right: 16px; }
.col-time { color: var(--text-secondary, #64748B); font-size: 13px; }
.col-action { display: flex; gap: 12px; }

/* 标签样式 */
.type-tag { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 500; }
.type-tag.reminder { background-color: #FEF2F2; color: #DC2626; border: 1px solid #FECACA; }
.type-tag.evaluation { background-color: #F0FDF4; color: #16A34A; border: 1px solid #BBF7D0; }
.type-tag.system { background-color: #EFF6FF; color: #2563EB; border: 1px solid #BFDBFE; }

/* 操作按钮样式 */
.text-btn { background: none; border: none; color: var(--color-primary, #4F46E5); cursor: pointer; font-size: 13px; padding: 0; font-weight: 600;}
.text-btn:hover { text-decoration: underline; }

.empty-state { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #94A3B8; font-size: 14px; gap: 8px;}
.empty-icon { font-size: 32px; }

/* 分页容器 */
.pagination-wrapper {
  margin-top: 16px;
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end; /* 右对齐 */
}
</style>