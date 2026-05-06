<template>
  <AdminStandardPage 
    title="菜单管理"
    :columns="columns"
    :data="tableData"
    gridLayout="1.5fr 1.5fr 1.5fr 1fr 150px"
  />
</template>

<script setup lang="ts">
import AdminStandardPage from '@/views/common/AdminStandardPage.vue'

const columns = [
  { key: 'menuName', label: '菜单名称' },
  { key: 'routePath', label: '路由路径' },
  { key: 'permission', label: '权限标识' },
  { key: 'status', label: '状态', isTag: true }
]

const tableData: any[] = []
</script>