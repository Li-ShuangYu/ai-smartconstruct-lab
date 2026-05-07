<template>
  <div class="monitor"><h3 class="title">作业监控</h3>
    <div v-if="homeworkData" class="stats"><n-progress type="circle" :percentage="homeworkData?.submitRate??0" :color="(homeworkData?.submitRate??0)>=80?'#10B981':'#F59E0B'" /><span class="stat-label">交卷率</span></div>
    <div v-if="errorRates && errorRates.length" class="chart-wrap"><div ref="chartRef" style="width:100%;height:200px" /></div>
    <n-empty v-else description="暂无答题数据" />
    <div class="controls"><n-input-number v-model:value="newTime" size="small" :min="1" style="width:100px" placeholder="分钟" /><n-button size="small" @click="emit('intervene','adjust_time',{minutes:newTime})" style="margin-left:8px">调整时长</n-button><n-button size="small" type="error" style="margin-left:8px" @click="emit('intervene','force_submit')">强制收卷</n-button></div>
  </div>
</template>
<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { NProgress, NButton, NInputNumber, NEmpty } from 'naive-ui'
import * as echarts from 'echarts'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
const emit = defineEmits<{ (e:'intervene', actionType:string, payload?:any): void }>()
const chartRef = ref<HTMLElement|null>(null); const newTime = ref(10); let chart: echarts.ECharts|null = null
const homeworkData = computed(() => props.liveData as {submitRate?:number;errorRates?:{name:string;rate:number}[]} | null)
const errorRates = computed(() => homeworkData.value?.errorRates || [])
function renderChart(){
  if(!chartRef.value||!errorRates.value.length) return
  if(!chart) chart = echarts.init(chartRef.value)
  chart.setOption({ tooltip:{}, xAxis:{ type:'category', data:errorRates.value.map(e=>e.name) }, yAxis:{ type:'value', max:100 }, series:[{ type:'bar', data:errorRates.value.map(e=>e.rate), itemStyle:{color:'#EF4444'} }] })
}
watch(errorRates,()=>setTimeout(renderChart,100),{deep:true})
onMounted(()=>setTimeout(renderChart,300))
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.stats{display:flex;align-items:center;gap:16px;margin-bottom:12px}.stat-label{font-size:14px;color:#64748B}.chart-wrap{margin-bottom:12px}.controls{display:flex;align-items:center;margin-top:8px}</style>
