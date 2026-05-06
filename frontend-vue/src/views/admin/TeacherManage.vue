<template>
  <div class="admin-page">
    <div class="page-header">
      <h1>教师管理</h1>
    </div>

    <div class="toolbar">
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input type="text" v-model="keyword" placeholder="搜索教师姓名或工号..." class="search-input" @keyup.enter="fetchData" />
      </div>
      <button class="primary-btn" @click="openModal(null)">+ 新增教师</button>
    </div>

    <div class="data-grid">
      <div class="grid-header" style="grid-template-columns: 80px 1fr 1fr 120px 150px;">
        <span>ID</span>
        <span>姓名</span>
        <span>工号</span>
        <span>部门ID</span>
        <span>操作</span>
      </div>
      <div class="grid-body">
        <div v-for="row in data.records" :key="row.userId" class="grid-row" style="grid-template-columns: 80px 1fr 1fr 120px 150px;">
          <span>{{ row.userId }}</span>
          <span>{{ row.realName }}</span>
          <span>{{ row.employeeNo }}</span>
          <span>{{ row.deptId }}</span>
          <div class="action-cell">
            <button class="text-btn" @click="openModal(row)">编辑</button>
            <div class="v-divider"></div>
            <button class="text-btn danger" @click="handleDelete(row)">删除</button>
          </div>
        </div>
        <div v-if="data.records.length === 0" class="empty-state">暂无数据</div>
      </div>
    </div>

    <div class="pagination-wrap">
      <span>共 {{ data.total }} 条</span>
      <button class="page-btn" :disabled="page <= 1" @click="page--; fetchData()">上一页</button>
      <span>{{ page }} / {{ Math.ceil(data.total / pageSize) || 1 }}</span>
      <button class="page-btn" :disabled="page >= Math.ceil(data.total / pageSize)" @click="page++; fetchData()">下一页</button>
    </div>

    <n-modal v-model:show="showModal" preset="card" :title="editingId ? '编辑教师' : '新增教师'" style="width: 500px;">
      <n-form :model="form" label-placement="top">
        <n-form-item label="姓名">
          <n-input v-model:value="form.realName" placeholder="请输入姓名" />
        </n-form-item>
        <n-form-item label="工号">
          <n-input v-model:value="form.employeeNo" placeholder="请输入工号" />
        </n-form-item>
        <n-form-item label="部门ID">
          <n-input-number v-model:value="form.deptId" placeholder="请输入部门ID" style="width: 100%;" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 8px;">
          <button class="secondary-btn" @click="showModal = false">取消</button>
          <button class="primary-btn" :disabled="saving" @click="save">{{ saving ? '保存中...' : '保存' }}</button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { NButton, NModal, NForm, NFormItem, NInput, NInputNumber, NSpace, NPagination, NTag, useMessage } from 'naive-ui'
import * as api from '@/services/modules/admin.service'
import type { Teacher, PageResult } from '@/services/types/admin.types'

const message = useMessage()
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const keyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const editingId = ref<number | null>(null)
const data = ref<PageResult<Teacher>>({ records: [], total: 0, page: 1, pageSize: 10 })
const form = reactive<Partial<Teacher>>({ realName: '', employeeNo: '', deptId: 0 })

async function fetchData() {
  loading.value = true
  try {
    const r = await api.getTeachers(page.value, pageSize.value, keyword.value || undefined)
    if (r.code === 200) {
      data.value = r.data
    } else {
      message.error(r.message || '获取数据失败')
    }
  } catch (e: any) {
    message.error(e?.response?.data?.message || '网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

function openModal(row: Teacher | null) {
  editingId.value = row?.userId ?? null
  if (row) {
    Object.assign(form, { realName: row.realName, employeeNo: row.employeeNo, deptId: row.deptId })
  } else {
    Object.assign(form, { realName: '', employeeNo: '', deptId: 0 })
  }
  showModal.value = true
}

async function save() {
  if (!form.realName?.trim()) { message.warning('请输入姓名'); return }
  if (!form.employeeNo?.trim()) { message.warning('请输入工号'); return }
  saving.value = true
  try {
    if (editingId.value) {
      await api.updateTeacher(editingId.value, form as Teacher)
    } else {
      await api.addTeacher(form as Teacher)
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

async function handleDelete(row: Teacher) {
  try {
    await api.deleteTeacher(row.userId!)
    message.success('已删除')
    await fetchData()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '删除失败')
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.admin-page { padding: 24px; background: #fff; border-radius: 8px; min-height: 100%; }
.page-header h1 { font-size: 20px; font-weight: 800; color: #0F172A; margin: 0 0 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; padding-bottom: 16px; border-bottom: 1px solid #E2E8F0; margin-bottom: 16px; }
.search-box { position: relative; width: 300px; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); font-size: 14px; color: #94A3B8; }
.search-input { width: 100%; padding: 8px 12px 8px 36px; border: 1px solid #E2E8F0; border-radius: 6px; font-size: 13px; box-sizing: border-box; }
.search-input:focus { outline: none; border-color: #4F46E5; }
.primary-btn { padding: 8px 16px; background: #4F46E5; color: #fff; border: none; border-radius: 6px; cursor: pointer; font-size: 13px; }
.primary-btn:hover { background: #4338CA; }
.primary-btn:disabled { background: #A5B4FC; cursor: not-allowed; }
.secondary-btn { padding: 8px 16px; background: #fff; color: #4F46E5; border: 1px solid #4F46E5; border-radius: 6px; cursor: pointer; font-size: 13px; }
.data-grid { border: 1px solid #E2E8F0; border-radius: 8px; overflow: hidden; }
.grid-header { display: grid; gap: 0; padding: 12px 16px; background: #F8FAFC; font-weight: 600; font-size: 13px; color: #64748B; }
.grid-body { }
.grid-row { display: grid; gap: 0; padding: 12px 16px; border-top: 1px solid #E2E8F0; font-size: 13px; color: #334155; align-items: center; }
.grid-row:hover { background: #F8FAFC; }
.action-cell { display: flex; align-items: center; gap: 4px; }
.text-btn { background: none; border: none; color: #4F46E5; cursor: pointer; font-size: 13px; padding: 4px 8px; border-radius: 4px; }
.text-btn:hover { background: #EEF2FF; }
.text-btn.danger { color: #EF4444; }
.text-btn.danger:hover { background: #FEF2F2; }
.v-divider { width: 1px; height: 12px; background: #E2E8F0; margin: 0 4px; }
.empty-state { padding: 48px; text-align: center; color: #94A3B8; font-size: 14px; }
.pagination-wrap { display: flex; align-items: center; justify-content: flex-end; gap: 12px; margin-top: 16px; font-size: 13px; color: #64748B; }
.page-btn { padding: 6px 12px; border: 1px solid #E2E8F0; background: #fff; border-radius: 6px; cursor: pointer; font-size: 13px; }
.page-btn:hover:not(:disabled) { border-color: #4F46E5; color: #4F46E5; }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
</style>