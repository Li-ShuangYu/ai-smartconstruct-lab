<template>
  <div class="admin-page">
    <div class="page-header"><h1>课程统管</h1></div>
    <div style="display:flex;gap:12px;margin-bottom:12px;align-items:center">
      <n-button type="primary" size="small" @click="openModal(null)">新增课程</n-button>
      <n-input v-model:value="keyword" placeholder="搜索名称/代码..." clearable style="max-width:240px" size="small" @keyup.enter="fetchData" />
    </div>
    <n-data-table :columns="columns" :data="data.records" :loading="loading" :bordered="true" size="small" />
    <div style="margin-top:12px;display:flex;justify-content:flex-end">
      <n-pagination v-model:page="page" :item-count="data.total" :page-size="pageSize" @update:page="fetchData" />
    </div>

    <n-modal v-model:show="showModal" preset="card" :title="editingId?'编辑课程':'新增课程'" style="max-width:520px">
      <n-form :model="form" label-placement="left" label-width="100">
        <n-form-item label="课程名称"><n-input v-model:value="form.courseName" placeholder="请输入课程名称" /></n-form-item>
        <n-form-item label="状态"><n-select v-model:value="form.status" :options="[{label:'草稿',value:0},{label:'已发布',value:1}]" style="width: 100%" /></n-form-item>
        <n-form-item label="需要选课码"><n-switch v-model:value="form.needEnrollCode" :checked-value="1" :unchecked-value="0" /></n-form-item>
        <n-form-item v-if="form.needEnrollCode === 1" label="选课授权码"><n-input v-model:value="form.enrollCode" placeholder="请输入选课授权码" /></n-form-item>
        <n-form-item label="授课教师">
          <div class="teacher-select-wrapper">
            <n-input :value="selectedTeacherName" placeholder="点击选择授课教师" :readonly="true" class="teacher-input" />
            <n-button size="small" @click="openTeacherPicker">选择</n-button>
          </div>
        </n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save" :loading="saving">保存</n-button></n-space></template>
    </n-modal>

    <!-- 教师选择弹窗 -->
    <n-modal v-model:show="showTeacherPicker" preset="card" title="选择授课教师" style="width: 600px">
      <n-input v-model:value="teacherKeyword" placeholder="搜索教师姓名..." clearable style="margin-bottom: 12px" />
      <div class="teacher-list">
        <div v-for="teacher in filteredTeachers" :key="teacher.userId" class="teacher-item" :class="{ selected: form.teacherId === teacher.userId }" @click="selectTeacher(teacher)">
          <div class="teacher-info">
            <span class="teacher-name">{{ teacher.realName }}</span>
            <span class="teacher-dept">{{ getDeptName(teacher.deptId) }}</span>
          </div>
          <span v-if="form.teacherId === teacher.userId" class="teacher-check">✓</span>
        </div>
        <div v-if="filteredTeachers.length === 0" class="empty-state">暂无教师数据</div>
      </div>
      <template #footer><n-space justify="end"><n-button @click="showTeacherPicker=false">取消</n-button><n-button type="primary" @click="confirmTeacher">确认选择</n-button></n-space></template>
    </n-modal>

    <!-- 二次确认删除弹窗 -->
    <n-modal v-model:show="showDeleteModal" preset="card" title="确认删除" style="max-width:420px">
      <p style="margin:12px 0;">确定要删除课程 <strong>{{ deleteTarget?.name }}</strong> 吗？此操作不可恢复。</p>
      <template #footer>
        <n-space justify="end"><n-button @click="showDeleteModal=false">取消</n-button><n-button type="error" @click="doDelete" :loading="deleting">确认删除</n-button></n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h, computed } from 'vue'
import { NButton, NDataTable, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NPagination, NTag, NSwitch, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import * as api from '@/services/modules/admin.service'
import type { Course, PageResult, Teacher, Department } from '@/services/types/admin.types'

const message = useMessage()
const loading=ref(false),saving=ref(false),showModal=ref(false),showDeleteModal=ref(false),keyword=ref(''),page=ref(1),pageSize=ref(10),editingId=ref<string|null>(null),deleting=ref(false)
let deleteTarget:any=null
const data=ref<PageResult<Course>>({records:[],total:0,page:1,pageSize:10})
const form=reactive<any>({courseName:'',description:'',status:0,needEnrollCode:0,enrollCode:'',teacherId:''})

// 教师选择相关
const showTeacherPicker = ref(false)
const teacherKeyword = ref('')
const teachers = ref<Teacher[]>([])
const depts = ref<Department[]>([])

const filteredTeachers = computed(() => {
  let result = teachers.value
  if (teacherKeyword.value) {
    const kw = teacherKeyword.value.toLowerCase()
    result = result.filter(t => t.realName.toLowerCase().includes(kw))
  }
  return result
})

const selectedTeacherName = computed(() => {
  if (!form.teacherId) return ''
  const teacher = teachers.value.find(t => t.userId === form.teacherId)
  return teacher?.realName || ''
})

const columns: DataTableColumns<Course> = [
  {title:'课程名称',key:'courseName'},
  {title:'状态',key:'status',width:80,render(row){return row.status===1?h(NTag,{type:'success',size:'small'},{default:()=>'已发布'}):h(NTag,{type:'default',size:'small'},{default:()=>'草稿'})}},
  {title:'授课教师',key:'teacherName',width:120,render(row){return row.teacherName||'-'}},
  {title:'需选课码',key:'needEnrollCode',width:80,render(row){return row.needEnrollCode===1?'是':'否'}},
  {title:'选课码',key:'enrollCode',width:120,render(row){return row.enrollCode||'-'}},
  {title:'创建时间',key:'createdAt',width:160,render(row){return formatDate(row.createdAt)}},
  {title:'操作',key:'actions',width:220,render(row){return h('div',{style:'display:flex;gap:6px'},[
    h(NButton,{size:'tiny',onClick:()=>openModal(row)},{default:()=>'编辑'}),
    h(NButton,{size:'tiny',type:row.status===1?'warning':'success',onClick:()=>toggleStatus(row)},{default:()=>row.status===1?'下线':'发布'}),
    h(NButton,{size:'tiny',type:'error',onClick:()=>handleDelete(row)},{default:()=>'删除'})
  ])}}
]

async function fetchData(){
  loading.value = true
  try {
    const r = await api.getCourses(page.value, pageSize.value, keyword.value || undefined)
    if (r.code === 200) {
      data.value = r.data
    } else {
      message.error(r.message || '获取数据失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message || '网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

function openModal(row:Course|null){
  editingId.value=row?.id??null
  if(row){
    Object.assign(form,{courseName:row.courseName,description:row.description||'',status:row.status,needEnrollCode:row.needEnrollCode||0,enrollCode:row.enrollCode||'',teacherId:row.teacherId||''})
  }else{
    Object.assign(form,{courseName:'',description:'',status:0,needEnrollCode:0,enrollCode:'',teacherId:''})
  }
  showModal.value=true
}

function openTeacherPicker() {
  showTeacherPicker.value = true
}

function selectTeacher(teacher: Teacher) {
  form.teacherId = teacher.userId
}

function confirmTeacher() {
  showTeacherPicker.value = false
}

function getDeptName(deptId: number | undefined): string {
  if (!deptId) return '-'
  const dept = depts.value.find(d => d.id === deptId)
  return dept?.deptName || String(deptId)
}

function getTeacherName(teacherId: string | undefined): string {
  if (!teacherId) return '-'
  const teacher = teachers.value.find(t => t.userId === teacherId)
  return teacher?.realName || teacherId
}

function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

async function fetchTeachers() {
  try {
    const r = await api.getTeachers(1, 1000)
    if (r.code === 200) {
      teachers.value = r.data.records || []
    }
  } catch (e: any) {
    message.error('获取教师数据失败')
  }
}

async function fetchDepts() {
  try {
    const r = await api.getDepts()
    if (r.code === 200) {
      depts.value = r.data || []
    }
  } catch (e: any) {
    message.error('获取院系数据失败')
  }
}
async function save(){
  if(!form.courseName.trim()){message.warning('请输入课程名称');return}
  saving.value=true
  try {
    const body:any = {courseName:form.courseName,description:form.description,status:form.status,needEnrollCode:form.needEnrollCode}
    if(form.teacherId) body.teacherId = form.teacherId
    const res = editingId.value ? await api.updateCourse(editingId.value, body) : await api.addCourse(body)
    if(res.code!==200){message.error(res.message||'保存失败');return}
    message.success('保存成功')
    showModal.value=false
    await fetchData()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '保存失败')
  } finally {
    saving.value=false
  }
}
function handleDelete(row:Course){
  deleteTarget = {name: row.courseName, id: row.id}
  showDeleteModal.value = true
}
async function doDelete(){
  if(!deleteTarget)return
  deleting.value=true
  try {
    const res = await api.deleteCourse(deleteTarget.id)
    if(res.code!==200){message.error(res.message||'删除失败');return}
    message.success('已删除')
    showDeleteModal.value=false
    deleteTarget=null
    await fetchData()
  } catch (e:any){message.error(e?.response?.data?.message||'删除失败')}
  finally{deleting.value=false}
}
async function toggleStatus(row:Course){
  const ns=row.status===1?0:1
  try {
    await api.updateCourseStatus(row.id!,ns)
    message.success('已更新')
    await fetchData()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '状态更新失败')
  }
}

onMounted(async () => {
  await fetchData()
  await fetchTeachers()
  await fetchDepts()
})
</script>
<style scoped>
.admin-page{padding:24px;background:#fff;border-radius:8px;min-height:100%}
.page-header h1{font-size:20px;font-weight:800;color:#0F172A;margin:0 0 16px}
.teacher-select-wrapper{display:flex;gap:8px;align-items:center}
.teacher-input{flex:1}
.teacher-list{max-height:300px;overflow-y:auto;border:1px solid #E2E8F0;border-radius:8px}
.teacher-item{display:flex;justify-content:space-between;align-items:center;padding:12px 16px;cursor:pointer;border-bottom:1px solid #F1F5F9;transition:background .2s}
.teacher-item:last-child{border-bottom:none}
.teacher-item:hover{background:#F8FAFC}
.teacher-item.selected{background:#EEF2FF}
.teacher-info{display:flex;flex-direction:column;gap:2px}
.teacher-name{font-weight:600;color:#1E293B}
.teacher-dept{font-size:13px;color:#64748B}
.teacher-check{color:#4F46E5}
.empty-state{padding:32px;text-align:center;color:#94A3B8}
</style>
