<template>
  <div class="register-form-wrapper">
    <div class="form-header">
      <h2 class="form-title">创建账号</h2>
      <p class="form-desc">请选择身份并填写注册信息</p>
    </div>

    <div class="role-segment">
      <div
        v-for="role in roles"
        :key="role.id"
        class="segment-item"
        :class="{ active: currentRole === role.id }"
        @click="switchRole(role.id)"
      >
        <div class="icon" v-html="role.icon"></div>
        <span>{{ role.label }}</span>
      </div>
    </div>

    <form @submit.prevent="handleRegister" class="register-form">
      <div class="input-group">
        <div class="input-field">
          <FontAwesomeIcon icon="fa-solid fa-id-card" class="input-icon" />
          <input type="text" v-model="form.username" :placeholder="idPlaceholder" required />
        </div>
      </div>

      <div class="input-group">
        <div class="input-field">
          <FontAwesomeIcon icon="fa-solid fa-user" class="input-icon" />
          <input type="text" v-model="form.realName" placeholder="请输入真实姓名" required />
        </div>
      </div>

      <!-- 院系下拉 -->
      <div class="input-group">
        <div class="input-field">
          <FontAwesomeIcon icon="fa-solid fa-building" class="input-icon" />
          <select v-model="selectedDeptId" class="form-select">
            <option value="" disabled>请选择院系</option>
            <option v-for="d in deptList" :key="d.id" :value="d.id">{{ d.deptName }}</option>
          </select>
        </div>
      </div>

      <!-- 学生专属：专业 + 班级 -->
      <template v-if="currentRole === 'student'">
        <div class="input-group">
          <div class="input-field">
            <FontAwesomeIcon icon="fa-solid fa-graduation-cap" class="input-icon" />
            <select v-model="selectedMajorId" class="form-select" :disabled="!selectedDeptId">
              <option value="" disabled>请选择专业</option>
              <option v-for="m in majorList" :key="m.id" :value="m.id">{{ m.majorName }}</option>
            </select>
          </div>
        </div>

        <div class="input-group">
          <div class="input-field">
            <FontAwesomeIcon icon="fa-solid fa-users" class="input-icon" />
            <select v-model="selectedClassId" class="form-select" :disabled="!selectedMajorId">
              <option value="" disabled>请选择班级</option>
              <option v-for="c in classList" :key="c.id" :value="c.id">{{ c.className }}</option>
            </select>
          </div>
        </div>
      </template>

      <div class="input-group">
        <div class="input-field">
          <FontAwesomeIcon icon="fa-solid fa-lock" class="input-icon" />
          <input type="password" v-model="form.password" placeholder="设置登录密码" required />
        </div>
      </div>

      <div class="input-group">
        <div class="input-field">
          <FontAwesomeIcon icon="fa-solid fa-check-double" class="input-icon" />
          <input type="password" v-model="form.confirmPassword" placeholder="请再次确认密码" required />
        </div>
      </div>

      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
      <div v-if="successMsg" class="success-msg">{{ successMsg }}</div>

      <button type="submit" class="submit-btn" :disabled="loading">
        {{ loading ? '注册中...' : '立即注册' }}
      </button>

      <div class="login-prompt">
        已有账号？<router-link to="/auth/login">返回登录</router-link>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { register as registerApi } from '@/services/modules/auth.service'
import { getDepartments, getMajors, getClasses } from '@/services/modules/org.service'
import type { Department, Major, AdminClass } from '@/services/types/org.types'

const router = useRouter()
const currentRole = ref('student')
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

const roleMap: Record<string, number> = { student: 3, teacher: 2 }

const form = reactive({
  username: '',
  realName: '',
  password: '',
  confirmPassword: ''
})

const selectedDeptId = ref<string | number>('')
const selectedMajorId = ref<string | number>('')
const selectedClassId = ref<string | number>('')

const deptList = ref<Department[]>([])
const majorList = ref<Major[]>([])
const classList = ref<AdminClass[]>([])

const roles = [
  { id: 'student', label: '学生', icon: '<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M22 10l-10-5-10 5 10 5 10-5z"></path><path d="M6 12v5c3 3 9 3 12 0v-5"></path></svg>' },
  { id: 'teacher', label: '教师', icon: '<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M2 3h20v14H2z"></path><path d="M12 17v4M8 21h8"></path></svg>' }
]

const idPlaceholder = computed(() => {
  return currentRole.value === 'student' ? '请输入学号' : '请输入工号'
})

function switchRole(roleId: string) {
  currentRole.value = roleId
  selectedDeptId.value = ''
  selectedMajorId.value = ''
  selectedClassId.value = ''
  majorList.value = []
  classList.value = []
}

// 级联：院系 → 专业
watch(selectedDeptId, async (val) => {
  selectedMajorId.value = ''
  selectedClassId.value = ''
  majorList.value = []
  classList.value = []
  if (val) {
    try {
      const res = await getMajors(val as number)
      if (res.code === 200) majorList.value = res.data || []
    } catch { /* ignore */ }
  }
})

// 级联：专业 → 班级
watch(selectedMajorId, async (val) => {
  selectedClassId.value = ''
  classList.value = []
  if (val) {
    try {
      const res = await getClasses(val as number)
      if (res.code === 200) classList.value = res.data || []
    } catch { /* ignore */ }
  }
})

onMounted(async () => {
  try {
    const res = await getDepartments()
    if (res.code === 200) deptList.value = res.data || []
  } catch { /* ignore */ }
})

const handleRegister = async () => {
  errorMsg.value = ''
  successMsg.value = ''

  if (!form.username || !form.realName || !form.password || !form.confirmPassword) {
    errorMsg.value = '请填写所有必填项'
    return
  }
  if (!selectedDeptId.value) {
    errorMsg.value = '请选择所属院系'
    return
  }
  if (currentRole.value === 'student') {
    if (!selectedMajorId.value) { errorMsg.value = '请选择专业'; return }
    if (!selectedClassId.value) { errorMsg.value = '请选择班级'; return }
  }
  if (form.password !== form.confirmPassword) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }
  if (form.password.length < 6) {
    errorMsg.value = '密码长度不能少于6位'
    return
  }

  loading.value = true
  try {
    const res = await registerApi({
      username: form.username,
      password: form.password,
      roleType: roleMap[currentRole.value],
      realName: form.realName,
      deptId: selectedDeptId.value ? Number(selectedDeptId.value) : undefined,
      majorId: selectedMajorId.value ? Number(selectedMajorId.value) : undefined,
      classId: selectedClassId.value ? Number(selectedClassId.value) : undefined,
      studentNo: currentRole.value === 'student' ? form.username : undefined,
      employeeNo: currentRole.value === 'teacher' ? form.username : undefined
    })
    if (res.code === 200) {
      successMsg.value = '注册成功，即将跳转登录页...'
      setTimeout(() => router.push('/auth/login'), 1500)
    } else {
      errorMsg.value = res.message || '注册失败'
    }
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-form-wrapper {
  width: 100%;
  max-width: 360px;
  padding: 0 10px;
}

.form-header {
  text-align: center;
  margin-bottom: 20px;
}

.form-title {
  font-size: 22px;
  font-weight: 800;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.form-desc {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}

.role-segment {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}

.segment-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 0;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.segment-item.active {
  border-color: #6366F1;
  background: #EEF2FF;
  color: #6366F1;
  box-shadow: 0 4px 10px rgba(99, 102, 241, 0.08);
}

.segment-item span {
  font-size: 12px;
  font-weight: 700;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.input-group {
  margin: 0;
}

.input-field {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  color: #94a3b8;
  font-size: 13px;
  z-index: 1;
}

.input-field input,
.form-select {
  width: 100%;
  padding: 11px 14px 11px 38px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px;
  color: #1e293b;
  background-color: #fcfdfe;
  transition: all 0.2s;
  outline: none;
  font-family: inherit;
}

.form-select {
  appearance: none;
  cursor: pointer;
}

.form-select:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.input-field input:focus,
.form-select:focus {
  border-color: #6366F1;
  background-color: #fff;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.06);
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: #6366F1;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  margin-top: 6px;
  transition: all 0.2s ease;
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.submit-btn:hover {
  background-color: #4f46e5;
  box-shadow: 0 6px 15px rgba(99, 102, 241, 0.2);
}

.login-prompt {
  text-align: center;
  margin-top: 10px;
  font-size: 13px;
  color: #94a3b8;
}

.login-prompt a {
  color: #6366F1;
  font-weight: 700;
  text-decoration: none;
}

.login-prompt a:hover {
  text-decoration: underline;
}

.error-msg {
  color: #ef4444;
  font-size: 13px;
  text-align: center;
}

.success-msg {
  color: #22c55e;
  font-size: 13px;
  text-align: center;
}
</style>
