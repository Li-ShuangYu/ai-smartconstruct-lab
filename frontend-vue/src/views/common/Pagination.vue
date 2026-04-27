<template>
  <div class="pagination-container">
    <div class="page-info">
      共 <span class="highlight">{{ total }}</span> 条数据
    </div>

    <div class="page-actions">
      <div class="page-sizes">
        <select :value="pageSize" @change="handleSizeChange" class="size-select">
          <option v-for="size in pageSizes" :key="size" :value="size">
            {{ size }} 条/页
          </option>
        </select>
      </div>

      <div class="pager-group">
        <button 
          class="pager-btn" 
          :disabled="currentPage === 1" 
          @click="changePage(currentPage - 1)"
        >
          &lt;
        </button>
        
        <button 
          v-for="page in pageNumbers" 
          :key="page"
          class="pager-btn"
          :class="{ active: page === currentPage, dot: page === '...' }"
          :disabled="page === '...'"
          @click="page !== '...' && changePage(Number(page))"
        >
          {{ page }}
        </button>

        <button 
          class="pager-btn" 
          :disabled="currentPage === totalPages || totalPages === 0" 
          @click="changePage(currentPage + 1)"
        >
          &gt;
        </button>
      </div>

      <div class="page-jumper">
        跳至
        <input 
          type="number" 
          v-model.number="jumpInput" 
          @keyup.enter="handleJump"
          @blur="handleJump"
          class="jumper-input"
          min="1"
          :max="totalPages"
        />
        页
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

const props = defineProps({
  total: { type: Number, required: true },
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  pageSizes: { type: Array as () => number[], default: () => [10, 20, 50, 100] }
})

const emit = defineEmits(['update:currentPage', 'update:pageSize', 'change'])

const jumpInput = ref(props.currentPage)

// 同步外部传入的页码到输入框
watch(() => props.currentPage, (val) => {
  jumpInput.value = val
})

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

// 核心逻辑：动态计算显示的页码（处理省略号）
const pageNumbers = computed(() => {
  const current = props.currentPage
  const total = totalPages.value
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }
  if (current <= 4) {
    return [1, 2, 3, 4, 5, '...', total]
  }
  if (current >= total - 3) {
    return [1, '...', total - 4, total - 3, total - 2, total - 1, total]
  }
  return [1, '...', current - 1, current, current + 1, '...', total]
})

// 事件处理
const changePage = (page: number) => {
  if (page < 1 || page > totalPages.value || page === props.currentPage) return
  emit('update:currentPage', page)
  emit('change', { page, limit: props.pageSize })
}

const handleSizeChange = (e: Event) => {
  const newSize = Number((e.target as HTMLSelectElement).value)
  emit('update:pageSize', newSize)
  // 切换条数时，自动回到第一页
  emit('update:currentPage', 1)
  emit('change', { page: 1, limit: newSize })
}

const handleJump = () => {
  let page = Math.floor(jumpInput.value)
  if (isNaN(page) || page < 1) page = 1
  if (page > totalPages.value) page = totalPages.value
  jumpInput.value = page
  changePage(page)
}
</script>

<style scoped>
/* 严控容器高度与留白，单行Flex布局 */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  /* background-color: #fff; */
  border-top: 1px solid var(--color-border, #E2E8F0);
  font-size: 14px;
  color: #64748B;
  box-sizing: border-box;
}

.page-info .highlight {
  color: #0F172A;
  font-weight: 700;
  margin: 0 4px;
}

.page-actions {
  display: flex;
  align-items: center;
  gap: 16px; /* 控制各个操作块之间的间距 */
}

/* 下拉框样式 */
.size-select {
  padding: 4px 8px;
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  background-color: #fff;
  color: #475569;
  font-size: 13px;
  cursor: pointer;
  outline: none;
  transition: border-color 0.2s;
}
.size-select:focus, .size-select:hover {
  border-color: #818CF8;
}

/* 按钮组样式：紧凑相连 */
.pager-group {
  display: flex;
  gap: 4px;
}

.pager-btn {
  min-width: 28px;
  height: 28px;
  padding: 0 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  color: #475569;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.pager-btn:hover:not(:disabled) {
  color: #4F46E5;
  border-color: #818CF8;
}

.pager-btn.active {
  background-color: #4F46E5;
  color: #fff;
  border-color: #4F46E5;
  font-weight: 700;
}

.pager-btn:disabled {
  color: #CBD5E1;
  background-color: #F8FAFC;
  cursor: not-allowed;
}

.pager-btn.dot {
  border: none;
  background: transparent;
  cursor: default;
  color: #94A3B8;
}

/* 跳转输入框样式 */
.page-jumper {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.jumper-input {
  width: 40px;
  height: 26px;
  text-align: center;
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  font-size: 13px;
  color: #0F172A;
  outline: none;
  transition: border-color 0.2s;
  /* 隐藏上下箭头 */
  -moz-appearance: textfield;
}
.jumper-input::-webkit-outer-spin-button,
.jumper-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.jumper-input:focus {
  border-color: #818CF8;
}
</style>