<template>
  <div class="page-container">
    <div class="main-content layout-grid-2x2">
      <div v-for="(group, idx) in groups" :key="idx" class="glass-card flex-col relative overflow-hidden group-card" :style="{ borderTopWidth: '4px', borderTopColor: groupConfigs[idx].color }">
        
        <div class="card-header flex-between border-b relative z-10 bg-white/50">
          <div class="flex-row gap-3">
            <div class="badge-round" :style="{ backgroundColor: groupConfigs[idx].color }">组{{ idx + 1 }}</div>
            <h2 class="text-lg font-bold text-slate-800">{{ groupConfigs[idx].title }}</h2>
          </div>
          <span class="tag-success">需求已提交</span>
        </div>
        
        <transition name="fade">
          <div v-if="group.isLoading" class="overlay-mask">
            <div class="font-bold text-sm tracking-widest animate-pulse mb-3" :style="{ color: groupConfigs[idx].color }">AI 需求深度推演中...</div>
            <div class="progress-bg"><div class="progress-bar" :style="{ width: group.progress + '%', backgroundColor: groupConfigs[idx].color }"></div></div>
          </div>
        </transition>
        
        <transition name="fade">
          <div v-show="!group.isLoading" class="card-body flex-row gap-5 relative z-10">
            <div class="flex-1 flex-col justify-center gap-3">
              <div>
                <p class="font-bold mb-1 flex-row text-[13px]" :style="{ color: groupConfigs[idx].color }"><span class="rect-mark" :style="{ backgroundColor: groupConfigs[idx].color }"></span>【主线需求：通信加密设计】</p>
                <p class="text-xs text-slate-500 pl-3 leading-relaxed">{{ groupConfigs[idx].mainTask }}</p>
              </div>
              <div>
                <p class="font-bold mb-1 flex-row text-[13px]" :style="{ color: groupConfigs[idx].color }"><span class="rect-mark" :style="{ backgroundColor: groupConfigs[idx].color }"></span>【支线需求：{{ groupConfigs[idx].subTitle }}】</p>
                <p class="text-xs text-slate-500 pl-3 leading-relaxed">{{ groupConfigs[idx].subTask }}</p>
              </div>
            </div>
            
            <div class="divider-v"></div>
            
            <div class="flex-1 flex-col justify-center gap-3">
              <div class="info-box" :style="{ backgroundColor: groupConfigs[idx].color + '0a', borderColor: groupConfigs[idx].color + '33' }">
                <span class="font-bold text-[12px] flex-row mb-1" :style="{ color: groupConfigs[idx].color }">
                  <svg style="width: 14px; height: 14px; flex-shrink: 0; margin-right: 4px;" fill="currentColor" viewBox="0 0 20 20"><path d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v2H7a1 1 0 100 2h2v2a1 1 0 102 0v-2h2a1 1 0 100-2h-2V7z"></path></svg> AI需求分类与资料推送
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
</template>

<script setup>
import { onMounted, reactive } from 'vue';

const groupConfigs = [
  { color: '#3b82f6', title: '低功耗优化方向', subTitle: '低功耗专项', mainTask: '满足机密性、完整性、可用性约束，需重点适应无人机算力有限与时延敏感特性。', subTask: '机载设备功耗严格受限，增加密钥更新的低功耗需求设计。', aiInsight: '梳理5条核心需求，已下发低功耗与加密平衡相关参考资料及算力约束建议。', plan: 'PRESENT轻量级加密 + 简化密钥更新流程' },
  { color: '#ef4444', title: '侧信道防护方向', subTitle: '侧信道防护专项', mainTask: '通信加密的时延需严格控制，避免影响无人机飞行指令的实时传输。', subTask: '应对无人机易受侧信道与重放攻击的风险，侧信道抑制泄露是核心防御需求。', aiInsight: '已补充侧信道防护关键点，同步下发掩码防护技术参考资料及工程优化技巧。', plan: 'AES算法 + 轻量级掩码防护技术' },
  { color: '#f59e0b', title: '抗重放攻击方向', subTitle: '抗重放专项', mainTask: '强化身份认证避免非法设备接入链路。加密算法需高度适配机载算力限制。', subTask: '需设置随机数和序列号等关键参数，确保抗重放机制防触发准确无误。', aiInsight: '逻辑校验无遗漏。已自动完善身份认证防非法接入细节，限定安全参数范围。', plan: 'AES加密算法 + 动态随机数机制' },
  { color: '#8b5cf6', title: '后量子算法适配', subTitle: '后量子适配专项', mainTask: '通信数据加密需支持多种数据类型，确保无人机底层指令与数据传输皆安全。', subTask: '评估算力消耗与兼容性需求，简化算法适配流程并降低对无人机的算力负担。', aiInsight: '可视化分类图表已生成。同步下发后量子算法适配所需具体技术指标参数。', plan: '后量子加密算法 + 低算力适配' }
];

const groups = reactive([
  { isLoading: true, progress: 0, delay: 800 },
  { isLoading: true, progress: 0, delay: 1600 },
  { isLoading: true, progress: 0, delay: 2400 },
  { isLoading: true, progress: 0, delay: 3200 }
]);

const startLoadingSimulation = () => {
  groups.forEach((group, index) => {
    setTimeout(() => { group.progress = 100; setTimeout(() => { group.isLoading = false; }, 1500); }, group.delay);
  });
};

onMounted(() => { startLoadingSimulation(); });
</script>

<style scoped>
.page-container { height: 100%; display: flex; flex-direction: column; background: transparent; overflow: hidden; font-family: sans-serif; color: #1e293b; padding: 24px; }
.main-content { flex: 1; min-height: 0; }
.layout-grid-2x2 { display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: 24px; height: 100%; }

.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
.group-card { transition: border-color 0.3s; } .group-card:hover { border-color: #cbd5e1; }
.flex-col { display: flex; flex-direction: column; } .flex-row { display: flex; align-items: center; } .flex-between { display: flex; justify-content: space-between; align-items: center; } .flex-1 { flex: 1; }
.gap-3 { gap: 12px; } .gap-5 { gap: 20px; }

.card-header { padding: 12px 20px; border-bottom: 1px solid #e2e8f0; }
.badge-round { width: 40px; height: 24px; border-radius: 12px; color: white; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold; }
.text-lg { font-size: 18px; } .font-bold { font-weight: bold; }
.tag-success { background: #dcfce7; color: #10b981; font-size: 12px; padding: 4px 12px; border-radius: 4px; font-weight: bold; border: 1px solid #bbf7d0; }

.overlay-mask { position: absolute; inset: 0; top: 53px; background: rgba(255,255,255,0.9); display: flex; flex-direction: column; align-items: center; justify-content: center; z-index: 20; backdrop-filter: blur(4px); }
.progress-bg { width: 66%; height: 6px; background: #e2e8f0; border-radius: 3px; overflow: hidden; }
.progress-bar { height: 100%; transition: width 1.5s linear; }

.card-body { padding: 20px; flex: 1; display: flex; align-items: stretch; }
.rect-mark { width: 4px; height: 12px; border-radius: 2px; margin-right: 8px; }
.text-xs { font-size: 12px; } .text-slate-500 { color: #64748b; } .text-slate-700 { color: #334155; } .text-slate-800 { color: #1e293b; } .text-slate-400 { color: #94a3b8; }
.divider-v { width: 1px; background: #e2e8f0; margin: 8px 0; }
.info-box { padding: 12px; border: 1px solid #e2e8f0; border-radius: 12px; }
.bg-white { background: white; }
.truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.animate-pulse { animation: pulse 2s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }
.fade-enter-active, .fade-leave-active { transition: opacity 0.8s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>