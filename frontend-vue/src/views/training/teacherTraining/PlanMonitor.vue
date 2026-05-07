<template>
  <div class="monitor"><h3 class="title">方案收集状态</h3>
    <n-grid v-if="plans && plans.length" :x-gap="12" :y-gap="12" cols="1 s:2 m:3">
      <n-gi v-for="p in plans" :key="p.id"><n-card size="small" :title="p.name"><p class="file">文件: {{ p.fileName || '未上传' }}</p><n-button size="tiny" v-if="p.fileName" @click="emit('intervene','preview',{id:p.id})">预览</n-button><n-button size="tiny" type="error" style="margin-left:4px" @click="emit('intervene','reject',{id:p.id})">驳回</n-button></n-card></n-gi>
    </n-grid>
    <n-empty v-else description="暂无方案数据" />
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { NCard, NButton, NGrid, NGi, NEmpty } from 'naive-ui'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
const emit = defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const plans = computed(() => (props.liveData?.plans as any[]) || [])
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.file{font-size:12px;color:#64748B;margin-bottom:8px}</style>
