import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 导入教师和学生模块路由
import teacherRoutes from './modules/teacher'
import studentRoutes from './modules/student'
import adminRoutes from './modules/admin'
import authRoutes from './modules/auth'



// NProgress 配置
NProgress.configure({ showSpinner: false, speed: 400 })

const routes = [
  {
    path: '/',
    redirect: '/auth/login'
  },
  ...teacherRoutes,
  ...studentRoutes,
  ...adminRoutes,
  ...authRoutes,
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

  // 1. 【核心逻辑】根据路径前缀动态判断角色并注入 meta
  // 这样 index.vue 就可以直接通过 route.meta.role 获取角色了
  if (to.path.startsWith('/student')) {
    to.meta.role = 'student'
  } else if (to.path.startsWith('/teacher')) {
    to.meta.role = 'teacher'
  }else if (to.path.startsWith('/admin')) {
    to.meta.role = 'admin'
  } else if (to.path.startsWith('/auth')) {
    to.meta.role = 'auth'
  }



  // 2. 动态设置页面标题
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