<template>
  <div class="monitor"><h3 class="title">互评散点图</h3>
    <div v-if="scores && scores.length" class="chart-wrap"><div ref="chartRef" style="width:100%;height:280px" /></div>
    <n-empty v-else description="暂无互评数据" />
    <div v-if="anomalies && anomalies.length" class="anomalies"><h4>异常评分</h4><n-tag v-for="a in anomalies" :key="a.id" closable type="error" @close="emit('intervene','invalidate_score',{id:a.id})">{{ a.studentName }}: {{ a.score }}</n-tag></div>
  </div>
</template>
<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { NTag, NEmpty } from 'naive-ui'
import * as echarts from 'echarts'
const props = defineProps<{ config?: Record<string,any>; liveData?: any }>()
const emit = defineEmits<{ (e:'intervene', actionType:string, payload:any): void }>()
const chartRef = ref<HTMLElement|null>(null); let chart: echarts.ECharts|null = null
const scores = computed(() => (props.liveData?.reviewScores as {reviewer:string;target:string;score:number}[]) || [])
const anomalies = computed(() => (props.liveData?.anomalies as any[]) || [])
function renderChart(){
  if(!chartRef.value||!scores.value.length) return
  if(!chart) chart = echarts.init(chartRef.value)
  chart.setOption({ tooltip:{}, xAxis:{ type:'value',name:'评分' }, yAxis:{ type:'value',name:'被评者' },
    series:[{ type:'scatter', data:scores.value.map(s=>({value:[s.score,s.target.charCodeAt(0)||0],name:s.reviewer})),
      markLine:{ silent:true, data:[{ yAxis:0 }] } }] })
  chart.off('click'); chart.on('click',(p:any)=>{ if(p.data) emit('intervene','select_outlier',{name:p.name,value:p.value}) })
}
watch(scores,()=>setTimeout(renderChart,100),{deep:true}); onMounted(()=>setTimeout(renderChart,300))
</script>
<style scoped>.monitor{padding:16px}.title{font-size:16px;font-weight:700;margin:0 0 12px 0;color:#0F172A}.chart-wrap{margin-bottom:12px}.anomalies h4{font-size:14px;margin:0 0 8px 0}.anomalies{display:flex;flex-wrap:wrap;gap:6px}</style>
