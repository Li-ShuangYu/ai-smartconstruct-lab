<template>
  <div class="page-container">
    <div class="page-header flex-between mb-5">
      <div>
        <h1 class="text-2xl font-bold text-slate-800">各组任务方向</h1>
        <div class="title-underline"></div>
      </div>
      <div class="flex-row gap-3">
        <div v-show="isGeneratingSongs" class="text-sm font-bold text-slate-500">{{ generationStep }}</div>
        <div class="flex-row gap-3">
          <button 
            @click="generateTeamSongs" 
            :disabled="isGeneratingSongs || allSongsGenerated"
            class="btn-generate"
            :class="isGeneratingSongs || allSongsGenerated ? 'btn-disabled' : 'btn-active'"
          >
           <span>{{ isGeneratingSongs ? '正在生成队歌' : allSongsGenerated ? '队歌生成完毕' : '生成各组队歌' }}</span>
           <svg v-if="isGeneratingSongs" style="width: 16px; height: 16px; flex-shrink: 0;" class="spinner border-slate-400" viewBox="0 0 24 24"></svg>
           <svg v-else-if="allSongsGenerated" style="width: 16px; height: 16px; flex-shrink: 0;" fill="currentColor" viewBox="0 0 24 24"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"></path></svg>
           <svg v-else style="width: 16px; height: 16px; flex-shrink: 0;" fill="currentColor" viewBox="0 0 24 24"><path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"></path></svg>
          </button>
          <button @click="goToNextStage" class="btn-primary">
            进入下一阶段
            <svg style="width: 16px; height: 16px; flex-shrink: 0; margin-left: 4px;" fill="currentColor" viewBox="0 0 24 24"><path d="M12 5v14l7-7z"></path></svg>
          </button>
        </div>
      </div>
    </div>
    
    <div class="main-content layout-grid-2x2">
      <div v-for="(group, idx) in groups" :key="idx" class="glass-card flex-col relative overflow-hidden group-card" :style="{ borderTopWidth: '4px', borderTopColor: groupConfigs[idx].color }">
        
        <!-- 音乐跳动效果 (使用纯CSS，脱离 Tailwind) -->
        <div v-if="isMusicPlaying && currentPlayingGroup === idx" class="music-wave-container">
          <div v-for="(bar, bIndex) in bars" :key="bIndex" class="music-bar" :style="{ backgroundColor: groupConfigs[idx].color, height: (bar * 80) + '%', opacity: 0.1 + (bar * 0.15) }"></div>
        </div>

        <div class="card-header flex-between border-b relative z-10 bg-white/50">
          <div class="flex-row gap-3">
            <div class="badge-round" :style="{ backgroundColor: groupConfigs[idx].color }">组{{ idx + 1 }}</div>
            <h2 class="text-lg font-bold text-slate-800">{{ groupConfigs[idx].title }}</h2>
          </div>
          <div class="flex-row gap-2">
            <button v-show="group.isSongGenerated" @click="playMusic(idx)" class="btn-play" :style="{ color: groupConfigs[idx].color, backgroundColor: groupConfigs[idx].color + '1a' }">
              <svg v-if="!group.isPlaying" style="width: 12px; height: 12px;" fill="currentColor" viewBox="0 0 24 24"><path d="M8 5v14l11-7z"></path></svg>
              <svg v-else style="width: 12px; height: 12px;" fill="currentColor" viewBox="0 0 24 24"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"></path></svg>
            </button>
            <span class="tag-success">任务已确认</span>
          </div>
        </div>
        
        <transition name="fade">
          <div v-if="group.isLoading" class="overlay-mask">
            <div class="font-bold text-sm tracking-widest animate-pulse mb-3" :style="{ color: groupConfigs[idx].color }">AI 任务辅助分析中...</div>
            <div class="progress-bg"><div class="progress-bar" :style="{ width: group.progress + '%', backgroundColor: groupConfigs[idx].color }"></div></div>
          </div>
        </transition>
        
        <transition name="fade">
          <div v-show="!group.isLoading" class="card-body flex-row gap-5 relative z-10">
            <div class="flex-1 flex-col justify-center gap-3">
              <div>
                <p class="font-bold mb-1 flex-row text-[13px]" :style="{ color: groupConfigs[idx].color }"><span class="rect-mark" :style="{ backgroundColor: groupConfigs[idx].color }"></span>【主线任务：通信加密设计】</p>
                <p class="text-xs text-slate-500 pl-3 leading-relaxed">{{ groupConfigs[idx].mainTask }}</p>
              </div>
              <div>
                <p class="font-bold mb-1 flex-row text-[13px]" :style="{ color: groupConfigs[idx].color }"><span class="rect-mark" :style="{ backgroundColor: groupConfigs[idx].color }"></span>【支线任务：{{ groupConfigs[idx].subTitle }}】</p>
                <p class="text-xs text-slate-500 pl-3 leading-relaxed">{{ groupConfigs[idx].subTask }}</p>
              </div>
            </div>
            
            <div class="divider-v"></div>
            
            <div class="flex-1 flex-col justify-center gap-3">
              <div class="info-box" :style="{ backgroundColor: groupConfigs[idx].color + '0a', borderColor: groupConfigs[idx].color + '33' }">
                <span class="font-bold text-[12px] flex-row mb-1" :style="{ color: groupConfigs[idx].color }">
                  <svg style="width: 14px; height: 14px; flex-shrink: 0; margin-right: 4px;" fill="currentColor" viewBox="0 0 20 20"><path d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v2H7a1 1 0 100 2h2v2a1 1 0 102 0v-2h2a1 1 0 100-2h-2V7z"></path></svg> AI 任务辅助分析
                </span>
                <p class="text-slate-500 text-[11.5px] leading-snug">{{ groupConfigs[idx].aiInsight }}</p>
              </div>
              <div class="info-box bg-white">
                <p class="text-xs text-slate-700 font-bold mb-2"><span class="text-slate-400 font-normal">预选方案：</span>{{ groupConfigs[idx].plan }}</p>
                <div class="flex-row text-[11px] pt-2 border-t border-slate-100 text-slate-500">
                  <span class="bg-slate-100 px-2 py-0.5 rounded mr-2 font-bold shrink-0">3人分工</span>
                  <span class="truncate">理论型 | 实践型 | 组织型</span>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
  <audio ref="audioElement" class="hidden"></audio>
</template>

<script setup>
// [SCRIPT 代码原封不动，完美保留，与源文件21一致]
import { onMounted, onUnmounted, reactive, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import audioLight from '../../../assets/audio/轻量级.mp3';
import audioSideChannel from '../../../assets/audio/侧信道.mp3';
import audioAntiReplay from '../../../assets/audio/抗重放.mp3';
import audioPostQuantum from '../../../assets/audio/后量子算法.mp3';

const audioFiles = { 0: audioLight, 1: audioSideChannel, 2: audioAntiReplay, 3: audioPostQuantum };
const router = useRouter();

const groupConfigs = [
  { color: '#3b82f6', title: '低功耗优化方向', subTitle: '低功耗专项', mainTask: '实现数据加密传输与双向身份认证，需重点适应无人机算力有限与时延敏感特性。', subTask: '机载设备功耗严格受限，需优化加密算法与密钥流程以降低整体算力开销。', aiInsight: '已辅助明确低功耗与加密平衡指标，推送PRESENT算法架构与简化流程资料。', plan: 'PRESENT轻量级加密 + 简化密钥更新流程' },
  { color: '#ef4444', title: '侧信道防护方向', subTitle: '侧信道防护专项', mainTask: '保障无线通信数据机密性与完整性，加密时延需严格控制以确保飞行指令实时传输。', subTask: '需阻断功耗、时序等物理信息泄露，抵御差分功耗分析等侧信道攻击风险。', aiInsight: '已辅助排查物理信息泄露风险点，推送轻量级掩码与恒定时间代码参考。', plan: 'AES算法 + 轻量级掩码防护技术' },
  { color: '#f59e0b', title: '抗重放攻击方向', subTitle: '抗重放专项', mainTask: '全程加密控制指令与飞行数据，强化身份认证，算法需高度适配机载算力限制。', subTask: '需设置严格的滑动窗口与同步校验机制，阻止截取旧数据包干扰。', aiInsight: '已辅助梳理同步校验逻辑细节，下发滑动窗口计数器与动态机制参考。', plan: 'AES加密算法 + 动态随机数机制' },
  { color: '#8b5cf6', title: '后量子算法适配', subTitle: '后量子适配专项', mainTask: '支持多种数据类型加密，底层通信算法需满足未来量子计算环境下的安全要求。', subTask: '采用抗量子破解的轻量机制，需评估算力消耗并简化适配流程。', aiInsight: '已辅助生成算法适配算力评估指标，推送Kyber密钥封装机制。', plan: '后量子加密算法 + 低算力适配' }
];

const goToNextStage = async () => {
  try { await fetch('/api/state/update', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ teacher_go_scheme: 1 }) }); } catch (error) {}
  router.push('/teacher/scheme-split');
};

const isGeneratingSongs = ref(false);
const generationProgress = ref(0);
const generationStep = ref('');
const allSongsGenerated = computed(() => groups.every(g => g.isSongGenerated));

const generateTeamSongs = () => {
  isGeneratingSongs.value = true; generationProgress.value = 0;
  const steps = ['正在分析各组任务方向...', '正在生成歌词...', '正在调整时长...', '正在合成音乐...', '正在优化音效...'];
  let stepIndex = 0;
  const interval = setInterval(() => {
    generationStep.value = steps[stepIndex]; generationProgress.value = (stepIndex + 1) * 20; stepIndex++;
    if (stepIndex >= steps.length) {
      clearInterval(interval);
      setTimeout(() => { groups.forEach(g => g.isSongGenerated = true); isGeneratingSongs.value = false; generationProgress.value = 0; generationStep.value = ''; }, 500);
    }
  }, 1000);
};

const audioElement = ref(null);
const isMusicPlaying = ref(false);
const currentPlayingGroup = ref(-1);
const audioContext = ref(null);
const analyser = ref(null);
const dataArray = ref(null);
const bars = ref([0, 0, 0, 0, 0, 0, 0, 0]);

const initAudioAnalysis = () => {
  if (audioElement.value) {
    try {
      audioContext.value = new (window.AudioContext || window.webkitAudioContext)();
      const source = audioContext.value.createMediaElementSource(audioElement.value);
      analyser.value = audioContext.value.createAnalyser();
      source.connect(analyser.value); analyser.value.connect(audioContext.value.destination); analyser.value.fftSize = 128;
      dataArray.value = new Uint8Array(analyser.value.frequencyBinCount); updateBars();
    } catch (error) {}
  }
};

const updateBars = () => {
  if (analyser.value && dataArray.value && isMusicPlaying.value) {
    analyser.value.getByteFrequencyData(dataArray.value);
    bars.value = Array.from(dataArray.value.slice(0, 8)).map(value => value / 255);
    requestAnimationFrame(updateBars);
  }
};

const playMusic = (index) => {
  const group = groups[index];
  groups.forEach((g, i) => { if (i !== index && g.isPlaying) g.isPlaying = false; });
  if (group.isPlaying) {
    if (audioElement.value) audioElement.value.pause();
    group.isPlaying = false; isMusicPlaying.value = false; currentPlayingGroup.value = -1;
  } else {
    if (audioElement.value) {
      audioElement.value.src = audioFiles[index];
      audioElement.value.addEventListener('canplaythrough', () => { audioElement.value.play().catch(e => {}); }, { once: true });
    }
    group.isPlaying = true; isMusicPlaying.value = true; currentPlayingGroup.value = index;
    if (!audioContext.value) initAudioAnalysis(); else updateBars();
  }
};

const groups = reactive([
  { isLoading: true, progress: 0, delay: 800, isSongGenerated: false, isPlaying: false },
  { isLoading: true, progress: 0, delay: 1600, isSongGenerated: false, isPlaying: false },
  { isLoading: true, progress: 0, delay: 2400, isSongGenerated: false, isPlaying: false },
  { isLoading: true, progress: 0, delay: 3200, isSongGenerated: false, isPlaying: false }
]);

const startLoadingSimulation = () => {
  groups.forEach((group) => { setTimeout(() => { group.progress = 100; setTimeout(() => { group.isLoading = false; }, 1500); }, group.delay); });
};

onMounted(() => {
  if (audioElement.value) { audioElement.value.addEventListener('ended', () => { groups.forEach(g => g.isPlaying = false); isMusicPlaying.value = false; currentPlayingGroup.value = -1; }); }
  startLoadingSimulation();
});
onUnmounted(() => { if (audioElement.value) audioElement.value.pause(); });
</script>

<style scoped>
.page-container { height: 100%; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; padding: 24px; }
.page-header { flex-shrink: 0; display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.text-2xl { font-size: 24px; } .font-bold { font-weight: bold; }
.title-underline { width: 96px; height: 4px; border-radius: 2px; background: linear-gradient(to right, #3b82f6, #a855f7); margin-top: 8px; }

.flex-row { display: flex; align-items: center; } .flex-col { display: flex; flex-direction: column; } .flex-between { display: flex; justify-content: space-between; align-items: center; } .flex-1 { flex: 1; }
.gap-2 { gap: 8px; } .gap-3 { gap: 12px; } .gap-5 { gap: 20px; }

.btn-generate { display: flex; align-items: center; gap: 8px; padding: 8px 16px; border-radius: 8px; font-size: 14px; font-weight: bold; transition: all 0.2s; border: none; }
.btn-active { background: #dcfce7; color: #047857; cursor: pointer; border: 1px solid #22c55e; } .btn-active:hover { background: #bbf7d0; }
.btn-disabled { background: #f1f5f9; color: #94a3b8; cursor: not-allowed; border: 1px solid #e2e8f0; }
.btn-primary { background: #3b82f6; color: white; padding: 8px 24px; border: none; border-radius: 8px; font-size: 14px; font-weight: bold; cursor: pointer; display: flex; align-items: center; transition: background 0.2s; }
.btn-primary:hover { background: #2563eb; }

.main-content { flex: 1; min-height: 0; }
.layout-grid-2x2 { display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: 20px; height: 100%; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
.group-card { transition: border-color 0.3s; } .group-card:hover { border-color: #cbd5e1; }

.card-header { padding: 12px 20px; border-bottom: 1px solid #e2e8f0; }
.badge-round { width: 40px; height: 24px; border-radius: 12px; color: white; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold; }
.text-lg { font-size: 18px; }
.tag-success { background: #f0fdf4; color: #16a34a; font-size: 12px; padding: 4px 12px; border-radius: 4px; font-weight: bold; border: 1px solid #bbf7d0; }
.btn-play { width: 24px; height: 24px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: none; cursor: pointer; transition: transform 0.2s; } .btn-play:hover { transform: scale(1.1); }

.card-body { padding: 20px; flex: 1; display: flex; align-items: stretch; }
.rect-mark { width: 4px; height: 12px; border-radius: 2px; margin-right: 8px; }
.text-xs { font-size: 12px; } .text-slate-500 { color: #64748b; } .text-slate-700 { color: #334155; }
.divider-v { width: 1px; background: #e2e8f0; margin: 8px 0; }
.info-box { padding: 12px; border: 1px solid #e2e8f0; border-radius: 12px; }
.bg-white { background: white; }

.overlay-mask { position: absolute; inset: 0; top: 53px; background: rgba(255,255,255,0.9); display: flex; flex-direction: column; align-items: center; justify-content: center; z-index: 20; backdrop-filter: blur(4px); }
.progress-bg { width: 66%; height: 6px; background: #e2e8f0; border-radius: 3px; overflow: hidden; }
.progress-bar { height: 100%; transition: width 1.5s linear; }

.music-wave-container { position: absolute; inset: 0; display: flex; align-items: flex-end; padding: 16px; pointer-events: none; z-index: 0; }
.music-bar { flex: 1; margin: 0 2px; border-radius: 2px 2px 0 0; transition: height 0.1s ease-out; }
.truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.spinner { animation: spin 1s linear infinite; border: 2px solid; border-top-color: transparent; border-radius: 50%; }
.animate-pulse { animation: pulse 2s infinite; }
@keyframes spin { 100% { transform: rotate(360deg); } }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }
.fade-enter-active, .fade-leave-active { transition: opacity 0.8s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>