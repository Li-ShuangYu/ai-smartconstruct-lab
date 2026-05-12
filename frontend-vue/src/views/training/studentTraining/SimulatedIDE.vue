<template>
  <div class="flex h-screen w-full bg-[#181818] text-[#cccccc] font-sans overflow-hidden select-none">
    
    <div class="w-[65%] flex flex-col border-r border-[#2b2b2b] h-full overflow-hidden relative">
      
      <div v-if="appState === 3" class="bg-[#1e4620]/80 border-b border-[#2ea043] p-1.5 flex items-center justify-center fade-in z-20 shadow-md">
        <span class="text-[#3fb950] font-bold text-xs flex items-center tracking-wide">
          <svg class="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          🎉 数组学习任务完成！所有算法测试通过，数组反转、排序、查找功能正常运行
        </span>
      </div>

      <div v-if="appState === 2 && !diffActionHandled" class="bg-[#3a1d1d] border-b border-[#f85149] p-1.5 flex items-center px-4 fade-in z-20">
        <svg class="w-3.5 h-3.5 text-[#f85149] mr-2 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        <span class="text-[#f85149] text-[11px] font-mono truncate">Run Error: [IndexError] list index out of range. QuickSort partition index calculation error.</span>
      </div>

      <div class="flex-[7] flex flex-col min-h-0 bg-[#1e1e1e] relative">
        <div class="flex items-center justify-between bg-[#181818] border-b border-[#2b2b2b] text-[12px]">
          <div class="flex">
            <div class="px-4 py-2 bg-[#1e1e1e] border-t border-[#007acc] text-[#e3e3e3] flex items-center gap-2 cursor-pointer">
              <svg class="w-4 h-4 text-[#4B8BBE]" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15h-2v-2h2v2zm0-4h-2V7h2v6zm4 4h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
              array_operations.py
              <span class="ml-1.5 text-gray-500 hover:text-gray-300">×</span>
            </div>
          </div>
          <div class="flex items-center space-x-2.5 px-3 text-gray-400">
            <svg class="w-4 h-4 hover:text-gray-200 cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 7v8a2 2 0 002 2h6M8 7V5a2 2 0 012-2h4.586a1 1 0 01.707.293l4.414 4.414a1 1 0 01.293.707V15a2 2 0 01-2 2h-2M8 7H6a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2v-2"></path></svg>
            <svg class="w-4 h-4 hover:text-gray-200 cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
            <svg class="w-4 h-4 hover:text-gray-200 cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            <svg class="w-4 h-4 hover:text-gray-200 cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4"></path></svg>
            <span class="text-gray-600 font-bold">···</span>
          </div>
        </div>
        <div class="px-4 py-1 text-[11px] text-[#858585] border-b border-[#2b2b2b] flex items-center gap-1.5 font-mono">
          <span>src</span> <span class="text-gray-600">></span> <span>python_learning</span> <span class="text-gray-600">></span> <span>algorithms</span> <span class="text-gray-600">></span> <span class="text-[#cccccc]">array_operations.py</span> <span class="text-gray-600">></span> <span class="text-[#dcdcaa]">ArrayOperations</span> <span class="text-gray-600">></span> <span class="text-[#dcdcaa]">__init__</span>
        </div>

        <div v-if="showDiffBar" class="absolute top-16 left-0 right-0 bg-[#1e4620]/90 backdrop-blur-sm border-y border-[#2ea043]/50 px-4 py-1.5 flex justify-between items-center z-30 fade-in shadow-[0_4px_15px_rgba(0,0,0,0.3)]">
          <div class="flex items-center text-[#e3e3e3] text-[12px] gap-2">
            <svg class="w-4 h-4 text-[#3fb950]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
            变更已完成，请确认是否采纳。
          </div>
          <div class="flex gap-2">
            <button @click="handleRejectDiff" class="px-2.5 py-1 text-[#cccccc] hover:text-white text-[11px] rounded flex items-center transition">
              <span class="mr-1.5 font-medium">拒绝</span> <span class="text-[10px] text-gray-500 font-mono">Ctrl+Backspace</span>
            </button>
            <button @click="handleAcceptDiff" class="px-2.5 py-1 bg-[#e3e3e3] hover:bg-white text-black text-[11px] rounded flex items-center transition shadow-sm font-medium">
              <span class="mr-1.5">保留变更 (Accept)</span> <span class="text-[10px] text-gray-600 font-mono">Ctrl+Enter</span>
            </button>
            <div class="border-l border-[#2ea043]/50 pl-2 ml-1 text-gray-400 flex items-center space-x-1 cursor-pointer">
               <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7"></path></svg>
               <span class="text-[10px]">1/1</span>
               <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path></svg>
            </div>
          </div>
        </div>

        <div class="flex-1 overflow-y-auto custom-scrollbar font-mono text-[13px] leading-[22px] pt-2 pb-10 relative" ref="codeScrollRef">
          <div v-if="codeLines.length === 0" class="absolute inset-0 flex items-center justify-center text-[#858585]">
            <div class="flex flex-col items-center gap-3 animate-pulse">
                <svg class="w-8 h-8 text-[#4B8BBE]/50" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15h-2v-2h2v2zm0-4h-2V7h2v6zm4 4h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
                <span>等待 Builder 生成代码...</span>
            </div>
          </div>
          <div v-for="(line, idx) in codeLines" :key="idx" class="flex hover:bg-[#2a2d2e] transition-colors w-full group" :class="getLineBgColor(line)">
            <div class="w-14 text-right pr-4 text-[#858585] select-none shrink-0 text-[12px]" :class="getLineNumColor(line)">
              {{ line.diffType === 'added' ? '+' : line.diffType === 'removed' ? '-' : (idx + 1) }}
            </div>
            <div class="whitespace-pre pl-2 break-all" :class="getLineTextColor(line)" v-html="line.content"></div>
          </div>
        </div>
      </div>

      <div class="flex-[3] flex flex-col min-h-0 bg-[#1e1e1e] border-t border-[#2b2b2b]">
        <div class="flex items-center justify-between text-[11px] text-[#cccccc] pl-4 pt-1.5 pb-1 border-b border-[#2b2b2b] select-none">
          <div class="flex gap-4">
            <div class="hover:text-white cursor-pointer">问题</div>
            <div class="hover:text-white cursor-pointer">输出</div>
            <div class="hover:text-white cursor-pointer">调试控制台</div>
            <div class="text-[#e3e3e3] border-b border-[#007acc] pb-1 cursor-pointer font-medium">终端</div>
            <div class="hover:text-white cursor-pointer">SQL Preview</div>
            <div class="hover:text-white cursor-pointer">GitLens</div>
          </div>
          <div class="flex items-center space-x-2 pr-3 text-[#858585]">
            <svg class="w-3.5 h-3.5 hover:text-white cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 002-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
            <span class="text-xs">python</span>
            <svg class="w-3.5 h-3.5 hover:text-white cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6zM16 13a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 01-1 1h-2a1 1 0 01-1-1v-6z"></path></svg>
            <svg class="w-3.5 h-3.5 hover:text-white cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
            <span class="font-bold cursor-pointer hover:text-white px-1">···</span>
            <svg class="w-3.5 h-3.5 hover:text-white cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7"></path></svg>
            <svg class="w-3.5 h-3.5 hover:text-white cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </div>
        </div>
        <div class="flex-1 overflow-y-auto custom-scrollbar p-3 font-mono text-[12px] leading-[1.6]" ref="terminalScrollRef">
          <div v-for="(log, idx) in terminalLogs" :key="idx" :class="log.color">
            {{ log.text }}
          </div>
          <div v-if="isTerminalRunning" class="inline-block w-2 h-3.5 bg-[#cccccc] animate-pulse ml-1 align-middle"></div>
        </div>
      </div>
    </div>

    <div class="w-[35%] flex flex-col bg-[#1e1e1e] border-l border-[#2b2b2b] h-full shadow-[-5px_0_15px_rgba(0,0,0,0.2)] z-10 relative">
      <div class="px-4 py-3 flex justify-between items-center border-b border-[#2b2b2b]">
        <div class="font-bold text-[14px] tracking-wide text-[#e3e3e3]">CODE</div>
        <div class="flex space-x-3 text-[#858585]">
          <svg class="w-4 h-4 hover:text-[#cccccc] cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 4v16m8-8H4"></path></svg>
          <svg class="w-4 h-4 hover:text-[#cccccc] cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          <svg class="w-4 h-4 hover:text-[#cccccc] cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"></path></svg>
          <svg class="w-4 h-4 hover:text-[#cccccc] cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M6 18L18 6M6 6l12 12"></path></svg>
        </div>
      </div>

      <div class="px-4 py-3 flex items-center gap-2 text-[#e3e3e3]">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 002-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
        <span class="font-bold text-[13px]">Builder</span>
      </div>

      <div class="flex-1 overflow-y-auto custom-scrollbar px-4 pb-4 space-y-6" ref="chatScrollRef">
        <div v-for="(block, idx) in chatBlocks" :key="idx" class="fade-in">
          
          <div v-if="block.role === 'user'" class="flex justify-end">
            <div class="bg-[#2d2d2d] text-[#e3e3e3] text-[13px] px-4 py-2.5 rounded-xl rounded-tr-sm max-w-[95%] leading-relaxed border border-[#3c3c3c]">
              {{ block.content }}
            </div>
          </div>

          <div v-else class="flex flex-col gap-2 w-full">
            
            <div v-if="block.type === 'agent'" class="pl-5 mt-2 mb-2">
                <div class="bg-[#252526] border border-[#3c3c3c] rounded-md overflow-hidden transition-all duration-300 w-full max-w-[95%]">
                    <div class="px-3 py-2 flex items-center justify-between">
                    <div class="flex items-center gap-2 text-[12px] flex-1 min-w-0">
                        <svg class="w-3.5 h-3.5 text-[#e3ce62] shrink-0" viewBox="0 0 24 24" fill="currentColor"><path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"/></svg>
                        <span class="font-medium text-[#cccccc] truncate">{{ block.filename }}</span>
                        <span v-if="block.path" class="text-[#858585] font-mono truncate hidden lg:inline">{{ block.path }}</span>
                        <span v-if="block.status === 'success' && block.add > 0" class="text-[#3fb950] font-mono shrink-0">+{{ block.add }}</span>
                        <span v-if="block.status === 'success' && block.del > 0" class="text-[#f85149] font-mono shrink-0">-{{ block.del }}</span>
                    </div>
                    <div class="flex items-center gap-2 shrink-0 ml-3">
                        <svg v-if="block.status === 'loading'" class="animate-spin h-3.5 w-3.5 text-[#cccccc]" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                        <button v-else-if="block.status === 'success' && block.path" class="text-[11px] text-[#cccccc] bg-[#333333] hover:bg-[#444444] px-2 py-0.5 rounded border border-[#444444] transition-colors">查看变更</button>
                        <svg v-else-if="block.status === 'success'" class="w-3.5 h-3.5 text-[#3fb950]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                    </div>
                    </div>
                </div>
            </div>

            <div v-if="block.type === 'text'" class="text-[13px] text-[#cccccc] leading-[1.7] mt-1 pl-1">
              <div v-html="block.content" class="rich-text-content"></div>
              <span v-if="block.isTyping" class="inline-block w-1.5 h-3.5 bg-[#cccccc] ml-1 animate-pulse align-middle"></span>
            </div>

          </div>
        </div>
      </div>

      <div class="p-4 bg-[#1e1e1e] relative">
        
        <div v-if="canDeploy" class="absolute -top-10 left-4 right-4 bg-[#252526] border border-[#3c3c3c] rounded-md px-3 py-1.5 flex items-center justify-between shadow-lg text-[12px] text-[#cccccc] fade-in">
          <div class="flex items-center gap-2">
            <svg class="w-3.5 h-3.5 text-[#858585]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16"></path></svg>
            <svg class="w-3.5 h-3.5 text-[#858585]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
            <span class="text-[#3fb950] flex items-center gap-1"><svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg> 任务完成</span>
            <span>文件需要审查</span>
            <svg class="w-3.5 h-3.5 text-[#858585]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 9l4-4 4 4m0 6l-4 4-4-4"></path></svg>
          </div>
        </div>

        <div class="relative bg-[#252526] border border-[#3c3c3c] rounded-lg focus-within:border-[#555555] transition-colors flex flex-col">
          
          <div class="flex items-center gap-1.5 px-3 pt-2 text-[#858585] text-[12px] border-b border-transparent">
             <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"></path></svg>
             <span>@Builder</span>
             <svg class="w-3.5 h-3.5 ml-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.121 14.121L19 19m-7-7l7-7m-7 7l-2.879 2.879M12 12L9.121 9.121m0 5.758a3 3 0 10-4.243 4.243 3 3 0 004.243-4.243zm0-5.758a3 3 0 10-4.243-4.243 3 3 0 004.243 4.243z"></path></svg>
          </div>

          <textarea 
            v-model="currentInput"
            :disabled="isGenerating"
            rows="3"
            class="w-full bg-transparent text-[13px] text-[#e3e3e3] px-3 py-2 outline-none resize-none custom-scrollbar placeholder-[#6e6e6e] disabled:opacity-50 min-h-[70px]"
            placeholder="您正在与 Builder 聊天"
          ></textarea>
          
          <div class="flex items-center justify-between px-2 pb-2">
            <div class="flex items-center gap-2 text-[#858585]">
              <span class="hover:text-[#cccccc] cursor-pointer font-mono text-[14px]">@</span>
              <span class="hover:text-[#cccccc] cursor-pointer font-mono text-[14px]">#</span>
              <svg class="w-4 h-4 hover:text-[#cccccc] cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
            </div>
            <div class="flex items-center gap-3">
              <div class="text-[#858585] text-[12px] flex items-center gap-1 cursor-pointer hover:text-[#cccccc]">
                <span>Auto</span>
                <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path></svg>
              </div>
              <svg class="w-4 h-4 text-[#858585] hover:text-[#cccccc] cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"></path></svg>
              <svg class="w-4 h-4 text-[#858585] hover:text-[#cccccc] cursor-pointer" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"></path></svg>
              <button 
                @click="handleSend" 
                :disabled="isGenerating || !currentInput.trim()"
                class="p-1 rounded transition-colors"
                :class="(isGenerating || !currentInput.trim()) ? 'text-[#555555]' : 'bg-[#e3e3e3] text-black hover:bg-white'"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"></path></svg>
              </button>
            </div>
          </div>
        </div>
        
        <button 
          @click="navigateToDeploy"
          :disabled="!canDeploy"
          class="w-full mt-3 py-2 rounded-md text-[13px] font-bold transition-all duration-300 flex justify-center items-center gap-2"
          :class="canDeploy ? 'bg-[#238636] hover:bg-[#2ea043] text-white shadow-[0_0_10px_rgba(46,160,67,0.3)]' : 'bg-[#21262d] text-[#8b949e] border border-[#30363d] cursor-not-allowed'"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"></path></svg>
          进入下一个节点
        </button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const appState = ref(1)
const isGenerating = ref(false)
const canDeploy = ref(false)
const chatBlocks = ref([])
const currentInput = ref('')
const chatScrollRef = ref(null)
const codeScrollRef = ref(null)
const terminalScrollRef = ref(null)

const codeLines = ref([])
const terminalLogs = ref([])
const isTerminalRunning = ref(false)
const showDiffBar = ref(false)
const diffActionHandled = ref(false)

const delay = (ms) => new Promise(res => setTimeout(res, ms))

const scrollToBottom = async () => {
  await nextTick()
  if (chatScrollRef.value) {
    chatScrollRef.value.scrollTop = chatScrollRef.value.scrollHeight
  }
}

const scrollCodeToBottom = async () => {
  await nextTick()
  if (codeScrollRef.value) {
    codeScrollRef.value.scrollTop = codeScrollRef.value.scrollHeight
  }
}

const scrollToDiff = async () => {
  await nextTick()
  if (codeScrollRef.value) {
    const diffIndex = codeLines.value.findIndex(line => line.diffType === 'added' || line.diffType === 'removed')
    if (diffIndex !== -1) {
      const topPos = diffIndex * 22 - 150
      codeScrollRef.value.scrollTo({ top: Math.max(0, topPos), behavior: 'smooth' })
    }
  }
}

const scrollTerminalToBottom = async () => {
  await nextTick()
  if (terminalScrollRef.value) {
    terminalScrollRef.value.scrollTop = terminalScrollRef.value.scrollHeight
  }
}

const streamRichText = async (htmlContent) => {
  const block = { role: 'ai', type: 'text', content: '', isTyping: true }
  chatBlocks.value.push(block)
  await scrollToBottom()
  
  const chunkSize = 15
  for (let i = 0; i < htmlContent.length; i += chunkSize) {
    block.content += htmlContent.slice(i, i + chunkSize)
    await delay(10)
    if (i % 60 === 0) await scrollToBottom()
  }
  block.content = htmlContent
  block.isTyping = false
  await scrollToBottom()
}

const streamCodeLines = async (linesToAppend, delayMs = 15) => {
  for (const line of linesToAppend) {
    codeLines.value.push(line)
    await delay(delayMs)
    if(codeLines.value.length % 5 === 0) await scrollCodeToBottom()
  }
  await scrollCodeToBottom()
}

const streamTerminal = async (logs, delayMs = 120) => {
  isTerminalRunning.value = true
  for (const log of logs) {
    terminalLogs.value.push(log)
    await delay(delayMs)
    await scrollTerminalToBottom()
  }
  isTerminalRunning.value = false
}

const pushAgent = async (filename, path, waitTime, addLines = 0, delLines = 0) => {
  const block = { role: 'ai', type: 'agent', filename, path, status: 'loading', add: addLines, del: delLines }
  chatBlocks.value.push(block)
  await scrollToBottom()
  await delay(waitTime)
  block.status = 'success'
  await scrollToBottom()
}

const autoFillNextInput = (inputText) => {
  currentInput.value = inputText
}

onMounted(() => {
  appState.value = 1
  currentInput.value = "请帮我生成一个Python数组操作学习模块，包含数组反转、快速排序、二分查找等常用算法，要有完整详细的注释说明"
})

const handleSend = async () => {
  if (isGenerating.value) return
  
  const query = currentInput.value
  currentInput.value = ''
  isGenerating.value = true
  canDeploy.value = false

  chatBlocks.value.push({ role: 'user', content: query })
  await scrollToBottom()

  if (appState.value === 1) {
    await runState1Sequence()
  } else if (appState.value === 2) {
    await runState2Sequence()
  }
}

const runState1Sequence = async () => {
  await streamRichText("好的，我将为您创建一个完整的Python数组操作学习模块，包含数组反转、快速排序和二分查找算法...")
  await pushAgent("规划数组操作模块结构", "", 1500)
  codeLines.value = []
  
  await streamCodeLines(getV1FullCode().slice(0, 30), 15)
  await streamRichText("模块结构已定义，正在编写数组反转算法...")
  await pushAgent("编写数组反转算法", "array_operations.py", 2000, 45, 0)
  await streamCodeLines(getV1FullCode().slice(30, 80), 10)
  
  await streamRichText("数组反转完成，正在实现快速排序算法...")
  await pushAgent("编写快速排序算法", "array_operations.py", 2500, 55, 0)
  await streamCodeLines(getV1FullCode().slice(80, 140), 10)
  
  await streamRichText("快速排序完成，正在添加二分查找算法...")
  await pushAgent("编写二分查找算法", "array_operations.py", 2000, 40, 0)
  await streamCodeLines(getV1FullCode().slice(140), 10)

  await streamRichText("所有代码生成完毕，正在进行系统自动测试...")
  const checkAgent = { role: 'ai', type: 'agent', filename: 'AI 代码测试', path: '', status: 'loading' }
  chatBlocks.value.push(checkAgent)
  await scrollToBottom()
  await streamTerminal(getTerminalBuildLogs(), 150)
  checkAgent.status = 'success'

  const summaryHtml = `
    <h3 class="text-[14px] font-bold text-white mb-2 mt-1">已完成！</h3>
    <p class="mb-3">我已经生成了完整的 <code>array_operations.py</code> 数组操作学习模块。该模块包含三种核心算法的实现。</p>
    <h4 class="font-bold text-[#e3e3e3] mb-1">实现的功能：</h4>
    <ul class="list-disc pl-5 space-y-1 mb-3 text-[#cccccc]">
        <li><code>reverse_array()</code> - 原地数组反转算法</li>
        <li><code>quick_sort()</code> - 经典快速排序实现</li>
        <li><code>binary_search()</code> - 二分查找算法</li>
        <li><code>print_array()</code> - 数组可视化输出</li>
    </ul>
    <h4 class="font-bold text-[#e3e3e3] mb-1">使用示例：</h4>
    <ol class="list-decimal pl-5 space-y-1">
        <li><b>导入模块：</b><code class="bg-[#2d2d2d] px-1 py-0.5 rounded text-[#e3e3e3]">from array_operations import ArrayOperations</code></li>
        <li><b>创建实例：</b><code class="bg-[#2d2d2d] px-1 py-0.5 rounded text-[#e3e3e3]">arr = ArrayOperations([3, 1, 4, 1, 5, 9])</code></li>
        <li><b>调用方法：</b><code class="bg-[#2d2d2d] px-1 py-0.5 rounded text-[#e3e3e3]">arr.quick_sort()</code></li>
    </ol>
  `
  await streamRichText(summaryHtml)
  
  isGenerating.value = false
  appState.value = 2
  canDeploy.value = true
  
  setTimeout(() => {
    autoFillNextInput("运行报错了，IndexError: list index out of range，请检查快速排序的分区索引计算")
  }, 500)
}

const runState2Sequence = async () => {
  await streamRichText("收到报错信息，正在分析快速排序的索引越界问题...")
  await pushAgent("分析快速排序分区算法", "", 1800)
  await streamRichText("找到问题了！Partition函数中的索引计算有误，基准值选择和边界处理存在缺陷...")
  await pushAgent("修复快速排序索引越界问题", "array_operations.py", 2000, 8, 5)
  
  codeLines.value = getV2CodeDiff_Part1()
  await scrollToDiff()

  await streamRichText("分区逻辑已修复，正在添加边界检查和输入验证...")
  await pushAgent("添加输入验证和边界保护", "array_operations.py", 2000, 12, 3)
  
  codeLines.value = getV2CodeDiff_Full()
  await scrollToDiff()

  await streamRichText("代码修正已完成，正在执行二次测试...")
  const checkAgent = { role: 'ai', type: 'agent', filename: 'AI 二次测试', path: '', status: 'loading' }
  chatBlocks.value.push(checkAgent)
  await scrollToBottom()
  await streamTerminal(getV2TerminalPrecheck(), 120)
  checkAgent.status = 'success'

  showDiffBar.value = true
  await streamRichText("已修复快速排序的索引越界问题，并增加了输入验证...")
  await scrollToDiff()
  
  isGenerating.value = false
}

const handleAcceptDiff = async () => {
  showDiffBar.value = false
  diffActionHandled.value = true
  codeLines.value = getV2FullCode() 
  
  isGenerating.value = true
  await pushAgent("应用变更并运行测试用例", "", 1500)
  await streamTerminal([
    { text: '$ python -m pytest test_array_operations.py', color: 'text-[#cccccc]' },
    { text: '============================= test session starts ==============================', color: 'text-[#858585]' },
    { text: 'collecting ... collected 15 items', color: 'text-[#858585]' },
    { text: 'test_reverse_array.py::test_empty_array ... PASSED', color: 'text-[#3fb950]' },
    { text: 'test_reverse_array.py::test_single_element ... PASSED', color: 'text-[#3fb950]' },
    { text: 'test_reverse_array.py::test_normal_array ... PASSED', color: 'text-[#3fb950]' },
    { text: 'test_quick_sort.py::test_sort_empty ... PASSED', color: 'text-[#3fb950]' },
    { text: 'test_quick_sort.py::test_sort_single ... PASSED', color: 'text-[#3fb950]' },
    { text: 'test_quick_sort.py::test_sort_random ... PASSED', color: 'text-[#3fb950]' },
    { text: 'test_binary_search.py::test_found ... PASSED', color: 'text-[#3fb950]' },
    { text: 'test_binary_search.py::test_not_found ... PASSED', color: 'text-[#3fb950]' },
    { text: '============================== 15 passed in 0.12s ===============================', color: 'text-[#3fb950]' },
    { text: '✅ [SYSTEM] All tests passed. Array operations module is ready.', color: 'text-[#4B8BBE] font-bold' }
  ], 100)
  
  await streamRichText("<p>变更已应用并验证通过。所有15个测试用例均通过，数组操作模块运行正常。</p>")
  isGenerating.value = false
  appState.value = 3
  canDeploy.value = true
  
  setTimeout(() => {
    autoFillNextInput("所有测试都通过了，任务完成！")
  }, 500)
}

const handleRejectDiff = () => {
  showDiffBar.value = false
  codeLines.value = getV1FullCode()
  isGenerating.value = false
}

const navigateToDeploy = () => { if (appState.value !== 3) { router.push('/student/deploy') } }

const getLineBgColor = (line) => {
  if (line.diffType === 'added') return 'bg-[#1e4620]/40 border-l-[3px] border-[#2ea043]'
  if (line.diffType === 'removed') return 'bg-[#4b1818]/40 border-l-[3px] border-[#f85149] line-through opacity-70'
  return 'border-l-[3px] border-transparent'
}
const getLineNumColor = (line) => {
  if (line.diffType === 'added') return 'text-[#2ea043]'
  if (line.diffType === 'removed') return 'text-[#f85149]'
  return 'text-[#858585]'
}
const getLineTextColor = (line) => {
  if (line.diffType === 'added') return 'text-[#7ee787]'
  if (line.diffType === 'removed') return 'text-[#ff7b72]'
  return 'text-[#cccccc]'
}

// Python数组操作学习模块代码
const getV1FullCode = () => {
  const code = [
    { content: '#!/usr/bin/env python' },
    { content: '# -*- coding: utf-8 -*-' },
    { content: '""" Python Array Operations Learning Module """' },
    { content: '@Author: Builder AI' },
    { content: '@Description: Comprehensive array operations including reverse, quicksort, binary search' },
    { content: '@Version: 1.0' },
    { content: '"""' },
    { content: '' },
    { content: 'import random' },
    { content: 'import sys' },
    { content: 'import time' },
    { content: '' },
    { content: 'class ArrayOperations:' },
    { content: '    def __init__(self, arr=None):' },
    { content: '        # Initialize array, support empty array' },
    { content: '        self.array = arr if arr is not None else []' },
    { content: '        self.original = arr.copy() if arr else []' },
    { content: '' },
    { content: '    def print_array(self, title="Array"):' },
    { content: '        # Print array beautifully' },
    { content: '        print(f"{title}: {self.array}")' },
    { content: '' },
    { content: '    def reverse_array(self):' },
    { content: '        """' },
    { content: '        In-place array reverse algorithm' },
    { content: '        Time Complexity: O(n)' },
    { content: '        Space Complexity: O(1)' },
    { content: '        """' },
    { content: '        left = 0' },
    { content: '        right = len(self.array) - 1' },
    { content: '' },
    { content: '        while left < right:' },
    { content: '            # Swap elements at left and right pointers' },
    { content: '            self.array[left], self.array[right] = self.array[right], self.array[left]' },
    { content: '            left += 1' },
    { content: '            right -= 1' },
    { content: '' },
    { content: '        return self.array' },
    { content: '' },
    { content: '    def _partition(self, low, high):' },
    { content: '        """' },
    { content: '        QuickSort partition function' },
    { content: '        Select pivot and partition the array' },
    { content: '        """' },
    { content: '        # Bug: choosing last element as pivot with index issue' },
    { content: '        pivot = self.array[high]' },
    { content: '        i = low - 1' },
    { content: '' },
    { content: '        for j in range(low, high):' },
    { content: '            if self.array[j] <= pivot:' },
    { content: '                i = i + 1' },
    { content: '                self.array[i], self.array[j] = self.array[j], self.array[i]' },
    { content: '' },
    { content: '        self.array[i + 1], self.array[high] = self.array[high], self.array[i + 1]' },
    { content: '        return i + 1' },
    { content: '' },
    { content: '    def _quick_sort_helper(self, low, high):' },
    { content: '        """' },
    { content: '        QuickSort recursive helper function' },
    { content: '        Time Complexity: O(n log n) average, O(n^2) worst' },
    { content: '        """' },
    { content: '        if low < high:' },
    { content: '            # Get partition index' },
    { content: '            pi = self._partition(low, high)' },
    { content: '' },
    { content: '            # Recursively sort left half' },
    { content: '            self._quick_sort_helper(low, pi - 1)' },
    { content: '' },
    { content: '            # Recursively sort right half' },
    { content: '            self._quick_sort_helper(pi + 1, high)' },
    { content: '' },
    { content: '    def quick_sort(self):' },
    { content: '        """' },
    { content: '        QuickSort main function' },
    { content: '        Bug: no handling for empty or single-element array' },
    { content: '        """' },
    { content: '        self._quick_sort_helper(0, len(self.array))' },
    { content: '        return self.array' },
    { content: '' },
    { content: '    def binary_search(self, target):' },
    { content: '        """' },
    { content: '        Binary search algorithm' },
    { content: '        Time Complexity: O(log n)' },
    { content: '        Note: array must be sorted' },
    { content: '        """' },
    { content: '        left = 0' },
    { content: '        right = len(self.array) - 1' },
    { content: '' },
    { content: '        while left <= right:' },
    { content: '            mid = int((left + right) / 2)' },
    { content: '' },
    { content: '            if self.array[mid] == target:' },
    { content: '                return mid  # Return found index' },
    { content: '            elif self.array[mid] < target:' },
    { content: '                left = mid + 1' },
    { content: '            else:' },
    { content: '                right = mid - 1' },
    { content: '' },
    { content: '        return -1  # Not found' },
    { content: '' },
    { content: '    def generate_random_array(self, size=10, min_val=0, max_val=100):' },
    { content: '        """Generate random array for testing"""' },
    { content: '        self.array = [random.randint(min_val, max_val) for _ in range(size)]' },
    { content: '        self.original = self.array.copy()' },
    { content: '        return self.array' },
    { content: '' },
    { content: 'def main():' },
    { content: '    print("="*50)' },
    { content: '    print("Python Array Operations Learning Module")' },
    { content: '    print("="*50)' },
    { content: '' },
    { content: '    # Demo: Reverse Array' },
    { content: '    arr1 = ArrayOperations([1, 2, 3, 4, 5])' },
    { content: '    print("Original array:", arr1.array)' },
    { content: '    arr1.reverse_array()' },
    { content: '    print("Reversed array:", arr1.array)' },
    { content: '' },
    { content: '    # Demo: Quick Sort' },
    { content: '    arr2 = ArrayOperations([64, 34, 25, 12, 22, 11, 90])' },
    { content: '    print("Before sort:", arr2.array)' },
    { content: '    arr2.quick_sort()' },
    { content: '    print("After sort:", arr2.array)' },
    { content: '' },
    { content: '    # Demo: Binary Search' },
    { content: '    arr3 = ArrayOperations([11, 12, 22, 25, 34, 64, 90])' },
    { content: '    target = 25' },
    { content: '    result = arr3.binary_search(target)' },
    { content: '    print(f"Binary search for {target}: index = {result}")' },
    { content: '' },
    { content: '    print("="*50)' },
    { content: '    print("All demos completed!")' },
    { content: '    print("="*50)' },
    { content: '' },
    { content: 'if __name__ == "__main__":' },
    { content: '    main()' }
  ]
  return code.map(line => ({ content: highlightPython(line.content) }))
}

const highlightPython = (text) => {
  if (!text) return ''
  
  let result = text
  
  const keywords = ['def', 'class', 'return', 'if', 'else', 'elif', 'while', 'for', 'in', 'and', 'or', 'not', 'is', 'None', 'True', 'False', 'import', 'from', 'as', 'try', 'except', 'finally', 'with', 'raise', 'yield', 'lambda', 'pass', 'continue', 'break']
  const builtins = ['len', 'range', 'int', 'str', 'float', 'list', 'dict', 'set', 'tuple', 'print', 'type', 'isinstance', 'super', 'self', 'cls']
  
  keywords.forEach(keyword => {
    const regex = new RegExp(`\\b(${keyword})\\b`, 'g')
    result = result.replace(regex, '<span class="text-[#c586c0]">$1</span>')
  })
  
  builtins.forEach(builtin => {
    const regex = new RegExp(`\\b(${builtin})\\b`, 'g')
    result = result.replace(regex, '<span class="text-[#569cd6]">$1</span>')
  })
  
  result = result.replace(/(["'`])(?:(?!\1)[^\\]|\\.)*\1/g, '<span class="text-[#ce9178]">$&</span>')
  result = result.replace(/\b(\d+)\b/g, '<span class="text-[#b5cea8]">$1</span>')
  result = result.replace(/(#.*$)/gm, '<span class="text-[#6a9955]">$1</span>')
  result = result.replace(/\b([A-Z][a-zA-Z0-9_]*)\b/g, '<span class="text-[#4ec9b0]">$1</span>')
  
  return result
}

const getV2CodeDiff_Part1 = () => {
  const code = getV1FullCode()
  const quickSortIdx = code.findIndex(c => c.content.includes('def quick_sort(self):'))
  if (quickSortIdx !== -1) {
    code.splice(quickSortIdx + 1, 5,
      { content: '        """', diffType: 'removed' },
      { content: '        QuickSort main function', diffType: 'removed' },
      { content: '        Bug: no handling for empty or single-element array', diffType: 'removed' },
      { content: '        """', diffType: 'removed' },
      { content: '        self._quick_sort_helper(0, len(self.array))', diffType: 'removed' },
      { content: '        """', diffType: 'added' },
      { content: '        QuickSort main function', diffType: 'added' },
      { content: '        Fixed: added boundary checks for empty/single-element arrays', diffType: 'added' },
      { content: '        """', diffType: 'added' },
      { content: '        if len(self.array) <= 1:', diffType: 'added' },
      { content: '            return self.array', diffType: 'added' },
      { content: '        self._quick_sort_helper(0, len(self.array) - 1)', diffType: 'added' }
    )
  }
  return code
}

const getV2CodeDiff_Full = () => {
  const code = getV2CodeDiff_Part1()
  const initIdx = code.findIndex(c => c.content.includes('def __init__(self'))
  if (initIdx !== -1) {
    code.splice(initIdx + 1, 0,
      { content: '        # Add input validation', diffType: 'added' },
      { content: '        self._validate_array(arr)', diffType: 'added' }
    )
  }
  
  const classEndIdx = code.findIndex(c => c.content.includes('def main():'))
  if (classEndIdx !== -1) {
    code.splice(classEndIdx, 0,
      { content: '', diffType: 'added' },
      { content: '    def _validate_array(self, arr):', diffType: 'added' },
      { content: '        """Validate input array format"""', diffType: 'added' },
      { content: '        if arr is not None and not isinstance(arr, list):', diffType: 'added' },
      { content: '            raise TypeError("Input must be a list")', diffType: 'added' },
      { content: '        for item in (arr or []):', diffType: 'added' },
      { content: '            if not isinstance(item, (int, float)):', diffType: 'added' },
      { content: '                raise ValueError("Array elements must be numeric")', diffType: 'added' }
    )
  }
  return code
}

const getV2FullCode = () => {
  return getV2CodeDiff_Full().filter(c => c.diffType !== 'removed').map(c => ({ content: c.content }))
}

const getTerminalBuildLogs = () => {
  return [
    { text: '$ python -m py_compile array_operations.py', color: 'text-[#cccccc]' },
    { text: 'Syntax check passed.', color: 'text-[#3fb950]' },
    { text: '', color: 'text-[#cccccc]' },
    { text: '$ python array_operations.py', color: 'text-[#cccccc]' },
    { text: '==================================================', color: 'text-[#858585]' },
    { text: 'Python Array Operations Learning Module', color: 'text-[#cccccc]' },
    { text: '==================================================', color: 'text-[#858585]' },
    { text: '', color: 'text-[#cccccc]' },
    { text: 'Original array: [1, 2, 3, 4, 5]', color: 'text-[#cccccc]' },
    { text: 'Reversed array: [5, 4, 3, 2, 1]', color: 'text-[#3fb950]' },
    { text: '', color: 'text-[#cccccc]' },
    { text: 'Before sort: [64, 34, 25, 12, 22, 11, 90]', color: 'text-[#cccccc]' },
    { text: '[ERROR] IndexError: list index out of range', color: 'text-[#f85149]' },
    { text: '', color: 'text-[#cccccc]' },
    { text: '✗ QuickSort failed due to index boundary error.', color: 'text-[#f85149]' }
  ]
}

const getV2TerminalPrecheck = () => {
  return [
    { text: '$ python -m py_compile array_operations.py', color: 'text-[#cccccc]' },
    { text: 'Syntax check passed.', color: 'text-[#3fb950]' },
    { text: '', color: 'text-[#cccccc]' },
    { text: 'Validating boundary conditions...', color: 'text-[#858585]' },
    { text: '↳ Empty array handling: OK', color: 'text-[#3fb950]' },
    { text: '↳ Single element array: OK', color: 'text-[#3fb950]' },
    { text: '↳ QuickSort partition index: OK', color: 'text-[#3fb950]' },
    { text: '', color: 'text-[#cccccc]' },
    { text: '✓ Precheck completed. Code ready for acceptance.', color: 'text-[#4B8BBE]' }
  ]
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #424242;
  border: 3px solid #1e1e1e;
  border-radius: 6px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #4f4f4f;
}

.fade-in {
  animation: fadeIn 0.3s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}

.rich-text-content :deep(code) {
  background-color: #2d2d2d;
  padding: 0.15rem 0.3rem;
  border-radius: 3px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 0.9em;
  color: #e3e3e3;
}
</style>