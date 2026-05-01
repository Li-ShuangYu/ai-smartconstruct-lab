const trainingRoutes = [
  // 1. 常规带侧边栏的工作台路由组
  {
    path: '/training',
    name: 'TrainingRoot',
    // 外层容器：负责渲染毛玻璃白板和 Header
    component: () => import('@/components/layout/TrainingLayout/index.vue'),
    children: [
      {
        // 直接将路径写全，去掉中间那层没有意义的嵌套
        path: 'student-training/student-group-choose',
        name: 'StudentGroupChoose',
        // 直接渲染具体业务页面，不要再夹杂 Header 了
        component: () => import('@/views/training/studentTraining/StudentGroupChoose.vue'),
        meta: { title: '选择实训组别' }
      },
      {
        path: 'student-training/student-my-score-result',
        name: 'StudentMyScoreResult',
        component: () => import('@/views/training/studentTraining/StudentMyScoreResult.vue'),
        meta: { title: '我的实训成绩' }
      },
      {
        path: 'student-training/student-scheme-detail',
        name: 'StudentSchemeDetail',
        component: () => import('@/views/training/studentTraining/StudentSchemeDetail.vue'),    
        meta: { title: '实训方案详情' }
      },
      {
        path: 'student-training/student-scheme-upload',
        name: 'StudentSchemeUpload',
        component: () => import('@/views/training/studentTraining/StudentSchemeUpload.vue'),    
        meta: { title: '上传实训方案' }
      },
      {
        path: 'student-training/student-task-select',
        name: 'StudentTaskSelect',
        component: () => import('@/views/training/studentTraining/StudentTaskSelect.vue'),    
        meta: { title: '选择实训任务' }
      },
      {
        path: 'student-training/student-task-split',
        name: 'StudentTaskSplit',
        component: () => import('@/views/training/studentTraining/StudentTaskSplit.vue'),    
        meta: { title: '提交实训任务' }
      },
      {
        path: 'student-training/student-ai-generate',
        name: 'StudentAiGenerate',
        component: () => import('@/views/training/studentTraining/StudentAiGenerate.vue'),    
        meta: { title: '实训任务生成' }
      },
      {
        path: 'student-training/student-debug',
        name: 'StudentDebug',
        component: () => import('@/views/training/studentTraining/StudentDebug.vue'),    
        meta: { title: '调试机器人' }
      },
      {
        path: 'student-training/student-deploy',
        name: 'StudentDeploy',
        component: () => import('@/views/training/studentTraining/StudentDeploy.vue'),    
        meta: { title: '部署机器人' }
      },
      {
        path: 'teacher-training/teacher-scheme-split',
        name: 'TeacherSchemeSplit',
        component: () => import('@/views/training/teacherTraining/TeacherSchemeSplit.vue'),    
        meta: { title: '拆分实训任务' }
      },
      {
        path: 'teacher-training/teacher-simulation',
        name: 'TeacherSimulation',
        component: () => import('@/views/training/teacherTraining/TeacherSimulation.vue'),    
        meta: { title: '模拟实训任务' }
      },
      {
        path: 'teacher-training/teacher-group-choose',
        name: 'TeacherGroupChoose',
        component: () => import('@/views/training/teacherTraining/TeacherStudentGroup.vue'),    
        meta: { title: '选择实训组别' }
      },
      {
        path: 'teacher-training/teacher-task-publish',
        name: 'TeacherTaskPublish',
        component: () => import('@/views/training/teacherTraining/TeacherTaskPublish.vue'),    
        meta: { title: '发布实训任务' }
      },
      {
        path: 'teacher-training/teacher-task-split',
        name: 'TeacherTaskSplit',
        component: () => import('@/views/training/teacherTraining/TeacherTaskSplit.vue'),    
        meta: { title: '拆分实训任务' }
      },
      {
        path: 'teacher-training/teacher-ai-evaluate',
        name: 'TeacherAiEvaluate',
        component: () => import('@/views/training/teacherTraining/TeacherAiEvaluate.vue'),    
        meta: { title: '评估实训任务结果' }
      },
      {
        path: 'teacher-training/teacher-demand-split',
        name: 'TeacherDemandSplit',
        component: () => import('@/views/training/teacherTraining/TeacherDemandSplit.vue'),    
        meta: { title: '拆分实训任务需求' }
      },
      {
        path: 'teacher-training/teacher-demand-summary',
        name: 'TeacherDemandSummary',
        component: () => import('@/views/training/teacherTraining/TeacherDemandSummary.vue'),    
        meta: { title: '实训任务需求总结' }
      },
      {
        path: 'teacher-training/teacher-group-score-overview',
        name: 'TeacherGroupScoreOverview',
        component: () => import('@/views/training/teacherTraining/TeacherGroupScoreOverview.vue'),    
        meta: { title: '实训成绩总览' }
      },
      {
        path: 'teacher-training/teacher-scheme-detail',
        name: 'TeacherSchemeDetail',
        component: () => import('@/views/training/teacherTraining/TeacherSchemeDetail.vue'),    
        meta: { title: '实训方案详情' }
      },
    ]
  }
]

export default trainingRoutes;