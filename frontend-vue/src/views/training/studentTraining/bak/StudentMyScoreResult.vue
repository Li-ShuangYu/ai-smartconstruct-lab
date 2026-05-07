<template>
  <div class="page-container">
    <header class="page-header">
      <div class="header-left">
        <div class="status-indicator" :style="{ backgroundColor: getHexColor('bg') }"></div>
        <h1 class="page-title">最终成绩与总结报告</h1>
      </div>
      <div class="header-right">
         <span class="group-tag" :style="{ borderColor: getHexColor('border'), color: getHexColor('text') }">
          {{ currentGroup.name }} - {{ currentGroup.title }}
        </span>
      </div>
    </header>

    <main class="main-content">
      <div class="layout-grid-12">
        
        <!-- 左侧分数汇总 -->
        <div class="grid-col-3 flex-col gap-4">
          <div class="glass-card p-6 flex-col">
            <h2 class="text-xl font-bold mb-6 text-slate-800">总分看板</h2>
            <div class="score-grid">
              <div class="score-box">
                <span class="score-label">AI打分</span>
                <span class="score-num" :style="{ color: getHexColor('text') }">{{ currentGroup.scores.ai }}</span>
              </div>
              <div class="score-box">
                <span class="score-label">教师评分</span>
                <span class="score-num">{{ currentGroup.scores.teacher }}</span>
              </div>
              <div class="score-box">
                <span class="score-label">小组互评</span>
                <span class="score-num">{{ currentGroup.scores.peer }}</span>
              </div>
              <div class="score-box bg-slate-100 border-none shadow-inner">
                <span class="score-label text-slate-600">最终得分</span>
                <span class="text-3xl font-black" :style="{ color: getHexColor('text') }">{{ currentGroup.scores.total }}</span>
              </div>
            </div>
          </div>
          
          <div class="glass-card flex-1 p-6 border-top-thick" :style="{ borderTopColor: getHexColor('border') }">
            <h3 class="text-sm font-bold mb-4 text-slate-800">方案评估结论</h3>
            <div class="flex-col gap-4">
              <div class="eval-box" :style="{ borderLeftColor: getHexColor('border') }">
                <div class="text-xs font-bold text-slate-500 mb-1">综合评价</div>
                <div class="text-sm text-slate-700 leading-relaxed">{{ currentGroup.evaluation }}</div>
              </div>
              <div class="eval-box" style="border-left-color: #f59e0b;">
                <div class="text-xs font-bold text-slate-500 mb-1">未来优化方向</div>
                <div class="text-sm text-slate-700 leading-relaxed">{{ currentGroup.optimization }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 中间雷达图 -->
        <div class="grid-col-5 flex-col">
          <div class="glass-card flex-1 p-6 flex-col relative">
            <h3 class="text-lg font-bold mb-4 text-slate-800">系统架构指标雷达图</h3>
            <!-- 雷达图容器 -->
            <div class="radar-wrapper flex-1">
               <div ref="radarChartRef" class="radar-dom"></div>
            </div>
          </div>
        </div>

        <!-- 右侧音乐与总结 -->
        <div class="grid-col-4 flex-col gap-4">
          <div class="glass-card flex-1 p-6 flex-col border-top-thick" :style="{ borderTopColor: getHexColor('border') }">
            <h3 class="text-lg font-bold mb-2 text-slate-800">项目专属队歌</h3>
            <div class="text-2xl font-black tracking-widest text-slate-700 mb-4">🎵 AI 智能生成配乐</div>
            
            <div class="music-info bg-slate-50 p-4 rounded-xl border border-slate-200 mb-6">
              <div class="text-xs font-bold text-slate-500 mb-1">STYLE / 风格解码</div>
              <div class="font-bold text-slate-800 text-sm mb-1">{{ currentGroup.music.tags }}</div>
              <div class="text-xs text-slate-500">{{ currentGroup.music.type }}</div>
            </div>

            <div class="lyric-box flex-1 bg-white border border-slate-200 rounded-xl flex items-center justify-center p-4 overflow-hidden relative">
               <div class="text-lg font-bold text-center" :style="{ color: getHexColor('text') }">
                 (点击播放音乐按钮体验)
               </div>
            </div>
          </div>
        </div>

      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import * as echarts from 'echarts';

const savedColorTheme = ref(null);
const currentGroupId = ref(1);
const radarChartRef = ref(null);
let chartInstance = null;

const groups = [
  {
    id: 1, name: '第一组', title: '通信安全 + 低功耗专用版', subtitle: '轻量级无人机加密', themeColor: '#3b82f6', 
    scores: { ai: 92, teacher: 88, peer: 90, total: 90 }, evaluation: '8ms极低时延，完美适配硬件。', optimization: '引入时间戳加速。', radarData: [92, 88, 90, 85, 95],
    music: { tags: '清脆活泼、快节奏', type: '映射轻量级' }
  },
  {
    id: 2, name: '第二组', title: '通信安全 + 侧信道防护', subtitle: '抗物理攻击高安全', themeColor: '#ef4444', 
    scores: { ai: 85, teacher: 88, peer: 82, total: 85 }, evaluation: '掩码机制极佳。', optimization: '优化随机数。', radarData: [85, 88, 82, 90, 80],
    music: { tags: '沉稳厚重', type: '映射掩码防护' }
  },
  {
    id: 3, name: '第三组', title: '通信安全 + 抗重放攻击', subtitle: '抗重放防护', themeColor: '#f59e0b', 
    scores: { ai: 88, teacher: 90, peer: 85, total: 88 }, evaluation: '滑动窗口阻断重放。', optimization: '升级布隆过滤。', radarData: [88, 90, 85, 85, 88],
    music: { tags: '机械感强', type: '映射时间戳严格' }
  },
  {
    id: 4, name: '第四组', title: '通信安全 + 后量子算法', subtitle: '抗量子暴力破解', themeColor: '#8b5cf6', 
    scores: { ai: 95, teacher: 92, peer: 90, total: 92 }, evaluation: '成功移植Kyber。', optimization: '引入NTT加速。', radarData: [95, 92, 90, 95, 92],
    music: { tags: '未来感', type: '大公钥体积' }
  }
];

const currentGroup = computed(() => groups.find(g => g.id === currentGroupId.value) || groups[0]);

const getHexColor = (type) => {
  const defaultColors = { bg: '#2563eb', border: '#3b82f6', text: '#60a5fa' };
  if (!savedColorTheme.value) return defaultColors[type];
  const borderClass = savedColorTheme.value.activeBorderClass;
  const colorMap = {
    'border-blue-500': { bg: '#2563eb', border: '#3b82f6', text: '#60a5fa' },
    'border-red-500': { bg: '#dc2626', border: '#ef4444', text: '#f87171' },
    'border-amber-500': { bg: '#d97706', border: '#f59e0b', text: '#fbbf24' },
    'border-purple-500': { bg: '#9333ea', border: '#a855f7', text: '#c084fc' }
  };
  return colorMap[borderClass]?.[type] || defaultColors[type];
};

const initRadarChart = () => {
  if (!radarChartRef.value) return;
  if (chartInstance) chartInstance.dispose();
  chartInstance = echarts.init(radarChartRef.value);
  const color = getHexColor('border');
  
  const option = {
    radar: {
      indicator: [ { name: '安全保密性', max: 100 }, { name: '数据完整性', max: 100 }, { name: '可用性', max: 100 }, { name: '成本控制', max: 100 }, { name: '创新性', max: 100 } ],
      radius: '60%', center: ['50%', '50%'],
      axisName: { color: '#475569', fontSize: 14, fontWeight: 'bold' },
      splitLine: { lineStyle: { color: ['#e2e8f0'] } },
      splitArea: { show: false }, axisLine: { lineStyle: { color: '#e2e8f0' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: currentGroup.value.radarData, itemStyle: { color: color },
        areaStyle: { color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [{ color: color, offset: 0, opacity: 0.1 }, { color: color, offset: 1, opacity: 0.4 }]) },
        lineStyle: { width: 3, color: color }, symbolSize: 6
      }]
    }]
  };
  chartInstance.setOption(option);
};

onMounted(() => {
  const storedInfo = localStorage.getItem('selectedGroupInfo');
  if (storedInfo) {
    const groupInfo = JSON.parse(storedInfo);
    savedColorTheme.value = groupInfo.colorTheme;
    currentGroupId.value = parseInt(groupInfo.groupId) || 1;
  }
  nextTick(() => { initRadarChart(); });
  window.addEventListener('resize', () => { if (chartInstance) chartInstance.resize(); });
});
</script>

<style scoped>
.page-container { height: 100%;min-height: 400px;padding: 0px; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; }
.page-header { flex-shrink: 0; height: 64px; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; border-bottom: 1px solid #e2e8f0; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); z-index: 10; }
.header-left { display: flex; align-items: center; gap: 12px; }
.status-indicator { width: 6px; height: 24px; border-radius: 4px; }
.page-title { font-size: 18px; font-weight: 800; margin: 0; }
.group-tag { border: 1px solid; padding: 6px 16px; border-radius: 8px; font-size: 14px; font-weight: bold; background: white; }

.main-content { flex: 1; overflow-y: auto; padding: 24px; }
.layout-grid-12 { display: grid; grid-template-columns: repeat(12, 1fr); gap: 24px; height: 100%; min-height: 500px; }
.grid-col-3 { grid-column: span 3; }
.grid-col-4 { grid-column: span 4; }
.grid-col-5 { grid-column: span 5; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; }
.border-top-thick { border-top-width: 4px; border-top-style: solid; }
.flex-col { display: flex; flex-direction: column; } .flex-1 { flex: 1; }
.p-6 { padding: 24px; } .mb-6 { margin-bottom: 24px; } .mb-4 { margin-bottom: 16px; } .mb-2 { margin-bottom: 8px; } .mb-1 { margin-bottom: 4px; }
.gap-4 { gap: 16px; }

.text-xl { font-size: 20px; } .text-lg { font-size: 18px; } .text-sm { font-size: 14px; } .text-xs { font-size: 12px; } .text-2xl { font-size: 24px; } .text-3xl { font-size: 30px; }
.font-bold { font-weight: bold; } .font-black { font-weight: 900; }
.text-slate-800 { color: #1e293b; } .text-slate-700 { color: #334155; } .text-slate-600 { color: #475569; } .text-slate-500 { color: #64748b; }

.score-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.score-box { background: white; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.score-label { font-size: 12px; font-weight: bold; color: #64748b; margin-bottom: 8px; }
.score-num { font-size: 24px; font-weight: 900; color: #1e293b; }
.bg-slate-100 { background: #f1f5f9; } .border-none { border: none; } .shadow-inner { box-shadow: inset 0 2px 4px 0 rgba(0,0,0,0.06); }

.eval-box { background: white; border: 1px solid #e2e8f0; padding: 16px; border-radius: 12px; border-left-width: 4px; border-left-style: solid; }
.radar-wrapper { position: relative; width: 100%; height: 100%; min-height: 300px; }
.radar-dom { position: absolute; inset: 0; }
.relative { position: relative; }
</style>