<template>
  <AdminStandardPage 
    title="公共实训模板库"
    :columns="columns"
    :data="tableData"
    gridLayout="2fr 1fr 1fr 1fr 150px"
  />
</template>

<script setup lang="ts">
import AdminStandardPage from '@/views/common/AdminStandardPage.vue'

const columns = [
  { key: 'templateName', label: '实训模板名称' },
  { key: 'author', label: '贡献者' },
  { key: 'usageCount', label: '引用次数' },
  { key: 'status', label: '审核状态', isTag: true }
]

const tableData = [
  { id: 1, templateName: '抗重放(Anti-Replay)机制分析实训', author: '付老师', usageCount: '128', status: '已上架', statusType: 'active' },
  { id: 2, templateName: '理论导学伙伴(多Agent互动)模板', author: '楼茂密', usageCount: '56', status: '审核中', statusType: 'warning' }
]
</script>