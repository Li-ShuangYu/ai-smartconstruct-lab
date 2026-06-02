<template>
  <div class="page-wrapper flex items-center justify-center w-full h-full min-h-[calc(100vh-100px)]">
    
    <div class="glass-card w-full max-w-3xl p-8 md:p-12 flex flex-col relative z-10">
      
      <div class="flex justify-between items-start mb-8 pb-6 border-b border-gray-200/50">
        <div>
          <div class="mb-2 text-xs font-bold text-indigo-400 tracking-widest uppercase flex items-center gap-2">
            <svg style="width: 14px; height: 14px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" /></svg>
            Node: TASK_DISTRIBUTE
          </div>
          <h1 class="text-3xl font-bold text-gray-800">{{ taskConfig.taskTitle }}</h1>
        </div>
        <div class="text-right">
          <span class="inline-block px-3 py-1 bg-indigo-50 border border-indigo-100 text-indigo-600 rounded-md text-xs font-bold tracking-wider">
            WORK ORDER
          </span>
        </div>
      </div>

      <div class="flex-1 bg-white/60 border border-gray-100 rounded-xl p-8 mb-8 shadow-sm relative overflow-hidden">
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 opacity-5 pointer-events-none">
          <svg style="width: 300px; height: 300px; flex-shrink: 0;" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2L2 7l10 5 10-5-10-5zm0 7.5l-10-5v10.5l10 5 10-5V4.5l-10 5z"/></svg>
        </div>

        <h3 class="text-lg font-bold text-gray-800 mb-4 flex items-center gap-2">
          <span class="w-1.5 h-6 bg-indigo-500 rounded-full"></span>
          任务背景与说明
        </h3>
        <p class="text-gray-600 leading-relaxed mb-6 whitespace-pre-wrap">{{ taskConfig.taskDesc }}</p>

        <h3 class="text-lg font-bold text-gray-800 mb-4 flex items-center gap-2">
          <span class="w-1.5 h-6 bg-purple-500 rounded-full"></span>
          具体执行要求
        </h3>
        <ul class="space-y-3 text-gray-600">
          <li v-for="(req, index) in taskConfig.requirements" :key="index" class="flex items-start gap-3">
            <svg class="text-indigo-400 mt-1" style="width: 16px; height: 16px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
            <span class="leading-relaxed">{{ req }}</span>
          </li>
        </ul>
      </div>

      <div class="flex flex-col items-center">
        <div v-if="isReceived" class="mb-4 flex items-center gap-2 text-sm text-green-600 bg-green-50 px-6 py-2 rounded-full border border-green-100 animate-fade-in shadow-sm">
          <svg style="width: 18px; height: 18px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
          <span class="font-bold">任务已确认查收，系统已在侧边栏生成专属任务书入口。</span>
        </div>
        
        <button 
          class="hero-send-btn w-full md:w-2/3 justify-center text-lg py-4 rounded-xl shadow-lg transition-all duration-300"
          :class="isReceived ? 'opacity-50 cursor-not-allowed bg-green-600 shadow-none' : 'hover:-translate-y-1 hover:shadow-indigo-500/40'"
          @click="handleReceive"
          :disabled="isReceived"
        >
          <svg v-if="!isReceived" style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122" /></svg>
          <svg v-else style="width: 20px; height: 20px; flex-shrink: 0;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
          {{ isReceived ? '已查收，可进入下一步' : '我已仔细阅读，确认接收任务' }}
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['complete'])

// 模拟通过编排注入的任务配置数据
const taskConfig = ref({
  taskTitle: 'Python算法实践项目 — 学生成绩管理系统',
  taskDesc: `本次实训进入工程实践阶段。你需要运用所学数据结构和算法知识，使用 Python 实现一个「学生成绩管理系统」，完成学生信息存储、成绩排序分析、数据持久化和命令行交互等核心功能。

项目目标：
1. 综合运用数组、链表、字典等数据结构
2. 实践快速排序/归并排序等算法
3. 掌握文件 I/O、JSON 序列化等技能
4. 培养工程化编码习惯（函数拆分、异常处理、注释规范）`,
  requirements: [
    '使用字典存储学生基本信息（学号、姓名、成绩），支持增删改查操作，需处理重复学号、空值等异常场景',
    '实现至少两种排序算法（如快速排序和归并排序）对成绩进行排序，输出排序结果并对比算法耗时',
    '计算平均分、最高分、最低分、方差等统计指标，以格式化表格输出',
    '将学生数据持久化保存到 JSON 文件，支持程序重启后自动加载历史数据',
    '实现菜单驱动的命令行交互界面，包含添加、删除、修改、查询、排序、统计、退出等选项',
    '扩展功能（选做）：使用 matplotlib 生成成绩分布柱状图和饼图'
  ]
})

const isReceived = ref(false)

const handleReceive = () => {
  if (isReceived.value) return
  isReceived.value = true
  // 查收后自动完成，进入下一节点
  emit('complete')
}
</script>

<style scoped>
.animate-fade-in { animation: fadeIn 0.4s ease-out forwards; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
