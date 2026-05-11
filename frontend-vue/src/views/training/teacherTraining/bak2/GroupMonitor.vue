<template>
  <div class="monitor"><h3 class="title">分组大厅</h3>
    <n-grid v-if="groups && groups.length" :x-gap="12" :y-gap="12" cols="1 s:2 m:3 l:4">
      <n-gi v-for="g in groups" :key="g.groupId"><n-card size="small" :title="g.groupName"><n-tag type="info" size="small">组长: {{ g.leader }}</n-tag><div class="members"><n-tag v-for="m in g.members" :key="m" size="tiny" style="margin:2px">{{ m }}</n-tag></div></n-card></n-gi>
    </n-grid>
    <n-empty v-else description="暂无分组数据" />
    <div v-if="unassigned && unassigned.length" class="unassigned"><h4>未分组学生</h4><n-tag v-for="u in unassigned" :key="u.studentId" closable @close="$emit('intervene','assign',{studentId:u.studentId})">{{ u.realName }}</n-tag></div>
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { NCard, NTag, NGrid, NGi, NEmpty } from 'naive-ui'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const groups = computed(() => (props.liveData?.groups as any[]) || [])
const unassigned = computed(() => (props.liveData?.unassigned as any[]) || [])
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.members{margin-top:8px}.unassigned{margin-top:16px}.unassigned h4{font-size:14px;margin:0 0 8px 0}</style>
