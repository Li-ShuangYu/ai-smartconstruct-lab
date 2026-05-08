<template>
  <div class="page-wrapper w-full min-h-screen">
    
    <div class="glass-card w-full h-full p-6 flex flex-col z-10">lg:flex-row gap-6 z-10 h-[850px]">
      
      <div class="w-full lg:w-[380px] flex flex-col h-full bg-white/60 border border-gray-100 rounded-2xl p-6 shadow-sm overflow-y-auto custom-scrollbar">
        <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: CODING_CLASS</div>
        <h1 class="text-2xl font-bold mb-4 text-gradient-primary">{{ taskConfig.title }}</h1>
        
        <div class="prose prose-sm text-gray-600 mb-6 flex-1 space-y-4">
          <p class="font-medium text-gray-800">任务背景：</p>
          <p>{{ taskConfig.desc }}</p>
          
          <p class="font-medium text-gray-800 mt-4">编码要求：</p>
          <ul class="list-disc pl-4 space-y-2 text-sm">
            <li v-for="(req, index) in taskConfig.requirements" :key="index">{{ req }}</li>
          </ul>
        </div>

        <div class="pt-4 border-t border-indigo-100/50 mt-auto">
          <div v-if="isPassed" class="mb-4 flex items-center gap-2 text-sm text-green-600 bg-green-50 p-3 rounded-lg border border-green-100 animate-fade-in">
            <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <span>测试用例全部通过，满足下一步条件</span>
          </div>
          <button 
            class="hero-send-btn w-full justify-center text-base py-3.5 rounded-xl shadow-lg transition-all"
            :class="{'opacity-50 cursor-not-allowed': codeStatus === 'running'}"
            @click="submitCode"
            :disabled="codeStatus === 'running'"
          >
            <svg v-if="codeStatus === 'running'" style="width: 18px; height: 18px; animation: spin 1s linear infinite;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
            <svg v-else style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
            {{ codeStatus === 'running' ? '正在验证中...' : (isPassed ? '进入下一环节' : '提交验证') }}
          </button>
        </div>
      </div>

      <div class="flex-1 flex flex-col gap-4 h-full min-w-0">
        
        <div class="flex-[2] bg-[#1e1e1e] rounded-2xl border border-gray-700/50 flex flex-col overflow-hidden shadow-2xl relative group">
          <div class="h-10 bg-[#2d2d2d] flex items-center px-4 justify-between border-b border-gray-700/50">
            <div class="flex gap-2">
              <div class="w-3 h-3 rounded-full bg-red-500"></div>
              <div class="w-3 h-3 rounded-full bg-yellow-500"></div>
              <div class="w-3 h-3 rounded-full bg-green-500"></div>
            </div>
            <div class="text-gray-400 text-xs font-mono flex items-center gap-2">
               <svg style="width: 14px; height: 14px; flex-shrink: 0; color: #3b82f6;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" /></svg>
               sm4_round_function.py
            </div>
            <div class="w-12"></div> </div>
          
          <div class="flex-1 relative flex">
            <div class="w-12 bg-[#1e1e1e] border-r border-gray-700/50 flex flex-col items-end py-4 pr-2 text-gray-600 font-mono text-sm select-none">
              <span v-for="n in 15" :key="n">{{ n }}</span>
            </div>
            <textarea 
              v-model="codeContent" 
              class="flex-1 bg-transparent text-[#d4d4d4] font-mono text-sm p-4 outline-none resize-none custom-scrollbar leading-relaxed"
              spellcheck="false"
            ></textarea>
          </div>
          
          <button @click="runCode" class="absolute bottom-6 right-6 bg-indigo-600 hover:bg-indigo-500 text-white p-3 rounded-full shadow-lg transition-transform hover:scale-105" title="运行测试">
            <svg style="width: 20px; height: 20px; flex-shrink: 0;" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z" clip-rule="evenodd" />
            <!-- </path> -->
          </svg>
          </button>
        </div>

        <div class="flex-[1] bg-[#0d0d0d] rounded-2xl border border-gray-800 flex flex-col overflow-hidden shadow-inner">
          <div class="h-8 bg-[#1a1a1a] flex items-center px-4 text-xs font-mono text-gray-400 uppercase tracking-wider">
            Terminal / Console
          </div>
          <div class="flex-1 p-4 font-mono text-sm overflow-y-auto custom-scrollbar">
            <div v-if="!consoleOutput" class="text-gray-600 italic">等待执行...</div>
            <div v-else v-html="consoleOutput"></div>
          </div>
        </div>

      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const taskConfig = ref({
  title: 'SM4 轮函数非线性变换',
  desc: '在 SM4 算法中，轮函数 F 是核心。请补全轮函数中的非线性变换 τ (Tau) 的代码。',
  requirements: [
    '接收一个 32 位的输入 A。',
    '将 A 拆分为 4 个 8 位的字节。',
    '查 S 盒 (Sbox) 得到对应的 4 个输出字节。',
    '将 4 个输出字节拼接成 32 位的字并返回。'
  ]
})

const codeContent = ref(`def tau_transform(a):
    # a 是一个 32 位的整数
    # 请在此处编写你的代码实现 S 盒替换
    
    
    return result
`)

const consoleOutput = ref('')
const codeStatus = ref('idle') // idle, running
const isPassed = ref(false)

const runCode = () => {
  consoleOutput.value = `<span class="text-blue-400">> root@student-env:~$ python run_tests.py</span><br>`
  setTimeout(() => {
    consoleOutput.value += `<span class="text-yellow-400">[执行日志]</span> 正在编译并执行...<br>`
    if (codeContent.value.includes('return result') && codeContent.value.length < 150) {
      consoleOutput.value += `<span class="text-red-500">[ERROR]</span> 测试用例 1 失败：返回结果未定义或类型错误。<br>`
    } else {
      consoleOutput.value += `<span class="text-green-500">[SUCCESS]</span> 模拟运行通过，输出值符合预期。<br>`
    }
  }, 600)
}

const submitCode = () => {
  if (isPassed.value) {
    alert("流转到下一个节点")
    return
  }
  
  codeStatus.value = 'running'
  consoleOutput.value = `<span class="text-indigo-400">> 正在向服务器提交校验...</span><br>`
  
  setTimeout(() => {
    codeStatus.value = 'idle'
    // 模拟一种通过情况
    if (codeContent.value.length > 100) {
      isPassed.value = true
      consoleOutput.value += `<span class="text-green-500 font-bold">✔ 全部 5 个测试用例通过！</span><br>`
    } else {
      consoleOutput.value += `<span class="text-red-500">✘ 校验失败：逻辑未完成。</span><br>`
    }
  }, 1500)
}
</script>

<style scoped>
/* 针对深色背景定制的滚动条 */
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 4px; }
.custom-scrollbar:hover::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.2); }

.animate-fade-in { animation: fadeIn 0.4s ease-out forwards; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>