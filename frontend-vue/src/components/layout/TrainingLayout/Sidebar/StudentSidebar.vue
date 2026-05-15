<template>
  <div class="h-full flex transition-all duration-300 ease-in-out shrink-0" :class="isCollapsed ? 'w-16' : 'w-48'">
    <div class="h-full w-full bg-white/75 backdrop-blur-md border-r border-white/40 flex flex-col items-center py-6 shadow-xl">
      <button @click="isCollapsed = !isCollapsed" class="mb-8 p-2 rounded-xl bg-white/50 hover:bg-indigo-50 text-indigo-600 transition-colors border border-indigo-100 shadow-sm">
        <svg class="w-6 h-6 transition-transform duration-300" :class="isCollapsed ? 'rotate-180' : ''" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>

      <nav class="flex-1 w-full flex flex-col gap-3 px-3 overflow-y-auto custom-scrollbar">
        <button 
          v-for="item in activeMenuItems" :key="item.id"
          @click="togglePanel(item.id)"
          class="flex items-center p-3 rounded-xl transition-all group relative"
          :class="activePanel === item.id ? 'bg-gradient-to-br from-indigo-400 to-purple-500 text-white shadow-md' : 'hover:bg-white/80 text-gray-600 hover:text-indigo-600'"
        >
          <span v-html="item.icon" class="w-6 h-6 shrink-0"></span>
          <span v-if="!isCollapsed" class="ml-3 text-sm font-bold whitespace-nowrap overflow-hidden">{{ item.label }}</span>
          <span v-if="item.id === 'messages' && unreadCount > 0" class="absolute top-2 right-2 w-2 h-2 bg-red-500 rounded-full"></span>
        </button>
      </nav>

      <div class="w-full px-3 pt-4 border-t border-gray-200/50 mt-auto">
        <button @click="saveAndExit" class="w-full flex items-center justify-center p-3 rounded-xl hover:bg-red-50 text-red-500 transition-all group">
          <svg class="w-6 h-6 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" /></svg>
          <span v-if="!isCollapsed" class="ml-3 text-sm font-bold">暂存退出</span>
        </button>
      </div>
    </div>

    <transition name="fade-slide">
      <div v-if="activePanel" class="absolute left-[calc(100%+16px)] top-12 w-80 max-h-[80vh] bg-white/90 backdrop-blur-xl border border-white/50 shadow-2xl rounded-2xl flex flex-col z-10 overflow-hidden">
        <div class="px-4 py-3 border-b border-gray-200/60 flex justify-between items-center bg-gray-50/50">
          <h3 class="font-bold text-gray-800">{{ activePanelTitle }}</h3>
          <button @click="activePanel = null" class="text-gray-400 hover:text-red-500">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
          </button>
        </div>

        <div v-if="activePanel === 'send'" class="flex-1 flex flex-col p-4">
          <textarea class="w-full h-32 p-3 border border-gray-200 rounded-xl resize-none focus:outline-none focus:border-indigo-500 text-sm bg-white" placeholder="输入要发送给老师的消息..."></textarea>
          <div class="mt-3 flex items-center justify-between">
            <label class="cursor-pointer text-indigo-500 hover:text-indigo-600 flex items-center gap-1 text-sm font-bold bg-indigo-50 px-3 py-1.5 rounded-lg border border-indigo-100 transition-all">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" /></svg>
              <span>发文件</span>
              <input type="file" class="hidden" />
            </label>
            <button class="px-5 py-2 bg-gradient-to-r from-indigo-500 to-purple-600 text-white rounded-lg text-sm font-bold shadow-md hover:shadow-lg active:scale-95 transition-all">发送</button>
          </div>
        </div>

        <div v-else class="p-6 text-center text-gray-400 text-sm">
          {{ activePanelTitle }} 内容渲染区
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  currentNodeType: { type: String, default: 'normal' }
})

const isCollapsed = ref(true)
const activePanel = ref(null)
const unreadCount = ref(2)

const menuItems = [
  { id: 'messages', label: '我的消息', icon: '<svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"/></svg>' },
  { id: 'send', label: '联系教师', icon: '<svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/></svg>' },
  { id: 'content', label: '实训内容', icon: '<svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>' },
  { id: 'progress', label: '实训进度', icon: '<svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"/></svg>' },
  { id: 'notebook', label: '智记 (Notebook)', icon: '<svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>' },
]

const activeMenuItems = computed(() => {
  if (props.currentNodeType === 'exam') return menuItems.filter(i => i.id !== 'notebook')
  return menuItems
})

const activePanelTitle = computed(() => activeMenuItems.value.find(i => i.id === activePanel.value)?.label || '')

const togglePanel = (id) => { activePanel.value = activePanel.value === id ? null : id }

const saveAndExit = () => {
  if(confirm('确定暂存进度并退出实训吗？')) {
    // 处理退出逻辑
  }
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.fade-slide-enter-from, .fade-slide-leave-to { opacity: 0; transform: translateX(-10px) scale(0.98); }
</style>