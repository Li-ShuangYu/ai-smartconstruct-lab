const teacherRoutes = [
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
        path: 'training-manage',
        name: 'TrainingManage',
        component: () => import('@/views/teacher/TrainingManage.vue'),
        meta: { title: '实训编排与管理' }
      },
      {
        path: 'class-course-manage',
        name: 'ClassCourseManage',
        component: () => import('@/views/teacher/ClassCourseManage.vue'),
        meta: { title: '班级实训管理与课程管理' }
      },
      // 2. 【关键分离】：沉浸式编排页面，独立路由，不继承侧边栏布局
      {
        path: 'training-create',
        name: 'TrainingCreate',
        component: () => import('@/views/teacher/TrainingCreate.vue'),
        meta: { title: '实训编排', hideSidebar: true }
      },
      {
        path: 'teacher-live-monitor',
        name: 'TeacherLiveMonitor',
        component: () => import('@/views/teacher/TeacherLiveMonitor.vue'),
        meta: { title: '直播监控', hideSidebar: true }
      },
      {
        path: 'training-publish',
        name: 'TrainingPublish',
        component: () => import('@/views/teacher/TrainingPublish.vue'),
        meta: { title: '发布实训', hideSidebar: true }
      },
      // {
      //   path: 'training-result',
      //   name: 'TrainingResult',
      //   component: () => import('@/views/teacher/TrainingResult.vue'),
      //   meta: { title: '实训结果', hideSidebar: true }
      // },
      // {
      //   path: 'class-competency/:studentId',
      //   name: 'ClassCompetencyProfile',
      //   component: () => import('@/views/teacher/ClassCompetencyProfile.vue'),
      //   meta: { title: '班级实训总结分析', hideSidebar: true }
      // },
      // {
      //   path: 'student-competency/:studentId',
      //   name: 'StudentCompetencyProfile',
      //   component: () => import('@/views/teacher/StudentCompetencyProfile.vue'),
      //   meta: { title: '学生实训总结评价', hideSidebar: true }
      // },
      // {
      //   path: 'evaluation',
      //   name: 'EvaluationDashboard',
      //   component: () => import('@/views/teacher/EvaluationDashboard.vue'),
      //   meta: { title: '评估分析'}
      // },
      // ...保留原来的 assignment 和 evaluation
    ]
  }
]

export default teacherRoutes;