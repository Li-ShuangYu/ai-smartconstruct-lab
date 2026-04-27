<template>
  <AdminStandardPage 
    title="教学组织架构维护"
    :columns="columns"
    :data="tableData"
    gridLayout="2fr 1fr 1fr 1fr 150px"
  />
</template>

<script setup lang="ts">
import AdminStandardPage from '@/views/common/AdminStandardPage.vue'

const columns = [
  { key: 'orgName', label: '组织名称' },
  { key: 'level', label: '层级' },
  { key: 'director', label: '负责人' },
  { key: 'status', label: '状态', isTag: true }
]

const tableData = [
  { id: 1, orgName: '密码工程学院', level: '院系', director: '付老师', status: '正常', statusType: 'active' },
  { id: 2, orgName: '24级密码工程01班', level: '行政班级', director: '楼茂密', status: '正常', statusType: 'active' }
]
</script>