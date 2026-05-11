<template>
  <div class="monitor"><h3 class="title">组间评分排行</h3>
    <div v-if="rankings && rankings.length" class="rank"><div v-for="(r,i) in rankings" :key="r.groupId" class="rank-row"><span class="rank-num">#{{ i+1 }}</span><span>{{ r.groupName }}</span><n-progress :percentage="r.score||0" :height="14" style="flex:1;margin:0 12px" :color="i===0?'#4F46E5':i<3?'#10B981':'#94A3B8'" /></div></div>
    <n-empty v-else description="暂无排行数据" />
    <div class="controls"><n-input-number v-model:value="weight" size="small" :min="0" :max="100" style="width:80px" /><span>% 权重</span><n-button size="small" @click="emit('intervene','adjust_weight',{weight:weight.value})" style="margin-left:8px">应用</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { NProgress, NInputNumber, NButton, NEmpty } from 'naive-ui'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
const emit = defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const rankings = computed(() => (props.liveData?.rankings as any[]) || [])
const weight = ref(props.liveData?.weight || 30)
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.rank-row{display:flex;align-items:center;margin-bottom:8px;font-size:13px}.rank-num{font-weight:800;width:30px;color:#4F46E5}.controls{display:flex;align-items:center;gap:6px;margin-top:12px;font-size:13px}</style>
