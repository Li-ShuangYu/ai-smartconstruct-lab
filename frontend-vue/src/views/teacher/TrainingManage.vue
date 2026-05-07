<template>
  <div>
    <header class="manage-header">
      <h1 class="panel-main-title">{{ pageLabel }}</h1>
      <div class="header-actions">
        <n-button v-if="mainTab === 'template'" type="primary" @click="$router.push('/teacher/training-create')">+ 新建实训模板</n-button>
        <n-button v-if="mainTab === 'template'" quaternary @click="loadTemplates" :loading="loading" style="margin-left: 8px">刷新</n-button>
      </div>
    </header>

    <div class="tabs-nav">
      <div class="tab-item" :class="{ active: mainTab === 'template' }" @click="switchMainTab('template')">实训模板仓库</div>
      <div class="tab-item" :class="{ active: mainTab === 'task' }" @click="switchMainTab('task')">实训任务</div>
    </div>

    <div v-if="mainTab === 'task'" class="sub-tabs">
      <div v-for="tab in taskTabs" :key="tab.key" class="sub-tab-item" :class="{ active: taskTab === tab.key }" @click="switchTaskTab(tab.key)">{{ tab.label }}</div>
    </div>

    <n-spin :show="loading">
      <!-- 模板列表 -->
      <main v-if="mainTab === 'template'" class="table-container">
        <div class="data-grid-header">
          <span>模板名称</span>
          <span>AI 状态</span>
          <span>创建时间</span>
          <span class="action-header">操作</span>
        </div>
        <div class="data-grid-body">
          <div v-for="tpl in templates" :key="tpl.id" class="grid-row">
            <span class="name">{{ tpl.templateName }}</span>
            <span>
              <n-tag :type="aiStatusTag(tpl.aiStatus)" size="small">
                <template v-if="tpl.aiStatus === 1"><n-spin :size="14" /> </template>
                {{ aiStatusLabel(tpl.aiStatus) }}
              </n-tag>
            </span>
            <span class="time">{{ formatTime(tpl.createdAt) }}</span>
            <div class="actions">
              <button v-if="tpl.aiStatus === 2" class="text-btn primary" @click="$router.push(`/teacher/training-publish?templateId=${tpl.id}`)">创建实训任务</button>
              <button v-if="tpl.aiStatus === 3" class="text-btn" @click="showError(tpl)">查看原因</button>
              <button class="text-btn danger" @click="handleDeleteTemplate(tpl)">删除</button>
            </div>
          </div>
          <div v-if="!loading && templates.length === 0" class="empty-state">暂无模板数据</div>
        </div>
        <div class="pagination-wrapper" v-if="tplTotal > 0">
          <n-pagination v-model:page="tplPage" :page-size="tplPageSize" :item-count="tplTotal" :page-sizes="[5,10,20]" show-size-picker
            @update:page="loadTemplates" @update:page-size="(s: number) => { tplPageSize = s; tplPage = 1; loadTemplates() }" />
        </div>
      </main>

      <!-- 任务列表 -->
      <main v-if="mainTab === 'task'" class="table-container">
        <div class="data-grid-header">
          <span>实训名称</span>
          <span>时间信息</span>
          <span>当前状态</span>
          <span class="action-header">操作</span>
        </div>
        <div class="data-grid-body">
          <div v-for="item in tasks" :key="item.id" class="grid-row">
            <span class="name">{{ item.taskName || '未命名实训' }}</span>
            <span class="time">{{ formatTime(item.startTime) }}</span>
            <span><span class="status-tag" :class="taskStatusClass(item.status)">{{ taskStatusLabel(item.status) }}</span></span>
            <div class="actions">
              <template v-if="item.status === 0">
                <button class="text-btn primary" @click="$router.push(`/teacher/training-publish?taskId=${item.id}`)">开启实训</button>
              </template>
              <template v-if="item.status === 1">
                <button class="text-btn danger" @click="handleEndTask(item)">结束实训</button>
                <button class="text-btn primary" @click="$router.push(`/teacher/teacher-live-monitor?taskId=${item.id}`)">进入实训</button>
              </template>
              <template v-if="item.status === 2">
                <button class="text-btn primary" @click="$router.push(`/teacher/class-competency/${item.id}`)">查看详情</button>
              </template>
            </div>
          </div>
          <div v-if="!loading && tasks.length === 0" class="empty-state">暂无相关实训数据</div>
        </div>
        <div class="pagination-wrapper" v-if="taskTotal > 0">
          <n-pagination v-model:page="taskPage" :page-size="taskPageSize" :item-count="taskTotal" :page-sizes="[5,10,20]" show-size-picker
            @update:page="loadTasks" @update:page-size="(s: number) => { taskPageSize = s; taskPage = 1; loadTasks() }" />
        </div>
      </main>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { NButton, NPagination, NSpin, NTag, useMessage } from 'naive-ui'
import { getTemplates, deleteTemplate } from '@/services/modules/teacher-template.service'
import { getTrainingTasks } from '@/services/modules/teacher-dashboard.service'
import type { TrainingTemplate } from '@/services/types/template.types'
import type { TrainingTaskItem } from '@/services/types/dashboard.types'

const message = useMessage()
const loading = ref(false)

const mainTab = ref('template')
const pageLabel = computed(() => mainTab.value === 'template' ? '实训模板仓库' : '实训任务管理')

// --- 模板 ---
const templates = ref<TrainingTemplate[]>([])
const tplPage = ref(1); const tplPageSize = ref(5); const tplTotal = ref(0)
let pollTimer: ReturnType<typeof setInterval> | undefined

function aiStatusLabel(s: number) { if (s === 0) return '草稿'; if (s === 1) return 'AI处理中'; if (s === 2) return '已就绪'; return '异常' }
function aiStatusTag(s: number): 'default' | 'warning' | 'success' | 'error' { if (s === 0) return 'default'; if (s === 1) return 'warning'; if (s === 2) return 'success'; return 'error' }

// --- 任务 ---
const tasks = ref<TrainingTaskItem[]>([])
const taskTab = ref(0)
const taskPage = ref(1); const taskPageSize = ref(5); const taskTotal = ref(0)
const taskTabs = [{ label: '全部实训', key: 0 }, { label: '未开始', key: 1 }, { label: '进行中', key: 2 }, { label: '已结束', key: 3 }]

function taskStatusLabel(s: number) { if (s === 0) return '未开始'; if (s === 1) return '进行中'; return '已结束' }
function taskStatusClass(s: number) { return s === 1 ? 'success' : 'default' }
function formatTime(t?: string) { if (!t) return '-'; return t.slice(0, 16).replace('T', ' ') }

async function loadTemplates() {
  loading.value = true
  try {
    const res = await getTemplates(tplPage.value, tplPageSize.value)
    if (res.code === 200) { templates.value = res.data.records; tplTotal.value = res.data.total }
  } finally { loading.value = false }
}

function startPolling() {
  stopPolling()
  pollTimer = setInterval(() => {
    const hasProcessing = templates.value.some(t => t.aiStatus === 1)
    if (hasProcessing) loadTemplates()
  }, 4000)
}
function stopPolling() { if (pollTimer) { clearInterval(pollTimer); pollTimer = undefined } }

async function handleDeleteTemplate(tpl: TrainingTemplate) {
  if (!tpl.id) return
  try { await deleteTemplate(tpl.id); message.success('模板已删除'); loadTemplates() } catch { message.error('删除失败') }
}
function showError(tpl: TrainingTemplate) { message.error(tpl.errorReason || 'AI处理异常，请重试') }

async function loadTasks() {
  loading.value = true
  try {
    let s: number | undefined
    if (taskTab.value === 1) s = 0; else if (taskTab.value === 2) s = 1; else if (taskTab.value === 3) s = 2
    const res = await getTrainingTasks(taskPage.value, taskPageSize.value, s)
    if (res.code === 200) { tasks.value = res.data.records; taskTotal.value = res.data.total }
  } finally { loading.value = false }
}

function switchMainTab(t: string) { mainTab.value = t; if (t === 'template') { loadTemplates(); startPolling() } else { stopPolling(); loadTasks() } }
function switchTaskTab(k: number) { taskTab.value = k; taskPage.value = 1; loadTasks() }
function handleEndTask(_item: TrainingTaskItem) { message.info('结束实训功能待实现') }

onMounted(() => { loadTemplates(); startPolling() })
onUnmounted(() => stopPolling())
</script>

<style scoped>
.manage-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; align-items: center; }
.panel-main-title { font-size: 24px; font-weight: 800; color: #0F172A; }
.tabs-nav { display: flex; gap: 32px; border-bottom: 1px solid #E2E8F0; margin-bottom: 8px; margin-top: 12px; }
.tab-item { padding: 12px 4px; cursor: pointer; color: #64748B; font-weight: 600; border-bottom: 2px solid transparent; transition: all 0.3s; }
.tab-item.active { color: #4F46E5; border-bottom-color: #4F46E5; }
.sub-tabs { display: flex; gap: 24px; margin-bottom: 16px; margin-top: 8px; padding: 0 8px; }
.sub-tab-item { font-size: 13px; color: #94A3B8; cursor: pointer; font-weight: 500; padding: 4px 0; }
.sub-tab-item.active { color: #4F46E5; font-weight: 700; border-bottom: 2px solid #4F46E5; }
.data-grid-header, .grid-row { display: grid; grid-template-columns: 2.5fr 1fr 1.5fr 200px; align-items: center; }
.data-grid-header { padding: 12px 24px; background: #F1F5F9; border-radius: 8px; font-weight: 700; color: #475569; }
.data-grid-body { min-height: 300px; }
.grid-row { padding: 16px 24px; background: white; margin-top: 10px; border-radius: 12px; border: 1px solid #F1F5F9; transition: all 0.2s; }
.grid-row:hover { background: #F8FAFC; border-color: #E2E8F0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
.name { font-weight: 600; color: #1E293B; }
.time { font-size: 13px; color: #64748B; }
.status-tag { padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; display: inline-block; }
.status-tag.success { background: #DCFCE7; color: #166534; }
.status-tag.default { background: #F1F5F9; color: #475569; }
.actions { display: flex; gap: 12px; align-items: center; }
.text-btn { background: none; border: none; cursor: pointer; font-size: 13px; font-weight: 600; color: #64748B; padding: 0; transition: color 0.2s; }
.text-btn:hover { color: #334155; text-decoration: underline; }
.text-btn.primary { color: #4F46E5; }
.text-btn.primary:hover { color: #4338CA; }
.text-btn.danger { color: #EF4444; }
.text-btn.danger:hover { color: #DC2626; }
.empty-state { padding: 40px; text-align: center; color: #94A3B8; font-size: 14px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; padding: 12px 24px; background: white; border-radius: 12px; border: 1px solid #F1F5F9; }
</style>
