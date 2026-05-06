<template>
  <AdminStandardPage 
    title="服务监控"
    :columns="columns"
    :data="tableData"
    gridLayout="1.5fr 1fr 1.5fr 1fr 150px"
  />
</template>

<script setup lang="ts">
import AdminStandardPage from '@/views/common/AdminStandardPage.vue'

const columns = [
  { key: 'nodeId', label: '服务器节点' },
  { key: 'service', label: '微服务模块' },
  { key: 'health', label: '健康度(CPU/Mem)' },
  { key: 'status', label: '运行状态', isTag: true }
]

const tableData: any[] = []
</script>