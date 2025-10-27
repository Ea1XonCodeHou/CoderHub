import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue')
    },
    {
      path: '/home',
      name: 'home',
      component: () => import('@/views/Home.vue'),
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/Profile.vue'),
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/article/editor',
      name: 'article-editor',
      component: () => import('@/views/ArticleEditor.vue'),
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/article/:id',
      name: 'article-detail',
      component: () => import('@/views/ArticleDetail.vue'),
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/Dashboard.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = localStorage.getItem('userInfo')
  
  if (to.meta.requiresAuth) {
    if (!token || !userInfo) {
      // 未登录，跳转到登录页
      next('/')
    } else {
      // 已登录，检查角色权限
      const user = JSON.parse(userInfo)
      if (to.meta.role && user.role !== to.meta.role) {
        // 角色不匹配，跳转到对应首页
        if (user.role === 'ADMIN') {
          next('/dashboard')
        } else {
          next('/home')
        }
      } else {
        next()
      }
    }
  } else {
    // 不需要认证的页面（登录、注册）
    if (token && userInfo) {
      // 已登录用户访问登录页，重定向到对应首页
      const user = JSON.parse(userInfo)
      if (user.role === 'ADMIN') {
        next('/dashboard')
      } else {
        next('/home')
      }
    } else {
      next()
    }
  }
})

export default router

