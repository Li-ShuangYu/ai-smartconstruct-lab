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
          <span>模板描述</span>
          <span>AI 状态</span>
          <span>创建时间</span>
          <span class="action-header">操作</span>
        </div>
        <div class="data-grid-body">
          <div v-for="tpl in templates" :key="tpl.id" class="grid-row">
            <span class="name">{{ tpl.templateName }}</span>
            <span class="desc">{{ tpl.templateDesc || '-' }}</span>
            <span>
              <n-tag :type="aiStatusTag(tpl.aiStatus)" size="small">
                <template v-if="tpl.aiStatus === 1"><n-spin :size="14" /> </template>
                {{ aiStatusLabel(tpl.aiStatus) }}
              </n-tag>
            </span>
            <span class="time">{{ formatTime(tpl.createdAt) }}</span>
            <div class="actions">
              <button class="text-btn" @click="openEditTemplate(tpl)">修改</button>
              <button v-if="tpl.aiStatus === 2" class="text-btn primary" @click="openPublish(tpl)">发布任务</button>
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
        <div class="data-grid-header task-header">
          <span>编号</span>
          <span>实训名称</span>
          <span>模版名称</span>
          <span>下发范围</span>
          <span>下发目标</span>
          <span>实训类型</span>
          <span>实训时间</span>
          <span>实训状态</span>
          <span class="action-header">操作</span>
        </div>
        <div class="data-grid-body">
          <div v-for="(item, index) in tasks" :key="item.id" class="grid-row task-row">
            <span>{{ index + 1 }}</span>
            <span class="name">{{ item.taskName || '未命名实训' }}</span>
            <span>{{ item.templateName || '-' }}</span>
            <span>{{ item.dispatchScope === 1 ? '班级' : item.dispatchScope === 2 ? '课程' : '-' }}</span>
            <span>{{ item.dispatchTargetName || '-' }}</span>
            <span>{{ item.isInClass === 1 ? '课堂实训' : '课后实训' }}</span>
            <span class="time">{{ formatTimeRange(item.startTime, item.endTime) }}</span>
            <span><span class="status-tag" :class="taskStatusClass(item.status)">{{ taskStatusLabel(item.status) }}</span></span>
            <div class="actions">
              <template v-if="item.status === 0">
                <button class="text-btn" @click="openTaskDispatch(item)">重新下发实训</button>
              </template>
              <button class="text-btn" @click="openEditTask(item)">编辑</button>
              <template v-if="item.status === 0">
                <button class="text-btn primary" @click="handleStartTask(item)">开始实训</button>
              </template>
              <button class="text-btn danger" @click="handleDeleteTask(item)">删除</button>
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

    <!-- 发布任务弹窗 -->
    <n-modal v-model:show="showPublish" preset="card" title="下发实训任务" style="width: 520px" :mask-closable="false">
      <n-form ref="publishFormRef" :model="publishForm" :rules="publishRules" label-placement="left" label-width="100">
        <n-form-item label="当前模板">
          <n-input :value="publishForm.templateId ? editingTemplateName : ''" disabled style="color:#64748B" />
        </n-form-item>
        <n-form-item label="任务名称" path="taskName">
          <n-input v-model:value="publishForm.taskName" placeholder="请输入实训任务名称" />
        </n-form-item>
        <n-form-item label="下发范围">
          <n-radio-group v-model:value="publishForm.dispatchScope">
            <n-radio :value="1">按班级下发</n-radio>
            <n-radio :value="2">按课程下发</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item :label="publishForm.dispatchScope === 1 ? '目标班级' : '目标课程'" path="dispatchTargetId">
          <n-select v-model:value="publishForm.dispatchTargetId" :options="targetOptions" :placeholder="publishForm.dispatchScope === 1 ? '请选择下发班级' : '请选择下发课程'" />
        </n-form-item>
        <n-form-item label="课堂实训">
          <n-switch v-model:value="publishForm.isInClass" :checked-value="1" :unchecked-value="0" />
        </n-form-item>
        <n-form-item label="开始时间" path="startTime">
          <n-date-picker v-model:value="publishForm.startTime" type="datetime" placeholder="选择开始时间" style="width:100%" />
        </n-form-item>
        <n-form-item label="结束时间" path="endTime">
          <n-date-picker v-model:value="publishForm.endTime" type="datetime" placeholder="选择结束时间" style="width:100%" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div class="modal-footer"><n-button @click="showPublish = false">取消</n-button>
        <n-button type="primary" :loading="publishing" @click="confirmPublish">确认下发</n-button></div>
      </template>
    </n-modal>

    <!-- 修改模板弹窗 -->
    <n-modal v-model:show="showEditTemplate" preset="card" title="修改模板" style="width: 480px" :mask-closable="false">
      <n-form ref="editFormRef" :model="editForm" :rules="editRules" label-placement="left" label-width="100">
        <n-form-item label="模板名称" path="templateName">
          <n-input v-model:value="editForm.templateName" placeholder="请输入模板名称" />
        </n-form-item>
        <n-form-item label="模板描述">
          <n-input v-model:value="editForm.templateDesc" type="textarea" placeholder="请输入模板描述（可选）" :rows="3" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div class="modal-footer">
          <n-button @click="showEditTemplate = false">取消</n-button>
          <n-button type="primary" :loading="editingTemplate" @click="confirmEditTemplate">保存</n-button>
        </div>
      </template>
    </n-modal>

    <!-- 编辑任务弹窗 -->
    <n-modal v-model:show="showEditTask" preset="card" title="编辑任务" style="width: 480px" :mask-closable="false">
      <n-form ref="editTaskFormRef" :model="editTaskForm" :rules="editTaskRules" label-placement="left" label-width="100">
        <n-form-item label="实训名称" path="taskName">
          <n-input v-model:value="editTaskForm.taskName" placeholder="请输入实训名称" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div class="modal-footer">
          <n-button @click="showEditTask = false">取消</n-button>
          <n-button type="primary" :loading="editingTask" @click="confirmEditTask">保存</n-button>
        </div>
      </template>
    </n-modal>

    <!-- 任务下发弹窗 -->
    <n-modal v-model:show="showTaskDispatch" preset="card" title="下发实训任务" style="width: 520px" :mask-closable="false">
      <n-form ref="dispatchFormRef" :model="dispatchForm" :rules="dispatchRules" label-placement="left" label-width="100">
        <n-form-item label="任务名称">
          <n-input :value="dispatchForm.taskName" disabled />
        </n-form-item>
        <n-form-item label="下发方式" path="dispatchType">
          <n-radio-group v-model:value="dispatchForm.dispatchType">
            <n-radio value="class">按班级下发</n-radio>
            <n-radio value="course">按课程下发</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item :label="dispatchForm.dispatchType === 'class' ? '选择班级' : '选择课程'" path="targetId">
          <n-select 
            v-model:value="dispatchForm.targetId" 
            :options="dispatchTargetOptions" 
            :placeholder="dispatchForm.dispatchType === 'class' ? '请选择要下发的班级' : '请选择要下发的课程'"
            multiple
            :max-tag-count="3"
          />
        </n-form-item>
        <n-form-item label="已选目标">
          <div class="selected-targets">
            <n-tag v-for="item in selectedTargetNames" :key="item" closable @close="removeTarget(item)" type="info" style="margin-right: 8px; margin-bottom: 4px;">
              {{ item }}
            </n-tag>
            <span v-if="selectedTargetNames.length === 0" style="color: #94A3B8; font-size: 13px;">暂未选择</span>
          </div>
        </n-form-item>
        <n-form-item label="是否课堂实训">
          <n-switch v-model:value="dispatchForm.isClassTraining" />
        </n-form-item>
        <n-form-item label="起止时间">
          <n-date-picker 
            v-model:value="dispatchForm.timeRange" 
            type="daterange"
            show-time
            :placeholder="['开始时间', '结束时间'] as unknown as string"
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <div class="modal-footer">
          <n-button @click="showTaskDispatch = false">取消</n-button>
          <n-button type="primary" :loading="dispatching" @click="confirmDispatch">确认下发</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { NButton, NPagination, NSpin, NTag, NModal, NForm, NFormItem, NInput, NSelect, NRadioGroup, NRadio, NSwitch, NDatePicker, useMessage } from 'naive-ui'
import type { FormRules, FormInst } from 'naive-ui'
import { getTemplates, deleteTemplate, updateTemplate } from '@/services/modules/teacher-template.service'
import { getTrainingTasks, createTrainingTask, startTrainingTask, reDispatchTrainingTask, updateTrainingTask, deleteTrainingTask } from '@/services/modules/teacher-dashboard.service'
import { getClasses } from '@/services/modules/admin.service'
import { getTeacherCourses } from '@/services/modules/teacher.service'
import type { TrainingTemplate } from '@/services/types/template.types'
import type { TrainingTaskItem } from '@/services/types/dashboard.types'
import type { AdminClass, Course } from '@/services/types/admin.types'

interface PublishFormData {
  taskName: string
  dispatchTargetId: string | null
  templateId: string
  dispatchScope: number
  isInClass: number
  startTime: number | null
  endTime: number | null
}

const message = useMessage()
const loading = ref(false)
const mainTab = ref('template')
const pageLabel = computed(() => mainTab.value === 'template' ? '实训模板仓库' : '实训任务管理')

const templates = ref<TrainingTemplate[]>([])
const tplPage = ref(1)
const tplPageSize = ref(5)
const tplTotal = ref(0)
let pollTimer: ReturnType<typeof setInterval> | undefined

function aiStatusLabel(s: number) { if (s===0) return '草稿'; if (s===1) return 'AI处理中'; if (s===2) return '已就绪'; return '异常' }
function aiStatusTag(s: number): 'default'|'warning'|'success'|'error' { if (s===0) return 'default'; if (s===1) return 'warning'; if (s===2) return 'success'; return 'error' }

const tasks = ref<TrainingTaskItem[]>([])
const taskTab = ref(0)
const taskPage = ref(1)
const taskPageSize = ref(5)
const taskTotal = ref(0)
const taskTabs = [{ label:'全部实训',key:0 },{ label:'未开始',key:1 },{ label:'进行中',key:2 },{ label:'已结束',key:3 }]

function taskStatusLabel(s: number) { if (s===0) return '未开始'; if (s===1) return '进行中'; return '已结束' }
function taskStatusClass(s: number) { return s===1?'success':'default' }
function formatTime(t?: string) { if (!t) return '-'; return t.slice(0,16).replace('T',' ') }
function formatTimeRange(start?: string, end?: string) { 
  if (!start) return '-'; 
  if (!end) return formatTime(start); 
  return `${formatTime(start)} ~ ${formatTime(end)}` 
}

async function loadTemplates() { loading.value=true; try { const r = await getTemplates(tplPage.value,tplPageSize.value); if(r.code===200){templates.value=r.data.records;tplTotal.value=r.data.total} } finally {loading.value=false} }
async function handleDeleteTemplate(tpl: TrainingTemplate) { if(!tpl.id)return; try{await deleteTemplate(String(tpl.id));message.success('模板已删除');loadTemplates()}catch{message.error('删除失败')} }
function showError(tpl: TrainingTemplate) { message.error(tpl.errorReason||'AI处理异常') }

async function loadTasks() { loading.value=true; try{ let s:number|undefined; if(taskTab.value===1)s=0;else if(taskTab.value===2)s=1;else if(taskTab.value===3)s=2; const r = await getTrainingTasks(taskPage.value,taskPageSize.value,s); if(r.code===200){tasks.value=r.data.records;taskTotal.value=r.data.total} } finally{loading.value=false} }
function switchMainTab(t:string) { mainTab.value=t; if(t==='template'){loadTemplates()}else{loadTasks()} }
function switchTaskTab(k:number) { taskTab.value=k;taskPage.value=1;loadTasks() }
function handleEndTask(_:TrainingTaskItem) { message.info('结束实训功能待实现') }

// 修改模板相关
const showEditTemplate = ref(false)
const editingTemplate = ref(false)
const editFormRef = ref<FormInst | null>(null)
const editForm = ref({
  id: '',
  templateName: '',
  templateDesc: ''
})
const editRules: FormRules = {
  templateName: [{ required: true, message: '请输入模板名称' }]
}

function openEditTemplate(tpl: TrainingTemplate) {
  editForm.value = {
    id: String(tpl.id || ''),
    templateName: tpl.templateName || '',
    templateDesc: tpl.templateDesc || ''
  }
  showEditTemplate.value = true
}

async function confirmEditTemplate() {
  try {
    await editFormRef.value?.validate()
  } catch {
    return
  }
  editingTemplate.value = true
  try {
    const res = await updateTemplate(editForm.value.id, {
      templateName: editForm.value.templateName,
      templateDescription: editForm.value.templateDesc
    })
    if (res.code !== 200) { message.error(res.message || '保存失败'); return }
    message.success('模板已保存')
    showEditTemplate.value = false
    loadTemplates()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '保存失败')
  } finally {
    editingTemplate.value = false
  }
}

const showPublish = ref(false)
const publishing = ref(false)
const editingTemplateName = ref('')
const publishFormRef = ref<FormInst|null>(null)
const publishForm = ref<PublishFormData>({ taskName:'', dispatchTargetId:null, templateId:'', dispatchScope:1, isInClass:0, startTime:null, endTime:null })
const publishRules: FormRules = { taskName:[{required:true,message:'请输入任务名称'}], dispatchTargetId:[{required:true,message:'请选择目标'}], startTime:[{required:true,message:'请选择开始时间'}], endTime:[{required:true,message:'请选择结束时间'}] }
const targetOptions = ref<{label:string,value:string}[]>([])

async function openPublish(tpl: TrainingTemplate) {
  editingTemplateName.value = tpl.templateName || ''
  publishForm.value = {
    taskName: tpl.templateName || '',
    dispatchTargetId: null,
    templateId: String(tpl.id || ''),
    dispatchScope: 1,
    isInClass: 0,
    startTime: null,
    endTime: null
  }
  try { const r = await getClasses(); if(r.code===200) targetOptions.value = r.data.map((c:AdminClass)=>({label:c.className,value:String(c.id||'')})) } catch{/* */ }
  showPublish.value = true
}

watch(() => publishForm.value.dispatchScope, async (scope) => {
  publishForm.value.dispatchTargetId = null
  if (scope === 1) {
    try { const r = await getClasses(); if(r.code===200) targetOptions.value = r.data.map((c:AdminClass)=>({label:c.className,value:String(c.id||'')})) } catch{/* */}
  } else {
    try { const r = await getTeacherCourses(1, 100); if(r.code===200) targetOptions.value = r.data.records.map((c:Course)=>({label:c.courseName,value:String(c.id!)})) } catch{/* */}
  }
})

async function confirmPublish() {
  try { await publishFormRef.value?.validate() } catch { return }
  if (!publishForm.value.startTime || !publishForm.value.endTime) { message.error('请设置起止时间'); return }
  if (publishForm.value.endTime <= publishForm.value.startTime) { message.error('结束时间必须晚于开始时间'); return }
  publishing.value = true
  try {
    const start = new Date(publishForm.value.startTime).toISOString()
    const end = new Date(publishForm.value.endTime).toISOString()
    const r = await createTrainingTask({
      templateId: publishForm.value.templateId,
      taskName: publishForm.value.taskName,
      dispatchScope: publishForm.value.dispatchScope,
      dispatchTargetId: publishForm.value.dispatchTargetId!,
      isInClass: publishForm.value.isInClass,
      startTime: start,
      endTime: end
    })
    if (r.code === 200) { message.success(`任务已下发，共 ${r.data.studentCount} 名学生`); showPublish.value = false; switchMainTab('task') }
    else message.error(r.message||'下发失败')
  } catch (e: any) { message.error(e?.response?.data?.message || '操作失败') } finally { publishing.value = false }
}

const showTaskDispatch = ref(false)
const dispatching = ref(false)
const dispatchFormRef = ref<FormInst|null>(null)
const dispatchForm = ref({
  taskId: 0,
  taskName: '',
  dispatchType: 'class',
  targetId: [] as string[],
  isClassTraining: false,
  timeRange: null as [number, number] | null
})
const dispatchRules: FormRules = {
  dispatchType: [{ required: true, message: '请选择下发方式' }],
  targetId: [{ required: true, message: '请选择下发目标', type: 'array', min: 1 }]
}
const dispatchTargetOptions = ref<{label:string,value:string}[]>([])
const classList = ref<{id:string,name:string}[]>([])
const courseList = ref<{id:string,name:string}[]>([])

const selectedTargetNames = computed(() => {
  const selectedIds = dispatchForm.value.targetId
  const options = dispatchTargetOptions.value
  return selectedIds.map(id => {
    const opt = options.find(o => o.value === id)
    return opt ? opt.label : id
  })
})

async function loadClassOptions() {
  try {
    const r = await getClasses()
    if (r.code === 200) {
      classList.value = r.data.map((c: AdminClass) => ({ id: String(c.id || ''), name: c.className }))
      dispatchTargetOptions.value = classList.value.map(c => ({ label: c.name, value: c.id }))
    }
  } catch { /* */ }
}

async function loadCourseOptions() {
  try {
    const r = await getTeacherCourses(1, 100)
    if (r.code === 200) {
      courseList.value = r.data.records.map((c: Course) => ({ id: String(c.id!), name: c.courseName }))
      dispatchTargetOptions.value = courseList.value.map(c => ({ label: c.name, value: c.id }))
    }
  } catch { /* */ }
}

async function openTaskDispatch(task: TrainingTaskItem) {
  dispatchForm.value = {
    taskId: task.id!,
    taskName: task.taskName || '未命名实训',
    dispatchType: 'class',
    targetId: [],
    isClassTraining: false,
    timeRange: null
  }
  await loadClassOptions()
  showTaskDispatch.value = true
}

watch(() => dispatchForm.value.dispatchType, async (type) => {
  dispatchForm.value.targetId = []
  if (type === 'class') {
    await loadClassOptions()
  } else {
    await loadCourseOptions()
  }
})

function removeTarget(name: string) {
  const option = dispatchTargetOptions.value.find(o => o.label === name)
  if (option) {
    dispatchForm.value.targetId = dispatchForm.value.targetId.filter(id => id !== option.value)
  }
}

async function confirmDispatch() {
  try { await dispatchFormRef.value?.validate() } catch { return }
  dispatching.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    const targetNames = selectedTargetNames.value.join('、')
    message.success(`实训任务已成功下发至：${targetNames}`)
    showTaskDispatch.value = false
  } catch { message.error('下发失败') } finally { dispatching.value = false }
}

// 编辑任务相关
const showEditTask = ref(false)
const editingTask = ref(false)
const editTaskFormRef = ref<FormInst|null>(null)
interface EditTaskForm {
  id: number
  taskName: string
}
const editTaskForm = ref<EditTaskForm>({ id: 0, taskName: '' })
const editTaskRules: FormRules = {
  taskName: [{ required: true, message: '请输入实训名称' }]
}

function openEditTask(task: TrainingTaskItem) {
  editTaskForm.value = {
    id: task.id,
    taskName: task.taskName || ''
  }
  showEditTask.value = true
}

async function confirmEditTask() {
  try { await editTaskFormRef.value?.validate() } catch { return }
  editingTask.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 800))
    const idx = tasks.value.findIndex(t => t.id === editTaskForm.value.id)
    if (idx !== -1 && tasks.value[idx]) {
      tasks.value[idx].taskName = editTaskForm.value.taskName
    }
    message.success('任务已保存')
    showEditTask.value = false
  } catch { message.error('保存失败') } finally { editingTask.value = false }
}

// 删除任务
function handleDeleteTask(task: TrainingTaskItem) {
  try {
    const idx = tasks.value.findIndex(t => t.id === task.id)
    if (idx !== -1) {
      tasks.value.splice(idx, 1)
      taskTotal.value--
    }
    message.success('任务已删除')
  } catch {
    message.error('删除失败')
  }
}

async function handleStartTask(task: TrainingTaskItem) {
  try {
    const res = await startTrainingTask(String(task.id))
    if (res.code !== 200) { message.error(res.message || '操作失败'); return }
    message.success('实训已启动')
    loadTasks()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '操作失败')
  }
}

onMounted(()=>{loadTemplates()})
onUnmounted(()=>{})
</script>

<style scoped>
.manage-header { display:flex; justify-content:space-between; align-items:center; }
.header-actions { display:flex; align-items:center; }
.panel-main-title { font-size:24px; font-weight:800; color:#0F172A; }
.tabs-nav { display:flex; gap:32px; border-bottom:1px solid #E2E8F0; margin-bottom:8px; margin-top:12px; }
.tab-item { padding:12px 4px; cursor:pointer; color:#64748B; font-weight:600; border-bottom:2px solid transparent; transition:all .3s; }
.tab-item.active { color:#4F46E5; border-bottom-color:#4F46E5; }
.sub-tabs { display:flex; gap:24px; margin-bottom:16px; margin-top:8px; padding:0 8px; }
.sub-tab-item { font-size:13px; color:#94A3B8; cursor:pointer; font-weight:500; padding:4px 0; }
.sub-tab-item.active { color:#4F46E5; font-weight:700; border-bottom:2px solid #4F46E5; }
.data-grid-header,.grid-row { display:grid; grid-template-columns:1.5fr 2fr 1fr 1.5fr 220px; align-items:center; }
.data-grid-header { padding:12px 24px; background:#F1F5F9; border-radius:8px; font-weight:700; color:#475569; }
.data-grid-body { min-height:300px; }
.grid-row { padding:16px 24px; background:white; margin-top:10px; border-radius:12px; border:1px solid #F1F5F9; transition:all .2s; }
.grid-row:hover { background:#F8FAFC; border-color:#E2E8F0; box-shadow:0 4px 6px -1px rgba(0,0,0,.05); }
.name { font-weight:600; color:#1E293B; }
.desc { font-size:13px; color:#64748B; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.time { font-size:13px; color:#64748B; }
.status-tag { padding:4px 10px; border-radius:6px; font-size:12px; font-weight:600; display:inline-block; }
.status-tag.success { background:#DCFCE7; color:#166534; }
.status-tag.default { background:#F1F5F9; color:#475569; }
.actions { display:flex; gap:12px; align-items:center; }

/* 任务列表样式 */
.task-header { grid-template-columns:60px 1.5fr 1.2fr 80px 1.5fr 100px 1.5fr 80px 280px; }
.task-row { grid-template-columns:60px 1.5fr 1.2fr 80px 1.5fr 100px 1.5fr 80px 280px; }
.text-btn { background:none; border:none; cursor:pointer; font-size:13px; font-weight:600; color:#64748B; padding:0; transition:color .2s; }
.text-btn:hover { color:#334155; text-decoration:underline; }
.text-btn.primary { color:#4F46E5; }
.text-btn.primary:hover { color:#4338CA; }
.text-btn.danger { color:#EF4444; }
.text-btn.danger:hover { color:#DC2626; }
.empty-state { padding:40px; text-align:center; color:#94A3B8; font-size:14px; }
.pagination-wrapper { display:flex; justify-content:flex-end; margin-top:20px; padding:12px 24px; background:white; border-radius:12px; border:1px solid #F1F5F9; }
.modal-footer { display:flex; justify-content:flex-end; gap:12px; }
.selected-targets { min-height: 32px; display: flex; flex-wrap: wrap; align-items: center; }
</style>
