<template>
  <div>
    <header class="manage-header">
      <h1 class="panel-main-title">实训资源管理控制台</h1>
      <button class="primary-btn" @click="$router.push('/teacher/training-create')">+ 新建实训模板</button>
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
        <span>名称</span><span>创建时间</span><span>状态</span><span>操作</span>
      </div>
      <div class="data-grid-body">
        <div v-for="item in tableData[currentTab]" :key="item.id" class="grid-row">
          <span class="name">{{ item.name }}</span>
          <span class="time">{{ item.time }}</span>
          <span>
            <span class="status-tag" :class="item.statusType">{{ item.status }}</span>
          </span>
          <div class="actions">
            <button class="text-btn" @click="handleEdit(item)">编辑</button>
            <button class="text-btn primary" @click="handleAction(item)">
              {{ currentTab === 'template' ? '开始实训' : currentTab === 'instance' ? '进入监控' : '查看结果' }}
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

type TabKey = 'template' | 'instance' | 'history'

const currentTab = ref<TabKey>('template')
const tabs = [
  { key: 'template', label: '实训模板库' },
  { key: 'instance', label: '进行中副本' },
  { key: 'history', label: '历史实训记录' }
]

const tableData = ref({
  template: [
    { id: 1, name: '无人机通信抗重放攻击实训', time: '2026-04-20', status: '已就绪', statusType: 'success' },
    { id: 2, name: '后量子密码学 PQC 算法演练', time: '2026-04-22', status: '草稿', statusType: 'info' }
  ],
  instance: [
    { id: 101, name: '24级密码工程1班-抗重放实操', time: '2026-04-25', status: '运行中', statusType: 'warn' }
  ],
  history: [
    { id: 201, name: '23级信息安全2班-国密算法测试', time: '2026-03-15', status: '已归档', statusType: 'default' }
  ]
})

const handleEdit = (item: any) => {
  router.push(`/teacher/training-create?id=${item.id}`)
}

const handleAction = (item: any) => {
  if (currentTab.value === 'template') {
    router.push(`/teacher/training-publish?templateId=${item.id}`)
  } else if (currentTab.value === 'instance') {
    router.push(`/teacher/teacher-live-monitor?instanceId=${item.id}`)
  } else {
    router.push(`/teacher/class-competency/${item.id}`)
  }
}
</script>

<style scoped>
.manage-header { margin-top: -24px; display: flex; justify-content: space-between; align-items: center;}
.panel-main-title { font-size: 24px; font-weight: 800; color: #0F172A; }
.primary-btn { background: #4F46E5; color: white; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; }
.tabs-nav { display: flex; gap: 32px; border-bottom: 1px solid #E2E8F0; margin-bottom: 24px; }
.tab-item { padding: 12px 4px; cursor: pointer; color: #64748B; font-weight: 600; transition: all 0.3s; border-bottom: 2px solid transparent; }
.tab-item.active { color: #4F46E5; border-bottom-color: #4F46E5; }
.data-grid-header { display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; padding: 12px 24px; background: #F1F5F9; border-radius: 8px; font-weight: 700; color: #475569; }
.grid-row { display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; padding: 20px 24px; background: white; margin-top: 12px; border-radius: 12px; align-items: center; box-shadow: 0 2px 4px rgba(0,0,0,0.02); }
.status-tag { padding: 4px 10px; border-radius: 6px; font-size: 13px; font-weight: 600; }
.status-tag.success { background: #DCFCE7; color: #166534; }
.status-tag.warn { background: #FEF3C7; color: #92400E; }
.actions { display: flex; gap: 16px; }
.text-btn { background: none; border: none; cursor: pointer; font-weight: 600; color: #64748B; }
.text-btn.primary { color: #4F46E5; }
</style>