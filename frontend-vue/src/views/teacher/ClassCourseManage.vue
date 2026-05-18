<template>
  <!-- <div class="page-container"> -->
    <div>
    <div class="page-header">
      <h1 class="page-title">教务组织管理</h1>
      <n-button v-if="activeTab === 'course'" type="primary" @click="handleCreate">新增课程</n-button>
    </div>

    <div class="tabs-nav">
      <div class="tab-item" :class="{ active: activeTab === 'class' }" @click="switchTab('class')">行政班级管理</div>
      <div class="tab-item" :class="{ active: activeTab === 'course' }" @click="switchTab('course')">课程管理</div>
    </div>

    <n-spin :show="loading">
      <!-- 班级列表 -->
      <main v-if="activeTab === 'class'">
        <div class="data-grid-header">
          <span>班级名称</span><span>学生人数</span><span class="action-header">操作</span>
        </div>
        <div v-for="cls in classes" :key="cls.id" class="grid-row">
          <span class="name">{{ cls.className }}</span>
          <span class="time">-</span>
          <div class="actions">
            <button class="text-btn primary" @click="openClassStudents(cls)">查看学生</button>
          </div>
        </div>
        <div v-if="!loading && classes.length === 0" class="empty-state">暂无班级数据</div>
      </main>

      <!-- 课程列表 -->
      <main v-if="activeTab === 'course'">
        <div class="data-grid-header">
          <span>课程名称</span><span>状态</span><span class="action-header">操作</span>
        </div>
        <div v-for="crs in courses" :key="crs.id" class="grid-row">
          <span class="name">{{ crs.courseName }}</span>
          <span><n-tag :type="crs.status === 1 ? 'success' : 'default'" size="small">{{ crs.status === 1 ? '已发布' : '草稿' }}</n-tag></span>
          <div class="actions">
            <button class="text-btn primary" @click="openCourseStudents(crs)">查看学生</button>
            <button class="text-btn" @click="openEdit(crs)">编辑</button>
            <button class="text-btn" :class="crs.status === 1 ? 'warning' : ''" @click="handleToggleStatus(crs)">{{ crs.status === 1 ? '下线' : '发布' }}</button>
            <button class="text-btn danger" @click="handleDeleteCourse(crs)">删除</button>
          </div>
        </div>
        <div v-if="!loading && courses.length === 0" class="empty-state">暂无课程数据</div>
        <div class="pagination-wrapper" v-if="courseTotal > 0">
          <n-pagination v-model:page="coursePage" :page-size="coursePageSize" :item-count="courseTotal" show-size-picker
            @update:page="loadCourses" @update:page-size="(s:number)=>{coursePageSize=s;coursePage=1;loadCourses()}" />
        </div>
      </main>
    </n-spin>

    <!-- 学生列表 Drawer -->
    <n-modal v-model:show="drawerShow" preset="card" :title="drawerTitle" style="width: 600px" :mask-closable="false">
      <n-input v-model:value="studentKeyword" placeholder="搜索姓名或学号..." clearable style="margin-bottom: 12px" @update:value="onStudentSearch" />
      <n-data-table :columns="studentCols" :data="drawerStudents" :loading="drawerLoading" :row-key="(r:any)=>r.userId" size="small" :bordered="false" />
      <template #footer><div class="modal-footer"><n-button @click="drawerShow=false">关闭</n-button></div></template>
    </n-modal>

    <!-- 新建/编辑课程 Modal -->
    <n-modal v-model:show="showModal" preset="card" :title="editingId ? '编辑课程' : '新增课程'" style="width: 560px" :mask-closable="false">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="课程名称" path="courseName"><n-input v-model:value="form.courseName" placeholder="请输入课程名称" /></n-form-item>
        <n-form-item label="课程简介"><n-input v-model:value="form.description" type="textarea" placeholder="请输入课程简介" :autosize="{minRows:3,maxRows:5}" /></n-form-item>
        <n-form-item label="需要选课码"><n-switch v-model:value="form.needEnrollCode" :checked-value="1" :unchecked-value="0" /></n-form-item>
        <n-form-item v-if="form.needEnrollCode === 1" label="选课授权码"><n-input v-model:value="form.enrollCode" placeholder="请输入选课授权码" /></n-form-item>
      </n-form>
      <template #footer><div class="modal-footer"><n-button @click="showModal=false">取消</n-button><n-button type="primary" :loading="submitting" @click="handleSubmit">保存</n-button></div></template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { NButton, NPagination, NSpin, NTag, NModal, NForm, NFormItem, NInput, NSwitch, NDataTable, useMessage } from 'naive-ui'
import type { FormRules, FormInst, DataTableColumn } from 'naive-ui'
import type { Course, AdminClass } from '@/services/types/admin.types'
import { getTeacherCourses, createCourse, updateCourse, deleteCourse, toggleCourseStatus } from '@/services/modules/teacher.service'
import { getClassStudents, getCourseStudents } from '@/services/modules/teacher-dashboard.service'
import { getClasses } from '@/services/modules/admin.service'

const message = useMessage()
const activeTab = ref('class')
const loading = ref(false); const submitting = ref(false)

// 班级
const classes = ref<AdminClass[]>([])

// 课程
const courses = ref<Course[]>([])
const coursePage = ref(1); const coursePageSize = ref(10); const courseTotal = ref(0)
const showModal = ref(false); const editingId = ref<number|null>(null); const formRef = ref<FormInst|null>(null)
const form = reactive<Course>({ courseName:'', description:'', status:0, needEnrollCode:0, enrollCode:'' })
const rules: FormRules = { courseName: [{required:true,message:'请输入课程名称'}] }

// 学生 Drawer
const drawerShow = ref(false); const drawerLoading = ref(false)
const drawerTitle = ref(''); const drawerStudents = ref<any[]>([]); const studentKeyword = ref('')
let drawerTargetId = 0; let drawerType = ref<'class'|'course'>('class')
let searchTimer: any

const studentCols: DataTableColumn[] = [
  { title:'姓名', key:'realName' }, { title:'学号', key:'studentNo' }, { title:'用户名', key:'username' }
]

function switchTab(t: string) { activeTab.value = t; if (t==='class') loadClasses(); else loadCourses() }

async function loadClasses() {
  loading.value = true
  try { const r = await getClasses(); if (r.code===200) classes.value = r.data } finally { loading.value = false }
}

async function loadCourses() {
  loading.value = true
  try { const r = await getTeacherCourses(coursePage.value, coursePageSize.value); if (r.code===200) { courses.value = r.data.records; courseTotal.value = r.data.total } } finally { loading.value = false }
}

async function openClassStudents(cls: AdminClass) {
  drawerType.value = 'class'; drawerTargetId = cls.id; drawerTitle.value = `${cls.className} - 学生列表`
  studentKeyword.value = ''; drawerShow.value = true
  await loadDrawerStudents()
}
async function openCourseStudents(crs: Course) {
  drawerType.value = 'course'; drawerTargetId = crs.id!; drawerTitle.value = `${crs.courseName} - 选课学生`
  studentKeyword.value = ''; drawerShow.value = true
  await loadDrawerStudents()
}
async function loadDrawerStudents() {
  drawerLoading.value = true
  try {
    const kw = studentKeyword.value || undefined
    const r = drawerType.value === 'class' ? await getClassStudents(drawerTargetId, kw) : await getCourseStudents(drawerTargetId, kw)
    if (r.code === 200) drawerStudents.value = r.data
  } finally { drawerLoading.value = false }
}
function onStudentSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => loadDrawerStudents(), 300)
}

function resetForm() { form.courseName=''; form.description=''; form.status=0; form.needEnrollCode=0; form.enrollCode=''; editingId.value=null }
function handleCreate() { resetForm(); showModal.value = true }
function openEdit(row: Course) {
  editingId.value = row.id ?? null
  form.courseName = row.courseName; form.description = row.description ?? ''
  form.status = row.status; form.needEnrollCode = row.needEnrollCode ?? 0; form.enrollCode = row.enrollCode ?? ''
  showModal.value = true
}
async function handleSubmit() {
  try { await formRef.value?.validate() } catch { return }
  submitting.value = true
  try {
    if (editingId.value) { await updateCourse(editingId.value, { ...form }); message.success('课程已更新') }
    else { await createCourse({ ...form }); message.success('课程已创建') }
    showModal.value = false; loadCourses()
  } catch { message.error('操作失败') } finally { submitting.value = false }
}
async function handleToggleStatus(row: Course) {
  const ns = row.status === 1 ? 0 : 1
  try { await toggleCourseStatus(row.id!, ns); message.success(ns===1?'课程已发布':'课程已下线'); loadCourses() } catch { message.error('操作失败') }
}
async function handleDeleteCourse(row: Course) {
  try { await deleteCourse(row.id!); message.success('课程已删除'); loadCourses() } catch { message.error('删除失败') }
}

onMounted(() => loadClasses())
</script>

<style scoped>
/* .page-container { padding: 24px; } */
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: #0F172A; margin: 0; }
.tabs-nav { display: flex; gap: 32px; border-bottom: 1px solid #E2E8F0; margin-bottom: 20px; }
.tab-item { padding: 12px 4px; cursor: pointer; color: #64748B; font-weight: 600; border-bottom: 2px solid transparent; transition: all .3s; }
.tab-item.active { color: #4F46E5; border-bottom-color: #4F46E5; }
.data-grid-header, .grid-row { display: grid; align-items: center; padding: 12px 24px; }
.data-grid-header { grid-template-columns: 2fr 1fr 240px; background: #F1F5F9; border-radius: 8px; font-weight: 700; color: #475569; }
.grid-row { grid-template-columns: 2fr 1fr 240px; background: white; margin-top: 10px; border-radius: 12px; border: 1px solid #F1F5F9; transition: all .2s; }
.grid-row:hover { background: #F8FAFC; border-color: #E2E8F0; box-shadow: 0 4px 6px -1px rgba(0,0,0,.05); }
.name { font-weight: 600; color: #1E293B; }
.time { font-size: 13px; color: #64748B; }
.actions { display: flex; gap: 8px; align-items: center; }
.text-btn { background: none; border: none; cursor: pointer; font-size: 13px; font-weight: 600; color: #64748B; padding: 0; transition: color .2s; }
.text-btn:hover { color: #334155; text-decoration: underline; }
.text-btn.primary { color: #4F46E5; }
.text-btn.warning { color: #F59E0B; }
.text-btn.danger { color: #EF4444; }
.empty-state { padding: 40px; text-align: center; color: #94A3B8; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; padding: 12px 24px; background: white; border-radius: 12px; border: 1px solid #F1F5F9; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; }
</style>
