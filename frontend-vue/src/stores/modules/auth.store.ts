import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 用户信息接口
 */
export interface UserInfo {
  userId: number
  username: string
  roleType: number
}

/**
 * 认证状态管理 Store
 *
 * 管理用户登录状态、Token 和用户信息
 *
 * @module stores/modules/auth.store
 */
export const useAuthStore = defineStore('auth', () => {
  /** JWT 认证令牌 */
  const token = ref<string>(localStorage.getItem('auth_token') || '')
  /** 用户ID */
  const userId = ref<number>(0)
  /** 用户名 */
  const username = ref<string>('')
  /** 角色类型：1=管理员，2=教师，3=学生 */
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

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)

  /** 角色标签 */
  const roleLabel = computed(() => {
    switch (roleType.value) {
      case 1: return 'admin'
      case 2: return 'teacher'
      case 3: return 'student'
      default: return ''
    }
  })

  /**
   * 设置认证信息
   *
   * @param t JWT 令牌
   * @param id 用户ID
   * @param name 用户名
   * @param role 角色类型
   */
  function setAuth(t: string, id: number, name: string, role: number) {
    token.value = t
    userId.value = id
    username.value = name
    roleType.value = role
    localStorage.setItem('auth_token', t)
    localStorage.setItem('auth_user', JSON.stringify({ userId: id, username: name, roleType: role }))
  }

  /** 退出登录 */
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
