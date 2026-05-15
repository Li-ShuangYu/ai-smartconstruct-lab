<template>
  <div style="height: 100vh;" class="p-6 bg-slate-50">
    
    <div class="glass-card w-full h-full p-6 flex gap-6 z-10 overflow-hidden">
      
      <aside class="w-[320px] shrink-0 flex flex-col border-r border-gray-200/60 pr-6 h-full">
        
        <div class="mb-4 shrink-0">
          <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: COURSE_READ</div>
          <h2 class="text-2xl font-bold text-gray-800">课件阅读与学习</h2>
          <p class="text-sm text-gray-500 mt-2">请阅读以下全部教学资源，每份资料阅读覆盖率需达到阈值。</p>
        </div>

        <div class="flex-1 overflow-y-auto pr-2 space-y-3 custom-scrollbar mb-6">
          <div 
            v-for="file in resourceFiles" 
            :key="file.id"
            @click="activeFileId = file.id"
            class="p-4 rounded-xl border transition-all cursor-pointer relative overflow-hidden group"
            :class="[
              activeFileId === file.id 
                ? 'bg-indigo-50/80 border-indigo-300 shadow-sm' 
                : 'bg-white/50 border-gray-100 hover:border-indigo-200 hover:bg-gray-50/80'
            ]"
          >
            <div 
              class="absolute bottom-0 left-0 h-1 bg-indigo-500 transition-all duration-500 ease-out"
              :class="{'opacity-100': activeFileId === file.id, 'opacity-40': activeFileId !== file.id, 'bg-green-500': file.progress >= file.threshold}"
              :style="{ width: `${file.progress}%` }"
            ></div>

            <div class="flex items-start gap-3">
              <div class="mt-0.5" v-html="getFileIcon(file.type)"></div>
              
              <div class="flex-1 min-w-0">
                <h4 class="text-sm font-semibold text-gray-800 truncate" :title="file.name">
                  {{ file.name }}
                </h4>
                <div class="flex items-center justify-between mt-2">
                  <span class="text-xs text-gray-500 flex items-center gap-1">
                    <svg v-if="file.progress >= file.threshold" class="w-3 h-3 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                    进度: {{ file.progress }}% / {{ file.threshold }}%
                  </span>
                  <span class="text-[10px] px-1.5 py-0.5 rounded bg-white border border-gray-200 text-gray-400 font-mono uppercase">
                    {{ file.type }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="shrink-0 space-y-4 pt-4 border-t border-gray-100">
          <div v-if="isAllConditionsMet" class="flex items-center gap-2 text-sm text-green-600 bg-green-50 p-3 rounded-lg border border-green-100">
             <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
             <span>所有课件均已达标，可进入下一环节</span>
          </div>
          <div v-else class="flex items-center gap-2 text-sm text-amber-600 bg-amber-50 p-3 rounded-lg border border-amber-100">
             <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
             <span>需全部阅读覆盖率达标 ({{ completedCount }}/{{ resourceFiles.length }})</span>
          </div>

          <button 
            @click="handleComplete"
            class="hero-send-btn w-full justify-center text-base py-3.5 rounded-xl shadow-lg transition-all flex items-center gap-2"
            :class="[
              isCompleted ? 'hover:shadow-indigo-500/30' : {'opacity-50 grayscale cursor-not-allowed': !isAllConditionsMet, 'hover:shadow-indigo-500/30': isAllConditionsMet}
            ]"
            :disabled="!isAllConditionsMet && !isCompleted"
          >
            <!-- {{ isCompleted ? '等待教师进入下一节点' }} -->
            {{ isCompleted ? '进入下一节点' : '完成学习' }}
            <svg style="width: 16px; height: 16px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
          </button>
        </div>

      </aside>

      <main class="flex-1 flex flex-col h-full min-w-0">
        <div class="flex-1 flex flex-col border border-gray-200/60 bg-white/40 rounded-xl overflow-hidden shadow-inner relative min-h-0">
          
          <div class="h-14 border-b border-gray-200/60 bg-white/50 backdrop-blur-md flex items-center justify-between px-6 shrink-0">
            <span class="font-medium text-gray-700 text-base flex items-center gap-2">
              <div v-html="getFileIcon(activeFile.type, '20px')"></div>
              当前预览: {{ activeFile.name }}
            </span>
            
            <div class="flex items-center gap-3">
              <button 
                v-if="activeFile.type === 'ppt'"
                class="px-3 py-1.5 hover:bg-indigo-50 text-indigo-600 rounded-lg transition-colors flex items-center gap-1.5 text-sm font-bold border border-indigo-100"
              >
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" /></svg>
                放映
              </button>
              
              <div v-if="activeFile.type === 'ppt'" class="h-4 w-[1px] bg-gray-300 mx-1"></div>

              <button class="p-2 hover:bg-gray-100 text-gray-500 rounded-lg transition-colors" title="全屏阅读">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4" /></svg>
              </button>
              <button class="p-2 hover:bg-gray-100 text-gray-500 rounded-lg transition-colors" title="下载资源">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
              </button>
            </div>
          </div>

          <div @click="simulateReadProgress" class="flex-1 flex flex-col items-center justify-center bg-gray-50/50 overflow-y-auto p-8 group relative cursor-pointer" title="点击此处模拟滚动阅读增加进度">
            <div class="w-24 h-24 mb-6 opacity-40 transition-transform group-hover:scale-105" v-html="getFileIcon(activeFile.type, '96px')"></div>
            <p class="text-gray-500 font-bold text-lg tracking-wider">正在渲染 {{ activeFile.type.toUpperCase() }} 资源内容</p>
            <p class="text-sm text-gray-400 mt-2">(点击此区域模拟滚动，增加阅读进度 +20%)</p>
          </div>
        </div>
      </main>

      <aside class="w-[280px] xl:w-[320px] shrink-0 flex flex-col border-l border-gray-200/60 pl-6 h-full">
        
        <div class="flex items-center gap-2 mb-4 shrink-0">
          <svg class="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>
          <h3 class="font-bold text-indigo-900 text-base">AI 课件洞察</h3>
        </div>

        <div class="flex-1 overflow-y-auto custom-scrollbar pr-1 space-y-4">
          
          <div class="bg-gradient-to-br from-indigo-50/80 to-purple-50/50 border border-indigo-100/60 rounded-xl overflow-hidden shadow-sm transition-all duration-300">
            <div @click="aiPanels.summary = !aiPanels.summary" class="px-4 py-3 flex justify-between items-center cursor-pointer hover:bg-white/40">
              <div class="text-sm font-bold text-gray-700 flex items-center gap-2">
                <svg class="w-4 h-4 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h7" /></svg> 核心摘要
              </div>
              <svg class="w-4 h-4 text-gray-400 transition-transform" :class="{'rotate-180': aiPanels.summary}" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
            </div>
            <div v-show="aiPanels.summary" class="px-4 pb-4 pt-1">
              <div class="bg-white/60 p-3 rounded-lg border border-white/40">
                <p class="text-sm text-gray-700 leading-relaxed">{{ activeFile.ai.summary }}</p>
              </div>
            </div>
          </div>

          <div class="bg-gradient-to-br from-indigo-50/80 to-purple-50/50 border border-indigo-100/60 rounded-xl overflow-hidden shadow-sm transition-all duration-300">
            <div @click="aiPanels.highlights = !aiPanels.highlights" class="px-4 py-3 flex justify-between items-center cursor-pointer hover:bg-white/40">
              <div class="text-sm font-bold text-gray-700 flex items-center gap-2">
                <svg class="w-4 h-4 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" /></svg> 提炼重点
              </div>
              <svg class="w-4 h-4 text-gray-400 transition-transform" :class="{'rotate-180': aiPanels.highlights}" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
            </div>
            <div v-show="aiPanels.highlights" class="px-4 pb-4 pt-1">
              <div class="bg-white/60 p-3 rounded-lg border border-white/40">
                <ul class="text-sm text-gray-700 space-y-2">
                  <li v-for="(point, idx) in activeFile.ai.highlights" :key="idx" class="flex items-start gap-2">
                    <span class="text-purple-400 font-bold mt-0.5">•</span>
                    <span class="flex-1 leading-tight">{{ point }}</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <div class="bg-gradient-to-br from-indigo-50/80 to-purple-50/50 border border-indigo-100/60 rounded-xl overflow-hidden shadow-sm transition-all duration-300">
            <div @click="aiPanels.toc = !aiPanels.toc" class="px-4 py-3 flex justify-between items-center cursor-pointer hover:bg-white/40">
              <div class="text-sm font-bold text-gray-700 flex items-center gap-2">
                <svg class="w-4 h-4 text-emerald-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" /></svg> 快捷导航
              </div>
              <svg class="w-4 h-4 text-gray-400 transition-transform" :class="{'rotate-180': aiPanels.toc}" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
            </div>
            <div v-show="aiPanels.toc" class="px-4 pb-4 pt-1">
              <div class="bg-white/60 p-3 rounded-lg border border-white/40 flex flex-wrap gap-2">
                <span v-for="(nav, idx) in activeFile.ai.toc" :key="idx" class="text-xs px-2 py-1.5 bg-white border border-indigo-100 text-indigo-700 rounded shadow-sm cursor-pointer hover:bg-indigo-50 transition-colors w-full truncate">
                  {{ nav }}
                </span>
              </div>
            </div>
          </div>

        </div>
      </aside>
      
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 模拟多种类型的文件资源列表数据
const resourceFiles = ref([
  {
    id: 1,
    name: '1_Python基础语法速通.pdf',
    type: 'pdf',
    progress: 100,
    threshold: 80,
    ai: {
      summary: '本指南浓缩了Python核心基础语法，涵盖变量声明、数据类型以及基本运算符，是快速入门的必备手册。',
      highlights: ['动态类型与变量命名规范', '列表、字典等核心数据结构', '缩进规则与代码块概念'],
      toc: ['1. 快速入门', '2. 数据类型', '3. 运算符', '4. 控制流']
    }
  },
  {
    id: 2,
    name: '2_数组与内存管理机制.pptx',
    type: 'ppt',
    progress: 40,
    threshold: 80,
    ai: {
      summary: '深度图解数组在计算机内存中的连续分配机制，对比了不同语言下数组处理的时间空间复杂度差异。',
      highlights: ['连续内存分配原理', '数组扩容策略探究', '多维数组内存映射机制'],
      toc: ['什么是数组', '内存视图', '扩容算法', '性能对比']
    }
  },
  {
    id: 3,
    name: '3_实训操作标准规范.docx',
    type: 'word',
    progress: 0,
    threshold: 90,
    ai: {
      summary: '详细说明了本次实训的提交流程、代码命名规范以及互评打分的扣分细则。',
      highlights: ['PEP8 编码规范要求', '作业提交 Git 流程', '互评阶段打分维度'],
      toc: ['环境要求', '代码规范', '提交指南', '评分细则']
    }
  },
  {
    id: 4,
    name: '4_环境配置指令集.txt',
    type: 'txt',
    progress: 0,
    threshold: 100,
    ai: {
      summary: '包含了构建本地实训环境所需的全部 pip 安装指令与 Docker 配置脚本。',
      highlights: ['requirements.txt 列表', 'Docker 镜像拉取指令', '常见报错及解决命令'],
      toc: ['Pip依赖', 'Docker配置', '测试验证']
    }
  }
])

const activeFileId = ref(1)

// 是否已完成学习
const isCompleted = ref(false)

// AI 侧边栏折叠状态控制
const aiPanels = reactive({
  summary: true,
  highlights: true,
  toc: true
})

// 计算当前选中的文件
const activeFile = computed(() => {
  return resourceFiles.value.find(f => f.id === activeFileId.value) || resourceFiles.value[0]
})

// 计算已达标的文件数量
const completedCount = computed(() => {
  return resourceFiles.value.filter(f => f.progress >= f.threshold).length
})

// 判断是否所有文件都达到各自的阅读阈值
const isAllConditionsMet = computed(() => {
  return resourceFiles.value.every(f => f.progress >= f.threshold)
})

// 辅助方法：模拟阅读进度的增加 (点击中间预览区触发)
const simulateReadProgress = () => {
  const file = resourceFiles.value.find(f => f.id === activeFileId.value)
  if (file && file.progress < 100) {
    file.progress = Math.min(file.progress + 20, 100)
  }
}

// 完成按钮逻辑
const handleComplete = () => {
  if (!isCompleted.value) {
    // 第一次点击：标记完成学习
    isCompleted.value = true
  } else {
    // 第二次点击：进入下一节点
    router.push('/student/training/video')
  }
}

// 辅助方法：根据文件类型返回不同的 SVG 图标
const getFileIcon = (type, size = '24px') => {
  const baseStyle = `style="width: ${size}; height: ${size}; flex-shrink: 0;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"`
  
  switch(type.toLowerCase()) {
    case 'pdf':
      return `<svg class="text-red-500" ${baseStyle}><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><path d="M9 15h6"></path><path d="M9 11h6"></path></svg>`
    case 'ppt':
      return `<svg class="text-orange-500" ${baseStyle}><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="3" y1="9" x2="21" y2="9"></line><line x1="9" y1="21" x2="9" y2="9"></line></svg>`
    case 'word':
      return `<svg class="text-blue-500" ${baseStyle}><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><path d="M9 15l2 2 4-4"></path></svg>`
    case 'txt':
      return `<svg class="text-gray-500" ${baseStyle}><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="8" y1="13" x2="16" y2="13"></line><line x1="8" y1="17" x2="16" y2="17"></line></svg>`
    default:
      return `<svg class="text-indigo-400" ${baseStyle}><path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path><polyline points="13 2 13 9 20 9"></polyline></svg>`
  }
}
</script>

<style scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 1.5rem;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.07);
}

.hero-send-btn {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: white;
}

.hero-send-btn:not(:disabled):hover {
  transform: translateY(-2px);
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}
</style>