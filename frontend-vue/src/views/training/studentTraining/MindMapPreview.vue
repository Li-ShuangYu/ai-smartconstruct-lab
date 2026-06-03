﻿﻿﻿﻿﻿<template>
  <div style="height: calc(100vh - 64px); min-height: 500px;">
    <div class="glass-card w-full h-full p-3 flex flex-col z-10 relative overflow-hidden">
      
      <div class="flex justify-between items-center mb-3 shrink-0">
        <div>
          <div class="mb-1 text-xs font-bold text-indigo-400 tracking-widest uppercase">Node: MINDMAP_PREVIEW</div>
          <h2 class="text-2xl font-bold text-gray-800">思维导图预习与自评</h2>
        </div>
        
        <div class="flex items-center gap-6">
          <div class="flex items-center gap-3 bg-white/50 px-4 py-2 rounded-xl border border-indigo-100 shadow-sm">
            <div class="relative w-10 h-10">
              <svg class="w-full h-full" viewBox="0 0 36 36">
                <path class="text-gray-200" stroke-width="3" stroke="currentColor" fill="transparent" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
                <path class="text-indigo-500 transition-all duration-500" stroke-width="3" :stroke-dasharray="`${evalProgress}, 100`" stroke-linecap="round" stroke="currentColor" fill="transparent" d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" />
              </svg>
              <div class="absolute inset-0 flex items-center justify-center text-[10px] font-bold text-indigo-600">{{ Math.round(evalProgress) }}%</div>
            </div>
            <div class="text-xs">
              <p class="text-gray-500">评估进度</p>
              <p class="font-bold text-gray-700">{{ evaluatedCount }} / {{ totalNodes }} 核心节点</p>
            </div>
          </div>

          <button
            @click="handleSubmit"
            class="hero-send-btn px-8 py-3 rounded-xl shadow-lg transition-all flex items-center gap-2 hover:shadow-indigo-500/30"
          >
            完成预习提交
            <svg style="width: 18px; height: 18px;" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" /></svg>
          </button>
        </div>
      </div>

      <div class="flex-1 flex gap-6 min-h-0">

        <div class="flex-[3] bg-gray-50/50 rounded-2xl border border-gray-200/60 shadow-inner overflow-hidden relative group" ref="canvasWrapperRef">
          <div class="absolute inset-0 opacity-[0.03] pointer-events-none" style="background-image: radial-gradient(#6366f1 1px, transparent 1px); background-size: 30px 30px;"></div>
          
          <div ref="mindMapContainer" class="w-full h-full outline-none"></div>

          <div class="absolute bottom-4 left-4 flex gap-2 z-10">
            <button @click="handleZoomIn" class="p-2 bg-white rounded-lg border border-gray-200 shadow-sm hover:bg-indigo-50 text-gray-600 transition-colors" title="放大">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg>
            </button>
            <button @click="handleZoomOut" class="p-2 bg-white rounded-lg border border-gray-200 shadow-sm hover:bg-indigo-50 text-gray-600 transition-colors" title="缩小">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" /></svg>
            </button>
            <button @click="handleFitView" class="p-2 bg-white rounded-lg border border-gray-200 shadow-sm hover:bg-indigo-50 text-gray-600 transition-colors" title="适应屏幕">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4h16v16H4V4z" /></svg>
            </button>
          </div>
        </div>

        <div class="flex-[1] min-w-[320px] max-w-[400px] flex flex-col gap-4">
          <div class="flex-1 bg-white/60 border border-gray-200/60 rounded-2xl p-6 shadow-sm flex flex-col overflow-hidden">
            <div v-if="activeNode" class="h-full flex flex-col">
              <div class="shrink-0 mb-6">
                <span class="px-2 py-1 bg-indigo-100 text-indigo-600 text-[10px] font-bold rounded uppercase tracking-wider">Knowledge Point</span>
                <h3 class="text-xl font-bold text-gray-800 mt-2">{{ activeNode.label }}</h3>
              </div>
              
              <div class="flex-1 overflow-y-auto custom-scrollbar pr-2 mb-6">
                <div class="bg-indigo-50/30 border border-indigo-100/50 rounded-xl p-4 mb-4">
                  <h4 class="text-xs font-bold text-indigo-500 mb-2 flex items-center gap-1">
                    <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                    AI 知识解析
                  </h4>
                  <p class="text-sm text-gray-600 leading-relaxed italic">
                    {{ activeNode.desc || '选中节点以查看详细解析与学习建议。' }}
                  </p>
                </div>
                
                <div class="space-y-4">
                  <h4 class="text-xs font-bold text-gray-400 uppercase tracking-widest">学习要点</h4>
                  <ul class="space-y-2">
                    <li v-for="(item, idx) in activeNode.points" :key="idx" class="flex gap-2 text-sm text-gray-600">
                      <span class="text-indigo-400 font-bold">•</span> {{ item }}
                    </li>
                  </ul>
                </div>
              </div>

              <div class="shrink-0 pt-6 border-t border-gray-100">
                <p class="text-xs font-bold text-gray-500 mb-4 text-center uppercase tracking-tighter">评估您对此知识点的掌握程度</p>
                <div class="grid grid-cols-3 gap-3">
                  <button 
                    v-for="(label, key) in difficultyMap" :key="key"
                    @click="markDifficulty(key)"
                    class="flex flex-col items-center gap-2 py-3 rounded-xl border transition-all"
                    :class="[
                      activeNode.difficulty === key 
                      ? `bg-${getColor(key)}-50 border-${getColor(key)}-200 text-${getColor(key)}-600 shadow-sm scale-[1.02]` 
                      : 'bg-white border-gray-100 text-gray-400 hover:border-gray-200'
                    ]"
                  >
                    <span class="text-lg">{{ getEmoji(key) }}</span>
                    <span class="text-xs font-bold">{{ label }}</span>
                  </button>
                </div>
              </div>
            </div>

            <div v-else class="h-full flex flex-col items-center justify-center text-center">
              <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mb-4 text-gray-300">
                <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 15l-2 5L9 9l11 4-5 2zm0 0l5 5M7.188 2.239l.777 2.897M5.136 7.965l-2.898-.777M13.95 4.05l-2.122 2.122m-5.657 5.656l-2.12 2.122" /></svg>
              </div>
              <p class="text-gray-400 text-sm">请在左侧思维导图中<br/>点击一个知识点进行预习</p>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import MindMap from 'simple-mind-map'

const emit = defineEmits(['complete'])

const difficultyMap = { 'easy': '容易', 'medium': '一般', 'hard': '困难' }

// 50+ 节点多层级知识图谱数据
const rootNode = ref({
  id: 'root',
  label: 'Python 算法知识体系',
  children: [
    {
      id: 'n1', label: '数据结构基础',
      desc: '数据结构是计算机存储、组织数据的方式。Python 内置了丰富的数据结构，理解它们的底层实现是编写高效代码的基础。',
      points: ['数据结构的定义与分类', 'Python内置数据结构概览', '时间复杂度与空间复杂度'],
      difficulty: null,
      children: [
        {
          id: 'n1-1', label: '数组与列表',
          desc: 'Python 的 list 是动态数组实现，支持 O(1) 随机访问。当容量不足时自动扩容（约 1.125 倍），理解这一机制对性能优化至关重要。',
          points: ['动态数组扩容机制', '索引与切片操作', '列表推导式'],
          difficulty: null,
          children: [
            { id: 'n1-1-1', label: '连续内存存储', desc: '数组在内存中占用连续空间，CPU 缓存友好，支持高速迭代访问。', points: ['Cache Line 对齐', '内存局部性原理', '物理 vs 虚拟内存'], difficulty: null },
            { id: 'n1-1-2', label: 'list 扩容因子', desc: 'PyListObject 的扩容策略是过度分配，避免频繁 realloc。', points: ['扩容倍数 1.125', '内存碎片控制', '摊销分析'], difficulty: null },
            { id: 'n1-1-3', label: '浅拷贝与深拷贝', desc: 'list 的切片和 copy() 是浅拷贝，deepcopy() 递归复制所有嵌套对象。', points: ['引用语义', '共享 vs 独立', '递归深拷贝风险'], difficulty: null }
          ]
        },
        {
          id: 'n1-2', label: '链表',
          desc: '链表由节点组成，每个节点包含数据和指向下一节点的指针。链表的优势在于插入和删除操作只需 O(1) 时间。',
          points: ['单向链表', '双向链表', '循环链表'],
          difficulty: null,
          children: [
            { id: 'n1-2-1', label: '单向链表实现', desc: '每个节点只保存指向下一个节点的引用，尾部节点指向 None。', points: ['Node 类定义', '遍历与查找', '头部插入 O(1)'], difficulty: null },
            { id: 'n1-2-2', label: '双向链表实现', desc: '每个节点同时保存 prev 和 next 两个引用，支持双向遍历。', points: ['prev/next 指针', '尾部删除优化', 'LRU 缓存实现'], difficulty: null }
          ]
        },
        {
          id: 'n1-3', label: '栈与队列',
          desc: '栈是后进先出（LIFO）结构，队列是先进先出（FIFO）结构。Python 的 list 和 collections.deque 分别适合这两种场景。',
          points: ['LIFO vs FIFO 特性', 'list 作为栈', 'deque 作为队列'],
          difficulty: null,
          children: [
            { id: 'n1-3-1', label: '栈的经典应用', desc: '括号匹配、函数调用栈、撤销操作、深度优先搜索都用到栈。', points: ['括号匹配算法', '调用栈帧', 'DFS 非递归实现'], difficulty: null },
            { id: 'n1-3-2', label: '队列的经典应用', desc: 'BFS、消息队列、任务调度、滑动窗口等场景广泛使用队列。', points: ['BFS 层次遍历', '生产者消费者', '优先队列与堆'], difficulty: null }
          ]
        }
      ]
    },
    {
      id: 'n2', label: '高级数据结构',
      desc: '在掌握基础数据结构后，树、堆、哈希表等高级结构为解决复杂问题提供了更强大的工具。',
      points: ['树的遍历与递归', '堆与优先队列', '哈希表与冲突解决'],
      difficulty: null,
      children: [
        {
          id: 'n2-1', label: '二叉树',
          desc: '二叉树是每个节点最多有两个子节点的树结构，是理解更复杂树结构的基础。',
          points: ['前/中/后序遍历', '层序遍历', '二叉搜索树 BST'],
          difficulty: null,
          children: [
            { id: 'n2-1-1', label: '前序遍历', desc: '根→左→右 的顺序遍历，可用于复制树结构。', points: ['递归实现', '迭代实现（栈）', 'Morris 遍历'], difficulty: null },
            { id: 'n2-1-2', label: '中序遍历', desc: '左→根→右 的顺序遍历，在 BST 中得到有序序列。', points: ['BST 有序输出', '递归实现', '迭代实现'], difficulty: null },
            { id: 'n2-1-3', label: '后序遍历', desc: '左→右→根 的顺序遍历，可用于删除或计算树的高度。', points: ['计算树深度', '递归实现', '应用场景'], difficulty: null },
            { id: 'n2-1-4', label: '层序遍历', desc: '从上到下、从左到右逐层访问，使用队列实现 BFS。', points: ['队列辅助', '逐层输出', 'Zigzag 遍历'], difficulty: null }
          ]
        },
        {
          id: 'n2-2', label: '堆',
          desc: '堆是一种特殊的完全二叉树，常用于实现优先队列。Python 的 heapq 模块提供了最小堆实现。',
          points: ['大根堆 vs 小根堆', '堆化操作', '堆排序'],
          difficulty: null,
          children: [
            { id: 'n2-2-1', label: '堆的存储结构', desc: '堆通常用数组存储，父节点与子节点的索引关系可通过公式计算。', points: ['数组表示法', 'parent = (i-1)//2', '子节点 2i+1/2i+2'], difficulty: null },
            { id: 'n2-2-2', label: '堆排序算法', desc: '利用堆结构进行排序：建堆 O(n)，每次取出堆顶 O(logn)，总 O(nlogn)。', points: ['建堆过程', '下沉与上浮', '不稳定性分析'], difficulty: null },
            { id: 'n2-2-3', label: 'Top K 问题', desc: '维护大小为 K 的小根堆，可以高效解决 Top K 问题。', points: ['海量数据筛选', '时间复杂度 O(nlogk)', '空间优化'], difficulty: null }
          ]
        },
        {
          id: 'n2-3', label: '哈希表',
          desc: '哈希表通过哈希函数将键映射到存储位置，实现平均 O(1) 的查找性能。Python 的 dict 是高度优化的哈希表实现。',
          points: ['哈希函数', '冲突解决', '负载因子与扩容'],
          difficulty: null,
          children: [
            { id: 'n2-3-1', label: '哈希冲突处理', desc: '常见的冲突解决策略：链地址法、开放地址法（线性探测/二次探测）。', points: ['链地址法实现', '线性探测', '二次探测与双重哈希'], difficulty: null },
            { id: 'n2-3-2', label: 'dict 底层实现', desc: 'Python 3.6+ 的 dict 采用紧凑哈希表实现，节省约 50% 内存。', points: ['Indices + Entries', '紧凑存储', '插入顺序保留'], difficulty: null },
            { id: 'n2-3-3', label: '集合 set 原理', desc: 'set 与 dict 共享底层实现，只存储键不存储值。', points: ['哈希集合', '并交差运算', '去重与成员检测'], difficulty: null }
          ]
        }
      ]
    },
    {
      id: 'n3', label: '排序算法',
      desc: '排序是计算机科学中最基础的算法之一，不同的排序算法在时间复杂度、空间复杂度和稳定性上各有优劣。',
      points: ['O(n²) 排序', 'O(nlogn) 排序', 'O(n) 排序', '排序稳定性'],
      difficulty: null,
      children: [
        {
          id: 'n3-1', label: 'O(n²) 排序',
          desc: '冒泡、选择、插入三种基础排序算法，适合小规模数据（n < 1000）。',
          points: ['冒泡排序', '选择排序', '插入排序'],
          difficulty: null,
          children: [
            { id: 'n3-1-1', label: '冒泡排序', desc: '通过相邻元素两两比较和交换，将最大元素逐渐"冒泡"到末尾。', points: ['比较相邻元素', '提前终止优化', '鸡尾酒排序'], difficulty: null },
            { id: 'n3-1-2', label: '选择排序', desc: '每轮选择剩余元素中的最小值，放到已排序序列的末尾。', points: ['不稳定排序', '比较次数固定', '原地排序'], difficulty: null },
            { id: 'n3-1-3', label: '插入排序', desc: '将未排序元素插入到已排序序列的正确位置，对于几乎有序的数据效率极高。', points: ['稳定排序', '在线算法', '近乎有序 O(n)'], difficulty: null }
          ]
        },
        {
          id: 'n3-2', label: 'O(nlogn) 排序',
          desc: '快速排序和归并排序是实践中最高效的通用排序算法。',
          points: ['快速排序分治思想', '归并排序', '比较与选择'],
          difficulty: null,
          children: [
            { id: 'n3-2-1', label: '快速排序', desc: '采用分治策略，选择 pivot 将数组分为两部分递归排序。平均 O(nlogn)，最坏 O(n²)。', points: ['pivot 选择策略', '原地分区', '三路快排优化'], difficulty: null },
            { id: 'n3-2-2', label: '归并排序', desc: '将数组递归分成两半分别排序再合并，稳定且最坏 O(nlogn)，需要 O(n) 额外空间。', points: ['分治合并', '合并过程', '外部排序应用'], difficulty: null },
            { id: 'n3-2-3', label: '希尔排序', desc: '插入排序的改进版，通过间隔分组插入排序逐步减少间隔，最后变为普通插入排序。', points: ['间隔序列', '分组插入', '时间复杂度争议'], difficulty: null }
          ]
        },
        {
          id: 'n3-3', label: 'O(n) 排序',
          desc: '计数排序、桶排序、基数排序通过利用数据的特殊性质，在特定条件下可以达到线性时间复杂度。',
          points: ['计数排序条件', '桶排序分布', '基数排序逐位'],
          difficulty: null,
          children: [
            { id: 'n3-3-1', label: '计数排序', desc: '适用于数据范围较小的整数排序，统计每个值出现的次数后直接输出。', points: ['值域范围要求', '稳定版本实现', '空间换时间'], difficulty: null },
            { id: 'n3-3-2', label: '桶排序', desc: '将数据分到有限数量的桶中，每个桶内分别排序再合并。', points: ['桶的数量选择', '桶内排序策略', '均匀分布最优'], difficulty: null }
          ]
        }
      ]
    },
    {
      id: 'n4', label: '搜索算法',
      desc: '搜索算法解决在数据集合中查找特定元素的问题，从基础的线性查找到高效的二分查找和启发式搜索。',
      points: ['线性查找', '二分查找', '深度优先搜索', '广度优先搜索'],
      difficulty: null,
      children: [
        {
          id: 'n4-1', label: '二分查找',
          desc: '在有序数组中通过不断缩小搜索范围，将线性查找的 O(n) 优化到 O(logn)。',
          points: ['有序数组前提', '边界条件处理', '变种问题'],
          difficulty: null,
          children: [
            { id: 'n4-1-1', label: '经典二分实现', desc: '维护 left/right 指针，每次取中间值与 target 比较。', points: ['循环不变量', 'mid 计算防溢出', '三种模板'], difficulty: null },
            { id: 'n4-1-2', label: '二分查找变种', desc: '查找第一个/最后一个等于 target 的位置、查找最接近 target 的值。', points: ['左边界', '右边界', '旋转数组搜索'], difficulty: null }
          ]
        },
        {
          id: 'n4-2', label: 'DFS 深度优先搜索',
          desc: '沿着一条路径尽可能深入地搜索，直到无法继续再回溯。常用于遍历树和图、求解路径问题。',
          points: ['递归实现', '回溯思想', '剪枝优化'],
          difficulty: null,
          children: [
            { id: 'n4-2-1', label: '回溯算法', desc: 'DFS 的一种形式，在每一步尝试所有选择，不满足条件时回溯。', points: ['选择→递归→撤销', '全排列问题', 'N 皇后问题'], difficulty: null },
            { id: 'n4-2-2', label: '记忆化搜索', desc: '在 DFS 中缓存已计算过的状态，避免重复计算，是动态规划的一种实现方式。', points: ['缓存中间结果', '自顶向下 DP', '斐波那契优化'], difficulty: null }
          ]
        },
        {
          id: 'n4-3', label: 'BFS 广度优先搜索',
          desc: '逐层遍历搜索空间，保证找到的路径是最短路径。使用队列作为辅助数据结构。',
          points: ['队列辅助遍历', '最短路径保证', '双向 BFS'],
          difficulty: null,
          children: [
            { id: 'n4-3-1', label: 'BFS 最短路径', desc: '在无权图中，BFS 首次访问到目标节点的路径就是最短路径。', points: ['层数记录', '路径重建', '网格最短路径'], difficulty: null },
            { id: 'n4-3-2', label: '双向 BFS', desc: '从起点和终点同时进行 BFS，当两个搜索区域相遇时找到最短路径。', points: ['交替扩展', '相遇判断', '搜索空间减半'], difficulty: null }
          ]
        }
      ]
    },
    {
      id: 'n5', label: '算法思想与范式',
      desc: '掌握经典的算法设计思想，能够帮助你在面对新问题时快速找到解决方案的方向。',
      points: ['递归与分治', '动态规划', '贪心算法'],
      difficulty: null,
      children: [
        {
          id: 'n5-1', label: '递归与分治',
          desc: '将大问题分解为相同形式的子问题，递归求解子问题后合并结果。递归三要素：终止条件、递推公式、返回值。',
          points: ['递归三要素', '分治策略', '主定理'],
          difficulty: null,
          children: [
            { id: 'n5-1-1', label: '主定理分析', desc: '用主定理（Master Theorem）可以快速分析分治算法的时间复杂度。', points: ['T(n)=aT(n/b)+f(n)', '三种情况', '归并排序分析'], difficulty: null },
            { id: 'n5-1-2', label: '分治经典案例', desc: '归并排序、快速排序、最大子数组和、最近点对问题。', points: ['最大子数组和', '最近点对', '矩阵乘法 Strassen'], difficulty: null }
          ]
        },
        {
          id: 'n5-2', label: '动态规划',
          desc: '通过将问题分解为重叠子问题并存储子问题的解来避免重复计算，适合求解最优化问题。',
          points: ['最优子结构', '状态转移方程', '重叠子问题'],
          difficulty: null,
          children: [
            { id: 'n5-2-1', label: '背包问题', desc: '0-1 背包、完全背包、多重背包，是理解 DP 的核心模型。', points: ['0-1 背包递推', '空间压缩优化', '完全背包'], difficulty: null },
            { id: 'n5-2-2', label: '区间 DP', desc: '解决区间上的最优化问题，如矩阵链乘法、回文分割等。', points: ['区间长度遍历', '状态划分', '石子合并'], difficulty: null },
            { id: 'n5-2-3', label: '最长公共子序列', desc: 'LCS 是字符串 DP 的经典问题，广泛应用于 diff 算法和生物信息学。', points: ['二维 DP 表', '回溯构造解', '空间优化'], difficulty: null },
            { id: 'n5-2-4', label: '状态压缩 DP', desc: '当状态可以用二进制位表示时，通过位运算高效枚举子集。', points: ['位掩码表示', '子集枚举', '旅行商问题 TSP'], difficulty: null }
          ]
        },
        {
          id: 'n5-3', label: '贪心算法',
          desc: '每一步都做出当前看起来最优的选择，希望最终得到全局最优解。局部最优不一定全局最优。',
          points: ['贪心选择性质', '最优子结构', '正确性证明'],
          difficulty: null,
          children: [
            { id: 'n5-3-1', label: '区间调度问题', desc: '选择最多的互不重叠的区间，按结束时间排序的贪心策略可得到最优解。', points: ['结束时间排序', '贪心选择证明', '变种问题'], difficulty: null },
            { id: 'n5-3-2', label: '哈夫曼编码', desc: '数据压缩算法，通过构建哈夫曼树为出现频率高的字符分配短编码。', points: ['最小堆构建', '前缀编码', '压缩率分析'], difficulty: null },
            { id: 'n5-3-3', label: '最短路径 Dijkstra', desc: '带权图的最短路径算法，每次选择距离最小的未处理节点。', points: ['松弛操作', '优先队列优化', '负权边限制'], difficulty: null }
          ]
        }
      ]
    },
    {
      id: 'n6', label: 'Python 实战技巧',
      desc: '掌握 Python 特有的编程技巧和标准库工具，让算法实现更简洁高效。',
      points: ['标准库高效工具', '函数式编程', '性能优化'],
      difficulty: null,
      children: [
        { id: 'n6-1', label: 'itertools 模块', desc: 'itertools 提供了高效的迭代器工具，如排列组合、无限迭代、分组等。', points: ['permutations/combinations', 'chain/groupby', 'cycle/islice'], difficulty: null },
        { id: 'n6-2', label: 'collections 模块', desc: '提供了 deque、Counter、defaultdict、OrderedDict 等常用数据结构。', points: ['Counter 计数', 'defaultdict 默认值', 'deque 双端队列'], difficulty: null },
        { id: 'n6-3', label: 'functools 函数工具', desc: 'lru_cache 自动缓存函数结果，reduce 实现累积计算，partial 固定参数。', points: ['lru_cache 缓存', 'reduce 归约', 'partial 偏函数'], difficulty: null },
        { id: 'n6-4', label: '性能分析工具', desc: 'timeit 精确测量代码耗时，cProfile 分析函数调用统计，memory_profiler 追踪内存。', points: ['timeit 微基准', 'cProfile 热点分析', '内存优化技巧'], difficulty: null }
      ]
    }
  ]
})

const activeNode = ref(null)
const mindMapContainer = ref(null)
const canvasWrapperRef = ref(null)
let mindMapInstance = null
let resizeObserver = null

// 获取所有核心节点用于计算进度
const allNodes = computed(() => {
  const result = []
  const traverse = (node) => {
    if (node.id !== 'root') result.push(node)
    if (node.children) node.children.forEach(traverse)
  }
  traverse(rootNode.value)
  return result
})

const totalNodes = computed(() => allNodes.value.length)
const evaluatedCount = computed(() => allNodes.value.filter(n => n.difficulty).length)
const evalProgress = computed(() => (evaluatedCount.value / totalNodes.value) * 100)
const isAllEvaluated = computed(() => evaluatedCount.value === totalNodes.value)

// 转换业务数据为 simple-mind-map 所需格式
const transformData = (node) => {
  return {
    data: {
      text: node.label,
      uid: node.id, 
      ...node 
    },
    children: node.children ? node.children.map(transformData) : []
  }
}

// 递归查找业务节点数据
const findNodeById = (id, currentNode = rootNode.value) => {
  if (currentNode.id === id) return currentNode
  if (currentNode.children) {
    for (let child of currentNode.children) {
      const found = findNodeById(id, child)
      if (found) return found
    }
  }
  return null
}

onMounted(() => {
  // 用 nextTick 确保 DOM 已渲染完成
  setTimeout(() => {
    if (!mindMapContainer.value) return
    mindMapInstance = new MindMap({
      el: mindMapContainer.value,
      direction: 'right',
      theme: 'fresh-purple',
      data: transformData(rootNode.value),
      fit: true,
      mouseScaleBehavior: 'zoom',
      readonly: true,
      enableFreeDrag: false,
      themeConfig: {
        node: { expandBtnSize: 0 },
        root: { expandBtnSize: 0 }
      }
    })

    mindMapInstance.on('node_click', (node, e) => {
      const uid = node.nodeData.data.uid
      const foundNode = findNodeById(uid)
      if (foundNode) {
        activeNode.value = foundNode
      }
    })

    // 默认收起所有二级以下子节点，并放大到能清晰看到根节点
    setTimeout(() => {
      if (!mindMapInstance) return
      try {
        // 收起根节点的所有直接子节点，使其不展开
        const root = mindMapInstance.getRootNode && mindMapInstance.getRootNode()
        if (root && root.children && root.children.length) {
          root.children.forEach((child) => {
            try {
              if (typeof child.setShowChild === 'function') {
                child.setShowChild(false)
              } else if (mindMapInstance.execCommand) {
                mindMapInstance.execCommand('TOGGLE_NODE_EXPAND', false, [child])
              }
            } catch (err) {
              console.warn('收起子节点失败', err)
            }
          })
        }
        // 渲染并放大到能清晰看到根节点
        if (typeof mindMapInstance.render === 'function') {
          mindMapInstance.render()
        }
        if (typeof mindMapInstance.view.setScale === 'function') {
          mindMapInstance.view.setScale(1.8, [0, 0])
        } else if (typeof mindMapInstance.view.enlarge === 'function') {
          // 多次放大以保证根节点足够清晰
          mindMapInstance.view.enlarge()
          mindMapInstance.view.enlarge()
          mindMapInstance.view.enlarge()
        } else if (typeof mindMapInstance.view.fit === 'function') {
          mindMapInstance.view.fit()
        }
      } catch (err) {
        console.warn('初始化思维导图缩放失败', err)
      }
    }, 250)

    // 用 ResizeObserver 监听容器尺寸变化，自动适应
    if (canvasWrapperRef.value) {
      resizeObserver = new ResizeObserver(() => {
        if (mindMapInstance) {
          mindMapInstance.view.fit()
        }
      })
      resizeObserver.observe(canvasWrapperRef.value)
    }
  }, 100)
})

onBeforeUnmount(() => {
  if (resizeObserver) resizeObserver.disconnect()
  if (mindMapInstance) {
    mindMapInstance.destroy()
  }
})

// --- 控件与业务方法 ---
const handleZoomIn = () => mindMapInstance && mindMapInstance.view.enlarge()
const handleZoomOut = () => mindMapInstance && mindMapInstance.view.narrow()
const handleFitView = () => mindMapInstance && mindMapInstance.view.fit()

const markDifficulty = (level) => {
  if (activeNode.value && activeNode.value.id !== 'root') {
    activeNode.value.difficulty = level
    updateNodeStyle(activeNode.value.id, level)
  }
}

const getColor = (key) => key === 'easy' ? 'green' : (key === 'medium' ? 'indigo' : 'red')
const getEmoji = (key) => key === 'easy' ? '😊' : (key === 'medium' ? '😐' : '😰')

// 【核心修改】通过 simple-mind-map 的内部 API 更新节点渲染样式
const updateNodeStyle = (nodeId, difficulty) => {
  if (!mindMapInstance) return
  
  // simple-mind-map 使用 fillColor 和 color 控制背景和字体色
  const styleMap = {
    'easy': { fillColor: '#22c55e', color: '#ffffff', borderColor: '#16a34a' },
    'medium': { fillColor: '#6366f1', color: '#ffffff', borderColor: '#4f46e5' },
    'hard': { fillColor: '#ef4444', color: '#ffffff', borderColor: '#dc2626' }
  }
  
  const styles = styleMap[difficulty]
  
  // 在 readonly 模式下，获取 renderer 中的节点实例并直接覆盖样式属性
  const nodeInstance = mindMapInstance.renderer.findNodeByUid(nodeId)
  
  if (nodeInstance) {
    nodeInstance.setStyle('fillColor', styles.fillColor)
    nodeInstance.setStyle('color', styles.color)
    nodeInstance.setStyle('borderColor', styles.borderColor)
  }
}

const handleSubmit = () => {
  emit('complete')
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
  height: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

/* 覆盖 simple-mind-map 默认获取焦点时的黑边 */
:deep(.smm-canvas) {
  outline: none !important;
}

/* 强制隐藏 simple-mind-map 的加减号展开按钮组 */
:deep(.smm-expand-btn) {
  display: none !important;
  opacity: 0 !important;
  pointer-events: none !important;
}

/* 隐藏节点处于 active 状态下的自带外边框，让样式更加纯净 */
:deep(.smm-node.active) {
  outline: none !important;
}
</style>
