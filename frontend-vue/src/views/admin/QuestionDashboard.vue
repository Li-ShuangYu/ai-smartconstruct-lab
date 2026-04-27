<template>
  <AdminStandardPage 
    title="公共题库大盘"
    :columns="columns"
    :data="tableData"
    gridLayout="2.5fr 1fr 1fr 1fr 150px"
  />
</template>

<script setup lang="ts">
import AdminStandardPage from '@/views/common/AdminStandardPage.vue'

const columns = [
  { key: 'content', label: '题目内容' },
  { key: 'type', label: '题型' },
  { key: 'kgNode', label: '关联知识图谱' },
  { key: 'difficulty', label: '难度', isTag: true }
]

const tableData = [
  { id: 1, content: '简述抗重放攻击防御机制中时间戳与随机数的作用差异', type: '简答题', kgNode: '抗重放机制', difficulty: '困难', statusType: 'warning' },
  { id: 2, content: 'SM4国密算法的分组长度是多少？', type: '单选题', kgNode: '对称密码学', difficulty: '简单', statusType: 'active' }
]
</script>