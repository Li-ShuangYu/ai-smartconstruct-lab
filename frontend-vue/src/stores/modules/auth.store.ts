import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface UserInfo {
  userId: number
  username: string
  roleType: number
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('auth_token') || '')
  const userId = ref<number>(0)
  const username = ref<string>('')
  const roleType = ref<number>(0)

  const storedUser = localStorage.getItem('auth_user')
  if (storedUser) {
    try {
      const u: UserInfo = JSON.parse(storedUser)
      userId.value = u.userId
      username.value = u.username
      roleType.value = u.roleType
    } catch { /* ignore corrupt data */ }
  }

  const isLoggedIn = computed(() => !!token.value)

  const roleLabel = computed(() => {
    switch (roleType.value) {
      case 1: return 'admin'
      case 2: return 'teacher'
      case 3: return 'student'
      default: return ''
    }
  })

  function setAuth(t: string, id: number, name: string, role: number) {
    token.value = t
    userId.value = id
    username.value = name
    roleType.value = role
    localStorage.setItem('auth_token', t)
    localStorage.setItem('auth_user', JSON.stringify({ userId: id, username: name, roleType: role }))
  }

  function logout() {
    token.value = ''
    userId.value = 0
    username.value = ''
    roleType.value = 0
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
  }

  return { token, userId, username, roleType, isLoggedIn, roleLabel, setAuth, logout }
})
