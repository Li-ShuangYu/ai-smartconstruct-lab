<template>
  <div class="admin-page">
    <div class="page-header"><h1>题库与题目管理</h1></div>

    <n-tabs v-model:value="activeTab" type="line" animated>
      <!-- 题库 -->
      <n-tab-pane name="banks" tab="题库管理">
        <n-button type="primary" size="small" @click="openBankModal(null)" style="margin-bottom:12px">新增题库</n-button>
        <n-data-table :columns="bankCols" :data="bankData.records" :loading="loading1" :bordered="true" size="small" />
        <div style="margin-top:12px;display:flex;justify-content:flex-end">
          <n-pagination v-model:page="bankPage" :item-count="bankData.total" :page-size="bankPS" @update:page="fetchBanks" />
        </div>
      </n-tab-pane>

      <!-- 题目 -->
      <n-tab-pane name="questions" tab="题目管理">
        <div style="display:flex;gap:12px;margin-bottom:12px;align-items:center">
          <n-button type="primary" size="small" @click="openQModal(null)">新增题目</n-button>
          <n-select v-model:value="qBankFilter" placeholder="按题库筛选" clearable :options="bankOpts" style="max-width:220px" size="small" @update:value="onQBankFilter" />
        </div>
        <n-data-table :columns="qCols" :data="qData.records" :loading="loading2" :bordered="true" size="small" />
        <div style="margin-top:12px;display:flex;justify-content:flex-end">
          <n-pagination v-model:page="qPage" :item-count="qData.total" :page-size="qPS" @update:page="fetchQuestions" />
        </div>
      </n-tab-pane>
    </n-tabs>

    <!-- Bank Modal -->
    <n-modal v-model:show="showBankModal" preset="card" :title="editingBankId?'编辑题库':'新增题库'" style="max-width:460px">
      <n-form :model="bankForm" label-placement="left" label-width="80">
        <n-form-item label="题库名称"><n-input v-model:value="bankForm.bankName" /></n-form-item>
        <n-form-item label="共享"><n-switch v-model:value="bankForm.isPublic" :checked-value="1" :unchecked-value="0" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showBankModal=false">取消</n-button><n-button type="primary" @click="saveBank" :loading="saving">保存</n-button></n-space></template>
    </n-modal>

    <!-- Question Modal -->
    <n-modal v-model:show="showQModal" preset="card" :title="editingQId?'编辑题目':'新增题目'" style="max-width:520px">
      <n-form :model="qForm" label-placement="left" label-width="80">
        <n-form-item label="所属题库"><n-select v-model:value="qForm.bankId" :options="bankOpts" /></n-form-item>
        <n-form-item label="题型"><n-select v-model:value="qForm.questionType" :options="[{label:'单选题',value:1},{label:'多选题',value:2},{label:'判断题',value:3},{label:'简答题',value:4}]" /></n-form-item>
        <n-form-item label="题目内容"><n-input v-model:value="qForm.content" type="textarea" /></n-form-item>
        <n-form-item label="标准答案"><n-input v-model:value="qForm.standardAnswer" type="textarea" /></n-form-item>
        <n-form-item label="分值"><n-input-number v-model:value="qForm.defaultScore" :min="0" :step="1" /></n-form-item>
        <n-form-item label="排序号"><n-input-number v-model:value="qForm.sortNum" :min="0" :step="1" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="showQModal=false">取消</n-button><n-button type="primary" @click="saveQ" :loading="saving">保存</n-button></n-space></template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h } from 'vue'
import { NButton, NDataTable, NModal, NForm, NFormItem, NInput, NInputNumber, NSelect, NSpace, NPagination, NSwitch, NTabPane, NTabs, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import * as api from '@/services/modules/admin.service'
import type { QuestionBank, Question, PageResult } from '@/services/types/admin.types'

const message=useMessage()
const activeTab=ref('banks')
const saving=ref(false),loading1=ref(false),loading2=ref(false)
const bankPage=ref(1),bankPS=ref(10),qPage=ref(1),qPS=ref(10),qBankFilter=ref<number|undefined>(undefined)
const bankData=ref<PageResult<QuestionBank>>({records:[],total:0,page:1,pageSize:10})
const qData=ref<PageResult<Question>>({records:[],total:0,page:1,pageSize:10})

// Bank form
const showBankModal=ref(false),editingBankId=ref<number|null>(null)
const bankForm=reactive<QuestionBank>({bankName:'',isPublic:0})
const bankOpts=computed(()=>bankData.value.records.map(b=>({label:b.bankName,value:b.id})))

// Question form
const showQModal=ref(false),editingQId=ref<number|null>(null)
const qForm=reactive<Question>({bankId:0,questionType:1,content:'',standardAnswer:'',defaultScore:5,sortNum:0})

const bankCols: DataTableColumns<QuestionBank> = [
  {title:'ID',key:'id',width:60},
  {title:'题库名称',key:'bankName'},
  {title:'共享',key:'isPublic',width:80,render(row){return row.isPublic===1?h(NTag,{type:'success',size:'small'},{default:()=>'是'}):h(NTag,{type:'default',size:'small'},{default:()=>'否'})}},
  {title:'操作',key:'actions',width:220,render(row){return h('div',{style:'display:flex;gap:6px'},[
    h(NButton,{size:'tiny',onClick:()=>openBankModal(row)},{default:()=>'编辑'}),
    h(NButton,{size:'tiny',type:row.isPublic===1?'warning':'success',onClick:()=>doShare(row)},{default:()=>row.isPublic?'取消共享':'共享'}),
    h(NButton,{size:'tiny',type:'error',onClick:()=>handleDelBank(row)},{default:()=>'删除'})
  ])}}
]

const qCols: DataTableColumns<Question> = [
  {title:'ID',key:'id',width:60},
  {title:'题目内容',key:'content',ellipsis:{tooltip:true}},
  {title:'题型',key:'questionType',width:80,render(row){const m:Record<number,string>={1:'单选',2:'多选',3:'判断',4:'简答'};return m[row.questionType]||'未知'}},
  {title:'分值',key:'defaultScore',width:70},
  {title:'操作',key:'actions',width:140,render(row){return h('div',{style:'display:flex;gap:6px'},[
    h(NButton,{size:'tiny',onClick:()=>openQModal(row)},{default:()=>'编辑'}),
    h(NButton,{size:'tiny',type:'error',onClick:()=>handleDelQ(row)},{default:()=>'删除'})
  ])}}
]

// Bank CRUD
async function fetchBanks(){loading1.value=true;try{const r=await api.getQuestionBanks(bankPage.value,bankPS.value);if(r.code===200)bankData.value=r.data!}catch{}finally{loading1.value=false}}
function openBankModal(row:QuestionBank|null){editingBankId.value=row?.id??null;if(row){bankForm.bankName=row.bankName;bankForm.isPublic=row.isPublic}else{bankForm.bankName='';bankForm.isPublic=0};showBankModal.value=true}
async function saveBank(){if(!bankForm.bankName.trim()){message.warning('请输入名称');return};saving.value=true;try{editingBankId.value?await api.updateQuestionBank(editingBankId.value,{...bankForm}):await api.addQuestionBank({...bankForm});message.success('成功');showBankModal.value=false;await fetchBanks()}catch{message.error('失败')}finally{saving.value=false}}
async function handleDelBank(row:QuestionBank){try{await api.deleteQuestionBank(row.id!);message.success('已删除');await fetchBanks()}catch{message.error('失败')}}
async function doShare(row:QuestionBank){const ns=row.isPublic===1?0:1;try{await api.toggleBankShare(row.id!,ns);message.success('已更新');await fetchBanks()}catch{message.error('失败')}}

// Question CRUD
function onQBankFilter(){qPage.value=1;fetchQuestions()}
async function fetchQuestions(){loading2.value=true;try{const r=await api.getQuestions(qPage.value,qPS.value,qBankFilter.value);if(r.code===200)qData.value=r.data!}catch{}finally{loading2.value=false}}
function openQModal(row:Question|null){editingQId.value=row?.id??null;if(row){Object.assign(qForm,row)}else{Object.assign(qForm,{bankId:qBankFilter.value||0,questionType:1,content:'',standardAnswer:'',defaultScore:5,sortNum:0})};showQModal.value=true}
async function saveQ(){if(!qForm.content.trim()){message.warning('请输入题目内容');return};saving.value=true;try{editingQId.value?await api.updateQuestion(editingQId.value,{...qForm}):await api.addQuestion({...qForm});message.success('成功');showQModal.value=false;await fetchQuestions()}catch{message.error('失败')}finally{saving.value=false}}
async function handleDelQ(row:Question){try{await api.deleteQuestion(row.id!);message.success('已删除');await fetchQuestions()}catch{message.error('失败')}}

onMounted(async()=>{await fetchBanks();await fetchQuestions()})
</script>
<style scoped>.admin-page{padding:24px;background:#fff;border-radius:8px;min-height:100%}.page-header h1{font-size:20px;font-weight:800;color:#0F172A;margin:0 0 16px}</style>
