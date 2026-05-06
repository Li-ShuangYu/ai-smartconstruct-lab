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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h } from 'vue'
import { NButton, NDataTable, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NTabPane, NTabs, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import * as api from '@/services/modules/admin.service'
import type { Department, Major, AdminClass } from '@/services/types/admin.types'

const message = useMessage()
const activeTab = ref('depts')
const saving = ref(false)
const loading = reactive({ depts: false, majors: false, classes: false })

// ===== 院系 =====
const deptList = ref<Department[]>([])
const showDeptModal = ref(false); const editingDeptId = ref<number | null>(null)
const deptForm = reactive<Department>({ id: 0, deptName: '' })
const deptOptions = computed(() => deptList.value.map(d => ({ label: d.deptName, value: d.id })))

const deptColumns: DataTableColumns<Department> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '院系名称', key: 'deptName' },
  { title: '操作', key: 'actions', width: 160, render(row) { return h('div', { style: 'display:flex;gap:8px' }, [h(NButton, { size:'tiny', onClick:()=>openDeptModal(row) }, { default:()=>'编辑' }), h(NButton, { size:'tiny', type:'error', onClick:()=>handleDeleteDept(row) }, { default:()=>'删除' })]) } }
]

function openDeptModal(row: Department | null) {
  editingDeptId.value = row?.id ?? null; deptForm.deptName = row?.deptName ?? ''; showDeptModal.value = true
}
async function saveDept() {
  if(!deptForm.deptName.trim()){message.warning('请输入名称');return}
  saving.value=true; try{ editingDeptId.value?await api.updateDept(editingDeptId.value,{...deptForm}):await api.addDept({...deptForm}); message.success('成功'); showDeptModal.value=false; await fetchDepts() }catch{message.error('失败')} finally{saving.value=false}
}
async function handleDeleteDept(row: Department) { try{await api.deleteDept(row.id);message.success('已删除');await fetchDepts()}catch{message.error('失败')} }
async function fetchDepts() { loading.depts=true; try{const r=await api.getDepts();if(r.code===200)deptList.value=r.data||[]}catch{}finally{loading.depts=false} }

// ===== 专业 =====
const majorList = ref<Major[]>([]); const majorDeptFilter = ref<number | null>(null)
const showMajorModal = ref(false); const editingMajorId = ref<number | null>(null)
const majorForm = reactive<Major>({ id:0, deptId:0, majorName:'' })

const majorColumns: DataTableColumns<Major> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '专业名称', key: 'majorName' },
  { title: '所属院系', key: 'deptId', render(row){ return deptList.value.find(d=>d.id===row.deptId)?.deptName??'-' } },
  { title: '操作', key: 'actions', width: 160, render(row){ return h('div',{style:'display:flex;gap:8px'},[h(NButton,{size:'tiny',onClick:()=>openMajorModal(row)},{default:()=>'编辑'}),h(NButton,{size:'tiny',type:'error',onClick:()=>handleDeleteMajor(row)},{default:()=>'删除'})]) } }
]
const filteredMajors = computed(()=> majorDeptFilter.value ? majorList.value.filter(m=>m.deptId===majorDeptFilter.value) : majorList.value)

function openMajorModal(row: Major|null) { editingMajorId.value=row?.id??null; majorForm.deptId=row?.deptId??(deptList.value[0]?.id??0); majorForm.majorName=row?.majorName??''; showMajorModal.value=true }
async function saveMajor() { if(!majorForm.majorName.trim()){message.warning('请输入名称');return}; saving.value=true; try{ editingMajorId.value?await api.updateMajor(editingMajorId.value,{...majorForm}):await api.addMajor({...majorForm}); message.success('成功'); showMajorModal.value=false; await fetchMajors() }catch{message.error('失败')} finally{saving.value=false} }
async function handleDeleteMajor(row: Major) { try{await api.deleteMajor(row.id);message.success('已删除');await fetchMajors()}catch{message.error('失败')} }
async function fetchMajors() { loading.majors=true; try{const r=await api.getMajors();if(r.code===200)majorList.value=r.data||[]}catch{}finally{loading.majors=false} }

// ===== 班级 =====
const classList = ref<AdminClass[]>([]); const classDeptFilter = ref<number|null>(null); const classMajorFilter = ref<number|null>(null)
const showClassModal = ref(false); const editingClassId = ref<number|null>(null)
const classForm = reactive<AdminClass & { deptId: number|null }>({ id:0, majorId:0, className:'', deptId:null })

const classColumns: DataTableColumns<AdminClass> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '班级名称', key: 'className' },
  { title: '所属专业', key: 'majorId', render(row){ return majorList.value.find(m=>m.id===row.majorId)?.majorName??'-' } },
  { title: '操作', key: 'actions', width: 160, render(row){ return h('div',{style:'display:flex;gap:8px'},[h(NButton,{size:'tiny',onClick:()=>openClassModal(row)},{default:()=>'编辑'}),h(NButton,{size:'tiny',type:'error',onClick:()=>handleDeleteClass(row)},{default:()=>'删除'})]) } }
]

const classMajorOptions = computed(()=> { let l=majorList.value; if(classDeptFilter.value) l=l.filter(m=>m.deptId===classDeptFilter.value); return l.map(m=>({label:m.majorName,value:m.id})) })
const classFormMajorOpts = computed(()=> { if(!classForm.deptId) return majorList.value.map(m=>({label:m.majorName,value:m.id})); return majorList.value.filter(m=>m.deptId===classForm.deptId).map(m=>({label:m.majorName,value:m.id})) })
const filteredClasses = computed(()=> { let l=classList.value; if(classDeptFilter.value){ const mids=majorList.value.filter(m=>m.deptId===classDeptFilter.value).map(m=>m.id); l=l.filter(c=>mids.includes(c.majorId)) } if(classMajorFilter.value) l=l.filter(c=>c.majorId===classMajorFilter.value); return l })

function onClassDeptFilter(){ classMajorFilter.value=null }
function onClassFormDept(){ classForm.majorId=0 }

function openClassModal(row: AdminClass|null) {
  editingClassId.value=row?.id??null
  if(row){ const major=majorList.value.find(m=>m.id===row.majorId); classForm.deptId=major?.deptId??null; classForm.majorId=row.majorId; classForm.className=row.className }
  else { classForm.deptId=null; classForm.majorId=0; classForm.className='' }
  showClassModal.value=true
}
async function saveClass() { if(!classForm.className.trim()){message.warning('请输入名称');return}; if(!classForm.majorId){message.warning('请选择专业');return}; saving.value=true; try{ const p:AdminClass={majorId:classForm.majorId,className:classForm.className}; editingClassId.value?await api.updateClass(editingClassId.value,p):await api.addClass(p); message.success('成功'); showClassModal.value=false; await fetchClasses() }catch{message.error('失败')} finally{saving.value=false} }
async function handleDeleteClass(row: AdminClass) { try{await api.deleteClass(row.id);message.success('已删除');await fetchClasses()}catch{message.error('失败')} }
async function fetchClasses() { loading.classes=true; try{const r=await api.getClasses();if(r.code===200)classList.value=r.data||[]}catch{}finally{loading.classes=false} }

onMounted(async ()=>{ await fetchDepts(); await fetchMajors(); await fetchClasses() })
</script>

<style scoped>
.admin-page { padding: 24px; background: #fff; border-radius: 8px; min-height: 100%; }
.page-header h1 { font-size: 20px; font-weight: 800; color: #0F172A; margin: 0 0 16px; }
</style>
