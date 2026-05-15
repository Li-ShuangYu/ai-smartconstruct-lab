<template>
  <div class="global-ai-assistant">
    <div 
      v-show="!isOpen"
      ref="floatBall"
      class="fixed z-[9999] w-14 h-14 rounded-full shadow-[0_8px_30px_rgb(0,0,0,0.12)] bg-gradient-to-br from-indigo-500 to-purple-600 text-white flex items-center justify-center cursor-grab active:cursor-grabbing hover:scale-110 transition-transform duration-200"
      :style="{ left: position.x + 'px', top: position.y + 'px', transition: isDragging ? 'none' : 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)' }"
      @mousedown="startDrag"
      @touchstart.passive="startDrag"
    >
      <svg class="w-8 h-8" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 5c.67 0 1.35.09 2 .26 1.78-2 5.03-2.84 5.5-2.84a.5.5 0 0 1 .5.5v3.08A9 9 0 0 1 12 21a9 9 0 0 1-8-15.01V2.92a.5.5 0 0 1 .5-.5c.47 0 3.72.84 5.5 2.84.65-.17 1.33-.26 2-.26z" />
        <path d="M8 14v.5" /><path d="M16 14v.5" />
        <path d="M11.25 16.25h1.5L12 17l-.75-.75z" />
      </svg>
      <span class="absolute top-0 right-0 w-3 h-3 bg-red-500 border-2 border-white rounded-full animate-pulse"></span>
    </div>

    <transition name="pop">
      <div v-show="isOpen" class="fixed bottom-6 right-6 w-[380px] h-[600px] z-[9998] bg-white/85 backdrop-blur-2xl border border-white/60 shadow-2xl rounded-2xl flex flex-col overflow-hidden">
        
        <div class="h-14 bg-gradient-to-r from-indigo-500 to-purple-600 px-4 flex items-center justify-between shrink-0 shadow-sm relative z-20">
          <div class="flex items-center gap-2">
            <button @click="toggleHistory" class="p-1.5 text-white/80 hover:text-white hover:bg-white/20 rounded-lg transition-colors">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16m-7 6h7"/></svg>
            </button>
            <span class="font-bold text-white tracking-wide">喵喵 AI 助教</span>
          </div>
          <button @click="closeChat" class="p-1.5 text-white/80 hover:text-white hover:bg-white/20 rounded-lg transition-colors">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
          </button>
        </div>

        <div class="flex-1 relative overflow-hidden bg-slate-50/50">
          
          <div class="absolute left-0 top-0 h-full w-2/3 bg-white/95 backdrop-blur-md border-r border-gray-200 shadow-xl z-10 transition-transform duration-300 flex flex-col"
               :class="showHistory ? 'translate-x-0' : '-translate-x-full'">
            <div class="p-3 border-b border-gray-100 text-sm font-bold text-gray-600">历史对话</div>
            <div class="flex-1 overflow-y-auto custom-scrollbar p-2 space-y-1">
              <button v-for="i in 5" :key="i" class="w-full text-left px-3 py-2 text-xs text-gray-600 hover:bg-indigo-50 hover:text-indigo-600 rounded-lg truncate transition-colors">
                {{ i === 1 ? '关于 Docker 的底层原理' : '上次代码报错怎么改...' }}
              </button>
            </div>
          </div>

          <div class="h-full overflow-y-auto p-4 space-y-4 custom-scrollbar" @click="showHistory = false">
            <div class="flex flex-col items-start gap-1">
              <span class="text-[10px] text-gray-400 ml-2">喵喵 AI</span>
              <div class="bg-white border border-gray-100 shadow-sm text-sm text-gray-700 px-4 py-2.5 rounded-2xl rounded-tl-sm max-w-[85%]">
                同学你好！我是你的专属 AI 助教，遇到任何实训问题都可以问我哦 🐱
              </div>
            </div>
            
            <div class="flex flex-col items-end gap-1">
              <div class="bg-gradient-to-br from-indigo-500 to-purple-600 shadow-sm text-sm text-white px-4 py-2.5 rounded-2xl rounded-tr-sm max-w-[85%]">
                帮我检查一下刚才写的 Vue3 组件状态为什么没有更新？
              </div>
            </div>
          </div>
        </div>

        <div class="p-3 bg-white border-t border-gray-100 shrink-0 z-20">
          <div class="relative flex items-center">
            <textarea rows="1" class="w-full bg-gray-50 border border-gray-200 text-sm rounded-xl py-2.5 pl-4 pr-12 resize-none focus:outline-none focus:border-indigo-400 focus:ring-1 focus:ring-indigo-400 custom-scrollbar" placeholder="输入你的问题..."></textarea>
            <button class="absolute right-2 p-1.5 bg-indigo-500 hover:bg-indigo-600 text-white rounded-lg transition-colors shadow-sm active:scale-95">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/></svg>
            </button>
          </div>
        </div>

      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// 状态控制
const isOpen = ref(false)
const showHistory = ref(false)

// 拖拽与坐标控制
const floatBall = ref(null)
const position = ref({ x: 0, y: 0 })
const isDragging = ref(false)
let hasMoved = false // 用于区分是“点击”还是“拖拽”

// 初始化位置 (右下角偏上)
onMounted(() => {
  position.value = {
    x: window.innerWidth - 80,
    y: window.innerHeight * 0.7
  }
})

// === 核心逻辑：360悬浮球拖拽与吸边 ===
let startMousePos = { x: 0, y: 0 }
let startElPos = { x: 0, y: 0 }

const startDrag = (e) => {
  e.preventDefault() // 防止选中文本
  isDragging.value = true
  hasMoved = false
  
  const clientX = e.clientX || e.touches?.[0].clientX
  const clientY = e.clientY || e.touches?.[0].clientY
  
  startMousePos = { x: clientX, y: clientY }
  startElPos = { ...position.value }

  window.addEventListener('mousemove', onDrag)
  window.addEventListener('touchmove', onDrag, { passive: false })
  window.addEventListener('mouseup', endDrag)
  window.addEventListener('touchend', endDrag)
}

const onDrag = (e) => {
  if (!isDragging.value) return
  
  const clientX = e.clientX || e.touches?.[0].clientX
  const clientY = e.clientY || e.touches?.[0].clientY
  
  const deltaX = clientX - startMousePos.x
  const deltaY = clientY - startMousePos.y
  
  // 移动超过 3px 才认定为拖拽，防止轻微手抖导致无法点击
  if (Math.abs(deltaX) > 3 || Math.abs(deltaY) > 3) {
    hasMoved = true
  }

  // 边界约束
  let newX = startElPos.x + deltaX
  let newY = startElPos.y + deltaY
  
  const maxX = window.innerWidth - 56 // 56 是球的宽度
  const maxY = window.innerHeight - 56
  
  newX = Math.max(0, Math.min(newX, maxX))
  newY = Math.max(0, Math.min(newY, maxY))

  position.value = { x: newX, y: newY }
}

const endDrag = () => {
  isDragging.value = false
  window.removeEventListener('mousemove', onDrag)
  window.removeEventListener('touchmove', onDrag)
  window.removeEventListener('mouseup', endDrag)
  window.removeEventListener('touchend', endDrag)

  // 如果没有移动（或者移动极小），说明是点击事件
  if (!hasMoved) {
    isOpen.value = true
    return
  }

  // 吸边逻辑 (松手后自动贴近左侧或右侧)
  const centerX = window.innerWidth / 2
  if (position.value.x < centerX) {
    position.value.x = 10 // 吸附左边缘
  } else {
    position.value.x = window.innerWidth - 66 // 吸附右边缘
  }
}

// 界面控制逻辑
const closeChat = () => {
  isOpen.value = false
  showHistory.value = false // 关闭时重置历史状态
}

const toggleHistory = () => {
  showHistory.value = !showHistory.value
}
</script>

<style scoped>
/* 滚动条美化 */
.custom-scrollbar::-webkit-scrollbar { width: 5px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }

/* 窗口弹出动画 */
.pop-enter-active, .pop-leave-active {
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  transform-origin: bottom right;
}
.pop-enter-from, .pop-leave-to {
  opacity: 0;
  transform: scale(0.8) translateY(20px);
}
</style>