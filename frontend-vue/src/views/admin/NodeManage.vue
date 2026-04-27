<template>
  <AdminStandardPage 
    title="实训编排节点管理"
    :columns="columns"
    :data="tableData"
    gridLayout="1.5fr 1fr 2fr 1fr 150px"
  />
</template>

<script setup lang="ts">
import AdminStandardPage from '@/views/common/AdminStandardPage.vue'

const columns = [
  { key: 'nodeName', label: '节点名称' },
  { key: 'ipAddress', label: 'IP地址' },
  { key: 'runningService', label: '承载服务' },
  { key: 'status', label: '节点状态', isTag: true }
]

const tableData = [
  { id: 1, nodeName: 'K8s-Worker-155', ipAddress: '192.168.1.155', runningService: 'auth-0 鉴权容器', status: '运行中', statusType: 'active' },
  { id: 2, nodeName: 'Docker-Host-158', ipAddress: '192.168.1.158', runningService: '抗重放实训沙箱', status: '维护中', statusType: 'warning' }
]
</script>