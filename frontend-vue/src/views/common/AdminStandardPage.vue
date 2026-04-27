<template>
  <div class="admin-page-container">
    <header class="page-header">
      <h1 class="panel-main-title">{{ title }}</h1>
    </header>

    <div class="toolbar">
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input type="text" v-model="searchQuery" :placeholder="`搜索${title}...`" class="search-input" />
      </div>
      <button class="primary-btn" @click="$emit('add')">+ 新增数据</button>
    </div>

    <main class="data-container">
      <div class="data-grid-header" :style="{ gridTemplateColumns: gridLayout }">
        <span v-for="col in columns" :key="col.key">{{ col.label }}</span>
        <span class="action-col">操作</span>
      </div>
      
      <div class="data-grid-body">
        <div 
          v-for="row in filteredData" 
          :key="row.id" 
          class="grid-row"
          :style="{ gridTemplateColumns: gridLayout }"
        >
          <span v-for="col in columns" :key="col.key" class="cell-text" :title="row[col.key]">
            <span v-if="col.isTag" class="status-tag" :class="row.statusType || 'default'">
              {{ row[col.key] }}
            </span>
            <template v-else>{{ row[col.key] }}</template>
          </span>
          
          <div class="action-col">
            <button class="text-btn" @click="$emit('edit', row)">编辑</button>
            <div class="v-divider"></div>
            <button class="text-btn danger" @click="$emit('delete', row)">删除</button>
          </div>
        </div>
        
        <div v-if="filteredData.length === 0" class="empty-state">暂无数据</div>
      </div>
    </main>
    
    <Pagination :total="data.length" v-model:current-page="currentPage" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Pagination from '@/views/common/Pagination.vue'

const props = defineProps({
  title: { type: String, required: true },
  columns: { type: Array as () => any[], required: true },
  data: { type: Array as () => any[], required: true },
  gridLayout: { type: String, default: 'repeat(auto-fit, minmax(100px, 1fr)) 150px' }
})

defineEmits(['add', 'edit', 'delete'])

const searchQuery = ref('')
const currentPage = ref(1)

const filteredData = computed(() => {
  if (!searchQuery.value) return props.data
  return props.data.filter(item => 
    Object.values(item).some(val => String(val).toLowerCase().includes(searchQuery.value.toLowerCase()))
  )
})
</script>

<style scoped>
.admin-page-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--color-surface, #ffffff);
  border-radius: 8px;
  padding: 24px;
  box-sizing: border-box;
}

.page-header { margin-bottom: 16px; }
.panel-main-title { font-size: 20px; font-weight: 800; color: #0F172A; margin: 0; }

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #E2E8F0;
}

.search-box { position: relative; width: 300px; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); font-size: 14px; color: #94A3B8; }
.search-input { width: 100%; padding: 8px 12px 8px 36px; border: 1px solid #E2E8F0; border-radius: 6px; font-size: 13px; box-sizing: border-box;}
.search-input:focus { outline: none; border-color: #4F46E5; }

.primary-btn { background: #4F46E5; color: white; border: none; padding: 8px 16px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; }

.data-container { flex: 1; overflow: hidden; display: flex; flex-direction: column; margin-top: 16px; border: 1px solid #E2E8F0; border-radius: 6px;}

/* 核心：极其严谨的单行网格，杜绝高度冗余留白 */
.data-grid-header, .grid-row {
  display: grid;
  align-items: center;
  padding: 10px 16px;
  font-size: 13px;
}

.data-grid-header { background: #F8FAFC; font-weight: 700; color: #475569; border-bottom: 1px solid #E2E8F0; }
.grid-row { border-bottom: 1px solid #F1F5F9; color: #1E293B; transition: background 0.2s; }
.grid-row:hover { background: #F8FAFC; }
.grid-row:last-child { border-bottom: none; }

.cell-text { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding-right: 12px; }

.status-tag { padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; }
.status-tag.active { background: #DCFCE7; color: #166534; }
.status-tag.warning { background: #FEF3C7; color: #92400E; }
.status-tag.default { background: #F1F5F9; color: #475569; }

.action-col { display: flex; gap: 8px; align-items: center; justify-content: flex-start; }
.text-btn { background: none; border: none; color: #4F46E5; font-size: 13px; cursor: pointer; padding: 0; font-weight: 600;}
.text-btn.danger { color: #EF4444; }
.v-divider { width: 1px; height: 12px; background: #CBD5E1; }

.empty-state { padding: 40px; text-align: center; color: #94A3B8; font-size: 13px; }
</style>