<template>
  <div class="monitor"><h3 class="title">难度评价分布</h3>
    <div v-if="evalStats && evalStats.length" class="chart-wrap"><div ref="chartRef" style="width:100%;height:240px" /></div>
    <n-empty v-else description="暂无评价数据" />
  </div>
</template>
<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { NEmpty } from 'naive-ui'
import * as echarts from 'echarts'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const chartRef = ref<HTMLElement|null>(null); let chart: echarts.ECharts|null = null
const evalStats = computed(() => (props.liveData?.evalStats as {name:string;easy:number;medium:number;hard:number}[]) || [])
function renderChart(){
  if(!chartRef.value||!evalStats.value.length) return
  if(!chart) chart = echarts.init(chartRef.value)
  chart.setOption({ tooltip:{}, legend:{data:['易','中','难']}, xAxis:{ type:'category', data:evalStats.value.map(e=>e.name) }, yAxis:{ type:'value' }, series:[{ name:'易', type:'bar', stack:'total', data:evalStats.value.map(e=>e.easy||0), itemStyle:{color:'#10B981'} },{ name:'中', type:'bar', stack:'total', data:evalStats.value.map(e=>e.medium||0), itemStyle:{color:'#F59E0B'} },{ name:'难', type:'bar', stack:'total', data:evalStats.value.map(e=>e.hard||0), itemStyle:{color:'#EF4444'} }] })
}
watch(evalStats,()=>setTimeout(renderChart,100),{deep:true}); onMounted(()=>setTimeout(renderChart,300))
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.chart-wrap{margin-bottom:12px}</style>
