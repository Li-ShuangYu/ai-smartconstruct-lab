<template>
  <div class="monitor"><h3 class="title">导图绘制状态</h3>
    <n-grid v-if="maps && maps.length" :x-gap="12" :y-gap="12" cols="2 s:3 m:4"><n-gi v-for="m in maps" :key="m.groupId"><n-card size="small" :title="m.groupName" class="thumb-card"><div class="thumb"><span>节点数: {{ m.nodeCount || 0 }}</span></div><n-button size="tiny" @click="emit('intervene','fullscreen',{groupId:m.groupId})">全屏查看</n-button></n-card></n-gi></n-grid>
    <n-empty v-else description="暂无导图数据" />
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { NCard, NGrid, NGi, NButton, NEmpty } from 'naive-ui'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
const emit = defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const maps = computed(() => (props.liveData?.maps as any[]) || [])
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.thumb-card{text-align:center}.thumb{height:80px;display:flex;align-items:center;justify-content:center;background:#F8FAFC;border-radius:8px;margin-bottom:8px;color:#64748B;font-size:13px}</style>
