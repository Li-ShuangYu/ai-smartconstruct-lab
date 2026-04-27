<template>
  <AdminStandardPage 
    title="学生统管"
    :columns="columns"
    :data="tableData"
    gridLayout="1fr 1fr 1.5fr 1fr 150px"
  />
</template>

<script setup lang="ts">
import AdminStandardPage from '@/views/common/AdminStandardPage.vue'

const columns = [
  { key: 'studentId', label: '学号' },
  { key: 'name', label: '姓名' },
  { key: 'className', label: '所属班级' },
  { key: 'status', label: '实训状态', isTag: true }
]

const tableData = [
  { id: 1, studentId: 'S2401001', name: 'Tom 同学', className: '24级密码工程01班', status: '实训中', statusType: 'active' },
  { id: 2, studentId: 'S2502012', name: 'Alice', className: '25级信息安全专项班', status: '离线', statusType: 'default' }
]
</script>