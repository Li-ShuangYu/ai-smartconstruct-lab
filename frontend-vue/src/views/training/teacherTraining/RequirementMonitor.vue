<template>
  <div style="height: 100%;">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10">
      
      <div class="flex justify-between items-end mb-6 pb-4 border-b border-gray-200/50">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: REQ_UPLOAD Monitor</div>
          <h1 class="text-3xl font-bold text-gray-800 flex items-center gap-3">
             <svg class="text-indigo-500" style="width: 28px; height: 28px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg>
             需求汇聚与全局图谱
          </h1>
        </div>
        
        <div class="flex items-center gap-4 bg-white/60 px-5 py-2.5 rounded-xl border border-gray-100 shadow-sm">
           <div class="flex flex-col items-center">
             <span class="text-xs text-gray-500">提交率</span>
             <span class="text-xl font-bold text-indigo-600">85%</span>
           </div>
           <div class="w-px h-8 bg-gray-200"></div>
           <div class="flex flex-col items-center">
             <span class="text-xs text-gray-500">共提取关键词</span>
             <span class="text-xl font-bold text-purple-600">142</span>
           </div>
        </div>
      </div>

      <div class="flex-1 flex flex-col lg:flex-row gap-6 min-h-0">
        
        <div class="flex-[1.5] bg-white/50 border border-gray-200/60 rounded-2xl flex flex-col overflow-hidden shadow-sm">
          <div class="bg-gray-50/80 px-4 py-3 border-b border-gray-100 flex items-center justify-between">
            <span class="font-bold text-gray-700 text-sm">全局高频词云池</span>
            <span class="text-xs text-gray-400">悬浮可剔除无意义干扰词</span>
          </div>
          
          <div class="flex-1 flex flex-wrap content-center justify-center gap-x-8 gap-y-6 p-8 relative">
             <transition-group name="fade-word">
               <div v-for="word in wordCloudData" :key="word.id" 
                    v-show="!word.isFiltered"
                    class="relative group transition-transform hover:scale-110 cursor-pointer"
                    :style="{ fontSize: word.size + 'px', fontWeight: word.weight }">
                 <span :class="word.colorClass">{{ word.text }}</span>
                 <button @click="filterWord(word.id)" class="absolute -top-3 -right-4 w-5 h-5 bg-red-100 text-red-500 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-500 hover:text-white shadow-sm">
                   <svg style="width: 12px; height: 12px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
                 </button>
               </div>
             </transition-group>
          </div>
        </div>

        <div class="flex-[1] flex flex-col bg-white/40 border border-gray-100 rounded-2xl shadow-sm overflow-hidden">
          <div class="bg-indigo-50/50 px-4 py-3 border-b border-indigo-100/50 flex items-center justify-between">
            <span class="font-bold text-indigo-900 text-sm flex items-center gap-2">
               <svg style="width: 16px; height: 16px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16" /></svg>
               学生原始需求瀑布流
            </span>
          </div>

          <div class="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar bg-gray-50/30">
            <transition-group name="list">
              <div v-for="req in sortedRequirements" :key="req.id" 
                   class="bg-white border p-4 rounded-xl shadow-sm relative transition-all duration-300 hover:shadow-md"
                   :class="req.isPinned ? 'border-indigo-300 bg-indigo-50/30' : 'border-gray-100'">
                
                <div class="flex justify-between items-start mb-2">
                  <div class="flex items-center gap-2">
                    <div class="w-6 h-6 rounded-full bg-gradient-to-br from-indigo-100 to-purple-100 text-indigo-700 flex items-center justify-center text-xs font-bold">
                      {{ req.studentName.charAt(0) }}
                    </div>
                    <span class="text-sm font-bold text-gray-700">{{ req.studentName }}</span>
                    <span class="text-[10px] text-gray-400">{{ req.time }}</span>
                  </div>
                  
                  <button @click="togglePin(req)" 
                          class="p-1.5 rounded-md transition-colors"
                          :class="req.isPinned ? 'text-indigo-600 bg-indigo-100' : 'text-gray-300 hover:text-indigo-400 hover:bg-gray-100'"
                          title="设为优质需求并置顶">
                    <svg style="width: 16px; height: 16px; flex-shrink: 0;" fill="currentColor" viewBox="0 0 20 20"><path d="M5 4a2 2 0 012-2h6a2 2 0 012 2v14l-5-2.5L5 18V4z" /></svg>
                  </button>
                </div>
                
                <p class="text-sm text-gray-600 leading-relaxed">{{ req.content }}</p>
                
                <div v-if="req.isPinned" class="absolute top-2 right-10 text-[10px] font-bold text-indigo-500 border border-indigo-200 bg-white px-1.5 py-0.5 rounded shadow-sm">
                  已置顶 (优质)
                </div>
              </div>
            </transition-group>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 模拟词云数据 (带 id 用于过滤)
const wordCloudData = ref([
  { id: 1, text: 'SM4加密', size: 52, weight: 900, colorClass: 'text-indigo-600', isFiltered: false },
  { id: 2, text: '无用测试词', size: 36, weight: 600, colorClass: 'text-gray-400', isFiltered: false }, // 干扰词
  { id: 3, text: '密钥协商', size: 40, weight: 800, colorClass: 'text-blue-500', isFiltered: false },
  { id: 4, text: '哈哈哈哈', size: 24, weight: 500, colorClass: 'text-gray-300', isFiltered: false }, // 干扰词
  { id: 5, text: '国密改造', size: 34, weight: 700, colorClass: 'text-indigo-400', isFiltered: false },
  { id: 6, text: '性能瓶颈', size: 30, weight: 700, colorClass: 'text-purple-500', isFiltered: false },
  { id: 7, text: '合规性', size: 38, weight: 800, colorClass: 'text-pink-500', isFiltered: false },
])

const filterWord = (id) => {
  const word = wordCloudData.value.find(w => w.id === id)
  if (word) word.isFiltered = true
}

// 模拟瀑布流需求数据
const requirements = ref([
  { id: 101, studentName: '林同学', time: '14:20:05', content: '在高并发场景下，SM4 的加解密速度可能成为系统瓶颈，需要考虑加入硬件加速卡的支持。', isPinned: false },
  { id: 102, studentName: '张同学', time: '14:21:12', content: '我觉得主要问题是不知道怎么做。', isPinned: false }, // 划水回答
  { id: 103, studentName: '陈同学', time: '14:22:30', content: '除了数据加密，密钥的生命周期管理也是一大痛点，我们需要确保会话密钥的定时更新机制。', isPinned: true }, // 已置顶的优质回答
  { id: 104, studentName: '王同学', time: '14:25:01', content: '必须符合国密局的相关规范，不然过不了安评。', isPinned: false },
])

// 将置顶的需求排在最前面
const sortedRequirements = computed(() => {
  return [...requirements.value].sort((a, b) => {
    if (a.isPinned && !b.isPinned) return -1
    if (!a.isPinned && b.isPinned) return 1
    return 0 // 同级则按原本顺序
  })
})

const togglePin = (req) => {
  req.isPinned = !req.isPinned
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(99, 102, 241, 0.3); border-radius: 4px; }

/* 词云过滤动画 */
.fade-word-leave-active { transition: all 0.4s ease; }
.fade-word-leave-to { opacity: 0; transform: scale(0.1); }

/* 瀑布流排序动画 */
.list-move, .list-enter-active, .list-leave-active { transition: all 0.5s ease; }
.list-enter-from, .list-leave-to { opacity: 0; transform: translateX(30px); }
</style>