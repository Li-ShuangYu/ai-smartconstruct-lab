<template>
  <div class="monitor"><h3 class="title">导图预习热力</h3>
    <div v-if="nodeClicks && nodeClicks.length" class="heatmap"><div v-for="n in nodeClicks" :key="n.nodeId" class="heat-cell" :style="{backgroundColor:`rgba(79,70,229,${Math.min(1,n.rate||0)})`,color:(n.rate||0)>0.5?'white':'#1E293B'}">{{ n.label }}</div></div>
    <n-empty v-else description="暂无点击数据" />
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { NEmpty } from 'naive-ui'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const nodeClicks = computed(() => (props.liveData?.nodeClickRates as {nodeId:string;label:string;rate:number}[]) || [])
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.heatmap{display:flex;flex-wrap:wrap;gap:8px}.heat-cell{padding:10px 16px;border-radius:8px;font-size:13px;font-weight:600;transition:all .2s}</style>
