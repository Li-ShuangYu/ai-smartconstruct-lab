<template>
  <div>
    <header class="manage-header">
      <h1 class="panel-main-title">实训资源管理控制台</h1>
      <button v-if="currentTab === 'template'" class="primary-btn" @click="$router.push('/teacher/training-create')">
        + 新建实训模板
      </button>
    </header>

    <div class="tabs-nav">
      <div v-for="tab in tabs" :key="tab.key" 
           class="tab-item" :class="{ active: currentTab === tab.key }"
           @click="currentTab = tab.key as TabKey">
        {{ tab.label }}
      </div>
    </div>

    <main class="table-container">
      <div class="data-grid-header">
        <span>实训名称</span>
        <span>时间信息</span>
        <span>当前状态</span>
        <span class="action-header">操作</span>
      </div>
      
      <div class="data-grid-body">
        <transition name="fade" mode="out-in">
          <div :key="currentTab">
            <div v-for="item in tableData[currentTab]" :key="item.id" class="grid-row">
              <span class="name">{{ item.name }}</span>
              <span class="time">{{ item.time }}</span>
              <span>
                <span class="status-tag" :class="item.statusType">{{ item.status }}</span>
              </span>
              
              <div class="actions">
                <template v-if="currentTab === 'template'">
                  <button class="text-btn" @click="handleEditTemplate(item)">编辑</button>
                  <button class="text-btn primary" :disabled="item.status !== '已就绪'" @click="handleCreateTask(item)">
                    创建实训任务
                  </button>
                </template>

                <template v-if="currentTab === 'pending'">
                  <button class="text-btn" @click="handleEditTask(item)">修改信息</button>
                  <button class="text-btn primary" @click="handleStartTask(item)">开启实训</button>
                </template>

                <template v-if="currentTab === 'ongoing'">
                  <button class="text-btn danger" @click="handleEndTask(item)">结束实训</button>
                  <button class="text-btn primary" @click="handleEnterLive(item)">进入实训</button>
                </template>

                <template v-if="currentTab === 'history'">
                  <button class="text-btn primary" @click="handleViewHistory(item)">查看实训内容</button>
                </template>
              </div>
            </div>
            
            <div v-if="tableData[currentTab].length === 0" class="empty-state">
              暂无相关实训数据
            </div>
          </div>
        </transition>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

type TabKey = 'template' | 'pending' | 'ongoing' | 'history'

const currentTab = ref<TabKey>('template')
const tabs = [
  { key: 'template', label: '实训模板仓库' },
  { key: 'pending', label: '待启实训任务' },
  { key: 'ongoing', label: '进行中的实训' },
  { key: 'history', label: '结束实训记录' }
]

// 模拟数据：结合流程图中的状态流转以及密码工程术语
const tableData = ref({
  template: [
    { id: 1, name: '无人机通信抗重放演练', time: '更新于 2026-04-26', status: '已就绪', statusType: 'success' },
    { id: 2, name: '后量子密码学 PQC 算法', time: '更新于 2026-04-27', status: '草稿', statusType: 'info' },
    { id: 3, name: '密码侧信道分析模板', time: '更新于 2026-04-27', status: 'AI处理中', statusType: 'warn' }
  ],
  pending: [
    { id: 101, name: '24级密码工程1班-抗重放实操', time: '计划开始: 2026-04-28', status: '未开始', statusType: 'default' }
  ],
  ongoing: [
    { id: 102, name: '23级信息安全2班-国密算法测试', time: '已运行: 2小时15分', status: '进行中', statusType: 'success' }
  ],
  history: [
    { id: 201, name: '22级网络安全-基础加密实训', time: '结束于 2026-03-15', status: '已结束', statusType: 'default' }
  ]
})

// === 操作逻辑分发 ===

const handleEditTemplate = (item: any) => {
  router.push(`/teacher/training-create?id=${item.id}`)
}

const handleCreateTask = (item: any) => {
  if(item.status !== '已就绪') return
  router.push(`/teacher/training-publish?templateId=${item.id}`)
}

const handleEditTask = (item: any) => {
  console.log('修改实训任务信息', item.id)
  // 跳转到任务修改页
}

const handleStartTask = (item: any) => {
  console.log('手动开启实训', item.id)
  // 状态流转逻辑
}

const handleEndTask = (item: any) => {
  console.log('结束实训', item.id)
  // 状态流转逻辑
}

const handleEnterLive = (item: any) => {
  router.push(`/teacher/teacher-live-monitor?instanceId=${item.id}`)
}

const handleViewHistory = (item: any) => {
  router.push(`/teacher/class-competency/${item.id}`)
}
</script>

<style scoped>
.manage-header { margin-top: -24px; display: flex; justify-content: space-between; align-items: center;}
.panel-main-title { font-size: 24px; font-weight: 800; color: #0F172A; }
.primary-btn { background: #4F46E5; color: white; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; transition: background 0.2s; }
.primary-btn:hover { background: #4338CA; }

.tabs-nav { display: flex; gap: 32px; border-bottom: 1px solid #E2E8F0; margin-bottom: 24px; }
.tab-item { padding: 12px 4px; cursor: pointer; color: #64748B; font-weight: 600; transition: all 0.3s; border-bottom: 2px solid transparent; }
.tab-item.active { color: #4F46E5; border-bottom-color: #4F46E5; }

/* 严格控制网格布局比例和宽度，操作列固定尺寸，单行紧凑对齐，杜绝留白过多 */
.data-grid-header, .grid-row { 
  display: grid; 
  grid-template-columns: 2.5fr 1.5fr 1fr 200px; 
  align-items: center; 
}

.data-grid-header { padding: 12px 24px; background: #F1F5F9; border-radius: 8px; font-weight: 700; color: #475569; }
.action-header { text-align: left; }

.grid-row { padding: 20px 24px; background: white; margin-top: 12px; border-radius: 12px; box-shadow: 0 2px 4px rgba(0,0,0,0.02); transition: background 0.2s;}
.grid-row:hover { background: #F8FAFC; }

.name { font-weight: 600; color: #1E293B; }
.time { font-size: 13px; color: #64748B; }

.status-tag { padding: 4px 10px; border-radius: 6px; font-size: 13px; font-weight: 600; display: inline-block; text-align: center;}
.status-tag.success { background: #DCFCE7; color: #166534; }
.status-tag.warn { background: #FEF3C7; color: #92400E; }
.status-tag.info { background: #DBEAFE; color: #1E40AF; }
.status-tag.default { background: #F1F5F9; color: #475569; }

.actions { display: flex; gap: 12px; align-items: center; justify-content: flex-start;}
.text-btn { background: none; border: none; cursor: pointer; font-size: 14px; font-weight: 600; color: #64748B; padding: 0; transition: color 0.2s;}
.text-btn:hover { color: #334155; }
.text-btn.primary { color: #4F46E5; }
.text-btn.primary:hover { color: #4338CA; }
.text-btn.danger { color: #EF4444; }
.text-btn.danger:hover { color: #DC2626; }
.text-btn:disabled { color: #CBD5E1; cursor: not-allowed; }

.empty-state { padding: 40px; text-align: center; color: #94A3B8; font-size: 14px; }

/* Tab 切换动画 */
.fade-enter-active,
.fade-leave-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; transform: translateY(8px); }
</style>