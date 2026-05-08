<template>
  <div class="page-wrapper w-full min-h-screen">
    
    <div class="glass-card w-full h-full p-8 md:p-10 flex flex-col md:flex-row gap-10 z-10">
      
      <div class="flex-1 flex flex-col">
        <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: PLAN_UPLOAD</div>
        <h1 class="text-3xl font-bold mb-6 text-gray-800">方案文件上传</h1>
        
        <div class="bg-indigo-50/50 border border-indigo-100 rounded-2xl p-6 mb-6 shadow-sm flex-1">
          <h3 class="font-bold text-indigo-900 mb-4 flex items-center gap-2">
            <svg class="text-indigo-500" style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            上传具体要求
          </h3>
          <p class="text-gray-600 leading-relaxed whitespace-pre-wrap text-sm">{{ nodeConfig.uploadReq }}</p>
        </div>

        <div class="flex items-center gap-3 text-sm">
          <span class="text-gray-500">允许的文件格式：</span>
          <div class="flex gap-2">
            <span v-for="fmt in allowedFormats" :key="fmt" class="px-2 py-1 bg-gray-100 text-gray-600 font-bold rounded uppercase border border-gray-200">
              .{{ fmt }}
            </span>
          </div>
        </div>
      </div>

      <div class="flex-[1.2] flex flex-col justify-center relative">
        
        <div v-if="status === 'rejected'" class="mb-4 flex items-start gap-3 p-4 bg-red-50 border border-red-200 rounded-xl animate-fade-in shadow-sm">
           <svg class="text-red-500 mt-0.5" style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
           <div>
             <h4 class="text-sm font-bold text-red-700">方案被驳回打回</h4>
             <p class="text-xs text-red-600 mt-1">教师留言：请重新核对 SM4 接口文档规范，缺少密钥更新流程，请修改后重新上传。</p>
           </div>
        </div>

        <div 
          class="flex-1 border-2 border-dashed rounded-2xl flex flex-col items-center justify-center p-8 transition-all duration-300 relative overflow-hidden group"
          :class="[
            isDragging ? 'border-indigo-500 bg-indigo-50/50' : 'border-gray-300 bg-white/40 hover:border-indigo-400 hover:bg-white/60',
            status === 'success' ? 'border-green-400 bg-green-50/30' : ''
          ]"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="handleDrop"
        >
          <input type="file" ref="fileInput" class="hidden" :accept="acceptString" @change="handleFileSelect">

          <template v-if="status === 'idle' || status === 'rejected'">
            <div class="w-20 h-20 rounded-full bg-indigo-50 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300 shadow-inner">
              <svg class="text-indigo-400" style="width: 40px; height: 40px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" /></svg>
            </div>
            <h3 class="text-lg font-bold text-gray-700 mb-2">点击或将文件拖拽到此处</h3>
            <p class="text-sm text-gray-500 text-center">支持 {{ nodeConfig.format }} 格式<br>文件大小不超过 50MB</p>
            <button @click="$refs.fileInput.click()" class="mt-6 px-6 py-2.5 bg-white border border-gray-200 text-indigo-600 font-bold rounded-lg hover:border-indigo-300 hover:shadow-md transition-all">
              选择本地文件
            </button>
          </template>

          <template v-else-if="status === 'uploading'">
            <svg class="text-indigo-500 mb-4" style="width: 48px; height: 48px; animation: spin 1s linear infinite;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
            <h3 class="text-lg font-bold text-gray-700 mb-4">正在上传中...</h3>
            <div class="w-full max-w-xs bg-gray-200 rounded-full h-2">
              <div class="bg-indigo-500 h-2 rounded-full transition-all duration-300" :style="{ width: uploadProgress + '%' }"></div>
            </div>
            <span class="text-xs text-gray-500 mt-2">{{ uploadProgress }}%</span>
          </template>

          <template v-else-if="status === 'success'">
            <div class="w-20 h-20 rounded-full bg-green-100 flex items-center justify-center mb-6 shadow-inner animate-pop-in">
              <svg class="text-green-500" style="width: 40px; height: 40px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
            </div>
            <h3 class="text-lg font-bold text-gray-800 mb-2">文件上传成功</h3>
            <div class="flex items-center gap-2 px-4 py-2 bg-white border border-gray-100 rounded-lg shadow-sm mb-6">
               <svg class="text-gray-400" style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
               <span class="text-sm font-medium text-gray-700 truncate max-w-[200px]">{{ selectedFile?.name || '设计方案v1.pdf' }}</span>
            </div>
            <button @click="resetUpload" class="text-sm text-indigo-500 hover:text-indigo-600 underline">重新上传</button>
          </template>

        </div>

        <div class="mt-6">
          <button 
            class="hero-send-btn w-full justify-center text-base py-3.5 rounded-xl shadow-lg transition-all"
            :class="status === 'success' ? 'hover:shadow-indigo-500/40' : 'opacity-50 grayscale cursor-not-allowed'"
            :disabled="status !== 'success'"
          >
            完成上传，进入下一环节
            <svg style="width: 16px; height: 16px; margin-left: 4px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
          </button>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const nodeConfig = ref({
  format: 'pdf,docx',
  uploadReq: '1. 方案需包含 SM4 算法的系统架构图及核心数据流向图。\n2. 需详细说明接口封装的标准，包括参数类型、返回值及错误码定义。\n3. 文档排版清晰，需使用提供的标准化模板撰写。'
})

const allowedFormats = computed(() => nodeConfig.value.format.split(',').map(f => f.trim()))
const acceptString = computed(() => allowedFormats.value.map(f => `.${f}`).join(','))

const isDragging = ref(false)
const status = ref('idle') // idle, uploading, success, rejected
const uploadProgress = ref(0)
const selectedFile = ref(null)

// 模拟文件校验与上传
const processFile = (file) => {
  if (!file) return
  
  // 校验后缀名
  const ext = file.name.split('.').pop().toLowerCase()
  if (!allowedFormats.value.includes(ext)) {
    alert(`仅支持 ${nodeConfig.value.format} 格式的文件！`)
    return
  }

  selectedFile.value = file
  status.value = 'uploading'
  uploadProgress.value = 0

  // 模拟上传进度
  const timer = setInterval(() => {
    uploadProgress.value += Math.floor(Math.random() * 20) + 5
    if (uploadProgress.value >= 100) {
      uploadProgress.value = 100
      clearInterval(timer)
      setTimeout(() => { status.value = 'success' }, 300)
    }
  }, 200)
}

const handleFileSelect = (e) => {
  processFile(e.target.files[0])
}

const handleDrop = (e) => {
  isDragging.value = false
  processFile(e.dataTransfer.files[0])
}

const resetUpload = () => {
  status.value = 'idle'
  selectedFile.value = null
  uploadProgress.value = 0
}

// 开发调试用：可以暴露一个方法模拟被教师驳回
const mockReject = () => {
  status.value = 'rejected'
}
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.4s ease-out forwards; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-pop-in { animation: popIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards; }
@keyframes popIn {
  0% { opacity: 0; transform: scale(0.5); }
  100% { opacity: 1; transform: scale(1); }
}
</style>