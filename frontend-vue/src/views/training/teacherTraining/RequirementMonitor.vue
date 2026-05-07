<template>
  <div class="monitor"><h3 class="title">需求词云与列表</h3>
    <div v-if="wordFreq && wordFreq.length" class="chart-wrap"><div ref="chartRef" style="width:100%;height:240px" /></div>
    <n-empty v-else description="暂无词频数据" />
    <div v-if="reqList && reqList.length" class="req-list"><div v-for="r in reqList" :key="r.id" class="req-row"><span>{{ r.content }}</span><n-button size="tiny" @click="emit('intervene','pin_req',{id:r.id})">置顶</n-button></div></div>
  </div>
</template>
<script setup lang="ts">
import { computed, ref, watch, onMounted, h } from 'vue'
import { NButton, NEmpty } from 'naive-ui'
import * as echarts from 'echarts'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
const emit = defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const chartRef = ref<HTMLElement|null>(null); let chart: echarts.ECharts | null = null
const wordFreq = computed(() => (props.liveData?.wordFrequencies as {name:string;value:number}[]) || [])
const reqList = computed(() => (props.liveData?.requirements as any[]) || [])
function renderChart() {
  if (!chartRef.value || !wordFreq.value.length) return
  if (!chart) chart = echarts.init(chartRef.value)
  // 模拟词云布局：用散点图+文字标签近似
  const max = Math.max(...wordFreq.value.map(w=>w.value), 1)
  chart.setOption({
    tooltip:{}, xAxis:{show:false}, yAxis:{show:false},
    series:[{ type:'scatter', symbolSize:(d:any[])=>Math.max(18,((d[2]??0)/max)*60), data:wordFreq.value.map((w,i)=>({value:[Math.random()*100,Math.random()*100,w.value],name:w.name})),
      label:{show:true,formatter:(p:any)=>(p as any)?.name||'',fontSize:12}, itemStyle:{color:()=>['#4F46E5','#7C3AED','#2563EB','#059669','#D97706'][Math.floor(Math.random()*5)]} }]
  })
}
watch(wordFreq, ()=>{ setTimeout(renderChart,100) },{deep:true})
onMounted(()=>{ setTimeout(renderChart,300) })
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.chart-wrap{margin-bottom:12px}.req-list{max-height:200px;overflow-y:auto}.req-row{display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid #F1F5F9;font-size:13px}</style>
