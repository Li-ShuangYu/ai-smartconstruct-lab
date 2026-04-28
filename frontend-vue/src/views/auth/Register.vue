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
        @click="currentRole = role.id"
      >
        <div class="icon" v-html="role.icon"></div>
        <span>{{ role.label }}</span>
      </div>
    </div>

    <form @submit.prevent="handleRegister" class="register-form">
      <div class="input-group">
        <div class="input-field">
          <i class="fas fa-id-card input-icon"></i>
          <input type="text" v-model="form.id" :placeholder="idPlaceholder" required />
        </div>
      </div>

      <div class="input-group">
        <div class="input-field">
          <i class="fas fa-user input-icon"></i>
          <input type="text" v-model="form.name" placeholder="请输入真实姓名" required />
        </div>
      </div>

      <div class="input-group">
        <div class="input-field">
          <i class="fas fa-lock input-icon"></i>
          <input type="password" v-model="form.password" placeholder="设置登录密码" required />
        </div>
      </div>

      <div class="input-group">
        <div class="input-field">
          <i class="fas fa-check-double input-icon"></i>
          <input type="password" v-model="form.confirmPassword" placeholder="请再次确认密码" required />
        </div>
      </div>

      <button type="submit" class="submit-btn">立即注册</button>
      
      <div class="login-prompt">
        已有账号？<router-link to="/auth/login">返回登录</router-link>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentRole = ref('student')

const form = reactive({
  id: '',
  name: '',
  password: '',
  confirmPassword: ''
})

const roles = [
  { id: 'student', label: '学生', icon: '<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M22 10l-10-5-10 5 10 5 10-5z"></path><path d="M6 12v5c3 3 9 3 12 0v-5"></path></svg>' },
  { id: 'teacher', label: '教师', icon: '<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M2 3h20v14H2z"></path><path d="M12 17v4M8 21h8"></path></svg>' },
  { id: 'admin', label: '管理', icon: '<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>' }
]

const idPlaceholder = computed(() => {
  if (currentRole.value === 'student') return '请输入学号'
  if (currentRole.value === 'teacher') return '请输入工号'
  return '管理员账号'
})

const handleRegister = () => {
  if (form.password !== form.confirmPassword) {
    alert('两次输入的密码不一致')
    return
  }
  console.log('注册提交:', { role: currentRole.value, ...form })
  // 模拟注册成功
  router.push('/login')
}
</script>

<style scoped>
.register-form-wrapper {
  width: 100%;
  max-width: 320px;
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

/* 角色分段控制器：注册页采用更紧凑的设计 */
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

/* 表单紧凑排列 */
.register-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
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
}

.input-field input {
  width: 100%;
  padding: 11px 14px 11px 38px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px;
  color: #1e293b;
  background-color: #fcfdfe;
  transition: all 0.2s;
  outline: none;
}

.input-field input:focus {
  border-color: #6366F1;
  background-color: #fff;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.06);
}

/* 按钮与跳转链接 */
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
</style>