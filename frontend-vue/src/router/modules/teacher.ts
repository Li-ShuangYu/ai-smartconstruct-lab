import type { RouteRecordRaw } from 'vue-router'

const teacherRoutes: Array<RouteRecordRaw> = [
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
      },
      {
        path: 'training/create',
        name: 'TrainingCreate',
        component: () => import('@/views/teacher/TrainingCreate.vue'),
        meta: { title: '创建实训编排' } // 这个是下一步要死磕的页面
      },
      {
        path: 'assignment',
        name: 'AssignmentManage',
        component: () => import('@/views/teacher/AssignmentManage.vue'),
        meta: { title: '作业与题库' }
      },
      {
        path: 'evaluation',
        name: 'EvaluationManage',
        component: () => import('@/views/teacher/EvaluationManage.vue'),
        meta: { title: '多维学情评价' }
      }
    ]
  }
]

export default teacherRoutes;