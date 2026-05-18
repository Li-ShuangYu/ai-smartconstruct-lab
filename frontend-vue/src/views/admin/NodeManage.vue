<template>
  <div class="admin-page">
    <div class="page-header"><h1>编排节点管理</h1></div>
    <div style="display:flex;gap:12px;margin-bottom:12px;align-items:center">
      <n-button type="primary" size="small" @click="openModal(null)">新增节点</n-button>
      <n-input v-model:value="keyword" placeholder="搜索节点名称/类型..." clearable style="max-width:240px" size="small" @keyup.enter="fetchData" />
    </div>
    <n-data-table :columns="columns" :data="filteredList" :loading="loading" :bordered="true" size="small" />

    <n-modal v-model:show="showModal" preset="card" :title="editingId ? '编辑节点' : '新增节点'" style="max-width:480px">
      <n-form :model="form" label-placement="left" label-width="80">
        <n-form-item label="节点类型">
          <n-input v-model:value="form.nodeType" placeholder="如 RESOURCE_READ" :disabled="!!editingId" />
        </n-form-item>
        <n-form-item label="节点名称">
          <n-input v-model:value="form.nodeName" placeholder="如 资源阅读" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="form.isActive" :checked-value="1" :unchecked-value="0" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="save" :loading="saving">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 二次确认删除弹窗 -->
    <n-modal v-model:show="showDeleteConfirm" preset="card" title="确认删除" style="max-width:400px">
      <p>确定要删除节点 <strong>{{ deleteTarget?.nodeName }}</strong>（{{ deleteTarget?.nodeType }}）吗？此操作不可恢复。</p>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDeleteConfirm = false">取消</n-button>
          <n-button type="error" @click="doDelete">确认删除</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue'
import { NButton, NDataTable, NModal, NForm, NFormItem, NInput, NSpace, NPagination, NTag, NSwitch, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import * as api from '@/services/modules/admin.service'
import type { NodeDef } from '@/services/types/admin.types'

const message = useMessage()
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const showDeleteConfirm = ref(false)
const keyword = ref('')
const editingId = ref<number | null>(null)
const deleteTarget = ref<NodeDef | null>(null)
const nodeList = ref<NodeDef[]>([])

const form = ref<NodeDef>({ nodeType: '', nodeName: '', isActive: 1 })

const filteredList = computed(() => {
  if (!keyword.value) return nodeList.value
  const kw = keyword.value.toLowerCase()
  return nodeList.value.filter(n =>
    n.nodeName?.toLowerCase().includes(kw) || n.nodeType?.toLowerCase().includes(kw)
  )
})

const columns: DataTableColumns<NodeDef> = [
  { title: '节点类型', key: 'nodeType', width: 200 },
  { title: '节点名称', key: 'nodeName' },
  {
    title: '状态', key: 'isActive', width: 80,
    render(row) {
      return row.isActive === 1
        ? h(NTag, { type: 'success', size: 'small' }, { default: () => '开启' })
        : h(NTag, { type: 'warning', size: 'small' }, { default: () => '关闭' })
    }
  },
  {
    title: '操作', key: 'actions', width: 240,
    render(row) {
      const isFixed = row.nodeType === 'START' || row.nodeType === 'END'
      return h('div', { style: 'display:flex;gap:6px' }, [
        h(NButton, {
          size: 'tiny',
          disabled: isFixed,
          onClick: () => openModal(row)
        }, { default: () => '修改' }),
        h(NButton, {
          size: 'tiny',
          type: 'error',
          disabled: isFixed,
          onClick: () => confirmDelete(row)
        }, { default: () => '删除' }),
        h(NButton, {
          size: 'tiny',
          type: row.isActive === 1 ? 'warning' : 'success',
          disabled: isFixed,
          onClick: () => toggleActive(row)
        }, { default: () => row.isActive === 1 ? '关闭' : '开启' })
      ])
    }
  }
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.getNodes()
    if (res.code === 200) {
      nodeList.value = res.data
    } else {
      message.error(res.message || '获取数据失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message || '网络错误')
  } finally {
    loading.value = false
  }
}

const openModal = (row: NodeDef | null) => {
  editingId.value = row?.id ?? null
  if (row) {
    form.value = { nodeType: row.nodeType, nodeName: row.nodeName, isActive: row.isActive }
  } else {
    form.value = { nodeType: '', nodeName: '', isActive: 1 }
  }
  showModal.value = true
}

const save = async () => {
  if (!form.value.nodeType?.trim()) { message.warning('节点类型不能为空'); return }
  if (!form.value.nodeName?.trim()) { message.warning('节点名称不能为空'); return }
  saving.value = true
  try {
    if (editingId.value) {
      const res = await api.updateNode(editingId.value, form.value)
      if (res.code !== 200) { message.error(res.message || '修改失败'); return }
    } else {
      const res = await api.addNode(form.value)
      if (res.code !== 200) { message.error(res.message || '新增失败'); return }
    }
    message.success('保存成功')
    showModal.value = false
    await fetchData()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const confirmDelete = (row: NodeDef) => {
  deleteTarget.value = row
  showDeleteConfirm.value = true
}

const doDelete = async () => {
  if (!deleteTarget.value?.id) return
  try {
    const res = await api.deleteNode(deleteTarget.value.id)
    if (res.code !== 200) { message.error(res.message || '删除失败'); return }
    message.success('已删除')
    showDeleteConfirm.value = false
    deleteTarget.value = null
    await fetchData()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '删除失败')
  }
}

const toggleActive = async (row: NodeDef) => {
  if (!row.id) return
  const newVal = row.isActive === 1 ? 0 : 1
  try {
    const res = await api.toggleNode(row.id, newVal)
    if (res.code === 200) {
      row.isActive = newVal
      message.success('已更新')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message || '操作失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.admin-page { padding: 24px; background: #fff; border-radius: 8px; min-height: 100%; }
.page-header h1 { font-size: 20px; font-weight: 800; color: #0F172A; margin: 0 0 16px; }
</style>