<template>
  <div class="admin-page">
    <div class="page-header"><h1>实训模板库</h1></div>
    <div style="margin-bottom:12px">
      <n-select v-model:value="aiStatus" placeholder="按AI状态筛选" clearable :options="statusOpts" style="max-width:200px" size="small" @update:value="onFilter" />
    </div>
    <n-data-table :columns="columns" :data="data.records" :loading="loading" :bordered="true" size="small" />
    <div style="margin-top:12px;display:flex;justify-content:flex-end">
      <n-pagination v-model:page="page" :item-count="data.total" :page-size="pageSize" @update:page="fetchData" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NButton, NDataTable, NSelect, NPagination, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import * as api from '@/services/modules/admin.service'
import type { TrainingTemplate, PageResult } from '@/services/types/admin.types'

const message=useMessage()
const loading=ref(false),page=ref(1),pageSize=ref(10),aiStatus=ref<number|undefined>(undefined)
const data=ref<PageResult<TrainingTemplate>>({records:[],total:0,page:1,pageSize:10})
const statusOpts=[{label:'草稿',value:0},{label:'AI处理中',value:1},{label:'已就绪',value:2},{label:'异常',value:3}]
const sm:Record<number,{type:string,label:string}>={0:{type:'default',label:'草稿'},1:{type:'warning',label:'处理中'},2:{type:'success',label:'就绪'},3:{type:'error',label:'异常'}}

const columns: DataTableColumns<TrainingTemplate> = [
  {title:'ID',key:'id',width:60},
  {title:'模板名称',key:'templateName'},
  {title:'AI状态',key:'aiStatus',width:100,render(row){const s=sm[row.aiStatus]||sm[0];return h(NTag,{type:s?.type as any,size:'small'},{default:()=>s?.label||'未知'})}},
  {title:'异常信息',key:'errorReason',width:200,render(row){return row.errorReason||'-'}},
  {title:'创建时间',key:'createdAt',width:160},
  {title:'操作',key:'actions',width:80,render(row){return h(NButton,{size:'tiny',type:'error',onClick:()=>handleDelete(row)},{default:()=>'删除'})}}
]

function onFilter(){page.value=1;fetchData()}
async function fetchData(){loading.value=true;try{const r=await api.getTemplates(page.value,pageSize.value,aiStatus.value);if(r.code===200)data.value=r.data!}catch(e:any){message.error(e?.response?.data?.message||'获取模板列表失败')}finally{loading.value=false}}
async function handleDelete(row:TrainingTemplate){try{await api.deleteTemplate(row.id!);message.success('已删除');await fetchData()}catch{message.error('失败')}}
onMounted(()=>fetchData())
</script>
<style scoped>.admin-page{padding:24px;background:#fff;border-radius:8px;min-height:100%}.page-header h1{font-size:20px;font-weight:800;color:#0F172A;margin:0 0 16px}</style>
