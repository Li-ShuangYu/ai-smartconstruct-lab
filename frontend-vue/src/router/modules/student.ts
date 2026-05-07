const studentRoutes = [
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
        meta: { title: '学生工作台' }
      },
      {
        path: 'student-cabin/:id',
        name: 'StudentCabin',
        component: () => import('@/views/student/StudentCabin.vue'),
        meta: { title: '学生舱位' }
      },
      {
        path: 'training-detail/:id',
        name: 'StudentTrainingDetail',
        component: () => import('@/views/student/TrainingDetail.vue'),
        meta: { title: '实训详情' }
      },
      {
        path: 'my-class',
        name: 'StudentMyClass',
        component: () => import('@/views/student/MyClass.vue'),
        meta: { title: '我的班级' }
      },
      {
        path: 'courselist',
        name: 'StudentCourseList',
        component: () => import('@/views/student/CourseList.vue'),
        meta: { title: '我的课程' }
      },
      {
        path: 'notifications',
        name: 'StudentNotifications',
        component: () => import('@/views/student/Notifications.vue'),
        meta: { title: '消息通知' }
      },
      {
        path: 'training-preview',
        name: 'StudentTrainingPreview',
        component: () => import('@/views/student/TrainingPreview.vue'),
        meta: { title: '实训预览' }
      },
      {
        path: 'training-execute',
        name: 'StudentTrainingExecute',
        component: () => import('@/views/student/TrainingExecute.vue'),
        meta: { title: '实训执行' }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/UserProfile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

export default studentRoutes;
