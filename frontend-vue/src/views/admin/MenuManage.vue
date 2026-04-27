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

const tableData = [
  { id: 1, menuName: '学生工作台', routePath: '/student/workbench', permission: 'sys:student:workbench', status: '启用', statusType: 'active' },
  { id: 2, menuName: '实训模板仓库', routePath: '/teacher/training-manage', permission: 'sys:teacher:template', status: '启用', statusType: 'active' }
]
</script>