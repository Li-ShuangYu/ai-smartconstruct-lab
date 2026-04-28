const authRoutes = [
  {
    path: '/auth',
    // 1. 父组件指向通用的鉴权布局页面
    name: 'authRoot',
    component: () => import('@/components/layout/AuthLayout/index.vue'),
    redirect: '/auth/login',
    children: [
      {
        path: 'login', // 默认渲染登录页
        name: 'Login',
        // 2. 子组件指向真正的登录卡片页面（注意路径是 views/auth ！）
        component: () => import('@/views/auth/Login.vue'),
        meta: { title: '登录系统' }
      },
      // 以后你的注册页就可以直接加在这里：
      // {
      //   path: '/register',
      //   name: 'Register',
      //   component: () => import('@/views/auth/Register.vue'),
      // }
    ]
  }
]

export default authRoutes;