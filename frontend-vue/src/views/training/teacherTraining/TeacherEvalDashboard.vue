<template>
  <div class="monitor"><h3 class="title">综合评价台</h3>
    <n-data-table v-if="studentScores && studentScores.length" :columns="cols" :data="studentScores" :row-key="(r:any)=>r.studentId" size="small" />
    <n-empty v-else description="暂无评分数据" />
    <n-button type="primary" style="margin-top:12px" @click="emit('intervene','publish_scores',{students:studentScores})">发布总成绩</n-button>
  </div>
</template>
<script setup lang="ts">
import { computed, h } from 'vue'
import { NDataTable, NInput, NButton, NEmpty } from 'naive-ui'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
const emit = defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const studentScores = computed(() => (props.liveData?.studentScores as any[]) || [])
// 分数微调和评语录入使用 h 函数内联 input 供教师编辑
const cols = computed(() => [
  { title:'姓名', key:'realName', width:100 },
  { title:'机评分', key:'machineScore', width:80 },
  { title:'微调分', key:'adjust', width:100, render:(r:any)=>h(NInput,{size:'tiny',value:r.adjustScore,placeholder:'调整',onUpdateValue:(v:string)=>{r.adjustScore=v}}) },
  { title:'评语', key:'comment', render:(r:any)=>h(NInput,{size:'tiny',value:r.comment||'',placeholder:'评语',onUpdateValue:(v:string)=>{r.comment=v}}) }
])
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}</style>
