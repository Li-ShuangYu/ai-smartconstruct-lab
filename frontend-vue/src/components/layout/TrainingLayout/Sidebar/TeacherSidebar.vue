<template>
  <div class="h-full flex transition-all duration-300 ease-in-out shrink-0" :class="isCollapsed ? 'w-16' : 'w-48'">
    <div class="h-full w-full bg-white/75 backdrop-blur-md border-r border-white/40 flex flex-col items-center py-6 shadow-xl">
      <button @click="isCollapsed = !isCollapsed" class="mb-8 p-2 rounded-xl bg-white/50 hover:bg-purple-50 text-purple-600 transition-colors border border-purple-100 shadow-sm">
        <svg class="w-6 h-6 transition-transform duration-300" :class="isCollapsed ? 'rotate-180' : ''" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>

      <nav class="flex-1 w-full flex flex-col gap-3 px-3 overflow-y-auto custom-scrollbar">
        <button 
          v-for="item in menuItems" :key="item.id"
          @click="togglePanel(item.id)"
          class="flex items-center p-3 rounded-xl transition-all group relative"
          :class="activePanel === item.id ? 'bg-gradient-to-br from-purple-500 to-indigo-600 text-white shadow-md' : 'hover:bg-white/80 text-gray-600 hover:text-purple-600'"
        >
          <span v-html="item.icon" class="w-6 h-6 shrink-0"></span>
          <span v-if="!isCollapsed" class="ml-3 text-sm font-bold whitespace-nowrap overflow-hidden">{{ item.label }}</span>
          <span v-if="item.id === 'messages' && unreadCount > 0" class="absolute top-2 right-2 w-2 h-2 bg-red-500 rounded-full"></span>
        </button>
      </nav>
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
          <div class="mb-3">
            <label class="text-xs font-bold text-gray-600 mb-1 block">发送对象</label>
            <select class="w-full p-2 border border-gray-200 rounded-lg text-sm bg-white focus:outline-none focus:border-purple-500">
              <option value="all">📢 全班广播</option>
              <option value="group1">实训第一组</option>
              <option value="student1">李双宇 (求助中)</option>
            </select>
          </div>
          <textarea class="w-full h-24 p-3 border border-gray-200 rounded-xl resize-none focus:outline-none focus:border-purple-500 text-sm bg-white" placeholder="输入指令或解答内容..."></textarea>
          <div class="mt-3 flex items-center justify-between">
             <label class="cursor-pointer text-purple-600 hover:text-purple-700 flex items-center gap-1 text-sm font-bold bg-purple-50 px-3 py-1.5 rounded-lg border border-purple-100 transition-all">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" /></svg>
              <span>发文件</span>
              <input type="file" class="hidden" />
            </label>
            <button class="px-5 py-2 bg-gradient-to-r from-purple-500 to-indigo-600 text-white rounded-lg text-sm font-bold shadow-md hover:shadow-lg active:scale-95 transition-all">发送</button>
          </div>
        </div>

        <div v-else class="p-6 text-center text-gray-400 text-sm">
          {{ activePanelTitle }} 数据监控渲染区
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const isCollapsed = ref(true)
const activePanel = ref(null)
const unreadCount = ref(5)

const menuItems = [
  { id: 'messages', label: '求助与消息', icon: '<svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/></svg>' },
  { id: 'send', label: '指令分发/广播', icon: '<svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5.882V19.24a1.76 1.76 0 01-3.417.592l-2.147-6.15M18 13a3 3 0 100-6M5.436 13.683A4.001 4.001 0 017 6h1.832c4.1 0 7.625-1.234 9.168-3v14c-1.543-1.766-5.067-3-9.168-3H7a3.988 3.988 0 01-1.564-.317z"/></svg>' },
  { id: 'monitor', label: '班级监控', icon: '<svg fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>' }
]

const activePanelTitle = computed(() => menuItems.find(i => i.id === activePanel.value)?.label || '')
const togglePanel = (id) => { activePanel.value = activePanel.value === id ? null : id }
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.fade-slide-enter-from, .fade-slide-leave-to { opacity: 0; transform: translateX(-10px) scale(0.98); }
</style>