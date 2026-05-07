<template>
  <div class="page-container">
    <div class="layout-wrapper">
        <!-- 左侧 -->
        <div class="left-col glass-card border-r flex-col fade-in">
            <div class="header-box border-b">
                <div class="flex-row text-lg">
                    <svg style="width: 20px; height: 20px; flex-shrink: 0;" class="text-green" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path></svg>
                    <span class="font-bold text-slate-800 ml-3">学生需求提交列表</span>
                    <span class="text-green text-sm ml-2 font-bold">主线/支线任务归档</span>
                </div>
                <button disabled class="btn-listening border">
                    <svg v-if="isSimulating" style="width: 12px; height: 12px; flex-shrink: 0;" class="spinner text-slate-500 mr-1.5" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                    <div v-else-if="!hasFinished" class="dot-green mr-1.5 animate-pulse"></div>
                    <span>{{ isSimulating ? `接收第 ${currentPlayingGroup} 组...` : (hasFinished ? '全部接收完毕' : '自动监听中') }}</span>
                </button>
            </div>
            
            <div class="stats-grid border-b">
                <div class="stat-card border bg-white/50" :class="{'shadow-blue': isSimulating}">
                    <div class="text-sm text-slate-500 font-bold">学生任务接收完成率</div>
                    <div class="text-3xl text-slate-800 mt-1 font-black">{{ stats.completionRate }}%</div>
                </div>
                <div class="stat-card border bg-white/50" :class="{'shadow-blue': isSimulating}">
                    <div class="text-sm text-slate-500 font-bold">学生提交需求总数</div>
                    <div class="text-3xl text-slate-800 mt-1 font-black">{{ stats.totalDemands }}<span class="text-sm text-slate-500 ml-1">条</span></div>
                </div>
                <div class="stat-card border bg-white/50" :class="{'shadow-blue': isSimulating}">
                    <div class="text-sm text-slate-500 font-bold">参与提交的学生数</div>
                    <div class="text-3xl text-slate-800 mt-1 font-black">{{ stats.studentCount }}<span class="text-sm text-slate-500 ml-1">人</span></div>
                </div>
            </div>

            <!-- 关键点：滚动区 -->
            <div class="list-wrapper flex-1" style="min-height: 0;">
                <div v-if="visibleDemands.length === 0" class="empty-msg text-slate-500">等待学生提交需求...</div>
                <transition-group name="list" tag="div" class="list-container">
                    <div v-for="(demand, index) in visibleDemands" :key="demand.id" class="list-item border-b hover-bg-light" :style="{ top: `${index * 96}px` }">
                        <div class="time-text">{{ demand.timeText }}</div>
                        <div class="flex-row items-start gap-3">
                            <img :src="`https://ui-avatars.com/api/?name=${demand.name}&background=${demand.avatarBg}&color=fff`" class="avatar shadow-sm" alt="avatar">
                            <div class="flex-col w-full pr-14">
                                <div class="demand-content truncate-2-lines text-slate-700 font-bold" :title="demand.content">{{ demand.content }}</div>
                                <div class="flex-row gap-2 mt-1.5">
                                    <span class="text-sm text-slate-500">{{ demand.name }}（{{ demand.role }}）</span>
                                    <span class="demand-tag border" :class="`tag-${demand.color}`">{{ demand.tag }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </transition-group>
            </div>

            <div class="pagination border-t bg-white/30 text-slate-500">
                <span class="text-green font-bold" v-if="stats.totalDemands > 0">1</span>
                <span class="hover-black cursor-pointer" v-if="stats.totalDemands > 5">2</span>
                <span class="hover-black cursor-pointer" v-if="stats.totalDemands > 10">3</span>
            </div>
        </div>

        <!-- 右侧 -->
        <div class="right-col flex-1 flex-col">
            <div class="flex-1 flex-col glass-card ml-6 mb-6 p-4 min-h-0 fade-in">
                <div class="title-green mb-2">
                    <svg style="width: 20px; height: 20px; flex-shrink: 0; margin-right: 8px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path></svg>
                    需求词云分析 <span class="text-slate-500 font-normal text-sm ml-2">（当前任务：无人机通信加密设计）</span>
                </div>
                <div class="word-cloud-box bg-white/50 border flex-1">
                    <div v-if="visibleWords.length === 0" class="empty-msg text-slate-500">等待数据源生成词云...</div>
                    <transition-group name="fade">
                        <div v-for="word in visibleWords" :key="word.text" class="wc-item absolute" :class="[word.colorClass, word.sizeClass]" :style="{ top: word.top, left: word.left }">
                            {{ word.text }}
                        </div>
                    </transition-group>
                </div>
            </div>

            <div class="flex-1 flex-col glass-card ml-6 p-4 min-h-0 fade-in">
                <div class="flex-between mb-2">
                    <div class="title-green text-sm">
                        <svg style="width: 16px; height: 16px; flex-shrink: 0; margin-right: 8px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"></path></svg>
                        需求汇总梳理
                    </div>
                    <div class="flex-row gap-2">
                        <button @click="triggerAIReview" :disabled="stats.totalDemands === 0 || aiReviewState !== 0" class="btn-review">
                            {{ aiReviewState === 2 ? 'AI 评审已完成' : 'AI 评审各组需求' }}
                        </button>
                        <button @click="navigateToTaskPublish" :disabled="aiReviewState !== 2" class="btn-jump">
                            <svg style="width: 12px; height: 12px; flex-shrink: 0; margin-right: 4px;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                            <span>跳转至任务发布页</span>
                        </button>
                    </div>
                </div>
                
                <div v-if="aiReviewState === 0" class="empty-box bg-slate-50 border text-slate-500 flex-1">
                    暂无汇总数据，请先提取学生需求并进行AI评审
                </div>

                <div v-else-if="aiReviewState === 1 || aiReviewState === 2" class="grid-2x2 flex-1 min-h-0">
                    <div v-for="(group, index) in groups" :key="group.id" class="review-card border bg-white" :style="{ borderColor: !group.isLoading ? (group.color + '66') : '#e2e8f0' }">
                        <transition name="fade-overlay">
                            <div v-if="group.isLoading" class="loading-mask bg-white/90">
                                <div class="scan-line" :style="{ background: `linear-gradient(to right, transparent, ${group.color}, transparent)` }"></div>
                                <div class="mb-3 text-sm font-bold tracking-widest animate-pulse" :style="{ color: group.color }">AI 需求深度推演中...</div>
                                <div class="progress-bg border border-slate-200"><div class="progress-bar" :style="{ width: group.progress + '%', backgroundColor: group.color, boxShadow: `0 0 8px ${group.color}` }"></div></div>
                            </div>
                        </transition>
                        
                        <transition name="fade-content">
                            <div v-show="!group.isLoading" class="review-content custom-scrollbar">
                                <div class="review-header border-b border-slate-200">
                                    <div class="flex-row gap-2">
                                        <div class="icon-bg" :style="{ backgroundColor: group.color + '20', color: group.color }"><svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg></div>
                                        <div class="font-bold text-sm tracking-wide" :style="{ color: group.color }">{{ group.direction }}</div>
                                    </div>
                                    <span class="tag-success border">任务已确认</span>
                                </div>
                                <div class="flex-col gap-2 flex-1 min-h-0">
                                    <div><div class="text-xs font-bold text-slate-800 mb-1">{{ group.mainTaskTitle }}</div><p class="text-xs text-slate-600 leading-snug">{{ group.mainTaskDesc }}</p></div>
                                    <div><div class="text-xs font-bold text-slate-800 mb-1">{{ group.branchTaskTitle }}</div><p class="text-xs text-slate-600 leading-snug">{{ group.branchTaskDesc }}</p></div>
                                </div>
                                <div class="ai-box bg-slate-50 border mt-2 shrink-0">
                                    <div class="text-xs font-bold mb-1 flex-row gap-1" :style="{ color: group.color }"><svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path></svg> AI 任务辅助分析</div>
                                    <p class="text-xs text-slate-500 leading-snug">{{ group.aiAnalysis }}</p>
                                </div>
                            </div>
                        </transition>
                    </div>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
// [SCRIPT 逻辑完美保留，原封不动，与源文件19一致]
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isSimulating = ref(false); const hasFinished = ref(false); const stats = reactive({ completionRate: 0, totalDemands: 0, studentCount: 0 }); const visibleDemands = ref([]); const visibleWords = ref([])
const currentPlayingGroup = ref(0); const animationQueue = reactive([]); const playedGroups = new Set(); let pollingTimer = null; const aiReviewState = ref(0)

const groups = reactive([
  { id: 1, color: '#3b82f6', progress: 0, isLoading: true, delay: 300, direction: '低功耗优化方向', mainTaskTitle: '【主线任务：通信加密设计】', mainTaskDesc: '实现数据加密传输与双向身份认证，需重点适应无人机算力有限与时延敏感特性。', branchTaskTitle: '【支线任务：低功耗专项】', branchTaskDesc: '机载设备功耗严格受限，需优化加密算法与密钥流程以降低整体算力开销。', aiAnalysis: '已辅助明确低功耗与加密平衡指标，推送PRESENT算法架构与简化流程资料，为最终方案奠定基础。' },
  { id: 2, color: '#ef4444', progress: 0, isLoading: true, delay: 600, direction: '侧信道防护方向', mainTaskTitle: '【主线任务：通信加密设计】', mainTaskDesc: '保障无线通信数据机密性与完整性，加密时延需严格控制以确保飞行指令实时传输。', branchTaskTitle: '【支线任务：侧信道防护专项】', branchTaskDesc: '需阻断功耗、时序等物理信息泄露，抵御差分功耗分析等侧信道攻击风险。', aiAnalysis: '已辅助排查物理信息泄露风险点，推送轻量级掩码与恒定时间代码参考，成功引导加固选型。' },
  { id: 3, color: '#f59e0b', progress: 0, isLoading: true, delay: 900, direction: '抗重放攻击方向', mainTaskTitle: '【主线任务：通信加密设计】', mainTaskDesc: '全程加密控制指令与飞行数据，强化身份认证，算法需高度适配机载算力限制。', branchTaskTitle: '【支线任务：抗重放专项】', branchTaskDesc: '需设置严格的滑动窗口与同步校验机制，阻止截取旧数据包干扰，确保指令唯一有效。', aiAnalysis: '已辅助梳理同步校验逻辑细节，下发滑动窗口计数器与动态机制参考，确保防护方案有效无误触。' },
  { id: 4, color: '#8b5cf6', progress: 0, isLoading: true, delay: 1200, direction: '后量子算法适配方向', mainTaskTitle: '【主线任务：通信加密设计】', mainTaskDesc: '支持多种数据类型加密，底层通信算法需满足未来量子计算环境下的安全要求。', branchTaskTitle: '【支线任务：后量子适配专项】', branchTaskDesc: '采用抗量子破解的轻量机制，需评估算力消耗并简化适配流程，降低机载硬件负担。', aiAnalysis: '已辅助生成算法适配算力评估指标，推送Kyber密钥封装机制与降耗优化指南，铺垫最终方案。' }
]);

const allDemands = [
    { id: 1, name: '李论', role: '理论型', avatarBg: '3b82f6', color: 'blue', tag: '主线需求', content: '通过AI资料推送工具查到，密码系统需满足机密性、完整性、可用性。' }, { id: 2, name: '李论', role: '理论型', avatarBg: '3b82f6', color: 'blue', tag: '核心约束', content: '结合无人机场景特性，必须将算力有限、时延敏感作为核心约束。' }, { id: 3, name: '张实', role: '实践型', avatarBg: '3b82f6', color: 'blue', tag: '支线低功耗', content: '我之前做过仿真，无人机机载设备功耗不能太高，这也是重要需求。' }, { id: 4, name: '赵组', role: '组织型', avatarBg: '3b82f6', color: 'blue', tag: 'AI工具交互', content: 'AI助教，我们组梳理了初始需求，麻烦用AI需求分类工具帮我们分类。' }, { id: 5, name: '赵组', role: '组织型', avatarBg: '3b82f6', color: 'blue', tag: 'AI工具交互', content: '请重点标注通信加密相关的核心需求（语音输入同步记录）。' },
    { id: 6, name: '王论', role: '理论型', avatarBg: 'ef4444', color: 'red', tag: '支线侧信道', content: 'AI资料推送工具显示，无人机易受侧信道攻击与重放攻击。' }, { id: 7, name: '王论', role: '理论型', avatarBg: 'ef4444', color: 'red', tag: '通信协议', content: '通信协议设计必须包含严格的侧信道防护与抗重放机制。' }, { id: 8, name: '刘实', role: '实践型', avatarBg: 'ef4444', color: 'red', tag: '时延约束', content: '通信加密的时延不能太长，否则严重影响无人机指令传输。' }, { id: 9, name: '陈组', role: '组织型', avatarBg: 'ef4444', color: 'red', tag: 'AI同步', content: 'AI助教，我们需求已提交，请用分类工具处理，并推送防护资料。' },
    { id: 10, name: '孙论', role: '理论型', avatarBg: 'f59e0b', color: 'yellow', tag: '主线需求', content: 'AI工具推送资料显示，避免非法设备接入链路是核心，必须身份认证。' }, { id: 11, name: '孙论', role: '理论型', avatarBg: 'f59e0b', color: 'yellow', tag: '身份认证', content: '必须实现双向身份认证，且认证握手时延需控制在毫秒级以内。' }, { id: 12, name: '周实', role: '实践型', avatarBg: 'f59e0b', color: 'yellow', tag: '硬件适配', content: '加密算法要适配无人机算力，不能太复杂，否则现实中无法落地。' }, { id: 13, name: '吴组', role: '组织型', avatarBg: 'f59e0b', color: 'yellow', tag: 'AI校验', content: 'AI助教，用需求分类工具帮我们检查是否有遗漏，补充典型安全需求。' },
    { id: 14, name: '郑论', role: '理论型', avatarBg: '8b5cf6', color: 'purple', tag: '支线后量子', content: '后量子算法适配是支线，通过AI资料推送工具获取了算力兼容要求。' }, { id: 15, name: '郑论', role: '理论型', avatarBg: '8b5cf6', color: 'purple', tag: '兼容性', content: '除基础安全性外，需重点评估算法复杂度对现有嵌入式芯片算力的兼容性。' }, { id: 16, name: '王实', role: '实践型', avatarBg: '8b5cf6', color: 'purple', tag: '数据安全', content: '通信数据加密需要支持多种数据类型，确保指令、传输数据都安全。' }, { id: 17, name: '李组', role: '组织型', avatarBg: '8b5cf6', color: 'purple', tag: '可视化图表', content: 'AI助教，请分类整理并标注主线与支线相关需求，生成可视化分类图表。' },
    { id: 18, name: '赵组', role: '组织型', avatarBg: '3b82f6', color: 'blue', tag: '确认接收', content: '组1核心需求5条分类完成，已收到AI推送的低功耗相关参考资料。' }, { id: 19, name: '陈组', role: '组织型', avatarBg: 'ef4444', color: 'red', tag: '确认接收', content: '组2补充的侧信道防护细节已通过AI需求分类工具录入系统。' }, { id: 20, name: '吴组', role: '组织型', avatarBg: 'f59e0b', color: 'yellow', tag: '确认接收', content: '组3需求无遗漏，身份认证时延控制的细节已补充并同步完成。' }, { id: 21, name: '李论', role: '理论型', avatarBg: '3b82f6', color: 'blue', tag: '支线低功耗', content: '结合AI资料补充：机载电池容量有限，加密算法执行时功耗需最低水平。' }, { id: 22, name: '张实', role: '实践型', avatarBg: '3b82f6', color: 'blue', tag: '方案设计', content: '没问题，增加的1条密钥更新低功耗需求在后续方案我会重点考虑。' }, { id: 23, name: '赵组', role: '组织型', avatarBg: '3b82f6', color: 'blue', tag: 'AI备案', content: '同步更新最终需求清单，提交给AI需求分类工具完成备案。' },
    { id: 24, name: '王论', role: '理论型', avatarBg: 'ef4444', color: 'red', tag: '支线侧信道', content: 'AI提示我们在物理层面的硬件侧信道防护考量还不够，需要补充。' }, { id: 25, name: '刘实', role: '实践型', avatarBg: 'ef4444', color: 'red', tag: '方案设计', content: '确实，我把硬件侧信道防护也加进补充需求里了。' }, { id: 26, name: '李组', role: '组织型', avatarBg: '8b5cf6', color: 'purple', tag: '确认接收', content: '后量子算法适配相关技术参数已通过AI资料推送工具全部同步。' }, { id: 27, name: '孙论', role: '理论型', avatarBg: 'f59e0b', color: 'yellow', tag: '主线需求', content: 'AI生成的图表很清晰，主线认证机制的毫秒级时延是设计的硬指标。' }, { id: 28, name: '吴组', role: '组织型', avatarBg: 'f59e0b', color: 'yellow', tag: '最终提交', content: '各组讨论完毕，确认无误，最终需求大纲准备提交系统进行统一审批。' }
]

const wordCloudPool = [
    { text: '通信加密', top: '45%', left: '45%', colorClass: 'text-green-500', sizeClass: 'text-4xl font-bold' }, { text: '低功耗优化', top: '25%', left: '32%', colorClass: 'text-blue-500', sizeClass: 'text-3xl' }, { text: '侧信道防护', top: '70%', left: '60%', colorClass: 'text-red-500', sizeClass: 'text-2xl' }, { text: '抗重放攻击', top: '30%', left: '65%', colorClass: 'text-yellow-500', sizeClass: 'text-xl' }, { text: '后量子算法', top: '65%', left: '28%', colorClass: 'text-purple-500', sizeClass: 'text-xl' },
    { text: '机密性', top: '45%', left: '22%', colorClass: 'text-blue-500', sizeClass: 'text-lg' }, { text: '完整性', top: '45%', left: '75%', colorClass: 'text-red-500', sizeClass: 'text-lg' }, { text: '可用性', top: '18%', left: '50%', colorClass: 'text-yellow-500', sizeClass: 'text-base' }, { text: '时延敏感', top: '78%', left: '50%', colorClass: 'text-purple-500', sizeClass: 'text-base' },
    { text: '算力有限', top: '22%', left: '18%', colorClass: 'text-blue-500', sizeClass: 'text-sm' }, { text: '身份认证', top: '78%', left: '78%', colorClass: 'text-red-500', sizeClass: 'text-sm' }, { text: '密钥更新', top: '78%', left: '22%', colorClass: 'text-yellow-500', sizeClass: 'text-sm' }, { text: '兼容性', top: '22%', left: '78%', colorClass: 'text-purple-500', sizeClass: 'text-sm' }, { text: '低时延', top: '12%', left: '68%', colorClass: 'text-blue-500', sizeClass: 'text-sm' }, { text: '低功耗', top: '82%', left: '32%', colorClass: 'text-red-500', sizeClass: 'text-sm' }
]

let timeUpdateInterval = null

const resetBackendState = async () => { try { await fetch('/api/state/reset', { method: 'POST' }); } catch (error) {} }
const fetchState = async () => { try { const res = await fetch('/api/state'); const state = await res.json(); [1, 2, 3, 4].forEach(groupId => { const fieldName = `demand_g${groupId}_submitted`; if (state[fieldName] === 1 && !playedGroups.has(groupId) && !animationQueue.includes(groupId)) { animationQueue.push(groupId); } }); processQueue(); } catch (error) {} }

const processQueue = () => { if (isSimulating.value || animationQueue.length === 0) return; const nextGroup = animationQueue.shift(); playGroupAnimation(nextGroup); }
const playGroupAnimation = (groupId) => {
    isSimulating.value = true; currentPlayingGroup.value = groupId; playedGroups.add(groupId);
    const demandStart = (groupId - 1) * 7; const demandEnd = groupId * 7; const groupDemands = allDemands.slice(demandStart, demandEnd);
    const wordStart = (groupId - 1) * 4; const wordEnd = groupId === 4 ? 15 : groupId * 4; const groupWords = wordCloudPool.slice(wordStart, wordEnd);
    let currentTick = 0; const totalTicks = groupDemands.length; const intervalMs = 600; 

    const groupSimInterval = setInterval(() => {
        if (currentTick >= totalTicks) { clearInterval(groupSimInterval); isSimulating.value = false; if (playedGroups.size === 4) { hasFinished.value = true; } processQueue(); return; }
        const newItem = { ...groupDemands[currentTick], timestamp: Date.now(), timeText: '刚刚' }; visibleDemands.value.unshift(newItem); if (visibleDemands.value.length > 5) { visibleDemands.value.pop(); }
        stats.totalDemands++; stats.completionRate = Math.min(100, Math.floor((stats.totalDemands / 28) * 100)); stats.studentCount = Math.min(12, Math.ceil((stats.totalDemands / 28) * 12));
        if (currentTick % 2 === 0 && (currentTick / 2) < groupWords.length) { visibleWords.value.push(groupWords[currentTick / 2]); }
        currentTick++;
    }, intervalMs);
}

const startTimeUpdater = () => { if(timeUpdateInterval) clearInterval(timeUpdateInterval); timeUpdateInterval = setInterval(() => { visibleDemands.value.forEach(d => { const diff = Math.floor((Date.now() - d.timestamp) / 1000); d.timeText = diff === 0 ? '刚刚' : `${diff}秒前`; }) }, 1000) }

onMounted(() => { resetBackendState(); startTimeUpdater(); fetchState(); pollingTimer = setInterval(fetchState, 1000); })
onUnmounted(() => { if (pollingTimer) clearInterval(pollingTimer); if (timeUpdateInterval) clearInterval(timeUpdateInterval); })

const triggerAIReview = () => {
    if(aiReviewState.value !== 0) return; aiReviewState.value = 1
    groups.forEach(group => { group.isLoading = true; group.progress = 0; });
    const startLoadingSimulation = () => { groups.forEach((group) => { setTimeout(() => { group.progress = 100; setTimeout(() => { group.isLoading = false; }, 1200); }, group.delay); }); };
    startLoadingSimulation(); setTimeout(() => { aiReviewState.value = 2; }, 4000);
}
const navigateToTaskPublish = () => { router.push('/teacher/task-publish') }
</script>

<style scoped>
.page-container { height: 100%; display: flex; flex-direction: column; overflow: hidden; background: transparent; color: #1e293b; font-family: sans-serif; padding: 24px; }
.layout-wrapper { display: flex; flex: 1; min-height: 0; overflow: hidden; }
.glass-card { background: rgba(255,255,255,0.7); backdrop-filter: blur(10px); border: 1px solid #e2e8f0; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }

.border-b { border-bottom: 1px solid #e2e8f0; } .border-r { border-right: 1px solid #e2e8f0; } .border-t { border-top: 1px solid #e2e8f0; } .border { border: 1px solid #e2e8f0; }
.text-gray { color: #64748b; } .text-gray-light { color: #94a3b8; } .text-white { color: #1e293b; }
.text-green { color: #10b981; } .text-green-dark { color: #047857; } .title-green { color: #10b981; font-weight: bold; display: flex; align-items: center; }

.flex-1 { flex: 1; min-height: 0; } .flex-col { display: flex; flex-direction: column; } .flex-row { display: flex; align-items: center; } .flex-between { display: flex; justify-content: space-between; align-items: center; }
.gap-2 { gap: 8px; } .gap-3 { gap: 12px; } .p-4 { padding: 16px; }

/* 左侧栏 */
.left-col { width: 45%; display: flex; flex-direction: column; }
.header-box { padding: 16px; display: flex; justify-content: space-between; align-items: center; }
.btn-listening { background: white; color: #64748b; padding: 6px 12px; border-radius: 6px; display: flex; align-items: center; font-size: 13px; font-weight: bold; }
.dot-green { width: 8px; height: 8px; background: #10b981; border-radius: 50%; }

.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; padding: 16px; }
.stat-card { border-radius: 8px; padding: 12px; height: 80px; display: flex; flex-direction: column; justify-content: space-between; transition: all 0.3s; }
.shadow-blue { box-shadow: 0 0 15px rgba(59,130,246,0.15); border-color: #3b82f6; }

/* 防止套娃核心：必须在滚动父级加 min-height: 0 */
.list-wrapper { position: relative; overflow: hidden; min-height: 0; }
.list-container { display: flex; flex-direction: column; position: relative; height: 100%; }
.list-item { position: absolute; width: 100%; padding: 16px; transition: all 0.3s; background: rgba(255,255,255,0.5); }
.hover-bg-light:hover { background: white; }
.time-text { position: absolute; top: 16px; right: 16px; font-size: 12px; color: #94a3b8; }
.avatar { width: 40px; height: 40px; border-radius: 50%; flex-shrink: 0; }
.demand-content { font-size: 14px; line-height: 1.5; margin-bottom: 8px; }
.demand-tag { font-size: 11px; padding: 2px 8px; border-radius: 4px; font-weight: bold; }
.tag-blue { background: rgba(59,130,246,0.1); color: #3b82f6; border-color: rgba(59,130,246,0.2); }
.tag-red { background: rgba(239,68,68,0.1); color: #ef4444; border-color: rgba(239,68,68,0.2); }
.tag-yellow { background: rgba(245,158,11,0.1); color: #f59e0b; border-color: rgba(245,158,11,0.2); }
.tag-purple { background: rgba(139,92,246,0.1); color: #8b5cf6; border-color: rgba(139,92,246,0.2); }

.pagination { padding: 12px 16px; display: flex; justify-content: center; gap: 12px; font-size: 14px; min-height: 44px; font-weight: bold; }
.hover-black:hover { color: #1e293b; }

/* 右侧栏 */
.right-col { display: flex; flex-direction: column; }
.word-cloud-box { border-radius: 8px; position: relative; overflow: hidden; }
.wc-item { position: absolute; font-weight: bold; transition: all 1s ease; }
.text-green-500 { color: #10b981; } .text-blue-500 { color: #3b82f6; } .text-red-500 { color: #ef4444; } .text-yellow-500 { color: #f59e0b; } .text-purple-500 { color: #8b5cf6; }
.text-4xl { font-size: 36px; } .text-3xl { font-size: 30px; } .text-2xl { font-size: 24px; } .text-xl { font-size: 20px; } .text-lg { font-size: 18px; } .text-base { font-size: 16px; } .text-sm { font-size: 14px; }

.btn-review { background: #10b981; color: white; font-size: 13px; font-weight: bold; padding: 6px 16px; border-radius: 6px; border: none; cursor: pointer; transition: background 0.2s; } .btn-review:hover:not(:disabled) { background: #059669; } .btn-review:disabled { background: #cbd5e1; cursor: not-allowed; }
.btn-jump { background: #06b6d4; color: white; font-size: 13px; font-weight: bold; padding: 6px 16px; border-radius: 6px; border: none; display: flex; align-items: center; cursor: pointer; transition: background 0.2s; } .btn-jump:hover:not(:disabled) { background: #0891b2; } .btn-jump:disabled { background: #cbd5e1; cursor: not-allowed; }

.empty-box { display: flex; align-items: center; justify-content: center; border-radius: 12px; font-size: 14px; border-style: dashed !important; font-weight: bold; }
.empty-msg { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: bold; }
.grid-2x2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

.review-card { border-radius: 12px; position: relative; overflow: hidden; transition: border-color 0.5s; display: flex; flex-direction: column; }
.loading-mask { position: absolute; inset: 0; z-index: 30; display: flex; flex-direction: column; align-items: center; justify-content: center; backdrop-filter: blur(4px); }
.progress-bg { width: 66%; height: 6px; background: #e2e8f0; border-radius: 3px; overflow: hidden; }
.progress-bar { height: 100%; transition: width 1.2s linear; }

/* 评论内容区也要加 min-height: 0 保证不撑破卡片 */
.review-content { padding: 16px; display: flex; flex-direction: column; flex: 1; min-height: 0; overflow-y: auto; }
.review-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; padding-bottom: 12px; }
.icon-bg { padding: 6px; border-radius: 6px; }
.tag-success { background: #dcfce7; color: #10b981; border-color: #bbf7d0; font-size: 11px; padding: 2px 8px; border-radius: 4px; font-weight: bold; }
.ai-box { padding: 12px; border-radius: 8px; margin-top: 12px; }

.list-move, .list-enter-active, .list-leave-active { transition: all 0.5s ease; }
.list-enter-from { opacity: 0; transform: translateY(-30px); }
.list-leave-to { opacity: 0; transform: translateY(30px) scale(0.95); }
.fade-enter-active, .fade-leave-active { transition: opacity 1s ease, transform 1s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: scale(0.8); }
.truncate-2-lines { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.scan-line { width: 100%; height: 2px; position: absolute; animation: scan 2.5s linear infinite; opacity: 0.4; }
.fade-overlay-leave-active { transition: opacity 0.6s ease-out; } .fade-overlay-leave-to { opacity: 0; }
.fade-content-enter-active { transition: opacity 0.8s ease-in; } .fade-content-enter-from { opacity: 0; }
.fade-in { animation: fadeIn 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards; opacity: 0; }

.spinner { animation: spin 1s linear infinite; border: 2px solid; border-top-color: transparent; border-radius: 50%; }
.animate-pulse { animation: pulse 2s infinite; }
@keyframes scan { 0% { top: 0; } 100% { top: 100%; } }
@keyframes fadeIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
@keyframes spin { 100% { transform: rotate(360deg); } }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }
.custom-scrollbar::-webkit-scrollbar { width: 4px; } .custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 2px; }
</style>