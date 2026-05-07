<template>
  <div class="monitor"><h3 class="title">就位状态</h3>
    <n-data-table v-if="students && students.length" :columns="cols" :data="students" :row-key="(r:any)=>r.studentId" size="small" :bordered="false" />
    <n-empty v-else description="暂无学生数据" />
    <div class="actions" v-if="students && students.length"><n-button size="small" type="warning" @click="emit('intervene','remind_all')">催办全部未就位</n-button><n-button size="small" type="primary" style="margin-left:8px" @click="emit('intervene','force_start')">强制开启实训</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { computed, h } from 'vue'
import { NDataTable, NButton, NTag, NEmpty } from 'naive-ui'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
const emit = defineEmits<{ (e:'intervene', actionType:string, payload?:any): void }>()
const students = computed(() => (props.liveData?.students as any[]) || [])
const cols = [{ title:'姓名', key:'realName' },{ title:'状态', key:'ready', render:(r:any)=>h(NTag as any,{type:r.ready?'success':'default',size:'small'},{default:()=>r.ready?'已就位':'未就位'}) },{ title:'进入时间', key:'enterTime' },{ title:'操作', key:'actions', render:(r:any)=>!r.ready?h(NButton as any,{size:'tiny',type:'warning',onClick:()=>emit('intervene','remind',{studentId:r.studentId})},{default:()=>'催办'}):null }]
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.actions{display:flex;margin-top:12px}</style>
