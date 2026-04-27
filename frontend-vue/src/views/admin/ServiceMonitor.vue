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

const tableData = [
  { id: 1, nodeId: 'Machine-155', service: 'auth-0 鉴权容器', health: '12% / 45%', status: '运行中', statusType: 'active' },
  { id: 2, nodeId: 'Machine-158', service: '实训调度引擎', health: '89% / 92%', status: '负载告警', statusType: 'warning' }
]
</script>