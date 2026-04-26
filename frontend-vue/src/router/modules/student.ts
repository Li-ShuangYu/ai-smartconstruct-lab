const studentRoutes = [
  // 1. 常规带侧边栏的工作台路由组
  {
    path: '/student',
    name: 'studentRoot',
    component: () => import('@/components/layout/WorkbenchLayout/index.vue'),
    redirect: '/student/workbench',
    children: [
      {
        path: 'workbench',
        name: 'studentWorkbench',
        component: () => import('@/views/student/Workbench.vue'),
        meta: { title: '工作台概览' }
      },
      {
        path: 'student-cabin',
        name: 'StudentCabin',
        component: () => import('@/views/student/StudentCabin.vue'),
        meta: { title: '学生舱位' , hideSidebar: true }
      }
      // ...保留原来的 assignment 和 evaluation
    ]
  },
  // 2. 【关键分离】：沉浸式编排页面，独立路由，不继承侧边栏布局
 
]

export default studentRoutes;