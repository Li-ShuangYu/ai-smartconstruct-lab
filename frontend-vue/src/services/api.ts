import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

/**
 * HTTP 请求封装
 *
 * 基于 Axios 封装全局 HTTP 请求实例，提供统一的请求/响应拦截器
 *
 * @module services/api
 */
const http: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器
 *
 * 在请求发送前，从 localStorage 获取 JWT Token 并添加到请求头
 */
http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('auth_token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

/**
 * 响应拦截器
 *
 * 统一处理响应和错误：
 * - 401 响应时清除本地认证信息并跳转到登录页
 */
http.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_user')
      const currentPath = window.location.pathname
      if (!currentPath.startsWith('/auth')) {
        setTimeout(() => { window.location.href = '/auth/login' }, 1500)
      }
    }
    return Promise.reject(error)
  }
)

export default http
