<template>
  <div class="monitor"><h3 class="title">编码环境监控</h3>
    <div v-if="containers && containers.length" class="grid"><div v-for="c in containers" :key="c.studentId || c.groupId" class="cell"><n-tag :type="c.active?'success':'default'" size="small">{{ c.name || c.studentId }}</n-tag><n-progress :percentage="c.passRate||0" :height="10" style="margin:4px 0" /><n-button size="tiny" @click="emit('intervene','observe',{id:c.studentId||c.groupId})">旁观</n-button><n-button size="tiny" type="error" style="margin-left:4px" @click="emit('intervene','reset_env',{id:c.studentId||c.groupId})">重置</n-button></div></div>
    <n-empty v-else description="暂无容器数据" />
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { NTag, NProgress, NButton, NEmpty } from 'naive-ui'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
const emit = defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const containers = computed(() => (props.liveData?.containers as any[]) || [])
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(160px,1fr));gap:8px}.cell{padding:10px;border:1px solid #E2E8F0;border-radius:8px;text-align:center}</style>
