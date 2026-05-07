<template>
  <div class="page-container">
    <header class="page-header">
      <div class="header-left">
        <div class="status-indicator bg-green"></div>
        <h1 class="page-title">无人机通信加密 - 仿真性能综合对比</h1>
      </div>
      <button class="btn-primary">
        <svg class="icon-sm mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
        进入课程收尾
      </button>
    </header>

    <main class="main-content layout-grid-2x2">
      <div v-for="group in groupConfigs" :key="group.id" class="glass-card flex-col relative" :style="{ borderTopColor: group.themeColor, borderTopWidth: '4px' }">
        <div class="card-header flex-between border-b">
          <h2 class="font-bold flex-row gap-2 text-lg" :style="{ color: group.themeColor }">
            <span class="tag-sm" :style="{ backgroundColor: group.themeColor + '20' }">{{ group.name }}</span>
            {{ group.title }}
          </h2>
          <span class="status-badge flex-row gap-1" :style="{ color: group.themeColor }">
            <span class="status-dot animate-pulse" :style="{ backgroundColor: group.themeColor }"></span>
            {{ group.statusText }}
          </span>
        </div>

        <div class="metrics-grid">
          <div v-for="metric in group.metrics" :key="metric.label" class="metric-box border">
            <div class="metric-label">{{ metric.label }}</div>
            <div class="metric-value" :class="metric.class || 'text-slate-800'">
              {{ metric.value }} <span class="metric-unit">{{ metric.unit }}</span>
            </div>
          </div>
        </div>

        <div class="chart-container">
          <div class="chart-label">实时加密吞吐量波形</div>
          <div :id="`chart-${group.id}`" class="echarts-dom"></div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
// [SCRIPT 代码原封不动，完美保留，与源文件18一致]
import { onMounted, onBeforeUnmount, reactive } from 'vue';
import * as echarts from 'echarts';

const groupConfigs = reactive([
  {
    id: 'group1', name: '第一组', title: '低功耗优化方案', themeColor: '#3b82f6', statusText: '运行中', isAlert: false, baseMetrics: { latency: 45, power: 8.5, successRate: 99.99 },
    metrics: [ { label: '端到端时延', value: '45', unit: 'ms' }, { label: '系统功耗', value: '8.5', unit: 'W', class: 'text-barGreen' }, { label: '加密成功率', value: '99.99%', unit: '', class: 'text-barGreen' } ],
    dataGenerator: () => 80 + (Math.random() * 40 - 20),
    metricGenerator: function() { const waveValue = this.dataGenerator(); const factor = waveValue / 80; return { latency: Math.round(this.baseMetrics.latency * factor), power: (this.baseMetrics.power * factor).toFixed(1), successRate: Math.min(99.99, (this.baseMetrics.successRate - Math.random() * 0.5)).toFixed(2) }; }
  },
  {
    id: 'group2', name: '第二组', title: '侧信道防护方案', themeColor: '#ef4444', statusText: '运行中', isAlert: false, baseMetrics: { latency: 12, power: 8.2, successRate: 98.50 },
    metrics: [ { label: '端到端时延', value: '12', unit: 'ms', class: 'text-barGreen' }, { label: '系统功耗', value: '8.2', unit: 'W' }, { label: '加密成功率', value: '98.50%', unit: '', class: 'text-barOrange' } ],
    dataGenerator: () => 120 + (Math.random() * 60 - 30),
    metricGenerator: function() { const waveValue = this.dataGenerator(); const factor = waveValue / 120; return { latency: Math.round(this.baseMetrics.latency * factor), power: (this.baseMetrics.power * factor).toFixed(1), successRate: Math.min(99.99, (this.baseMetrics.successRate - Math.random() * 1.5)).toFixed(2) }; }
  },
  {
    id: 'group3', name: '第三组', title: '抗重放攻击方案', themeColor: '#f59e0b', statusText: '运行中', isAlert: false, baseMetrics: { latency: 85, power: 15.6, successRate: 99.90 },
    metrics: [ { label: '端到端时延', value: '85', unit: 'ms', class: 'text-barOrange' }, { label: '系统功耗', value: '15.6', unit: 'W' }, { label: '加密成功率', value: '99.90%', unit: '', class: 'text-barGreen' } ],
    counter: 0,
    dataGenerator: function() { this.counter++; return 60 + Math.sin(this.counter / 2) * 20 + (Math.random() * 30 - 15); },
    metricGenerator: function() { const waveValue = this.dataGenerator(); const factor = waveValue / 60; return { latency: Math.round(this.baseMetrics.latency * factor), power: (this.baseMetrics.power * factor).toFixed(1), successRate: Math.min(99.99, (this.baseMetrics.successRate - Math.random() * 0.3)).toFixed(2) }; }
  },
  {
    id: 'group4', name: '第四组', title: '后量子算法适配方案', themeColor: '#8b5cf6', statusText: '运行中', isAlert: false, baseMetrics: { latency: 150, power: 20.4, successRate: 95.50 },
    metrics: [ { label: '端到端时延', value: '150', unit: 'ms', class: 'text-barOrange' }, { label: '系统功耗', value: '20.4', unit: 'W' }, { label: '加密成功率', value: '95.50%', unit: '', class: 'text-barOrange' } ],
    dataGenerator: () => { let val = 30 + Math.random() * 50; if (Math.random() > 0.85) val = Math.random() * 10; return val; },
    metricGenerator: function() { const waveValue = this.dataGenerator(); const factor = waveValue / 30; return { latency: Math.round(this.baseMetrics.latency * factor), power: (this.baseMetrics.power * factor).toFixed(1), successRate: Math.min(99.99, (this.baseMetrics.successRate - Math.random() * 4)).toFixed(2) }; }
  }
]);

let chartInstances = []; let timers = [];

const initCharts = () => {
  groupConfigs.forEach((config) => {
    const chartDom = document.getElementById(`chart-${config.id}`);
    if (!chartDom) return;
    const myChart = echarts.init(chartDom);
    let data = Array.from({ length: 200 }, (_, i) => config.dataGenerator(i));
    const option = {
      animation: false, grid: { top: 30, bottom: 5, left: 0, right: 0 }, xAxis: { type: 'category', show: false, boundaryGap: false }, yAxis: { type: 'value', show: false, min: 'dataMin', max: 'dataMax' },
      series: [{ type: 'line', data: data, smooth: 0.1, symbol: 'none', lineStyle: { color: config.themeColor, width: 2 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: config.themeColor + '66' }, { offset: 1, color: config.themeColor + '00' }]) } }]
    };
    myChart.setOption(option);
    chartInstances.push(myChart);
    const timer = setInterval(() => { data.shift(); data.push(config.dataGenerator()); myChart.setOption({ series: [{ data }] }); }, 30);
    timers.push(timer);
  });
};

const updateMetrics = () => {
  groupConfigs.forEach((config) => {
    const newMetrics = config.metricGenerator();
    config.metrics[0].value = newMetrics.latency; config.metrics[1].value = newMetrics.power; config.metrics[2].value = newMetrics.successRate + '%';
  });
};

let metricTimer = null;
onMounted(() => { setTimeout(initCharts, 100); metricTimer = setInterval(updateMetrics, 1000); window.addEventListener('resize', handleResize); });
onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); timers.forEach(clearInterval); if (metricTimer) clearInterval(metricTimer); chartInstances.forEach(instance => instance.dispose()); });
const handleResize = () => { chartInstances.forEach(instance => instance.resize()); };
</script>

<style scoped>
.page-container { height: 100%; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; }
.page-header { flex-shrink: 0; height: 64px; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); z-index: 10; }
.header-left { display: flex; align-items: center; gap: 12px; }
.status-indicator { width: 6px; height: 24px; border-radius: 4px; }
.bg-green { background-color: #10b981; }
.page-title { font-size: 18px; font-weight: 800; margin: 0; }

.btn-primary { background: #10b981; color: white; padding: 8px 24px; border: none; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; display: flex; align-items: center; transition: background 0.2s; }
.btn-primary:hover { background: #059669; }

.main-content { flex: 1; padding: 24px; overflow: hidden; }
.layout-grid-2x2 { display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: 24px; height: 100%; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); display: flex; flex-direction: column; overflow: hidden; border-top-style: solid; }
.card-header { padding: 12px 20px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.5); display: flex; justify-content: space-between; align-items: center; }
.flex-row { display: flex; align-items: center; } .gap-2 { gap: 8px; } .gap-1 { gap: 4px; }
.font-bold { font-weight: bold; } .text-lg { font-size: 18px; }
.tag-sm { padding: 4px 8px; border-radius: 6px; font-size: 12px; font-weight: bold; }

.status-badge { font-size: 12px; font-weight: bold; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; }

.metrics-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; padding: 16px 20px 8px 20px; }
.metric-box { background: white; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; text-align: center; }
.metric-label { font-size: 12px; color: #64748b; margin-bottom: 4px; font-weight: bold; }
.metric-value { font-size: 20px; font-weight: 900; font-family: monospace; }
.metric-unit { font-size: 12px; font-weight: normal; color: #94a3b8; }
.text-slate-800 { color: #1e293b; }
.text-barGreen { color: #16a34a; } .text-barOrange { color: #d97706; }

.chart-container { flex: 1; width: 100%; padding: 0 20px 20px 20px; position: relative; display: flex; flex-direction: column; min-height: 0; }
.chart-label { font-size: 12px; font-weight: bold; color: #94a3b8; margin-bottom: 8px; z-index: 10; }
.echarts-dom { flex: 1; width: 100%; }

.icon-sm { width: 20px; height: 20px; }
.animate-pulse { animation: pulse 2s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }
</style>