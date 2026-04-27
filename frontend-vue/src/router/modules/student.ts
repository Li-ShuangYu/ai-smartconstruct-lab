const studentRoutes = [
  {
    path: '/student',
    name: 'studentRoot',
    // 确保这里的路径指向我们刚才修改好的包含过渡动画的 index.vue
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
        // 关键配置 1：加上 /:id，用于传递实训任务 ID
        path: 'student-cabin/:id',
        name: 'StudentCabin',
        component: () => import('@/views/student/StudentCabin.vue'),
        meta: { title: '学生舱位'}
      },
      {
        // 关键配置 2：实训详情（只读态）
        path: 'training-detail/:id',
        name: 'StudentTrainingDetail',
        component: () => import('@/views/student/TrainingDetail.vue'), // 需新建此文件
        meta: { title: '实训详情'}
      },
      {
        // 占位：我的班级
        path: 'my-class',
        name: 'StudentMyClass',
        component: () => import('@/views/student/MyClass.vue'), // 需新建此文件
        meta: { title: '我的班级'}
      },
      {
        // 占位：我的课程
        path: 'courselist',
        name: 'StudentCourseList',
        component: () => import('@/views/student/CourseList.vue'), // 需新建此文件
        meta: { title: '我的课程'}
      },
      {
        // 占位：通知
        path: 'notifications',
        name: 'StudentNotifications',
        component: () => import('@/views/student/Notifications.vue'), // 需新建此文件
        meta: { title: '消息通知'}
      },
      {
        // 占位：个人中心
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/UserProfile.vue'), // 需新建此文件
        meta: { title: '个人中心'}
      }
    ]
  }
]

export default studentRoutes;