<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">课程管理</h1>
      <n-button type="primary" @click="showCreateModal = true">新增课程</n-button>
    </div>

    <n-data-table
      :columns="columns"
      :data="courses"
      :loading="loading"
      :pagination="pagination"
      :row-key="(row: Course) => row.id ?? 0"
      remote
      @update:page="handlePageChange"
      @update:page-size="handlePageSizeChange"
    />

    <n-modal v-model:show="showCreateModal" :title="editingId ? '编辑课程' : '新增课程'">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left">
        <n-form-item label="课程名称" path="courseName">
          <n-input v-model:value="form.courseName" placeholder="请输入课程名称" />
        </n-form-item>
        <n-form-item label="课程代码" path="courseCode">
          <n-input v-model:value="form.courseCode" placeholder="请输入课程代码" />
        </n-form-item>
        <n-form-item label="课程简介">
          <n-input v-model:value="form.description" type="textarea" placeholder="请输入课程简介" />
        </n-form-item>
        <n-form-item label="需要选课码">
          <n-switch v-model:value="form.needEnrollCode" :checked-value="1" :unchecked-value="0" />
        </n-form-item>
        <n-form-item v-if="form.needEnrollCode === 1" label="选课授权码">
          <n-input v-model:value="form.enrollCode" placeholder="请输入选课授权码" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCreateModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { NButton, NDataTable, NModal, NForm, NFormItem, NInput, NSwitch, NSpace, NTag, useMessage } from 'naive-ui'
import type { DataTableColumn, PaginationProps, FormRules } from 'naive-ui'
import type { Course } from '@/services/types/admin.types'
import { getTeacherCourses, createCourse, updateCourse, deleteCourse, toggleCourseStatus } from '@/services/modules/teacher.service'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const courses = ref<Course[]>([])
const showCreateModal = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref()

const pagination = ref<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  prefix: ({ itemCount }) => `共 ${itemCount} 条`
})

const defaultForm = (): Course => ({
  courseName: '',
  courseCode: '',
  description: '',
  status: 0,
  needEnrollCode: 0,
  enrollCode: ''
})

const form = ref<Course>(defaultForm())

const rules: FormRules = {
  courseName: [{ required: true, message: '请输入课程名称' }],
  courseCode: [{ required: true, message: '请输入课程代码' }]
}

const columns: DataTableColumn<Course>[] = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '课程名称', key: 'courseName' },
  { title: '课程代码', key: 'courseCode' },
  { title: '简介', key: 'description', ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'default' }, { default: () => row.status === 1 ? '已发布' : '草稿' })
    }
  },
  {
    title: '选课码',
    key: 'needEnrollCode',
    width: 100,
    render(row) {
      return row.needEnrollCode === 1 ? '需要' : '不需要'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 260,
    render(row) {
      const buttons = [
        h(NButton, {
          size: 'small',
          type: row.status === 1 ? 'warning' : 'success',
          style: { marginRight: '8px' },
          onClick: () => handleToggleStatus(row)
        }, { default: () => row.status === 1 ? '下线' : '发布' }),
        h(NButton, {
          size: 'small',
          style: { marginRight: '8px' },
          onClick: () => openEdit(row)
        }, { default: () => '编辑' }),
        h(NButton, {
          size: 'small',
          type: 'error',
          onClick: () => handleDelete(row)
        }, { default: () => '删除' })
      ]
      return h('div', buttons)
    }
  }
]

async function loadCourses() {
  loading.value = true
  try {
    const res = await getTeacherCourses(pagination.value.page, pagination.value.pageSize)
    if (res.code === 200) {
      courses.value = res.data.records
      pagination.value.itemCount = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  pagination.value.page = page
  loadCourses()
}

function handlePageSizeChange(size: number) {
  pagination.value.pageSize = size
  pagination.value.page = 1
  loadCourses()
}

function openEdit(row: Course) {
  editingId.value = row.id ?? null
  form.value = {
    courseName: row.courseName,
    courseCode: row.courseCode,
    description: row.description ?? '',
    status: row.status,
    needEnrollCode: row.needEnrollCode ?? 0,
    enrollCode: row.enrollCode ?? ''
  }
  showCreateModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (editingId.value) {
      await updateCourse(editingId.value, form.value)
      message.success('课程已更新')
    } else {
      await createCourse(form.value)
      message.success('课程已创建')
    }
    showCreateModal.value = false
    form.value = defaultForm()
    editingId.value = null
    loadCourses()
  } catch {
    message.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row: Course) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await toggleCourseStatus(row.id!, newStatus)
    message.success(newStatus === 1 ? '课程已发布' : '课程已下线')
    loadCourses()
  } catch {
    message.error('操作失败')
  }
}

async function handleDelete(row: Course) {
  try {
    await deleteCourse(row.id!)
    message.success('课程已删除')
    loadCourses()
  } catch {
    message.error('删除失败')
  }
}

onMounted(() => loadCourses())
</script>

<style scoped>
.page-container {
  padding: 24px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-title {
  font-size: 24px;
  font-weight: 800;
  color: #0F172A;
  margin: 0;
}
</style>
