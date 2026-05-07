<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">课程空间</h1>
      <n-input v-model:value="keyword" placeholder="搜索课程名称或代码..." clearable style="width: 280px" @update:value="onSearch">
        <template #prefix><span style="color:#94A3B8">🔍</span></template>
      </n-input>
    </div>

    <div class="tabs-nav">
      <div class="tab-item" :class="{ active: activeTab === 'my' }" @click="switchTab('my')">我的课程</div>
      <div class="tab-item" :class="{ active: activeTab === 'enroll' }" @click="switchTab('enroll')">选课大厅</div>
    </div>

    <n-spin :show="loading">
      <!-- 我的课程 -->
      <n-grid v-if="activeTab === 'my' && courses.length > 0" :x-gap="16" :y-gap="16" cols="1 s:2 m:3 l:4" responsive="screen">
        <n-gi v-for="crs in courses" :key="crs.id">
          <n-card :title="crs.courseName" size="small" hoverable>
            <template #header-extra><n-tag :bordered="false" size="small">{{ crs.courseCode }}</n-tag></template>
            <p class="course-desc">{{ crs.description || '暂无简介' }}</p>
            <template #footer>
              <n-space>
                <n-button size="small" @click="showDetail(crs)">查看详情</n-button>
                <n-button size="small" @click="openCourseStudents(crs)">课程成员</n-button>
              </n-space>
            </template>
          </n-card>
        </n-gi>
      </n-grid>

      <!-- 选课大厅 -->
      <n-grid v-if="activeTab === 'enroll' && courses.length > 0" :x-gap="16" :y-gap="16" cols="1 s:2 m:3 l:4" responsive="screen">
        <n-gi v-for="crs in courses" :key="crs.id">
          <n-card :title="crs.courseName" size="small" hoverable>
            <template #header-extra><n-tag :bordered="false" size="small">{{ crs.courseCode }}</n-tag></template>
            <p class="course-desc">{{ crs.description || '暂无简介' }}</p>
            <template #footer>
              <n-button v-if="(crs as any).isEnrolled" block disabled secondary>已加入</n-button>
              <n-button v-else block type="primary" @click="handleEnroll(crs as any)">加入课程</n-button>
            </template>
          </n-card>
        </n-gi>
      </n-grid>
      <n-empty v-if="!loading && courses.length === 0" description="暂无课程" style="padding: 80px 0" />
    </n-spin>

    <div v-if="total > 0" class="pagination-wrap">
      <n-pagination v-model:page="page" :page-size="pageSize" :item-count="total" :page-sizes="[8,12,16,20]" show-size-picker @update:page="loadCourses" @update:page-size="handleSizeChange" />
    </div>

    <!-- 选课码弹窗 -->
    <n-modal v-model:show="enrollModal.show" preset="card" title="输入选课码" style="width: 420px" :mask-closable="false">
      <n-form ref="enrollFormRef" :model="enrollModal" :rules="enrollRules" label-placement="left" label-width="80">
        <n-form-item label="选课码" path="code"><n-input v-model:value="enrollModal.code" placeholder="请输入选课授权码" /></n-form-item>
      </n-form>
      <template #footer><div class="modal-footer"><n-button @click="enrollModal.show=false">取消</n-button><n-button type="primary" :loading="enrolling" @click="confirmEnroll">确认加入</n-button></div></template>
    </n-modal>

    <!-- 课程详情弹窗 -->
    <n-modal v-model:show="detailShow" preset="card" title="课程详情" style="width: 500px">
      <p style="white-space:pre-wrap">{{ detailContent }}</p>
      <template #footer><div class="modal-footer"><n-button @click="detailShow=false">关闭</n-button></div></template>
    </n-modal>

    <!-- 课程成员 Drawer -->
    <n-modal v-model:show="memberShow" preset="card" :title="memberTitle" style="width: 500px">
      <n-data-table :columns="memberCols" :data="memberList" :loading="memberLoading" size="small" :row-key="(r:any)=>r.userId" />
      <template #footer><div class="modal-footer"><n-button @click="memberShow=false">关闭</n-button></div></template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NButton, NCard, NTag, NGrid, NGi, NPagination, NInput, NModal, NForm, NFormItem, NSpace, NSpin, NEmpty, NDataTable, useMessage } from 'naive-ui'
import type { FormRules, DataTableColumn } from 'naive-ui'
import type { AvailableCourse, EnrolledCourse } from '@/services/types/admin.types'
import { getAvailableCourses, enrollCourse, getMyCourses } from '@/services/modules/student.service'
import { getCourseStudents } from '@/services/modules/teacher-dashboard.service'

const message = useMessage(); const loading = ref(false); const enrolling = ref(false)
const activeTab = ref('my'); const courses = ref<any[]>([])
const page = ref(1); const pageSize = ref(8); const total = ref(0); const keyword = ref('')
const enrollFormRef = ref(); let searchTimer: any
const enrollModal = ref({ show:false, code:'', courseId:0 })
const enrollRules: FormRules = { code:[{required:true,message:'请输入选课码'}] }

// 课程详情
const detailShow = ref(false); const detailContent = ref('')

// 课程成员
const memberShow = ref(false); const memberTitle = ref(''); const memberLoading = ref(false)
const memberList = ref<any[]>([])
const memberCols: DataTableColumn[] = [{ title:'姓名',key:'realName'},{ title:'学号',key:'studentNo'},{ title:'用户名',key:'username' }]

async function loadCourses() {
  loading.value = true
  try {
    if (activeTab.value === 'my') {
      const r = await getMyCourses(page.value, pageSize.value)
      if (r.code === 200) { courses.value = r.data.records; total.value = r.data.total }
    } else {
      const r = await getAvailableCourses(page.value, pageSize.value, keyword.value || undefined)
      if (r.code === 200) { courses.value = r.data.records; total.value = r.data.total }
    }
  } finally { loading.value = false }
}
function switchTab(t: string) { activeTab.value = t; page.value = 1; keyword.value = ''; loadCourses() }
function onSearch() { if (searchTimer) clearTimeout(searchTimer); searchTimer = setTimeout(()=>{page.value=1;loadCourses()},300) }
function handleSizeChange(s: number) { pageSize.value = s; page.value = 1; loadCourses() }

function handleEnroll(crs: AvailableCourse) {
  if (crs.needEnrollCode === 1) { enrollModal.value = { show:true, code:'', courseId: crs.id } }
  else doEnroll(crs.id)
}
async function confirmEnroll() { try{await enrollFormRef.value.validate()}catch{return}; await doEnroll(enrollModal.value.courseId, enrollModal.value.code) }
async function doEnroll(courseId: number, code?: string) {
  enrolling.value = true
  try { const r = await enrollCourse(courseId, code); if (r.code===200){message.success('选课成功');enrollModal.value.show=false;loadCourses()} else message.error(r.message||'选课失败') } catch{message.error('操作失败')} finally{enrolling.value=false}
}

function showDetail(crs: any) { detailContent.value = crs.description || '暂无详情'; detailShow.value = true }

async function openCourseStudents(crs: any) {
  memberTitle.value = `${crs.courseName} - 选课学生`; memberShow.value = true; memberLoading.value = true
  try { const r = await getCourseStudents(crs.id); if (r.code===200) memberList.value = r.data } finally { memberLoading.value = false }
}

onMounted(() => loadCourses())
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 800; color: #0F172A; margin: 0; }
.tabs-nav { display: flex; gap: 32px; border-bottom: 1px solid #E2E8F0; margin-bottom: 20px; }
.tab-item { padding: 12px 4px; cursor: pointer; color: #64748B; font-weight: 600; border-bottom: 2px solid transparent; transition: all .3s; }
.tab-item.active { color: #4F46E5; border-bottom-color: #4F46E5; }
.course-desc { color: #64748B; font-size: 13px; min-height: 40px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 24px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; }
</style>
