<template>
  <AdminStandardPage 
    title="操作日志审计"
    :columns="columns"
    :data="tableData"
    gridLayout="1fr 1fr 2fr 1fr 150px"
  />
</template>

<script setup lang="ts">
import AdminStandardPage from '@/views/common/AdminStandardPage.vue'

const columns = [
  { key: 'operator', label: '操作人' },
  { key: 'module', label: '业务模块' },
  { key: 'action', label: '操作内容' },
  { key: 'result', label: '执行结果', isTag: true }
]

const tableData = [
  { id: 1, operator: '陈嘉键', module: '编排节点管理', action: '执行容器诊断，重启机器 158 的 Docker 守护进程', result: '成功', statusType: 'active' },
  { id: 2, operator: '付老师', module: '教务中心', action: '下发《抗重放(Anti-Replay)机制演练》实训任务至 24级密码工程01班', result: '成功', statusType: 'active' }
]
</script>