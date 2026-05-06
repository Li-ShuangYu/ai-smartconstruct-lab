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
        <n-form-item label="课程名称"><n-input v-model:value="form.courseName" /></n-form-item>
        <n-form-item label="课程代码"><n-input v-model:value="form.courseCode" /></n-form-item>
        <n-form-item label="课程简介"><n-input v-model:value="form.description" type="textarea" /></n-form-item>
        <n-form-item label="状态"><n-select v-model:value="form.status" :options="[{label:'草稿',value:0},{label:'已发布',value:1}]" /></n-form-item>
        <n-form-item label="选课码"><n-input v-model:value="form.enrollCode" placeholder="可选" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save" :loading="saving">保存</n-button></n-space></template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { NButton, NDataTable, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NPagination, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import * as api from '@/services/modules/admin.service'
import type { Course, PageResult } from '@/services/types/admin.types'

const message = useMessage()
const loading=ref(false),saving=ref(false),showModal=ref(false),keyword=ref(''),page=ref(1),pageSize=ref(10),editingId=ref<number|null>(null)
const data=ref<PageResult<Course>>({records:[],total:0,page:1,pageSize:10})
const form=reactive<Course>({courseName:'',courseCode:'',description:'',status:0,needEnrollCode:0,enrollCode:''})

const columns: DataTableColumns<Course> = [
  {title:'ID',key:'id',width:60},
  {title:'课程名称',key:'courseName'},
  {title:'课程代码',key:'courseCode',width:120},
  {title:'状态',key:'status',width:80,render(row){return row.status===1?h(NTag,{type:'success',size:'small'},{default:()=>'已发布'}):h(NTag,{type:'default',size:'small'},{default:()=>'草稿'})}},
  {title:'选课码',key:'enrollCode',width:100,render(row){return row.enrollCode||'-'}},
  {title:'操作',key:'actions',width:220,render(row){return h('div',{style:'display:flex;gap:6px'},[
    h(NButton,{size:'tiny',onClick:()=>openModal(row)},{default:()=>'编辑'}),
    h(NButton,{size:'tiny',type:row.status===1?'warning':'success',onClick:()=>toggleStatus(row)},{default:()=>row.status===1?'下线':'发布'}),
    h(NButton,{size:'tiny',type:'error',onClick:()=>handleDelete(row)},{default:()=>'删除'})
  ])}}
]

async function fetchData(){loading.value=true;try{const r=await api.getCourses(page.value,pageSize.value,keyword.value||undefined);if(r.code===200)data.value=r.data!}catch{}finally{loading.value=false}}
function openModal(row:Course|null){editingId.value=row?.id??null;if(row){Object.assign(form,row)}else{Object.assign(form,{courseName:'',courseCode:'',description:'',status:0,needEnrollCode:0,enrollCode:''})};showModal.value=true}
async function save(){if(!form.courseName.trim()){message.warning('请输入课程名称');return};saving.value=true;try{editingId.value?await api.updateCourse(editingId.value,{...form}):await api.addCourse({...form});message.success('成功');showModal.value=false;await fetchData()}catch{message.error('失败')}finally{saving.value=false}}
async function handleDelete(row:Course){try{await api.deleteCourse(row.id!);message.success('已删除');await fetchData()}catch{message.error('失败')}}
async function toggleStatus(row:Course){const ns=row.status===1?0:1;try{await api.updateCourseStatus(row.id!,ns);message.success('已更新');await fetchData()}catch{message.error('失败')}}
onMounted(()=>fetchData())
</script>
<style scoped>.admin-page{padding:24px;background:#fff;border-radius:8px;min-height:100%}.page-header h1{font-size:20px;font-weight:800;color:#0F172A;margin:0 0 16px}</style>
