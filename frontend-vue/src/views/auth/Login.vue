<template>
  <div class="login-form-wrapper">
    <div class="role-segment">
      <div 
        v-for="role in roles" 
        :key="role.id"
        class="segment-item"
        :class="{ active: currentRole === role.id }"
        @click="currentRole = role.id"
      >
        <div class="icon" v-html="role.icon"></div>
        <span>{{ role.label }}</span>
      </div>
    </div>

    <form @submit.prevent="handleLogin" class="login-form">
      
      <div class="input-group">
        <label>账号</label>
        <div class="input-field">
          <i class="fas fa-user input-icon"></i>
          <input type="text" v-model="account" :placeholder="accountPlaceholder" required />
        </div>
      </div>

      <div class="input-group">
        <label>密码</label>
        <div class="input-field">
          <i class="fas fa-lock input-icon"></i>
          <input type="password" v-model="password" placeholder="请输入密码"  required />
        </div>
      </div>

      <div class="form-options">
        <label class="remember-checkbox">
          <input type="checkbox" v-model="rememberMe" />
          <span>记住我</span>
        </label>
        <a href="#" class="forgot-link">忘记密码？</a>
      </div>

      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

      <button type="submit" class="submit-btn" :disabled="loading" @click="handleLogin">
        {{ loading ? '登录中...' : '进入系统' }}
      </button>
      <div class="register-prompt">
        没有账号？<router-link to="/auth/register">立即注册</router-link>
      </div>

    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { login as loginApi } from '@/services/modules/auth.service'
import { useAuthStore } from '@/stores/modules/auth.store'

const router = useRouter()
const authStore = useAuthStore()
const currentRole = ref('student')
const account = ref('')
const password = ref('')
const rememberMe = ref(true)
const loading = ref(false)
const errorMsg = ref('')

const roleMap: Record<string, number> = { student: 3, teacher: 2, admin: 1 }

const routeMap: Record<string, string> = {
  student: '/student/workbench',
  teacher: '/teacher/workbench',
  admin: '/admin'
}

const roles = [
  { id: 'student', label: '学生端', icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 10v6M2 10l10-5 10 5-10 5z"></path><path d="M6 12v5c3 3 9 3 12 0v-5"></path></svg>' },
  { id: 'teacher', label: '教师端', icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 3h20v14H2z"></path><path d="M12 17v4M8 21h8"></path></svg>' },
  { id: 'admin', label: '管理端', icon: '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>' }
]

const accountPlaceholder = computed(() => {
  if (currentRole.value === 'student') return '请输入学生学号'
  if (currentRole.value === 'teacher') return '请输入教师工号'
  return '请输入管理员账号'
})

const handleLogin = async () => {
  errorMsg.value = ''
  if (!account.value || !password.value) {
    errorMsg.value = '请输入账号和密码'
    return
  }
  loading.value = true
  try {
    const res = await loginApi({
      username: account.value,
      password: password.value,
      roleType: roleMap[currentRole.value]!
    })
    if (res.code === 200 && res.data) {
      const { token, userId, username, roleType } = res.data
      authStore.setAuth(token, userId, username, roleType)
      const roleKey = currentRole.value
      router.push(routeMap[roleKey] || '/student/workbench')
    } else {
      errorMsg.value = res.message || '登录失败'
    }
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-form-wrapper {
  width: 100%;
  max-width: 360px; /* 限制表单宽度，使其居中且紧凑 */
  padding: 0 10px;
}

/* 角色分段控制器 (复刻图中的三大块按钮) */
.role-segment {
  display: flex;
  gap: 12px;
  margin-bottom: 40px;
}

.segment-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 0;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.segment-item:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.segment-item.active {
  border-color: #6366F1;
  background: #EEF2FF;
  color: #6366F1;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.1);
}

.segment-item span {
  font-size: 13px;
  font-weight: 600;
}

/* 表单输入区 */
.login-form {
  display: flex;
  flex-direction: column;
}

.input-group {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-group label {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
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
  font-size: 14px;
}

.input-field input {
  width: 100%;
  padding: 12px 14px 12px 38px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  color: #1e293b;
  transition: all 0.2s;
  outline: none;
}

.input-field input:focus {
  border-color: #6366F1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.input-field input::placeholder {
  color: #cbd5e1;
}

/* 选项区 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.remember-checkbox {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
}

.forgot-link {
  font-size: 13px;
  color: #6366F1;
  text-decoration: none;
  font-weight: 600;
}

.forgot-link:hover {
  text-decoration: underline;
}

/* 登录按钮 */
.submit-btn {
  width: 100%;
  padding: 14px;
  background-color: #6366F1; /* 复刻图中的紫蓝色 */
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submit-btn:hover {
  background-color: #4f46e5;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.submit-btn:active {
  transform: scale(0.98);
}

.error-msg {
  color: #ef4444;
  font-size: 13px;
  margin-bottom: 16px;
  text-align: center;
}

/* 注册入口 */
.register-prompt {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: #94a3b8;
}

.register-prompt a {
  color: #6366F1;
  font-weight: 600;
  text-decoration: none;
}

.register-prompt a:hover {
  text-decoration: underline;
}
</style>