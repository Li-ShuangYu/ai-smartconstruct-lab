<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">选课大厅</h1>
      <n-input
        v-model:value="keyword"
        placeholder="搜索课程名称或代码..."
        clearable
        style="width: 280px"
        @update:value="onSearch"
      >
        <template #prefix>
          <span style="color: #94A3B8">🔍</span>
        </template>
      </n-input>
    </div>

    <n-spin :show="loading">
      <n-grid v-if="courses.length > 0" :x-gap="16" :y-gap="16" :cols="1" responsive="screen" item-responsive>
        <n-gi v-for="course in courses" :key="course.id" :span="1" :s="1" :m="2" :l="3" :xl="4">
          <n-card :title="course.courseName" size="small" hoverable>
            <template #header-extra>
              <n-tag :bordered="false" size="small">{{ course.courseCode }}</n-tag>
            </template>
            <p class="course-desc">{{ course.description || '暂无简介' }}</p>
            <template #footer>
              <n-button
                v-if="course.isEnrolled"
                block
                disabled
                secondary
              >已加入</n-button>
              <n-button
                v-else
                block
                type="primary"
                @click="handleEnroll(course)"
              >加入课程</n-button>
            </template>
          </n-card>
        </n-gi>
      </n-grid>
      <n-empty v-else description="暂无可用课程" style="padding: 80px 0" />
    </n-spin>

    <div v-if="total > 0" class="pagination-wrap">
      <n-pagination
        v-model:page="page"
        :page-size="pageSize"
        :item-count="total"
        :page-sizes="[8, 12, 16, 20]"
        show-size-picker
        @update:page="loadCourses"
        @update:page-size="handleSizeChange"
      />
    </div>

    <n-modal
      v-model:show="enrollModal.show"
      preset="card"
      title="输入选课码"
      style="width: 420px"
      :mask-closable="false"
    >
      <n-form ref="enrollFormRef" :model="enrollModal" :rules="enrollRules" label-placement="left" label-width="80">
        <n-form-item label="选课码" path="code">
          <n-input v-model:value="enrollModal.code" placeholder="请输入选课授权码" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div class="modal-footer">
          <n-button @click="enrollModal.show = false">取消</n-button>
          <n-button type="primary" :loading="enrolling" @click="confirmEnroll">确认加入</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  NButton, NCard, NTag, NGrid, NGi, NPagination, NInput, NModal, NForm, NFormItem, NSpace, NSpin, NEmpty, useMessage
} from 'naive-ui'
import type { FormRules } from 'naive-ui'
import type { AvailableCourse } from '@/services/types/admin.types'
import { getAvailableCourses, enrollCourse } from '@/services/modules/student.service'

const message = useMessage()
const loading = ref(false)
const enrolling = ref(false)
const courses = ref<AvailableCourse[]>([])
const page = ref(1)
const pageSize = ref(8)
const total = ref(0)
const keyword = ref('')
const enrollFormRef = ref()

let searchTimer: ReturnType<typeof setTimeout> | undefined

const enrollModal = ref({ show: false, code: '', courseId: 0 })
const enrollRules: FormRules = {
  code: [{ required: true, message: '请输入选课码' }]
}

async function loadCourses() {
  loading.value = true
  try {
    const res = await getAvailableCourses(page.value, pageSize.value, keyword.value || undefined)
    if (res.code === 200) {
      courses.value = res.data.records
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function onSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    loadCourses()
  }, 300)
}

function handleSizeChange(size: number) {
  pageSize.value = size
  page.value = 1
  loadCourses()
}

function handleEnroll(course: AvailableCourse) {
  if (course.needEnrollCode === 1) {
    enrollModal.value = { show: true, code: '', courseId: course.id }
  } else {
    doEnroll(course.id)
  }
}

async function confirmEnroll() {
  try {
    await enrollFormRef.value.validate()
  } catch {
    return
  }
  await doEnroll(enrollModal.value.courseId, enrollModal.value.code)
}

async function doEnroll(courseId: number, enrollCode?: string) {
  enrolling.value = true
  try {
    const res = await enrollCourse(courseId, enrollCode)
    if (res.code === 200) {
      message.success('选课成功')
      enrollModal.value.show = false
      loadCourses()
    } else {
      message.error(res.message || '选课失败')
    }
  } catch {
    message.error('操作失败')
  } finally {
    enrolling.value = false
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
.course-desc {
  color: #64748B;
  font-size: 13px;
  min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
