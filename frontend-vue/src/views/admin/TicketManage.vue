<template>
  <div class="admin-page">
    <div class="page-header"><h1>工单与反馈处理</h1></div>

    <n-tabs v-model:value="activeTab" type="line" animated>
      <n-tab-pane name="tickets" tab="运维工单">
        <div style="margin-bottom:12px">
          <n-select v-model:value="ticketStatusFilter" placeholder="按状态筛选" clearable :options="[{label:'待处理',value:0},{label:'处理中',value:1},{label:'已解决',value:2}]" style="max-width:200px" size="small" @update:value="onTicketFilter" />
        </div>
        <n-data-table :columns="ticketCols" :data="ticketData.records" :loading="loading1" :bordered="true" size="small" />
        <div style="margin-top:12px;display:flex;justify-content:flex-end">
          <n-pagination v-model:page="ticketPage" :item-count="ticketData.total" :page-size="ticketPS" @update:page="fetchTickets" />
        </div>
      </n-tab-pane>

      <n-tab-pane name="feedbacks" tab="系统反馈">
        <n-data-table :columns="fbCols" :data="fbData.records" :loading="loading2" :bordered="true" size="small" />
        <div style="margin-top:12px;display:flex;justify-content:flex-end">
          <n-pagination v-model:page="fbPage" :item-count="fbData.total" :page-size="fbPS" @update:page="fetchFbs" />
        </div>
      </n-tab-pane>
    </n-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { NButton, NDataTable, NSelect, NPagination, NTabs, NTabPane, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import * as api from '@/services/modules/admin.service'
import type { Ticket, Feedback, PageResult } from '@/services/types/admin.types'

const message=useMessage()
const activeTab=ref('tickets')
const loading1=ref(false),loading2=ref(false)
const ticketPage=ref(1),ticketPS=ref(10),ticketStatusFilter=ref<number|undefined>(undefined)
const fbPage=ref(1),fbPS=ref(10)
const ticketData=ref<PageResult<Ticket>>({records:[],total:0,page:1,pageSize:10})
const fbData=ref<PageResult<Feedback>>({records:[],total:0,page:1,pageSize:10})

const sm:Record<number,{type:string,label:string}>={0:{type:'warning',label:'待处理'},1:{type:'info',label:'处理中'},2:{type:'success',label:'已解决'}}

const ticketCols: DataTableColumns<Ticket> = [
  {title:'ID',key:'id',width:60},
  {title:'标题',key:'title'},
  {title:'内容',key:'content',ellipsis:{tooltip:true},width:300},
  {title:'状态',key:'status',width:90,render(row){const s=sm[row.status]||sm[0];return h(NTag,{type:s.type as any,size:'small'},{default:()=>s.label})}},
  {title:'提交时间',key:'createdAt',width:160},
  {title:'操作',key:'actions',width:160,render(row){return h('div',{style:'display:flex;gap:6px'},[
    h(NButton,{size:'tiny',type:'success',onClick:()=>doTicketStatus(row,2),disabled:row.status===2},{default:()=>'解决'}),
    h(NButton,{size:'tiny',onClick:()=>doTicketStatus(row,1),disabled:row.status===1},{default:()=>'处理'}),
    h(NButton,{size:'tiny',type:'error',onClick:()=>handleDelTicket(row)},{default:()=>'删除'})
  ])}}
]

const fbCols: DataTableColumns<Feedback> = [
  {title:'ID',key:'id',width:60},
  {title:'用户ID',key:'userId',width:80},
  {title:'反馈内容',key:'content',ellipsis:{tooltip:true}},
  {title:'时间',key:'createdAt',width:160},
  {title:'操作',key:'actions',width:80,render(row){return h(NButton,{size:'tiny',type:'error',onClick:()=>handleDelFb(row)},{default:()=>'删除'})}}
]

function onTicketFilter(){ticketPage.value=1;fetchTickets()}
async function fetchTickets(){loading1.value=true;try{const r=await api.getTickets(ticketPage.value,ticketPS.value,ticketStatusFilter.value);if(r.code===200)ticketData.value=r.data!}catch(e:any){message.error(e?.response?.data?.message||'获取工单列表失败')}finally{loading1.value=false}}
async function fetchFbs(){loading2.value=true;try{const r=await api.getFeedbacks(fbPage.value,fbPS.value);if(r.code===200)fbData.value=r.data!}catch(e:any){message.error(e?.response?.data?.message||'获取反馈列表失败')}finally{loading2.value=false}}
async function doTicketStatus(row:Ticket,status:number){try{await api.updateTicketStatus(row.id!,status);message.success('已更新');await fetchTickets()}catch{message.error('失败')}}
async function handleDelTicket(row:Ticket){try{await api.deleteTicket(row.id!);message.success('已删除');await fetchTickets()}catch{message.error('失败')}}
async function handleDelFb(row:Feedback){try{await api.deleteFeedback(row.id!);message.success('已删除');await fetchFbs()}catch{message.error('失败')}}
onMounted(async()=>{await fetchTickets();await fetchFbs()})
</script>
<style scoped>.admin-page{padding:24px;background:#fff;border-radius:8px;min-height:100%}.page-header h1{font-size:20px;font-weight:800;color:#0F172A;margin:0 0 16px}</style>
