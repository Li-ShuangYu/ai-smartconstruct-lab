<template>
  <div class="monitor"><h3 class="title">班级实训报表</h3>
    <div v-if="reportData"><n-statistic label="完课人数" :value="reportData?.completed??0" /><n-statistic label="总人数" :value="reportData?.total??0" style="margin-left:16px" /></div>
    <div v-if="scoreDist && scoreDist.length" class="chart-wrap"><div ref="chartRef" style="width:100%;height:240px" /></div>
    <n-empty v-else description="暂无评分分布数据" />
    <n-popconfirm @positive-click="emit('intervene','archive')"><template #trigger><n-button type="error">结束实训并归档</n-button></template>此操作不可撤销，确认归档？</n-popconfirm>
  </div>
</template>
<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { NStatistic, NButton, NPopconfirm, NEmpty } from 'naive-ui'
import * as echarts from 'echarts'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
const emit = defineEmits<{ (e:'intervene', actionType:string, payload?:any): void }>()
const chartRef = ref<HTMLElement|null>(null); let chart: echarts.ECharts|null = null
const reportData = computed(() => props.liveData as {completed?:number;total?:number;scoreDist?:{level:string;count:number}[]}|null)
const scoreDist = computed(() => reportData.value?.scoreDist || [])
function renderChart(){
  if(!chartRef.value||!scoreDist.value.length) return
  if(!chart) chart = echarts.init(chartRef.value)
  chart.setOption({ tooltip:{}, series:[{ type:'pie', radius:['40%','70%'], data:scoreDist.value.map(s=>({name:s.level,value:s.count})), label:{show:true} }] })
}
watch(scoreDist,()=>setTimeout(renderChart,100),{deep:true}); onMounted(()=>setTimeout(renderChart,300))
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.chart-wrap{margin-bottom:12px}</style>
