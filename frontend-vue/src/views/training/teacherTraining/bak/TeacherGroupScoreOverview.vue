<template>
  <div class="page-container">
    <header class="page-header">
      <div class="header-left">
        <div class="status-indicator bg-green"></div>
        <h1 class="page-title">无人机通信加密 - 综合方案评估</h1>
      </div>
      <button @click="backToSchemeSplit" class="btn-outline">
        <svg style="width: 16px; height: 16px; flex-shrink: 0; margin-right: 8px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" /></svg>
        返回到方案设计
      </button>
    </header>

    <div v-if="!isReady" class="flex-1 flex-col flex-center z-10">
      <div class="spinner-xl border-green mb-6"></div>
      <h2 class="text-2xl font-bold text-slate-800 mb-6 tracking-wider">等待评分完成...</h2>
      <div class="waiting-list">
        <div class="waiting-item">
          <span class="text-slate-500 font-bold">正在等待 AI 批改</span>
          <span v-if="states.aiEvaluated" class="text-green-500 font-black text-lg">✓</span><span v-else class="text-slate-400">⏳</span>
        </div>
        <div class="waiting-item">
          <span class="text-slate-500 font-bold">正在等待 教师评分</span>
          <span v-if="states.teacherScored" class="text-green-500 font-black text-lg">✓</span><span v-else class="text-slate-400">⏳</span>
        </div>
        <div class="waiting-item">
          <span class="text-slate-500 font-bold">正在等待 组间互评</span>
          <span v-if="states.studentScored" class="text-green-500 font-black text-lg">✓</span><span v-else class="text-slate-400">⏳</span>
        </div>
      </div>
    </div>

    <main v-else class="main-content layout-grid-2x2">
      <div v-for="(group, index) in groups" :key="group.id" class="glass-card flex-row overflow-hidden shadow-lg border-2 transition-all" :style="{ borderColor: group.color }">
        
        <div class="w-1-2 flex-col p-5 border-r justify-between" :style="{ borderRightColor: 'rgba(226, 232, 240, 0.8)' }">
          <div class="shrink-0 flex-row gap-2 mb-2">
            <span class="tag-sm" :style="{ backgroundColor: group.color + '20', color: group.color }">{{ group.name }}</span> 
            <span class="text-base font-bold text-slate-800 tracking-wide">综合评估报告</span>
          </div>

          <div class="shrink-0 mb-4 pl-1 flex-row items-baseline gap-3">
            <span class="text-slate-500 text-base font-bold">综合总评分</span>
            <div class="flex-row items-baseline gap-1">
              <span class="text-5xl font-black tracking-tighter score-glow" :style="{ color: group.color, '--glow-color': group.color }">{{ group.totalScore }}</span>
              <span class="text-slate-400 text-base font-medium">/ 100</span>
            </div>
          </div>

          <div class="flex-col gap-3 mt-auto">
            <div class="score-row bg-slate-50 border-l-4" :style="{ borderColor: group.color }">
              <div class="text-sm font-bold text-slate-600">AI 智能评分 <span class="text-xs font-normal opacity-70 ml-1">(33.3%)</span></div>
              <div class="text-base font-bold" :style="{ color: group.color }">{{ group.scores[0] }} 分</div>
            </div>
            <div class="score-row bg-white border-l-4 border-slate-300">
              <div class="text-sm font-bold text-slate-600">教师专家评分 <span class="text-xs font-normal opacity-70 ml-1">(33.3%)</span></div>
              <div class="text-base font-bold text-slate-800">{{ group.scores[1] }} 分</div>
            </div>
            <div class="score-row bg-white border-l-4 border-slate-300">
              <div class="text-sm font-bold text-slate-600">组间互评得分 <span class="text-xs font-normal opacity-70 ml-1">(33.3%)</span></div>
              <div class="text-base font-bold text-slate-800">{{ group.scores[2] }} 分</div>
            </div>
          </div>
        </div>

        <div class="w-1-2 relative p-4">
          <div :id="`chart-${group.id}`" class="chart-dom chart-breathe" :style="{ '--chart-color': group.color }"></div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
// [SCRIPT 代码原封不动，完美保留，与源文件20一致]
import { onMounted, onBeforeUnmount, reactive, ref, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';

const router = useRouter();

const isReady = ref(false);
const states = reactive({ aiEvaluated: false, teacherScored: false, studentScored: false });
let pollingTimer = null;

const backToSchemeSplit = () => { router.push('/teacher/scheme-split'); };

const groups = reactive([
  { id: 1, name: '第一组', color: '#3b82f6', totalScore: 90, scores: [92, 88, 90] },
  { id: 2, name: '第二组', color: '#ef4444', totalScore: 82, scores: [78, 85, 83] },
  { id: 3, name: '第三组', color: '#f59e0b', totalScore: 86, scores: [85, 88, 85] },
  { id: 4, name: '第四组', color: '#8b5cf6', totalScore: 91, scores: [88, 93, 92] }
]);

const chartInstances = [];
const indicators = [ { name: 'AI 评分', max: 120 }, { name: '教师评分', max: 120 }, { name: '组间互评', max: 120 } ];

const initChart = (group) => {
  const chartDom = document.getElementById(`chart-${group.id}`);
  if (!chartDom) return;
  const myChart = echarts.init(chartDom);
  const customIndicators = indicators.map((ind, idx) => ({ name: `${ind.name}\n${group.scores[idx]}分`, max: ind.max }));
  const option = {
    radar: { indicator: customIndicators, radius: '55%', center: ['50%', '50%'], splitNumber: 4, axisName: { color: '#64748b', fontSize: 13, fontWeight: 'bold', lineHeight: 16 }, splitLine: { lineStyle: { color: ['#e2e8f0'], width: 1 } }, splitArea: { show: false }, axisLine: { lineStyle: { color: '#e2e8f0' } } },
    series: [{ type: 'radar', data: [{ value: group.scores, itemStyle: { color: group.color }, areaStyle: { color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [ { color: group.color, offset: 0, opacity: 0.1 }, { color: group.color, offset: 1, opacity: 0.5 } ]) }, lineStyle: { width: 2, color: group.color }, symbol: 'circle', symbolSize: 8 }] }]
  };
  myChart.setOption(option); chartInstances.push(myChart);
};

const handleResize = () => { chartInstances.forEach(instance => instance.resize()); };

const fetchState = async () => {
  try {
    const res = await fetch('/api/state'); const state = await res.json();
    states.aiEvaluated = state.ai_evaluated === 1; states.teacherScored = state.teacher_scored_group === 1; states.studentScored = state.student_scored_group === 1;
    if (states.aiEvaluated && states.teacherScored && states.studentScored) {
      isReady.value = true; if (pollingTimer) clearInterval(pollingTimer);
      nextTick(() => { groups.forEach(group => { initChart(group); }); });
    }
  } catch (error) {}
};

onMounted(() => { fetchState(); pollingTimer = setInterval(fetchState, 1000); window.addEventListener('resize', handleResize); });
onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); chartInstances.forEach(instance => instance.dispose()); if (pollingTimer) clearInterval(pollingTimer); });
</script>

<style scoped>
.page-container { height: 100%; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; }
.page-header { flex-shrink: 0; height: 64px; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); z-index: 10; }
.header-left { display: flex; align-items: center; gap: 12px; }
.status-indicator { width: 6px; height: 24px; border-radius: 4px; }
.bg-green { background-color: #10b981; }
.page-title { font-size: 24px; font-weight: 900; margin: 0; color: #1e293b; }

.btn-outline { background: white; border: 1px solid #e2e8f0; color: #475569; padding: 8px 16px; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; display: flex; align-items: center; transition: all 0.2s; } .btn-outline:hover { background: #f8fafc; color: #1e293b; }

.flex-center { display: flex; align-items: center; justify-content: center; } .flex-col { display: flex; flex-direction: column; } .flex-row { display: flex; align-items: center; } .flex-1 { flex: 1; min-height: 0; }
.gap-2 { gap: 8px; } .gap-3 { gap: 12px; } .mb-6 { margin-bottom: 24px; } .mt-auto { margin-top: auto; } .pl-1 { padding-left: 4px; }
.w-1-2 { width: 50%; }

.spinner-xl { width: 64px; height: 64px; border: 4px solid #f1f5f9; border-top-color: currentColor; border-radius: 50%; animation: spin 1s linear infinite; } .border-green { border-top-color: #10b981; }
.text-2xl { font-size: 24px; } .font-bold { font-weight: bold; } .tracking-wider { letter-spacing: 0.05em; } .text-slate-800 { color: #1e293b; }
.waiting-list { display: flex; flex-direction: column; gap: 12px; width: 320px; }
.waiting-item { display: flex; align-items: center; justify-content: space-between; padding: 12px; background: white; border: 1px solid #e2e8f0; border-radius: 8px; }
.text-slate-500 { color: #64748b; } .text-green-500 { color: #10b981; } .text-slate-400 { color: #94a3b8; } .text-lg { font-size: 18px; } .font-black { font-weight: 900; }

.main-content { padding: 24px; overflow: hidden; }
.layout-grid-2x2 { display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: 24px; height: 100%; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border-radius: 16px; }
.shadow-lg { box-shadow: 0 10px 15px -3px rgba(0,0,0,0.05); } .border-2 { border-width: 2px; border-style: solid; }
.p-4 { padding: 16px; } .p-5 { padding: 20px; } .border-r { border-right: 1px solid; }

.tag-sm { padding: 4px 10px; border-radius: 6px; font-size: 13px; font-weight: bold; }
.text-base { font-size: 16px; } .tracking-wide { letter-spacing: 0.025em; }
.items-baseline { align-items: baseline; }
.text-5xl { font-size: 48px; } .tracking-tighter { letter-spacing: -0.05em; }

.score-row { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-radius: 0 8px 8px 0; }
.bg-slate-50 { background: #f8fafc; } .bg-white { background: white; }
.border-l-4 { border-left-width: 4px; border-left-style: solid; } .border-slate-300 { border-color: #cbd5e1; }
.text-sm { font-size: 14px; } .text-xs { font-size: 12px; } .text-slate-600 { color: #475569; }

.chart-dom { width: 100%; height: 100%; }

::-webkit-scrollbar { width: 0; height: 0; }

.score-glow { animation: scoreGlow 2.5s ease-in-out infinite; }
@keyframes scoreGlow { 0%, 100% { text-shadow: 0 0 8px var(--glow-color); opacity: 1; } 50% { text-shadow: 0 0 15px var(--glow-color), 0 0 25px var(--glow-color); opacity: 0.95; } }
.chart-breathe { animation: chartBreathe 3s ease-in-out infinite; position: relative; }
.chart-breathe::before { content: ''; position: absolute; top: 50%; left: 50%; width: 0; height: 0; border-radius: 50%; transform: translate(-50%, -50%); animation: radarGlow 3s ease-in-out infinite; pointer-events: none; z-index: 1; }
@keyframes chartBreathe { 0%, 100% { transform: scale(1); opacity: 1; } 50% { transform: scale(1.02); opacity: 0.95; } }
@keyframes radarGlow { 0% { width: 0; height: 0; box-shadow: 0 0 0 transparent; opacity: 1; } 50% { width: 60%; height: 60%; box-shadow: 0 0 40px color-mix(in srgb, var(--chart-color) 50%, transparent), 0 0 80px color-mix(in srgb, var(--chart-color) 30%, transparent), inset 0 0 30px color-mix(in srgb, var(--chart-color) 25%, transparent); opacity: 0.5; } 100% { width: 100%; height: 100%; box-shadow: 0 0 60px color-mix(in srgb, var(--chart-color) 20%, transparent), 0 0 100px color-mix(in srgb, var(--chart-color) 10%, transparent); opacity: 0; } }
@keyframes spin { 100% { transform: rotate(360deg); } }
</style>