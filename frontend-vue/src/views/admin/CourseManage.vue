<template>
  <AdminStandardPage 
    title="课程统管"
    :columns="columns"
    :data="tableData"
    gridLayout="2fr 1fr 1fr 1fr 150px"
  />
</template>

<script setup lang="ts">
import AdminStandardPage from '@/views/common/AdminStandardPage.vue'

const columns = [
  { key: 'courseName', label: '课程名称' },
  { key: 'credit', label: '学分' },
  { key: 'type', label: '课程类型' },
  { key: 'status', label: '排课状态', isTag: true }
]

const tableData = [
  { id: 1, courseName: '密码系统设计与理论', credit: '4.0', type: '专业必修', status: '已排课', statusType: 'active' },
  { id: 2, courseName: 'UAV抗重放通信协议专论', credit: '2.0', type: '专业选修', status: '待排课', statusType: 'warning' }
]
</script>