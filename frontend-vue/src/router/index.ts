import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import '../assets/main.css'
import '../assets/base.css'
// 导入教师和学生模块路由
import teacherRoutes from './modules/teacher'
import studentRoutes from './modules/student'
import adminRoutes from './modules/admin'
import authRoutes from './modules/auth'
import trainingRoutes from './modules/training'



// NProgress 配置
NProgress.configure({ showSpinner: false, speed: 400 })

const routes = [
  {
    path: '/',
    redirect: '/auth/login',
    meta: { title: '登录' }
  },
  ...teacherRoutes,
  ...studentRoutes,
  ...adminRoutes,
  ...authRoutes,
  ...trainingRoutes,
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/common/404.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局前置守卫：统一处理角色判断、进度条和标题
 */
router.beforeEach((to, from) => {
  NProgress.start()

  const token = localStorage.getItem('auth_token')
  const isAuthPage = to.path.startsWith('/auth')

  // 1. 认证检查：非认证页面需要有效 Token
  if (!isAuthPage && !token) {
    NProgress.done()
    return { path: '/auth/login', query: { redirect: to.fullPath } }
  }

  // 2. 已登录用户访问认证页，直接跳转对应首页
  if (isAuthPage && token) {
    try {
      const userStr = localStorage.getItem('auth_user')
      if (userStr) {
        const user = JSON.parse(userStr)
        const roleMap: Record<number, string> = { 1: '/admin', 2: '/teacher/workbench', 3: '/student/workbench' }
        const target = roleMap[user.roleType] || '/student/workbench'
        if (to.path === '/auth/login' || to.path === '/auth/register' || to.path === '/auth') {
          NProgress.done()
          return target
        }
      }
    } catch { /* ignore parse errors */ }
  }

  // 3. 【核心逻辑】根据路径前缀动态判断角色并注入 meta
  if (to.path.startsWith('/student')) {
    to.meta.role = 'student'
  } else if (to.path.startsWith('/teacher')) {
    to.meta.role = 'teacher'
  }else if (to.path.startsWith('/admin')) {
    to.meta.role = 'admin'
  } else if (to.path.startsWith('/auth')) {
    to.meta.role = 'auth'
  } else if (to.path.startsWith('/training')) {
    to.meta.role = 'training'
  }

  // 4. 动态设置页面标题
  const title = to.meta.title ? `${to.meta.title} - AI学苑` : 'AI学苑'
  document.title = title

  return true
})

router.afterEach(() => {
  // 配合 Layout 里的过度动画，延迟关闭进度条效果更丝滑
  setTimeout(() => {
    NProgress.done()
  }, 200)
})

export default router