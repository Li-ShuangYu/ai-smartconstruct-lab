<template>
  <div class="admin-page">
    <div class="page-header">
      <h1>学生统管</h1>
    </div>

    <div class="toolbar">
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input type="text" v-model="keyword" placeholder="搜索学生姓名或学号..." class="search-input" @keyup.enter="fetchData" />
      </div>
      <button class="primary-btn" @click="openModal(null)">+ 新增学生</button>
    </div>

    <div class="data-grid">
      <div class="grid-header" style="grid-template-columns: 120px 100px 1fr 1fr 1fr 160px 120px;">
        <span>账号</span>
        <span>姓名</span>
        <span>院系</span>
        <span>专业</span>
        <span>班级</span>
        <span>创建时间</span>
        <span>操作</span>
      </div>
      <div class="grid-body">
        <div v-for="row in data.records" :key="row.userId" class="grid-row" style="grid-template-columns: 120px 100px 1fr 1fr 1fr 160px 120px;">
          <span class="text-truncate" :title="row.username">{{ row.username }}</span>
          <span>{{ row.realName }}</span>
          <span>{{ getDeptName(row.deptId) }}</span>
          <span>{{ getMajorName(row.majorId) }}</span>
          <span>{{ getClassName(row.classId) }}</span>
          <span class="text-truncate" :title="formatDate(row.createdAt)">{{ formatDate(row.createdAt) }}</span>
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

    <n-modal v-model:show="showModal" preset="card" :title="editingId ? '编辑学生' : '新增学生'" style="width: 500px;">
      <n-form :model="form" label-placement="top" autocomplete="off">
        <n-form-item label="账号">
          <n-input v-model:value="form.username" :disabled="editingId !== null" placeholder="请输入账号" autocomplete="off" />
        </n-form-item>
        <n-form-item label="姓名">
          <n-input v-model:value="form.realName" placeholder="请输入姓名" autocomplete="off" />
        </n-form-item>
        <n-form-item label="院系">
          <n-select v-model:value="form.deptId" :options="deptOptions" placeholder="请选择院系" style="width: 100%;" @update:value="handleDeptChange" />
        </n-form-item>
        <n-form-item label="专业">
          <n-select v-model:value="form.majorId" :options="majorOptions" placeholder="请选择专业" style="width: 100%;" :disabled="!form.deptId" @update:value="handleMajorChange" />
        </n-form-item>
        <n-form-item label="班级">
          <n-select v-model:value="form.classId" :options="classOptions" placeholder="请选择班级" style="width: 100%;" :disabled="!form.majorId" />
        </n-form-item>
        <n-form-item label="登录密码">
          <n-input type="password" v-model:value="form.password" :placeholder="editingId ? '设置新密码（不修改请留空）' : '请设置登录密码'" autocomplete="new-password" />
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
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { NButton, NModal, NForm, NFormItem, NInput, NSelect, useMessage } from 'naive-ui'
import * as api from '@/services/modules/admin.service'
import { getDepartments, getMajors, getClasses } from '@/services/modules/org.service'
import type { Student, PageResult, Department, Major, AdminClass } from '@/services/types/admin.types'

const message = useMessage()
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const keyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const editingId = ref<number | null>(null)
const data = ref<PageResult<Student>>({ records: [], total: 0, page: 1, pageSize: 10 })
const depts = ref<Department[]>([])
const majors = ref<Major[]>([])
const classes = ref<AdminClass[]>([])
const form = reactive<Partial<Student>>({ username: '', realName: '', deptId: 0, majorId: 0, classId: 0, password: '' })

const deptOptions = computed(() => {
  return depts.value.map(dept => ({
    label: dept.deptName,
    value: dept.id
  }))
})

const majorOptions = computed(() => {
  return majors.value.map(major => ({
    label: major.majorName,
    value: major.id
  }))
})

const classOptions = computed(() => {
  return classes.value.map(cls => ({
    label: cls.className,
    value: cls.id
  }))
})

async function fetchData() {
  loading.value = true
  try {
    const r = await api.getStudents(page.value, pageSize.value, keyword.value || undefined)
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

async function fetchDepts() {
  try {
    const r = await getDepartments()
    if (r.code === 200) {
      depts.value = r.data || []
    }
  } catch (e: any) {
    message.error('获取院系数据失败')
  }
}

async function fetchMajors(deptId: number) {
  try {
    const r = await getMajors(deptId)
    if (r.code === 200) {
      majors.value = r.data || []
    }
  } catch (e: any) {
    message.error('获取专业数据失败')
  }
}

async function fetchClasses(majorId: number) {
  try {
    const r = await getClasses(majorId)
    if (r.code === 200) {
      classes.value = r.data || []
    }
  } catch (e: any) {
    message.error('获取班级数据失败')
  }
}

function handleDeptChange(deptId: number) {
  form.majorId = 0
  form.classId = 0
  majors.value = []
  classes.value = []
  if (deptId) {
    fetchMajors(deptId)
  }
}

function handleMajorChange(majorId: number) {
  form.classId = 0
  classes.value = []
  if (majorId) {
    fetchClasses(majorId)
  }
}

function getDeptName(deptId: number | undefined): string {
  if (!deptId) return '-'
  const dept = depts.value.find(d => d.id === deptId)
  return dept?.deptName || String(deptId)
}

function getMajorName(majorId: number | undefined): string {
  if (!majorId) return '-'
  const major = majors.value.find(m => m.id === majorId)
  return major?.majorName || String(majorId)
}

function getClassName(classId: number | undefined): string {
  if (!classId) return '-'
  const cls = classes.value.find(c => c.id === classId)
  return cls?.className || String(classId)
}

function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

function openModal(row: Student | null) {
  editingId.value = row?.userId ?? null
  if (row) {
    Object.assign(form, { username: row.username, realName: row.realName, deptId: row.deptId, majorId: row.majorId, classId: row.classId, password: '' })
    if (row.deptId) fetchMajors(row.deptId)
    if (row.majorId) fetchClasses(row.majorId)
  } else {
    Object.assign(form, { username: '', realName: '', deptId: 0, majorId: 0, classId: 0, password: '' })
    majors.value = []
    classes.value = []
  }
  showModal.value = true
}

async function save() {
  if (!form.username?.trim()) { message.warning('请输入账号'); return }
  if (!form.realName?.trim()) { message.warning('请输入姓名'); return }
  if (!form.deptId) { message.warning('请选择院系'); return }
  if (!form.majorId) { message.warning('请选择专业'); return }
  if (!form.classId) { message.warning('请选择班级'); return }
  if (editingId.value === null && !form.password?.trim()) { 
    message.warning('请设置登录密码'); 
    return 
  }
  if (editingId.value === null && form.password.length < 6) {
    message.warning('密码长度不能少于 6 位');
    return
  }
  if (editingId.value !== null && form.password && form.password.length < 6) {
    message.warning('密码长度不能少于 6 位');
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await api.updateStudent(editingId.value, form as Student)
    } else {
      await api.addStudent(form as Student)
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

async function handleDelete(row: Student) {
  try {
    await api.deleteStudent(row.userId!)
    message.success('已删除')
    await fetchData()
  } catch (e: any) {
    message.error(e?.response?.data?.message || '删除失败')
  }
}

onMounted(async () => {
  await fetchDepts()
  await fetchData()
})
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
.text-truncate { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.empty-state { padding: 48px; text-align: center; color: #94A3B8; font-size: 14px; }
.pagination-wrap { display: flex; align-items: center; justify-content: flex-end; gap: 12px; margin-top: 16px; font-size: 13px; color: #64748B; }
.page-btn { padding: 6px 12px; border: 1px solid #E2E8F0; background: #fff; border-radius: 6px; cursor: pointer; font-size: 13px; }
.page-btn:hover:not(:disabled) { border-color: #4F46E5; color: #4F46E5; }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
</style>