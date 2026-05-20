<template>
  <div class="admin-page">
    <div class="page-header"><h1>教学组织架构维护</h1></div>

    <n-tabs v-model:value="activeTab" type="line" animated>
      <!-- ===== 院系 ===== -->
      <n-tab-pane name="depts" tab="院系管理">
        <n-button type="primary" size="small" @click="openDeptModal(null)" style="margin-bottom:12px">新增院系</n-button>
        <n-data-table :columns="deptColumns" :data="deptList" :loading="loading.depts" :bordered="true" size="small" />
      </n-tab-pane>

      <!-- ===== 专业 ===== -->
      <n-tab-pane name="majors" tab="专业管理">
        <div style="display:flex; gap:12px; margin-bottom:12px; align-items:center">
          <n-button type="primary" size="small" @click="openMajorModal(null)">新增专业</n-button>
          <n-select v-model:value="majorDeptFilter" placeholder="按院系筛选" clearable :options="deptOptions" style="max-width:220px" size="small" />
        </div>
        <n-data-table :columns="majorColumns" :data="filteredMajors" :loading="loading.majors" :bordered="true" size="small" />
      </n-tab-pane>

      <!-- ===== 班级 ===== -->
      <n-tab-pane name="classes" tab="班级管理">
        <div style="display:flex; gap:12px; margin-bottom:12px; align-items:center">
          <n-button type="primary" size="small" @click="openClassModal(null)">新增班级</n-button>
          <n-select v-model:value="classDeptFilter" placeholder="按院系" clearable :options="deptOptions" style="max-width:180px" size="small" @update:value="onClassDeptFilter" />
          <n-select v-model:value="classMajorFilter" placeholder="按专业" clearable :options="classMajorOptions" style="max-width:180px" size="small" />
        </div>
        <n-data-table :columns="classColumns" :data="filteredClasses" :loading="loading.classes" :bordered="true" size="small" />
      </n-tab-pane>
    </n-tabs>

    <!-- Modals -->
    <n-modal v-model:show="showDeptModal" preset="card" title="院系信息" style="max-width:460px">
      <n-form :model="deptForm" label-placement="left" label-width="80"><n-form-item label="名称"><n-input v-model:value="deptForm.deptName" /></n-form-item></n-form>
      <template #footer><n-space justify="end"><n-button @click="showDeptModal=false">取消</n-button><n-button type="primary" @click="saveDept" :loading="saving">保存</n-button></n-space></template>
    </n-modal>

    <n-modal v-model:show="showMajorModal" preset="card" title="专业信息" style="max-width:460px">
      <n-form :model="majorForm" label-placement="left" label-width="80">
        <n-form-item label="院系"><n-select v-model:value="majorForm.deptId" :options="deptOptions" /></n-form-item>
        <n-form-item label="名称"><n-input v-model:value="majorForm.majorName" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showMajorModal=false">取消</n-button><n-button type="primary" @click="saveMajor" :loading="saving">保存</n-button></n-space></template>
    </n-modal>

    <n-modal v-model:show="showClassModal" preset="card" title="班级信息" style="max-width:460px">
      <n-form :model="classForm" label-placement="left" label-width="80">
        <n-form-item label="院系"><n-select v-model:value="classForm.deptId" :options="deptOptions" @update:value="onClassFormDept" /></n-form-item>
        <n-form-item label="专业"><n-select v-model:value="classForm.majorId" :options="classFormMajorOpts" /></n-form-item>
        <n-form-item label="名称"><n-input v-model:value="classForm.className" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showClassModal=false">取消</n-button><n-button type="primary" @click="saveClass" :loading="saving">保存</n-button></n-space></template>
    </n-modal>

    <!-- 二次确认删除弹窗 -->
    <n-modal v-model:show="showDeleteModal" preset="card" title="确认删除" style="max-width:420px">
      <p style="margin:12px 0;">确定要删除 <strong>{{ deleteTargetName }}</strong> 吗？此操作不可恢复。</p>
      <template #footer>
        <n-space justify="end"><n-button @click="showDeleteModal=false">取消</n-button><n-button type="error" @click="doDelete" :loading="deleting">确认删除</n-button></n-space>
      </template>
    </n-modal>

    <!-- 查看学生弹窗 -->
    <n-modal v-model:show="showStudentModal" preset="card" title="班级学生列表" style="width: 700px">
      <n-data-table :columns="studentColumns" :data="classStudents" :loading="loadingStudents" :bordered="true" size="small" />
      <template #footer>
        <n-space justify="end"><n-button @click="showStudentModal=false">关闭</n-button></n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h } from 'vue'
import { NButton, NDataTable, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NTabPane, NTabs, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import * as api from '@/services/modules/admin.service'
import type { Department, Major, AdminClass, Student } from '@/services/types/admin.types'

const message = useMessage()
const activeTab = ref('depts')
const saving = ref(false)
const loading = reactive({ depts: false, majors: false, classes: false })
const loadingStudents = ref(false)

// ===== 删除确认 =====
const showDeleteModal = ref(false)
const deleteTargetName = ref('')
let deleteAction: (() => Promise<any>) | null = null
const deleting = ref(false)

// ===== 查看学生 =====
const showStudentModal = ref(false)
const classStudents = ref<Student[]>([])
const studentColumns: DataTableColumns<Student> = [
  { title: '账号', key: 'username', width: 120 },
  { title: '姓名', key: 'realName', width: 100 },
  { title: '注册时间', key: 'createdAt', render(row) { return formatDate(row.createdAt) } }
]

function confirmDelete(name: string, fn: () => Promise<any>) {
  deleteTargetName.value = name
  deleteAction = fn
  showDeleteModal.value = true
}
async function doDelete() {
  if (!deleteAction) return
  deleting.value = true
  try {
    const res = await deleteAction()
    if (res && res.code && res.code !== 200) { message.error(res.message || '删除失败'); return }
    message.success('已删除')
    showDeleteModal.value = false
    await refreshCurrent()
  } catch (e: any) { message.error(e?.response?.data?.message || '删除失败') }
  finally { deleting.value = false }
}
async function refreshCurrent() {
  if (activeTab.value === 'depts') await fetchDepts()
  else if (activeTab.value === 'majors') await fetchMajors()
  else await fetchClasses()
}

// ===== 院系 =====
const deptList = ref<Department[]>([])
const showDeptModal = ref(false); const editingDeptId = ref<string | null>(null)
const deptForm = reactive({ deptName: '' })
const deptOptions = computed(() => deptList.value.map(d => ({ label: d.deptName, value: d.id })))

const deptColumns: DataTableColumns<Department> = [
  { title: '院系名称', key: 'deptName' },
  { title: '创建时间', key: 'createdAt', render(row) { return formatDate(row.createdAt) } },
  {
    title: '操作', key: 'actions', width: 160,
    render(row) {
      return h('div', { style: 'display:flex;gap:8px' }, [
        h(NButton, { size: 'tiny', onClick: () => openDeptModal(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'tiny', type: 'error', onClick: () => confirmDelete(row.deptName, () => api.deleteDept(row.id!)) }, { default: () => '删除' })
      ])
    }
  }
]

function openDeptModal(row: Department | null) {
  editingDeptId.value = row?.id ?? null
  deptForm.deptName = row?.deptName ?? ''
  showDeptModal.value = true
}
async function saveDept() {
  if (!deptForm.deptName.trim()) { message.warning('请输入名称'); return }
  saving.value = true
  try {
    const body = { deptName: deptForm.deptName }
    const res = editingDeptId.value ? await api.updateDept(editingDeptId.value, body) : await api.addDept(body)
    if (res.code !== 200) { message.error(res.message || '失败'); return }
    message.success('成功')
    showDeptModal.value = false
    await fetchDepts()
  } catch (e: any) { message.error(e?.response?.data?.message || '失败') }
  finally { saving.value = false }
}
async function fetchDepts() {
  loading.depts = true
  try {
    const r = await api.getDepts()
    if (r.code === 200) deptList.value = r.data || []
  } catch (e: any) { message.error(e?.response?.data?.message || '获取院系列表失败') }
  finally { loading.depts = false }
}

// ===== 专业 =====
const majorList = ref<Major[]>([]); const majorDeptFilter = ref<string | null>(null)
const showMajorModal = ref(false); const editingMajorId = ref<string | null>(null)
const majorForm = reactive({ deptId: '', majorName: '' })

const majorColumns: DataTableColumns<Major> = [
  { title: '专业名称', key: 'majorName' },
  { title: '所属院系', key: 'deptId', render(row) { return deptList.value.find(d => d.id === row.deptId)?.deptName ?? '-' } },
  { title: '创建时间', key: 'createdAt', render(row) { return formatDate(row.createdAt) } },
  {
    title: '操作', key: 'actions', width: 160,
    render(row) {
      return h('div', { style: 'display:flex;gap:8px' }, [
        h(NButton, { size: 'tiny', onClick: () => openMajorModal(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'tiny', type: 'error', onClick: () => confirmDelete(row.majorName, () => api.deleteMajor(row.id!)) }, { default: () => '删除' })
      ])
    }
  }
]
const filteredMajors = computed(() => majorDeptFilter.value ? majorList.value.filter(m => String(m.deptId) === majorDeptFilter.value) : majorList.value)

function openMajorModal(row: Major | null) {
  editingMajorId.value = row?.id ?? null
  majorForm.deptId = row?.deptId ?? ''
  majorForm.majorName = row?.majorName ?? ''
  showMajorModal.value = true
}
async function saveMajor() {
  if (!majorForm.majorName.trim()) { message.warning('请输入名称'); return }
  if (!majorForm.deptId) { message.warning('请选择院系'); return }
  saving.value = true
  try {
    const body = { deptId: majorForm.deptId, majorName: majorForm.majorName }
    const res = editingMajorId.value ? await api.updateMajor(editingMajorId.value, body) : await api.addMajor(body)
    if (res.code !== 200) { message.error(res.message || '失败'); return }
    message.success('成功')
    showMajorModal.value = false
    await fetchMajors()
  } catch (e: any) { message.error(e?.response?.data?.message || '失败') }
  finally { saving.value = false }
}
async function fetchMajors() {
  loading.majors = true
  try {
    const r = await api.getMajors()
    if (r.code === 200) majorList.value = r.data || []
  } catch (e: any) { message.error(e?.response?.data?.message || '获取专业列表失败') }
  finally { loading.majors = false }
}

// ===== 班级 =====
const classList = ref<AdminClass[]>([]); const classDeptFilter = ref<string | null>(null); const classMajorFilter = ref<string | null>(null)
const showClassModal = ref(false); const editingClassId = ref<string | null>(null)
const classForm = reactive({ deptId: null as string | null, majorId: '', className: '' })

const classColumns: DataTableColumns<AdminClass> = [
  { title: '班级名称', key: 'className' },
  { title: '所属专业', key: 'majorId', render(row) { return majorList.value.find(m => m.id === row.majorId)?.majorName ?? '-' } },
  { title: '所属院系', key: 'deptId', render(row) { const m = majorList.value.find(mm => mm.id === row.majorId); return m ? deptList.value.find(d => d.id === m.deptId)?.deptName ?? '-' : '-' } },
  { title: '创建时间', key: 'createdAt', render(row) { return formatDate(row.createdAt) } },
  {
    title: '操作', key: 'actions', width: 220,
    render(row) {
      return h('div', { style: 'display:flex;gap:8px' }, [
        h(NButton, { size: 'tiny', onClick: () => openClassModal(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'tiny', onClick: () => viewStudents(row) }, { default: () => '查看学生' }),
        h(NButton, { size: 'tiny', type: 'error', onClick: () => confirmDelete(row.className, () => api.deleteClass(row.id!)) }, { default: () => '删除' })
      ])
    }
  }
]

const classMajorOptions = computed(() => {
  let l = majorList.value
  if (classDeptFilter.value) l = l.filter(m => String(m.deptId) === classDeptFilter.value)
  return l.map(m => ({ label: m.majorName, value: m.id }))
})
const classFormMajorOpts = computed(() => {
  if (!classForm.deptId) return majorList.value.map(m => ({ label: m.majorName, value: m.id }))
  return majorList.value.filter(m => String(m.deptId) === classForm.deptId).map(m => ({ label: m.majorName, value: m.id }))
})
const filteredClasses = computed(() => {
  let l = classList.value
  if (classDeptFilter.value) {
    const mids = majorList.value.filter(m => String(m.deptId) === classDeptFilter.value).map(m => m.id)
    l = l.filter(c => mids.includes(c.majorId))
  }
  if (classMajorFilter.value) l = l.filter(c => c.majorId === classMajorFilter.value)
  return l
})

function onClassDeptFilter() { classMajorFilter.value = null }
function onClassFormDept() { classForm.majorId = '' }

function openClassModal(row: AdminClass | null) {
  editingClassId.value = row?.id ?? null
  if (row) {
    const m = majorList.value.find(mm => mm.id === row.majorId)
    classForm.deptId = m?.deptId ?? null
    classForm.majorId = row.majorId
    classForm.className = row.className
  } else {
    classForm.deptId = null
    classForm.majorId = ''
    classForm.className = ''
  }
  showClassModal.value = true
}
async function saveClass() {
  if (!classForm.className.trim()) { message.warning('请输入名称'); return }
  if (!classForm.majorId) { message.warning('请选择专业'); return }
  saving.value = true
  try {
    const body = { deptId: classForm.deptId || '', majorId: classForm.majorId, className: classForm.className }
    const res = editingClassId.value ? await api.updateClass(editingClassId.value, body) : await api.addClass(body)
    if (res.code !== 200) { message.error(res.message || '失败'); return }
    message.success('成功')
    showClassModal.value = false
    await fetchClasses()
  } catch (e: any) { message.error(e?.response?.data?.message || '失败') }
  finally { saving.value = false }
}
async function fetchClasses() {
  loading.classes = true
  try {
    const r = await api.getClasses()
    if (r.code === 200) classList.value = r.data || []
  } catch (e: any) { message.error(e?.response?.data?.message || '获取班级列表失败') }
  finally { loading.classes = false }
}

async function viewStudents(row: AdminClass) {
  loadingStudents.value = true
  try {
    const r = await api.getAdminClassStudents(row.id!)
    if (r.code === 200) {
      classStudents.value = r.data || []
    }
    showStudentModal.value = true
  } catch (e: any) {
    message.error(e?.response?.data?.message || '获取学生列表失败')
  } finally {
    loadingStudents.value = false
  }
}

function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

onMounted(async () => { await fetchDepts(); await fetchMajors(); await fetchClasses() })
</script>

<style scoped>
.admin-page { padding: 24px; background: #fff; border-radius: 8px; min-height: 100%; }
.page-header h1 { font-size: 20px; font-weight: 800; color: #0F172A; margin: 0 0 16px; }
</style>