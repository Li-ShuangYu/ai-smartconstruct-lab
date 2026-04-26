import { createRouter, createWebHistory } from 'vue-router'
// 导入我们之前写的教师模块路由
import teacherRoutes from './modules/teacher'
import studentRoutes from './modules/student'

// 聚合所有路由
const routes = [
  // 根路径访问时，直接跳转到教师工作台（为了目前演示方便）
  {
    path: '/',
    redirect: '/teacher/workbench'
  },
  // 展开教师模块路由
  ...teacherRoutes,
  // 展开学生模块路由
  ...studentRoutes,
  
  // 预留一个简单的 404 捕获
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/common/404.vue') // 确保这个文件存在或先注释掉
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 这里预留了你规划的路由守卫入口
// import './guard' 

export default router