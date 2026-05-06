<template>
  <div class="admin-page">
    <div class="page-header"><h1>操作日志审计</h1></div>
    <div style="display:flex;gap:12px;margin-bottom:12px;align-items:center;flex-wrap:wrap">
      <n-select v-model:value="actionFilter" placeholder="按动作类型" clearable :options="actionOptions" style="max-width:200px" size="small" @update:value="onFilter" />
      <n-input v-model:value="userIdFilter" placeholder="用户ID" clearable style="max-width:140px" size="small" @keyup.enter="onFilter" />
      <n-button size="small" @click="onFilter">筛选</n-button>
      <n-button size="small" @click="resetFilter">重置</n-button>
    </div>
    <n-data-table :columns="columns" :data="data.records" :loading="loading" :bordered="true" size="small" />
    <div style="margin-top:12px;display:flex;justify-content:flex-end">
      <n-pagination v-model:page="page" :item-count="data.total" :page-size="pageSize" @update:page="fetchData" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NButton, NDataTable, NInput, NSelect, NPagination } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import * as api from '@/services/modules/admin.service'
import type { OperationLog, PageResult } from '@/services/types/admin.types'

const loading=ref(false),page=ref(1),pageSize=ref(10)
const actionFilter=ref<string|undefined>(undefined)
const userIdFilter=ref('')
const data=ref<PageResult<OperationLog>>({records:[],total:0,page:1,pageSize:10})

// 从日志数据中提取已有动作类型作为选项
const actionOptions = [
  {label:'新增院系',value:'新增院系'},{label:'编辑院系',value:'编辑院系'},{label:'删除院系',value:'删除院系'},
  {label:'新增专业',value:'新增专业'},{label:'编辑专业',value:'编辑专业'},{label:'删除专业',value:'删除专业'},
  {label:'新增班级',value:'新增班级'},{label:'编辑班级',value:'编辑班级'},{label:'删除班级',value:'删除班级'},
  {label:'新增课程',value:'新增课程'},{label:'编辑课程',value:'编辑课程'},{label:'删除课程',value:'删除课程'},
  {label:'切换课程状态',value:'切换课程状态'},
  {label:'新增编排节点',value:'新增编排节点'},{label:'编辑编排节点',value:'编辑编排节点'},{label:'删除编排节点',value:'删除编排节点'},
  {label:'切换节点开关',value:'切换节点开关'},
  {label:'删除实训模板',value:'删除实训模板'},{label:'修改模板状态',value:'修改模板状态'},
  {label:'新增题库',value:'新增题库'},{label:'编辑题库',value:'编辑题库'},{label:'删除题库',value:'删除题库'},
  {label:'切换题库共享状态',value:'切换题库共享状态'},
  {label:'新增题目',value:'新增题目'},{label:'编辑题目',value:'编辑题目'},{label:'删除题目',value:'删除题目'},
  {label:'变更工单状态',value:'变更工单状态'},
  {label:'删除反馈',value:'删除反馈'},
]

const columns: DataTableColumns<OperationLog> = [
  {title:'ID',key:'id',width:60},
  {title:'用户ID',key:'userId',width:80},
  {title:'动作类型',key:'actionType',width:150},
  {title:'IP地址',key:'ipAddress',width:140},
  {title:'操作时间',key:'createdAt',width:170},
]

function onFilter(){page.value=1;fetchData()}
function resetFilter(){actionFilter.value=undefined;userIdFilter.value='';page.value=1;fetchData()}
async function fetchData(){loading.value=true;try{const r=await api.getLogs(page.value,pageSize.value,actionFilter.value||undefined,userIdFilter.value?Number(userIdFilter.value):undefined);if(r.code===200)data.value=r.data!}catch{}finally{loading.value=false}}
onMounted(()=>fetchData())
</script>
<style scoped>.admin-page{padding:24px;background:#fff;border-radius:8px;min-height:100%}.page-header h1{font-size:20px;font-weight:800;color:#0F172A;margin:0 0 16px}</style>
