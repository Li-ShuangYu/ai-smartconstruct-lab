const adminRoutes = [
  {
    path: '/admin',
    name: 'AdminRoot',
    component: () => import('@/components/layout/WorkbenchLayout/index.vue'),
    // 新增这一行：配置默认重定向
    redirect: '/admin/teacher', 
    children: [
      // === 用户中心 ===
      { path: 'teacher', name: 'AdminTeacher', component: () => import('@/views/admin/TeacherManage.vue') },
      { path: 'student', name: 'AdminStudent', component: () => import('@/views/admin/StudentManage.vue') },
      { path: 'menu', name: 'AdminMenu', component: () => import('@/views/admin/MenuManage.vue') },
      
      // === 教务中心 ===
      { path: 'org', name: 'AdminOrg', component: () => import('@/views/admin/OrgManage.vue') },
      { path: 'course', name: 'AdminCourse', component: () => import('@/views/admin/CourseManage.vue') },
      
      // === 资源中心 ===
      { path: 'node', name: 'AdminNode', component: () => import('@/views/admin/NodeManage.vue') },
      { path: 'template', name: 'AdminTemplate', component: () => import('@/views/admin/TemplateDashboard.vue') },
      { path: 'question', name: 'AdminQuestion', component: () => import('@/views/admin/QuestionDashboard.vue') },
      
      // === 系统中心 ===
      { path: 'ticket', name: 'AdminTicket', component: () => import('@/views/admin/TicketManage.vue') },
      { path: 'monitor', name: 'AdminMonitor', component: () => import('@/views/admin/ServiceMonitor.vue') },
      { path: 'audit', name: 'AdminAudit', component: () => import('@/views/admin/AuditLog.vue') }
    ]
  }
]
export default adminRoutes;