<template>
  <AdminStandardPage 
    title="教师管理"
    :columns="columns"
    :data="tableData"
    gridLayout="1fr 1fr 1fr 1fr 150px"
  />
</template>

<script setup lang="ts">
import AdminStandardPage from '@/views/common/AdminStandardPage.vue'

const columns = [
  { key: 'empNo', label: '教职工号' },
  { key: 'name', label: '姓名' },
  { key: 'dept', label: '所属院系' },
  { key: 'status', label: '账号状态', isTag: true }
]

const tableData = [
  { id: 1, empNo: 'T2026001', name: '付老师', dept: '密码工程学院', status: '正常', statusType: 'active' },
  { id: 2, empNo: 'T2026002', name: '涅槃', dept: '网络安全中心', status: '正常', statusType: 'active' }
]
</script>