<template>
  <div class="monitor"><h3 class="title">任务接收状态</h3>
    <n-data-table v-if="taskList && taskList.length" :columns="cols" :data="taskList" :row-key="(r:any)=>r.groupId||r.studentId" size="small" />
    <n-empty v-else description="暂无任务数据" />
  </div>
</template>
<script setup lang="ts">
import { computed, h } from 'vue'
import { NDataTable, NButton, NTag, NEmpty } from 'naive-ui'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
const emit = defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const taskList = computed(() => (props.liveData?.taskStatus as any[]) || [])
const cols = [{ title:'名称', key:'name' },{ title:'状态', key:'accepted', render:(r:any)=>h(NTag,{type:r.accepted?'success':'warning',size:'small'},{default:()=>r.accepted?'已接收':'未查收'}) },{ title:'操作', key:'actions', render:(r:any)=>!r.accepted?h(NButton,{size:'tiny',type:'warning',onClick:()=>emit('intervene','flash_remind',{id:r.groupId||r.studentId})},{default:()=>'闪烁提醒'}):null }]
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}</style>
