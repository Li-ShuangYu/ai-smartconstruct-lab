<template>
  <div style="height: 100vh;" class="p-6 bg-slate-50">
    <div class="glass-card w-full h-full p-6 flex flex-col z-10 relative overflow-hidden font-sans">
      
      <div class="flex justify-between items-end mb-6 pb-4 border-b border-gray-200/50 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase flex items-center gap-2">
            <svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
            Node: MINDMAP_DRAW_MONITOR
          </div>
          <h1 class="text-3xl font-bold text-gray-800 flex items-center gap-3">
             全班思维导图监控墙
          </h1>
        </div>
        
        <div class="flex items-center gap-5">
          <div class="flex items-center gap-4 bg-white/60 px-5 py-2.5 rounded-xl border border-gray-100 shadow-sm">
             <div class="flex flex-col items-center">
               <span class="text-xs font-bold text-gray-500">已完成并提交</span>
               <span class="text-xl font-black text-green-600">{{ submittedCount }} <span class="text-sm text-gray-400 font-normal">/ {{ studentList.length }}</span></span>
             </div>
             <div class="w-px h-8 bg-gray-200"></div>
             <div class="flex flex-col items-center">
               <span class="text-xs font-bold text-gray-500">全班平均节点数</span>
               <span class="text-xl font-black text-indigo-600">{{ avgNodes }}</span>
             </div>
          </div>
          
          <button 
            @click="exportAllMaps"
            class="hero-send-btn px-6 py-3 rounded-xl shadow-lg transition-all flex items-center gap-2 text-sm font-bold active:scale-95"
          >
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
            一键导出全班作品 (.zip)
          </button>
        </div>
      </div>

      <div class="mb-4 flex items-center shrink-0">
        <div class="relative w-72">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索学生姓名..."
            class="w-full pl-9 pr-4 py-2.5 bg-white/60 border border-gray-200 rounded-xl text-sm text-gray-700 focus:outline-none focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100 transition-colors shadow-sm"
          >
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
      </div>

      <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 pb-4">
        <template v-if="paginatedStudents.length > 0">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            <div v-for="student in paginatedStudents" :key="student.id" 
                 class="bg-white border rounded-2xl p-0 flex flex-col shadow-sm transition-all duration-300 hover:shadow-lg relative overflow-hidden group cursor-pointer"
                 :class="student.isSubmitted ? 'border-green-300' : 'border-indigo-200 hover:border-indigo-400'"
                 @click="openSpectator(student)">
              
              <div class="px-4 py-3 border-b flex justify-between items-center z-10 bg-white/90 backdrop-blur-sm"
                   :class="student.isSubmitted ? 'border-green-100 bg-green-50/60' : 'border-indigo-100 bg-indigo-50/40'">
                <div class="flex items-center gap-2">
                  <div class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold text-white shadow-sm"
                       :class="student.isSubmitted ? 'bg-green-500' : 'bg-indigo-500'">
                    {{ student.name.charAt(0) }}
                  </div>
                  <div>
                    <span class="font-bold text-sm text-gray-800 block leading-tight">{{ student.name }}</span>
                    <span class="text-[10px] font-bold" :class="student.isSubmitted ? 'text-green-600' : 'text-indigo-500'">
                      {{ student.isSubmitted ? '已完成并提交' : '正在绘制中...' }}
                    </span>
                  </div>
                </div>
                
                <div class="flex flex-col items-end">
                   <span class="text-xs font-mono font-bold" :class="student.isSubmitted ? 'text-green-600' : 'text-indigo-600'">
                     {{ student.nodeCount }} 节点
                   </span>
                   <span v-if="!student.isSubmitted" class="flex h-2 w-2 mt-1 relative">
                     <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-indigo-400 opacity-75"></span>
                     <span class="relative inline-flex rounded-full h-2 w-2 bg-indigo-500"></span>
                   </span>
                </div>
              </div>

              <div class="h-40 bg-slate-50 relative dot-pattern overflow-hidden opacity-90 group-hover:opacity-100 transition-opacity">
                <div class="absolute inset-0 bg-indigo-900/10 opacity-0 group-hover:opacity-100 transition-opacity z-20 flex items-center justify-center backdrop-blur-[2px]">
                   <span class="bg-indigo-600 text-white px-4 py-2 rounded-xl text-sm font-bold shadow-lg flex items-center gap-2 transform translate-y-2 group-hover:translate-y-0 transition-transform">
                     <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM10 7v3m0 0v3m0-3h3m-3 0H7" /></svg>
                     放大查看真实画布
                   </span>
                </div>
                
                <div class="absolute inset-0 flex items-center justify-center transform scale-[0.6]">
                   <div class="w-24 h-12 bg-indigo-500 rounded-lg absolute z-10 shadow-sm" style="top: 50%; left: 50%; transform: translate(-50%, -50%);"></div>
                   
                   <div class="w-12 h-16 border-l-2 border-b-2 border-gray-400 absolute rounded-bl-xl" style="top: 25%; left: 50%;"></div>
                   <div class="w-20 h-10 bg-white border-2 border-gray-400 rounded absolute z-10" style="top: 25%; left: 30%; transform: translate(-50%, -50%);"></div>
                   
                   <div class="w-20 h-0.5 bg-gray-400 absolute" style="top: 50%; left: 25%;"></div>
                   <div class="w-20 h-10 bg-white border-2 border-gray-400 rounded absolute z-10" style="top: 50%; left: 20%; transform: translate(-50%, -50%);"></div>
                   
                   <div v-if="student.nodeCount > 4" class="w-20 h-0.5 bg-gray-400 absolute" style="top: 50%; left: 60%;"></div>
                   <div v-if="student.nodeCount > 4" class="w-20 h-10 bg-indigo-100 border-2 border-indigo-300 rounded absolute z-10" style="top: 50%; left: 85%; transform: translate(-50%, -50%);"></div>
                </div>
              </div>
              
            </div>
          </div>
        </template>
        <div v-else class="h-full flex flex-col items-center justify-center text-gray-400 space-y-3 pb-10">
           <svg style="width: 48px; height: 48px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
           <span class="text-sm font-medium">无匹配学生画布</span>
        </div>
      </div>

      <div class="mt-4 pt-4 border-t border-gray-200/60 flex items-center justify-between shrink-0">
        <button @click="prevPage" :disabled="currentPage === 1" class="px-5 py-2 rounded-xl text-sm font-bold transition-colors" :class="currentPage === 1 ? 'text-gray-400 cursor-not-allowed bg-transparent' : 'text-indigo-600 bg-indigo-50 hover:bg-indigo-100'">
          上一页
        </button>
        <div class="text-sm font-bold text-gray-500">
          第 <span class="text-indigo-600 mx-1">{{ currentPage }}</span> 页 / 共 <span class="mx-1">{{ totalPages || 1 }}</span> 页
        </div>
        <button @click="nextPage" :disabled="currentPage >= totalPages || totalPages === 0" class="px-5 py-2 rounded-xl text-sm font-bold transition-colors" :class="currentPage >= totalPages || totalPages === 0 ? 'text-gray-400 cursor-not-allowed bg-transparent' : 'text-indigo-600 bg-indigo-50 hover:bg-indigo-100'">
          下一页
        </button>
      </div>

      <div v-if="spectatingStudent" class="absolute inset-0 z-50 bg-gray-900/40 backdrop-blur-sm rounded-[1.5rem] flex items-center justify-center animate-fade-in p-6">
        <div class="bg-white rounded-2xl w-full h-full shadow-2xl flex flex-col overflow-hidden border border-gray-200">
          
          <div class="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 shrink-0 z-10 shadow-sm">
            <div class="flex items-center gap-4">
              <div v-if="!spectatingStudent.isSubmitted" class="flex items-center gap-2 bg-indigo-50 px-3 py-1.5 rounded-lg border border-indigo-100">
                <span class="w-2.5 h-2.5 rounded-full bg-indigo-500 animate-pulse"></span>
                <span class="text-xs font-bold text-indigo-700 tracking-wider">LIVE SPECTATING</span>
              </div>
              <div v-else class="flex items-center gap-2 bg-green-50 px-3 py-1.5 rounded-lg border border-green-100">
                <svg class="w-3 h-3 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
                <span class="text-xs font-bold text-green-700 tracking-wider">COMPLETED</span>
              </div>
              
              <div class="w-px h-6 bg-gray-200"></div>
              
              <div class="flex items-center gap-2">
                <div class="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 text-white flex items-center justify-center text-sm font-bold shadow-sm">
                  {{ spectatingStudent.name.charAt(0) }}
                </div>
                <div>
                  <span class="font-bold text-gray-800">{{ spectatingStudent.name }} 的思维导图</span>
                  <span class="text-[10px] text-gray-500 ml-2">节点数: {{ spectatingStudent.nodeCount }}</span>
                </div>
              </div>
            </div>
            
            <div class="flex items-center gap-3">
              <button @click="handleFitView" class="text-xs font-bold text-gray-500 hover:text-indigo-600 px-3 py-1.5 border border-gray-200 hover:bg-indigo-50 rounded-lg transition-colors flex items-center gap-1">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4h16v16H4V4z" /></svg>
                重置视角
              </button>
              <button @click="closeSpectator" class="text-gray-400 hover:text-red-500 hover:bg-red-50 p-2 rounded-lg transition-colors border border-transparent hover:border-red-100">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
              </button>
            </div>
          </div>
          
          <div class="flex-1 bg-slate-50/50 relative overflow-hidden group">
            <div class="absolute inset-0 opacity-[0.03] pointer-events-none" style="background-image: radial-gradient(#6366f1 1px, transparent 1px); background-size: 30px 30px;"></div>
            <div ref="spectatorMapContainer" class="w-full h-full outline-none"></div>
            
            <div class="absolute bottom-6 left-6 flex gap-2 z-10">
              <button @click="handleZoomIn" class="p-2.5 bg-white rounded-xl border border-gray-200 shadow-sm hover:bg-indigo-50 text-gray-600 transition-colors">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
              </button>
              <button @click="handleZoomOut" class="p-2.5 bg-white rounded-xl border border-gray-200 shadow-sm hover:bg-indigo-50 text-gray-600 transition-colors">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" /></svg>
              </button>
            </div>
          </div>
          
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import MindMap from 'simple-mind-map'

// ================= 学生数据与状态 =================
// 增加 mapData 字段用于渲染真实的思维导图
const studentList = ref([
  { 
    id: 1, name: '陈静', nodeCount: 8, isSubmitted: true,
    mapData: {
      data: { text: 'Python 数组实训', uid: 'root' },
      children: [
        { data: { text: '连续内存分配', uid: 'c1' }, children: [{ data: { text: '寻址公式', uid: 'c1-1' }, children: [] }] },
        { data: { text: '动态扩容', uid: 'c2' }, children: [{ data: { text: 'Over-allocation', uid: 'c2-1' }, children: [] }] },
      ]
    }
  },
  { 
    id: 2, name: '林峰', nodeCount: 5, isSubmitted: false,
    mapData: {
      data: { text: 'Python 数组', uid: 'root' },
      children: [
        { data: { text: 'List 的特性', uid: 'c1' }, children: [] },
        { data: { text: '切片语法', uid: 'c2' }, children: [] }
      ]
    }
  },
  { id: 3, name: '张伟', nodeCount: 12, isSubmitted: true, mapData: { data: { text: '数组满分总结', uid: 'root' }, children: [] } },
  { id: 4, name: '王磊', nodeCount: 3, isSubmitted: false, mapData: { data: { text: '正在构思中', uid: 'root' }, children: [] } }, 
  { id: 5, name: '李娜', nodeCount: 7, isSubmitted: true, mapData: { data: { text: '数组结构', uid: 'root' }, children: [] } },
  { id: 6, name: '赵云', nodeCount: 6, isSubmitted: false, mapData: { data: { text: '数组操作', uid: 'root' }, children: [] } },
  { id: 7, name: '周杰', nodeCount: 4, isSubmitted: false, mapData: { data: { text: '学习笔记', uid: 'root' }, children: [] } },
  { id: 8, name: '吴敏', nodeCount: 9, isSubmitted: true, mapData: { data: { text: 'Python List', uid: 'root' }, children: [] } },
])

const submittedCount = computed(() => studentList.value.filter(s => s.isSubmitted).length)
const avgNodes = computed(() => {
  const sum = studentList.value.reduce((acc, curr) => acc + curr.nodeCount, 0)
  return Math.round(sum / studentList.value.length)
})

// ================= 搜索与分页 =================
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(8)

const filteredStudents = computed(() => {
  if (!searchQuery.value.trim()) return studentList.value
  const lowerSearch = searchQuery.value.trim().toLowerCase()
  return studentList.value.filter(s => s.name.toLowerCase().includes(lowerSearch))
})

const totalPages = computed(() => Math.ceil(filteredStudents.value.length / pageSize.value))

const paginatedStudents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStudents.value.slice(start, end)
})

watch(searchQuery, () => { currentPage.value = 1 })
const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value < totalPages.value) currentPage.value++ }

// ================= 一键导出 =================
const exportAllMaps = () => {
  alert('已将全班导图打包生成 .zip 文件，开始下载！')
}

// ================= 全屏实时监控与 MindMap =================
const spectatingStudent = ref(null)
const spectatorMapContainer = ref(null)
let spectatorMapInstance = null
let simulationTimer = null

const openSpectator = async (student) => {
  spectatingStudent.value = student
  await nextTick()
  
  // 实例化真实的 simple-mind-map
  spectatorMapInstance = new MindMap({
    el: spectatorMapContainer.value,
    direction: 'right',
    theme: 'fresh-purple',
    data: student.mapData,
    fit: true,
    mouseScaleBehavior: 'zoom',
    readonly: true, // 监控模式只能看不能改
    themeConfig: {
      root: { shape: 'rectangle', fillColor: '#6366f1', color: '#ffffff', borderColor: '#4f46e5', borderWidth: 2 },
      second: { shape: 'rectangle', fillColor: '#ffffff', borderColor: '#549688', color: '#334155', borderWidth: 2 },
      node: { shape: 'rectangle', fillColor: '#ffffff', borderColor: '#549688', color: '#334155', borderWidth: 1 }
    }
  })

  // 如果学生未提交，模拟其实时绘制（每3秒加一个节点）
  if (!student.isSubmitted) {
    let mockNodeCounter = 1
    simulationTimer = setInterval(() => {
      if (spectatorMapInstance) {
        // 模拟给根节点添加一个子节点
        const root = spectatorMapInstance.renderer.rootNode
        if (root) {
          spectatorMapInstance.execCommand('INSERT_CHILD_NODE', false, root, {
            data: { text: `正在书写内容... ${mockNodeCounter}` }
          })
          mockNodeCounter++
          spectatingStudent.value.nodeCount++
        }
      }
    }, 3000)
  }
}

const closeSpectator = () => {
  if (simulationTimer) {
    clearInterval(simulationTimer)
    simulationTimer = null
  }
  if (spectatorMapInstance) {
    spectatorMapInstance.destroy()
    spectatorMapInstance = null
  }
  spectatingStudent.value = null
}

const handleZoomIn = () => spectatorMapInstance?.view.enlarge()
const handleZoomOut = () => spectatorMapInstance?.view.narrow()
const handleFitView = () => spectatorMapInstance?.view.fit()
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

.hero-send-btn:hover {
  transform: translateY(-2px);
}

.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.2); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.4); }

.dot-pattern {
  background-image: radial-gradient(#cbd5e1 1.5px, transparent 1.5px);
  background-size: 24px 24px;
}

.animate-fade-in { animation: fadeIn 0.3s ease-out forwards; }
@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.98); }
  to { opacity: 1; transform: scale(1); }
}

/* 覆盖 simple-mind-map 样式，隐藏多余控件 */
:deep(.smm-canvas) { outline: none !important; }
:deep(.smm-expand-btn) { display: none !important; opacity: 0 !important; pointer-events: none !important; }
</style>