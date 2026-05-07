<template>
  <div class="profile-container">
    <h1 class="panel-main-title">个人中心</h1>
    <n-spin :show="loading">
      <div class="profile-layout">
        <aside class="profile-sidebar">
          <div class="avatar-card sidebar-block">
            <div class="avatar-wrapper">
              <div class="text-avatar">{{ initial }}</div>
            </div>
            <h2 class="user-name">{{ profile.realName || profile.username }}</h2>
            <p class="user-role">{{ profile.deptName || '' }} | 教师</p>
            <div class="user-id">工号: {{ profile.employeeNo || '-' }}</div>
          </div>

          <div class="info-card sidebar-block">
            <h3 class="mini-title">基本信息</h3>
            <div class="info-item"><span class="info-label">手机号</span><span class="info-value">{{ profile.phone || '未填写' }}</span></div>
            <div class="info-item"><span class="info-label">个人简介</span><span class="info-value">{{ profile.bio || '未填写' }}</span></div>
          </div>
        </aside>

        <main class="settings-main">
          <div class="settings-card">
            <h3 class="section-title">修改登录密码</h3>
            <n-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-placement="left" label-width="100">
              <n-form-item label="原密码" path="oldPassword">
                <n-input v-model:value="pwdForm.oldPassword" type="password" placeholder="请输入原密码" />
              </n-form-item>
              <n-form-item label="新密码" path="newPassword">
                <n-input v-model:value="pwdForm.newPassword" type="password" placeholder="请输入新密码" />
              </n-form-item>
            </n-form>
            <div class="submit-action">
              <n-button type="primary" :loading="pwdSubmitting" @click="handleUpdatePassword">修改密码</n-button>
            </div>
          </div>
        </main>
      </div>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NSpin, NForm, NFormItem, NInput, NButton, useMessage } from 'naive-ui'
import type { FormRules, FormInst } from 'naive-ui'
import { getProfile, updatePassword } from '@/services/modules/teacher-dashboard.service'
import type { TeacherProfile } from '@/services/types/dashboard.types'

const message = useMessage()
const loading = ref(false)
const pwdSubmitting = ref(false)
const pwdFormRef = ref<FormInst | null>(null)
const profile = ref<TeacherProfile>({ userId: 0, username: '' })
const pwdForm = ref({ oldPassword: '', newPassword: '' })
const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码' }],
  newPassword: [{ required: true, message: '请输入新密码', min: 6 }]
}

const initial = computed(() => (profile.value.realName || profile.value.username || '?').charAt(0))

onMounted(async () => {
  loading.value = true
  try {
    const res = await getProfile()
    if (res.code === 200) profile.value = res.data
  } finally {
    loading.value = false
  }
})

async function handleUpdatePassword() {
  try {
    await pwdFormRef.value?.validate()
  } catch { return }
  pwdSubmitting.value = true
  try {
    const res = await updatePassword(pwdForm.value)
    if (res.code === 200) {
      message.success('密码修改成功')
      pwdForm.value = { oldPassword: '', newPassword: '' }
    } else {
      message.error(res.message || '修改失败')
    }
  } catch {
    message.error('操作失败')
  } finally {
    pwdSubmitting.value = false
  }
}
</script>

<style scoped>
.profile-container { max-width: 900px; margin: 0 auto; padding: 24px; box-sizing: border-box; }
.panel-main-title { font-size: 22px; font-weight: 800; color: #0F172A; margin: 0 0 24px 0; }
.profile-layout { display: grid; grid-template-columns: 280px 1fr; gap: 24px; align-items: start; }
.profile-sidebar { display: flex; flex-direction: column; gap: 20px; }
.sidebar-block { background: #FFFFFF; border-radius: 12px; padding: 24px; border: 1px solid #E2E8F0; }
.avatar-card { text-align: center; }
.avatar-wrapper { width: 80px; height: 80px; border-radius: 50%; margin: 0 auto 16px; background: linear-gradient(135deg, #4338CA, #6366F1); display: flex; align-items: center; justify-content: center; }
.text-avatar { font-size: 32px; font-weight: 800; color: white; }
.user-name { font-size: 18px; font-weight: 800; margin: 0 0 6px 0; color: #1E293B; }
.user-role { font-size: 13px; color: #64748B; margin: 0 0 12px 0; }
.user-id { font-size: 12px; color: #94A3B8; font-family: monospace; background: #F8FAFC; padding: 4px 8px; border-radius: 6px; display: inline-block; }
.mini-title { font-size: 14px; font-weight: 700; color: #1E293B; margin: 0 0 16px 0; padding-bottom: 8px; border-bottom: 1px solid #F1F5F9; }
.info-item { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 12px; }
.info-label { color: #64748B; }
.info-value { font-weight: 500; color: #334155; }
.settings-main { display: flex; flex-direction: column; gap: 20px; }
.settings-card { background: #FFFFFF; border-radius: 12px; padding: 32px; border: 1px solid #E2E8F0; }
.section-title { font-size: 16px; font-weight: 700; color: #0F172A; margin: 0 0 20px 0; padding-left: 10px; border-left: 4px solid #4F46E5; }
.submit-action { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
