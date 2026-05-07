<template>
  <div class="monitor"><h3 class="title">阅读进度</h3>
    <n-statistic v-if="avgTime != null" label="平均阅读时长 (分)" :value="avgTime" style="margin-bottom:12px" />
    <div v-if="progressList && progressList.length"><div v-for="s in progressList" :key="s.studentId" class="row"><span>{{ s.realName }}</span><n-progress :percentage="s.progress" :height="18" :color="s.progress===0?'#EF4444':undefined" style="flex:1;margin:0 12px" /><n-button v-if="s.progress===0" size="tiny" type="warning" @click="$emit('intervene','remind_read',{studentId:s.studentId})">提醒</n-button></div></div>
    <n-empty v-else description="暂无阅读数据" />
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { NProgress, NButton, NStatistic, NEmpty } from 'naive-ui'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const progressList = computed(() => (props.liveData?.studentProgress as any[]) || [])
const avgTime = computed(() => props.liveData?.avgReadMinutes ?? null)
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.row{display:flex;align-items:center;margin-bottom:8px;font-size:13px;color:#475569}</style>
