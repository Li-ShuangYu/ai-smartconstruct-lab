<template>
  <AdminStandardPage 
    title="工单与系统反馈处理"
    :columns="columns"
    :data="tableData"
    gridLayout="1fr 1fr 2.5fr 1fr 150px"
  />
</template>

<script setup lang="ts">
import AdminStandardPage from '@/views/common/AdminStandardPage.vue'

const columns = [
  { key: 'ticketId', label: '工单编号' },
  { key: 'submitter', label: '提交人' },
  { key: 'desc', label: '问题描述' },
  { key: 'status', label: '处理状态', isTag: true }
]

const tableData = [
  { id: 1, ticketId: 'TK-20260427-01', submitter: 'Tom 同学', desc: '学生舱位容器启动失败，提示 auth-0 异常', status: '待处理', statusType: 'warning' },
  { id: 2, ticketId: 'TK-20260426-15', submitter: '涅槃', desc: '实训雷达图数据可视化图例重叠，请求调整 ECharts 参数', status: '已解决', statusType: 'active' }
]
</script>