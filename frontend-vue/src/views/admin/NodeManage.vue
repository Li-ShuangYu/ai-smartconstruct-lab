<template>
  <div class="admin-page">
    <div class="page-header"><h1>编排节点管理</h1></div>
    <n-button type="primary" size="small" @click="openModal(null)" style="margin-bottom:12px">新增节点</n-button>
    <n-data-table :columns="columns" :data="data.records" :loading="loading" :bordered="true" size="small" />
    <div style="margin-top:12px;display:flex;justify-content:flex-end">
      <n-pagination v-model:page="page" :item-count="data.total" :page-size="pageSize" @update:page="fetchData" />
    </div>

    <n-modal v-model:show="showModal" preset="card" :title="editingId?'编辑节点':'新增节点'" style="max-width:460px">
      <n-form :model="form" label-placement="left" label-width="80">
        <n-form-item label="节点编码"><n-input v-model:value="form.nodeCode" placeholder="如: UPLOAD" /></n-form-item>
        <n-form-item label="节点名称"><n-input v-model:value="form.nodeName" placeholder="如: 方案上传" /></n-form-item>
        <n-form-item label="启用"><n-switch v-model:value="form.isActive" :checked-value="1" :unchecked-value="0" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showModal=false">取消</n-button><n-button type="primary" @click="save" :loading="saving">保存</n-button></n-space></template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { NButton, NDataTable, NModal, NForm, NFormItem, NInput, NSpace, NPagination, NSwitch, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import * as api from '@/services/modules/admin.service'
import type { NodeDef, PageResult } from '@/services/types/admin.types'

const message=useMessage()
const loading=ref(false),saving=ref(false),showModal=ref(false),page=ref(1),pageSize=ref(10),editingId=ref<number|null>(null)
const data=ref<PageResult<NodeDef>>({records:[],total:0,page:1,pageSize:10})
const form=reactive<NodeDef>({nodeCode:'',nodeName:'',isActive:1})

const columns: DataTableColumns<NodeDef> = [
  {title:'ID',key:'id',width:60},
  {title:'节点编码',key:'nodeCode',width:120},
  {title:'节点名称',key:'nodeName'},
  {title:'全局开关',key:'isActive',width:100,render(row){return row.isActive===1?h(NTag,{type:'success',size:'small'},{default:()=>'启用'}):h(NTag,{type:'default',size:'small'},{default:()=>'停用'})}},
  {title:'操作',key:'actions',width:220,render(row){return h('div',{style:'display:flex;gap:6px'},[
    h(NButton,{size:'tiny',onClick:()=>openModal(row)},{default:()=>'编辑'}),
    h(NButton,{size:'tiny',type:row.isActive===1?'warning':'success',onClick:()=>doToggle(row)},{default:()=>row.isActive?'停用':'启用'}),
    h(NButton,{size:'tiny',type:'error',onClick:()=>handleDelete(row)},{default:()=>'删除'})
  ])}}
]

async function fetchData(){loading.value=true;try{const r=await api.getNodes(page.value,pageSize.value);if(r.code===200)data.value=r.data!}catch{}finally{loading.value=false}}
function openModal(row:NodeDef|null){editingId.value=row?.id??null;if(row){form.nodeCode=row.nodeCode;form.nodeName=row.nodeName;form.isActive=row.isActive}else{form.nodeCode='';form.nodeName='';form.isActive=1};showModal.value=true}
async function save(){if(!form.nodeCode.trim()||!form.nodeName.trim()){message.warning('请填写完整');return};saving.value=true;try{editingId.value?await api.updateNode(editingId.value,{...form}):await api.addNode({...form});message.success('成功');showModal.value=false;await fetchData()}catch{message.error('失败')}finally{saving.value=false}}
async function handleDelete(row:NodeDef){try{await api.deleteNode(row.id!);message.success('已删除');await fetchData()}catch{message.error('失败')}}
async function doToggle(row:NodeDef){const ns=row.isActive===1?0:1;try{await api.toggleNode(row.id!,ns);message.success('已更新');await fetchData()}catch{message.error('失败')}}
onMounted(()=>fetchData())
</script>
<style scoped>.admin-page{padding:24px;background:#fff;border-radius:8px;min-height:100%}.page-header h1{font-size:20px;font-weight:800;color:#0F172A;margin:0 0 16px}</style>
