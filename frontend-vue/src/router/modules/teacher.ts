import { RouteRecordRaw } from 'vue-router'

const teacherRoutes: Array<RouteRecordRaw> = [
  // 1. 常规带侧边栏的工作台路由组
  {
    path: '/teacher',
    name: 'TeacherRoot',
    component: () => import('@/components/layout/WorkbenchLayout/index.vue'),
    redirect: '/teacher/workbench',
    children: [
      {
        path: 'workbench',
        name: 'TeacherWorkbench',
        component: () => import('@/views/teacher/Workbench.vue'),
        meta: { title: '工作台概览' }
      },
      {
        path: 'training',
        name: 'TrainingManage',
        component: () => import('@/views/teacher/TrainingManage.vue'),
        meta: { title: '实训编排与管理' }
      }
      // ...保留原来的 assignment 和 evaluation
    ]
  },
  // 2. 【关键分离】：沉浸式编排页面，独立路由，不继承侧边栏布局
  {
    path: '/teacher/training/create',
    name: 'TrainingCreate',
    component: () => import('@/views/teacher/TrainingCreate.vue'),
    meta: { title: '创建实训编排 (沉浸舱)' }
  }
]

export default teacherRoutes;