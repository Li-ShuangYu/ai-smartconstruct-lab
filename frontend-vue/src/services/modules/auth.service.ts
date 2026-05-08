import http from '@/services/api'
import type { LoginRequest, RegisterRequest, AuthResponse, ApiResult } from '@/services/types/auth.types'

/**
 * 用户认证服务
 *
 * 提供登录、注册、获取用户信息等接口调用
 *
 * @module services/modules/auth.service
 */

/**
 * 用户登录
 *
 * @param data 登录请求参数（用户名、密码、角色类型）
 * @returns Promise<ApiResult<AuthResponse>> 包含Token和用户信息的响应
 */
export function login(data: LoginRequest): Promise<ApiResult<AuthResponse>> {
  return http.post('/api/auth/login', data).then(res => res.data)
}

/**
 * 用户注册
 *
 * @param data 注册请求参数
 * @returns Promise<ApiResult<void>> 注册结果
 */
export function register(data: RegisterRequest): Promise<ApiResult<void>> {
  return http.post('/api/auth/register', data).then(res => res.data)
}

/**
 * 获取当前用户信息
 *
 * @returns Promise<ApiResult<AuthResponse>> 当前用户信息
 */
export function userinfo(): Promise<ApiResult<AuthResponse>> {
  return http.get('/api/auth/userinfo').then(res => res.data)
}
